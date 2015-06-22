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

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.disMaxQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.nestedQuery;
import static org.elasticsearch.index.query.QueryBuilders.wildcardQuery;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
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
import javax.ws.rs.core.MultivaluedMap;
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
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.DisMaxQueryBuilder;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.elasticsearch.search.SearchHit;
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
        Client client = new TransportClient().addTransportAddress(new InetSocketTransportAddress("130.199.219.147", 9300));
/**
 * Current Mapping for the channel 
 * PUT /channelfinder/_mapping/channel
{
  "channel": {
    "properties": {
      "name": {
        "type": "string",
        "analyzer": "whitespace"
      },
      "owner": {
        "type": "string",
        "analyzer": "whitespace"
      },
      "xmlProperties": {
        "properties": {
          "properties": {
            "type": "nested",
            "include_in_parent": true,
            "properties": {
              "name": {
                "type": "string",
                "analyzer": "whitespace"
              },
              "owner": {
                "type": "string"
              },
              "value": {
                "type": "string",
                "analyzer": "whitespace"
              }
            }
          }
        }
      },
      "xmlTags": {
        "properties": {
          "tags": {
            "type": "nested",
            "properties": {
              "name": {
                "type": "string",
                "analyzer": "whitespace"
              },
              "owner": {
                "type": "string",
                "analyzer": "whitespace"
              }
            }
          }
        }
      }
    }
  }
}
 */
        String user = securityContext.getUserPrincipal() != null ? securityContext.getUserPrincipal().getName() : "";
        try {
            MultivaluedMap<String, String> parameters = uriInfo.getQueryParameters();
            BoolQueryBuilder qb = boolQuery();
            DisMaxQueryBuilder nameQuery = disMaxQuery();
            
            
            for (Entry<String, List<String>> parameter : parameters.entrySet()) {
                switch (parameter.getKey()) {
                case "~name":
                    for (String pattern : parameter.getValue()) {
                        nameQuery.add(wildcardQuery("name", pattern));
                    }
                    qb.must(nameQuery);
                    break;
                case "~tag":
                    DisMaxQueryBuilder tagQuery = disMaxQuery();
                    for (String pattern : parameter.getValue()) {
                        tagQuery.add(wildcardQuery("xmlTags.tags.name", pattern));
                    }
                    qb.must(nestedQuery("xmlTags.tags", tagQuery));
                    break;
                default:
                    DisMaxQueryBuilder propertyQuery = disMaxQuery();
                    for (String pattern : parameter.getValue()) {
                        propertyQuery
                                .add(nestedQuery("xmlProperties.properties",
                                        boolQuery()
                                                .must(matchQuery("xmlProperties.properties.name", parameter.getKey()))
                                                .must(wildcardQuery("xmlProperties.properties.value", pattern))));
                    }
                    qb.must(propertyQuery);
                    break;
                }
            }
            
            long start = System.currentTimeMillis();
            SearchResponse qbResult = client.prepareSearch("channelfinder").setQuery(qb).setSize(10000).execute().actionGet();
            audit.info("Query: " + (System.currentTimeMillis() - start));
            audit.info("Query Size : " + qbResult.getHits().getTotalHits());
            ObjectMapper mapper = new ObjectMapper();
            XmlChannels result = new XmlChannels();
            start = System.currentTimeMillis();
            if(qbResult != null){
                for (SearchHit hit : qbResult.getHits()) {
                    result.addXmlChannel(mapper.readValue(hit.source(), XmlChannel.class));
                }
            }
            audit.info("Parse: " + (System.currentTimeMillis() - start));
            Response r = Response.ok(result).build();
            log.fine(user + "|" + uriInfo.getPath() + "|GET|OK|" + r.getStatus()
                    + "|returns " + result.getChannels().size() + " channels");
            return r;
        } catch (Exception e) {
            return handleException(user, "GET", Response.Status.INTERNAL_SERVER_ERROR, e);
        } finally {
            client.close();
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
        Client client = new TransportClient().addTransportAddress(new InetSocketTransportAddress("130.199.219.147", 9300));
        UserManager um = UserManager.getInstance();
        um.setUser(securityContext.getUserPrincipal(), securityContext.isUserInRole("Administrator"));
        ObjectMapper mapper = new ObjectMapper();
        try {
            long start = System.currentTimeMillis();
            validateChannels(data, client);
            audit.info(um.getUserName() + "|" + uriInfo.getPath() + "|POST|validation : "+ (System.currentTimeMillis() - start));
            start = System.currentTimeMillis();
            BulkRequestBuilder bulkRequest = client.prepareBulk();
            for (XmlChannel channel : data.getChannels()) {
                bulkRequest.add(client.prepareUpdate("channelfinder", "channel", channel.getName()).setDoc(mapper.writeValueAsBytes(channel))
                        .setUpsert(new IndexRequest("channelfinder", "channel", channel.getName()).source(mapper.writeValueAsBytes(channel))));
            }
            String prepare = "|Prepare: " + (System.currentTimeMillis()-start) + "|";
            start = System.currentTimeMillis();
            BulkResponse bulkResponse = bulkRequest.execute().actionGet();
            String execute = "|Execute: " + (System.currentTimeMillis()-start)+"|";
            start = System.currentTimeMillis();
            if (bulkResponse.hasFailures()) {
                audit.severe(bulkResponse.buildFailureMessage());
                throw new Exception();
            } else {
                Response r = Response.noContent().build();
                audit.info(um.getUserName() + "|" + uriInfo.getPath() + "|POST|OK|" + r.getStatus() + prepare + execute + "|data="
                        + XmlChannels.toLog(data));
                return r;
            }
        } catch (IllegalArgumentException e) {
            return handleException(um.getUserName(), "POST", Response.Status.BAD_REQUEST, e);
        } catch (Exception e) {
            return handleException(um.getUserName(), "POST", Response.Status.INTERNAL_SERVER_ERROR, e);
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
    @Produces({"application/xml", "application/json"})
    public Response read(@PathParam("chName") String chan) {
        audit.info("getting ch:" + chan);
        long start = System.currentTimeMillis();
        Client client = new TransportClient().addTransportAddress(new InetSocketTransportAddress("130.199.219.147", 9300));
        System.out.println("client initialization: "+ (System.currentTimeMillis() - start));
        String user = securityContext.getUserPrincipal() != null ? securityContext.getUserPrincipal().getName() : "";
        XmlChannel result = null;
        try {
            GetResponse response = client.prepareGet("channelfinder", "channel", chan).execute().actionGet();
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
            start = System.currentTimeMillis();
            validateChannel(data, client);
            audit.info(um.getUserName() + "|" + uriInfo.getPath() + "|PUT|validation : "+ (System.currentTimeMillis() - start));
            IndexRequest indexRequest = new IndexRequest("channelfinder", "channel", chan)
                    .source(mapper.writeValueAsBytes(data));
            UpdateRequest updateRequest = new UpdateRequest("channelfinder", "channel", chan)
                    .doc(mapper.writeValueAsBytes(data)).upsert(indexRequest);
            UpdateResponse result = client.update(updateRequest).get();
            Response r = Response.noContent().build();
            audit.info(um.getUserName() + "|" + uriInfo.getPath() + "|PUT|OK|" + r.getStatus() + "|data="
                    + XmlChannel.toLog(data));
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
        Client client = new TransportClient().addTransportAddress(new InetSocketTransportAddress("130.199.219.147", 9300));
        System.out.println("client initialization: "+ (System.currentTimeMillis() - start));
        UserManager um = UserManager.getInstance();
        um.setUser(securityContext.getUserPrincipal(), securityContext.isUserInRole("Administrator"));
        ObjectMapper mapper = new ObjectMapper();
        try {
            start = System.currentTimeMillis();
            validateChannel(data, client);
            audit.info(um.getUserName() + "|" + uriInfo.getPath() + "|POST|validation : "+ (System.currentTimeMillis() - start));
            UpdateRequest updateRequest = new UpdateRequest("channelfinder", "channel", chan)
                    .doc(mapper.writeValueAsBytes(data));
            UpdateResponse result = client.update(updateRequest).get();
            Response r = Response.noContent().build();
            audit.info(um.getUserName() + "|" + uriInfo.getPath() + "|POST|OK|" + r.getStatus()
                    + "|data=" + XmlChannel.toLog(data));
            return r;
        } catch (IllegalArgumentException e) {
            return handleException(um.getUserName(), "POST", Response.Status.BAD_REQUEST, e);
        } catch (Exception e) {
            return handleException(um.getUserName(), "POST", Response.Status.INTERNAL_SERVER_ERROR, e);
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
            DeleteResponse response = client.prepareDelete("channelfinder", "channel", chan).execute().get();
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
    
    /**
     * Check is all the tags and properties already exist
     * @return
     * @throws IOException 
     * @throws JsonMappingException 
     * @throws JsonParseException 
     */
    private boolean validateChannels(XmlChannels channels, Client client) throws JsonParseException, JsonMappingException, IOException{
        for (XmlChannel channel : channels.getChannels()) {
            if (channel.getName() == null || channel.getName().isEmpty()) {
                throw new IllegalArgumentException("Invalid channel name ");
            }
            for (XmlProperty xmlProperty : channel.getXmlProperties().getProperties()) {
                if (xmlProperty.getValue() == null || xmlProperty.getValue().isEmpty()) {
                    throw new IllegalArgumentException("Invalid property value (missing or null or empty string) for '"+xmlProperty.getName()+"'");
                }
            }
        }
        List<String> tagsNames = new ArrayList<String>();
        List<String> propertyNames = new ArrayList<String>();
        ObjectMapper mapper = new ObjectMapper();
        SearchResponse response = client.prepareSearch("properties").setTypes("property")
                .setQuery(new MatchAllQueryBuilder()).execute().actionGet();
        for (SearchHit hit : response.getHits()) {
            propertyNames.add(mapper.readValue(hit.getSourceAsString(), XmlProperty.class).getName());
        }
        response = client.prepareSearch("tags").setTypes("tag").setQuery(new MatchAllQueryBuilder()).execute()
                .actionGet();
        for (SearchHit hit : response.getHits()) {
            tagsNames.add(mapper.readValue(hit.getSourceAsString(), XmlTag.class).getName());
        }
        if (tagsNames.containsAll(ChannelUtil.getTagNames(channels.getChannels()))
                && propertyNames.containsAll(ChannelUtil.getPropertyNames(channels.getChannels()))) {
            return true;
        }else{
            throw new IllegalArgumentException("The Tags and/or Properties don't exist");
        }
    }
    
    /**
     * Check is all the tags and properties already exist
     * @return
     * @throws IOException 
     * @throws JsonMappingException 
     * @throws JsonParseException 
     */
    private boolean validateChannel(XmlChannel channel, Client client) throws JsonParseException, JsonMappingException, IOException {
        if (channel.getName() == null || channel.getName().isEmpty()) {
            throw new IllegalArgumentException("Invalid channel name ");
        }
        for (XmlProperty xmlProperty : channel.getXmlProperties().getProperties()) {
            if (xmlProperty.getValue() == null || xmlProperty.getValue().isEmpty()) {
                throw new IllegalArgumentException("Invalid property value (missing or null or empty string) for '"+xmlProperty.getName()+"'");
            }
        }
        List<String> tagsNames = new ArrayList<String>();
        List<String> propertyNames = new ArrayList<String>();
        ObjectMapper mapper = new ObjectMapper();
        SearchResponse response = client.prepareSearch("properties").setTypes("property")
                .setQuery(new MatchAllQueryBuilder()).execute().actionGet();
        for (SearchHit hit : response.getHits()) {
            propertyNames.add(mapper.readValue(hit.getSourceAsString(), XmlProperty.class).getName());
        }
        response = client.prepareSearch("tags").setTypes("tag").setQuery(new MatchAllQueryBuilder()).execute()
                .actionGet();
        for (SearchHit hit : response.getHits()) {
            tagsNames.add(mapper.readValue(hit.getSourceAsString(), XmlTag.class).getName());
        }
        if (tagsNames.containsAll(ChannelUtil.getTagNames(channel))
                && propertyNames.containsAll(ChannelUtil.getPropertyNames(channel))) {
            return true;
        } else {
            throw new IllegalArgumentException("The Tags and/or Properties don't exist");
        }
    }
}
