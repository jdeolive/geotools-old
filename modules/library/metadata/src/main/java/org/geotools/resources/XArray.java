/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2001-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.resources;

import java.lang.reflect.Array;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.util.Comparator;
import java.util.Locale;
import org.geotools.util.Utilities;


/**
 * Simple operations on arrays. This class provides a central place for
 * inserting and deleting elements in an array, as well as resizing the array.
 * This class may be removed if JavaSoft provide some language construct
 * functionally equivalent to C/C++'s {@code realloc}.
 *
 * @since 2.0
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux (IRD)
 *
 * @todo Replace all {@code resize} methods by {@code Arrays.copyOf} when we will be allowed to
 *       compile for Java 6.
 */
public final class XArray {
    /**
     * All object constructions of this class are forbidden.
     */
    private XArray() {
    }

    /**
     * Returns a new table which contains the same elements as {@code array} but with the
     * {@code length} specified. If the desired {@code length} is longer than the initial
     * length of the {@code array} table, the returned table will contain all the elements
     * of {@code array} as well as the elements initialised to {@code null} at the end of
     * the table. If, on the contrary, the desired {@code length} is shorter than the initial
     * length of the {@code array} table, the table will be truncated (that is to say the
     * surplus {@code array} elements will be forgotten). If the length of {@code array} is
     * equal to {@code length}, then {@code array} will be returned as it stands.
     *
     * @param  array Table to copy
     * @param  length Length of the desired table.
     * @return Table of the same type as {@code array}, of length
     *         {@code length} and containing the data from {@code array}.
     */
    private static <T> T doResize(final T array, final int length) {
        final int current = array == null ? 0 : Array.getLength(array);
        if (current != length) {
            @SuppressWarnings("unchecked")
            final T newArray = (T) Array.newInstance(array.getClass().getComponentType(), length);
            System.arraycopy(array, 0, newArray, 0, Math.min(current, length));
            return newArray;
        } else {
            return array;
        }
    }

    /**
     * Returns a new table which contains the same elements as {@code array} but with the
     * {@code length} specified. If the desired {@code length} is longer than the initial
     * length of the {@code array} table, the returned table will contain all the elements
     * of {@code array} as well as the elements initialised to {@code null} at the end of
     * the table. If, on the contrary, the desired {@code length} is shorter than the initial
     * length of the {@code array} table, the table will be truncated (that is to say the surplus
     * {@code array} elements will be forgotten). If the length of {@code array} is equal to
     * {@code length}, then {@code array} will be returned as it stands.
     *
     * @param  array Table to copy.
     * @param  length Length of the desired table.
     * @return Table of the same type as {@code array}, of length
     *         {@code length} and containing the data from {@code array}.
     */
    public static <E> E[] resize(final E[] array, final int length) {
        return doResize(array, length);
    }

    /**
     * Returns a new table which contains the same elements as {@code array} but with the
     * {@code length} specified. If the desired {@code length} is longer than the initial
     * length of the {@code array} table, the returned table will contain all the elements
     * of {@code array} as well as the elements initialised to {@code null} at the end of
     * the table. If, on the contrary, the desired {@code length} is shorter than the initial
     * length of the {@code array} table, the table will be truncated (that is to say the surplus
     * {@code array} elements will be forgotten). If the length of {@code array} is equal to
     * {@code length}, then {@code array} will be returned as it stands.
     *
     * @param  array Table to copy.
     * @param  length Length of the desired table.
     * @return Table of the same type as {@code array}, of length
     *         {@code length} and containing the data from {@code array}.
     */
    public static double[] resize(final double[] array, final int length) {
        return doResize(array, length);
    }

    /**
     * Returns a new table which contains the same elements as {@code array} but with the
     * {@code length} specified. If the desired {@code length} is longer than the initial
     * length of the {@code array} table, the returned table will contain all the elements
     * of {@code array} as well as the elements initialised to {@code null} at the end of
     * the table. If, on the contrary, the desired {@code length} is shorter than the initial
     * length of the {@code array} table, the table will be truncated (that is to say the surplus
     * {@code array} elements will be forgotten). If the length of {@code array} is equal to
     * {@code length}, then {@code array} will be returned as it stands.
     *
     * @param  array Table to copy.
     * @param  length Length of the desired table.
     * @return Table of the same type as {@code array}, of length
     *         {@code length} and containing the data from {@code array}.
     */
    public static float[] resize(final float[] array, final int length) {
        return doResize(array, length);
    }

