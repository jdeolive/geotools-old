package org.geotools.data.shapefile.dbf;

/**
 * Thrown when an error relating to the shapefile
 * occurs.
 */
public class DbaseFileException extends Exception{
    public DbaseFileException(String s){
        super(s);
    }
    public DbaseFileException(String s,Throwable cause){
        super(s,cause);
    }
}




