/*
 * GMLCoordinateHandler.java
 *
 * Created on 07 March 2002, 15:37
 */

package org.geotools.gml.handlers;
import org.geotools.gml.*;
import com.vividsolutions.jts.geom.*;
import java.util.*;
/**
 *
 * @author  ian
 */
public class GMLCoordinatesHandler extends GMLHandler{
    ArrayList coordList = new ArrayList();
    /** Creates a new instance of GMLCoordinateHandler */
    public GMLCoordinatesHandler() {
    }
    
    public Geometry finish(GeometryFactory gf) {
        return null;
    }
    public Coordinate[] getCoordinates(){
        return (Coordinate[])coordList.toArray(new Coordinate[0]);
    }
    public void parseText(String s){
        StringTokenizer st = new StringTokenizer(s,",");
        if(st.countTokens()>2){
            System.out.println("problem parsing coordinate "+s);
        }else{
            double x = Double.parseDouble(st.nextToken());
            double y = Double.parseDouble(st.nextToken());
            System.out.println("adding coordinate in coords handler");
            coordList.add(new Coordinate(x,y));
            
        }
        
    }
    public void addGeometry(Geometry g){}
    public void addCoordinate(Coordinate c){}

}
