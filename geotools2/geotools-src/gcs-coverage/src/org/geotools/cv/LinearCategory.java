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
package org.geotools.cv;

// JAI dependencies
import javax.media.jai.util.Range;

// OpenGIS dependencies
import org.opengis.cv.CV_SampleDimension;

// Geotools dependencies
import org.geotools.pt.Matrix;
import org.geotools.ct.MathTransform1D;
import org.geotools.ct.TransformException;
import org.geotools.ct.MathTransformFactory;
import org.geotools.resources.gcs.Resources;
import org.geotools.resources.gcs.ResourceKeys;


/**
 * A quantitative {@link Category} optimized for linear relation.
 * This category defines a transformation between sample values <var>s</var>
 * and geophysics values <var>x</var> using an equation of the form:
 *
 * <center><var>x</var><code>&nbsp;=&nbsp;{@link #offset}&nbsp;+&nbsp;{@link
 * #scale}*</code><var>s</var></center>
 *
 * @version $Id: LinearCategory.java,v 1.1 2002/07/17 23:30:55 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
final class LinearCategory extends Category {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -347452192591481064L;
    
    /**
     * Offset is the value to add to grid values for this category.
     * This attribute is typically used when the category represents
     * elevation data (or any other geophysics parameter).
     *
     * @see CV_SampleDimension#getOffset()
     */
    protected final double offset;
    
    /**
     * Scale is the value which is multiplied to grid values for this
     * category.  This attribute is typically used when the category
     * represents elevation data (or any other geophysics parameter).
     *
     * @see CV_SampleDimension#getScale()
     */
    protected final double scale;
    
    /**
     * Create a category mapping values from <code>sampleValueRange</code>
     * to <code>geophysicsValueRange</code> with a linear relation.
     */
    LinearCategory(final String  name,
                   final int[]   ARGB,
                   final Range   sampleValueRange,
                   final Range   geophysicsValueRange) throws IllegalArgumentException
    {
        this(name, ARGB, sampleValueRange, createLinearTransform(sampleValueRange,
                                                             geophysicsValueRange));
    }

    /**
     * Construct a category with the specified linear relationship.
     */
    LinearCategory(final String  name,
                   final int[]   ARGB,
                   final Range   sampleValueRange,
                   final double  scale,
                   final double  offset) throws IllegalArgumentException
    {
        this(name, ARGB, sampleValueRange, createLinearTransform(scale, offset));
        assert (this.scale  == scale );
        assert (this.offset == offset);
    }
    
    /**
     * Construct a category with the specified linear math transform. The
     * <code>sampleToGeophysics</code> argument <strong>must</strong> be
     * a linear transform, otherwise an exception will be thrown.
     */
    LinearCategory(final String          name,
                   final int[]           ARGB,
                   final Range           sampleValueRange,
                   final MathTransform1D sampleToGeophysics) throws IllegalArgumentException
    {
        super(name, ARGB, sampleValueRange, sampleToGeophysics);
        try {
            scale  = sampleToGeophysics.derivative(Double.NaN);
            offset = sampleToGeophysics.transform(0);
        } catch (TransformException exception) {
            IllegalArgumentException e = new IllegalArgumentException();
            e.initCause(exception);
            throw e;
        }
        if (Double.isNaN(scale) || Double.isInfinite(scale)) {
            throw new IllegalArgumentException(Resources.format(
                    ResourceKeys.ERROR_BAD_COEFFICIENT_$2, "scale", new Double(scale)));
        }
        if (Double.isNaN(offset) || Double.isInfinite(offset)) {
            throw new IllegalArgumentException(Resources.format(
                    ResourceKeys.ERROR_BAD_COEFFICIENT_$2, "offset", new Double(offset)));
        }
    }

    /**
     * Returns a linear transform with the supplied scale and offset values.
     * This is a convenience method for common category creation.
     */
    private static MathTransform1D createLinearTransform(final double scale, final double offset) {
        Matrix matrix = new Matrix(2,2);
        matrix.setElement(0,0, scale);
        matrix.setElement(0,1, offset);
        return (MathTransform1D)MathTransformFactory.getDefault().createAffineTransform(matrix);
    }

    /**
     * Create a linear transform mapping values from <code>sampleValueRange</code>
     * to <code>geophysicsValueRange</code>.
     */
    private static MathTransform1D createLinearTransform(final Range sampleValueRange,
                                                         final Range geophysicsValueRange)
    {
        if (sampleValueRange.isMinIncluded() != geophysicsValueRange.isMinIncluded() ||
            sampleValueRange.isMaxIncluded() != geophysicsValueRange.isMaxIncluded())
        {
            throw new IllegalArgumentException();
        }
        final Class type = sampleValueRange.getElementClass();
        final double minSample = doubleValue(type,     sampleValueRange.getMinValue(), 0);
        final double maxSample = doubleValue(type,     sampleValueRange.getMaxValue(), 0);
        final double minValue  = doubleValue(type, geophysicsValueRange.getMinValue(), 0);
        final double maxValue  = doubleValue(type, geophysicsValueRange.getMaxValue(), 0);
        final double scale     = (maxValue-minValue) / (maxSample-minSample);
        final double offset    = minValue - scale*minSample;
        return createLinearTransform(scale, offset);
    }
    
    /**
     * Compute the geophysics value from a sample value. This
     * transformation involve the following linear equation:
     *
     * <p align="center"><code>x&nbsp;=&nbsp;{@link #offset}&nbsp;+&nbsp;{@link #scale}*s</code></p>
     *
     * The sample value <var>s</var> should be in the range <code>[lower..upper]</code>,
     * where <code>lower</code> and <code>upper</code> have been supplied at construction time.
     * However, this methods do not performs any range check. Index out of range may lead to
     * extrapolation, which may or may not have a physical maining.
     *
     * @param s  The sample value.
     * @return   The geophysics value as a real number.
     *
     * @see SampleDimension#toGeophysicsValue
     */
    protected double toGeophysicsValue(final int s) {
        return offset + scale*s;
    }

    /**
     * Compute the sample value from a geophysics value.  This operation is the inverse
     * of {@link #toGeophysicsValue}, except for a range check: if the resulting index
     * is outside this category's range (<code>[lower..upper]</code>), then it will be
     * clamp to <code>lower</code> or <code>upper-1</code> as necessary.
     *
     * @param  value The geophysics value.
     * @return The sample value in the range <code>[lower..upper]</code>.
     *
     * @see SampleDimension#toSampleValue
     */
    double toSampleValue(final double value) {
        final double sample = (value-offset)/scale;
        if (sample >= maxSample) return maxSample;
        if (sample >= minSample) return sample;
        // Clamps NaN values to 'minSample'.
        return minSample;
    }
}
