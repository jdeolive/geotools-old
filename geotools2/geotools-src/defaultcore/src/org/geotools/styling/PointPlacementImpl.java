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
 * @version $Id: PointPlacementImpl.java,v 1.1 2002/10/14 14:16:30 ianturton Exp $
 * @author Ian Turton, CCG
 */
public class PointPlacementImpl implements PointPlacement {

    /**
     * The logger for the default core module.
     */
    private static final Logger LOGGER = Logger.getLogger("org.geotools.core");

    AnchorPoint anchorPoint = new AnchorPointImpl();
    Displacement displacement = new DisplacementImpl();
    Expression rotation = null;
    /** Creates a new instance of DefaultPointPlacement */
    public PointPlacementImpl() {
        try {
            rotation = new org.geotools.filter.ExpressionLiteral(new Integer(0));
        } catch (org.geotools.filter.IllegalFilterException ife){
            LOGGER.severe("Failed to build defaultPointPlacement: " + ife);
        }
    }
    
    /**
     * Returns the AnchorPoint which identifies the location inside a text
     * label to use as an "anchor" for positioning it relative to a point
     * geometry.
     * @return Label's AnchorPoint.
     */
    public org.geotools.styling.AnchorPoint getAnchorPoint() {
        return anchorPoint;
    }
    
    /**
     * Setter for property anchorPoint.
     * @param anchorPoint New value of property anchorPoint.
     */
    public void setAnchorPoint(org.geotools.styling.AnchorPoint anchorPoint) {
        this.anchorPoint = anchorPoint;
    }
    
    /**
     * Returns the Displacement which gives X and Y offset displacements
     * to use for rendering a text label near a point.
     * @return The label displacement.
     */
    
    public org.geotools.styling.Displacement getDisplacement() {
        return displacement;
    }
    
    /**
     * Setter for property displacement.
     * @param displacement New value of property displacement.
     */
    public void setDisplacement(org.geotools.styling.Displacement displacement) {
        this.displacement = displacement;
    }
    
    /**
     * Returns the rotation of the label.
     * @return The rotation of the label.
     */
    public org.geotools.filter.Expression getRotation() {
        return rotation;
    }
    
    /**
     * Setter for property rotation.
     * @param rotation New value of property rotation.
     */
    public void setRotation(org.geotools.filter.Expression rotation) {
        this.rotation = rotation;
    }
 
}
