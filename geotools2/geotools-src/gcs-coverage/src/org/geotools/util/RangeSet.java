/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Management Committee (PMC)
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
package org.geotools.util;

// Collections
import java.util.Arrays;
import java.util.SortedSet;
import java.util.AbstractSet;
import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.ConcurrentModificationException;

// Miscellaneous
import java.io.Serializable;
import java.lang.reflect.Array;
import javax.media.jai.util.Range;

// Geotools dependencies
import org.geotools.util.Cloneable;
import org.geotools.resources.Utilities;
import org.geotools.resources.ClassChanger;
import org.geotools.resources.rsc.Resources;
import org.geotools.resources.rsc.ResourceKeys;


/**
 * An ordered set of ranges. <code>RangeSet</code> objects store an arbitrary number of
 * {@linkplain Range ranges} in any Java's primitives (<code>int</code>, <code>float</code>,
 * etc.) or any {@linkplain Comparable comparable} objects. Ranges may be added in any order.
 * When a range is added, <code>RangeSet</code> first looks for an existing range overlapping the
 * specified range. If an overlapping range is found, ranges are merged as of {@link Range#union}.
 * Consequently, ranges returned by {@link #iterator} may not be the same than added ranges.
 * <br><br>
 * All entries in this set can be seen as {@link Range} objects.
 * This class is not thread-safe.
 *
 * @version $Id: RangeSet.java,v 1.7 2003/08/28 15:41:57 desruisseaux Exp $
 * @author Martin Desruisseaux
 * @author Andrea Aime
 */
public class RangeSet extends AbstractSet implements SortedSet, Cloneable, Serializable {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 3222336180818126987L;

    /**
     * The comparator for ranges. Defined only in order to comply to {@link #comparator}
     * contract, but not used for internal working in this class.
     */
    private static final Comparator COMPARATOR = new Comparator() {
        public int compare(final Object o1, final Object o2) {
            final Range r1 = (Range) o1;
            final Range r2 = (Range) o2;
            int cmin = r1.getMinValue().compareTo(r2.getMinValue());
            int cmax = r1.getMaxValue().compareTo(r2.getMaxValue());
            if (cmin == 0) cmin = (r1.isMinIncluded() ? -1 : 0) - (r2.isMinIncluded() ? -1 : 0);
            if (cmax == 0) cmax = (r1.isMaxIncluded() ? +1 : 0) - (r2.isMaxIncluded() ? +1 : 0);
            if (cmin==cmax) return cmax; // Easy case: min and max are both greater, smaller or eq.
            if (cmin==0)    return cmax; // Easy case: only max value differ.
            if (cmax==0)    return cmin; // Easy case: only min value differ.
            // One range is included in the other.
            throw new IllegalArgumentException("Unordered ranges");
        }
    };

    /**
     * Tableau de correspondances  entre  les type primitifs
     * et leurs "wrappers". Les classes aux index pairs sont
     * les types primitifs, tandis que les classes aux index
     * impairs sont leurs "wrappers".
     */
    private static final Class[] PRIMITIVES = {
        Double   .TYPE,    Double   .class,
        Float    .TYPE,    Float    .class,
        Long     .TYPE,    Long     .class,
        Integer  .TYPE,    Integer  .class,
        Short    .TYPE,    Short    .class,
        Byte     .TYPE,    Byte     .class,
        Character.TYPE,    Character.class
    };

    /**
     * The primitive types, as an index in the {@link #PRIMITIVES} array divided by 2.
     */
    private static final byte DOUBLE=0, FLOAT=1, LONG=2, INTEGER=3, SHORT=4, BYTE=5, CHARACTER=6,
                              OTHER = -1;

    /**
     * Le type des données de l'intervalle.  Il s'agit du type
     * qui sera spécifié aux objets {@link Range} représentant
     * un intervalle.
     */
    private final Class type;

    /**
     * Ce champ a une valeur identique à <code>type</code>, sauf
     * si <code>elementType</code> est un type primitif. Dans ce
     * cas, il sera <code>{@link Number}.class</code>.
     */
    private final Class relaxedType;

    /**
     * Le type des données utilisé dans le tableau <code>array</code>.
     * Il s'agira souvent du même type que <code>type</code>, sauf si
     * ce dernier était le "wrapper" d'un des types primitifs du Java.
     * Dans ce cas, <code>elementType</code> sera ce type primitif.
     */
    private final Class elementType;

    /**
     * The primitive type, as one of <code>DOUBLE</code>, <code>FLOAT</code>, <code>LONG</code>,
     * <code>INTEGER</code>, <code>SHORT</code>, <code>BYTE</code>, <code>CHARACTER</code> or
     * <code>OTHER</code> enumeration.
     */
    private final byte indexType;

