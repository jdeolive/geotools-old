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

// Miscellaneous
import java.util.Locale;
import java.lang.System;
import java.lang.reflect.Array;
import java.text.FieldPosition;
import java.text.NumberFormat;


/**
 * Simple operations on arrays. This class provides a central place for
 * inserting and deleting elements in an array, as well as resizing the array.
 * This class may be removed if JavaSoft provide some language construct
 * functionally equivalent to C/C++'s <code>realloc</code>.
 *
 * @version $Id: XArray.java,v 1.7 2003/11/15 16:01:32 aaime Exp $
 * @author Martin Desruisseaux
 */
public final class XArray {
    /**
     * All object constructions of this class are forbidden.
     */
    private XArray() {
    }

    /**
     * Returns a new table which contains the same elements as
     * <code>array</code> but with the <code>length</code> specified.
     * If the desired <code>length</code> is longer than the initial
     * length of the <code>array</code> table, the returned table will contain
     * all the elements of <code>array</code> as well as the elements
     * initialised to <code>null</code> at the end of the table. If, on the
     * contrary, the desired <code>length</code> is shorter than the initial
     * length of the <code>array</code> table, the table will be truncated
     * (that is to say the surplus <code>array</code> elements will be
     * forgotten). If the length of <code>array</code> is equal to
     * <code>length</code>, then <code>array</code> will be returned as it stands.
     *
     * @param  array Table to copy
     * @param  length Length of the desired table.
     * @return Table of the same type as <code>array</code>, of length
     *         <code>length</code> and containing the data from
     *         <code>array</code>.
     */
    private static Object doResize(final Object array, final int length) {
        final int current = array == null ? 0 : Array.getLength(array);
        if (current!=length) {
            final Object newArray=Array.newInstance(array.getClass().getComponentType(), length);
            System.arraycopy(array, 0, newArray, 0, Math.min(current, length));
            return newArray;
        }
        else return array;
    }

    /**
     * Returns a new table which contains the same elements as
     * <code>array</code> but with the <code>length</code> specified.
     * If the desired <code>length</code> is longer than the initial
     * length of the <code>array</code> table, the returned table will contain
     * all the elements of <code>array</code> as well as the elements
     * initialised to <code>null</code> at the end of the table. If, on the
     * contrary, the desired <code>length</code> is shorter than the initial
     * length of the <code>array</code> table, the table will be truncated
     * (that is to say the surplus <code>array</code> elements will be
     * forgotten). If the length of <code>array</code> is equal to
     * <code>length</code>, then <code>array</code> will be returned as it
     * stands.
     *
     * @param  array Table to copy.
     * @param  length Length of the desired table.
     * @return Table of the same type as <code>array</code>, of length
     *         <code>length</code> and containing the data from
     *         <code>array</code>.
     */
    public static Object[] resize(final Object[] array, final int length) {
        return (Object[]) doResize(array, length);
    }

    /**
     * Returns a new table which contains the same elements as
     * <code>array</code> but with the <code>length</code> specified.
     * If the desired <code>length</code> is longer than the initial
     * length of the <code>array</code> table, the returned table will contain
     * all the elements of <code>array</code> as well as the elements
     * initialised to <code>null</code> at the end of the table. If, on the
     * contrary, the desired <code>length</code> is shorter than the initial
     * length of the <code>array</code> table, the table will be truncated
     * (that is to say the surplus <code>array</code> elements will be
     * forgotten). If the length of <code>array</code> is equal to
     * <code>length</code>, then <code>array</code> will be returned as it
     * stands.
     *
     * @param  array Table to copy.
     * @param  length Length of the desired table.
     * @return Table of the same type as <code>array</code>, of length
     *         <code>length</code> and containing the data from
     *         <code>array</code>.
     */
    public static double[] resize(final double[] array, final int length) {
        return (double[]) doResize(array, length);
    }

    /**
     * Returns a new table which contains the same elements as
     * <code>array</code> but with the <code>length</code> specified.
     * If the desired <code>length</code> is longer than the initial
     * length of the <code>array</code> table, the returned table will contain
     * all the elements of <code>array</code> as well as the elements
     * initialised to <code>null</code> at the end of the table. If, on the
     * contrary, the desired <code>length</code> is shorter than the initial
     * length of the <code>array</code> table, the table will be truncated
     * (that is to say the surplus <code>array</code> elements will be
     * forgotten). If the length of <code>array</code> is equal to
     * <code>length</code>, then <code>array</code> will be returned as it
     * stands.
     *
     * @param  array Table to copy.
     * @param  length Length of the desired table.
     * @return Table of the same type as <code>array</code>, of length
     *         <code>length</code> and containing the data from
     *         <code>array</code>.
     */
    public static float[] resize(final float[] array, final int length) {
        return (float[]) doResize(array, length);
    }

