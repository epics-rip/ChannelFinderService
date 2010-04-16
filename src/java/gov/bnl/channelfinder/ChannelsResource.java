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
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.core.SecurityContext;

/**
 *
 * @author rlange
 */

@Path("/channels/")
public class ChannelsResource {
    @Context
    protected UriInfo uriInfo;
    @Context
    protected SecurityContext securityContext;
  
    /** Creates a new instance of ChannelsResource */
    public ChannelsResource() {
    }

    /**
     * Get method for retrieving a collection of Channel instances in XML format.
     *
     * @return an instance of XmlChannels
     */
    @GET
    @Produces({"application/xml", "application/json"})
    public XmlChannels get() {
        return AccessManager.getInstance().findChannelsByPropertyMatch(uriInfo.getQueryParameters());
    }

    /**
     * Post method for creating channel instances using XML as the input format.
     *
     * @param data an XmlChannels entity that is deserialized from a XML stream
     */
    @POST
    @Consumes({"application/xml", "application/json"})
    public void post(XmlChannels data) {
        UserManager.getInstance().setUser(securityContext.getUserPrincipal());
        AccessManager.getInstance().createChannels(data);
    }

}