    /**
     * Returns a new table which contains the same elements as {@code array} but with the
     * {@code length} specified. If the desired {@code length} is longer than the initial
     * length of the {@code array} table, the returned table will contain all the elements
     * of {@code array} as well as the elements initialised to {@code null} at the end of
     * the table. If, on the contrary, the desired {@code length} is shorter than the initial
     * length of the {@code array} table, the table will be truncated (that is to say the surplus
     * {@code array} elements will be forgotten). If the length of {@code array} is equal to
     * {@code length}, then {@code array} will be returned as it stands.
     *
     * @param  array Table to copy.
     * @param  length Length of the desired table.
     * @return Table of the same type as {@code array}, of length
     *         {@code length} and containing the data from {@code array}.
     */
    public static long[] resize(final long[] array, final int length) {
        return doResize(array, length);
    }

    /**
     * Returns a new table which contains the same elements as {@code array} but with the
     * {@code length} specified. If the desired {@code length} is longer than the initial
     * length of the {@code array} table, the returned table will contain all the elements
     * of {@code array} as well as the elements initialised to {@code null} at the end of
     * the table. If, on the contrary, the desired {@code length} is shorter than the initial
     * length of the {@code array} table, the table will be truncated (that is to say the
     * surplus {@code array} elements will be forgotten). If the length of {@code array} is
     * equal to {@code length}, then {@code array} will be returned as it stands.
     *
     * @param  array Table to copy.
     * @param  length Length of the desired table.
     * @return Table of the same type as {@code array}, of length
     *         {@code length} and containing the data from {@code array}.
     */
    public static int[] resize(final int[] array, final int length) {
        return doResize(array, length);
    }

    /**
     * Returns a new table which contains the same elements as {@code array} but with the
     * {@code length} specified. If the desired {@code length} is longer than the initial
     * length of the {@code array} table, the returned table will contain all the elements
     * of {@code array} as well as the elements initialised to {@code null} at the end of
     * the table. If, on the contrary, the desired {@code length} is shorter than the initial
     * length of the {@code array} table, the table will be truncated (that is to say the
     * surplus {@code array} elements will be forgotten). If the length of {@code array} is
     * equal to {@code length}, then {@code array} will be returned as it stands.
     *
     * @param  array Table to copy.
     * @param  length Length of the desired table.
     * @return Table of the same type as {@code array}, of length
     *         {@code length} and containing the data from {@code array}.
     */
    public static short[] resize(final short[] array, final int length) {
        return doResize(array, length);
    }

    /**
     * Returns a new table which contains the same elements as {@code array} but with the
     * {@code length} specified. If the desired {@code length} is longer than the initial
     * length of the {@code array} table, the returned table will contain all the elements
     * of {@code array} as well as the elements initialised to {@code null} at the end of
     * the table. If, on the contrary, the desired {@code length} is shorter than the initial
     * length of the {@code array} table, the table will be truncated (that is to say the surplus
     * {@code array} elements will be forgotten). If the length of {@code array} is equal to
     * {@code length}, then {@code array} will be returned as it stands.
     *
     * @param  array Table to copy.
     * @param  length Length of the desired table.
     * @return Table of the same type as {@code array}, of length
     *         {@code length} and containing the data from {@code array}.
     */
    public static byte[] resize(final byte[] array, final int length) {
        return doResize(array, length);
    }

   /**
    * Returns a new table which contains the same elements as {@code array} but with the
    * {@code length} specified. If the desired {@code length} is longer than the initial
    * length of the {@code array} table, the returned table will contain all the elements
    * of {@code array} as well as the elements initialised to {@code null} at the end of
    * the table. If, on the contrary, the desired {@code length} is shorter than the initial
    * length of the {@code array} table, the table will be truncated (that is to say the surplus
    * {@code array} elements will be forgotten). If the length of {@code array} is equal to
    * {@code length}, then {@code array} will be returned as it stands.
    *
    * @param  array Table to copy.
    * @param  length Length of the desired table.
    * @return Table of the same type as {@code array}, of length
    *         {@code length} and containing the data from {@code array}.
    */
    public static char[] resize(final char[] array, final int length) {
        return doResize(array, length);
    }

    /**
     * Returns a new table which contains the same elements as {@code array} but with the
     * {@code length} specified. If the desired {@code length} is longer than the initial
     * length of the {@code array} table, the returned table will contain all the elements
     * of {@code array} as well as the elements initialised to {@code null} at the end of
     * the table. If, on the contrary, the desired {@code length} is shorter than the initial
     * length of the {@code array} table, the table will be truncated (that is to say the surplus
     * {@code array} elements will be forgotten). If the length of {@code array} is equal to
     * {@code length}, then {@code array} will be returned as it stands.
     *
     * @param  array Table to copy.
     * @param  length Length of the desired table.
     * @return Table of the same type as {@code array}, of length
     *         {@code length} and containing the data from {@code array}.
     */
    public static boolean[] resize(final boolean[] array, final int length) {
        return doResize(array, length);
    }

