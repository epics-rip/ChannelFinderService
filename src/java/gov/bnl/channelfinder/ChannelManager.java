/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.bnl.channelfinder;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Collection;
import javax.ws.rs.core.MultivaluedMap;

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
     * Begins a transaction by establishing a connection and beginning the transaction
     */
    public void begin() {
        try {
            con.set(DbConnection.getInstance().getConnection());
            con.get().setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            con.get().setAutoCommit(false);
        } catch (Exception e) {
            throw new RuntimeException("Cannot establish database connection", e);
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
            throw new RuntimeException("Could not commit the requested changes to the database", e);
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
            throw new RuntimeException("Could not roll back changes to the database", e);
        }
    }

    /**
     * Return single channel found by channel name.
     * @param name name to look for
     * @return XmlChannel with found channel and its properties
     */
    public XmlChannel findChannelByName(String name) {
        MatchQuery query = new MatchQuery(name);
        try {
            con.set(DbConnection.getInstance().getConnection());
            ResultSet rs = query.executeQuery(con.get());

            String lastchan = "";
            XmlChannel xmlChan = null;
            XmlProperties xmlProps = null;

            while (rs.next()) {
                String thischan = rs.getString("channel");
                if (rs.isFirst()) {
                    xmlChan = new XmlChannel(thischan);
                    xmlProps = new XmlProperties();
                    xmlChan.setXmlProperties(xmlProps);
                    lastchan = thischan;
                }
                if (!thischan.equals(lastchan)) break;
                xmlProps.addProperty(new XmlProperty(rs.getString("property"), rs.getString("value")));
            }
            con.get().close();
            return xmlChan;
        } catch (Exception e) {
            throw new RuntimeException("SQL Error during channel find operation", e);
        }

    }

    /**
     * Returns channels found by matching property values and/or channel names.
     * @param query the query to be used for matching
     * @return XmlChannels container with all found channels and their properties
     */
    private XmlChannels findChannelsByMatch(MatchQuery query) {
        try {
            con.set(DbConnection.getInstance().getConnection());
            ResultSet rs = query.executeQuery(con.get());

            String lastchan = "";
            XmlChannels xmlChans = new XmlChannels();
            XmlChannel xmlChan = null;
            XmlProperties xmlProps = null;

            while (rs.next()) {
                String thischan = rs.getString("channel");
                if (!thischan.equals(lastchan) || rs.isFirst()) {
                    xmlChan = new XmlChannel(thischan);
                    xmlChans.addChannel(xmlChan);
                    xmlProps = new XmlProperties();
                    xmlChan.setXmlProperties(xmlProps);
                    lastchan = thischan;
                }
                xmlProps.addProperty(new XmlProperty(rs.getString("property"), rs.getString("value")));
            }
            con.get().close();
            return xmlChans;
        } catch (Exception e) {
            throw new RuntimeException("SQL Error during property match operation", e);
        }
    }

    /**
     * Return channels found by matching the channel name.
     * @param matches collection of channel name patterns to match
     * @return XmlChannels container with all found channels and their properties
     */
    public XmlChannels findChannelsByNameMatch(Collection<String> matches) {
        MatchQuery query = new MatchQuery(matches);
        return findChannelsByMatch(query);
    }

    /**
     * Returns channels found by matching property values.
     * @param matches multivalued map of property names and patterns to match
     * their values against.
     * @return XmlChannels container with all found channels and their properties
     */
    public XmlChannels findChannelsByPropertyMatch(MultivaluedMap<String, String> matches) {
        MatchQuery query = new MatchQuery(matches);
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
            throw new RuntimeException("SQL Error during channel delete operation", e);
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
            throw new RuntimeException("Channel name from URL and data do not match");
        }

        begin();
        try {
            DeleteChannelQuery d = new DeleteChannelQuery(name);
            d.executeQuery(con.get());
            CreateChannelQuery c = new CreateChannelQuery(data);
            c.executeQuery(con.get());
        } catch (Exception e) {
            rollback();
            throw new RuntimeException("SQL Error during channel create/update operation", e);
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
            for (XmlChannel chan : data.getChannelCollection()) {
                CreateChannelQuery q = new CreateChannelQuery(chan);
                q.executeQuery(con.get());
            }
        } catch (Exception e) {
            rollback();
            throw new RuntimeException("SQL Error during channels create operation", e);
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
            throw new RuntimeException("SQL Error during channel create operation", e);
        }
        commit();
    }
}
