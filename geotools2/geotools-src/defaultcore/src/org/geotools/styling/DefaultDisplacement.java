/*
 * DefaultDisplacement.java
 *
 * Created on 03 July 2002, 13:19
 */

package org.geotools.styling;
import org.geotools.filter.*;
/**
 *
 * @author  iant
 */
public class DefaultDisplacement implements Displacement {
    private static org.apache.log4j.Logger _log =
    org.apache.log4j.Logger.getLogger(DefaultDisplacement.class);    
    private Expression displacementX = null;
    private Expression displacementY = null;
    /** Creates a new instance of DefaultDisplacement */
    public DefaultDisplacement() {
        try{
            displacementX = new org.geotools.filter.ExpressionLiteral(new Integer(0));
            displacementY = new org.geotools.filter.ExpressionLiteral(new Integer(0));
        } catch (org.geotools.filter.IllegalFilterException ife){
            _log.fatal("Failed to build defaultDisplacement: "+ife);
            System.err.println("Failed to build defaultDisplacement: "+ife);
        }
    }
 
    /** Setter for property displacementX.
     * @param displacementX New value of property displacementX.
     */
    public void setDisplacementX(Expression displacementX) {
        this.displacementX = displacementX;
    }
    
    /** Setter for property displacementY.
     * @param displacementY New value of property displacementY.
     */
    public void setDisplacementY(Expression displacementY) {
        this.displacementY = displacementY;
    }
    
    /** Getter for property displacementX.
     * @return Value of property displacementX.
     */
    public Expression getDisplacementX() {
        return displacementX;
    }
    
    /** Getter for property displacementY.
     * @return Value of property displacementY.
     */
    public Expression getDisplacementY() {
        return displacementY;
    }
    
}
