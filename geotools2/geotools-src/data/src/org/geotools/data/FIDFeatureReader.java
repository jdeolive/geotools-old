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
import java.util.logging.Logger;


/**
 * Experimental FeatureReader that always takes the first column of the
 * attributeReader as the FeatureID.  I want to get this working with postgis,
 * but then will consider other options, for those who want featureIDs created
 * automatically.  Perhaps a constructor param or a method to say that you
 * would just like to have the FeatureReader increment one for each feature,
 * prepending the typeName.  I'm also don't really like the one argument
 * constructor defaulting to the xxx typename.  I feel that it should perhaps
 * take a typename.  If people deliberately set to null then we could use xxx
 * or something. ch
 * 
 * <p>
 * This now feels sorta hacky, I'm not sure that I like it, but I'm going to
 * commit as I need to go now and revisit it in a bit.  I think the idea of
 * passing in an FIDAttributeReader might be cleaner, and if none is provided
 * then do an auto-increment one.  This might then work as the
 * DefaultFeatureReader.
 * </p>
 *
 * @author Ian Schneider
 * @author Chris Holmes, TOPP
 * @version $Id: FIDFeatureReader.java,v 1.2 2003/11/04 00:28:49 cholmesny Exp $
 */
public class FIDFeatureReader implements FeatureReader {
    /** The logger for the data module. */
    private static final Logger LOGGER = Logger.getLogger("org.geotools.data");
    private final AttributeReader attributeReader;
    private final FeatureType schema;
    private final FIDReader fidReader;
    protected final Object[] attributes;

    /**
     * Creates a new instance of AbstractFeatureReader
     *
     * @param attributeReader AttributeReader
     * @param fidReader FIDReader used to ID Features
     * @param schema FeatureType to use, may be <code>null</code>
     *
     * @throws SchemaException if we could not determine the correct
     *         FeatureType
     */
    public FIDFeatureReader(AttributeReader attributeReader,
        FIDReader fidReader, FeatureType schema) throws SchemaException {
        this.attributeReader = attributeReader;
        this.fidReader = fidReader;

        if (schema == null) {
            schema = createSchema();
        }

        this.schema = schema;
        this.attributes = new Object[attributeReader.getAttributeCount()];
    }

    public FIDFeatureReader(AttributeReader attributeReader, FIDReader fidReader)
        throws SchemaException {
        this(attributeReader, fidReader, null);
    }

    public Feature next()
        throws IOException, IllegalAttributeException, NoSuchElementException {
        if (attributeReader.hasNext()) {
            attributeReader.next();

            return readFeature(attributeReader);
        } else {
            throw new NoSuchElementException(
                "There are no more Features to be read");
        }
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
        LOGGER.fine("reading feature ");

        //Seems like doing it here could be a bit expensive.
        //The other option from this is to have this constructed with two
        //attributeReaders, the FID one and real attributes one.  Could then
        //have default FIDAttributeReader.
        String fid = fidReader.next();

        for (int i = 0, ii = atts.getAttributeCount(); i < ii; i++) {
            attributes[i] = atts.read(i);
        }

        return schema.create(attributes, fid);
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