    /**
     * Grabs elements from the middle of a table.
     *
     * @param array   Table from which to grab elements.
     * @param index   {@code array} index of the first element to grab.
     *                All subsequent elements of {@code array} can be moved forward.
     * @param length  Number of elements to grab.
     * @return        Table which contains the {@code array} data with the
     *                extracted elements.  This method can directly return
     *                {@code dst}, but most often it returns a newly created table.
     */
    private static <T> T doRemove(final T array, final int index, final int length) {
        if (length == 0) {
            return array;
        }
        int arrayLength = Array.getLength(array);
        @SuppressWarnings("unchecked")
        final T newArray = (T) Array.newInstance(array.getClass().getComponentType(), arrayLength -= length);
        System.arraycopy(array, 0,            newArray, 0,                 index);
        System.arraycopy(array, index+length, newArray, index, arrayLength-index);
        return newArray;
    }

    /**
     * Grabs elements from the middle of a table.
     *
     * @param array   Table from which to grab the elements.
     * @param index   {@code array} index of the first element to grab.
     *                All subsequent {@code array} elements can be moved forward.
     * @param length  Number of elements to grab.
     * @return        Table which contains the {@code array} data with the
     *                extracted elements.  This method can directly return
     *                {@code dst}, but most often it returns a newly created table.
     */
    public static <E> E[] remove(final E[] array, final int index, final int length) {
        return doRemove(array, index, length);
    }

   /**
     * Grabs elements from the middle of a table.
     *
     * @param array   Table from which to grab elements.
     * @param index   {@code array} index of the first element to grab.
     *                All subsequent elements of {@code array} can be moved forward.
     * @param length  Number of elements to grab.
     * @return        Table which contains the {@code array} data with the
     *                extracted elements.  This method can directly return
     *                {@code dst}, but most often it returns a newly created table.
     */
    public static double[] remove(final double[] array, final int index, final int length) {
        return doRemove(array, index, length);
    }

   /**
     * Grabs elements from the middle of a table.
     *
     * @param array   Table from which to grab elements.
     * @param index   {@code array} index of the first element to grab.
     *                All subsequent elements of {@code array} can be moved forward.
     * @param length  Number of elements to grab.
     * @return        Table which contains the {@code array} data with the
     *                extracted elements.  This method can directly return
     *                {@code dst}, but most often it returns a newly created table.
     */
    public static float[] remove(final float[] array, final int index, final int length) {
        return doRemove(array, index, length);
    }

    /**
     * Grabs elements from the middle of a table.
     *
     * @param array   Table from which to grab elements.
     * @param index   {@code array} index of the first element to grab.
     *                All subsequent elements of {@code array} can be moved forward.
     * @param length  Number of elements to grab.
     * @return        Table which contains the {@code array} data with the
     *                extracted elements.  This method can directly return
     *                {@code dst}, but most often it returns a newly created table.
     */
    public static long[] remove(final long[] array, final int index, final int length) {
        return doRemove(array, index, length);
    }

    /**
     * Grabs elements from the middle of a table.
     *
     * @param array   Table from which to grab elements.
     * @param index   {@code array} index of the first element to grab.
     *                All subsequent elements of {@code array} can be moved forward.
     * @param length  Number of elements to grab.
     * @return        Table which contains the {@code array} data with the
     *                extracted elements.  This method can directly return
     *                {@code dst}, but most often it returns a newly created table.
     */
    public static int[] remove(final int[] array, final int index, final int length) {
        return doRemove(array, index, length);
    }

    /**
     * Grabs elements from the middle of a table.
     *
     * @param array   Table from which to grab elements.
     * @param index   {@code array} index of the first element to grab.
     *                All subsequent elements of {@code array} can be moved forward.
     * @param length  Number of elements to grab.
     * @return        Table which contains the {@code array} data with the
     *                extracted elements.  This method can directly return
     *                {@code dst}, but most often it returns a newly created table.
     */
    public static short[] remove(final short[] array, final int index, final int length) {
        return doRemove(array, index, length);
    }

    /**
     * Grabs elements from the middle of a table.
     *
     * @param array   Table from which to grab elements.
     * @param index   {@code array} index of the first element to grab.
     *                All subsequent elements of {@code array} can be moved forward.
     * @param length  Number of elements to grab.
     * @return        Table which contains the {@code array} data with the
     *                extracted elements.  This method can directly return
     *                {@code dst}, but most often it returns a newly created table.
     */
    public static byte[] remove(final byte[] array, final int index, final int length) {
        return doRemove(array, index, length);
    }

    /**
     * Grabs elements from the middle of a table.
     *
     * @param array   Table from which to grab elements.
     * @param index   {@code array} index of the first element to grab.
     *                All subsequent elements of {@code array} can be moved forward.
     * @param length  Number of elements to grab.
     * @return        Table which contains the {@code array} data with the
     *                extracted elements.  This method can directly return
     *                {@code dst}, but most often it returns a newly created table.
     */
    public static char[] remove(final char[] array, final int index, final int length) {
        return doRemove(array, index, length);
    }

