/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
 *
 */
package org.geotools.gui.widget;

import org.geotools.gui.tools.Tool;
import org.geotools.map.event.MapBoundsListener;
import org.geotools.map.event.MapLayerListListener;



/**
 * This class provides core functionality for drawing a map.  A redraw is
 * required if any of the parameters in the associated Context changes.
 *
 * @author Cameron Shorter
 * @version $Id: MapPane.java,v 1.9 2004/03/14 18:44:26 aaime Exp $
 *
 * @task TODO Should extend LayerListener as well.  Ie, if features inside a \
 *       layer change, then a redraw is required.
 * @deprecated
 */
public interface MapPane extends MapBoundsListener, MapLayerListListener {
    /**
     * Set the tool for this mapPane.  The tool handles all the mouse and key
     * actions on behalf of this mapPane.  Different tools can be assigned in
     * order to get the mapPane to behave differently.
     *
     * @param tool The tool to use for this mapPane.
     *
     * @throws IllegalArgumentException if tool is null.
     */
    void setTool(Tool tool) throws IllegalArgumentException;

    /**
     * Get the tool assigned to this mapPane.  If none is assigned, then null
     * is returned.
     *
     * @return The tool assigned to this mapPane.
     */
    Tool getTool();
}
