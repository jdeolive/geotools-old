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

// Divers
import java.awt.geom.Point2D;
import java.io.Serializable;
import org.geotools.resources.Utilities;
import org.geotools.resources.renderer.Resources;
import org.geotools.resources.renderer.ResourceKeys;


/**
 * Classe de base des classes enveloppant un tableau de points (<var>x</var>,<var>y</var>).
 * Les accès aux éléments de ce tableaux ne peuvent pas être fait de façon aléatoires. Ils
 * doivent obligatoirement passer par un itérateur retourné par {@link #iterator}. Cette
 * limitation est nécessaire pour faciliter l'implémentation de certains algorithmes de
 * compression des données.
 * <br><br>
 * <strong>Note sur le vocabulaire employé:</strong> Dans la documentation de cette classe,
 * le terme <em>point</em> se réfère à une paire de coordonnées (<var>x</var>,<var>y</var>)
 * tandis que le terme  <em>coordonnée</em>  se réfère à une seule valeur  <var>x</var> ou
 * <var>y</var>  (en français, "ordonnée" est plutôt utilisé pour la coordonnée le long de
 * l'axe des <var>y</var>, la coordonnée le long de l'axe des <var>x</var> étant l'abscisse).
 * Pour un point situé à l'index <code>i</code>, les coordonnées <var>x</var> et <var>y</var>
 * correspondantes se trouvent aux index <code>2*i</code> et <code>2*i+1</code> respectivement.
 *
 * @version $Id: PointArray.java,v 1.8 2003/05/13 11:00:46 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public abstract class PointArray implements Serializable {
    /**
     * Numéro de série (pour compatibilité avec des versions antérieures).
     */
    private static final long serialVersionUID = 1281113806110831086L;

    /**
     * Retourne un tableau de points enveloppant le tableau de coordonnées
     * spécifié en argument. Si le tableau spécifié est nul ou de longueur
     * 0, alors cette méthode retourne <code>null</code>.
     *
     * @param  array Tableau de coordonnées (<var>x</var>,<var>y</var>).
     *         Ce tableau doit obligatoirement avoir une longueur paire.
     * @return Le tableau de points. Ce tableau ne sera pas affecté par
     *         les éventuelles modifications aux données du tableau
     *         <code>array</code>.
     */
    public static PointArray getInstance(float[] array) {
        if (array==null || array.length==0) {
            return null;
        }
        return new DefaultArray(array);
        // Le constructeur de 'DefaultArray' vérifiera
        // si le tableau est de longueur paire.
    }

    /**
     * Retourne un tableau de points enveloppant le tableau de coordonnées
     * spécifié en argument. Si le tablean ne contient aucun point, alors
     * cette méthode retourne <code>null</code>.
     *
     * @param  array Tableau de coordonnées (<var>x</var>,<var>y</var>).
     * @param  lower Index de la première coordonnées <var>x</var> à
     *         prendre en compte dans le tableau <code>array</code>.
     * @param  upper Index suivant celui de la dernière coordonnée <var>y</var> à
     *         prendre en compte dans le tableau <code>array</code>. La différence
     *         <code>upper-lower</code> doit obligatoirement être paire.
     * @return Le tableau de points. Ce tableau ne sera pas affecté par
     *         les éventuelles modifications aux données du tableau
     *         <code>array</code>.
     */
    public static PointArray getInstance(final float[] array, final int lower, final int upper) {
        checkRange(array, lower, upper);
        if (upper == lower) {
            return null;
        }
        final float[] newArray = new float[upper-lower];
        System.arraycopy(array, lower, newArray, 0, newArray.length);
        return new DefaultArray(newArray);
    }

    /**
     * Vérifie la validité des arguments spécifiés.
     *
     * @param  array Tableau de coordonnées (<var>x</var>,<var>y</var>).
     * @param  lower Index de la première coordonnées <var>x</var> à
     *         prendre en compte dans le tableau <code>array</code>.
     * @param  upper Index suivant celui de la dernière coordonnée <var>y</var> à
     *         prendre en compte dans le tableau <code>array</code>. La différence
     *         <code>upper-lower</code> doit obligatoirement être paire.
     * @throws IllegalArgumentException si la plage <code>[lower..upper]</code>
     *         n'est pas valide ou est en dehors des limites du tableau.
     */
    static void checkRange(final float[] array, final int lower, final int upper)
            throws IllegalArgumentException
    {
        assert (array.length & 1) == 0 : array.length;
        assert (lower        & 1) == 0 : lower;
        assert (upper        & 1) == 0 : upper;
        if (upper < lower) {
            throw new IllegalArgumentException(Resources.format(ResourceKeys.ERROR_BAD_RANGE_$2,
                                               new Integer(lower), new Integer(upper)));
        }
        if (((upper-lower)&1) !=0) {
            throw new IllegalArgumentException(Resources.format(ResourceKeys.ERROR_ODD_ARRAY_LENGTH_$1,
                                               new Integer(upper-lower)));
        }
        if (lower < 0) {
            throw new ArrayIndexOutOfBoundsException(lower);
        }
        if (upper > array.length) {
            throw new ArrayIndexOutOfBoundsException(upper);
        }
    }

    /**
     * Constructeur par défaut.
     */
    protected PointArray() {
    }

    /**
     * Returns the index of the first valid ordinate.
     *
     * This method is overriden by all <code>PointArray</code> subclasses in this package.
     * Note that this method is not <code>protected</code> in this <code>PointArray</code>
     * class because it is used only by {@link #capacity}, which is a package-private helper
     * method for {@link #toArray} implementations only.
     */
    int lower() {
        return 0;
    }

    /**
     * Returns the index after the last valid ordinate.
     *
     * This method is overriden by all <code>PointArray</code> subclasses in this package.
     * Note that this method is not <code>protected</code> in this <code>PointArray</code>
     * class because it is used only by {@link #capacity}, which is a package-private helper
     * method for {@link #toArray} implementations only.
     */
    int upper() {
        return 2*count();
    }

    /**
     * Retourne le nombre de points dans ce tableau.
     */
    public abstract int count();

    /**
     * Returns an estimation of memory usage in bytes. This method is for information
     * purpose only. The memory used by this array may be shared with an other array,
     * resulting in a total memory consumption lower than the sum of <code>getMemoryUsage()</code>
     * return values. Furthermore, this method do not take in account the extra bytes
     * generated by Java Virtual Machine for each objects.
     *
     * @return An <em>estimation</em> of memory usage in bytes.
     */
    public abstract long getMemoryUsage();

    /**
     * Mémorise dans l'objet spécifié
     * les coordonnées du premier point.
     *
     * @param  point Point dans lequel mémoriser la coordonnée.
     * @return L'argument <code>point</code>, ou un nouveau point
     *         si <code>point</code> était nul.
     */
    public abstract Point2D getFirstPoint(final Point2D point);

    /**
     * Mémorise dans l'objet spécifié
     * les coordonnées du dernier point.
     *
     * @param  point Point dans lequel mémoriser la coordonnée.
     * @return L'argument <code>point</code>, ou un nouveau point
     *         si <code>point</code> était nul.
     */
    public abstract Point2D getLastPoint(final Point2D point);

    /**
     * Retourne un itérateur qui balaiera les
     * points partir de l'index spécifié.
     */
    public abstract PointIterator iterator(final int index);

    /**
     * Retourne un tableau enveloppant les mêmes points que le tableau courant,
     * mais des index <code>lower</code> inclusivement jusqu'à <code>upper</code>
     * exclusivement. Si le sous-tableau ne contient aucun point (c'est-à-dire si
     * <code>lower==upper</code>), alors cette méthode retourne <code>null</code>.
     *
     * @param lower Index du premier point à prendre en compte.
     * @param upper Index suivant celui du dernier point à prendre en compte.
     */
    public abstract PointArray subarray(final int lower, final int upper);

    /**
     * Insère tous les points de <code>toMerge</code> dans le tableau <code>this</code>.
     * Si le drapeau <code>reverse</code> à la valeur <code>true</code>, alors les points
     * de <code>toMerge</code> seront copiées en ordre inverse.
     *
     * @param  index Index à partir d'où insérer les points dans ce tableau. Le point à cet
     *         index ainsi que tous ceux qui le suivent seront décalés vers des index plus élevés.
     * @param  toMerge Tableau de points à insérer. Ses valeurs seront copiées.
     */
    public final PointArray insertAt(final int index, final PointArray toMerge, final boolean reverse) {
        return toMerge.insertTo(this, index, reverse);
    }

    /**
     * Insère les données de <code>this</code> dans le tableau spécifié. Cette méthode est
     * strictement réservée à l'implémentation de {@link #insertAt(int,PointArray,boolean)}.
     * La classe {@link DefaultArray} remplace l'implémentation par défaut par une nouvelle
     * implémentation qui évite de copier les données avec {@link #toArray()}.
     */
    PointArray insertTo(final PointArray dest, final int index, final boolean reverse) {
        final float[] array = toArray();
        return dest.insertAt(index, array, 0, array.length, reverse);
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
    public abstract PointArray insertAt(final int index, final float toMerge[],
                                        final int lower, final int upper, final boolean reverse);

    /**
     * Renverse l'ordre de tous les points compris dans ce tableau.
     *
     * @return <code>this</code> si l'inversion a pu être faite sur-place,
     *         ou un autre tableau si ça n'a pas été possible.
     */
    public abstract PointArray reverse();

    /**
     * Retourne un tableau immutable qui contient les mêmes données que celui-ci.
     * Après l'appel de cette méthode, toute tentative de modification (avec les
     * méthodes {@link #insertAt} ou {@link #reverse}) vont retourner un autre
     * tableau de façon à ne pas modifier le tableau immutable.
     *
     * @param  compress <code>true</code> si l'on souhaite aussi comprimer les
     *         données. Cette compression peut se traduire par une plus grande
     *         lenteur lors des accès aux données, ainsi qu'une perte de précision.
     * @return Tableau immutable et éventuellement compressé, <code>this</code>
     *         si ce tableau répondait déjà aux conditions ou <code>null</code>
     *         si ce tableau ne contient aucune donnée.
     */
    public PointArray getFinal(final boolean compress) {
        return count()>0 ? this : null;
    }

    /**
     * Append (<var>x</var>,<var>y</var>) coordinates to the specified destination array.
     * The destination array will be filled starting at index {@link ArrayData#length}.
     * If <code>resolution2</code> is greater than 0, then points that are closer than
     * <code>sqrt(resolution2)</code> from previous one will be skiped.
     *
     * @param  The destination array. The coordinates will be filled in
     *         {@link ArrayData#array}, which will be expanded if needed.
     *         After this method completed, {@link ArrayData#length} will
     *         contains the index after the <code>array</code>'s element
     *         filled with the last <var>y</var> ordinate.
     * @param  resolution2 The minimum squared distance desired between points.
     *
     * @task REVISIT: Current implementations compute distance using Pythagoras formulas, which
     *                is okay for projected coordinates but not right for geographic (longitude
     *                / latitude) coordinates. This is not a real problem when the rendering CS
     *                is the same than the data CS,  since the decimation performed here target
     *                specifically the rendering device  (the important thing is that distances
     *                look okay to user's eyes). However, it may be a problem when the rendering
     *                CS is different, since points that are equidistant in the data CS may not
     *                be equidistant in the rendering CS.
     */
    public abstract void toArray(ArrayData dest, float resolution2);

    /**
     * Retourne une copie de toutes les coordonnées (<var>x</var>,<var>y</var>) de ce tableau.
     */
    public final float[] toArray() {
        final ArrayData data = new ArrayData(2*count());
        toArray(data, 0);
        assert data.length == data.array.length;
        return data.array;
    }

    /**
     * Used by {@link #toArray}  implementations in order to expand the destination array
     * as needed.   This method is invoking when <code>toArray</code> has already started
     * to fill the destination array. The <code>src</code> and <code>dst</code> arguments
     * refer to the current position in the copy loop.  This method try to guess the size
     * that the destination array should have based on the proportion of the array filled
     * up to date, and conservatively expand its guess by about 12%.
     *
     * @param  src The index position in the source array.
     * @param  dst The index position in the destination array.
     * @param  offset The first element to be filled in the destination array.
     * @return A guess of the required length for completing the array filling.
     */
    final int capacity(int src, int dst, final int offset) {
        final int lower  = lower();
        final int length = upper() - lower;
        int guess;
        dst -= offset;
        src -= lower;
        assert (src & 1) == 0 : src;
        assert (dst & 1) == 0 : dst;
        if (src == 0) {
            guess = length / 8;
        } else {
            guess = (int)(dst * (long)length / src);  // Prediction of the total length required.
            guess -= dst;                             // The amount to growth.
            guess += guess/8;                         // Conservatively add some space.
        }
        guess &= ~1; // Make sure the length is even.
        return offset + Math.min(length, dst+Math.max(guess, 32));
    }

    /**
     * Retourne une chaîne de caractères représentant ce tableau. Cette chaîne
     * contiendra le nom de la classe utilisée, le nombre de points ainsi que
     * les points de départ et d'arrivé.
     */
    public final String toString() {
        final Point2D.Float point=new Point2D.Float();
        final StringBuffer buffer=new StringBuffer(Utilities.getShortClassName(this));
        final int count=count();
        buffer.append('[');
        buffer.append(count);
        buffer.append(" points");
        if (count!=0) {
            getFirstPoint(point);
            buffer.append(" (");
            buffer.append(point.x);
            buffer.append(", ");
            buffer.append(point.y);
            buffer.append(")-(");

            getLastPoint(point);
            buffer.append(point.x);
            buffer.append(", ");
            buffer.append(point.y);
            buffer.append(')');
        }
        buffer.append(']');
        return buffer.toString();
    }

    /**
     * Indique si ce tableau est identique au tableau spécifié. Deux
     * tableaux seront considérés identiques s'ils contiennent les
     * mêmes points dans le même ordre.
     */
    public final boolean equals(final PointArray that) {
        if (that==this) return true;
        if (that==null) return false;
        if (this.count() != that.count()) {
            return false;
        }
        final PointIterator it1 = this.iterator(0);
        final PointIterator it2 = that.iterator(0);
        while (it1.hasNext()) {
            if (!it2.hasNext() ||
                Float.floatToIntBits(it1.nextX()) != Float.floatToIntBits(it2.nextX()) ||
                Float.floatToIntBits(it1.nextY()) != Float.floatToIntBits(it2.nextY())) return false;
        }
        return !it2.hasNext();
    }

    /**
     * Indique si cet objet est identique à l'objet spécifié.   Cette méthode considère deux
     * objets identiques si <code>that</code> est d'une classe dérivée de {@link PointArray}
     * et si les deux tableaux contiennent les mêmes points dans le même ordre.
     */
    public final boolean equals(final Object that) {
        return (that instanceof PointArray) && equals((PointArray) that);
    }

    /**
     * Retourne un code représentant cet objet.
     */
    public final int hashCode() {
        final Point2D point = getFirstPoint(null);
        return count() ^ Float.floatToIntBits((float)point.getX())
                       ^ Float.floatToIntBits((float)point.getY());
    }
}
