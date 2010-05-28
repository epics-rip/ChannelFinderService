/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
 * @author rlange
 */
@Path("/tags/{name}")
public class TagsResource {
    @Context
    private UriInfo uriInfo;
    @Context
    private SecurityContext securityContext;

    private Logger audit = Logger.getLogger(this.getClass().getPackage().getName() + ".audit");
    private Logger log = Logger.getLogger(this.getClass().getName());

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
        String user = securityContext.getUserPrincipal() != null ? securityContext.getUserPrincipal().getName() : "";
        XmlChannels result = null;
        try {
            db.getConnection();
            db.beginTransaction();
            result = AccessManager.getInstance().findChannelsByTag(name);
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
        UserManager um = UserManager.getInstance();
        um.setUser(securityContext.getUserPrincipal(), securityContext.isUserInRole("Administrator"));
        try {
            db.getConnection();
            db.beginTransaction();
            AccessManager.getInstance().putTag(name, data);
            db.commit();
            Response r = Response.noContent().build();
            audit.info(um.getUserName() + "|" + uriInfo.getPath() + "|PUT|OK|" + r.getStatus()
                    + "|data=" + XmlChannels.toLog(data));
            return r;
        } catch (CFException e) {
            log.warning(um.getUserName() + "|" + uriInfo.getPath() + "|PUT|ERROR|" + e.getResponseStatusCode()
                    + "|data=" + XmlChannels.toLog(data) + "|cause=" + e);
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
        UserManager um = UserManager.getInstance();
        um.setUser(securityContext.getUserPrincipal(), securityContext.isUserInRole("Administrator"));
        try {
            db.getConnection();
            db.beginTransaction();
            AccessManager.getInstance().addTag(name, data);
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
        UserManager um = UserManager.getInstance();
        um.setUser(securityContext.getUserPrincipal(), securityContext.isUserInRole("Administrator"));
        try {
            db.getConnection();
            db.beginTransaction();
            AccessManager.getInstance().deleteTag(name);
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
     * PUT method for adding the tag identified by <tt>tag</tt> to the channel
     * <tt>chan</tt> (both path parameters). The payload structure <tt>data</tt>
     * specifies the owner in case the tag does not exist yet.
     *
     * @param tag URI path parameter: tag name
     * @param chan URI path parameter: channel to add <tt>tag</tt> to
     * @param data tag data (specifying tag ownership)
     * @return HTTP Response
     */
    @PUT
    @Path("{chan}")
    @Consumes({"application/xml", "application/json"})
    public Response putSingle(@PathParam("name") String tag, @PathParam("chan") String chan, XmlTag data) {
        DbConnection db = DbConnection.getInstance();
        UserManager um = UserManager.getInstance();
        um.setUser(securityContext.getUserPrincipal(), securityContext.isUserInRole("Administrator"));
        try {
            db.getConnection();
            db.beginTransaction();
            AccessManager.getInstance().addSingleTag(tag, chan, data);
            db.commit();
            Response r = Response.noContent().build();
            audit.info(um.getUserName() + "|" + uriInfo.getPath() + "|PUT|OK|" + r.getStatus()
                    + "|data=" + XmlTag.toLog(data));
            return r;
        } catch (CFException e) {
            log.warning(um.getUserName() + "|" + uriInfo.getPath() + "|PUT|ERROR|" + e.getResponseStatusCode()
                    + "|data=" + XmlTag.toLog(data) + "|cause=" + e);
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
    public Response deleteSingle(@PathParam("name") String tag, @PathParam("chan") String chan) {
        DbConnection db = DbConnection.getInstance();
        UserManager um = UserManager.getInstance();
        um.setUser(securityContext.getUserPrincipal(), securityContext.isUserInRole("Administrator"));
        try {
            db.getConnection();
            db.beginTransaction();
            AccessManager.getInstance().deleteSingleTag(tag, chan);
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
