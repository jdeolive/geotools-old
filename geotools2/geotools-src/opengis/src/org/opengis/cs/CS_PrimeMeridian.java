package org.opengis.cs;
import org.opengis.pt.*;

/** A meridian used to take longitude measurements from.*/
public interface CS_PrimeMeridian extends CS_Info
{
    /** Returns the longitude value relative to the Greenwich Meridian.
     *  The longitude is expressed in this objects angular units.
     */
    double getLongitude();

    /** Returns the AngularUnits.*/
    CS_AngularUnit getAngularUnit();
}

