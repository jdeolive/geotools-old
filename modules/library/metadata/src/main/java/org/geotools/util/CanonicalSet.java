/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2003-2007, Geotools Project Managment Committee (PMC)
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
package org.geotools.util;


/**
 * A canonical set of objects, used to optimize memory use.
 * The operation of this set is similar in spirit to the {@link String#intern} method.
 * The following example shows a convenient way to use {@code CanonicalSet} as an
 * internal pool of immutable objects.
 *
 * <blockquote><pre>
 * public Foo create(String definition) {
 *      Foo created = new Foo(definition);
 *      return (Foo) canonicalSet.unique(created);
 * }
 * </pre></blockquote>
 *
 * The {@code CanonicalSet} has a {@link #get} method that is not part of the {@link java.util.Set}
 * interface. This {@code get} method retrieves an entry from this set that is equals to
 * the supplied object. The {@link #unique} method combines a {@code get} followed by a
 * {@code put} operation if the specified object was not in the set.
 * <p>
 * The set of objects is held by weak references as explained in {@link WeakHashSet}.
 * The {@code CanonicalSet} class is thread-safe.
 *
 * @since 2.4
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Jody Garnett
 */
public class CanonicalSet<E> extends WeakHashSet<E> {
    /**
     * Constructs a {@code CanonicalSet}.
     *
     * @deprecated Use {@link #newInstance} instead.
     */
    public CanonicalSet() {
    }

    /**
     * Constructs a {@code CanonicalSet} for elements of the specified type.
     *
     * @since 2.5
     */
    protected CanonicalSet(final Class<E> type) {
        super(type);
    }

    /**
     * Constructs a {@code CanonicalSet} for elements of the specified type.
     *
     * @since 2.5
     */
    public static <E> CanonicalSet<E> newInstance(final Class<E> type) {
        return new CanonicalSet<E>(type);
    }

    /**
     * Returns an object equals to the specified object, if present. If
     * this set doesn't contains any object equals to {@code object},
     * then this method returns {@code null}.
     *
     * @see #unique(Object)
     */
    public synchronized <T extends E> T get(final T object) {
        return intern(object, GET);
    }

    /**
     * Returns an object equals to {@code object} if such an object already exist in this
     * {@code CanonicalSet}. Otherwise, adds {@code object} to this {@code CanonicalSet}.
     * This method is equivalents to the following code:
     *
     * <blockquote><pre>
     * if (object != null) {
     *     Object current = get(object);
     *     if (current != null) {
     *         return current;
     *     } else {
     *         add(object);
     *     }
     * }
     * return object;
     * </pre></blockquote>
     */
    public synchronized <T extends E> T unique(final T object) {
        return intern(object, INTERN);
    }

    /**
     * Iteratively call {@link #unique(Object)} for an array of objects.
     * This method is equivalents to the following code:
     *
     * <blockquote><pre>
     * for (int i=0; i<objects.length; i++) {
     *     objects[i] = unique(objects[i]);
     * }
     * </pre></blockquote>
     */
    public synchronized void uniques(final E[] objects) {
        for (int i=0; i<objects.length; i++) {
            objects[i] = intern(objects[i], INTERN);
        }
    }
}
