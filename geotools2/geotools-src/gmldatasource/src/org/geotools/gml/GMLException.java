/*
 * GMLException.java
 *
 * Created on 05 March 2002, 10:53
 */

package org.geotools.gml;

/**
 *
 * @author  ian
 */
public class GMLException extends java.lang.Exception {
    
    /**
     * Creates a new instance of <code>GMLException</code>
     * without detail message.
     */
    public GMLException() {
    }
    
    
    /**
     * Constructs an instance of <code>GMLException</code> 
     * with the specified detail message.
     * @param msg the detail message.
     */
    public GMLException(String msg) {
        super(msg);
    }
}


