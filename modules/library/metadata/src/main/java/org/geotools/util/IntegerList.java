/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2008, Geotools Project Managment Committee (PMC)
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

import java.util.Arrays;
import java.util.AbstractList;
import java.util.RandomAccess;
import java.io.Serializable;
import org.opengis.util.Cloneable;
import org.geotools.resources.XArray;
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.ErrorKeys;


/**
 * A list of unsigned integer values. This class packs the values in the minimal amount of bits
 * required for storing unsigned integers of the given {@linkplain #maximalValue maximal value}.
 *
 * @since 2.5
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class IntegerList extends AbstractList<Integer> implements RandomAccess, Serializable, Cloneable {
    /**
     * For cross-version compatibility.
     */
    private static final long serialVersionUID = 1241962316404811189L;

    /**
     * The bit count for values.
     */
    private final int bitCount;

    /**
     * The mask computed as {@code (1 << bitCount) - 1}.
     */
    private final int mask;

    /**
     * The packed values. We use the {@code long} type instead of {@code int}
     * on the basis that 64 bits machines are becoming more and more common.
     */
    private long[] values;

    /**
     * The list size. Initially 0.
     */
    private int size;

    /**
     * Creates an initially empty list with the given initial capacity.
     *
     * @param initialCapacity The initial capacity.
     * @param maximalValue The maximal value to be allowed, inclusive.
     */
    public IntegerList(int initialCapacity, int maximalValue) {
        this(initialCapacity, 0, maximalValue);
    }

    /**
     * Creates a new list with the given initial size.
     * The value of all elements are initialized to 0.
     *
     * @param initialCapacity The initial capacity. If this value is smaller than
     *        {@code initialSize}, then the later will be used as the initial capacity.
     * @param initialSize The initial size.
     * @param maximalValue The maximal value to be allowed, inclusive.
     */
    public IntegerList(int initialCapacity, int initialSize, int maximalValue) {
        if (initialSize > initialCapacity) {
            initialCapacity = initialSize;
        }
        if (initialCapacity <= 0) {
            throw new IllegalArgumentException(Errors.format(
                    ErrorKeys.NOT_GREATER_THAN_ZERO_$1, initialCapacity));
        }
        if (maximalValue <= 0) {
            throw new IllegalArgumentException(Errors.format(
                    ErrorKeys.NOT_GREATER_THAN_ZERO_$1, maximalValue));
        }
        int bitCount = 0;
        do {
            bitCount++;
            maximalValue >>>= 1;
        } while (maximalValue != 0);
        this.bitCount = bitCount;
        mask = (1 << bitCount) - 1;
        values = new long[length(initialCapacity)];
        size = initialSize;
    }

    /**
     * Returns the array length required for holding a list of the given size.
     *
     * @param size The list size.
     * @return The array length for holding a list of the given size.
     */
    private int length(final int size) {
        return (size * bitCount + (Long.SIZE - 1)) / Long.SIZE;
    }

    /**
     * Returns the maximal value that can be stored in this list.
     * May be slighly higher than the value given to the constructor.
     *
     * @return The maximal value, inclusive.
     */
    public int maximalValue() {
        return mask;
    }

    /**
     * Returns the current number of values in this list.
     *
     * @return The number of values.
     */
    public int size() {
        return size;
    }

    /**
     * Sets the list size to the given value. If the new size is lower than previous size,
     * then the elements after the new size are discarted. If the new size is greater than
     * the previous one, then the extra elements are initialized to 0.
     *
     * @param size The new size.
     */
    public void resize(final int size) {
        if (size < 0) {
            throw new IllegalArgumentException();
        }
        if (size > this.size) {
            int base = this.size * bitCount;
            final int offset = base % Long.SIZE;
            base /= Long.SIZE;
            if (offset != 0 && base < values.length) {
                values[base] &= (1L << offset) - 1;
                base++;
            }
            final int length = length(size);
            Arrays.fill(values, base, Math.min(length, values.length), 0L);
            if (length > values.length) {
                values = XArray.resize(values, length);
            }
        }
        this.size = size;
    }

    /**
     * Discarts all elements in this list.
     */
    @Override
    public void clear() {
        size = 0;
    }

    /**
     * Adds the given element to this list.
     *
     * @param value The value to add.
     * @throws NullPointerException if the given value is null.
     * @throws IllegalArgumentException if the given value is out of bounds.
     */
    @Override
    public boolean add(final Integer value) throws IllegalArgumentException {
        addInteger(value);
        return true;
    }

    /**
     * Adds the given element as the {@code int} primitive type.
     *
     * @param value The value to add.
     * @throws IllegalArgumentException if the given value is out of bounds.
     */
    public void addInteger(final int value) throws IllegalArgumentException {
        final int length = length(++size);
        if (length > values.length) {
            values = XArray.resize(values, 2*values.length);
        }
        try {
            setInteger(size - 1, value);
        } catch (RuntimeException exception) {
            size--; // Roll back the increase in size.
            throw exception;
        }
    }

    /**
     * Returns the element at the given index.
     *
     * @param index The element index.
     * @return The value at the given index.
     * @throws IndexOutOfBoundsException if the given index is out of bounds.
     */
    public Integer get(final int index) throws IndexOutOfBoundsException {
        return getInteger(index);
    }

    /**
     * Returns the element at the given index as the {@code int} primitive type.
     *
     * @param index The element index.
     * @return The value at the given index.
     * @throws IndexOutOfBoundsException if the given index is out of bounds.
     */
    public int getInteger(int index) throws IndexOutOfBoundsException {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException(Errors.format(ErrorKeys.INDEX_OUT_OF_BOUNDS_$1, index));
        }
        index *= bitCount;
        int base   = index / Long.SIZE;
        int offset = index % Long.SIZE;
        int value  = (int) (values[base] >>> offset);
        offset = Long.SIZE - offset;
        if (offset < bitCount) {
            final int high = (int) values[++base];
            value |= (high << offset);
        }
        value &= mask;
        return value;
    }

    /**
     * Sets the element at the given index.
     *
     * @param index The element index.
     * @param value The value at the given index.
     * @return The previous value at the given index.
     * @throws IndexOutOfBoundsException if the given index is out of bounds.
     * @throws IllegalArgumentException if the given value is out of bounds.
     * @throws NullPointerException if the given value is null.
     */
    @Override
    public Integer set(final int index, final Integer value) throws IndexOutOfBoundsException {
        final Integer old = get(index);
        setInteger(index, value);
        return old;
    }

    /**
     * Sets the element at the given index as the {@code int} primitive type.
     *
     * @param index The element index.
     * @param value The value at the given index.
     * @throws IndexOutOfBoundsException if the given index is out of bounds.
     * @throws IllegalArgumentException if the given value is out of bounds.
     */
    public void setInteger(int index, int value) throws IndexOutOfBoundsException {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException(Errors.format(ErrorKeys.INDEX_OUT_OF_BOUNDS_$1, index));
        }
        if (value < 0 || value > mask) {
            throw new IllegalArgumentException(Errors.format(ErrorKeys.VALUE_OUT_OF_BOUNDS_$3,
                    value, 0, mask));
        }
        index *= bitCount;
        int base   = index / Long.SIZE;
        int offset = index % Long.SIZE;
        values[base] &= ~(((long) mask) << offset);
        values[base] |= ((long) value) << offset;
        offset = Long.SIZE - offset;
        if (offset < bitCount) {
            value >>>= offset;
            values[++base] &= ~(((long) mask) >>> offset);
            values[base] |= value;
        }
    }

    /**
     * Trims the capacity of this list to be its current size.
     */
    public void trimToSize() {
        values = XArray.resize(values, size * bitCount / Long.SIZE);
    }

    /**
     * Returns a clone of this list.
     *
     * @return A clone of this list.
     */
    @Override
    public IntegerList clone() {
        final IntegerList clone;
        try {
            clone = (IntegerList) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
        clone.values = clone.values.clone();
        return clone;
    }
}