    /**
     * Grabs elements from the middle of a table.
     *
     * @param array   Table from which to grab elements.
     * @param index   {@code array} index of the first element to grab.
     *                All subsequent elements of {@code array} can be moved forward.
     * @param length  Number of elements to grab.
     * @return        Table which contains the {@code array} data with the
     *                extracted elements.  This method can directly return
     *                {@code dst}, but most often it returns a newly created table.
     */
    public static boolean[] remove(final boolean[] array, final int index, final int length) {
        return doRemove(array, index, length);
    }

    /**
     * Inserts spaces into the middle of a table.  These "spaces" will be made
     * up of null elements.
     *
     * @param array   Table in which to insert spaces.
     * @param index   {@code array} index where spaces should be inserted.
     *                All {@code array} elements which have an index equal
     *                to or higher than {@code index} will be moved forward.
     * @param length  Number of spaces to insert.
     * @return        Table which contains the {@code array} data with the
     *                additional space. This method can directly return
     *                {@code dst}, but most often it returns a newly created table.
     */
    private static <T> T doInsert(final T array, final int index, final int length) {
        if (length == 0) {
            return array;
        }
        final int arrayLength = Array.getLength(array);
        @SuppressWarnings("unchecked")
        final T newArray = (T) Array.newInstance(array.getClass().getComponentType(), arrayLength + length);
        System.arraycopy(array, 0,     newArray, 0,            index            );
        System.arraycopy(array, index, newArray, index+length, arrayLength-index);
        return newArray;
    }

    /**
     * Inserts spaces into the middle of a table.  These "spaces" will be made up of null elements.
     *
     * @param array   Table in which to insert spaces.
     * @param index   {@code array} index where spaces should be inserted.
     *                All {@code array} elements which have an index equal
     *                to or higher than {@code index} will be moved forward.
     * @param length  Number of spaces to insert.
     * @return        Table which contains the {@code array} data with the
     *                additional space. This method can directly return
     *                {@code dst}, but most often it returns a newly created table.
     */
    public static <E> E[] insert(final E[] array, final int index, final int length) {
        return doInsert(array, index, length);
    }

    /**
     * Inserts spaces into the middle of a table.  These "spaces" will be made up of zeros.
     *
     * @param array   Table in which to insert spaces.
     * @param index   {@code array} index where spaces should be inserted.
     *                All {@code array} elements which have an index equal
     *                to or higher than {@code index} will be moved forward.
     * @param length  Number of spaces to insert.
     * @return        Table which contains the {@code array} data with the
     *                additional space. This method can directly return
     *                {@code dst}, but most often it returns a newly created table.
     */
    public static double[] insert(final double[] array, final int index, final int length) {
        return doInsert(array, index, length);
    }

    /**
     * Inserts spaces into the middle of a table.  These "spaces" will be made up of zeros.
     *
     * @param array   Table in which to insert spaces.
     * @param index   {@code array} index where spaces should be inserted.
     *                All {@code array} elements which have an index equal
     *                to or higher than {@code index} will be moved forward.
     * @param length  Number of spaces to insert.
     * @return        Table which contains the {@code array} data with the
     *                additional space. This method can directly return
     *                {@code dst}, but most often it returns a newly created table.
     */
    public static float[] insert(final float[] array, final int index, final int length) {
        return doInsert(array, index, length);
    }

    /**
     * Inserts spaces into the middle of a table.  These "spaces" will be made up of zeros.
     *
     * @param array   Table in which to insert spaces.
     * @param index   {@code array} index where spaces should be inserted.
     *                All {@code array} elements which have an index equal
     *                to or higher than {@code index} will be moved forward.
     * @param length  Number of spaces to insert.
     * @return        Table which contains the {@code array} data with the
     *                additional space. This method can directly return
     *                {@code dst}, but most often it returns a newly created table.
     */
    public static long[] insert(final long[] array, final int index, final int length) {
        return doInsert(array, index, length);
    }

    /**
     * Inserts spaces into the middle of a table.  These "spaces" will be made up of zeros.
     *
     * @param array   Table in which to insert spaces.
     * @param index   {@code array} index where spaces should be inserted.
     *                All {@code array} elements which have an index equal
     *                to or higher than {@code index} will be moved forward.
     * @param length  Number of spaces to insert.
     * @return        Table which contains the {@code array} data with the
     *                additional space. This method can directly return
     *                {@code dst}, but most often it returns a newly created table.
     */
    public static int[] insert(final int[] array, final int index, final int length) {
        return doInsert(array, index, length);
    }

