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

import org.geotools.filter.Expression;

/**
 * @version $Id: DefaultLinePlacement.java,v 1.2 2002/07/11 18:20:43 loxnard Exp $
 * @author Ian Turton, CCG
 */
public class DefaultLinePlacement implements LinePlacement {
    private static org.apache.log4j.Logger _log =
    org.apache.log4j.Logger.getLogger(DefaultLinePlacement.class);
    
    private Expression perpendicularOffset = null;
    
    /** Creates a new instance of DefaultLinePlacement */
    public DefaultLinePlacement() {
        try {
            perpendicularOffset = new org.geotools.filter.ExpressionLiteral(new Integer(0));
        } catch (org.geotools.filter.IllegalFilterException ife){
            _log.fatal("Failed to build defaultLinePlacement: " + ife);
            System.err.println("Failed to build defaultLinePlacement: " + ife);
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
