/*
 * Copyright (c) 2010 Brookhaven National Laboratory
 * Copyright (c) 2010 Helmholtz-Zentrum Berlin für Materialien und Energie GmbH
 * Subject to license terms and conditions.
 */

package gov.bnl.channelfinder;

import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.api.json.JSONJAXBContext;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.ContextResolver;
import javax.xml.bind.JAXBContext;

/**
 *
 * @author Ralph Lange <Ralph.Lange@bessy.de>
 */
@Provider
public class MyJAXBContextResolver implements ContextResolver<JAXBContext> {

    private JAXBContext context;
    private Class[] types = {XmlChannels.class};

    public MyJAXBContextResolver() throws Exception {
        this.context = new JSONJAXBContext(
                JSONConfiguration.mapped()
                .rootUnwrapping(false)
                .build(),
                types);
    }

    public JAXBContext getContext(Class<?> objectType) {
        return (types[0].equals(objectType)) ? context : null;
    }
}
