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
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.AffineTransform;

// Geotools dependencies
import org.geotools.pt.Matrix;
import org.geotools.pt.Envelope;
import org.geotools.pt.Dimensioned;
import org.geotools.ct.MathTransform;
import org.geotools.ct.MathTransform2D;
import org.geotools.ct.MathTransformFactory;
import org.geotools.ct.TransformException;
import org.geotools.pt.MismatchedDimensionException;
import org.geotools.cv.PointOutsideCoverageException;
import org.geotools.ct.NoninvertibleTransformException;

// Resources
import org.geotools.resources.Utilities;
import org.geotools.resources.CTSUtilities;
import org.geotools.resources.gcs.Resources;
import org.geotools.resources.gcs.ResourceKeys;


/**
 * Describes the valid range of grid coordinates and the math
 * transform to transform grid coordinates to real world coordinates.
 *
 * @version 1.00
 * @author OpenGIS (www.opengis.org)
 * @author Martin Desruisseaux
 */
public class GridGeometry implements Dimensioned, Serializable {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -8740895616121262893L;
    
    /**
     * The valid coordinate range of a grid coverage. The lowest
     * valid grid coordinate is zero. A grid with 512 cells can
     * have a minimum coordinate of 0 and maximum of 512, with 511
     * as the highest valid index.
     */
    private final GridRange gridRange;
    
    /**
     * The math transform (usually an affine transform). This math transform
     * maps pixel center. A coordinate can be mapped from pixel coordinates
     * to "real world" coordinates using the following line:
     *
     * <pre>gridToCoordinateSystem.transform(pixels, point);</pre>
     */
    private final MathTransform gridToCoordinateSystem;
    
    /**
     * A math transform mapping only the two first dimensions of
     * <code>gridToCoordinateSystem</code>, or <code>null</code>
     * if such a "sub-transform" is not available.
     */
    private final MathTransform2D gridToCoordinateSystem2D;
    
    /**
     * The inverse of <code>gridToCoordinateSystem2D</code>.
     */
    private final MathTransform2D gridFromCoordinateSystem2D;
    
    /**
     * Construct a new grid geometry from a math transform.
     *
     * @param gridRange The valid coordinate range of a grid coverage.
     * @param gridToCoordinateSystem The math transform which allows for the transformations
     *        from grid coordinates (pixel's <em>center</em>) to real world earth coordinates.
     */
    public GridGeometry(final GridRange gridRange, final MathTransform gridToCoordinateSystem) {
        this.gridRange                  = gridRange;
        this.gridToCoordinateSystem     = gridToCoordinateSystem;
        this.gridToCoordinateSystem2D   = (gridToCoordinateSystem instanceof MathTransform2D) ?
                                          (MathTransform2D) gridToCoordinateSystem : null;
        this.gridFromCoordinateSystem2D = inverse(gridToCoordinateSystem2D);
        
        final int dimRange  = gridRange.getDimension();
        final int dimSource = gridToCoordinateSystem.getDimSource();
        final int dimTarget = gridToCoordinateSystem.getDimTarget();
        if (dimRange != dimSource) {
            throw new MismatchedDimensionException(dimRange, dimSource);
        }
        if (dimRange != dimTarget) {
            throw new MismatchedDimensionException(dimRange, dimTarget);
        }
    }
    
