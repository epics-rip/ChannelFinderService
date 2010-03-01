/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.bnl.channelfinder;

import com.sun.jersey.api.json.JSONJAXBContext;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.ContextResolver;
import javax.xml.bind.JAXBContext;

/**
 *
 * @author rlange
 */
@Provider
public class MyJAXBContextResolver implements ContextResolver<JAXBContext> {

    private JAXBContext context;
    private Class[] types = {XmlChannels.class};

    public MyJAXBContextResolver() throws Exception {
        Map props = new HashMap<String, Object>();
        props.put(JSONJAXBContext.JSON_ROOT_UNWRAPPING, Boolean.FALSE);
        this.context = new JSONJAXBContext(types, props);
    }

    public JAXBContext getContext(Class<?> objectType) {
        return (types[0].equals(objectType)) ? context : null;
    }
}
