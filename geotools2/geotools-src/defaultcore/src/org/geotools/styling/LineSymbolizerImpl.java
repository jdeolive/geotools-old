/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
 */
package org.geotools.styling;

/**
 * Provides a representation of a LineSymbolizer in an SLD Document.  A
 * LineSymbolizer defines how a line geometry should be rendered.
 *
 * @author James Macgill
 * @version $Id: LineSymbolizerImpl.java,v 1.13 2003/08/20 21:13:46 cholmesny Exp $
 */
public class LineSymbolizerImpl implements LineSymbolizer, Cloneable {
    private Stroke stroke = null;
    private String geometryName = null;

    /**
     * Creates a new instance of DefaultLineSymbolizer
     */
    protected LineSymbolizerImpl() {
    }

    /**
     * This property defines the geometry to be used for styling.<br>
     * The property is optional and if it is absent (null) then the "default"
     * geometry property of the feature should be used. Geometry types other
     * than inherently linear types can be used. If a point geometry is used,
     * it should be interpreted as a line of zero length and two end caps.  If
     * a polygon is used (or other "area" type) then its closed outline should
     * be used as the line string (with no end caps). The geometryPropertyName
     * is the name of a geometry property in the Feature being styled.
     * Typically, features only have one geometry so, in general, the need to
     * select one is not required. Note: this moves a little away from the SLD
     * spec which provides an XPath reference to a Geometry object, but does
     * follow it in spirit.
     *
     * @return String The name of the attribute in the feature being styled
     *         that should be used.  If null then the default geometry should
     *         be used.
     */
    public String geometryPropertyName() {
        return geometryName;
    }

    /**
     * Sets the GeometryPropertyName.
     *
     * @param name The name of the geometryProperty.
     *
     * @see #LineSymbolizerImpl.geometryPropertyName()
     */
    public void setGeometryPropertyName(String name) {
        geometryName = name;
    }

    /**
     * Provides the graphical-symbolization parameter to use for the linear
     * geometry.
     *
     * @return The Stroke style to use when rendering lines.
     */
    public Stroke getStroke() {
        return stroke;
    }

    /**
     * Sets the graphical-symbolization parameter to use for the linear
     * geometry.
     *
     * @param stroke The Stroke style to use when rendering lines.
     */
    public void setStroke(Stroke stroke) {
        this.stroke = stroke;
    }

    /**
     * Accepts a StyleVisitor to perform some operation on this LineSymbolizer.
     *
     * @param visitor The visitor to accept.
     */
    public void accept(StyleVisitor visitor) {
        visitor.visit(this);
    }

    /**
     * Creates a deep copy clone.
     *
     * @return The deep copy clone.
     */
    public Object clone() {
        LineSymbolizerImpl clone;

        try {
            clone = (LineSymbolizerImpl) super.clone();

            if (stroke != null) {
                clone.stroke = (Stroke) stroke.clone();
            }
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e); // this should never happen.
        }

        return clone;
    }

    /**
     * Generates a hashcode for the LineSymbolizerImpl.
     *
     * @return A hashcode.
     */
    public int hashCode() {
        final int PRIME = 1000003;
        int result = 0;

        if (stroke != null) {
            result = (PRIME * result) + stroke.hashCode();
        }

        if (geometryName != null) {
            result = (PRIME * result) + geometryName.hashCode();
        }

        return result;
    }

    /**
     * Compares this LineSymbolizerImpl with another for  equality.
     * 
     * <p>
     * Two LineSymbolizerImpls are equal if they have the same
     * geometryPropertyName and the same stroke.
     * </p>
     *
     * @param oth The other LineSymbolizerImpl
     *
     * @return True if this and oth are equal.
     */
    public boolean equals(Object oth) {
        if (this == oth) {
            return true;
        }

        if (oth == null) {
            return false;
        }

        if (oth.getClass() != getClass()) {
            return false;
        }

        LineSymbolizerImpl other = (LineSymbolizerImpl) oth;

        if (this.geometryName == null) {
            if (other.geometryName != null) {
                return false;
            }
        } else {
            if (!this.geometryName.equals(other.geometryName)) {
                return false;
            }
        }

        if (this.stroke == null) {
            if (other.stroke != null) {
                return false;
            }
        } else {
            if (!this.stroke.equals(other.stroke)) {
                return false;
            }
        }

        return true;
    }
}
