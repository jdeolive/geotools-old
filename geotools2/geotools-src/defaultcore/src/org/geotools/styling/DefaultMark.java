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
    private static org.apache.log4j.Logger _log = 
        org.apache.log4j.Logger.getLogger(DefaultMark.class);    
    Fill fill = new DefaultFill();
    Stroke stroke = new DefaultStroke();
    String wellKnownName = "Square";
    Polygon shape;
    double size = 6.0;
    double rotation = 0.0;
    private GeometryFactory geometryFactory = new GeometryFactory();
    /** Creates a new instance of DefaultMark */
    public DefaultMark() {
        _log.info("creating defaultMark");
    }
    public DefaultMark(String name){
        _log.info("creating "+name+" type mark");
        setWellKnownName(name);
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
    public void setWellKnownName(java.lang.String wellKnownName) {
        _log.debug("setting WellKnowName to "+wellKnownName);
        for(int i = 0;i< WellKnownNames.length; i++){
            if(wellKnownName.equalsIgnoreCase(WellKnownNames[i])){
                this.wellKnownName = WellKnownNames[i];
                _log.debug("set WellKnowName to "+this.wellKnownName);
                return;
            }
        }
    }
    
    public void setRotation(double rotation) {
        this.rotation = rotation;
    }
    
    /** Getter for property size.
     * @return Value of property size.
     */
    public double getSize() {
        return size;
    }
    
    /** Getter for property rotation.
     * @return Value of property rotation.
     */
    public double getRotation() {
        return rotation;
    }
    private static String[] WellKnownNames = {"Square","Circle","Cross","Triangle","Star","X"};
}
