/*
 * DefaultLinePlacement.java
 *
 * Created on 03 July 2002, 13:00
 */

package org.geotools.styling;

import org.geotools.filter.Expression;

/**
 *
 * @author  iant
 */
public class DefaultLinePlacement implements LinePlacement {
    private static org.apache.log4j.Logger _log =
    org.apache.log4j.Logger.getLogger(DefaultLinePlacement.class);
    
    private Expression perpendicularOffset = null;
    
    /** Creates a new instance of DefaultLinePlacement */
    public DefaultLinePlacement() {
        try{
            perpendicularOffset = new org.geotools.filter.ExpressionLiteral(new Integer(0));
        } catch (org.geotools.filter.IllegalFilterException ife){
            _log.fatal("Failed to build defaultLinePlacement: "+ife);
            System.err.println("Failed to build defaultLinePlacement: "+ife);
        }
    }
    
    /** Getter for property perpendicularOffset.
     * @return Value of property perpendicularOffset.
     */
    public Expression getPerpendicularOffset() {
        return perpendicularOffset;
    }
    
    /** Setter for property perpendicularOffset.
     * @param perpendicularOffset New value of property perpendicularOffset.
     */
    public void setPerpendicularOffset(Expression perpendicularOffset) {
        this.perpendicularOffset = perpendicularOffset;
    }
    
}
