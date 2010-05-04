/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.bnl.channelfinder;

import java.sql.SQLException;
import java.util.Collection;
import javax.ws.rs.core.MultivaluedMap;

/**
 *
 * @author rlange
 */
public class AccessManager {
    private static AccessManager instance = new AccessManager();
    private static ChannelManager cm = ChannelManager.getInstance();

    /**
     * Create an instance of AccessManager
     */
    private AccessManager() {
    }

    /**
     * Returns the (singleton) instance of AccessManager
     * @return the instance of AccessManager
     */
    public static AccessManager getInstance() {
        return instance;
    }

    /**
     * Return single channel found by channel name.
     *
     * @param name name to look for
     * @return XmlChannel with found channel and its properties
     * @throws SQLException
     */
    public XmlChannel findChannelByName(String name) throws SQLException {
        return cm.findChannelByName(name);
    }

    /**
     * Return single channel found by channel name.
     *
     * @param name name to look for
     * @return XmlChannel with found channel and its properties
     * @throws SQLException
     */
    public XmlChannels findChannelsByTag(String name) throws SQLException {
        return cm.findChannelsByTag(name);
    }

    /**
     * Return channels found by matching the channel name.
     *
     * @param matches collection of channel name patterns to match
     * @return XmlChannels container with all found channels and their properties
     * @throws SQLException
     */
    public XmlChannels findChannelsByNameMatch(Collection<String> matches) throws SQLException {
        return cm.findChannelsByNameMatch(matches);
    }

    /**
     * Returns channels found by matching property values.
     *
     * @param matches multivalued map of property names and patterns to match
     * their values against.
     * @return XmlChannels container with all found channels and their properties
     * @throws SQLException
     */
    public XmlChannels findChannelsByMultiMatch(MultivaluedMap<String, String> matches) throws SQLException {
        return cm.findChannelsByMultiMatch(matches);
    }

    /**
     * Deletes a channel identified by <tt>name</tt>.
     * 
     * @param name channel to delete
     * @throws SQLException
     */
    public void deleteChannel(String name) throws SQLException {
        cm.deleteChannel(name);
    }

    /**
     * Update a channel identified by <tt>name</tt>, creating it when necessary.
     * The property set in <tt>data</tt> has to be complete, i.e. the existing
     * channel properties are <b>replaced</b> with the properties in <tt>data</tt>.
     *
     * @param name channel to update
     * @param data XmlChannel data
     * @throws SQLException
     */
    public void updateChannel(String name, XmlChannel data) throws SQLException {
        cm.updateChannel(name, data);
    }

    /**
     * Create channels specified in <tt>data</tt>.
     * 
     * @param data XmlChannels data
     * @throws SQLException
     */
    public void createChannels(XmlChannels data) throws SQLException {
        cm.createChannels(data);
    }

    /**
     * Create a new channel using the property set in <tt>data</tt>.
     * 
     * @param data XmlChannel data
     * @throws SQLException
     */
    public void createChannel(XmlChannel data) throws SQLException {
        cm.createChannel(data);
    }

    /**
     * Merge property set in <tt>data</tt> into the existing channel <tt>name</tt>.
     *
     * @param name channel to merge the properties and tags into
     * @param data XmlChannel data containing properties and tags
     * @throws SQLException
     */
    public void mergeChannel(String name, XmlChannel data) throws SQLException {
        cm.mergeChannel(name, data);
    }

    /**
     * Deletes a tag identified by <tt>name</tt> from all channels.
     * 
     * @param name tag to delete
     * @throws SQLException
     */
    public void deleteTag(String name) throws SQLException {
        cm.deleteTag(name);
    }

    /**
     * Adds a tag identified by <tt>name</tt> to all channels in <tt>data</tt>.
     *
     * @param name tag to add
     * @param data XmlChannels data containing channel names
     * @throws SQLException
     */
    public void addTag(String name, XmlChannels data) throws SQLException {
        cm.addTag(name, DbOwnerMap.getInstance().getPropertyOwner(name), data);
    }

    /**
     * Adds a tag identified by <tt>name</tt> <b>exclusively</b>
     * to all channels in <tt>data</tt>.
     *
     * @param name tag to add
     * @param data XmlChannels data containing channel names
     * @throws SQLException
     */
    public void putTag(String name, XmlChannels data) throws SQLException {
        cm.putTag(name, DbOwnerMap.getInstance().getPropertyOwner(name), data);
    }

    /**
     * Adds a tag identified by <tt>name</tt> to single channel <tt>chan</tt>,
     * with ownership specified in <tt>data</tt> (if not in database).
     *
     * @param name tag to add
     * @param chan channel to add tag to
     * @param data XmlChannels data containing tag ownership (for new tag)
     * @throws SQLException
     */
    public void addSingleTag(String name, String chan, XmlChannel data) throws SQLException {
        cm.addSingleTag(name, DbOwnerMap.getInstance().getPropertyOwner(name), chan, data);
    }

    /**
     * Deletes a tag identified by <tt>name</tt> from channel <tt>chan</tt>.
     *
     * @param name tag to delete
     * @param chan channel to delete tag from
     * @throws SQLException
     */
    public void deleteSingleTag(String name, String chan) throws SQLException {
        cm.deleteSingleTag(name, chan);
    }
}
