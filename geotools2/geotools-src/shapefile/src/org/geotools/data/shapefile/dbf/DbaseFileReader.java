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
 *    This file is based on an origional contained in the GISToolkit project:
 *    http://gistoolkit.sourceforge.net/
 *
 */
package org.geotools.data.shapefile.dbf;


import java.io.*;
import java.util.*;
import java.nio.*;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import org.geotools.resources.NumberParser;


/** A DbaseFileReader is used to read a dbase III format file.
 * <br>
 * The general use of this class is:
 * <CODE><PRE>
 * FileChannel in = new FileInputStream("thefile.dbf").getChannel();
 * DbaseFileReader r = new DbaseFileReader( in )
 * Object[] fields = new Object[r.getHeader().getNumFields()];
 * while (r.hasNext()) {
 *    r.readEntry(fields);
 *    // do stuff
 * }
 * r.close();
 * </PRE></CODE>
 *
 * @author Ian Schneider
 */
public class DbaseFileReader {
  
  DbaseFileHeader header;
  ByteBuffer buffer;
  ReadableByteChannel channel;
  CharBuffer charBuffer;
  CharsetDecoder decoder;
  char[] fieldTypes;
  int[] fieldLengths;
  int cnt = 1;
  
  /** Creates a new instance of DBaseFileReader
   * @param channel The readable channel to use.
   * @throws IOException If an error occurs while initializing.
   */
  public DbaseFileReader(ReadableByteChannel channel) throws IOException {
    this.channel = channel;
    
    header = new DbaseFileHeader();
    header.readHeader(channel);
    
    init();
  }
  
  private int fill(ByteBuffer buffer,ReadableByteChannel channel) throws IOException {
    int r = buffer.remaining();
    // channel reads return -1 when EOF or other error
    // because they a non-blocking reads, 0 is a valid return value!!
    while (buffer.remaining() > 0 && r != -1) {
      r = channel.read(buffer);
    }
    if (r == -1)
      buffer.limit(buffer.position());
    return r;
  }
  
  private void bufferCheck() throws IOException {
    // remaining is less than record length
    // compact the remaining data and read again
    if (buffer.remaining() < header.getRecordLength()) {
      buffer.compact();
      fill(buffer,channel);
      buffer.position(0);
    }
  }
  
  private void init() throws IOException {
    // create the ByteBuffer
    // if we have a FileChannel, lets map it
    if (channel instanceof FileChannel) {
      FileChannel fc = (FileChannel) channel;
      buffer = fc.map(FileChannel.MapMode.READ_ONLY,0,fc.size());
      buffer.position((int) fc.position());
    } else {
      // Some other type of channel
      // start with a 8K buffer, should be more than adequate
      int size = 8 * 1024;
      // if for some reason its not, resize it
      size = header.getRecordLength() > size ? header.getRecordLength() : size;
      buffer = ByteBuffer.allocateDirect(size);
      // fill it and reset
      fill(buffer,channel);
      buffer.flip();
    }
    
    // The entire file is in little endian
    buffer.order(ByteOrder.LITTLE_ENDIAN);
    
    // Set up some buffers and lookups for efficiency
    fieldTypes = new char[header.getNumFields()];
    fieldLengths = new int[header.getNumFields()];
    for (int i = 0, ii = header.getNumFields(); i < ii; i++) {
      fieldTypes[i] = header.getFieldType(i);
      fieldLengths[i] = header.getFieldLength(i);
    }
    
    charBuffer = CharBuffer.allocate(header.getRecordLength() - 1);
    Charset chars = Charset.forName("ISO-8859-1");
    decoder = chars.newDecoder();
  }
  
  /** Get the header from this file. The header is read upon instantiation.
   * @return The header associated with this file or null if an error occurred.
   */  
  public DbaseFileHeader getHeader() {
    return header;
  }
  
  /** Clean up all resources associated with this reader.<B>Highly recomended.</B>
   * @throws IOException If an error occurs.
   */  
  public void close() throws IOException {
    if (channel.isOpen())
      channel.close();
    channel = null;
    header = null;
    buffer = null;
    charBuffer = null;
  }
  
  /** Query the reader as to whether there is another record.
   * @return True if more records exist, false otherwise.
   */  
  public boolean hasNext() {
    return cnt < header.getNumRecords() + 1;
  }
  
  /** Get the next record (entry). Will return a new array of values.
   * @throws IOException If an error occurs.
   * @return A new array of values.
   */  
  public Object[] readEntry() throws IOException {
    return readEntry(new Object[header.getNumFields()]);
  }
  
  /** Skip the next record.
   * @throws IOException If an error occurs.
   */
  public void skip() throws IOException {
    boolean foundRecord = false;
    while (!foundRecord) {
      // retrieve the record length
      int tempRecordLength = header.getRecordLength();
      
      bufferCheck();
      
      // read the deleted flag
      char tempDeleted = (char) buffer.get();
      
      // skip the next bytes
      buffer.position(buffer.position() + tempRecordLength - 1); //the 1 is for the deleted flag just read.
      
      // add the row if it is not deleted.
      if (tempDeleted != '*') {
        foundRecord = true;
      }
    }
  }
  
