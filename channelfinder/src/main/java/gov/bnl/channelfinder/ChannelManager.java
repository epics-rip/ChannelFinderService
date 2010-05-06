/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.bnl.channelfinder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
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
     *
     * @return the instance of ChannelManager
     */
    public static ChannelManager getInstance() {
        return instance;
    }

    /**
     * Merges XmlProperties and XmlTags of two channels in place
     *
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
     *
     * @param name name to look for
     * @return XmlChannel with found channel and its properties
     * @throws CFException on SQLException
     */
    public XmlChannel findChannelByName(String name) throws CFException {
        XmlChannel xmlChan = null;
        FindChannelsQuery query = FindChannelsQuery.createSingleChannelMatchQuery(name);

        try {
            ResultSet rs = query.executeQuery(DbConnection.getInstance().getConnection());

            while (rs.next()) {
                String thischan = rs.getString("channel");
                if (rs.isFirst()) {
                    xmlChan = new XmlChannel(thischan, rs.getString("cowner"));
                }
                addProperty(xmlChan, rs);
            }

        } catch (SQLException e) {
            throw new CFException(Response.Status.INTERNAL_SERVER_ERROR,
                    "SQL Exception during channel search request", e);
        }
        return xmlChan;
    }

    /**
     * Returns channels found by matching property/tag values and/or channel names.
     *
     * @param query query to be used for matching
     * @return XmlChannels container with all found channels and their properties/tags
     */
    private XmlChannels findChannelsByMatch(FindChannelsQuery query) throws CFException {
        XmlChannels xmlChans = new XmlChannels();
        XmlChannel xmlChan = null;
        try {
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
        } catch (SQLException e) {
            throw new CFException(Response.Status.INTERNAL_SERVER_ERROR,
                    "SQL Exception during find channels request", e);
        }
    }

    /**
     * Return channels found by matching the channel name.
     *
     * @param matches collection of channel name patterns to match
     * @return XmlChannels container with all found channels and their properties
     * @throws CFException wrapping an SQLException
     */
    public XmlChannels findChannelsByNameMatch(Collection<String> matches) throws CFException {
        FindChannelsQuery query = FindChannelsQuery.createChannelMatchQuery(matches);
        return findChannelsByMatch(query);
    }

    /**
     * Return channels found by matching tags against a collection of name patterns.
     *
     * @param matches collection of name patterns to match
     * @return XmlChannels container with all found channels and their properties
     * @throws CFException wrapping an SQLException
     */
    public XmlChannels findChannelsByTagMatch(Collection<String> matches) throws CFException {
        FindChannelsQuery query = FindChannelsQuery.createTagMatchQuery(matches);
        return findChannelsByMatch(query);
    }

    /**
     * Return channels found by matching tags against a single name pattern.
     *
     * @param name tag name pattern to match
     * @return XmlChannels container with all found channels and their properties
     * @throws CFException wrapping an SQLException
     */
    public XmlChannels findChannelsByTag(String name) throws CFException {
        FindChannelsQuery query = FindChannelsQuery.createSingleTagMatchQuery(name);
        return findChannelsByMatch(query);
    }

    /**
     * Returns channels found by matching property values, tag names, channel names.
     *
     * @param matches multivalued map of property, tag, channel names and patterns to match
     * their values against.
     * @return XmlChannels container with all found channels and their properties
     * @throws CFException wrapping an SQLException
     */
    public XmlChannels findChannelsByMultiMatch(MultivaluedMap<String, String> matches) throws CFException {
        FindChannelsQuery query = FindChannelsQuery.createMultiMatchQuery(matches);
        return findChannelsByMatch(query);
    }

    /**
     * Deletes a channel identified by <tt>name</tt>.
     *
     * @param name channel to delete
     * @throws CFException wrapping an SQLException
     */
    public void deleteChannel(String name) throws CFException {
        DeleteChannelQuery dq = new DeleteChannelQuery(name);
        dq.executeQuery(DbConnection.getInstance().getConnection());
    }

    /**
     * Deletes a tag identified by <tt>name</tt> from all channels.
     *
     * @param tag tag to delete
     * @throws CFException wrapping an SQLException
     */
    public void deleteTag(String tag) throws CFException {
        DeleteTagQuery dq = new DeleteTagQuery(tag);
        dq.executeQuery(DbConnection.getInstance().getConnection());
    }

    /**
     * Deletes a tag identified by <tt>name</tt> from a single channel.
     *
     * @param tag tag to delete
     * @param chan channel to delete it from
     * @throws CFException wrapping an SQLException
     */
    public void deleteSingleTag(String tag, String chan) throws CFException {
        DeleteTagQuery dq = new DeleteTagQuery(tag, chan);
        dq.executeQuery(DbConnection.getInstance().getConnection());
    }

    /**
     * Add the tag identified by <tt>tag</tt> and <tt>owner</tt> to the channels
     * specified in the XmlChannels <tt>data</tt>.
     *
     * @param tag tag to add
     * @param data XmlChannels container with all channels to add tag to
     * @throws CFException on ownership mismatch, or wrapping an SQLException
     */
    public void addTag(String tag, XmlChannels data) throws CFException {
        String owner = DbOwnerMap.getInstance().enforcedPropertyOwner(tag, data);
        if (owner == null) {
            throw new CFException(Response.Status.BAD_REQUEST,
                    "Tag ownership for " + tag + " undefined in db and payload");
        }
        AddTagQuery q = new AddTagQuery(tag, owner, data);
        q.executeQuery(DbConnection.getInstance().getConnection());
    }

    /**
     * Add the tag identified by <tt>tag</tt> and <tt>owner</tt> <b>exclusively</b>
     * to the channels specified in the XmlChannels <tt>data</tt>.
     *
     * @param tag tag to add
     * @param data XmlChannels container with all channels to add tag to
     * @throws CFException on ownership mismatch, or wrapping an SQLException
     */
    public void putTag(String tag, XmlChannels data) throws CFException {
        String owner = DbOwnerMap.getInstance().enforcedPropertyOwner(tag, data);
        if (owner == null) {
            throw new CFException(Response.Status.BAD_REQUEST,
                    "Tag ownership for " + tag + " undefined in db and payload");
        }
        DeleteTagQuery dq = new DeleteTagQuery(tag);
        AddTagQuery aq = new AddTagQuery(tag, owner, data);
        dq.executeQuery(DbConnection.getInstance().getConnection());
        aq.executeQuery(DbConnection.getInstance().getConnection());
    }

    /**
     * Add the tag identified by <tt>tag</tt> and <tt>owner</tt>
     * to the single channel <tt>channel</tt>.
     *
     * @param tag tag to add
     * @param channel 
     * @param data XmlChannel to add tag to
     * @throws CFException on ownership mismatch, or wrapping an SQLException
     */
    public void addSingleTag(String tag, String channel, XmlChannel data) throws CFException {
        String owner = DbOwnerMap.getInstance().enforcedPropertyOwner(tag, new XmlChannels(data));
        if (owner == null) {
            throw new CFException(Response.Status.BAD_REQUEST,
                    "Tag ownership for " + tag + " undefined in db and payload");
        }
        if (!channel.equals(data.getName())) {
            throw new CFException(Response.Status.BAD_REQUEST,
                    "Specified channel name " + channel +
                    " and payload channel name " + data.getName() + " do not match");
        }
        AddTagQuery aq = new AddTagQuery(tag, owner, data);
        aq.executeQuery(DbConnection.getInstance().getConnection());
    }

    /**
     * Update a channel identified by <tt>name</tt>, creating it when necessary.
     * The property set in <tt>data</tt> has to be complete, i.e. the existing channel properties
     * are <b>replaced</b> with the properties in <tt>data</tt>.
     *
     * @param name channel to update
     * @param data XmlChannel data
     * @throws CFException on ownership or name mismatch, or wrapping an SQLException
     */
    public void updateChannel(String name, XmlChannel data) throws CFException {
        if (!name.equals(data.getName())) {
            throw new CFException(Response.Status.BAD_REQUEST,
                    "Specified channel name " + name +
                    " and payload channel name " + data.getName() + " do not match");
        }
        DbConnection db = DbConnection.getInstance();
        if (DbOwnerMap.getInstance().matchesOwnersIn(data)) {
            DeleteChannelQuery dq = new DeleteChannelQuery(name);
            CreateChannelQuery cq = new CreateChannelQuery(data);
            dq.executeQuery(db.getConnection());
            cq.executeQuery(db.getConnection());
        }
    }

    /**
     * Create channels specified in <tt>data</tt>.
     *
     * @param data XmlChannels data
     * @throws CFException on ownership mismatch, or wrapping an SQLException
     */
    public void createChannels(XmlChannels data) throws CFException {
        for (XmlChannel chan : data.getChannels()) {
            createChannel(chan);
        }
    }

    /**
     * Create a new channel using the property set in <tt>data</tt>.
     *
     * @param data XmlChannel data
     * @throws CFException on ownership or name mismatch, or wrapping an SQLException
     */
    public void createChannel(XmlChannel data) throws CFException {
        CreateChannelQuery q = new CreateChannelQuery(data);
        q.executeQuery(DbConnection.getInstance().getConnection());
    }

    /**
     * Merge property set in <tt>data</tt> into the existing channel <tt>name</tt>.
     *
     * @param name channel to merge the properties and tags into
     * @param data XmlChannel data containing properties and tags
     * @throws CFException on name or owner mismatch, or wrapping an SQLException
     */
    public void mergeChannel(String name, XmlChannel data) throws CFException {
        XmlChannel dest = findChannelByName(name);
        mergeXmlChannels(dest, data);
        updateChannel(name, dest);
    }
}
