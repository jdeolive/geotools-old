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

// Input/output
import java.io.Serializable;


/**
 * Specifies the order of the bytes in multi-byte values.
 *
 * @version 1.00
 * @since   1.00
 */
public class GC_ByteInValuePacking implements Serializable
{
    /**
     * Use <code>serialVersionUID</code> from first
     * draft for interoperability with GCS 1.00.
     */
    private static final long serialVersionUID = -2382925740471469782L;

    /**
     * The enum value.
     */
    public int value;

    /**
     * Big Endian.
     */
    public static final int GC_wkbXDR = 0;

    /**
     * Little Endian.
     */
    public static final int GC_wkbNDR = 1;

    /**
     * Construct a new enum value.
     */
    public GC_ByteInValuePacking(final int value)
    {this.value = value;}

    /**
     * Returns the enum value.
     */
    public int hashCode()
    {return value;}

    /**
     * Compares the specified object with
     * this enum for equality.
     */
    public boolean equals(final Object object)
    {
        if (object!=null && getClass().equals(object.getClass()))
        {
            return ((GC_ByteInValuePacking) object).value == value;
        }
        else return false;
    }

    /**
     * Returns a string représentation of this enum.
     * The returned string is implementation dependent.
     * It is usually provided for debugging purposes only.
     */
    public String toString()
    {
        final StringBuffer buffer=new StringBuffer("GC_ByteInValuePacking");
        buffer.append('[');
        buffer.append(value);
        buffer.append(']');
        return buffer.toString();
    }
}
