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
package org.geotools.vpf.io;

import org.geotools.vpf.RowField;
import org.geotools.vpf.TableColumnDef;
import org.geotools.vpf.TableHeader;
import org.geotools.vpf.TableRow;
import org.geotools.vpf.exc.VPFHeaderFormatException;
import org.geotools.vpf.ifc.DataTypesDefinition;
import org.geotools.vpf.ifc.FileConstants;
import org.geotools.vpf.ifc.VPFHeader;
import org.geotools.vpf.ifc.VPFRow;
import org.geotools.vpf.util.DataUtils;
import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * Class <code>TableInputStream</code> implements
 *
 * @author <a href="mailto:kobit@users.sf.net">Artur Hefczyc</a>
 * @version $Id: TableInputStream.java,v 1.19 2003/05/19 20:59:38 kobit Exp $
 */
public class TableInputStream extends VPFInputStream implements FileConstants,
    DataTypesDefinition {
    /**
     * Variable constant <code>AHEAD_BUFFER_SIZE</code> keeps value of  number
     * of records to read ahead and keep in cache to improve further access to
     * data.
     */
    public static final int AHEAD_BUFFER_SIZE = 0;

    /**
     * Creates a new <code><code>TableInputStream</code></code> instance.
     *
     * @param file a <code><code>String</code></code> value
     *
     * @exception IOException if an error occurs
     */
    public TableInputStream(String file) throws IOException {
        super(file);
    }

    /**
     * Creates a new <code><code>TableInputStream</code></code> instance.
     *
     * @param file a <code><code>String</code></code> value
     * @param byteOrder a <code><code>char</code></code> value
     *
     * @exception IOException if an error occurs
     */
    public TableInputStream(String file, char byteOrder)
        throws IOException {
        super(file, byteOrder);
    }

    /**
     * Method <code>readHeader</code> is used to perform
     *
     * @return a <code><code>VPFHeader</code></code> value
     *
     * @exception VPFHeaderFormatException if an error occurs
     * @exception IOException if an error occurs
     */
    public VPFHeader readHeader()
        throws VPFHeaderFormatException, IOException {
        byte[] fourBytes = new byte[4];
        int res = input.read(fourBytes);
        char order = readChar();
        char ctrl = order;

        if (order == VPF_RECORD_SEPARATOR) {
            order = LITTLE_ENDIAN_ORDER;
        } else {
            ctrl = readChar();
        }

        if (order == LITTLE_ENDIAN_ORDER) {
            fourBytes = DataUtils.toBigEndian(fourBytes);
        }

        int length = DataUtils.decodeInt(fourBytes);

        if (ctrl != VPF_RECORD_SEPARATOR) {
            throw new VPFHeaderFormatException(
                "Header format does not fit VPF file definition.");
        }

        String description = readString("" + VPF_RECORD_SEPARATOR);
        String narrativeTable = readString("" + VPF_RECORD_SEPARATOR);
        ArrayList colDefs = new ArrayList();
        TableColumnDef colDef = readColumnDef();

        while (colDef != null) {
            //             System.out.println(colDef.toString());
            colDefs.add(colDef);
            ctrl = readChar();

            if (ctrl != VPF_FIELD_SEPARATOR) {
                throw new VPFHeaderFormatException(
                    "Header format does not fit VPF file definition.");
            }

            colDef = readColumnDef();
        }

        if (colDefs.size() == 0) {
            colDefs = null;
        }

        return new TableHeader(length, order, description, narrativeTable,
            colDefs);
    }

    /**
     * Describe <code>readColumnDef</code> method here.
     *
     * @return a <code>TableColumnDef</code> value
     *
     * @exception VPFHeaderFormatException if an error occurs
     * @exception IOException if an error occurs
     * @exception NumberFormatException if an error occurs
     */
    private TableColumnDef readColumnDef()
        throws VPFHeaderFormatException, IOException, NumberFormatException {
        char ctrl = readChar();

        if (ctrl == VPF_RECORD_SEPARATOR) {
            return null;
        }

        String name = ctrl + readString("=");
        char type = readChar();
        ctrl = readChar();

        if (ctrl != VPF_ELEMENT_SEPARATOR) {
            throw new VPFHeaderFormatException(
                "Header format does not fit VPF file definition.");
        }

        String elemStr = readString("" + VPF_ELEMENT_SEPARATOR).trim();

        if (elemStr.equals("*")) {
            elemStr = "-1";
        }

        int elements = Integer.parseInt(elemStr);
        char key = readChar();
        ctrl = readChar();

        if (ctrl != VPF_ELEMENT_SEPARATOR) {
            throw new VPFHeaderFormatException(
                "Header format does not fit VPF file definition.");
        }

        String colDesc = readString("" + VPF_ELEMENT_SEPARATOR +
                VPF_FIELD_SEPARATOR);
        String descTableName = readString("" + VPF_ELEMENT_SEPARATOR +
                VPF_FIELD_SEPARATOR);
        String indexFile = readString("" + VPF_ELEMENT_SEPARATOR +
                VPF_FIELD_SEPARATOR);
        String narrTable = readString("" + VPF_ELEMENT_SEPARATOR +
                VPF_FIELD_SEPARATOR);

        return new TableColumnDef(name, type, elements, key, colDesc,
            descTableName, indexFile, narrTable);
    }

    /**
     * Method <code>readRow</code> is used to perform
     *
     * @return a <code><code>VPFRow</code></code> value
     *
     * @exception IOException if an error occurs
     */
    public VPFRow readRow() throws IOException {
        //    condeb("Current file position: "+input.getFilePointer());
        List rowsDef = ((TableHeader) header).getColumnDefs();
        RowField[] fieldsArr = new RowField[rowsDef.size()];
        HashMap fieldsMap = new HashMap();

        for (int i = 0; i < rowsDef.size(); i++) {
            TableColumnDef tcd = (TableColumnDef) rowsDef.get(i);

            //             condeb("Reading field: "+tcd.getName()+
            //                 ", columnSize="+tcd.getColumnSize());
            Object value = null;

            try {
                if (tcd.getColumnSize() < 0) {
                    value = readVariableSizeData(tcd.getType());
                } else {
                    value = readFixedSizeData(tcd.getType(),
                            tcd.getElementsNumber());
                }
            } catch (EOFException e) {
                return null;
            }

            RowField field = new RowField(value, tcd.getType());
            fieldsArr[i] = field;
            fieldsMap.put(tcd.getName(), field);
        }

        return new TableRow(fieldsArr, fieldsMap);
    }

    /**
     * Method <code>tableSize</code> is used to perform
     *
     * @return an <code><code>int</code></code> value
     */
    public int tableSize() {
        return -1;
    }

    /**
     * Method <code>main</code> is used to perform
     *
     * @param args a <code><code>String[]</code></code> value
     *
     * @exception IOException if an error occurs
     */
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.out.println("Put valid file name as parameter.");
            System.exit(1);
        }

        TableInputStream testInput = new TableInputStream(args[0]);
        TableHeader testHeader = (TableHeader) testInput.getHeader();
        System.out.println(testHeader.toString());

        List fieldDefs = testHeader.getColumnDefs();
        TableRow row = (TableRow) testInput.readRow();
        int counter = 0;

        while (row != null) {
            for (int i = 0; i < fieldDefs.size(); i++) {
                TableColumnDef tcd = (TableColumnDef) fieldDefs.get(i);
                System.out.println(tcd.getName() + "=" + row.get(i).toString());
            }

            row = (TableRow) testInput.readRow();
        }
    }
}
