/*
 *    Geotools - OpenSource mapping toolkit
 *    (C) 2002, Centre for Computational Geography
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */

package org.geotools.styling;

// J2SE dependencies
import java.util.logging.Logger;

// Geotools dependencies
import org.geotools.filter.*;


/**
 * @version $Id: DisplacementImpl.java,v 1.1 2002/10/14 14:16:31 ianturton Exp $
 * @author Ian Turton, CCG
 */
public class DisplacementImpl implements Displacement {

    /**
     * The logger for the default core module.
     */
    private static final Logger LOGGER = Logger.getLogger("org.geotools.core");

    private Expression displacementX = null;
    private Expression displacementY = null;
    /** Creates a new instance of DefaultDisplacement */
    public DisplacementImpl() {
        try {
            displacementX = new org.geotools.filter.ExpressionLiteral(new Integer(0));
            displacementY = new org.geotools.filter.ExpressionLiteral(new Integer(0));
        } catch (org.geotools.filter.IllegalFilterException ife){
            LOGGER.severe("Failed to build defaultDisplacement: " + ife);
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
