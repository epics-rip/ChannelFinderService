/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.bnl.channelfinder;

import java.sql.SQLException;
import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.xml.ws.WebServiceException;

/**
 *
 * @author rlange
 */

@Path("/channels/")
public class ChannelsResource {
    @Context
    private UriInfo uriInfo;
    @Context
    private SecurityContext securityContext;
  
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
        try {
            db.getConnection();
            db.beginTransaction();
            XmlChannels result = AccessManager.getInstance().findChannelsByMultiMatch(uriInfo.getQueryParameters());
            db.commit();
            return Response.ok(result).build();
        } catch (CFException e) {
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
    public Response post(XmlChannels data) {
        DbConnection db = DbConnection.getInstance();
        UserManager.getInstance().setUser(securityContext.getUserPrincipal());
        try {
            db.getConnection();
            db.beginTransaction();
            DbOwnerMap.getInstance().loadMapsFor(data);
            AccessManager.getInstance().createChannels(data);
            db.commit();
            return Response.noContent().build();
        } catch (CFException e) {
            return e.toResponse();
        } finally {
            db.releaseConnection();
        }
    }
}