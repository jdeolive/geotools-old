/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
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
package org.geotools.resources;

// Standard set of Java objects.
import java.lang.Number;
import java.lang.Long;
import java.util.Date;


/**
 * A central place to register transformations between an arbitrary class and a
 * {@link Number}. For example, it is sometime convenient to consider {@link Date}
 * objects as if they were {@link Long} objects for computation purpose in generic
 * algorithms. Client can call the following method to convert an arbitrary object
 * to a {@link Number}:
 *
 * <blockquote><pre>
 * Object someArbitraryObject = new Date();
 * Number myObjectAsANumber = {@link ClassChanger#toNumber ClassChanger.toNumber}(someArbitraryObject);
 * </pre></blockquote>
 *
 * @version $Id: ClassChanger.java,v 1.6 2003/08/10 20:26:30 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public abstract class ClassChanger {
    /**
     * Wrapper classes sorted by their wide.
     */
    private static final Class[] CLASS_RANK = {
        Byte   .class,
        Short  .class,
        Integer.class,
        Long   .class,
        Float  .class,
        Double .class
    };

    /**
     * Liste des classes d'objets pouvant être convertis en nombre. Cette liste
     * contiendra par défaut quelques instances de {@link ClassChanger} pour
     * quelques classes standards du Java, telle que {@link Date}. Toutefois,
     * d'autres objets pourront être ajoutés par la suite. Cette liste est
     * <u>ordonnée</u>. Les classe le plus hautes dans la hierarchie (les
     * classes parentes) doivent apparaître à la fin.
     */
    private static ClassChanger[] list = new ClassChanger[] {
        new ClassChanger(Date.class, Long.class) {
            protected Number convert(final Comparable object) {
                return new Long(((Date) object).getTime());
            }

            protected Comparable inverseConvert(final Number value) {
                return new Date(value.longValue());
            }
        }
    };

    /**
     * Parent class for {@link #convert}'s input objects.
     */
    private final Class source;

    /**
     * Parent class for {@link #convert}'s output objects.
     */
    private final Class target;

    /**
     * Construct a new class changer.
     *
     * @param source Parent class for {@link #convert}'s input objects.
     * @param target Parent class for {@link #convert}'s output objects.
     */
    protected ClassChanger(final Class source, final Class target) {
        this.source = source;
        this.target = target;
        if (!Comparable.class.isAssignableFrom(source)) {
            throw new IllegalArgumentException(String.valueOf(source));
        }
        if (!Number.class.isAssignableFrom(target)) {
            throw new IllegalArgumentException(String.valueOf(target));
        }
    }

    /**
     * Returns the numerical value for an object.
     *
     * @param  object Object to convert (may be null).
     * @return The object's numerical value.
     * @throws ClassCastException if <code>object</code> is not of the expected class.
     */
    protected abstract Number convert(final Comparable object) throws ClassCastException;

    /**
     * Returns an instance of the converted classe from a numerical value.
     *
     * @param  The value to wrap.
     * @return An instance of the source classe.
     */
    protected abstract Comparable inverseConvert(final Number value);

    /**
     * Returns a string representation for this class changer.
     */
    public String toString() {
        return "ClassChanger["+source.getName()+"\u00A0\u21E8\u00A0"+target.getName()+']';
    }

    /**
     * Register a new transformation. All registered {@link ClassChanger} will
     * be taken in account by the {@link #toNumber} method. The example below
     * register a transformation for the {@link Date} class:
     *
     * <blockquote><pre>
     * &nbsp;ClassChanger.register(new ClassChanger(Date.class, Long.class) {
     * &nbsp;    protected Number convert(final Comparable o) {
     * &nbsp;        return new Long(((Date) o).getTime());
     * &nbsp;    }
     * &nbsp;
     * &nbsp;    protected Comparable inverseConvert(final Number number) {
     * &nbsp;        return new Date(number.longValue());
     * &nbsp;    }
     * &nbsp;});
     * </pre></blockquote>
     *
     * @param  converter The {@link ClassChanger} to add.
     * @throws IllegalStateException if an other {@link ClassChanger} was already
     *         registered for the same <code>source</code> class. This is usually
     *         not a concern since the registration usually take place during the
     *         class initialization ("static" constructor).
     */
    public static synchronized void register(final ClassChanger converter) throws IllegalStateException {
        int i;
        for (i=0; i<list.length; i++) {
            if (list[i].source.isAssignableFrom(converter.source)) {
                /*
                 * On a trouvé un convertisseur qui utilisait
                 * une classe parente. Le nouveau convertisseur
                 * devra s'insérer avant son parent. Mais on va
                 * d'abord s'assurer qu'il n'existait pas déjà
                 * un convertisseur pour cette classe.
                 */
                for (int j=i; j<list.length; j++) {
                    if (list[j].source.equals(converter.source)) {
                        throw new IllegalStateException(list[j].toString());
                    }
                }
                break;
            }
        }
        list = (ClassChanger[]) XArray.insert(list, i, 1);
        list[i] = converter;
    }

    /**
     * Returns the class changer for the specified classe.
     *
     * @param  source The class.
     * @return The class changer for the specified class.
     * @throws ClassNotFoundException if <code>source</code> is not a registered class.
     */
    private static synchronized ClassChanger getClassChanger(final Class source)
        throws ClassNotFoundException
    {
        for (int i=0; i<list.length; i++) {
            if (list[i].source.isAssignableFrom(source)) {
                return list[i];
            }
        }
        throw new ClassNotFoundException(source.getName());
    }

    /**
     * Returns the target class for the specified source class, if a suitable
     * transformation is known. The source class is a {@link Comparable} subclass
     * that will be specified as input to {@link #convert}. The target class is a
     * {@link Number} subclass that will be returned as output by {@link #convert}.
     * If no suitable mapping is found, then <code>source</code> is returned.
     */
    public static synchronized Class getTransformedClass(final Class source) {
        if (source != null) {
            for (int i=0; i<list.length; i++) {
                if (list[i].source.isAssignableFrom(source)) {
                    return list[i].target;
                }
            }
        }
        return source;
    }

    /**
     * Returns the numeric value for the specified object. For example the code
     * <code>toNumber(new&nbsp;Date())</code> returns the {@link Date#getTime()}
     * value of the specified date object as a {@link Long}.
     *
     * @param  object Object to convert (may be null).
     * @return <code>null</code> if <code>object</code> was null; otherwise
     *         <code>object</code> if the supplied object is already an instance
     *         of {@link Number}; otherwise a new number with the numerical value.
     * @throws ClassNotFoundException if <code>object</code> is not an instance
     *         of a registered class.
     */
    public static Number toNumber(final Comparable object)
        throws ClassNotFoundException
    {
        if (object != null) {
            if (object instanceof Number) {
                return (Number) object;
            }
            return getClassChanger(object.getClass()).convert(object);
        }
        return null;
    }

    /**
     * Wrap the specified number as an instance of the specified classe.
     * For example <code>toComparable(Date.class,&nbsp;new&nbsp;Long(time))</code>
     * is equivalent to <code>new&nbsp;Date(time)</code>. There is of course no
     * point to use this method if the destination class is know at compile time.
     * This method is useful for creating instance of classes choosen dynamically
     * at run time.
     *
     * @param  value  The numerical value (may be null).
     * @param  classe The desired classe for return value.
     * @throws ClassNotFoundException if <code>classe</code> is not a registered class.
     */
    public static Comparable toComparable(final Number value, final Class classe)
        throws ClassNotFoundException
    {
        if (value != null) {
            if (Number.class.isAssignableFrom(classe)) {
                return (Comparable)value;
            }
            return getClassChanger(classe).inverseConvert(value);
        }
        return null;
    }

    /**
     * Cast the number to the specified class. The class must by one of {@link Byte},
     * {@link Short}, {@link Integer}, {@link Long}, {@link Float} or {@link Double}.
     */
    public static Number cast(final Number n, final Class c) {
        if (n!=null && !n.getClass().equals(c)) {
            if (Byte   .class.equals(c)) return new Byte   (n.  byteValue());
            if (Short  .class.equals(c)) return new Short  (n. shortValue());
            if (Integer.class.equals(c)) return new Integer(n.   intValue());
            if (Long   .class.equals(c)) return new Long   (n.  longValue());
            if (Float  .class.equals(c)) return new Float  (n. floatValue());
            if (Double .class.equals(c)) return new Double (n.doubleValue());
            throw new IllegalArgumentException(Utilities.getShortName(c));
        }
        return n;
    }

    /**
     * Returns the class of the widest type. Numbers <code>n1</code> and <code>n2</code>
     * must be instance of any of {@link Byte}, {@link Short}, {@link Integer}, {@link Long},
     * {@link Float} or {@link Double} types. At most one of the argument can be null.
     */
    public static Class getWidestClass(final Number n1, final Number n2) {
        return getWidestClass((n1!=null) ? n1.getClass() : null,
                              (n2!=null) ? n2.getClass() : null);
    }

    /**
     * Returns the class of the widest type. Classes <code>c1</code> and <code>c2</code>
     * must be of any of {@link Byte}, {@link Short}, {@link Integer}, {@link Long},
     * {@link Float} or {@link Double} types. At most one of the argument can be null.
     */
    public static Class getWidestClass(final Class c1, final Class c2) {
        if (c1==null) return c2;
        if (c2==null) return c1;
        return CLASS_RANK[Math.max(getRank(c1), getRank(c2))];
    }

    /**
     * Returns the class of the finest type. Classes <code>c1</code> and <code>c2</code>
     * must be of any of {@link Byte}, {@link Short}, {@link Integer}, {@link Long},
     * {@link Float} or {@link Double} types. At most one of the argument can be null.
     */
    public static Class getFinestClass(final Class c1, final Class c2) {
        if (c1==null) return c2;
        if (c2==null) return c1;
        return CLASS_RANK[Math.min(getRank(c1), getRank(c2))];
    }

    /**
     * Returns the smallest class capable to hold the specified value.
     */
    public static Class getFinestClass(final double value) {
        final long lg = (long)value;
        if (value == lg) {
            if (lg >=    Byte.MIN_VALUE  &&  lg <=    Byte.MAX_VALUE) return    Byte.class;
            if (lg >=   Short.MIN_VALUE  &&  lg <=   Short.MAX_VALUE) return   Short.class;
            if (lg >= Integer.MIN_VALUE  &&  lg <= Integer.MAX_VALUE) return Integer.class;
            if (lg >=   Short.MIN_VALUE  &&  lg <=    Long.MAX_VALUE) return    Long.class;
        }
        final float fv = (float) value;
        if (value == (double)fv) {
            return Float.class;
        }
        return Double.class;
    }

    /**
     * Returns the rank (in the {@link #CLASS_RANK} array) of the specified class.
     */
    private static int getRank(final Class c) {
        for (int i=0; i<CLASS_RANK.length; i++) {
            if (CLASS_RANK[i].isAssignableFrom(c)) {
                return i;
            }
        }
        throw new IllegalArgumentException(Utilities.getShortName(c));
    }
}
