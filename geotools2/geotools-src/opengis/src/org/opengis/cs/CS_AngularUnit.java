package org.opengis.cs;
import org.opengis.pt.*;

/** Definition of angular units.*/
public interface CS_AngularUnit extends CS_Unit
{
    /** Returns the number of radians per AngularUnit.*/
    double getRadiansPerUnit();
}
