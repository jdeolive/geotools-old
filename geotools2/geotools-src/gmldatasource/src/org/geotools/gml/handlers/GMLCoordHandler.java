/*
 * GMLCoordHandler.java
 *
 * Created on 07 March 2002, 16:18
 */

package org.geotools.gml.handlers;
import com.vividsolutions.jts.geom.*;
/** Handler for Coord elements
 *
 * @author ian
 * @version $Id: GMLCoordHandler.java,v 1.2 2002/03/11 14:41:36 ianturton Exp $
 */
public class GMLCoordHandler extends org.geotools.gml.GMLHandler {
    double x=0.0;
    double y=0.0;
    double z= Double.NaN;
    /** Creates a new instance of GMLCoordHandler */
    public GMLCoordHandler() {
    }

    /** does nothing
     * @param gf
     * @return
     */    
    public Geometry finish(GeometryFactory gf) {
        return null;
    }
    
    /** does nothing
     */    
    public void addGeometry(Geometry g) {
    }
    
    /** does nothing
     */    
    public void addCoordinate(Coordinate c) {
    }
    /** returns a coordinate represented by the handler
     * @return the coordinate
     */    
    public Coordinate getCoordinate(){
        return new Coordinate(x,y,z);
    }
    
    /** Getter for property x.
     * @return Value of property x.
     */
    public double getX() {
        return x;
    }
    
    /** Setter for property x.
     * @param x New value of property x.
     */
    public void setX(double x) {
        this.x = x;
    }
    
    /** Getter for property y.
     * @return Value of property y.
     */
    public double getY() {
        return y;
    }
    
    /** Setter for property y.
     * @param y New value of property y.
     */
    public void setY(double y) {
        this.y = y;
    }
    
    /** Getter for property z.
     * @return Value of property z.
     */
    public double getZ() {
        return z;
    }
    
    /** Setter for property z.
     * @param z New value of property z.
     */
    public void setZ(double z) {
        this.z = z;
    }
    
}
