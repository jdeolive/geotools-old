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
import org.geotools.filter.Expression;


/**
 * @version $Id: LinePlacementImpl.java,v 1.3 2002/10/24 16:54:40 ianturton Exp $
 * @author Ian Turton, CCG
 */
public class LinePlacementImpl implements LinePlacement {

    /**
     * The logger for the default core module.
     */
    private static final Logger LOGGER = Logger.getLogger("org.geotools.core");
    private static final org.geotools.filter.FilterFactory filterFactory = org.geotools.filter.FilterFactory.createFilterFactory();
    private Expression perpendicularOffset = null;
    
    /** Creates a new instance of DefaultLinePlacement */
    public LinePlacementImpl() {
        try {
            perpendicularOffset = filterFactory.createLiteralExpression(new Integer(0));
        } catch (org.geotools.filter.IllegalFilterException ife){
            LOGGER.severe("Failed to build defaultLinePlacement: " + ife);
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