    /**
     * Construct a new grid geometry. An affine transform will be computed automatically
     * from the specified envelope.  The <code>inverse</code> argument tells whatever or
     * not an axis should be inversed. Callers will typically set <code>inverse[1]</code>
     * to <code>true</code> in order to inverse the <var>y</var> axis.
     *
     * @param gridRange The valid coordinate range of a grid coverage.
     * @param userRange The corresponding coordinate range in user coordinate.
     *                  This rectangle must contains entirely all pixels, i.e.
     *                  the rectangle's upper left corner must coincide with
     *                  the upper left corner of the first pixel and the rectangle's
     *                  lower right corner must coincide with the lower right corner
     *                  of the last pixel.
     * @param inverse   Tells whatever or not inverse axis. A <code>null</code> value
     *                  inverse no axis.
     */
    public GridGeometry(final GridRange gridRange,
                        final Envelope  userRange,
                        final boolean[] inverse)
    {
        this.gridRange = gridRange;
        /*
         * Check arguments validity.
         * Dimensions must match.
         */
        final int dimension = gridRange.getDimension();
        if (userRange.getDimension() != dimension) {
            throw new MismatchedDimensionException(gridRange, userRange);
        }
        if (inverse!=null && inverse.length!=dimension) {
            throw new MismatchedDimensionException(dimension, inverse.length);
        }
        /*
         * Prepare elements for the 2D sub-transform.
         * Those elements will be set during the matrix
         * setup below.
         */
        double scaleX = 1;
        double scaleY = 1;
        double transX = 0;
        double transY = 0;
        /*
         * Setup the multi-dimensional affine transform for use with OpenGIS.
         * According OpenGIS specification, transforms must map pixel center.
         * This is done by adding 0.5 to grid coordinates.
         */
        final Matrix matrix = new Matrix(dimension+1);
        matrix.setElement(dimension, dimension, 1);
        for (int i=0; i<dimension; i++) {
            double scale = userRange.getLength(i) / gridRange.getLength(i);
            double trans;
            if (inverse==null || !inverse[i]) {
                trans = userRange.getMinimum(i);
            } else {
                scale = -scale;
                trans = userRange.getMaximum(i);
            }
            trans -= scale * (gridRange.getLower(i)-0.5);
            matrix.setElement(i, i,         scale);
            matrix.setElement(i, dimension, trans);
            /*
             * Keep two-dimensional components for the AffineTransform.
             */
            switch (i) {
                case 0: scaleX=scale; transX=trans; break;
                case 1: scaleY=scale; transY=trans; break;
            }
        }
        final MathTransformFactory factory = MathTransformFactory.getDefault();
        gridToCoordinateSystem = factory.createAffineTransform(matrix);
        if (gridToCoordinateSystem instanceof MathTransform2D) {
            gridToCoordinateSystem2D = (MathTransform2D) gridToCoordinateSystem;
        } else {
            gridToCoordinateSystem2D = factory.createAffineTransform(
                    new AffineTransform(scaleX, 0, 0, scaleY, transX, transY));
        }
        this.gridFromCoordinateSystem2D = inverse(gridToCoordinateSystem2D);
    }
    
    /**
     * Construct a new two-dimensional grid geometry. A math transform will
     * be computed automatically with an inverted <var>y</var> axis (i.e.
     * <code>gridRange</code> and <code>userRange</code> are assumed to
     * have <var>y</var> axis in opposite direction).
     *
     * @param gridRange The valid coordinate range of a grid coverage.
     *                  Increasing <var>x</var> values goes right and
     *                  increasing <var>y</var> values goes <strong>down</strong>.
     * @param userRange The corresponding coordinate range in user coordinate.
     *                  Increasing <var>x</var> values goes right and
     *                  increasing <var>y</var> values goes <strong>up</strong>.
     *                  This rectangle must contains entirely all pixels, i.e.
     *                  the rectangle's upper left corner must coincide with
     *                  the upper left corner of the first pixel and the rectangle's
     *                  lower right corner must coincide with the lower right corner
     *                  of the last pixel.
     */
    public GridGeometry(final Rectangle gridRange, final Rectangle2D userRange) {
        final double scaleX = userRange.getWidth()  / gridRange.getWidth();
        final double scaleY = userRange.getHeight() / gridRange.getHeight();
        final double transX = userRange.getMinX()   - gridRange.x*scaleX;
        final double transY = userRange.getMaxY()   + gridRange.y*scaleY;
        final AffineTransform tr = new AffineTransform(scaleX, 0, 0, -scaleY, transX, transY);
        tr.translate(0.5, 0.5); // Map to pixel center
        
        this.gridRange                  = new GridRange(gridRange);
        this.gridToCoordinateSystem2D   = MathTransformFactory.getDefault().createAffineTransform(tr);
        this.gridToCoordinateSystem     = gridToCoordinateSystem2D;
        this.gridFromCoordinateSystem2D = inverse(gridToCoordinateSystem2D);
    }
    
