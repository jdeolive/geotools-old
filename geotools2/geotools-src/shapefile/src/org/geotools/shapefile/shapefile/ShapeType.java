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

/** Not much but a type safe enumeration of file types as ints and names. The
 * descriptions can easily be tied to a ResourceBundle if someone wants to do that.
 * @author Ian Schneider
 */
public final class ShapeType {
  
  /** Represents a Null shape (id = 0). */  
  public static final ShapeType NULL  = new ShapeType(0,"Null");
  /** Represents a Point shape (id = 1). */  
  public static final ShapeType POINT = new ShapeType(1,"Point");
  /** Represents a PointZ shape (id = 11). */  
  public static final ShapeType POINTZ = new ShapeType(11,"PointZ");
  /** Represents a PointM shape (id = 21). */  
  public static final ShapeType POINTM = new ShapeType(21,"PointM");
  /** Represents an Arc shape (id = 3). */  
  public static final ShapeType ARC   = new ShapeType(3,"Arc");
  /** Represents an ArcZ shape (id = 13). */  
  public static final ShapeType ARCZ   = new ShapeType(13,"ArcZ");
  /** Represents an ArcM shape (id = 23). */  
  public static final ShapeType ARCM   = new ShapeType(23,"ArcM");
  /** Represents a Polygon shape (id = 5). */  
  public static final ShapeType POLYGON = new ShapeType(5,"Polygon");
  /** Represents a PolygonZ shape (id = 15). */  
  public static final ShapeType POLYGONZ = new ShapeType(15,"PolygonZ");
  /** Represents a PolygonM shape (id = 25). */  
  public static final ShapeType POLYGONM = new ShapeType(25,"PolygonM");
  /** Represents a MultiPoint shape (id = 8). */  
  public static final ShapeType MULTIPOINT = new ShapeType(8,"MultiPoint");
  /** Represents a MultiPointZ shape (id = 18). */  
  public static final ShapeType MULTIPOINTZ = new ShapeType(18,"MultiPointZ");
  /** Represents a MultiPointZ shape (id = 28). */  
  public static final ShapeType MULTIPOINTM = new ShapeType(28,"MultiPointM");
  
  /** Represents an Undefined shape (id = -1). */  
  public static final ShapeType UNDEFINED = new ShapeType(-1,"Undefined");
  
  /** The integer id of this ShapeType. */  
  public final int id;
  /** The human-readable name for this ShapeType.<br>
   * Could easily use ResourceBundle for internationialization.
   */  
  public final String name;
  
  /** Creates a new instance of ShapeType. Hidden on purpose.
   * @param id The id.
   * @param name The name.
   */
  protected ShapeType(int id,String name) {
    this.id = id;
    this.name = name;
  }
  
  /** Get the name of this ShapeType.
   * @return The name.
   */  
  public String toString() { return name; }
  
  /** Is this a multipoint shape? Hint- all shapes are multipoint except NULL,
   * UNDEFINED, and the POINTs.
   * @return true if multipoint, false otherwise.
   */  
  public boolean isMultiPoint() {
    if (this == UNDEFINED) return false;
    if (this == NULL) return false;
    if (this == POINT) return false;
    return true;
  }
  
  /** Determine the ShapeType for the id.
   * @param id The id to search for.
   * @return The ShapeType for the id.
   */  
  public static ShapeType forID(int id) {
    switch (id) {
      case 0:
        return NULL;
      case 1:
        return POINT;
      case 11:
        return POINTZ;
      case 21:
        return POINTM;
      case 3:
        return ARC;
      case 13:
        return ARCZ;
      case 23:
        return ARCM;
      case 5:
        return POLYGON;
      case 15:
        return POLYGONZ;
      case 25:
        return POLYGONM;
      case 8:
        return MULTIPOINT;
      case 18:
        return MULTIPOINTZ;
      case 28:
        return MULTIPOINTM;
      default:
        return UNDEFINED;
    }
  }
  
  /** Each ShapeType corresponds to a handler. In the future this should probably go
   * else where to allow different handlers, or something...
   * @throws InvalidShapefileException If the ShapeType is bogus.
   * @return The correct handler for this ShapeType. Returns a new one.
   */  
  public ShapeHandler getShapeHandler() throws InvalidShapefileException {
    switch (id) {
      case 1: case 11: case 21:
        return new PointHandler(this);
      case 3: case 13: case 23:
        return new MultiLineHandler(this);
      case 5: case 15: case 25:
        return new PolygonHandler(this);
      case 8: case 18: case 28:
        return new MultiPointHandler(this);
    }
    return null;
  }
  
}
