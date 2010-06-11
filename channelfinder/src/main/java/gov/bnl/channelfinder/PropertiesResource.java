/*
 * Copyright (c) 2010 Brookhaven National Laboratory
 * Copyright (c) 2010 Helmholtz-Zentrum Berlin für Materialien und Energie GmbH
 * Subject to license terms and conditions.
 */
package gov.bnl.channelfinder;

import java.util.logging.Logger;
import javax.ws.rs.core.Context;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

/**
 *
 * @author Ralph Lange <Ralph.Lange@bessy.de>
 */
@Path("/properties/{name}")
public class PropertiesResource {
    @Context
    private UriInfo uriInfo;
    @Context
    private SecurityContext securityContext;

    private Logger audit = Logger.getLogger(this.getClass().getPackage().getName() + ".audit");
    private Logger log = Logger.getLogger(this.getClass().getName());

    /** Creates a new instance of PropertiesResource */
    public PropertiesResource() {
    }

    /**
     * DELETE method for deleting the property identified by the path parameter <tt>name</tt>
     * from all channels.
     *
     * @param name URI path parameter: tag name to delete
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
            AccessManager.getInstance().deleteProperty(name);
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

    /**
     * DELETE method for deleting the property identified by <tt>property</tt> from the channel
     * <tt>chan</tt> (both path parameters).
     *
     * @param property URI path parameter: property name to delete
     * @param chan URI path parameter: channel to delete <tt>property</tt> from
     * @return HTTP Response
     */
    @DELETE
    @Path("{chan}")
    public Response deleteSingle(@PathParam("name") String property, @PathParam("chan") String chan) {
        DbConnection db = DbConnection.getInstance();
        UserManager um = UserManager.getInstance();
        um.setUser(securityContext.getUserPrincipal(), securityContext.isUserInRole("Administrator"));
        try {
            db.getConnection();
            db.beginTransaction();
            AccessManager.getInstance().deleteSingleProperty(property, chan);
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
