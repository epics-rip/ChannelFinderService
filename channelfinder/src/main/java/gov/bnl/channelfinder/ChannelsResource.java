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
    private DbConnection db = DbConnection.getInstance();
  
    /** Creates a new instance of ChannelsResource */
    public ChannelsResource() {
    }

    /**
     * GET method for retrieving a collection of Channel instances,
     * based on a multi-parameter query specifiying patterns for tags, property values,
     * and channel names to match against.
     *
     * @return matching channels with their properties and tags
     */
    @GET
    @Produces({"application/xml", "application/json"})
    public XmlChannels get() {
        XmlChannels result = null;
        try {
            db.getConnection();
            db.beginTransaction();
            result = AccessManager.getInstance().findChannelsByMultiMatch(uriInfo.getQueryParameters());
            db.commit();
        } catch (SQLException e) {
            throw new WebServiceException("SQLException during channels GET operation", e);
        } finally {
            db.releaseConnection();
        }
        return result;
    }

    /**
     * POST method for creating channel instances.
     *
     * @param data channels data (from payload)
     */
    @POST
    @Consumes({"application/xml", "application/json"})
    public void post(XmlChannels data) {
        UserManager.getInstance().setUser(securityContext.getUserPrincipal());
        try {
            db.getConnection();
            db.beginTransaction();
            AccessManager.getInstance().createChannels(data);
            db.commit();
        } catch (SQLException e) {
            throw new WebServiceException("SQLException during channels POST operation", e);
        } finally {
            db.releaseConnection();
        }
    }
}