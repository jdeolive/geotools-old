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

// Geotools dependencies
import org.geotools.pt.Matrix;
import org.geotools.pt.CoordinatePoint;
import org.geotools.pt.MismatchedDimensionException;
import org.geotools.resources.Utilities;

// J2SE dependencies
import java.io.Serializable;


/**
 * Transform which passes through a subset of ordinates to another transform.
 * This allows transforms to operate on a subset of ordinates.  For example,
 * if you have (<var>latitude</var>,<var>longitude</var>,<var>height</var>)
 * coordinates, then you may wish to convert the height values from feet to
 * meters without affecting the latitude and longitude values.
 *
 * @version 1.00
 * @author OpenGIS (www.opengis.org)
 * @author Martin Desruisseaux
 */
final class PassThroughTransform extends AbstractMathTransform implements Serializable {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -1673997634240223449L;
    
    /**
     * Index of the first affected ordinate.
     */
    protected final int firstAffectedOrdinate;
    
    /**
     * Number of unaffected ordinates after the affected ones.
     * Always 0 when used through the strict OpenGIS API.
     */
    protected final int numTrailingOrdinates;
    
    /**
     * The sub transform.
     */
    protected final MathTransform transform;
    
    /**
     * The inverse transform. This field
     * will be computed only when needed.
     */
    private transient PassThroughTransform inverse;
    
    /**
     * Create a pass through transform.
     *
     * @param firstAffectedOrdinate Index of the first affected ordinate.
     * @param transform The sub transform.
     * @param numTrailingOrdinates Number of trailing ordinates to pass through.
     *        Affected ordinates will range from <code>firstAffectedOrdinate</code>
     *        inclusive to <code>dimTarget-numTrailingOrdinates</code> exclusive.
     */
    public PassThroughTransform(final int firstAffectedOrdinate,
                                final MathTransform transform,
                                final int numTrailingOrdinates)
    {
        if (transform instanceof PassThroughTransform) {
            final PassThroughTransform passThrough = (PassThroughTransform) transform;
            this.firstAffectedOrdinate = passThrough.firstAffectedOrdinate + firstAffectedOrdinate;
            this.numTrailingOrdinates  = passThrough.numTrailingOrdinates  + numTrailingOrdinates;
            this.transform             = passThrough.transform;
        }  else {
            this.firstAffectedOrdinate = firstAffectedOrdinate;
            this.numTrailingOrdinates  = numTrailingOrdinates;
            this.transform             = transform;
        }
    }
    
    /**
     * Gets the dimension of input points.
     */
    public int getDimSource() {
        return firstAffectedOrdinate + transform.getDimSource() + numTrailingOrdinates;
    }
    
    /**
     * Gets the dimension of output points.
     */
    public int getDimTarget() {
        return firstAffectedOrdinate + transform.getDimTarget() + numTrailingOrdinates;
    }
    
    /**
     * Tests whether this transform does not move any points.
     */
    public boolean isIdentity() {
        return transform.isIdentity();
    }
    
    /**
     * Transforms a list of coordinate point ordinal values.
     */
    public void transform(final float[] srcPts, int srcOff,
                          final float[] dstPts, int dstOff, int numPts)
        throws TransformException
    {
        final int subDimSource = transform.getDimSource();
        final int subDimTarget = transform.getDimTarget();
        int srcStep = numTrailingOrdinates;
        int dstStep = numTrailingOrdinates;
        if (srcPts==dstPts && srcOff<dstOff) {
            final int dimSource = getDimSource();
            final int dimTarget = getDimTarget();
            srcOff += numPts * dimSource;
            dstOff += numPts * dimTarget;
            srcStep -= 2*dimSource;
            dstStep -= 2*dimTarget;
        }
        while (--numPts >= 0) {
            System.arraycopy   (srcPts, srcOff,                        dstPts, dstOff,              firstAffectedOrdinate);
            transform.transform(srcPts, srcOff+=firstAffectedOrdinate, dstPts, dstOff+=firstAffectedOrdinate,           1);
            System.arraycopy   (srcPts, srcOff+=subDimSource,          dstPts, dstOff+=subDimTarget, numTrailingOrdinates);
            srcOff += srcStep;
            dstOff += dstStep;
        }
    }
    
    /**
     * Transforms a list of coordinate point ordinal values.
     */
    public void transform(final double[] srcPts, int srcOff,
                          final double[] dstPts, int dstOff, int numPts)
        throws TransformException
    {
        final int subDimSource = transform.getDimSource();
        final int subDimTarget = transform.getDimTarget();
        int srcStep = numTrailingOrdinates;
        int dstStep = numTrailingOrdinates;
        if (srcPts==dstPts && srcOff<dstOff) {
            final int dimSource = getDimSource();
            final int dimTarget = getDimTarget();
            srcOff += numPts * dimSource;
            dstOff += numPts * dimTarget;
            srcStep -= 2*dimSource;
            dstStep -= 2*dimTarget;
        }
        while (--numPts >= 0) {
            System.arraycopy   (srcPts, srcOff,                        dstPts, dstOff,              firstAffectedOrdinate);
            transform.transform(srcPts, srcOff+=firstAffectedOrdinate, dstPts, dstOff+=firstAffectedOrdinate,           1);
            System.arraycopy   (srcPts, srcOff+=subDimSource,          dstPts, dstOff+=subDimTarget, numTrailingOrdinates);
            srcOff += srcStep;
            dstOff += dstStep;
        }
    }
    
