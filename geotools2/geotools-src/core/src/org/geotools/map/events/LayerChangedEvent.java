/*
 * LayerChangedEvent.java
 *
 * Created on 10 July 2003, 17:10
 */

package org.geotools.map.events;

import java.util.EventObject;

/**
 *
 * @author  iant
 */
public class LayerChangedEvent extends EventObject {
    
    private int reason=0;
    
    
    
    /** Creates a new instance of LayerChangedEvent */
    public LayerChangedEvent(Object source) {
        super(source);
        
    }
    
    public LayerChangedEvent(Object source, int reason){
        this(source);
        this.reason = reason;
        
    }
    /** Getter for property reason.
     * @return Value of property reason.
     *
     */
    public int getReason() {
        return reason;
    }
    
    /** Setter for property reason.
     * @param reason New value of property reason.
     *
     */
    public void setReason(int reason) {
        this.reason = reason;
    }
    
    /** Getter for property source.
     * @return Value of property source.
     *
     */
    public java.lang.Object getSource() {
        return source;
    }
    
    /** Setter for property source.
     * @param source New value of property source.
     *
     */
    public void setSource(java.lang.Object source) {
        this.source = source;
    }
    
}
