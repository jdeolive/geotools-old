package org.opengis.cs;
import org.opengis.pt.*;

/** An aggregate of two coordinate systems (CRS).
 *  One of these is usually a CRS based on a two dimensional coordinate system
 *  such as a geographic or a projected coordinate system with a horizontal
 *  datum.  The other is a vertical CRS which is a one-dimensional coordinate
 *  system with a vertical datum.
 */
public interface CS_CompoundCoordinateSystem extends CS_CoordinateSystem
{
    /** Gets first sub-coordinate system. */
    CS_CoordinateSystem getHeadCS();

    /** Gets second sub-coordinate system. */
    CS_CoordinateSystem getTailCS();
}

