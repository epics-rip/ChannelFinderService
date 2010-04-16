/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.bnl.channelfinder;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import com.sun.jersey.api.core.ResourceContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

/**
 *
 * @author rlange
 */

@Path("/channel/{name}")
public class ChannelResource {
    @Context
    protected UriInfo uriInfo;
    @Context
    protected ResourceContext resourceContext;
    protected Integer id;

    /** Creates a new instance of ChannelResource */
    public ChannelResource() {
    }

    /**
     * HTTP GET method for retrieving an instance of Channel identified by name in XML format.
     *
     * @param name channel name
     * @return an instance of XmlChannel
     */
    @GET
    @Produces({"application/xml", "application/json"})
    public XmlChannel get(
            @PathParam("name") String name) {
        return AccessManager.getInstance().findChannelByName(name);
    }

    /**
     * HTTP PUT method for creating/updating an instance of Channel identified by the
     * XML input.
     * The <em>complete</em> set of properties for the channel must be supplied,
     * which will replace the existing set of properties.
     *
     * @param data an XmlChannel entity that is deserialized from a XML stream
     */
    @PUT
    @Consumes({"application/xml", "application/json"})
    public void put(@PathParam("name") String name, XmlChannel data) {
        AccessManager.getInstance().updateChannel(name, data);
    }

    /**
     * HTTP POST method for merging properties and tags of the Channel identified by the
     * XML input into an existing Channel.
     *
     * @param data an XmlChannel entity that is deserialized from a XML stream
     */
    @POST
    @Consumes({"application/xml", "application/json"})
    public void post(@PathParam("name") String name, XmlChannel data) {
        AccessManager.getInstance().mergeChannel(name, data);
    }

    /**
     * HTTP DELETE method for deleting an instance of Channel identified by
     * path parameter <tt>name</tt>.
     *
     * @param name channel to delete
     */
    @DELETE
    public void delete(@PathParam("name") String name) {
        AccessManager.getInstance().deleteChannel(name);
    }
}
