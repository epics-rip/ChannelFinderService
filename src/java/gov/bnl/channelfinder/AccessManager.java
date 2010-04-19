/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.bnl.channelfinder;

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
     * @param name name to look for
     * @return XmlChannel with found channel and its properties
     */
    public XmlChannel findChannelByName(String name) {
        return cm.findChannelByName(name);
    }

    /**
     * Return channels found by matching the channel name.
     * @param matches collection of channel name patterns to match
     * @return XmlChannels container with all found channels and their properties
     */
    public XmlChannels findChannelsByNameMatch(Collection<String> matches) {
        return cm.findChannelsByNameMatch(matches);
    }

    /**
     * Returns channels found by matching property values.
     * @param matches multivalued map of property names and patterns to match
     * their values against.
     * @return XmlChannels container with all found channels and their properties
     */
    public XmlChannels findChannelsByMultiMatch(MultivaluedMap<String, String> matches) {
        return cm.findChannelsByMultiMatch(matches);
    }

    /**
     * Deletes a channel identified by <tt>name</tt>.
     * @param name channel to delete
     */
    public void deleteChannel(String name) {
        cm.deleteChannel(name);
    }

    /**
     * Update a channel identified by <tt>name</tt>, creating it when necessary.
     * The property set in <tt>data</tt> has to be complete, i.e. the existing channel properties
     * are <b>replaced</b> with the properties in <tt>data</tt>.
     * @param name channel to update
     * @param data XmlChannel data
     */
    public void updateChannel(String name, XmlChannel data) {
        cm.updateChannel(name, data);
    }

    /**
     * Create channels specified in <tt>data</tt>.
     * @param data XmlChannels data
     */
    public void createChannels(XmlChannels data) {
        cm.createChannels(data);
    }

    /**
     * Create a new channel using the property set in <tt>data</tt>.
     * @param data XmlChannel data
     */
    public void createChannel(XmlChannel data) {
        cm.createChannel(data);
    }

    /**
     * Merge property set in <tt>data</tt> into the existing channel <tt>name</tt>.
     * @param name channel to merge the properties and tags into
     * @param data XmlChannel data containing properties and tags
     */
    public void mergeChannel(String name, XmlChannel data) {
        cm.mergeChannel(name, data);
    }
}
