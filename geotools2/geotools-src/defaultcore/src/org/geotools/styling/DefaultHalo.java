/*
 * DefaultHalo.java
 *
 * Created on 03 July 2002, 13:23
 */

package org.geotools.styling;

import org.geotools.filter.*;
/**
 *
 * @author  iant
 */
public class DefaultHalo implements Halo {
    private static org.apache.log4j.Logger _log =
    org.apache.log4j.Logger.getLogger(DefaultHalo.class);        
    private Fill fill = new DefaultFill();
    private Expression radius = null;
    /** Creates a new instance of DefaultHalo */
    public DefaultHalo() {
        try{
            radius = new org.geotools.filter.ExpressionLiteral(new Integer(0));
        } catch (org.geotools.filter.IllegalFilterException ife){
            _log.fatal("Failed to build defaultHalo: "+ife);
            System.err.println("Failed to build defaultHalo: "+ife);
        }        
    }
    
    /** Getter for property fill.
     * @return Value of property fill.
     */
    public org.geotools.styling.Fill getFill() {
        return fill;
    }
    
    /** Setter for property fill.
     * @param fill New value of property fill.
     */
    public void setFill(org.geotools.styling.Fill fill) {
        this.fill = fill;
    }
    
    /** Getter for property radius.
     * @return Value of property radius.
     */
    public org.geotools.filter.Expression getRadius() {
        return radius;
    }
    
    /** Setter for property radius.
     * @param radius New value of property radius.
     */
    public void setRadius(org.geotools.filter.Expression radius) {
        this.radius = radius;
    }
    
}
