package org.opengis.cs;
import org.opengis.pt.*;

/** A projection from geographic coordinates to projected coordinates.*/
public interface CS_Projection extends CS_Info
{
    /** Gets number of parameters of the projection. */
    int getNumParameters();

    /** Gets an indexed parameter of the projection.
     *  @param index Zero based index of parameter to fetch.
     */
    CS_ProjectionParameter getParameter(int index);

    /** Gets the projection classification name (e.g. 'Transverse_Mercator').
     */
    String getClassName();
}