    /**
     * Inserts spaces into the middle of a table.  These "spaces" will be made up of zeros.
     *
     * @param array   Table in which to insert spaces.
     * @param index   {@code array} index where spaces should be inserted.
     *                All {@code array} elements which have an index equal
     *                to or higher than {@code index} will be moved forward.
     * @param length  Number of spaces to insert.
     * @return        Table which contains the {@code array} data with the
     *                additional space. This method can directly return
     *                {@code dst}, but most often it returns a newly created table.
     */
    public static short[] insert(final short[] array, final int index, final int length) {
        return doInsert(array, index, length);
    }

    /**
     * Inserts spaces into the middle of a table.  These "spaces" will be made up of zeros.
     *
     * @param array   Table in which to insert spaces.
     * @param index   {@code array} index where spaces should be inserted.
     *                All {@code array} elements which have an index equal
     *                to or higher than {@code index} will be moved forward.
     * @param length  Number of spaces to insert.
     * @return        Table which contains the {@code array} data with the
     *                additional space. This method can directly return
     *                {@code dst}, but most often it returns a newly created table.
     */
    public static byte[] insert(final byte[] array, final int index, final int length) {
        return doInsert(array, index, length);
    }

    /**
     * Inserts spaces into the middle of a table.  These "spaces" will be made up of zeros.
     *
     * @param array   Table in which to insert spaces.
     * @param index   {@code array} index where spaces should be inserted.
     *                All {@code array} elements which have an index equal
     *                to or higher than {@code index} will be moved forward.
     * @param length  Number of spaces to insert.
     * @return        Table which contains the {@code array} data with the
     *                additional space. This method can directly return
     *                {@code dst}, but most often it returns a newly created table.
     */
    public static char[] insert(final char[] array, final int index, final int length) {
        return doInsert(array, index, length);
    }

    /**
     * Inserts spaces into the middle of a table.  These "spaces" will be made up of {@code false}.
     *
     * @param array   Table in which to insert spaces.
     * @param index   {@code array} index where spaces should be inserted.
     *                All {@code array} elements which have an index equal
     *                to or higher than {@code index} will be moved forward.
     * @param length  Number of spaces to insert.
     * @return        Table which contains the {@code array} data with the
     *                additional space. This method can directly return
     *                {@code dst}, but most often it returns a newly created table.
     */
    public static boolean[] insert(final boolean[] array, final int index, final int length) {
        return doInsert(array, index, length);
    }

    /**
     * Inserts a table slice into another table.  The {@code src} table
     * will be entirely or partially inserted into the {@code dst} table.
     *
     * @param src     Table to insert into {@code dst}.
     * @param src_pos Index of the first data item of {@code src} to insert into {@code dst}.
     * @param dst     Table in which to insert {@code src} data.
     * @param dst_pos {@code dst} index in which to insert {@code src} data. All elements of
     *                {@code dst} whose index is equal to or greater than {@code dst_pos} will
     *                be moved forward.
     * @param length  Number of {@code src} data items to insert.
     * @return        Table which contains the combination of {@code src} and {@code dst}. This
     *                method can directly return {@code dst}, but never {@code src}. It most
     *                often returns a newly created table.
     */
    // Returns type should be T, but it doesn't seem to work with Java 5.
    // Revisit when we will be allowed to compile for Java 6 (it does work for the later).
    private static <T> Object doInsert(final T src, final int src_pos,
                                       final T dst, final int dst_pos, final int length)
    {
        if (length == 0) {
            return dst;
        }
        final int dstLength = Array.getLength(dst);
        @SuppressWarnings("unchecked")
        final T newArray = (T) Array.newInstance(dst.getClass().getComponentType(), dstLength+length);
        System.arraycopy(dst, 0,       newArray, 0,              dst_pos          );
        System.arraycopy(src, src_pos, newArray, dst_pos,        length           );
        System.arraycopy(dst, dst_pos, newArray, dst_pos+length, dstLength-dst_pos);
        return newArray;
    }

    /**
     * Inserts a table slice into another table.  The {@code src} table
     * will be entirely or partially inserted into the {@code dst} table.
     *
     * @param src     Tablea to insert into {@code dst}.
     * @param src_pos Index of the first data item of {@code src} to insert into {@code dst}.
     * @param dst     Table in which to insert {@code src} data.
     * @param dst_pos {@code dst} index in which to insert {@code src} data. All elements of
     *                {@code dst} whose index is equal to or greater than {@code dst_pos} will
     *                be moved forward.
     * @param length  Number of {@code src} data items to insert.
     * @return        Table which contains the combination of {@code src} and {@code dst}. This
     *                method can directly return {@code dst}, but never {@code src}. It most
     *                often returns a newly created table.
     */
    @SuppressWarnings("unchecked")
    public static <E> E[] insert(final E[] src, final int src_pos,
                                 final E[] dst, final int dst_pos, final int length)
    {
        // Following cast is required for Java 5 (remove with Java 6).
        return (E[]) doInsert(src, src_pos, dst, dst_pos, length);
    }

