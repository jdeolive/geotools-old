/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
 * (C) 2003, Institut de Recherche pour le Développement
 * (C) 1998, Pêches et Océans Canada
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *
 * Contacts:
 *     UNITED KINGDOM: James Macgill
 *             mailto:j.macgill@geog.leeds.ac.uk
 *
 *     FRANCE: Surveillance de l'Environnement Assistée par Satellite
 *             Institut de Recherche pour le Développement / US-Espace
 *             mailto:seasnet@teledetection.fr
 *
 *     CANADA: Observatoire du Saint-Laurent
 *             Institut Maurice-Lamontagne
 *             mailto:osl@osl.gc.ca
 */
package org.geotools.math;

// J2SE and vecmath dependencies
import java.io.Serializable;
import javax.vecmath.Point3d;
import javax.vecmath.MismatchedSizeException;

// Geotools dependencies
import org.geotools.util.Cloneable;


/**
 * Equation of a plane in a three-dimensional space (<var>x</var>,<var>y</var>,<var>z</var>).
 * The plane equation is expressed by {@link #c}, {@link #cx} and {@link #cy} coefficients as
 * below:
 *
 * <blockquote>
 *     <var>z</var>(<var>x</var>,<var>y</var>) = <var>c</var> +
 *     <var>cx</var>*<var>x</var> + <var>cy</var>*<var>y</var>
 * </blockquote>
 *
 * Those coefficients can be set directly, or computed by a linear regression of this plane
 * through a set of three-dimensional points.
 *
 * @version $Id: Plane.java,v 1.4 2003/08/28 15:41:18 desruisseaux Exp $
 * @author Martin Desruisseaux
 * @author Howard Freeland
 */
public class Plane implements Cloneable, Serializable {
    /**
     * Serial number for compatibility with different versions.
     */
//    private static final long serialVersionUID = 2185952238314399110L;

    /**
     * The <var>c</var> coefficient for this plane. This coefficient appears in the place equation
     * <var><strong>c</strong></var>+<var>cx</var>*<var>x</var>+<var>cy</var>*<var>y</var>.
     */
    public double c;

    /**
     * The <var>cx</var> coefficient for this plane. This coefficient appears in the place equation
     * <var>c</var>+<var><strong>cx</strong></var>*<var>x</var>+<var>cy</var>*<var>y</var>.
     */
    public double cx;

    /**
     * The <var>cy</var> coefficient for this plane. This coefficient appears in the place equation
     * <var>c</var>+<var>cx</var>*<var>x</var>+<var><strong>cy</strong></var>*<var>y</var>.
     */
    public double cy;

    /**
     * Construct a new plane. All coefficients are set to 0.
     */
    public Plane() {
    }

    /**
     * Compute the <var>z</var> value for the specified (<var>x</var>,<var>y</var>) point.
     * The <var>z</var> value is computed using the following equation:
     *
     * <blockquote><code>
     *   z(x,y) = c + cx*x + cy*y
     * </code></blockquote>
     *
     * @param x The <var>x</var> value.
     * @param y The <var>y</var> value.
     * @return  The <var>z</var> value.
     */
    public final double z(final double x, final double y) {
        return c + cx*x + cy*y;
    }

    /**
     * Compute the <var>y</var> value for the specified (<var>x</var>,<var>z</var>) point.
     * The <var>y</var> value is computed using the following equation:
     *
     * <blockquote><code>
     *   y(x,z) = (z - (c+cx*x)) / cy
     * </code></blockquote>
     *
     * @param x The <var>x</var> value.
     * @param z The <var>y</var> value.
     * @return  The <var>y</var> value.
     */
    public final double y(final double x, final double z) {
        return (z - (c+cx*x)) / cy;
    }

    /**
     * Compute the <var>x</var> value for the specified (<var>y</var>,<var>z</var>) point.
     * The <var>x</var> value is computed using the following equation:
     *
     * <blockquote><code>
     *   x(y,z) = (z - (c+cy*y)) / cx
     * </code></blockquote>
     *
     * @param y The <var>x</var> value.
     * @param z The <var>y</var> value.
     * @return  The <var>x</var> value.
     */
    public final double x(final double y, final double z) {
        return (z - (c+cy*y)) / cx;
    }

