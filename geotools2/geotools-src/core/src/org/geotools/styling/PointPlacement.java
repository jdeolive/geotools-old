/*
 * PointPlacement.java
 *
 * Created on 03 July 2002, 12:17
 */

package org.geotools.styling;
import org.geotools.filter.Expression;
/**
 * A PointPlacement specifies how a text label is positioned relative to a geometric point
 * $Id: PointPlacement.java,v 1.1 2002/07/03 13:35:21 ianturton Exp $
 * @author  iant
 */
public interface PointPlacement extends LabelPlacement{
    /**
     * returns the  AnchorPoint which identifies the location inside a textlabel to use as an
     * "anchor" for positioning it relative to a point geometry.
     */
    public AnchorPoint getAnchorPoint();
    /**
     *returns the Displacement which gives X and Y offset displacements to use for rendering 
     * a text label near a point.
     */
    public Displacement getDisplacement();
    /** 
     * returns the rotation of the label
     */
    public Expression getRotation();
}