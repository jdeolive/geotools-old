/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2003-2008, Open Source Geospatial Foundation (OSGeo)
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

package org.geotools.gui.swing.event;

import org.geotools.gui.swing.JMapPane;
import org.geotools.map.MapContext;

/**
 * A MapPaneEvent class used to signal that the map pane is
 * using a new map context
 *
 * @author Michael Bedward
 * @since 2.6
 */
public class MapPaneNewContextEvent extends MapPaneEvent {
    private MapContext oldContext;
    private MapContext newContext;


    /**
     * Constructor
     * @param pane the map pane
     * @param oldContext the previous map context (may be null)
     * @param newContext the new map context (may be null)
     */
    public MapPaneNewContextEvent(JMapPane pane, MapContext oldContext, MapContext newContext) {
        super(pane, MapPaneEvent.Type.NEW_CONTEXT);

        this.oldContext = oldContext;
        this.newContext = newContext;
    }

    /**
     * Get a (live) reference to the map pane's previous map context.
     * The return value may be null.
     */
    public MapContext getOldContext() {
        return oldContext;
    }

    /**
     * Get a (live) reference to the map pane's new map context.
     * The return value may be null.
     */
    public MapContext getNewContext() {
        return newContext;
    }
    
}