    /**
     * Computes the plane's coefficients from the specified points.  Three points
     * are enough for determining exactly the plan, providing that the points are
     * not colinear.
     *
     * @throws ArithmeticException If the three points are colinear.
     */
    public void setPlane(final Point3d P1, final Point3d P2, final Point3d P3)
            throws ArithmeticException
    {
        final double m00 = P2.x*P3.y - P3.x*P2.y;
        final double m01 = P3.x*P1.y - P1.x*P3.y;
        final double m02 = P1.x*P2.y - P2.x*P1.y;
        final double det = m00 + m01 + m02;
        if (det == 0) {
            throw new ArithmeticException("Points are colinear");
        }
        c  = ((   m00   )*P1.z + (   m01   )*P2.z + (   m02   )*P3.z) / det;
        cx = ((P2.y-P3.y)*P1.z + (P3.y-P1.y)*P2.z + (P1.y-P2.y)*P3.z) / det;
        cy = ((P3.x-P2.x)*P1.z + (P1.x-P3.x)*P2.z + (P2.x-P1.x)*P3.z) / det;
    }

    /**
     * Compute the plane's coefficients from a set of points. This method use
     * a linear regression in the least-square sense. Result is undertermined
     * if all points are colinear.
     *
     * @param x vector of <var>x</var> coordinates
     * @param y vector of <var>y</var> coordinates
     * @param z vector of <var>z</var> values
     *
     * @throws MismatchedSizeException if <var>x</var>, <var>y</var> and <var>z</var>
     *         don't have the same length.
     */
    public void setPlane(final double[] x, final double[] y, final double[] z)
            throws MismatchedSizeException
    {
        final int N = x.length;
        if (N!=y.length || N!=z.length) {
            throw new MismatchedSizeException();
        }
        double sum_x  = 0;
        double sum_y  = 0;
        double sum_z  = 0;
        double sum_xx = 0;
        double sum_yy = 0;
        double sum_xy = 0;
        double sum_zx = 0;
        double sum_zy = 0;
        for (int i=0; i<N; i++) {
            final double xi = x[i];
            final double yi = y[i];
            final double zi = z[i];
            sum_x  += xi;
            sum_y  += yi;
            sum_z  += zi;
            sum_xx += xi*xi;
            sum_yy += yi*yi;
            sum_xy += xi*yi;
            sum_zx += zi*xi;
            sum_zy += zi*yi;
        }
        /*
         *    ( sum_zx - sum_z*sum_x )  =  cx*(sum_xx - sum_x*sum_x) + cy*(sum_xy - sum_x*sum_y)
         *    ( sum_zy - sum_z*sum_y )  =  cx*(sum_xy - sum_x*sum_y) + cy*(sum_yy - sum_y*sum_y)
         */
        final double ZX = sum_zx - sum_z*sum_x/N;
        final double ZY = sum_zy - sum_z*sum_y/N;
        final double XX = sum_xx - sum_x*sum_x/N;
        final double XY = sum_xy - sum_x*sum_y/N;
        final double YY = sum_yy - sum_y*sum_y/N;
        final double den= (XY*XY - XX*YY);

        cy = (ZX*XY - ZY*XX) / den;
        cx = (ZY*XY - ZX*YY) / den;
        c  = (sum_z - (cx*sum_x + cy*sum_y)) / N;
    }

    /**
     * Returns a string representation of this plane.
     * The string will contains the plane's equation, as below:
     * 
     * <blockquote>
     *     <var>z</var>(<var>x</var>,<var>y</var>) = {@link #c} +
     *     {@link #cx}*<var>x</var> + {@link #cy}*<var>y</var>
     * </blockquote>
     */
    public String toString() {
        final StringBuffer buffer = new StringBuffer("z(x,y)= ");
        if (c==0 && cx==0 && cy==0) {
            buffer.append(0);
        } else {
            if (c != 0) {
                buffer.append(c);
                buffer.append(" + ");
            }
            if (cx != 0) {
                buffer.append(cx);
                buffer.append("*x");
                if (cy != 0) {
                        buffer.append(" + ");
                }
            }
            if (cy != 0) {
                buffer.append(cy);
                buffer.append("*y");
            }
        }
        return buffer.toString();
    }
	
    /**
     * Compare this plane with the specified object for equality.
     */
    public boolean equals(final Object object) {
        if (object!=null && getClass().equals(object.getClass())) {
            final Plane that = (Plane) object;
            return Double.doubleToLongBits(this.c ) == Double.doubleToLongBits(that.c ) &&
                   Double.doubleToLongBits(this.cy) == Double.doubleToLongBits(that.cx) &&
                   Double.doubleToLongBits(this.cx) == Double.doubleToLongBits(that.cy);
        } else {
            return false;
        }
    }
	
    /**
     * Returns a hash code value for this plane.
     */
    public int hashCode() {
        final long code = Double.doubleToLongBits(c ) +
                      37*(Double.doubleToLongBits(cy) +
                      37*(Double.doubleToLongBits(cx)));
        return (int) code ^ (int) (code >>> 32);
    }

    /**
     * Returns a clone of this plane.
     */
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException exception) {
            throw new AssertionError(exception);
        }
    }
}