    /**
     * Returns a new table which contains the same elements as
     * <code>array</code> but with the <code>length</code> specified.
     * If the desired <code>length</code> is longer than the initial
     * length of the <code>array</code> table, the returned table will contain
     * all the elements of <code>array</code> as well as the elements
     * initialised to <code>null</code> at the end of the table. If, on the
     * contrary, the desired <code>length</code> is shorter than the initial
     * length of the <code>array</code> table, the table will be truncated
     * (that is to say the surplus <code>array</code> elements will be
     * forgotten). If the length of <code>array</code> is equal to
     * <code>length</code>, then <code>array</code> will be returned as it
     * stands.
     *
     * @param  array Table to copy.
     * @param  length Length of the desired table.
     * @return Table of the same type as <code>array</code>, of length
     *         <code>length</code> and containing the data from
     *         <code>array</code>.
     */
    public static long[] resize(final long[] array, final int length) {
        return (long[]) doResize(array, length);
    }

    /**
     * Returns a new table which contains the same elements as
     * <code>array</code> but with the <code>length</code> specified.
     * If the desired <code>length</code> is longer than the initial
     * length of the <code>array</code> table, the returned table will contain
     * all the elements of <code>array</code> as well as the elements
     * initialised to <code>null</code> at the end of the table. If, on the
     * contrary, the desired <code>length</code> is shorter than the initial
     * length of the <code>array</code> table, the table will be truncated
     * (that is to say the surplus <code>array</code> elements will be
     * forgotten). If the length of <code>array</code> is equal to
     * <code>length</code>, then <code>array</code> will be returned as it
     * stands.
     *
     * @param  array Table to copy.
     * @param  length Length of the desired table.
     * @return Table of the same type as <code>array</code>, of length
     *         <code>length</code> and containing the data from
     *         <code>array</code>.
     */
    public static int[] resize(final int[] array, final int length) {
        return (int[]) doResize(array, length);
    }

   /**
     * Returns a new table which contains the same elements as
     * <code>array</code> but with the <code>length</code> specified.
     * If the desired <code>length</code> is longer than the initial
     * length of the <code>array</code> table, the returned table will contain
     * all the elements of <code>array</code> as well as the elements
     * initialised to <code>null</code> at the end of the table. If, on the
     * contrary, the desired <code>length</code> is shorter than the initial
     * length of the <code>array</code> table, the table will be truncated
     * (that is to say the surplus <code>array</code> elements will be
     * forgotten). If the length of <code>array</code> is equal to
     * <code>length</code>, then <code>array</code> will be returned as it
     * stands.
     *
     * @param  array Table to copy.
     * @param  length Length of the desired table.
     * @return Table of the same type as <code>array</code>, of length
     *         <code>length</code> and containing the data from
     *         <code>array</code>.
     */
    public static short[] resize(final short[] array, final int length) {
        return (short[]) doResize(array, length);
    }

   /**
     * Returns a new table which contains the same elements as
     * <code>array</code> but with the <code>length</code> specified.
     * If the desired <code>length</code> is longer than the initial
     * length of the <code>array</code> table, the returned table will contain
     * all the elements of <code>array</code> as well as the elements
     * initialised to <code>null</code> at the end of the table. If, on the
     * contrary, the desired <code>length</code> is shorter than the initial
     * length of the <code>array</code> table, the table will be truncated
     * (that is to say the surplus <code>array</code> elements will be
     * forgotten). If the length of <code>array</code> is equal to
     * <code>length</code>, then <code>array</code> will be returned as it
     * stands.
     *
     * @param  array Table to copy.
     * @param  length Length of the desired table.
     * @return Table of the same type as <code>array</code>, of length
     *         <code>length</code> and containing the data from
     *         <code>array</code>.
     */
    public static byte[] resize(final byte[] array, final int length) {
        return (byte[]) doResize(array, length);
    }

   /**
     * Returns a new table which contains the same elements as
     * <code>array</code> but with the <code>length</code> specified.
     * If the desired <code>length</code> is longer than the initial
     * length of the <code>array</code> table, the returned table will contain
     * all the elements of <code>array</code> as well as the elements
     * initialised to <code>null</code> at the end of the table. If, on the
     * contrary, the desired <code>length</code> is shorter than the initial
     * length of the <code>array</code> table, the table will be truncated
     * (that is to say the surplus <code>array</code> elements will be
     * forgotten). If the length of <code>array</code> is equal to
     * <code>length</code>, then <code>array</code> will be returned as it
     * stands.
     *
     * @param  array Table to copy.
     * @param  length Length of the desired table.
     * @return Table of the same type as <code>array</code>, of length
     *         <code>length</code> and containing the data from
     *         <code>array</code>.
     */
    public static char[] resize(final char[] array, final int length) {
        return (char[]) doResize(array, length);
    }

