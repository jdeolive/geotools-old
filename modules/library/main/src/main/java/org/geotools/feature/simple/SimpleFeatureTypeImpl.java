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
package org.geotools.feature.simple;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.geotools.feature.NameImpl;
import org.geotools.feature.type.Descriptors;
import org.geotools.feature.type.FeatureTypeImpl;
import org.geotools.feature.type.Types;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.GeometryType;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.InternationalString;

/**
 * Implementation fo SimpleFeatureType, subtypes must be atomic and are stored
 * in a list.
 * 
 * @author Justin
 */
public class SimpleFeatureTypeImpl extends FeatureTypeImpl implements
		SimpleFeatureType {

	// list of types
	List<AttributeType> types = null;

	public SimpleFeatureTypeImpl(Name name, List<AttributeDescriptor> schema,
			GeometryDescriptor defaultGeometry, boolean isAbstract, 
			List<Filter> restrictions, AttributeType superType, InternationalString description) {
		super(name, (List) schema, defaultGeometry, isAbstract, restrictions, superType,
				description);
	}
	
	
	public List<AttributeDescriptor> getAttributes() {
		return (List) getProperties();
	}
	
	public List<AttributeType> getTypes() {
      if (types == null) {
          synchronized (this) {
              if (types == null) {
                  types = new ArrayList<AttributeType>();
                  for (Iterator<AttributeDescriptor> itr = getAttributes().iterator(); itr.hasNext();) {
                      AttributeDescriptor ad = itr.next();
                      types.add(ad.getType());
                  }
              }
          }
      }

      return types;
	}
	
	public AttributeType getType(Name name) {
	    AttributeDescriptor attribute = (AttributeDescriptor) getProperty(name);
	    if ( attribute != null ) {
	        return attribute.getType();
	    }
	    
	    return null;
	}
	public AttributeType getType(String name) {
	    AttributeDescriptor attribute = (AttributeDescriptor) getProperty(name);
        if ( attribute != null ) {
            return attribute.getType();
        }
        
        return null;
    }
	
	public AttributeType getType(int index) {
        AttributeDescriptor attribute = getAttribute( index );
        return attribute.getType();
    }
	
	public AttributeDescriptor getAttribute(Name name) {
		return (AttributeDescriptor) getProperty( name );
	}

	public AttributeDescriptor getAttribute(String name) {
	    return (AttributeDescriptor) getProperty( name );
	}

	public AttributeDescriptor getAttribute(int index) {
		return getAttributes().get(index);
	}
	
	public int indexOf(Name name) {
	    int index = 0;
        for (Iterator<AttributeDescriptor> itr = getAttributes().iterator(); itr.hasNext(); index++) {
            AttributeDescriptor descriptor = (AttributeDescriptor) itr.next();
            if (descriptor.getName().equals(name)) {
                return index;
            }
        }
        return -1;
	}
	
	public int indexOf(String name) {
		return indexOf( new NameImpl(name));
	}

	public int getAttributeCount() {
		return properties.size();
	}


	public String getTypeName() {
		return getName().getLocalPart();
	}
	
}