    /**
     * Tableau d'intervalles.   Il peut s'agir d'un tableau d'un des types primitifs
     * du Java   (par exemple <code>int[]</code> ou <code>float[]</code>),   ou d'un
     * tableau de type <code>Comparable[]</code>. Les éléments de ce tableau doivent
     * obligatoirement être en ordre strictement croissant et sans doublon.
     * <br><br>
     * La longueur de ce tableau est le double du nombre d'intervalles.  Il aurait
     * été plus efficace d'utiliser une variable séparée  (pour ne pas être obligé
     * d'agrandir ce tableau à chaque ajout d'un intervalle), mais malheureusement
     * le J2SE 1.4 ne nous fournit pas de méthode <code>Arrays.binarySearch</code>
     * qui nous permettent de spécifier les limites du tableau  (voir RFE #4306897
     * à http://developer.java.sun.com/developer/bugParade/bugs/4306897.html).
     */
    private Object array;

    /**
     * Compte le nombre de modifications apportées au tableau des intervalles.
     * Ce comptage sert à vérifier si une modification survient pendant qu'un
     * itérateur balayait les intervalles.
     */
    private int modCount;

    /**
     * <code>true</code> if and only if the element class represents a primitive type.
     * This is equivalents to <code>elementType.isPrimitive()</code> and is computed
     * once for ever for performance reason.
     */
    private final boolean isPrimitive;

    /**
     * <code>true</code> if we should invoke {@link ClassChanger#toNumber}
     * before to store a value into the array. It will be the case if the
     * array <code>array</code> contains primitive elements and the type
     * <code>type</code> is not the corresponding wrapper.
     */
    private final boolean useClassChanger;

    /**
     * <code>true</code> if instances of {@link NumberRange} should be created instead
     * of {@link Range}.
     */
    private final boolean isNumeric;

    /**
     * Construct an empty set of range.
     *
     * @param  type The class of the range elements. It must be a primitive
     *         type or a class implementing {@link Comparable}.
     * @throws IllegalArgumentException if <code>type</code> is not a
     *         primitive type or a class implementing {@link Comparable}.
     */
    public RangeSet(Class type) throws IllegalArgumentException {
        // If 'type' is a primitive type,
        // find the corresponding wrapper.
        byte indexType = OTHER;
        for (int i=0; i<PRIMITIVES.length; i+=2) {
            if (PRIMITIVES[i].equals(type)) {
                type = PRIMITIVES[i+1];
                indexType = (byte) (i/2);
                break;
            }
        }
        if (!Comparable.class.isAssignableFrom(type)) {
            throw new IllegalArgumentException(Resources.format(
                      ResourceKeys.ERROR_NOT_COMPARABLE_CLASS_$1,
                      Utilities.getShortClassName(type)));
        }
        Class elementType = ClassChanger.getTransformedClass(type); // e.g. change Date --> Long
        useClassChanger   = (elementType != type);
        // If 'elementType' is a wrapper class,
        // find the corresponding primitive type.
        for (int i=0; i<PRIMITIVES.length; i+=2) {
            if (PRIMITIVES[i+1].equals(elementType)) {
                elementType = PRIMITIVES[i];
                indexType = (byte) (i/2);
                break;
            }
        }
        this.type        = type;
        this.indexType   = indexType;
        this.elementType = elementType;
        this.isPrimitive = elementType.isPrimitive();
        this.isNumeric   = Number.class.isAssignableFrom(type);
        this.relaxedType = isNumeric ? Number.class : type;
    }

    /**
     * Returns the comparator associated with this sorted set.
     */
    public Comparator comparator() {
        return COMPARATOR;
    }

    /**
     * Remove all elements from this set of ranges.
     */
    public void clear() {
        array = null;
        modCount++;
    }

    /**
     * Returns the number of ranges in this set.
     */
    public int size() {
        return (array!=null) ? Array.getLength(array)/2 : 0;
    }

    /**
     * Add a range to this set. Range may be added in any order.
     * If the specified range overlap an existing range, the two
     * range will be merged as of {@link Range#union}.
     * <br><br>
     * Note: current version do not support open interval (i.e.
     *       <code>Range.is[Min/Max]Included()</code> must return
     *       <code>true</code>). It may be fixed in a future version.
     *
     * @param r The range to add. The <code>RangeSet</code> class
     *          will never modify the supplied {@link Range} object.
     * @return <code>true</code> if this set changed as a result of the call.
     * @throws ClassCastException if the argument is not a {@link Range} object.
     *
     * @task TODO: support open intervals.
     */
    public boolean add(final Object r) throws ClassCastException {
        final Range range = (Range) r;
        if (!range.isMinIncluded() || !range.isMaxIncluded()) {
            // TODO: support open intervals.
            throw new UnsupportedOperationException("Open interval not yet supported");
        }
        return add(range.getMinValue(), range.getMaxValue());
    }

