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
 *
 * @author jamesm
 * @author aaime
 * @author Ian Schneider
 * 
 */
public class ShapefileWriter {
  
  WritableByteChannel shpChannel;
  WritableByteChannel shxChannel;
  ByteBuffer shapeBuffer;
  ByteBuffer indexBuffer;
  ShapeHandler handler;
  ShapeType type;
  int fileLength;
  
  /** Creates a new instance of ShapeFileWriter */
  public ShapefileWriter(WritableByteChannel shpChannel, WritableByteChannel shxChannel) {
    this.shpChannel = shpChannel;
    this.shxChannel = shxChannel;
  }
  
  private void init(final GeometryCollection geometries) throws IOException,ShapefileException {
    handler = type.getShapeHandler();
    fileLength = 100;
    //int largestShapeSize = 0;
    for (int i = geometries.getNumGeometries() - 1; i >= 0; i--) {
      // shape length + record (2 ints)
      int size = handler.getLength( geometries.getGeometryN(i) ) + 8;
      fileLength += size;
//      if (size > largestShapeSize)
//        largestShapeSize = size;
    }
    allocateBuffers(geometries.getNumGeometries());
  }
  
  private void allocateBuffers(int geomCnt) throws IOException {
    if (shpChannel instanceof FileChannel) {
      FileChannel shpc = (FileChannel) shpChannel;
      FileChannel shxc = (FileChannel) shxChannel;
      shapeBuffer = shpc.map(FileChannel.MapMode.READ_WRITE,0, fileLength);
      indexBuffer = shxc.map(FileChannel.MapMode.READ_WRITE,0, 100 + 8 * geomCnt);
      indexBuffer.order(ByteOrder.BIG_ENDIAN);
    } else {
      throw new RuntimeException("Can only handle FileChannels - fix me!");
    }
  }
  
  private void writeHeaders(final GeometryCollection geometries) throws IOException {
    ShapefileHeader header = new ShapefileHeader();
    Envelope bounds = geometries.getEnvelopeInternal();
    header.write(shapeBuffer, type, geometries.getNumGeometries(), fileLength / 2,
    bounds.getMinX(),bounds.getMinY(), bounds.getMaxX(),bounds.getMaxY()
    );
    header.write(indexBuffer, type, geometries.getNumGeometries(), 50 + 4 * geometries.getNumGeometries(),
    bounds.getMinX(),bounds.getMinY(), bounds.getMaxX(),bounds.getMaxY()
    );
  }
  
  //ShapeFileDimentions =>    2=x,y ; 3=x,y,m ; 4=x,y,z,m
  public void write(GeometryCollection geometries, ShapeType type) throws IOException,ShapefileException {
    this.type = type;
    
    init(geometries);
    writeHeaders(geometries);
    
    int offset = 50;
    int lp = shapeBuffer.position();
    for (int i = 0, ii = geometries.getNumGeometries(); i < ii; i++) {
      Geometry g = geometries.getGeometryN(i);

      // write to the shp
      int length = handler.getLength(g) / 2;
      shapeBuffer.order(ByteOrder.BIG_ENDIAN);
      shapeBuffer.putInt(i + 1);
      shapeBuffer.putInt(length);
      shapeBuffer.order(ByteOrder.LITTLE_ENDIAN);
      shapeBuffer.putInt(type.id);
      handler.write(shapeBuffer,g);
      
      assert (length * 2 == (shapeBuffer.position() - lp) - 8);

      lp = shapeBuffer.position();
      System.out.flush();
      
      // write to the shx
      indexBuffer.putInt(offset);
      indexBuffer.putInt(length);
      offset += length + 4;
    }

    shpChannel.close();
    shxChannel.close();
  }
  
  
}
