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

import org.geotools.vpf.Coordinate2DDouble;
import org.geotools.vpf.Coordinate2DFloat;
import org.geotools.vpf.Coordinate3DDouble;
import org.geotools.vpf.Coordinate3DFloat;
import org.geotools.vpf.TripletId;
import org.geotools.vpf.exc.VPFDataException;
import org.geotools.vpf.ifc.DataTypesDefinition;
import org.geotools.vpf.ifc.FileConstants;
import org.geotools.vpf.ifc.VPFHeader;
import org.geotools.vpf.ifc.VPFRow;
import org.geotools.vpf.util.DataUtils;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.LinkedList;
import java.util.List;


/**
 * VPFInputStream.java Created: Mon Feb 24 22:39:57 2003
 *
 * @author <a href="mailto:kobit@users.sourceforge.net">Artur Hefczyc</a>
 * @version $Id: VPFInputStream.java,v 1.8 2003/04/04 09:15:49 kobit Exp $
 */
public abstract class VPFInputStream implements FileConstants,
    DataTypesDefinition {
    protected List rowsReadAhead = new LinkedList();
    protected String streamFile = null;
    protected VPFInputStream variableIndex = null;
    protected RandomAccessFile input = null;
    protected char byteOrder = LITTLE_ENDIAN_ORDER;
    protected String accessMode = "r";
    protected VPFHeader header = null;

    /**
     * Creates a new VPFInputStream object.
     *
     * @param file DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public VPFInputStream(String file) throws IOException {
        this.streamFile = file;
        input = new RandomAccessFile(streamFile, accessMode);
        header = readHeader();

        //     condeb("("+streamFile+
        //            ") header.getRecordSize()="+header.getRecordSize());
        if (header.getRecordSize() < 0) {
            variableIndex =
                new VariableIndexInputStream(
                    getVariableIndexFileName(),
                    getByteOrder()
                );
        }

        // end of if (header.getRecordSize() == -1)
    }

    /**
     * Creates a new VPFInputStream object.
     *
     * @param file DOCUMENT ME!
     * @param byteOrder DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public VPFInputStream(
        String file,
        char byteOrder
    ) throws IOException {
        this.streamFile = file;
        this.byteOrder = byteOrder;
        input = new RandomAccessFile(streamFile, accessMode);
        header = readHeader();
    }

    // VariableIndexInputStream constructor

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public abstract VPFHeader readHeader() throws IOException;

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public abstract VPFRow readRow() throws IOException;

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public abstract int tableSize() throws IOException;

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getVariableIndexFileName() {
        if (streamFile.equals("fcs")) {
            return "fcz";
        } // end of if (streamFile.equals("fcs"))
        else {
            return streamFile.substring(0, streamFile.length() - 1) + "x";
        }

        // end of else
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
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

    protected void unread(long bytes) throws IOException {
        input.seek(input.getFilePointer() - bytes);
    }

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
        } // end of if (header.getRecordSize() == -1)
        else {
            seek(header.getLength() + ((pos - 1) * header.getRecordSize()));

            //       condeb("seek: "+(header.getLength()+(pos-1)*header.getRecordSize()));
        }

        // end of if (header.getRecordSize() == -1) else
    }

    /**
     * DOCUMENT ME!
     *
     * @param index DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public VPFRow readRow(int index) throws IOException {
        setPosition(index);

        return readRow();
    }

    /**
     * DOCUMENT ME!
     *
     * @param rows DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public int readRows(VPFRow[] rows) throws IOException {
        int counter = 0;
        VPFRow row = readRow();

        while ((row != null) && (counter < rows.length)) {
            rows[counter++] = row;
            row = readRow();
        }

        // end of while (row != null)
        return counter;
    }

    /**
     * DOCUMENT ME!
     *
     * @param rows DOCUMENT ME!
     * @param fromIndex DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public int readRows(
        VPFRow[] rows,
        int fromIndex
    ) throws IOException {
        int counter = 0;
        setPosition(fromIndex);

        VPFRow row = readRow();

        while ((row != null) && (counter < rows.length)) {
            rows[counter++] = row;
            row = readRow();
        }

        // end of while (row != null)
        return counter;
    }

    protected char readChar() throws IOException {
        return (char) input.read();
    }

    protected String readString(String terminators) throws IOException {
        StringBuffer text = new StringBuffer();
        char ctrl = readChar();

        if (terminators.indexOf(ctrl) != -1) {
            if (ctrl == VPF_FIELD_SEPARATOR) {
                unread(1);
            }

            // end of if (ctrl == VPF_RECORD_SEPARATOR)
            return null;
        }

        while (terminators.indexOf(ctrl) == -1) {
            text.append(ctrl);
            ctrl = readChar();
        }

        // end of while (terminators.indexOf(ctrl) != -1)
        if (text.toString().equals(STRING_NULL_VALUE)) {
            return null;
        } // end of if (text.equals("null"))
        else {
            return text.toString();
        }

        // end of if (text.equals("null")) else
    }

    protected Object readVariableSizeData(char dataType)
        throws IOException {
        int instances = readInteger();

        return readFixedSizeData(dataType, instances);
    }

    protected Object readFixedSizeData(
        char dataType,
        int instancesCount
    ) throws IOException {
        Object result = null;

        switch (dataType) {
        case DATA_TEXT:
        case DATA_LEVEL1_TEXT:
        case DATA_LEVEL2_TEXT:
        case DATA_LEVEL3_TEXT:

            byte[] dataBytes =
                new byte[instancesCount * DataUtils.getDataTypeSize(dataType)];
            input.read(dataBytes);

            if (
                DataUtils.isNumeric(dataType)
                    && (byteOrder == LITTLE_ENDIAN_ORDER)
            ) {
                dataBytes = DataUtils.toBigEndian(dataBytes);
            }

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

            byte tripletDef = (byte) input.read();
            int dataSize = TripletId.calculateDataSize(tripletDef);
            byte[] tripletData = new byte[dataSize + 1];
            tripletData[0] = tripletDef;

            if (dataSize > 0) {
                input.read(tripletData, 1, dataSize);
            }

            // end of if (dataSize > 0)
            result = new TripletId(tripletData);

            break;

        case DATA_2_COORD_F: {
            float[][] data = new float[instancesCount][2];

            for (int i = 0; i < instancesCount; i++) {
                data[i][0] = readFloat();
                data[i][1] = readFloat();
            }

            // end of for (int i = 0; i < instancesCount; i++)
            result = new Coordinate2DFloat(data);
        }

        break;

        case DATA_2_COORD_R: {
            double[][] data = new double[instancesCount][2];

            for (int i = 0; i < instancesCount; i++) {
                data[i][0] = readDouble();
                data[i][1] = readDouble();
            }

            // end of for (int i = 0; i < instancesCount; i++)
            result = new Coordinate2DDouble(data);
        }

        break;

        case DATA_3_COORD_F: {
            float[][] data = new float[instancesCount][3];

            for (int i = 0; i < instancesCount; i++) {
                data[i][0] = readFloat();
                data[i][1] = readFloat();
                data[i][2] = readFloat();
            }

            // end of for (int i = 0; i < instancesCount; i++)
            result = new Coordinate3DFloat(data);
        }

        break;

        case DATA_3_COORD_R: {
            double[][] data = new double[instancesCount][3];

            for (int i = 0; i < instancesCount; i++) {
                data[i][0] = readDouble();
                data[i][1] = readDouble();
                data[i][2] = readDouble();
            }

            // end of for (int i = 0; i < instancesCount; i++)
            result = new Coordinate3DDouble(data);
        }

        break;

        default:
            break;
        } // end of switch (dataType)

        return result;

        //     byte[] result = new byte[bytesCount];
        //     int size = input.read(result);
        //     if (size != bytesCount)
        //     {
        //       throw new VPFRowDataException("Insuffitient data in stream: is "+size+
        //                                     " should be: "+tcd.getColumnSize());
        //     } // end of if (size != tcd.getColumnSize())
        //     if (numeric && getByteOrder() == LITTLE_ENDIAN_ORDER)
        //     {
        //       result = DataUtils.toBigEndian(result);
        //     } // end of if (numeric)
        //     return result;
    }

    protected byte[] readNumber(int cnt) throws IOException {
        byte[] dataBytes = new byte[cnt];
        int res = input.read(dataBytes);

        if (res == cnt) {
            if (byteOrder == LITTLE_ENDIAN_ORDER) {
                dataBytes = DataUtils.toBigEndian(dataBytes);
            }

            // end of if (byteOrder == LITTLE_ENDIAN_ORDER)
            return dataBytes;
        } // end of if (res == cnt)
        else {
            throw new VPFDataException("Inssufficient bytes in input stream");
        }

        // end of if (res == cnt) else
    }

    protected short readShort() throws IOException {
        return DataUtils.decodeShort(readNumber(DATA_SHORT_INTEGER_LEN));
    }

    protected int readInteger() throws IOException {
        return DataUtils.decodeInt(readNumber(DATA_LONG_INTEGER_LEN));
    }

    protected float readFloat() throws IOException {
        return DataUtils.decodeFloat(readNumber(DATA_SHORT_FLOAT_LEN));
    }

    protected double readDouble() throws IOException {
        return DataUtils.decodeDouble(readNumber(DATA_LONG_FLOAT_LEN));
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int availableRows() {
        return (rowsReadAhead != null) ? rowsReadAhead.size() : 0;
    }

    /**
     * DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public void close() throws IOException {
        input.close();
        input = null;
    }

    protected void condeb(String msg) {
        System.out.println(msg);
    }
}


// VPFInputStream
