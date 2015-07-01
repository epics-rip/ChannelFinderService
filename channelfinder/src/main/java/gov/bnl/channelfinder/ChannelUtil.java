/**
 * Copyright (C) 2010-2012 Brookhaven National Laboratory
 * Copyright (C) 2010-2012 Helmholtz-Zentrum Berlin für Materialien und Energie GmbH
 * All rights reserved. Use is subject to license terms.
 */
package gov.bnl.channelfinder;

import java.util.Collection;
import java.util.HashSet;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;


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
     * @param channel
     * @return Collection of names of tags
     */
    public static Collection<String> getTagNames(XmlChannel channel) {       
        return Collections2.transform(channel.getXmlTags().getTags(), new Function<XmlTag, String>() {
            @Override
            public String apply(XmlTag tag) {
                return tag.getName();
            }
        });
    }

    /**
     * Return a union of tag names associated with channels
     * 
     * @param channels
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
     * @param channel
     * @return Collection of names of properties
     */
    public static Collection<String> getPropertyNames(XmlChannel channel) {
        return Collections2.transform(channel.getXmlProperties().getProperties(), new Function<XmlProperty, String>() {
            @Override
            public String apply(XmlProperty property) {
                return property.getName();
            }
        });
    }

    /**
     * Return a union of property names associated with channels
     * 
     * @param channels
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
     * @param channels
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
     * @param channel
     * @param propertyName
     * @return Property
     */
    public static XmlProperty getProperty(XmlChannel channel, String propertyName) {
        Collection<XmlProperty> property = Collections2.filter(channel.getXmlProperties().getProperties(),
                new PropertyNamePredicate(propertyName));
        if (property.size() == 1)
            return property.iterator().next();
        else
            return null;
    }

    private static class PropertyNamePredicate implements Predicate<XmlProperty> {

        private String propertyName;

        PropertyNamePredicate(String propertyName) {
            this.propertyName = propertyName;
        }

        @Override
        public boolean apply(XmlProperty input) {
            if (input.getName().equals(propertyName))
                return true;
            return false;
        }
    }
}