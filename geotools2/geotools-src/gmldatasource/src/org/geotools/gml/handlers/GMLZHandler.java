/*
 * GMLZHandler.java
 *
 * Created on 07 March 2002, 17:51
 */

package org.geotools.gml.handlers;
import com.vividsolutions.jts.geom.*;
/** handler for Z elements
 *
 * @author ian
 * @version $Id: GMLZHandler.java,v 1.2 2002/03/11 14:41:47 ianturton Exp $
 */
public class GMLZHandler extends GMLXYZHandler {
    double z = Double.NaN;
    /** Creates a new instance of GMLZHandler */
    public GMLZHandler() {
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
    /** convert string to z value
     * @param s  */    
        public void parseText(String s){
        try{
            setZ(Double.parseDouble(s));
        }catch(NumberFormatException e){
            System.err.println(""+e);
            setZ(Double.NaN);
        }
    }
}