    /**
     * Returns a new table which contains the same elements as
     * <code>array</code> but with the <code>length</code> specified.
     * If the desired <code>length</code> is longer than the initial
     * length of the <code>array</code> table, the returned table will contain
     * all the elements of <code>array</code> as well as the elements
     * initialised to <code>null</code> at the end of the table. If, on the
     * contrary, the desired <code>length</code> is shorter than the initial
     * length of the <code>array</code> table, the table will be truncated
     * (that is to say the surplus <code>array</code> elements will be
     * forgotten). If the length of <code>array</code> is equal to
     * <code>length</code>, then <code>array</code> will be returned as it
     * stands.
     *
     * @param  array Table to copy.
     * @param  length Length of the desired table.
     * @return Table of the same type as <code>array</code>, of length
     *         <code>length</code> and containing the data from
     *         <code>array</code>.
     */
    public static boolean[] resize(final boolean[] array, final int length) {
        return (boolean[]) doResize(array, length);
    }

    /**
     * Grabs elements from the middle of a table.
     *
     * @param array   Table from which to grab elements.
     * @param index   <code>array</code> index of the first element to grab.
     *                All subsequent elements of <code>array</code>
     *                can be moved forward.
     * @param length  Number of elements to grab.
     * @return        Table which contains the <code>array</code> data with the
     *                extracted elements.  This method can directly return 
     *                <code>dst</code>, but most often it returns a newly created
     *                table.
     */
    private static Object doRemove(final Object array, final int index, final int length) {
        if (length==0) {
            return array;
        }
        int array_length=Array.getLength(array);
        final Object newArray=Array.newInstance(array.getClass().getComponentType(), array_length-=length);
        System.arraycopy(array, 0,            newArray, 0,                  index);
        System.arraycopy(array, index+length, newArray, index, array_length-index);
        return newArray;
    }

    /**
     * Grabs elements from the middle of a table.
     *
     * @param array   Table from which to grab the elements.
     * @param index   <code>array</code> index of the first element to grab.
     *                All subsequent <code>array</code> elements can be moved forward.
     * @param length  Number of elements to grab.
     * @return        Table which contains the <code>array</code> data with the
     *                extracted elements.  This method can directly return 
     *                <code>dst</code>, but most often it returns a newly created
     *                table.
     */
    public static Object[] remove(final Object[] array, final int index, final int length) {
        return (Object[]) doRemove(array, index, length);
    }

   /**
     * Grabs elements from the middle of a table.
     *
     * @param array   Table from which to grab elements.
     * @param index   <code>array</code> index of the first element to grab.
     *                All subsequent elements of <code>array</code>
     *                can be moved forward.
     * @param length  Number of elements to grab.
     * @return        Table which contains the <code>array</code> data with the
     *                extracted elements.  This method can directly return 
     *                <code>dst</code>, but most often it returns a newly created
     *                table.
     */
    public static double[] remove(final double[] array, final int index, final int length) {
        return (double[]) doRemove(array, index, length);
    }

   /**
     * Grabs elements from the middle of a table.
     *
     * @param array   Table from which to grab elements.
     * @param index   <code>array</code> index of the first element to grab.
     *                All subsequent elements of <code>array</code>
     *                can be moved forward.
     * @param length  Number of elements to grab.
     * @return        Table which contains the <code>array</code> data with the
     *                extracted elements.  This method can directly return 
     *                <code>dst</code>, but most often it returns a newly created
     *                table.
     */
    public static float[] remove(final float[] array, final int index, final int length) {
        return (float[]) doRemove(array, index, length);
    }

  /**
     * Grabs elements from the middle of a table.
     *
     * @param array   Table from which to grab elements.
     * @param index   <code>array</code> index of the first element to grab.
     *                All subsequent elements of <code>array</code>
     *                can be moved forward.
     * @param length  Number of elements to grab.
     * @return        Table which contains the <code>array</code> data with the
     *                extracted elements.  This method can directly return 
     *                <code>dst</code>, but most often it returns a newly created
     *                table.
     */
    public static long[] remove(final long[] array, final int index, final int length) {
        return (long[]) doRemove(array, index, length);
    }

