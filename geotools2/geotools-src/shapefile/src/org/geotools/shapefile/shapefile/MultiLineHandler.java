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


package org.geotools.shapefile.shapefile;

import com.vividsolutions.jts.geom.*;
import java.nio.ByteBuffer;


/*
 * $Id: MultiLineHandler.java,v 1.2 2003/03/30 20:21:09 ianschneider Exp $
 * @author aaime
 * @author Ian Schneider
 */
/** The default JTS handler for shapefile. Currently uses the default JTS
 * GeometryFactory, since it doesn't seem to matter.
 */
public class MultiLineHandler implements ShapeHandler {
  final ShapeType shapeType;
  GeometryFactory geometryFactory = new GeometryFactory();
  
  /** Create a MultiLineHandler for ShapeType.ARC */
  public MultiLineHandler() {
    shapeType = ShapeType.ARC;
  }
  
  /** Create a MultiLineHandler for one of:<br>
   * ShapeType.ARC,ShapeType.ARCM,ShapeType.ARCZ
   * @param type The ShapeType to use.
   * @throws InvalidShapefileException If the ShapeType is not correct (see constructor).
   */
  public MultiLineHandler(ShapeType type) throws InvalidShapefileException {
    if ((type != ShapeType.ARC) && (type != ShapeType.ARCM) && (type != ShapeType.ARCZ)) {
      throw new InvalidShapefileException(
      "MultiLineHandler constructor - expected type to be 3,13 or 23");
    }
    
    shapeType = type;
  }
  
  /** Get the type of shape stored (ShapeType.ARC,ShapeType.ARCM,ShapeType.ARCZ) */
  public ShapeType getShapeType() {
    return shapeType;
  }
  
  /** */
  public int getLength(Object geometry) {
    MultiLineString multi = (MultiLineString) geometry;
    
    int numlines;
    int numpoints;
    
    numlines = multi.getNumGeometries();
    numpoints = multi.getNumPoints();
    
    if (shapeType == ShapeType.ARC) {
      return 44 + (4 * numlines) + (numpoints * 16);
    }
    
    if (shapeType == ShapeType.ARCM) {
      return 44 + (4 * numlines) + (numpoints * 16) + 8 + 8 + (8 * numpoints);
    }
    
    if (shapeType == ShapeType.ARCZ) {
      return 44 + (4 * numlines) + (numpoints * 16) + 8 + 8 + (8 * numpoints) + 8 + 8 +
      (8 * numpoints);
    }
    
    throw new IllegalStateException("Expected ShapeType of Arc, got " + shapeType);
  }
  
  private Object createNull() {
    return geometryFactory.createLineString(null);
  }
  
  public Object read(ByteBuffer buffer, ShapeType type) {
    if (type == ShapeType.NULL)
      return createNull();
    //read bounding box (not needed)
    buffer.position( buffer.position() + 4 * 8);
    
    
    int numParts = buffer.getInt();
    int numPoints = buffer.getInt(); //total number of points
    
    int[] partOffsets = new int[numParts];
    
    //points = new Coordinate[numPoints];
    for (int i = 0; i < numParts; i++) {
      partOffsets[i] = buffer.getInt();
    }
    
    LineString[] lines = new LineString[numParts];
    Coordinate[] coords = new Coordinate[numPoints];
    
    for (int t = 0; t < numPoints; t++) {
      coords[t] = new Coordinate(buffer.getDouble(), buffer.getDouble());
    }
    
    if (shapeType == ShapeType.ARCZ) {
      //z min, max
      buffer.position(buffer.position() + 2 * 8);
      
      
      for (int t = 0; t < numPoints; t++) {
        coords[t].z = buffer.getDouble(); //z value
      }
    }
    
    int offset = 0;
    int start;
    int finish;
    int length;
    
    for (int part = 0; part < numParts; part++) {
      start = partOffsets[part];
      
      if (part == (numParts - 1)) {
        finish = numPoints;
      } else {
        finish = partOffsets[part + 1];
      }
      
      length = finish - start;
      
      Coordinate[] points = new Coordinate[length];
      
      for (int i = 0; i < length; i++) {
        points[i] = coords[offset];
        offset++;
      }
      
      lines[part] = geometryFactory.createLineString(points);
    }
    
    if (numParts == 1) {
      return lines[0];
    } else {
      return geometryFactory.createMultiLineString(lines);
    }
  }
  
