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
 * Provides information for the parameters required for grid coverage processing
 * operations and grid exchange. This information includes such information as
 * the name of the parameter, parameter description, parameter type etc.
 *
 * @version 1.00
 * @since   1.00
 */
public class GC_ParameterInfo implements Serializable
{
    /**
     * Use <code>serialVersionUID</code> from first
     * draft for interoperability with GCS 1.00.
     */
    private static final long serialVersionUID = 8197671884000119230L;

    /**
     * Parameter name.
     */
    public String name;

    /**
     * Parameter description.
     * If no description, the value will be null.
     */
    public String description;

    /**
     * Parameter type.
     * The enumeration contains standard parameter types for integer, string,
     * floating-point numbers, objects, etc.
     */
    public GC_ParameterType type;

    /**
     * Default value for parameter.
     * The type {@link Object} can be any type including a {@link Number} or a
     * {@link String}. For example, a filtering operation could have a default
     * kernel size of 3. If there is no default value, defaultValue will be null.
     */
    public Object defaultValue;

    /**
     * Minimum parameter value.
     * For example, a filtering operation could have a minimum kernel size of 3.
     */
    public double minimumValue;

    /**
     * Maximum parameter value.
     * For example, a filtering operation could have a maximum kernel size of 9.
     */
    public double maximumValue;

    /**
     * Construct an empty Data type object. Caller
     * must initialize {@link #name}, {@link #description}, {@link #type},
     * {@link #defaultValue}, {@link #minimumValue} and {@link #maximumValue}.
     */
    public GC_ParameterInfo()
    {}

    /**
     * Returns a hash value for this <code>ParameterInfo</code>.
     * This value need not remain consistent between
     * different implementations of the same class.
     */
    public int hashCode()
    {
        final long lgcode = Double.doubleToLongBits(minimumValue) +
                         37*Double.doubleToLongBits(maximumValue);
        int code = (int) lgcode ^ (int) (lgcode >>> 32);
        if (name != null) code = code*37 + name.hashCode();
        if (type != null) code = code*37 + type.hashCode();
        return code;
    }

    /**
     * Compare two objects for equalities.
     * One or both objects may be null.
     */
    static boolean equals(final Object object1, final Object object2)
    {return (object1==object2) || (object1!=null && object1.equals(object2));}

    /**
     * Compares the specified object with
     * this parameter info for equality.
     */
    public boolean equals(final Object object)
    {
        if (object!=null && getClass().equals(object.getClass()))
        {
            final GC_ParameterInfo that = (GC_ParameterInfo) object;
            return equals(name,         that.name       ) &&
                   equals(description,  that.description) &&
                   equals(type,         that.type       ) &&
                   equals(defaultValue, that.defaultValue       ) &&
                   Double.doubleToLongBits(minimumValue) == Double.doubleToLongBits(that.minimumValue) &&
                   Double.doubleToLongBits(maximumValue) == Double.doubleToLongBits(that.maximumValue);
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
        final StringBuffer buffer=new StringBuffer("GC_ParameterInfo");
        buffer.append('[');
        buffer.append(name);
        buffer.append(',');
        buffer.append(description);
        buffer.append(',');
        buffer.append(type);
        buffer.append(',');
        buffer.append(defaultValue);
        buffer.append(',');
        buffer.append(minimumValue);
        buffer.append(',');
        buffer.append(maximumValue);
        buffer.append(']');
        return buffer.toString();
    }
}
