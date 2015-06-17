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

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.elasticsearch.search.SearchHit;

/**
 * Top level Jersey HTTP methods for the .../tags URL
 *
 * @author Ralph Lange <Ralph.Lange@helmholtz-berlin.de>
 */
@Path("/tags/")
public class TagsResource {
    @Context
    private UriInfo uriInfo;
    @Context
    private SecurityContext securityContext;

    private Logger audit = Logger.getLogger(this.getClass().getPackage().getName() + ".audit");
    private Logger log = Logger.getLogger(this.getClass().getName());
    
    private final String tagNameRegex = "[^\\s/]+";

    /** Creates a new instance of TagsResource */
    public TagsResource() {
    }

    /**
     * GET method for retrieving the list of tags in the database.
     *
     * @param name URI path parameter: tag name to search for
     * @return list of channels with their properties and tags that match
     */

    @GET
    @Produces({"application/xml", "application/json"})
    public Response list() {
        Client client = new TransportClient().addTransportAddress(new InetSocketTransportAddress("130.199.219.147", 9300));
        String user = securityContext.getUserPrincipal() != null ? securityContext.getUserPrincipal().getName() : "";
        XmlTags result = new XmlTags();
        ObjectMapper mapper = new ObjectMapper();
        try {
            SearchResponse response = client.prepareSearch("tags").setTypes("tag").setQuery(new MatchAllQueryBuilder()).execute().actionGet();
            for (SearchHit hit : response.getHits()) {
                result.addXmlTag(mapper.readValue(hit.getSourceAsString(), XmlTag.class));
            }
            Response r = Response.ok(result).build();
            log.fine(user + "|" + uriInfo.getPath() + "|GET|OK|" + r.getStatus()
                    + "|returns " + result.getTags().size() + " tags");
            return r;
        } catch (Exception e) {
            return handleException(user,Response.Status.INTERNAL_SERVER_ERROR , e);
        } finally {
            client.close();
        }
    }

    /**
     * POST method for creating multiple tags.
     *
     * @param data XmlTags data (from payload)
     * @return HTTP Response
     * @throws IOException when audit or log fail
     */
    @POST
    @Consumes({"application/xml", "application/json"})
    public Response add(XmlTags data) throws IOException {
        Client client = new TransportClient().addTransportAddress(new InetSocketTransportAddress("130.199.219.147", 9300));
        UserManager um = UserManager.getInstance();
        um.setUser(securityContext.getUserPrincipal(), securityContext.isUserInRole("Administrator"));
        ObjectMapper mapper = new ObjectMapper();
        try {
            BulkRequestBuilder bulkRequest = client.prepareBulk();
            for (XmlTag tag : data.getTags()) {
                bulkRequest.add(client.prepareUpdate("tags", "tag", tag.getName()).setDoc(mapper.writeValueAsBytes(tag))
                        .setUpsert(new IndexRequest("tags", "tag", tag.getName()).source(mapper.writeValueAsBytes(tag))));
            }
            BulkResponse bulkResponse = bulkRequest.execute().actionGet();
            if (bulkResponse.hasFailures()) {
                return handleException("todo", Response.Status.INTERNAL_SERVER_ERROR, null);
            } else {
                Response r = Response.noContent().build();
                audit.info(um.getUserName() + "|" + uriInfo.getPath() + "|POST|OK|" + r.getStatus() + "|data="
                        + XmlTags.toLog(data));
                return r;
            }
        } catch (Exception e) {
            return handleException("todo", Response.Status.INTERNAL_SERVER_ERROR , e);
        } finally {
            client.close();
        }
    }

