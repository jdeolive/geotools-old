/*
 *    GeoTools - The Open Source Java GIS Tookit
 *    http://geotools.org
 * 
 *    (C) 2001-2008, Open Source Geospatial Foundation (OSGeo)
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
 */
package org.geotools.resources;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.ErrorKeys;


/**
 * A set of miscellaneous methods working on {@link Class} objects.
 *
 * @since 2.5
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux (IRD)
 */
public final class Classes {
    /**
     * Constants to be used in {@code switch} statements.
     */
    public static final byte DOUBLE=8, FLOAT=7, LONG=6, INTEGER=5, SHORT=4, BYTE=3,
                            CHARACTER=2, BOOLEAN=1, OTHER=0;

    /**
     * Mapping between a primitive type and its wrapper, if any.
     */
    private static final Map<Class<?>,Classes> MAPPING = new HashMap<Class<?>,Classes>(16);
    static {
        new Classes(Double   .TYPE, Double   .class, true,  false, (byte) Double   .SIZE, DOUBLE   );
        new Classes(Float    .TYPE, Float    .class, true,  false, (byte) Float    .SIZE, FLOAT    );
        new Classes(Long     .TYPE, Long     .class, false, true,  (byte) Long     .SIZE, LONG     );
        new Classes(Integer  .TYPE, Integer  .class, false, true,  (byte) Integer  .SIZE, INTEGER  );
        new Classes(Short    .TYPE, Short    .class, false, true,  (byte) Short    .SIZE, SHORT    );
        new Classes(Byte     .TYPE, Byte     .class, false, true,  (byte) Byte     .SIZE, BYTE     );
        new Classes(Character.TYPE, Character.class, false, false, (byte) Character.SIZE, CHARACTER);
        new Classes(Boolean  .TYPE, Boolean  .class, false, false, (byte) 1,              BOOLEAN  );
        new Classes(Void     .TYPE, Void     .class, false, false, (byte) 0,              OTHER    );
    }

    /** The primitive type.                     */ private final Class<?> primitive;
    /** The wrapper for the primitive type.     */ private final Class<?> wrapper;
    /** {@code true} for floating point number. */ private final boolean  isFloat;
    /** {@code true} for integer number.        */ private final boolean  isInteger;
    /** The size in bytes.                      */ private final byte     size;
    /** Constant to be used in switch statement.*/ private final byte     ordinal;

    /**
     * Creates a mapping between a primitive type and its wrapper.
     */
    private Classes(Class<?> primitive, Class<?> wrapper, boolean isFloat, boolean isInteger,
                    byte size, byte ordinal)
    {
        this.primitive = primitive;
        this.wrapper   = wrapper;
        this.isFloat   = isFloat;
        this.isInteger = isInteger;
        this.size      = size;
        this.ordinal   = ordinal;
        if (MAPPING.put(primitive, this) != null || MAPPING.put(wrapper, this) != null) {
            throw new AssertionError(); // Should never happen.
        }
    }

    /**
     * Returns the class of the specified object, or {@code null} if {@code object} is null.
     * This method is also useful for fetching the class of an object known only by its bound
     * type. As of Java 6, the usual pattern:
     *
     * <blockquote><pre>
     * Number n = 0;
     * Class<? extends Number> c = n.getClass();
     * </pre></blockquote>
     *
     * doesn't seem to work if {@link Number} is replaced by a parametired type {@code T}.
     */
    @SuppressWarnings("unchecked")
    public static <T> Class<? extends T> getClass(final T object) {
        return (object != null) ? (Class<? extends T>) object.getClass() : null;
    }

    /**
     * Returns all classes implemented by the given set of objects.
     */
    private static Set<Class<?>> getClasses(final Collection<?> objects) {
        final Set<Class<?>> types = new LinkedHashSet<Class<?>>();
        for (final Object object : objects) {
            if (object != null) {
                types.add(object.getClass());
            }
        }
        return types;
    }

    /**
     * Returns the most specific class implemented by the objects in the given collection.
     * If no class are {@linkplain Class#isAssignableFrom assignable} to all others, then
     * this method returns the {@linkplain #commonClass most specific common super class}.
     *
     * @param  objects A collection of objects. May contains duplicated values and null values.
     * @return The most specific class.
     */
    public static Class<?> specializedClass(final Collection<?> objects) {
        final Set<Class<?>> types = getClasses(objects);
        final Class<?> type = removeAssignables(types);
        return (type != null) ? type : commonSuperClass(types);
    }

