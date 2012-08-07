package gov.bnl.channelfinder;
/**
 * #%L
 * ChannelFinder Directory Service
 * %%
 * Copyright (C) 2010 - 2012 Helmholtz-Zentrum Berlin für Materialien und Energie GmbH
 * %%
 * Copyright (C) 2010 - 2012 Brookhaven National Laboratory
 * All rights reserved. Use is subject to license terms.
 * #L%
 */

import java.io.IOException;
import java.util.logging.Logger;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

/**
 * Top level Jersey HTTP methods for the .../properties URL
 *
 * @author Ralph Lange <Ralph.Lange@helmholtz-berlin.de>
 */
@Path("/properties/")
public class PropertiesResource {
    @Context
    private UriInfo uriInfo;
    @Context
    private SecurityContext securityContext;

    private Logger audit = Logger.getLogger(this.getClass().getPackage().getName() + ".audit");
    private Logger log = Logger.getLogger(this.getClass().getName());
    
    private final String propertyNameRegex = "[^\\s/]+";

    /** Creates a new instance of PropertiesResource */
    public PropertiesResource() {
    }

    /**
     * GET method for retrieving the list of properties in the database.
     *
     * @param name URI path parameter: tag name to search for
     * @return list of channels with their properties and tags that match
     */

    @GET
    @Produces({"application/xml", "application/json"})
    public Response list() {
        DbConnection db = DbConnection.getInstance();
        ChannelManager cm = ChannelManager.getInstance();
        String user = securityContext.getUserPrincipal() != null ? securityContext.getUserPrincipal().getName() : "";
        XmlProperties result = null;
        try {
            db.getConnection();
            db.beginTransaction();
            result = cm.listProperties();
            db.commit();
            Response r = Response.ok(result).build();
            log.fine(user + "|" + uriInfo.getPath() + "|GET|OK|" + r.getStatus()
                    + "|returns " + result.getProperties().size() + " properties");
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
     * POST method for creating multiple properties.
     *
     * @param data XmlProperties data (from payload)
     * @return HTTP Response
     * @throws IOException when audit or log fail
     */
    @POST
    @Consumes({"application/xml", "application/json"})
    public Response add(XmlProperties data) throws IOException {
        DbConnection db = DbConnection.getInstance();
        ChannelManager cm = ChannelManager.getInstance();
        UserManager um = UserManager.getInstance();
        um.setUser(securityContext.getUserPrincipal(), securityContext.isUserInRole("Administrator"));
        try {
            cm.checkValidNameAndOwner(data, propertyNameRegex);
            db.getConnection();
            db.beginTransaction();
            if (!um.userHasAdminRole()) {
                cm.checkUserBelongsToGroup(um.getUserName(), data);
            }
            cm.createOrReplaceProperties(data);
            db.commit();
            Response r = Response.noContent().build();
            audit.info(um.getUserName() + "|" + uriInfo.getPath() + "|POST|OK|" + r.getStatus()
                    + "|data=" + XmlProperties.toLog(data));
            return r;
        } catch (CFException e) {
            log.warning(um.getUserName() + "|" + uriInfo.getPath() + "|POST|ERROR|" + e.getResponseStatusCode()
                    + "|data=" + XmlProperties.toLog(data) + "|cause=" + e);
            return e.toResponse();
        } finally {
            db.releaseConnection();
        }
    }

    /**
     * GET method for retrieving the property with the
     * path parameter <tt>propName</tt> and its channels.
     *
     * @param prop URI path parameter: property name to search for
     * @return list of channels with their properties and tags that match
     */
    @GET
    @Path("{propName : "+propertyNameRegex+"}")
    @Produces({"application/xml", "application/json"})
    public Response read(@PathParam("propName") String prop) {
        DbConnection db = DbConnection.getInstance();
        ChannelManager cm = ChannelManager.getInstance();
        String user = securityContext.getUserPrincipal() != null ? securityContext.getUserPrincipal().getName() : "";
        XmlProperty result = null;
        try {
            db.getConnection();
            db.beginTransaction();
            result = cm.findPropertyByName(prop);
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
     * PUT method for creating and <b>exclusively</b> adding the property identified
     * by the path parameter <tt>propName</tt> to all channels identified by the
     * payload structure <tt>data</tt>.
     * Setting the owner attribute in the XML root element is mandatory.
     * Values for the properties are taken from the payload.
     *
     * @param prop URI path parameter: property name
     * @param data list of channels to add the property <tt>name</tt> to
     * @return HTTP Response
     */
    @PUT
    @Path("{propName : "+propertyNameRegex+"}")
    @Consumes({"application/xml", "application/json"})
    public Response create(@PathParam("propName") String prop, XmlProperty data) {
        DbConnection db = DbConnection.getInstance();
        ChannelManager cm = ChannelManager.getInstance();
        UserManager um = UserManager.getInstance();
        um.setUser(securityContext.getUserPrincipal(), securityContext.isUserInRole("Administrator"));
        try {
            cm.checkValidNameAndOwner(data, propertyNameRegex);
            cm.checkNameMatchesPayload(prop, data);
            cm.checkValidValue(data.getXmlChannels());
            db.getConnection();
            db.beginTransaction();
            if (!um.userHasAdminRole()) {
                cm.checkUserBelongsToGroup(um.getUserName(), data);
            }
            cm.createOrReplaceProperty(prop, data);
            db.commit();
            Response r = Response.noContent().build();
            audit.info(um.getUserName() + "|" + uriInfo.getPath() + "|PUT|OK|" + r.getStatus()
                    + "|data=" + XmlProperty.toLog(data));
            return r;
        } catch (CFException e) {
            log.warning(um.getUserName() + "|" + uriInfo.getPath() + "|PUT|ERROR|" + e.getResponseStatusCode()
                    + "|data=" + XmlProperty.toLog(data) + "|cause=" + e);
            return e.toResponse();
        } finally {
            db.releaseConnection();
        }
    }

    /**
     * POST method for updating the property identified by the path parameter <tt>name</tt>,
     * adding it to all channels identified by the payload structure <tt>data</tt>.
     * Setting the owner attribute in the XML root element is mandatory.
     * Values for the properties are taken from the payload.
     *
     * @param prop URI path parameter: property name
     * @param data list of channels to add the property <tt>name</tt> to
     * @return HTTP Response
     */
    @POST
    @Path("{propName : "+propertyNameRegex+"}")
    @Consumes({"application/xml", "application/json"})
    public Response update(@PathParam("propName") String prop, XmlProperty data) {
        DbConnection db = DbConnection.getInstance();
        ChannelManager cm = ChannelManager.getInstance();
        UserManager um = UserManager.getInstance();
        um.setUser(securityContext.getUserPrincipal(), securityContext.isUserInRole("Administrator"));
        try {
            cm.checkValidValue(data.getXmlChannels());
            db.getConnection();
            db.beginTransaction();
            if (!um.userHasAdminRole()) {
                cm.checkUserBelongsToGroupOfProperty(um.getUserName(), prop);
                cm.checkUserBelongsToGroup(um.getUserName(), data);
            }
            cm.updateProperty(prop, data);
            db.commit();
            Response r = Response.noContent().build();
            audit.info(um.getUserName() + "|" + uriInfo.getPath() + "|POST|OK|" + r.getStatus()
                    + "|data=" + XmlProperty.toLog(data));
            return r;
        } catch (CFException e) {
            log.warning(um.getUserName() + "|" + uriInfo.getPath() + "|POST|ERROR|" + e.getResponseStatusCode()
                    + "|data=" + XmlProperty.toLog(data) + "|cause=" + e);
            return e.toResponse();
        } finally {
            db.releaseConnection();
        }
    }

    /**
     * DELETE method for deleting the property identified by the path parameter
     * <tt>name</tt> from all channels.
     *
     * @param prop URI path parameter: tag name to remove
     * @return HTTP Response
     */
    @DELETE
    @Path("{propName : "+propertyNameRegex+"}")
    public Response remove(@PathParam("propName") String prop) {
        DbConnection db = DbConnection.getInstance();
        ChannelManager cm = ChannelManager.getInstance();
        UserManager um = UserManager.getInstance();
        um.setUser(securityContext.getUserPrincipal(), securityContext.isUserInRole("Administrator"));
        try {
            db.getConnection();
            db.beginTransaction();
            if (!um.userHasAdminRole()) {
                cm.checkUserBelongsToGroup(um.getUserName(), cm.findPropertyByName(prop));
            }
            cm.removeExistingProperty(prop);
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
     * PUT method for adding the property identified by <tt>prop</tt> to the channel
     * <tt>chan</tt> (both path parameters).
     *
     * @param prop URI path parameter: property name
     * @param chan URI path parameter: channel to addSingle <tt>tag</tt> to
     * @param data tag data (specifying tag ownership)
     * @return HTTP Response
     */
    @PUT
    @Path("{tagName}/{chName}")
    @Consumes({"application/xml", "application/json"})
    public Response addSingle(@PathParam("tagName") String prop, @PathParam("chName") String chan, XmlProperty data) {
        DbConnection db = DbConnection.getInstance();
        ChannelManager cm = ChannelManager.getInstance();
        UserManager um = UserManager.getInstance();
        um.setUser(securityContext.getUserPrincipal(), securityContext.isUserInRole("Administrator"));
        try {
            cm.checkNameMatchesPayload(prop, data);
            cm.checkValidValue(data);
            db.getConnection();
            db.beginTransaction();
            if (!um.userHasAdminRole()) {
                cm.checkUserBelongsToGroupOfProperty(um.getUserName(), data.getName());
            }
            cm.addSingleProperty(prop, chan, data);
            db.commit();
            Response r = Response.noContent().build();
            audit.info(um.getUserName() + "|" + uriInfo.getPath() + "|PUT|OK|" + r.getStatus()
                    + "|data=" + XmlProperty.toLog(data));
            return r;
        } catch (CFException e) {
            log.warning(um.getUserName() + "|" + uriInfo.getPath() + "|PUT|ERROR|" + e.getResponseStatusCode()
                    + "|data=" + XmlProperty.toLog(data) + "|cause=" + e);
            return e.toResponse();
        } finally {
            db.releaseConnection();
        }
    }

    /**
     * DELETE method for deleting the property identified by <tt>prop</tt> from the channel
     * <tt>chan</tt> (both path parameters).
     *
     * @param prop URI path parameter: property name to remove
     * @param chan URI path parameter: channel to remove <tt>property</tt> from
     * @return HTTP Response
     */
    @DELETE
    @Path("{propName}/{chName}")
    public Response removeSingle(@PathParam("propName") String prop, @PathParam("chName") String chan) {
        DbConnection db = DbConnection.getInstance();
        ChannelManager cm = ChannelManager.getInstance();
        UserManager um = UserManager.getInstance();
        um.setUser(securityContext.getUserPrincipal(), securityContext.isUserInRole("Administrator"));
        try {
            db.getConnection();
            db.beginTransaction();
            if (!um.userHasAdminRole()) {
                cm.checkUserBelongsToGroup(um.getUserName(), cm.findPropertyByName(prop));
            }
            cm.removeSingleProperty(prop, chan);
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
