/*
 *    Geotools - OpenSource mapping toolkit
 *    (C) 2002, Centre for Computational Geography
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
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */

package org.geotools.data.shapefile.shp;

import java.io.*;
import java.nio.*;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.FileChannel;
import java.util.*;

/** The general use of this class is:
 * <CODE><PRE>
 * FileChannel in = new FileInputStream("thefile.dbf").getChannel();
 * ShapefileReader r = new ShapefileReader( in )
 * while (r.hasNext()) {
 *    Geometry shape = (Geometry) r.nextRecord().shape()
 *    // do stuff
 * }
 * r.close();
 * </PRE></CODE>
 * You don't have to immediately ask for the shape from the record. The record
 * will contain the bounds of the shape and will only read the shape when the
 * shape() method is called.
 * This ShapefileReader.Record is the same object every time, so if you need data
 * from the Record, be sure to copy it.
 * @author jamesm
 * @author aaime
 * @author Ian Schneider
 */
public class ShapefileReader {
  
  /** The reader returns only one Record instance in its lifetime. The
   * record contains the current record information.
   */  
  public final class Record {
    int length;
    int number = 0;
    int offset;
    /** The minimum X value. */    
    public double minX;
    /** The minimum Y value. */    
    public double minY;
    /** The maximum X value. */    
    public double maxX;
    /** The maximum Y value. */    
    public double maxY;
    ShapeType type;
    int end = 0;
    boolean ready = false;
    /** Fetch the shape stored in this record. */    
    public Object shape() {
      return handler.read(buffer,type);
    }
    public int offset() {
      return offset;
    }
    /** A summary of the record. */    
    public String toString() {
      return "Record " + number + " length " + length + " bounds " + minX + "," + minY + " " + maxX + "," + maxY;
    }
  }
  
  private ShapeHandler handler;
  private ShapefileHeader header;
  private ReadableByteChannel channel;
  private ByteBuffer buffer;
  private ShapeType fileShapeType = ShapeType.UNDEFINED;
  private final Record record = new Record();
  
  /** Creates a new instance of ShapeFile.
   * @param channel The ReadableByteChannel this reader will use.
   * @param strict True to make the header parsing throw Exceptions if the version or magic number
   * are incorrect.
   * @throws IOException If problems arise.
   * @throws InvalidShapefileException If for some reason the file contains invalid records.
   */
  public ShapefileReader(ReadableByteChannel channel, boolean strict) throws IOException, InvalidShapefileException {
    this.channel = channel;
    init(strict);
  }
  
  /** Default constructor. Calls ShapefileReader(channel,true).
   * @param channel
   * @throws IOException
   * @throws InvalidShapefileException
   */  
  public ShapefileReader(ReadableByteChannel channel) throws IOException, InvalidShapefileException {
    this(channel,true);
  }
  
  // convenience to peak at a header
  /** A short cut for reading the header from the given channel.
   * @param channel The channel to read from.
   * @param strict True to make the header parsing throw Exceptions if the version or magic number
   * are incorrect.
   * @throws IOException If problems arise.
   * @return A ShapefileHeader object.
   */  
  public static ShapefileHeader readHeader(ReadableByteChannel channel,boolean strict) throws IOException {
    ByteBuffer buffer = ByteBuffer.allocateDirect(100);
    if ( fill(buffer, channel) == -1 )
      throw new EOFException("Premature end of header");
    buffer.flip();
    ShapefileHeader header = new ShapefileHeader();
    header.read(buffer, strict);
    return header;
  }
  
  // ensure the capacity of the buffer is of size by doubling the original
  // capacity until it is big enough
  // this may be naiive and result in out of MemoryError as implemented...
  private ByteBuffer ensureCapacity(ByteBuffer buffer,int size) {
    // This sucks if you accidentally pass is a MemoryMappedBuffer of size 80M
    // like I did while messing around, within moments I had 1 gig of swap...
    if (buffer.isReadOnly()) return buffer;
    
    int limit = buffer.limit();
    while (limit < size) {
      limit *= 2;
    }
    if (limit != buffer.limit()) {
      if (record.ready)
        buffer = ByteBuffer.allocateDirect(limit);
      else
        throw new IllegalArgumentException("next before hasNext");
    }
    return buffer;
  }
  
  // for filling a ReadableByteChannel
  private static int fill(ByteBuffer buffer,ReadableByteChannel channel) throws IOException {
    int r = buffer.remaining();
    int cnt = 0;
    // channel reads return -1 when EOF or other error
    // because they a non-blocking reads, 0 is a valid return value!!
    while (buffer.remaining() > 0 && r != -1) {
      r = channel.read(buffer);
    }
    if (r == -1)
      buffer.limit(buffer.position());
    return r;
  }
  
