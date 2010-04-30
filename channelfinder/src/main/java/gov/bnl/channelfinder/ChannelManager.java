/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.bnl.channelfinder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import javax.ws.rs.core.MultivaluedMap;
import javax.xml.ws.WebServiceException;

/**
 *
 * @author rlange
 */
public class ChannelManager {

    private static ChannelManager instance = new ChannelManager();

    /**
     * Create an instance of ChannelManager
     */
    private ChannelManager() {
    }

    /**
     * Returns the (singleton) instance of ChannelManager
     * @return the instance of ChannelManager
     */
    public static ChannelManager getInstance() {
        return instance;
    }

    /**
     * Merges XmlProperties and XmlTags of two channels in place
     * @param dest destination channel
     * @param src source channel
     */
    public static void mergeXmlChannels(XmlChannel dest, XmlChannel src) {
        src_props:
        for (XmlProperty s : src.getXmlProperties()) {
            for (XmlProperty d : dest.getXmlProperties()) {
                if (d.getName().equals(s.getName())) {
                    d.setValue(s.getValue());
                    continue src_props;
                }
            }
            dest.getXmlProperties().add(s);
        }
        src_tags:
        for (XmlTag s : src.getXmlTags()) {
            for (XmlTag d : dest.getXmlTags()) {
                if (d.getName().equals(s.getName())) {
                    continue src_tags;
                }
            }
            dest.getXmlTags().add(s);
        }
    }

    /**
     * Find owner of a given property <tt>name</tt> in a collection of
     * XmlProperty <tt>data</tt> and check for consistency.
     * @param owner ownership to check against, will be set if null and property is found
     * @param data XmlProperty collection
     * @param name name of property to search
     */
    public static void findAndCheckPropertyOwner(String owner, Collection<XmlProperty> data, String name) {
        for (XmlProperty p : data) {
            if (p.getName().equals(name)) {
                if (owner == null) {
                    owner = p.getOwner();
                } else if (!owner.equals(p.getOwner())) {
                    throw new WebServiceException("Inconsistent owner for property " + name);
                }
            }
        }
    }

    /**
     * Find owner of a given tag <tt>name</tt> in a collection of XmlTag data.
     * @param owner ownership to check against, will be set if null and tag is found
     * @param data XmlTag collection
     * @param name name of tag to search
     */
    public static void findAndCheckTagOwner(String owner, Collection<XmlTag> data, String name) {
        for (XmlTag t : data) {
            if (t.getName().equals(name)) {
                if (owner == null) {
                    owner = t.getOwner();
                } else if (!owner.equals(t.getOwner())) {
                    throw new WebServiceException("Inconsistent owner for tag " + name);
                }
            }
        }
    }

    /**
     * Find owner of a given entity <tt>name</tt> (channel/property/tag)
     * in XmlChannels <tt>data</tt> and checks for consistency.
     * @param data XmlChannels collection
     * @param name name of entity to search
     * @return owner of the entity, null if entity not found
     */
    public static String findAndCheckOwner(XmlChannels data, String name) {
        String owner = null;
        for (XmlChannel ch : data.getChannels()) {
            if (ch.getName() != null && ch.getName().equals(name)) {
                return ch.getOwner();
            }
            findAndCheckPropertyOwner(owner, ch.getXmlProperties(), name);
            findAndCheckTagOwner(owner, ch.getXmlTags(), name);
        }
        return owner;
    }

    /**
     * Find owner of a given entity <tt>name</tt> (channel/property/tag)
     * in a single XmlChannel <tt>data</tt>.
     * @param data XmlChannel object
     * @param name name of tag to search
     * @return owner of the tag, null if tag was not found
     */
    public static String findAndCheckOwner(XmlChannel data, String name) {
        XmlChannels chans = new XmlChannels();
        chans.addChannel(data);
        return findAndCheckOwner(chans, name);
    }

    /**
     * Return properties and tags found by their names.
     * @param names collection of strings: names to search
     * @return unnamed XmlChannel container with found properties and tags
     * @throws SQLException
     */
    public XmlChannel findPropertiesByName(Collection<String> names) throws SQLException {
        XmlChannel xmlChan = new XmlChannel();
        FindPropertiesQuery query = FindPropertiesQuery.createFindPropertiesQuery(names);
        ResultSet rs = query.executeQuery(DbConnection.getInstance().getConnection());

        while (rs.next()) {
            if (rs.getString("value") == null) {
                xmlChan.addTag(new XmlTag(rs.getString("property"), rs.getString("owner")));
            } else {
                xmlChan.addProperty(new XmlProperty(rs.getString("property"),
                        rs.getString("owner"), rs.getString("value")));
            }
        }
        return xmlChan;
    }

    /**
     * Return properties and tags found by a single name.
     * @param name name to look for
     * @return unnamed XmlChannel container with found properties and tags
     * @throws SQLException
     */
    public XmlChannel findPropertiesByName(String name) throws SQLException {
        return findPropertiesByName(Collections.singleton(name));
    }

