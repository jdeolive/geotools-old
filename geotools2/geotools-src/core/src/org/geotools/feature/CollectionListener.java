package org.geotools.feature;

public interface CollectionListener extends java.util.EventListener {
    /** Gets called when a TableChangedEvent is fired
     */
    public void collectionChanged(CollectionEvent tce);
}

