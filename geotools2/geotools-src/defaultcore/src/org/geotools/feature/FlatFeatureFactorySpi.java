/*
 * FlatFeatureFactorySpi.java
 *
 * Created on May 20, 2003, 11:20 AM
 */

package org.geotools.feature;

/**
 *
 * @author  jamesm
 */
public class FlatFeatureFactorySpi implements org.geotools.feature.FeatureFactorySpi {
    
    /** Creates a new instance of FlatFeatureFactorySpi */
    public FlatFeatureFactorySpi() {
    }
    
    public boolean canCreate(FeatureType type) {
        return type instanceof FeatureTypeFlat;
    }
    
    public FeatureFactory getFactory(FeatureType type) {
        return new FlatFeatureFactory(type);
    }
    
}
