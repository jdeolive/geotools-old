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

// Collections
import java.util.List;

// Java Advanced Imaging
import javax.media.jai.util.Range;
import javax.media.jai.ParameterList;
import javax.media.jai.ParameterListDescriptor;
import javax.media.jai.ParameterListDescriptorImpl;

// Geotools dependencies
import org.geotools.cv.Category;
import org.geotools.gc.GridCoverage;
import org.geotools.cv.SampleDimension;


/**
 * Make a color ramp more "gradual" for geophysics categories. This operation applies only on the
 * color palette of geophysics categories (i.e. category for which {@link Category#isQuantitative}
 * returns <code>true</code>). If it find  a range of sample values using the same color, then this
 * operation will interpolate the colors in the color palette. This interpolation give a smoother
 * appeareance to images that uses this color palette.
 * <br><br>
 * <strong>Example:</strong> Consider the following color palette. Sample values 0-2 are red,
 * 3-5 are green and 6-8 are blue. In each block of identical color, this operation lets the
 * first occurence inchanged and interpolate all others. The last block of color (blue in our
 * example) is inchanged since we don't know which color come next.
 *
 * <blockquote><pre>
 * sample    RGB before    RGB after
 * ------    ----------    ---------
 *     00 .. FF0000 ...... FF0000
 *     01    FF0000        AA5500
 *     02    FF0000        55AA00
 *     03 .. 00FF00 ...... 00FF00
 *     04    00FF00        00AA55
 *     05    00FF00        0055AA
 *     06 .. 0000FF ...... 0000FF
 *     07    0000FF        0000FF
 *     08    0000FF        0000FF
 * </pre></blockquote>
 *
 * @version $Id: GradualColormapOperation.java,v 1.3 2003/03/14 17:11:43 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
final class GradualColormapOperation extends IndexColorOperation {
    /**
     * Constante indiquant qu'il faut garder la première couleur
     * lorsqu'une plage de couleurs constantes est rencontrée.
     */
    private static final int KEEP_LOWER_COLOR = +1;

    /**
     * Constante indiquant qu'il faut garder la dernière couleur
     * lorsqu'une plage de couleurs constantes est rencontrée.
     */
    private static final int KEEP_UPPER_COLOR = -1;

    /**
     * Constante indiquant quelle couleur il faut garder
     * lorsque l'opérateur trouve une plage de couleurs
     * identiques.
     *
     * @task TODO: Allow setting this parameter from the ParameterList.
     */
    private final int mode = KEEP_LOWER_COLOR;

    /**
     * Construit une opération qui interpolera les couleurs.
     *
     * @param mode Indique quelle couleur il faut garder lorsque l'opérateur trouve une plage de
     *             couleurs identiques. La valeur {@link #KEEP_LOWER_COLOR} indique qu'il faut
     *             laisser inchangée la première couleur de la plage et interpoler les autres
     *             (la dernière couleur de la plage sera donc la plus affectée). La valeur
     *             {@link #KEEP_UPPER_COLOR} indique au contraire qu'il faut laisser inchangée
     *             la dernière couleur de la plage et interpoler les autres (la première couleur
     *             sera donc la plus affectée).
     */
    public GradualColormapOperation() {
        super("GradualColormap", new ParameterListDescriptorImpl(
              null,         // the object to be reflected upon for enumerated values.
              new String[]  // the names of each parameter.
              {
                  "Source"
              },
              new Class[]   // the class of each parameter.
              {
                  GridCoverage.class
              },
              new Object[] // The default values for each parameter.
              {
                  ParameterListDescriptor.NO_PARAMETER_DEFAULT
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
        final List categories = sampleDimension.getCategories();
        for (int j=categories.size(); --j>=0;) {
            final Category category = (Category) categories.get(j);
            if (category.isQuantitative()) {
                final Range range = category.getRange();
                int sens  = +1;
                int lower = (int)Math.ceil (((Number)range.getMinValue()).doubleValue());
                int upper = (int)Math.floor(((Number)range.getMaxValue()).doubleValue());
                if (!range.isMinIncluded()) lower++;
                if (!range.isMaxIncluded()) upper--;
                if (mode == KEEP_UPPER_COLOR) {
                    sens = -1;
                    final int swap = lower;
                    lower = upper;
                    upper = swap;
                }
                for (int i=lower; i!=upper;) {
                    int color = ARGB[i];
                    final int lo = i;
                    final int Ai = (color >>> 24) & 0xFF;
                    final int Ri = (color >>> 16) & 0xFF;
                    final int Gi = (color >>>  8) & 0xFF;
                    final int Bi = (color >>>  0) & 0xFF;
                    int Af,Rf,Gf,Bf;
                    int up=lo; do {
                        up += sens;
                        color = ARGB[up];
                        Af = (color >>> 24) & 0xFF;
                        Rf = (color >>> 16) & 0xFF;
                        Gf = (color >>>  8) & 0xFF;
                        Bf = (color >>>  0) & 0xFF;
                    }
                    while (up!=upper && Rf==Ri && Gf==Gi && Bf==Bi);
                    final double delta = (double)(up-lo);
                    final double    fA = (double)(Af-Ai) / delta;
                    final double    fR = (double)(Rf-Ri) / delta;
                    final double    fG = (double)(Gf-Gi) / delta;
                    final double    fB = (double)(Bf-Bi) / delta;
                    while ((i+=sens) != up) {
                        final double i0 = i-lo;
                        ARGB[i] = ((Ai + (int)Math.rint(i0*fA)) << 24) |
                                  ((Ri + (int)Math.rint(i0*fR)) << 16) |
                                  ((Gi + (int)Math.rint(i0*fG)) <<  8) |
                                  ((Bi + (int)Math.rint(i0*fB)) <<  0);
                    }
                }
            }
        }
        return sampleDimension;
    }
}
