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

import org.geotools.data.vpf.exc.VPFDataException;
import org.geotools.data.vpf.ifc.DataTypesDefinition;
import org.geotools.data.vpf.ifc.FileConstants;
import org.geotools.data.vpf.ifc.VPFHeader;
import org.geotools.data.vpf.ifc.VPFRow;
import org.geotools.data.vpf.util.DataUtils;
import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.LinkedList;
import java.util.List;


/**
 * VPFInputStream.java Created: Mon Feb 24 22:39:57 2003
 *
 * @author <a href="mailto:kobit@users.sourceforge.net">Artur Hefczyc</a>
 * @version $Id: VPFInputStream.java,v 1.1 2003/06/15 11:42:07 kobit Exp $
 */
public abstract class VPFInputStream implements FileConstants,
    DataTypesDefinition {
    /**
     * Describe variable <code>input</code> here.
     *
     */
    protected RandomAccessFile input = null;
    /**
     * Describe variable <code>header</code> here.
     *
     */
    protected VPFHeader header = null;
    /**
     * Describe variable <code>streamFile</code> here.
     *
     */
    protected String streamFile = null;
    /**
     * Describe variable <code>rowsReadAhead</code> here.
     *
     */
    private List rowsReadAhead = new LinkedList();
    /**
     * Describe variable <code>variableIndex</code> here.
     *
     */
    private VPFInputStream variableIndex = null;
    /**
     * Describe variable <code>byteOrder</code> here.
     *
     */
    private char byteOrder = LITTLE_ENDIAN_ORDER;
    /**
     * Describe variable <code>accessMode</code> here.
     *
     */
    private String accessMode = "r";

    /**
     * Creates a new <code>VPFInputStream</code> instance.
     *
     * @param file a <code>String</code> value
     * @exception IOException if an error occurs
     */
    public VPFInputStream(String file) throws IOException {
        this.streamFile = file;
        input = new RandomAccessFile(streamFile, accessMode);
        header = readHeader();

//     condeb("("+streamFile+
//            ") header.getRecordSize()="+header.getRecordSize());
        if (header.getRecordSize() < 0) {
            variableIndex =
                new VariableIndexInputStream(getVariableIndexFileName(),
                                             getByteOrder());
        }

        // end of if (header.getRecordSize() == -1)
    }

    /**
     * Creates a new <code>VPFInputStream</code> instance.
     *
     * @param file a <code>String</code> value
     * @param byteOrder a <code>char</code> value
     * @exception IOException if an error occurs
     */
    public VPFInputStream(String file, char byteOrder)
        throws IOException {
        this.streamFile = file;
        this.byteOrder = byteOrder;
        input = new RandomAccessFile(streamFile, accessMode);
        header = readHeader();
    }

    // VariableIndexInputStream constructor

    /**
     * Describe <code>readHeader</code> method here.
     *
     * @return a <code>VPFHeader</code> value
     * @exception IOException if an error occurs
     */
    public abstract VPFHeader readHeader() throws IOException;

    /**
     * Describe <code>readRow</code> method here.
     *
     * @return a <code>VPFRow</code> value
     * @exception IOException if an error occurs
     */
    public abstract VPFRow readRow() throws IOException;

    /**
     * Describe <code>tableSize</code> method here.
     *
     * @return an <code>int</code> value
     * @exception IOException if an error occurs
     */
    public abstract int tableSize() throws IOException;

    /**
     * Describe <code>getVariableIndexFileName</code> method here.
     *
     * @return a <code>String</code> value
     */
    public String getVariableIndexFileName() {
        if (streamFile.equals("fcs")) {
            return "fcz";
        } else {
            return streamFile.substring(0, streamFile.length() - 1) + "x";
        }
    }

    /**
     * Describe <code>getHeader</code> method here.
     *
     * @return a <code>VPFHeader</code> value
     */
    public VPFHeader getHeader() {
        return header;
    }

    /**
     * Get the ByteOrder value.
     *
     * @return the ByteOrder value.
     */
    public char getByteOrder() {
        return byteOrder;
    }

    /**
     * Set the ByteOrder value.
     *
     * @param newByteOrder The new ByteOrder value.
     */
    public void setByteOrder(char newByteOrder) {
        this.byteOrder = newByteOrder;
    }

    /**
     * Describe <code>unread</code> method here.
     *
     * @param bytes a <code>long</code> value
     * @exception IOException if an error occurs
     */
    protected void unread(long bytes) throws IOException {
        input.seek(input.getFilePointer() - bytes);
    }

    /**
     * Describe <code>seek</code> method here.
     *
     * @param pos a <code>long</code> value
     * @exception IOException if an error occurs
     */
    protected void seek(long pos) throws IOException {
        input.seek(pos);
    }

    /**
     * DOCUMENT ME!
     *
     * @param pos DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public void setPosition(long pos) throws IOException {
//     condeb("setPosition: "+pos);
//     condeb("header.getRecordSize(): "+header.getRecordSize());
        if (header.getRecordSize() < 0) {
            VariableIndexRow varRow =
                (VariableIndexRow) variableIndex.readRow((int) pos);

//       condeb("Variable index info:\noffset="+varRow.getOffset()+
//              "\nsize="+varRow.getSize());
            seek(varRow.getOffset());

//       condeb("seek: "+varRow.getOffset());
        } else {
            seek(header.getLength() + ((pos - 1) * header.getRecordSize()));

//       condeb("seek: "+(header.getLength()+(pos-1)*header.getRecordSize()));
        }
    }

    /**
     * Method <code>readRow</code> is used to perform
     *
     * @param index an <code><code>int</code></code> value
     *
     * @return a <code><code>VPFRow</code></code> value
     *
     * @exception IOException if an error occurs
     */
    public VPFRow readRow(int index) throws IOException {
        setPosition(index);

        return readRow();
    }

    /**
     * Describe <code>readAllRows</code> method here.
     *
     * @return a <code>List</code> value
     * @exception IOException if an error occurs
     */
    public List readAllRows() throws IOException {
        LinkedList list = new LinkedList();
        VPFRow row = readRow();

        while (row != null) {
            list.add(row);
            row = readRow();
        }

        return list;
    }

    /**
     * Method <code>readRows</code> is used to perform
     *
     * @param rows a <code><code>VPFRow[]</code></code> value
     *
     * @return an <code><code>int</code></code> value
     *
     * @exception IOException if an error occurs
     */
    public int readRows(VPFRow[] rows) throws IOException {
        int counter = 0;
        VPFRow row = readRow();

        while ((row != null) && (counter < rows.length)) {
            rows[counter++] = row;
            row = readRow();
        }

        return counter;
    }

    /**
     * Describe <code>readRows</code> method here.
     *
     * @param rows a <code>VPFRow[]</code> value
     * @param fromIndex an <code>int</code> value
     * @return an <code>int</code> value
     * @exception IOException if an error occurs
     */
    public int readRows(VPFRow[] rows, int fromIndex) throws IOException {
        setPosition(fromIndex);
        return readRows(rows);
    }

    /**
     * Describe <code>readChar</code> method here.
     *
     * @return a <code>char</code> value
     * @exception IOException if an error occurs
     */
    protected char readChar() throws IOException {
        return (char) input.read();
    }

    /**
     * Describe <code>readString</code> method here.
     *
     * @param terminators a <code>String</code> value
     * @return a <code>String</code> value
     * @exception IOException if an error occurs
     */
    protected String readString(String terminators) throws IOException {
        StringBuffer text = new StringBuffer();
        char ctrl = readChar();

        if (terminators.indexOf(ctrl) != -1) {
            if (ctrl == VPF_FIELD_SEPARATOR) {
                unread(1);
            }

            return null;
        }

        while (terminators.indexOf(ctrl) == -1) {
            text.append(ctrl);
            ctrl = readChar();
        }

        if (text.toString().equals(STRING_NULL_VALUE)) {
            return null;
        } else {
            return text.toString();
        }
    }

    /**
     * Describe <code>readVariableSizeData</code> method here.
     *
     * @param dataType a <code>char</code> value
     * @return an <code>Object</code> value
     * @exception IOException if an error occurs
     */
    protected Object readVariableSizeData(char dataType)
        throws IOException {
        int instances = readInteger();

        return readFixedSizeData(dataType, instances);
    }

    /**
     * Describe <code>readFixedSizeData</code> method here.
     *
     * @param dataType a <code>char</code> value
     * @param instancesCount an <code>int</code> value
     * @return an <code>Object</code> value
     * @exception IOException if an error occurs
     */
    protected Object readFixedSizeData(char dataType, int instancesCount)
        throws IOException {
        Object result = null;

        switch (dataType) {
        case DATA_TEXT:
        case DATA_LEVEL1_TEXT:
        case DATA_LEVEL2_TEXT:
        case DATA_LEVEL3_TEXT:
            byte[] dataBytes =
                new byte[instancesCount * DataUtils.getDataTypeSize(dataType)];
            input.read(dataBytes);
            result = DataUtils.decodeData(dataBytes, dataType);
            break;

        case DATA_SHORT_FLOAT:
            result = new Float(readFloat());
            break;

        case DATA_LONG_FLOAT:
            result = new Double(readDouble());
            break;

        case DATA_SHORT_INTEGER:
            result = new Short(readShort());
            break;

        case DATA_LONG_INTEGER:
            result = new Integer(readInteger());
            break;

        case DATA_NULL_FIELD:
            result = "NULL";
            break;

        case DATA_TRIPLET_ID:
            result = readTripletId();
            break;

        case DATA_2_COORD_F:
            result = readCoordFloat(instancesCount, 2);
            break;

        case DATA_2_COORD_R:
            result = readCoordDouble(instancesCount, 2);
            break;

        case DATA_3_COORD_F:
            result = readCoordFloat(instancesCount, 3);
            break;

        case DATA_3_COORD_R:
            result = readCoordDouble(instancesCount, 3);
            break;

        default:
            break;
        } // end of switch (dataType)

        return result;
    }

    /**
     * Describe <code>readNumber</code> method here.
     *
     * @param cnt an <code>int</code> value
     * @return a <code>byte[]</code> value
     * @exception IOException if an error occurs
     */
    protected byte[] readNumber(int cnt) throws IOException {
        byte[] dataBytes = new byte[cnt];
        int res = input.read(dataBytes);

        if (res == cnt) {
            if (byteOrder == LITTLE_ENDIAN_ORDER) {
                dataBytes = DataUtils.toBigEndian(dataBytes);
            }

            return dataBytes;
        } else {
            if (res < 1) {
                throw new EOFException("No more bytes in input stream");
            } else {
                throw new VPFDataException(
                    "Inssufficient bytes in input stream : " + res);
            }
        }
    }

    /**
     * Describe <code>readShort</code> method here.
     *
     * @return a <code>short</code> value
     * @exception IOException if an error occurs
     */
    protected short readShort() throws IOException {
        return DataUtils.decodeShort(readNumber(DATA_SHORT_INTEGER_LEN));
    }

    /**
     * Describe <code>readInteger</code> method here.
     *
     * @return an <code>int</code> value
     * @exception IOException if an error occurs
     */
    protected int readInteger() throws IOException {
        return DataUtils.decodeInt(readNumber(DATA_LONG_INTEGER_LEN));
    }

    /**
     * Describe <code>readFloat</code> method here.
     *
     * @return a <code>float</code> value
     * @exception IOException if an error occurs
     */
    protected float readFloat() throws IOException {
        return DataUtils.decodeFloat(readNumber(DATA_SHORT_FLOAT_LEN));
    }

    /**
     * Describe <code>readDouble</code> method here.
     *
     * @return a <code>double</code> value
     * @exception IOException if an error occurs
     */
    protected double readDouble() throws IOException {
        return DataUtils.decodeDouble(readNumber(DATA_LONG_FLOAT_LEN));
    }

    protected TripletId readTripletId() throws IOException {
        byte tripletDef = (byte) input.read();
        int dataSize = TripletId.calculateDataSize(tripletDef);
        byte[] tripletData = new byte[dataSize + 1];
        tripletData[0] = tripletDef;

        if (dataSize > 0) {
            input.read(tripletData, 1, dataSize);
        }

        return new TripletId(tripletData);
    }

    protected CoordinateFloat readCoordFloat(int instancesCount, int dim)
        throws IOException {
        float[][] data = new float[instancesCount][dim];
        for (int i = 0; i < instancesCount; i++) {
            for (int j = 0; j < dim; j++) {
                data[i][j] = readFloat();
            }
        }
        return new CoordinateFloat(data);
    }

    protected CoordinateDouble readCoordDouble(int instancesCount, int dim)
        throws IOException {
        double[][] data = new double[instancesCount][dim];
        for (int i = 0; i < instancesCount; i++) {
            for (int j = 0; j < dim; j++) {
                data[i][j] = readDouble();
            }
        }
        return new CoordinateDouble(data);
    }

    /**
     * Describe <code>availableRows</code> method here.
     *
     * @return an <code>int</code> value
     */
    public int availableRows() {
        return (rowsReadAhead != null) ? rowsReadAhead.size() : 0;
    }

    /**
     * Describe <code>close</code> method here.
     *
     * @exception IOException if an error occurs
     */
    public void close() throws IOException {
        input.close();
        input = null;
    }

    /**
     * Describe <code>condeb</code> method here.
     *
     * @param msg a <code>String</code> value
     */
    protected void condeb(String msg) {
        System.out.println(msg);
    }
}


// VPFInputStream
