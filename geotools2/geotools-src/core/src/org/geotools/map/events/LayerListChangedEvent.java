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

import java.util.EventObject;
import org.geotools.map.Layer;

 /**
 * Event data passed when a layer is added or removed from LayerModel.
 *
 * @version $Id: LayerListChangedEvent.java,v 1.2 2002/08/09 12:54:38 camerons Exp $
 * @author Cameron Shorter
 */
public class LayerListChangedEvent extends EventObject {

    private Layer[] layers;

    /**
     * @param layers The new list of layers.
     */
    public LayerListChangedEvent(
            final Object source,
            final Layer[] layers) {
        super(source);
        this.layers = layers;
    }

    /** Get the new list of layers.
     * @return The new list of layers.
     */
    public Layer[] getLayerList() {
        return this.layers;
    }
}
