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
package org.geotools.util;

// Collections
import java.util.Arrays;
import java.util.SortedSet;
import java.util.AbstractSet;
import java.util.NoSuchElementException;
import java.util.ConcurrentModificationException;

// Miscellaneous
import java.io.Serializable;
import java.lang.reflect.Array;
import javax.media.jai.util.Range;

// Geotools dependencies
import org.geotools.resources.Utilities;
import org.geotools.resources.ClassChanger;
import org.geotools.resources.rsc.Resources;
import org.geotools.resources.rsc.ResourceKeys;


/**
 * An ordered set of ranges. <code>RangeSet</code> objects store efficiently
 * an arbitrary number of ranges in any Java's primitives (<code>int</code>,
 * <code>float</code>, etc.) or any {@link Comparable} objects.  Ranges may
 * be added in any order.  When a range is added, this class first look for
 * an existing range overlapping the specified range. If an overlapping range
 * is found, ranges are merged as of {@link Range#union}. Consequently, ranges
 * returned by {@link #iterator} may not be the same than added ranges.
 * <br><br>
 * All entries in this set can be seen as {@link Range} objects.
 *
 * @version 1.0
 * @author Martin Desruisseaux
 */
public class RangeSet extends AbstractSet implements Serializable /*TODO: SortedSet*/ {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -5429867506081578080L;

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
     * Construct an empty set of range.
     *
     * @param type The class of the range elements. It must
     *             be a primitive type or a class extending
     *             {@link Comparable}.
     * @throws IllegalArgumentException if <code>type</code>
     *         is not a primitive type or a class extending
     *         {@link Comparable}.
     */
    public RangeSet(Class type) throws IllegalArgumentException {
        // If 'type' is a primitive type,
        // find the corresponding wrapper.
        for (int i=0; i<PRIMITIVES.length; i+=2) {
            if (PRIMITIVES[i].equals(type)) {
                type = PRIMITIVES[i+1];
                break;
            }
        }
        if (!Comparable.class.isAssignableFrom(type)) {
            throw new IllegalArgumentException(Resources.format(
                      ResourceKeys.ERROR_NOT_COMPARABLE_CLASS_$1,
                      Utilities.getShortClassName(type)));
        }
        Class elementType = ClassChanger.getTransformedClass(type); // e.g. change Date --> Long
        useClassChanger   = (elementType!=type);
        // If 'elementType' is a wrapper class,
        // find the corresponding primitive type.
        for (int i=0; i<PRIMITIVES.length; i+=2) {
            if (PRIMITIVES[i+1].equals(elementType)) {
                elementType = PRIMITIVES[i];
                break;
            }
        }
        this.type        = type;
        this.elementType = elementType;
        this.isPrimitive = elementType.isPrimitive();
        this.relaxedType = (!useClassChanger && isPrimitive) ? Number.class : type;
    }

