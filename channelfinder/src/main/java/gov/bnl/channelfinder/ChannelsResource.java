/*
 * Copyright (c) 2010 Brookhaven National Laboratory
 * Copyright (c) 2010 Helmholtz-Zentrum Berlin für Materialien und Energie GmbH
 * Subject to license terms and conditions.
 */

package gov.bnl.channelfinder;

import java.io.IOException;
import java.util.logging.Logger;
import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

/**
 *
 * @author Ralph Lange <Ralph.Lange@bessy.de>
 */

@Path("/channels/")
public class ChannelsResource {
    @Context
    private UriInfo uriInfo;
    @Context
    private SecurityContext securityContext;

    private Logger audit = Logger.getLogger(this.getClass().getPackage().getName() + ".audit");
    private Logger log = Logger.getLogger(this.getClass().getName());
  
    /** Creates a new instance of ChannelsResource */
    public ChannelsResource() {
    }

    /**
     * GET method for retrieving a collection of Channel instances,
     * based on a multi-parameter query specifiying patterns for tags, property values,
     * and channel names to match against.
     *
     * @return HTTP Response
     */
    @GET
    @Produces({"application/xml", "application/json"})
    public Response get() {
        DbConnection db = DbConnection.getInstance();
        String user = securityContext.getUserPrincipal() != null ? securityContext.getUserPrincipal().getName() : "";
        try {
            db.getConnection();
            db.beginTransaction();
            XmlChannels result = AccessManager.getInstance().findChannelsByMultiMatch(uriInfo.getQueryParameters());
            db.commit();
            Response r = Response.ok(result).build();
            log.fine(user + "|" + uriInfo.getPath() + "|GET|OK|" + r.getStatus()
                    + "|returns " + result.getChannels().size() + " channels");
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
     * POST method for creating channel instances.
     *
     * @param data channels data (from payload)
     * @return HTTP Response
     */
    @POST
    @Consumes({"application/xml", "application/json"})
    public Response post(XmlChannels data) throws IOException {
        DbConnection db = DbConnection.getInstance();
        UserManager um = UserManager.getInstance();
        um.setUser(securityContext.getUserPrincipal(), securityContext.isUserInRole("Administrator"));
        try {
            db.getConnection();
            db.beginTransaction();
            AccessManager.getInstance().createChannels(data);
            db.commit();
            Response r = Response.noContent().build();
            audit.info(um.getUserName() + "|" + uriInfo.getPath() + "|POST|OK|" + r.getStatus()
                    + "|data=" + XmlChannels.toLog(data));
            return r;
        } catch (CFException e) {
            log.warning(um.getUserName() + "|" + uriInfo.getPath() + "|POST|ERROR|" + e.getResponseStatusCode()
                    + "|data=" + XmlChannels.toLog(data) + "|cause=" + e);
            return e.toResponse();
        } finally {
            db.releaseConnection();
        }
    }
}