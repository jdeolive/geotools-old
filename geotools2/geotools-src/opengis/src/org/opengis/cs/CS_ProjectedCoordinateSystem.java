package org.opengis.cs;
import org.opengis.pt.*;

/** A 2D cartographic coordinate system.*/
public interface CS_ProjectedCoordinateSystem extends CS_HorizontalCoordinateSystem
{
    /** Returns the GeographicCoordinateSystem.*/
    CS_GeographicCoordinateSystem getGeographicCoordinateSystem();

    /** Returns the LinearUnits.
     *  The linear unit must be the same as the CS_CoordinateSystem units.
     */
    CS_LinearUnit getLinearUnit();

    /** Gets the projection.
     */
    CS_Projection getProjection();
}

