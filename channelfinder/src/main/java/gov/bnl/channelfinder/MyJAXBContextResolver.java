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


import java.util.Arrays;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBContext;

import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.api.json.JSONJAXBContext;

@Provider
public class MyJAXBContextResolver implements ContextResolver<JAXBContext> {

    private JAXBContext context;
    @SuppressWarnings("rawtypes")
    private Class[] types = { XmlTag.class, XmlProperty.class, XmlChannel.class };

    public MyJAXBContextResolver() throws Exception {
        this.context = new JSONJAXBContext(JSONConfiguration.natural().build(), types);
    }

    public JAXBContext getContext(Class<?> objectType) {
        return Arrays.asList(types).contains(objectType) ? context:null;
    }
}