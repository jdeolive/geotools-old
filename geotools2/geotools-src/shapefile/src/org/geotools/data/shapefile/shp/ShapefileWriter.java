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

import com.vividsolutions.jts.geom.*;
import java.io.IOException;
import java.nio.channels.WritableByteChannel;
import java.nio.channels.FileChannel;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * ShapefileWriter allows for the storage of geometries in esris shp format.
 * During writing, an index will also be created. To create a ShapefileWriter,
 * do something like<br>
 * <code>
 *   GeometryCollection geoms;
 *   File shp = new File("myshape.shp");
 *   File shx = new File("myshape.shx");
 *   ShapefileWriter writer = new ShapefileWriter(
 *     shp.getChannel(),shx.getChannel()
 *   );
 *   writer.write(geoms,ShapeType.ARC);
 * </code>
 * This example assumes that each shape in the collection is a LineString.
 *   
 * @see org.geotools.data.shapefile.ShapefileDataStore
 * @author jamesm
 * @author aaime
 * @author Ian Schneider
 * 
 */
public class ShapefileWriter {
  
  FileChannel shpChannel;
  FileChannel shxChannel;
  ByteBuffer shapeBuffer;
  ByteBuffer indexBuffer;
  ShapeHandler handler;
  ShapeType type;
  int offset;
  int lp;
  int cnt;
  
  /** Creates a new instance of ShapeFileWriter */
  public ShapefileWriter(FileChannel shpChannel, FileChannel shxChannel) {
    this.shpChannel = shpChannel;
    this.shxChannel = shxChannel;
  }
  
//  private void allocateBuffers(int geomCnt, int fileLength) throws IOException {
//    if (shpChannel instanceof FileChannel) {
//      FileChannel shpc = (FileChannel) shpChannel;
//      FileChannel shxc = (FileChannel) shxChannel;
//      shapeBuffer = shpc.map(FileChannel.MapMode.READ_WRITE,0, fileLength);
//      indexBuffer = shxc.map(FileChannel.MapMode.READ_WRITE,0, 100 + 8 * geomCnt);
//      indexBuffer.order(ByteOrder.BIG_ENDIAN);
//    } else {
//      throw new RuntimeException("Can only handle FileChannels - fix me!");
//    }
//  }
  
  private void allocateBuffers() {
    shapeBuffer = ByteBuffer.allocateDirect(64 * 1024);
    indexBuffer = ByteBuffer.allocateDirect(100); 
  }
  
  private void checkShapeBuffer(int size) {
    if (shapeBuffer.capacity() < size) 
        shapeBuffer = ByteBuffer.allocateDirect(size);
  }
  
  private void drain() throws IOException {
    shapeBuffer.flip();
    indexBuffer.flip();
    while (shapeBuffer.remaining() > 0)
        shpChannel.write(shapeBuffer);
    while (indexBuffer.remaining() > 0)
        shxChannel.write(indexBuffer);
    shapeBuffer.flip().limit(shapeBuffer.capacity());
    indexBuffer.flip().limit(indexBuffer.capacity());
  }
  
  private void writeHeaders(GeometryCollection geometries,ShapeType type) throws IOException {
//    ShapefileHeader header = new ShapefileHeader();
//    Envelope bounds = geometries.getEnvelopeInternal();
//    header.write(shapeBuffer, type, geometries.getNumGeometries(), fileLength / 2,
//    bounds.getMinX(),bounds.getMinY(), bounds.getMaxX(),bounds.getMaxY()
//    );
//    header.write(indexBuffer, type, geometries.getNumGeometries(), 50 + 4 * geometries.getNumGeometries(),
//    bounds.getMinX(),bounds.getMinY(), bounds.getMaxX(),bounds.getMaxY()
//    );
      int fileLength = 100;
    //int largestShapeSize = 0;
      for (int i = geometries.getNumGeometries() - 1; i >= 0; i--) {
      // shape length + record (2 ints)
        int size = handler.getLength( geometries.getGeometryN(i) ) + 8;
        fileLength += size;
//      if (size > largestShapeSize)
//        largestShapeSize = size;
      }
      writeHeaders(geometries.getEnvelopeInternal(), type, geometries.getNumGeometries(), fileLength);
  }
  
  public void writeHeaders(Envelope bounds,ShapeType type,int numberOfGeometries,int fileLength) throws IOException {

      try {
        handler = type.getShapeHandler();
      } catch (ShapefileException se) {
        throw new RuntimeException("unexpected Exception",se);
      }
      if (shapeBuffer == null)
        allocateBuffers();
      ShapefileHeader header = new ShapefileHeader();
      header.write(shapeBuffer, type, numberOfGeometries, fileLength/2, 
      bounds.getMinX(),bounds.getMinY(),bounds.getMaxX(),bounds.getMaxY());
      header.write(indexBuffer, type, numberOfGeometries, 50 + 4 * numberOfGeometries, 
      bounds.getMinX(),bounds.getMinY(),bounds.getMaxX(),bounds.getMaxY());
      
      offset = 50;
      this.type = type;
      cnt = 0;
      
      shpChannel.position(0);
      shxChannel.position(0);
      drain();
  }
  
  public void writeGeometry(Geometry g) throws IOException {
      if (shapeBuffer == null)
          throw new IOException("Must write headers first");
      lp = shapeBuffer.position();
      int length = handler.getLength(g);
      
      checkShapeBuffer(length);
      
      length /= 2;
      
      shapeBuffer.order(ByteOrder.BIG_ENDIAN);
      shapeBuffer.putInt(++cnt);
      shapeBuffer.putInt(length);
      shapeBuffer.order(ByteOrder.LITTLE_ENDIAN);
      shapeBuffer.putInt(type.id);
      handler.write(shapeBuffer,g);
      
      assert (length * 2 == (shapeBuffer.position() - lp) - 8);

      lp = shapeBuffer.position();
      
      // write to the shx
      indexBuffer.putInt(offset);
      indexBuffer.putInt(length);
      offset += length + 4;
      
      drain();
      assert(shapeBuffer.position() == 0);
  }
  
  public void close() throws IOException {
    shpChannel.close();
    shxChannel.close();
  }
  
  public void write(GeometryCollection geometries, ShapeType type) throws IOException,ShapefileException {
    handler = type.getShapeHandler();
      
    writeHeaders(geometries,type);
    
    lp = shapeBuffer.position();
    for (int i = 0, ii = geometries.getNumGeometries(); i < ii; i++) {
      Geometry g = geometries.getGeometryN(i);

      writeGeometry(g);
    }

    close();
  }
  
  
}
