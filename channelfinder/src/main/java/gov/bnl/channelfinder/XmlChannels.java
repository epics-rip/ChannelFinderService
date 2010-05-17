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
  
    /** Creates a new instance of XmlChannels. */
    public XmlChannels() {
    }

    /** Creates a new instance of XmlChannels with one initial channel.
     * @param c initial element
     */
    public XmlChannels(XmlChannel c) {
        items.add(c);
    }

    /**
     * Returns a collection of XmlChannel.
     *
     * @return a collection of XmlChannel
     */
    @XmlElement(name = "channel")
    public Collection<XmlChannel> getChannels() {
        return items;
    }

    /**
     * Sets the collection of channels.
     *
     * @param items new channel collection
     */
    public void setChannels(Collection<XmlChannel> items) {
        this.items = items;
    }

    /**
     * Adds a channel to the channel collection.
     *
     * @param item the XmlChannel to add
     */
    public void addChannel(XmlChannel item) {
        this.items.add(item);
    }

    /**
     * Creates a compact string representation for the log.
     *
     * @param item the XmlChannel to add
     */
    public static String toLog(XmlChannels data) {
        if (data.getChannels().size() == 0) {
            return "[None]";
        } else {
            StringBuilder s = new StringBuilder();
            s.append("[");
            for (XmlChannel c : data.getChannels()) {
                s.append(XmlChannel.toLog(c) + ",");
            }
            s.delete(s.length()-1, s.length());
            s.append("]");
            return s.toString();
        }
    }
}
