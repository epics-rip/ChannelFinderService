/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.bnl.epicsChannelFinder;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author rlange
 */
@XmlRootElement(name = "property")
public class XmlProperty {
    private String name = null;
    private String value = null;

    /**
     * Creates a new instance of XmlProperty.
     *
     */
    public XmlProperty() {
    }

    /**
     * Creates a new instance of XmlProperty.
     *
     * @param name
     * @param value
     */
    public XmlProperty(String name, String value) {
        this.value = value;
        this.name = name;
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
     * Getter for value.
     *
     * @return value for value
     */
    @XmlAttribute
    public String getValue() {
        return value;
    }

    /**
     * Setter for value.
     *
     * @param value the value to set
     */
    public void setValue(String value) {
        this.value = value;
    }

}
