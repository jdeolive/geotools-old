/*
 * Projection.java
 *
 * Created on February 19, 2002, 4:10 PM
 */

package org.geotools.proj4j;


/**
 *
 * @author  jamesm
 */
public class Projection {
    public static final int  PJD_UNKNOWN   =0;
    public static final int  PJD_3PARAM    =1;   /* Molodensky */
    public static final int  PJD_7PARAM    =2;   /* Molodensky */
    public static final int  PJD_GRIDSHIFT =3;
    public static final int  PJD_WGS84     =4;   /* WGS84 (or anything considered equivelent) */
    
    
    
    
    String descr;
    ParamSet params;
    boolean over;   /* over-range flag */
    boolean geoc;   /* geocentric latitude flag */
    double
    a,  /* major axis or radius if es==0 */
    e,  /* eccentricity */
    es, /* e ^ 2 */
    ra, /* 1/A */
    one_es, /* 1 - e^2 */
    rone_es, /* 1/one_es */
    lam0, phi0, /* central longitude, latitude */
    x0, y0, /* easting and northing */
    k0,	/* general scaling factor */
    to_meter, fr_meter; /* cartesian scaling */
    int     datum_type; /* PJD_UNKNOWN/3PARAM/7PARAM/GRIDSHIFT/WGS84 */
    double datum_params[] = new double[7];
    boolean is_latlong;
    
    /** Creates a new instance of Projection from an argument set*/
    public Projection(String[] args) {
        this.params = new ParamSet();
        for(int i=0;i<args.length;i++){
            params.addParam(
            
    }
    
    public boolean isDatumEqual(Projection test){
        if( datum_type != test.datum_type ) {
            return false;
        }
        else if( a != test.a
        || Math.abs(es - test.es) > 0.000000000050 ) {
        /* the tolerence for es is to ensure that GRS80 and WGS84 are
           considered identical */
            return false;
        }
        else if( datum_type == PJD_3PARAM ) {
            return (datum_params[0] == test.datum_params[0]
            && datum_params[1] == test.datum_params[1]
            && datum_params[2] == test.datum_params[2]);
        }
        else if( datum_type == PJD_7PARAM ) {
            return (datum_params[0] == test.datum_params[0]
            && datum_params[1] == test.datum_params[1]
            && datum_params[2] == test.datum_params[2]
            && datum_params[3] == test.datum_params[3]
            && datum_params[4] == test.datum_params[4]
            && datum_params[5] == test.datum_params[5]
            && datum_params[6] == test.datum_params[6]);
        }
        else if( datum_type == PJD_GRIDSHIFT ) {
            return params.getStringParam("nadgrids").equals(test.params.getStringParam("nadgrids"));
        }
        else
            return true;
    }
    
    
    
}
