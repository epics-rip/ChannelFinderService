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

import static gov.bnl.channelfinder.ElasticSearchClient.getNewClient;
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.disMaxQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.nestedQuery;
import static org.elasticsearch.index.query.QueryBuilders.wildcardQuery;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
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
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.DisMaxQueryBuilder;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortBuilders;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.support.WriteRequest.RefreshPolicy;
/**
 * Top level Jersey HTTP methods for the .../channels URL
 * 
 * @author Kunal Shroff {@literal <shroffk@bnl.gov>}, Ralph Lange {@literal <ralph.lange@gmx.de>}
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
    @Produces({"application/json"})
    public Response query() {
        StringBuffer performance = new StringBuffer();
        long start = System.currentTimeMillis();
        long totalStart = System.currentTimeMillis();
        Client client = ElasticSearchClient.getSearchClient();
        start = System.currentTimeMillis();
        String user = securityContext.getUserPrincipal() != null ? securityContext.getUserPrincipal().getName() : "";
        try {
            MultivaluedMap<String, String> parameters = uriInfo.getQueryParameters();
            BoolQueryBuilder qb = boolQuery();
            int size = 10000;
            int from = 0;
            for (Entry<String, List<String>> parameter : parameters.entrySet()) {
                switch (parameter.getKey()) {
                case "~name":
                    for (String value : parameter.getValue()) {
                        DisMaxQueryBuilder nameQuery = disMaxQuery();
                        for (String pattern : value.split("\\|")) {
                            nameQuery.add(wildcardQuery("name", pattern.trim()));
                        }
                        qb.must(nameQuery);
                    }
                    break;
                case "~tag":
                    for (String value : parameter.getValue()) {
                        DisMaxQueryBuilder tagQuery = disMaxQuery();
                        for (String pattern : value.split("\\|")) {
                            tagQuery.add(wildcardQuery("tags.name", pattern.trim()));
                        }
                        qb.must(nestedQuery("tags", tagQuery, ScoreMode.Avg));
                    }
                    break;
                case "~size":
            		Optional<String> maxSize = parameter.getValue().stream().max((o1, o2) -> {
            				return Integer.valueOf(o1).compareTo(Integer.valueOf(o2));
            		});
            		if (maxSize.isPresent()) {
            			size = Integer.valueOf(maxSize.get());
            		}
            		break;
                case "~from":
            		Optional<String> maxFrom = parameter.getValue().stream().max((o1, o2) -> {
            				return Integer.valueOf(o1).compareTo(Integer.valueOf(o2));
            		});
            		if (maxFrom.isPresent()) {
            			from = Integer.valueOf(maxFrom.get());
            		}
            		break;
                default:
                    DisMaxQueryBuilder propertyQuery = disMaxQuery();
                    for (String value : parameter.getValue()) {
                        for (String pattern : value.split("\\|")) {
                            propertyQuery.add(nestedQuery("properties",
                                    boolQuery()
                                            .must(matchQuery("properties.name", parameter.getKey().trim()))
                                            .must(wildcardQuery("properties.value", pattern.trim())),
                                    ScoreMode.Avg));
                        }
                    }
                    qb.must(propertyQuery);
                    break;
                }
            }
            
            performance.append("|prepare:" + (System.currentTimeMillis() - start));
            start = System.currentTimeMillis();
            SearchRequestBuilder builder = client.prepareSearch("channelfinder").setQuery(qb).setSize(size);
            if(from >= 0){
            	builder.addSort(SortBuilders.fieldSort("name.keyword"));
            	builder.setFrom(from);
            }
            final SearchResponse qbResult = builder.execute().actionGet();
            performance.append("|query:("+qbResult.getHits().getTotalHits()+")" + (System.currentTimeMillis() - start));
            start = System.currentTimeMillis();
            final ObjectMapper mapper = new ObjectMapper();
            mapper.addMixIn(XmlProperty.class, OnlyXmlProperty.class);
            mapper.addMixIn(XmlTag.class, OnlyXmlTag.class);
            start = System.currentTimeMillis();
            
            StreamingOutput stream = new StreamingOutput() {
                
                @Override
                public void write(OutputStream os) throws IOException, WebApplicationException {
                    JsonGenerator jg = mapper.getFactory().createGenerator(os, JsonEncoding.UTF8);
                    jg.writeStartArray();
                    if(qbResult != null){
                        for (SearchHit hit : qbResult.getHits()) {
                            jg.writeObject(mapper.readValue(hit.source(), XmlChannel.class));
                        }
                    }
                    jg.writeEndArray();
                    jg.flush();
                    jg.close();
                }
            };
            
            
            performance.append("|parse:" + (System.currentTimeMillis() - start));
            Response r = Response.ok(stream).build();
            log.info(user + "|" + uriInfo.getPath() + "|GET|OK" + performance.toString() + "|total:"
                    + (System.currentTimeMillis() - totalStart) + "|" + r.getStatus()
                    + "|returns " + qbResult.getHits().getTotalHits() + " channels");
            return r;
        } catch (Exception e) {
            return handleException(user, "GET", Response.Status.INTERNAL_SERVER_ERROR, e);
        } finally {
        }
    }

    /**
     * PUT method for creating multiple channel instances.
     *
     * @param data XmlChannels data (from payload)
     * @return HTTP Response
     * @throws IOException when audit or log fail
     */
    @PUT
    @Consumes({"application/json"})
    public Response create(List<XmlChannel> data) throws IOException {
        Client client = getNewClient();
        UserManager um = UserManager.getInstance();
        um.setUser(securityContext.getUserPrincipal(), securityContext.isUserInRole("Administrator"));
        ObjectMapper mapper = new ObjectMapper();
        try {
            long start = System.currentTimeMillis();
            data = validateChannels(data, client);
            audit.info(um.getUserName() + "|" + uriInfo.getPath() + "|PUT|validation : "+ (System.currentTimeMillis() - start));
            start = System.currentTimeMillis();
            BulkRequestBuilder bulkRequest = client.prepareBulk();
            for (XmlChannel channel : data) {
                bulkRequest.add(client.prepareUpdate("channelfinder", "channel", channel.getName()).setDoc(mapper.writeValueAsBytes(channel))
                        .setUpsert(new IndexRequest("channelfinder", "channel", channel.getName()).source(mapper.writeValueAsBytes(channel))));
            }
            String prepare = "|Prepare: " + (System.currentTimeMillis()-start) + "|";
            start = System.currentTimeMillis();
            bulkRequest.setRefreshPolicy(RefreshPolicy.IMMEDIATE);
            BulkResponse bulkResponse = bulkRequest.execute().actionGet();
            String execute = "|Execute: " + (System.currentTimeMillis()-start)+"|";
            start = System.currentTimeMillis();
            if (bulkResponse.hasFailures()) {
                audit.severe(bulkResponse.buildFailureMessage());
                throw new Exception();
            } else {
                Response r = Response.noContent().build();
                audit.info(um.getUserName() + "|" + uriInfo.getPath() + "|POST|OK|" + r.getStatus() + prepare + execute + "|data=" + (data));
                return r;
            }
        } catch (IllegalArgumentException e) {
            return handleException(um.getUserName(), "PUT", Response.Status.BAD_REQUEST, e);
        } catch (Exception e) {
            return handleException(um.getUserName(), "PUT", Response.Status.INTERNAL_SERVER_ERROR, e);
        } finally {
            client.close();
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
    @Produces({"application/json"})
    public Response read(@PathParam("chName") String chan) {
        audit.info("getting ch:" + chan);
        Client client = ElasticSearchClient.getSearchClient();
        String user = securityContext.getUserPrincipal() != null ? securityContext.getUserPrincipal().getName() : "";
        try {
            final GetResponse response = client.prepareGet("channelfinder", "channel", chan).execute().actionGet();
            Response r;
            if (response.isExists()) {
                final ObjectMapper mapper = new ObjectMapper();
                mapper.addMixIn(XmlProperty.class, OnlyXmlProperty.class);
                mapper.addMixIn(XmlTag.class, OnlyXmlTag.class);
                StreamingOutput stream = new StreamingOutput() {
                    
                    @Override
                    public void write(OutputStream os) throws IOException, WebApplicationException {
                        JsonGenerator jg = mapper.getFactory().createGenerator(os, JsonEncoding.UTF8);
                        jg.writeObject(mapper.readValue(response.getSourceAsBytes(), XmlChannel.class));
                        jg.flush();
                        jg.close();
                    }
                };
                r = Response.ok(stream).build();
            } else {
                r = Response.status(Response.Status.NOT_FOUND).build();
            }
            log.fine(user + "|" + uriInfo.getPath() + "|GET|OK|" + r.getStatus());
            return r;
        } catch (Exception e) {
            return handleException(user, "GET", Response.Status.INTERNAL_SERVER_ERROR, e);
        } finally {
            
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
    @Consumes("application/json")
    public Response create(@PathParam("chName") String chan, XmlChannel data) {
        audit.severe("PUT:"+XmlChannel.toLog(data));
        UserManager um = UserManager.getInstance();
        um.setUser(securityContext.getUserPrincipal(), securityContext.isUserInRole("Administrator"));
        if(data.getName()==null || data.getName().isEmpty()){
            return handleException(um.getUserName(), "PUT", Response.Status.BAD_REQUEST, "Specified channel name '"
                    + chan + "' and payload channel name '" + data.getName() + "' do not match");
        }
        if(!validateChannelName(chan, data)){
            return handleException(um.getUserName(), "PUT", Response.Status.BAD_REQUEST, "Specified channel name '"
                    + chan + "' and payload channel name '" + data.getName() + "' do not match");
        }
        long start = System.currentTimeMillis();
        Client client = getNewClient();
        ObjectMapper mapper = new ObjectMapper();
        try {
            start = System.currentTimeMillis();
            data = validateChannel(data, client);
            audit.info(um.getUserName() + "|" + uriInfo.getPath() + "|PUT|validation : "+ (System.currentTimeMillis() - start));
            IndexRequest indexRequest = new IndexRequest("channelfinder", "channel", chan)
                    .source(mapper.writeValueAsBytes(data));
            UpdateRequest updateRequest = new UpdateRequest("channelfinder", "channel", chan)
                    .doc(mapper.writeValueAsBytes(data)).upsert(indexRequest);
            UpdateResponse result = client.update(updateRequest).actionGet();
            Response r = Response.noContent().build();
            audit.info(um.getUserName() + "|" + uriInfo.getPath() + "|PUT|OK|" + r.getStatus() + "|data=" + XmlChannel.toLog(data));
            return r;
        } catch (IllegalArgumentException e) {
            return handleException(um.getUserName(), "PUT", Response.Status.BAD_REQUEST, e);
        } catch (Exception e) {
            return handleException(um.getUserName(), "PUT", Response.Status.INTERNAL_SERVER_ERROR, e);
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
        UserManager um = UserManager.getInstance();
        um.setUser(securityContext.getUserPrincipal(), securityContext.isUserInRole("Administrator"));
        Client client = getNewClient();
        if(data.getName()==null || data.getName().isEmpty()){
            return handleException(um.getUserName(), "PUT", Response.Status.BAD_REQUEST, "Specified channel name '"
                    + chan + "' and payload channel name '" + data.getName() + "' do not match");
        }
        if(!validateChannelName(chan, data)){
            return renameChannel(um, client, chan, data);
        }
        ObjectMapper mapper = new ObjectMapper();
        try {
            start = System.currentTimeMillis();
            data = validateChannel(data, client);
            audit.info(um.getUserName() + "|" + uriInfo.getPath() + "|POST|validation : "+ (System.currentTimeMillis() - start));
            start = System.currentTimeMillis();
            GetResponse response = client.prepareGet("channelfinder", "channel", chan).execute().actionGet();
            if(response.isExists()){
                XmlChannel channel= mapper.readValue(response.getSourceAsBytes(), XmlChannel.class);
                channel.setName(data.getName());
                channel.setOwner(data.getOwner());
                Collection<String> propNames = ChannelUtil.getPropertyNames(data);
                data.getProperties().addAll(channel.getProperties().stream().filter(p -> {
                    return !propNames.contains(p.getName());
                }).collect(Collectors.toList()));
                channel.setProperties(data.getProperties());
                Collection<String> tagNames = ChannelUtil.getTagNames(data);
                data.getTags().addAll(channel.getTags().stream().filter(t -> {
                    return !tagNames.contains(t.getName());
                }).collect(Collectors.toList()));
                channel.setTags(data.getTags());
                UpdateRequest updateRequest = new UpdateRequest("channelfinder", "channel", chan)
                        .doc(mapper.writeValueAsBytes(channel));
                audit.info(um.getUserName() + "|" + uriInfo.getPath() + "|POST|prepare : "+ (System.currentTimeMillis() - start));
                start = System.currentTimeMillis();
                UpdateResponse result = client.update(updateRequest).actionGet();
                Response r = Response.noContent().build();
                audit.info(um.getUserName() + "|" + uriInfo.getPath() + "|POST|OK|" + r.getStatus()
                        + "|data=" + XmlChannel.toLog(data));
                return r;
            }else{
                return handleException(um.getUserName(), "POST", Response.Status.NOT_FOUND, "Specified channel '"+chan+"' does not exist");
            }
        } catch (IllegalArgumentException e) {
            return handleException(um.getUserName(), "POST", Response.Status.BAD_REQUEST, e);
        } catch (Exception e) {
            return handleException(um.getUserName(), "POST", Response.Status.INTERNAL_SERVER_ERROR, e);
        } finally {
            client.close();
        }
    }

    
    private Response renameChannel(UserManager um, Client client, String chan, XmlChannel data) {
        GetResponse response = client.prepareGet("channelfinder", "channel", chan).execute().actionGet();
        if(!response.isExists()){
            handleException(um.getUserName(), "POST", Response.Status.NOT_FOUND, "Specified channel '"+chan+"' does not exist");
        }
        ObjectMapper mapper = new ObjectMapper();
        try {
            XmlChannel originalChannel = mapper.readValue(response.getSourceAsBytes(), XmlChannel.class);
            originalChannel.setName(data.getName());
            Collection<String> propNames = ChannelUtil.getPropertyNames(data);
            data.getProperties().addAll(originalChannel.getProperties().stream().filter(p -> {
                return !propNames.contains(p.getName());
            }).collect(Collectors.toList()));
            originalChannel.setProperties(data.getProperties());
            Collection<String> tagNames = ChannelUtil.getTagNames(data);
            data.getTags().addAll(originalChannel.getTags().stream().filter(t -> {
                return !tagNames.contains(t.getName());
            }).collect(Collectors.toList()));
            originalChannel.setTags(data.getTags());
            BulkRequestBuilder bulkRequest = client.prepareBulk();
            bulkRequest.add(new DeleteRequest("channelfinder", "channel", chan));
            IndexRequest indexRequest = new IndexRequest("channelfinder", "channel", originalChannel.getName())
                    .source(mapper.writeValueAsBytes(originalChannel));
            bulkRequest.add(indexRequest);
            bulkRequest.setRefreshPolicy(RefreshPolicy.IMMEDIATE);
            BulkResponse bulkResponse = bulkRequest.execute().actionGet();
            if (bulkResponse.hasFailures()) {
                audit.severe(bulkResponse.buildFailureMessage());
                if (bulkResponse.buildFailureMessage().contains("DocumentMissingException")) {
                    return handleException(um.getUserName(), "POST", Response.Status.NOT_FOUND,
                            bulkResponse.buildFailureMessage());
                } else {
                    return handleException(um.getUserName(), "POST", Response.Status.INTERNAL_SERVER_ERROR,
                            bulkResponse.buildFailureMessage());
                }
            } else {
                Response r = Response.ok(originalChannel).build();
                audit.info(um.getUserName() + "|" + uriInfo.getPath() + "|POST|OK|" + r.getStatus() + "|data=");
                return r;
            }
        } catch (IOException e) {
            return handleException(um.getUserName(), "POST", Response.Status.INTERNAL_SERVER_ERROR, e);
        }
    }

    private boolean validateChannelName(String chan, XmlChannel data) {
        return chan.equals(data.getName());
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
        Client client = getNewClient();
        UserManager um = UserManager.getInstance();
        um.setUser(securityContext.getUserPrincipal(), securityContext.isUserInRole("Administrator"));
        try {
            DeleteResponse deleteResponse = client.prepareDelete("channelfinder", "channel", chan).execute().get();
            if(deleteResponse.getResult() == DocWriteResponse.Result.DELETED){
                Response r = Response.ok().build();
                audit.info(um.getUserName() + "|" + uriInfo.getPath() + "|DELETE|OK|" + r.getStatus());
                return r;
            } else {
                return Response.status(Status.NOT_FOUND).entity("Specified channel '"+chan+"' does not exist").build();
            }
        } catch (Exception e) {
            return handleException(um.getUserName(), "DELETE", Response.Status.INTERNAL_SERVER_ERROR, e);
        } finally {
            client.close();
        }
    }

    /**
     * Check is all the tags and properties already exist
     * @return
     * @throws IOException 
     * @throws JsonMappingException 
     * @throws JsonParseException 
     */
    private List<XmlChannel> validateChannels(List<XmlChannel> channels, Client client) throws JsonParseException, JsonMappingException, IOException{
        for (XmlChannel channel : channels) {
            if (channel.getName() == null || channel.getName().isEmpty()) {
                throw new IllegalArgumentException("Invalid channel name ");
            }
            if (channel.getOwner() == null || channel.getOwner().isEmpty()) {
                throw new IllegalArgumentException("Invalid channel owner (null or empty string) for '"+channel.getName()+"'");
            }
            for (XmlProperty xmlProperty : channel.getProperties()) {
                if (xmlProperty.getValue() == null || xmlProperty.getValue().isEmpty()) {
                    throw new IllegalArgumentException("Invalid property value (missing or null or empty string) for '"+xmlProperty.getName()+"'");
                }
            }
        }
        final Map<String, XmlTag> tags = new HashMap<String, XmlTag>();
        final Map<String, XmlProperty> properties = new HashMap<String, XmlProperty>();
        
        ObjectMapper mapper = new ObjectMapper();
        mapper.addMixIn(XmlProperty.class, OnlyXmlProperty.class);
        mapper.addMixIn(XmlTag.class, OnlyXmlTag.class);
        
        SearchResponse response = client.prepareSearch("properties").setTypes("property")
                .setQuery(new MatchAllQueryBuilder()).setSize(1000).execute().actionGet();
        for (SearchHit hit : response.getHits()) {
            XmlProperty prop = mapper.readValue(hit.getSourceAsString(), XmlProperty.class);
            properties.put(prop.getName(), prop);
        }
        response = client.prepareSearch("tags").setTypes("tag").setQuery(new MatchAllQueryBuilder()).setSize(1000).execute()
                .actionGet();
        for (SearchHit hit : response.getHits()) {
            XmlTag tag = mapper.readValue(hit.getSourceAsString(), XmlTag.class);
            tags.put(tag.getName(), tag);
        }
        if (tags.keySet().containsAll(ChannelUtil.getTagNames(channels))
                && properties.keySet().containsAll(ChannelUtil.getPropertyNames(channels))) {
            for (XmlChannel channel : channels) {
                channel.getTags().parallelStream().forEach((tag) -> {
                    tag.setOwner(tags.get(tag.getName()).getOwner());
                });
                channel.getProperties().parallelStream().forEach((prop) -> {
                    prop.setOwner(properties.get(prop.getName()).getOwner());
                });
            }
            return channels;
        }else{
            StringBuffer errorMsg = new StringBuffer();
            Collection<String> missingTags = ChannelUtil.getTagNames(channels);
            missingTags.removeAll(tags.keySet());
            for (String tag : missingTags) {
                errorMsg.append(tag+"|");
            }
            Collection<String> missingProps = ChannelUtil.getPropertyNames(channels);
            missingProps.removeAll(properties.keySet());
            for (String prop : missingProps) {
                errorMsg.append(prop+"|");
            }
            throw new IllegalArgumentException("The following Tags and/or Properties on the channel don't exist -- " + errorMsg.toString());
        
        }
    }
    
    /**
     * Check is all the tags and properties already exist
     * @return
     * @throws IOException 
     * @throws JsonMappingException 
     * @throws JsonParseException 
     */
    private XmlChannel validateChannel(XmlChannel channel, Client client) throws JsonParseException, JsonMappingException, IOException {

        if (channel.getName() == null || channel.getName().isEmpty()) {
            throw new IllegalArgumentException("Invalid channel name ");
        }

        if (channel.getOwner() == null || channel.getOwner().isEmpty()) {
            throw new IllegalArgumentException("Invalid channel owner (null or empty string) for '"+channel.getName()+"'");
        }

        for (XmlProperty xmlProperty : channel.getProperties()) {
            if (xmlProperty.getValue() == null || xmlProperty.getValue().isEmpty()) {
                throw new IllegalArgumentException(
                        "Invalid property value (missing or null or empty string) for '" + xmlProperty.getName() + "'");
            }
        }
        final Map<String, XmlTag> tags = new HashMap<String, XmlTag>();
        final Map<String, XmlProperty> properties = new HashMap<String, XmlProperty>();
        
        ObjectMapper mapper = new ObjectMapper();
        mapper.addMixIn(XmlProperty.class, OnlyXmlProperty.class);
        mapper.addMixIn(XmlTag.class, OnlyXmlTag.class);
        
        SearchResponse response = client.prepareSearch("properties").setTypes("property")
                .setQuery(new MatchAllQueryBuilder()).setSize(1000).execute().actionGet();
        for (SearchHit hit : response.getHits()) {
            XmlProperty prop = mapper.readValue(hit.getSourceAsString(), XmlProperty.class);
            properties.put(prop.getName(), prop);
        }
        response = client.prepareSearch("tags").setTypes("tag").setQuery(new MatchAllQueryBuilder()).setSize(1000).execute()
                .actionGet();
        for (SearchHit hit : response.getHits()) {
            XmlTag tag = mapper.readValue(hit.getSourceAsString(), XmlTag.class);
            tags.put(tag.getName(), tag);
        }
        if (tags.keySet().containsAll(ChannelUtil.getTagNames(channel))
                && properties.keySet().containsAll(ChannelUtil.getPropertyNames(channel))) {
            channel.getTags().parallelStream().forEach((tag) -> {
                tag.setOwner(tags.get(tag.getName()).getOwner());
            });
            channel.getProperties().parallelStream().forEach((prop) -> {
                prop.setOwner(properties.get(prop.getName()).getOwner());
            });
            return channel;
        } else {
            StringBuffer errorMsg = new StringBuffer();
            Collection<String> missingTags = ChannelUtil.getTagNames(channel);
            missingTags.removeAll(tags.keySet());
            for (String tag : missingTags) {
                errorMsg.append(tag+"|");
            }
            Collection<String> missingProps = ChannelUtil.getPropertyNames(channel);
            missingProps.removeAll(properties.keySet());
            for (String prop : missingProps) {
                errorMsg.append(prop+"|");
            }
            throw new IllegalArgumentException("The following Tags and/or Properties on the channel don't exist -- " + errorMsg.toString());
        }
    }

    private Response handleException(String user, String requestType, Response.Status status, Exception e) {
        return handleException(user, requestType, status, e.getMessage());
    }

    private Response handleException(String user, String requestType, Response.Status status, String message) {
        log.warning(user + "|" + uriInfo.getPath() + "|" +requestType+ "|ERROR|" + status + "|cause=" + message);
        return new CFException(status, message).toResponse();
    }
    
    abstract class OnlyXmlProperty {
        @JsonIgnore
        private List<XmlChannel> channels;
    }

    abstract class OnlyXmlTag {
        @JsonIgnore
        private List<XmlChannel> channels;
    }
}
