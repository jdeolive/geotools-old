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


/**
 * Legacy implementation of {@link LayerList}
 *
 * @author Cameron Shorter
 * @version $Id: LayerListImpl.java,v 1.7 2003/08/18 16:33:06 desruisseaux Exp $
 *
 * @deprecated Use {@link DefaultLayerList} instead.
 */
public class LayerListImpl extends DefaultLayerList {
    /**
     * Creates a layer list initialised with one layer.
     *
     * @param layer The layer to add.
     */
    public LayerListImpl(Layer layer) {
        super(layer);
    }

    /**
     * Creates a layer list initialised with an array of layers.
     *
     * @param layers The new layers that are to be added.
     */
    public LayerListImpl(Layer[] layers) {
        super(layers);
    }
    
    /**
     * Notify all listeners that have registered interest for notification on
     * {@linkplain org.geotools.map.event.LayerListEvent layer list change event}.
     *
     * @deprecated Use {@link #fireLayerListChanged()} instead.
     */
    protected void fireLayerListChangedListener() {
        fireLayerListChanged();
    }
}