  public void write(ByteBuffer buffer, Object geometry) {
    MultiLineString multi = (MultiLineString) geometry;
    
    Envelope box = multi.getEnvelopeInternal();
    buffer.putDouble(box.getMinX());
    buffer.putDouble(box.getMinY());
    buffer.putDouble(box.getMaxX());
    buffer.putDouble(box.getMaxY());
    
    int numParts = multi.getNumGeometries();
    
    buffer.putInt(numParts);
    int npoints = multi.getNumPoints();
    buffer.putInt(npoints);
    
    LineString[] lines = new LineString[numParts];
    int idx = 0;
    
    for (int i = 0; i < numParts; i++) {
      lines[i] = (LineString) multi.getGeometryN(i);
      buffer.putInt(idx);
      idx = idx + lines[i].getNumPoints();
    }
    
    Coordinate[] coords = multi.getCoordinates();
    
    for (int t = 0; t < npoints; t++) {
      buffer.putDouble(coords[t].x);
      buffer.putDouble(coords[t].y);
    }
    
    if (shapeType == ShapeType.ARCZ) {
      double[] zExtreame = JTSUtilities.zMinMax(coords);
      
      if (Double.isNaN(zExtreame[0])) {
        buffer.putDouble(0.0);
        buffer.putDouble(0.0);
      } else {
        buffer.putDouble(zExtreame[0]);
        buffer.putDouble(zExtreame[1]);
      }
      
      for (int t = 0; t < npoints; t++) {
        double z = coords[t].z;
        
        if (Double.isNaN(z)) {
          buffer.putDouble(0.0);
        } else {
          buffer.putDouble(z);
        }
      }
    }
    
    if (shapeType == ShapeType.ARCZ) {
      buffer.putDouble(-10E40);
      buffer.putDouble(-10E40);
      
      for (int t = 0; t < npoints; t++) {
        buffer.putDouble(-10E40);
      }
    }
  }
  
}


/*
 * $Log: MultiLineHandler.java,v $
 * Revision 1.2  2003/03/30 20:21:09  ianschneider
 * Moved buffer branch to main
 *
 * Revision 1.1.2.3  2003/03/12 15:30:14  ianschneider
 * made ShapeType final for handlers - once they're created, it won't change.
 *
 * Revision 1.1.2.2  2003/03/07 00:36:41  ianschneider
 *
 * Added back the additional ShapeType parameter in ShapeHandler.read. ShapeHandler's need
 * return their own special "null" shape if needed. Fixed the ShapefileReader to not throw
 * exceptions for "null" shapes. Fixed ShapefileReader to accomodate junk after the last valid
 * record. The theory goes, if the shape number is proper, that is, one greater than the
 * previous, we consider that a valid record and attempt to read it. I suppose, by chance, the
 * junk could coincide with the next record number. Stupid ESRI. Fixed some record-length
 * calculations which resulted in writing of bad shapefiles.
 *
 * Revision 1.1.2.1  2003/03/06 01:16:34  ianschneider
 *
 * The initial changes for moving to java.nio. Added some documentation and improved
 * exception handling. Works for reading, may work for writing as of now.
 *
 * Revision 1.1  2003/02/27 22:35:50  aaime
 * New shapefile module, initial commit
 *
 * Revision 1.2  2003/01/22 18:31:05  jaquino
 * Enh: Make About Box configurable
 *
 * Revision 1.3  2002/10/30 22:36:11  dblasby
 * Line reader now returns LINESTRING(..) if there is only one part to the arc
 * polyline.
 *
 * Revision 1.2  2002/09/09 20:46:22  dblasby
 * Removed LEDatastream refs and replaced with EndianData[in/out]putstream
 *
 * Revision 1.1  2002/08/27 21:04:58  dblasby
 * orginal
 *
 * Revision 1.2  2002/03/05 10:23:59  jmacgill
 * made sure geometries were created using the factory methods
 *
 * Revision 1.1  2002/02/28 00:38:50  jmacgill
 * Renamed files to more intuitve names
 *
 * Revision 1.3  2002/02/13 00:23:53  jmacgill
 * First semi working JTS version of Shapefile code
 *
 * Revision 1.2  2002/02/11 18:42:45  jmacgill
 * changed read and write statements so that they produce and take Geometry objects instead of specific MultiLine objects
 * changed parts[] array name to partOffsets[] for clarity and consistency with ShapePolygon
 *
 * Revision 1.1  2002/02/11 16:54:43  jmacgill
 * added shapefile code and directories
 *
 */
