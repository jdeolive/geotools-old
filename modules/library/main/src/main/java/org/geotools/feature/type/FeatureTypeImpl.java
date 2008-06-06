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
package org.geotools.feature.type;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.geotools.resources.Utilities;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.GeometryType;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.InternationalString;

/**
 * 
 * Base implementation of FeatureType.
 * 
 * @author gabriel
 *
 */
public class FeatureTypeImpl extends ComplexTypeImpl implements FeatureType {
	
	protected GeometryDescriptor defaultGeometry;

	public FeatureTypeImpl(
		Name name, Collection<PropertyDescriptor> schema, GeometryDescriptor defaultGeometry, 
		boolean isAbstract, List<Filter> restrictions, AttributeType superType, 
		InternationalString description
	) {
		super(name, schema, true, isAbstract, restrictions, superType, description);
		this.defaultGeometry = defaultGeometry;
		
		if ( defaultGeometry != null && 
		        !(defaultGeometry.getType() instanceof GeometryType ) )  {
		    throw new IllegalArgumentException( "defaultGeometry must have a GeometryType");
		}
        
	}

	public CoordinateReferenceSystem getCRS() {
	    if ( defaultGeometry != null && defaultGeometry.getType().getCRS() != null) {
	        return defaultGeometry.getType().getCRS();
	    }
		for( Iterator<PropertyDescriptor> p = properties.iterator(); p.hasNext(); ) {
		    PropertyDescriptor property = p.next();
		    if ( property instanceof GeometryDescriptor ) {
		        GeometryDescriptor geometry = (GeometryDescriptor) property;
		        if ( geometry.getType().getCRS() != null ) {
		            return geometry.getType().getCRS();
		        }
		    }
		}
		
		return null;
	}
	
	public GeometryDescriptor getDefaultGeometry() {
	    if (defaultGeometry == null) {
            for (Iterator<PropertyDescriptor> p = properties.iterator(); p.hasNext();) {
                PropertyDescriptor property = p.next();
                if (property instanceof GeometryDescriptor ) {
                    defaultGeometry = (GeometryDescriptor) property; 
                    break;
                }
            }
        }
        return defaultGeometry;
	}
	
	public boolean equals(Object o) {
		if(!(o instanceof FeatureType)){
    		return false;
    	}
    	if(!super.equals(o)){
    		return false;
    	}
    	
    	FeatureType other = (FeatureType) o;
    	if (!Utilities.equals( defaultGeometry, other.getDefaultGeometry())) {
    		return false;
    	}
    	
    	return true;
	}
	
	public int hashCode() {
		int hashCode = super.hashCode();
		
		if ( defaultGeometry != null ) {
			hashCode = hashCode ^ defaultGeometry.hashCode();
		}
		
		return hashCode;
	}
}
