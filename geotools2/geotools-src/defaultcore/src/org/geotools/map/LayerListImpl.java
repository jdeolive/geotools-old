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
package org.geotools.map;

import com.vividsolutions.jts.geom.Envelope;
import org.geotools.map.events.LayerListListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EventObject;
import java.util.List;
//import java.util.logging.Logger;
import javax.swing.event.EventListenerList;
import org.geotools.feature.CollectionListener;


/**
 * LayerList stores DataSources associated with a geographic map. Geotools uses
 * a Model-View-Control (MVC) design to control maps. The Tools classes
 * process key and mouse actions, and the Renderers handle displaying of the
 * data.
 *
 * @author Cameron Shorter
 * @version $Id: LayerListImpl.java,v 1.6 2003/07/17 07:09:53 ianschneider Exp $
 */
public class LayerListImpl implements LayerList, CollectionListener {
    /** The class used for identifying for logging. */
    //private static final Logger LOGGER = Logger.getLogger("org.geotools.map");

    /** The layers stored by this LayerList */
    private List layers = Collections.synchronizedList(new ArrayList());

    /** Classes to notify if the LayerList changes */
    private EventListenerList listenerList = new EventListenerList();
    
    private Envelope bounds = null;

    /**
     * Create a Layer Model without any layers.
     */
    public LayerListImpl() {
    }

    /**
     * Create a Layer Model with one layer and trigger a LayerListChangedEvent.
     *
     * @param layer Then new layer that has been added.
     */
    public LayerListImpl(Layer layer) {
        addLayer(layer);
    }

    /**
     * Create a Layer Model with an array of layers and trigger a
     * LayerChangedEvent.
     *
     * @param layer The new layers that are to be added.
     */
    public LayerListImpl(Layer[] layer) {
        addLayers(layer);
    }

    /**
     * Register interest in receiving a LayerListChangedEvent.  A
     * LayerListChangedEvent is sent if a layer is added or removed, but not
     * if the data within a layer changes.
     *
     * @param llce The object to notify when Layers have changed.
     */
    public void addLayerListChangedListener(LayerListListener llce) {
        listenerList.add(LayerListListener.class, llce);
    }

    /**
     * Remove interest in receiving an LayerListChangedEvent.
     *
     * @param llcl The object to stop sending LayerListChangedEvents.
     */
    public void removeLayerListChangedListener(LayerListListener llcl) {
        listenerList.remove(LayerListListener.class, llcl);
    }

    /**
     * Notify all listeners that have registered interest for notification an
     * LayerListChangedEvent.
     */
    protected void fireLayerListChangedListener() {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();

        // Process the listeners last to first, notifying
        // those that are interested in this event
        EventObject llce = new EventObject(this);

        //(Layer[])layers.toArray(new Layer[0]));
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == LayerListListener.class) {
                ((LayerListListener) listeners[i + 1]).layerListChanged(llce);
            }
        }
    }

    /**
     * Add a new layer and trigger a LayerListChangedEvent.
     *
     * @param layer Then new layer that has been added.
     */
    public void addLayer(Layer layer) {
        layers.add(layer);
        fireLayerListChangedListener();
    }

    /**
     * Remove a layer and trigger a LayerListChangedEvent.
     *
     * @param layer Then new layer that has been removed.
     */
    public void removeLayer(Layer layer) {
        layers.remove(layer);
        fireLayerListChangedListener();
    }

    /**
     * Add an array of new layers and trigger a LayerListChangedEvent.
     *
     * @param layer The new layers that are to be added.
     */
    public void addLayers(Layer[] layer) {
        for (int i = 0; i < layer.length; i++) {
            layers.add(layer[i]);
        }

        fireLayerListChangedListener();
    }

    /**
     * Remove an array of new layers and trigger a LayerListChangedEvent.
     *
     * @param layer The layers that are to be removed.
     */
    public void removeLayers(Layer[] layer) {
        if (layer != null) {
            for (int i = 0; i < layer.length; i++) {
                layers.remove(layer[i]);
            }

            fireLayerListChangedListener();
        }
    }

    /**
     * Return this model's list of layers.  If no layers are present, then an
     * empty array is returned.
     *
     * @return This model's list of layers.
     */
    public Layer[] getLayers() {
        return (Layer[]) layers.toArray(new Layer[0]);
    }

    /**
     * Get the bounding box of all the layers in this LayerList. If all the
     * layers cannot determine the bounding box in the speed required for each
     * layer, then null is returned.
     *
     * @return The bounding box of the datasource or null if unknown and too
     *         expensive for the method to calculate.
     *
     * @task REVISIT: Consider changing return of getBbox to Filter once
     *       Filters can be unpacked.
     */
    public Envelope getBbox() {
      if (bounds == null) {
        bounds = new Envelope();
        Layer[] layerArray = (Layer[]) layers.toArray(new Layer[0]);

        for (int i = 0; i < layerArray.length; i++) {
          bounds.expandToInclude(layerArray[i].getFeatures().getBounds());
        }
      }

      return bounds;
    }

    
    public void collectionChanged(org.geotools.feature.CollectionEvent tce) {
      bounds = null;
    }
    
}