    /**
     * Add a range of values to this set. Range may be added in any order.
     * If the specified range overlap an existing range, the two ranges
     * will be merged.
     *
     * @param  lower The lower value, inclusive.
     * @param  upper The upper value, inclusive.
     * @return <code>true</code> if this set changed as a result of the call.
     * @throws IllegalArgumentException if <code>lower</code> is greater than <code>upper</code>.
     */
    public boolean add(Comparable lower, Comparable upper) throws IllegalArgumentException {
        if (!relaxedType.isAssignableFrom(lower.getClass())) {
            throw new IllegalArgumentException(String.valueOf(lower));
        }
        if (!relaxedType.isAssignableFrom(upper.getClass())) {
            throw new IllegalArgumentException(String.valueOf(upper));
        }
        if (lower.compareTo(upper) > 0) {
            throw new IllegalArgumentException(Resources.format(
                      ResourceKeys.ERROR_BAD_RANGE_$2, lower, upper));
        }
        if (useClassChanger) {
            try {
                lower = (Comparable)ClassChanger.toNumber(lower);
                upper = (Comparable)ClassChanger.toNumber(upper);
            } catch (ClassNotFoundException exception) {
                // Should not happen, since this operation is legal according the constructor.
                final ClassCastException e=new ClassCastException(exception.getLocalizedMessage());
                e.initCause(exception);
                throw e;
            }
        }
        if (array == null) {
            modCount++;
            array = Array.newInstance(elementType, 2);
            Array.set(array, 0, lower);
            Array.set(array, 1, upper);
            return true;
        }
        final int modCountChk = modCount;
        int i0 = binarySearch(lower);
        int i1;
        if (i0 < 0) {
            /*
             * Si le début de la plage ne correspond pas à une des dates en
             * mémoire, il faudra l'insérer à quelque part dans le tableau.
             * Si la date tombe dans une des plages déjà existantes (si son
             * index est impair), on étend la date de début pour prendre le
             * début de la plage. Visuellement, on fait:
             *
             *   0   1     2      3     4   5    6     7
             *   #####     ########     #####    #######
             *             <---^           ^
             *             lower(i=3)   upper(i=5)
             */
            if (((i0=~i0) & 1) != 0) { // Attention: c'est ~ et non -
                lower = (Comparable)Array.get(array, --i0);
                i1 = binarySearch(upper);
            } else {
                /*
                 * Si la date de début ne tombe pas dans une plage déjà
                 * existante, il faut étendre la valeur de début qui se
                 * trouve dans le tableau. Visuellement, on fait:
                 *
                 *   0   1     2      3     4   5    6     7
                 *   #####  ***########     #####    #######
                 *          ^                 ^
                 *       lower(i=2)        upper(i=5)
                 */
                if (i0!=Array.getLength(array) && (i1=binarySearch(upper))!= ~i0) {
                    modCount++;
                    Array.set(array, i0, lower);
                } else {
                    /*
                     * Un cas particulier se produit si la nouvelle plage
                     * est à insérer à la fin du tableau. Dans ce cas, on
                     * n'a qu'à agrandir le tableau et écrire les valeurs
                     * directement à la fin. Ce traitement est nécessaire
                     * pour eviter les 'ArrayIndexOutOfBoundsException'.
                     * Un autre cas particulier se produit si la nouvelle
                     * plage est  entièrement  comprise entre deux plages
                     * déjà existantes.  Le même code ci-dessous insèrera
                     * la nouvelle plage à l'index 'i0'.
                     */
                    modCount++;
                    final Object old = array;
                    final int length = Array.getLength(array);
                    array = Array.newInstance(elementType, length+2);
                    System.arraycopy(old,  0, array,  0,          i0);
                    System.arraycopy(old, i0, array, i0+2, length-i0);
                    Array.set(array, i0+0, lower);
                    Array.set(array, i0+1, upper);
                    return true;
                }
            }
        } else {
            i0 &= ~1;
            i1 = binarySearch(upper);
        }
        /*
         * A ce stade, on est certain que 'i0' est pair et pointe vers le début
         * de la plage dans le tableau. Fait maintenant le traitement pour 'i1'.
         */
        if (i1 < 0) {
            /*
             * Si la date de fin tombe dans une des plages déjà existantes
             * (si son index est impair), on l'étend pour pendre la fin de
             * la plage trouvée dans le tableau. Visuellement, on fait:
             *
             *   0   1     2      3     4   5    6     7
             *   #####     ########     #####    #######
             *             ^             ^-->
             *          lower(i=2)     upper(i=5)
             */
            if (((i1=~i1) & 1) != 0) { // Attention: c'est ~ et non -
                upper = (Comparable)Array.get(array, i1);
            } else {
                /*
                 * Si la date de fin ne tombe pas dans une plage déjà
                 * existante, il faut étendre la valeur de fin qui se
                 * trouve dans le tableau. Visuellement, on fait:
                 *
                 *   0   1     2      3     4   5    6     7
                 *   #####     ########     #####**  #######
                 *             ^                  ^
                 *          lower(i=2)         upper(i=6)
                 */
                modCount++;
                Array.set(array, --i1, upper);
            }
        } else {
            i1 |= 1;
        }
        /*
         * A ce stade, on est certain que 'i1' est impair et pointe vers la fin
         * de la plage dans le tableau. On va maintenant supprimer tout ce qui
         * se trouve entre 'i0' et 'i1', à l'exclusion de 'i0' et 'i1'.
         */
        assert (i0 & 1)==0 : i0;
        assert (i1 & 1)!=0 : i1;
        final int n = i1 - (++i0);
        if (n > 0) {
            modCount++;
            final Object old = array;
            final int length = Array.getLength(array);
            array = Array.newInstance(elementType, length-n);
            System.arraycopy(old,  0, array,  0, i0);
            System.arraycopy(old, i1, array, i0, length-i1);
        }
        assert (Array.getLength(array) & 1)==0;
        return modCountChk != modCount;
    }

