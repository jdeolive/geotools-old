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

package org.geotools.vpf;

import org.geotools.vpf.ifc.DataTypesDefinition;


/**
 * RowField.java Created: Mon Jan 27 13:58:34 2003
 *
 * @author <a href="mailto:kobit@users.sourceforge.net">Artur Hefczyc</a>
 * @version $Id: RowField.java,v 1.7 2003/04/04 09:15:44 kobit Exp $
 */
public class RowField implements DataTypesDefinition {
    protected Object value = null;
    protected char type = CHAR_NULL_VALUE;

    /**
     * Creates a new RowField object.
     *
     * @param value DOCUMENT ME!
     * @param type DOCUMENT ME!
     */
    public RowField(
        Object value,
        char type
    ) {
        this.value = value;
        this.type = type;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String toString() {
        if (value != null) {
            //       return value.toString()+" ("+type+")";
            return value.toString();
        } // end of if (value != null)
        else {
            //      return "null ("+type+")";
            return "null";
        }

        // end of if (value != null) else
    }

    /**
     * DOCUMENT ME!
     *
     * @param obj DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean equals(Object obj) {
        if ((obj == null) || !(obj instanceof RowField)) {
            return false;
        }

        // end of if (row == null || !(row instanceof TableRow))
        return toString().equals(obj.toString());
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public char getType() {
        return type;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Object getValue() {
        return value;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getAsString() {
        if (value != null) {
            return value.toString();
        } // end of if (value != null)
        else {
            return null;
        }

        // end of else
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
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
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public short getAsShort() {
        return ((Number) value).shortValue();
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public float getAsFloat() {
        return ((Number) value).floatValue();
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public double getAsDouble() {
        return ((Number) value).doubleValue();
    }
}


// RowField
