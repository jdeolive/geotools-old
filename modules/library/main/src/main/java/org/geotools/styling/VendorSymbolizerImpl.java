/*
 *    GeoTools - The Open Source Java GIS Toolkit
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


import java.util.HashMap;
import java.util.Map;

import javax.measure.quantity.Length;
import javax.measure.unit.Unit;

import org.geotools.util.Utilities;
import org.opengis.filter.expression.Expression;
import org.opengis.style.StyleVisitor;


/**
 * ExtensioSymbolizer capturing a vendor specific extension.
 * <p>
 * This is a default placeholder to record a vendor specific extension; in case an implementation
 * could not be found on the classpath.
 * 
 * @author James Macgill, CCG
 * @author Johann Sorel (Geomatys)
 * @source $URL: http://svn.osgeo.org/geotools/trunk/modules/library/main/src/main/java/org/geotools/styling/PolygonSymbolizerImpl.java $
 * @version $Id: PolygonSymbolizerImpl.java 33833 2009-09-04 12:26:28Z jive $
 */
public class VendorSymbolizerImpl implements ExtensionSymbolizer {
    
    private DescriptionImpl description;
    private String name;
    private Unit<Length> uom;
    private String geometryName = null;
    private String extensionName;
    private Map<String, Expression> parameters = new HashMap<String, Expression>();

    /**
     * Creates a new instance of DefaultPolygonStyler
     */
    protected VendorSymbolizerImpl() {
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public Description getDescription() {
        return description;
    }
    
    public void setDescription(org.opengis.style.Description description) {
        this.description = DescriptionImpl.cast( description );
    }
    
    /**
     * This property defines the geometry to be used for styling.<br>
     * The property is optional and if it is absent (null) then the "default"
     * geometry property of the feature should be used. Geometry types other
     * than inherently area types can be used. If a line is used then the line
     * string is closed for filling (only) by connecting its end point to its
     * start point. The geometryPropertyName is the name of a geometry
     * property in the Feature being styled.  Typically, features only have
     * one geometry so, in general, the need to select one is not required.
     * Note: this moves a little away from the SLD spec which provides an
     * XPath reference to a Geometry object, but does follow it in spirit.
     *
     * @return The name of the attribute in the feature being styled  that
     *         should be used.  If null then the default geometry should be
     *         used.
     */
    public String getGeometryPropertyName() {
        return geometryName;
    }

    /**
     * Sets the GeometryPropertyName.
     *
     * @param name The name of the GeometryProperty.
     *
     * @see #PolygonSymbolizerImpl.geometryPropertyName()
     */
    public void setGeometryPropertyName(String name) {
        geometryName = name;
    }

    public Unit<Length> getUnitOfMeasure() {
        return uom;
    }

    public void setUnitOfMeasure(Unit<Length> uom) {
        this.uom = uom;
    }


    /**
     * Generates a hashcode for the PolygonSymbolizerImpl.
     *
     * @return A hashcode.
     */
    public int hashCode() {
        final int PRIME = 1000003;
        int result = 0;

        if (geometryName != null) {
            result = (PRIME * result) + geometryName.hashCode();
        }
        
        if (description != null) {
            result = (PRIME * result) + description.hashCode();
        }
        
        if (uom != null) {
            result = (PRIME * result) + uom.hashCode();
        }
                
        return result;
    }

    /**
     * Compares this PolygonSymbolizerImpl with another.
     * 
     * <p>
     * Two PolygonSymbolizerImpls are equal if they have the same
     * geometryProperty, fill and stroke.
     * </p>
     *
     * @param oth the object to compare against.
     *
     * @return true if oth is equal to this object.
     */
    public boolean equals(Object oth) {
        if (this == oth) {
            return true;
        }

        if (oth instanceof VendorSymbolizerImpl) {
            VendorSymbolizerImpl other = (VendorSymbolizerImpl) oth;

            return Utilities.equals(this.geometryName,
                other.geometryName)
            && Utilities.equals(description, other.description)
            && Utilities.equals(uom, other.uom);
        }

        return false;
    }

    static VendorSymbolizerImpl cast(org.opengis.style.Symbolizer symbolizer) {
        if( symbolizer == null ){
            return null;
        }
        else if (symbolizer instanceof VendorSymbolizerImpl){
            return (VendorSymbolizerImpl) symbolizer;
        }
        else if( symbolizer instanceof org.opengis.style.ExtensionSymbolizer ){
            org.opengis.style.ExtensionSymbolizer extensionSymbolizer = (org.opengis.style.ExtensionSymbolizer) symbolizer;
            VendorSymbolizerImpl copy = new VendorSymbolizerImpl();
            copy.setDescription( extensionSymbolizer.getDescription() );
            copy.setGeometryPropertyName( extensionSymbolizer.getGeometryPropertyName());
            copy.setName(extensionSymbolizer.getName());
            copy.setUnitOfMeasure( extensionSymbolizer.getUnitOfMeasure());
            
            return copy;
        }
        else {
            return null; // not possible
        }
    }

    public String getExtensionName() {
        return extensionName;
    }

    public Map<String, Expression> getParameters() {
        return parameters;
    }

    public void setExtensionName(String name) {
        this.extensionName = name;
    }

    public Object accept(StyleVisitor visitor, Object data) {
        return visitor.visit( this, data );
    }

    public void accept(org.geotools.styling.StyleVisitor visitor) {
        visitor.visit(this);
    }

}
