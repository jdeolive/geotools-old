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

// J2SE dependencies
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Collections;
import javax.swing.event.EventListenerList;

// JTS dependencies
import com.vividsolutions.jts.geom.Envelope;

// Geotools dependencies
import org.geotools.map.event.LayerListEvent;
import org.geotools.map.event.LayerListListener;
import org.geotools.feature.CollectionListener;
import org.geotools.feature.CollectionEvent;


/**
 * Default implementation of {@link LayerList}.
 *
 * @author Cameron Shorter
 * @author Martin Desruisseaux
 * @version $Id: DefaultLayerList.java,v 1.4 2003/08/18 16:33:06 desruisseaux Exp $
 *
 * @task REVISIT: This class should probably implements java.util.List.
 *                Maybe we should extend directly ArrayList.
 */
public class DefaultLayerList implements LayerList, CollectionListener {
    /**
     * The layers stored by this <code>LayerList</code>.
     *
     * @task REVISIT: Why synchronisation on this list.
     */
    private final List layers = Collections.synchronizedList(new ArrayList());

    /**
     * The bounding box.
     *
     * @see #getBounds
     */
    private Envelope bounds;

    /**
     * Listeners to notify if the LayerList changes.
     * Will be constructed only when first needed.
     */
    private EventListenerList listenerList;

    /**
     * Creates a layer list without any layers.
     */
    public DefaultLayerList() {
    }

    /**
     * Creates a layer list initialised with one layer.
     *
     * @param layer The layer to add.
     */
    public DefaultLayerList(Layer layer) {
        addLayer(layer);
    }

    /**
     * Creates a layer list initialised with an array of layers.
     *
     * @param layers The new layers that are to be added.
     */
    public DefaultLayerList(Layer[] layers) {
        addLayers(layers);
    }

    /**
     * {@inheritDoc}
     */
    public void addLayer(final Layer layer) {
        layer.getFeatures().addListener(this);
        layers.add(layer);
        fireLayerListChanged();
    }

    /**
     * {@inheritDoc}
     */
    public void removeLayer(final Layer layer) {
        layer.getFeatures().removeListener(this);
        layers.remove(layer);
        fireLayerListChanged();
    }

    /**
     * {@inheritDoc}
     */
    public void addLayers(final Layer[] layers) {
        if (layers != null) {
            for (int i=0; i<layers.length; i++) {
                final Layer layer = layers[i];
                layer.getFeatures().addListener(this);
                this.layers.add(layer);
            }
            fireLayerListChanged();
        }
    }

    /**
     * {@inheritDoc}
     */
    public void removeLayers(final Layer[] layers) {
        if (layers != null) {
            for (int i=0; i<layers.length; i++) {
                final Layer layer = layers[i];
                layer.getFeatures().removeListener(this);
                this.layers.remove(layer);
            }
            fireLayerListChanged();
        }
    }

    /**
     * {@inheritDoc}
     */
    public Layer[] getLayers() {
        return (Layer[]) layers.toArray(new Layer[layers.size()]);
    }

    /**
     * {@inheritDoc}
     */
    public Envelope getBounds() {
        if (bounds == null) {
            for (final Iterator it=layers.iterator(); it.hasNext();) {
                final Layer layer = (Layer) it.next();
                final Envelope toAdd = layer.getFeatures().getBounds();
                if (toAdd == null) {
                    return null;
                }
                if (bounds == null) {
                    bounds = new Envelope(toAdd);
                } else {
                    bounds.expandToInclude(toAdd);
                }
            }
        }
        return bounds;
    }

    /**
     * {@inheritDoc}
     *
     * @deprecated Use {@link #getBounds} instead.
     */
    public Envelope getBbox() {
        return getBounds();
    }

    /**
     * Invoked automatically when a feature collection in this list changed.
     *
     * @param event The collection change event.
     */
    public void collectionChanged(final CollectionEvent event) {
        bounds = null;
    }

    /**
     * {@inheritDoc}
     */
    public void addLayerListListener(final LayerListListener listener) {
        if (listenerList == null) {
            listenerList = new EventListenerList();
            listenerList.add(LayerListListener.class, listener);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public void removeLayerListListener(final LayerListListener listener) {
        if (listenerList != null) {
            listenerList.remove(LayerListListener.class, listener);
            if (listenerList.getListenerCount() == 0) {
                listenerList = null;
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * @deprecated Use {@link #addLayerListListener} instead.
     */
    public void addLayerListChangedListener(final LayerListListener llce) {
        addLayerListListener(llce);
    }

    /**
     * {@inheritDoc}
     *
     * @deprecated Use {@link #removeLayerListListener} instead.
     */
    public void removeLayerListChangedListener(final LayerListListener llcl) {
        removeLayerListListener(llcl);
    }
    
    /**
     * Notify all listeners that have registered interest for notification on
     * {@linkplain LayerListEvent layer list change event}.
     */
    protected void fireLayerListChanged() {
        if (listenerList != null) {
            // Guaranteed to return a non-null array
            Object[] listeners = listenerList.getListenerList();

            // Process the listeners last to first, notifying
            // those that are interested in this event
            LayerListEvent event = null;
            for (int i=listeners.length; (i-=2)>= 0;) {
                if (listeners[i] == LayerListListener.class) {
                    if (event == null) {
                        event = new LayerListEvent(this);
                    }
                    ((LayerListListener) listeners[i+1]).layerListChanged(event);
                }
            }
        }
    }
}
