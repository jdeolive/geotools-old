/*
 * OpenGIS® Grid Coverage Services Implementation Specification
 * Copyright (2001) OpenGIS consortium
 *
 * THIS COPYRIGHT NOTICE IS A TEMPORARY PATCH.   Version 1.00 of official
 * OpenGIS's interface files doesn't contain a copyright notice yet. This
 * file is a slightly modified version of official OpenGIS's interface.
 * Changes have been done in order to fix RMI problems and are documented
 * on the SEAGIS web site (seagis.sourceforge.net). THIS FILE WILL LIKELY
 * BE REPLACED BY NEXT VERSION OF OPENGIS SPECIFICATIONS.
 */
package org.opengis.gc;

// Miscellaneous
import java.util.Arrays;
import java.io.Serializable;


/**
 * Specifies the range of valid coordinates for each dimension of the coverage.
 *
 * @version 1.00
 * @since   1.00
 */
public class GC_GridRange implements Serializable
{
    /**
     * Use <code>serialVersionUID</code> from first
     * draft for interoperability with GCS 1.00.
     */
    private static final long serialVersionUID = 8876971007576355810L;

    /**
     * The valid minimum inclusive grid coordinate.
     * The sequence contains a minimum value for each dimension of the grid coverage.
     * The lowest valid grid coordinate is zero.
     */
    public int[] lo;

    /**
     * The valid maximum exclusive grid coordinate.
     * The sequence contains a maximum value for each dimension of the grid coverage.
     */
    public int[] hi;

    /**
     * Construct an empty Data type object. Caller
     * must initialize {@link #lo} and {@link #hi}
     */
    public GC_GridRange()
    {}

    /**
     * Construct a new Data Type object.
     */
    public GC_GridRange(final int[] lo, final int[] hi)
    {
        this.lo = lo;
        this.hi = hi;
    }

    /**
     * Returns a hash value for this <code>GridRange</code>.
     * This value need not remain consistent between
     * different implementations of the same class.
     */
    public int hashCode()
    {return hashCode(lo) + 37*hashCode(hi);}

    /**
     * Returns an hash code value for an array.
     */
    private static int hashCode(final int[] array)
    {
        int code = 0;
        if (array!=null)
            for (int i=array.length; --i>=0;)
                code = 37*code + array[i];
        return code;
    }

    /**
     * Compares the specified object with
     * this grid range for equality.
     */
    public boolean equals(final Object object)
    {
        if (object!=null && getClass().equals(object.getClass()))
        {
            final GC_GridRange that = (GC_GridRange) object;
            return Arrays.equals(this.lo, that.lo) &&
                   Arrays.equals(this.hi, that.hi); // Note: this is J2SE 1.2 API.
        }
        else return false;
    }

    /**
     * Write an array.
     */
    private static void toString(final StringBuffer buffer, final int[] array)
    {
        buffer.append('[');
        for (int i=0; i<array.length; i++)
        {
            if (i!=0) buffer.append(',');
            buffer.append(array[i]);
        }
        buffer.append(']');
    }

    /**
     * Returns a string représentation of this enum.
     * The returned string is implementation dependent.
     * It is usually provided for debugging purposes only.
     */
    public String toString()
    {
        final StringBuffer buffer=new StringBuffer("GC_GridRange");
        buffer.append('[');
        toString(buffer, lo);
        buffer.append(',');
        toString(buffer, hi);
        buffer.append(']');
        return buffer.toString();
    }
}
