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
import java.awt.geom.Rectangle2D;

// Resources
import org.geotools.resources.Utilities;
import org.geotools.resources.XRectangle2D;
import org.geotools.resources.cts.Resources;
import org.geotools.resources.cts.ResourceKeys;


/**
 * A box defined by two positions. The two positions must have the
 * same dimension. Each of the ordinate values in the minimum point
 * must be less than or equal to the corresponding ordinate value
 * in the maximum point. Please note that these two points may be
 * outside the valid domain of their coordinate system. (Of course
 * the points and envelope do not explicitly reference a coordinate
 * system, but their implicit coordinate system is defined by their
 * context.)
 *
 * @version 1.00
 * @author OpenGIS (www.opengis.org)
 * @author Martin Desruisseaux
 *
 * @see org.opengis.pt.PT_Envelope
 * @see java.awt.geom.Rectangle2D
 */
public class Envelope implements Dimensioned, Cloneable, Serializable {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -3228667532994790309L;
    
    /**
     * Minimum and maximum ordinate values. The first half contains minimum
     * ordinates, while the last half contains maximum ordinates.
     */
    private final double[] ord;
    
    /**
     * Check if ordinate values in the minimum point are less than or
     * equal to the corresponding ordinate value in the maximum point.
     *
     * @throws IllegalArgumentException if an ordinate value in the minimum
     *         point is not less than or equal to the corresponding ordinate
     *         value in the maximum point.
     */
    private void checkCoherence() throws IllegalArgumentException {
        final int dimension = ord.length/2;
        for (int i=0; i<dimension; i++) {
            if (!(ord[i] <= ord[dimension+i])) { // Use '!' in order to catch 'NaN'.
                throw new IllegalArgumentException(Resources.format(
                        ResourceKeys.ERROR_ILLEGAL_ENVELOPE_ORDINATE_$1, new Integer(i)));
            }
        }
    }
    
    /**
     * Construct a copy of the specified envelope.
     */
    private Envelope(final Envelope envelope) {
        ord = (double[]) envelope.ord.clone();
    }
    
    /**
     * Construct an empty envelope of the specified dimension.
     * All ordinates are initialized to 0.
     */
    public Envelope(final int dimension) {
        ord = new double[dimension*2];
    }
    
    /**
     * Construct one-dimensional envelope defined by a range of values.
     *
     * @param min The minimal value.
     * @param max The maximal value.
     */
    public Envelope(final double min, final double max) {
        ord = new double[] {min, max};
        checkCoherence();
    }
    
    /**
     * Construct a envelope defined by two positions.
     *
     * @param  minCP Minimum ordinate values.
     * @param  maxCP Maximum ordinate values.
     * @throws MismatchedDimensionException if the two positions don't have the same dimension.
     * @throws IllegalArgumentException if an ordinate value in the minimum point is not
     *         less than or equal to the corresponding ordinate value in the maximum point.
     */
    public Envelope(final double[] minCP, final double[] maxCP)
        throws MismatchedDimensionException
    {
        if (minCP.length != maxCP.length) {
            throw new MismatchedDimensionException(minCP.length, maxCP.length);
        }
        ord = new double[minCP.length + maxCP.length];
        System.arraycopy(minCP, 0, ord, 0,            minCP.length);
        System.arraycopy(maxCP, 0, ord, minCP.length, maxCP.length);
        checkCoherence();
    }
    
    /**
     * Construct a envelope defined by two positions.
     *
     * @param  minCP Point containing minimum ordinate values.
     * @param  maxCP Point containing maximum ordinate values.
     * @throws MismatchedDimensionException if the two positions don't have the same dimension.
     * @throws IllegalArgumentException if an ordinate value in the minimum point is not
     *         less than or equal to the corresponding ordinate value in the maximum point.
     */
    public Envelope(final CoordinatePoint minCP, final CoordinatePoint maxCP)
        throws MismatchedDimensionException
    {
        this(minCP.ord, maxCP.ord);
    }
    
    /**
     * Construct two-dimensional envelope defined by a {@link Rectangle2D}.
     */
    public Envelope(final Rectangle2D rect) {
        ord = new double[] {
            rect.getMinX(), rect.getMinY(),
            rect.getMaxX(), rect.getMaxY()
        };
        checkCoherence();
    }
    