    /**
     * Inverse the specified math transform.
     */
    private static MathTransform2D inverse(final MathTransform2D gridToCoordinateSystem2D) throws IllegalArgumentException {
        if (gridToCoordinateSystem2D!=null) {
            try {
                return (MathTransform2D) gridToCoordinateSystem2D.inverse();
            } catch (NoninvertibleTransformException exception) {
                IllegalArgumentException e = new IllegalArgumentException(); // TODO
                e.initCause(exception);
                throw e;
            }
        }
        return null;
    }
    
    /**
     * Returns the number of dimensions.
     */
    public int getDimension() {
        return gridRange.getDimension();
    }
    
    /**
     * Returns the valid coordinate range of a grid coverage.
     * The lowest valid grid coordinate is zero. A grid with
     * 512 cells can have a minimum coordinate of 0 and maximum
     * of 512, with 511 as the highest valid index.
     */
    public GridRange getGridRange() {
        return gridRange;
    }
    
    /**
     * Returns the math transform which allows  for the transformations from grid
     * coordinates to real world earth coordinates.     The transform is often an
     * affine transformation. The coordinate system of the real world coordinates
     * is given by {@link org.geotools.cv.Coverage#getCoordinateSystem}. If no math
     * transform is available, this method returns <code>null</code>.
     * <br><br>
     * OpenGIS requires that the transform maps <em>pixel centers</em> to real world
     * coordinates. This is different from some other systems that map pixel's upper
     * left corner.
     */
    public MathTransform getGridToCoordinateSystem() {
        return gridToCoordinateSystem;
    }
    
    /**
     * Returns a math transform for the first two dimensions, if such a transform exists.
     * This is a convenient method for working on horizontal data while ignoring vertical
     * or temporal dimensions. This method will succed only if the first two dimensions
     * are separable from extra dimensions (i.e. the transformation from
     * (<var>x<sub>0</sub></var>,&nbsp;<var>x<sub>1</sub></var>,&nbsp;<var>x<sub>2</sub></var>...) to
     * (<var>y<sub>0</sub></var>,&nbsp;<var>y<sub>1</sub></var>,&nbsp;<var>y<sub>2</sub></var>...)
     * must be such that <var>y<sub>0</sub></var> and <var>y<sub>1</sub></var> depend only on
     * <var>x<sub>0</sub></var> and <var>x<sub>1</sub></var>).
     *
     * @return The transform which allows for the transformations from grid coordinates
     *         to real world earth coordinates, operating only on the first two dimensions.
     *         The returned transform is often an instance of {@link AffineTransform}, which
     *         make it convenient for interoperability with Java2D.
     * @throws IllegalStateException if a two-dimensional transform is not available
     *         for this grid geometry.
     */
    public MathTransform2D getGridToCoordinateSystem2D() throws IllegalStateException {
        if (gridToCoordinateSystem2D!=null) {
            return gridToCoordinateSystem2D;
        }
        throw new IllegalStateException(Resources.format(
                ResourceKeys.ERROR_NO_TRANSFORM2D_AVAILABLE));
    }
    
    /**
     * Transform a point using the inverse of {@link #getGridToCoordinateSystem2D()}.
     *
     * @param  point The point in logical coordinate system.
     * @return A new point in the grid coordinate system.
     * @throws IllegalStateException if a two-dimensional inverse
     *         transform is not available for this grid geometry.
     * @throws PointOutsideCoverageException if the transformation failed.
     */
    final Point2D inverseTransform(final Point2D point) throws IllegalStateException {
        if (gridFromCoordinateSystem2D!=null) {
            try {
                return gridFromCoordinateSystem2D.transform(point, null);
            } catch (TransformException exception) {
                final PointOutsideCoverageException e=new PointOutsideCoverageException(point);
                e.initCause(exception);
                throw e;
            }
        }
        throw new IllegalStateException(); // TODO
    }
    
