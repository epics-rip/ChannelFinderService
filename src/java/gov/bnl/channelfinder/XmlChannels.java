/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.bnl.channelfinder;

import java.util.ArrayList;
import java.util.Collection;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlElement;

/**
 *
 * @author rlange
 */

@XmlRootElement(name = "channels")
public class XmlChannels {
    private Collection<XmlChannel> items = new ArrayList<XmlChannel>();
  
    /** Creates a new instance of XmlChannels */
    public XmlChannels() {
    }

    /**
     * Returns a collection of XmlChannel.
     *
     * @return a collection of XmlChannel
     */
    @XmlElement
    public Collection<XmlChannel> getChannelCollection() {
        return items;
    }

    /**
     * Adds a channel to the channel collection.
     *
     * @param item the XmlChannel to add
     */
    public void addChannel(XmlChannel item) {
        this.items.add(item);
    }

}