    /**
     * Gets the derivative of this transform at a point.
     */
    public Matrix derivative(final CoordinatePoint point) throws TransformException {
        final int nSkipped = firstAffectedOrdinate + numTrailingOrdinates;
        final int transDim = transform.getDimSource();
        final int pointDim = point.getDimension();
        if (pointDim != transDim+nSkipped) {
            throw new MismatchedDimensionException(pointDim, transDim+nSkipped);
        }
        final CoordinatePoint subPoint = new CoordinatePoint(transDim);
        System.arraycopy(point.ord, firstAffectedOrdinate, subPoint.ord, 0, transDim);
        return expand(transform.derivative(subPoint),
                      firstAffectedOrdinate, numTrailingOrdinates, 0);
    }

    /**
     * Create a pass through transform from a matrix. This method is invoked when the
     * sub-transform can be express as a matrix. It is also invoked for computing the
     * matrix returned by {@link #derivative}.
     *
     * @param subMatrix The sub-transform as a matrix.
     * @param firstAffectedOrdinate Index of the first affected ordinate.
     * @param numTrailingOrdinates Number of trailing ordinates to pass through.
     * @param affine 0 if the matrix do not contains translation terms, or 1 if
     *        the matrix is an affine transform with translation terms.
     */
    static Matrix expand(final Matrix subMatrix,
                         final int firstAffectedOrdinate,
                         final int numTrailingOrdinates,
                         final int affine)
    {
        final int  nSkipped = firstAffectedOrdinate + numTrailingOrdinates;
        final int    numRow = subMatrix.getNumRow() - affine;
        final int    numCol = subMatrix.getNumCol() - affine;
        final Matrix matrix = new Matrix(numRow + nSkipped + affine,
                                         numCol + nSkipped + affine);
        matrix.setZero();

        //  Set UL part to 1:   [ 1  0             ]
        //                      [ 0  1             ]
        //                      [                  ]
        //                      [                  ]
        //                      [                  ]
        for (int j=0; j<firstAffectedOrdinate; j++) {
            matrix.setElement(j, j, 1);
        }
        //  Set central part:   [ 1  0  0  0  0  0 ]
        //                      [ 0  1  0  0  0  0 ]
        //                      [ 0  0  ?  ?  ?  0 ]
        //                      [ 0  0  ?  ?  ?  0 ]
        //                      [                  ]
        subMatrix.copySubMatrix(0, 0, numRow, numCol,
                                firstAffectedOrdinate, firstAffectedOrdinate, matrix);
        
        //  Set LR part to 1:   [ 1  0  0  0  0  0 ]
        //                      [ 0  1  0  0  0  0 ]
        //                      [ 0  0  ?  ?  ?  0 ]
        //                      [ 0  0  ?  ?  ?  0 ]
        //                      [ 0  0  0  0  0  1 ]
        final int offset = numCol-numRow;
        final int numRowOut = numRow + nSkipped;
        for (int j=numRowOut-numTrailingOrdinates; j<numRowOut; j++) {
            matrix.setElement(j, j+offset, 1);
        }
        if (affine != 0) {
            // Copy the translation terms in the last column.
            subMatrix.copySubMatrix(0, numCol, numRow, affine,
                                    firstAffectedOrdinate, numCol+nSkipped, matrix);
            // Copy the last row as a safety, but it should contains only 0.
            subMatrix.copySubMatrix(numRow, 0, affine, numCol,
                                    numRow+nSkipped, firstAffectedOrdinate, matrix);
            // Copy the lower right corner, which should contains only 1.
            subMatrix.copySubMatrix(numRow, numCol, affine, affine,
                                    numRow+nSkipped, numCol+nSkipped, matrix);
        }
        return matrix;
    }
    
    /**
     * Creates the inverse transform of this object.
     */
    public synchronized MathTransform inverse() throws NoninvertibleTransformException {
        if (inverse==null) {
            inverse = new PassThroughTransform(firstAffectedOrdinate,
                                               transform.inverse(),
                                               numTrailingOrdinates);
            inverse.inverse = this;
        }
        return inverse;
    }
    
    /**
     * Compares the specified object with
     * this math transform for equality.
     */
    public boolean equals(final Object object) {
        if (object==this) {
            return true;
        }
        if (super.equals(object)) {
            final PassThroughTransform that = (PassThroughTransform) object;
            return this.firstAffectedOrdinate == that.firstAffectedOrdinate &&
                   this.numTrailingOrdinates  == that.numTrailingOrdinates  &&
                   Utilities.equals(this.transform, that.transform);
        }
        return false;
    }
    
    /**
     * Returns the WKT for this math transform.
     */
    public String toString() {
        final StringBuffer buffer = new StringBuffer("PASSTHROUGH_MT[");
        buffer.append(firstAffectedOrdinate);
        buffer.append(',');
        if (numTrailingOrdinates!=0) {
            // TODO: This parameter is not part of OpenGIS specification!
            //       We should returns a more complex WKT here, using an
            //       affine transform to change the coordinates order.
            buffer.append(numTrailingOrdinates);
            buffer.append(',');
        }
        buffer.append(transform);
        buffer.append(']');
        return buffer.toString();
    }
}
