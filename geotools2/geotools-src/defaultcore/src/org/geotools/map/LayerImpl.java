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


/**
 * Legacy implementation of {@link Layer}
 *
 * @author Cameron Shorter
 * @version $Id: LayerImpl.java,v 1.11 2003/08/18 16:33:06 desruisseaux Exp $
 *
 * @deprecated Use {@link DefaultLayer} instead.
 */
public class LayerImpl extends DefaultLayer {
    /**
     * Creates a Layer.
     *
     * @param features The features for this layer.
     * @param style The style to use when rendering features associated with this layer.
     * @throws IllegalArgumentException if an argument is <code>null</code>.
     */
    protected LayerImpl(FeatureCollection features, Style style) throws IllegalArgumentException {
        super(features, style);
    }

    /**
     * Notify all listeners that have registered interest for notification an
     * LayerChangedEvent.
     *
     * @deprecated Use {@link #fireLayerChanged} instead.
     */
    protected void fireLayerChangedListener(final int reason) {
        fireLayerChanged(reason);
    }
}
