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

// JAI dependencies
import javax.media.jai.ParameterList;

// Geotools dependencies
import org.geotools.pt.Matrix;
import org.geotools.pt.CoordinatePoint;
import org.geotools.pt.MismatchedDimensionException;
import org.geotools.ct.MathTransform;
import org.geotools.ct.AbstractMathTransform;
import org.geotools.ct.TransformException;
import org.geotools.ct.NoninvertibleTransformException;


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
 * @version $Id: PowerTransform1D.java,v 1.1 2002/07/10 18:20:13 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
final class PowerTransform1D extends AbstractMathTransform implements MathTransform1D, Serializable
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
     * Construct a new power transform. The transformation equation is
     *
     * <code>  y = scale * base^x  </code>
     *
     * @param base   The base to be raised to a power.
     * @param scale  The scale value to be multiplied.
     */
    public PowerTransform1D(final double base, final double scale) {
        this.base   = base;
        this.scale  = scale;
        this.lnBase = Math.log(base);
    }

    /**
     * Construct a new logarithmic transform which is the
     * inverse of the supplied power transform.
     */
    PowerTransform1D(final LogarithmTransform1D inverse) {
        this.base     = inverse.base;
        this.lnBase   = inverse.lnBase;
        this.scale    = Math.pow(base, -inverse.offset);
        this.inverse  = inverse;
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
            inverse = new LogarithmTransform1D(this);
        }
        return inverse;
    }
    
    /**
     * Gets the derivative of this function at a value.
     */
    public double derivative(final double value) throws TransformException {
        return lnBase * transform(value);
    }
    
    /**
     * Transforms the specified value.
     */
    public double transform(final double value) throws TransformException {
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
     * The provider for {@link PowerTransform} and {@link LogarithmTransform}.
     *
     * @version $Id: PowerTransform1D.java,v 1.1 2002/07/10 18:20:13 desruisseaux Exp $
     * @author Martin Desruisseaux
     */
    static final class Provider extends MathTransformProvider {
        /**
         * <code>false</code> to create a provider for {@link PowerTransform}, or
         * <code>true</code> to create a provider for {@link LogarithmTransform}.
         */
        private final boolean logarithm;

        /**
         * Create a provider for power or logarithmic transforms.
         *
         * @param logarithm <code>false</code> to create a provider for
         *        {@link PowerTransform}, or <code>true</code> to create
         *        a provider for {@link LogarithmTransform}.
         */
        public Provider(final boolean logarithm) {
            super(logarithm ? "Logarithm" : "Power",
                  -1, // TODO: ResourceKeys.LOGARITHM
                  null);
            this.logarithm = logarithm;
            put("Base", 10, POSITIVE_RANGE);
            putObject("Transform", MathTransform.class);
        }
        
        /**
         * Returns a transform for the specified parameters.
         *
         * @param  parameters The parameter values.
         * @return A {@link MathTransform} object of this classification.
         */
        public MathTransform create(final ParameterList parameters) {
            final double base = parameters.getDoubleParameter("Base");
            final MathTransform tr = (MathTransform) parameters.getObjectParameter("Transform");
            if (logarithm) {
                if (tr instanceof PowerTransform1D) {
                    // TODO: implement it
                }
            } else {
                if (tr instanceof LogarithmTransform1D) {
                    // TODO: implement it
                }
            }
            if (tr instanceof LinearTransform1D) {
                final LinearTransform1D tr1 = (LinearTransform1D) tr;
                if (logarithm) {
                    final double lnBase = Math.log(base);
                    return new LogarithmTransform1D(lnBase*lnBase/Math.log(tr1.offset), -tr1.scale);
                } else {
                    return new PowerTransform1D(Math.pow(base, tr1.scale),
                                                Math.pow(base, tr1.offset));
                }
            }
            // TODO: make it more general.
            throw new UnsupportedOperationException(
                    "Only 1D linear transforms are currently supported.");
        }
    }
}
