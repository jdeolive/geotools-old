package org.opengis.cs;
import org.opengis.pt.*;

/** A 3D coordinate system, with its origin at the center of the Earth.
 *  The X axis points towards the prime meridian. The Y axis points East
 *  or West. The Z axis points North or South. By default the Z axis will
 *  point North, and the Y axis will point East (e.g. a right handed
 *  system), but you should check the axes for non-default values.
 */
public interface CS_GeocentricCoordinateSystem extends CS_CoordinateSystem
{
    /** Returns the HorizontalDatum.
     * The horizontal datum is used to determine where the center of the Earth
     * is considered to be. All coordinate points will be measured from the
     * center of the Earth, and not the surface.
     */
    CS_HorizontalDatum getHorizontalDatum();

    /** Gets the units used along all the axes. */
    CS_LinearUnit getLinearUnit();

    /** Returns the PrimeMeridian.*/
    CS_PrimeMeridian getPrimeMeridian();
}