    /**
     * Returns the pixel coordinate of a rectangle containing the
     * specified geographic area. If the rectangle can't be computed,
     * then this method returns <code>null</code>.
     */
    final Rectangle inverseTransform(Rectangle2D bounds) {
        if (bounds!=null && gridFromCoordinateSystem2D!=null) {
            try {
                bounds = CTSUtilities.transform(gridFromCoordinateSystem2D, bounds, null);
                final int xmin = (int)Math.floor(bounds.getMinX() - 0.5);
                final int ymin = (int)Math.floor(bounds.getMinY() - 0.5);
                final int xmax = (int)Math.ceil(bounds.getMaxX() - 0.5);
                final int ymax = (int)Math.ceil(bounds.getMaxY() - 0.5);
                return new Rectangle(xmin, ymin, xmax-xmin, ymax-ymin);
            } catch (TransformException exception) {
                // Ignore, since this method is invoked from 'GridCoverage.prefetch' only.
                // It doesn't matter if the transformation failed; 'prefetch' is just a hint.
            }
        }
        return null;
    }
    
    /**
     * Try to guess which axis are inverted in this grid geometry. If
     * this method can't make the guess, it returns <code>null</code>.
     *
     * @return An array with length equals  to the number of dimensions in
     *         the "real world" coordinate system, or <code>null</code> if
     *         if this array can't be deduced.
     */
    final boolean[] areAxisInverted() {
        final Matrix matrix;
        try {
            // Try to get the affine transform, assuming it is
            // insensitive to location (thus the 'null' argument).
            matrix = gridToCoordinateSystem.derivative(null);
        } catch (NullPointerException exception) {
            // The approximate affine transform is location-dependent.
            // We can't guess axis orientation from this.
            return null;
        } catch (Exception exception) {
            // Some other error occured. We didn't expected it,
            // but it will not prevent 'GridCoverage' to work.
            Utilities.unexpectedException("org.geotools.gcs", "MathTransform",
                                          "derivative", exception);
            return null;
        }
        final int numCol = matrix.getNumCol();
        final boolean[] inverse = new boolean[matrix.getNumRow()];
        for (int j=0; j<inverse.length; j++) {
            for (int i=0; i<numCol; i++) {
                final double value = matrix.getElement(j,i);
                if (i==j) {
                    inverse[j] = (value < 0);
                } else if (value!=0) {
                    // Matrix is not diagonal.
                    // Can't guess axis direction.
                    return null;
                }
            }
        }
        return inverse;
    }
    
    /**
     * Returns a hash value for this grid geometry.
     * This value need not remain consistent between
     * different implementations of the same class.
     */
    public int hashCode() {
        return gridToCoordinateSystem.hashCode()*37 + gridRange.hashCode();
    }
    
    /**
     * Compares the specified object with
     * this grid geometry for equality.
     */
    public boolean equals(final Object object) {
        if (object instanceof GridGeometry) {
            final GridGeometry that = (GridGeometry) object;
            return Utilities.equals(this.gridRange,              that.gridRange             ) &&
                   Utilities.equals(this.gridToCoordinateSystem, that.gridToCoordinateSystem);
        }
        return false;
    }
    
    /**
     * Returns a string représentation of this grid range.
     * The returned string is implementation dependent. It
     * is usually provided for debugging purposes.
     */
    public String toString() {
        final StringBuffer buffer=new StringBuffer(Utilities.getShortClassName(this));
        buffer.append('[');
        buffer.append(gridRange);
        buffer.append(", ");
        buffer.append(gridToCoordinateSystem);
        buffer.append(']');
        return buffer.toString();
    }
}
