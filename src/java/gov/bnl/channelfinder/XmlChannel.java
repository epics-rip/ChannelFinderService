/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.bnl.channelfinder;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAttribute;

/**
 *
 * @author rlange
 */

@XmlRootElement(name = "channel")
public class XmlChannel {
    private String name = null;
    private String owner = null;
    private XmlProperties properties = null;
    private XmlTags tags = null;
  
    /** Creates a new instance of XmlChannel */
    public XmlChannel() {
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
     * Creates a new instance of XmlChannel.
     *
     * @param name channel name
     * @param owner owner name
     * @param properties properties container
     */
    public XmlChannel(String name, String owner, XmlProperties properties) {
        this.name = name;
        this.owner = owner;
        this.properties = properties;
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
    @XmlElement(name = "properties")
    public XmlProperties getXmlProperties() {
        return properties;
    }

    /**
     * Setter for channel's XmlProperties.
     *
     * @param properties
     */
    public void setXmlProperties(XmlProperties properties) {
        this.properties = properties;
    }

    /**
     * Getter for the channel's XmlTags.
     *
     * @return the XmlTags for this channel
     */
    @XmlElement(name = "tags")
    public XmlTags getXmlTags() {
        return tags;
    }

    /**
     * Setter for the channel's XmlTags.
     *
     * @param tags XmlTags object to set
     */
    public void setXmlTags(XmlTags tags) {
        this.tags = tags;
    }

}