    /**
     * Inserts a table slice into another table.  The {@code src} table
     * will be entirely or partially inserted into the {@code dst} table.
     *
     * @param src     Tablea to insert into {@code dst}.
     * @param src_pos Index of the first data item of {@code src} to insert into {@code dst}.
     * @param dst     Table in which to insert {@code src} data.
     * @param dst_pos {@code dst} index in which to insert {@code src} data. All elements of
     *                {@code dst} whose index is equal to or greater than {@code dst_pos} will
     *                be moved forward.
     * @param length  Number of {@code src} data items to insert.
     * @return        Table which contains the combination of {@code src} and {@code dst}. This
     *                method can directly return {@code dst}, but never {@code src}. It most
     *                often returns a newly created table.
     */
    public static double[] insert(final double[] src, final int src_pos,
                                  final double[] dst, final int dst_pos, final int length)
    {
        // Following cast is required for Java 5 (remove with Java 6).
        return (double[]) doInsert(src, src_pos, dst, dst_pos, length);
    }

    /**
     * Inserts a table slice into another table.  The {@code src} table
     * will be entirely or partially inserted into the {@code dst} table.
     *
     * @param src     Tablea to insert into {@code dst}.
     * @param src_pos Index of the first data item of {@code src} to insert into {@code dst}.
     * @param dst     Table in which to insert {@code src} data.
     * @param dst_pos {@code dst} index in which to insert {@code src} data. All elements of
     *                {@code dst} whose index is equal to or greater than {@code dst_pos} will
     *                be moved forward.
     * @param length  Number of {@code src} data items to insert.
     * @return        Table which contains the combination of {@code src} and {@code dst}. This
     *                method can directly return {@code dst}, but never {@code src}. It most
     *                often returns a newly created table.
     */
    public static float[] insert(final float[] src, final int src_pos,
                                 final float[] dst, final int dst_pos, final int length)
    {
        // Following cast is required for Java 5 (remove with Java 6).
        return (float[]) doInsert(src, src_pos, dst, dst_pos, length);
    }

    /**
     * Inserts a table slice into another table.  The {@code src} table
     * will be entirely or partially inserted into the {@code dst} table.
     *
     * @param src     Tablea to insert into {@code dst}.
     * @param src_pos Index of the first data item of {@code src} to insert into {@code dst}.
     * @param dst     Table in which to insert {@code src} data.
     * @param dst_pos {@code dst} index in which to insert {@code src} data. All elements of
     *                {@code dst} whose index is equal to or greater than {@code dst_pos} will
     *                be moved forward.
     * @param length  Number of {@code src} data items to insert.
     * @return        Table which contains the combination of {@code src} and {@code dst}. This
     *                method can directly return {@code dst}, but never {@code src}. It most
     *                often returns a newly created table.
     */
    public static long[] insert(final long[] src, final int src_pos,
                                final long[] dst, final int dst_pos, final int length)
    {
        // Following cast is required for Java 5 (remove with Java 6).
        return (long[]) doInsert(src, src_pos, dst, dst_pos, length);
    }

    /**
     * Inserts a table slice into another table.  The {@code src} table
     * will be entirely or partially inserted into the {@code dst} table.
     *
     * @param src     Tablea to insert into {@code dst}.
     * @param src_pos Index of the first data item of {@code src} to insert into {@code dst}.
     * @param dst     Table in which to insert {@code src} data.
     * @param dst_pos {@code dst} index in which to insert {@code src} data. All elements of
     *                {@code dst} whose index is equal to or greater than {@code dst_pos} will
     *                be moved forward.
     * @param length  Number of {@code src} data items to insert.
     * @return        Table which contains the combination of {@code src} and {@code dst}. This
     *                method can directly return {@code dst}, but never {@code src}. It most
     *                often returns a newly created table.
     */
    public static int[] insert(final int[] src, final int src_pos,
                               final int[] dst, final int dst_pos, final int length)
    {
        // Following cast is required for Java 5 (remove with Java 6).
        return (int[]) doInsert(src, src_pos, dst, dst_pos, length);
    }

    /**
     * Inserts a table slice into another table.  The {@code src} table
     * will be entirely or partially inserted into the {@code dst} table.
     *
     * @param src     Tablea to insert into {@code dst}.
     * @param src_pos Index of the first data item of {@code src} to insert into {@code dst}.
     * @param dst     Table in which to insert {@code src} data.
     * @param dst_pos {@code dst} index in which to insert {@code src} data. All elements of
     *                {@code dst} whose index is equal to or greater than {@code dst_pos} will
     *                be moved forward.
     * @param length  Number of {@code src} data items to insert.
     * @return        Table which contains the combination of {@code src} and {@code dst}. This
     *                method can directly return {@code dst}, but never {@code src}. It most
     *                often returns a newly created table.
     */
    public static short[] insert(final short[] src, final int src_pos,
                                 final short[] dst, final int dst_pos, final int length)
    {
        // Following cast is required for Java 5 (remove with Java 6).
        return (short[]) doInsert(src, src_pos, dst, dst_pos, length);
    }

