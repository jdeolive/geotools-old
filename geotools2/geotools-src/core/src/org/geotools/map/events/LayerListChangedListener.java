package org.geotools.map.events;

import java.util.EventListener;

/**
 * Methods to handle a change in the list of Layers.
 * @author Cameron Shorter
 * @version $Id: LayerListChangedListener.java,v 1.1 2002/07/14 11:55:54 camerons Exp $
 */
public interface LayerListChangedListener extends EventListener {

    /**
     * Process an LayerListChangedEvent, probably involves a redraw.
     * @param LayerListChangedEvent The new extent.
     */
    void LayerListChanged(
            LayerListChangedEvent layerListChangedEvent);
}
