package org.geotools.featuretable;
import java.util.EventObject;
public class TableChangedEvent extends java.util.EventObject {
    // TODO: potential for reason codes here later
    public TableChangedEvent(Object source){
        super(source);
    }
}

