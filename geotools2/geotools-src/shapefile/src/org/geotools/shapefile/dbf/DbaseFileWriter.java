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
package org.geotools.shapefile.dbf;

import org.geotools.shapefile.endian.*;

import java.io.*;
import java.text.*;

import java.util.*;
import java.util.zip.*;


/**
 * A DbaseFileReader is used to read a dbase III format file.
 *
 */
public class DbaseFileWriter extends DbaseFileStream {
    // the OutputStream being written
    protected EndianDataOutputStream myDbfStream = null;
    protected DbaseFileHeader myHeader;
    protected DbaseFileWriter.FieldFormatter formatter = new DbaseFileWriter.FieldFormatter();
    
    /**
     * Create a reader on the named file
     */
    public DbaseFileWriter(String outFilename, DbaseFileHeader header) throws FileNotFoundException, IOException {
        super(outFilename);
        
        OutputStream fout = null;
        
        // check to see if it is a compressed file
        if (myGzipExt != null) {
            FileOutputStream fileOut = new FileOutputStream(myFilename + myDbfExt + myGzipExt);
            fout = (OutputStream) new GZIPOutputStream(fileOut);
        } else {
            fout = (OutputStream) new FileOutputStream(myFilename + myDbfExt);
        }
        myDbfStream = new EndianDataOutputStream(new BufferedOutputStream(fout));
        
        // open the file and read the header
        writeHeader(header);
        myHeader = header;
    }
    
    /**
     * Reads the dbase header record
     */
    protected void writeHeader(DbaseFileHeader header) {
        try {
            // read the header
            header.writeHeader(myDbfStream);
            
            // reset field variables in case they need to be recalculated
            myFieldNames = null;
            
        } catch (Exception ioe) {
            System.err.println("Couldn't read dbase header for " + myFilename + ": " + ioe);
        }
    }
    
    
    /**
     * Write a single dbase record
     * @return the read shapefile record or null if there are no more records
     */
    public void write(Object[] record) throws IOException, DbaseFileException {
        if (record.length != myHeader.getNumFields()) {
            throw new DbaseFileException("Wrong number of fields " + record.length + 
                                          " expected " +  myHeader.getNumFields());
        }
        
        String s;
        
        myDbfStream.writeByteLE(' ');
        
        int len;
        
        StringBuffer tmps;
        
        for (int i = 0; i < myHeader.getNumFields(); i++) {
            len = myHeader.getFieldLength(i);
            Object o = record[i];
            String fieldString = null;
            
            switch (myHeader.getFieldType(i)) {
                case 'C':
                case 'c':
                    
                case 'L':
                    
                case 'M':
                    
                case 'G':
                    fieldString = formatter.getFieldString(myHeader.getFieldLength(i), (String) o);
                    break;
                    
                case 'N':
                case 'n':
                    // int?
                    if (myHeader.getFieldDecimalCount(i) == 0) {
                        fieldString = formatter.getFieldString(myHeader.getFieldLength(i), 0, (Number) o);
                        break;
                    }
                    
                case 'F':
                case 'f':
                    fieldString = formatter.getFieldString(myHeader.getFieldLength(i), 
                                                           myHeader.getFieldDecimalCount(i),
                                                           (Number) o);
                    break;
                   
                case 'D':
                case 'd':
                    fieldString = formatter.getFieldString((Date) o);
                    break;
                    
                default:
                    fieldString = formatter.getFieldString(myHeader.getFieldLength(i), o.toString());
                    
            } // switch
            myDbfStream.write(fieldString.getBytes());
        }
    }
    
    public void close() throws IOException {
        myDbfStream.writeByteLE(0x1a); // eof mark
        
        myDbfStream.close();
    }
    
    
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
                    for(int i = s.length(); i < size; i++)
                        buffer.append(' ');
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
                int month = calendar.get(Calendar.MONTH);
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
                for(int i = 0; i < diff; i++)
                    buffer.insert(0, ' ');
            }
            
            // buffer.setLength(size);
            return buffer.toString();
        }
    }
    
    
}