    /**
     * GET method for retrieving the tag with the
     * path parameter <tt>tagName</tt> and its channels.
     *
     * @param tag URI path parameter: tag name to search for
     * @return list of channels with their properties and tags that match
     */
    @GET
    @Path("{tagName: "+tagNameRegex+"}")
    @Produces({"application/xml", "application/json"})
    public Response read(@PathParam("tagName") String tag) {
        long start = System.currentTimeMillis();
        Client client = new TransportClient().addTransportAddress(new InetSocketTransportAddress("130.199.219.147", 9300));
        System.out.println("client initialization: "+ (System.currentTimeMillis() - start));
        String user = securityContext.getUserPrincipal() != null ? securityContext.getUserPrincipal().getName() : "";
        XmlTag result = null;        
        try {
            GetResponse response = client.prepareGet("tags", "tag", tag).execute().actionGet();
            ObjectMapper mapper = new ObjectMapper();
            result = mapper.readValue(response.getSourceAsBytes(), XmlTag.class);
            Response r;
            if (result == null) {
                r = Response.status(Response.Status.NOT_FOUND).build();
            } else {
                r = Response.ok(result).build();
            }
            log.fine(user + "|" + uriInfo.getPath() + "|GET|OK|" + r.getStatus());
            return r;
        } catch (JsonParseException e) {
            return handleException(user, Response.Status.INTERNAL_SERVER_ERROR , e);
        } catch (JsonMappingException e) {
            return handleException(user, Response.Status.INTERNAL_SERVER_ERROR , e);
        } catch (IOException e) {
            return handleException(user, Response.Status.INTERNAL_SERVER_ERROR , e);
        } finally {
            client.close();
        }
    }

    /**
     * PUT method to create and <b>exclusively</b> update the tag identified by the
     * path parameter <tt>name</tt> to all channels identified in the payload
     * structure <tt>data</tt>.
     * Setting the owner attribute in the XML root element is mandatory.
     *
     * @param tag URI path parameter: tag name
     * @param data XmlTag structure containing the list of channels to be tagged
     * @return HTTP Response
     */
    @PUT
    @Path("{tagName: "+tagNameRegex+"}")
    @Consumes({"application/xml", "application/json"})
    public Response create(@PathParam("tagName") String tag, XmlTag data) {
        long start = System.currentTimeMillis();
        Client client = new TransportClient().addTransportAddress(new InetSocketTransportAddress("130.199.219.147", 9300));
        System.out.println("client initialization: "+ (System.currentTimeMillis() - start));
        UserManager um = UserManager.getInstance();
        um.setUser(securityContext.getUserPrincipal(), securityContext.isUserInRole("Administrator"));
        try {
            IndexRequest indexRequest = new IndexRequest("tags", "tag", tag).source(jsonBuilder().startObject()
                    .field("name", data.getName()).field("owner", data.getOwner()).endObject());
            UpdateRequest updateRequest = new UpdateRequest("tags", "tag", tag).doc(
                    jsonBuilder().startObject()
                    .field("name", data.getName()).field("owner", data.getOwner()).endObject()).upsert(indexRequest);
            UpdateResponse result = client.update(updateRequest).get();
            Response r = Response.noContent().build();
            audit.info(um.getUserName() + "|" + uriInfo.getPath() + "|PUT|OK|" + r.getStatus() + "|data="
                    + XmlTag.toLog(data));
            return r;
        } catch (Exception e) {
            return handleException("todo", Response.Status.INTERNAL_SERVER_ERROR, e);
        } finally {
            client.close();
        }
    }

    /**
     * POST method to update the the tag identified by the path parameter <tt>name</tt>,
     * adding it to all channels identified by the channels inside the payload
     * structure <tt>data</tt>.
     * Setting the owner attribute in the XML root element is mandatory.
     *
     * @param tag URI path parameter: tag name
     * @param data list of channels to addSingle the tag <tt>name</tt> to
     * @return HTTP Response
     */
    @POST
    @Path("{tagName: "+tagNameRegex+"}")
    @Consumes({"application/xml", "application/json"})
    public Response update(@PathParam("tagName") String tag, XmlTag data) {
        long start = System.currentTimeMillis();
        Client client = new TransportClient().addTransportAddress(new InetSocketTransportAddress("130.199.219.147", 9300));
        System.out.println("client initialization: "+ (System.currentTimeMillis() - start));
        UserManager um = UserManager.getInstance();
        um.setUser(securityContext.getUserPrincipal(), securityContext.isUserInRole("Administrator"));
        try {
            UpdateRequest updateRequest = new UpdateRequest("tags", "tag", tag).doc(jsonBuilder().startObject()
                    .field("name", data.getName()).field("owner", data.getOwner()).endObject());
            UpdateResponse result = client.update(updateRequest).get();
            Response r = Response.noContent().build();
            audit.info(um.getUserName() + "|" + uriInfo.getPath() + "|PUT|OK|" + r.getStatus() + "|data="
                    + XmlTag.toLog(data));
            return r;
        } catch (Exception e) {
            return handleException(um.getUserName() , Response.Status.INTERNAL_SERVER_ERROR, e);
        } finally {
            client.close();
        }
    }

