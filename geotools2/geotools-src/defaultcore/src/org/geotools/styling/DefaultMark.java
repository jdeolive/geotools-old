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
    Fill fill = new DefaultFill();
    Stroke stroke = new DefaultStroke();
    String wellKnownName = "Square";
    Polygon shape;
    private GeometryFactory geometryFactory = new GeometryFactory();
    /** Creates a new instance of DefaultMark */
    public DefaultMark() {
        setShape(wellKnownName);
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
            if(wellKnownName.equalsIgnoreCase(knownShapes[i])){
                this.wellKnownName=knownShapes[i];
                return setShape(knownShapesPoints[i]);
            }
        }
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
    
    /** Setter for property wellKnownName.
     * @param wellKnownName New value of property wellKnownName.
     */
    public void setWellKnownName(java.lang.String wellKnownName) {
        this.wellKnownName = wellKnownName;
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
                newPoints[i] = new Coordinate(centre.x+(x/scale),centre.y+(y/scale));
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
    static String[] knownShapes = {"Square"};
    static String[][] knownShapesPoints = {{"-3,-3","-3,3","3,3","3,-3","-3,-3"}};
}
