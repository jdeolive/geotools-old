package org.opengis.cs;
import org.opengis.pt.*;

/** Procedure used to measure positions on the surface of the Earth.*/
public interface CS_HorizontalDatum extends CS_Datum
{
    /** Returns the Ellipsoid.*/
    CS_Ellipsoid getEllipsoid();

    /** Gets preferred parameters for a Bursa Wolf transformation into WGS84.
     *  The 7 returned values correspond to (dx,dy,dz) in meters, (ex,ey,ez)
     *  in arc-seconds, and scaling in parts-per-million.
     *  This method will always fail for horizontal datums with type CS_HD_Other.
     *  This method may also fail if no suitable transformation is available.
     *  Failures are indicated using the normal failing behavior of the DCP
     *  (e.g. throwing an exception).
     *
     */
    CS_WGS84ConversionInfo getWGS84Parameters();
}

