/*
 *  The Java Conflation Suite (JCS) is a library of Java classes that
 *  can be used to build automated or semi-automated conflation solutions.
 *
 *  Copyright (C) 2002 Vivid Solutions
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *  For more information, contact:
 *
 *  Vivid Solutions
 *  Suite #1A
 *  2328 Government Street
 *  Victoria BC  V8T 5G5
 *  Canada
 *
 *  (250)385-6040
 *  jcs.vividsolutions.com
 */
package org.geotools.shapefile.endian;

import java.io.*;


/**
 *  A class that gives most of the functionality of DataOutputStream, but is endian aware.
 *  Uses a real java.io.DataOutputStream to actually do the writing.
 */
public class EndianDataOutputStream {
    private java.io.DataOutputStream outputStream;

    /** Creates new EndianDataOutputStream */
    public EndianDataOutputStream(java.io.OutputStream out) {
        outputStream = new DataOutputStream(out);
    }

    /** close stream**/
    public void close() throws IOException {
        outputStream.close();
    }

    /** write bytes */
    public void write(byte[] b, int off, int len) throws IOException {
        outputStream.write(b, off, len);
    }
    
    /** write bytes */
    public void write(byte[] b) throws IOException {
        outputStream.write(b);
    }


    /** flush stream**/
    public void flush() throws IOException {
        outputStream.flush();
    }

    /** write a byte in LittleEndian  - this is exactly the same as the BigEndian version since there's no endian in a single byte */
    public void writeByteLE(int b) throws IOException {
        outputStream.writeByte(b);
    }

    /** write a byte in BigEndian  - this is exactly the same as the LittleEndian version since there's no endian in a single byte */
    public void writeByteBE(int b) throws IOException {
        outputStream.writeByte(b);
    }

    /** write a set of bytes in LittleEndian  - this is exactly the same as the BigEndian version since there's no endian in a single byte */
    public void writeBytesLE(String s) throws IOException {
        outputStream.writeBytes(s);
    }

    /** write a set of bytes in BigEndian  - this is exactly the same as the LittleEndian version since there's no endian in a single byte */
    public void writeBytesBE(String s) throws IOException {
        outputStream.writeBytes(s);
    }

    /** write a 16bit short in BigEndian*/
    public void writeShortBE(int s) throws IOException {
        outputStream.writeShort(s);
    }

    /** write a 16bit short in LittleEndian*/
    public void writeShortLE(int s) throws IOException {
        outputStream.writeByte(s);
        outputStream.writeByte(s >> 8);
    }

    /** write a 32bit int in BigEndian*/
    public void writeIntBE(int i) throws IOException {
        outputStream.writeInt(i);
    }

    /** write a 32bit int in  LittleEndian*/
    public void writeIntLE(int i) throws IOException {
        outputStream.writeByte(i);
        outputStream.writeByte(i >> 8);
        outputStream.writeByte(i >> 16);
        outputStream.writeByte(i >> 24);
    }

    /** write a 64bit long in BigEndian*/
    public void writeLongBE(long l) throws IOException {
        outputStream.writeLong(l);
    }

    /** write a 64bit long in LittleEndian*/
    public void writeLongLE(long l) throws IOException {
        outputStream.writeByte((byte) (l));
        outputStream.writeByte((byte) (l >> 8));
        outputStream.writeByte((byte) (l >> 16));
        outputStream.writeByte((byte) (l >> 24));
        outputStream.writeByte((byte) (l >> 32));
        outputStream.writeByte((byte) (l >> 40));
        outputStream.writeByte((byte) (l >> 48));
        outputStream.writeByte((byte) (l >> 56));
    }

    /** write a 64bit double in BigEndian*/
    public void writeDoubleBE(double d) throws IOException {
        outputStream.writeDouble(d);
    }

    /** write a 64bit double in LittleEndian*/
    public void writeDoubleLE(double d) throws IOException {
        this.writeLongLE(Double.doubleToLongBits(d));
    }
    
}
