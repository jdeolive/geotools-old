package org.geotools.featuretable;

public interface TableChangedListener extends java.util.EventListener {
    /** Gets called when a TableChangedEvent is fired
     */
    public void tableChanged(TableChangedEvent tce);
}

