/*
 * GMLXHandler.java
 *
 * Created on 07 March 2002, 16:31
 */

package org.geotools.gml.handlers;
import com.vividsolutions.jts.geom.*;
/** handler for X elements
 *
 * @author ian
 * @version $Id: GMLXHandler.java,v 1.2 2002/03/11 14:38:57 ianturton Exp $
 */
public class GMLXHandler extends GMLXYZHandler {
    double x=0.0;
    /** Creates a new instance of GMLXHandler */
    public GMLXHandler() {
    }

    /** not used
     */    
    public Geometry finish(GeometryFactory gf) {
        return null;
    }
    
    /** not used
     */    
    public void addGeometry(Geometry g) {
    }
    
    /** not used
     *
     */    
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
    /** convert string to x value
     * @param s  */    
    public void parseText(String s){
        try{
            setX(Double.parseDouble(s));
        }catch(NumberFormatException e){
            System.err.println(""+e);
            setX(Double.NaN);
        }
    }
    
    
}
