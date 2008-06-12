/*
 *    GeoTools - The Open Source Java GIS Tookit
 *    http://geotools.org
 * 
 *    (C) 2006-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.gui.swing.map.map2d.stream.event;

import java.util.EventObject;

import org.geotools.gui.swing.map.map2d.stream.SelectableMap2D;
import org.geotools.gui.swing.map.map2d.stream.SelectableMap2D.SELECTION_FILTER;
import org.geotools.gui.swing.map.map2d.stream.handler.SelectionHandler;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Selection Event generated by a SelectableMap2D
 * @author Johann Sorel
 */
public class SelectionEvent extends EventObject{

    private final Geometry oldgeo;
    private final Geometry newgeo;
    private final SELECTION_FILTER oldfilter;
    private final SELECTION_FILTER newfilter;
    private final SelectionHandler oldhandler;
    private final SelectionHandler newhandler;
    
    
    /**
     * create a Map2DSelectionEvent
     * @param map : Map2D source componant
     * @param oldgeo : old JTS Geometry  of the selection or null
     * @param newgeo : new JTS Geometry  of the selection or null
     * @param filter : SELECTION_FILTER
     * @param handler : SelectionHandler
     */
    public SelectionEvent(SelectableMap2D map, Geometry oldgeo,Geometry newgeo, SELECTION_FILTER filter, SelectionHandler handler){
        super(map);
        this.oldgeo = oldgeo;
        this.newgeo = newgeo;
        this.oldfilter = filter;
        this.newfilter = filter;
        this.oldhandler = handler;
        this.newhandler = handler;
    }
    
    /**
     * create a Map2DSelectionEvent
     * @param map : Map2D source componant
     * @param geo : JTS Geometry  of the selection or null
     * @param oldfilter : old SELECTION_FILTER
     * @param newfilter : new SELECTION_FILTER
     * @param handler : SelectionHandler
     */
    public SelectionEvent(SelectableMap2D map, Geometry geo, SELECTION_FILTER oldfilter, SELECTION_FILTER newfilter, SelectionHandler handler){
        super(map);
        this.oldgeo = geo;
        this.newgeo = geo;
        this.oldfilter = oldfilter;
        this.newfilter = newfilter;
        this.oldhandler = handler;
        this.newhandler = handler;
    }
    
    /**
     * create a Map2DSelectionEvent
     * @param map : Map2D source componant
     * @param geo : JTS Geometry  of the selection or null
     * @param filter : SELECTION_FILTER
     * @param oldhandler : old SelectionHandler
     * @param newhandler : new SelectionHandler
     */
    public SelectionEvent(SelectableMap2D map, Geometry geo, SELECTION_FILTER filter, SelectionHandler oldhandler,SelectionHandler newhandler){
        super(map);
        this.oldgeo = geo;
        this.newgeo = geo;
        this.oldfilter = filter;
        this.newfilter = filter;
        this.oldhandler = oldhandler;
        this.newhandler = newhandler;
    }

    /**
     * Geometry corresponding to the selection zone.
     * Geometry is in the mapcontext CRS.
     * @return JTS Geometry, or null if no selection.
     */
    public Geometry getGeometry() {
        return newgeo;
    }
    
    /**
     * Geometry corresponding to the old selection zone.
     * Geometry is in the mapcontext CRS.
     * @return JTS Geometry, or null if no selection.
     */
    public Geometry getPreviousGeometry() {
        return oldgeo;
    }
    
    /**
     * New Filter
     * @return SELECTION_FILTER
     */
    public SELECTION_FILTER getFilter() {
        return newfilter;
    }
    
    /**
     * Old Filter
     * @return SELECTION_FILTER
     */
    public SELECTION_FILTER getPreviousFilter() {
        return oldfilter;
    }
    
    /**
     * New SelectionHandler
     * @return SelectionHandler
     */
    public SelectionHandler getHandler() {
        return newhandler;
    }
    
    /**
     * Old SelectionHandler
     * @return SelectionHandler
     */
    public SelectionHandler getPreviousHandler() {
        return oldhandler;
    }
    
    
}
