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
package org.geotools.map.event;


// Geotools dependencies
import org.geotools.map.Layer; // For Javadoc
import java.beans.PropertyChangeListener;

// J2SE dependencies
import java.util.EventListener;
import java.util.EventObject;


/**
 * The listener that's notified when some {@linkPlain MapLayer layer} property changes.
 *
 * @author Cameron Shorter
 * @author Martin Desruisseaux
 * @version $Id: MapLayerListener.java,v 1.1 2003/12/04 23:20:33 aaime Exp $
 *
 * @see Layer
 * @see LayerEvent
 */
public interface MapLayerListener extends EventListener {
    /**
     * Invoked when some property of this layer has changed. May be data,  style, title,
     * visibility.
     *
     * @param event encapsulating the event information
     */
    void layerChanged(MapLayerEvent event);

    /**
     * Invoked when the component has been made visible.
     *
     * @param event encapsulating the event information
     */
    void layerShown(MapLayerEvent event);

    /**
     * nvoked when the component has been made invisible.
     *
     * @param event encapsulating the event information
     */
    void layerHidden(MapLayerEvent event);
}
