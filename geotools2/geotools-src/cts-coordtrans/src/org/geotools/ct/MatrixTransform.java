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
package org.geotools.ct;

// Geotools dependencies and resources
import org.geotools.pt.Matrix;
import org.geotools.pt.CoordinatePoint;
import org.geotools.resources.Utilities;
import org.geotools.resources.cts.Resources;
import org.geotools.resources.cts.ResourceKeys;
import org.geotools.resources.XAffineTransform;

// J2SE dependencies
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.util.Arrays;
import java.io.Serializable;

// JAI dependencies
import javax.media.jai.ParameterList;

// Vecmath (Java3D) dependencies
import javax.vecmath.GMatrix;
import javax.vecmath.SingularMatrixException;


/**
 * Transforms multi-dimensional coordinate points using a {@link Matrix}.
 *
 * @version $Id: MatrixTransform.java,v 1.7 2003/04/14 21:08:14 desruisseaux Exp $
 * @author OpenGIS (www.opengis.org)
 * @author Martin Desruisseaux
 */
final class MatrixTransform extends AbstractMathTransform implements LinearTransform, Serializable {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -2104496465933824935L;
    
    /**
     * The number of rows.
     */
    private final int numRow;
    
    /**
     * The number of columns.
     */
    private final int numCol;
    
    /**
     * Elements of the matrix. Column indice vary fastest.
     */
    private final double[] elt;
    
    /**
     * Construct a transform.
     */
    protected MatrixTransform(final GMatrix matrix) {
        numRow = matrix.getNumRow();
        numCol = matrix.getNumCol();
        elt = new double[numRow*numCol];
        int index = 0;
        for (int j=0; j<numRow; j++) {
            for (int i=0; i<numCol; i++) {
                elt[index++] = matrix.getElement(j,i);
            }
        }
    }
    
    /**
     * Transforms an array of floating point coordinates by this matrix. Point coordinates
     * must have a dimension equals to <code>{@link Matrix#getNumCol}-1</code>. For example,
     * for square matrix of size 4&times;4, coordinate points are three-dimensional and
     * stored in the arrays starting at the specified offset (<code>srcOff</code>) in the order
     * <code>[x<sub>0</sub>, y<sub>0</sub>, z<sub>0</sub>,
     *        x<sub>1</sub>, y<sub>1</sub>, z<sub>1</sub>...,
     *        x<sub>n</sub>, y<sub>n</sub>, z<sub>n</sub>]</code>.
     *
     * The transformed points <code>(x',y',z')</code> are computed as below
     * (note that this computation is similar to {@link PerspectiveTransform}):
     *
     * <blockquote><pre>
     * [ u ]     [ m<sub>00</sub>  m<sub>01</sub>  m<sub>02</sub>  m<sub>03</sub> ] [ x ]
     * [ v ]  =  [ m<sub>10</sub>  m<sub>11</sub>  m<sub>12</sub>  m<sub>13</sub> ] [ y ]
     * [ w ]     [ m<sub>20</sub>  m<sub>21</sub>  m<sub>22</sub>  m<sub>23</sub> ] [ z ]
     * [ t ]     [ m<sub>30</sub>  m<sub>31</sub>  m<sub>32</sub>  m<sub>33</sub> ] [ 1 ]
     *
     *   x' = u/t
     *   y' = v/t
     *   y' = w/t
     * </pre></blockquote>
     *
     * @param srcPts The array containing the source point coordinates.
     * @param srcOff The offset to the first point to be transformed in the source array.
     * @param dstPts The array into which the transformed point coordinates are returned.
     * @param dstOff The offset to the location of the first transformed point that is stored
     *               in the destination array. The source and destination array sections can
     *               be overlaps.
     * @param numPts The number of points to be transformed
     */
    public void transform(float[] srcPts, int srcOff,
                          final float[] dstPts, int dstOff, int numPts)
    {
        final int  inputDimension = numCol-1; // The last ordinate will be assumed equals to 1.
        final int outputDimension = numRow-1;
        final double[]     buffer = new double[numRow];
        if (srcPts==dstPts) {
            // We are going to write in the source array. Checks if
            // source and destination sections are going to clash.
            final int upperSrc = srcOff + numPts*inputDimension;
            if (upperSrc > dstOff) {
                if (inputDimension >= outputDimension ? dstOff > srcOff :
                            dstOff + numPts*outputDimension > upperSrc) {
                    // If source overlaps destination, then the easiest workaround is
                    // to copy source data. This is not the most efficient however...
                    srcPts = new float[numPts*inputDimension];
                    System.arraycopy(dstPts, srcOff, srcPts, 0, srcPts.length);
                    srcOff = 0;
                }
            }
        }
        while (--numPts>=0) {
            int mix=0;
            for (int j=0; j<numRow; j++) {
                double sum=elt[mix + inputDimension];
                for (int i=0; i<inputDimension; i++) {
                    sum += srcPts[srcOff+i]*elt[mix++];
                }
                buffer[j] = sum;
                mix++;
            }
            final double w = buffer[outputDimension];
            for (int j=0; j<outputDimension; j++) {
                // 'w' is equals to 1 if the transform is affine.
                dstPts[dstOff++] = (float) (buffer[j]/w);
            }
            srcOff += inputDimension;
        }
    }
    
