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

// J2SE dependencies
import java.io.Serializable;

// Geotools dependencies
import org.geotools.pt.Matrix;
import org.geotools.ct.MathTransform;
import org.geotools.ct.AbstractMathTransform;
import org.geotools.ct.NoninvertibleTransformException;


/**
 * A one dimensional, linear transform. Input values are converted into output values
 * using the following equation:
 *
 * <p align="center"><code>y&nbsp;=&nbsp;{@link #offset}&nbsp;+&nbsp;{@link #scale}*x</code></p>
 *
 * This class is really a special case of {@link MatrixTransform} using a 2&times;2 affine
 * transform. However, this specialized <code>LinearTransform1D</code> class is faster.
 *
 * @version $Id: LinearTransform1D.java,v 1.3 2002/07/18 09:10:49 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
class LinearTransform1D extends AbstractMathTransform
                        implements MathTransform1D, LinearTransform, Serializable
{
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -7595037195668813000L;

    /**
     * The value which is multiplied to input values.
     */
    public final double scale;
    
    /**
     * The value to add to input values.
     */
    public final double offset;

    /**
     * The inverse of this transform. Created only when first needed.
     */
    private transient MathTransform inverse;

    /**
     * Construct a new linear transform. The transformation equation is:
     *
     * <p><code>y&nbsp;=&nbsp;{@link #offset}&nbsp;+&nbsp;{@link #scale}*x</code></p>
     *
     * @param scale  The <code>scale</code>  term in the linear equation.
     * @param offset The <code>offset</code> term in the linear equation.
     */
    protected LinearTransform1D(final double scale, final double offset) {
        this.scale   = scale;
        this.offset  = offset;
    }

    /**
     * Construct a new linear transform. The transformation equation is:
     *
     * <p><code>y&nbsp;=&nbsp;{@link #offset}&nbsp;+&nbsp;{@link #scale}*x</code></p>
     *
     * @param scale  The <code>scale</code>  term in the linear equation.
     * @param offset The <code>offset</code> term in the linear equation.
     */
    public static LinearTransform1D create(final double scale, final double offset) {
        if (scale == 0) {
            return new ConstantTransform1D(offset);
        }
        return new LinearTransform1D(scale, offset);
    }
    
    /**
     * Gets the dimension of input points, which is 1.
     */
    public int getDimSource() {
        return 1;
    }
    
    /**
     * Gets the dimension of output points, which is 1.
     */
    public int getDimTarget() {
        return 1;
    }

    /**
     * Returns this transform as an affine transform matrix.
     */
    public Matrix getMatrix() {
        return new Matrix(2, 2, new double[] {scale, offset, 0, 1});
    }
    
    /**
     * Creates the inverse transform of this object.
     */
    public MathTransform inverse() throws NoninvertibleTransformException {
        if (inverse == null) {
            if (isIdentity()) {
                inverse = this;
            } else if (scale != 0) {
                final LinearTransform1D inverse;
                inverse = create(1/scale, -offset/scale);
                inverse.inverse = this;
                this.inverse = inverse;
            } else {
                inverse = super.inverse();
            }
        }
        return inverse;
    }
    
    /**
     * Tests whether this transform does not move any points.
     */
    public boolean isIdentity() {
        return offset==0 && scale==1;
    }
    
    /**
     * Gets the derivative of this function at a value.
     */
    public double derivative(final double value) {
        return scale;
    }
    
    /**
     * Transforms the specified value.
     */
    public double transform(double value) {
        return offset + scale*value;
    }
    
    /**
     * Transforms a list of coordinate point ordinal values.
     */
    public void transform(final float[] srcPts, int srcOff,
                          final float[] dstPts, int dstOff, int numPts)
    {
        if (srcPts!=dstPts || srcOff>=dstOff) {
            while (--numPts >= 0) {
                dstPts[dstOff++] = (float) (offset + scale*srcPts[srcOff++]);
            }
        } else {
            srcOff += numPts;
            dstOff += numPts;
            while (--numPts >= 0) {
                dstPts[--dstOff] = (float) (offset + scale*srcPts[--srcOff]);
            }
        }
    }
    
    /**
     * Transforms a list of coordinate point ordinal values.
     */
    public void transform(final double[] srcPts, int srcOff,
                          final double[] dstPts, int dstOff, int numPts)
    {
        if (srcPts!=dstPts || srcOff>=dstOff) {
            while (--numPts >= 0) {
                dstPts[dstOff++] = offset + scale*srcPts[srcOff++];
            }
        } else {
            srcOff += numPts;
            dstOff += numPts;
            while (--numPts >= 0) {
                dstPts[--dstOff] = offset + scale*srcPts[--srcOff];
            }
        }
    }
    
    /**
     * Returns a hash value for this transform.
     * This value need not remain consistent between
     * different implementations of the same class.
     */
    public int hashCode() {
        long code;
        code = 78512786 + Double.doubleToLongBits(offset);
        code =  code*37 + Double.doubleToLongBits(scale);
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
            final LinearTransform1D that = (LinearTransform1D) object;
            return Double.doubleToLongBits(this.scale)  == Double.doubleToLongBits(that.scale) &&
                   Double.doubleToLongBits(this.offset) == Double.doubleToLongBits(that.offset);
        }
        return false;
    }
    
    /**
     * Returns the WKT for this math transform.
     */
    public String toString() {
        return MatrixTransform.toString(getMatrix());
    }
}
