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
 *
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package org.geotools.ct;

// J2SE dependencies
import java.io.Serializable;
import java.awt.geom.Point2D;

// Geotools dependencies
import org.geotools.pt.Matrix;
import org.geotools.pt.CoordinatePoint;

// Resources
import org.geotools.resources.Utilities;
import org.geotools.resources.cts.Resources;
import org.geotools.resources.cts.ResourceKeys;


/**
 * Base class for concatenated transform. Concatenated transforms are
 * serializable if all their step transforms are serializables.
 *
 * @version $Id: ConcatenatedTransform.java,v 1.5 2003/05/13 10:58:48 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
class ConcatenatedTransform extends AbstractMathTransform implements Serializable {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 5772066656987558634L;
    
    /**
     * The math transform factory that created this concatenated transform.
     * Will be used for creating the inverse transform when needed. This
     * field is usually not null, except after deserialization. It can't
     * be serialized since {@link MathTransformFactory} are usually not
     * serializable.
     *
     * @task REVISIT: Not serializing this field may not be the right thing to do if
     *                a user wants to work with a custom {@link MathTransformFactory}.
     */
    private transient MathTransformFactory provider;
    
    /**
     * The first math transform.
     */
    protected final MathTransform transform1;
    
    /**
     * The second math transform.
     */
    protected final MathTransform transform2;
    
    /**
     * The inverse transform. This field
     * will be computed only when needed.
     */
    private transient MathTransform inverse;
    
    /**
     * Construct a concatenated transform. This constructor is for subclasses only. To
     * create a concatenated transform, use the factory method {@link #create} instead.
     *
     * @param provider   The math transform factory that created this concatenated transform.
     * @param transform1 The first math transform.
     * @param transform2 The second math transform.
     */
    protected ConcatenatedTransform(final MathTransformFactory provider,
                                    final MathTransform transform1,
                                    final MathTransform transform2)
    {
        this.provider   = provider;
        this.transform1 = transform1;
        this.transform2 = transform2;
        if (!isValid()) {
            throw new IllegalArgumentException(Resources.format(
                    ResourceKeys.ERROR_CANT_CONCATENATE_CS_$2,
                    getName(transform1), getName(transform2)));
        }
    }

    /**
     * Construct a new concatenated transform.  This factory method checks for step transforms
     * dimension. The returned transform will implements {@link MathTransform2D} if source and
     * target dimensions are equal to 2.  Likewise, it will implements {@link MathTransform1D}
     * if source and target dimensions are equal to 1.  {@link MathTransform} implementations
     * are available in two version: direct and non-direct. The "non-direct" version use an
     * intermediate buffer when performing transformations; they are slower and consume more
     * memory. They are used only as a fallback when a "direct" version can't be created.
     *
     * @param provider  The math transform factory that created this concatenated transform.
     * @param tr1       The first math transform.
     * @param tr2       The second math transform.
     * @return          The concatenated transform.
     */
    public static ConcatenatedTransform create(final MathTransformFactory provider,
                                               final MathTransform tr1,
                                               final MathTransform tr2)
    {
        final int dimSource = tr1.getDimSource();
        final int dimTarget = tr2.getDimTarget();
        //
        // Check if the result need to be a MathTransform1D.
        //
        if (dimSource==1 && dimTarget==1) {
            if (tr1 instanceof MathTransform1D && tr2 instanceof MathTransform1D) {
                return new ConcatenatedTransformDirect1D(provider, (MathTransform1D)tr1,
                                                                   (MathTransform1D)tr2);
            } else {
                return new ConcatenatedTransform1D(provider, tr1, tr2);
            }
        } else
        //
        // Check if the result need to be a MathTransform2D.
        //
        if (dimSource==2 && dimTarget==2) {
            if (tr1 instanceof MathTransform2D && tr2 instanceof MathTransform2D) {
                return new ConcatenatedTransformDirect2D(provider, (MathTransform2D)tr1,
                                                                   (MathTransform2D)tr2);
            } else {
                return new ConcatenatedTransform2D(provider, tr1, tr2);
            }
        } else
        //
        // Check for the general case.
        //
        if (dimSource==tr1.getDimTarget() && tr2.getDimSource()==dimTarget) {
            return new ConcatenatedTransformDirect(provider, tr1, tr2);
        } else {
            return new ConcatenatedTransform(provider, tr1, tr2);
        }
    }
    
    /**
     * Returns a name for the specified coordinate system.
     */
    private static final String getName(final MathTransform transform) {
        if (transform instanceof AbstractMathTransform) {
            String name = ((AbstractMathTransform) transform).getName(null);
            if (name!=null && (name=name.trim()).length()!=0) {
                return name;
            }
        }
        return Utilities.getShortClassName(transform);
    }
    
    /**
     * Check if transforms are compatibles. The default
     * implementation check if transfert dimension match.
     */
    protected boolean isValid() {
        return transform1.getDimTarget() == transform2.getDimSource();
    }
    
    /**
     * Gets the dimension of input points.
     */
    public final int getDimSource() {
        return transform1.getDimSource();
    }
    
    /**
     * Gets the dimension of output points.
     */
    public final int getDimTarget() {
        return transform2.getDimTarget();
    }
    
    /**
     * Transforms the specified <code>ptSrc</code> and stores the result in <code>ptDst</code>.
     */
    public CoordinatePoint transform(final CoordinatePoint ptSrc,
                                           CoordinatePoint ptDst)
        throws TransformException
    {
        assert isValid();
        //  Note: If we know that the transfert dimension is the same than source
        //        and target dimension, then we don't need to use an intermediate
        //        point. This optimization is done in ConcatenatedTransformDirect.
        return transform2.transform(transform1.transform(ptSrc, null), ptDst);
    }
    
    /**
     * Transforms a list of coordinate point ordinal values.
     */
    public void transform(final double[] srcPts, final int srcOff,
                          final double[] dstPts, final int dstOff, final int numPts)
        throws TransformException
    {
        assert isValid();
        //  Note: If we know that the transfert dimension is the same than source
        //        and target dimension, then we don't need to use an intermediate
        //        buffer. This optimization is done in ConcatenatedTransformDirect.
        final double[] tmp = new double[numPts*transform1.getDimTarget()];
        transform1.transform(srcPts, srcOff, tmp, 0, numPts);
        transform2.transform(tmp, 0, dstPts, dstOff, numPts);
    }
    
    /**
     * Transforms a list of coordinate point ordinal values.
     */
    public void transform(final float[] srcPts, final int srcOff,
                          final float[] dstPts, final int dstOff, final int numPts)
        throws TransformException
    {
        assert isValid();
        //  Note: If we know that the transfert dimension is the same than source
        //        and target dimension, then we don't need to use an intermediate
        //        buffer. This optimization is done in ConcatenatedTransformDirect.
        final float[] tmp = new float[numPts*transform1.getDimTarget()];
        transform1.transform(srcPts, srcOff, tmp, 0, numPts);
        transform2.transform(tmp, 0, dstPts, dstOff, numPts);
    }
    
    /**
     * Creates the inverse transform of this object.
     */
    public synchronized final MathTransform inverse() throws NoninvertibleTransformException {
        assert isValid();
        if (inverse == null) {
            if (provider == null) {
                provider = MathTransformFactory.getDefault();
            }
            inverse = provider.createConcatenatedTransform(transform2.inverse(),
                                                           transform1.inverse());
            if (inverse instanceof ConcatenatedTransform) {
                ((ConcatenatedTransform) inverse).inverse = this;
            }
        }
        return inverse;
    }
    
    /**
     * Gets the derivative of this transform at a point. We must delegate to the
     * {@link #derivative(CoordinatePoint)} version  because  the transformation
     * steps {@link #transform1} and {@link #transform2} may not be instances of
     * {@link MathTransform2D}. The only class which is allowed to delegate directly
     * to the {@link Point2D} version is {@link ConcatenatedTransformDirect2D}.
     *
     * @param  point The coordinate point where to evaluate the derivative.
     * @return The derivative at the specified point as a 2&times;2 matrix.
     * @throws TransformException if the derivative can't be evaluated at the specified point.
     */
    public Matrix derivative(final Point2D point) throws TransformException {
        return derivative(new CoordinatePoint(point));
    }
    
    /**
     * Gets the derivative of this transform at a point.
     *
     * @param  point The coordinate point where to evaluate the derivative.
     * @return The derivative at the specified point (never <code>null</code>).
     * @throws TransformException if the derivative can't be evaluated at the specified point.
     */
    public Matrix derivative(final CoordinatePoint point) throws TransformException {
        final Matrix matrix1 = transform1.derivative(point);
        final Matrix matrix2 = transform2.derivative(transform1.transform(point, null));
        // Compute "matrix = matrix2 * matrix1". Reuse an existing matrix object
        // if possible, which is always the case when both matrix are square.
        final int numRow = matrix2.getNumRow();
        final int numCol = matrix1.getNumCol();
        final Matrix matrix;
        if (numCol == matrix2.getNumCol()) {
            matrix = matrix2;
            matrix2.mul(matrix1);
        } else {
            matrix = new Matrix(numRow, numCol);
            matrix.mul(matrix2, matrix1);
        }
        return matrix;
    }
    
    /**
     * Tests whether this transform does not move any points.
     * Default implementation check if the two transforms are
     * identity. This a way too conservative aproach, but it
     * it doesn't hurt since ConcatenatedTransform should not
     * have been created if it were to result in an identity
     * transform (this case should have been detected earlier).
     */
    public final boolean isIdentity() {
        return transform1.isIdentity() && transform2.isIdentity();
    }
    
    /**
     * Returns a hash value for this transform.
     */
    public final int hashCode() {
        return transform1.hashCode() + 37*transform2.hashCode();
    }
    
    /**
     * Compares the specified object with
     * this math transform for equality.
     */
    public final boolean equals(final Object object) {
        if (object==this) {
            // Slight optimization
            return true;
        }
        if (super.equals(object)) {
            final ConcatenatedTransform that = (ConcatenatedTransform) object;
            return Utilities.equals(this.transform1, that.transform1) &&
                   Utilities.equals(this.transform2, that.transform2);
        }
        return false;
    }
    
    /**
     * Returns the WKT for this math transform.
     */
    public final String toString() {
        final StringBuffer buffer = new StringBuffer("CONCAT_MT[");
        addWKT(buffer, this, true);
        buffer.append(']');
        return buffer.toString();
    }
    
    /**
     * Append to a string buffer the WKT
     * for the specified math transform.
     */
    private static void addWKT(final StringBuffer buffer,
                               final MathTransform transform,
                               final boolean first)
    {
        if (transform instanceof ConcatenatedTransform) {
            final ConcatenatedTransform concat = (ConcatenatedTransform) transform;
            addWKT(buffer, concat.transform1, first);
            addWKT(buffer, concat.transform2, false);
        } else {
            if (!first) {
                buffer.append(", ");
            }
            buffer.append(transform);
        }
    }
}
