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
import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.wildcardQuery;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

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
import org.elasticsearch.index.engine.DocumentMissingException;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

import gov.bnl.channelfinder.ChannelsResource.OnlyXmlProperty;
import gov.bnl.channelfinder.TagsResource.MyMixInForXmlChannels;
import java.util.concurrent.ExecutionException;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.transport.RemoteTransportException;

/**
 * Top level Jersey HTTP methods for the .../properties URL
 *
 * @author Kunal Shroff {@literal <shroffk@bnl.gov>}, Ralph Lange {@literal <Ralph.Lange@helmholtz-berlin.de>}
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
     * @return list of properties
     */
    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    public Response list() {
        Client client = getNewClient();
        final String user = securityContext.getUserPrincipal() != null ? securityContext.getUserPrincipal().getName() : "";
        final ObjectMapper mapper = new ObjectMapper();
        mapper.addMixIn(XmlProperty.class, OnlyXmlProperty.class);
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
            final SearchResponse response = client.prepareSearch("properties")
                                            .setTypes("property")
                                            .setQuery(new MatchAllQueryBuilder())
                                            .setSize(size).execute().actionGet();
            StreamingOutput stream = new StreamingOutput() {
                @Override
                public void write(OutputStream os) throws IOException, WebApplicationException {
                    JsonGenerator jg = mapper.getFactory().createGenerator(os, JsonEncoding.UTF8);
                    jg.writeStartArray();
                    if(response != null){
                        for (SearchHit hit : response.getHits()) {
                            jg.writeObject(mapper.readValue(hit.source(), XmlProperty.class));
                        }
                    }
                    jg.writeEndArray();
                    jg.flush();
                    jg.close();
                }
            };
            Response r = Response.ok(stream).build();
            audit.info(user + "|" + uriInfo.getPath() + "|GET|OK|" + r.getStatus() + "|returns " + response.getHits().getTotalHits()+ " properties");
            return r;
        } catch (Exception e) {
            return handleException(user, "GET", Response.Status.INTERNAL_SERVER_ERROR, e);
        } finally {
            client.close();
        }
    }

    /**
     * PUT method for creating multiple properties.
     *
     * @param data XmlProperties data (from payload)
     * @return HTTP Response
     * @throws IOException
     *             when audit or log fail
     */
    @PUT
    @Consumes("application/json")
    public Response create(List<XmlProperty> data) throws IOException {
        Client client = getNewClient();
        UserManager um = UserManager.getInstance();
        um.setUser(securityContext.getUserPrincipal(), securityContext.isUserInRole("Administrator"));
        ObjectMapper mapper = new ObjectMapper();
        try {
            BulkRequestBuilder bulkRequest = client.prepareBulk();
            for (XmlProperty property : data) {
                bulkRequest.add(client.prepareUpdate("properties", "property", property.getName())
                                      .setDoc(mapper.writeValueAsBytes(property))
                                      .setUpsert(
                                              new IndexRequest("properties", "property", property.getName())
                                              .source(mapper.writeValueAsBytes(property)))
                                );
            }
            bulkRequest.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
            BulkResponse bulkResponse = bulkRequest.execute().actionGet();
            if (bulkResponse.hasFailures()) {
                return handleException(um.getUserName(), "PUT", Response.Status.INTERNAL_SERVER_ERROR,
                        bulkResponse.buildFailureMessage());
            } else {
                Response r = Response.noContent().build();
                audit.info(um.getUserName() + "|" + uriInfo.getPath() + "|PUT|OK|" + r.getStatus() + "|data=" + data);
                return r;
            }
        } catch (Exception e) {
            return handleException(um.getUserName(), "PUT", Response.Status.INTERNAL_SERVER_ERROR, e);
        } finally {
            client.close();
        }
    }

    /**
     * POST method for creating multiple properties.
     *
     * If the channels don't exist it will fail
     *
     * @param data XmlProperties data (from payload)
     * @return HTTP Response
     * @throws IOException
     *             when audit or log fail
     */
    @POST
    @Consumes("application/json")
    public Response update(List<XmlProperty> data) throws IOException {
        Client client = getNewClient();
        UserManager um = UserManager.getInstance();
        um.setUser(securityContext.getUserPrincipal(), securityContext.isUserInRole("Administrator"));
        ObjectMapper mapper = new ObjectMapper();
        try {
            BulkRequestBuilder bulkRequest = client.prepareBulk();
            for (XmlProperty property : data) {
                bulkRequest.add(client.prepareUpdate("properties", "property", property.getName())
                                      .setDoc(mapper.writeValueAsBytes(property))
                                      .setUpsert(
                                              new IndexRequest("properties", "property", property.getName())
                                              .source(mapper.writeValueAsBytes(property)))
                                );
                if (property.getChannels() != null) {
                    HashMap<String, String> param = new HashMap<String, String>(); 
                    param.put("name", property.getName());
                    param.put("owner", property.getOwner());
                    param.put("value", property.getValue());
                    for (XmlChannel channel : property.getChannels()) {
                        bulkRequest.add(new UpdateRequest("channelfinder", "channel", channel.getName())
                                .script("removeProperty = new Object();"
                                        + "for (xmlProp in ctx._source.properties) "
                                        + "{ if (xmlProp.name == property.name) { removeProperty = xmlProp} }; "
                                        + "ctx._source.tags.remove(removeProperty);"
                                        + "ctx._source.tags.add(property)")
                                .addScriptParam("property", param));
                    }
                }
            }
            bulkRequest.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
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
                Response r = Response.noContent().build();
                audit.info(um.getUserName() + "|" + uriInfo.getPath() + "|PUT|OK|" + r.getStatus() + "|data=" + data);
                return r;
            }
        } catch (Exception e) {
            return handleException(um.getUserName(), "POST", Response.Status.INTERNAL_SERVER_ERROR, e);
        } finally {
            client.close();
        }
    }
    /**
     * GET method for retrieving the property with the path parameter
     * <tt>propName</tt> 
     * 
     * To get all its channels use the parameter "withChannels"
     *
     * @param prop
     *            URI path parameter: property name to search for
     * @return list of channels with their properties and tags that match
     */
    @GET
    @Path("{propName : " + propertyNameRegex + "}")
    @Produces("application/json")
    public Response read(@PathParam("propName") String prop) {
        MultivaluedMap<String, String> parameters = uriInfo.getQueryParameters();
        Client client = getNewClient();
        String user = securityContext.getUserPrincipal() != null ? securityContext.getUserPrincipal().getName() : "";
        XmlProperty result = null;
        try {
            GetResponse response = client.prepareGet("properties", "property", prop).execute().actionGet();
            if (response.isExists()) {
                ObjectMapper mapper = new ObjectMapper();
                result = mapper.readValue(response.getSourceAsBytes(), XmlProperty.class);
                Response r;
                if (result == null) {
                    r = Response.status(Response.Status.NOT_FOUND).build();
                } else {
                    //TODO iterator or scrolling needed
                    if (parameters.containsKey("withChannels")) {
                        final SearchResponse channelResult = client.prepareSearch("channelfinder")
                                .setQuery(matchQuery("properties.name", prop.trim()))
                                .setSize(10000).execute().actionGet();
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
                log.fine(user + "|" + uriInfo.getPath() + "|GET|OK|" + r.getStatus());
                return r;
            } else {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        } catch (Exception e) {
            return handleException(user, "GET", Response.Status.INTERNAL_SERVER_ERROR, e);
        } finally {
            client.close();
        }
    }

    /**
     * PUT method for creating and <b>exclusively</b> adding the property
     * identified by the path parameter <tt>propName</tt> to all channels
     * identified by the payload structure <tt>data</tt>. Setting the owner
     * attribute in the XML root element is mandatory. Values for the properties
     * are taken from the payload.
     *
     * TODO: implement the destructive write.
     *
     * @param prop URI path parameter: property name
     * @param data list of channels to add the property <tt>name</tt> to
     * @return HTTP Response
     */
    @PUT
    @Path("{propName : " + propertyNameRegex + "}")
    @Consumes("application/json" )
    public Response create(@PathParam("propName") String prop, XmlProperty data) {
        if (!prop.equals(data.getName())){
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Specified property name '"+prop+"' and payload property name '"+data.getName()+"' do not match").build();
        }
        long start = System.currentTimeMillis();
        Client client = getNewClient();
        UserManager um = UserManager.getInstance();
        um.setUser(securityContext.getUserPrincipal(), securityContext.isUserInRole("Administrator"));
        try {
            BulkRequestBuilder bulkRequest = client.prepareBulk();
            IndexRequest indexRequest = new IndexRequest("properties", "property", prop).source(jsonBuilder()
                    .startObject().field("name", data.getName()).field("owner", data.getOwner()).endObject());
            UpdateRequest updateRequest = new UpdateRequest("properties", "property", prop).doc(jsonBuilder()
                    .startObject().field("name", data.getName()).field("owner", data.getOwner()).endObject())
                    .upsert(indexRequest);
            bulkRequest.add(updateRequest);
            
            SearchResponse qbResult = client.prepareSearch("channelfinder")
                    .setQuery(QueryBuilders.matchQuery("properties.name", prop))
                    .setSearchType(SearchType.QUERY_THEN_FETCH)
                    .setFetchSource(new String[]{"name"}, null) 
                    .setSize(10000).execute().actionGet();
            if (qbResult != null) {
                for (SearchHit hit : qbResult.getHits()) {
                    String channelName = hit.field("name").getValue().toString();
                    bulkRequest.add(new UpdateRequest("channelfinder", "channel", channelName)
                            .script("removeProp = new Object();" 
                                    + "for (xmlProp in ctx._source.properties) "
                                    + "{ if (xmlProp.name == prop) { removeProp = xmlProp} }; "
                                    + "ctx._source.properties.remove(removeProp);")
                            .addScriptParam("prop", prop));
                }
            }
            
            if (data.getChannels() != null) {
                for (XmlChannel channel : data.getChannels()) {
                    HashMap<String, String> param = new HashMap<String, String>(); 
                    param.put("name", data.getName());
                    param.put("owner", data.getOwner());
                    String value = ChannelUtil.getProperty(channel, data.getName()).getValue();
                    if(value == null || value.isEmpty()){
                        return handleException(um.getUserName(), "POST", Status.BAD_REQUEST,
                                "Invalid property value (missing or null or empty string) for '"+data.getName()+"'");
                    }
                    param.put("value", ChannelUtil.getProperty(channel, data.getName()).getValue());
                    bulkRequest.add(new UpdateRequest("channelfinder", "channel", channel.getName())
                            .script("ctx._source.properties.add(prop)")
                            .addScriptParam("prop", param));
                }
            }
            bulkRequest.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
            BulkResponse bulkResponse = bulkRequest.execute().actionGet();
            if (bulkResponse.hasFailures()) {
                audit.severe(bulkResponse.buildFailureMessage());
                if (bulkResponse.buildFailureMessage().contains("DocumentMissingException")) {
                    return handleException(um.getUserName(), "PUT", Response.Status.NOT_FOUND,
                            bulkResponse.buildFailureMessage());
                } else {
                    return handleException(um.getUserName(), "PUT", Response.Status.INTERNAL_SERVER_ERROR,
                            bulkResponse.buildFailureMessage());
                }
            } else {
                GetResponse response = client.prepareGet("properties", "property", prop).execute().actionGet();
                ObjectMapper mapper = new ObjectMapper();
                XmlProperty result = mapper.readValue(response.getSourceAsBytes(), XmlProperty.class);
                Response r;
                if (result == null) {
                    r = Response.status(Response.Status.NOT_FOUND).build();
                } else {
                    r = Response.ok(result).build();
                }
                audit.info(um.getUserName() + "|" + uriInfo.getPath() + "|PUT|OK|" + r.getStatus() + "|data=" + XmlProperty.toLog(data));
                return r;
            }
        } catch (Exception e) {
            return handleException(um.getUserName(), "PUT", Response.Status.INTERNAL_SERVER_ERROR, e);
        } finally {
            client.close();
        }
    }

    /**
     * POST method for updating the property identified by the path parameter
     * <tt>name</tt>, adding it to all channels identified by the payload
     * structure <tt>data</tt>. Setting the owner attribute in the XML root
     * element is mandatory. Values for the properties are taken from the
     * payload.
     *
     * @param prop URI path parameter: property name
     * @param data list of channels to add the property <tt>name</tt> to
     * @return HTTP Response
     */
    @POST
    @Path("{propName : " + propertyNameRegex + "}")
    @Consumes("application/json")
    public Response update(@PathParam("propName") String prop, XmlProperty data) {
        Client client = getNewClient();
        UserManager um = UserManager.getInstance();
        um.setUser(securityContext.getUserPrincipal(), securityContext.isUserInRole("Administrator"));
        if(data.getName() == null || data.getName().isEmpty()){
            handleException(um.getUserName(), "POST", Status.BAD_REQUEST, "payload data has invalid/incorrect property name " + data.getName());
        }
        try {
            GetResponse response = client.prepareGet("properties", "property", prop).execute().actionGet();
            if(!response.isExists()){
                return handleException(um.getUserName(), "POST", Response.Status.NOT_FOUND, "A property named '"+prop+"' does not exist");
            }
            ObjectMapper mapper = new ObjectMapper();
            XmlProperty original = mapper.readValue(response.getSourceAsBytes(), XmlProperty.class);
            // rename a property
            if(!original.getName().equals(data.getName())){
                return renameProperty(um, client, original, data);
            }
            
            String propOwner = data.getOwner() != null && !data.getOwner().isEmpty()? data.getOwner() : original.getOwner();
            
            BulkRequestBuilder bulkRequest = client.prepareBulk();
            UpdateRequest updateRequest = new UpdateRequest("properties", "property", prop)
                                                            .doc(jsonBuilder()
                                                                    .startObject()
                                                                    .field("name", data.getName())
                                                                    .field("owner", propOwner)
                                                                    .endObject());

            if(!original.getOwner().equals(data.getOwner())){
                HashMap<String, String> param = new HashMap<String, String>(); 
                param.put("name", data.getName());
                param.put("owner", propOwner);
                SearchResponse queryResponse = client.prepareSearch("channelfinder")
                        .setQuery(wildcardQuery("properties.name", original.getName().trim()))
                        .setSearchType(SearchType.QUERY_THEN_FETCH)
                        .setFetchSource(new String[]{"name"}, null) 
                        .setSize(10000).execute().actionGet();
                for (SearchHit hit : queryResponse.getHits()) {
                    bulkRequest.add(new UpdateRequest("channelfinder", "channel", hit.getId())
                            .script("origProp = new Object();"
                                    + "for (xmlProp in ctx._source.properties) "
                                    + "{ if (xmlProp.name == prop.name) { origProp = xmlProp} }; "
                                    + "ctx._source.properties.remove(origProp);"
                                    + "origProp.owner = prop.owner;"
                                    + "ctx._source.properties.add(origProp)")
                            .addScriptParam("prop", param));
                }
            }
            bulkRequest.add(updateRequest);
            if (data.getChannels() != null) {
                for (XmlChannel channel : data.getChannels()) {
                    HashMap<String, String> param = new HashMap<String, String>(); 
                    param.put("name", data.getName());
                    param.put("owner", propOwner);
                    String value = ChannelUtil.getProperty(channel, data.getName()).getValue();
                    if(value == null || value.isEmpty()){
                        return handleException(um.getUserName(), "POST", Status.BAD_REQUEST,
                                "Invalid property value (missing or null or empty string) for '"+data.getName()+"'");
                    }
                    param.put("value", value);
                    bulkRequest.add(new UpdateRequest("channelfinder", "channel", channel.getName())
                            .script("removeProp = new Object();"
                                    + "for (property in ctx._source.properties) "
                                    + "{ if (property.name == prop.name) { removeProp = property} }; "
                                    + "ctx._source.properties.remove(removeProp);"
                                    + "ctx._source.properties.add(prop)")
                            .addScriptParam("prop", param));
                }
            }
            bulkRequest.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
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
                Response r = Response.ok(bulkResponse.getItems()[0].getResponse()).build();
                audit.info(um.getUserName() + "|" + uriInfo.getPath() + "|POST|OK|" + r.getStatus() + "|data=" + XmlProperty.toLog(data));
                return r;
            }
        } catch (Exception e) {
            return handleException(um.getUserName(), "POST", Response.Status.INTERNAL_SERVER_ERROR, e);
        } finally {
            client.close();
        }
    }

    private Response renameProperty(UserManager um, Client client, XmlProperty original, XmlProperty data) {
        try {
            SearchResponse queryResponse = client.prepareSearch("channelfinder")
                    .setQuery(wildcardQuery("properties.name", original.getName().trim()))
                    .setSearchType(SearchType.QUERY_THEN_FETCH)
                    .setFetchSource(new String[]{"name"}, null) 
                    .setSize(10000).execute().actionGet();
            List<String> channelNames = new ArrayList<String>();
            for (SearchHit hit : queryResponse.getHits()) {
                channelNames.add(hit.getId());
            }
            BulkRequestBuilder bulkRequest = client.prepareBulk();
            bulkRequest.add(new DeleteRequest("properties", "property", original.getName()));
            IndexRequest indexRequest = new IndexRequest("properties", "property", data.getName()).source(jsonBuilder()
                    .startObject().field("name", data.getName()).field("owner", data.getOwner()).endObject());
            UpdateRequest updateRequest;
            updateRequest = new UpdateRequest("properties", "property", data.getName()).doc(jsonBuilder().startObject()
                    .field("name", data.getName()).field("owner", data.getOwner()).endObject()).upsert(indexRequest);
            bulkRequest.add(updateRequest);
            if (!channelNames.isEmpty()) {
                HashMap<String, String> originalParam = new HashMap<String, String>(); 
                originalParam.put("name", original.getName());
                HashMap<String, String> param = new HashMap<String, String>(); 
                param.put("name", data.getName());
                for (String channel : channelNames) {
                    bulkRequest.add(new UpdateRequest("channelfinder", "channel", channel)
                            .script("origProp = new Object();"
                                    + "for (xmlProp in ctx._source.properties) "
                                    + "{ if (xmlProp.name == originalProp.name) { origProp = xmlProp} }; "
                                    + "ctx._source.properties.remove(origProp);"
                                    + "origProp.name = newProp.name;"
                                    + "ctx._source.properties.add(origProp)")
                            .addScriptParam("originalProp", originalParam)
                            .addScriptParam("newProp", param));
                }
            }
            bulkRequest.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
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
                Response r = Response.ok(bulkResponse).build();
                audit.info(um.getUserName() + "|" + uriInfo.getPath() + "|POST|OK|" + r.getStatus() + "|data="
                        + XmlProperty.toLog(data));
                return r;
            }
        } catch (IOException e) {
            return handleException(um.getUserName(), "POST", Response.Status.INTERNAL_SERVER_ERROR, e);
        }
    }

    /**
     * DELETE method for deleting the property identified by the path parameter
     * <tt>name</tt> from all channels.
     *
     * @param prop
     *            URI path parameter: tag name to remove
     * @return HTTP Response
     */
    @DELETE
    @Path("{propName : " + propertyNameRegex + "}")
    public Response remove(@PathParam("propName") String prop) {
        Client client = getNewClient();
        UserManager um = UserManager.getInstance();
        um.setUser(securityContext.getUserPrincipal(), securityContext.isUserInRole("Administrator"));
        try {
            BulkRequestBuilder bulkRequest = client.prepareBulk();
            bulkRequest.add(new DeleteRequest("properties", "property", prop));
            SearchResponse qbResult = client.prepareSearch("channelfinder")
                    .setQuery(QueryBuilders.matchQuery("properties.name", prop))
                    .setSearchType(SearchType.QUERY_THEN_FETCH)
                    .setFetchSource(new String[]{"name"}, null) 
                    .setSize(10000).execute().actionGet();
            if (qbResult != null) {
                for (SearchHit hit : qbResult.getHits()) {
                    String channelName = hit.field("name").getValue().toString();
                    bulkRequest.add(new UpdateRequest("channelfinder", "channel", channelName)
                            .script("removeProp = new Object();" 
                                    + "for (xmlProp in ctx._source.properties) "
                                    + "{ if (xmlProp.name == prop) { removeProp = xmlProp} }; "
                                    + "ctx._source.properties.remove(removeProp);")
                            .addScriptParam("prop", prop));
                }
            }
            
            bulkRequest.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
            BulkResponse bulkResponse = bulkRequest.execute().actionGet();
            if (bulkResponse.hasFailures()) {
                audit.severe(bulkResponse.buildFailureMessage());
                throw new Exception();
            } else {
                DeleteResponse deleteResponse = bulkResponse.getItems()[0].getResponse();
                if (deleteResponse.getResult() == DocWriteResponse.Result.DELETED) {
                    Response r = Response.ok().build();
                    audit.info(um.getUserName() + "|" + uriInfo.getPath() + "|DELETE|OK|" + r.getStatus());
                    return r;
                } else {
                    return handleException(um.getUserName(), "PUT", Response.Status.NOT_FOUND,
                            new Exception("Property " + prop + " does not exist."));
                }
            }
        } catch (Exception e) {
            return handleException(um.getUserName(), "PUT", Response.Status.INTERNAL_SERVER_ERROR, e);
        } finally {
            client.close();
        }
    }

    /**
     * PUT method for adding the property identified by <tt>prop</tt> to the
     * channel <tt>chan</tt> (both path parameters).
     *
     * @param prop URI path parameter: property name
     * @param chan URI path parameter: channel to addSingle <tt>tag</tt> to
     * @param data property data (specifying property ownership and value)
     * @return HTTP Response
     */
    @PUT
    @Path("{propName}/{chName}")
    @Consumes({ "application/xml", "application/json" })
    public Response addSingle(@PathParam("propName") String prop, @PathParam("chName") String chan, XmlProperty data) {
        Client client = getNewClient();
        UserManager um = UserManager.getInstance();
        if (data.getValue() == null || data.getValue().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid property value (missing or null or empty string) for '" + prop + "'").build();
        }
        if (!prop.equals(data.getName())){
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Specified property name '"+prop+"' and payload property name '"+data.getName()+"' do not match").build();
        }
        um.setUser(securityContext.getUserPrincipal(), securityContext.isUserInRole("Administrator"));
        XmlProperty result = null;
        try {
            GetResponse response = client.prepareGet("properties", "property", prop).execute().actionGet();
            ObjectMapper mapper = new ObjectMapper();
            mapper.addMixIn(XmlChannel.class, MyMixInForXmlChannels.class);
            result = mapper.readValue(response.getSourceAsBytes(), XmlProperty.class);
            if (result != null) {
                XContentBuilder xb =  XContentFactory.jsonBuilder().startObject();
                xb.startArray("properties");
                xb.startObject();
                xb.field("name", data.getName());      
                xb.field("value", data.getValue());
                // ignores the provided user and matches the one present in the properties index
                xb.field("owner", result.getOwner());
                xb.endObject();
                xb.endArray();
                xb.endObject();

                client.update(new UpdateRequest("channelfinder", "channel", chan)
                        .doc(xb)).get();

                Response r = Response.ok().build();
                return r;
            }else{
                return handleException(um.getUserName(), "PUT", Status.BAD_REQUEST,
                        "Property " +prop+ " does not exist ");
            }
        } catch (DocumentMissingException | RemoteTransportException | ExecutionException e) {
            return handleException(um.getUserName(), "PUT", Response.Status.BAD_REQUEST,
                    "Channels specified in property update do not exist" + e.getMessage());
        } catch (Exception e) {
            return handleException(um.getUserName(), "PUT", Response.Status.INTERNAL_SERVER_ERROR, e);
        } finally {
            client.close();
        }
    }

    /**
     * DELETE method for deleting the property identified by <tt>prop</tt> from
     * the channel <tt>chan</tt> (both path parameters).
     *
     * @param prop URI path parameter: property name to remove
     * @param chan URI path parameter: channel to remove <tt>property</tt> from
     * @return HTTP Response
     */
    @DELETE
    @Path("{propName}/{chName}")
    public Response removeSingle(@PathParam("propName") String prop, @PathParam("chName") String chan) {
        Client client = getNewClient();
        UserManager um = UserManager.getInstance();
        um.setUser(securityContext.getUserPrincipal(), securityContext.isUserInRole("Administrator"));
        try {
            if(client.prepareGet("properties", "property", prop).get().isExists()){
                UpdateResponse updateResponse = client.update(new UpdateRequest("channelfinder", "channel", chan)
                        .script(" removeProps = new java.util.ArrayList();" + "for (property in ctx._source.properties)"
                                + "{ if (property.name == prop) { removeProps.add(property)} };"
                                + "for (removeProp in removeProps) {ctx._source.properties.remove(removeProp)}")
                        .addScriptParam("prop", prop)).actionGet();
                Response r = Response.ok().build();
                return r;
            } else {
                return handleException(um.getUserName(), "DELETE", Status.NOT_FOUND, "Property " +prop+ " does not exist ");
            }
        } catch (DocumentMissingException e) {
            return handleException(um.getUserName(), "DELETE", Status.NOT_FOUND, "Channel does not exist " + e.getDetailedMessage());
        } catch (Exception e) {
            return handleException(um.getUserName(), "DELETE", Response.Status.INTERNAL_SERVER_ERROR, e);
        } finally {
            client.close();
        }
    }

    private Response handleException(String user, String method, Response.Status status, Exception e) {
        return handleException(user, method, status, e.getMessage());
    }

    private Response handleException(String user, String method, Response.Status status, String message) {
        log.warning(user + "|" + uriInfo.getPath() + "|"+method+"|ERROR|" + status + "|cause=" + message);
        return new CFException(status, message).toResponse();
    }
}
