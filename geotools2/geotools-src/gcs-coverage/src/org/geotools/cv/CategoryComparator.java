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
 * d'un de leurs champs. Les champs pris en compte sont {@link Category#lower} et
 * {@link Category#minimum}, qui interviennent respectivement dans les décodages
 * et encodages des pixels.
 *
 * @version 1.0
 * @author Martin Desruisseaux
 */
abstract class CategoryComparator implements Comparator {
    /**
     * Comparateur servant à classer les thèmes en ordre
     * croissant d'index {@link Category#lower}.
     */
    public static final CategoryComparator BY_INDEX=new CategoryComparator() {
        protected double getLower(final Category category) {return category.lower;}
        protected double getUpper(final Category category) {return category.upper;}
    };
    
    /**
     * Comparateur servant à classer les thèmes en ordre
     * croissant de valeurs {@link Category#minimum}.
     */
    public static final CategoryComparator BY_VALUES=new CategoryComparator() {
        protected double getLower(final Category category) {return category.minimum;}
        protected double getUpper(final Category category) {return category.maximum;}
    };
    
    /**
     * Retourne la valeur inférieure du champ à comparer. Cette méthode retournera
     * {@link Category#lower} ou {@link Category#minimum} selon l'implémentation
     * utilisée. Notez que le type 'double' utilise 52 bits pour sa mantise, ce
     * qui est amplement suffisant pour les 32 bits du type 'int'.
     */
    protected abstract double getLower(final Category category);
    
    /**
     * Retourne la valeur supérieure du champ à comparer. Cette méthode retournera
     * {@link Category#upper} ou {@link Category#maximum} selon l'implémentation
     * utilisée. Notez que le type 'double' utilise 52 bits pour sa mantise, ce
     * qui est amplement suffisant pour les 32 bits du type 'int'.
     */
    protected abstract double getUpper(final Category category);
    
    /**
     * Compare deux objets {@link Category}. Cette méthode sert à
     * classer les thèmes en ordre croissant de valeurs de leur
     * champ {@link #getLower}.
     */
    public final int compare(final Object o1, final Object o2) {
        final double v1 = getLower((Category)o1);
        final double v2 = getLower((Category)o2);
        final int cmp=Double.compare(v1, v2);
        if (cmp==0) {
            // Special test for NaN
            final long    bits1  = Double.doubleToRawLongBits(v1);
            final long    bits2  = Double.doubleToRawLongBits(v2);
            final boolean isNaN1 = Double.isNaN(v1);
            final boolean isNaN2 = Double.isNaN(v2);
            if (!isNaN1 &&  isNaN2) return -1;
            if ( isNaN1 && !isNaN2) return +1;
            if (  bits1  <   bits2) return -1;
            if (  bits1  >   bits2) return +1;
            return 0;
        }
        return cmp;
    }
    
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
            if (getUpper(categories[i-1]) > getLower(categories[i])) {
                return false;
            }
        }
        return true;
    }
}
