/*
 * Geotools - OpenSource mapping toolkit
 * (C) 2002, Centre for Computational Geography
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
 *
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package org.geotools.gc;

// J2SE dependencies
import java.io.Serializable;
import java.util.Arrays;
import java.awt.Rectangle;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;

// Weak references
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

// OpenGIS dependencies
import org.opengis.gc.GC_GridRange;

// Geotools dependencies
import org.geotools.pt.Dimensioned;
import org.geotools.resources.Utilities;
import org.geotools.resources.gcs.Resources;
import org.geotools.resources.gcs.ResourceKeys;


/**
 * Defines a range of grid coverage coordinates.
 *
 * @version $Id: GridRange.java,v 1.8 2003/04/16 19:25:33 desruisseaux Exp $
 * @author <A HREF="www.opengis.org">OpenGIS</A>
 * @author Martin Desruisseaux
 *
 * @see GC_GridRange
 */
public class GridRange implements Dimensioned, Serializable {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 1452569710967224145L;

    /**
     * Minimum and maximum grid ordinates. The first half contains minimum
     * ordinates, while the last half contains maximum ordinates.
     */
    private final int[] index;

    /**
     * OpenGIS object returned by {@link #toOpenGIS}.
     * It may be a hard or a weak reference.
     */
    transient Object proxy;
    
    /**
     * Check if ordinate values in the minimum index are less than or
     * equal to the corresponding ordinate value in the maximum index.
     *
     * @throws IllegalArgumentException if an ordinate value in the minimum index is not
     *         less than or equal to the corresponding ordinate value in the maximum index.
     */
    private void checkCoherence() throws IllegalArgumentException {
        final int dimension = index.length/2;
        for (int i=0; i<dimension; i++) {
            final int lower = index[i];
            final int upper = index[dimension+i];
            if (!(lower <= upper)) {
                throw new IllegalArgumentException(Resources.format(
                        ResourceKeys.ERROR_BAD_GRID_RANGE_$3, new Integer(i),
                        new Integer(lower), new Integer(upper)));
            }
        }
    }
    
    /**
     * Construct an initially empty grid range of the specified dimension.
     */
    private GridRange(final int dimension) {
        index = new int[dimension*2];
    }
    
    /**
     * Construct one-dimensional grid range.
     *
     * @param min The minimal inclusive value.
     * @param max The maximal exclusive value.
     */
    public GridRange(final int lower, final int upper) {
        index = new int[] {lower, upper};
        checkCoherence();
    }
    
    /**
     * Construct a new grid range.
     *
     * @param lower The valid minimum inclusive grid coordinate.
     *              The array contains a minimum value for each
     *              dimension of the grid coverage. The lowest
     *              valid grid coordinate is zero.
     * @param upper The valid maximum exclusive grid coordinate.
     *              The array contains a maximum value for each
     *              dimension of the grid coverage.
     *
     * @see #getLowers
     * @see #getUppers
     */
    public GridRange(final int[] lower, final int[] upper) {
        if (lower.length != upper.length) {
            throw new IllegalArgumentException(Resources.format(
                        ResourceKeys.ERROR_MISMATCHED_DIMENSION_$2,
                        new Integer(lower.length), new Integer(upper.length)));
        }
        index = new int[lower.length + upper.length];
        System.arraycopy(lower, 0, index, 0,            lower.length);
        System.arraycopy(upper, 0, index, lower.length, upper.length);
        checkCoherence();
    }
    
    /**
     * Construct two-dimensional range defined by a {@link Rectangle}.
     */
    public GridRange(final Rectangle rect) {
        index = new int[] {
            rect.x,            rect.y,
            rect.x+rect.width, rect.y+rect.height
        };
        checkCoherence();
    }
    
    /**
     * Construct two-dimensional range defined by a {@link Raster}.
     */
    public GridRange(final Raster raster) {
        final int x = raster.getMinX();
        final int y = raster.getMinY();
        index = new int[] {
            x,                   y,
            x+raster.getWidth(), y+raster.getHeight()
        };
        checkCoherence();
    }
    
