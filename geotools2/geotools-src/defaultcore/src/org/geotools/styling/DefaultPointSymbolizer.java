/*
 * DefaultPointSymbolizer.java
 *
 * Created on 28 May 2002, 14:54
 */

package org.geotools.styling;

/**
 *
 * @author  iant
 */
public class DefaultPointSymbolizer implements PointSymbolizer {
    String geometryPropertyName = null;
    Graphic graphic = new DefaultGraphic();
    /** Creates a new instance of DefaultPointSymbolizer */
    public DefaultPointSymbolizer() {
    }
    
    /**
     * This property defines the geometry to be used for styling.<br>
     * The property is optional and if it is absent (null) then the "default"
     * geometry property of the feature should be used.
     *
     * Geometry types other than inherently pointy types can be used.
     *
     * The geometryPropertyName is the name of a geometry property in the
     * Feature being styled typicaly features only have one geometry so in
     * general the need to select one is not required.
     *
     * Note: this moves a little away from the SLD spec which provides an XPath
     * reference is given to a Geometry object, but does follow it in spirit.
     *
     * @return String The name of the attribute in the feature being styled
     * that should be used.  If null then the default geometry should be used.
     */
    public String geometryPropertyName() {
        return geometryPropertyName;
    }
    public void setGeometryPropertyName(String name) {
        geometryPropertyName = name;
    }
        
    /**
     * Provides the graphical-symbolization parameter to use for the
     * point geometry.
     *
     * @return The Graphic to be used when drawing a point
     */
    public Graphic getGraphic() {
        return graphic;
    }
    
    /** Setter for property graphic.
     * @param graphic New value of property graphic.
     */
    public void setGraphic(org.geotools.styling.Graphic graphic) {
        this.graphic = graphic;
    }
    
}
