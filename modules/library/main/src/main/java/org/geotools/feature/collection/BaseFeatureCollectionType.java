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
package org.geotools.feature.collection;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;

import org.geotools.feature.FeatureTypes;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.NameImpl;
import org.geotools.feature.simple.SimpleFeatureTypeImpl;
import org.geotools.resources.Utilities;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.GeometryDescriptor;

/**
 * Limited implementation of SimpleFeatureCollectionType for internal use
 * by this class.
 * <p>
 * This type is completely empty in that it contains no attribute types, the only
 * goal is to cough up the correct member type.
 * </p>
 * @author Justin Deoliveira (The Open Planning Project)
 */
public class BaseFeatureCollectionType extends SimpleFeatureTypeImpl  {
    
    final SimpleFeatureType memberType;
    
	public BaseFeatureCollectionType(SimpleFeatureType memberType) {
	    super( new NameImpl( FeatureTypes.DEFAULT_NAMESPACE.toString(), "AbstractFeatureColletionType") ,
            Collections.EMPTY_LIST, null, false, Collections.EMPTY_LIST, null, null
	    );		           
	    this.memberType = memberType;
		//super( new TypeName(FeatureTypes.DEFAULT_NAMESPACE.toString(), "AbstractFeatureColletionType"), memberType, null );
	}
	
	public SimpleFeatureType getMemberType() {
	    return memberType;
	}
//	public Set getMemberTypes() {
//	    return Collections.singleton( getMemberType() );
//	}
//	public Set getMembers() {
//	    AssociationType contains = new AssociationTypeImpl( new TypeName("contains"), memberType, false, false, Collections.EMPTY_SET, null, null ); 
//	    AssociationDescriptorImpl member = new AssociationDescriptorImpl( null, new Name("member"), 0, Integer.MAX_VALUE );
//	    return Collections.singleton( member );
//	}
	public SimpleFeature create(Object[] attributes) throws IllegalAttributeException {
	    throw new UnsupportedOperationException("Types of feature collection do not support feature creation");
	}

	public SimpleFeature create(Object[] attributes, String featureID) throws IllegalAttributeException {
		throw new UnsupportedOperationException("Types of feature collection do not support feature creation");
	}

	public SimpleFeature duplicate(SimpleFeature feature) throws IllegalAttributeException {
		throw new UnsupportedOperationException("Types of feature collection do not support feature creation");
	}

	public int find(AttributeDescriptor type) {
		return find( type.getLocalName() );
	}

	public int find(String attName) {
		return indexOf(attName);
	}

	public SimpleFeatureType[] getAncestors() {
		return new SimpleFeatureType[]{};
	}

	public AttributeDescriptor getAttributeType(String xPath) {
		return (AttributeDescriptor) getAttribute(name);
	}

	public AttributeDescriptor getAttributeType(int position) {
		return (AttributeDescriptor) getAttribute(position);
	}

	public AttributeDescriptor[] getAttributeTypes() {
		return new AttributeDescriptor[]{};
	}

	public URI getNamespace() {
		try {
			return new URI(getName().getNamespaceURI());
		} 
		catch (URISyntaxException e) {
			//cant happen
			return null;
		}
	}

	public GeometryDescriptor getDefaultGeometry() {
		return (GeometryDescriptor) super.getDefaultGeometry();
	}

	public String getTypeName() {
		return getName().getLocalPart();
	}

	public boolean hasAttributeType(String xPath) {
		return indexOf(xPath) != -1;
	}

	public boolean isDescendedFrom(URI nsURI, String typeName) {
		return false;
	}

	public boolean isDescendedFrom(SimpleFeatureType type) {
		return false;
	}

	public boolean equals(Object other) {
	    if ( other instanceof BaseFeatureCollectionType ) {
	        if ( super.equals( other ) ) {
	            return Utilities.equals( memberType, ((BaseFeatureCollectionType)other).memberType);
	        }
	    }
	    
	    return false;
	}
	
	
	public int hashCode() {
	    int hash = super.hashCode();
	    if ( memberType != null ) {
	        hash ^= memberType.hashCode();
	    }
	    
	    return hash;
	}
}
