/**
 * 
 */
package gov.bnl.channelfinder;

/*
 * #%L
 * ChannelFinder Directory Service
 * %%
 * Copyright (C) 2010 - 2015 Helmholtz-Zentrum Berlin für Materialien und Energie GmbH
 * %%
 * Copyright (C) 2010 - 2012 Brookhaven National Laboratory
 * All rights reserved. Use is subject to license terms.
 * #L%
 */


import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.net.InetSocketAddress;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

/**
 * @author Kunal Shroff {@literal <shroffk@bnl.gov>}
 *
 */
public class ElasticSearchClient implements ServletContextListener {

    private static Logger log = Logger.getLogger(ElasticSearchClient.class.getCanonicalName());

    private static Settings settings;
    
    private static TransportClient searchClient;
    private static TransportClient indexClient;

    public static TransportClient  getSearchClient() {
        return searchClient;
    }
    
    public static TransportClient getIndexClient() {
        return indexClient;
    }

    /**
     * Returns a new {@link TransportClient} using the default settings
     * **IMPORTANT** it is the responsibility of the caller to close this client
     * @return es transport client
     */
    @SuppressWarnings("resource")
    public static TransportClient getNewClient() {
        String host = settings.get("network.host");
        int port = Integer.valueOf(settings.get("transport.tcp.port"));
        try {
            return new PreBuiltTransportClient(Settings.EMPTY).addTransportAddress(new InetSocketTransportAddress(new InetSocketAddress(host,port)));
        } catch (ElasticsearchException e) {
            log.severe(e.getDetailedMessage());
            return null;
        }
    }
    
    public static Settings getSettings(){
        return settings;
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            log.info("Initializing a new Transport clients.");
            String yaml  = "elasticsearch.yml";
            settings = Settings.builder().loadFromStream(yaml,getClass().getClassLoader().getResourceAsStream(yaml)).build();
            String host = settings.get("network.host");
            int port = Integer.valueOf(settings.get("transport.tcp.port"));
            
            searchClient = new PreBuiltTransportClient(settings);
            indexClient = new PreBuiltTransportClient(settings);
            searchClient.addTransportAddress(new InetSocketTransportAddress(new InetSocketAddress(host,port)));
            indexClient.addTransportAddress(new InetSocketTransportAddress(new InetSocketAddress(host,port)));
        } catch (IOException e) {
            log.severe(e.getMessage());
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        log.info("Closeing the default Transport clients.");
        searchClient.close();
        indexClient.close();
    }

}
