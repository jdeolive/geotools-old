/*
 * MockFeatureCollections.java
 *
 * Created on August 12, 2003, 7:27 PM
 */

package org.geotools.feature;

/**
 *
 * @author  jamesm
 */
public class MockFeatureCollections extends org.geotools.feature.FeatureCollections {
    
    /** Creates a new instance of MockFeatureCollections */
    public MockFeatureCollections() {
    }
    
    protected FeatureCollection createCollection() {
        return new MockFeatureCollection();
    }
    
}