   /**
     * Grabs elements from the middle of a table.
     *
     * @param array   Table from which to grab elements.
     * @param index   <code>array</code> index of the first element to grab.
     *                All subsequent elements of <code>array</code>
     *                can be moved forward.
     * @param length  Number of elements to grab.
     * @return        Table which contains the <code>array</code> data with the
     *                extracted elements.  This method can directly return 
     *                <code>dst</code>, but most often it returns a newly created
     *                table.
     */
    public static int[] remove(final int[] array, final int index, final int length) {
        return (int[]) doRemove(array, index, length);
    }

    /**
     * Grabs elements from the middle of a table.
     *
     * @param array   Table from which to grab elements.
     * @param index   <code>array</code> index of the first element to grab.
     *                All subsequent elements of <code>array</code>
     *                can be moved forward.
     * @param length  Number of elements to grab.
     * @return        Table which contains the <code>array</code> data with the
     *                extracted elements.  This method can directly return 
     *                <code>dst</code>, but most often it returns a newly created
     *                table.
     */
    public static short[] remove(final short[] array, final int index, final int length) {
        return (short[]) doRemove(array, index, length);
    }

   /**
     * Grabs elements from the middle of a table.
     *
     * @param array   Table from which to grab elements.
     * @param index   <code>array</code> index of the first element to grab.
     *                All subsequent elements of <code>array</code>
     *                can be moved forward.
     * @param length  Number of elements to grab.
     * @return        Table which contains the <code>array</code> data with the
     *                extracted elements.  This method can directly return 
     *                <code>dst</code>, but most often it returns a newly created
     *                table.
     */
    public static byte[] remove(final byte[] array, final int index, final int length) {
        return (byte[]) doRemove(array, index, length);
    }

    /**
     * Grabs elements from the middle of a table.
     *
     * @param array   Table from which to grab elements.
     * @param index   <code>array</code> index of the first element to grab.
     *                All subsequent elements of <code>array</code>
     *                can be moved forward.
     * @param length  Number of elements to grab.
     * @return        Table which contains the <code>array</code> data with the
     *                extracted elements.  This method can directly return 
     *                <code>dst</code>, but most often it returns a newly created
     *                table.
     */
    public static char[] remove(final char[] array, final int index, final int length) {
        return (char[]) doRemove(array, index, length);
    }

   /**
     * Grabs elements from the middle of a table.
     *
     * @param array   Table from which to grab elements.
     * @param index   <code>array</code> index of the first element to grab.
     *                All subsequent elements of <code>array</code>
     *                can be moved forward.
     * @param length  Number of elements to grab.
     * @return        Table which contains the <code>array</code> data with the
     *                extracted elements.  This method can directly return 
     *                <code>dst</code>, but most often it returns a newly created
     *                table.
     */
    public static boolean[] remove(final boolean[] array, final int index, final int length) {
        return (boolean[]) doRemove(array, index, length);
    }

    /**
     * Inserts spaces into the middle of a table.  These "spaces" will be made
     * up of null elements.
     *
     * @param array   Table in which to insert spaces.
     * @param index   <code>array</code> index where spaces should be inserted.
     *                All <code>array</code> elements which have an index equal
     *                to or higher than <code>index</code> will be moved
     *                forward.
     * @param length  Number of spaces to insert.
     * @return        Table which contains the <code>array</code> data with the
     *                additional space. This method can directly return
     *                <code>dst</code>, but most often it returns a newly
     *                created table.
     */
    private static Object doInsert(final Object array, final int index, final int length) {
        if (length==0) {
            return array;
        }
        final int array_length=Array.getLength(array);
        final Object newArray=Array.newInstance(array.getClass().getComponentType(), array_length+length);
        System.arraycopy(array, 0,     newArray, 0,            index             );
        System.arraycopy(array, index, newArray, index+length, array_length-index);
        return newArray;
    }

     /**
     * Inserts spaces into the middle of a table.  These "spaces" will be made
     * up of null elements.
     *
     * @param array   Table in which to insert spaces.
     * @param index   <code>array</code> index where spaces should be inserted.
     *                All <code>array</code> elements which have an index equal
     *                to or higher than <code>index</code> will be moved
     *                forward.
     * @param length  Number of spaces to insert.
     * @return        Table which contains the <code>array</code> data with the
     *                additional space. This method can directly return
     *                <code>dst</code>, but most often it returns a newly
     *                created table.
     */
    public static Object[] insert(final Object[] array, final int index, final int length) {
        return (Object[]) doInsert(array, index, length);
    }