  private void init(boolean strict) throws IOException,InvalidShapefileException {
    header = readHeader(channel,strict);
    fileShapeType = header.getShapeType();
    handler = fileShapeType.getShapeHandler();
    
    if (handler == null)
      throw new IOException("Unsuported shape type:" + fileShapeType);
    
    if (channel instanceof FileChannel) {
      FileChannel fc = (FileChannel) channel;
      buffer = fc.map(FileChannel.MapMode.READ_ONLY,0,fc.size());
      buffer.position(100);
    } else {
      // start with 8K buffer
      buffer = ByteBuffer.allocateDirect(8 * 1024);
      fill(buffer,channel);
      buffer.flip();
    }
    
    // make sure the record end is set now...
    record.end = buffer.position();
  }
  
  /** Get the header. Its parsed in the constructor.
   * @return The header that is associated with this file.
   */  
  public ShapefileHeader getHeader() {
    return header;
  }
  
  // do important cleanup stuff.
  // Closes channel !
  /** Clean up any resources. Closes the channel.
   * @throws IOException If errors occur while closing the channel.
   */  
  public void close() throws IOException {
    if (channel.isOpen())
      channel.close();
    channel = null;
    header = null;
  }
  
  /** If there exists another record. Currently checks the stream for the presence of
   * 8 more bytes, the length of a record. If this is true and the record indicates
   * the next logical record number, there exists more records.
   * @throws IOException
   * @return True if has next record, false otherwise.
   */  
  public boolean hasNext() throws IOException {
    // ensure the proper position, regardless of read or handler behavior
    buffer.position(record.end);
    
    // are there less than eight bytes (record header size) ?
    if (buffer.remaining() < 8) {
      
      if (buffer.isReadOnly()) {
        return false;
      }
      // compact the buffer and fill
      buffer.compact();
      if (fill(buffer,channel) == -1) {
        return false;
      }
      // reset
      buffer.position(0);
    }
    
    buffer.mark();
    buffer.order(ByteOrder.BIG_ENDIAN);
    int number = buffer.getInt();
    if (number != record.number + 1) {
      return false;
    }
    buffer.reset();

    record.ready = true;
    // if not must be more stuff
    return true;
  }
  
  /** Fetch the next record information.
   * @throws IOException
   * @return The record instance associated with this reader.
   */  
  public Record nextRecord() throws IOException {
    
    // record header is big endian
    buffer.order(ByteOrder.BIG_ENDIAN);
    
    record.offset = buffer.position();
    
    // read shape record header
    int recordNumber = buffer.getInt();
    // silly ESRI say contentLength is in 2-byte words
    // and ByteByffer uses bytes.
    // track the record location
    int recordLength = buffer.getInt() * 2;

    // capacity is less than required for the record
    // copy the old into the newly allocated
    if (buffer.capacity() < recordLength) {
      ByteBuffer old = buffer;
      buffer = ensureCapacity(buffer, recordLength);
      buffer.put(old);
      fill(buffer,channel);
      buffer.position(0);
    } else 
    // remaining is less than record length
    // compact the remaining data and read again
    if (buffer.remaining() < recordLength) {
      buffer.compact();
      fill(buffer,channel);
      buffer.position(0);
    }
    
    // shape record is all little endian
    buffer.order(ByteOrder.LITTLE_ENDIAN);
    
    // read the type, handlers don't need it
    ShapeType recordType = ShapeType.forID(buffer.getInt());
    
    // this usually happens if the handler logic is bunk,
    // but bad files could exist as well...
    if (recordType != ShapeType.NULL && recordType != fileShapeType)
      throw new IllegalStateException("ShapeType changed illegally from " + fileShapeType + " to " + recordType);

    // peek at bounds, then reset for handler
    // many handler's may ignore bounds reading, but we don't want to
    // second guess them...
    buffer.mark();
    if (recordType.isMultiPoint()) {
      record.minX = buffer.getDouble();
      record.minY = buffer.getDouble();
      record.maxX = buffer.getDouble();
      record.maxY = buffer.getDouble();
    } 
    else if (recordType != recordType.NULL) {
      record.minX = record.maxX = buffer.getDouble();
      record.minY = record.minY = buffer.getDouble();
    }
    buffer.reset();

    // update all the record info.
    record.length = recordLength;
    record.type = recordType;
    record.number = recordNumber;
    // remember, we read one int already...
    record.end = buffer.position() + recordLength - 4;
    record.ready = false;
    return record;
  }
  
  public Object shapeAt(int offset) throws IOException {
    buffer.position(offset);
    record.ready = true;
    return nextRecord().shape();
  }
   
  public static void main(String[] args) throws Exception {
    FileChannel channel = new FileInputStream(args[0]).getChannel();
    ShapefileReader reader = new ShapefileReader(channel);
    while (reader.hasNext())
      System.out.println(reader.nextRecord().shape());
  }
  
}
