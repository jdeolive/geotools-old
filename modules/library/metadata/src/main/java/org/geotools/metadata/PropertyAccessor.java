/*
 *    GeoTools - The Open Source Java GIS Tookit
 *    http://geotools.org
 * 
 *    (C) 2007-2008, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.metadata;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.LinkedHashSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.geotools.util.Utilities;
import org.geotools.resources.XArray;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Errors;


/**
 * The getters declared in a GeoAPI interface, together with setters (if any)
 * declared in the Geotools implementation.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
final class PropertyAccessor {
    /**
     * The prefix for getters on boolean values.
     */
    private static final String IS = "is";

    /**
     * The prefix for getters (general case).
     */
    private static final String GET = "get";

    /**
     * The prefix for setters.
     */
    private static final String SET = "set";

    /**
     * Methods to exclude from {@link #getGetters}. They are method inherited from
     * {@link java.lang.Object}. Some of them, especially {@link Object#hashCode()}
     * {@link Object#toString()} and {@link Object#clone()}, may be declared explicitly
     * in some interface with a formal contract. Note: only no-argument methods need to
     * be declared in this list.
     */
    private static final String[] EXCLUDES = {
        "clone", "finalize", "getClass", "hashCode", "notify", "notifyAll", "toString", "wait"
    };

    /**
     * Getters shared between many instances of this class. Two different implementations
     * may share the same getters but different setters.
     */
    private static final Map<Class, Method[]> SHARED_GETTERS = new HashMap<Class, Method[]>();

    /**
     * The implemented metadata interface.
     */
    final Class<?> type;

    /**
     * The implementation class. The following condition must hold:
     *
     * <blockquote><pre>
     * type.{@linkplain Class#isAssignableFrom isAssignableFrom}(implementation);
     * </pre></blockquote>
     */
    final Class<?> implementation;

    /**
     * The getter methods. This array should not contain any null element.
     */
    private final Method[] getters;

    /**
     * The corresponding setter methods, or {@code null} if none. This array must have
     * the same length than {@link #getters}. For every {@code getters[i]} element,
     * {@code setters[i]} is the corresponding setter or {@code null} if there is none.
     */
    private final Method[] setters;

    /**
     * Creates a new property reader for the specified metadata implementation.
     *
     * @param  metadata The metadata implementation to wrap.
     * @param  type The interface implemented by the metadata.
     *         Should be the value returned by {@link #getType}.
     */
    PropertyAccessor(final Class<?> implementation, final Class<?> type) {
        this.implementation = implementation;
        this.type           = type;
        assert type.isAssignableFrom(implementation) : implementation;
        getters = getGetters(type);
        Method[] setters = null;
        final Class<?>[] arguments = new Class[1];
        for (int i=0; i<getters.length; i++) {
            final Method getter = getters[i];
            final Method setter; // To be determined later
            arguments[0] = getter.getReturnType();
            String name  = getter.getName();
            final int base = prefix(name).length();
            if (name.length() > base) {
                final char lo = name.charAt(base);
                final char up = Character.toUpperCase(lo);
                if (lo != up) {
                    name = SET + up + name.substring(base + 1);
                } else {
                    name = SET + name.substring(base);
                }
            }
            try {
                setter = implementation.getMethod(name, arguments);
            } catch (NoSuchMethodException e) {
                continue;
            }
            if (setters == null) {
                setters = new Method[getters.length];
            }
            setters[i] = setter;
        }
        this.setters = setters;
    }

    /**
     * Returns the metadata interface implemented by the specified implementation.
     * Only one metadata interface can be implemented.
     *
     * @param  metadata The metadata implementation to wraps.
     * @param  interfacePackage The root package for metadata interfaces.
     * @return The single interface, or {@code null} if none where found.
     */
    static Class<?> getType(Class<?> implementation, final String interfacePackage) {
        if (implementation != null && !implementation.isInterface()) {
            /*
             * Gets every interfaces from the supplied package in declaration order,
             * including the ones declared in the super-class.
             */
            final Set<Class<?>> interfaces = new LinkedHashSet<Class<?>>();
            do {
                getInterfaces(implementation, interfacePackage, interfaces);
                implementation = implementation.getSuperclass();
            } while (implementation != null);
            /*
             * If we found more than one interface, removes the
             * ones that are sub-interfaces of the other.
             */
            for (final Iterator<Class<?>> it=interfaces.iterator(); it.hasNext();) {
                final Class<?> candidate = it.next();
                for (final Class<?> child : interfaces) {
                    if (candidate != child && candidate.isAssignableFrom(child)) {
                        it.remove();
                        break;
                    }
                }
            }
            final Iterator<Class<?>> it=interfaces.iterator();
            if (it.hasNext()) {
                final Class<?> candidate = it.next();
                if (!it.hasNext()) {
                    return candidate;
                }
                // Found more than one interface; we don't know which one to pick.
                // Returns 'null' for now; the caller will thrown an exception.
            }
        }
        return null;
    }

    /**
     * Puts every interfaces for the given type in the specified collection.
     * This method invokes itself recursively for scanning parent interfaces.
     */
    private static void getInterfaces(final Class<?> type, final String interfacePackage,
            final Collection<Class<?>> interfaces)
    {
        for (final Class<?> candidate : type.getInterfaces()) {
            if (candidate.getName().startsWith(interfacePackage)) {
                interfaces.add(candidate);
            }
            getInterfaces(candidate, interfacePackage, interfaces);
        }
    }

    /**
     * Returns the getters. The returned array should never be modified,
     * since it may be shared among many instances of {@code PropertyAccessor}.
     */
    private static Method[] getGetters(final Class<?> type) {
        synchronized (SHARED_GETTERS) {
            Method[] getters = SHARED_GETTERS.get(type);
            if (getters == null) {
                getters = type.getMethods();
                int count = 0;
                for (int i=0; i<getters.length; i++) {
                    final Method candidate = getters[i];
                    if (candidate.getAnnotation(Deprecated.class) != null) {
                        // Ignores deprecated methods.
                        continue;
                    }
                    if (!candidate.getReturnType().equals(Void.TYPE) &&
                         candidate.getParameterTypes().length == 0)
                    {
                        /*
                         * We do not require a name starting with "get" or "is" prefix because some
                         * methods do not begin with such prefix, as in "ConformanceResult.pass()".
                         * Consequently we must provide special cases for no-arg methods inherited
                         * from java.lang.Object because some interfaces declare explicitly the
                         * contract for those methods.
                         */
                        final String name = candidate.getName();
                        if (!name.startsWith(SET) && !isExcluded(name)) {
                            getters[count++] = candidate;
                        }
                    }
                }
                getters = XArray.resize(getters, count);
                SHARED_GETTERS.put(type, getters);
            }
            return getters;
        }
    }

    /**
     * Returns {@code true} if the specified method is on the exclusion list.
     */
    private static boolean isExcluded(final String name) {
        for (int i=0; i<EXCLUDES.length; i++) {
            if (name.equals(EXCLUDES[i])) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the prefix of the specified method name. If the method name don't starts with
     * a prefix (for example {@link org.opengis.metadata.quality.ConformanceResult#pass()}),
     * then this method returns an empty string.
     */
    private static String prefix(final String name) {
        if (name.startsWith(GET)) {
            return GET;
        }
        if (name.startsWith(IS)) {
            return IS;
        }
        if (name.startsWith(SET)) {
            return SET;
        }
        return "";
    }

    /**
     * Returns the number of properties that can be read.
     */
    final int count() {
        return getters.length;
    }

    /**
     * Returns the index of the specified property, or -1 if none.
     * The search is case-insensitive.
     */
    final int indexOf(String key) {
        key = key.trim();
        for (int i=0; i<getters.length; i++) {
            final String name = getters[i].getName();
            final int base    = prefix(name).length();
            final int length  = key.length();
            if (name.length() == base + length && name.regionMatches(true, base, key, 0, length)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Returns {@code true} if the specified string starting at the specified index contains
     * no lower case characters. The characters don't have to be in upper case however (e.g.
     * non-alphabetic characters)
     */
    private static boolean isAcronym(final String name, int offset) {
        final int length = name.length();
        while (offset < length) {
            if (Character.isLowerCase(name.charAt(offset++))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the name of the property at the given index, or {@code null} if none.
     */
    final String name(final int index) {
        if (index >= 0 && index < getters.length) {
            String name = getters[index].getName();
            final int base = prefix(name).length();
            /*
             * Remove the "get" or "is" prefix and turn the first character after the
             * prefix into lower case. For example the method name "getTitle" will be
             * replaced by the property name "title". We will performs this operation
             * only if there is at least 1 character after the prefix.
             */
            if (name.length() > base) {
                if (isAcronym(name, base)) {
                    name = name.substring(base);
                } else {
                    final char up = name.charAt(base);
                    final char lo = Character.toLowerCase(up);
                    if (up != lo) {
                        name = lo + name.substring(base + 1);
                    } else {
                        name = name.substring(base);
                    }
                }
            }
            return name;
        }
        return null;
    }

    /**
     * Returns the type of the property at the given index.
     */
    final Class type(final int index) {
        if (index >= 0 && index < getters.length) {
            return getters[index].getReturnType();
        }
        return null;
    }

    /**
     * Returns {@code true} if the property at the given index is writable.
     */
    final boolean isWritable(final int index) {
        return (index >= 0) && (index < getters.length) && (setters != null) && (setters[index] != null);
    }

    /**
     * Returns the value for the specified metadata, or {@code null} if none.
     */
    final Object get(final int index, final Object metadata) {
        return (index >= 0 && index < getters.length) ? get(getters[index], metadata) : null;
    }

    /**
     * Gets a value from the specified metadata. We do not expect any checked exception to
     * be thrown, since {@code org.opengis.metadata} do not declare any.
     *
     * @param method The method to use for the query.
     * @param metadata The metadata object to query.
     */
    private static Object get(final Method method, final Object metadata) {
        assert !method.getReturnType().equals(Void.TYPE) : method;
        try {
            return method.invoke(metadata, (Object[]) null);
        } catch (IllegalAccessException e) {
            // Should never happen since 'getters' should contains only public methods.
            throw new AssertionError(e);
        } catch (InvocationTargetException e) {
            final Throwable cause = e.getTargetException();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            if (cause instanceof Error) {
                throw (Error) cause;
            }
            throw new UndeclaredThrowableException(cause);
        }
    }

    /**
     * Set a value for the specified metadata.
     *
     * @return The old value.
     * @throws IllegalArgumentException if the specified property can't be set.
     */
    final Object set(final int index, final Object metadata, final Object value)
            throws IllegalArgumentException
    {
        if (index >= 0 && index < getters.length && setters != null) {
            final Method setter = setters[index];
            if (setter != null) {
                final Object old = get(getters[index], metadata);
                set(setter, metadata, new Object[] {value});
                return old;
            }
        }
        throw new IllegalArgumentException(
                Errors.format(ErrorKeys.ILLEGAL_ARGUMENT_$1, "key"));
    }

    /**
     * Sets a value for the specified metadata. We do not expect any checked exception to
     * be thrown.
     *
     * @param method The method to use for the query.
     * @param metadata The metadata object to query.
     */
    private static void set(final Method method, final Object metadata, final Object[] arguments) {
        try {
            method.invoke(metadata, arguments);
        } catch (IllegalAccessException e) {
            // Should never happen since 'setters' should contains only public methods.
            throw new AssertionError(e);
        } catch (InvocationTargetException e) {
            final Throwable cause = e.getTargetException();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            if (cause instanceof Error) {
                throw (Error) cause;
            }
            throw new UndeclaredThrowableException(cause);
        }
    }

    /**
     * Compares the two specified metadata objects. The comparaison is <cite>shallow</cite>,
     * i.e. all metadata attributes are compared using the {@link Object#equals} method without
     * recursive call to this {@code shallowEquals} method for other metadata.
     * <p>
     * This method can optionaly excludes null values from the comparaison. In metadata,
     * null value often means "don't know", so in some occasion we want to consider two
     * metadata as different only if an attribute value is know for sure to be different.
     *
     * @param metadata1 The first metadata object to compare.
     * @param metadata2 The second metadata object to compare.
     * @param skipNulls If {@code true}, only non-null values will be compared.
     */
    public boolean shallowEquals(final Object metadata1, final Object metadata2, final boolean skipNulls) {
        assert type.isInstance(metadata1) : metadata1;
        assert type.isInstance(metadata2) : metadata2;
        for (int i=0; i<getters.length; i++) {
            final Method  method = getters[i];
            final Object  value1 = get(method, metadata1);
            final Object  value2 = get(method, metadata2);
            final boolean empty1 = isEmpty(value1);
            final boolean empty2 = isEmpty(value2);
            if (empty1 && empty2) {
                continue;
            }
            if (!Utilities.equals(value1, value2)) {
                if (!skipNulls || (!empty1 && !empty2)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Copies all metadata from source to target. The source can be any implementation of
     * the metadata interface, but the target must be the implementation expected by this
     * class.
     *
     * @param  source The metadata to copy.
     * @param  target The target metadata.
     * @param  skipNulls If {@code true}, only non-null values will be copied.
     * @return {@code true} in case of success, or {@code false} if at least
     *         one setter method was not found.
     * @throws UnmodifiableMetadataException if the target metadata is unmodifiable.
     */
    public boolean shallowCopy(final Object source, final Object target, final boolean skipNulls)
            throws UnmodifiableMetadataException
    {
        boolean success = true;
        assert type          .isInstance(source) : source;
        assert implementation.isInstance(target) : target;
        final Object[] arguments = new Object[1];
        for (int i=0; i<getters.length; i++) {
            arguments[0] = get(getters[i], source);
            if (!skipNulls || !isEmpty(arguments[0])) {
                if (setters == null) {
                    return false;
                }
                final Method setter = setters[i];
                if (setter != null) {
                    set(setter, target, arguments);
                } else {
                    success = false;
                }
            }
        }
        return success;
    }

    /**
     * Replaces every properties in the specified metadata by their
     * {@linkplain ModifiableMetadata#unmodifiable unmodifiable variant.
     */
    final void freeze(final Object metadata) {
        assert implementation.isInstance(metadata) : metadata;
        if (setters != null) {
            final Object[] arguments = new Object[1];
            for (int i=0; i<getters.length; i++) {
                final Method setter = setters[i];
                if (setter != null) {
                    final Object source = get(getters[i], metadata);
                    final Object target = ModifiableMetadata.unmodifiable(source);
                    if (source != target) {
                        arguments[0] = target;
                        set(setter, metadata, arguments);
                    }
                }
            }
        }
    }

    /**
     * Returns {@code true} if the metadata is modifiable. This method is not public because it
     * uses heuristic rules. In case of doubt, this method conservatively returns {@code true}.
     */
    final boolean isModifiable() {
        if (setters != null) {
            return true;
        }
        for (int i=0; i<getters.length; i++) {
            // Immutable objects usually don't need to be cloned. So if
            // an object is cloneable, it is probably not immutable.
            if (Cloneable.class.isAssignableFrom(getters[i].getReturnType())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns a hash code for the specified metadata. The hash code is defined as the
     * sum of hash code values of all non-null properties. This is the same contract than
     * {@link java.util.Set#hashCode} and ensure that the hash code value is insensitive
     * to the ordering of properties.
     */
    public int hashCode(final Object metadata) {
        assert type.isInstance(metadata) : metadata;
        int code = 0;
        for (int i=0; i<getters.length; i++) {
            final Object value = get(getters[i], metadata);
            if (!isEmpty(value)) {
                code += value.hashCode();
            }
        }
        return code;
    }

    /**
     * Counts the number of non-null properties.
     */
    public int count(final Object metadata, final int max) {
        assert type.isInstance(metadata) : metadata;
        int count = 0;
        for (int i=0; i<getters.length; i++) {
            if (!isEmpty(get(getters[i], metadata))) {
                if (++count >= max) {
                    break;
                }
            }
        }
        return count;
    }

    /**
     * Returns {@code true} if the specified object is null or an empty collection,
     * array or string.
     */
    static boolean isEmpty(final Object value) {
        return value == null ||
                ((value instanceof Collection) && ((Collection) value).isEmpty()) ||
                ((value instanceof CharSequence) && value.toString().trim().length() == 0) ||
                (value.getClass().isArray() && Array.getLength(value) == 0);
    }
}
