/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.bnl.channelfinder;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Collection;
import javax.ws.rs.core.MultivaluedMap;
import javax.xml.ws.WebServiceException;

/**
 *
 * @author rlange
 */
public class ChannelManager {
    private static ChannelManager instance = new ChannelManager();
    private ThreadLocal<Connection> con = new ThreadLocal<Connection>();

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
     * Finds owner of a given tag <tt>tag</tt> in XmlChannels data
     * @param data XmlChannels object
     * @param tag name of tag to search
     * @return owner of the tag, null if tag was not found
     */
    public static String findTagOwner(XmlChannels data, String tag) {
        String owner = null;
        for (XmlChannel ch : data.getChannels()) {
            for (XmlTag t : ch.getXmlTags()) {
                if (owner == null) owner = t.getOwner();
                else if (!owner.equals(t.getOwner())) {
                    throw new WebServiceException("Inconsistent owner in payload for tag " + tag);
                }
            }
        }
        return owner;
    }

    /**
     * Finds owner of a given tag <tt>tag</tt> in a single XmlChannel <tt>data</tt>
     * @param data XmlChannel object
     * @param tag name of tag to search
     * @return owner of the tag, null if tag was not found
     */
    public static String findTagOwner(XmlChannel data, String tag) {
        XmlChannels chans = new XmlChannels();
        chans.addChannel(data);
        return findTagOwner(chans, tag);
    }

    /**
     * Begins a transaction by establishing a connection and beginning the transaction
     */
    public void begin() {
        try {
            con.set(DbConnection.getInstance().getConnection());
            con.get().setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            con.get().setAutoCommit(false);
        } catch (Exception e) {
            throw new WebServiceException("Cannot establish database connection", e);
        }
    }

    /**
     * Ends a transaction by committing and closing the connection
     */
    public void commit() {
        try {
            con.get().commit();
            con.get().close();
        } catch (Exception e) {
            throw new WebServiceException("Could not commit the requested changes to the database", e);
        }
    }

    /**
     * Ends a transaction by rolling back and closing the connection
     */
    public void rollback() {
        try {
            con.get().rollback();
            con.get().close();
        } catch (Exception e) {
            throw new WebServiceException("Could not roll back changes to the database", e);
        }
    }

    /**
     * Return single channel found by channel name.
     * @param name name to look for
     * @return XmlChannel with found channel and its properties
     */
    public XmlChannel findChannelByName(String name) {
        MatchQuery query = MatchQuery.createSingleChannelMatchQuery(name);
        try {
            con.set(DbConnection.getInstance().getConnection());
            ResultSet rs = query.executeQuery(con.get());

            String lastchan = "";
            XmlChannel xmlChan = null;

            while (rs.next()) {
                String thischan = rs.getString("channel");
                if (rs.isFirst()) {
                    xmlChan = new XmlChannel(thischan, rs.getString("cowner"));
                    lastchan = thischan;
                }
                if (rs.getString("value") == null)
                    xmlChan.addTag(new XmlTag(rs.getString("property"), rs.getString("owner")));
                else
                    xmlChan.addProperty(new XmlProperty(rs.getString("property"),
                        rs.getString("owner"), rs.getString("value")));
            }
            con.get().close();
            return xmlChan;
        } catch (Exception e) {
            throw new WebServiceException("SQL Error during channel find operation", e);
        }

    }

    /**
     * Returns channels found by matching property/tag values and/or channel names.
     * @param query the query to be used for matching
     * @return XmlChannels container with all found channels and their properties/tags
     */
    private XmlChannels findChannelsByMatch(MatchQuery query) {
        try {
            con.set(DbConnection.getInstance().getConnection());
            ResultSet rs = query.executeQuery(con.get());

            String lastchan = "";
            XmlChannels xmlChans = new XmlChannels();
            XmlChannel xmlChan = null;

            while (rs.next()) {
                String thischan = rs.getString("channel");
                if (!thischan.equals(lastchan) || rs.isFirst()) {
                    xmlChan = new XmlChannel(thischan, rs.getString("cowner"));
                    xmlChans.addChannel(xmlChan);
                    lastchan = thischan;
                }
                if (rs.getString("value") == null)
                    xmlChan.addTag(new XmlTag(rs.getString("property"), rs.getString("owner")));
                else
                    xmlChan.addProperty(new XmlProperty(rs.getString("property"),
                        rs.getString("owner"), rs.getString("value")));
            }
            con.get().close();
            return xmlChans;
        } catch (Exception e) {
            throw new WebServiceException("SQL Error during property match operation", e);
        }
    }

