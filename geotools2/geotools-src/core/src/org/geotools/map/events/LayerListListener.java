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
import java.util.EventObject;

/**
 * Methods to handle a change in the list of Layers.
 * @author Cameron Shorter
 * @version $Id: LayerListListener.java,v 1.1 2003/01/28 11:31:16 camerons Exp $
 */
public interface LayerListListener extends EventListener {

    /**
     * Process an LayerListChangedEvent, probably involves a redraw.
     * @param LayerListChangedEvent The new extent.
     */
    void layerListChanged(
            EventObject layerListChangedEvent);
}
