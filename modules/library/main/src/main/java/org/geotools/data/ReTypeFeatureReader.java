/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2003-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.data;

import java.io.IOException;
import java.util.NoSuchElementException;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;


/**
 * Supports on the fly retyping of  FeatureReader<SimpleFeatureType, SimpleFeature> contents.
 * <p>
 * This may be used to have a DataStore work with your own representation of
 * Feature information.
 * </p>
 * <p>
 * Example Use:
 * </p>
 * <pre><code>
 *  FeatureReader<SimpleFeatureType, SimpleFeature> reader = dataStore.getFeatureReader( query, Transaction.AUTO_COMMIT );
 * reader = new ReTypeFeatureReader( reader, myFeatureType );
 * try {
 *   while( reader.hasNext() ){
 *     Feature f = reader.next();
 *     System.out.println( f );
 *   }
 * }
 * finally {
 *   reader.close(); // will close both
 * } 
 * </code></pre>
 * 
 * @author Jody Garnett, Refractions Research
 * @source $URL$
 */
public class ReTypeFeatureReader implements  FeatureReader<SimpleFeatureType, SimpleFeature> {
     FeatureReader<SimpleFeatureType, SimpleFeature> reader;
    SimpleFeatureType featureType;
    AttributeDescriptor[] types;
    boolean clone;

    /**
     * Constructs a FetureReader that will ReType streaming content.
     *
     * @param reader Origional FeatureReader
     * @param featureType Target FeatureType
     */
    public ReTypeFeatureReader(FeatureReader<SimpleFeatureType, SimpleFeature> reader, SimpleFeatureType featureType) {
        this(reader, featureType, true);
    }
    
    /**
     * Constructs a FetureReader that will ReType streaming content.
     *
     * @param reader Origional FeatureReader
     * @param featureType Target FeatureType
     * @since 2.3
     */
    public ReTypeFeatureReader(FeatureReader <SimpleFeatureType, SimpleFeature> reader, SimpleFeatureType featureType, boolean clone) {
        this.reader = reader;
        this.featureType = featureType;
        this.clone = clone;
        types = typeAttributes(featureType, reader.getFeatureType());
    }

    /**
     * Supplies mapping from origional to target FeatureType.
     * 
     * <p>
     * Will also ensure that origional can cover target
     * </p>
     *
     * @param target Desired FeatureType
     * @param origional Origional FeatureType
     *
     * @return Mapping from originoal to target FeatureType
     *
     * @throws IllegalArgumentException if unable to provide a mapping
     */
    protected AttributeDescriptor[] typeAttributes(SimpleFeatureType target,
        SimpleFeatureType origional) {
        if (target.equals(origional)) {
            throw new IllegalArgumentException(
                "FeatureReader allready produces contents with the correct schema");
        }

        if (target.getAttributeCount() > origional.getAttributeCount()) {
            throw new IllegalArgumentException(
                "Unable to retype  FeatureReader<SimpleFeatureType, SimpleFeature> (origional does not cover requested type)");
        }

        String xpath;
        AttributeDescriptor[] types = new AttributeDescriptor[target.getAttributeCount()];

        for (int i = 0; i < target.getAttributeCount(); i++) {
            AttributeDescriptor attrib = target.getDescriptor(i);
            xpath = attrib.getLocalName();
            types[i] = attrib;

            if (!attrib.equals(origional.getDescriptor(xpath))) {
                throw new IllegalArgumentException(
                    "Unable to retype  FeatureReader<SimpleFeatureType, SimpleFeature> (origional does not cover "
                    + xpath + ")");
            }
        }

        return types;
    }

    /**
     * @see org.geotools.data.FeatureReader#getFeatureType()
     */
    public SimpleFeatureType getFeatureType() {
        return featureType;
    }

    /**
     * @see org.geotools.data.FeatureReader#next()
     */
    public SimpleFeature next()
        throws IOException, IllegalAttributeException, NoSuchElementException {
        if (reader == null) {
            throw new IOException("FeatureReader has been closed");
        }

        SimpleFeature next = reader.next();
        String id = next.getID();

        Object[] attributes = new Object[types.length];
        String xpath;

        for (int i = 0; i < types.length; i++) {
            xpath = types[i].getLocalName();
            if(clone)
                attributes[i] = DataUtilities.duplicate(next.getAttribute(xpath));
            else
                attributes[i] = next.getAttribute(xpath);
        }

        return SimpleFeatureBuilder.build(featureType, attributes, id);
    }

    /**
     * @see org.geotools.data.FeatureReader#hasNext()
     */
    public boolean hasNext() throws IOException {
        return reader.hasNext();
    }

    /**
     * @see org.geotools.data.FeatureReader#close()
     */
    public void close() throws IOException {
        if (reader != null) {
            reader.close();
            reader = null;
            featureType = null;
            types = null;
        }
    }
}
