package org.opengis.cs;
import org.opengis.pt.*;

/** A named projection parameter value.
 *  The linear units of parameters' values match the linear units of the
 *  containing projected coordinate system.  The angular units of parameter
 *  values match the angular units of the geographic coordinate system that
 *  the projected coordinate system is based on.  (Notice that this is
 *  different from CT_Parameter, where the units are always meters and
 *  degrees.)
 */
public class CS_ProjectionParameter implements java.io.Serializable
{
    /** The parameter name.*/
    public String name;

    /** The parameter value. */
    public double value;
}

