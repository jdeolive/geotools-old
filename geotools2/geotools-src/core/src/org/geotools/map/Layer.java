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

import org.geotools.feature.FeatureCollection;
import org.geotools.styling.Style;


/**
 * Layer is an aggregation of both a FeatureCollection and Style.
 *
 * @author Cameron Shorter
 * @version $Id: Layer.java,v 1.7 2003/08/07 22:11:22 cholmesny Exp $
 */
public interface Layer {
    /**
     * Get the style for this layer.  If style has not been set, then null is
     * returned.
     *
     * @return The style (SLD).
     */
    Style getStyle();

    /**
     * Get the feature collection for this layer.  If dataSource has not been
     * set yet, then null is returned. Ammended (IanS) changed to
     * FeatureCollection
     *
     * @return the features for this layer.
     */
    FeatureCollection getFeatures();

    /**
     * Specify whether this layer is visable on a MapPane or whether the layer
     * is hidden.
     *
     * @param visable Set the layer visable if TRUE.
     */
    void setVisability(boolean visable);

    /**
     * Determine whether this layer is visable on a MapPane or whether the
     * layer is hidden.
     *
     * @return TRUE if the layer is visable.
     *
     * @task There is a typo here, change name to getVisibility()
     */
    boolean getVisability();

    /**
     * Set the title of this layer.
     *
     * @param title The title of this layer.
     */
    void setTitle(String title);
}
