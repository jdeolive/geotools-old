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

package org.geotools.gui.swing.map.map2d.listener;

import java.util.EventListener;

import org.geotools.gui.swing.map.map2d.event.Map2DNavigationEvent;

/**
 * NavigableMap2DListener used to listen to Map2D Navigation events 
 * @author Johann Sorel
 */
public interface Map2DNavigationListener extends EventListener{
    
    /**
     * called when the navigation handler change
     * @param mce : Map2DNavigationEvent
     */
    public void navigationHandlerChanged(Map2DNavigationEvent mce);
    
    
}
