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
@Path("/channel/{name}")
public class ChannelResource {

    @Context
    private SecurityContext securityContext;

    /** Creates a new instance of ChannelResource */
    public ChannelResource() {
    }

    /**
     * GET method for retrieving an instance of Channel identified by <tt>name</tt>.
     *
     * @param name channel name
     * @return HTTP Response
     */
    @GET
    @Produces({"application/xml", "application/json"})
    public Response get(@PathParam("name") String name) {
        DbConnection db = DbConnection.getInstance();
        XmlChannel result = null;
        try {
            db.getConnection();
            db.beginTransaction();
            result = AccessManager.getInstance().findChannelByName(name);
            db.commit();
            if (result == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            } else {
                return Response.ok(result).build();
            }
        } catch (CFException e) {
            return e.toResponse();
        } finally {
            db.releaseConnection();
        }
    }

    /**
     * PUT method for creating/updating a channel instance identified by the payload.
     * The <b>complete</b> set of properties for the channel must be supplied,
     * which will replace the existing set of properties.
     *
     * @param name name of channel to create or update
     * @param data new data (properties/tags) for channel <tt>name</tt>
     * @return HTTP response
     */
    @PUT
    @Consumes({"application/xml", "application/json"})
    public Response put(@PathParam("name") String name, XmlChannel data) {
        DbConnection db = DbConnection.getInstance();
        UserManager.getInstance().setUser(securityContext.getUserPrincipal());
        try {
            db.getConnection();
            db.beginTransaction();
            DbOwnerMap.getInstance().loadMapsFor(data);
            AccessManager.getInstance().updateChannel(name, data);
            db.commit();
            return Response.noContent().build();
        } catch (CFException e) {
            return e.toResponse();
        } finally {
            db.releaseConnection();
        }
    }

    /**
     * POST method for merging properties and tags of the Channel identified by the
     * payload into an existing channel.
     *
     * @param name name of channel to update
     * @param data new data (properties/tags) to be merged into channel <tt>name</tt>
     * @return HTTP response
     */
    @POST
    @Consumes({"application/xml", "application/json"})
    public Response post(@PathParam("name") String name, XmlChannel data) {
        DbConnection db = DbConnection.getInstance();
        UserManager.getInstance().setUser(securityContext.getUserPrincipal());
        try {
            db.getConnection();
            db.beginTransaction();
            DbOwnerMap.getInstance().loadMapsFor(data);
            AccessManager.getInstance().mergeChannel(name, data);
            db.commit();
            return Response.noContent().build();
        } catch (CFException e) {
            return e.toResponse();
        } finally {
            db.releaseConnection();
        }
    }

    /**
     * DELETE method for deleting a channel instance identified by
     * path parameter <tt>name</tt>.
     *
     * @param name channel to delete
     * @return HTTP Response
     */
    @DELETE
    public Response delete(@PathParam("name") String name) {
        DbConnection db = DbConnection.getInstance();
        UserManager.getInstance().setUser(securityContext.getUserPrincipal());
        try {
            db.getConnection();
            db.beginTransaction();
            DbOwnerMap.getInstance().loadMapForChannel(name);
            AccessManager.getInstance().deleteChannel(name);
            db.commit();
            return Response.ok().build();
        } catch (CFException e) {
            return e.toResponse();
        } finally {
            db.releaseConnection();
        }
    }
}