    /**
     * Add a range of values to this set. Range may be added in any order.
     * If the specified range overlap an existing range, the two ranges
     * will be merged.
     *
     * @param  lower The lower value, inclusive.
     * @param  upper The upper value, inclusive.
     * @return <code>true</code> if this set changed as a result of the call.
     * @throws IllegalArgumentException if <code>lower</code> is greater than <code>upper</code>.
     */
    public boolean add(byte lower, byte upper) throws IllegalArgumentException {
        return add(new Byte(lower), new Byte(upper));
    }

    /**
     * Add a range of values to this set. Range may be added in any order.
     * If the specified range overlap an existing range, the two ranges
     * will be merged.
     *
     * @param  lower The lower value, inclusive.
     * @param  upper The upper value, inclusive.
     * @return <code>true</code> if this set changed as a result of the call.
     * @throws IllegalArgumentException if <code>lower</code> is greater than <code>upper</code>.
     */
    public boolean add(short lower, short upper) throws IllegalArgumentException {
        return add(new Short(lower), new Short(upper));
    }

    /**
     * Add a range of values to this set. Range may be added in any order.
     * If the specified range overlap an existing range, the two ranges
     * will be merged.
     *
     * @param  lower The lower value, inclusive.
     * @param  upper The upper value, inclusive.
     * @return <code>true</code> if this set changed as a result of the call.
     * @throws IllegalArgumentException if <code>lower</code> is greater than <code>upper</code>.
     */
    public boolean add(int lower, int upper) throws IllegalArgumentException {
        return add(new Integer(lower), new Integer(upper));
    }

    /**
     * Add a range of values to this set. Range may be added in any order.
     * If the specified range overlap an existing range, the two ranges
     * will be merged.
     *
     * @param  lower The lower value, inclusive.
     * @param  upper The upper value, inclusive.
     * @return <code>true</code> if this set changed as a result of the call.
     * @throws IllegalArgumentException if <code>lower</code> is greater than <code>upper</code>.
     */
    public boolean add(long lower, long upper) throws IllegalArgumentException {
        return add(new Long(lower), new Long(upper));
    }

    /**
     * Add a range of values to this set. Range may be added in any order.
     * If the specified range overlap an existing range, the two ranges
     * will be merged.
     *
     * @param  lower The lower value, inclusive.
     * @param  upper The upper value, inclusive.
     * @return <code>true</code> if this set changed as a result of the call.
     * @throws IllegalArgumentException if <code>lower</code> is greater than <code>upper</code>.
     */
    public boolean add(float lower, float upper) throws IllegalArgumentException {
        return add(new Float(lower), new Float(upper));
    }

    /**
     * Add a range of values to this set. Range may be added in any order.
     * If the specified range overlap an existing range, the two ranges
     * will be merged.
     *
     * @param  lower The lower value, inclusive.
     * @param  upper The upper value, inclusive.
     * @return <code>true</code> if this set changed as a result of the call.
     * @throws IllegalArgumentException if <code>lower</code> is greater than <code>upper</code>.
     */
    public boolean add(double lower, double upper) throws IllegalArgumentException {
        return add(new Double(lower), new Double(upper));
    }
    
