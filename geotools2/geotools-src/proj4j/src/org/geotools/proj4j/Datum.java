/*
 * Datum.java
 *
 * Created on 19 February 2002, 22:36
 */

package org.geotools.proj4j;

/**
 *
 * @author  James Macgill
 */
public class Datum {
    String    id;     /* datum keyword */
    String    defn;   /* ie. "to_wgs84=..." */
    String    ellipse_id; /* ie from ellipse table */
    String    comments; /* EPSG code, etc */

    /** Creates a new instance of Datum */
    public Datum(String id,String defn,String ellipse_id,String comments) {
        this.id = id;
        this.defn = defn;
        this.ellipse_id = ellipse_id;
        this.comments = comments;
    }
    
     static final Datum[] datums={
        new Datum("WGS84","towgs84=0,0,0","WGS84","")
     };

}
