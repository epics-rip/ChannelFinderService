/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.bnl.epicsChannelFinder;

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
    private XmlProperties properties = null;
  
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
     * @param properties properties container
     */
    public XmlChannel(String name, XmlProperties properties) {
        this.name = name;
        this.properties = properties;
    }

    /**
     * Getter for name.
     *
     * @return value for name
     */
    @XmlAttribute
    public String getName() {
        return name;
    }

    /**
     * Setter for name.
     *
     * @param value the value to set
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Getter for XmlProperties.
     *
     * @return value for XmlProperties
     */
    @XmlElement(name = "properties")
    public XmlProperties getXmlProperties() {
        return properties;
    }

    /**
     * Setter for XmlProperties.
     *
     * @param value the value to set
     */
    public void setXmlProperties(XmlProperties value) {
        this.properties = value;
    }

}