    /**
     * Transforms an array of floating point coordinates by this matrix. Point coordinates
     * must have a dimension equals to <code>{@link Matrix#getNumCol}-1</code>. For example,
     * for square matrix of size 4&times;4, coordinate points are three-dimensional and
     * stored in the arrays starting at the specified offset (<code>srcOff</code>) in the order
     * <code>[x<sub>0</sub>, y<sub>0</sub>, z<sub>0</sub>,
     *        x<sub>1</sub>, y<sub>1</sub>, z<sub>1</sub>...,
     *        x<sub>n</sub>, y<sub>n</sub>, z<sub>n</sub>]</code>.
     *
     * The transformed points <code>(x',y',z')</code> are computed as below
     * (note that this computation is similar to {@link PerspectiveTransform}):
     *
     * <blockquote><pre>
     * [ u ]     [ m<sub>00</sub>  m<sub>01</sub>  m<sub>02</sub>  m<sub>03</sub> ] [ x ]
     * [ v ]  =  [ m<sub>10</sub>  m<sub>11</sub>  m<sub>12</sub>  m<sub>13</sub> ] [ y ]
     * [ w ]     [ m<sub>20</sub>  m<sub>21</sub>  m<sub>22</sub>  m<sub>23</sub> ] [ z ]
     * [ t ]     [ m<sub>30</sub>  m<sub>31</sub>  m<sub>32</sub>  m<sub>33</sub> ] [ 1 ]
     *
     *   x' = u/t
     *   y' = v/t
     *   y' = w/t
     * </pre></blockquote>
     *
     * @param srcPts The array containing the source point coordinates.
     * @param srcOff The offset to the first point to be transformed in the source array.
     * @param dstPts The array into which the transformed point coordinates are returned.
     * @param dstOff The offset to the location of the first transformed point that is stored
     *               in the destination array. The source and destination array sections can
     *               be overlaps.
     * @param numPts The number of points to be transformed
     */
    public void transform(double[] srcPts, int srcOff,
                          final double[] dstPts, int dstOff, int numPts)
    {
        final int  inputDimension = numCol-1; // The last ordinate will be assumed equals to 1.
        final int outputDimension = numRow-1;
        final double[]     buffer = new double[numRow];
        if (srcPts==dstPts) {
            // We are going to write in the source array. Checks if
            // source and destination sections are going to clash.
            final int upperSrc = srcOff + numPts*inputDimension;
            if (upperSrc > dstOff) {
                if (inputDimension >= outputDimension ? dstOff > srcOff :
                            dstOff + numPts*outputDimension > upperSrc) {
                    // If source overlaps destination, then the easiest workaround is
                    // to copy source data. This is not the most efficient however...
                    srcPts = new double[numPts*inputDimension];
                    System.arraycopy(dstPts, srcOff, srcPts, 0, srcPts.length);
                    srcOff = 0;
                }
            }
        }
        while (--numPts>=0) {
            int mix=0;
            for (int j=0; j<numRow; j++) {
                double sum=elt[mix + inputDimension];
                for (int i=0; i<inputDimension; i++) {
                    sum += srcPts[srcOff+i]*elt[mix++];
                }
                buffer[j] = sum;
                mix++;
            }
            final double w = buffer[outputDimension];
            for (int j=0; j<outputDimension; j++) {
                // 'w' is equals to 1 if the transform is affine.
                dstPts[dstOff++] = buffer[j]/w;
            }
            srcOff += inputDimension;
        }
    }
    
    /**
     * Gets the derivative of this transform at a point.
     * For a matrix transform, the derivative is the
     * same everywhere.
     */
    public Matrix derivative(final Point2D point) {
        return derivative((CoordinatePoint)null);
    }
    
    /**
     * Gets the derivative of this transform at a point.
     * For a matrix transform, the derivative is the
     * same everywhere.
     */
    public Matrix derivative(final CoordinatePoint point) {
        final Matrix matrix = getMatrix();
        matrix.setSize(numRow-1, numCol-1);
        return matrix;
    }
    
    /**
     * Returns a copy of the matrix.
     */
    public Matrix getMatrix() {
        return new Matrix(numRow, numCol, elt);
    }
    
    /**
     * Gets the dimension of input points.
     */
    public int getDimSource() {
        return numCol-1;
    }
    
    /**
     * Gets the dimension of output points.
     */
    public int getDimTarget() {
        return numRow-1;
    }
    
