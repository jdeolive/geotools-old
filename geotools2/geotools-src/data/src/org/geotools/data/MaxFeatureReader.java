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

import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.IllegalAttributeException;
import java.io.IOException;
import java.util.NoSuchElementException;


/**
 * Basic support for a FeatureReader that limits itself to the number of
 * features passed in.
 *
 * @author Chris Holmes
 * @version $Id: MaxFeatureReader.java,v 1.1 2003/11/11 02:33:05 cholmesny Exp $
 */
public class MaxFeatureReader implements FeatureReader {
    protected final FeatureReader featureReader;
    protected final int maxFeatures;
    protected int counter = 0;

    /**
     * Creates a new instance of MaxFeatureReader
     *
     * @param featureReader FeatureReader being maxed
     * @param maxFeatures DOCUMENT ME!
     */
    public MaxFeatureReader(FeatureReader featureReader, int maxFeatures) {
        this.featureReader = featureReader;
        this.maxFeatures = maxFeatures;
    }

    public Feature next()
        throws IOException, IllegalAttributeException, NoSuchElementException {
        if (hasNext()) {
            counter++;

            return featureReader.next();
        } else {
            throw new NoSuchElementException("No such Feature exists");
        }
    }

    public void close() throws IOException {
        featureReader.close();
    }

    public FeatureType getFeatureType() {
        return featureReader.getFeatureType();
    }

    /**
     * <p></p>
     *
     * @return <code>true</code> if the featureReader has not passed the max
     *         and still has more features.
     *
     * @throws IOException If the reader we are filtering encounters a problem
     */
    public boolean hasNext() throws IOException {
        return (featureReader.hasNext() && (counter < maxFeatures));
    }
}
