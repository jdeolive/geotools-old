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
package org.geotools.map;

/**
 * LayerModel stores DataSources associated with a geographic map.
 * Geotools uses a Model-View-Control (MVC) design to control maps.
 * The Tools classes process key and mouse actions, and the Renderers handle
 * displaying of the data.
 *
 * @version $Id: LayerModel.java,v 1.4 2002/08/09 12:54:38 camerons Exp $
 * @author Cameron Shorter
 * @task TODO: Add incrementOrder(layer) decrementOrderLayer
 * makeFirst(layer) makLast(Layer)
 */

import org.geotools.map.events.LayerListChangedEvent;
import org.geotools.map.events.LayerListChangedListener;
import org.geotools.map.Layer;

public interface LayerModel {
    
    /**
     * Register interest in receiving a LayerListChangedEvent.  A
     * LayerListChangedEvent is sent if a layer is added or removed, but not if
     * the data within a layer changes.
     * @param llce The object to notify when Layers have changed.
     */
    public void addLayerListChangedListener(
            LayerListChangedListener llce);

    /**
     * Remove interest in receiving an LayerListChangedEvent.
     * @param llcl The object to stop sending LayerListChangedEvents.
     */
    public void removeLayerListChangedListener(
            LayerListChangedListener llcl);

    /**
     * Add a new layer and trigger a LayerListChangedEvent.
     * @param layer Then new layer that has been added.
     */
    public void addLayer(
            Layer layer);

    /**
     * Remove a layer and trigger a LayerListChangedEvent.
     * @param layer Then new layer that has been removed.
     */
    public void removeLayer(
            Layer layer);
 
    /**
     * Add an array of new layers and trigger a LayerListChangedEvent.
     * @param layer The new layers that are to be added.
     */
    public void addLayers(
            Layer[] layer);
 
    /**
     * Remove an array of new layers and trigger a LayerListChangedEvent.
     * @param layer The layers that are to be removed.
     */
    public void removeLayers(
            Layer[] layer);

    /**
     * Return this model's list of layers.  If no layers are present, then
     * null is returned.
     * @return This model's list of layers.
     */
    public Layer[] getLayers();
    

}
