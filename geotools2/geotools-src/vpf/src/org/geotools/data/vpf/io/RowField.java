/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
 *
 */

package org.geotools.data.vpf.io;

import org.geotools.data.vpf.ifc.DataTypesDefinition;


/**
 * RowField.java Created: Mon Jan 27 13:58:34 2003
 *
 * @author <a href="mailto:kobit@users.sourceforge.net">Artur Hefczyc</a>
 * @version $Id: RowField.java,v 1.1 2003/06/15 11:42:07 kobit Exp $
 */
public class RowField implements DataTypesDefinition {
    /**
     * Describe variable <code>value</code> here.
     *
     */
    private Object value = null;
    /**
     * Describe variable <code>type</code> here.
     *
     */
    private char type = CHAR_NULL_VALUE;

    /**
     * Creates a new <code><code>RowField</code></code> instance.
     *
     * @param value an <code><code>Object</code></code> value
     * @param type a <code><code>char</code></code> value
     */
    public RowField(Object value, char type) {
        this.value = value;
        this.type = type;
    }

    /**
     * Method <code>toString</code> is used to perform 
     *
     * @return a <code><code>String</code></code> value
     */
    public String toString() {
        if (value != null) {
            //       return value.toString()+" ("+type+")";
            return value.toString();
        } else {
            //      return "null ("+type+")";
            return "null";
        }
    }

    /**
     * Method <code>equals</code> is used to perform 
     *
     * @param obj an <code><code>Object</code></code> value
     * @return a <code><code>boolean</code></code> value
     */
    public boolean equals(Object obj) {
        if ((obj == null) || !(obj instanceof RowField)) {
            return false;
        }
        return toString().equals(obj.toString());
    }

    public int hashCode() {
        return toString().hashCode();
    }

    /**
     * Method <code>getType</code> is used to perform 
     *
     * @return a <code><code>char</code></code> value
     */
    public char getType() {
        return type;
    }

    /**
     * Method <code>getValue</code> is used to perform 
     *
     * @return an <code><code>Object</code></code> value
     */
    public Object getValue() {
        return value;
    }

    /**
     * Method <code>getAsString</code> is used to perform 
     *
     * @return a <code><code>String</code></code> value
     */
    public String getAsString() {
        if (value != null) {
            return value.toString();
        } else {
            return null;
        }
    }

    /**
     * Method <code>getAsInt</code> is used to perform 
     *
     * @return an <code><code>int</code></code> value
     */
    public int getAsInt() {
        return ((Number) value).intValue();
    }

    /**
     * <code>getAsLong</code> returns <code>long</code> value but it is
     * <code>java int</code> value conerted to <code>java long</code>. VPF
     * standard support <code>long int</code> values, however it is type of 32
     * bits what is <code>java int</code> type indeed.
     *
     * @return a <code>long</code> value
     */
    public long getAsLong() {
        return ((Number) value).longValue();
    }

    /**
     * Method <code>getAsShort</code> is used to perform 
     *
     * @return a <code><code>short</code></code> value
     */
    public short getAsShort() {
        return ((Number) value).shortValue();
    }

    /**
     * Method <code>getAsFloat</code> is used to perform 
     *
     * @return a <code><code>float</code></code> value
     */
    public float getAsFloat() {
        return ((Number) value).floatValue();
    }

    /**
     * Method <code>getAsDouble</code> is used to perform 
     *
     * @return a <code><code>double</code></code> value
     */
    public double getAsDouble() {
        return ((Number) value).doubleValue();
    }
}
