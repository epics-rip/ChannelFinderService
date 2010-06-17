/*
 * Copyright (c) 2010 Brookhaven National Laboratory
 * Copyright (c) 2010 Helmholtz-Zentrum Berlin für Materialien und Energie GmbH
 * Subject to license terms and conditions.
 */

package gov.bnl.channelfinder;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Tag object that can be represented as XML/JSON in payload data.
 *
 * @author Ralph Lange <Ralph.Lange@bessy.de>
 */
@XmlType(propOrder = {"name","owner"})
@XmlRootElement(name = "tag")
public class XmlTag {
    private String name = null;
    private String owner = null;

    /**
     * Creates a new instance of XmlTag.
     *
     */
    public XmlTag() {
    }

    /**
     * Creates a new instance of XmlTag.
     *
     * @param name
     * @param owner
     */
    public XmlTag(String name, String owner) {
        this.owner = owner;
        this.name = name;
    }

    /**
     * Getter for tag name.
     *
     * @return tag name
     */
    @XmlAttribute
    public String getName() {
        return name;
    }

    /**
     * Setter for tag name.
     *
     * @param name tag name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Getter for tag owner.
     *
     * @return tag owner
     */
    @XmlAttribute
    public String getOwner() {
        return owner;
    }

    /**
     * Setter for tag owner.
     *
     * @param owner tag owner
     */
    public void setOwner(String owner) {
        this.owner = owner;
    }

    /**
     * Creates a compact string representation for the log.
     *
     * @param data the XmlTag to log
     * @return string representation for log
     */
    public static String toLog(XmlTag data) {
        return data.getName() + "(" + data.getOwner() + ")";
    }
}
