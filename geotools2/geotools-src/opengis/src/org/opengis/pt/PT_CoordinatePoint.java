package org.opengis.pt;

/** A position defined by a list of numbers.
 * The ordinate values are indexed from 0 to (NumDim-1), where NumDim is the
 * dimension of the coordinate system the coordinate point belongs
 * in.
 */
public class PT_CoordinatePoint implements java.io.Serializable
{
    /** The ordinates of the coordinate point. */
    public double[] ord;
}

