/*
 * Map.java
 *
 * Created on March 27, 2002, 2:54 PM
 */

package org.geotools.map;

import org.geotools.renderer.Renderer;
import org.geotools.feature.FeatureCollection;
import com.vividsolutions.jts.geom.Envelope;
import org.geotools.styling.Style;
import org.opengis.cs.CS_CoordinateSystem;

/**
 * Holds a set of FeatureCollections together with styles and a single output
 * coordinate system.
 * @author  jamesm
 */
public interface Map {
    
    /**
     * Set the coordinate system to be used when outputing the features in
     * this map.
     * The features being added to it may well have a veriaty of different
     * coordinate systems.
     * @param cs The single coordinat system all features will be converted
     *           for output.
     */
    public void setCoordinateSystem(CS_CoordinateSystem cs);
    
    /**
     * Add a collection of features to this map together with a style
     * specification.  The features need not have the same coordiate system
     * as the one specifed for this map, though it is strongly recomended that
     * all features within the collection share a single coordinate system.
     * TODO: Confirm if single cs should be enforced.
     * TODO: rename this method addFeatureCollection.
     *
     * @param fc The collection of features to add.
     * @param style The style to apply to these features.
     */
    public void addFeatureTable(FeatureCollection fc, Style style);
    
    /**
     * Display or output the portion of the map that falls within a 
     * specified envelop using the provided renderer.
     * @param renderer The renderer which will produce the output
     * @param envelope The portion of the map to be rendered
     */
    public void render(Renderer renderer, Envelope envelope);
    
}