    /**
     * Returns the most specific class which is a common parent of all specified objects.
     *
     * @param  objects A collection of objects. May contains duplicated values and null values.
     * @return The most specific class common to all supplied objects.
     */
    public static Class<?> commonClass(final Collection<?> objects) {
        final Set<Class<?>> types = getClasses(objects);
        /*
         * First check if a type is assignable from all other types. At most one such
         * type can exists. We check for it first in order to avoid the creation of a
         * temporary HashSet if such type is found.
         */
search: for (final Class<?> candidate : types) {
            for (final Class<?> type : types) {
                if (!candidate.isAssignableFrom(type)) {
                    continue search;
                }
            }
            return candidate;
        }
        return commonSuperClass(types);
    }

    /**
     * Returns the most specific class which is a common parent of all the specified classes.
     * This method should be invoked when no common parent has been found in the supplied list.
     */
    private static Class<?> commonSuperClass(final Collection<Class<?>> types) {
        // Build a list of all super classes.
        final Set<Class<?>> superTypes = new LinkedHashSet<Class<?>>();
        for (Class<?> type : types) {
            while ((type = type.getSuperclass()) != null) {
                if (!superTypes.add(type)) {
                    // If the type was already in the set, then its super-types are in the set too.
                    break;
                }
            }
        }
        // Removes every elements that are not assignable from every supplied types.
        for (final Iterator<Class<?>> it=superTypes.iterator(); it.hasNext();) {
            final Class<?> candidate = it.next();
            for (final Class<?> type : types) {
                if (!candidate.isAssignableFrom(type)) {
                    it.remove();
                    break;
                }
            }
        }
        // Now removes every classes that can be assigned from an other classes.
        // We should have only one left, the most specific one in the hierarchy.
        return removeAssignables(superTypes);
    }

    /**
     * Removes every classes in the specified collection which are assignable from an other
     * class from the same collection. As a result of this method call, the given collection
     * should contains only leaf classes.
     *
     * @param  types The collection to trim.
     * @return If there is exactly one element left, that element. Otherwise {@code null}.
     */
    private static Class<?> removeAssignables(final Collection<Class<?>> types) {
        for (final Iterator<Class<?>> it=types.iterator(); it.hasNext();) {
            final Class<?> candidate = it.next();
            for (final Class<?> type : types) {
                if (candidate != type && candidate.isAssignableFrom(type)) {
                    it.remove();
                    break;
                }
            }
        }
        return (types.size() == 1) ? types.iterator().next() : null;
    }

    /**
     * Returns {@code true} if the two specified objects implements exactly the same set of
     * interfaces. Only interfaces assignable to {@code base} are compared. Declaration order
     * doesn't matter. For example in ISO 19111, different interfaces exist for different coordinate
     * system geometries ({@code CartesianCS}, {@code PolarCS}, etc.). We can check if two
     * CS implementations has the same geometry with the following code:
     *
     * <blockquote><code>
     * if (sameInterfaces(cs1, cs2, {@linkplain org.opengis.referencing.cs.CoordinateSystem}.class))
     * </code></blockquote>
     */
    public static <T> boolean sameInterfaces(final Class<? extends T> object1,
                                             final Class<? extends T> object2,
                                             final Class<T> base)
    {
        if (object1 == object2) {
            return true;
        }
        if (object1==null || object2==null) {
            return false;
        }
        final Class<?>[] c1 = object1.getInterfaces();
        final Class<?>[] c2 = object2.getInterfaces();
        /*
         * Trim all interfaces that are not assignable to 'base' in the 'c2' array.
         * Doing this once will avoid to redo the same test many time in the inner
         * loops j=[0..n].
         */
        int n = 0;
        for (int i=0; i<c2.length; i++) {
            final Class<?> c = c2[i];
            if (base.isAssignableFrom(c)) {
                c2[n++] = c;
            }
        }
        /*
         * For each interface assignable to 'base' in the 'c1' array, check if
         * this interface exists also in the 'c2' array. Order doesn't matter.
         */
compare:for (int i=0; i<c1.length; i++) {
            final Class<?> c = c1[i];
            if (base.isAssignableFrom(c)) {
                for (int j=0; j<n; j++) {
                    if (c.equals(c2[j])) {
                        System.arraycopy(c2, j+1, c2, j, --n-j);
                        continue compare;
                    }
                }
                return false; // Interface not found in 'c2'.
            }
        }
        return n == 0; // If n>0, at least one interface was not found in 'c1'.
    }

    /**
     * Returns {@code true} if the given {@code type} is a floating point type.
     *
     * @param  type The type to test (may be {@code null}).
     * @return {@code true} if {@code type} is the primitive or wrapper class of
     *         {@link Float} or {@link Double}.
     */
    public static boolean isFloat(final Class<?> type) {
        final Classes mapping = MAPPING.get(type);
        return (mapping != null) && mapping.isFloat;
    }