    /**
     * Remove a range of values from this set. Range may be removed in any order.
     *
     * @param lower The lower value to remove, exclusive.
     * @param upper The upper value to remove, exclusive.
     * @return <code>true</code> if this set changed as a result of the call.
     * @throws IllegalArgumentException if <code>lower</code> is greater than <code>upper</code>.
     */
    public boolean remove(Comparable lower, Comparable upper) throws IllegalArgumentException {
        if (!relaxedType.isAssignableFrom(lower.getClass())) {
            throw new IllegalArgumentException(String.valueOf(lower));
        }
        if (!relaxedType.isAssignableFrom(upper.getClass())) {
            throw new IllegalArgumentException(String.valueOf(upper));
        }
        if (lower.compareTo(upper) >= 0) {
            throw new IllegalArgumentException(Resources.format(
                      ResourceKeys.ERROR_BAD_RANGE_$2, lower, upper));
        }
        if (useClassChanger) {
            try {
                lower = (Comparable) ClassChanger.toNumber(lower);
                upper = (Comparable) ClassChanger.toNumber(upper);
            } catch (ClassNotFoundException exception) {
                // Should not happen, since this operation is legal according the constructor.
                final ClassCastException e = new ClassCastException(exception.getLocalizedMessage());
                e.initCause(exception);
                throw e;
            }
        }
        // if already empty, or range outside the current set, nothing to change
        if (array == null) {
            return false;
        } 
        final int modCountChk = modCount;
        int i0 = binarySearch(lower);
        int i1 = binarySearch(upper);
        if (i0 < 0) {
            if (((i0 = ~i0) & 1) != 0) { // Attention: c'est ~ et non -
                /*
                 * Si le début de la plage ne correspond pas à une des dates en mémoire,
                 * il faudra faire un trou à quelque part dans le tableau. Si la date tombe
                 * dans une des plages déjà existantes (si son index est impair), on change
                 * la date de fin de la plage existante. Visuellement, on fait:
                 *
                 *   0   1     2      3     4   5    6     7
                 *   #####     #####---     --###    #######
                 *                 ^          ^
                 *             lower(i=3)   upper(i=5)
                 */
                modCount++;
                if (i1 != ~i0) {
                    Array.set(array, i0, lower);
                } else {
                    /*
                     * Special case if the upper index is inside the same range than the lower one:
                     *
                     *   0   1     2                3     4   5
                     *   #####     ####---------#####     #####
                     *                ^         ^
                     *           lower(i=3)   upper(i=3)
                     */
                    final Object old = array;
                    final int length = Array.getLength(array);
                    array = Array.newInstance(elementType, length + 2);
                    System.arraycopy(old, 0,  array, 0, i0);
                    System.arraycopy(old, i0, array, i0 + 2, length - i0);
                    Array.set(array, i0 + 0, lower);
                    Array.set(array, i0 + 1, upper);
                    return true;
                }
            } else {
                /*
                 * Si la date de début ne tombe pas dans une plage déjà
                 * existante, il faut prendre la date de fin de la plage
                 * précédente. Visuellement, on fait:
                 *
                 *   0   1     2      3     4   5    6     7
                 *   #####     ########     #####    #######
                 *       <---^                  ^
                 *       lower(i=2)        upper(i=5)
                 */
                i0--;
            }
        } else {
            if ((i0 & 1) == 0) {
                i0--;
            }
        }
        /*
         * A ce stade, on est certain que 'i0' est impair et pointe vers la fin
         * d'une plage dans le tableau. Fait maintenant le traitement pour 'i1'.
         */
        if (i1 < 0) {
            /*
             * Si la date de fin tombe dans une des plages déjà existantes
             * (si son index est impair), on change la date de début de la
             * plage existante. Visuellement, on fait:
             *
             *   0   1     2      3     4   5    6     7
             *   #####     ########     --###    #######
             *                    ^       ^
             *            lower(i=3)    upper(i=5)
             */
            if (((i1 = ~i1) & 1) != 0) { // Attention: c'est ~ et non -
                modCount++;
                Array.set(array, --i1, upper);
            } else {
                /*
                 * Si la date de fin ne tombe pas dans une plage déjà existante, il
                 * faudra (plus tard) supprimer les éventuelles plages qui le précède.
                 *
                 *   0   1     2      3        4     5        6         7
                 *   #####     ########        #######        ###########
                 *                    ^                  ^
                 *            lower(i=3)         upper(i=6)
                 */
                // nothing to do
            }
        } else {
            i1 &= ~1;
        }
        /*
         * A ce stade, on est certain que 'i1' est pair et pointe vers la début
         * de la plage dans le tableau. On va maintenant supprimer tout ce qui
         * se trouve entre 'i0' et 'i1', à l'exclusion de 'i0' et 'i1'.
         */
        assert (i0 & 1) != 0 : i0;
        assert (i1 & 1) == 0 : i1;
        final int n = i1 - (++i0);
        if (n > 0) {
            modCount++;
            final Object old = array;
            final int length = Array.getLength(array);
            array = Array.newInstance(elementType, length - n);
            System.arraycopy(old, 0, array, 0, i0);
            System.arraycopy(old, i1, array, i0, length - i1);
        }
        assert (Array.getLength(array) & 1) == 0;
        return modCountChk != modCount;
    }
    
    /**
     * Remove a range of values from this set. Range may be removed in any order.
     *
     * @param lower The lower value to remove, exclusive.
     * @param upper The upper value to remove, exclusive.
     * @return <code>true</code> if this set changed as a result of the call.
     * @throws IllegalArgumentException if <code>lower</code> is greater than <code>upper</code>.
     */
    public boolean remove(byte lower, byte upper) throws IllegalArgumentException {
        return remove(new Byte(lower), new Byte(upper));
    }
    
