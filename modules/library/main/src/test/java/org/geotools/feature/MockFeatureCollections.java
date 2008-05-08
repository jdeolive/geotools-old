/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2003-2006, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *    
 *    Created on August 12, 2003, 7:27 PM
 */
package org.geotools.feature;

import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

/**
 *
 * @author  jamesm
 * @source $URL$
 */
public class MockFeatureCollections extends org.geotools.feature.FeatureCollections {
    
    /** Creates a new instance of MockFeatureCollections */
    public MockFeatureCollections() {
    }
    protected FeatureCollection<SimpleFeatureType, SimpleFeature> createCollection(String id) {
    	return new MockFeatureCollection();
    }
    protected FeatureCollection<SimpleFeatureType, SimpleFeature> createCollection() {
        return new MockFeatureCollection();
    }
    
}
