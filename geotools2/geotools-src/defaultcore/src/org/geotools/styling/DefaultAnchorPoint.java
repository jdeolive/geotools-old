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

import org.geotools.filter.*;
/**
 * @version $Id: DefaultAnchorPoint.java,v 1.3 2002/07/11 18:09:02 loxnard Exp $
 * @author Ian Turton, CCG
 */
public class DefaultAnchorPoint implements AnchorPoint {
    private static org.apache.log4j.Logger _log =
    org.apache.log4j.Logger.getLogger(DefaultAnchorPoint.class);    
    private Expression anchorPointX = null;
    private Expression anchorPointY = null;
    /** Creates a new instance of DefaultAnchorPoint */
    public DefaultAnchorPoint() {
        try {
            anchorPointX = new org.geotools.filter.ExpressionLiteral(new Double(0.0));
            anchorPointY = new org.geotools.filter.ExpressionLiteral(new Double(0.5));
        } catch (org.geotools.filter.IllegalFilterException ife){
            _log.fatal("Failed to build defaultAnchorPoint: " + ife);
            System.err.println("Failed to build defaultAnchorPoint: " + ife);
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