    /**
     * Tests whether this transform does not move any points.
     */
    public boolean isIdentity() {
        if (numRow != numCol) {
            return false;
        }
        int index=0;
        for (int j=0; j<numRow; j++) {
            for (int i=0; i<numCol; i++) {
                if (elt[index++] != (i==j ? 1 : 0)) {
                    return false;
                }
            }
        }
        return true;
    }
    
    /**
     * Creates the inverse transform of this object.
     */
    public MathTransform inverse() throws NoninvertibleTransformException {
        if (isIdentity()) {
            return this;
        }
        final Matrix matrix = getMatrix();
        try {
            matrix.invert();
        } catch (SingularMatrixException exception) {
            NoninvertibleTransformException e = new NoninvertibleTransformException(
                    Resources.format(ResourceKeys.ERROR_NONINVERTIBLE_TRANSFORM));
            e.initCause(exception);
            throw e;
        }
        return new MatrixTransform(matrix);
    }
    
    /**
     * Returns a hash value for this transform.
     * This value need not remain consistent between
     * different implementations of the same class.
     */
    public int hashCode() {
        long code=2563217;
        for (int i=elt.length; --i>=0;) {
            code = code*37 + Double.doubleToLongBits(elt[i]);
        }
        return (int)(code >>> 32) ^ (int)code;
    }
    
    /**
     * Compares the specified object with
     * this math transform for equality.
     */
    public boolean equals(final Object object) {
        if (object==this) {
            // Slight optimization
            return true;
        }
        if (super.equals(object)) {
            final MatrixTransform that = (MatrixTransform) object;
            return this.numRow == that.numRow &&
                   this.numCol == that.numCol &&
                   Arrays.equals(this.elt, that.elt);
        }
        return false;
    }
    
    /**
     * Returns the WKT for this math transform.
     */
    public String toString() {
        return toString(getMatrix());
    }
    
    /**
     * Returns the WKT for an affine transform
     * using the specified matrix.
     */
    static String toString(final Matrix matrix) {
        final int numRow = matrix.getNumRow();
        final int numCol = matrix.getNumCol();
        final StringBuffer buffer = paramMT("Affine");
        final StringBuffer eltBuf = new StringBuffer("elt_");
        addParameter(buffer, "num_row", numRow);
        addParameter(buffer, "num_col", numCol);
        for (int j=0; j<numRow; j++) {
            for (int i=0; i<numCol; i++) {
                final double value = matrix.getElement(j,i);
                if (value != (i==j ? 1 : 0)) {
                    eltBuf.setLength(4);
                    eltBuf.append(j);
                    eltBuf.append('_');
                    eltBuf.append(i);
                    addParameter(buffer, eltBuf.toString(), value);
                }
            }
        }
        buffer.append(']');
        return buffer.toString();
    }
    
    /**
     * The provider for {@link MatrixTransform}.
     *
     * @version $Id: MatrixTransform.java,v 1.7 2003/04/14 21:08:14 desruisseaux Exp $
     * @author Martin Desruisseaux
     */
    static final class Provider extends MathTransformProvider {
        /**
         * Create a provider for affine transform.
         * The default matrix size is 4&times;4.
         */
        public Provider() {
            super("Affine", ResourceKeys.AFFINE_TRANSFORM, null);
            final int defaultSize = MatrixParameters.DEFAULT_SIZE.intValue();
            putInt("num_row", defaultSize, MatrixParameters.POSITIVE_RANGE);
            putInt("num_col", defaultSize, MatrixParameters.POSITIVE_RANGE);
        }
    
        /**
         * Returns a newly created parameter list. This custom parameter list
         * is different from the default one in that it is "extensible", i.e.
         * new parameters may be added if the matrix's size growth.
         */
        public ParameterList getParameterList() {
            return new MatrixParameters();
        }
        
        /**
         * Returns a transform for the specified parameters.
         *
         * @param  parameters The parameter values in standard units.
         * @return A {@link MathTransform} object of this classification.
         *
         * @task REVISIT: Should we invoke {@link MathTransformFactory#createAffineTransform}
         *       instead? It would force us to keep a reference to {@link MathTransformFactory}
         *       (and not forget to change the reference if this provider is copied into an
         *       other factory)...
         */
        public MathTransform create(final ParameterList parameters) {
            final Matrix matrix = MatrixParameters.getMatrix(parameters);
            if (matrix.isAffine()) {
                switch (matrix.getNumRow()) {
                    case 3: return new AffineTransform2D(matrix.toAffineTransform2D());
                    case 2: return LinearTransform1D.create(matrix.getElement(0,0),
                                                            matrix.getElement(0,1));
                }
            }
            if (matrix.isIdentity()) {
                // The 1D and 2D cases have their own optimized identity transform,
                // which is why this test must come after the 'isAffine()' test.
                return new IdentityTransform(matrix.getNumRow()-1);
            }
            return new MatrixTransform(matrix);
        }
    }
}
