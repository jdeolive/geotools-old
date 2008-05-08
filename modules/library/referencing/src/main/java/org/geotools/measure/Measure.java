/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2003-2006, GeoTools Project Managment Committee (PMC)
 *    (C) 2001, Institut de Recherche pour le Développement
 *    (C) 1999, Fisheries and Oceans Canada
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
package org.geotools.measure;

import javax.units.Unit;
import org.geotools.resources.Utilities;


/**
 * A scalar with an unit.
 *
 * @since 2.1
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public final class Measure extends Number {
    /**
     * For compatibility with different versions.
     */
    private static final long serialVersionUID = 6917234039472328164L;

    /**
     * The scalar value.
     */
    private final double value;

    /**
     * The unit.
     */
    private final Unit unit;

    /**
     * Creates a new measure with the specified value and unit.
     */
    public Measure(final double value, final Unit unit) {
        this.value = value;
        this.unit  = unit;
    }

    /** Returns the scalar value. */ public double doubleValue() {return (double) value;}
    /** Returns the scalar value. */ public float   floatValue() {return (float)  value;}
    /** Returns the scalar value. */ public long     longValue() {return (long)   value;}
    /** Returns the scalar value. */ public int       intValue() {return (int)    value;}
    /** Returns the scalar value. */ public short   shortValue() {return (short)  value;}
    /** Returns the scalar value. */ public byte     byteValue() {return (byte)   value;}

    /**
     * Returns the unit.
     */
    public Unit getUnit() {
        return unit;
    }

    /**
     * Returns a hash code value for this measure.
     */
    @Override
    public int hashCode() {
        long code = Double.doubleToLongBits(value);
        return (int)code ^ (int)(code >>> 32) ^ unit.hashCode();
    }

    /**
     * Compares this measure with the specified object for equality.
     */
    @Override
    public boolean equals(final Object object) {
        if (object instanceof Measure) {
            final Measure that = (Measure) object;
            return Double.doubleToLongBits(value) == Double.doubleToLongBits(that.value) &&
                   Utilities.equals(unit, that.unit);
        }
        return false;
    }

    /**
     * Returns a string representation of this measure.
     */
    @Override
    public String toString() {
        final StringBuilder buffer = new StringBuilder();
        buffer.append(value);
        buffer.append(' ');
        buffer.append(unit);
        return buffer.toString();
    }
}
