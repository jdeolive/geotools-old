/*
 * ProjectionException.java
 *
 * Created on 19 February 2002, 23:12
 */

package org.geotools.proj4j;

/**
 *
 * @author  James Macgill
 */
public class ProjectionException extends java.lang.Exception {

    /**
     * Creates a new instance of <code>ProjectionException</code> without detail message.
     */
    public ProjectionException() {
    }


    /**
     * Constructs an instance of <code>ProjectionException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public ProjectionException(String msg) {
        super(msg);
    }
}


