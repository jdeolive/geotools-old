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
import java.nio.channels.*;



/** IndexFile parser for .shx files.<br>
 * For now, the creation of index files is done in the ShapefileWriter. But this
 * can be used to access the index.<br>
 * For details on the index file, see <br>
 * <a href="http://www.esri.com/library/whitepapers/pdfs/shapefile.pdf"><b>"ESRI(r) Shapefile - A Technical Description"</b><br>
 * * <i>'An ESRI White Paper . May 1997'</i></a>
 *
 * @author Ian Schneider
 */
public class IndexFile {
  
  private ShapefileHeader header = null;
  private int[] content;

  /** Load the index file from the given channel.
   * @param channel The channel to read from.
   * @throws IOException If an error occurs.
   */  
  public IndexFile( ReadableByteChannel channel ) throws IOException {
    readHeader(channel);
    readRecords(channel);
  }
  
  /** Get the header of this index file.
   * @return The header of the index file.
   */  
  public ShapefileHeader getHeader() {
    return header;
  }
  
  private void readHeader(ReadableByteChannel channel) throws IOException {
    ByteBuffer buffer = ByteBuffer.allocateDirect(100);
    while (buffer.remaining() > 0) {
      channel.read(buffer);
    }
    buffer.flip();
    header = new ShapefileHeader();
    header.read(buffer, true);
  }
  
  private void readRecords(ReadableByteChannel channel) throws IOException {
    int remaining = (header.getFileLength() * 2) - 100;
    ByteBuffer buffer = ByteBuffer.allocateDirect(remaining);
    buffer.order(ByteOrder.BIG_ENDIAN);
    while (buffer.remaining() > 0) {
      channel.read(buffer);
    }
    buffer.flip();
    int records = remaining / 4;
    content = new int[ records ];
    IntBuffer ints = buffer.asIntBuffer();
    ints.get(content);
  }

  /** Get the number of records in this index.
   * @return The number of records.
   */  
  public int getRecordCount( ) {
    return content.length / 2;
  }
  
  /** Get the offset of the record (in real bytes, not 16-bit words).
   * @param index The index, from 0 to getRecordCount - 1
   * @return The offset in bytes.
   */  
  public int getOffset( int index ) {
    return content[2 * index];
  }
  
  /** Get the content length of the given record in bytes, not 16 bit words.
   * @param index The index, from 0 to getRecordCount - 1
   * @return The lengh in bytes of the record.
   */  
  public int getContentLength( int index) {
    return content[2 * index + 1];
  }
  
  
}