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

package org.geotools.gui.swing.map.map2d.event;

import java.util.EventObject;

import org.geotools.gui.swing.map.map2d.Map2D;
import org.geotools.gui.swing.map.map2d.StreamingMap2D.ACTION_STATE;
import org.geotools.gui.swing.map.map2d.strategy.RenderingStrategy;

/**
 * @author Johann Sorel
 */
public class Map2DEvent extends EventObject{

    private final ACTION_STATE oldstate;
    private final ACTION_STATE newstate;
    private final RenderingStrategy oldstrategy;
    private final RenderingStrategy newstrategy;
            
    /**
     * create a Map2DEvent
     * @param map : Map2D source componant
     * @param oldaction : previous action state
     * @param newaction : new action state
     * @param strategy : RenderingStrategy
     */
    public Map2DEvent(Map2D map, ACTION_STATE oldaction, ACTION_STATE newaction, RenderingStrategy strategy){
        super(map);
        this.oldstate = oldaction;
        this.newstate = newaction;
        this.oldstrategy = strategy;
        this.newstrategy = strategy;
    }

    /**
     * create a Map2DEvent
     * @param map : Map2D source componant
     * @param action : action state
     * @param oldstrategy : old RenderingStrategy
     * @param newstrategy : new RenderingStrategy
     */
    public Map2DEvent(Map2D map, ACTION_STATE action, RenderingStrategy oldstrategy, RenderingStrategy newstrategy){
        super(map);
        this.oldstate = action;
        this.newstate = action;        
        this.oldstrategy = oldstrategy;
        this.newstrategy = newstrategy;
    }
    
    /**
     * get previous action state
     * @return ACTION_STATE
     */
    public ACTION_STATE getPreviousState() {
        return oldstate;
    }

    /**
     * get new action state
     * @return ACTION_STATE
     */
    public ACTION_STATE getState() {
        return newstate;
    }
    
    /**
     * get the new Strategy
     * @return RenderingStrategy
     */
    public RenderingStrategy getStrategy() {
        return newstrategy;
    }
    
    
    /**
     * get the old strategy
     * @return RenderingStrategy
     */
    public RenderingStrategy getPreviousStrategy() {
        return oldstrategy;
    }
    
    
    
    
    
    
}
