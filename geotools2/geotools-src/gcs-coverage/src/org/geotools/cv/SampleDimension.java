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

// J2SE and JAI dependencies
import java.util.Locale;
import javax.media.jai.util.Range;

// OpenGIS dependencies
import org.opengis.cv.CV_SampleDimension;

// Geotools dependencies
import org.geotools.units.Unit;


/**
 * Describes the data values for a coverage.
 * For a grid coverage a sample dimension is a band.
 *
 * @version 1.00
 * @author OpenGIS (www.opengis.org)
 * @author Martin Desruisseaux
 *
 * @see org.opengis.cv.CV_SampleDimension
 */
public class SampleDimension {
    /**
     * The category list for this sample dimension,
     * or <code>null</code> if this sample dimension
     * has no category.
     */
    private final CategoryList categories;
    
    /**
     * The minimum value in this sample dimension.
     */
    private final double minimum;
    
    /**
     * The maximum value in this sample dimension.
     */
    private final double maximum;
    
    /**
     * Construct a sample dimension with a set of categories.
     *
     * @param categories The category list for this sample dimension, or
     *        <code>null</code> if this sample dimension has no category.
     */
    public SampleDimension(final CategoryList categories) {
        this.categories = categories;
        double min = Double.NEGATIVE_INFINITY;
        double max = Double.POSITIVE_INFINITY;
        if (categories!=null) {
            final Range range = categories.getRange(true);
            if (range!=null) {
                Comparable n;
                n=range.getMinValue(); if (n instanceof Number) min=((Number)n).doubleValue();
                n=range.getMaxValue(); if (n instanceof Number) max=((Number)n).doubleValue();
            }
        }
        this.minimum = min;
        this.maximum = max;
    }
    
    /**
     * Get the sample dimension title or description.
     * This string may be <code>null</code> if no description is present.
     *
     * @param  locale The locale, or <code>null</code> for the default one.
     * @return The localized description. If no description was available
     *         in the specified locale, a default locale is used.
     *
     * @see CV_SampleDimension#getDescription()
     */
    public String getDescription(final Locale locale) {
        return (categories!=null) ? categories.getName(locale) : null;
    }
    
    /**
     * Returns the category list for the values contained in a sample dimension.
     * This allows for names to be assigned to numerical values. If no categories
     * exist, <code>null</code> is returned.
     *
     * @see CV_SampleDimension#getCategoryNames()
     * @see CV_SampleDimension#getOffset()
     * @see CV_SampleDimension#getScale()
     */
    public CategoryList getCategoryList() {
        return categories;
    }
    
    // NOTE: "getPaletteInterpretation()" is not available in Geotools since
    //       palette are backed by IndexColorModel, which support only RGB.
    
    /**
     * Returns the color interpretation of the sample dimension.
     * A sample dimension can be an index into a color palette or be a color model
     * component. If the sample dimension is not assigned a color interpretation
     * the value is {@link ColorInterpretation#UNDEFINED}.
     *
     * @see CV_SampleDimension#getColorInterpretation()
     */
    public ColorInterpretation getColorInterpretation() {
        return ColorInterpretation.UNDEFINED;
    }
    
    // NOTE: "scale" and "offset" functionality are handled by Category.
    
    /**
     * Returns the unit information for this sample dimension.
     * May returns <code>null</code> is this dimension has no units.
     *
     * @see CV_SampleDimension#getUnits()
     */
    public Unit getUnits() {
        return (categories!=null) ? categories.getUnits() : null;
    }
    
    /**
     * Returns the minimum value occurring in this sample dimension.
     * The default implementation query the value from {@link CategoryList}, if available.
     *
     * @see CV_SampleDimension#getMinimumValue()
     */
    public double getMinimumValue() {
        return minimum;
    }
    
    /**
     * Returns the maximum value occurring in this sample dimension.
     * The default implementation query the value from {@link CategoryList}, if available.
     *
     * @see CV_SampleDimension#getMaximumValue()
     */
    public double getMaximumValue() {
        return maximum;
    }
}
