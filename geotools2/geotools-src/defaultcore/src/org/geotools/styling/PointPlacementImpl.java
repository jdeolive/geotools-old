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

import org.geotools.util.Cloneable;
import org.geotools.util.EqualsUtils;

/**
 * @version $Id: PointPlacementImpl.java,v 1.10 2003/09/06 04:52:31 seangeo Exp $
 * @author Ian Turton, CCG
 */
public class PointPlacementImpl implements PointPlacement, Cloneable {
    /**
     * The logger for the default core module.
     */
    private static final java.util.logging.Logger LOGGER = 
            java.util.logging.Logger.getLogger("org.geotools.core");
    private static final org.geotools.filter.FilterFactory filterFactory = 
            org.geotools.filter.FilterFactory.createFilterFactory();
    private AnchorPoint anchorPoint = new AnchorPointImpl();
    private Displacement displacement = new DisplacementImpl();
    private org.geotools.filter.Expression rotation = null;

    /** Creates a new instance of DefaultPointPlacement */
    public PointPlacementImpl() {
        try {
            rotation = filterFactory.createLiteralExpression(new Integer(0));
        } catch (org.geotools.filter.IllegalFilterException ife) {
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
        if (anchorPoint == null) {
            this.anchorPoint = new AnchorPointImpl();
        } else {
            this.anchorPoint = anchorPoint;
        }
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
        if (displacement == null) {
            this.displacement = new DisplacementImpl();
        } else {
            this.displacement = displacement;
        }
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
    
    public void accept(StyleVisitor visitor) {
        visitor.visit(this);
     }
    
    /* (non-Javadoc)
     * @see org.geotools.util.Cloneable#clone()
     */
    public Object clone() {
        try {
            PointPlacementImpl clone = (PointPlacementImpl) super.clone();
            clone.anchorPoint = (AnchorPoint) ((Cloneable)anchorPoint).clone();
            clone.displacement = (Displacement) ((Cloneable)displacement).clone();
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Won't happen");
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        
        if (obj instanceof PointPlacementImpl) {
            PointPlacementImpl other = (PointPlacementImpl) obj;
            return EqualsUtils.equals(anchorPoint, other.anchorPoint) &&
                EqualsUtils.equals(displacement, other.displacement) &&
                EqualsUtils.equals(rotation, other.rotation);
        }
        
        return false;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        final int PRIME = 37;
        int result = 17;
        
        if (anchorPoint != null) {
            result = result * PRIME + anchorPoint.hashCode();
        }
        
        if (displacement != null) {
            result = result * PRIME + displacement.hashCode();
        }
        
        if (rotation != null) {
            result = result * PRIME + rotation.hashCode();
        }
        
        return result;
    }

}