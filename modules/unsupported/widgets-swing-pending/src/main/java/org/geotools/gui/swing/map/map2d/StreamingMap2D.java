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

package org.geotools.gui.swing.map.map2d;

import org.geotools.gui.swing.map.map2d.listener.Map2DListener;
import org.geotools.gui.swing.map.map2d.strategy.RenderingStrategy;

/**
 * Map2D interface, used for mapcontext viewing
 * 
 * @author Johann Sorel
 */
public interface StreamingMap2D extends Map2D{
       
    /**
     * Possible actions states available for a map
     */
    public static enum ACTION_STATE{
        NAVIGATE,
        SELECT,
        EDIT,
        NONE
    };
    
    
    /**
     * set the rendering strategy
     * @param strategy : throw nullpointexception if strategy is null
     */
    public void setRenderingStrategy(RenderingStrategy strategy);    
    /**
     * get the map2d rendering strategy
     * @return RenderingStrategy : should never return null;
     */
    public RenderingStrategy getRenderingStrategy();
        
    /**
     * add a Map2DListener
     * @param listener : Map2Dlistener to add
     */
    public void addMap2DListener(Map2DListener listener);        
    /**
     * remove a Map2DListener
     * @param listener : Map2DListener to remove
     */
    public void removeMap2DListener(Map2DListener listener);    
    /**
     * 
     * @return array of Map2DListener
     */
    public Map2DListener[] getMap2DListeners();
    
    //------------------Action State--------------------------------------------    
    /**
     * set the action state. Pan, ZoomIn, ZoomOut ...
     * @param state : MapConstants.ACTION_STATE
     */
    public void setActionState(ACTION_STATE state);    
    /**
     * get the actual action state
     * @return MapConstants.ACTION_STATE
     */
    public ACTION_STATE getActionState();
            
        
}
