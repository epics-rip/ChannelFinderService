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

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;

/**
 * Gets inserted into JAXB to configure JSON marshalling.
 *
 * @author Ralph Lange {@literal <ralph.lange@gmx.de>}
 */
@Provider
public class MyJAXBContextResolver implements ContextResolver<ObjectMapper> {

    final ObjectMapper defaultObjectMapper;
    
    @SuppressWarnings("rawtypes")
    private Class[] types = { XmlTag.class, XmlProperty.class, XmlChannel.class };

    public MyJAXBContextResolver() throws Exception {
        defaultObjectMapper = createDefaultMapper();
    }

    public ObjectMapper getContext(Class<?> objectType) {
        return Arrays.asList(types).contains(objectType) ? defaultObjectMapper:null;
    }
    
    private static ObjectMapper createDefaultMapper() {
        final ObjectMapper result = new ObjectMapper();
        result.enable(SerializationFeature.INDENT_OUTPUT);
        result.setAnnotationIntrospector(createJaxbJacksonAnnotationIntrospector());
        return result;
    }


    private static AnnotationIntrospector createJaxbJacksonAnnotationIntrospector() {
        final AnnotationIntrospector jaxbIntrospector = new JaxbAnnotationIntrospector(TypeFactory.defaultInstance());
        final AnnotationIntrospector jacksonIntrospector = new JacksonAnnotationIntrospector();
        return AnnotationIntrospector.pair(jacksonIntrospector, jaxbIntrospector);
    }
}