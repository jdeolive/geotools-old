package org.opengis.cs;
import org.opengis.pt.*;

/** A coordinate system based on latitude and longitude.
 *  Some geographic coordinate systems are Lat/Lon, and some are Lon/Lat.
 *  You can find out which this is by examining the axes.  You should also
 *  check the angular units, since not all geographic coordinate systems use
 *  degrees.
 */
public interface CS_GeographicCoordinateSystem extends CS_HorizontalCoordinateSystem
{
    /** Returns the AngularUnit.
     *  The angular unit must be the same as the CS_CoordinateSystem units.
     */
    CS_AngularUnit getAngularUnit();

    /** Returns the PrimeMeridian.*/
    CS_PrimeMeridian getPrimeMeridian();

    /** Gets the number of available conversions to WGS84 coordinates. */
    int getNumConversionToWGS84();

    /** Gets details on a conversion to WGS84.
     *  Some geographic coordinate systems provide several transformations
     *  into WGS84, which are designed to provide good accuracy in different
     *  areas of interest.  The first conversion (with index=0) should
     *  provide acceptable accuracy over the largest possible area of
     *  interest.
     *  @param index Zero based index of conversion to fetch.
     */
    CS_WGS84ConversionInfo  getWGS84ConversionInfo(int index);
}

