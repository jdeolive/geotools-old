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

/**
 * Implemented by classes that wish to receive MapPaneEvents
 *
 * @author Michael Bedward
 * @since 2.6
 */
public interface MapPaneListener {

    /**
     * Called by the map pane when it has set a new map context
     */
    public void onNewContext(MapPaneNewContextEvent ev);

    /**
     * Called by the map pane when it has set a new renderer
     */
    public void onNewRenderer(MapPaneNewRendererEvent ev);

}
