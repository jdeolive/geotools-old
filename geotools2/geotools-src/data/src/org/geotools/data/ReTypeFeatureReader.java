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

import org.geotools.feature.AttributeType;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.IllegalAttributeException;
import java.io.IOException;
import java.util.NoSuchElementException;


/**
 * Supports on the fly retyping of FeatureReader contents.
 * <p>
 * This may be used to have a DataStore work with your own representation of
 * Feature information.
 * </p>
 * <p>
 * Example Use:
 * </p>
 * <pre><code>
 * FeatureReader reader = dataStore.getFeatureReader( query, Transaction.AUTO_COMMIT );
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
 */
public class ReTypeFeatureReader implements FeatureReader {
    FeatureReader reader;
    FeatureType featureType;
    AttributeType[] types;

    /**
     * Constructs a FetureReader that will ReType streaming content.
     *
     * @param reader Origional FeatureReader
     * @param featureType Target FeatureType
     */
    public ReTypeFeatureReader(FeatureReader reader, FeatureType featureType) {
        this.reader = reader;
        this.featureType = featureType;
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
    protected AttributeType[] typeAttributes(FeatureType target,
        FeatureType origional) {
        if (target.equals(origional)) {
            throw new IllegalArgumentException(
                "FeatureReader allready produces contents with the correct schema");
        }

        if (target.getAttributeCount() > origional.getAttributeCount()) {
            throw new IllegalArgumentException(
                "Unable to retype FeatureReader (origional does not cover requested type)");
        }

        String xpath;
        AttributeType[] types = new AttributeType[target.getAttributeCount()];

        for (int i = 0; i < target.getAttributeCount(); i++) {
            AttributeType attrib = target.getAttributeType(i);
            xpath = attrib.getName();
            types[i] = attrib;

            if (!attrib.equals(origional.getAttributeType(xpath))) {
                throw new IllegalArgumentException(
                    "Unable to retype FeatureReader (origional does not cover "
                    + xpath + ")");
            }
        }

        return types;
    }

    /**
     * @see org.geotools.data.FeatureReader#getFeatureType()
     */
    public FeatureType getFeatureType() {
        return featureType;
    }

    /**
     * @see org.geotools.data.FeatureReader#next()
     */
    public Feature next()
        throws IOException, IllegalAttributeException, NoSuchElementException {
        if (reader == null) {
            throw new IOException("FeatureReader has been closed");
        }

        Feature next = reader.next();
        String id = next.getID();

        Object[] attributes = new Object[types.length];
        String xpath;

        for (int i = 0; i < types.length; i++) {
            xpath = types[i].getName();
            attributes[i] = types[i].duplicate(next.getAttribute(xpath));
        }

        return featureType.create(attributes, id);
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
