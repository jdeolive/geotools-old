/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2003-2006, Geotools Project Managment Committee (PMC)
 *    (C) 2001, Institut de Recherche pour le Développement
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

import java.util.Set;
import java.util.Queue;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Collections;
import java.util.AbstractQueue;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.io.Serializable;
import java.io.ObjectStreamException;
import org.geotools.util.logging.Logging;


/**
 * A set of miscellaneous methods.
 *
 * @since 2.0
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public final class Utilities {
    /**
     * An array of strings containing only white spaces. Strings' lengths are equal to their
     * index + 1 in the {@code spacesFactory} array. For example, {@code spacesFactory[4]}
     * contains a string of length 5. Strings are constructed only when first needed.
     */
    private static final String[] spacesFactory = new String[20];

    /**
     * The singleton instance to be returned by {@link #emptyQueue}.
     */
    private static Queue EMPTY_QUEUE = new EmptyQueue<Object>();

    /**
     * The class for the {@link #EMPTY_QUEUE} instance. Defined as a named class rather than
     * anonymous in order to avoid serialization issue.
     */
    private static final class EmptyQueue<E> extends AbstractQueue<E> implements Serializable {
        /** For cross-version compatibility. **/
        private static final long serialVersionUID = -6147951199761870325L;

        /** No effect on an queue which is already empty. */
        @Override
        public void clear() {
        }

        /** Returns {@code true} is all case. */
        @Override
        public boolean isEmpty() {
            return true;
        }

        /** Returns the size, which is always 0. */
        public int size() {
            return 0;
        }

        /** Returns an empty iterator. */
        public Iterator<E> iterator() {
            final Set<E> empty = Collections.emptySet();
            return empty.iterator();
        }

        /** Always returns {@code false} since this queue doesn't accept any element. */
        public boolean offer(E e) {
            return false;
        }

        /** Always returns {@code null} since this queue is always empty. */
        public E poll() {
            return null;
        }

        /** Always returns {@code null} since this queue is always empty. */
        public E peek() {
            return null;
        }

        /** Returns the singleton instance of deserialization. */
        protected Object readResolve() throws ObjectStreamException {
            return EMPTY_QUEUE;
        }
    }

    /**
     * Forbid object creation.
     */
    private Utilities() {
    }

    /**
     * Convenience method for testing two objects for equality. One or both objects may be null.
     */
    public static boolean equals(final Object object1, final Object object2) {
        return (object1==object2) || (object1!=null && object1.equals(object2));
    }

    /**
     * Convenience method for testing two objects for equality. One or both objects may be null.
     * If both are non-null and are arrays, then every array elements will be compared.
     * <p>
     * This method may be useful when the objects may or may not be array. If they are known
     * to be arrays, consider using {@link Arrays#deepEquals(Object[],Object[])} or one of its
     * primitive counter-part instead.
     * <p>
     * <strong>Rules for choosing an {@code equals} or {@code deepEquals} method</strong>
     * <ul>
     *   <li>If <em>both</em> objects are declared as {@code Object[]} (not anything else like
     *   {@code String[]}), consider using {@link Arrays#deepEquals(Object[],Object[])} except
     *   if it is known that the array elements can never be other arrays.</li>
     *
     *   <li>Otherwise if both objects are arrays (e.g. {@code Expression[]}, {@code String[]},
     *   {@code int[]}, <cite>etc.</cite>), use {@link Arrays#equals(Object[],Object[])}. This
     *   rule is applicable to arrays of primitive type too, since {@code Arrays.equals} is
     *   overriden with primitive counter-parts.</li>
     *
     *   <li>Otherwise if at least one object is anything else than {@code Object} (e.g.
     *   {@code String}, {@code Expression}, <cite>etc.</cite>), use {@link #equals(Object,Object)}.
     *   Using this {@code deepEquals} method would be an overkill since there is no chance that
     *   {@code String} or {@code Expression} could be an array.</li>
     *
     *   <li>Otherwise if <em>both</em> objects are declared exactly as {@code Object} type and
     *   it is known that they could be arrays, only then invoke this {@code deepEquals} method.
     *   In such case, make sure that the hash code is computed using {@link #deepHashCode} for
     *   consistency.</li>
     * </ul>
     */
    public static boolean deepEquals(final Object object1, final Object object2) {
        if (object1 == object2) {
            return true;
        }
        if (object1 == null || object2 == null) {
            return false;
        }
        if (object1 instanceof Object[]) {
            return (object2 instanceof Object[]) &&
                    Arrays.deepEquals((Object[]) object1, (Object[]) object2);
        }
        if (object1 instanceof double[]) {
            return (object2 instanceof double[]) &&
                    Arrays.equals((double[]) object1, (double[]) object2);
        }
        if (object1 instanceof float[]) {
            return (object2 instanceof float[]) &&
                    Arrays.equals((float[]) object1, (float[]) object2);
        }
        if (object1 instanceof long[]) {
            return (object2 instanceof long[]) &&
                    Arrays.equals((long[]) object1, (long[]) object2);
        }
        if (object1 instanceof int[]) {
            return (object2 instanceof int[]) &&
                    Arrays.equals((int[]) object1, (int[]) object2);
        }
        if (object1 instanceof short[]) {
            return (object2 instanceof short[]) &&
                    Arrays.equals((short[]) object1, (short[]) object2);
        }
        if (object1 instanceof byte[]) {
            return (object2 instanceof byte[]) &&
                    Arrays.equals((byte[]) object1, (byte[]) object2);
        }
        if (object1 instanceof char[]) {
            return (object2 instanceof char[]) &&
                    Arrays.equals((char[]) object1, (char[]) object2);
        }
        if (object1 instanceof boolean[]) {
            return (object2 instanceof boolean[]) &&
                    Arrays.equals((boolean[]) object1, (boolean[]) object2);
        }
        return object1.equals(object2);
    }

    /**
     * Returns a hash code for the specified object, which may be an array.
     * This method returns one of the following values:
     * <p>
     * <ul>
     *   <li>If the supplied object is {@code null}, then this method returns 0.</li>
     *   <li>Otherwise if the object is an array of objects, then
     *       {@link Arrays#deepHashCode(Object[])} is invoked.</li>
     *   <li>Otherwise if the object is an array of primitive type, then the corresponding
     *       {@link Arrays#hashCode(double[]) Arrays.hashCode(...)} method is invoked.</li>
     *   <li>Otherwise {@link Object#hashCode()} is invoked.<li>
     * </ul>
     * <p>
     * This method should be invoked <strong>only</strong> if the object type is declared
     * exactly as {@code Object}, not as some subtype like {@code Object[]}, {@code String} or
     * {@code float[]}. In the later cases, use the appropriate {@link Arrays} method instead.
     */
    public static int deepHashCode(final Object object) {
        if (object == null) {
            return 0;
        }
        if (object instanceof Object[]) {
            return Arrays.deepHashCode((Object[]) object);
        }
        if (object instanceof double[]) {
            return Arrays.hashCode((double[]) object);
        }
        if (object instanceof float[]) {
            return Arrays.hashCode((float[]) object);
        }
        if (object instanceof long[]) {
            return Arrays.hashCode((long[]) object);
        }
        if (object instanceof int[]) {
            return Arrays.hashCode((int[]) object);
        }
        if (object instanceof short[]) {
            return Arrays.hashCode((short[]) object);
        }
        if (object instanceof byte[]) {
            return Arrays.hashCode((byte[]) object);
        }
        if (object instanceof char[]) {
            return Arrays.hashCode((char[]) object);
        }
        if (object instanceof boolean[]) {
            return Arrays.hashCode((boolean[]) object);
        }
        return object.hashCode();
    }

    /**
     * Returns a string representation of the specified object, which may be an array.
     * This method returns one of the following values:
     * <p>
     * <ul>
     *   <li>If the object is an array of objects, then
     *       {@link Arrays#deepToString(Object[])} is invoked.</li>
     *   <li>Otherwise if the object is an array of primitive type, then the corresponding
     *       {@link Arrays#toString(double[]) Arrays.toString(...)} method is invoked.</li>
     *   <li>Otherwise {@link String#valueOf(String)} is invoked.<li>
     * </ul>
     * <p>
     * This method should be invoked <strong>only</strong> if the object type is declared
     * exactly as {@code Object}, not as some subtype like {@code Object[]}, {@code Number} or
     * {@code float[]}. In the later cases, use the appropriate {@link Arrays} method instead.
     */
    public static String deepToString(final Object object) {
        if (object instanceof Object[]) {
            return Arrays.deepToString((Object[]) object);
        }
        if (object instanceof double[]) {
            return Arrays.toString((double[]) object);
        }
        if (object instanceof float[]) {
            return Arrays.toString((float[]) object);
        }
        if (object instanceof long[]) {
            return Arrays.toString((long[]) object);
        }
        if (object instanceof int[]) {
            return Arrays.toString((int[]) object);
        }
        if (object instanceof short[]) {
            return Arrays.toString((short[]) object);
        }
        if (object instanceof byte[]) {
            return Arrays.toString((byte[]) object);
        }
        if (object instanceof char[]) {
            return Arrays.toString((char[]) object);
        }
        if (object instanceof boolean[]) {
            return Arrays.toString((boolean[]) object);
        }
        return String.valueOf(object);
    }

    /**
     * Returns a {@linkplain Queue queue} which is always empty and accepts no element.
     *
     * @see Collections#emptyList
     * @see Collections#emptySet
     */
    @SuppressWarnings("unchecked")
    public static <E> Queue<E> emptyQueue() {
        return EMPTY_QUEUE;
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
     *
     * @deprecated Moved to {@link Classes}.
     */
    public static <T> boolean sameInterfaces(final Class<? extends T> object1,
                                             final Class<? extends T> object2,
                                             final Class<T> base)
    {
        return Classes.sameInterfaces(object1, object2, base);
    }

    /**
     * Returns a string of the specified length filled with white spaces.
     * This method tries to return a pre-allocated string if possible.
     *
     * @param  length The string length. Negative values are clamped to 0.
     * @return A string of length {@code length} filled with white spaces.
     */
    public static String spaces(int length) {
        /*
         * No need to synchronize.  In the unlikely event of two threads calling this method
         * at the same time and the two calls creating a new string, the String.intern() call
         * will take care of canonicalizing the strings.
         */
        final int last = spacesFactory.length-1;
        if (length<0) length=0;
        if (length <= last) {
            if (spacesFactory[length]==null) {
                if (spacesFactory[last]==null) {
                    char[] blancs = new char[last];
                    Arrays.fill(blancs, ' ');
                    spacesFactory[last]=new String(blancs).intern();
                }
                spacesFactory[length] = spacesFactory[last].substring(0,length).intern();
            }
            return spacesFactory[length];
        } else {
            char[] blancs = new char[length];
            Arrays.fill(blancs, ' ');
            return new String(blancs);
        }
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
     *
     * @deprecated Moved to {@link Classes}.
     */
    public static String getShortName(Class<?> classe) {
        return Classes.getShortName(classe);
    }

    /**
     * Returns a short class name for the specified object. This method will
     * omit the package name. For example, it will return "String" instead
     * of "java.lang.String" for a {@link String} object.
     *
     * @param  object The object (may be {@code null}).
     * @return A short class name for the specified object.
     *
     * @deprecated Moved to {@link Classes}.
     */
    public static String getShortClassName(final Object object) {
        return Classes.getShortClassName(object);
    }

    /**
     * Invoked when a recoverable error occurs. This exception is similar to
     * {@link #unexpectedException unexpectedException} except that it doesn't
     * log the stack trace and uses a lower logging level.
     *
     * @param paquet  The package where the error occurred. This information
     *                may be used to fetch an appropriate {@link Logger} for
     *                logging the error.
     * @param classe  The class where the error occurred.
     * @param method  The method name where the error occurred.
     * @param error   The error.
     *
     * @deprecated Moved to {@link Logging#recoverableException}.
     */
    public static void recoverableException(final String   paquet,
                                            final Class<?> classe,
                                            final String   method,
                                            final Throwable error)
    {
        final LogRecord record = getLogRecord(error);
        record.setLevel(Level.FINER);
        record.setSourceClassName (classe.getName());
        record.setSourceMethodName(method);
        Logging.getLogger(paquet).log(record);
    }

    /**
     * Returns a log record for the specified exception.
     *
     * @deprecated Will be deleted after we removed the {@link #recoverableException}
     *             deprecated method.
     */
    public static LogRecord getLogRecord(final Throwable error) {
        final StringBuilder buffer = new StringBuilder(Classes.getShortClassName(error));
        final String message = error.getLocalizedMessage();
        if (message != null) {
            buffer.append(": ");
            buffer.append(message);
        }
        return new LogRecord(Level.WARNING, buffer.toString());
    }
}