    /**
     * Return channels found by matching the channel name.
     * @param matches collection of channel name patterns to match
     * @return XmlChannels container with all found channels and their properties
     */
    public XmlChannels findChannelsByNameMatch(Collection<String> matches) {
        MatchQuery query = MatchQuery.createChannelMatchQuery(matches);
        return findChannelsByMatch(query);
    }

    /**
     * Return channels found by matching the tag name.
     * @param matches collection of channel name patterns to match
     * @return XmlChannels container with all found channels and their properties
     */
    public XmlChannels findChannelsByTagMatch(Collection<String> matches) {
        MatchQuery query = MatchQuery.createTagMatchQuery(matches);
        return findChannelsByMatch(query);
    }

    /**
     * Return channels found by matching the tag name.
     * @param name tag name
     * @return XmlChannels container with all found channels and their properties
     */
    public XmlChannels findChannelsByTag(String name) {
        MatchQuery query = MatchQuery.createSingleTagMatchQuery(name);
        return findChannelsByMatch(query);
    }

    /**
     * Returns channels found by matching property values.
     * @param matches multivalued map of property, tag, channel names and patterns to match
     * their values against.
     * @return XmlChannels container with all found channels and their properties
     */
    public XmlChannels findChannelsByMultiMatch(MultivaluedMap<String, String> matches) {
        MatchQuery query = MatchQuery.createMultiMatchQuery(matches);
        return findChannelsByMatch(query);
    }

    /**
     * Deletes a channel identified by <tt>name</tt>.
     * @param name channel to delete
     */
    public void deleteChannel(String name) {
        DeleteChannelQuery dq = new DeleteChannelQuery(name);

        begin();
        try {
            dq.executeQuery(con.get());
        } catch (Exception e) {
            throw new WebServiceException("SQL Error during channel delete operation", e);
        }
        commit();
    }

    /**
     * Deletes a tag identified by <tt>name</tt> from all channels.
     * @param tag tag to delete
     */
    public void deleteTag(String tag) {
        DeleteTagQuery dq = new DeleteTagQuery(tag);

        begin();
        try {
            dq.executeQuery(con.get());
        } catch (Exception e) {
            throw new WebServiceException("SQL Error during tag delete operation", e);
        }
        commit();
    }

    /**
     * Deletes a tag identified by <tt>name</tt> from all channels.
     * @param tag tag to delete
     * @param chan channel to delete it from
     */
    public void deleteSingleTag(String tag, String chan) {
        DeleteTagQuery dq = new DeleteTagQuery(tag, chan);

        begin();
        try {
            dq.executeQuery(con.get());
        } catch (Exception e) {
            throw new WebServiceException("SQL Error during single tag delete operation", e);
        }
        commit();
    }

    /**
     * Asserts the operation on named tag <tt>tag</tt> with the specified <tt>owner</tt>
     * on the channels specified in the XmlChannels <tt>data</tt> does not violate data
     * integrity.
     * @param tag tag to add
     * @param owner owner for new tag
     * @param data XmlChannels container with all channels for the operation
     */
    private String assertTagOwner(String tag, String owner) {
        // retrieve tag owner from database
        XmlChannels chans = findChannelsByTag(tag);
        String dbowner = findTagOwner(chans, tag);
        if (owner == null) owner = dbowner;

        // throw if no owner from database and not specified
        if (owner == null)
            throw new WebServiceException("No owner specified for new tag " + tag);

        // throw if specified and existing owner exist and do not match
        if (owner != null && dbowner != null && !owner.equals(dbowner))
            throw new WebServiceException("Specified owner " + owner
                    + " and existing owner " + dbowner + " for tag " + tag + " do not match");

        return owner;
    }

