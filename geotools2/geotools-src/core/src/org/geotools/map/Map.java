/*
 * Map.java
 *
 * Created on March 27, 2002, 2:54 PM
 */

package org.geotools.map;

import org.geotools.styling.*;
import org.geotools.featuretable.*;
import org.opengis.cs.CS_CoordinateSystem;

/**
 *
 * @author  jamesm
 */
public interface Map {
    
    public void setCoordinateSystem(CS_CoordinateSystem cs);
    public void addFeatureTable(FeatureTable ft,Style style);
    
}

