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

import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.api.json.JSONJAXBContext;
import java.util.Arrays;
import java.util.List;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.ContextResolver;
import javax.xml.bind.JAXBContext;

/**
 * Gets inserted into JAXB to configure JSON marshalling.
 *
 * @author Ralph Lange <Ralph.Lange@helmholtz-berlin.de>
 */
@Provider
public class MyJAXBContextResolver implements ContextResolver<JAXBContext> {

    private JAXBContext context;
    private List<Class<?>> types = Arrays.asList(XmlChannels.class,
            XmlProperties.class, XmlTags.class);

    public MyJAXBContextResolver() throws Exception {
        this.context = new JSONJAXBContext(
                JSONConfiguration.mapped()
                .rootUnwrapping(false)
                .build(),
                types.toArray(new Class[types.size()]));
    }

    public JAXBContext getContext(Class<?> objectType) {
        return (types.contains(objectType)) ? context : null;
    }
}