    /**
     * Construct two-dimensional range defined by a {@link RenderedImage}.
     */
    public GridRange(final RenderedImage image) {
        this(image,0);
    }
    
    /**
     * Construct multi-dimensional range defined by a {@link RenderedImage}.
     *
     * @param image The image.
     * @param dimension Number of dimensions for this grid range.
     *        Dimensions over 2 will be set to the [0..1] range.
     */
    GridRange(final RenderedImage image, final int dimension) {
        index = new int[dimension*2];
        final int x = image.getMinX();
        final int y = image.getMinY();
        index[0] = x;
        index[1] = y;
        index[dimension+0] = x+image.getWidth();
        index[dimension+1] = y+image.getHeight();
        Arrays.fill(index, dimension+2, index.length, 1);
        checkCoherence();
    }
    
    /**
     * Returns the number of dimensions.
     */
    public int getDimension() {
        return index.length/2;
    }
    
    /**
     * Returns the valid minimum inclusive grid
     * coordinate along the specified dimension.
     *
     * @see GC_GridRange#getLo
     * @see #getLowers
     */
    public int getLower(final int dimension) {
        if (dimension < index.length/2) {
            return index[dimension];
        }
        throw new ArrayIndexOutOfBoundsException(dimension);
    }
    
    /**
     * Returns the valid maximum exclusive grid
     * coordinate along the specified dimension.
     *
     * @see GC_GridRange#getHi
     * @see #getUppers
     */
    public int getUpper(final int dimension) {
        if (dimension >= 0) {
            return index[dimension + index.length/2];
        }
        else throw new ArrayIndexOutOfBoundsException(dimension);
    }
    
    /**
     * Returns the number of integer grid coordinates along the specified dimension.
     * This is equals to <code>getUpper(dimension)-getLower(dimension)</code>.
     */
    public int getLength(final int dimension) {
        return index[dimension+index.length/2] - index[dimension];
    }

    /**
     * Returns the valid minimum inclusive grid coordinates along all dimensions.
     */
    public int[] getLowers() {
        final int[] lo = new int[index.length/2];
        System.arraycopy(index, 0, lo, 0, lo.length);
        return lo;
    }

    /**
     * Returns the valid maximum exclusive grid coordinates along all dimensions.
     */
    public int[] getUppers() {
        final int[] hi = new int[index.length/2];
        System.arraycopy(index, index.length/2, hi, 0, hi.length);
        return hi;
    }
    
    /**
     * Returns a new grid range that encompass only some dimensions of this grid range.
     * This method copy this grid range's index into a new grid range, beginning at
     * dimension <code>lower</code> and extending to dimension <code>upper-1</code>.
     * Thus the dimension of the subgrid range is <code>upper-lower</code>.
     *
     * @param  lower The first dimension to copy, inclusive.
     * @param  upper The last  dimension to copy, exclusive.
     * @return The subgrid range.
     * @throws IndexOutOfBoundsException if an index is out of bounds.
     */
    public GridRange getSubGridRange(final int lower, final int upper) {
        final int curDim = index.length/2;
        final int newDim = upper-lower;
        if (lower<0 || lower>curDim) {
            throw new IndexOutOfBoundsException(org.geotools.resources.cts.Resources.format(
                    org.geotools.resources.cts.ResourceKeys.ERROR_ILLEGAL_ARGUMENT_$2,
                    "lower", new Integer(lower)));
        }
        if (newDim<0 || upper>curDim) {
            throw new IndexOutOfBoundsException(org.geotools.resources.cts.Resources.format(
                    org.geotools.resources.cts.ResourceKeys.ERROR_ILLEGAL_ARGUMENT_$2,
                    "upper", new Integer(upper)));
        }
        final GridRange gridRange = new GridRange(newDim);
        System.arraycopy(index, lower,        gridRange.index, 0,      newDim);
        System.arraycopy(index, lower+curDim, gridRange.index, newDim, newDim);
        return gridRange;
    }
    
