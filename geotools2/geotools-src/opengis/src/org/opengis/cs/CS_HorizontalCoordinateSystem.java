package org.opengis.cs;
import org.opengis.pt.*;

/** A 2D coordinate system suitable for positions on the Earth's surface.*/
public interface CS_HorizontalCoordinateSystem extends CS_CoordinateSystem
{
    /** Returns the HorizontalDatum.*/
    CS_HorizontalDatum getHorizontalDatum();
}

