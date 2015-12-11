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
import static gov.bnl.channelfinder.ElasticSearchClient.getNewClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.annotate.JsonIgnoreType;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;

/**
 * Top level Jersey HTTP methods for the .../tags URL
 *
 * @author Kunal Shroff <shroffk@bnl.gov>, Ralph Lange <Ralph.Lange@helmholtz-berlin.de>
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
    @Produces({MediaType.APPLICATION_JSON})
    public Response list() {
        Client client = getNewClient();
        String user = securityContext.getUserPrincipal() != null ? securityContext.getUserPrincipal().getName() : "";
        List<XmlTag> result = new ArrayList<XmlTag>();
        ObjectMapper mapper = new ObjectMapper();
        try {
            SearchResponse response = client.prepareSearch("tags").setTypes("tag").setQuery(new MatchAllQueryBuilder()).setSize(10000).execute().actionGet();
            for (SearchHit hit : response.getHits()) {
                result.add(mapper.readValue(hit.getSourceAsString(), XmlTag.class));
            }
            Response r = Response.ok(result.toArray(new XmlTag[result.size()])).build();
            log.fine(user + "|" + uriInfo.getPath() + "|GET|OK|" + r.getStatus() + "|returns " + result.size() + " tags");
            return r;
        } catch (Exception e) {
            return handleException(user,Response.Status.INTERNAL_SERVER_ERROR , e);
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
    @Produces({MediaType.APPLICATION_JSON})
    public Response read(@PathParam("tagName") String tag) {
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
                    r = Response.ok(result).build();
                }
                log.fine(user + "|" + uriInfo.getPath() + "|GET|OK|" + r.getStatus());
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
            BulkRequestBuilder bulkRequest = client.prepareBulk();
            IndexRequest indexRequest = new IndexRequest("tags", "tag", tag).source(jsonBuilder().startObject()
                    .field("name", data.getName()).field("owner", data.getOwner()).endObject());
            UpdateRequest updateRequest = new UpdateRequest("tags", "tag", tag).doc(
                    jsonBuilder().startObject()
                    .field("name", data.getName()).field("owner", data.getOwner()).endObject()).upsert(indexRequest);
            bulkRequest.add(updateRequest);
            SearchResponse qbResult = client.prepareSearch("channelfinder")
                    .setQuery(QueryBuilders.matchQuery("tags.name", tag)).addField("name").setSize(10000).execute().actionGet();

            Set<String> existingChannels = new HashSet<String>();
            for (SearchHit hit : qbResult.getHits()) {
                existingChannels.add(hit.field("name").getValue().toString());
            }

            Set<String> newChannels = new HashSet<String>();
            if (data.getChannels() != null) {
                newChannels.addAll(
                        Collections2.transform(data.getChannels(), new Function<XmlChannel, String>() {
                            @Override
                            public String apply(XmlChannel channel) {
                                return channel.getName();
                            }
                        }));
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
                        .script("removeTag = new Object();" 
                                + "for (xmltag in ctx._source.tags) "
                                + "{ if (xmltag.name == tag.name) { removeTag = xmltag} }; "
                                + "ctx._source.tags.remove(removeTag);")
                        .addScriptParam("tag", param));
            }
            for (String ch : add) {
                bulkRequest.add(new UpdateRequest("channelfinder", "channel", ch)
                        .refresh(true)
                        .script("ctx._source.tags.add(tag)")
                        .addScriptParam("tag", param));
            }

            bulkRequest.setRefresh(true);
            BulkResponse bulkResponse = bulkRequest.execute().actionGet();
            if (bulkResponse.hasFailures()) {
                audit.severe(bulkResponse.buildFailureMessage());
                throw new Exception();
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
                        + (System.currentTimeMillis() - start) + "|" + r.getStatus() + "|data=" + XmlTag.toLog(data));
                return r;
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
     * @param tag URI path parameter: tag name
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
                            Collections2.transform(xmlTag.getChannels(), new Function<XmlChannel, String>() {
                                @Override
                                public String apply(XmlChannel channel) {
                                    return channel.getName();
                                }
                            }));
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
                            .refresh(true)
                            .script("ctx._source.tags.add(tag)")
                            .addScriptParam("tag", param));
                }

            }

            bulkRequest.setRefresh(true);
            BulkResponse bulkResponse = bulkRequest.execute().actionGet();
            if (bulkResponse.hasFailures()) {
                audit.severe(bulkResponse.buildFailureMessage());
                throw new Exception();
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
    @Consumes({"application/xml", "application/json"})
    public Response update(@PathParam("tagName") String tag, XmlTag data) {
        long start = System.currentTimeMillis();
        Client client = getNewClient();
        audit.info("client initialization: "+ (System.currentTimeMillis() - start));
        UserManager um = UserManager.getInstance();
        um.setUser(securityContext.getUserPrincipal(), securityContext.isUserInRole("Administrator"));
        try {
            BulkRequestBuilder bulkRequest = client.prepareBulk();
            UpdateRequest updateRequest = new UpdateRequest("tags", "tag", tag)
                                                .doc(jsonBuilder().startObject()
                                                           .field("name", data.getName())
                                                           .field("owner", data.getOwner()).endObject());
            bulkRequest.add(updateRequest);
            if (data.getChannels() != null) {
                HashMap<String, String> param = new HashMap<String, String>(); 
                param.put("name", data.getName());
                param.put("owner", data.getOwner());
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
                throw new Exception();
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
                return handleException(um.getUserName(), Response.Status.INTERNAL_SERVER_ERROR, null);
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
                                    + "for (xmltag in ctx._source.xmlTags) "
                                    + "{ if (xmltag.name == tag) { removeTag = xmltag} }; "
                                    + "ctx._source.tags.remove(removeTag);")
                            .addScriptParam("tag", tag));
                }
            }
            bulkRequest.setRefresh(true);
            BulkResponse bulkResponse = bulkRequest.execute().actionGet();
            if (bulkResponse.hasFailures()) {
                audit.severe(bulkResponse.buildFailureMessage());
                throw new Exception();
            } else {
                Response r = Response.ok().build();
                audit.info(um.getUserName() + "|" + uriInfo.getPath() + "|DELETE|OK|" + r.getStatus());
                return r;
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
            mapper.getSerializationConfig().addMixInAnnotations(XmlChannel.class, MyMixInForXmlChannels.class);
            result = mapper.readValue(response.getSourceAsBytes(), XmlTag.class);
            
            if (result != null) {
                String str = mapper.writeValueAsString(result);
                HashMap<String, String> param = new HashMap<String, String>(); 
                param.put("name", result.getName());
                param.put("owner", result.getOwner());
                UpdateResponse updateResponse = client.update(new UpdateRequest("channelfinder", "channel", chan)
                        .refresh(true)
                        .script("removeTags = new java.util.ArrayList();"
                            + "for (tag in ctx._source.xmlTags) "
                            + "{ if (tag.name == tag.name) { removeTags.add(tag)} }; "
                            + "for (removeTag in removeTags) {ctx._source.xmlTags.remove(removeTag)};"
                            + "ctx._source.xmlTags.add(tag)")
                        .addScriptParam("tag", param)).actionGet();
                Response r = Response.ok().build();
                return r;
            }else{
                return Response.status(Status.BAD_REQUEST).build();
            }
        } catch (Exception e) {
            return handleException(um.getUserName(), Response.Status.INTERNAL_SERVER_ERROR, e);
        } finally {
            client.close();
        }
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
            UpdateResponse updateResponse = client.update(new UpdateRequest("channelfinder", "channel", chan)
                    .refresh(true)
                    .script(" removeTags = new java.util.ArrayList();"
                            + "for (tag in ctx._source.tags) "
                            + "{ if (tag.name == tag.name) { removeTags.add(tag)} }; "
                            + "for (removeTag in removeTags) {ctx._source.tags.remove(removeTag)}")
                    .addScriptParam("tagName", tag)).actionGet();
            Response r = Response.ok().build();
            return r;
        } catch (Exception e) {
            return handleException(um.getUserName(), Response.Status.INTERNAL_SERVER_ERROR, e);
        } finally {
            client.close();
        }
    }

    private Response handleException(String user, Response.Status status, Exception e){
        log.warning(user + "|" + uriInfo.getPath() + "|GET|ERROR|" + status +  "|cause=" + e);
        return new CFException(status, e.getMessage()).toResponse();
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