    /**
     * Returns a {@link Rectangle} with the same bounds as this <code>GridRange</code>.
     * This is a convenience method for interoperability with Java2D.
     *
     * @throws IllegalStateException if this grid range is not two-dimensional.
     */
    public Rectangle toRectangle() throws IllegalStateException {
        if (index.length == 4) {
            return new Rectangle(index[0], index[1], index[2]-index[0], index[3]-index[1]);
        } else {
            throw new IllegalStateException(org.geotools.resources.cts.Resources.format(
                    org.geotools.resources.cts.ResourceKeys.ERROR_NOT_TWO_DIMENSIONAL_$1,
                    new Integer(getDimension())));
        }
    }
    
    /**
     * Returns a hash value for this grid range.
     * This value need not remain consistent between
     * different implementations of the same class.
     */
    public int hashCode() {
        int code=45123678;
        if (index!=null) {
            for (int i=index.length; --i>=0;) {
                code = code*31 + index[i];
            }
        }
        return code;
    }
    
    /**
     * Compares the specified object with
     * this grid range for equality.
     */
    public boolean equals(final Object object) {
        if (object instanceof GridRange) {
            final GridRange that = (GridRange) object;
            return Arrays.equals(this.index, that.index);
        }
        return false;
    }
    
    /**
     * Returns a string représentation of this grid range.
     * The returned string is implementation dependent. It
     * is usually provided for debugging purposes.
     */
    public String toString() {
        final int dimension = index.length/2;
        final StringBuffer buffer=new StringBuffer(Utilities.getShortClassName(this));
        buffer.append('[');
        for (int i=0; i<dimension; i++) {
            if (i!=0) {
                buffer.append(", ");
            }
            buffer.append(index[i]);
            buffer.append("..");
            buffer.append(index[i+dimension]);
        }
        buffer.append(']');
        return buffer.toString();
    }




    /////////////////////////////////////////////////////////////////////////
    ////////////////                                         ////////////////
    ////////////////             OPENGIS ADAPTER             ////////////////
    ////////////////                                         ////////////////
    /////////////////////////////////////////////////////////////////////////

    /**
     * Returns an OpenGIS interface for this grid range. This method first
     * looks in the cache. If no interface was previously cached, then this
     * method creates a new adapter and caches the result.
     *
     * @param  adapters The originating {@link Adapters}.
     * @return The OpenGIS interface. The returned type is a generic {@link Object}
     *         in order to avoid premature class loading of OpenGIS interface.
     */
    final synchronized Object toOpenGIS(final Object adapters) {
        if (proxy != null) {
            if (proxy instanceof Reference) {
                final Object ref = ((Reference) proxy).get();
                if (ref != null) {
                    return ref;
                }
            } else {
                return proxy;
            }
        }
        final Object opengis = new Export(adapters);
        proxy = new WeakReference(opengis);
        return opengis;
    }

    /**
     * Wraps a {@link GridRange} object for use with OpenGIS.
     */
    final class Export implements GC_GridRange, Serializable {
        /**
         * Serial number for interoperability with different versions.
         */
        private static final long serialVersionUID = -4941451644914317370L;

        /**
         * Constructs an OpenGIS structure.
         */
        public Export(final Object adapters) {
        }
        
        /**
         * Returns the underlying implementation.
         */
        public final GridRange getImplementation() {
            return GridRange.this;
        }

        /**
         * The valid minimum inclusive grid coordinate.
         */
        public int[] getLo() {
            final int[] lower = new int[getDimension()];
            for (int i=0; i<lower.length; i++) {
                lower[i] = getLower(i);
            }
            return lower;
        }

        /**
         * The valid maximum exclusive grid coordinate.
         */
        public int[] getHi() {
            final int[] upper = new int[getDimension()];
            for (int i=0; i<upper.length; i++) {
                upper[i] = getUpper(i);
            }
            return upper;
        }
    }
}