    /**
     * Convenience method for checking the envelope's dimension validity.
     * This method is usually call for argument checking.
     *
     * @param  expectedDimension Expected dimension for this envelope.
     * @throws MismatchedDimensionException if this envelope doesn't have the expected dimension.
     */
    void ensureDimensionMatch(final int expectedDimension) throws MismatchedDimensionException {
        final int dimension = getDimension();
        if (dimension != expectedDimension) {
            throw new MismatchedDimensionException(dimension, expectedDimension);
        }
    }
    
    /**
     * Determines whether or not this envelope is empty.
     * An envelope is non-empty only if it has a length
     * greater that 0 along all dimensions.
     */
    public boolean isEmpty() {
        final int dimension = ord.length/2;
        for (int i=0; i<dimension; i++) {
            if (!(ord[i] < ord[i+dimension])) { // Use '!' in order to catch NaN
                return true;
            }
        }
        return false;
    }
    
    /**
     * Returns the number of dimensions.
     */
    public int getDimension() {
        return ord.length/2;
    }
    
    /**
     * Returns the minimal ordinate along the specified dimension.
     */
    public double getMinimum(final int dimension) {
        if (dimension < ord.length/2) {
            return ord[dimension];
        } else {
            throw new ArrayIndexOutOfBoundsException(dimension);
        }
    }
    
    /**
     * Returns the maximal ordinate along the specified dimension.
     */
    public double getMaximum(final int dimension) {
        if (dimension >= 0) {
            return ord[dimension + ord.length/2];
        } else {
            throw new ArrayIndexOutOfBoundsException(dimension);
        }
    }
    
    /**
     * Returns the center ordinate
     * along the specified dimension.
     */
    public double getCenter(final int dimension) {
        return 0.5*(ord[dimension] + ord[dimension+ord.length/2]);
    }
    
    /**
     * Returns the envelope length along the specified dimension.
     * This length is equals to the maximum ordinate minus the
     * minimal ordinate.
     */
    public double getLength(final int dimension) {
        return ord[dimension+ord.length/2] - ord[dimension];
    }
    
    /**
     * Set the envelope's range along the specified dimension.
     *
     * @param dimension The dimension to set.
     * @param minimum   The minimum value along the specified dimension.
     * @param maximum   The maximum value along the specified dimension.
     */
    public void setRange(final int dimension, double minimum, double maximum) {
        if (minimum > maximum) {
            // Make an empty envelope (min==max)
            // while keeping it legal (min<=max).
            minimum = maximum = 0.5*(minimum+maximum);
        }
        if (dimension>=0) {
            // Do not make any change if 'dimension' is out of range.
            ord[dimension+ord.length/2] = maximum;
            ord[dimension             ] = minimum;
        } else {
            throw new ArrayIndexOutOfBoundsException(dimension);
        }
    }
    
    /**
     * Adds a point to this envelope. The resulting envelope
     * is the smallest envelope that contains both the original envelope and the
     * specified point. After adding a point, a call to {@link #contains} with the
     * added point as an argument will return <code>true</code>, except if one of
     * the point's ordinates was {@link Double#NaN} (in which case the corresponding
     * ordinate have been ignored).
     *
     * @param  point The point to add.
     * @throws MismatchedDimensionException if the specified point doesn't have
     *         the expected dimension.
     */
    public void add(final CoordinatePoint point) throws MismatchedDimensionException {
        final int dim = ord.length/2;
        point.ensureDimensionMatch(dim);
        for (int i=0; i<dim; i++) {
            final double value = point.ord[i];
            if (value < ord[i    ]) ord[i    ]=value;
            if (value > ord[i+dim]) ord[i+dim]=value;
        }
    }
    
    /**
     * Adds an envelope object to this envelope.
     * The resulting envelope is the union of the
     * two <code>Envelope</code> objects.
     *
     * @param  envelope the <code>Envelope</code> to add to this envelope.
     * @throws MismatchedDimensionException if the specified envelope doesn't
     *         have the expected dimension.
     */
    public void add(final Envelope envelope) throws MismatchedDimensionException {
        final int dim = ord.length/2;
        envelope.ensureDimensionMatch(dim);
        for (int i=0; i<dim; i++) {
            final double min = envelope.ord[i    ];
            final double max = envelope.ord[i+dim];
            if (min < ord[i    ]) ord[i    ]=min;
            if (max > ord[i+dim]) ord[i+dim]=max;
        }
    }
    
