/*
 * Copyright (c) 2010 Brookhaven National Laboratory
 * Copyright (c) 2010 Helmholtz-Zentrum Berlin für Materialien und Energie GmbH
 * Subject to license terms and conditions.
 */
package gov.bnl.channelfinder;

import java.util.logging.Logger;
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
import javax.ws.rs.core.UriInfo;

/**
 *
 * @author Ralph Lange <Ralph.Lange@bessy.de>
 */
@Path("/channel/{name}")
public class ChannelResource {
    @Context
    private UriInfo uriInfo;
    @Context
    private SecurityContext securityContext;
    
    private Logger audit = Logger.getLogger(this.getClass().getPackage().getName() + ".audit");
    private Logger log = Logger.getLogger(this.getClass().getName());

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
        String user = securityContext.getUserPrincipal() != null ? securityContext.getUserPrincipal().getName() : "";
        XmlChannel result = null;
        try {
            db.getConnection();
            db.beginTransaction();
            result = AccessManager.getInstance().findChannelByName(name);
            db.commit();
            Response r;
            if (result == null) {
                r = Response.status(Response.Status.NOT_FOUND).build();
            } else {
                r = Response.ok(result).build();
            }
            log.fine(user + "|" + uriInfo.getPath() + "|GET|OK|" + r.getStatus());
            return r;
        } catch (CFException e) {
            log.warning(user + "|" + uriInfo.getPath() + "|GET|ERROR|"
                    + e.getResponseStatusCode() +  "|cause=" + e);
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
        UserManager um = UserManager.getInstance();
        um.setUser(securityContext.getUserPrincipal(), securityContext.isUserInRole("Administrator"));
        try {
            db.getConnection();
            db.beginTransaction();
            AccessManager.getInstance().updateChannel(name, data);
            db.commit();
            Response r = Response.noContent().build();
            audit.info(um.getUserName() + "|" + uriInfo.getPath() + "|PUT|OK|" + r.getStatus()
                    + "|data=" + XmlChannel.toLog(data));
            return r;
        } catch (CFException e) {
            log.warning(um.getUserName() + "|" + uriInfo.getPath() + "|PUT|ERROR|" + e.getResponseStatusCode()
                    + "|data=" + XmlChannel.toLog(data) + "|cause=" + e);
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
        UserManager um = UserManager.getInstance();
        um.setUser(securityContext.getUserPrincipal(), securityContext.isUserInRole("Administrator"));
        try {
            db.getConnection();
            db.beginTransaction();
            AccessManager.getInstance().mergeChannel(name, data);
            db.commit();
            Response r = Response.noContent().build();
            audit.info(um.getUserName() + "|" + uriInfo.getPath() + "|POST|OK|" + r.getStatus()
                    + "|data=" + XmlChannel.toLog(data));
            return r;
        } catch (CFException e) {
            log.warning(um.getUserName() + "|" + uriInfo.getPath() + "|POST|ERROR|" + e.getResponseStatusCode()
                    + "|data=" + XmlChannel.toLog(data) + "|cause=" + e);
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
        UserManager um = UserManager.getInstance();
        um.setUser(securityContext.getUserPrincipal(), securityContext.isUserInRole("Administrator"));
        try {
            db.getConnection();
            db.beginTransaction();
            AccessManager.getInstance().deleteChannel(name);
            db.commit();
            Response r = Response.ok().build();
            audit.info(um.getUserName() + "|" + uriInfo.getPath() + "|DELETE|OK|" + r.getStatus());
            return r;
        } catch (CFException e) {
            log.warning(um.getUserName() + "|" + uriInfo.getPath() + "|DELETE|ERROR|" + e.getResponseStatusCode()
                    + "|cause=" + e);
            return e.toResponse();
        } finally {
            db.releaseConnection();
        }
    }
}
