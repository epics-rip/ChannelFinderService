/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.bnl.channelfinder;

import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import com.sun.jersey.api.core.ResourceContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;

/**
 *
 * @author rlange
 */

@Path("/channels/")
public class ChannelsResource {
    /**
     *
     */
    @Context
    protected UriInfo uriInfo;
    /**
     *
     */
    @Context
    protected ResourceContext resourceContext;
  
    /** Creates a new instance of ChannelsResource */
    public ChannelsResource() {
    }


    /**
     * Get method for retrieving a collection of Channel instances in XML format.
     *
     * @param ui
     * @return an instance of XmlChannels
     */
    @GET
    @Produces({"application/xml", "application/json"})
    public XmlChannels get(
            @Context UriInfo ui) {
            return ChannelManager.getInstance().findChannelsByPropertyMatch(ui.getQueryParameters());
    }

    /**
     * Post method for creating channel instances using XML as the input format.
     *
     * @param data an XmlChannels entity that is deserialized from a XML stream
     */
    @POST
    @Consumes({"application/xml", "application/json"})
    public void post(XmlChannels data) {
            ChannelManager.getInstance().createChannels(data);
    }

}