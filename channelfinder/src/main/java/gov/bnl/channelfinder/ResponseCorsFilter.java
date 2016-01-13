package gov.bnl.channelfinder;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.MultivaluedMap;

/**
 *
 * @author berryman
 */
public class ResponseCorsFilter implements ContainerResponseFilter {
    
    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {

        MultivaluedMap<String, Object> headers = responseContext.getHeaders();

        headers.add("Access-Control-Allow-Origin", "*");
        headers.add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        headers.add("Access-Control-Allow-Credentials", "true");
        
        String reqHead = requestContext.getHeaderString("Access-Control-Request-Headers");
        
        if(null != reqHead && !reqHead.equals(null)){
            headers.add("Access-Control-Allow-Headers", reqHead);
        }       
    }
        
}