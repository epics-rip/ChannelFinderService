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

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

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

import org.codehaus.jackson.map.ObjectMapper;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

/**
 * Top level Jersey HTTP methods for the .../channels URL
 * 
 * @author Ralph Lange <Ralph.Lange@helmholtz-berlin.de>
 */

@Path("/channels/")
public class ChannelsResource {
    @Context
    private UriInfo uriInfo;
    @Context
    private SecurityContext securityContext;

    private Logger audit = Logger.getLogger(this.getClass().getPackage().getName() + ".audit");
    private Logger log = Logger.getLogger(this.getClass().getName());
    private final String chNameRegex = "[^\\s]+";
  
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
    public Response query() {
        DbConnection db = DbConnection.getInstance();
        ChannelManager cm = ChannelManager.getInstance();
        String user = securityContext.getUserPrincipal() != null ? securityContext.getUserPrincipal().getName() : "";
        try {
            db.getConnection();
            db.beginTransaction();
            XmlChannels result = cm.findChannelsByMultiMatch(uriInfo.getQueryParameters());
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
     * POST method for creating multiple channel instances.
     *
     * @param data XmlChannels data (from payload)
     * @return HTTP Response
     * @throws IOException when audit or log fail
     */
    @POST
    @Consumes({"application/xml", "application/json"})
    public Response add(XmlChannels data) throws IOException {
        DbConnection db = DbConnection.getInstance();
        ChannelManager cm = ChannelManager.getInstance();
        UserManager um = UserManager.getInstance();
        um.setUser(securityContext.getUserPrincipal(), securityContext.isUserInRole("Administrator"));
        try {
            cm.checkValidNameAndOwner(data, chNameRegex);
            cm.checkValidValue(data);
            db.getConnection();
            db.beginTransaction();
            if (!um.userHasAdminRole()) {
                cm.checkUserBelongsToGroup(um.getUserName(), data);
            }
            cm.createOrReplaceChannels(data);
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
     * GET method for retrieving an instance of Channel identified by <tt>chan</tt>.
     *
     * @param chan channel name
     * @return HTTP Response
     */
    @GET
    @Path("{chName: "+chNameRegex+"}")
    @Produces({"application/xml", "application/json"})
    public Response read(@PathParam("chName") String chan) {
        audit.info("getting ch:" + chan);
        long start = System.currentTimeMillis();
        Client client = new TransportClient().addTransportAddress(new InetSocketTransportAddress("130.199.219.147", 9300));
        System.out.println("client initialization: "+ (System.currentTimeMillis() - start));
        String user = securityContext.getUserPrincipal() != null ? securityContext.getUserPrincipal().getName() : "";
        XmlChannel result = null;
        try {
            GetResponse response = client.prepareGet("channels", "channel", chan).execute().actionGet();
            ObjectMapper mapper = new ObjectMapper();
            result = mapper.readValue(response.getSourceAsBytes(), XmlChannel.class);
            Response r;
            if (result == null) {
                r = Response.status(Response.Status.NOT_FOUND).build();
            } else {
                r = Response.ok(result).build();
            }
            log.fine(user + "|" + uriInfo.getPath() + "|GET|OK|" + r.getStatus());
            return r;
        } catch (Exception e) {
            return handleException(user, "GET", Response.Status.INTERNAL_SERVER_ERROR, e);
        } finally {
            client.close();
        }
    }

    /**
     * PUT method for creating/replacing a channel instance identified by the payload.
     * The <b>complete</b> set of properties for the channel must be supplied,
     * which will replace the existing set of properties.
     *
     * @param chan name of channel to create or add
     * @param data new data (properties/tags) for channel <tt>chan</tt>
     * @return HTTP response
     */
    @PUT
    @Path("{chName: "+chNameRegex+"}")
    @Consumes({"application/xml", "application/json"})
    public Response create(@PathParam("chName") String chan, XmlChannel data) {
        long start = System.currentTimeMillis();
        Client client = new TransportClient().addTransportAddress(new InetSocketTransportAddress("130.199.219.147", 9300));
        System.out.println("client initialization: "+ (System.currentTimeMillis() - start));
        UserManager um = UserManager.getInstance();
        um.setUser(securityContext.getUserPrincipal(), securityContext.isUserInRole("Administrator"));
        ObjectMapper mapper = new ObjectMapper();
        try {
            IndexRequest indexRequest = new IndexRequest("channels", "channel", chan)
                    .source(mapper.writeValueAsBytes(data));
            UpdateRequest updateRequest = new UpdateRequest("channels", "channel", chan)
                    .doc(mapper.writeValueAsBytes(data)).upsert(indexRequest);
            UpdateResponse result = client.update(updateRequest).get();
            Response r = Response.noContent().build();
            audit.info(um.getUserName() + "|" + uriInfo.getPath() + "|PUT|OK|" + r.getStatus() + "|data="
                    + XmlChannel.toLog(data));
            return r;
        } catch (Exception e) {
            return handleException(um.getUserName(),"PUT", Response.Status.INTERNAL_SERVER_ERROR, e);
        } finally {
            client.close();
        }
    }

    /**
     * POST method for merging properties and tags of the Channel identified by the
     * payload into an existing channel.
     *
     * @param chan name of channel to add
     * @param data new XmlChannel data (properties/tags) to be merged into channel <tt>chan</tt>
     * @return HTTP response
     */
    @POST
    @Path("{chName: "+chNameRegex+"}")
    @Consumes({"application/xml", "application/json"})
    public Response update(@PathParam("chName") String chan, XmlChannel data) {
        long start = System.currentTimeMillis();
        Client client = new TransportClient().addTransportAddress(new InetSocketTransportAddress("130.199.219.147", 9300));
        System.out.println("client initialization: "+ (System.currentTimeMillis() - start));
        UserManager um = UserManager.getInstance();
        um.setUser(securityContext.getUserPrincipal(), securityContext.isUserInRole("Administrator"));
        ObjectMapper mapper = new ObjectMapper();
        try {
            UpdateRequest updateRequest = new UpdateRequest("channels", "channel", chan)
                    .doc(mapper.writeValueAsBytes(data));
            UpdateResponse result = client.update(updateRequest).get();
            Response r = Response.noContent().build();
            audit.info(um.getUserName() + "|" + uriInfo.getPath() + "|POST|OK|" + r.getStatus()
                    + "|data=" + XmlChannel.toLog(data));
            return r;
        } catch (Exception e) {
            return handleException(um.getUserName(),"POST", Response.Status.INTERNAL_SERVER_ERROR, e);
        } finally {
            client.close();
        }
    }

    /**
     * DELETE method for deleting a channel instance identified by
     * path parameter <tt>chan</tt>.
     *
     * @param chan channel to remove
     * @return HTTP Response
     */
    @DELETE
    @Path("{chName: "+chNameRegex+"}")
    public Response remove(@PathParam("chName") String chan) {
        audit.info("deleting ch:" + chan);
        Client client = new TransportClient().addTransportAddress(new InetSocketTransportAddress("130.199.219.147", 9300));
        UserManager um = UserManager.getInstance();
        um.setUser(securityContext.getUserPrincipal(), securityContext.isUserInRole("Administrator"));
        try {
            DeleteResponse response = client.prepareDelete("channels", "channel", chan).execute().get();
            Response r = Response.ok().build();
            audit.info(um.getUserName() + "|" + uriInfo.getPath() + "|DELETE|OK|" + r.getStatus());
            return r;
        } catch (Exception e) {
            return handleException(um.getUserName(), "DELETE", Response.Status.INTERNAL_SERVER_ERROR, e);
        } finally {
            client.close();
        }
    }

    private Response handleException(String user, String requestType, Response.Status status, Exception e){
        log.warning(user + "|" + uriInfo.getPath() + "|"+requestType+"|ERROR|" + status +  "|cause=" + e);
        return new CFException(status, e.getMessage()).toResponse();
    }
}