    /**
     * Inserts a table slice into another table.  The {@code src} table
     * will be entirely or partially inserted into the {@code dst} table.
     *
     * @param src     Tablea to insert into {@code dst}.
     * @param src_pos Index of the first data item of {@code src} to
     *                insert into {@code dst}.
     * @param dst     Table in which to insert {@code src} data.
     * @param dst_pos {@code dst} index in which to insert {@code src} data. All elements of
     *                {@code dst} whose index is equal to or greater than {@code dst_pos} will
     *                be moved forward.
     * @param length  Number of {@code src} data items to insert.
     * @return        Table which contains the combination of {@code src} and {@code dst}. This
     *                method can directly return {@code dst}, but never {@code src}. It most
     *                often returns a newly created table.
     */
    public static byte[] insert(final byte[] src, final int src_pos,
                                final byte[] dst, final int dst_pos, final int length)
    {
        // Following cast is required for Java 5 (remove with Java 6).
        return (byte[]) doInsert(src, src_pos, dst, dst_pos, length);
    }

    /**
     * Inserts a table slice into another table.  The {@code src} table
     * will be entirely or partially inserted into the {@code dst} table.
     *
     * @param src     Tablea to insert into {@code dst}.
     * @param src_pos Index of the first data item of {@code src} to insert into {@code dst}.
     * @param dst     Table in which to insert {@code src} data.
     * @param dst_pos {@code dst} index in which to insert {@code src} data. All elements of
     *                {@code dst} whose index is equal to or greater than {@code dst_pos} will
     *                be moved forward.
     * @param length  Number of {@code src} data items to insert.
     * @return        Table which contains the combination of {@code src} and {@code dst}. This
     *                method can directly return {@code dst}, but never {@code src}. It most
     *                often returns a newly created table.
     */
    public static char[] insert(final char[] src, final int src_pos,
                                final char[] dst, final int dst_pos, final int length)
    {
        // Following cast is required for Java 5 (remove with Java 6).
        return (char[]) doInsert(src, src_pos, dst, dst_pos, length);
    }

    /**
     * Inserts a table slice into another table.  The {@code src} table
     * will be entirely or partially inserted into the {@code dst} table.
     *
     * @param src     Tablea to insert into {@code dst}.
     * @param src_pos Index of the first data item of {@code src} to insert into {@code dst}.
     * @param dst     Table in which to insert {@code src} data.
     * @param dst_pos {@code dst} index in which to insert {@code src} data. All elements of
     *                {@code dst} whose index is equal to or greater than {@code dst_pos} will
     *                be moved forward.
     * @param length  Number of {@code src} data items to insert.
     * @return        Table which contains the combination of {@code src} and {@code dst}. This
     *                method can directly return {@code dst}, but never {@code src}. It most
     *                often returns a newly created table.
     */
    public static boolean[] insert(final boolean[] src, final int src_pos,
                                   final boolean[] dst, final int dst_pos, final int length)
    {
        // Following cast is required for Java 5 (remove with Java 6).
        return (boolean[]) doInsert(src, src_pos, dst, dst_pos, length);
    }

