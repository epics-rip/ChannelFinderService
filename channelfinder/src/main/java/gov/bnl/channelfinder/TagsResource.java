/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.bnl.channelfinder;

import java.sql.SQLException;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.xml.ws.WebServiceException;

/**
 *
 * @author rlange
 */
@Path("/tags/{name}")
public class TagsResource {

    @Context
    private SecurityContext securityContext;

    /** Creates a new instance of TagsResource */
    public TagsResource() {
    }

    /**
     * GET method for retrieving the list of channels that are tagged with the
     * path parameter <tt>name</tt>.
     *
     * @param name URI path parameter: tag name to search for
     * @return list of channels with their properties and tags that match
     */
    @GET
    @Produces({"application/xml", "application/json"})
    public Response get(@PathParam("name") String name) {
        DbConnection db = DbConnection.getInstance();
        XmlChannels result = null;
        try {
            db.getConnection();
            db.beginTransaction();
            result = AccessManager.getInstance().findChannelsByTag(name);
            db.commit();
            return Response.ok(result).build();
        } catch (CFException e) {
            return e.toResponse();
        } finally {
            db.releaseConnection();
        }
    }

    /**
     * PUT method for <b>exclusively</b> adding the tag identified by the path parameter
     * <tt>name</tt> to all channels identified by the payload structure <tt>data</tt>.
     *
     * @param name URI path parameter: tag name
     * @param data list of channels to put the tag <tt>name</tt> on
     * @return HTTP Response
     */
    @PUT
    @Consumes({"application/xml", "application/json"})
    public Response put(@PathParam("name") String name, XmlChannels data) {
        DbConnection db = DbConnection.getInstance();
        UserManager.getInstance().setUser(securityContext.getUserPrincipal());
        try {
            db.getConnection();
            db.beginTransaction();
            OwnerMap.getInstance().loadMapFromDbForProperty(name);
            AccessManager.getInstance().putTag(name, data);
            db.commit();
            return Response.noContent().build();
        } catch (CFException e) {
            return e.toResponse();
        } finally {
            db.releaseConnection();
        }
    }

    /**
     * POST method for adding the tag identified by the path parameter <tt>name</tt>
     * to all channels identified by the payload structure <tt>data</tt>.
     *
     * @param name URI path parameter: tag name
     * @param data list of channels to add the tag <tt>name</tt> to
     * @return HTTP Response
     */
    @POST
    @Consumes({"application/xml", "application/json"})
    public Response post(@PathParam("name") String name, XmlChannels data) {
        DbConnection db = DbConnection.getInstance();
        UserManager.getInstance().setUser(securityContext.getUserPrincipal());
        try {
            db.getConnection();
            db.beginTransaction();
            OwnerMap.getInstance().loadMapFromDbForProperty(name);
            AccessManager.getInstance().addTag(name, data);
            db.commit();
            return Response.noContent().build();
        } catch (CFException e) {
            return e.toResponse();
        } finally {
            db.releaseConnection();
        }
    }

    /**
     * DELETE method for deleting the tag identified by the path parameter <tt>name</tt>
     * from all channels.
     *
     * @param name URI path parameter: tag name to delete
     * @return HTTP Response
     */
    @DELETE
    public Response delete(@PathParam("name") String name) {
        DbConnection db = DbConnection.getInstance();
        UserManager.getInstance().setUser(securityContext.getUserPrincipal());
        try {
            db.getConnection();
            db.beginTransaction();
            OwnerMap.getInstance().loadMapFromDbForProperty(name);
            AccessManager.getInstance().deleteTag(name);
            db.commit();
            return Response.ok().build();
        } catch (CFException e) {
            return e.toResponse();
        } finally {
            db.releaseConnection();
        }
    }

    /**
     * PUT method for adding the tag identified by <tt>tag</tt> to the channel
     * <tt>chan</tt> (both path parameters). The payload structure <tt>data</tt>
     * specifies the owner in case the tag does not exist yet.
     *
     * @param tag URI path parameter: tag name
     * @param chan URI path parameter: channel to add <tt>tag</tt> to
     * @param data channel data (specifying tag ownership)
     * @return HTTP Response
     */
    @PUT
    @Path("{chan}")
    @Consumes({"application/xml", "application/json"})
    public Response putSingle(@PathParam("name") String tag, @PathParam("chan") String chan, XmlChannel data) {
        DbConnection db = DbConnection.getInstance();
        UserManager.getInstance().setUser(securityContext.getUserPrincipal());
        try {
            db.getConnection();
            db.beginTransaction();
            OwnerMap.getInstance().loadMapFromDbForProperty(tag);
            AccessManager.getInstance().addSingleTag(tag, chan, data);
            db.commit();
            return Response.noContent().build();
        } catch (CFException e) {
            return e.toResponse();
        } finally {
            db.releaseConnection();
        }
    }

    /**
     * DELETE method for deleting the tag identified by <tt>tag</tt> from the channel
     * <tt>chan</tt> (both path parameters).
     *
     * @param tag URI path parameter: tag name to delete
     * @param chan URI path parameter: channel to delete <tt>tag</tt> from
     * @return HTTP Response
     */
    @DELETE
    @Path("{chan}")
    @Consumes({"application/xml", "application/json"})
    public Response deleteSingle(@PathParam("name") String tag, @PathParam("chan") String chan) {
        DbConnection db = DbConnection.getInstance();
        UserManager.getInstance().setUser(securityContext.getUserPrincipal());
        try {
            db.getConnection();
            db.beginTransaction();
            OwnerMap.getInstance().loadMapFromDbForProperty(tag);
            AccessManager.getInstance().deleteSingleTag(tag, chan);
            db.commit();
            return Response.ok().build();
        } catch (CFException e) {
            return e.toResponse();
        } finally {
            db.releaseConnection();
        }
    }
}
