package org.opengis.cs;
import org.opengis.pt.*;

/** A one-dimensional coordinate system suitable for vertical measurements. */
public interface CS_VerticalCoordinateSystem extends CS_CoordinateSystem
{
    /** Gets the vertical datum, which indicates the measurement method. */
    CS_VerticalDatum getVerticalDatum();

    /** Gets the units used along the vertical axis.
     *  The vertical units must be the same as the CS_CoordinateSystem units.
     */
    CS_LinearUnit getVerticalUnit();
}

