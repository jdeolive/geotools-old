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
 * Describes the packing of data values within grid coverages.
 * It includes the packing scheme of data values with less then 8 bits per value
 * within a byte, byte packing (Little Endian / Big Endian) for values with more
 * than 8 bits and the packing of the values within the dimensions.
 *
 * @version 1.00
 * @since   1.00
 */
public class GC_GridPacking implements Serializable
{
    /**
     * Use <code>serialVersionUID</code> from first
     * draft for interoperability with GCS 1.00.
     */
    private static final long serialVersionUID = 8835511412962017097L;

    /**
     * Order of bytes packed in values for sample dimensions with greater than 8 bits.
     */
    public GC_ByteInValuePacking byteInValuePacking;

    /**
     * Order of values packed in a byte for
     * {@link org.opengis.cv.CV_SampleDimensionType#CV_1BIT CV_1BIT},
     * {@link org.opengis.cv.CV_SampleDimensionType#CV_2BIT CV_2BIT} and
     * {@link org.opengis.cv.CV_SampleDimensionType#CV_4BIT CV_4BIT} data types.
     */
    public GC_ValueInBytePacking valueInBytePacking;

    /**
     * Gives the ordinate index for the band.
     * This index indicates how to form a band-specific coordinate from a grid coordinate
     * and a sample dimension number. This indicates the order in which the grid values
     * are stored in streamed data. This packing order is used when grid values are
     * retrieved using the <code>getPackedDataBlock</code> or set using
     * <code>setPackedDataBlock</code> operations on {@link GC_GridCoverage}.
     *
     *  bandPacking of
     *  <UL>
     *    <li>0 : the full band-specific coordinate is (b, n1, n2...)</li>
     *    <li>1 : the full band-specific coordinate is (n1, b, n2...)</li>
     *    <li>2 : the full band-specific coordinate is (n1, n2, b...)</li>
     *  </UL>
     *  Where
     *  <UL>
     *    <li>b is band</li>
     *    <li>n1 is dimension 1</li>
     *    <li>n2 is dimension 2</li>
     *  </UL>
     *  For 2 dimensional grids, band packing of 0 is referred to as band sequential,
     *  1 line interleaved and 2 pixel interleaved.
     */
    public int bandPacking;

    /**
     * Construct an empty Data type object. Caller
     * must initialize {@link #byteInValuePacking}, {@link #valueInBytePacking} and
     * {@link #bandPacking}.
     */
    public GC_GridPacking()
    {}

    /**
     * Construct a new Data Type object.
     */
    public GC_GridPacking(final GC_ByteInValuePacking byteInValuePacking, final GC_ValueInBytePacking valueInBytePacking, final int bandPacking)
    {
        this.byteInValuePacking = byteInValuePacking;
        this.valueInBytePacking = valueInBytePacking;
        this.bandPacking        = bandPacking;
    }

    /**
     * Returns a hash value for this <code>GridPacking</code>.
     * This value need not remain consistent between
     * different implementations of the same class.
     */
    public int hashCode()
    {
        int code = bandPacking;
        if (byteInValuePacking != null) code = code*37 + byteInValuePacking.hashCode();
        if (valueInBytePacking != null) code = code*37 + valueInBytePacking.hashCode();
        return code;
    }

    /**
     * Compares the specified object with
     * this grid packing for equality.
     */
    public boolean equals(final Object object)
    {
        if (object!=null && getClass().equals(object.getClass()))
        {
            final GC_GridPacking that = (GC_GridPacking) object;
            return bandPacking == that.bandPacking &&
                   GC_ParameterInfo.equals(byteInValuePacking, that.byteInValuePacking) &&
                   GC_ParameterInfo.equals(valueInBytePacking, that.valueInBytePacking);
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
        final StringBuffer buffer=new StringBuffer("GC_GridPacking");
        buffer.append('[');
        buffer.append(byteInValuePacking);
        buffer.append(',');
        buffer.append(valueInBytePacking);
        buffer.append(',');
        buffer.append(bandPacking);
        buffer.append(']');
        return buffer.toString();
    }
}
