/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
 * (C) 2001, Institut de Recherche pour le Développement
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
package org.geotools.pt;

// Miscellaneous
import java.util.Arrays;
import java.io.Serializable;
import java.awt.geom.Point2D;

// Geotools dependencies
import org.geotools.util.Cloneable;
import org.geotools.resources.Utilities;
import org.geotools.resources.cts.Resources;
import org.geotools.resources.cts.ResourceKeys;


/**
 * A position defined by a list of numbers. The ordinate
 * values are indexed from <code>0</code> to <code>(numDim-1)</code>,
 * where <code>numDim</code> is the dimension of the coordinate system
 * the coordinate point belongs in.
 *
 * @version 1.00
 * @author OpenGIS (www.opengis.org)
 * @author Martin Desruisseaux
 *
 * @see org.opengis.pt.PT_CoordinatePoint
 * @see java.awt.geom.Point2D
 */
public class CoordinatePoint implements Dimensioned, Cloneable, Serializable {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -6975990652038126533L;
    
    /**
     * The ordinates of the coordinate point.
     *
     * @see org.opengis.pt.PT_CoordinatePoint#ord
     */
    public final double[] ord;
    
    /**
     * Construct a coordinate with the
     * specified number of dimensions.
     *
     * @param  numDim Number of dimensions.
     * @throws NegativeArraySizeException if <code>numDim</code> is negative.
     */
    public CoordinatePoint(final int numDim) throws NegativeArraySizeException {
        ord = new double[numDim];
    }
    
    /**
     * Construct a coordinate with the specified ordinates.
     * The <code>ord</code> array will be copied.
     */
    public CoordinatePoint(final double[] ord) {
        this.ord = (double[]) ord.clone();
    }
    
    /**
     * Construct a 2D coordinate from the specified ordinates.
     */
    public CoordinatePoint(final double x, final double y) {
        ord = new double[] {x,y};
    }
    
    /**
     * Construct a 3D coordinate from the specified ordinates.
     */
    public CoordinatePoint(final double x, final double y, final double z) {
        ord = new double[] {x,y,z};
    }
    
    /**
     * Construct a coordinate from the specified {@link Point2D}.
     */
    public CoordinatePoint(final Point2D point) {
        this(point.getX(), point.getY());
    }
    
    /**
     * Construct a coordinate initialized to the same values than the specified point.
     */
    public CoordinatePoint(final CoordinatePoint point) {
        ord = (double[]) point.ord.clone();
    }

    /**
     * Set this coordinate to the specified {@link Point2D}.
     * This coordinate must be two-dimensional.
     *
     * @param  point The new coordinate for this point.
     * @throws MismatchedDimensionException if this coordinate point is not two-dimensional.
     */
    public void setLocation(final Point2D point) throws MismatchedDimensionException {
        if (ord.length != 2) {
            throw new MismatchedDimensionException(Resources.format(
                        ResourceKeys.ERROR_NOT_TWO_DIMENSIONAL_$1, new Integer(ord.length)));
        }
        ord[0] = point.getX();
        ord[1] = point.getY();
    }

    /**
     * Set this coordinate to the specified <code>CoordinatePoint</code>.
     *
     * @param  point The new coordinate for this point.
     * @throws MismatchedDimensionException if this point doesn't
     *         have the expected dimension.
     */
    public void setLocation(final CoordinatePoint point) throws MismatchedDimensionException {
        ensureDimensionMatch(point.ord.length);
        System.arraycopy(point.ord, 0, ord, 0, ord.length);
    }
    
    /**
     * Returns the ordinate value along the specified dimension.
     * This is equivalent to <code>{@link #ord}[dimension]</code>.
     */
    public final double getOrdinate(final int dimension) {
        return ord[dimension];
    }
    
    /**
     * The number of ordinates of a <code>CoordinatePoint</code>.
     * This is equivalent to <code>{@link #ord}.length</code>.
     */
    public final int getDimension() {
        return ord.length;
    }
    
    /**
     * Convenience method for checking the point's dimension validity.
     * This method is usually call for argument checking.
     *
     * @param  expectedDimension Expected dimension for this point.
     * @throws MismatchedDimensionException if this point doesn't
     *         have the expected dimension.
     */
    final void ensureDimensionMatch(final int expectedDimension)
        throws MismatchedDimensionException
    {
        final int dimension = getDimension();
        if (dimension != expectedDimension) {
            throw new MismatchedDimensionException(dimension, expectedDimension);
        }
    }
    
    /**
     * Returns a {@link Point2D} with the same coordinate
     * as this <code>CoordinatePoint</code>. This is a
     * convenience method for interoperability with Java2D.
     *
     * @throws IllegalStateException if this coordinate point is not two-dimensional.
     */
    public Point2D toPoint2D() throws IllegalStateException {
        if (ord.length == 2) {
            return new Point2D.Double(ord[0], ord[1]);
        } else {
            throw new IllegalStateException(Resources.format(
                        ResourceKeys.ERROR_NOT_TWO_DIMENSIONAL_$1, new Integer(ord.length)));
        }
    }
    
    /**
     * Returns a hash value for this coordinate.
     * This value need not remain consistent between
     * different implementations of the same class.
     */
    public int hashCode() {
        return hashCode(ord);
    }
    
    /**
     * Returns a hash value for the specified ordinates.
     */
    static int hashCode(final double[] ord) {
        long code = 78516481;
        if (ord!=null) {
            for (int i=ord.length; --i>=0;) {
                code = code*31 + Double.doubleToLongBits(ord[i]);
            }
        }
        return (int)(code >>> 32) ^ (int)code;
    }
    
    /**
     * Compares the specified object with
     * this coordinate for equality.
     */
    public boolean equals(final Object object) {
        if (object instanceof CoordinatePoint) {
            final CoordinatePoint that = (CoordinatePoint) object;
            return Arrays.equals(this.ord, that.ord);
        }
        return false;
    }
    
    /**
     * Returns a deep copy of this coordinate.
     */
    public Object clone() {
        return new CoordinatePoint(ord);
    }
    
    /**
     * Returns a string representation of this coordinate.
     * The returned string is implementation dependent.
     * It is usually provided for debugging purposes.
     */
    public String toString() {
        return toString(this, ord);
    }
    
    /**
     * Returns a string representation of an object.
     * The returned string is implementation dependent.
     * It is usually provided for debugging purposes.
     */
    static String toString(final Object owner, final double[] ord) {
        final StringBuffer buffer = new StringBuffer(Utilities.getShortClassName(owner));
        buffer.append('[');
        for (int i=0; i<ord.length; i++) {
            if (i!=0) buffer.append(", ");
            buffer.append(ord[i]);
        }
        buffer.append(']');
        return buffer.toString();
    }
}
