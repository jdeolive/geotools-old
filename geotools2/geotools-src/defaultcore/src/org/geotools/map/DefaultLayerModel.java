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
 * LayerModel stores FeatureCollections associated with a geographic map.
 * Geotools uses a Model-View-Control (MVC) design to control maps.
 * The Tools classes process key and mouse actions, and the Renderers handle
 * displaying of the data.
 *
 * @version $Id: DefaultLayerModel.java,v 1.1 2002/07/14 11:55:54 camerons Exp $
 * @author Cameron Shorter
 * 
 */

import java.util.Vector;
import javax.swing.event.EventListenerList;
import org.geotools.map.events.LayerListChangedEvent;
import org.geotools.map.events.LayerListChangedListener;
import org.geotools.feature.FeatureCollection;

public class DefaultLayerModel implements LayerModel {
    
    private Vector layers;
    private EventListenerList listenerList = new EventListenerList();
   
    /**
     * Create a Layer Model without any layers.
     */
    public DefaultLayerModel()
    {
    }

    /**
     * Create a Layer Model with one layer and trigger a LayerListChangedEvent.
     * @param layer Then new layer that has been added.
     */
    public DefaultLayerModel(
            FeatureCollection layer)
    {
        addLayer(layer);
    }

    /**
     * Create a Layer Model with an array of layers and trigger a
     * LayerChangedEvent.
     * @param layer The new layers that are to be added.
     */
    public DefaultLayerModel(
            FeatureCollection[] layer)
    {
        addLayers(layer);
    }

    /**
     * Register interest in receiving a LayerListChangedEvent.  A
     * LayerListChangedEvent is sent if a layer is added or removed, but not if
     * the data within a layer changes.
     * @param llce The object to notify when Layers have changed.
     */
    public void addLayerListChangedListener(
            LayerListChangedListener llce){
        listenerList.add(LayerListChangedListener.class, llce);
    }

    /**
     * Remove interest in receiving an LayerListChangedEvent.
     * @param llcl The object to stop sending LayerListChangedEvents.
     */
    public void removeLayerListChangedListener(
            LayerListChangedListener llcl) {
        listenerList.remove(LayerListChangedListener.class, llcl);
    }

    /**
     * Notify all listeners that have registered interest for
     * notification an LayerListChangedEvent.
     */
    protected void fireLayerListChangedListener() {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        LayerListChangedEvent llce = new LayerListChangedEvent(
                this,
                (FeatureCollection[])this.layers.toArray());
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == LayerListChangedListener.class) {
                ((LayerListChangedListener)
                    listeners[i + 1]).LayerListChanged(llce);
            }
        }
    }

    /**
     * Add a new layer and trigger a LayerListChangedEvent.
     * @param layer Then new layer that has been added.
     */
    public void addLayer(
            FeatureCollection layer)
    {
        this.layers.add(layer);
        fireLayerListChangedListener();
    }
 
    /**
     * Remove a layer and trigger a LayerListChangedEvent.
     * @param layer Then new layer that has been removed.
     */
    public void removeLayer(
            FeatureCollection layer)
    {
        this.layers.remove(layer);
        fireLayerListChangedListener();
    }
 
    /**
     * Add an array of new layers and trigger a LayerListChangedEvent.
     * @param layer The new layers that are to be added.
     */
    public void addLayers(
            FeatureCollection[] layer)
    {
        for (int i = 0; i < layer.length; i++) {
            this.layers.add(layer[i]);
        }
        fireLayerListChangedListener();
    }
 
    /**
     * Remove an array of new layers and trigger a LayerListChangedEvent.
     * @param layer The layers that are to be removed.
     */
    public void removeLayers(
            FeatureCollection[] layer)
    {
        for (int i = 0; i < layer.length; i++) {
            this.layers.remove(layer[i]);
        }
        fireLayerListChangedListener();
    }
}
