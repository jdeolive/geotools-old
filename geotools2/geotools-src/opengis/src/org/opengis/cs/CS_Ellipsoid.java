package org.opengis.cs;
import org.opengis.pt.*;

/** An approximation of the Earth's surface as a squashed sphere.*/
public interface CS_Ellipsoid extends  CS_Info
{
    /** Gets the equatorial radius.
     *  The returned length is expressed in this object's axis units.
     */
    double getSemiMajorAxis();

    /** Gets the polar radius.
     *  The returned length is expressed in this object's axis units.
     */
    double getSemiMinorAxis();

    /** Returns the value of the inverse of the flattening constant.
     *  The inverse flattening is related to the equatorial/polar radius
     *  by the formula ivf=re/(re-rp). For perfect spheres, this formula
     *  breaks down, and a special IVF value of zero is used.
     */
    double getInverseFlattening();

    /** Is the Inverse Flattening definitive for this ellipsoid?
     *  Some ellipsoids use the IVF as the defining value, and calculate the
     *  polar radius whenever asked. Other ellipsoids use the polar radius to
     *  calculate the IVF whenever asked. This distinction can be important to
     * avoid floating-point rounding errors.
     */
    boolean isIvfDefinitive();

    /** Returns the LinearUnit.
     *  The units of the semi-major and semi-minor axis values.
     */
    CS_LinearUnit getAxisUnit();
}