    /**
     * Adds a property or tag to an XmlChannel.
     *
     */
    private static void addProperty(XmlChannel c, ResultSet rs) throws SQLException {
        if (rs.getString("property") != null) {
            if (rs.getString("value") == null) {
                c.addTag(new XmlTag(rs.getString("property"), rs.getString("owner")));
            } else {
                c.addProperty(new XmlProperty(rs.getString("property"),
                        rs.getString("owner"), rs.getString("value")));
            }
        }
    }

    /**
     * Return single channel found by channel name.
     * @param name name to look for
     * @return XmlChannel with found channel and its properties
     * @throws SQLException
     */
    public XmlChannel findChannelByName(String name) throws SQLException {
        XmlChannel xmlChan = null;
        FindChannelsQuery query = FindChannelsQuery.createSingleChannelMatchQuery(name);
        ResultSet rs = query.executeQuery(DbConnection.getInstance().getConnection());

        while (rs.next()) {
            String thischan = rs.getString("channel");
            if (rs.isFirst()) {
                xmlChan = new XmlChannel(thischan, rs.getString("cowner"));
            }
            addProperty(xmlChan, rs);
        }
        return xmlChan;
    }

    /**
     * Returns channels found by matching property/tag values and/or channel names.
     * @param query query to be used for matching
     * @return XmlChannels container with all found channels and their properties/tags
     */
    private XmlChannels findChannelsByMatch(FindChannelsQuery query) throws SQLException {
        XmlChannels xmlChans = new XmlChannels();
        XmlChannel xmlChan = null;
        ResultSet rs = query.executeQuery(DbConnection.getInstance().getConnection());

        String lastchan = "";
        while (rs.next()) {
            String thischan = rs.getString("channel");
            if (!thischan.equals(lastchan) || rs.isFirst()) {
                xmlChan = new XmlChannel(thischan, rs.getString("cowner"));
                xmlChans.addChannel(xmlChan);
                lastchan = thischan;
            }
            addProperty(xmlChan, rs);
        }
        return xmlChans;
    }

    /**
     * Return channels found by matching the channel name.
     * @param matches collection of channel name patterns to match
     * @return XmlChannels container with all found channels and their properties
     * @throws SQLException
     */
    public XmlChannels findChannelsByNameMatch(Collection<String> matches) throws SQLException {
        FindChannelsQuery query = FindChannelsQuery.createChannelMatchQuery(matches);
        return findChannelsByMatch(query);
    }

    /**
     * Return channels found by matching tags against a collection of name patterns.
     * @param matches collection of name patterns to match
     * @return XmlChannels container with all found channels and their properties
     * @throws SQLException
     */
    public XmlChannels findChannelsByTagMatch(Collection<String> matches) throws SQLException {
        FindChannelsQuery query = FindChannelsQuery.createTagMatchQuery(matches);
        return findChannelsByMatch(query);
    }

    /**
     * Return channels found by matching tags against a single name pattern.
     * @param name tag name pattern to match
     * @return XmlChannels container with all found channels and their properties
     * @throws SQLException
     */
    public XmlChannels findChannelsByTag(String name) throws SQLException {
        FindChannelsQuery query = FindChannelsQuery.createSingleTagMatchQuery(name);
        return findChannelsByMatch(query);
    }

    /**
     * Returns channels found by matching property values, tag names, channel names.
     * @param matches multivalued map of property, tag, channel names and patterns to match
     * their values against.
     * @return XmlChannels container with all found channels and their properties
     * @throws SQLException
     */
    public XmlChannels findChannelsByMultiMatch(MultivaluedMap<String, String> matches) throws SQLException {
        FindChannelsQuery query = FindChannelsQuery.createMultiMatchQuery(matches);
        return findChannelsByMatch(query);
    }

    /**
     * Deletes a channel identified by <tt>name</tt>.
     * @param name channel to delete
     * @throws SQLException
     */
    public void deleteChannel(String name) throws SQLException {
        DeleteChannelQuery dq = new DeleteChannelQuery(name);
        dq.executeQuery(DbConnection.getInstance().getConnection());
    }

    /**
     * Deletes a tag identified by <tt>name</tt> from all channels.
     * @param tag tag to delete
     * @throws SQLException
     */
    public void deleteTag(String tag) throws SQLException {
        DeleteTagQuery dq = new DeleteTagQuery(tag);
        dq.executeQuery(DbConnection.getInstance().getConnection());
    }

    /**
     * Deletes a tag identified by <tt>name</tt> from a single channel.
     * @param tag tag to delete
     * @param chan channel to delete it from
     * @throws SQLException
     */
    public void deleteSingleTag(String tag, String chan) throws SQLException {
        DeleteTagQuery dq = new DeleteTagQuery(tag, chan);
        dq.executeQuery(DbConnection.getInstance().getConnection());
    }

