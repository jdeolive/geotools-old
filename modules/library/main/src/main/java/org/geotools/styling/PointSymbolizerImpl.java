/*
 *    GeoTools - The Open Source Java GIS Tookit
 *    http://geotools.org
 * 
 *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
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
 */
package org.geotools.styling;


// OpenGIS dependencies
import org.geotools.resources.Utilities;
import org.opengis.util.Cloneable;


/**
 * Provides a Java representation of the PointSymbolizer. This defines how
 * points are to be rendered.
 *
 * @author Ian Turton, CCG
 * @source $URL$
 * @version $Id$
 */
public class PointSymbolizerImpl implements PointSymbolizer, Cloneable {
    private String geometryPropertyName = null;
    private Graphic graphic = new GraphicImpl();

    /**
     * Creates a new instance of DefaultPointSymbolizer
     */
    protected PointSymbolizerImpl() {
    }

    public String getGeometryPropertyName() {
        return geometryPropertyName;
    }

    /**
     * Sets the Geometry Property Name.
     *
     * @param name The Geometry Property Name.
     */
    public void setGeometryPropertyName(String name) {
        geometryPropertyName = name;
    }

    /**
     * Provides the graphical-symbolization parameter to use for the point
     * geometry.
     *
     * @return The Graphic to be used when drawing a point
     */
    public Graphic getGraphic() {
        return graphic;
    }

    /**
     * Setter for property graphic.
     *
     * @param graphic New value of property graphic.
     */
    public void setGraphic(Graphic graphic) {
        if (this.graphic == graphic) {
            return;
        }
        this.graphic = graphic;
    }

    /**
     * Accept a StyleVisitor to perform an operation on this symbolizer.
     *
     * @param visitor The StyleVisitor to accept.
     */
    public void accept(StyleVisitor visitor) {
        visitor.visit(this);
    }

    /**
     * Creates a deep copy clone.
     *
     * @return The deep copy clone.
     *
     * @throws RuntimeException DOCUMENT ME!
     */
    public Object clone() {
        PointSymbolizerImpl clone;

        try {
            clone = (PointSymbolizerImpl) super.clone();
            clone.graphic = (Graphic) ((Cloneable) graphic).clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e); // this should never happen.
        }

        return clone;
    }

    /**
     * Generates the hashcode for the PointSymbolizer
     *
     * @return the hashcode
     */
    public int hashCode() {
        final int PRIME = 1000003;
        int result = 0;

        if (geometryPropertyName != null) {
            result = (PRIME * result) + geometryPropertyName.hashCode();
        }

        if (graphic != null) {
            result = (PRIME * result) + graphic.hashCode();
        }

        return result;
    }

    /**
     * Checks this PointSymbolizerImpl with another for equality.
     * 
     * <p>
     * Two PointSymbolizers are equal if the have the same geometry property
     * name and their graphic object is equal.
     * </p>
     * 
     * <p>
     * Note: this method only works for other instances of PointSymbolizerImpl,
     * not other implementors of PointSymbolizer
     * </p>
     *
     * @param oth The object to compare with this PointSymbolizerImpl for
     *        equality.
     *
     * @return True of oth is a PointSymbolizerImpl that is equal.
     */
    public boolean equals(Object oth) {
        if (this == oth) {
            return true;
        }

        if (oth instanceof PointSymbolizerImpl) {
            PointSymbolizerImpl other = (PointSymbolizerImpl) oth;

            return Utilities.equals(geometryPropertyName,
                other.geometryPropertyName)
            && Utilities.equals(graphic, other.graphic);
        }

        return false;
    }
}
