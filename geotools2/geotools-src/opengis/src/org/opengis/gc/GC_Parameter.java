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
 * The parameter required for a grid coverage processing operation.
 * This structure contains the parameter name (as defined from the
 * {@link GC_ParameterInfo} structure) and it s value.
 *
 * @version 1.00
 * @since   1.00
 */
public class GC_Parameter implements Serializable
{
    /**
     * Use <code>serialVersionUID</code> from first
     * draft for interoperability with GCS 1.00.
     */
    private static final long serialVersionUID = -2263108414899483691L;

    /**
     * Parameter name.
     */
    public String name;

    /**
     * The value for parameter.
     * The type {@link Object} can be any type including a {@link Number},
     * a {@link String} or an instance of an interface. For example, a grid
     * processor operation will typically require a parameter for the input
     * grid coverage. This parameter may have <code>"Source"</code> as the
     * parameter name and the instance of the grid coverage as the value.
     */
    public Object value;

    /**
     * Construct an empty Data type object. Caller
     * must initialize {@link #name} and {@link #value}
     */
    public GC_Parameter()
    {}

    /**
     * Construct a new Data Type object.
     */
    public GC_Parameter(final String name, final Object value)
    {
        this.name = name;
        this.value = value;
    }

    /**
     * Returns a hash value for this <code>Parameter</code>.
     * This value need not remain consistent between
     * different implementations of the same class.
     */
    public int hashCode()
    {
        int code = 0;
        if (name  != null) code ^= name.hashCode();
        if (value != null) code ^= value.hashCode();
        return code;
    }

    /**
     * Compares the specified object with
     * this parameter for equality.
     */
    public boolean equals(final Object object)
    {
        if (object!=null && getClass().equals(object.getClass()))
        {
            final  GC_Parameter that = (GC_Parameter) object;
            return GC_ParameterInfo.equals(name,  that.name) &&
                   GC_ParameterInfo.equals(value, that.value);
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
        final StringBuffer buffer=new StringBuffer("GC_Parameter");
        buffer.append('[');
        buffer.append(name);
        buffer.append(',');
        buffer.append(value);
        buffer.append(']');
        return buffer.toString();
    }
}
