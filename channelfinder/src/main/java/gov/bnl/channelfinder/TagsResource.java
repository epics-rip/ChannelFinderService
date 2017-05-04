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
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.wildcardQuery;
import static gov.bnl.channelfinder.ElasticSearchClient.getNewClient;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;

import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.engine.DocumentMissingException;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;

import com.fasterxml.jackson.annotation.JsonIgnoreType;
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import gov.bnl.channelfinder.ChannelsResource.OnlyXmlTag;

/**
 * Top level Jersey HTTP methods for the .../tags URL
 *
 * @author Kunal Shroff {@literal <shroffk@bnl.gov>}, Ralph Lange {@literal <ralph.lange@gmx.de>}
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
     * @return list of tags
     */
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public Response list() {
        Client client = getNewClient();
        String user = securityContext.getUserPrincipal() != null ? securityContext.getUserPrincipal().getName() : "";
        final ObjectMapper mapper = new ObjectMapper();
        mapper.addMixIn(XmlTag.class, OnlyXmlTag.class);
        try {
            MultivaluedMap<String, String> parameters = uriInfo.getQueryParameters();
            int size = 10000;
            if (parameters.containsKey("~size")) {
                Optional<String> maxSize = parameters.get("~size").stream().max((o1, o2) -> {
                    return Integer.valueOf(o1).compareTo(Integer.valueOf(o2));
                });
                if (maxSize.isPresent()) {
                    size = Integer.valueOf(maxSize.get());
                }

            }
            final SearchResponse response = client.prepareSearch("tags")
                                                  .setTypes("tag")
                                                  .setQuery(new MatchAllQueryBuilder())
                                                  .setSize(size)
                                                  .execute().actionGet();
            StreamingOutput stream = new StreamingOutput(){
                @Override
                public void write(OutputStream os) throws IOException, WebApplicationException {
                    JsonGenerator jg = mapper.getFactory().createGenerator(os, JsonEncoding.UTF8);
                    jg.writeStartArray();
                    if(response != null){
                        for (SearchHit hit : response.getHits()) {
                            jg.writeObject(mapper.readValue(hit.source(), XmlTag.class));
                        }
                    }
                    jg.writeEndArray();
                    jg.flush();
                    jg.close();
                }
            };
            Response r = Response.ok(stream).build();
            log.fine(user + "|" + uriInfo.getPath() + "|GET|OK|" + r.getStatus() + response.getTook() + "|returns " + response.getHits().getTotalHits() + " tags");
            return r;
        } catch (Exception e) {
            return handleException(user,Response.Status.INTERNAL_SERVER_ERROR , e);
        } finally {
            client.close();
        }
    }

    /**
     * GET method for retrieving the tag with the
     * path parameter <tt>tagName</tt> 
     * 
     * To get all its channels use the parameter "withChannels"
     *
     * @param tag URI path parameter: tag name to search for
     * @return list of channels with their properties and tags that match
     */
    @GET
    @Path("{tagName: "+tagNameRegex+"}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response read(@PathParam("tagName") String tag) {
        MultivaluedMap<String, String> parameters = uriInfo.getQueryParameters();
        long start = System.currentTimeMillis();
        Client client = getNewClient();
        audit.info("client initialization: "+ (System.currentTimeMillis() - start));
        String user = securityContext.getUserPrincipal() != null ? securityContext.getUserPrincipal().getName() : "";
        XmlTag result = null;        
        try {
            GetResponse response = client.prepareGet("tags", "tag", tag).execute().actionGet();
            if (response.isExists()) {
                ObjectMapper mapper = new ObjectMapper();
                result = mapper.readValue(response.getSourceAsBytes(), XmlTag.class);
                Response r;
                if (result == null) {
                    r = Response.status(Response.Status.NOT_FOUND).build();
                } else {
                    if (parameters.containsKey("withChannels")) {
                        // TODO iterator or scrolling needed
                        final SearchResponse channelResult = client.prepareSearch("channelfinder")
                                .setQuery(matchQuery("tags.name", tag.trim())).setSize(10000).execute().actionGet();
                        List<XmlChannel> channels = new ArrayList<XmlChannel>();
                        if (channelResult != null) {
                            for (SearchHit hit : channelResult.getHits()) {
                                channels.add(mapper.readValue(hit.source(), XmlChannel.class));
                            }
                        }
                        result.setChannels(channels);
                    }
                    r = Response.ok(result).build();
                }
                log.fine(user + "|" + "|" + uriInfo.getPath() + "|GET|OK|" + r.getStatus());
                return r;
            } else {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
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
    @Consumes({MediaType.APPLICATION_JSON})
    public Response create(@PathParam("tagName") String tag, XmlTag data) {
        long start = System.currentTimeMillis();
        Client client = getNewClient();
        audit.info("client initialization: "+ (System.currentTimeMillis() - start));
        UserManager um = UserManager.getInstance();
        um.setUser(securityContext.getUserPrincipal(), securityContext.isUserInRole("Administrator"));
        try {
            if (tag.equals(data.getName())) {
                BulkRequestBuilder bulkRequest = client.prepareBulk();
                IndexRequest indexRequest = new IndexRequest("tags", "tag", tag).source(jsonBuilder().startObject()
                        .field("name", data.getName()).field("owner", data.getOwner()).endObject());
                UpdateRequest updateRequest = new UpdateRequest("tags", "tag", tag).doc(jsonBuilder().startObject()
                        .field("name", data.getName()).field("owner", data.getOwner()).endObject())
                        .upsert(indexRequest);
                bulkRequest.add(updateRequest);
                SearchResponse qbResult = client.prepareSearch("channelfinder")
                        .setQuery(QueryBuilders.matchQuery("tags.name", tag)).addField("name").setSize(10000).execute()
                        .actionGet();

                Set<String> existingChannels = new HashSet<String>();
                for (SearchHit hit : qbResult.getHits()) {
                    existingChannels.add(hit.field("name").getValue().toString());
                }

                Set<String> newChannels = new HashSet<String>();
                if (data.getChannels() != null) {
                    newChannels
                            .addAll(data.getChannels().stream().map(XmlChannel::getName).collect(Collectors.toSet()));
                }

                Set<String> remove = new HashSet<String>(existingChannels);
                remove.removeAll(newChannels);

                Set<String> add = new HashSet<String>(newChannels);
                add.removeAll(existingChannels);

                HashMap<String, String> param = new HashMap<String, String>();
                param.put("name", data.getName());
                param.put("owner", data.getOwner());
                for (String ch : remove) {
                    bulkRequest.add(new UpdateRequest("channelfinder", "channel", ch).refresh(true)
                            .script("removeTag = new Object();" + "for (xmltag in ctx._source.tags) "
                                    + "{ if (xmltag.name == tag.name) { removeTag = xmltag} }; "
                                    + "ctx._source.tags.remove(removeTag);")
                            .addScriptParam("tag", param));
                }
                for (String ch : add) {
                    bulkRequest.add(new UpdateRequest("channelfinder", "channel", ch).refresh(true)
                            .script("ctx._source.tags.add(tag)").addScriptParam("tag", param));
                }

                bulkRequest.setRefresh(true);
                BulkResponse bulkResponse = bulkRequest.execute().actionGet();
                if (bulkResponse.hasFailures()) {
                    audit.severe(bulkResponse.buildFailureMessage());
                    if (bulkResponse.buildFailureMessage().contains("DocumentMissingException")) {
                        return handleException(um.getUserName(), Response.Status.NOT_FOUND,
                                bulkResponse.buildFailureMessage());
                    } else {
                        return handleException(um.getUserName(), Response.Status.INTERNAL_SERVER_ERROR,
                                bulkResponse.buildFailureMessage());
                    }
                } else {
                    GetResponse response = client.prepareGet("tags", "tag", tag).execute().actionGet();
                    ObjectMapper mapper = new ObjectMapper();
                    XmlTag result = mapper.readValue(response.getSourceAsBytes(), XmlTag.class);
                    Response r;
                    if (result == null) {
                        r = Response.status(Response.Status.NOT_FOUND).build();
                    } else {
                        r = Response.ok(result).build();
                    }
                    audit.info(um.getUserName() + "|" + uriInfo.getPath() + "|PUT|OK|"
                            + (System.currentTimeMillis() - start) + "|" + r.getStatus() + "|data="
                            + XmlTag.toLog(data));
                    return r;
                }
            } else {
                return Response.status(Status.BAD_REQUEST).entity("Specified tag name '" + tag
                        + "' and payload tag name '" + data.getName() + "' do not match").build();
            }
        } catch (Exception e) {
            return handleException(um.getUserName(), Response.Status.INTERNAL_SERVER_ERROR, e);
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
     * @param data XmlTag structure containing the list of channels to be tagged
     * @return HTTP Response
     */
    @PUT
    @Consumes({MediaType.APPLICATION_JSON})
    public Response createTags(List<XmlTag> data) {
        long start = System.currentTimeMillis();
        Client client = getNewClient();
        audit.info("client initialization: "+ (System.currentTimeMillis() - start));
        UserManager um = UserManager.getInstance();
        um.setUser(securityContext.getUserPrincipal(), securityContext.isUserInRole("Administrator"));
        try {
            BulkRequestBuilder bulkRequest = client.prepareBulk();
            for (XmlTag xmlTag : data) {
                IndexRequest indexRequest = new IndexRequest("tags", "tag", xmlTag.getName()).source(jsonBuilder().startObject()
                        .field("name", xmlTag.getName()).field("owner", xmlTag.getOwner()).endObject());
                UpdateRequest updateRequest = new UpdateRequest("tags", "tag", xmlTag.getName()).doc(
                        jsonBuilder().startObject()
                        .field("name", xmlTag.getName()).field("owner", xmlTag.getOwner()).endObject()).upsert(indexRequest);
                bulkRequest.add(updateRequest);
                SearchResponse qbResult = client.prepareSearch("channelfinder")
                        .setQuery(QueryBuilders.matchQuery("tags.name", xmlTag.getName())).addField("name").setSize(10000).execute().actionGet();

                Set<String> existingChannels = new HashSet<String>();
                for (SearchHit hit : qbResult.getHits()) {
                    existingChannels.add(hit.field("name").getValue().toString());
                }

                Set<String> newChannels = new HashSet<String>();
                if (xmlTag.getChannels() != null) {
                    newChannels.addAll(
                            xmlTag.getChannels().stream().map(XmlChannel::getName).collect(Collectors.toList()));
                }

                Set<String> remove = new HashSet<String>(existingChannels);
                remove.removeAll(newChannels);
                
                Set<String> add = new HashSet<String>(newChannels);
                add.removeAll(existingChannels);

                HashMap<String, String> param = new HashMap<String, String>(); 
                param.put("name", xmlTag.getName());
                param.put("owner", xmlTag.getOwner());
                for (String ch : remove) {
                    bulkRequest.add(new UpdateRequest("channelfinder", "channel", ch).refresh(true)
                            .script("removeTag = new Object();" 
                                    + "for (xmltag in ctx._source.tags) "
                                    + "{ if (xmltag.name == tag.name) { removeTag = xmltag} }; "
                                    + "ctx._source.tags.remove(removeTag);")
                            .addScriptParam("tag", param));
                }
                for (String ch : add) {
                    bulkRequest.add(new UpdateRequest("channelfinder", "channel", ch)
                            .script("ctx._source.tags.add(tag)")
                            .addScriptParam("tag", param));
                }
            }

            bulkRequest.setRefresh(true);
            BulkResponse bulkResponse = bulkRequest.execute().actionGet();
            if (bulkResponse.hasFailures()) {
                audit.severe(bulkResponse.buildFailureMessage());
                if (bulkResponse.buildFailureMessage().contains("DocumentMissingException")) {
                    return handleException(um.getUserName(), Response.Status.NOT_FOUND,
                            bulkResponse.buildFailureMessage());
                } else {
                    return handleException(um.getUserName(), Response.Status.INTERNAL_SERVER_ERROR,
                            bulkResponse.buildFailureMessage());
                }
            } else {
                return Response.ok(data).build();
            }
        } catch (Exception e) {
            return handleException(um.getUserName(), Response.Status.INTERNAL_SERVER_ERROR, e);
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
     * TODO: Optimize the bulk channel update
     *
     * @param tag URI path parameter: tag name
     * @param data list of channels to addSingle the tag <tt>name</tt> to
     * @return HTTP Response
     */
    @POST
    @Path("{tagName: "+tagNameRegex+"}")
    @Consumes({"application/json"})
    public Response update(@PathParam("tagName") String tag, XmlTag data) {
        long start = System.currentTimeMillis();
        Client client = getNewClient();
        audit.info("client initialization: "+ (System.currentTimeMillis() - start));
        UserManager um = UserManager.getInstance();
        um.setUser(securityContext.getUserPrincipal(), securityContext.isUserInRole("Administrator"));
        try {
            GetResponse response = client.prepareGet("tags", "tag", tag).execute().actionGet();
            if(!response.isExists()){
                return handleException(um.getUserName(), Response.Status.NOT_FOUND, "A tag named '"+tag+"' does not exist");
            }
            ObjectMapper mapper = new ObjectMapper();
            XmlTag original = mapper.readValue(response.getSourceAsBytes(), XmlTag.class);
            // rename a tag
            if(!original.getName().equals(data.getName())){
                return renameTag(um, client, original, data);
            }
            String tagOwner = data.getOwner() != null && !data.getOwner().isEmpty()? data.getOwner() : original.getOwner();
            
            BulkRequestBuilder bulkRequest = client.prepareBulk();
            UpdateRequest updateRequest = new UpdateRequest("tags", "tag", tag)
                                                .doc(jsonBuilder().startObject()
                                                           .field("name", data.getName())
                                                           .field("owner", tagOwner)
                                                           .endObject());
            // New owner
            HashMap<String, String> param = new HashMap<String, String>(); 
            param.put("name", data.getName());
            param.put("owner", tagOwner);
            if(!original.getOwner().equals(data.getOwner())){
                SearchResponse queryResponse = client.prepareSearch("channelfinder")
                        .setQuery(wildcardQuery("tags.name", original.getName().trim())).addFields("name").setSize(10000).execute()
                        .actionGet();
                for (SearchHit hit : queryResponse.getHits()) {
                    bulkRequest.add(new UpdateRequest("channelfinder", "channel", hit.getId())
                            .refresh(true)
                            .script("removeTag = new Object();"
                                    + "for (xmltag in ctx._source.tags) "
                                    + "{ if (xmltag.name == tag.name) { removeTag = xmltag} }; "
                                    + "ctx._source.tags.remove(removeTag);"
                                    + "ctx._source.tags.add(tag)")
                            .addScriptParam("tag", param));
                }
            }
            bulkRequest.add(updateRequest);
            if (data.getChannels() != null) {                
                for (XmlChannel channel : data.getChannels()) {
                    bulkRequest.add(new UpdateRequest("channelfinder", "channel", channel.getName())
                            .refresh(true)
                            .script("removeTag = new Object();"
                                    + "for (xmltag in ctx._source.tags) "
                                    + "{ if (xmltag.name == tag.name) { removeTag = xmltag} }; "
                                    + "ctx._source.tags.remove(removeTag);"
                                    + "ctx._source.tags.add(tag)")
                            .addScriptParam("tag", param));
                }
            }
            bulkRequest.setRefresh(true);
            BulkResponse bulkResponse = bulkRequest.execute().actionGet();
            if (bulkResponse.hasFailures()) {
                audit.severe(bulkResponse.buildFailureMessage());
                if (bulkResponse.buildFailureMessage().contains("DocumentMissingException")) {
                    return handleException(um.getUserName(), Response.Status.NOT_FOUND,
                            bulkResponse.buildFailureMessage());
                } else {
                    return handleException(um.getUserName(), Response.Status.INTERNAL_SERVER_ERROR,
                            bulkResponse.buildFailureMessage());
                }
            } else {
                Response r = Response.ok().build();
                audit.info(um.getUserName() + "|" + uriInfo.getPath() + "|POST|OK|" + r.getStatus() + "|data="
                        + XmlTag.toLog(data));
                return r;
            }
        } catch (Exception e) {
            return handleException(um.getUserName() , Response.Status.INTERNAL_SERVER_ERROR, e);
        } finally {
            client.close();
        }
    }

    /**
     * Utility method to rename an existing tag
     * @param data 
     * @param original 
     * @param client 
     * @param um 
     * @param client
     * @param original
     * @param data
     * @return
     */
    private Response renameTag(UserManager um, Client client, XmlTag original, XmlTag data) {
        try {
            SearchResponse queryResponse = client.prepareSearch("channelfinder")
                    .setQuery(wildcardQuery("tags.name", original.getName().trim())).addFields("name").setSize(10000).execute()
                    .actionGet();
            List<String> channelNames = new ArrayList<String>();
            for (SearchHit hit : queryResponse.getHits()) {
                channelNames.add(hit.getId());
            }
            String tagOwner = data.getOwner() != null && !data.getOwner().isEmpty()? data.getOwner() : original.getOwner();
            
            BulkRequestBuilder bulkRequest = client.prepareBulk();
            bulkRequest.add(new DeleteRequest("tags", "tag", original.getName()));
            IndexRequest indexRequest = new IndexRequest("tags", "tag", data.getName()).source(jsonBuilder()
                    .startObject().field("name", data.getName()).field("owner", data.getOwner()).endObject());
            UpdateRequest updateRequest;
            updateRequest = new UpdateRequest("tags", "tag", data.getName()).doc(jsonBuilder().startObject()
                    .field("name", data.getName()).field("owner", data.getOwner()).endObject()).upsert(indexRequest);
            bulkRequest.add(updateRequest);
            if (!channelNames.isEmpty()) {
                HashMap<String, String> originalParam = new HashMap<String, String>(); 
                originalParam.put("name", original.getName());
                HashMap<String, String> param = new HashMap<String, String>(); 
                param.put("name", data.getName());
                param.put("owner", tagOwner);
                for (String channel : channelNames) {
                    bulkRequest.add(new UpdateRequest("channelfinder", "channel", channel)
                            .refresh(true)
                            .script("removeTag = new Object();"
                                    + "for (xmltag in ctx._source.tags) "
                                    + "{ if (xmltag.name == originalTag.name) { removeTag = xmltag} }; "
                                    + "ctx._source.tags.remove(removeTag);"
                                    + "ctx._source.tags.add(tag)")
                            .addScriptParam("originalTag", originalParam)
                            .addScriptParam("tag", param));
                }
            }
            bulkRequest.setRefresh(true);
            BulkResponse bulkResponse = bulkRequest.execute().actionGet();
            if (bulkResponse.hasFailures()) {
                audit.severe(bulkResponse.buildFailureMessage());
                if (bulkResponse.buildFailureMessage().contains("DocumentMissingException")) {
                    return handleException(um.getUserName(), Response.Status.NOT_FOUND,
                            bulkResponse.buildFailureMessage());
                } else {
                    return handleException(um.getUserName(), Response.Status.INTERNAL_SERVER_ERROR,
                            bulkResponse.buildFailureMessage());
                }
            } else {
                Response r = Response.ok().build();
                audit.info(um.getUserName() + "|" + uriInfo.getPath() + "|POST|OK|" + r.getStatus() + "|data="
                        + XmlTag.toLog(data));
                return r;
            }
        } catch (IOException e) {
            return handleException(um.getUserName(), Response.Status.INTERNAL_SERVER_ERROR,
                    e);
        }
    }

    /**
     * POST method for creating multiple tags and updating all the appropriate channels
     * If the channels don't exist it will fail
     *
     * @param data XmlTags data (from payload)
     * @return HTTP Response
     * @throws IOException when audit or log fail
     */
    @POST
    @Consumes({MediaType.APPLICATION_JSON})
    public Response updateTags(List<XmlTag> data) throws IOException {
        Client client = getNewClient();
        UserManager um = UserManager.getInstance();
        um.setUser(securityContext.getUserPrincipal(), securityContext.isUserInRole("Administrator"));
        try {
            BulkRequestBuilder bulkRequest = client.prepareBulk();
            for (XmlTag tag : data) {
                bulkRequest.add(new UpdateRequest("tags", "tag", tag.getName())
                                        .doc(jsonBuilder().startObject()
                                                .field("name", tag.getName())
                                                .field("owner", tag.getOwner())
                                                .endObject()));
                if (tag.getChannels() != null) {
                    HashMap<String, String> param = new HashMap<String, String>(); 
                    param.put("name", tag.getName());
                    param.put("owner", tag.getOwner());
                    for (XmlChannel channel : tag.getChannels()) {
                        bulkRequest.add(new UpdateRequest("channelfinder", "channel", channel.getName())
                                .refresh(true)
                                .script("removeTag = new Object();"
                                        + "for (xmltag in ctx._source.tags) "
                                        + "{ if (xmltag.name == tag.name) { removeTag = xmltag} }; "
                                        + "ctx._source.tags.remove(removeTag);"
                                        + "ctx._source.tags.add(tag)")
                                .addScriptParam("tag", param));
                    }
                }
            }
            bulkRequest.setRefresh(true);
            BulkResponse bulkResponse = bulkRequest.execute().actionGet();
            if (bulkResponse.hasFailures()) {
                audit.severe(bulkResponse.buildFailureMessage());
                if (bulkResponse.buildFailureMessage().contains("DocumentMissingException")) {
                    return handleException(um.getUserName(), Response.Status.NOT_FOUND,
                            bulkResponse.buildFailureMessage());
                } else {
                    return handleException(um.getUserName(), Response.Status.INTERNAL_SERVER_ERROR,
                            bulkResponse.buildFailureMessage());
                }
            } else {
                Response r = Response.noContent().build();
                audit.info(um.getUserName() + "|" + uriInfo.getPath() + "|POST|OK|" + r.getStatus() + "|data="
                        + (data));
                return r;
            }
        } catch (Exception e) {
            return handleException(um.getUserName(), Response.Status.INTERNAL_SERVER_ERROR , e);
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
        Client client = getNewClient();
        UserManager um = UserManager.getInstance();
        um.setUser(securityContext.getUserPrincipal(), securityContext.isUserInRole("Administrator"));
        try {
            BulkRequestBuilder bulkRequest = client.prepareBulk();
            bulkRequest.add(new DeleteRequest("tags", "tag", tag));
            SearchResponse qbResult = client.prepareSearch("channelfinder")
                    .setQuery(QueryBuilders.matchQuery("tags.name", tag)).addField("name").setSize(10000).execute().actionGet();
            if (qbResult != null) {
                for (SearchHit hit : qbResult.getHits()) {
                    String channelName = hit.field("name").getValue().toString();
                    bulkRequest.add(new UpdateRequest("channelfinder", "channel", channelName).refresh(true)
                            .script("removeTag = new Object();" 
                                    + "for (xmltag in ctx._source.tags) "
                                    + "{ if (xmltag.name == tag) { removeTag = xmltag} }; "
                                    + "ctx._source.tags.remove(removeTag);")
                            .addScriptParam("tag", tag));
                }
            }
            bulkRequest.setRefresh(true);
            BulkResponse bulkResponse = bulkRequest.execute().actionGet();
            if (bulkResponse.hasFailures()) {
                audit.severe(bulkResponse.buildFailureMessage());
                return handleException(um.getUserName(), Response.Status.INTERNAL_SERVER_ERROR,
                        bulkResponse.buildFailureMessage());
            } else {
                DeleteResponse deleteResponse = bulkResponse.getItems()[0].getResponse();
                if (deleteResponse.isFound()) {
                    Response r = Response.ok().build();
                    audit.info(um.getUserName() + "|" + uriInfo.getPath() + "|DELETE|OK|" + r.getStatus());
                    return r;
                } else {
                    return handleException(um.getUserName(), Response.Status.NOT_FOUND,
                            new Exception("tag " + tag + " does not exist."));
                }
            }
        } catch (Exception e) {
            return handleException(um.getUserName(), Response.Status.INTERNAL_SERVER_ERROR, e);
        } finally {
            client.close();
        }
    }

    /**
     * PUT method for adding the tag identified by <tt>tag</tt> to the single
     * channel <tt>chan</tt> (both path parameters). 
     * 
     * TODO: could be simplified
     * with multi index update and script which can use wildcards thus removing
     * the need to explicitly define the entire tag
     * 
     * @param tag
     *            URI path parameter: tag name
     * @param chan
     *            URI path parameter: channel to update <tt>tag</tt> to
     * @param data
     *            tag data (ignored)
     * @return HTTP Response
     */
    @PUT
    @Path("{tagName}/{chName}")
    @Consumes({"application/xml", "application/json"})
    public Response addSingle(@PathParam("tagName") String tag, @PathParam("chName") String chan, XmlTag data) {
        Client client = getNewClient();
        UserManager um = UserManager.getInstance();
        um.setUser(securityContext.getUserPrincipal(), securityContext.isUserInRole("Administrator"));
        XmlTag result = null;
        try {
            GetResponse response = client.prepareGet("tags", "tag", tag).execute().actionGet();
            ObjectMapper mapper = new ObjectMapper();
            mapper.addMixIn(XmlChannel.class, MyMixInForXmlChannels.class);
            result = mapper.readValue(response.getSourceAsBytes(), XmlTag.class);
            
            if (result != null) {
//                if(um.userHasAdminRole() || um.userIsInGroup(result.getOwner())){
                if (validateTag(result, data)) {
                    HashMap<String, String> param = new HashMap<String, String>();
                    param.put("name", result.getName());
                    param.put("owner", result.getOwner());
                    UpdateResponse updateResponse = client
                            .update(new UpdateRequest("channelfinder", "channel", chan).refresh(true)
                                    .refresh(true)
                                    .script("removeTags = new java.util.ArrayList();" + "for (tag in ctx._source.tags) "
                                            + "{ if (tag.name == tag.name) { removeTags.add(tag)} }; "
                                            + "for (removeTag in removeTags) {ctx._source.tags.remove(removeTag)};"
                                            + "ctx._source.tags.add(tag)")
                                    .addScriptParam("tag", param))
                                    .actionGet();
                    Response r = Response.ok().build();
                    return r;
                } else {
                    return Response.status(Status.BAD_REQUEST).entity("Specified tag name '" + tag
                            + "' and payload tag name '" + data.getName() + "' do not match").build();
                }
//                }else{
//                    return Response.status(Status.FORBIDDEN)
//                            .entity("User '" + um.getUserName() + "' does not belong to owner group '"
//                                    + result.getOwner() + "' of tag '" + result.getName() + "'")
//                            .build();
//                }
            }else{
                return Response.status(Status.BAD_REQUEST).entity(tag + " Does not exist").build();
            }
        } catch (DocumentMissingException e) {
            return Response.status(Status.BAD_REQUEST).entity("Channels specified in tag update do not exist"+e.getDetailedMessage()).build();
        }  catch (Exception e) {
            return handleException(um.getUserName(), Response.Status.INTERNAL_SERVER_ERROR, e);
        } finally {
            client.close();
        }
    }

    /**
     * Check that the existing tag and the tag in the request body match 
     *  
     * @param existing
     * @param request
     * @return
     */
    private boolean validateTag(XmlTag existing, XmlTag request) {
        return existing.getName().equals(request.getName());
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
    public Response removeSingle(@PathParam("tagName") final String tag, @PathParam("chName") String chan) {
        Client client = getNewClient();
        UserManager um = UserManager.getInstance();
        um.setUser(securityContext.getUserPrincipal(), securityContext.isUserInRole("Administrator"));
        try {
            if (client.prepareGet("tags", "tag", tag).get().isExists()) {
                UpdateResponse updateResponse = client
                        .update(new UpdateRequest("channelfinder", "channel", chan).refresh(true)
                                .script(" removeTags = new java.util.ArrayList();" + "for (tag in ctx._source.tags) "
                                        + "{ if (tag.name == tag.name) { removeTags.add(tag)} }; "
                                        + "for (removeTag in removeTags) {ctx._source.tags.remove(removeTag)}")
                        .addScriptParam("tagName", tag)).actionGet();
                Response r = Response.ok().build();
                return r;
            } else {
                return handleException(um.getUserName(), Status.NOT_FOUND, "Tag " +tag+ " does not exist ");
            }
        } catch (DocumentMissingException e) {
            return Response.status(Status.NOT_FOUND).entity("Channel does not exist "+e.getDetailedMessage()).build();
        } catch (Exception e) {
            return handleException(um.getUserName(), Response.Status.INTERNAL_SERVER_ERROR, e);
        } finally {
            client.close();
        }
    }

    private Response handleException(String user, Response.Status status, Exception e) {
        return handleException(user, status, e.getMessage());
    }

    private Response handleException(String user, Response.Status status, String message) {
        log.warning(user + "|" + uriInfo.getPath() + "|GET|ERROR|" + status + "|cause=" + message);
        return new CFException(status, message).toResponse();
    }
    /**
     * A filter to be used with the jackson mapper to ignore the embedded
     * xmlchannels in the tag object
     * 
     * @author Kunal Shroff
     *
     */
    @JsonIgnoreType
    public class MyMixInForXmlChannels {
        //
    }
}