    /**
     * Tests if a specified coordinate is inside the boundary of this envelope.
     *
     * @param  point The point to text.
     * @return <code>true</code> if the specified coordinates are inside the boundary
     *         of this envelope; <code>false</code> otherwise.
     * @throws MismatchedDimensionException if the specified point doesn't have
     *         the expected dimension.
     */
    public boolean contains(final CoordinatePoint point) throws MismatchedDimensionException {
        final int dimension = ord.length/2;
        point.ensureDimensionMatch(dimension);
        for (int i=0; i<dimension; i++) {
            final double value = point.ord[i];
            if (!(value >= ord[i          ])) return false;
            if (!(value <= ord[i+dimension])) return false;
            // Use '!' in order to take 'NaN' in account.
        }
        return true;
    }
    
    /**
     * Returns a new envelope representing the intersection of this
     * <code>Envelope</code> with the specified <code>Envelope</code>.
     *
     * @param  envelope The <code>Envelope</code> to intersect with this envelope.
     * @return The largest envelope contained in both the specified <code>Envelope</code>
     *         and in this <code>Envelope</code>.
     * @throws MismatchedDimensionException if the specified envelope doesn't
     *         have the expected dimension.
     */
    public Envelope createIntersection(final Envelope envelope) throws MismatchedDimensionException {
        final int dim = ord.length/2;
        envelope.ensureDimensionMatch(dim);
        final Envelope dest = new Envelope(dim);
        for (int i=0; i<dim; i++) {
            double min = Math.max(ord[i    ], envelope.ord[i    ]);
            double max = Math.min(ord[i+dim], envelope.ord[i+dim]);
            if (min > max) {
                // Make an empty envelope (min==max)
                // while keeping it legal (min<=max).
                min = max = 0.5*(min+max);
            }
            dest.ord[i    ]=min;
            dest.ord[i+dim]=max;
        }
        return dest;
    }
    
    /**
     * Returns a new envelope that encompass only some dimensions of this envelope.
     * This method copy this envelope's ordinates into a new envelope, beginning at
     * dimension <code>lower</code> and extending to dimension <code>upper-1</code>.
     * Thus the dimension of the subenvelope is <code>upper-lower</code>.
     *
     * @param  lower The first dimension to copy, inclusive.
     * @param  upper The last  dimension to copy, exclusive.
     * @return The subenvelope.
     * @throws IndexOutOfBoundsException if an index is out of bounds.
     */
    public Envelope getSubEnvelope(final int lower, final int upper) {
        final int curDim = ord.length/2;
        final int newDim = upper-lower;
        if (lower<0 || lower>curDim) {
            throw new IndexOutOfBoundsException(Resources.format(
                    ResourceKeys.ERROR_ILLEGAL_ARGUMENT_$2, "lower", new Integer(lower)));
        }
        if (newDim<0 || upper>curDim) {
            throw new IndexOutOfBoundsException(Resources.format(
                    ResourceKeys.ERROR_ILLEGAL_ARGUMENT_$2, "upper", new Integer(upper)));
        }
        final Envelope envelope = new Envelope(newDim);
        System.arraycopy(ord, lower,        envelope.ord, 0,      newDim);
        System.arraycopy(ord, lower+curDim, envelope.ord, newDim, newDim);
        return envelope;
    }
    
    /**
     * Returns a {@link Rectangle2D} with the same bounds as this <code>Envelope</code>.
     * This is a convenience method for interoperability with Java2D.
     *
     * @throws IllegalStateException if this envelope is not two-dimensional.
     */
    public Rectangle2D toRectangle2D() throws IllegalStateException {
        if (ord.length == 4) {
            return new XRectangle2D(ord[0], ord[1], ord[2]-ord[0], ord[3]-ord[1]);
        } else {
            throw new IllegalStateException(Resources.format(
                    ResourceKeys.ERROR_NOT_TWO_DIMENSIONAL_$1, new Integer(getDimension())));
        }
    }
    
    /**
     * Returns a hash value for this envelope.
     * This value need not remain consistent between
     * different implementations of the same class.
     */
    public int hashCode() {
        return CoordinatePoint.hashCode(ord);
    }
    
    /**
     * Compares the specified object with
     * this envelope for equality.
     */
    public boolean equals(final Object object) {
        if (object instanceof Envelope) {
            final Envelope that = (Envelope) object;
            return Arrays.equals(this.ord, that.ord);
        }
        return false;
    }
    
    /**
     * Returns a deep copy of this envelope.
     */
    public Object clone() {
        return new Envelope(this);
    }
    
    /**
     * Returns a string representation of this envelope.
     * The returned string is implementation dependent.
     * It is usually provided for debugging purposes.
     */
    public String toString() {
        return CoordinatePoint.toString(this, ord);
    }
}
