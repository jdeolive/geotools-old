/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
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

// JAI dependencies
import javax.media.jai.util.Range;
import javax.media.jai.ParameterList;

// Geotools dependencies
import org.geotools.ct.MathTransform;
import org.geotools.ct.AbstractMathTransform;
import org.geotools.resources.cts.ResourceKeys;


/**
 * A one dimensional power transform. Input values are converted into output values
 * using the following equation:
 *
 * <p align="center"><code>y&nbsp;=&nbsp;{@link #scale}*{@link #base}<sup>x</sup></code></p>
 *
 * Reminder: This equation may be written in other form:
 *
 * <p align="center"><code>{@link #base}<sup>a&nbsp;+&nbsp;b*x</sup> =
 * {@link #base}<sup>a</sup>*({@link #base}<sup>b</sup>)<sup>x</sup></code></p>
 *
 * @version $Id: ExponentialTransform1D.java,v 1.5 2003/05/13 10:58:48 desruisseaux Exp $
 * @author Martin Desruisseaux
 *
 * @see LogarithmicTransform1D
 * @see LinearTransform1D
 */
final class ExponentialTransform1D extends AbstractMathTransform implements MathTransform1D, Serializable
{
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -6793025722241238043L;

    /**
     * The base to be raised to power.
     */
    public final double base;

    /**
     * Natural logarithm of {@link #base}.
     */
    final double lnBase;

    /**
     * The scale value to be multiplied.
     */
    public final double scale;

    /**
     * The inverse of this transform. Created only when first needed.
     */
    private transient MathTransform inverse;

    /**
     * Construct a new logarithmic transform which is the
     * inverse of the supplied power transform.
     */
    ExponentialTransform1D(final LogarithmicTransform1D inverse) {
        this.base     = inverse.base;
        this.lnBase   = inverse.lnBase;
        this.scale    = Math.pow(base, -inverse.offset);
        this.inverse  = inverse;
    }

    /**
     * Construct a new power transform. The transformation equation is
     *
     * <code>  y = scale * base^x  </code>
     *
     * @param base   The base to be raised to a power.
     * @param scale  The scale value to be multiplied.
     */
    protected ExponentialTransform1D(final double base, final double scale) {
        this.base   = base;
        this.scale  = scale;
        this.lnBase = Math.log(base);
    }

