/*
 * DefaultAnchorPoint.java
 *
 * Created on 03 July 2002, 13:16
 */

package org.geotools.styling;

import org.geotools.filter.*;
/**
 *
 * @author  iant
 */
public class DefaultAnchorPoint implements AnchorPoint {
    private static org.apache.log4j.Logger _log =
    org.apache.log4j.Logger.getLogger(DefaultAnchorPoint.class);    
    private Expression anchorPointX = null;
    private Expression anchorPointY = null;
    /** Creates a new instance of DefaultAnchorPoint */
    public DefaultAnchorPoint() {
        try{
            anchorPointX = new org.geotools.filter.ExpressionLiteral(new Integer(0));
            anchorPointY = new org.geotools.filter.ExpressionLiteral(new Integer(0));
        } catch (org.geotools.filter.IllegalFilterException ife){
            _log.fatal("Failed to build defaultAnchorPoint: "+ife);
            System.err.println("Failed to build defaultAnchorPoint: "+ife);
        }
    }
    
    /** Getter for property anchorPointX.
     * @return Value of property anchorPointX.
     */
    public org.geotools.filter.Expression getAnchorPointX() {
        return anchorPointX;
    }
    
    /** Setter for property anchorPointX.
     * @param anchorPointX New value of property anchorPointX.
     */
    public void setAnchorPointX(org.geotools.filter.Expression anchorPointX) {
        this.anchorPointX = anchorPointX;
    }
    
    /** Getter for property anchorPointY.
     * @return Value of property anchorPointY.
     */
    public org.geotools.filter.Expression getAnchorPointY() {
        return anchorPointY;
    }
    
    /** Setter for property anchorPointY.
     * @param anchorPointY New value of property anchorPointY.
     */
    public void setAnchorPointY(org.geotools.filter.Expression anchorPointY) {
        this.anchorPointY = anchorPointY;
    }
    
}
