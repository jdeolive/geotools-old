/*
 * DefaultGraphic.java
 *
 * Created on 23 May 2002, 14:56
 */

package org.geotools.styling;
import java.util.*;

/**
 *
 * @author  iant
 */
public class DefaultGraphic implements org.geotools.styling.Graphic {
    ArrayList externalGraphics = new ArrayList();
    ArrayList marks = new ArrayList();
    private static org.apache.log4j.Category _log = 
        org.apache.log4j.Category.getInstance("defaultcore.styling");
    double opacity = 1.0;
    double size = 6;
    double rotation = 0.0;
    
    /** Creates a new instance of DefaultGraphic */
    public DefaultGraphic() {
        
    }
    
    public DefaultGraphic(String extgraphic){
        ExternalGraphic eg = new DefaultExternalGraphic(extgraphic);
        externalGraphics.add(eg);
    }
    /**
     * Privides a list of external graphics which can be used to represent
     * this graphic.
     * Each one should be an equivelent representation but in a
     * different format.
     * If none are provied, or if none of the formats are supported then the
     * list of Marks should be used instead.
     *
     * @return An array of ExternalGraphics objects which should be equivlents
     *        but in different formats.  If null is returned use
     *        getMarks instead.
     */
    public ExternalGraphic[] getExternalGraphics() {
        if(externalGraphics.size()>0){
            return (ExternalGraphic[])externalGraphics.toArray(new ExternalGraphic[0]);
        }else{
            return null;
        }
    }
    
    public void addExternalGraphic(ExternalGraphic g){
        externalGraphics.add(g);
    }
    
    /**
     * Provides a list of suitible marks which can be used to represent this
     * graphic.
     * These should only be used if not ExternalGraphic is provided, or if none
     * of the external graphics formats are supported.
     *
     * @return An array of marks to use when displaying this Graphic.
     * By default a "square" with 50% gray fill and black outline with a size
     * of 6 pixels (unless a size is specifed) is provided.
     */
    public Mark[] getMarks() {
        if(marks.size()>0){
            return (Mark[])marks.toArray(new Mark[0]);
        }else{
            return new Mark[]{new DefaultMark()};
        }
    }
    
    public void addMark(DefaultMark m){
        if(m == null) return;
        marks.add(m);
        m.setSize(size);
    }
        
    /**
     * This specifies the level of translucency to use when rendering the graphic.<br>
     * The value is encoded as a floating-point value between 0.0 and 1.0 with 0.0
     * representing totaly transparent and 1.0 representing totaly opaque, with a linear scale of
     * translucency for intermediate values.<br>
     * For example, "0.65" would represent 65% opacity.  The default value is 1.0 (opaque)
     *
     * @return The opacity of the Graphic, where 0.0 is completly transparent and 1.0 is completly opaque.
     */
    public double getOpacity() {
        return opacity;
    }
    
    /**
     * This parameter defines the rotation of a graphic in the clockwise direction about its center point in decimal degrees.  The value encoded as a floating point number.
     *
     * @return The angle of rotation in decimal degrese, Negative values represent counter-clockwise rotation.  The default is 0.0 (no rotation)
     */
    public double getRotation() {
        return rotation;
    }
    
    /**
     * This paramteter gives the absolute size of teh graphic in pixels encoded as a floating point number.<p>
     * The default size of an image format (such as GIFD) is the inferent size of the image.  The default size of a format
     * without an inherent size (such as SVG) is deffined to be 16 pixels in height and the corresponding aspect in width.
     * If a size is specifed, the height of the graphic will be scaled to that size and the corresponding aspect will be used for the width.
     *
     * @return The size of the graphic, the default is context specific.  Negative values are not possible.
     */
    public double getSize() {
        return size;
    }
    
    /** Setter for property opacity.
     * @param opacity New value of property opacity.
     */
    public void setOpacity(double opacity) {
        this.opacity = opacity;
    }
    
    /** Setter for property rotation.
     * @param rotation New value of property rotation.
     */
    public void setRotation(double rotation) {
        this.rotation = rotation;
        Iterator i = marks.iterator();
        while(i.hasNext()){
            ((DefaultMark)i.next()).setRotation(rotation);
        }
    }
    
    /** Setter for property size.
     * @param size New value of property size.
     */
    public void setSize(double size) {
        this.size = size;
        Iterator i = marks.iterator();
        while(i.hasNext()){
            ((DefaultMark)i.next()).setSize(size);
        }
    }
    
}
