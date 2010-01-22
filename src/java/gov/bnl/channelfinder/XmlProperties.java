/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.bnl.channelfinder;

import java.util.ArrayList;
import java.util.Collection;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author rlange
 */
@XmlRootElement(name = "properties")
public class XmlProperties {
    private Collection<XmlProperty> items = new ArrayList<XmlProperty>();

    /** Creates a new instance of XmlProperties */
    public XmlProperties() {
    }

    /**
     * Creates a new instance of XmlProperties.
     *
     * @param items
     */
    public XmlProperties(Collection<XmlProperty> items) {
        this.items = items;
    }

    /**
     * Returns a collection of XmlProperty.
     *
     * @return a collection of XmlProperty
     */
    @XmlElement
    public Collection<XmlProperty> getProperty() {
        return items;
    }

    /**
     * Sets a collection of XmlProperty.
     *
     * @param items
     */
    public void setProperty(Collection<XmlProperty> items) {
        this.items = items;
    }

    /**
     * Adds an XmlProperty to the collection.
     *
     * @param item
     */
    public void addProperty(XmlProperty item) {
        this.items.add(item);
    }

}
