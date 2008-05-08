package org.geotools.tile;

import org.geotools.data.ServiceInfo;


/**
 * Description of tile service, including name,title,icon,bounds,tile size
 */
public interface TileServiceInfo extends ServiceInfo {
    /** Grab the stratagy used to produce tiles */
    public TileStratagy getTileStratagy();    
}
