/*
 * GeometryPickler.java
 *
 * Created on June 5, 2003, 11:51 AM
 */

package org.geotools.data.pickle;

import com.vividsolutions.jts.geom.*;
import java.io.*;

/**
 * The GeometryPickler provides a more efficent serialization of JTS Geometry
 * subclasses.<br>
 *
 * There is no preservation of PrecisionModel or srid.<br>
 *
 * <pre>
 * Records for coordinates are:
 *   short length
 *   double (x,y,z)*
 *
 * Records for non collections are unless otherwise specified:
 *   byte type
 *   coordinates
 * 
 * Records for collections are
 *   byte type
 *   geometry*
 *
 * Record for Point is
 *   byte POINT
 *   double (x,y,z)*
 *
 * Record for Polygon is
 *   byte POLY
 *   short number of interior rings
 *   linestring+ (outer + interior)
 * </pre>
 *   
 * @author  Ian Schneider
 */
public final class GeometryPickler {
  final int POINT = 1;
  final int LINE  = 2;
  final int POLY  = 3;
  final int COLLECTION = 10;
  final int MULTI_POINT = 11;
  final int MULTI_LINE = 12;
  final int MULTI_POLY = 13;
  
  static PrecisionModel pm = new PrecisionModel();
  static int srid = 0;
  
  public GeometryPickler() {
  }
  
  public void write(Geometry g, ObjectOutputStream out) throws IOException {
    if (g instanceof GeometryCollection) {
      writeCollection ((GeometryCollection)g,out);
    } else if (g instanceof Point) {
      writePoint(g,out); 
    } else if (g instanceof LineString) {
      writeLineString(g,out); 
    } else if (g instanceof Polygon) {
      writePolygon(g,out); 
    } else {
      throw new IOException("Cannot write " + g); 
    }
  }
  
  private void writeCollection(GeometryCollection gc, ObjectOutputStream out) throws IOException {
    final int cnt = gc.getNumGeometries();
    if (gc instanceof MultiPoint)
      out.writeByte(MULTI_POINT);
    else if (gc instanceof MultiLineString)
      out.writeByte(MULTI_LINE);
    else if (gc instanceof MultiPolygon)
      out.writeByte(MULTI_POLY);
    else
      out.writeByte(COLLECTION);
    out.writeShort(cnt);
    for (int i = 0; i < cnt; i++) {
      write(gc.getGeometryN(i),out); 
    }
  }
  
  private void writePoint(Geometry g, ObjectOutputStream out) throws IOException {
    out.writeByte(POINT);
    Coordinate c = g.getCoordinate();
    out.writeDouble(c.x);
    out.writeDouble(c.y);
    out.writeDouble(c.z);
  }
  
  private void writeLineString(Geometry g, ObjectOutputStream out) throws IOException {
    out.writeByte(LINE);
    writeCoordinates(g.getCoordinates(),out);
  }
  
  private void writePolygon(Geometry g, ObjectOutputStream out) throws IOException {
    out.writeByte(POLY);
    Polygon p = (Polygon) g;
    out.writeShort( p.getNumInteriorRing() );
    writeLineString(p.getExteriorRing(),out);
    for (int i = 0, ii = p.getNumInteriorRing(); i < ii; i++) {
      writeLineString(p.getInteriorRingN(i),out);
    }
  }
  
  private void writeCoordinates(Coordinate[] c, ObjectOutputStream out) throws IOException {
    out.writeShort(c.length);
    for (int i = 0, ii = c.length; i < ii; i++) {
      out.writeDouble(c[i].x);
      out.writeDouble(c[i].y);
      out.writeDouble(c[i].z);
    }
  }
  
  
  public Geometry read(ObjectInputStream in) throws IOException {
    final int type = in.readByte();
    switch (type) {
      case POINT:
        return readPoint(in);
      case LINE:
        return readLine(in,false);
      case POLY:
        return readPoly(in);
      case MULTI_POINT:
        return readMultiPoint(in);
      case MULTI_LINE:
        return readMultiLine(in);
      case MULTI_POLY:
        return readMultiPoly(in);
      case COLLECTION:
        return readCollection(in);
    }
    throw new IOException("Unknown geometry block : " + type);
  }
  
  private Coordinate[] readCoords(ObjectInputStream in) throws IOException {
    final short len = in.readShort();
    Coordinate[] coords = new Coordinate[len];
    for (int i = 0; i < len; i++) {
      coords[i] = new Coordinate(in.readDouble(),in.readDouble(),in.readDouble()); 
    }
    return coords;
  }
  
  private Point readPoint(ObjectInputStream in) throws IOException {
    Coordinate c = new Coordinate();
    c.x = in.readDouble();
    c.y = in.readDouble();
    c.z = in.readDouble();
    return new Point(c,pm,srid);
  }
  
  private LineString readLine(ObjectInputStream in, boolean asRing) throws IOException {
    if (asRing)
      return new LinearRing(readCoords(in),pm,srid);
    else
      return new LineString(readCoords(in), pm,srid);
  }
  
  private Polygon readPoly(ObjectInputStream in) throws IOException {
    final int interior = in.readShort();
    LinearRing[] interiorRings = new LinearRing[interior];
    in.readByte();
    LinearRing exterior = (LinearRing) readLine(in,true);
    for (int i = 0; i < interior; i++) {
      in.readByte();
      interiorRings[i] = (LinearRing) readLine(in,true);
    }
    return new Polygon(exterior,interiorRings,pm,srid);
    
  }
  
  private MultiPoint readMultiPoint(ObjectInputStream in) throws IOException {
    final int geoms = in.readShort();
    Point[] g = new Point[geoms];
    for (int i = 0; i < geoms; i++) {
      in.readByte();
      g[i] = readPoint(in); 
    }
    return new MultiPoint(g, pm,srid);
  }
  
  private MultiLineString readMultiLine(ObjectInputStream in) throws IOException {
    final int geoms = in.readShort();
    LineString[] g = new LineString[geoms];
    for (int i = 0; i < geoms; i++) {
      in.readByte();
      g[i] = readLine(in,false); 
    }
    return new MultiLineString(g, pm,srid);
  }
  
  private MultiPolygon readMultiPoly(ObjectInputStream in) throws IOException {
    final int geoms = in.readShort();
    Polygon[] g = new Polygon[geoms];
    for (int i = 0; i < geoms; i++) {
      in.readByte();
      g[i] = readPoly(in); 
    }
    return new MultiPolygon(g, pm,srid);
  }
  
  private GeometryCollection readCollection(ObjectInputStream in) throws IOException {
    final int geoms = in.readShort();
    Geometry[] g = new Geometry[geoms];
    for (int i = 0; i < geoms; i++) {
      g[i] = read(in); 
    }
    return new GeometryCollection(g, pm,srid);
  }
}
