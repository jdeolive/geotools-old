/*
 * Geotools - OpenSource mapping toolkit
 * (C) 2003, Institut de Recherche pour le Développement
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
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package org.geotools.gp;

// J2SE dependencies
import java.awt.Color;
import java.awt.RenderingHints;

// Java Advanced Imaging
import javax.media.jai.KernelJAI;
import javax.media.jai.ParameterList;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.ParameterListDescriptor;
import javax.media.jai.ParameterListDescriptorImpl;
import javax.media.jai.util.Range;

// Geotools dependencies
import org.geotools.cv.Category;
import org.geotools.gc.GridCoverage;
import org.geotools.cs.CoordinateSystem;
import org.geotools.resources.Utilities;


/**
 * An operation for convolution. This operation is built on top of the JAI's operation
 * "Convolve". It includes the OpenGIS "LaplaceType1Filter" and "LaplaceType2Filter"
 * operations.
 *
 * @version $Id: ConvolveOperation.java,v 1.2 2003/04/30 21:58:00 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
final class ConvolveOperation extends OperationJAI {
    /**
     * Kernel for the Laplace type 1 filter.
     */
    public static final KernelJAI LAPLACE_TYPE_1 = new KernelJAI(3, 3, new float[] {
        0/9f, -1/9f,  0/9f,
       -1/9f,  4/9f, -1/9f,
        0/9f, -1/9f,  0/9f
    });

    /**
     * Kernel for the Laplace type 2 filter.
     */
    public static final KernelJAI LAPLACE_TYPE_2 = new KernelJAI(3, 3, new float[] {
        -1/9f, -1/9f, -1/9f,
        -1/9f,  8/9f, -1/9f,
        -1/9f, -1/9f, -1/9f
    });

    /**
     * Small number for avoiding rounding errors. Should be close to the precision
     * of <code>float</code> type.
     */
    private static final double EPS = 1E-5;

    /**
     * The default scale factor to apply on the range computed by
     * {@link #deriveCategory}. For example a value of 0.04 means
     * that only values from 0 to 4% of the maximum will appears
     * in different colors.
     */
    private static final double DEFAULT_RANGE_SCALE = 0.04;
    
    /**
     * The default color palette for the gradients.
     */
    private static final Color[] DEFAULT_COLOR_PALETTE = new Color[] {
        new Color(192,224,255),
        new Color( 16, 32, 16),
        new Color(255,224,192)
    };

    /**
     * Parameter descriptor common to all convolution built on top of a predefined kernel
     * (e.g. "LaplaceType1Filter", "LaplaceType2Filter").
     */
    private static final ParameterListDescriptor DESCRIPTOR = new ParameterListDescriptorImpl(
        null,    // the object to be reflected upon for enumerated values.
        new String[] { // the names of each parameter.
          "Source",
        // "SampleDimension"
        },
        new Class[]   // the class of each parameter.
        {
          GridCoverage.class,
        // Integer.class
        },
        new Object[] // The default values for each parameter.
        {
          ParameterListDescriptor.NO_PARAMETER_DEFAULT,
        // ZERO
        },
        new Object[] // Defines the valid values for each parameter.
        {
          null,
        // RANGE_0
        });

    /**
     * The kernel to use for this convolution, or <code>null</code>
     * if the kernel must be fetched from user-provided parameter.
     */
    private final KernelJAI kernel;

    /**
     * Construct a default convolve operation.
     */
    public ConvolveOperation() {
        super("Convolve");
        kernel = null;
    }

    /**
     * Construct a convolve operation with the specified kernel.
     *
     * @param name The operation name to be registered to {@link GridCoverageProcessor}.
     *             This is not the JAI name.
     * @param kernel The kernel to use.
     */
    public ConvolveOperation(final String name, final KernelJAI kernel) {
        super(name, getOperationDescriptor("Convolve"), DESCRIPTOR);
        this.kernel = kernel;
    }

    /**
     * Returns a scale factor for the specified kernel. This scale factor will be used
     * for scaling the range of values of the target category.
     */
    private static final double getFactor(final KernelJAI kernel) {
        double sum = 0;
        final int width  = kernel.getWidth();
        final int height = kernel.getHeight();
        for (int y=height; --y>=0;) {
            for (int x=width; --x>=0;) {
                sum += kernel.getElement(x,y);
            }
        }
        return sum;
    }

    /**
     * Apply an operation. This method add the kernel to the parameter list
     * and invokes the super-class method.
     */
    protected GridCoverage doOperation(final GridCoverage[]    sources,
                                       final ParameterBlockJAI parameters,
                                       final RenderingHints    hints)
    {
        if (kernel != null) {
            parameters.setParameter("kernel", kernel);
        }
        return super.doOperation(sources, parameters, hints);
    }
    
    /**
     * Derive the quantitative category for a band in the destination image.
     * This implementation compute the expected values range from the kernel.
     */
    protected Category deriveCategory(final Category[] categories,
                                      final CoordinateSystem cs,
                                      final ParameterList parameters)
    {
        Category category = categories[0];
        final KernelJAI kernel = (KernelJAI) parameters.getObjectParameter("kernel");
        double factor = getFactor(kernel);
        if (Math.abs(factor-1) > EPS) {
            final boolean isGeophysics = (category == category.geophysics(true));
            Color[] colors = category.getColors();
            final Range range = category.geophysics(true).getRange();
            double minimum = ((Number) range.getMinValue()).doubleValue();
            double maximum = ((Number) range.getMaxValue()).doubleValue();
            if (Math.abs(factor) <= EPS) {
                // Heuristic approach
                maximum -= minimum;
                minimum = -maximum;
                factor = DEFAULT_RANGE_SCALE;
                colors = DEFAULT_COLOR_PALETTE;
            }
            minimum *= factor;
            maximum *= factor;
            if (minimum > maximum) {
                final double swap = minimum;
                minimum = maximum;
                maximum = swap;
            }
            category = new Category(category.getName(null), colors,
                                    category.geophysics(false).getRange(),
                                    new Range(Double.class, new Double(minimum), new Double(maximum)));
            return category.geophysics(isGeophysics);
        }
        return category;
    }
    
    /**
     * Compares the specified object with this operation for equality.
     */
    public boolean equals(final Object object) {
        if (object == this) {
            // Slight optimisation
            return true;
        }
        if (super.equals(object)) {
            final ConvolveOperation that = (ConvolveOperation) object;
            return Utilities.equals(this.kernel, that.kernel);
        }
        return false;
    }
}
