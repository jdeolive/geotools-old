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

// Geotools dependencies
import org.geotools.util.Cloneable;
import org.geotools.util.EqualsUtils;


/**
 * Provides a Java representation of the PointSymbolizer.
 * This defines how points are to be rendered. 
 * 
 * @version $Id: PointSymbolizerImpl.java,v 1.12 2003/09/06 04:52:31 seangeo Exp $
 * @author Ian Turton, CCG
 */
public class PointSymbolizerImpl implements PointSymbolizer, Cloneable {
    private String geometryPropertyName = null;
    private Graphic graphic = new GraphicImpl();

    /** Creates a new instance of DefaultPointSymbolizer */
    protected PointSymbolizerImpl() {
    }

    /**
     * This property defines the geometry to be used for styling.<br>
     * The property is optional and if it is absent (null) then the "default"
     * geometry property of the feature should be used.
     *
     * Geometry types other than inherently point types can be used.
     *
     * The geometryPropertyName is the name of a geometry property in the
     * Feature being styled.  Typically, features only have one geometry so,
     * in general, the need to select one is not required.
     *
     * Note: this moves a little away from the SLD spec which provides an XPath
     * reference to a Geometry object, but does follow it in spirit.
     *
     * @return String The name of the attribute in the feature being styled
     * that should be used.  If null then the default geometry should be used.
     */
    public String geometryPropertyName() {
        return geometryPropertyName;
    }

    /** Sets the Geometry Property Name.
     * 
     *  @param name The Geometry Property Name.
     */
    public void setGeometryPropertyName(String name) {
        geometryPropertyName = name;
    }

    /**
     * Provides the graphical-symbolization parameter to use for the
     * point geometry.
     *
     * @return The Graphic to be used when drawing a point
     */
    public Graphic getGraphic() {
        return graphic;
    }

    /**
     * Setter for property graphic.
     * @param graphic New value of property graphic.
     */
    public void setGraphic(org.geotools.styling.Graphic graphic) {
        this.graphic = graphic;
    }
    
    /** Accept a StyleVisitor to perform an operation
     *  on this symbolizer.
     * 
     *  @param visitor The StyleVisitor to accept.
     */
    public void accept(StyleVisitor visitor) {
        visitor.visit(this);
    }
    
    /** Creates a deep copy clone. 
     * 
     * @return The deep copy clone.
     */
    public Object clone() {
        PointSymbolizerImpl clone;
        try {
            clone = (PointSymbolizerImpl) super.clone();
            clone.graphic = (Graphic) ((Cloneable)graphic).clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e); // this should never happen.
        }
        return clone;
    }
    
    /** Generates the hashcode for the PointSymbolizer
     * 
     *  @return the hashcode
     */
    public int hashCode() {
        final int PRIME = 1000003;
        int result = 0;
        if (geometryPropertyName != null) {
            result = PRIME * result + geometryPropertyName.hashCode();
        }
        if (graphic != null) {
            result = PRIME * result + graphic.hashCode();
        }

        return result;
    }

    /** Checks this PointSymbolizerImpl with another for equality.
     * 
     *  <p>Two PointSymbolizers are equal if the have the same
     *  geometry property name and their graphic object is equal.
     * 
     *  <p>Note: this method only works for other instances of
     *  PointSymbolizerImpl, not other implementors of PointSymbolizer
     * 
     * @param oth The object to compare with this PointSymbolizerImpl
     * for equality.
     * @return True of oth is a PointSymbolizerImpl that is equal.
     * 
     */
    public boolean equals(Object oth) {
        if (this == oth) {
            return true;
        }

        if (oth instanceof PointSymbolizerImpl) {
            PointSymbolizerImpl other = (PointSymbolizerImpl) oth;
            return EqualsUtils.equals(geometryPropertyName, other.geometryPropertyName) &&
                EqualsUtils.equals(graphic, other.graphic);
        }
        
        return false;
    }

}