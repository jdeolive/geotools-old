/*
 * GeocentricException.java
 *
 * Created on 19 February 2002, 23:40
 */

package org.geotools.proj4j;

/**
 *
 * @author  James Macgill
 */
public class GeocentricException extends java.lang.Exception {

public static final long GEOCENT_NO_ERROR    =    0x0000;
public static final long GEOCENT_LAT_ERROR    =   0x0001;
public static final long GEOCENT_LON_ERROR   =    0x0002;
public static final long GEOCENT_A_ERROR     =    0x0004;
public static final long GEOCENT_B_ERROR     =    0x0008;
public static final long GEOCENT_A_LESS_B_ERROR = 0x0010;

private long code;

    /**
     * Creates a new instance of <code>GeocentricException</code> without detail message.
     */
    public GeocentricException(long code) {
        this("Geocentric Exception "+code);
        this.code = code;
    }


    /**
     * Constructs an instance of <code>GeocentricException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public GeocentricException(String msg) {
        super(msg);
    }
}


