package org.opengis.cs;
import org.opengis.pt.*;

/** Definition of linear units.*/
public interface CS_LinearUnit extends CS_Unit
{
    /** Returns the number of meters per LinearUnit.*/
    double getMetersPerUnit();
}

