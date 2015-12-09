package gov.bnl.channelfinder;
/**
 * #%L
 * ChannelFinder Directory Service
 * %%
 * Copyright (C) 2010 - 2012 Helmholtz-Zentrum Berlin für Materialien und Energie GmbH
 * %%
 * Copyright (C) 2010 - 2012 Brookhaven National Laboratory
 * All rights reserved. Use is subject to license terms.
 * #L%
 */

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.annotate.JsonPropertyOrder;

/**
 * Channel object that can be represented as XML/JSON in payload data.
 *
 * @author Ralph Lange <Ralph.Lange@helmholtz-berlin.de>
 */

@XmlRootElement(name = "channel")
public class XmlChannel {
    private String name;
    private String owner;
    private List<XmlProperty> properties = new ArrayList<XmlProperty>();
    private List<XmlTag> tags = new ArrayList<XmlTag>();
  
    /** Creates a new instance of XmlChannel */
    public XmlChannel() {
    }

    /**
     * Creates a new instance of XmlChannel.
     *
     * @param name channel name
     */
    public XmlChannel(String name) {
        this.name = name;
    }

    /**
     * Creates a new instance of XmlChannel.
     *
     * @param name channel name
     * @param owner owner name
     */
    public XmlChannel(String name, String owner) {
        this.name = name;
        this.owner = owner;
    }

    /**
     * 
     * @param name
     * @param owner
     * @param properties
     * @param tags
     */
    public XmlChannel(String name, String owner, List<XmlProperty> properties, List<XmlTag> tags) {
        this.name = name;
        this.owner = owner;
        this.properties = properties;
        this.tags = tags;
    }

    /**
     * Getter for channel name.
     *
     * @return name
     */
    @XmlAttribute
    public String getName() {
        return name;
    }

    /**
     * Setter for channel name.
     *
     * @param name the value to set
     */
    public void setName(String name) {
        this.name = name;
    }

    public List<XmlProperty> getProperties() {
        return properties;
    }

    public void setProperties(List<XmlProperty> properties) {
        this.properties = properties;
    }

    public List<XmlTag> getTags() {
        return tags;
    }

    public void setTags(List<XmlTag> tags) {
        this.tags = tags;
    }

    /**
     * Getter for channel owner.
     *
     * @return owner
     */
    @XmlAttribute
    public String getOwner() {
        return owner;
    }

    /**
     * Setter for channel owner.
     *
     * @param owner
     */
    public void setOwner(String owner) {
        this.owner = owner;
    }

    /**
     * Getter for channel's XmlProperties.
     *
     * @return XmlProperties
     */
    public List<XmlProperty> getXmlProperties() {
        return properties;
    }

    /**
     * Setter for channel's XmlProperties.
     *
     * @param properties XmlProperties
     */
    public void setXmlProperties(ArrayList<XmlProperty> properties) {
        this.properties = properties;
    }

    /**
     * Adds an XmlProperty to the channel.
     *
     * @param property single XmlProperty
     */
    public void addXmlProperty(XmlProperty property) {
        this.properties.add(property);
    }

    /**
     * Getter for the channel's XmlTags.
     *
     * @return XmlTags for this channel
     */
    public List<XmlTag> getXmlTags() {
        return tags;
    }

    /**
     * Setter for the channel's XmlTags.
     *
     * @param tags XmlTags
     */
    public void setXmlTags(ArrayList<XmlTag> tags) {
        this.tags = tags;
    }

    /**
     * Adds an XmlTag to the collection.
     *
     * @param tag
     */
    public void addXmlTag(XmlTag tag) {
        this.tags.add(tag);
    }

    /**
     * Creates a compact string representation for the log.
     *
     * @param data XmlChannel to create the string representation for
     * @return string representation
     */
    public static String toLog(XmlChannel data) {
        return data.getName() + "(" + data.getOwner() + "):["
                + (data.properties)
                + (data.tags)
                + "]";
    }
}
