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
package org.geotools.map.events;

import java.util.EventListener;
import java.util.EventObject;


/**
 * Methods to handle a change in the list of Layers.
 *
 * @author Cameron Shorter
 * @version $Id: LayerListListener.java,v 1.2 2003/08/07 22:44:51 cholmesny Exp $
 */
public interface LayerListListener extends EventListener {
    /**
     * Process an LayerListChangedEvent, probably involves a redraw.
     *
     * @param layerListChangedEvent The new extent.
     */
    void layerListChanged(EventObject layerListChangedEvent);
}
