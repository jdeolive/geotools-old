package org.geotools.feature;

/**
 * Interface to be implemeted by all listeners of CollectionEvents
 * @author Ray.
 */
public interface CollectionListener extends java.util.EventListener {
    /** 
     * Gets called when a CollectionEvent is fired
     * Typicaly firerd to signify a change has occured in the collection
     * @param tce The CollectionEvent
     */
    public void collectionChanged(CollectionEvent tce);
}