    /**
     * Remove all elements from this set of ranges.
     */
    public void clear() {
        array=null;
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
     */
    public boolean add(final Object r) throws ClassCastException {
        final Range range = (Range) r;
        if (!range.isMinIncluded() || !range.isMaxIncluded()) {
            // TODO: support open intervals.
            throw new UnsupportedOperationException();
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
    public boolean add(Comparable first, Comparable last)
        throws IllegalArgumentException
    {
        if (!relaxedType.isAssignableFrom(first.getClass())) {
            throw new IllegalArgumentException(String.valueOf(first));
        }
        if (!relaxedType.isAssignableFrom(last.getClass())) {
            throw new IllegalArgumentException(String.valueOf(last));
        }
        if (first.compareTo(last) > 0) {
            throw new IllegalArgumentException(Resources.format(
                      ResourceKeys.ERROR_BAD_RANGE_$2, first, last));
        }
        if (useClassChanger) {
            try {
                first = (Comparable)ClassChanger.toNumber(first);
                last  = (Comparable)ClassChanger.toNumber(last );
            } catch (ClassNotFoundException exception) {
                // Should not happen, since this operation is legal according the constructor.
                Utilities.unexpectedException("org.geotools.resources",
                                              "RangeSet", "add", exception);
            }
        }
        if (array==null) {
            modCount++;
            array=Array.newInstance(elementType, 2);
            Array.set(array, 0, first);
            Array.set(array, 1, last);
            return true;
        }
        final int modCountChk = modCount;
        int i0=binarySearch(array, first);
        int i1;
        if (i0<0) {
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
             *             first(i=3)   last(i=5)
             */
            if (((i0=~i0) & 1) != 0) { // Attention: c'est ~ et non -
                first = (Comparable)Array.get(array, --i0);
                i1=binarySearch(array, last);
            } else {
                /*
                 * Si la date de début ne tombe pas dans une plage déjà
                 * existante, il faut étendre la valeur de début qui se
                 * trouve dans le tableau. Visuellement, on fait:
                 *
                 *   0   1     2      3     4   5    6     7
                 *   #####  ***########     #####    #######
                 *          ^                 ^
                 *       first(i=2)        last(i=5)
                 */
                if (i0!=Array.getLength(array) && (i1=binarySearch(array, last))!= ~i0) {
                    modCount++;
                    Array.set(array, i0, first);
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
                    final Object old=array;
                    final int length=Array.getLength(array);
                    array=Array.newInstance(elementType, length+2);
                    System.arraycopy(old,  0, array,  0,          i0);
                    System.arraycopy(old, i0, array, i0+2, length-i0);
                    Array.set(array, i0+0, first);
                    Array.set(array, i0+1, last);
                    return true;
                }
            }
        } else {
            i0 &= ~1;
            i1=binarySearch(array, last);
        }
        /*
         * A ce stade, on est certain que 'i0' est pair et pointe vers le début
         * de la plage dans le tableau. Fait maintenant le traitement pour 'i1'.
         */
        if (i1<0) {
            /*
             * Si la date de fin tombe dans une des plages déjà existantes
             * (si son index est impair), on l'étend pour pendre la fin de
             * la plage trouvée dans le tableau. Visuellement, on fait:
             *
             *   0   1     2      3     4   5    6     7
             *   #####     ########     #####    #######
             *             ^             ^-->
             *          first(i=2)     last(i=5)
             */
            if (((i1=~i1) & 1) != 0) { // Attention: c'est ~ et non -
                last = (Comparable)Array.get(array, i1);
            } else {
                /*
                 * Si la date de fin ne tombe pas dans une plage déjà
                 * existante, il faut étendre la valeur de fin qui se
                 * trouve dans le tableau. Visuellement, on fait:
                 *
                 *   0   1     2      3     4   5    6     7
                 *   #####     ########     #####**  #######
                 *             ^                  ^
                 *          first(i=2)         last(i=6)
                 */
                modCount++;
                Array.set(array, --i1, last);
            }
        } else {
            i1 |= 1;
        }
        /*
         * A ce stade, on est certain que 'i1' est impair et pointe vers la fin
         * de la plage dans le tableau. On va maintenant supprimer tout ce qui
         * se trouve entre 'i0' et 'i1', à l'exclusion de 'i0' et 'i1'.
         */
        assert (i0 & 1)==0;
        assert (i1 & 1)!=0;
        final int n = i1 - (++i0);
        if (n > 0) {
            modCount++;
            final Object old=array;
            final int length=Array.getLength(array);
            array=Array.newInstance(elementType, length-n);
            System.arraycopy(old,  0, array,  0, i0);
            System.arraycopy(old, i1, array, i0, length-i1);
        }
        assert (Array.getLength(array) & 1)==0;
        return modCountChk!=modCount;
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
    public boolean add(byte first, byte last) throws IllegalArgumentException {
        return add(new Byte(first), new Byte(last));
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
    public boolean add(short first, short last) throws IllegalArgumentException {
        return add(new Short(first), new Short(last));
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
    public boolean add(int first, int last) throws IllegalArgumentException {
        return add(new Integer(first), new Integer(last));
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
    public boolean add(long first, long last) throws IllegalArgumentException {
        return add(new Long(first), new Long(last));
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
    public boolean add(float first, float last) throws IllegalArgumentException {
        return add(new Float(first), new Float(last));
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
    public boolean add(double first, double last) throws IllegalArgumentException {
        return add(new Double(first), new Double(last));
    }

    /**
     * Retourne l'index de l'élément <code>value</code>
     * dans le tableau <code>array</code>. Cette méthode
     * développe le tableau <code>array</code> en tableau
     * d'un des types intrinsèques du Java, et appelle la
     * méthode <code>Arrays.binarySearch</code> appropriée.
     */
    private static int binarySearch(final Object array, final Object value) {
        if (array instanceof double[]) return Arrays.binarySearch((double[]) array, ((Number)value).doubleValue());
        if (array instanceof float []) return Arrays.binarySearch((float []) array, ((Number)value).floatValue ());
        if (array instanceof long  []) return Arrays.binarySearch((long  []) array, ((Number)value).longValue  ());
        if (array instanceof int   []) return Arrays.binarySearch((int   []) array, ((Number)value).intValue   ());
        if (array instanceof short []) return Arrays.binarySearch((short []) array, ((Number)value).shortValue ());
        if (array instanceof byte  []) return Arrays.binarySearch((byte  []) array, ((Number)value).byteValue  ());
        return Arrays.binarySearch((Object[]) array, value);
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
     * @version 1.0
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
        private int length = getLength();

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
                return new Range(type, lower, true, upper, true);
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
                    final Object newArray=Array.newInstance(elementType, length-=2);
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
     * Returns the array length.
     */
    final int getLength() {
        return (array!=null) ? Array.getLength(array) : 0;
    }

    /**
     * Returns the value at the specified index.
     * Even index are lower bounds, while odd index are upper bounds.
     */
    final Comparable get(final int index) {
        Comparable value = (Comparable) Array.get(array, index);
        if (useClassChanger) {
            try {
                value = ClassChanger.toComparable((Number)value, type);
            } catch (ClassNotFoundException exception) {
                // Should not happen, since class type should have been checked by all 'add(...)' methods
                Utilities.unexpectedException("org.geotools.resources", "RangeSet.Iterator", "next", exception);
            }
        }
        return value;
    }

    /**
     * Returns the value at the specified index as a <code>double</code>.
     * Even index are lower bounds, while odd index are upper bounds.
     */
    final double getDouble(final int index) {
        return (isPrimitive) ? Array.getDouble(array, index) : ((Number) Array.get(array, index)).doubleValue();
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
                if (array instanceof double[]) return Arrays.equals((double[])this.array, (double[])that.array);
                if (array instanceof  float[]) return Arrays.equals(( float[])this.array, ( float[])that.array);
                if (array instanceof   long[]) return Arrays.equals((  long[])this.array, (  long[])that.array);
                if (array instanceof    int[]) return Arrays.equals((   int[])this.array, (   int[])that.array);
                if (array instanceof  short[]) return Arrays.equals(( short[])this.array, ( short[])that.array);
                if (array instanceof   byte[]) return Arrays.equals((  byte[])this.array, (  byte[])that.array);
                return Arrays.equals((Object[])this.array, (Object[])that.array);
            }
        }
        return false;
    }

    /**
     * Returns a string representation of this set of ranges.
     * The returned string is implementation dependent.
     * It is usually provided for debugging purposes.
     */
    public String toString() {
        final StringBuffer buffer = new StringBuffer(Utilities.getShortClassName(this));
        buffer.append('[');
        boolean first=true;
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
            first=false;
        }
        buffer.append(']');
        return buffer.toString();
    }
}