    /**
     * Construct a new power transform. The transformation equation is
     *
     * <code>  y = scale * base^x  </code>
     *
     * @param base   The base to be raised to a power.
     * @param scale  The scale value to be multiplied.
     */
    public static MathTransform1D create(final double base, final double scale) {
        if (base==0 || scale==0) {
            return LinearTransform1D.create(0, 0);
        }
        if (base==1) {
            return LinearTransform1D.create(0, scale);
        }
        return new ExponentialTransform1D(base, scale);
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
            inverse = new LogarithmicTransform1D(this);
        }
        return inverse;
    }
    
    /**
     * Gets the derivative of this function at a value.
     */
    public double derivative(final double value) {
        return lnBase * transform(value);
    }
    
    /**
     * Transforms the specified value.
     */
    public double transform(final double value) {
        return scale * Math.pow(base, value);
    }
    
    /**
     * Transforms a list of coordinate point ordinal values.
     */
    public void transform(float[] srcPts, int srcOff,
                          float[] dstPts, int dstOff, int numPts)
    {
        if (srcPts!=dstPts || srcOff>=dstOff) {
            while (--numPts >= 0) {
                dstPts[dstOff++] = (float) (scale*Math.pow(base, srcPts[srcOff++]));
            }
        } else {
            srcOff += numPts;
            dstOff += numPts;
            while (--numPts >= 0) {
                dstPts[--dstOff] = (float) (scale*Math.pow(base, srcPts[--srcOff]));
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
                dstPts[dstOff++] = scale*Math.pow(base, srcPts[srcOff++]);
            }
        } else {
            srcOff += numPts;
            dstOff += numPts;
            while (--numPts >= 0) {
                dstPts[--dstOff] = scale*Math.pow(base, srcPts[--srcOff]);
            }
        }
    }

    /**
     * Concatenates in an optimized way a {@link MathTransform} <code>other</code> to this
     * <code>MathTransform</code>. This implementation can optimize some concatenation with
     * {@link LinearTransform1D} and {@link LogarithmicTransform1D}.
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
                final double newBase  = Math.pow(base, linear.scale);
                final double newScale = Math.pow(base, linear.offset)*scale;
                if (!Double.isNaN(newBase) && !Double.isNaN(newScale)) {
                    return create(newBase, newScale);
                }
            } else {
                if (linear.offset == 0) {
                    return create(base, scale*linear.scale);
                }
            }
        } else if (other instanceof LogarithmicTransform1D) {
            return concatenateLog((LogarithmicTransform1D) other, applyOtherFirst);
        }
        return super.concatenate(other, applyOtherFirst);
    }

    /**
     * Concatenates in an optimized way a {@link LogarithmicTransform1D} <code>other</code> to this
     * <code>ExponentialTransform1D</code>.
     *
     * @param  other The math transform to apply.
     * @param  applyOtherFirst <code>true</code> if the transformation order is <code>other</code>
     *         followed by <code>this</code>, or <code>false</code> if the transformation order is
     *         <code>this</code> followed by <code>other</code>.
     * @return The combined math transform, or <code>null</code> if no optimized combined
     *         transform is available.
     */
    MathTransform concatenateLog(final LogarithmicTransform1D other, final boolean applyOtherFirst) {
        if (applyOtherFirst) {
            final double newScale = scale*Math.pow(base, other.offset);
            final double newPower = lnBase/other.lnBase;
            if (!Double.isNaN(newScale)) {
                if (newPower == 1) {
                    return LinearTransform1D.create(newScale, 0);
                }
                // TODO: Needs a transform here with the following equation:
                //
                //       y(x)  =  newScale * Math.pow(x, newPower);
            }
        } else if (scale > 0) {
            return LinearTransform1D.create(lnBase/other.lnBase,
                                   Math.log(scale)/other.lnBase + other.offset);
        }
        return null;
    }
    
    /**
     * Returns a hash value for this transform.
     * This value need not remain consistent between
     * different implementations of the same class.
     */
    public int hashCode() {
        long code;
        code = 16427632 + Double.doubleToLongBits(base);
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
            final ExponentialTransform1D that = (ExponentialTransform1D) object;
            return Double.doubleToLongBits(this.base)  == Double.doubleToLongBits(that.base) &&
                   Double.doubleToLongBits(this.scale) == Double.doubleToLongBits(that.scale);
        }
        return false;
    }
    
    /**
     * Returns the WKT for this math transform.
     */
    public String toString() {
        final StringBuffer buffer = paramMT("Exponential");
        addParameter(buffer, "base", base);
        if (scale != 1) {
            // TODO: The following is NOT a parameter. For WKT formatting, we should decompose this
            //       LogarithmicTransform1D into a ConcatenatedTransform using a AffineTransform instead.
            addParameter(buffer, "scale", scale);
        }
        buffer.append(']');
        return buffer.toString();
    }
    
    /**
     * The provider for {@link ExponentialTransform1D} and {@link LogarithmicTransform1D}.
     *
     * @version $Id: ExponentialTransform1D.java,v 1.5 2003/05/13 10:58:48 desruisseaux Exp $
     * @author Martin Desruisseaux
     */
    static final class Provider extends MathTransformProvider {
        /**
         * The range of allowed value for the "Dimension" parameter.
         * Current implementation support only one-dimensional transform.
         */
        private static final Range DIMENSION_RANGE;
        static
        {
            final Integer ONE = new Integer(1);
            DIMENSION_RANGE = new Range(Integer.class, ONE, true, ONE, true);
        }

        /**
         * <code>false</code> to create a provider for {@link ExponentialTransform1D}, or
         * <code>true</code> to create a provider for {@link LogarithmicTransform1D}.
         */
        private final boolean logarithm;

        /**
         * Create a provider for power or logarithmic transforms.
         *
         * @param logarithm <code>false</code> to create a provider for
         *        {@link ExponentialTransform1D}, or <code>true</code> to create
         *        a provider for {@link LogarithmicTransform1D}.
         */
        public Provider(final boolean logarithm) {
            super(logarithm ? "Logarithmic" : "Exponential",
                  logarithm ? ResourceKeys.LOGARITHM : ResourceKeys.EXPONENTIAL,
                  null);
            this.logarithm = logarithm;
            put("base", 10, POSITIVE_RANGE);
            putInt("dimension", 1, DIMENSION_RANGE);
        }
        
        /**
         * Returns a transform for the specified parameters.
         *
         * @param  parameters The parameter values.
         * @return A {@link MathTransform} object of this classification.
         */
        public MathTransform create(final ParameterList parameters) {
            final double   base = parameters.getDoubleParameter("base");
            final int dimension = parameters.getIntParameter("dimension");
            if (dimension == 1) {
                if (logarithm) {
                    return new LogarithmicTransform1D(base, 0);
                } else {
                    return ExponentialTransform1D.create(base, 1);
                }
            }
            // TODO: make it more general.
            throw new UnsupportedOperationException("Only 1D transforms are currently supported.");
        }
    }
}
