/*
 *    Geotools - OpenSource mapping toolkit
 *    (C) 2002, Centre for Computational Geography
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
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */
package org.geotools.map.events;

import java.util.EventListener;

/**
 * Methods to handle a change in visability of a layer.  After being notified
 * that LayerVisability has changed, the method should call the layer to
 * determine it's visability.
 * @author Cameron Shorter
 * @version $Id: LayerVisabilityListener.java,v 1.1 2002/08/09 12:55:02 camerons Exp $
 */
public interface LayerVisabilityListener extends EventListener {

    /**
     * Process an event notifying that the visability of a layer has changed.
     * After receiving the event, layer.getVisability() should be called to
     * determine the visability.
     * @param source The layer sending this event.
     */
    void LayerVisabilityChanged(Object source);
}