    /**
     * Asserts the owner for the tag/property <tt>name</tt> found in the database
     * matches the specified <tt>owner</tt>. If <tt>owner</tt> is <tt>null</tt>,
     * the owner found in the database is returned.
     * @param name property/tag to test
     * @param owner owner to test against
     */
    private String assertOrGetOwner(String name, String owner) throws SQLException {
        // retrieve tag owner from database
        XmlChannel chan = findPropertiesByName(name);
        String dbowner = findAndCheckOwner(chan, name);
        if (owner == null) {
            owner = dbowner;
        }

        // throw if no owner from database and not specified
        if (owner == null) {
            throw new WebServiceException("No owner specified for new property/tag " + name);
        }

        // throw if specified and existing owner exist and do not match
        if (owner != null && dbowner != null && !owner.equals(dbowner)) {
            throw new WebServiceException("Specified owner " + owner
                    + " and existing owner " + dbowner + " for " + name + " do not match");
        }

        return owner;
    }

    /**
     * Add the tag identified by <tt>tag</tt> and <tt>owner</tt> to the channels
     * specified in the XmlChannels <tt>data</tt>.
     * @param tag tag to add
     * @param owner owner for new tag
     * @param data XmlChannels container with all channels to add tag to
     * @throws SQLException
     */
    public void addTag(String tag, String owner, XmlChannels data) throws SQLException {
        owner = assertOrGetOwner(tag, owner);
        AddTagQuery q = new AddTagQuery(tag, owner, data);
        q.executeQuery(DbConnection.getInstance().getConnection());
    }

    /**
     * Add the tag identified by <tt>tag</tt> and <tt>owner</tt> <b>exclusively</b>
     * to the channels specified in the XmlChannels <tt>data</tt>.
     * @param tag tag to add
     * @param owner owner for new tag
     * @param data XmlChannels container with all channels to add tag to
     * @throws SQLException
     */
    public void putTag(String tag, String owner, XmlChannels data) throws SQLException {
        owner = assertOrGetOwner(tag, owner);
        DeleteTagQuery dq = new DeleteTagQuery(tag);
        AddTagQuery aq = new AddTagQuery(tag, owner, data);
        dq.executeQuery(DbConnection.getInstance().getConnection());
        aq.executeQuery(DbConnection.getInstance().getConnection());
    }

    /**
     * Add the tag identified by <tt>tag</tt> and <tt>owner</tt>
     * to the single channel <tt>channel</tt>.
     * @param tag tag to add
     * @param owner owner for new tag
     * @param channel 
     * @param data XmlChannel to add tag to
     * @throws SQLException
     */
    public void addSingleTag(String tag, String owner, String channel, XmlChannel data) throws SQLException {
        owner = assertOrGetOwner(tag, owner);
        if (!channel.equals(data.getName())) {
            throw new WebServiceException("Specified channel name " + channel
                    + " and payload channel name " + data.getName() + " do not match");
        }

        AddTagQuery aq = new AddTagQuery(tag, owner, data);
        aq.executeQuery(DbConnection.getInstance().getConnection());
    }

    /**
     * Update a channel identified by <tt>name</tt>, creating it when necessary.
     * The property set in <tt>data</tt> has to be complete, i.e. the existing channel properties
     * are <b>replaced</b> with the properties in <tt>data</tt>.
     * @param name channel to update
     * @param data XmlChannel data
     * @throws SQLException
     */
    public void updateChannel(String name, XmlChannel data) throws SQLException {
        if (!name.equals(data.getName())) {
            throw new WebServiceException("Channel name from URL (" + name
                    + ") and data (" + data.getName() + ") do not match");
        }
        /* FIXME: Check for property/tag owner integrity */
        DeleteChannelQuery dq = new DeleteChannelQuery(name);
        CreateChannelQuery cq = new CreateChannelQuery(data);
        dq.executeQuery(DbConnection.getInstance().getConnection());
        cq.executeQuery(DbConnection.getInstance().getConnection());
    }

    /**
     * Create channels specified in <tt>data</tt>.
     * @param data XmlChannels data
     * @throws SQLException
     */
    public void createChannels(XmlChannels data) throws SQLException {
        for (XmlChannel chan : data.getChannels()) {
            createChannel(chan);
        }
    }

    /**
     * Create a new channel using the property set in <tt>data</tt>.
     * @param data XmlChannel data
     * @throws SQLException
     */
    public void createChannel(XmlChannel data) throws SQLException {
        CreateChannelQuery q = new CreateChannelQuery(data);
        q.executeQuery(DbConnection.getInstance().getConnection());
    }

    /**
     * Merge property set in <tt>data</tt> into the existing channel <tt>name</tt>.
     * @param name channel to merge the properties and tags into
     * @param data XmlChannel data containing properties and tags
     * @throws SQLException
     */
    public void mergeChannel(String name, XmlChannel data) throws SQLException {
        XmlChannel dest = findChannelByName(name);
        mergeXmlChannels(dest, data);
        updateChannel(name, dest);
    }
}
