/*
 * GMLYHandler.java
 *
 * Created on 07 March 2002, 16:31
 */

package org.geotools.gml.handlers;
import com.vividsolutions.jts.geom.*;
/**
 *
 * @author  ian
 */
public class GMLYHandler extends GMLXYZHandler{
    double y = 0.0;
    
    /** Creates a new instance of GMLYHandler */
    public GMLYHandler() {
    }

    public Geometry finish(GeometryFactory gf) {
        return null;
    }
    
    public void addGeometry(Geometry g) {
    }
    
    public void addCoordinate(Coordinate c) {
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
        public void parseText(String s){
        try{
            setY(Double.parseDouble(s));
        }catch(NumberFormatException e){
            System.err.println(""+e);
            setY(Double.NaN);
        }
    }
}
