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

import org.geotools.vpf.ifc.VPFRow;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * TableRow.java Created: Thu Jan 02 23:58:39 2003
 *
 * @author <a href="mailto:kobit@users.fs.net">Artur Hefczyc</a>
 * @version 1.0
 */
public class TableRow implements VPFRow {
    protected RowField[] fieldsArr = null;
    protected HashMap fieldsMap = null;

    /**
     * Creates a new TableRow object.
     *
     * @param fieldsArr DOCUMENT ME!
     * @param fieldsMap DOCUMENT ME!
     */
    public TableRow(
        RowField[] fieldsArr,
        HashMap fieldsMap
    ) {
        this.fieldsArr = fieldsArr;
        this.fieldsMap = fieldsMap;
    }

    // TableRow constructor

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String toString() {
        //     StringBuffer buff = new StringBuffer(" ["+getClass().getName());
        //     buff.append(" (fieldsMap=");
        //     if (fieldsMap == null)
        //     {
        //       buff.append("null)");
        //     } // end of if (columnDefs == null)
        //     else
        //     {
        //       Iterator it = fieldsMap.entrySet().iterator();
        //       while (it.hasNext())
        //       {
        //         Map.Entry entry = (Map.Entry)it.next();
        //         buff.append("\n"+
        //                     entry.getKey().toString()+"="+
        //                     entry.getValue().toString());
        //       } // end of while (it.hasNext())
        //       buff.append("\n)");
        //     } // end of if (columnDefs == null) else
        //     buff.append("]");
        StringBuffer buff = new StringBuffer();

        if (fieldsMap == null) {
            buff.append("null)");
        } // end of if (columnDefs == null)
        else {
            for (int i = 0; i < fieldsArr.length; i++) {
                buff.append(fieldsArr[i].toString() + ":");
            }

            // end of for (int i = 0; i < fieldsArr.length; i++)
            buff.append(";");
        }

        // end of if (columnDefs == null) else
        return buff.toString();
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int fieldsCount() {
        return fieldsArr.length;
    }

    /**
     * DOCUMENT ME!
     *
     * @param name DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public RowField get(String name) {
        return (RowField) fieldsMap.get(name);
    }

    /**
     * DOCUMENT ME!
     *
     * @param idx DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public RowField get(int idx) {
        return fieldsArr[idx];
    }

    /**
     * DOCUMENT ME!
     *
     * @param obj DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean equals(Object obj) {
        if ((obj == null) || !(obj instanceof TableRow)) {
            return false;
        }

        // end of if (row == null || !(row instanceof TableRow))
        TableRow row = (TableRow) obj;

        if ((fieldsArr == null) || (row.fieldsArr == null)) {
            return false;
        }

        // end of if (fieldsArr == null || row.fieldsArr == null)
        if (fieldsArr.length != row.fieldsArr.length) {
            return false;
        }

        // end of if (fieldsArr.length != row.fieldsArr.length)
        for (int i = 0; i < fieldsArr.length; i++) {
            if (!fieldsArr[i].equals(row.fieldsArr[i])) {
                return false;
            }

            // end of if (!fieldsArr[i].equals(row.fieldsArr[i]))
        }

        // end of for (int i = 0; i < fieldsArr.length; i++)
        return true;
    }
}


// TableRow
