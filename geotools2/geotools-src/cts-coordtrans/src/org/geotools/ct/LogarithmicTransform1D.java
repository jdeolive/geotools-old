/*
 * Geotools - OpenSource mapping toolkit
 * (C) 2002, Centre for Computational Geography
 * (C) 2002, Institut de Recherche pour le Développement
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
import org.geotools.pt.CoordinatePoint;
import org.geotools.pt.MismatchedDimensionException;
import org.geotools.ct.MathTransform;
import org.geotools.ct.AbstractMathTransform;
import org.geotools.ct.TransformException;
import org.geotools.ct.NoninvertibleTransformException;


/**
 * A one dimensional, logarithmic transform. Input values are converted into output values
 * using the following equation:
 *
 * <p align="center"><code>y&nbsp;=&nbsp;{@link #offset}+log<sub>{@link #base}</sub>x</code></p>
 *
 * This transform is the inverse of {@link ExponentialTransform1D}.
 *
 * @version $Id: LogarithmicTransform1D.java,v 1.1 2002/07/15 18:28:43 desruisseaux Exp $
 * @author Martin Desruisseaux
 *
 * @see ExponentialTransform1D
 * @see LinearTransform1D
 */
final class LogarithmicTransform1D extends AbstractMathTransform implements MathTransform1D, Serializable
{
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 1402978401206564931L;

    /**
     * The base of the logarithm.
     */
    public final double base;

    /**
     * Natural logarithm of {@link #base}.
     */
    final double lnBase;

    /**
     * The offset to add to the logarithm.
     */
    public final double offset;

    /**
     * The inverse of this transform. Created only when first needed.
     */
    private transient MathTransform inverse;

    /**
     * Construct a new logarithmic transform which is the
     * inverse of the supplied power transform.
     */
    LogarithmicTransform1D(final ExponentialTransform1D inverse) {
        this.base    = inverse.base;
        this.lnBase  = inverse.lnBase;
        this.offset  = -Math.log(inverse.scale)/lnBase;
        this.inverse = inverse;
    }

    /**
     * Construct a new logarithmic transform. The transformation equation is
     *
     * <code>  y = offset + log_base(x)  </code>
     *
     * @param base    The base of the logarithm.
     * @param offset  The offset to add to the logarithm.
     */
    protected LogarithmicTransform1D(final double base, final double offset) {
        this.base    = base;
        this.offset  = offset;
        this.lnBase  = Math.log(base);
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
     * Creates the inverse transform of this object.
     */
    public MathTransform inverse() {
        if (inverse == null) {
            inverse = new ExponentialTransform1D(this);
        }
        return inverse;
    }
    
    /**
     * Gets the derivative of this function at a value.
     */
    public double derivative(final double value) throws TransformException {
        return 1 / (lnBase * value);
    }
    
    /**
     * Transforms the specified value.
     */
    public double transform(final double value) throws TransformException {
        return Math.log(value)/lnBase + offset;
    }

    /**
     * Transforms a list of coordinate point ordinal values.
     */
    public void transform(final float[] srcPts, int srcOff,
                          final float[] dstPts, int dstOff, int numPts)
    {
        if (srcPts!=dstPts || srcOff>=dstOff) {
            while (--numPts >= 0) {
                dstPts[dstOff++] = (float) (Math.log(srcPts[srcOff++])/lnBase + offset);
            }
        } else {
            srcOff += numPts;
            dstOff += numPts;
            while (--numPts >= 0) {
                dstPts[--dstOff] = (float) (Math.log(srcPts[srcOff++])/lnBase + offset);
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
                dstPts[dstOff++] = Math.log(srcPts[srcOff++])/lnBase + offset;
            }
        } else {
            srcOff += numPts;
            dstOff += numPts;
            while (--numPts >= 0) {
                dstPts[--dstOff] = Math.log(srcPts[srcOff++])/lnBase + offset;
            }
        }
    }

    /**
     * Concatenates in an optimized way a {@link MathTransform} <code>other</code> to this
     * <code>MathTransform</code>. This implementation can optimize some concatenation with
     * {@link LinearTransform1D} and {@link ExponentialTransform1D}.
     *
     * @param  other The math transform to apply.
     * @param  applyOtherFirst <code>true</code> if the transformation order is <code>other</code>
     *         followed by <code>this</code>, or <code>false</code> if the transformation order is
     *         <code>this</code> followed by <code>other</code>.
     * @return The combined math transform, or <code>null</code> if no optimized combined
     *         transform is available.
     */
    MathTransform concatenate(final MathTransform other, final boolean applyOtherFirst) {
        if (other instanceof LinearTransform) {
            final LinearTransform1D linear = (LinearTransform1D) other;
            if (applyOtherFirst) {
                if (linear.offset==0 && linear.scale>0) {
                    return new LogarithmicTransform1D(base, Math.log(linear.scale)/lnBase+offset);
                }
            } else {
                final double newBase = Math.pow(base, 1/linear.scale);
                if (!Double.isNaN(newBase)) {
                    return new LogarithmicTransform1D(newBase, linear.scale*offset + linear.offset);
                }
            }
        } else if (other instanceof ExponentialTransform1D) {
            return ((ExponentialTransform1D) other).concatenateLog(this, !applyOtherFirst);
        }
        return super.concatenate(other, applyOtherFirst);
    }
    
    /**
     * Returns a hash value for this transform.
     * This value need not remain consistent between
     * different implementations of the same class.
     */
    public int hashCode() {
        long code;
        code = 75493004 + Double.doubleToLongBits(base);
        code =  code*37 + Double.doubleToLongBits(offset);
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
            final LogarithmicTransform1D that = (LogarithmicTransform1D) object;
            return Double.doubleToLongBits(this.base)   == Double.doubleToLongBits(that.base) &&
                   Double.doubleToLongBits(this.offset) == Double.doubleToLongBits(that.offset);
        }
        return false;
    }
    
    /**
     * Returns the WKT for this math transform.
     */
    public String toString() {
        final StringBuffer buffer = paramMT("Logarithmic");
        addParameter(buffer, "base", base);
        if (offset != 0) {
            // TODO: The following is NOT a parameter. For WKT formatting, we should decompose this
            //       LogarithmicTransform1D into a ConcatenatedTransform using a AffineTransform instead.
            addParameter(buffer, "offset", offset);
        }
        buffer.append(']');
        return buffer.toString();
    }
}
