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

import javax.swing.event.EventListenerList;
import org.geotools.map.events.LayerChangedEvent;
import org.geotools.map.events.LayerListener;
import org.geotools.feature.FeatureCollection;
import org.geotools.styling.Style;


/**
 * Layer is an aggregation of both a FeatureCollection and Style.
 *
 * @author Cameron Shorter
 * @version $Id: LayerImpl.java,v 1.9 2003/08/05 20:04:39 jmacgill Exp $
 *
 * @task REVISIT: This class maybe should contain CoordinateSystem, which could
 *       either be set externally, or derived from one of its features.
 */
public class LayerImpl implements Layer {
    static final private int VISIBILITY = 0;
    static final private int TITLE = 0;
    /** Specify the DataSource which provides the features for this layer. */
    private FeatureCollection features;
    /** Specify the style for this layer. */
    private Style style;
    /**
     * Specify whether this layer is visable or not.  Defaults to TRUE on
     * initialisation.
     */
    private boolean visability = true;
    /** The title of this layer for use in Legend and similar. */
    private String title;
    /** Classes to notify if the LayerList changes */
    private EventListenerList listenerList = new EventListenerList();
    /**
     * Creates a Layer.
     *
     * @param dataSource The dataSource to query in order to get features for
     *        this layer.
     * @param style The style to use when rendering features associated with
     *        this layer.
     *
     * @throws IllegalArgumentException if an argument is <code>null</code>.
     */
    protected LayerImpl(
        FeatureCollection features,
        Style style
    ) throws IllegalArgumentException {
        if ((style == null) || (features == null)) {
            throw new IllegalArgumentException();
        } else {
            this.features = features;
            this.style = style;
            visability = true;
        }
    }



    /**
     * Get the style for this layer.
     *
     * @return The style (SLD).
     */
    public Style getStyle() {
        return style;
    }

    /**
     * Specify whether this layer is visable on a MapPane or whether the layer
     * is hidden.  Visibility defaults to TRUE on initialisation.
     *
     * @param visability Set the layer visable if TRUE.
     */
    public void setVisability(boolean visability) {
        this.visability = visability;
        fireLayerChangedListener(VISIBILITY);
    }

    /**
     * Specify whether this layer is visable on a MapPane or whether the layer
     * is hidden.  Visibility defaults to TRUE on initialisation.
     *
     * @return TRUE if visable.
     */
    public boolean getVisability() {
        return visability;
    }

    /**
     * Get the title of this layer.  If title has not been defined then an
     * empty string is returned.
     *
     * @return The title of this layer.
     */
    public String getTitle() {
        if (title == null) {
            return new String("");
        } else {
            return title;
        }
    }

    /**
     * Set the title of this layer.
     *
     * @param title The title of this layer.
     */
    public void setTitle(String title) {
        this.title = title;
        fireLayerChangedListener(TITLE);
    }

    /**
     * Return the title of this layer.  If no title has been defined, then the
     * class name is returned.
     *
     * @return the title of this layer.
     */
    public String toString() {
        if (title == null) {
            return super.toString();
        } else {
            return title;
        }
    }
    
    
    public void addLayerChangedListener(LayerListener llce) { 
        listenerList.add(LayerListener.class, llce); 
    }

    /**
     * Remove interest in receiving an LayerChangedEvent.
     *
     * @param llcl The object to stop sending LayerChangedEvents.
     */
    public void removeLayerChangedListener(LayerListener llcl) {  
        listenerList.remove(LayerListener.class, llcl);  
    }

    /**
     * Notify all listeners that have registered interest for notification an
     * LayerChangedEvent.
     */
    protected void fireLayerChangedListener(int reason) {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();

        // Process the listeners last to first, notifying
        // those that are interested in this event
        LayerChangedEvent llce = new LayerChangedEvent(this,reason);

        //(Layer[])layers.toArray(new Layer[0]));
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == LayerListener.class) {
                ((LayerListener) listeners[i + 1]).LayerChanged(llce);
            }
        }
    }
    
    public FeatureCollection getFeatures() {
      return features;
    }
    
}
