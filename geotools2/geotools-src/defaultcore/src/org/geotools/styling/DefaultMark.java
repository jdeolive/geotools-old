/*
 * DefaultMark.java
 *
 * Created on 28 May 2002, 15:09
 */

package org.geotools.styling;

/**
 *
 * @author  iant
 */
public class DefaultMark implements Mark {
    Fill fill = new DefaultFill();
    Stroke stroke = new DefaultStroke();
    String wellKnownName = "Square";
    /** Creates a new instance of DefaultMark */
    public DefaultMark() {
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
    
}
