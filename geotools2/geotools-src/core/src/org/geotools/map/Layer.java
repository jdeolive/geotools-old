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

// Geotools dependencies
import org.geotools.feature.FeatureCollection;
import org.geotools.styling.Style;
import org.geotools.map.event.LayerEvent;    // For Javadoc
import org.geotools.map.event.LayerListener;


/**
 * A layer to be rendered on a device. A layer is an aggregation of both a
 * {@link FeatureCollection} and a {@link Style}.
 *
 * @author Cameron Shorter
 * @author Martin Desruisseaux
 * @version $Id: Layer.java,v 1.8 2003/08/18 16:32:31 desruisseaux Exp $
 */
public interface Layer {
    /**
     * Get the feature collection for this layer.  If features has not been
     * set yet, then null is returned. 
     *
     * @return the features for this layer.
     */
    FeatureCollection getFeatures(); // Ammended (IanS) changed to FeatureCollection

    /**
     * Get the style for this layer.  If style has not been set, then null is
     * returned.
     *
     * @return The style (SLD).
     */
    Style getStyle();

    /**
     * Get the title of this layer. If title has not been defined then an
     * empty string is returned.
     *
     * @return The title of this layer.
     */
    String getTitle();

    /**
     * Set the title of this layer. A {@link LayerEvent} is fired
     * if the new title is different from the previous one.
     *
     * @param title The title of this layer.
     */
    void setTitle(String title);

    /**
     * Determine whether this layer is visible on a map pane or whether the
     * layer is hidden.
     *
     * @return <code>true</code> if the layer is visible,
     *         or <code>false</code> if the layer is hidden.
     */
    boolean isVisible();

    /**
     * Specify whether this layer is visible on a map pane or whether the layer
     * is hidden. A {@link LayerEvent} is fired if the visibility changed.
     *
     * @param visible Show the layer if <code>true</code>, or
     *                hide the layer if <code>false</code>
     */
    void setVisible(boolean visible);

    /**
     * Specify whether this layer is visable on a MapPane or whether the layer
     * is hidden.
     *
     * @param visable Set the layer visable if TRUE.
     *
     * @deprecated Use {@link #setVisible} instead.
     */
    void setVisability(boolean visable);

    /**
     * Determine whether this layer is visable on a MapPane or whether the
     * layer is hidden.
     *
     * @return TRUE if the layer is visable.
     *
     * @task There is a typo here, change name to getVisibility()
     *
     * @deprecated Use {@link #isVisible} instead.
     */
    boolean getVisability();

    /**
     * Add a listener to notify when a layer property changes. Changes
     * include layer visibility and the title text.
     *
     * @param listener The listener to add to the listener list.
     */
    void addLayerListener(LayerListener listener);

    /**
     * Removes a listener from the listener list for this layer.
     *
     * @param listener The listener to remove from the listener list.
     */
    void removeLayerListener(LayerListener listener);
}
