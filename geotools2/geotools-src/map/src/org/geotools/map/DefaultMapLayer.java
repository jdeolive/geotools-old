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
/*
 * DefaultMapLayer.java
 *
 * Created on 23 novembre 2003, 10.53
 */
package org.geotools.map;


// import org.geotools.data.DataUtilities;
import org.geotools.data.FeatureEvent;
import org.geotools.data.FeatureListener;
import org.geotools.data.FeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.map.MapLayer;
import org.geotools.map.event.MapLayerEvent;
import org.geotools.styling.Style;
import java.io.IOException;
import org.geotools.data.DataUtilities;


/**
 * Default implementation of the MapLayer implementation
 *
 * @author wolf
 */
public class DefaultMapLayer implements MapLayer {
    /** Holds value of property FeatureSource. */
    protected FeatureSource featureSource;

    /** Holds value of property style. */
    protected Style style;

    /** Holds value of property title. */
    protected String title;

    /** Holds value of property visible. */
    protected boolean visible;

    /** Utility field used by event firing mechanism. */
    protected javax.swing.event.EventListenerList listenerList = null;

    /** Listener to forward feature source events as layer events */
    protected FeatureListener sourceListener = new FeatureListener() {
            public void changed(FeatureEvent featureEvent) {
                fireMapLayerListenerLayerChanged(new MapLayerEvent(DefaultMapLayer.this,
                        MapLayerEvent.DATA_CHANGED));
            }
        };

    /**
     * Creates a new instance of DefaultMapLayer
     *
     * @param featureSource the data source for this layer
     * @param style the style used to represent this layer
     * @param title the layer title
     *
     * @throws NullPointerException DOCUMENT ME!
     */
    public DefaultMapLayer(FeatureSource featureSource, Style style, String title) {
        if ((featureSource == null) || (style == null) || (title == null)) {
            throw new NullPointerException();
        }

        // enable data source listening
        featureSource.addFeatureListener(sourceListener);

        this.featureSource = featureSource;
        this.style = style;
        this.title = title;
        this.visible = true;
    }

    /**
     * Creates a new instance of DefaultMapLayer
     *
     * @param featureSource the data source for this layer
     * @param style the style used to represent this layer
     */
    public DefaultMapLayer(FeatureSource featureSource, Style style) {
        this(featureSource, style, "");
    }

    /**
     * Creates a new instance of DefaultMapLayer using a non-emtpy feature collection as a
     * parameter
     *
     * @param collection the source feature collection
     * @param style the style used to represent this layer
     * @param title DOCUMENT ME!
     */
    public DefaultMapLayer(FeatureCollection collection, Style style, String title) {
        this(DataUtilities.source(collection), style, title);
    }

    /**
     * Creates a new instance of DefaultMapLayer using a non-emtpy feature collection as a
     * parameter
     *
     * @param collection the source feature collection
     * @param style the style used to represent this layer
     */
    public DefaultMapLayer(FeatureCollection collection, Style style) {
        this(DataUtilities.source(collection), style, "");
    }

    /**
     * Getter for property featureSource.
     *
     * @return Value of property featureSource.
     */
    public FeatureSource getFeatureSource() {
        return this.featureSource;
    }

    /**
     * Getter for property style.
     *
     * @return Value of property style.
     */
    public Style getStyle() {
        return this.style;
    }

    /**
     * Setter for property style.
     *
     * @param style New value of property style.
     *
     * @throws NullPointerException DOCUMENT ME!
     */
    public void setStyle(Style style) {
        if (style == null) {
            throw new NullPointerException();
        }

        this.style = style;
        fireMapLayerListenerLayerChanged(new MapLayerEvent(this, MapLayerEvent.STYLE_CHANGED));
    }

    /**
     * Getter for property title.
     *
     * @return Value of property title.
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * Setter for property title.
     *
     * @param title New value of property title.
     *
     * @throws NullPointerException DOCUMENT ME!
     */
    public void setTitle(String title) {
        if (title == null) {
            throw new NullPointerException();
        }

        this.title = title;

        fireMapLayerListenerLayerChanged(new MapLayerEvent(this, MapLayerEvent.METADATA_CHANGED));
    }

    /**
     * Getter for property visible.
     *
     * @return Value of property visible.
     */
    public boolean isVisible() {
        return this.visible;
    }

    /**
     * Setter for property visible.
     *
     * @param visible New value of property visible.
     */
    public void setVisible(boolean visible) {
        if (this.visible == visible) {
            return;
        }

        // change visibility and fire events
        this.visible = visible;

        MapLayerEvent event = new MapLayerEvent(this, MapLayerEvent.VISIBILITY_CHANGED);
        if (visible) {
            fireMapLayerListenerLayerShown(event);
        } else {
            fireMapLayerListenerLayerHidden(event);
        }
    }

    // ------------------------------------------------------------------------
    // EVENT HANDLING CODE
    // ------------------------------------------------------------------------

    /**
     * Registers MapLayerListener to receive events.
     *
     * @param listener The listener to register.
     */
    public synchronized void addMapLayerListener(org.geotools.map.event.MapLayerListener listener) {
        if (listenerList == null) {
            listenerList = new javax.swing.event.EventListenerList();
        }

        listenerList.add(org.geotools.map.event.MapLayerListener.class, listener);
    }

    /**
     * Removes MapLayerListener from the list of listeners.
     *
     * @param listener The listener to remove.
     */
    public synchronized void removeMapLayerListener(
        org.geotools.map.event.MapLayerListener listener) {
        listenerList.remove(org.geotools.map.event.MapLayerListener.class, listener);
    }

    /**
     * Notifies all registered listeners about the event.
     *
     * @param event The event to be fired
     */
    private void fireMapLayerListenerLayerChanged(org.geotools.map.event.MapLayerEvent event) {
        if (listenerList == null) {
            return;
        }

        Object[] listeners = listenerList.getListenerList();

        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == org.geotools.map.event.MapLayerListener.class) {
                ((org.geotools.map.event.MapLayerListener) listeners[i + 1]).layerChanged(event);
            }
        }
    }

    /**
     * Notifies all registered listeners about the event.
     *
     * @param event The event to be fired
     */
    private void fireMapLayerListenerLayerShown(org.geotools.map.event.MapLayerEvent event) {
        if (listenerList == null) {
            return;
        }

        Object[] listeners = listenerList.getListenerList();

        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == org.geotools.map.event.MapLayerListener.class) {
                ((org.geotools.map.event.MapLayerListener) listeners[i + 1]).layerShown(event);
            }
        }
    }

    /**
     * Notifies all registered listeners about the event.
     *
     * @param event The event to be fired
     */
    private void fireMapLayerListenerLayerHidden(org.geotools.map.event.MapLayerEvent event) {
        if (listenerList == null) {
            return;
        }

        Object[] listeners = listenerList.getListenerList();

        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == org.geotools.map.event.MapLayerListener.class) {
                ((org.geotools.map.event.MapLayerListener) listeners[i + 1]).layerHidden(event);
            }
        }
    }
}
