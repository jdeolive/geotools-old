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
 * @version $Id: HaloImpl.java,v 1.1 2002/10/14 14:16:18 ianturton Exp $
 * @author Ian Turton, CCG
 */
public class HaloImpl implements Halo {

    /**
     * The logger for the default core module.
     */
    private static final Logger LOGGER = Logger.getLogger("org.geotools.core");

    private FillImpl fill = new FillImpl();
    private Expression radius = null;
    /** Creates a new instance of DefaultHalo */
    public HaloImpl() {
        try {
            radius = new org.geotools.filter.ExpressionLiteral(new Integer(1));
        } catch (org.geotools.filter.IllegalFilterException ife){
            LOGGER.severe("Failed to build defaultHalo: " + ife);
        }
        fill.setColor("#FFFFFF"); // default halo is white
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
    public void setFill(org.geotools.styling.FillImpl fill) {
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
