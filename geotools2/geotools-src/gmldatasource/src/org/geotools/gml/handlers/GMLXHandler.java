/*
 * GMLXHandler.java
 *
 * Created on 07 March 2002, 16:31
 */

package org.geotools.gml.handlers;
import com.vividsolutions.jts.geom.*;
/**
 *
 * @author  ian
 */
public class GMLXHandler extends GMLXYZHandler {
    double x=0.0;
    /** Creates a new instance of GMLXHandler */
    public GMLXHandler() {
    }

    public Geometry finish(GeometryFactory gf) {
        return null;
    }
    
    public void addGeometry(Geometry g) {
    }
    
    public void addCoordinate(Coordinate c) {
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
    public void parseText(String s){
        try{
            setX(Double.parseDouble(s));
        }catch(NumberFormatException e){
            System.err.println(""+e);
            setX(Double.NaN);
        }
    }
    
    
}
