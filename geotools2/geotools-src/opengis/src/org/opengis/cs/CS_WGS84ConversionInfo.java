package org.opengis.cs;
import org.opengis.pt.*;

/** Parameters for a geographic transformation into WGS84.
 *  The Bursa Wolf parameters should be applied to geocentric coordinates,
 *  where the X axis points towards the Greenwich Prime Meridian, the Y axis
 *  points East, and the Z axis points North.
 */
public class CS_WGS84ConversionInfo implements java.io.Serializable
{
    /** Bursa Wolf shift in meters. */
    public double dx;

    /** Bursa Wolf shift in meters. */
    public double dy;

    /** Bursa Wolf shift in meters. */
    public double dz;

    /** Bursa Wolf rotation in arc seconds. */
    public double ex;

    /** Bursa Wolf rotation in arc seconds. */
    public double ey;

    /** Bursa Wolf rotation in arc seconds. */
    public double ez;

    /** Bursa Wolf scaling in parts per million. */
    public double ppm;

    /** Human readable text describing intended region of transformation. */
    public String areaOfUse;
}

