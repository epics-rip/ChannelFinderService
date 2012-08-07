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
import java.util.Collection;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlElement;

/**
 * Channels (collection) object that can be represented as XML/JSON in payload data.
 *
 * @author Ralph Lange <Ralph.Lange@helmholtz-berlin.de>
 */

@XmlRootElement(name = "properties")
public class XmlProperties {
    private Collection<XmlProperty> properties = new ArrayList<XmlProperty>();
  
    /** Creates a new instance of XmlProperties. */
    public XmlProperties() {
    }

    /** Creates a new instance of XmlProperties with one initial property.
     * @param c initial element
     */
    public XmlProperties(XmlProperty p) {
        properties.add(p);
    }

    /**
     * Returns a collection of XmlProperty.
     *
     * @return a collection of XmlProperty
     */
    @XmlElement(name = "property")
    public Collection<XmlProperty> getProperties() {
        return properties;
    }

    /**
     * Sets the collection of properties.
     *
     * @param items new property collection
     */
    public void setProperties(Collection<XmlProperty> items) {
        this.properties = items;
    }

    /**
     * Adds a property to the property collection.
     *
     * @param item the XmlProperty to add
     */
    public void addXmlProperty(XmlProperty item) {
        this.properties.add(item);
    }

    /**
     * Removes a property from the property collection.
     *
     * @param item the XmlProperty to remove
     */
    public void removeXmlProperty(XmlProperty item) {
        this.properties.remove(item);
    }

    /**
     * Creates a compact string representation for the log.
     *
     * @param data XmlChannel to create the string representation for
     * @return string representation
     */
    public static String toLog(XmlProperties data) {
        if (data.getProperties().size() == 0) {
            return "[None]";
        } else {
            StringBuilder s = new StringBuilder();
            s.append("[");
            for (XmlProperty p : data.getProperties()) {
                s.append(XmlProperty.toLog(p) + ",");
            }
            s.delete(s.length()-1, s.length());
            s.append("]");
            return s.toString();
        }
    }
}
