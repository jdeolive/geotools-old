/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
 *
 */
package org.geotools.data;

import org.geotools.feature.*;
import java.io.IOException;
import java.util.NoSuchElementException;


/**
 * Basic support for reading Features from an AttributeReader.
 *
 * @author Ian Schneider
 * @version $Id: DefaultFeatureReader.java,v 1.2 2003/11/04 00:28:49 cholmesny Exp $
 */
public class DefaultFeatureReader implements FeatureReader {
    private final AttributeReader attributeReader;
    private final FeatureType schema;
    protected final Object[] attributes;

    /**
     * Creates a new instance of AbstractFeatureReader
     *
     * @param attributeReader AttributeReader for contents
     * @param schema FeatureType to use, <code>null</code> if not provided
     *
     * @throws SchemaException If Schema could not be obtained
     */
    public DefaultFeatureReader(AttributeReader attributeReader,
        FeatureType schema) throws SchemaException {
        this.attributeReader = attributeReader;

        if (schema == null) {
            schema = createSchema();
        }

        this.schema = schema;
        this.attributes = new Object[attributeReader.getAttributeCount()];
    }

    public DefaultFeatureReader(AttributeReader attributeReader)
        throws SchemaException {
        this(attributeReader, null);
    }

    public Feature next()
        throws IOException, IllegalAttributeException, NoSuchElementException {
        Feature f = null;

        if (attributeReader.hasNext()) {
            attributeReader.next();
            f = readFeature(attributeReader);
        }

        return f;
    }

    protected FeatureType createSchema() throws SchemaException {
        FeatureTypeFactory factory = FeatureTypeFactory.newInstance("xxx");

        for (int i = 0, ii = attributeReader.getAttributeCount(); i < ii;
                i++) {
            factory.addType(attributeReader.getAttributeType(i));
        }

        return factory.getFeatureType();
    }

    protected Feature readFeature(AttributeReader atts)
        throws IllegalAttributeException, IOException {
        for (int i = 0, ii = atts.getAttributeCount(); i < ii; i++) {
            attributes[i] = atts.read(i);
        }

        return schema.create(attributes);
    }

    public void close() throws IOException {
        attributeReader.close();
    }

    public FeatureType getFeatureType() {
        return schema;
    }

    public boolean hasNext() throws IOException {
        return attributeReader.hasNext();
    }
}
