package org.opengis.cs;
import org.opengis.pt.*;

/** Orientation of axis.
 *  Some coordinate systems use non-standard orientations.  For example,
 *  the first axis in South African grids usually points West, instead of
 *  East. This information is obviously relevant for algorithms converting
 *  South African grid coordinates into Lat/Long.
 */
public class CS_AxisOrientationEnum
{
    public int value;

    /**  Unknown or unspecified axis orientation.
     * This can be used for local or fitted coordinate systems.
     */
    public static final int CS_AO_Other=0;

    /** Increasing ordinates values go North.
     *  This is usually used for Grid Y coordinates and Latitude.
     */
    public static final int CS_AO_North=1;

    /** Increasing ordinates values go South.
     *  This is rarely used.
     */
    public static final int CS_AO_South=2;

    /** Increasing ordinates values go East.
     *  This is rarely used.
     */
    public static final int CS_AO_East=3;

    /** Increasing ordinates values go West.
     *  This is usually used for Grid X coordinates and Longitude.
     */
    public static final int CS_AO_West=4;

    /** Increasing ordinates values go up.
     *  This is used for vertical coordinate systems.
     */
    public static final int CS_AO_Up=5;

    /** Increasing ordinates values go down.
     *  This is used for vertical coordinate systems.
     */
    public static final int CS_AO_Down=6;
}

