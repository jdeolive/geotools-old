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
package org.geotools.gp;

// Colors
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


/**
 * Operation replacing the colormap of a {@link GridCoverage}. Only colors
 * at index allocated to geophysics parameter are changed. The new color
 * ramp will have colors ranging from <code>lowerColor</code> to
 * <code>upperColor</code>.
 *
 * @version $Id: ColormapOperation.java,v 1.3 2002/07/27 12:40:49 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
final class ColormapOperation extends IndexColorOperation {
    /**
     * Construct a new "Colormap" operation.
     */
    public ColormapOperation() {
        super("Colormap", new ParameterListDescriptorImpl(
              null,         // the object to be reflected upon for enumerated values.
              new String[]  // the names of each parameter.
              {
                  "Source",
                  "LowerColor",
                  "UpperColor"
              },
              new Class[]   // the class of each parameter.
              {
                  GridCoverage.class,
                  Color.class,
                  Color.class
              },
              new Object[] // The default values for each parameter.
              {
                  ParameterListDescriptor.NO_PARAMETER_DEFAULT,
                  new Color( 16, 16, 16),
                  new Color(240,240,240)
              },
              null // Defines the valid values for each parameter.
        ));
    }

    /**
     * Transform the supplied RGB colors.
     */
    protected void transformColormap(final byte[] R,
                                     final byte[] G,
                                     final byte[] B,
                                     final SampleDimension band,
                                     final ParameterList parameters)
    {
        final Color lowerColor = (Color) parameters.getObjectParameter("LowerColor");
        final Color upperColor = (Color) parameters.getObjectParameter("UpperColor");
        final Category categories[] = (Category[]) band.getCategories().toArray();
        for (int j=categories.length; --j>=0;) {
            final Category category = categories[j];
            if (category.isQuantitative()) {
                final Range range = category.getRange();
                int lower = ((Number) range.getMinValue()).intValue();
                int upper = ((Number) range.getMaxValue()).intValue();
                if (!range.isMinIncluded()) lower++;
                if (!range.isMaxIncluded()) upper--;
                if (upper > lower) { // No change if there is only 1 color.
                    final double di = (double)(upper-lower);
                    final int    Ri =  lowerColor.getRed  ();
                    final int    Gi =  lowerColor.getGreen();
                    final int    Bi =  lowerColor.getBlue ();
                    final double fR = (upperColor.getRed  ()-Ri) / di;
                    final double fG = (upperColor.getGreen()-Gi) / di;
                    final double fB = (upperColor.getBlue ()-Bi) / di;
                    for (int i=lower; i<upper; i++) {
                        final double i0=i-lower;
                        R[i] = (byte) (Ri + (int) Math.rint(i0*fR));
                        G[i] = (byte) (Gi + (int) Math.rint(i0*fG));
                        B[i] = (byte) (Bi + (int) Math.rint(i0*fB));
                    }
                }
            }
        }
    }
}
