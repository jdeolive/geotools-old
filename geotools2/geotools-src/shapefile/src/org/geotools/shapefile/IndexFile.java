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

package org.geotools.shapefile;


import cmp.LEDataStream.LEDataInputStream;

import java.io.*;

import java.net.URL;

import java.util.Vector;

//Logging system
import java.util.logging.Logger;


/**
 * This class represnts an ESRI Shapefile index (.shx file).<p>
 * You construct it with a URL and can then get ask it for the
 * byte offset of a particular shape in the correpsonding Shapefile (.shp)<p>
 *
 * This class supports the index file as set out in:-<br>
 * <a href="http://www.esri.com/library/whitepapers/pdfs/shapefile.pdf">
 * <b>"ESRI(r) Shapefile - A Technical Description"</b><br>
 * <i>'An ESRI White Paper. May 1997'</i></a><p>
 *
 * @version $Id: IndexFile.java,v 1.1 2002/11/06 16:33:22 dledmonds Exp $
 * @author Darren Edmonds
 */
public class IndexFile {
  
  private static final Logger LOGGER = Logger.getLogger(
  "org.geotools.shapefile" );
  private final String ext = ".shx";
  
  private URL url = null;
  private FileHeader header = null;
  private Vector records = null;
  

  /**
   * Constructs a new IndexFile
   *
   * @param url URL pointing to the file
   */
  public IndexFile( URL url ) {
    this.url = url;
    
    String filename = url.getFile().toLowerCase();
    LOGGER.fine( "filename part of indexfile is " + filename );
    
    if ( !filename.endsWith( this.ext ) ) {
      LOGGER.warning( filename + " has incorrect extension, should be " + ext );
    }
    
    parse();
  }
  
  
  /**
   *
   */
  private void parse() {
    try {
      BufferedInputStream bis = new BufferedInputStream( url.openStream() );
      LEDataInputStream dis = new LEDataInputStream( bis );
      
      readHeader( dis );
      readRecords( dis );
      
      dis.close();
    }
    catch ( IOException e ) {
      LOGGER.fine( "unable to parse " + url.getFile().toLowerCase() );
    }
  }
  
  
  /**
   *
   */
  private void readHeader( LEDataInputStream dis ) throws IOException {
    header = new FileHeader( dis );
  }
  
  
  /**
   *
   */
  private void readRecords( LEDataInputStream dis ) throws IOException {
    records = new Vector();
    Record rec = null;
    
    try {
      for (;; ) {
        rec = new Record( dis );
        records.add( rec );
      }
    }
    catch( EOFException e ) {
      LOGGER.fine( "Finished reading " +
      new Integer( records.size() ).toString() + " records" );
    }
  }
  
  
  /**
   * Returns the total amount of records within the shapefiles
   * @return number of records
   */
  public int getRecordCount( ) {
    return records.size();
  }
  
  
  /**
   * Returns the offset for the file at a given index
   * @param index index for shape (0 to total no. of shapes)
   * @return offset in shapefile as int
   */
  public int getOffset( int index ) {
    Record rec = (Record)records.get( index );
    return rec.getOffset();
  }
  
  
  
  /**
   * Represents a file header in a shx file
   */
  class FileHeader {
    private int fileCode = -1;
    private int fileLength = -1;
    private int version = -1;
    private int shapeType = -1;
    private double[] bounds = new double[4];
    
    
    /**
     * Constructs a new IndexFile.FileHeader
     *
     * @param file LEDataInputStream for the index file
     */
    FileHeader( LEDataInputStream file ) throws IOException {
      getFileHeader( file );
    }
    
    
    private void getFileHeader( LEDataInputStream file ) throws IOException {
      file.setLittleEndianMode( false );
      fileCode = file.readInt( );
      LOGGER.fine( "File Code " + new Integer( fileCode ).toString() );
      
      // 5 integers which are not used
      //for( int i=0; i<5; i++ ) {
      //  int tmp = file.readInt( );
      //}
      file.skipBytes( 5 * 4 );
      
      fileLength = file.readInt( );
      LOGGER.fine( "File Length " + new Integer( fileLength ).toString() );
      
      file.setLittleEndianMode( true );
      version = file.readInt( );
      LOGGER.fine( "Version " + new Integer( version ).toString() );
      
      shapeType = file.readInt( );
      LOGGER.fine( "Shape Type " + new Integer( shapeType ).toString() );
      
      //read in the bounding box
      for( int i = 0; i<4; i++ ) {
        bounds[i] = file.readDouble( );
      }
      LOGGER.fine( "Bounds " + new Double( bounds[0] ).toString() + ", " +
      new Double( bounds[1] ).toString() + ", " +
      new Double( bounds[2] ).toString() + ", " +
      new Double( bounds[3] ).toString() );
      
      file.setLittleEndianMode( false );
      
      //skip remaining unnecessary bounding box values
      file.skipBytes( 32 );
    }
    
  } // Ends class FileHeader
  
  
  /**
   * Represents a record in a shx file
   */
  class Record {
    /*
     * The offset of a record in the main file is the number of 16-bit words from the
     * start of the main file to the first byte of the record header for the record.
     * Thus, the offset for the first record in the main file is 50, given the 100-byte
     * header.
     */
    private int offset = -1;
    private int contentLength = -1;
    
    
    /**
     * Constructs a new IndexFile.Record
     *
     * @param file LEDataInputStream for the index file
     */
    Record( LEDataInputStream file ) throws IOException {
      
      file.setLittleEndianMode( false );
      offset = file.readInt( );
      LOGGER.fine( "Offset " + new Integer( offset ).toString() );
      
      contentLength = file.readInt( );
      LOGGER.fine( "Content Length " + new Integer( contentLength ).toString() );
    }
    
    public int getOffset( ) { return offset; }
    public int getContentLength( ) { return contentLength; }
  } // Ends class Record
  
  
}