    /**
     * Returns {@code true} if all elements in the specified array are in increasing order.
     * This method is usefull in assertions.
     */
    public static <T> boolean isSorted(final T[] array, final Comparator<T> comparator) {
        for (int i=1; i<array.length; i++) {
            if (comparator.compare(array[i], array[i-1]) < 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns {@code true} if all elements in the specified array are in increasing order.
     * This method is usefull in assertions.
     */
    public static boolean isSorted(final char[] array) {
        for (int i=1; i<array.length; i++) {
            if (array[i] < array[i-1]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns {@code true} if all elements in the specified array are in increasing order.
     * This method is usefull in assertions.
     */
    public static boolean isSorted(final byte[] array) {
        for (int i=1; i<array.length; i++) {
            if (array[i] < array[i-1]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns {@code true} if all elements in the specified array are in increasing order.
     * This method is usefull in assertions.
     */
    public static boolean isSorted(final short[] array) {
        for (int i=1; i<array.length; i++) {
            if (array[i] < array[i-1]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns {@code true} if all elements in the specified array are in increasing order.
     * This method is usefull in assertions.
     */
    public static boolean isSorted(final int[] array) {
        for (int i=1; i<array.length; i++) {
            if (array[i] < array[i-1]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns {@code true} if all elements in the specified array are in increasing order.
     * This method is usefull in assertions.
     */
    public static boolean isSorted(final long[] array) {
        for (int i=1; i<array.length; i++) {
            if (array[i] < array[i-1]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns {@code true} if all elements in the specified array are in increasing order.
     * Since {@code NaN} values are unordered, they may appears anywhere in the array; they
     * will be ignored. This method is usefull in assertions.
     */
    public static boolean isSorted(final float[] array) {
        int previous = 0;
        for (int i=1; i<array.length; i++) {
            final float value = array[i];
            if (value < array[previous]) {
                return false;
            }
            if (!Float.isNaN(value)) {
                previous = i;
            }
        }
        return true;
    }

    /**
     * Returns {@code true} if all elements in the specified array are in increasing order.
     * Since {@code NaN} values are unordered, they may appears anywhere in the array; they
     * will be ignored. This method is usefull in assertions.
     */
    public static boolean isSorted(final double[] array) {
        int previous = 0;
        for (int i=1; i<array.length; i++) {
            final double value = array[i];
            if (value < array[previous]) {
                return false;
            }
            if (!Double.isNaN(value)) {
                previous = i;
            }
        }
        return true;
    }

    /**
     * Returns {@code true} if all elements in the specified array are in strictly increasing order.
     * This method is usefull in assertions.
     */
    public static boolean isStrictlySorted(final int[] array) {
        for (int i=1; i<array.length; i++) {
            if (array[i] <= array[i-1]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns {@code true} if all values in the specified array are equals to the specified
     * value, which may be {@link Float#NaN}.
     */
    public static boolean allEquals(final float[] array, final float value) {
        if (Float.isNaN(value)) {
            for (int i=0; i<array.length; i++) {
                if (!Float.isNaN(array[i])) {
                    return false;
                }
            }
        } else {
            for (int i=0; i<array.length; i++) {
                if (array[i] != value) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Returns {@code true} if all values in the specified array are equals to the specified
     * value, which may be {@link Double#NaN}.
     */
    public static boolean allEquals(final double[] array, final double value) {
        if (Double.isNaN(value)) {
            for (int i=0; i<array.length; i++) {
                if (!Double.isNaN(array[i])) {
                    return false;
                }
            }
        } else {
            for (int i=0; i<array.length; i++) {
                if (array[i] != value) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Returns {@code true} if the specified array contains at least one
     * {@link Float#NaN NaN} value.
     */
    public static boolean hasNaN(final float[] array) {
        for (int i=0; i<array.length; i++) {
            if (Float.isNaN(array[i])) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns {@code true} if the specified array contains at least one
     * {@link Double#NaN NaN} value.
     */
    public static boolean hasNaN(final double[] array) {
        for (int i=0; i<array.length; i++) {
            if (Double.isNaN(array[i])) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns {@code true} if the specified array contains the specified value, ignoring case.
     * This method should be used only for very small arrays.
     *
     * @param  array The array to search in. May be {@code null}.
     * @param  value The value to search.
     * @return {@code true} if the array contains the value.
     */
    public static boolean containsIgnoreCase(final String[] array, final String value) {
        if (array != null) {
            for (final String element : array) {
                if (value.equalsIgnoreCase(element)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns {@code true} if the specified array contains the specified value. This method
     * should be used only for very small arrays, or for search to be performed only once,
     * because it performs a linear search. If more than one search need to be done on the
     * same array, consider using {@link java.util.HashSet} instead.
     *
     * @param  array The array to search in. May be {@code null} and may contains null elements.
     * @param  value The value to search. May be {@code null}.
     * @return {@code true} if the array contains the value.
     */
    public static boolean contains(final Object[] array, final Object value) {
        if (array != null) {
            for (final Object element : array) {
                if (Utilities.equals(element, value)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns {@code true} if at least one element in the first array is {@linkplain Object#equals
     * equals} to an element in the second array. The element doesn't need to be at the same index
     * in both array.
     * <p>
     * This method should be used only for very small arrays since it may be very slow. If the
     * arrays are large or if an array will be involved in more than one search, consider using
     * {@link java.util.HashSet} instead.
     */
    public static boolean intersects(final Object[] array1, final Object[] array2) {
        for (final Object element : array1) {
            if (contains(array2, element)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns a string representation of an array of numbers. Current implementation
     * supports only primitive or subclasses of {@link Number}.
     *
     * @param  array The array to format.
     * @param  locale The locale for formatting.
     * @return The formatted array.
     *
     * @todo The separator should be local-dependent.
     * @todo Should we implements this functionality in {@link org.geotools.io.LineFormat} instead?
     */
    public static String toString(final Object array, final Locale locale) {
        final StringBuffer buffer = new StringBuffer();
        final NumberFormat format = NumberFormat.getNumberInstance(locale);
        final FieldPosition dummy = new FieldPosition(0);
        final int length = Array.getLength(array);
        for (int i=0; i<length; i++) {
            if (i != 0) {
                buffer.append(", "); // TODO: the separator should be local-dependent.
            }
            format.format(Array.get(array, i), buffer, dummy);
        }
        return buffer.toString();
    }
}
