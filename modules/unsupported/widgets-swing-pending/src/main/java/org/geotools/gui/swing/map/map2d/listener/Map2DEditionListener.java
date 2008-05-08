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

import org.geotools.gui.swing.map.map2d.event.Map2DEditionEvent;

/**
 * EditableMap2DListener used to listen to Map2D edition events
 * @author Johann Sorel
 */
public interface Map2DEditionListener extends EventListener{
    
    /**
     * called when the edited layer change
     * @param event : Map2DEditionEvent
     */
    public void editedLayerChanged(Map2DEditionEvent event);
    
    /**
     * called when edition handler change
     * @param event : Map2DEditionEvent
     */
    public void editionHandlerChanged(Map2DEditionEvent event);
    
}
