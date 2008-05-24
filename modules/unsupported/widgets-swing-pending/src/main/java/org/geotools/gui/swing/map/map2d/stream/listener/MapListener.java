/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2006, GeoTools Project Managment Committee (PMC)
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
 */

package org.geotools.gui.swing.map.map2d.stream.listener;

import java.util.EventListener;

import org.geotools.gui.swing.map.map2d.stream.event.MapEvent;

/**
 * Map2DListener used to listen to Map2D events
 * @author Johann Sorel
 */
public interface MapListener extends EventListener{

    /**
     * called when Rendering strategy change
     * @param mapEvent 
     */
    public void mapStrategyChanged(MapEvent mapEvent);
    
    /**
     * called when action state of the map changed
     * @param mapEvent 
     */
    public void mapActionStateChanged(MapEvent mapEvent);
    
}
