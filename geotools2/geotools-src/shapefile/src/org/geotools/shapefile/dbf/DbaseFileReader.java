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

import java.util.*;
import java.util.zip.GZIPInputStream;


/**
 * A DbaseFileReader is used to read a dbase III format file.
 *
 */
public class DbaseFileReader extends DbaseFileStream {
    // the InputStreams being read
    protected EndianDataInputStream myDbfStream = null;
    private int myReadPosition = 0;

    /**
     * Create a reader on the named file
     */
    public DbaseFileReader(String inFilename) throws FileNotFoundException, IOException {
        super(inFilename);

        InputStream fin = null;

        // check to see if it is a compressed file
        if (myGzipExt != null) {
            FileInputStream fileIn = new FileInputStream(myFilename + myDbfExt + myGzipExt);
            fin = (InputStream) new GZIPInputStream(fileIn);
        } else {
            fin = (InputStream) new FileInputStream(myFilename + myDbfExt);
        }
        myDbfStream = new EndianDataInputStream(new BufferedInputStream(fin));

        // open the file and read the header
        readHeader();
    }

    /**
     * Reads the dbase header record
     */
    protected void readHeader() {
        // create the header
        myHeader = new DbaseFileHeader();

        try {
            // read the header
            myHeader.readHeader(myDbfStream);

            // reset field variables in case they need to be recalculated
            myFieldNames = null;

            // myFieldTypes = null;
        } catch (Exception ioe) {
            System.err.println("Couldn't read dbase header for " + myFilename + ": " + ioe);
        }

        // how many records remain
        myReadPosition = myHeader.getHeaderLength();
    }


    /** Skip the next record. */
    public void skip() throws IOException {
        boolean foundRecord = false;
        while (!foundRecord) {
            // retrieve the record length
            int tempRecordLength = myHeader.getRecordLength();

            // read the deleted flag
            char tempDeleted = (char) myDbfStream.readByteLE();

            // skip the next bytes
            myDbfStream.skipBytes(tempRecordLength - 1); //the 1 is for the deleted flag just read.

            // add the row if it is not deleted.
            if (tempDeleted != '*') {
                foundRecord = true;
            }
        }
    }


    /**
     * Read a single dbase record
     * @return the read shapefile record or null if there are no more records
     */
    public void read(Object[] attrs, int offset) throws IOException {
        // retrieve the record length
        int tempNumFields = myHeader.getNumFields();

        // read the record, and skip until we find a non deleted record
        byte[] buffer = new byte[myHeader.getRecordLength()];
        char tempDeleted;
        do {
            myDbfStream.readFully(buffer);
            tempDeleted = (char) buffer[0];
            if(tempDeleted == '*')
                System.out.println("Skipping deleted row");
        } while (tempDeleted == '*');
        
        // it's possible to create strings out of the byte array directly, but
        // benchmarking shows that it's much more efficient to create
        // an intermediate string buffer instead (String object spends some
        // amount of time to guess the encoding of the buffer, like that
        // this time is spent only once)
        StringBuffer sb = new StringBuffer(new String(buffer, "ISO-8859-1"));

        // read the Fields
        int cursor = 1;
        for (int j = 0; j < tempNumFields; j++) {
            // find the length of the field.
            int tempFieldLength = myHeader.getFieldLength(j);

            // find the field type
            char tempFieldType = myHeader.getFieldType(j);
            String temp = sb.substring(cursor, cursor + tempFieldLength);

            // read the data.
            Object tempObject = null;
            switch (tempFieldType) {
                case 'L': // logical data type, one character (T,t,F,f,Y,y,N,n)

                    char tempChar = (char) buffer[cursor];
                    if ((tempChar == 'T') || (tempChar == 't') || (tempChar == 'Y') ||
                            (tempChar == 'y')) {
                        tempObject = Boolean.TRUE;
                    } else {
                        tempObject = Boolean.FALSE;
                    }
                    break;

                case 'C': // character record.

                    // use an encoding to ensure all 8 bits are loaded
                    tempObject = new String(buffer, cursor, tempFieldLength, "ISO-8859-1").trim();
                    break;

                case 'D': // date data type.

                    String tempString = new String(buffer, cursor, 4).trim();
                    int tempYear = Integer.parseInt(tempString);
                    tempString = new String(buffer, cursor + 4, 2).trim();
                    int tempMonth = Integer.parseInt(tempString) - 1;
                    tempString = new String(buffer, cursor + 6, 2).trim();
                    int tempDay = Integer.parseInt(tempString);
                    Calendar c = Calendar.getInstance();
                    c.set(c.YEAR, tempYear);
                    c.set(c.MONTH, tempMonth);
                    c.set(c.DAY_OF_MONTH, tempDay);
                    tempObject = c.getTime();
                    break;

                case 'F': // floating point number
                    try {
                        // String doubleString = new String(buffer, cursor, tempFieldLength);
                        String doubleString = sb.substring(cursor, cursor + tempFieldLength).trim();
                        if(doubleString.length() > 0) 
                            tempObject = Double.valueOf(doubleString);
                        else
                            tempObject = new Double(0.0);
                    } catch (NumberFormatException e) {
                        tempObject = new Double(0.0);
                    }
                    break;

                case 'N': // numeric == double or integer
                    try {
                        // String numString = new String(buffer, cursor, tempFieldLength);
                        String numString = sb.substring(cursor, cursor + tempFieldLength).trim();
                        if (myHeader.getFieldDecimalCount(j) > 0) {
                            if(numString.length() > 0) {
                                tempObject = Double.valueOf(numString);
                            } else {
                                tempObject = new Double(0.0);
                            }
                        } else {
                            if(numString.length() > 0) {
                                tempObject = Integer.valueOf(numString);
                            } else {
                                tempObject = new Integer(0);
                            }
                        }
                    } catch (NumberFormatException e) {
                        if (myHeader.getFieldDecimalCount(j) > 0) {
                            tempObject = new Double(0.0);
                        } else {
                            tempObject = new Integer(0);
                        }
                    }
                    break;

                default:
                    tempObject = new String(buffer, cursor, tempFieldLength);
            }
            attrs[j + offset] = tempObject;
            cursor = cursor + tempFieldLength;
        }

    }
    
    public void close() throws IOException {
        myDbfStream.close();
    }
}
