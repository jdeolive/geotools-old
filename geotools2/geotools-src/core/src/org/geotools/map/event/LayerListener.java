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

// J2SE dependencies
import java.util.EventListener;
import java.util.EventObject;

// Geotools dependencies
import org.geotools.map.Layer; // For Javadoc


/**
 * The listener that's notified when some {@linkplain Layer layer} property changes.
 *
 * @author Cameron Shorter
 * @author Martin Desruisseaux
 * @version $Id: LayerListener.java,v 1.1 2003/08/18 16:32:31 desruisseaux Exp $
 *
 * @see Layer
 * @see LayerEvent
 */
public interface LayerListener extends EventListener {
    /**
     * Invoked when some layer property changed, for example the layer title.
     * The implementation will typically involves a redraw.
     *
     * @param event The event.
     */
    void layerChanged(EventObject event);

    /**
     * nvoked when the component has been made visible.
     *
     * @param event The event.
     */
    void layerShown(EventObject event);

    /**
     * nvoked when the component has been made invisible.
     *
     * @param event The event.
     */
    void layerHidden(EventObject event);
}
