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
import org.geotools.vpf.util.DataUtils;


/**
 * This class contains definition of VPF standard table column definition
 * according to specification found in: "Interface Standard for Vector Product
 * Format." Objects of this type are immutable. Created: Thu Jan 02 23:11:27
 * 2003
 *
 * @author <a href="mailto:kobit@users.fs.net">Artur Hefczyc</a>
 * @version 1.0
 */
public class TableColumnDef implements DataTypesDefinition {
    protected String name = null;
    protected char type = CHAR_NULL_VALUE;
    protected int elementsNumber = 0;
    protected char keyType = CHAR_NULL_VALUE;
    protected String colDesc = null;
    protected String valDescTableName = null;
    protected String thematicIdx = null;
    protected String narrTable = null;

    /**
     * Creates a new TableColumnDef object.
     *
     * @param name DOCUMENT ME!
     * @param type DOCUMENT ME!
     * @param elementsNumber DOCUMENT ME!
     * @param keyType DOCUMENT ME!
     * @param colDesc DOCUMENT ME!
     * @param valDescTableName DOCUMENT ME!
     * @param thematicIdx DOCUMENT ME!
     * @param narrTable DOCUMENT ME!
     */
    public TableColumnDef(
        String name,
        char type,
        int elementsNumber,
        char keyType,
        String colDesc,
        String valDescTableName,
        String thematicIdx,
        String narrTable
    ) {
        this.name = name;
        this.type = type;
        this.elementsNumber = elementsNumber;
        this.keyType = keyType;
        this.colDesc = colDesc;
        this.valDescTableName = valDescTableName;
        this.thematicIdx = thematicIdx;
        this.narrTable = narrTable;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String toString() {
        StringBuffer buff = new StringBuffer();
        StringBuffer sb = null;
        sb = new StringBuffer("" + name);
        sb.setLength(16);
        buff.append(sb);
        sb = new StringBuffer("" + type);
        sb.setLength(5);
        buff.append(sb);
        sb = new StringBuffer("" + elementsNumber);
        sb.setLength(5);
        buff.append(sb);
        sb = new StringBuffer("" + keyType);
        sb.setLength(4);
        buff.append(sb);
        sb = new StringBuffer("" + colDesc);
        sb.setLength(55);
        buff.append(sb);
        sb = new StringBuffer("" + valDescTableName);
        sb.setLength(5);
        buff.append(sb);
        sb = new StringBuffer("" + thematicIdx);
        sb.setLength(5);
        buff.append(sb);
        sb = new StringBuffer("" + narrTable);
        sb.setLength(5);
        buff.append(sb);

        return buff.toString();
    }

    /**
     * Gets the value of name
     *
     * @return the value of name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Gets the value of type
     *
     * @return the value of type
     */
    public char getType() {
        return this.type;
    }

    /**
     * Gets the value of elementsNumber
     *
     * @return the value of elementsNumber
     */
    public int getElementsNumber() {
        return this.elementsNumber;
    }

    /**
     * Gets the value of keyType
     *
     * @return the value of keyType
     */
    public char getKeyType() {
        return this.keyType;
    }

    /**
     * Gets the value of colDesc
     *
     * @return the value of colDesc
     */
    public String getColDesc() {
        return this.colDesc;
    }

    /**
     * Gets the value of valDescTableName
     *
     * @return the value of valDescTableName
     */
    public String getValDescTableName() {
        return this.valDescTableName;
    }

    /**
     * Gets the value of thematicIdx
     *
     * @return the value of thematicIdx
     */
    public String getThematicIdx() {
        return this.thematicIdx;
    }

    /**
     * Gets the value of narrTable
     *
     * @return the value of narrTable
     */
    public String getNarrTable() {
        return this.narrTable;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int getColumnSize() {
        return DataUtils.getDataTypeSize(type) * elementsNumber;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean isNumeric() {
        return DataUtils.isNumeric(type);
    }
}


// TableColumnDef
