/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.bnl.channelfinder;

import java.util.ArrayList;
import java.util.Collection;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author rlange
 */
@XmlType(name = "tags")
public class XmlTags {
    private Collection<XmlTag> items = new ArrayList<XmlTag>();

    /** Creates a new instance of XmlTags */
    public XmlTags() {
    }

    /**
     * Creates a new instance of XmlTags.
     *
     * @param items
     */
    public XmlTags(Collection<XmlTag> items) {
        this.items = items;
    }

    /**
     * Returns a collection of XmlTag.
     *
     * @return a collection of XmlTag
     */
    @XmlElement
    public Collection<XmlTag> getTag() {
        return items;
    }

    /**
     * Sets a collection of XmlTag.
     *
     * @param items
     */
    public void setTag(Collection<XmlTag> items) {
        this.items = items;
    }

    /**
     * Adds an XmlTag to the collection.
     *
     * @param item
     */
    public void addTag(XmlTag item) {
        this.items.add(item);
    }

}
