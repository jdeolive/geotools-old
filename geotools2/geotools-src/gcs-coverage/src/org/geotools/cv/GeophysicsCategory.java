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

// J2SE dependencies
import java.awt.Color;
import java.util.Locale;

// Geotools dependencies
import org.geotools.ct.MathTransform1D;
import org.geotools.ct.TransformException;
import org.geotools.ct.MathTransformFactory;

// Resources
import org.geotools.util.NumberRange;
import org.geotools.resources.Utilities;
import org.geotools.resources.gcs.Resources;
import org.geotools.resources.gcs.ResourceKeys;


/**
 * A "geophysics" view of a category. Sample values in this category are equal to geophysics
 * values.   By definition, the {@link #getSampleToGeophysics} method for this class returns
 * the identity transform, or <code>null</code> if this category is a qualitative one.
 *
 * @version $Id: GeophysicsCategory.java,v 1.7 2003/05/04 22:33:14 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
final class GeophysicsCategory extends Category {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -7164422654831370784L;

    /**
     * The identity transform. To be returned by {@link #getSampleToGeophysics}.
     */
    static final MathTransform1D IDENTITY;
    static {
        IDENTITY = (MathTransform1D) MathTransformFactory.getDefault().createIdentityTransform(1);
    }
    
    /**
     * Creates a new instance of geophysics category.
     *
     * @param  inverse The originating {@link Category}.
     * @param  isQuantitative <code>true</code> if the originating category is quantitative.
     * @throws TransformException if a transformation failed.
     */
    GeophysicsCategory(Category inverse, boolean isQuantitative) throws TransformException {
        super(inverse, isQuantitative);
    }
    
    /**
     * Returns the category name localized in the specified locale.
     */
    public String getName(final Locale locale) {
        assert !(inverse instanceof GeophysicsCategory);
        return inverse.getName(locale);
    }
    
    /**
     * Returns the set of colors for this category.
     */
    public Color[] getColors() {
        assert !(inverse instanceof GeophysicsCategory);
        return inverse.getColors();
    }

    /**
     * Returns the range of geophysics value.
     *
     * @return The range of geophysics values.
     * @throws IllegalStateException if sample values can't be transformed into geophysics values.
     *
     * @task TODO: The algorithm for finding minimum and maximum values is very simple for
     *             now and will not work if the transformation has local extremas. We would
     *             need some more sophesticated algorithm for the most general cases. Such
     *             a general algorithm would be usefull in the super-class constructor as well.
     */
    public NumberRange getRange() throws IllegalStateException {
        if (range == null) try {
            final MathTransform1D tr = inverse.transform;
            final NumberRange r = inverse.range;
            boolean minIncluded = r.isMinIncluded();
            boolean maxIncluded = r.isMaxIncluded();
            double min  = tr.transform(r.getMinimum());
            double max  = tr.transform(r.getMaximum());
            double min2 = tr.transform(r.getMinimum(!minIncluded));
            double max2 = tr.transform(r.getMaximum(!maxIncluded));
            if ((minIncluded ? min2 : min) > (maxIncluded ? max2 : max)) {
                final double  tmp, tmp2;
                final boolean tmpIncluded;
                tmp=min;   tmp2=min2;  tmpIncluded=minIncluded;
                min=max;   min2=max2;  minIncluded=maxIncluded;
                max=tmp;   max2=tmp2;  maxIncluded=tmpIncluded;
            }
            assert Double.doubleToLongBits(minimum) == Double.doubleToLongBits(minIncluded ? min : min2);
            assert Double.doubleToLongBits(maximum) == Double.doubleToLongBits(maxIncluded ? max : max2);
            range = new Range(min, minIncluded, max, maxIncluded, min2, max2);

        } catch (TransformException cause) {
            IllegalStateException exception = new IllegalStateException(Resources.format(
                                                  ResourceKeys.ERROR_BAD_TRANSFORM_$1,
                                                  Utilities.getShortClassName(inverse.transform)));
            exception.initCause(cause);
            throw exception;
        }
        return range;
    }

    /**
     * Returns a transform from sample values to geophysics values, which (by definition)
     * is an identity transformation. If this category is not a quantitative one, then
     * this method returns <code>null</code>.
     */
    public MathTransform1D getSampleToGeophysics() {
        return isQuantitative() ? IDENTITY : null;
    }
    
    /**
     * Returns <code>true</code> if this category is quantitative.
     */
    public boolean isQuantitative() {
        assert !(inverse instanceof GeophysicsCategory) : inverse;
        return inverse.isQuantitative();
    }
    
    /**
     * Returns a new category for the same range of sample values but a different color palette.
     */
    public Category recolor(final Color[] colors) {
        assert !(inverse instanceof GeophysicsCategory) : inverse;
        return inverse.recolor(colors).inverse;
    }

    /**
     * Changes the mapping from sample to geophysics values.
     */
    public Category rescale(MathTransform1D sampleToGeophysics) {
        if (sampleToGeophysics.isIdentity()) {
            return this;
        }
        sampleToGeophysics = (MathTransform1D)MathTransformFactory.getDefault()
                .createConcatenatedTransform(inverse.getSampleToGeophysics(), sampleToGeophysics);
        return inverse.rescale(sampleToGeophysics).inverse;
    }

    /**
     * If <code>false</code>, returns a category with the original sample values.
     */
    public Category geophysics(final boolean toGeophysics) {
        assert !(inverse instanceof GeophysicsCategory) : inverse;
        return inverse.geophysics(toGeophysics);
    }




    /**
     * Range of geophysics values computed from the range of the {@linkplain #inverse indexed
     * category}. The <code>inverse.transform</code> transformation is used for computing the
     * inclusive and exclusive minimum and maximum values of this range.  We compute both the
     * inclusive and exclusive values because we can't rely on the default implementation, which
     * looks for the nearest representable number. For example is the range of index values is 0
     * to 10 exclusive (or 0 to 9 inclusive) and the scale is 2, then the range of geophysics
     * values is 0 to 20 exclusive or 0 to 18 inclusive, not 0 to 19.9999... The numbers between
     * 18 and 20 is a "gray area" where we don't know for sure what the user intend to do.
     *
     * @version $Id: GeophysicsCategory.java,v 1.7 2003/05/04 22:33:14 desruisseaux Exp $
     * @author Martin Desruisseaux
     *
     * @see GeophysicsCategory#getRange
     */
    private static final class Range extends NumberRange {
        /**
         * Serial number for interoperability with different versions.
         */
        private static final long serialVersionUID = -1416908614729956928L;

        /**
         * The minimal value to be returned by {@link #getMinimum(boolean)} when the
         * <code>inclusive</code> flag is the opposite of {@link #isMinIncluded()}.
         */
        private final double minimum2;

        /**
         * The maximal value to be returned by {@link #getMaximum(boolean)} when the
         * <code>inclusive</code> flag is the opposite of {@link #isMaxIncluded()}.
         */
        private final double maximum2;

        /**
         * Construct a range of <code>double</code> values.
         */
        public Range(final double minimum,  final boolean isMinIncluded,
                     final double maximum,  final boolean isMaxIncluded,
                     final double minimum2, final double  maximum2)
        {
            super(minimum, isMinIncluded, maximum, isMaxIncluded);
            this.minimum2 = minimum2;
            this.maximum2 = maximum2;
        }

        /**
         * Returns the minimum value with the specified inclusive or exclusive state.
         */
        public double getMinimum(final boolean inclusive) {
            return (inclusive == isMinIncluded()) ? getMinimum() : minimum2;
        }

        /**
         * Returns the maximum value with the specified inclusive or exclusive state.
         */
        public double getMaximum(final boolean inclusive) {
            return (inclusive == isMaxIncluded()) ? getMaximum() : maximum2;
        }
    }
}
