package org.geotools.data.shapefile.dbf;

import org.geotools.data.DataSourceException;
/**
 * Thrown when an error relating to the shapefile
 * occurs.
 */
public class DbaseFileException extends DataSourceException {
    public DbaseFileException(String s){
        super(s);
    }
    public DbaseFileException(String s,Throwable cause){
        super(s,cause);
    }
}