     /**
     * Inserts spaces into the middle of a table.  These "spaces" will be made
     * up of zeros.
     *
     * @param array   Table in which to insert spaces.
     * @param index   <code>array</code> index where spaces should be inserted.
     *                All <code>array</code> elements which have an index equal
     *                to or higher than <code>index</code> will be moved
     *                forward.
     * @param length  Number of spaces to insert.
     * @return        Table which contains the <code>array</code> data with the
     *                additional space. This method can directly return
     *                <code>dst</code>, but most often it returns a newly
     *                created table.
     */
    public static double[] insert(final double[] array, final int index, final int length) {
        return (double[]) doInsert(array, index, length);
    }

     /**
     * Inserts spaces into the middle of a table.  These "spaces" will be made
     * up of zeros.
     *
     * @param array   Table in which to insert spaces.
     * @param index   <code>array</code> index where spaces should be inserted.
     *                All <code>array</code> elements which have an index equal
     *                to or higher than <code>index</code> will be moved
     *                forward.
     * @param length  Number of spaces to insert.
     * @return        Table which contains the <code>array</code> data with the
     *                additional space. This method can directly return
     *                <code>dst</code>, but most often it returns a newly
     *                created table.
     */
    public static float[] insert(final float[] array, final int index, final int length) {
        return (float[]) doInsert(array, index, length);
    }

     /**
     * Inserts spaces into the middle of a table.  These "spaces" will be made
     * up of zeros.
     *
     * @param array   Table in which to insert spaces.
     * @param index   <code>array</code> index where spaces should be inserted.
     *                All <code>array</code> elements which have an index equal
     *                to or higher than <code>index</code> will be moved
     *                forward.
     * @param length  Number of spaces to insert.
     * @return        Table which contains the <code>array</code> data with the
     *                additional space. This method can directly return
     *                <code>dst</code>, but most often it returns a newly
     *                created table.
     */
    public static long[] insert(final long[] array, final int index, final int length) {
        return (long[]) doInsert(array, index, length);
    }

    /**
     * Inserts spaces into the middle of a table.  These "spaces" will be made
     * up of zeros.
     *
     * @param array   Table in which to insert spaces.
     * @param index   <code>array</code> index where spaces should be inserted.
     *                All <code>array</code> elements which have an index equal
     *                to or higher than <code>index</code> will be moved
     *                forward.
     * @param length  Number of spaces to insert.
     * @return        Table which contains the <code>array</code> data with the
     *                additional space. This method can directly return
     *                <code>dst</code>, but most often it returns a newly
     *                created table.
     */
    public static int[] insert(final int[] array, final int index, final int length) {
        return (int[]) doInsert(array, index, length);
    }

     /**
     * Inserts spaces into the middle of a table.  These "spaces" will be made
     * up of zeros.
     *
     * @param array   Table in which to insert spaces.
     * @param index   <code>array</code> index where spaces should be inserted.
     *                All <code>array</code> elements which have an index equal
     *                to or higher than <code>index</code> will be moved
     *                forward.
     * @param length  Number of spaces to insert.
     * @return        Table which contains the <code>array</code> data with the
     *                additional space. This method can directly return
     *                <code>dst</code>, but most often it returns a newly
     *                created table.
     */
    public static short[] insert(final short[] array, final int index, final int length) {
        return (short[]) doInsert(array, index, length);
    }

     /**
     * Inserts spaces into the middle of a table.  These "spaces" will be made
     * up of zeros.
     *
     * @param array   Table in which to insert spaces.
     * @param index   <code>array</code> index where spaces should be inserted.
     *                All <code>array</code> elements which have an index equal
     *                to or higher than <code>index</code> will be moved
     *                forward.
     * @param length  Number of spaces to insert.
     * @return        Table which contains the <code>array</code> data with the
     *                additional space. This method can directly return
     *                <code>dst</code>, but most often it returns a newly
     *                created table.
     */
    public static byte[] insert(final byte[] array, final int index, final int length) {
        return (byte[]) doInsert(array, index, length);
    }

     /**
     * Inserts spaces into the middle of a table.  These "spaces" will be made
     * up of zeros.
     *
     * @param array   Table in which to insert spaces.
     * @param index   <code>array</code> index where spaces should be inserted.
     *                All <code>array</code> elements which have an index equal
     *                to or higher than <code>index</code> will be moved
     *                forward.
     * @param length  Number of spaces to insert.
     * @return        Table which contains the <code>array</code> data with the
     *                additional space. This method can directly return
     *                <code>dst</code>, but most often it returns a newly
     *                created table.
     */
    public static char[] insert(final char[] array, final int index, final int length) {
        return (char[]) doInsert(array, index, length);
    }

