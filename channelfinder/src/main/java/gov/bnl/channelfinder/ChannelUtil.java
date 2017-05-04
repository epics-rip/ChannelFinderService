/**
 * Copyright (C) 2010-2012 Brookhaven National Laboratory
 * Copyright (C) 2010-2012 Helmholtz-Zentrum Berlin für Materialien und Energie GmbH
 * All rights reserved. Use is subject to license terms.
 */
package gov.bnl.channelfinder;

/*
 * #%L
 * ChannelFinder Directory Service
 * %%
 * Copyright (C) 2010 - 2015 Helmholtz-Zentrum Berlin für Materialien und Energie GmbH
 * %%
 * Copyright (C) 2010 - 2012 Brookhaven National Laboratory
 * All rights reserved. Use is subject to license terms.
 * #L%
 */


import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;


/**
 * @author shroffk
 * 
 */
public class ChannelUtil {

    /**
     * This class is not meant to be instantiated or extended
     */
    private ChannelUtil() {

    }

    /**
     * Return a list of tag names associated with this channel
     * 
     * @param channel channel to be processed
     * @return Collection of names of tags
     */
    public static Collection<String> getTagNames(XmlChannel channel) {
        return channel.getTags().stream().map(XmlTag::getName).collect(Collectors.toSet());
    }

    /**
     * Return a union of tag names associated with channels
     * 
     * @param channels channels to be processed
     * @return a set of all unique tag names associated with atleast one or more
     *         channel in channels
     */
    public static Collection<String> getTagNames(Collection<XmlChannel> channels) {
        Collection<String> tagNames = new HashSet<String>();
        for (XmlChannel channel : channels) {
            tagNames.addAll(getTagNames(channel));
        }
        return tagNames;
    }

    /**
     * Return a list of property names associated with this channel
     * 
     * @param channel channel to be processed
     * @return Collection of names of properties
     */
    public static Collection<String> getPropertyNames(XmlChannel channel) {
        return channel.getProperties().stream().map(XmlProperty::getName).collect(Collectors.toSet());
    }

    /**
     * Return a union of property names associated with channels
     * 
     * @param channels channels to be processed
     * @return a set of all unique property names associated with atleast one or
     *         more channel in channels
     */
    public static Collection<String> getPropertyNames(Collection<XmlChannel> channels) {
        Collection<String> propertyNames = new HashSet<String>();
        for (XmlChannel channel : channels) {
            propertyNames.addAll(getPropertyNames(channel));
        }
        return propertyNames;
    }

    /**
     * Returns all the channel Names in the given Collection of channels
     * 
     * @param channels channels to be processed
     * @return a set of all the unique names associated with the each channel in
     *         channels
     */
    public static Collection<String> getChannelNames(Collection<XmlChannel> channels) {
        Collection<String> channelNames = new HashSet<String>();
        for (XmlChannel channel : channels) {
            channelNames.add(channel.getName());
        }
        return channelNames;
    }

    /**
     * 
     * Return the property object with the name <tt>propertyName</tt> if it
     * exists on the channel <tt>channel</tt> else return null
     * 
     * @param channel channel to be processed
     * @param propertyName name of property being searched
     * @return Property property object if found
     */
    public static XmlProperty getProperty(XmlChannel channel, String propertyName) {
        Collection<XmlProperty> property = channel.getProperties().stream()
                .filter(p -> p.getName().equals(propertyName)).collect(Collectors.toSet());
        if (property.size() == 1)
            return property.iterator().next();
        else
            return null;
    }

}