/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
 * (C) 2001, Institut de Recherche pour le Développement
 * (C) 1999, Pêches et Océans Canada
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
package org.geotools.renderer.array;

// Geotools dependencies
import org.geotools.renderer.geom.CompressionLevel;


/**
 * Enveloppe un tableau <code>float[]</code> dans lequel des données pourront être ajoutées.
 *
 * @version $Id: DynamicArray.java,v 1.9 2003/05/27 18:22:43 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
final class DynamicArray extends SubArray {
    /**
     * Serial version for compatibility with previous version.
     */
    private static final long serialVersionUID = 3336921710471431118L;

    /**
     * Construit un tableau qui contiendra une copie des coordonnées du tableau spécifié.
     */
    public DynamicArray(final PointArray points) {
        super(points.toArray(), 0, 2*points.count());
        assert array.length == upper;
    }

    /**
     * Construit un tableau qui enveloppera les données <code>float[]</code> spécifiés.
     * Les données seront copiées, de sorte que les futures modifications apportées à
     * ce tableau ne modifieront pas les données originales.
     */
    public DynamicArray(final float[] array, final int lower, final int upper, final int extra) {
        super(new float[(upper-lower) + extra], lower, upper);
        assert (extra & 1) == 0 : extra;
        final int length = upper-lower;
        this.lower = extra/2;
        this.upper = this.lower + length;
        System.arraycopy(array, lower, this.array, this.lower, length);
    }

    /**
     * Retourne un tableau enveloppant les mêmes points que le tableau courant,
     * mais des index <code>lower</code> inclusivement jusqu'à <code>upper</code>
     * exclusivement. Si le sous-tableau ne contient aucun point (c'est-à-dire si
     * <code>lower==upper</code>), alors cette méthode retourne <code>null</code>.
     *
     * @param lower Index du premier point à prendre en compte.
     * @param upper Index suivant celui du dernier point à prendre en compte.
     */
    public PointArray subarray(int lower, int upper) {
        lower = 2*lower + this.lower;
        upper = 2*upper + this.lower;
        return getInstance(array, lower, upper, true);
    }

    /**
     * Insère les données (<var>x</var>,<var>y</var>) du tableau <code>toMerge</code> spécifié.
     * Si le drapeau <code>reverse</code> à la valeur <code>true</code>, alors les points de
     * <code>toMerge</code> seront copiées en ordre inverse.
     *
     * @param  index Index à partir d'où insérer les points dans ce tableau. Le point à cet
     *         index ainsi que tous ceux qui le suivent seront décalés vers des index plus élevés.
     * @param  toMerge Tableau de coordonnées (<var>x</var>,<var>y</var>) à insérer dans ce
     *         tableau de points. Ses valeurs seront copiées.
     * @param  lower Index de la première coordonnée de <code>toMerge</code> à copier dans ce tableau.
     * @param  upper Index suivant celui de la dernière coordonnée de <code>toMerge</code> à copier.
     * @param  reverse <code>true</code> s'il faut inverser l'ordre des points de <code>toMerge</code>
     *         lors de la copie. Cette inversion ne change pas l'ordre (<var>x</var>,<var>y</var>) des
     *         coordonnées de chaque points.
     *
     * @return <code>this</code> si l'insertion à pu être faite sur
     *         place, ou un autre tableau si ça n'a pas été possible.
     */
    public PointArray insertAt(int index, final float toMerge[],
                               final int mergeLower, final int mergeUpper, final boolean reverse)
    {
        int count = mergeUpper-mergeLower;
        if (count==0) {
            return this;
        }
        index  = 2*index + lower;
        final int     cLower = lower;
        final int     cUpper = upper;
        final int     cIndex = index;
        final float[] cArray = array;
        final boolean firstHalf = (cIndex < (cLower+cUpper)/2);
        /*
         * CAS 1: Les données sont ajoutées dans la première moitiée du tableau.
         *        Insère de la place en déplaçant les derniers points vers le début.
         *        Toutefois, s'il s'avère nécessaire d'agrandir le tableau, alors on
         *        en profitera pour ajouter un peu d'espace à la fin du tableau car
         *        ça pourrait servir plus tard...
         *
         * CAS 2: Les données sont ajoutées dans la dernière moitiée du tableau.
         *        Insère de la place en déplaçant les derniers points vers la fin,
         *        comme d'habitude. Toutefois, s'il s'avère nécessaire d'agrandir
         *        le tableau, alors on en profitera pour ajouter un peu d'espace
         *        au début du tableau car ça pourrait servir plus tard...
         */
        if (firstHalf) lower -= count;
        else           upper += count;
        if (lower<0 || upper>=cArray.length) {
            int offset = Math.max(1024, count);
            array  = new float[cArray.length + count + offset];
            offset = (offset/2) - cLower;
            lower  = cLower + offset;
            upper  = cUpper + offset + count;
            index  = cIndex + offset;
        }
        if (firstHalf) {
            if (array != cArray) {
                System.arraycopy(cArray, cIndex, array, index+count, cUpper-cIndex);
            }
            System.arraycopy(cArray, cLower, array, lower, cIndex-cLower);
            if (array == cArray) {
                index -= count;
            }
        } else {
            if (array != cArray) {
                System.arraycopy(cArray, cLower, array, lower, cIndex-cLower);
            }
            System.arraycopy(cArray, cIndex, array, index+count, cUpper-cIndex);
        }
        /*
         * Maintenant que de la place a été créée, copie les nouvelles données.
         * Durant la copie, on inversera l'ordre des points si ça avait été demandé.
         */
        if (reverse) {
            while ((count-=2) >= 0) {
                System.arraycopy(toMerge, mergeLower+count, array, index, 2);
                index += 2;
            }
        } else {
            System.arraycopy(toMerge, mergeLower, array, index, count);
        }
        return this;
    }

    /**
     * Inverse l'ordre des points dans le tableau <code>array</code> spécifié
     * entre les index <code>lower</code> et <code>upper</code>.
     */
    static void reverse(final float[] array, int lower, int upper) {
        assert (lower & 1) == 0;
        assert (upper & 1) == 0;
        while (lower < upper) {
            final float lowX=array[lower]; array[lower++]=array[upper-2];
            final float lowY=array[lower]; array[lower++]=array[--upper];
            array[  upper] = lowY;
            array[--upper] = lowX;
        }
    }

    /**
     * Renverse l'ordre de tous les points compris dans ce tableau.
     *
     * @return <code>this</code> si l'inversion a pu être faite sur-place,
     *         ou un autre tableau si ça n'a pas été possible.
     */
    public PointArray reverse() {
        reverse(array, lower, upper);
        return this;
    }

    /**
     * Retourne un tableau immutable qui contient les mêmes données que celui-ci.
     * Après l'appel de cette méthode, toute tentative de modification (avec les
     * méthodes {@link #insertAt} ou {@link #reverse}) vont retourner un autre
     * tableau de façon à ne pas modifier le tableau immutable.
     */
    public PointArray getFinal(final CompressionLevel level) {
        if (level==CompressionLevel.RELATIVE_AS_BYTES && count()>=8) {
            return new CompressedArray(array, lower, upper);
        }
        PointArray points = getInstance(array, lower, upper, true);
        if (points != null) {
            points = points.getFinal(level);
        }
        return points;
    }

    /**
     * Returns an estimation of memory usage in bytes. This method returns the same value
     * than {@link DefaultArray#getMemoryUsage}. This is not quite correct, since this
     * method may allocate more memory than needed for growing array. But this method is
     * just asked for an <em>estimation</em>.
     */
    public long getMemoryUsage() {
        return super.getMemoryUsage();
    }
}
