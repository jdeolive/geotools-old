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

// Collections
import java.util.Arrays;
import java.util.Comparator;


/**
 * Comparateur ayant la charge de classer des catégories {@link Category} en fonction
 * d'un de leurs champs. Les champs pris en compte sont {@link Category#minSample} et
 * {@link Category#minValue}, qui interviennent respectivement dans les décodages
 * et encodages des pixels.
 *
 * @version $Id: CategoryComparator.java,v 1.2 2002/07/17 23:30:55 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
abstract class CategoryComparator implements Comparator {
    /**
     * Comparateur servant à classer les thèmes en ordre
     * croissant d'index {@link Category#minSample}.
     */
    public static final CategoryComparator BY_SAMPLES = new CategoryComparator() {
        protected double getMinimum(final Category category) {return category.minSample;}
        protected double getMaximum(final Category category) {return category.maxSample;}
    };
    
    /**
     * Comparateur servant à classer les thèmes en ordre
     * croissant de valeurs {@link Category#minValue}.
     */
    public static final CategoryComparator BY_VALUES = new CategoryComparator() {
        protected double getMinimum(final Category category) {return category.minValue;}
        protected double getMaximum(final Category category) {return category.maxValue;}
    };
    
    /**
     * Retourne la valeur inférieure du champ à comparer. Cette méthode retournera
     * {@link Category#minSample} ou {@link Category#minValue} selon l'implémentation
     * utilisée. Notez que le type 'double' utilise 52 bits pour sa mantise, ce qui
     * est amplement suffisant pour les 32 bits du type 'int'.
     */
    protected abstract double getMinimum(final Category category);
    
    /**
     * Retourne la valeur supérieure du champ à comparer. Cette méthode retournera
     * {@link Category#maxSample} ou {@link Category#maxValue} selon l'implémentation
     * utilisée. Notez que le type 'double' utilise 52 bits pour sa mantise, ce qui
     * est amplement suffisant pour les 32 bits du type 'int'.
     */
    protected abstract double getMaximum(final Category category);
    
    /**
     * Classe les éléments du tableau <code>category</code>.
     * Le classement est fait dans une copie du tableau, qui
     * est retourné. Le tableau original n'est pas modifié.
     */
    public final Category[] sort(Category[] categories) {
        categories = (Category[]) categories.clone();
        Arrays.sort(categories, this);
        assert isSorted(categories);
        return categories;
    }
    
    /**
     * Vérifie si le tableau de thèmes spécifié est bien en ordre croissant.
     * La comparaison ne tient pas compte des valeurs <code>NaN</code>.
     */
    public final boolean isSorted(final Category[] categories) {
        for (int i=1; i<categories.length; i++) {
            if (compare(getMaximum(categories[i-1]),
                        getMinimum(categories[i-0])) > 0)
            {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Compare deux objets {@link Category}. Cette méthode sert à
     * classer les thèmes en ordre croissant de valeurs de leur
     * champ {@link #getMinimum}.
     */
    public final int compare(final Object o1, final Object o2) {
        return compare(getMinimum((Category)o1),
                       getMinimum((Category)o2));
    }

    /**
     * Compare deux valeurs de type <code>double</code>. Cette méthode
     * est similaire à {@link Double#compare(double,double)}, excepté
     * qu'elle ordonne aussi les différentes valeurs NaN.
     */
    private static int compare(final double v1, final double v2) {
        if (Double.isNaN(v1) && Double.isNaN(v2)) {
            final long bits1  = Double.doubleToRawLongBits(v1);
            final long bits2  = Double.doubleToRawLongBits(v2);
            if (bits1 < bits2) return -1;
            if (bits1 > bits2) return +1;
        }
        return Double.compare(v1, v2);
    }
    
    /**
     * Effectue une recherche bi-linéaire de la valeur spécifiée. Cette
     * méthode est semblable à {@link Arrays#binarySearch(double[],double)},
     * excepté qu'elle peut distinguer différentes valeurs de NaN.
     */
    public static int binarySearch(final double[] array, final double key) {
        int low  = 0;
        int high = array.length-1;
        final boolean keyIsNaN = Double.isNaN(key);
        while (low <= high) {
            final int mid = (low + high) >> 1;
            final double midVal = array[mid];
            
            final int cmp;
            if      (midVal < key) cmp = -1; // Neither val is NaN, midVal is smaller
            else if (midVal > key) cmp = +1; // Neither val is NaN, midVal is larger
            else {
                /*
                 * The following is an adaptation of evaluator's comments for bug #4471414
                 * (http://developer.java.sun.com/developer/bugParade/bugs/4471414.html).
                 * Extract from evaluator's comment:
                 *
                 *     [This] code is not guaranteed to give the desired results because
                 *     of laxity in IEEE 754 regarding NaN values. There are actually two
                 *     types of NaNs, signaling NaNs and quiet NaNs. Java doesn't support
                 *     the features necessary to reliably distinguish the two.  However,
                 *     the relevant point is that copying a signaling NaN may (or may not,
                 *     at the implementors discretion) yield a quiet NaN -- a NaN with a
                 *     different bit pattern (IEEE 754 6.2).  Therefore, on IEEE 754 compliant
                 *     platforms it may be impossible to find a signaling NaN stored in an
                 *     array since a signaling NaN passed as an argument to binarySearch may
                 *     get replaced by a quiet NaN.
                 */
                final long midRawBits = Double.doubleToRawLongBits(midVal);
                final long keyRawBits = Double.doubleToRawLongBits(key);
                if (midRawBits != keyRawBits) {
                    final boolean midIsNaN = Double.isNaN(midVal);
                    if (keyIsNaN) {
                        // If (mid,key)==(!NaN, NaN): -1.
                        // If two NaN arguments, compare NaN bits.
                        cmp = (!midIsNaN || midRawBits<keyRawBits) ? -1 : +1;
                    } else {
                        // If (mid,key)==(NaN, !NaN): +1.
                        // Otherwise, case for (-0.0, 0.0) and (0.0, -0.0).
                        cmp = (!midIsNaN && midRawBits<keyRawBits) ? -1 : +1;
                    }
                } else {
                    cmp = 0;
                }
            }
            if      (cmp < 0) low  = mid + 1;
            else if (cmp > 0) high = mid - 1;
            else return mid; // key found
        }
        return -(low + 1);  // key not found.
    }
}
