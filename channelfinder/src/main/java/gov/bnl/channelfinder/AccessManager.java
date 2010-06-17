/*
 * Copyright (c) 2010 Brookhaven National Laboratory
 * Copyright (c) 2010 Helmholtz-Zentrum Berlin für Materialien und Energie GmbH
 * Subject to license terms and conditions.
 */

package gov.bnl.channelfinder;

import java.util.Collection;
import java.util.Collections;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

/**
 * Intermediate layer to enforce ownership restrictions on all accesses.
 *
 * @author Ralph Lange <Ralph.Lange@bessy.de>
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
     * @throws CFException for SQL errors
     */
    public XmlChannel findChannelByName(String name) throws CFException {
        return cm.findChannelByName(name);
    }

    /**
     * Return single channel found by channel name.
     *
     * @param name name to look for
     * @return XmlChannel with found channel and its properties
     * @throws CFException
     */
    public XmlChannels findChannelsByTag(String name) throws CFException {
        return cm.findChannelsByTagMatch(Collections.singleton(name));
    }

    /**
     * Return channels found by matching the channel name.
     *
     * @param matches collection of channel name patterns to match
     * @return XmlChannels container with all found channels and their properties
     * @throws CFException
     */
    public XmlChannels findChannelsByNameMatch(Collection<String> matches) throws CFException {
        return cm.findChannelsByNameMatch(matches);
    }

    /**
     * Returns channels found by matching property values.
     *
     * @param matches multivalued map of property names and patterns to match
     * their values against.
     * @return XmlChannels container with all found channels and their properties
     * @throws CFException
     */
    public XmlChannels findChannelsByMultiMatch(MultivaluedMap<String, String> matches) throws CFException {
        return cm.findChannelsByMultiMatch(matches);
    }

    /**
     * Deletes a channel identified by <tt>name</tt>.
     * 
     * @param name channel to delete
     * @throws CFException
     */
    public void deleteChannel(String name) throws CFException {
        EntityMap.getInstance().loadDbMapForChannel(name, true);
        if (!UserManager.getInstance().userHasAdminRole()) {
            checkUserBelongsToDbOwners();
        }
        cm.deleteChannel(name, false);
    }

    /**
     * Update a channel identified by <tt>name</tt>, creating it when necessary.
     * The property set in <tt>data</tt> has to be complete, i.e. the existing
     * channel properties are <b>replaced</b> with the properties in <tt>data</tt>.
     *
     * Needs db and payload group membership for payload channel and properties.
     *
     * @param name channel to update
     * @param data XmlChannel data
     * @throws CFException for name or owner mismatch, SQL error in query
     */
    public void updateChannel(String name, XmlChannel data) throws CFException {
        EntityMap.getInstance().loadMapsFor(new XmlChannels(data), false);
        if (!UserManager.getInstance().userHasAdminRole()) {
            checkUserBelongsToDbOwners();
            checkUserBelongsToPayloadOwners();
        }
        cm.updateChannel(name, data);
    }

    /**
     * Create channels specified in <tt>data</tt>.
     *
     * Needs db and payload group membership for all payload channels and properties.
     * 
     * @param data XmlChannels data
     * @throws CFException
     */
    public void createChannels(XmlChannels data) throws CFException {
        EntityMap.getInstance().loadMapsFor(data, false);
        if (!UserManager.getInstance().userHasAdminRole()) {
            checkUserBelongsToDbOwners();
            checkUserBelongsToPayloadOwners();
        }
        cm.createChannels(data);
    }

    /**
     * Merge property set in <tt>data</tt> into the existing channel <tt>name</tt>.
     *
     * Needs db and payload group membership for all payload properties.
     *
     * @param name channel to merge the properties and tags into
     * @param data XmlChannel data containing properties and tags
     * @throws CFException for name or owner mismatch, SQL error in query
     */
    public void mergeChannel(String name, XmlChannel data) throws CFException {
        EntityMap.getInstance().loadMapsFor(new XmlChannels(data), false);
        if (!UserManager.getInstance().userHasAdminRole()) {
            checkUserBelongsToDbPropertyOwners();
            checkUserBelongsToPayloadPropertyOwners();
        }
        cm.mergeChannel(name, data);
    }

    /**
     * Deletes a tag identified by <tt>name</tt> from all channels.
     *
     * Needs db group membership for the named property.
     * 
     * @param name tag to delete
     * @throws CFException
     */
    public void deleteTag(String name) throws CFException {
        EntityMap.getInstance().loadDbPropertyMapFor(name);
        if (!UserManager.getInstance().userHasAdminRole()) {
            checkUserBelongsToDbOwners();
        }
       cm.deleteTag(name);
    }

    /**
     * Adds a tag identified by <tt>name</tt> to all channels in <tt>data</tt>.
     *
     * Needs db and payload group membership for the named property.
     *
     * @param name tag to add
     * @param data XmlChannels data containing channel names
     * @throws CFException
     */
    public void addTag(String name, XmlChannels data) throws CFException {
        EntityMap.getInstance().loadPayloadMapsFor(data, name);
        EntityMap.getInstance().loadDbPropertyMapFor(name);
        if (!UserManager.getInstance().userHasAdminRole()) {
            checkUserBelongsToDbOwners();
            checkUserBelongsToPayloadPropertyOwners();
        }
        cm.addTag(name, data);
    }

    /**
     * Adds a tag identified by <tt>name</tt> <b>exclusively</b>
     * to all channels in <tt>data</tt>.
     *
     * @param name tag to add
     * @param data XmlChannels data containing channel names
     * @throws CFException
     */
    public void putTag(String name, XmlChannels data) throws CFException {
        EntityMap.getInstance().loadPayloadMapsFor(data);
        EntityMap.getInstance().loadDbPropertyMapFor(name);
        if (!UserManager.getInstance().userHasAdminRole()) {
            checkUserBelongsToDbOwners();
            checkUserBelongsToPayloadOwnerGroupOfProperty(name);
        }
        cm.putTag(name, data);
    }

    /**
     * Adds a tag identified by <tt>name</tt> to single channel <tt>chan</tt>,
     * with ownership specified in <tt>data</tt> (if not in database).
     *
     * @param name tag to add
     * @param chan channel to add tag to
     * @param data XmlChannels data containing tag ownership (for new tag)
     * @throws CFException
     */
    public void addSingleTag(String name, String chan, XmlTag data) throws CFException {
        EntityMap.getInstance().loadPayloadMapsFor(data);
        EntityMap.getInstance().loadDbPropertyMapFor(name);
        if (!UserManager.getInstance().userHasAdminRole()) {
            checkUserBelongsToDbOwners();
            checkUserBelongsToPayloadOwnerGroupOfProperty(name);
        }
        cm.addSingleTag(name, chan, data);
    }

    /**
     * Deletes a tag identified by <tt>name</tt> from channel <tt>chan</tt>.
     *
     * @param name tag to delete
     * @param chan channel to delete tag from
     * @throws CFException
     */
    public void deleteSingleTag(String name, String chan) throws CFException {
        EntityMap.getInstance().loadDbPropertyMapFor(name);
        if (!UserManager.getInstance().userHasAdminRole()) {
            checkUserBelongsToDbOwners();
        }
        cm.deleteSingleTag(name, chan);
    }

    /**
     * Deletes a property identified by <tt>name</tt> from all channels.
     *
     * Needs db group membership for the named property.
     *
     * @param name tag to delete
     * @throws CFException
     */
    public void deleteProperty(String name) throws CFException {
        EntityMap.getInstance().loadDbPropertyMapFor(name);
        if (!UserManager.getInstance().userHasAdminRole()) {
            checkUserBelongsToDbOwners();
        }
       cm.deleteProperty(name);
    }

    /**
     * Deletes a property identified by <tt>name</tt> from channel <tt>chan</tt>.
     *
     * @param name property to delete
     * @param chan channel to delete tag from
     * @throws CFException
     */
    public void deleteSingleProperty(String name, String chan) throws CFException {
        EntityMap.getInstance().loadDbPropertyMapFor(name);
        if (!UserManager.getInstance().userHasAdminRole()) {
            checkUserBelongsToDbOwners();
        }
        cm.deleteSingleProperty(name, chan);
    }

    /**
     * Checks if the authenticated user is a member of all payload owner groups
     * for entities specified there.
     */
    private void checkUserBelongsToPayloadOwnerGroupOfProperty(String name) throws CFException {
        String grp = EntityMap.getInstance().getPayloadPropertyOwner(name);
        if (!UserManager.getInstance().userIsInGroup(grp)) {
            throw new CFException(Response.Status.FORBIDDEN,
                "User " + UserManager.getInstance().getUserName()
                + " does not belong to group " + grp + " specified in payload");
        }
    }

    /**
     * Checks if the authenticated user is a member of all payload owner groups
     * for entities specified there.
     */
    private void checkUserBelongsToPayloadOwnerGroups(boolean checkChannelNames) throws CFException {
        for (String grp : EntityMap.getInstance().getAllPayloadOwners(checkChannelNames)) {
            if (!UserManager.getInstance().userIsInGroup(grp)) {
                throw new CFException(Response.Status.FORBIDDEN,
                    "User " + UserManager.getInstance().getUserName()
                    + " does not belong to group " + grp + " specified in payload");
            }
        }
    }

    private void checkUserBelongsToPayloadOwners() throws CFException {
        checkUserBelongsToPayloadOwnerGroups(true);
    }

    private void checkUserBelongsToPayloadPropertyOwners() throws CFException {
        checkUserBelongsToPayloadOwnerGroups(false);
    }

    /**
     * Checks if the authenticated user is a member of all db owner groups
     * for entities specified in the payload.
     */
    private void checkUserBelongsToDbOwnerGroups(boolean checkChannelNames) throws CFException {
        for (String grp : EntityMap.getInstance().getAllDbOwners(checkChannelNames)) {
            if (!UserManager.getInstance().userIsInGroup(grp)) {
                throw new CFException(Response.Status.FORBIDDEN,
                    "User " + UserManager.getInstance().getUserName()
                    + " does not belong to group " + grp + " needed to modify database");
            }
        }
    }

    private void checkUserBelongsToDbOwners() throws CFException {
        checkUserBelongsToDbOwnerGroups(true);
    }

    private void checkUserBelongsToDbPropertyOwners() throws CFException {
        checkUserBelongsToDbOwnerGroups(false);
    }
}