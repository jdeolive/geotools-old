/*
 * GMLYHandler.java
 *
 * Created on 07 March 2002, 16:31
 */

package org.geotools.gml.handlers;
import com.vividsolutions.jts.geom.*;
/** handler for Y elements
 *
 * @author ian
 * @version $Id: GMLYHandler.java,v 1.2 2002/03/11 14:38:57 ianturton Exp $
 */
public class GMLYHandler extends GMLXYZHandler{
    double y = 0.0;
    
    /** Creates a new instance of GMLYHandler */
    public GMLYHandler() {
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
     */    
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
    /**
     * @param s  */    
        public void parseText(String s){
        try{
            setY(Double.parseDouble(s));
        }catch(NumberFormatException e){
            System.err.println(""+e);
            setY(Double.NaN);
        }
    }
}
