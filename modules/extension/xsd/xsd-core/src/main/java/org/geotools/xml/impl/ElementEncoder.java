/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.xml.impl;

import org.eclipse.xsd.XSDAttributeDeclaration;
import org.eclipse.xsd.XSDElementDeclaration;
import org.picocontainer.MutablePicoContainer;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import java.util.logging.Logger;


/**
 * Utility class to be used by bindings to encode an element or an attribute.
 *
 *
 * @author Justin Deoliveira, The Open Planning Project
 * TODO: rename this class, it is not just for element.s
 *
 */
public class ElementEncoder {
    /**
     * The walker used to traverse bindings
     */
    private BindingWalker bindingWalker;

    /**
     * The binding context
     */
    private MutablePicoContainer context;

    /**
     * Logger
     */
    private Logger logger;

    public ElementEncoder(BindingWalker bindingWalker, MutablePicoContainer context) {
        this.bindingWalker = bindingWalker;
        this.context = context;
    }

    /**
     * Sets the logger for the encoder to use.
     * @param logger
     */
    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    /**
     * Encodes a value corresponding to an element in a schema.
     *
     * @param value The value to encode.
     * @param element The declaration of the element corresponding to the value.
     * @param document The document used to create the encoded element.
     *
     * @return The encoded value as an element.
     */
    public Element encode(Object value, XSDElementDeclaration element, Document document) {
        ElementEncodeExecutor executor = new ElementEncodeExecutor(value, element, document, logger);
        bindingWalker.walk(element, executor, context);

        return executor.getEncodedElement();
    }

    public Attr encode(Object value, XSDAttributeDeclaration attribute, Document document) {
        AttributeEncodeExecutor executor = new AttributeEncodeExecutor(value, attribute, document,
                logger);

        bindingWalker.walk(attribute, executor, context);

        return executor.getEncodedAttribute();
    }

    public void setContext(MutablePicoContainer context) {
        this.context = context;
    }
}
