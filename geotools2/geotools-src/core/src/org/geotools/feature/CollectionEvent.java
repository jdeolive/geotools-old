package org.geotools.feature;

import java.util.EventObject;

/**
 * A simple event object to represent all events trigered by
 * FeatureCollection instances, typicaly change events.
 * @author Ray.
 */
public class CollectionEvent extends EventObject {
    /**
     * Construct a new CollectionEvent
     * TODO: potential for reason codes here later
     * @param source the collection which triggered the event
     */ 
    public CollectionEvent(Object source){
        super(source);
    }
}

