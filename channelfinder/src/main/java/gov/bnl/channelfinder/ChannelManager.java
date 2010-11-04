/*
 * Copyright (c) 2010 Brookhaven National Laboratory
 * Copyright (c) 2010 Helmholtz-Zentrum Berlin für Materialien und Energie GmbH
 * Subject to license terms and conditions.
 */
package gov.bnl.channelfinder;

import java.sql.Connection;
import java.util.Collection;
import java.util.Collections;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

/**
 * Central business logic layer that implements all directory operations.
 * 
 * @author Ralph Lange <Ralph.Lange@bessy.de>
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
        for (XmlProperty s : src.getXmlProperties().getProperties()) {
            for (XmlProperty d : dest.getXmlProperties().getProperties()) {
                if (d.getName().equals(s.getName())) {
                    d.setValue(s.getValue());
                    continue src_props;
                }
            }
            dest.getXmlProperties().addXmlProperty(s);
        }
        src_tags:
        for (XmlTag s : src.getXmlTags().getTags()) {
            for (XmlTag d : dest.getXmlTags().getTags()) {
                if (d.getName().equals(s.getName())) {
                    continue src_tags;
                }
            }
            dest.getXmlTags().addXmlTag(s);
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
        return FindChannelsQuery.findChannelByName(name);
    }

    /**
     * Return channels found by matching tags against a collection of name patterns.
     *
     * @param matches collection of name patterns to match
     * @return XmlChannels container with all found channels and their properties
     * @throws CFException wrapping an SQLException
     */
    public XmlChannels findChannelsByPropertyName(String name) throws CFException {
        return FindChannelsQuery.findChannelsByPropertyName(name);
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
        return FindChannelsQuery.findChannelsByMultiMatch(matches);
    }

    /**
     * Deletes a channel identified by <tt>name</tt>.
     *
     * @param name channel to delete
     * @throws CFException wrapping an SQLException
     */
    public void removeChannel(String name) throws CFException {
        DeleteChannelQuery.deleteChannelIgnoreNoexist(name);
    }

    /**
     * Deletes a channel identified by <tt>name</tt>.
     *
     * @param name channel to delete
     * @throws CFException wrapping an SQLException
     */
    public void removeExistingChannel(String name) throws CFException {
        DeleteChannelQuery.deleteChannelFailNoexist(name);
    }

    /**
     * List all properties in the database.
     *
     * @throws CFException wrapping an SQLException
     */
    public XmlProperties listProperties() throws CFException {
        return ListPropertiesQuery.getProperties();
    }

    /**
     * Add the property identified by <tt>prop</tt> to the channels
     * specified in the XmlChannels <tt>data</tt>.
     *
     * @param prop property to add
     * @param data XmlProperty data with all channels and values to add property to
     * @throws CFException on ownership mismatch, or wrapping an SQLException
     */
    public void updateProperty(String prop, XmlProperty data) throws CFException {
        UpdatePropertyQuery.updateProperty(data);
    }

    /**
     * Adds the property identified by <tt>tag</tt> <b>exclusively</b>
     * to the channels specified in the XmlProperty payload <tt>data</tt>, creating it
     * if necessary.
     *
     * @param prop property to add
     * @param data XmlProperty container with all channels to add property to
     * @throws CFException on ownership mismatch, or wrapping an SQLException
     */
    public void createOrReplaceProperty(String prop, XmlProperty data) throws CFException {
        DeletePropertyQuery.deleteAllValues(prop);
        UpdatePropertyQuery.updateProperty(data);
    }

    /**
     * Create properties specified in <tt>data</tt>.
     *
     * @param data XmlProperties data
     * @throws CFException on ownership mismatch, or wrapping an SQLException
     */
    public void createOrReplaceProperties(XmlProperties data) throws CFException {
        for (XmlProperty prop : data.getProperties()) {
            removeProperty(prop.getName());
            createOrReplaceProperty(prop.getName(), prop);
        }
    }

    /**
     * Add the property identified by <tt>prop</tt>
     * to the single channel <tt>chan</tt>.
     *
     * @param prop property to add
     * @param chan channel to add the property to
     * @param data XmlProperty (may contain value)
     * @throws CFException on ownership mismatch, or wrapping an SQLException
     */
    public void addSingleProperty(String prop, String chan, XmlProperty data) throws CFException {
        UpdatePropertyQuery.updateProperty(data);
    }

    /**
     * Deletes a property identified by <tt>name</tt> from all channels.
     *
     * @param property tag to delete
     * @throws CFException wrapping an SQLException
     */
    public void removeProperty(String property) throws CFException {
        DeletePropertyQuery.removeProperty(property);
    }

    /**
     * Deletes a property identified by <tt>name</tt> from a single channel.
     *
     * @param property tag to delete
     * @param channel channel to delete it from
     * @throws CFException wrapping an SQLException
     */
    public void removeSingleProperty(String property, String channel) throws CFException {
        DeletePropertyQuery.deleteOneValue(property, channel);
    }

    /**
     * List all tags in the database.
     *
     * @throws CFException wrapping an SQLException
     */
    public XmlTags listTags() throws CFException {
        return ListPropertiesQuery.getTags();
    }

    /**
     * Add the tag identified by <tt>tag</tt> and <tt>owner</tt> to the channels
     * specified in the XmlChannels <tt>data</tt>.
     *
     * @param tag tag to add
     * @param data XmlChannels container with all channels to add tag to
     * @throws CFException on ownership mismatch, or wrapping an SQLException
     */
    public void updateTag(String tag, XmlTag data) throws CFException {
        UpdatePropertyQuery.updateTag(data);
    }

    /**
     * Adds the tag identified by <tt>tag</tt> <b>exclusively</b>
     * to the channels specified in the XmlTag payload <tt>data</tt>, creating it
     * if necessary.
     *
     * @param tag tag to add
     * @param data XmlTag container with all channels to add tag to
     * @throws CFException on ownership mismatch, or wrapping an SQLException
     */
    public void createOrReplaceTag(String tag, XmlTag data) throws CFException {
        DeletePropertyQuery.deleteAllValues(tag);
        UpdatePropertyQuery.updateTag(data);
    }

    /**
     * Create tags specified in <tt>data</tt>.
     *
     * @param data XmlTags data
     * @throws CFException on ownership mismatch, or wrapping an SQLException
     */
    public void createOrReplaceTags(XmlTags data) throws CFException {
        for (XmlTag tag : data.getTags()) {
            removeChannel(tag.getName());
            createOrReplaceTag(tag.getName(), tag);
        }
    }

    /**
     * Add the tag identified by <tt>tag</tt> to the single channel <tt>channel</tt>.
     *
     * @param tag tag to add
     * @param channel
     * @throws CFException on ownership mismatch, or wrapping an SQLException
     */
    public void addSingleTag(String tag, String channel) throws CFException {
        UpdatePropertyQuery.updateTag(tag, channel);
    }

    /**
     * Deletes a tag identified by <tt>name</tt> from all channels.
     *
     * @param tag tag to delete
     * @throws CFException wrapping an SQLException
     */
    public void removeTag(String tag) throws CFException {
        DeletePropertyQuery.removeProperty(tag);
    }

    /**
     * Deletes a tag identified by <tt>name</tt> from a single channel.
     *
     * @param tag tag to delete
     * @param chan channel to delete it from
     * @throws CFException wrapping an SQLException
     */
    public void removeSingleTag(String tag, String chan) throws CFException {
        DeletePropertyQuery.deleteOneValue(tag, chan);
    }

    /**
     * Update a channel identified by <tt>name</tt>, creating it when necessary.
     * The property set in <tt>data</tt> has to be complete, i.e. the existing
     * channel properties are <b>replaced</b> with the properties in <tt>data</tt>.
     *
     * @param name channel to update
     * @param data XmlChannel data
     * @throws CFException on ownership or name mismatch, or wrapping an SQLException
     */
    public void createOrReplaceChannel(String name, XmlChannel data) throws CFException {
        DeleteChannelQuery.deleteChannelIgnoreNoexist(name);
        CreateChannelQuery.createChannel(data);
    }

    /**
     * Create channels specified in <tt>data</tt>.
     *
     * @param data XmlChannels data
     * @throws CFException on ownership mismatch, or wrapping an SQLException
     */
    public void createOrReplaceChannels(XmlChannels data) throws CFException {
        for (XmlChannel chan : data.getChannels()) {
            removeChannel(chan.getName());
            createOneChannel(chan);
        }
    }

    /**
     * Create a new channel using the property set in <tt>data</tt>.
     *
     * @param data XmlChannel data
     * @throws CFException on ownership or name mismatch, or wrapping an SQLException
     */
    private void createOneChannel(XmlChannel data) throws CFException {
        CreateChannelQuery.createChannel(data);
    }

    /**
     * Merge property set in <tt>data</tt> into the existing channel <tt>name</tt>.
     *
     * @param name channel to merge the properties and tags into
     * @param data XmlChannel data containing properties and tags
     * @throws CFException on name or owner mismatch, or wrapping an SQLException
     */
    public void updateChannel(String name, XmlChannel data) throws CFException {
        XmlChannel dest = findChannelByName(name);
        if (dest == null) {
            throw new CFException(Response.Status.FORBIDDEN,
                    "Specified channel '" + name
                    + "' does not exist");
        }
        dest.setOwner(data.getOwner());
        mergeXmlChannels(dest, data);
        createOrReplaceChannel(name, dest);
    }

    /**
     * Check that <tt>name</tt> matches the channel name in <tt>data</tt>.
     *
     * @param name channel name to check
     * @param data XmlChannel data to check against
     * @throws CFException on name mismatch
     */
    public void checkNameMatchesPayload(String name, XmlChannel data) throws CFException {
        if (!name.equals(data.getName())) {
            throw new CFException(Response.Status.BAD_REQUEST,
                    "Specified channel name '" + name
                    + "' and payload channel name '" + data.getName() + "' do not match");
        }
    }

    /**
     * Check that <tt>name</tt> matches the tag name in <tt>data</tt>.
     *
     * @param name tag name to check
     * @param data XmlTag data to check against
     * @throws CFException on name mismatch
     */
    public void checkNameMatchesPayload(String name, XmlTag data) throws CFException {
        if (!name.equals(data.getName())) {
            throw new CFException(Response.Status.BAD_REQUEST,
                    "Specified tag name '" + name
                    + "' and payload tag name '" + data.getName() + "' do not match");
        }
    }

    /**
     * Check that <tt>name</tt> matches the tag name in <tt>data</tt>.
     *
     * @param name tag name to check
     * @param data XmlTag data to check against
     * @throws CFException on name mismatch
     */
    public void checkNameMatchesPayload(String name, XmlProperty data) throws CFException {
        if (!name.equals(data.getName())) {
            throw new CFException(Response.Status.BAD_REQUEST,
                    "Specified property name '" + name
                    + "' and payload property name '" + data.getName() + "' do not match");
        }
    }

    /**
     * Check that <tt>user</tt> belongs to the owner group specified in the
     * channel <tt>data</tt>.
     *
     * @param user user name
     * @param data XmlChannel data to check ownership for
     * @throws CFException on name mismatch
     */
    public void checkUserBelongsToGroup(String user, XmlChannel data) throws CFException {
        UserManager um = UserManager.getInstance();
        if (!um.userIsInGroup(data.getOwner())) {
            throw new CFException(Response.Status.FORBIDDEN,
                    "User '" + um.getUserName()
                    + "' does not belong to owner group '" + data.getOwner()
                    + "' of channel '" + data.getName() + "'");
        }
    }

    /**
     * Check that <tt>user</tt> belongs to the owner groups of all channels in <tt>data</tt>.
     *
     * @param user user name
     * @param data XmlChannels data to check ownership for
     * @throws CFException on name mismatch
     */
    public void checkUserBelongsToGroup(String user, XmlChannels data) throws CFException {
        for (XmlChannel chan : data.getChannels()) {
            checkUserBelongsToGroup(user, chan);
        }
    }

    /**
     * Check that <tt>user</tt> belongs to the owner group specified in the
     * property <tt>data</tt>.
     *
     * @param user user name
     * @param data XmlProperty data to check ownership for
     * @throws CFException on name mismatch
     */
    public void checkUserBelongsToGroup(String user, XmlProperty data) throws CFException {
        UserManager um = UserManager.getInstance();
        if (!um.userIsInGroup(data.getOwner())) {
            throw new CFException(Response.Status.FORBIDDEN,
                    "User '" + um.getUserName()
                    + "' does not belong to owner group '" + data.getOwner()
                    + "' of property '" + data.getName() + "'");
        }
    }

    /**
     * Check that <tt>user</tt> belongs to the owner groups of all properties in <tt>data</tt>.
     *
     * @param user user name
     * @param data XmlChannels data to check ownership for
     * @throws CFException on name mismatch
     */
    public void checkUserBelongsToGroup(String user, XmlProperties data) throws CFException {
        for (XmlProperty prop : data.getProperties()) {
            checkUserBelongsToGroup(user, prop);
        }
    }

    /**
     * Check that <tt>user</tt> belongs to the owner group specified in the
     * tag <tt>data</tt>.
     *
     * @param user user name
     * @param data XmlTag data to check ownership for
     * @throws CFException on name mismatch
     */
    public void checkUserBelongsToGroup(String user, XmlTag data) throws CFException {
        UserManager um = UserManager.getInstance();
        if (!um.userIsInGroup(data.getOwner())) {
            throw new CFException(Response.Status.FORBIDDEN,
                    "User '" + um.getUserName()
                    + "' does not belong to owner group '" + data.getOwner()
                    + "' of tag '" + data.getName() + "'");
        }
    }

    /**
     * Check that <tt>user</tt> belongs to the owner groups of all Tags in <tt>data</tt>.
     *
     * @param user user name
     * @param data XmlChannels data to check ownership for
     * @throws CFException on name mismatch
     */
    public void checkUserBelongsToGroup(String user, XmlTags data) throws CFException {
        for (XmlTag tag : data.getTags()) {
            checkUserBelongsToGroup(user, tag);
        }
    }

    /**
     * Check that <tt>user</tt> belongs to the owner group of the property or tag
     * name <tt>name</tt>.
     *
     * @param user user name
     * @param name property or tag name to check ownership for
     * @throws CFException on name mismatch
     */
    public void checkUserBelongsToDatabaseGroup(String user, String name) throws CFException {
        checkUserBelongsToGroup(user, findChannelByName(name));
    }
}
