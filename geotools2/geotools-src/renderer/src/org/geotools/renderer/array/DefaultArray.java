/*
 * Geotools - OpenSource mapping toolkit
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

// Divers
import java.awt.geom.Point2D;
import org.geotools.resources.renderer.Resources;
import org.geotools.resources.renderer.ResourceKeys;


/**
 * Implémentation par défaut de {@link PointArray}. Cette classe enveloppe
 * un tableau de <code>float[]</code> sans utiliser quelle que compression
 * que ce soit. L'implémentation par défaut est imutable. Toutefois, certaines
 * classes dérivées (notamment {@link DynamicArray}) ne le seront pas forcément.
 *
 * @version $Id: DefaultArray.java,v 1.2 2003/01/20 00:06:34 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
class DefaultArray extends PointArray {
    /**
     * Numéro de série (pour compatibilité avec des versions antérieures).
     */
    private static final long serialVersionUID = 3160219929318094867L;

    /**
     * Tableaux des coordonnées à envelopper. Ces coordonnées
     * sont normalement mémorisées sous forme de paires (x,y).
     */
    protected float[] array;

    /**
     * Enveloppe le tableau <code>float[]</code> spécifié. Ce tableau
     * ne sera pas copié. Il devra obligatoirement avoir une longueur
     * paire.
     */
    public DefaultArray(final float[] array) {
        this.array=array;
        if ((array.length & 1) != 0) {
            throw new IllegalArgumentException(Resources.format(ResourceKeys.ERROR_ODD_ARRAY_LENGTH_$1,
                                               new Integer(array.length)));
        }
    }

    /**
     * Retourne l'index de la
     * première coordonnée valide.
     */
    protected int lower() {
        return 0;
    }

    /**
     * Retourne l'index suivant celui
     * de la dernière coordonnée valide.
     */
    protected int upper() {
        return array.length;
    }

    /**
     * Retourne le nombre de points dans ce tableau.
     */
    public final int count() {
        return (upper()-lower())/2;
    }

    /**
     * Returns an estimation of memory usage in bytes. This method count 8 bytes for each
     * (x,y) points plus 4 bytes for the internal fields (the {@link #array} reference).
     */
    public long getMemoryUsage() {
        return count()*8 + 4;
    }

    /**
     * Mémorise dans l'objet spécifié
     * les coordonnées du premier point.
     *
     * @param  point Point dans lequel mémoriser la coordonnée.
     * @return L'argument <code>point</code>, ou un nouveau point
     *         si <code>point</code> était nul.
     */
    public final Point2D getFirstPoint(final Point2D point) {
        final int lower=lower();
        assert lower <= upper();
        final float x = array[lower+0];
        final float y = array[lower+1];
        if (point != null) {
            point.setLocation(x,y);
            return point;
        } else {
            return new Point2D.Float(x,y);
        }
    }

    /**
     * Mémorise dans l'objet spécifié
     * les coordonnées du dernier point.
     *
     * @param  point Point dans lequel mémoriser la coordonnée.
     * @return L'argument <code>point</code>, ou un nouveau point
     *         si <code>point</code> était nul.
     */
    public final Point2D getLastPoint(final Point2D point) {
        final int upper=upper();
        assert(upper >= lower());
        final float x = array[upper-2];
        final float y = array[upper-1];
        if (point != null) {
            point.setLocation(x,y);
            return point;
        } else {
            return new Point2D.Float(x,y);
        }
    }

    /**
     * Retourne un itérateur qui balaiera les
     * points partir de l'index spécifié.
     */
    public final PointIterator iterator(final int index) {
        return new DefaultIterator(array, (2*index)+lower(), upper());
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
        final int thisLower=lower();
        final int thisUpper=upper();
        lower = lower*2 + thisLower;
        upper = upper*2 + thisLower;
        if (lower            == upper              ) return null;
        if (lower==thisLower && upper==thisUpper   ) return this;
        if (lower==0         && upper==array.length) return new DefaultArray(array);
        return new SubArray(array, lower, upper);
    }

    /**
     * Insère les données de <code>this</code> dans le tableau spécifié. Cette méthode est
     * strictement réservée à l'implémentation de {@link #insertAt(int,PointArray,boolean)}.
     * La classe {@link DefaultArray} remplace l'implémentation par défaut par une nouvelle
     * implémentation qui évite de copier les données avec {@link #toArray()}.
     */
    PointArray insertTo(final PointArray dest, final int index, final boolean reverse) {
        return dest.insertAt(index, array, lower(), upper(), reverse);
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
    public PointArray insertAt(final int index, final float toMerge[],
                               final int lower, final int upper, final boolean reverse)
    {
        int count = upper-lower;
        if (count == 0) {
            return this;
        }
        return new DynamicArray(array, lower(), upper(),
                                count+Math.min(count, 256)).insertAt(index, toMerge, lower, upper, reverse);
    }

    /**
     * Renverse l'ordre de tous les points compris dans ce tableau.
     *
     * @return <code>this</code> si l'invertion à pu être faite sur-place,
     *         ou un autre tableau si ça n'a pas été possible.
     */
    public PointArray reverse() {
        return new DynamicArray(array, lower(), upper(), 16).reverse();
    }

    /**
     * Retourne un tableau immutable qui contient les mêmes données que celui-ci.
     * Après l'appel de cette méthode, toute tentative de modification (avec les
     * méthodes {@link #insertAt} ou {@link #reverse}) vont retourner un autre
     * tableau de façon à ne pas modifier le tableau immutable.
     *
     * @param  compress <code>true</code> si l'on souhaite aussi comprimer les
     *         données. Cette compression peut se traduire par une plus grande
     *         lenteur lors des accès aux données.
     * @return Tableau immutable et éventuellement compressé, <code>this</code>
     *         si ce tableau répondait déjà aux conditions ou <code>null</code>
     *         si ce tableau ne contient aucune donnée.
     */
    public PointArray getFinal(final boolean compress) {
        if (compress && count() >= 8) {
            return new CompressedArray(array, lower(), upper());
        }
        return super.getFinal(compress);
    }

    /**
     * Retourne une copie des données de ce tableau. Toutes les données seront copiées
     * dans le tableau <code>copy</code> à partir de l'index <code>offset</code>.   Si
     * l'argument <code>n</code> est supérieur à 1, alors une décimation sera faîte en
     * ne retenant qu'un point sur <code>n</code>. Le tableau <code>copy</code> doit
     * avoir une longueur d'au moins <code>offset + ceil({@link #length}/n)</code>.
     *
     * @param  copy Tableau dans lequel copier les coordonnées. Si cet argument est nul,
     *         alors cette méthode se contentera de calculer la longueur minimale que
     *         devrait avoir le tableau <code>copy</code>.
     * @param  offset Index du premier élément de <code>copy</code>
     *         dans lequel copier la première coordonnée <var>x</var>.
     * @param  n Décimation à effectuer (1 pour n'en effectuer aucune).
     * @return Index suivant celui de la dernière coordonnée <var>y</var>
     *         copiée dans le tableau <code>copy</code>.
     */
    public final int toArray(final float[] copy, int offset, int n) {
        if (n < 1) {
            throw new IllegalArgumentException(String.valueOf(n));
        }
        if (copy == null) {
            int count = count();
            count = (count+(n-1)) / n;
            return offset + 2*count;
        }
        if (n == 1) {
            final int lower  = lower();
            final int length = upper()-lower;
            System.arraycopy(array, lower, copy, offset, length);
            return offset+length;
        } else {
            n *= 2;
            final int upper=upper();
            for (int i=lower(); i<upper; i+=n) {
                System.arraycopy(array, i, copy, offset, 2);
                offset += 2;
            }
            return offset;
        }
    }
}
