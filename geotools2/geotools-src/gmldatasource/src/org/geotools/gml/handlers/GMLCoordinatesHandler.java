/*
 * GMLCoordinateHandler.java
 *
 * Created on 07 March 2002, 15:37
 */

package org.geotools.gml.handlers;
import org.geotools.gml.*;
import com.vividsolutions.jts.geom.*;
import java.util.*;
/** A handler for a coordinates element
 * collects coordinates and returns an array of coordinates
 *
 * @author ian
 * @version $Id: GMLCoordinatesHandler.java,v 1.4 2002/03/11 14:38:57 ianturton Exp $
 */
public class GMLCoordinatesHandler extends GMLHandler{
    ArrayList coordList = new ArrayList();
    /** Creates a new instance of GMLCoordinateHandler */
    public GMLCoordinatesHandler() {
    }
    
    /** not used
     * @param gf
     * @return
     */    
    public Geometry finish(GeometryFactory gf) {
        return null;
    }
    /** finish collecting coordinates and return the list
     * @return an array of coordinates
     */    
    public Coordinate[] getCoordinates(){
        return (Coordinate[])coordList.toArray(new Coordinate[0]);
    }
    /** converts string to coordinates
     * @param s string containg comma seperated coordinates
     */    
    public void parseText(String s){
        StringTokenizer st = new StringTokenizer(s,",");
        int n = st.countTokens();
        if(n<2){
            System.err.println("error parsing coord");
            return;
        }else{
            
            double x = Double.parseDouble(st.nextToken());
            double y = Double.parseDouble(st.nextToken());
            if(n==2){
          
                coordList.add(new Coordinate(x,y));
                return;
            }
       
            double z = Double.parseDouble(st.nextToken());
            coordList.add(new Coordinate(x,y,z));
            
        }
        
    }
    /** not used
     */    
    public void addGeometry(Geometry g){}
    /** does nothing
     */    
    public void addCoordinate(Coordinate c){}
    
}
