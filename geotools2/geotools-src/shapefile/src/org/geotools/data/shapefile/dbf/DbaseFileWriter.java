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
import java.nio.channels.*;
import java.nio.*;
import java.text.*;

import java.util.*;


/** A DbaseFileReader is used to read a dbase III format file.
 * The general use of this class is:
 * <CODE><PRE>
 * DbaseFileHeader header = ...
 * WritableFileChannel out = new FileOutputStream("thefile.dbf").getChannel();
 * DbaseFileWriter w = new DbaseFileWriter(header,out);
 * while ( moreRecords ) {
 *   w.write( getMyRecord() );
 * }
 * w.close();
 * </PRE></CODE>
 * You must supply the <CODE>moreRecords</CODE> and <CODE>getMyRecord()</CODE>
 * logic...
 * @author Ian Schneider
 */
public class DbaseFileWriter  {
  
  private DbaseFileHeader header;
  private DbaseFileWriter.FieldFormatter formatter = new DbaseFileWriter.FieldFormatter();
  private WritableByteChannel channel;
  private ByteBuffer buffer;
  private final Number NULL_NUMBER = new Integer(0);
  private final String NULL_STRING = "";
  private final Date NULL_DATE = new Date();
  
  /** Create a DbaseFileWriter using the specified header and writing to the given
   * channel.
   * @param header The DbaseFileHeader to write.
   * @param out The Channel to write to.
   * @throws IOException If errors occur while initializing.
   */
  public DbaseFileWriter(DbaseFileHeader header,WritableByteChannel out) throws IOException {
    header.writeHeader(out);
    this.header = header;
    this.channel = out;
    
    init();
  }
  
  private void init() throws IOException {
    buffer = ByteBuffer.allocateDirect(header.getRecordLength());
    
  }
  
  private void write() throws IOException {
    buffer.position(0);
    int r = buffer.remaining();
    while ( (r -= channel.write(buffer)) > 0) {
      ; // do nothing
    }
  }
  
  /** Write a single dbase record.
   * @param record The entries to write.
   * @throws IOException If IO error occurs.
   * @throws DbaseFileException If the entry doesn't comply to the header.
   */
  public void write(Object[] record) throws IOException, DbaseFileException {
    
    if (record.length != header.getNumFields()) {
      throw new DbaseFileException("Wrong number of fields " + record.length +
      " expected " +  header.getNumFields());
    }
    
    buffer.position(0);
    
    // put the 'not-deleted' marker
    buffer.put( (byte) ' ');
    
    for (int i = 0; i < header.getNumFields(); i++) {
      String fieldString = fieldString(record[i], i);
      buffer.put(fieldString.getBytes());
    }
    
    write();
  }
  
  private String fieldString(Object obj,final int col) {
    String o;
    final int fieldLen = header.getFieldLength(col);
    switch (header.getFieldType(col)) {
      case 'C':
      case 'c':
        o = formatter.getFieldString(
          fieldLen, 
          obj == null ? NULL_STRING : obj.toString()
        );
        break;
      case 'L':
      case 'l':
        o = (obj == null ? "F" : obj == Boolean.TRUE ? "T" : "F");
//        o = formatter.getFieldString(
//          fieldLen, 
//          o
//        );
        break;
      case 'M':
      case 'G':
        o = formatter.getFieldString(
          fieldLen, 
          obj == null ? NULL_STRING : obj.toString()
        );
        break;
      case 'N':
      case 'n':
        // int?
        if (header.getFieldDecimalCount(col) == 0) {
          o = formatter.getFieldString(
            fieldLen, 0, (Number) (obj == null ? NULL_NUMBER : obj)
          );
          break;
        }
      case 'F':
      case 'f':
        o = formatter.getFieldString(fieldLen,
        header.getFieldDecimalCount(col),
        (Number) (obj == null ? NULL_NUMBER : obj)
        );
        break;
      case 'D':
      case 'd':
        o = formatter.getFieldString((Date) (obj == null ? NULL_DATE : obj));
        break;
      default:
        throw new RuntimeException("Unknown type " + header.getFieldType(col));
    }
    
    return o;
  }
  
  /** Release resources associated with this writer.
   * <B>Highly recommended</B>
   * @throws IOException If errors occur.
   */
  public void close() throws IOException {
    buffer.position(0);
    buffer.put((byte) 0).position(0).limit(1);
    write();
    channel.close();
  }
  
  
  /** Utility for formatting Dbase fields. */
  public static class FieldFormatter {
    private StringBuffer buffer = new StringBuffer(255);
    private NumberFormat numFormat = NumberFormat.getNumberInstance(Locale.US);
    private Calendar calendar = Calendar.getInstance(Locale.US);
    private String emtpyString;
    private static final int MAXCHARS = 255;
    
    public FieldFormatter() {
      // Avoid grouping on number format
      numFormat.setGroupingUsed(false);
      
      // build a 255 white spaces string
      StringBuffer sb = new StringBuffer(MAXCHARS);
      sb.setLength(MAXCHARS);
      for(int i = 0; i < MAXCHARS; i++) {
        sb.setCharAt(i, ' ');
      }
      
      emtpyString = sb.toString();
    }
    
    public String getFieldString(int size, String s) {
      buffer.replace(0, size, emtpyString);
      buffer.setLength(size);
      
      if(s != null) {
        buffer.replace(0, size, s);
        if(s.length() <= size) {
          for(int i = s.length(); i < size; i++) {
            buffer.append(' ');
          }
        }
      }
      
      buffer.setLength(size);
      return buffer.toString();
    }
    
    public String getFieldString(Date d) {
      
      if(d != null) {
        buffer.delete(0, buffer.length());
        
        calendar.setTime(d);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;  // returns 0 based month?
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        
        if(year < 1000) {
          if(year >= 100) {
            buffer.append("0");
          } else if(year >= 10) {
            buffer.append("00");
          } else {
            buffer.append("000");
          }
        }
        buffer.append(year);
        
        if(month < 10) {
          buffer.append("0");
        }
        buffer.append(month);
        
        if(day < 10) {
          buffer.append("0");
        }
        buffer.append(day);
      } else {
        buffer.setLength(8);
        buffer.replace(0, 8, emtpyString);
      }
      
      buffer.setLength(8);
      return buffer.toString();
    }
    
    public String getFieldString(int size, int decimalPlaces, Number n) {
      buffer.delete(0, buffer.length());
      
      if(n != null) {
        numFormat.setMaximumFractionDigits(decimalPlaces);
        numFormat.setMinimumFractionDigits(decimalPlaces);
        numFormat.format(n, buffer, new FieldPosition(NumberFormat.INTEGER_FIELD));
      }
      
      int diff = size - buffer.length();
      if(diff > 0) {
        for(int i = 0; i < diff; i++) {
          buffer.insert(0, ' ');
        }
      }
      
      // buffer.setLength(size);
      return buffer.toString();
    }
  }
  
  
}
