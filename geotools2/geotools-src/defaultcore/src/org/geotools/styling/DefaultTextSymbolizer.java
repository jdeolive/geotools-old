/*
 * DefaultTextSymbolizer.java
 *
 * Created on 03 July 2002, 12:42
 */

package org.geotools.styling;

import org.geotools.filter.*;
/**
 *
 * @author  iant
 */
public class DefaultTextSymbolizer implements TextSymbolizer {
    DefaultFill fill = new DefaultFill();
    Font font = new DefaultFont();
    Halo halo = new DefaultHalo();
    LabelPlacement labelPlacement = new DefaultPointPlacement();
    String geometryPropertyName = null;
    Expression label = null;
    /** Creates a new instance of DefaultTextSymbolizer */
    public DefaultTextSymbolizer() {
        fill.setColor("#000000"); // default text fill is black
    }
    
    /**
     * This property defines the geometry to be used for styling.<br>
     * The property is optional and if it is absent (null) then the "default"
     * geometry property of the feature should be used.
     *
     * Geometry types other than inherently point types can be used.
     *
     * The geometryPropertyName is the name of a geometry property in the
     * Feature being styled.  Typically, features only have one geometry so,
     * in general, the need to select one is not required.
     *
     * Note: this moves a little away from the SLD spec which provides an XPath
     * reference to a Geometry object, but does follow it in spirit.
     *
     * @return String The name of the attribute in the feature being styled
     * that should be used.  If null then the default geometry should be used.
     */
    public String geometryPropertyName() {
        return geometryPropertyName;
    }
    
    /** return the fill to be used to fill the text when rendered.
     */
    public Fill getFill() {
        return fill;
    }
    
    /** Setter for property fill.
     * @param fill New value of property fill.
     */
    public void setFill(org.geotools.styling.DefaultFill fill) {
        this.fill = fill;
    }
    /**
     * returns a device independent Font object that is tobe used to render the
     * label
     */
    public Font getFont() {
        return font;
    }
    /** Setter for property font.
     * @param font New value of property font.
     */
    public void setFont(org.geotools.styling.Font font) {
        this.font = font;
    }
    /** A halo fills an extended area outside the glyphs of a rendered text label
     * to make the label easier to read over a background.
     */
    public Halo getHalo() {
        return halo;
    }
    /** Setter for property halo.
     * @param halo New value of property halo.
     */
    public void setHalo(org.geotools.styling.Halo halo) {
        this.halo = halo;
    }
    /**
     * returns the label expression
     */
    public Expression getLabel() {
        return label;
    }
    /** Setter for property label
     * @param label New alue of property label
     */
    public void setLabel(Expression label){
        this.label = label;
    }
    
    /**
     * A pointPlacement specifies how a text element should be rendered relative
     * to its geometric point
     */
    public LabelPlacement getLabelPlacement() {
        return labelPlacement;
    }
    
    /** Setter for property labelPlacement.
     * @param labelPlacement New value of property labelPlacement.
     */
    public void setLabelPlacement(org.geotools.styling.LabelPlacement labelPlacement) {
        this.labelPlacement = labelPlacement;
    }
    
    /** Getter for property geometryPropertyName.
     * @return Value of property geometryPropertyName.
     */
    public java.lang.String getGeometryPropertyName() {
        return geometryPropertyName;
    }
    
    /** Setter for property geometryPropertyName.
     * @param geometryPropertyName New value of property geometryPropertyName.
     */
    public void setGeometryPropertyName(java.lang.String geometryPropertyName) {
        this.geometryPropertyName = geometryPropertyName;
    }
    
}