    /**
     * Add the tag identified by <tt>tag</tt> and <tt>owner</tt> to the channels
     * specified in the XmlChannels <tt>data</tt>.
     * @param tag tag to add
     * @param owner owner for new tag
     * @param data XmlChannels container with all channels to add tag to
     */
    public void addTag(String tag, String owner, XmlChannels data) {
        owner = assertTagOwner(tag, owner);

        AddTagQuery q = new AddTagQuery(tag, owner, data);

        begin();
        try {
            q.executeQuery(con.get());
        } catch (Exception e) {
            throw new WebServiceException("SQL Error during tag add operation", e);
        }
        commit();
    }

    /**
     * Add the tag identified by <tt>tag</tt> and <tt>owner</tt> exclusively
     * to the channels specified in the XmlChannels <tt>data</tt>.
     * @param tag tag to add
     * @param owner owner for new tag
     * @param data XmlChannels container with all channels to add tag to
     */
    public void putTag(String tag, String owner, XmlChannels data) {
        owner = assertTagOwner(tag, owner);

        DeleteTagQuery dq = new DeleteTagQuery(tag);
        AddTagQuery q = new AddTagQuery(tag, owner, data);

        begin();
        try {
            dq.executeQuery(con.get());
            q.executeQuery(con.get());
        } catch (Exception e) {
            throw new WebServiceException("SQL Error during tag add operation", e);
        }
        commit();
    }

    /**
     * Add the tag identified by <tt>tag</tt> and <tt>owner</tt>
     * to the single channel <tt>channel</tt>.
     * @param tag tag to add
     * @param owner owner for new tag
     * @param data XmlChannels container with all channels to add tag to
     */
    public void addSingleTag(String tag, String owner, String channel, XmlChannel data) {
        owner = assertTagOwner(tag, owner);

        if (!channel.equals(data.getName()))
            throw new WebServiceException("Specified channel name " + channel
                    + " and payload channel name " + data.getName() + " do not match");

        AddTagQuery q = new AddTagQuery(tag, owner, data);

        begin();
        try {
            q.executeQuery(con.get());
        } catch (Exception e) {
            throw new WebServiceException("SQL Error during single tag add operation", e);
        }
        commit();
    }

    /**
     * Update a channel identified by <tt>name</tt>, creating it when necessary.
     * The property set in <tt>data</tt> has to be complete, i.e. the existing channel properties
     * are <b>replaced</b> with the properties in <tt>data</tt>.
     * @param name channel to update
     * @param data XmlChannel data
     */
    public void updateChannel(String name, XmlChannel data) {
        if (!name.equals(data.getName())) {
            throw new WebServiceException("Channel name from URL and data do not match");
        }

        begin();
        try {
            DeleteChannelQuery d = new DeleteChannelQuery(name);
            d.executeQuery(con.get());
            CreateChannelQuery c = new CreateChannelQuery(data);
            c.executeQuery(con.get());
        } catch (Exception e) {
            rollback();
            throw new WebServiceException("SQL Error during channel create/update operation", e);
        }
        commit();
    }

    /**
     * Create channels specified in <tt>data</tt>.
     * @param data XmlChannels data
     */
    public void createChannels(XmlChannels data) {
        begin();
        try {
            for (XmlChannel chan : data.getChannels()) {
                CreateChannelQuery q = new CreateChannelQuery(chan);
                q.executeQuery(con.get());
            }
        } catch (Exception e) {
            rollback();
            throw new WebServiceException("SQL Error during channels create operation", e);
        }
        commit();
    }

    /**
     * Create a new channel using the property set in <tt>data</tt>.
     * @param data XmlChannel data
     */
    public void createChannel(XmlChannel data) {
        CreateChannelQuery q = new CreateChannelQuery(data);

        begin();
        try {
            q.executeQuery(con.get());
        } catch (Exception e) {
            rollback();
            throw new WebServiceException("SQL Error during channel create operation", e);
        }
        commit();
    }

    /**
     * Merge property set in <tt>data</tt> into the existing channel <tt>name</tt>.
     * @param name channel to merge the properties and tags into
     * @param data XmlChannel data containing properties and tags
     */
    public void mergeChannel(String name, XmlChannel data) {
        XmlChannel dest = findChannelByName(name);
        mergeXmlChannels(dest, data);
        updateChannel(name, dest);
    }
}
