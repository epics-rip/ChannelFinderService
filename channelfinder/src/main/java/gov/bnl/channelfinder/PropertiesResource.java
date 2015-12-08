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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

import gov.bnl.channelfinder.TagsResource.MyMixInForXmlChannels;

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
     * @param name
     *            URI path parameter: tag name to search for
     * @return list of channels with their properties and tags that match
     */

    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    public Response list() {
        Client client = getNewClient();
        String user = securityContext.getUserPrincipal() != null ? securityContext.getUserPrincipal().getName() : "";
        List<XmlProperty> result = new ArrayList<XmlProperty>();
        ObjectMapper mapper = new ObjectMapper();
        try {
            SearchResponse response = client.prepareSearch("properties")
                                            .setTypes("property")
                                            .setQuery(new MatchAllQueryBuilder())
                                            .setSize(10000)
                                            .execute().actionGet();
            for (SearchHit hit : response.getHits()) {
                result.add(mapper.readValue(hit.getSourceAsString(), XmlProperty.class));
            }
            Response r = Response.ok(result.toArray(new XmlProperty[result.size()])).build();
            audit.info(user + "|" + uriInfo.getPath() + "|GET|OK|" + r.getStatus() + "|returns " + result.size() + " properties");
            return r;
        } catch (Exception e) {
            return handleException(user, Response.Status.INTERNAL_SERVER_ERROR, e);
        } finally {
            client.close();
        }
    }

    /**
     * POST method for creating multiple properties.
     *
     * @param data XmlProperties data (from payload)
     * @return HTTP Response
     * @throws IOException
     *             when audit or log fail
     */
    @POST
    @Consumes({ "application/xml", "application/json" })
    public Response add(List<XmlProperty> data) throws IOException {
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
            bulkRequest.setRefresh(true);
            BulkResponse bulkResponse = bulkRequest.execute().actionGet();
            if (bulkResponse.hasFailures()) {
                // TODO remove the null
                return handleException(um.getUserName(), Response.Status.INTERNAL_SERVER_ERROR, null);
            } else {
                Response r = Response.noContent().build();
                audit.info(um.getUserName() + "|" + uriInfo.getPath() + "|POST|OK|" + r.getStatus() + "|data="
                        + data);
                return r;
            }
        } catch (Exception e) {
            return handleException(um.getUserName(), Response.Status.INTERNAL_SERVER_ERROR, e);
        } finally {
            client.close();
        }
    }

    /**
     * GET method for retrieving the property with the path parameter
     * <tt>propName</tt> and its channels.
     *
     * @param prop
     *            URI path parameter: property name to search for
     * @return list of channels with their properties and tags that match
     */
    @GET
    @Path("{propName : " + propertyNameRegex + "}")
    @Produces({ "application/xml", "application/json" })
    public Response read(@PathParam("propName") String prop) {
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
                    r = Response.ok(result).build();
                }
                log.fine(user + "|" + uriInfo.getPath() + "|GET|OK|" + r.getStatus());
                return r;
            } else {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        } catch (Exception e) {
            return handleException(user, Response.Status.INTERNAL_SERVER_ERROR, e);
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
    @Consumes({ "application/xml", "application/json" })
    public Response create(@PathParam("propName") String prop, XmlProperty data) {
        long start = System.currentTimeMillis();
        Client client = getNewClient();
        System.out.println("client initialization: " + (System.currentTimeMillis() - start));
        UserManager um = UserManager.getInstance();
        um.setUser(securityContext.getUserPrincipal(), securityContext.isUserInRole("Administrator"));
        try {
            BulkRequestBuilder bulkRequest = client.prepareBulk();
            IndexRequest indexRequest = new IndexRequest("properties", "property", prop).source(jsonBuilder()
                    .startObject().field("name", data.getName()).field("owner", data.getOwner()).endObject());
            UpdateRequest updateRequest = new UpdateRequest("properties", "property", prop).doc(jsonBuilder()
                    .startObject().field("name", data.getName()).field("owner", data.getOwner()).endObject())
                    .upsert(indexRequest).refresh(true);
            bulkRequest.add(updateRequest);
            
            SearchResponse qbResult = client.prepareSearch("channelfinder")
                    .setQuery(QueryBuilders.matchQuery("xmlProperties.properties.name", prop)).addField("name").setSize(10000).execute().actionGet();
            if (qbResult != null) {
                for (SearchHit hit : qbResult.getHits()) {
                    String channelName = hit.field("name").getValue().toString();
                    bulkRequest.add(new UpdateRequest("channelfinder", "channel", channelName).refresh(true)
                            .script("removeProp = new Object();" 
                                    + "for (xmlProp in ctx._source.xmlProperties.properties) "
                                    + "{ if (xmlProp.name == prop) { removeProp = xmlProp} }; "
                                    + "ctx._source.xmlProperties.properties.remove(removeProp);")
                            .addScriptParam("prop", prop));
                }
            }
            
            if (data.getXmlChannels() != null) {
                for (XmlChannel channel : data.getXmlChannels().getChannels()) {
                    HashMap<String, String> param = new HashMap<String, String>(); 
                    param.put("name", data.getName());
                    param.put("owner", data.getOwner());
                    param.put("value", ChannelUtil.getProperty(channel, data.getName()).getValue());
                    bulkRequest.add(new UpdateRequest("channelfinder", "channel", channel.getName())
                            .refresh(true)
                            .script("ctx._source.xmlProperties.properties.add(prop)")
                            .addScriptParam("prop", param));
                }
            }
            bulkRequest.setRefresh(true);
            BulkResponse bulkResponse = bulkRequest.execute().actionGet();
            if (bulkResponse.hasFailures()) {
                audit.severe(bulkResponse.buildFailureMessage());
                throw new Exception();
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
                audit.info(um.getUserName() + "|" + uriInfo.getPath() + "|POST|OK|" + r.getStatus() + "|data=" + XmlProperty.toLog(data));
                return r;
            }
        } catch (Exception e) {
            return handleException("todo", Response.Status.INTERNAL_SERVER_ERROR, e);
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
    @Consumes({ "application/xml", "application/json" })
    public Response update(@PathParam("propName") String prop, XmlProperty data) {
        Client client = getNewClient();
        UserManager um = UserManager.getInstance();
        um.setUser(securityContext.getUserPrincipal(), securityContext.isUserInRole("Administrator"));
        try {
            BulkRequestBuilder bulkRequest = client.prepareBulk();
            UpdateRequest updateRequest = new UpdateRequest("properties", "property", prop).doc(jsonBuilder()
                    .startObject().field("name", data.getName()).field("owner", data.getOwner()).endObject());
            bulkRequest.add(updateRequest);
            if (data.getXmlChannels() != null) {
                for (XmlChannel channel : data.getXmlChannels().getChannels()) {
                    HashMap<String, String> param = new HashMap<String, String>(); 
                    param.put("name", data.getName());
                    param.put("owner", data.getOwner());
                    param.put("value", ChannelUtil.getProperty(channel, data.getName()).getValue());
                    bulkRequest.add(new UpdateRequest("channelfinder", "channel", channel.getName())
                            .refresh(true)
                            .script("removeProp = new Object();"
                                    + "for (xmlProperty in ctx._source.xmlProperties.properties) "
                                    + "{ if (xmlProperty.name == prop.name) { removeProp = xmlProperty} }; "
                                    + "ctx._source.xmlProperties.properties.remove(removeProp);"
                                    + "ctx._source.xmlProperties.properties.add(prop)")
                            .addScriptParam("prop", param));
                }
            }
            bulkRequest.setRefresh(true);
            BulkResponse bulkResponse = bulkRequest.execute().actionGet();
            if (bulkResponse.hasFailures()) {
                audit.severe(bulkResponse.buildFailureMessage());
                throw new Exception();
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
                audit.info(um.getUserName() + "|" + uriInfo.getPath() + "|POST|OK|" + r.getStatus() + "|data=" + XmlProperty.toLog(data));
                return r;
            }
        } catch (Exception e) {
            return handleException(um.getUserName(), Response.Status.INTERNAL_SERVER_ERROR, e);
        } finally {
            client.close();
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
                    .setQuery(QueryBuilders.matchQuery("xmlProperties.properties.name", prop)).addField("name").setSize(10000).execute().actionGet();
            if (qbResult != null) {
                for (SearchHit hit : qbResult.getHits()) {
                    String channelName = hit.field("name").getValue().toString();
                    bulkRequest.add(new UpdateRequest("channelfinder", "channel", channelName).refresh(true)
                            .script("removeProp = new Object();" 
                                    + "for (xmlProp in ctx._source.xmlProperties.properties) "
                                    + "{ if (xmlProp.name == prop) { removeProp = xmlProp} }; "
                                    + "ctx._source.xmlProperties.properties.remove(removeProp);")
                            .addScriptParam("prop", prop));
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
     * PUT method for adding the property identified by <tt>prop</tt> to the
     * channel <tt>chan</tt> (both path parameters).
     *
     * @param prop URI path parameter: property name
     * @param chan URI path parameter: channel to addSingle <tt>tag</tt> to
     * @param data property data (specifying property ownership & value)
     * @return HTTP Response
     */
    @PUT
    @Path("{tagName}/{chName}")
    @Consumes({ "application/xml", "application/json" })
    public Response addSingle(@PathParam("tagName") String prop, @PathParam("chName") String chan, XmlProperty data) {
        Client client = getNewClient();
        UserManager um = UserManager.getInstance();
        um.setUser(securityContext.getUserPrincipal(), securityContext.isUserInRole("Administrator"));
        XmlProperty result = null;
        try {
            GetResponse response = client.prepareGet("properties", "property", prop).execute().actionGet();
            ObjectMapper mapper = new ObjectMapper();
            mapper.getSerializationConfig().addMixInAnnotations(XmlChannels.class, MyMixInForXmlChannels.class);
            result = mapper.readValue(response.getSourceAsBytes(), XmlProperty.class);
            if (result != null) {
                String str = mapper.writeValueAsString(result);
                HashMap<String, String> param = new HashMap<String, String>(); 
                param.put("name", data.getName());
                param.put("value", data.getValue());
                // ignores the provided user and matches the one present in the properties index
                param.put("owner", result.getOwner());

                UpdateResponse updateResponse = client.update(new UpdateRequest("channelfinder", "channel", chan)
                        .script("removeProps = new java.util.ArrayList(); "
                                + "for (property in ctx._source.xmlProperties.properties) "
                                + "{ if (property.name == prop.name) { removeProps.add(property)} }; "
                                + "for (removeProp in removeProps) {ctx._source.xmlProperties.properties.remove(removeProp)}; "
                                + "ctx._source.xmlProperties.properties.add(prop)")
                        .addScriptParam("prop", param)).actionGet();
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
     * DELETE method for deleting the property identified by <tt>prop</tt> from
     * the channel <tt>chan</tt> (both path parameters).
     *
     * @param prop
     *            URI path parameter: property name to remove
     * @param chan
     *            URI path parameter: channel to remove <tt>property</tt> from
     * @return HTTP Response
     */
    @DELETE
    @Path("{propName}/{chName}")
    public Response removeSingle(@PathParam("propName") String prop, @PathParam("chName") String chan) {
        Client client = getNewClient();
        UserManager um = UserManager.getInstance();
        um.setUser(securityContext.getUserPrincipal(), securityContext.isUserInRole("Administrator"));
        XmlChannel result = null;
        try {
            UpdateResponse updateResponse = client.update(new UpdateRequest("channelfinder", "channel", chan)
                    .script(" removeProps = new java.util.ArrayList();"
                            + "for (property in ctx._source.xmlProperties.properties)"
                            + "{ if (property.name == prop) { removeProps.add(property)} };"
                            + "for (removeProp in removeProps) {ctx._source.xmlProperties.properties.remove(removeProp)}")
                    .addScriptParam("prop", prop)
                    .refresh(true)).actionGet();
            Response r = Response.ok().build();
            return r;
        } catch (Exception e) {
            return handleException(um.getUserName(), Response.Status.INTERNAL_SERVER_ERROR, e);
        } finally {
            client.close();
        }
    }

    private Response handleException(String user, Response.Status status, Exception e) {
        log.warning(user + "|" + uriInfo.getPath() + "|GET|ERROR|" + status + "|cause=" + e);
        return new CFException(status, e.getMessage()).toResponse();
    }
}