     /**
     * Inserts spaces into the middle of a table.  These "spaces" will be made
     * up of <code>false</code>.
     *
     * @param array   Table in which to insert spaces.
     * @param index   <code>array</code> index where spaces should be inserted.
     *                All <code>array</code> elements which have an index equal
     *                to or higher than <code>index</code> will be moved
     *                forward.
     * @param length  Number of spaces to insert.
     * @return        Table which contains the <code>array</code> data with the
     *                additional space. This method can directly return
     *                <code>dst</code>, but most often it returns a newly
     *                created table.
     */
    public static boolean[] insert(final boolean[] array, final int index, final int length) {
        return (boolean[]) doInsert(array, index, length);
    }

    /**
     * Inserts a table slice into another table.  The <code>src</code> table
     * will be entirely or partially inserted into the <code>dst</code> table.
     *
     * @param src     Table to insert into <code>dst</code>.
     * @param src_pos Index of the first data item of <code>src</code> to
     *                insert into <code>dst</code>.
     * @param dst     Table in which to insert <code>src</code> data.
     * @param dst_pos <code>dst</code> index in which to insert
     *                <code>src</code> data. All elements of 
     *                <code>dst</code> whose index is equal to or greater than
     *                <code>dst_pos</code> will be moved forward.
     * @param length  Number of <code>src</code> data items to insert.
     * @return        Table which contains the combination of <code>src</code>
     *                and <code>dst</code>. This method can directly return 
     *                <code>dst</code>, but never <code>src</code>. It most
     *                often returns a newly created table.
     */
    private static Object doInsert(final Object src, final int src_pos, final Object dst, final int dst_pos, final int length) {
        if (length==0) {
            return dst;
        }
        final int dst_length=Array.getLength(dst);
        final Object newArray=Array.newInstance(dst.getClass().getComponentType(), dst_length+length);
        System.arraycopy(dst, 0,       newArray, 0,              dst_pos           );
        System.arraycopy(src, src_pos, newArray, dst_pos,        length            );
        System.arraycopy(dst, dst_pos, newArray, dst_pos+length, dst_length-dst_pos);
        return newArray;
    }

    /**
     * Inserts a table slice into another table.  The <code>src</code> table
     * will be entirely or partially inserted into the <code>dst</code> table.
     *
     * @param src     Tablea to insert into <code>dst</code>.
     * @param src_pos Index of the first data item of <code>src</code> to
     *                insert into <code>dst</code>.
     * @param dst     Table in which to insert <code>src</code> data.
     * @param dst_pos <code>dst</code> index in which to insert
     *                <code>src</code> data. All elements of 
     *                <code>dst</code> whose index is equal to or greater than
     *                <code>dst_pos</code> will be moved forward.
     * @param length  Number of <code>src</code> data items to insert.
     * @return        Table which contains the combination of <code>src</code>
     *                and <code>dst</code>. This method can directly return 
     *                <code>dst</code>, but never <code>src</code>. It most
     *                often returns a newly created table.
     */
    public static Object[] insert(final Object[] src, final int src_pos, final Object[] dst, final int dst_pos, final int length) {
        return (Object[]) doInsert(src, src_pos, dst, dst_pos, length);
    }

     /**
     * Inserts a table slice into another table.  The <code>src</code> table
     * will be entirely or partially inserted into the <code>dst</code> table.
     *
     * @param src     Tablea to insert into <code>dst</code>.
     * @param src_pos Index of the first data item of <code>src</code> to
     *                insert into <code>dst</code>.
     * @param dst     Table in which to insert <code>src</code> data.
     * @param dst_pos <code>dst</code> index in which to insert
     *                <code>src</code> data. All elements of 
     *                <code>dst</code> whose index is equal to or greater than
     *                <code>dst_pos</code> will be moved forward.
     * @param length  Number of <code>src</code> data items to insert.
     * @return        Table which contains the combination of <code>src</code>
     *                and <code>dst</code>. This method can directly return 
     *                <code>dst</code>, but never <code>src</code>. It most
     *                often returns a newly created table.
     */
    public static double[] insert(final double[] src, final int src_pos, final double[] dst, final int dst_pos, final int length) {
        return (double[]) doInsert(src, src_pos, dst, dst_pos, length);
    }