    /**
     * Remove a range of values from this set. Range may be removed in any order.
     *
     * @param lower The lower value to remove, exclusive.
     * @param upper The upper value to remove, exclusive.
     * @return <code>true</code> if this set changed as a result of the call.
     * @throws IllegalArgumentException if <code>lower</code> is greater than <code>upper</code>.
     */
    public boolean remove(short lower, short upper) throws IllegalArgumentException {
        return remove(new Short(lower), new Short(upper));
    }
    
    /**
     * Remove a range of values from this set. Range may be removed in any order.
     *
     * @param lower The lower value to remove, exclusive.
     * @param upper The upper value to remove, exclusive.
     * @return <code>true</code> if this set changed as a result of the call.
     * @throws IllegalArgumentException if <code>lower</code> is greater than <code>upper</code>.
     */
    public boolean remove(int lower, int upper) throws IllegalArgumentException {
        return remove(new Integer(lower), new Integer(upper));
    }
    
    /**
     * Remove a range of values from this set. Range may be removed in any order.
     *
     * @param lower The lower value to remove, exclusive.
     * @param upper The upper value to remove, exclusive.
     * @return <code>true</code> if this set changed as a result of the call.
     * @throws IllegalArgumentException if <code>lower</code> is greater than <code>upper</code>.
     */
    public boolean remove(long lower, long upper) throws IllegalArgumentException {
        return remove(new Long(lower), new Long(upper));
    }
    
    /**
     * Remove a range of values from this set. Range may be removed in any order.
     *
     * @param lower The lower value to remove, exclusive.
     * @param upper The upper value to remove, exclusive.
     * @return <code>true</code> if this set changed as a result of the call.
     * @throws IllegalArgumentException if <code>lower</code> is greater than <code>upper</code>.
     */
    public boolean remove(float lower, float upper) throws IllegalArgumentException {
        return remove(new Float(lower), new Float(upper));
    }
    
    /**
     * Remove a range of values from this set. Range may be removed in any order.
     *
     * @param lower The lower value to remove, exclusive.
     * @param upper The upper value to remove, exclusive.
     * @return <code>true</code> if this set changed as a result of the call.
     * @throws IllegalArgumentException if <code>lower</code> is greater than <code>upper</code>.
     */
    public boolean remove(double lower, double upper) throws IllegalArgumentException {
        return remove(new Double(lower), new Double(upper));
    }

    /**
     * Retourne l'index de l'élément <code>value</code> dans le tableau <code>array</code>.
     * Cette méthode interprète le tableau <code>array</code> comme un tableau d'un des types
     * intrinsèques du Java, et appelle la méthode <code>Arrays.binarySearch</code> appropriée.
     *
     * @param value The value to search. This value must have been converted with
     *        {@link #toNumber} prior to call this method.
     */
    private int binarySearch(final Comparable value) {
        switch (indexType) {
            case DOUBLE:   return Arrays.binarySearch((double[]) array, ((Number)value).doubleValue());
            case FLOAT:    return Arrays.binarySearch((float []) array, ((Number)value).floatValue ());
            case LONG:     return Arrays.binarySearch((long  []) array, ((Number)value).longValue  ());
            case INTEGER:  return Arrays.binarySearch((int   []) array, ((Number)value).intValue   ());
            case SHORT:    return Arrays.binarySearch((short []) array, ((Number)value).shortValue ());
            case BYTE:     return Arrays.binarySearch((byte  []) array, ((Number)value).byteValue  ());
            case CHARACTER:return Arrays.binarySearch((char[]) array,((Character)value).charValue  ());
            default:       return Arrays.binarySearch((Object[]) array, value);
        }
    }

    /**
     * Wrap the specified value in a number, if needed.
     */
    private Comparable toNumber(Comparable value) {
        assert type.isAssignableFrom(value.getClass()) : value;
        if (useClassChanger) try {
            value = (Comparable)ClassChanger.toNumber(value);
        } catch (ClassNotFoundException exception) {
            // Should not happen since the constructor should have make sure
            // that this operation is legal for value of class 'type'.
            final ClassCastException e = new ClassCastException(value.getClass().getName());
            e.initCause(exception);
            throw e;
        }
        return value;
    }

    /**
     * Returns a new {@link Range} object initialized with the given values.
     *
     * @param lower The lower value, inclusive.
     * @param upper The upper value, inclusive.
     */
    private Range newRange(final Comparable lower, final Comparable upper) {
        if (isNumeric) {
            return new NumberRange(type, lower, upper);
        } else {
            return new Range(type, lower, upper);
        }
    }

    /**
     * Returns the value at the specified index.
     * Even index are lower bounds, while odd index are upper bounds.
     */
    private Comparable get(final int index) {
        Comparable value = (Comparable) Array.get(array, index);
        if (useClassChanger) try {
            value = ClassChanger.toComparable((Number)value, type);
        } catch (ClassNotFoundException exception) {
            // Should not happen, since class type should
            // have been checked by all 'add(...)' methods
            final ClassCastException e = new ClassCastException(value.getClass().getName());
            e.initCause(exception);
            throw e;
        }
        return value;
    }