    /**
     * Returns {@code true} if the given {@code type} is an integer type.
     *
     * @param  type The type to test (may be {@code null}).
     * @return {@code true} if {@code type} is the primitive of wrapper class of
     *         {@link Long}, {@link Integer}, {@link Short} or {@link Byte}.
     */
    public static boolean isInteger(final Class<?> type) {
        final Classes mapping = MAPPING.get(type);
        return (mapping != null) && mapping.isInteger;
    }

    /**
     * Returns the number of bits used by number of the specified type.
     *
     * @param  type The type (may be {@code null}).
     * @return The number of bits, or 0 if unknow.
     */
    public static int getBitCount(final Class<?> type) {
        final Classes mapping = MAPPING.get(type);
        return (mapping != null) ? mapping.size : 0;
    }

    /**
     * Changes a primitive class to its wrapper (e.g. {@code int} to {@link Integer}).
     * If the specified class is not a primitive type, then it is returned unchanged.
     *
     * @param  type The primitive type (may be {@code null}).
     * @return The type as a wrapper.
     */
    public static Class<?> primitiveToWrapper(final Class<?> type) {
        final Classes mapping = MAPPING.get(type);
        return (mapping != null) ? mapping.wrapper : type;
    }

    /**
     * Changes a wrapper class to its primitive (e.g. {@link Integer} to {@code int}).
     * If the specified class is not a wrapper type, then it is returned unchanged.
     *
     * @param  type The wrapper type (may be {@code null}).
     * @return The type as a primitive.
     */
    public static Class<?> wrapperToPrimitive(final Class<?> type) {
        final Classes mapping = MAPPING.get(type);
        return (mapping != null) ? mapping.primitive : type;
    }

    /**
     * Returns one of {@link #DOUBLE}, {@link #FLOAT}, {@link #LONG}, {@link #INTEGER},
     * {@link #SHORT}, {@link #BYTE}, {@link #CHARACTER}, {@link #BOOLEAN} or {@link #OTHER}
     * constants for the given type. This is a commodity for usage in {@code switch} statememnts.
     */
    public static byte getEnumConstant(final Class<?> type) {
        final Classes mapping = MAPPING.get(type);
        return (mapping != null) ? mapping.ordinal : OTHER;
    }

    /**
     * Converts the specified string into a value object. The value object will be an instance
     * of {@link Boolean}, {@link Integer}, {@link Double}, <cite>etc.</cite> according the
     * specified type.
     *
     * @param  type The requested type.
     * @param  value the value to parse.
     * @return The value object, or {@code null} if {@code value} was null.
     * @throws IllegalArgumentException if {@code type} is not a recognized type.
     * @throws NumberFormatException if the string value is not parseable as a number
     *         of the specified type.
     */
    @SuppressWarnings("unchecked")
    public static <T> T valueOf(final Class<T> type, final String value)
            throws IllegalArgumentException, NumberFormatException
    {
        if (value == null) {
            return null;
        }
        if (Double .class.equals(type)) return (T) Double .valueOf(value);
        if (Float  .class.equals(type)) return (T) Float  .valueOf(value);
        if (Long   .class.equals(type)) return (T) Long   .valueOf(value);
        if (Integer.class.equals(type)) return (T) Integer.valueOf(value);
        if (Short  .class.equals(type)) return (T) Short  .valueOf(value);
        if (Byte   .class.equals(type)) return (T) Byte   .valueOf(value);
        if (Boolean.class.equals(type)) return (T) Boolean.valueOf(value);
        throw new IllegalArgumentException(Errors.format(ErrorKeys.UNKNOW_TYPE_$1, type));
    }

    /**
     * Returns a short class name for the specified class. This method will
     * omit the package name.  For example, it will return "String" instead
     * of "java.lang.String" for a {@link String} object. It will also name
     * array according Java language usage,  for example "double[]" instead
     * of "[D".
     *
     * @param  classe The object class (may be {@code null}).
     * @return A short class name for the specified object.
     */
    public static String getShortName(Class<?> classe) {
        if (classe == null) {
            return "<*>";
        }
        String name = classe.getSimpleName();
        Class<?> enclosing = classe.getEnclosingClass();
        if (enclosing != null) {
            final StringBuilder buffer = new StringBuilder();
            do {
                buffer.insert(0, '.').insert(0, enclosing.getSimpleName());
            } while ((enclosing = enclosing.getEnclosingClass()) != null);
            name = buffer.append(name).toString();
        }
        return name;
    }

    /**
     * Returns a short class name for the specified object. This method will
     * omit the package name. For example, it will return "String" instead
     * of "java.lang.String" for a {@link String} object.
     *
     * @param  object The object (may be {@code null}).
     * @return A short class name for the specified object.
     */
    public static String getShortClassName(final Object object) {
        return getShortName(getClass(object));
    }
}