     /**
     * Inserts a table slice into another table.  The <code>src</code> table
     * will be entirely or partially inserted into the <code>dst</code> table.
     *
     * @param src     Tablea to insert into <code>dst</code>.
     * @param src_pos Index of the first data item of <code>src</code> to
     *                insert into <code>dst</code>.
     * @param dst     Table in which to insert <code>src</code> data.
     * @param dst_pos <code>dst</code> index in which to insert
     *                <code>src</code> data. All elements of 
     *                <code>dst</code> whose index is equal to or greater than
     *                <code>dst_pos</code> will be moved forward.
     * @param length  Number of <code>src</code> data items to insert.
     * @return        Table which contains the combination of <code>src</code>
     *                and <code>dst</code>. This method can directly return 
     *                <code>dst</code>, but never <code>src</code>. It most
     *                often returns a newly created table.
     */
    public static float[] insert(final float[] src, final int src_pos, final float[] dst, final int dst_pos, final int length) {
        return (float[]) doInsert(src, src_pos, dst, dst_pos, length);
    }

     /**
     * Inserts a table slice into another table.  The <code>src</code> table
     * will be entirely or partially inserted into the <code>dst</code> table.
     *
     * @param src     Tablea to insert into <code>dst</code>.
     * @param src_pos Index of the first data item of <code>src</code> to
     *                insert into <code>dst</code>.
     * @param dst     Table in which to insert <code>src</code> data.
     * @param dst_pos <code>dst</code> index in which to insert
     *                <code>src</code> data. All elements of 
     *                <code>dst</code> whose index is equal to or greater than
     *                <code>dst_pos</code> will be moved forward.
     * @param length  Number of <code>src</code> data items to insert.
     * @return        Table which contains the combination of <code>src</code>
     *                and <code>dst</code>. This method can directly return 
     *                <code>dst</code>, but never <code>src</code>. It most
     *                often returns a newly created table.
     */
    public static long[] insert(final long[] src, final int src_pos, final long[] dst, final int dst_pos, final int length) {
        return (long[]) doInsert(src, src_pos, dst, dst_pos, length);
    }

     /**
     * Inserts a table slice into another table.  The <code>src</code> table
     * will be entirely or partially inserted into the <code>dst</code> table.
     *
     * @param src     Tablea to insert into <code>dst</code>.
     * @param src_pos Index of the first data item of <code>src</code> to
     *                insert into <code>dst</code>.
     * @param dst     Table in which to insert <code>src</code> data.
     * @param dst_pos <code>dst</code> index in which to insert
     *                <code>src</code> data. All elements of 
     *                <code>dst</code> whose index is equal to or greater than
     *                <code>dst_pos</code> will be moved forward.
     * @param length  Number of <code>src</code> data items to insert.
     * @return        Table which contains the combination of <code>src</code>
     *                and <code>dst</code>. This method can directly return 
     *                <code>dst</code>, but never <code>src</code>. It most
     *                often returns a newly created table.
     */
    public static int[] insert(final int[] src, final int src_pos, final int[] dst, final int dst_pos, final int length) {
        return (int[]) doInsert(src, src_pos, dst, dst_pos, length);
    }

   /**
     * Inserts a table slice into another table.  The <code>src</code> table
     * will be entirely or partially inserted into the <code>dst</code> table.
     *
     * @param src     Tablea to insert into <code>dst</code>.
     * @param src_pos Index of the first data item of <code>src</code> to
     *                insert into <code>dst</code>.
     * @param dst     Table in which to insert <code>src</code> data.
     * @param dst_pos <code>dst</code> index in which to insert
     *                <code>src</code> data. All elements of 
     *                <code>dst</code> whose index is equal to or greater than
     *                <code>dst_pos</code> will be moved forward.
     * @param length  Number of <code>src</code> data items to insert.
     * @return        Table which contains the combination of <code>src</code>
     *                and <code>dst</code>. This method can directly return 
     *                <code>dst</code>, but never <code>src</code>. It most
     *                often returns a newly created table.
     */
    public static short[] insert(final short[] src, final int src_pos, final short[] dst, final int dst_pos, final int length) {
        return (short[]) doInsert(src, src_pos, dst, dst_pos, length);
    }

     /**
     * Inserts a table slice into another table.  The <code>src</code> table
     * will be entirely or partially inserted into the <code>dst</code> table.
     *
     * @param src     Tablea to insert into <code>dst</code>.
     * @param src_pos Index of the first data item of <code>src</code> to
     *                insert into <code>dst</code>.
     * @param dst     Table in which to insert <code>src</code> data.
     * @param dst_pos <code>dst</code> index in which to insert
     *                <code>src</code> data. All elements of 
     *                <code>dst</code> whose index is equal to or greater than
     *                <code>dst_pos</code> will be moved forward.
     * @param length  Number of <code>src</code> data items to insert.
     * @return        Table which contains the combination of <code>src</code>
     *                and <code>dst</code>. This method can directly return 
     *                <code>dst</code>, but never <code>src</code>. It most
     *                often returns a newly created table.
     */
    public static byte[] insert(final byte[] src, final int src_pos, final byte[] dst, final int dst_pos, final int length) {
        return (byte[]) doInsert(src, src_pos, dst, dst_pos, length);
    }