    /**
     * Returns a {@linkplain Range#getMinValue range's minimum value} as a <code>double</code>.
     * The <code>index</code> can be any value from 0 inclusive to the set's {@link #size size}
     * exclusive. The returned values always increase with <code>index</code>.
     *
     * @param  index The range index, from 0 inclusive to {@link #size size} exclusive.
     * @return The minimum value for the range at the specified index.
     * @throws IndexOutOfBoundsException if <code>index</code> is out of bounds.
     * @throws ClassCastException if range elements are not convertible to numbers.
     */
    public final double getMinValueAsDouble(int index) throws IndexOutOfBoundsException,
                                                              ClassCastException
    {
        index *= 2;
        return (isPrimitive) ? Array.getDouble(array, index)
                             : ((Number) Array.get(array, index)).doubleValue();
    }

    /**
     * Returns a {@linkplain Range#getMaxValue range's maximum value} as a <code>double</code>.
     * The <code>index</code> can be any value from 0 inclusive to the set's {@link #size size}
     * exclusive. The returned values always increase with <code>index</code>.
     *
     * @param  index The range index, from 0 inclusive to {@link #size size} exclusive.
     * @return The maximum value for the range at the specified index.
     * @throws IndexOutOfBoundsException if <code>index</code> is out of bounds.
     * @throws ClassCastException if range elements are not convertible to numbers.
     */
    public final double getMaxValueAsDouble(int index) throws IndexOutOfBoundsException,
                                                              ClassCastException
    {
        index = 2*index + 1;
        return (isPrimitive) ? Array.getDouble(array, index)
                             : ((Number) Array.get(array, index)).doubleValue();
    }

    /**
     * If the specified value is inside a range, returns the index of this range.
     * Otherwise, returns <code>-1</code>.
     *
     * @param  value The value to search.
     * @return The index of the range which contains this value, or -1 if there is no such range.
     */
    public int indexOfRange(final Comparable value) {
        int index = binarySearch(toNumber(value));
        if (index < 0) {
            // Found an insertion point. Make sure that the insertion
            // point is inside a range (i.e. before the maximum value).
            index = ~index; // Tild sign, not minus.
            if ((index & 1) == 0) {
                return -1;
            }
        }
        index /= 2; // Round toward 0 (odd index are maximum values).
        assert newRange(get(2*index), get(2*index+1)).contains(value) : value;
        return index;
    }

    /**
     * Returns <code>true</code> if this set contains the specified element.
     */
    public boolean contains(final Object object) {
        final Range range = (Range) object;
        if (type.equals(range.getElementClass())) {
            if (range.isMinIncluded() && range.isMaxIncluded()) {
                final int index = binarySearch(toNumber(range.getMinValue()));
                if (index>=0 && (index&1)==0) {
                    return get(index+1).compareTo(range.getMaxValue()) == 0;
                }
            }
        }
        return false;
    }

    /**
     * Returns the first (lowest) range currently in this sorted set.
     *
     * @throws NoSuchElementException if the set is empty.
     */
    public Object first() throws NoSuchElementException {
        if (array!=null && Array.getLength(array)!=0) {
            return newRange(get(0), get(1));
        }
        throw new NoSuchElementException();
    }

    /**
     * Returns the last (highest) range currently in this sorted set.
     *
     * @throws NoSuchElementException if the set is empty.
     */
    public Object last() throws NoSuchElementException {
        if (array != null) {
            final int length = Array.getLength(array);
            if (length != 0) {
                return newRange(get(length-2), get(length-1));
            }
        }
        throw new NoSuchElementException();
    }

    /**
     * Returns a view of the portion of this sorted set whose elements range
     * from <code>lower</code>, inclusive, to <code>upper</code>, exclusive.
     *
     * @param  lower Low endpoint (inclusive) of the sub set.
     * @param  upper High endpoint (exclusive) of the sub set. 
     * @return A view of the specified range within this sorted set.
     */
    public SortedSet subSet(final Object lower, final Object upper) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Returns a view of the portion of this sorted set whose elements are
     * strictly less than <code>upper</code>.
     *
     * @param  upper High endpoint (exclusive) of the headSet.
     * @return A view of the specified initial range of this sorted set.
     */
    public SortedSet headSet(final Object upper) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Returns a view of the portion of this sorted set whose elements are
     * greater than or equal to <code>lower</code>.
     *
     * @param  lower Low endpoint (inclusive) of the tailSet. 
     * @return A view of the specified final range of this sorted set.
     */
    public SortedSet tailSet(final Object lower) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Returns an iterator over the elements in this set of ranges.
     * All elements are {@link Range} objects.
     */
    public java.util.Iterator iterator() {
        return new Iterator();
    }


