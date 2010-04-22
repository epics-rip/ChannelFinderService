/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.bnl.channelfinder;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.SecurityContext;

/**
 *
 * @author rlange
 */

@Path("/tags/{name}")
public class TagsResource {
    @Context
    protected SecurityContext securityContext;

    /** Creates a new instance of TagsResource */
    public TagsResource() {
    }

    /**
     * HTTP GET method for retrieving the list of Channels that are tagged with <tt>name</tt>.
     *
     * @param name tag name
     * @return an instance of XmlChannels
     */
    @GET
    @Produces({"application/xml", "application/json"})
    public XmlChannels get(@PathParam("name") String name) {
        return AccessManager.getInstance().findChannelsByTag(name);
    }

    /**
     * HTTP PUT method for exclusively adding the tag identified by <tt>name</tt>
     * to all Channels identified by the XML structure <tt>data</tt>.
     *
     * @param name tag name
     * @param data an XmlChannel entity that is deserialized from a XML stream
     */
    @PUT
    @Consumes({"application/xml", "application/json"})
    public void put(@PathParam("name") String name, XmlChannels data) {
        UserManager.getInstance().setUser(securityContext.getUserPrincipal());
        AccessManager.getInstance().putTag(name, data);
    }

    /**
     * HTTP POST method for adding the tag identified by <tt>name</tt> to all Channels
     * identified by the XML structure <tt>data</tt>.
     *
     * @param name tag name
     * @param data XmlChannels list that is deserialized from a XML stream
     */
    @POST
    @Consumes({"application/xml", "application/json"})
    public void post(@PathParam("name") String name, XmlChannels data) {
        UserManager.getInstance().setUser(securityContext.getUserPrincipal());
        AccessManager.getInstance().addTag(name, data);
    }

    /**
     * HTTP DELETE method for deleting a tag identified by the
     * path parameter <tt>name</tt> from all Channels.
     *
     * @param name tag to delete
     */
    @DELETE
    public void delete(@PathParam("name") String name) {
        UserManager.getInstance().setUser(securityContext.getUserPrincipal());
        AccessManager.getInstance().deleteTag(name);
    }

    /**
     * HTTP PUT method for adding the tag identified by <tt>name</tt>
     * to the channel <tt>chan</tt>, with the XML structure <tt>data</tt> specifying the owner for a new tag.
     *
     * @param name URI path parameter: tag name
     * @param chan URI path parameter: channel name
     * @param data an XmlChannel entity that is deserialized from a XML stream
     */
    @PUT
    @Path("{chan}")
    @Consumes({"application/xml", "application/json"})
    public void putSingle(@PathParam("name") String name, @PathParam("chan") String chan, XmlChannel data) {
        UserManager.getInstance().setUser(securityContext.getUserPrincipal());
        AccessManager.getInstance().addSingleTag(name, chan, data);
    }

    /**
     * HTTP DELETE method for adding the tag identified by <tt>name</tt>
     * from the channel <tt>chan</tt>.
     *
     * @param name URI path parameter: tag name
     * @param chan URI path parameter: channel name
     */
    @DELETE
    @Path("{chan}")
    @Consumes({"application/xml", "application/json"})
    public void deleteSingle(@PathParam("name") String name, @PathParam("chan") String chan) {
        UserManager.getInstance().setUser(securityContext.getUserPrincipal());
        AccessManager.getInstance().deleteSingleTag(name, chan);
    }
}