    /**
     * Inserts a table slice into another table.  The <code>src</code> table
     * will be entirely or partially inserted into the <code>dst</code> table.
     *
     * @param src     Tablea to insert into <code>dst</code>.
     * @param src_pos Index of the first data item of <code>src</code> to
     *                insert into <code>dst</code>.
     * @param dst     Table in which to insert <code>src</code> data.
     * @param dst_pos <code>dst</code> index in which to insert
     *                <code>src</code> data. All elements of 
     *                <code>dst</code> whose index is equal to or greater than
     *                <code>dst_pos</code> will be moved forward.
     * @param length  Number of <code>src</code> data items to insert.
     * @return        Table which contains the combination of <code>src</code>
     *                and <code>dst</code>. This method can directly return 
     *                <code>dst</code>, but never <code>src</code>. It most
     *                often returns a newly created table.
     */
    public static char[] insert(final char[] src, final int src_pos, final char[] dst, final int dst_pos, final int length) {
        return (char[]) doInsert(src, src_pos, dst, dst_pos, length);
    }

     /**
     * Inserts a table slice into another table.  The <code>src</code> table
     * will be entirely or partially inserted into the <code>dst</code> table.
     *
     * @param src     Tablea to insert into <code>dst</code>.
     * @param src_pos Index of the first data item of <code>src</code> to
     *                insert into <code>dst</code>.
     * @param dst     Table in which to insert <code>src</code> data.
     * @param dst_pos <code>dst</code> index in which to insert
     *                <code>src</code> data. All elements of 
     *                <code>dst</code> whose index is equal to or greater than
     *                <code>dst_pos</code> will be moved forward.
     * @param length  Number of <code>src</code> data items to insert.
     * @return        Table which contains the combination of <code>src</code>
     *                and <code>dst</code>. This method can directly return 
     *                <code>dst</code>, but never <code>src</code>. It most
     *                often returns a newly created table.
     */
    public static boolean[] insert(final boolean[] src, final int src_pos, final boolean[] dst, final int dst_pos, final int length) {
        return (boolean[]) doInsert(src, src_pos, dst, dst_pos, length);
    }

    /**
     * Returns <code>true</code> if all elements in the specified array are in increasing order.
     * This method is usefull in assertions.
     */
    public static boolean isSorted(final char[] array) {
        for (int i=1; i<array.length; i++)
            if (array[i] < array[i-1])
                return false;
        return true;
    }

    /**
     * Returns <code>true</code> if all elements in the specified array are in increasing order.
     * This method is usefull in assertions.
     */
    public static boolean isSorted(final byte[] array) {
        for (int i=1; i<array.length; i++)
            if (array[i] < array[i-1])
                return false;
        return true;
    }

    /**
     * Returns <code>true</code> if all elements in the specified array are in increasing order.
     * This method is usefull in assertions.
     */
    public static boolean isSorted(final short[] array) {
        for (int i=1; i<array.length; i++)
            if (array[i] < array[i-1])
                return false;
        return true;
    }

    /**
     * Returns <code>true</code> if all elements in the specified array are in increasing order.
     * This method is usefull in assertions.
     */
    public static boolean isSorted(final int[] array) {
        for (int i=1; i<array.length; i++)
            if (array[i] < array[i-1])
                return false;
        return true;
    }

    /**
     * Returns <code>true</code> if all elements in the specified array are in increasing order.
     * This method is usefull in assertions.
     */
    public static boolean isSorted(final long[] array) {
        for (int i=1; i<array.length; i++)
            if (array[i] < array[i-1])
                return false;
        return true;
    }

    /**
     * Returns <code>true</code> if all elements in the specified array are in increasing order.
     * Since <code>NaN</code> values are unordered, they may appears anywhere in the array; they
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
     * Returns <code>true</code> if all elements in the specified array are in increasing order.
     * Since <code>NaN</code> values are unordered, they may appears anywhere in the array; they
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
     * Returns <code>true</code> if the specified array contains at least one
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
     * Returns <code>true</code> if the specified array contains at least one
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
     * Returns a string representation of an array of numbers. Current implementation
     * supports only primitive or subclasses of {@link Number}.
     *
     * @param  array The array to format.
     * @param  locale The locale for formatting.
     * @return The formatted array.
     *
     * @task TODO: The separator should be local-dependent.
     * @task REVISIT: Should we implements this functionality in LineFormat instead?
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
