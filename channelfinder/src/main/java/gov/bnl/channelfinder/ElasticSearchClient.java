/**
 * 
 */
package gov.bnl.channelfinder;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

/**
 * @author Kunal Shroff <shroffk@bnl.gov>
 *
 */
public class ElasticSearchClient implements ServletContextListener {

    private static Client client;

    @SuppressWarnings("resource")
    public static Client getClient() {
        return client;
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("initialized");
        InetSocketTransportAddress address = new InetSocketTransportAddress("130.199.219.147", 9300);
        client = new TransportClient().addTransportAddress(address);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("closed");
        client.close();
    }

}
