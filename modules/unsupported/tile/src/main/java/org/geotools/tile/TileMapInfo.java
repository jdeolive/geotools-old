package org.geotools.tile;

import java.net.URI;
import java.util.SortedSet;


/**
 * Captures all the information used to define a tile map.
 * 
 * @author Jody Garnett
 */
public interface TileMapInfo extends GeoResourceInfo {   
    /**
     * Describes the range of ZoomLevels supported.
     * 
     * @return SortedSet of ZoomLevel
     */
    SortedSet getZoomLevels();
    
    /** Identifier used to tag georesource */
    URI getIdentifier();
}
