/*
 * DefaultMark.java
 *
 * Created on 28 May 2002, 15:09
 */

package org.geotools.styling;

import com.vividsolutions.jts.geom.*;
import java.util.StringTokenizer;
/**
 *
 * @author  iant
 */
public class DefaultMark implements Mark {
    private static org.apache.log4j.Category _log = 
        org.apache.log4j.Category.getInstance("defaultcore.styling");    
    Fill fill = new DefaultFill();
    Stroke stroke = new DefaultStroke();
    String wellKnownName = "Square";
    Polygon shape;
    double size = 6.0;
    double rotation = 0.0;
    private GeometryFactory geometryFactory = new GeometryFactory();
    /** Creates a new instance of DefaultMark */
    public DefaultMark() {
        
        setShape(wellKnownName);
    }
    public DefaultMark(String name){
        
        setShape(name);
    }
    
    
    
    private boolean setShape(String[] points){
        
        Coordinate[] newPoints = new Coordinate[points.length];
        for(int i=0;i<points.length;i++){
            StringTokenizer innerTok = new StringTokenizer(points[i],",");
            double x = Double.parseDouble(innerTok.nextToken());
            double y = Double.parseDouble(innerTok.nextToken());
            newPoints[i] = new Coordinate(x,y);
        }
        try{
            
            LinearRing ring = geometryFactory.createLinearRing(newPoints);
            shape = geometryFactory.createPolygon(ring,null);
            return true;
        } catch (TopologyException e){
            return false;
        }
    }
    public boolean setShape(String wellKnownName){
        for(int i=0;i<knownShapes.length;i++){
            _log.debug("comparing "+wellKnownName+" with "+knownShapes[i]);
            if(wellKnownName.equalsIgnoreCase(knownShapes[i])){
                this.wellKnownName=knownShapes[i];
                _log.info("setting mark to "+wellKnownName);
                return setShape(knownShapesPoints[i]);
            }
        }
        wellKnownName ="";
        return false;
    }
    
    /**
     * This parameter defines which fill style to use when renderin the Mark.
     *
     * @return the Fill definition to use when rendering the Mark.
     */
    public Fill getFill() {
        return fill;
    }
    
    /**
     * This paramterer defines which stroke style should be used when
     * rendering the Mark.
     *
     * @return The Stroke defenition to use when rendering the Mark.
     */
    public Stroke getStroke() {
        return stroke;
    }
    
    /**
     * This parameter gives the well-known name of the shape of the mark.<br>
     * Allowed names include at lest "square", "circle", "triangle", "star",
     * "cross" and "x" though renderers may draw a different symbol instead
     * if they don't have a shape for all of these.<br>
     *
     * @return The well known name of a shape.  The default value is "square".
     */
    public String getWellKnownName() {
        return wellKnownName;
    }
    
    /** Setter for property fill.
     * @param fill New value of property fill.
     */
    public void setFill(org.geotools.styling.Fill fill) {
        this.fill = fill;
    }
    
    /** Setter for property stroke.
     * @param stroke New value of property stroke.
     */
    public void setStroke(org.geotools.styling.Stroke stroke) {
        this.stroke = stroke;
    }
    
    public void setSize(double size){
        this.size = size;
    }
    /** Setter for property wellKnownName.
     * @param wellKnownName New value of property wellKnownName.
     */
    public boolean setWellKnownName(java.lang.String wellKnownName) {
        this.wellKnownName = wellKnownName;
        return setShape(wellKnownName);
    }
    /**
     * returns the actual geometry to render by combining the stored geometry of the mark with
     * the coordinates of the point to be displayed.
     */
    public Geometry getGeometry(Geometry geom, double scale){
        if (geom.isEmpty()){
            return geom;
        }
        
        if( geom instanceof Point ){
            // add path to point and return
            Coordinate centre = ((Point)geom).getCoordinate();
            
            Coordinate[] points = shape.getCoordinates();
            Coordinate[] newPoints = new Coordinate[points.length];
            for (int i = 0; i < points.length; i++){
                double x = points[i].x;
                double y = points[i].y;
                newPoints[i] = new Coordinate(centre.x+(size*x/scale),centre.y+(size*y/scale));
                // TODO: replace with an affineTransform to handle tanslation,scale and rotation
            }
            try{
                
                LinearRing ring = geometryFactory.createLinearRing(newPoints);
                Polygon p = geometryFactory.createPolygon(ring,null);
                return p;
            } catch (TopologyException e){
                return null;
            }
            
        }
        //TODO: implement line + polygon point methods
        return null;
    }
    
    public void setRotation(double rotation) {
        this.rotation = rotation;
    }
    /** 
     * a list of wellknownshapes that we know about 
     * square, circle, triangle, star, cross, x
     */
    static String[] knownShapes = {"Square","triangle"};
    /** 
     * the coordinates of the wellknownshapes as a unit shape centred on 0,0
     */
    static String[][] knownShapesPoints = {{"-.5,-.5","-.5,.5",".5,.5",".5,-.5","-.5,-.5"},
        {"0,.5","-.5,-.5",".5,-.5","0,.5"} // these numbers are not right TODO: calculate triangle properly
    };
}
