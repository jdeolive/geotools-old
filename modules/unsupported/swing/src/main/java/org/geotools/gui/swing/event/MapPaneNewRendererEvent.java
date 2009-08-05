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
import org.geotools.renderer.GTRenderer;

/**
 * A MapPaneEvent class used to signal that the map pane is
 * using a new renderer
 *
 * @author Michael Bedward
 * @since 2.6
 */
public class MapPaneNewRendererEvent extends MapPaneEvent {

    private GTRenderer oldRenderer;
    private GTRenderer newRenderer;

    /**
     * Constructor
     * @param pane the map pane
     * @param oldRenderer the previous renderer (may be null)
     * @param newRenderer the new renderer (may be null)
     */
    public MapPaneNewRendererEvent(JMapPane pane, GTRenderer oldRenderer, GTRenderer newRenderer) {
        super(pane, MapPaneEvent.Type.NEW_RENDERER);
        this.oldRenderer = oldRenderer;
        this.newRenderer = newRenderer;
    }

    /**
     * Get a (live) reference to the map pane's previous renderer.
     * The return value may be null.
     */
    public GTRenderer getOldRenderer() {
        return oldRenderer;
    }

    /**
     * Get a (live) reference to the map pane's new renderer.
     * The return value may be null.
     */
    public GTRenderer getNewRenderer() {
        return newRenderer;
    }
}
