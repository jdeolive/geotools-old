/*
 * DefaultPointPlacement.java
 *
 * Created on 03 July 2002, 13:08
 */

package org.geotools.styling;

import org.geotools.filter.*;
/**
 *
 * @author  iant
 */
public class DefaultPointPlacement implements PointPlacement {
    private static org.apache.log4j.Logger _log =
    org.apache.log4j.Logger.getLogger(DefaultPointPlacement.class);
    AnchorPoint anchorPoint = new DefaultAnchorPoint();
    Displacement displacement = new DefaultDisplacement();
    Expression rotation = null;
    /** Creates a new instance of DefaultPointPlacement */
    public DefaultPointPlacement() {
        try{
            rotation = new org.geotools.filter.ExpressionLiteral(new Integer(0));
        } catch (org.geotools.filter.IllegalFilterException ife){
            _log.fatal("Failed to build defaultPointPlacement: "+ife);
            System.err.println("Failed to build defaultPointPlacement: "+ife);
        }
    }
    
    /**
     * returns the  AnchorPoint which identifies the location inside a textlabel to use as an
     * "anchor" for positioning it relative to a point geometry.
     */
    public org.geotools.styling.AnchorPoint getAnchorPoint() {
        return anchorPoint;
    }
    
    /** Setter for property anchorPoint.
     * @param anchorPoint New value of property anchorPoint.
     */
    public void setAnchorPoint(org.geotools.styling.AnchorPoint anchorPoint) {
        this.anchorPoint = anchorPoint;
    }
    
    /**
     * returns the Displacement which gives X and Y offset displacements to use for rendering
     * a text label near a point.
     */
    
    public org.geotools.styling.Displacement getDisplacement() {
        return displacement;
    }
    
    /** Setter for property displacement.
     * @param displacement New value of property displacement.
     */
    public void setDisplacement(org.geotools.styling.Displacement displacement) {
        this.displacement = displacement;
    }
    
    /**
     * returns the rotation of the label
     */
    public org.geotools.filter.Expression getRotation() {
        return rotation;
    }
    
    /** Setter for property rotation.
     * @param rotation New value of property rotation.
     */
    public void setRotation(org.geotools.filter.Expression rotation) {
        this.rotation = rotation;
    }
 
}
