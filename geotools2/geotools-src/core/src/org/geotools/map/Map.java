/*
 * Map.java
 *
 * Created on March 27, 2002, 2:54 PM
 */

package org.geotools.map;

import org.geotools.styling.*;
import org.geotools.data.Extent;
import org.geotools.renderer.Renderer;
import org.geotools.feature.*;
import org.opengis.cs.CS_CoordinateSystem;
import com.vividsolutions.jts.geom.Envelope;

/**
 *
 * @author  jamesm
 */
public interface Map {
    
    public void setCoordinateSystem(CS_CoordinateSystem cs);
    public void addFeatureTable(FeatureCollection ft, Style style);
    public void render(Renderer renderer, Envelope envelope);
    
}

