package org.geotools.shapefile.shapefile;


/**
 * Thrown when an error relating to the shapefile
 * occures
 */
public class ShapefileException extends Exception {
    public ShapefileException() {
        super();
    }

    public ShapefileException(String s) {
        super(s);
    }
    
    public ShapefileException(String s, Throwable cause) {
        super(s, cause);
    }
}
