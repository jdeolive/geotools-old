package org.geotools.map.events;

import java.util.EventObject;
import org.geotools.feature.FeatureCollection;

 /**
 * Event data passed when a layer is added or removed from LayerModel.
 *
 * @version $Id: LayerListChangedEvent.java,v 1.1 2002/07/14 11:55:54 camerons Exp $
 * @author Cameron Shorter
 */
public class LayerListChangedEvent extends EventObject {

    private FeatureCollection[] layers;

    /**
     * @param layers The new list of layers.
     */
    public LayerListChangedEvent(
            final Object source,
            final FeatureCollection[] layers) {
        super(source);
        this.layers = layers;
    }

    /** Get the new list of layers.
     * @return The new list of layers.
     */
    public FeatureCollection[] getLayerList() {
        return this.layers;
    }
}
