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

import org.geotools.data.FeatureSource;
import org.geotools.map.event.MapLayerListener;
import org.geotools.styling.Style;


/**
 * A layer to be rendered on a device. A layer is an aggregation of both a {@link
 * FeatureCollection} and a {@link Style}.
 *
 * @author Cameron Shorter
 * @author Martin Desruisseaux
 * @version $Id: MapLayer.java,v 1.2 2003/12/23 17:21:02 aaime Exp $
 */
public interface MapLayer {
    /**
     * Get the feature collection for this layer.  If features has not been set yet, then null is
     * returned.
     *
     * @return the features for this layer.
     */
    FeatureSource getFeatureSource();

    /**
     * Get the style for this layer.  If style has not been set, then null is returned.
     *
     * @return The style (SLD).
     */
    Style getStyle();

    /**
     * Get the style for this layer.  If style has not been set, then null is returned.
     *
     * @param style The new style
     */
    void setStyle(Style style);

    /**
     * Get the title of this layer. If title has not been defined then an empty string is returned.
     *
     * @return The title of this layer.
     */
    String getTitle();

    /**
     * Set the title of this layer. A {@link LayerEvent} is fired if the new title is different
     * from the previous one.
     *
     * @param title The title of this layer.
     */
    void setTitle(String title);

    /**
     * Determine whether this layer is visible on a map pane or whether the layer is hidden.
     *
     * @return <code>true</code> if the layer is visible, or <code>false</code> if the layer is
     *         hidden.
     */
    boolean isVisible();

    /**
     * Specify whether this layer is visible on a map pane or whether the layer is hidden. A {@link
     * LayerEvent} is fired if the visibility changed.
     *
     * @param visible Show the layer if <code>true</code>, or hide the layer if <code>false</code>
     */
    void setVisible(boolean visible);

    /**
     * Add a listener to notify when a layer property changes. Changes include layer visibility and
     * the title text.
     *
     * @param listener The listener to add to the listener list.
     */
    void addMapLayerListener(MapLayerListener listener);

    /**
     * Removes a listener from the listener list for this layer.
     *
     * @param listener The listener to remove from the listener list.
     */
    void removeMapLayerListener(MapLayerListener listener);
}