  /** Copy the next record into the array starting at offset.
   * @param entry Th array  to copy into.
   * @param offset The offset to start at
   * @throws IOException If an error occurs.
   * @return The same array passed in.
   */  
  public Object[] readEntry(Object[] entry,final int offset) throws IOException {
    if (entry.length - offset < header.getNumFields())
      throw new ArrayIndexOutOfBoundsException();
    
    // retrieve the record length
    final int numFields = header.getNumFields();
    
    boolean foundRecord = false;
    while (!foundRecord) {
      
      bufferCheck();
      
      // read the deleted flag
      char deleted = (char) buffer.get();
      if (deleted == '*') {
        continue;
      }
      
      charBuffer.position(0);
      buffer.limit(buffer.position() + header.getRecordLength() - 1);
      decoder.decode(buffer,charBuffer,true);
      buffer.limit(buffer.capacity());
      charBuffer.flip();
      
      // read the Fields
      int fieldOffset = 0;
      for (int j=0; j < numFields; j++){
        entry[j + offset] = readObject(fieldOffset,j);
        fieldOffset += fieldLengths[j];
      }
      
      foundRecord = true;
    }
    
    cnt++;
    
    return entry;
  }
  
  /** Copy the next entry into the array.
   * @param entry The array to copy into.
   * @throws IOException If an error occurs.
   * @return The same array passed in.
   */  
  public Object[] readEntry(Object[] entry) throws IOException {
    return readEntry(entry,0);
  }
  
  private Object readObject(final int fieldOffset,final int fieldNum) throws IOException {
    final char type = fieldTypes[fieldNum];
    final int fieldLen = fieldLengths[fieldNum];
    
    if(fieldLen > 0) {
    
    switch (type){
      // (L)logical (T,t,F,f,Y,y,N,n)
      case 'l':
      case 'L':
        switch (charBuffer.charAt(fieldOffset)) {
          case 't': case 'T': case 'Y': case 'y':
            return Boolean.TRUE;
          case 'f': case 'F': case 'N': case 'n':
            return Boolean.FALSE;
          default:
            throw new IOException("Unknown logical value : " + charBuffer.charAt(fieldOffset));
        }
        
        // (C)character (String)
      case 'c':
      case 'C':
        // oh, this seems like a lot of work to parse strings...but,
        // For some reason if zero characters ( (int) char == 0 ) are allowed
        // in these strings, they do not compare correctly later on down the 
        // line....
        int start = fieldOffset;
        int end = fieldOffset + fieldLen - 1;
        // trim off whitespace and 'zero' chars
        while (start < end) {
          char c = charBuffer.get(start);
          if (c== 0 || Character.isWhitespace(c))
            start++;
          else break;
        }
        while (end > start) {
          char c = charBuffer.get(end);
          if (c == 0 || Character.isWhitespace(c)) {
            end--;
          }
          else break;
        }
        // set up the new indexes for start and end
        charBuffer.position(start).limit(end + 1);
        String s = charBuffer.toString();
        // this resets the limit...
        charBuffer.clear();
        return s;
        
        // (D)date (Date)
      case 'd':
      case 'D':
        String tempString = charBuffer.subSequence(fieldOffset,fieldOffset + 4).toString();
        int tempYear = Integer.parseInt(tempString);
        tempString = charBuffer.subSequence(fieldOffset + 4,fieldOffset + 6).toString();
        int tempMonth = Integer.parseInt(tempString) - 1;
        tempString = charBuffer.subSequence(fieldOffset + 6,fieldOffset + 8).toString();
        int tempDay = Integer.parseInt(tempString);
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(cal.YEAR,tempYear);
        cal.set(cal.MONTH, tempMonth);
        cal.set(cal.DAY_OF_MONTH, tempDay);
        return cal.getTime();
        
        
        // (F)floating (Double)
      case 'n':
      case 'N':
        try {
          if (header.getFieldDecimalCount(fieldNum) == 0) {
            return new Integer(NumberParser.parseInt(charBuffer, fieldOffset, fieldOffset + fieldLen - 1));
          }
          // else will fall through to the floating point number
        } catch (NumberFormatException e) {
          // todo: use progresslistener
          // e.printStackTrace();
          return new Integer(0);
        }
      case 'f':
      case 'F': // floating point number
       try {
          

          return new Double(NumberParser.parseDouble(charBuffer,fieldOffset, fieldOffset + fieldLen - 1));
        } catch (NumberFormatException e) {
          // todo: use progresslistener
          // e.printStackTrace();
          return new Double(0.0);
        }
      default:
        throw new IOException("Invalid field type : " + type);
    }
    
    } else {
        return null;
    }
  }
  
  public static void main(String[] args) throws Exception {
    FileChannel channel = new FileInputStream(args[0]).getChannel();
    DbaseFileReader reader = new DbaseFileReader(channel);
    System.out.println(reader.getHeader());
    int r = 0;
    while (reader.hasNext())
      System.out.println(++r + "," + java.util.Arrays.asList(reader.readEntry()));
  }
  
}
