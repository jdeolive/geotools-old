package org.opengis.cs;
import org.opengis.pt.*;

/** Details of axis.
 *  This is used to label axes, and indicate the orientation.
 */
public class CS_AxisInfo implements java.io.Serializable
{
    /** Gets enumerated value for orientation.
     */
    public CS_AxisOrientationEnum orientation;

    /** Human readable name for axis.
     *  Possible values are X, Y, Long, Lat or any other short string.
     */
    public String name;
}

