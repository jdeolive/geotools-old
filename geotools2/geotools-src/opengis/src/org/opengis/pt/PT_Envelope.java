package org.opengis.pt;

/** A box defined by two positions.
 * The two positions must have the same dimension.
 * Each of the ordinate values in the minimum point must be less than or equal
 * to the corresponding ordinate value in the maximum point.  Please note that
 * these two points may be outside the valid domain of their coordinate system.
 * (Of course the points and envelope do not explicitly reference a coordinate
 * system, but their implicit coordinate system is defined by their context.)
 */
public class PT_Envelope implements java.io.Serializable
{
    /** Point containing minimum ordinate values. */
    public PT_CoordinatePoint minCP;

    /** Point containing maximum ordinate values. */
    public PT_CoordinatePoint maxCP;
}

