/*
 * Copyright (c) 2010 Brookhaven National Laboratory
 * Copyright (c) 2010-2011 Helmholtz-Zentrum Berlin für Materialien und Energie GmbH
 * All rights reserved. Use is subject to license terms and conditions.
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
 * @author Ralph Lange <Ralph.Lange@helmholtz-berlin.de>
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
     * Return single property found by name.
     *
     * @param name name to look for
     * @return XmlProperty with found property and its channels/values
     * @throws CFException on SQLException
     */
    public XmlProperty findPropertyByName(String name) throws CFException {
        XmlProperty p = ListPropertiesQuery.findProperty(name);
        if (p != null) {
            XmlChannels c = FindChannelsQuery.findChannelsByPropertyName(name);
            if (c != null) {
                p.setXmlChannels(c);
            }
        }
        return p;
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
        UpdateValuesQuery.updateProperty(prop, data);
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
        DeletePropertyQuery.removeProperty(prop);
        CreatePropertyQuery.createProperty(data.getName(), data.getOwner());
        UpdateValuesQuery.updateProperty(data.getName(), data);
    }

    /**
     * Create or replace properties specified in <tt>data</tt>.
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
        UpdateValuesQuery.updateProperty(prop, data);
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
     * Deletes a property identified by <tt>name</tt> from all channels, failing if
     * the property does not exist.
     *
     * @param property tag to delete
     * @throws CFException wrapping an SQLException or on failure
     */
    public void removeExistingProperty(String property) throws CFException {
        DeletePropertyQuery.removeExistingProperty(property);
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
     * Return single tag found by name.
     *
     * @param name name to look for
     * @return XmlTag with found tag and its channels/values
     * @throws CFException on SQLException
     */
    public XmlTag findTagByName(String name) throws CFException {
        XmlTag t = ListPropertiesQuery.findTag(name);
        if (t != null) {
            XmlChannels c = FindChannelsQuery.findChannelsByPropertyName(name);
            if (c != null) {
                t.setXmlChannels(c);
            }
        }
        return t;
    }

    /**
     * Add the tag identified by <tt>tag</tt> and <tt>owner</tt> to the channels
     * specified in the XmlChannels <tt>data</tt>.
     *
     * @param tag tag to add
     * @param data XmlTag with list of all channels to add tag to
     * @throws CFException on ownership mismatch, or wrapping an SQLException
     */
    public void updateTag(String tag, XmlTag data) throws CFException {
        UpdateValuesQuery.updateTag(tag, data);
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
        DeletePropertyQuery.removeProperty(tag);
        CreatePropertyQuery.createTag(data.getName(), data.getOwner());
        UpdateValuesQuery.updateTag(data.getName(), data);
    }

    /**
     * Create tags specified in <tt>data</tt>.
     *
     * @param data XmlTags data
     * @throws CFException on ownership mismatch, or wrapping an SQLException
     */
    public void createOrReplaceTags(XmlTags data) throws CFException {
        for (XmlTag tag : data.getTags()) {
            removeTag(tag.getName());
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
        UpdateValuesQuery.updateTag(tag, channel);
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
            throw new CFException(Response.Status.NOT_FOUND,
                    "Specified channel '" + name
                    + "' does not exist");
        }
        dest.setName(data.getName());
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
     * Check the channel in <tt>data</tt> for valid name/owner data.
     *
     * @param data XmlChannel data to check
     * @throws CFException on error
     */
    public void checkValidNameAndOwner(XmlChannel data, String regex) throws CFException {
        if (data.getName() == null || !data.getName().matches(regex)) {
            throw new CFException(Response.Status.BAD_REQUEST,
                    "Invalid channel name " + data.getName());
        }
        if (data.getOwner() == null || data.getOwner().equals("")) {
            throw new CFException(Response.Status.BAD_REQUEST,
                    "Invalid channel owner (null or empty string) for '" + data.getName() + "'");
        }
    }

    /**
     * Check all channels in <tt>data</tt> for valid name/owner data.
     *
     * @param data XmlChannels data to check
     * @throws CFException on error
     */
    public void checkValidNameAndOwner(XmlChannels data, String regex) throws CFException {
        if (data == null || data.getChannels() == null) return;
        for (XmlChannel c : data.getChannels()) {
            checkValidNameAndOwner(c, regex);
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
        if (data == null) return;
        if (!name.equals(data.getName())) {
            throw new CFException(Response.Status.BAD_REQUEST,
                    "Specified tag name '" + name
                    + "' and payload tag name '" + data.getName() + "' do not match");
        }
    }

    /**
     * Check the tag in <tt>data</tt> for valid name/owner data.
     *
     * @param data XmlTag data to check
     * @throws CFException on name mismatch
     */
    public void checkValidNameAndOwner(XmlTag data, String regex) throws CFException {
        if (data.getName() == null || !data.getName().matches(regex)) {
            throw new CFException(Response.Status.BAD_REQUEST,
                    "Invalid tag name " + data.getName());
        }
        if (data.getOwner() == null || data.getOwner().equals("")) {
            throw new CFException(Response.Status.BAD_REQUEST,
                    "Invalid tag owner (null or empty string) for '" + data.getName() + "'");
        }
    }

    /**
     * Check all tags in <tt>data</tt> for valid name/owner data.
     *
     * @param data XmlTags data to check
     * @throws CFException on error
     */
    public void checkValidNameAndOwner(XmlTags data, String regex) throws CFException {
        if (data == null || data.getTags() == null) return;
        for (XmlTag t : data.getTags()) {
            checkValidNameAndOwner(t, regex);
        }
    }

    /**
     * Check that <tt>name</tt> matches the property name in <tt>data</tt>.
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
     * Check the property in <tt>data</tt> for valid name/owner data.
     *
     * @param data XmlTag data to check
     * @throws CFException on error
     */
    public void checkValidNameAndOwner(XmlProperty data, String regex) throws CFException {
        if (data.getName() == null || !data.getName().matches(regex)) {
            throw new CFException(Response.Status.BAD_REQUEST,
                    "Invalid property name " + data.getName());
        }
        if (data.getOwner() == null || data.getOwner().equals("")) {
            throw new CFException(Response.Status.BAD_REQUEST,
                    "Invalid property owner (null or empty string) for '" + data.getName() + "'");
        }
    }

    /**
     * Check all properties in <tt>data</tt> for valid name/owner data.
     *
     * @param data XmlProperties data to check
     * @throws CFException on error
     */
    public void checkValidNameAndOwner(XmlProperties data, String regex) throws CFException {
        if (data == null || data.getProperties() == null) return;
        for (XmlProperty p : data.getProperties()) {
            checkValidNameAndOwner(p, regex);
        }
    }

    /**
     * Check that <tt>user</tt> belongs to the owner group specified in the database for
     * channel <tt>chan</tt>.
     *
     * @param user user name
     * @param chan name of channel to check ownership for
     * @throws CFException on name mismatch
     */
    public void checkUserBelongsToGroupOfChannel(String user, String chan) throws CFException {
        if (chan == null || chan.equals("")) return;
        checkUserBelongsToGroup(user, FindChannelsQuery.findChannelByName(chan));
    }

    /**
     * Check that <tt>user</tt> belongs to the owner group specified in the database for
     * property <tt>prop</tt>.
     *
     * @param user user name
     * @param prop name of property to check ownership for
     * @throws CFException on name mismatch
     */
    public void checkUserBelongsToGroupOfProperty(String user, String prop) throws CFException {
        if (prop == null || prop.equals("")) return;
        checkUserBelongsToGroup(user, ListPropertiesQuery.findProperty(prop));
    }

    /**
     * Check that <tt>user</tt> belongs to the owner group specified in the database for
     * <tt>tag</tt>.
     *
     * @param user user name
     * @param tag name of tag to check ownership for
     * @throws CFException on name mismatch
     */
    public void checkUserBelongsToGroupOfTag(String user, String tag) throws CFException {
        if (tag == null || tag.equals("")) return;
        checkUserBelongsToGroup(user, ListPropertiesQuery.findTag(tag));
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
        if (data == null) return;
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
        if (data == null || data.getChannels() == null) return;
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
        if (data == null) return;
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
        if (data == null || data.getProperties() == null) return;
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
        if (data == null) return;
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
}
