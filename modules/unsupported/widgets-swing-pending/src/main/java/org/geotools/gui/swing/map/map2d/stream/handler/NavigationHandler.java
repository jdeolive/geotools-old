/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2003-2006, GeoTools Project Managment Committee (PMC)
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

package org.geotools.gui.swing.map.map2d.stream.handler;

import javax.swing.ImageIcon;

import org.geotools.gui.swing.map.map2d.stream.NavigableMap2D;

/**
 *
 * @author johann sorel
 */
public interface NavigationHandler {

        
    /**
     * 
     * @param map2d
     * @param mapComponent
     */
    void install(NavigableMap2D map2d);
    
    /**
     * 
     */
    void uninstall();
    
    /**
     * 
     * @return
     */
    boolean isInstalled();
    
    String getTitle();
    
    ImageIcon getIcon();
    
}