    /**
     * DELETE method for deleting the tag identified by the path parameter <tt>name</tt>
     * from all channels.
     *
     * @param tag URI path parameter: tag name to remove
     * @return HTTP Response
     */
    @DELETE
    @Path("{tagName: "+tagNameRegex+"}")
    public Response remove(@PathParam("tagName") String tag) {
        Client client = new TransportClient().addTransportAddress(new InetSocketTransportAddress("130.199.219.147", 9300));
        UserManager um = UserManager.getInstance();
        um.setUser(securityContext.getUserPrincipal(), securityContext.isUserInRole("Administrator"));
        try {
            DeleteResponse response = client.prepareDelete("tags", "tag", tag).execute().get();
            Response r = Response.ok().build();
            audit.info(um.getUserName() + "|" + uriInfo.getPath() + "|DELETE|OK|" + r.getStatus());
            return r;
        } catch (Exception e) {
            return handleException("todo", Response.Status.INTERNAL_SERVER_ERROR, e);
        } finally {
            client.close();
        }
    }

    /**
     * PUT method for adding the tag identified by <tt>tag</tt> to the single channel
     * <tt>chan</tt> (both path parameters).
     *
     * @param tag URI path parameter: tag name
     * @param chan URI path parameter: channel to update <tt>tag</tt> to
     * @param data tag data (ignored)
     * @return HTTP Response
     */
    @PUT
    @Path("{tagName}/{chName}")
    @Consumes({"application/xml", "application/json"})
    public Response addSingle(@PathParam("tagName") String tag, @PathParam("chName") String chan, XmlTag data) {
       //TODO
        return null;
    }

    /**
     * DELETE method for deleting the tag identified by <tt>tag</tt> from the channel
     * <tt>chan</tt> (both path parameters).
     *
     * @param tag URI path parameter: tag name to remove
     * @param chan URI path parameter: channel to remove <tt>tag</tt> from
     * @return HTTP Response
     */
    @DELETE
    @Path("{tagName}/{chName}")
    public Response removeSingle(@PathParam("tagName") String tag, @PathParam("chName") String chan) {
//        DbConnection db = DbConnection.getInstance();
//        ChannelManager cm = ChannelManager.getInstance();
//        UserManager um = UserManager.getInstance();
//        um.setUser(securityContext.getUserPrincipal(), securityContext.isUserInRole("Administrator"));
//        try {
//            db.getConnection();
//            db.beginTransaction();
//            if (!um.userHasAdminRole()) {
//                cm.checkUserBelongsToGroup(um.getUserName(), cm.findTagByName(tag));
//            }
//            cm.removeSingleTag(tag, chan);
//            db.commit();
//            Response r = Response.ok().build();
//            audit.info(um.getUserName() + "|" + uriInfo.getPath() + "|DELETE|OK|" + r.getStatus());
//            return r;
//        } catch (CFException e) {
//            log.warning(um.getUserName() + "|" + uriInfo.getPath() + "|DELETE|ERROR|" + e.getResponseStatusCode()
//                    + "|cause=" + e);
//            return e.toResponse();
//        } finally {
//            db.releaseConnection();
//        }
        //TODO
        return null;
    }

    private Response handleException(String user, Response.Status status, Exception e){
        log.warning(user + "|" + uriInfo.getPath() + "|GET|ERROR|" + status +  "|cause=" + e);
        return new CFException(status, e.getMessage()).toResponse();
    }
}
