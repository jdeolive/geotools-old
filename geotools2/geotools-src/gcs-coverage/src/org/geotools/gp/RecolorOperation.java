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
 */
package org.geotools.gp;

// J2SE dependencies
import java.util.Map;
import java.util.Collections;
import java.awt.Color;

// Java Advanced Imaging
import javax.media.jai.util.Range;
import javax.media.jai.ParameterList;
import javax.media.jai.ParameterListDescriptor;
import javax.media.jai.ParameterListDescriptorImpl;

// Geotools implementation
import org.geotools.cv.Category;
import org.geotools.gc.GridCoverage;
import org.geotools.cv.SampleDimension;
import org.geotools.resources.ImageUtilities;


/**
 * Operation replacing the colors of a {@link GridCoverage}. This operation accepts one
 * argument, <code>ColorMaps</code>, which must be an array of {@link Map} objects. Keys
 * are category names as {@link String}. Values are colors as <code>Color[]</code>. The
 * <code>null</code> key is a special value meaning "any quantitative category".
 *
 * @version $Id: RecolorOperation.java,v 1.2 2003/03/14 17:15:59 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
final class RecolorOperation extends IndexColorOperation {
    /**
     * Construct a new "Recolor" operation.
     */
    public RecolorOperation() {
        super("Recolor", new ParameterListDescriptorImpl(
              null,         // the object to be reflected upon for enumerated values.
              new String[]  // the names of each parameter.
              {
                  "Source",
                  "ColorMaps"
              },
              new Class[]   // the class of each parameter.
              {
                  GridCoverage.class,
                  Map[].class
              },
              new Object[] // The default values for each parameter.
              {
                  ParameterListDescriptor.NO_PARAMETER_DEFAULT,
                  new Map[] {
                      Collections.singletonMap(null, new Color[] {
                        new Color( 16, 16, 16),
                        new Color(240,240,240)
                      })
                  }
              },
              null // Defines the valid values for each parameter.
        ));
    }

    /**
     * Transform the supplied RGB colors.
     */
    protected SampleDimension transformColormap(final int[] ARGB,
                                                final int   band,
                                                final SampleDimension sampleDimension,
                                                final ParameterList   parameters)
    {
        final Map[] colorMaps = (Map[]) parameters.getObjectParameter("ColorMaps");
        if (colorMaps==null || colorMaps.length==0) {
            return sampleDimension;
        }
        boolean changed = false;
        final Map colorMap = colorMaps[Math.min(band, colorMaps.length-1)];
        final Category categories[] = (Category[]) sampleDimension.getCategories().toArray();
        for (int j=categories.length; --j>=0;) {
            Category category = categories[j];
            Color[] colors = (Color[]) colorMap.get(category.getName(null));
            if (colors == null) {
                if (!category.isQuantitative()) {
                    continue;
                }
                colors = (Color[]) colorMap.get(null);
                if (colors == null) {
                    continue;
                }
            }
            final Range range = category.getRange();
            int lower = ((Number) range.getMinValue()).intValue();
            int upper = ((Number) range.getMaxValue()).intValue();
            if (!range.isMinIncluded()) lower++;
            if ( range.isMaxIncluded()) upper++;
            ImageUtilities.expand(colors, ARGB, lower, upper);
            category = category.recolor(colors);
            if (!categories[j].equals(category)) {
                categories[j] = category;
                changed = true;
            }
        }
        return changed ? new SampleDimension(categories, sampleDimension.getUnits())
                       : sampleDimension;
    }
}