    /**
     * An iterator for iterating through ranges in a {@link RangeSet}.
     * All elements are {@link Range} objects.
     *
     * @version $Id: RangeSet.java,v 1.7 2003/08/28 15:41:57 desruisseaux Exp $
     * @author Martin Desruisseaux
     */
    private final class Iterator implements java.util.Iterator {
        /**
         * Modification count at construction time.
         */
        private int modCount = RangeSet.this.modCount;

        /**
         * The array length.
         */
        private int length = (array!=null) ? Array.getLength(array) : 0;

        /**
         * Current position in {@link RangeSet#array}.
         */
        private int position;

        /**
         * Returns <code>true</code> if the iteration has more elements.
         */
        public boolean hasNext() {
            return position<length;
        }
    
        /**
         * Returns the next element in the iteration.
         */
        public Object next() {
            if (hasNext()) {
                final Comparable lower = get(position++);
                final Comparable upper = get(position++);
                if (RangeSet.this.modCount != modCount) {
                    // Check it last, in case a change occured
                    // while we was constructing the element.
                    throw new ConcurrentModificationException();
                }
                return newRange(lower, upper);
            }
            throw new NoSuchElementException();
        }
    
        /**
         * Removes from the underlying collection the
         * last element returned by the iterator.
         */
        public void remove() {
            if (position!=0) {
                if (RangeSet.this.modCount == modCount) {
                    final Object newArray = Array.newInstance(elementType, length-=2);
                    System.arraycopy(array, position, newArray, position-=2, length-position);
                    System.arraycopy(array, 0,        newArray, 0,           position);
                    array = newArray;
                    modCount = ++RangeSet.this.modCount;
                } else {
                    throw new ConcurrentModificationException();
                }
            } else {
                throw new IllegalStateException();
            }
        }
    }

    /**
     * Returns a hash value for this set of ranges.
     * This value need not remain consistent between
     * different implementations of the same class.
     */
    public int hashCode() {
        int code = type.hashCode();
        if (array!=null) {
            for (int i=Array.getLength(array); (i-=8)>=0;) {
                code = code*37 + Array.get(array, i).hashCode();
            }
        }
        return code;
    }

    /**
     * Compares the specified object with
     * this set of ranges for equality.
     */
    public boolean equals(final Object object) {
        if (object!=null && object.getClass().equals(getClass())) {
            final RangeSet that = (RangeSet) object;
            if (Utilities.equals(this.type, that.type)) {
                switch (indexType) {
                    case DOUBLE:   return Arrays.equals((double[])this.array, (double[])that.array);
                    case FLOAT:    return Arrays.equals((float [])this.array, ( float[])that.array);
                    case LONG:     return Arrays.equals((long  [])this.array, (  long[])that.array);
                    case INTEGER:  return Arrays.equals((int   [])this.array, (   int[])that.array);
                    case SHORT:    return Arrays.equals((short [])this.array, ( short[])that.array);
                    case BYTE:     return Arrays.equals((byte  [])this.array, (  byte[])that.array);
                    case CHARACTER:return Arrays.equals((char  [])this.array, (  char[])that.array);
                    default:       return Arrays.equals((Object[])this.array, (Object[])that.array);
                }
            }
        }
        return false;
    }

    /**
     * Returns a clone of this range set.
     */
    public Object clone() {
        try {
            final RangeSet set = (RangeSet) super.clone();
            switch (set.indexType) {
                case DOUBLE:   set.array = ((double[])set.array).clone(); break;
                case FLOAT:    set.array = ((float [])set.array).clone(); break;
                case LONG:     set.array = ((long  [])set.array).clone(); break;
                case INTEGER:  set.array = ((int   [])set.array).clone(); break;
                case SHORT:    set.array = ((short [])set.array).clone(); break;
                case BYTE:     set.array = ((byte  [])set.array).clone(); break;
                case CHARACTER:set.array = ((char  [])set.array).clone(); break;
                default:       set.array = ((Object[])set.array).clone(); break;
            }
            return set;
        } catch (CloneNotSupportedException exception) {
            // Should not happen, since we are cloneable.
            throw new AssertionError(exception);
        }
    }

    /**
     * Returns a string representation of this set of ranges.
     * The returned string is implementation dependent.
     * It is usually provided for debugging purposes.
     */
    public String toString() {
        final StringBuffer buffer = new StringBuffer(Utilities.getShortClassName(this));
        buffer.append('[');
        boolean first = true;
        for (java.util.Iterator it=iterator(); it.hasNext();) {
            final Range range = (Range) it.next();
            if (!first) {
                buffer.append(',');
            }
            buffer.append('{');
            buffer.append(range.getMinValue());
            buffer.append("..");
            buffer.append(range.getMaxValue());
            buffer.append('}');
            first = false;
        }
        buffer.append(']');
        return buffer.toString();
    }
}
