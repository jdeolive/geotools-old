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
package org.opengis.cv;

// Input/output
import java.io.Serializable;


/**
 * Describes the color entry in a color table.
 *
 * @version 1.00
 * @since   1.00
 */
public class CV_PaletteInterpretation implements Serializable
{
    /**
     * Use <code>serialVersionUID</code> from first
     * draft for interoperability with GCS 1.00.
     */
    private static final long serialVersionUID = -1722525684694793520L;

    /**
     * The enum value.
     */
    public int value;

    /**
     * Gray Scale color palette.
     */
    public static final int CV_Gray = 0;

    /**
     * RGB (Red Green Blue) color palette.
     */
    public static final int CV_RGB = 1;

    /**
     * CYMK (Cyan Yellow Magenta blacK) color palette.
     */
    public static final int CV_CMYK = 2;

    /**
     * HSL (Hue Saturation Lightness) color palette.
     */
    public static final int CV_HLS = 3;

    /**
     * Construct a new enum value.
     */
    public CV_PaletteInterpretation(final int value)
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
            return ((CV_PaletteInterpretation) object).value == value;
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
        final StringBuffer buffer=new StringBuffer("CV_PaletteInterpretation");
        buffer.append('[');
        buffer.append(value);
        buffer.append(']');
        return buffer.toString();
    }
}
