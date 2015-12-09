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


import java.util.logging.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

/**
 * @author Kunal Shroff <shroffk@bnl.gov>
 *
 */
public class ElasticSearchClient implements ServletContextListener {

    private Logger log = Logger.getLogger(this.getClass().getName());

    private static Settings settings;
    
    private static TransportClient searchClient;
    private static TransportClient indexClient;

    public static TransportClient getSearchClient() {
        return searchClient;
    }
    
    public static TransportClient getIndexClient() {
        return indexClient;
    }

    /**
     * Returns a new {@link TransportClient} using the default settings
     * **IMPORTANT** it is the responsibility of the caller to close this client
     * @return
     */
    @SuppressWarnings("resource")
    public static TransportClient getNewClient() {
        String host = settings.get("network.host");
        int port = Integer.valueOf(settings.get("transport.tcp.port"));
        return new TransportClient().addTransportAddress(new InetSocketTransportAddress(host, port));
    }
    
    public static Settings getSettings(){
        return settings;
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        log.info("Initializing a new Transport clients.");
        searchClient = new TransportClient();
        indexClient = new TransportClient();
        settings = searchClient.settings();
        String host = settings.get("network.host");
        int port = Integer.valueOf(settings.get("transport.tcp.port"));
        searchClient.addTransportAddress(new InetSocketTransportAddress(host, port));
        indexClient.addTransportAddress(new InetSocketTransportAddress(host, port));
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        log.info("Closeing the default Transport clients.");
        searchClient.close();
        indexClient.close();
    }

}
