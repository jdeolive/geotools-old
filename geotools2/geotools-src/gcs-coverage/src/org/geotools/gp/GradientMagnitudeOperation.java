/*
 * Geotools - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Management Committee (PMC)
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
package org.geotools.gp;

// J2SE dependencies
import java.awt.Color;
import java.awt.geom.AffineTransform;

// Java Advanced Imaging
import javax.media.jai.KernelJAI;
import javax.media.jai.ParameterList;
import javax.media.jai.ParameterListDescriptor;
import javax.media.jai.ParameterListDescriptorImpl;

// OpenGIS dependencies
import org.geotools.cv.Category;
import org.geotools.gc.GridCoverage;
import org.geotools.ct.MathTransform1D;
import org.geotools.ct.MathTransform2D;
import org.geotools.cs.CoordinateSystem;
import org.geotools.util.NumberRange;

// Resources
import org.geotools.units.Unit;
import org.geotools.units.UnitException;
import org.geotools.resources.XAffineTransform;


/**
 * An operation for gradient magnitude.  This operation is similar
 * to the JAI's operation "GradientMagnitude", but the kernels are
 * normalized is such a way that the resulting gradients are closer
 * to "geophysics" measurements. The normalization include dividing
 * by the distance between pixels.
 *
 * @version $Id: GradientMagnitudeOperation.java,v 1.4 2003/08/04 19:07:22 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
final class GradientMagnitudeOperation extends OperationJAI {
    /**
     * Set to <code>true</code> for enabling some tracing code.
     */
    private static final boolean DEBUG = false;

    /**
     * Set to <code>true</code> to enable automatic kernel normalization. Normalization modifies
     * kernel coefficients according the "grid to coordinate system" transform in order to get
     * some meaningful engineering units (e.g. °C/km). The normalization factor is computed by
     * testing the original kernels against synthetic horizontal and vertical gradients of
     * 1 sampleUnit/csUnit.
     */
    private static final boolean NORMALIZE = true;

    /**
     * The default scale factor to apply on the range computed by
     * {@link #deriveCategory}. For example a value of 0.25 means
     * that only values from 0 to 25% of the expected maximum will
     * appears in different colors. This factor is ignored if the
     * user provided an explicit "TargetRange" argument.
     */
    private static final double DEFAULT_RANGE_SCALE = 0.25;
    
    /**
     * The default color palette for the gradients.
     */
    private static final Color[] DEFAULT_COLOR_PALETTE = new Color[] {
        new Color( 16, 32, 64),
        new Color(192,224,255)
    };
    
    /**
     * A flag indicating that {@link #getNormalizationFactorSquared}
     * should test the horizontal gradient computed by the supplied kernel.
     */
    private static final int HORIZONTAL = 1;
    
    /**
     * A flag indicating that {@link #getNormalizationFactorSquared}
     * should test the vertical gradient computed by the supplied kernel.
     */
    private static final int VERTICAL = 2;
    
    /**
     * Construct a default gradient magnitude operation.
     */
    public GradientMagnitudeOperation() {
        super(null, getOperationDescriptor("GradientMagnitude"), new ParameterListDescriptorImpl(
              null,         // the object to be reflected upon for enumerated values.
              new String[]  // the names of each parameter.
              {
                  "Source",
                  "Mask1",
                  "Mask2",
                  "TargetRange"
              },
              new Class[]   // the class of each parameter.
              {
                  GridCoverage.class,
                  KernelJAI.class,
                  KernelJAI.class,
                  RangeSpecifier.class
              },
              new Object[] // The default values for each parameter.
              {
                  ParameterListDescriptor.NO_PARAMETER_DEFAULT,
                  KernelJAI.GRADIENT_MASK_SOBEL_HORIZONTAL,
                  KernelJAI.GRADIENT_MASK_SOBEL_VERTICAL,
                  null  // Automatic
              },
              null // Defines the valid values for each parameter.
            ));
    }
    
    /**
     * Returns a scale factor for the supplied kernel. If <code>kernel</code>
     * compute horizontal grandient, this method returns <code>scaleX</code>.
     * Otherwise, if <code>kernel</code> compute vertical gradient, then this
     * method returns <code>scaleY</code>. Otherwise, returns a geometric
     * combinaison of both.
     */
    private static double getScaleFactor(final KernelJAI kernel, double scaleX, double scaleY) {
        scaleX *= scaleX;
        scaleY *= scaleY;
        double factorX = getNormalizationFactorSquared(kernel, HORIZONTAL);
        double factorY = getNormalizationFactorSquared(kernel, VERTICAL);
        double factor2 = (factorX*scaleX + factorY*scaleY) / (factorX+factorY);
        return Math.sqrt(factor2);
    }
    
    /**
     * Returns the square of a normalization factor for the supplied kernel.
     * The kernel can be normalized by invoking {@link #divide(KernelJAI,double)}
     * with the square root of this value.
     *
     * @param  kernel The kernel for which to compute normalization factor.
     * @param  type Any combinaison of {@link #HORIZONTAL} and {@link #VERTICAL}.
     * @return The square of a normalization factor that could be applied
     *         on the kernel.
     */
    private static double getNormalizationFactorSquared(final KernelJAI kernel, final int type) {
        double sumH = 0;
        double sumV = 0;
        final int width  = kernel.getWidth();
        final int height = kernel.getHeight();
        /*
         * Test the kernel  with a horizontal gradient       [ -1   0   1 ]
         * of 1/pixel. For example, we get sumH=8 with       [ -2   0   2 ]
         * the horizontal Sobel kernel show on right:        [ -1   0   1 ]
         */
        if ((type & HORIZONTAL) != 0) {
            int value = kernel.getYOrigin();
            for (int y=height; --y>=0;) {
                for (int x=width; --x>=0;) {
                    sumH += value * kernel.getElement(x,y);
                }
                value--;
            }
        }
        /*
         * Test the kernel  with a vertical gradient of      [ -1  -2  -1 ]
         * 1/pixel. For example, we get sumV=8 with the      [  0   0   0 ]
         * vertical Sobel kernel show on right:              [  1   2   1 ]
         */
        if ((type & VERTICAL) != 0) {
            int value = kernel.getXOrigin();
            for (int x=width; --x>=0;) {
                for (int y=height; --y>=0;) {
                    sumV += value * kernel.getElement(x,y);
                }
                value--;
            }
        }
        return (sumH*sumH) + (sumV*sumV);
    }
    
    /**
     * Returns the normalization factor for the supplied kernel. The kernel
     * can be normalized by invoking {@link #divide(KernelJAI,double)} with
     * this factor.
     *
     * @param  mask1 The first kernel for which to compute a normalization factor.
     * @param  mask2 The second kernel for which to compute a normalization factor.
     * @return The normalization factor that could be applied on both kernels.
     *
     * @task REVISIT: When the masks are symetric (e.g. Sobel, Prewitt (or Smoothed), isotropic,
     *       etc.), then this algorithm matches the "normalization factor" times "spatial factor"
     *       provided by
     *
     *       J.J. Simpson (1990), "On the accurate detection and enhancement of oceanic features
     *       observed in satellite data" in Remote sensing environment, 33:17-33.
     *
     *       However, for non-symetric masks (e.g. Kirsch), then a difference is found.
     *       We should provides a way to disable normalization when the user did it himself
     *       according some other rules than the one used here.
     */
    private static double getNormalizationFactor(final KernelJAI mask1, final KernelJAI mask2) {
        double factor;
        factor  = getNormalizationFactorSquared(mask1, HORIZONTAL|VERTICAL);
        factor += getNormalizationFactorSquared(mask2, HORIZONTAL|VERTICAL);
        factor  = Math.sqrt(factor/2);
        return factor;
    }
    
    /**
     * Divide a kernel by some number.
     *
     * @param  kernel The kernel to divide.
     * @param  denominator The factor to divide by.
     * @return The resulting kernel.
     */
    private static KernelJAI divide(KernelJAI kernel, final double denominator) {
        if (denominator != 1) {
            final float[] data = kernel.getKernelData();
            for (int i=0; i<data.length; i++) {
                data[i] /= denominator;
            }
            kernel = new KernelJAI(kernel.getWidth(),   kernel.getHeight(),
            kernel.getXOrigin(), kernel.getYOrigin(), data);
        }
        return kernel;
    }
    
    /**
     * Apply the operation on grid coverage. Default implementation looks for kernels
     * in the parameter list and divide kernel by the distance between pixel, in the
     * grid coverage's coordinate system.
     */
    protected GridCoverage deriveGridCoverage(final GridCoverage[] sources,
                                              final Parameters  parameters)
    {
        if (NORMALIZE) {
            final ParameterList block = parameters.parameters;
            KernelJAI mask1 = (KernelJAI) block.getObjectParameter("Mask1");
            KernelJAI mask2 = (KernelJAI) block.getObjectParameter("Mask2");
            /*
             * Normalize the kernel in such a way that pixel values likes
             * [-2 -1 0 +1 +2] will give a gradient of about 1 unit/pixel.
             */
            double factor = getNormalizationFactor(mask1, mask2);
            if (!(factor > 0)) {
                // Do not transform if factor is 0 or NaN.
                factor = 1;
            }
            /*
             * Compute a scale factor taking in account the transformation from
             * grid to coordinate system. This scale will convert gradient from
             * 1 unit/pixel to 1 unit/meters or 1 unit/degrees, depending the
             * coordinate systems axis unit.
             */
            double scaleMask1 = 1;
            double scaleMask2 = 1;
            if (sources.length != 0) {
                final MathTransform2D mtr;
                mtr = sources[0].getGridGeometry().getGridToCoordinateSystem2D();
                if (mtr instanceof AffineTransform) {
                    final AffineTransform tr = (AffineTransform) mtr;
                    final double scaleX = XAffineTransform.getScaleX0(tr);
                    final double scaleY = XAffineTransform.getScaleY0(tr);
                    scaleMask1 = getScaleFactor(mask1, scaleX, scaleY);
                    scaleMask2 = getScaleFactor(mask2, scaleX, scaleY);
                    if (!(scaleMask1>0 && scaleMask2>0)) {
                        // Do not scale if scale is 0 or NaN.
                        scaleMask1 = 1;
                        scaleMask2 = 1;
                    }
                    if (DEBUG) {
                        System.out.print("factor=     "); System.out.println(factor);
                        System.out.print("scaleMask1= "); System.out.println(scaleMask1);
                        System.out.print("scaleMask1= "); System.out.println(scaleMask2);
                    }
                }
            }
            block.setParameter("Mask1", divide(mask1, factor*scaleMask1));
            block.setParameter("Mask2", divide(mask2, factor*scaleMask2));
        }
        return super.deriveGridCoverage(sources, parameters);
    }
    
    /**
     * Derive the quantitative category for a band in the destination image.
     * This implementation compute the expected gradient range from the two
     * masks and the value range in the source grid coverage.
     */
    protected Category deriveCategory(final Category[] categories, final Parameters parameters) {
        NumberRange   range = null;
        Category   category = categories[0];
        Color[]      colors = DEFAULT_COLOR_PALETTE;
        String         name = category.getName(null);

        final NumberRange  samples = category.geophysics(false).getRange();
        final boolean isGeophysics = (category == category.geophysics(true));
        final RangeSpecifier   rsp = parameters.getRangeSpecifier();
        if (rsp != null) {
            colors = rsp.getColors();
            if (colors == null) {
                colors = DEFAULT_COLOR_PALETTE;
            }
            final MathTransform1D transform = rsp.getSampleToGeophysics();
            if (transform != null) {
                return new Category(name, colors, range, transform);
            }
            range = rsp.getRange();
        }
        if (range == null) {
            /*
             * If no range has been explicitely set, compute a default one from the normalized
             * kernels. The normalization has been done by 'doOperation' before this method is
             * invoked.
             */
            final ParameterList block = parameters.parameters;
            final KernelJAI mask1 = (KernelJAI) block.getObjectParameter("Mask1");
            final KernelJAI mask2 = (KernelJAI) block.getObjectParameter("Mask2");
            final int size = (mask1.getWidth() + mask1.getHeight() +
                              mask2.getWidth() + mask2.getHeight())/4;
            double factor = getNormalizationFactor(mask1, mask2) / (size-1);
            if (factor>0 && !Double.isInfinite(factor)) {
                range = category.geophysics(true).getRange();
                final double minimum = range.getMinimum();
                final double maximum = range.getMaximum();
                factor *= (maximum - minimum) * DEFAULT_RANGE_SCALE;
                range = new NumberRange(0, factor);
            }
        }
        if (range != null) {
            category = new Category(name, colors, samples, range);
            return category.geophysics(isGeophysics);
        }
        return super.deriveCategory(categories, parameters);
    }
    
    /**
     * Derive the unit of data for a band in the destination image.
     * This method compute <code>sample/axis</code> where:
     *
     * <ul>
     *   <li><code>sample</code> is the sample unit in source image.</li>
     *   <li><code>axis</code> is the coordinate system axis unit.</li>
     * </ul>
     */
    protected Unit deriveUnit(final Unit[] units, final Parameters parameters) {
        final CoordinateSystem cs = parameters.coordinateSystem;
        if (!DEBUG && units.length==1 && units[0]!=null) {
            final Unit spatialUnit = cs.getUnits(0);
            for (int i=Math.min(cs.getDimension(), 2); --i>=0;) {
                if (!spatialUnit.equals(cs.getUnits(i))) {
                    return super.deriveUnit(units, parameters);
                }
            }
            try {
                return units[0].divide(spatialUnit);
            } catch (UnitException exception) {
                // Can't compute units... We will compute image data
                // anyway, but the result will have no know unit.
            }
        }
        return super.deriveUnit(units, parameters);
    }
}
