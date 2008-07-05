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
package org.geotools.feature;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureImpl;
import org.opengis.feature.Association;
import org.opengis.feature.Attribute;
import org.opengis.feature.ComplexAttribute;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureFactory;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AssociationDescriptor;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.ComplexType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.geometry.coordinate.GeometryFactory;
import org.opengis.referencing.crs.CRSFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * Factory for creating instances of the Attribute family of classes.
 * 
 * @author Ian Schneider
 * @author Gabriel Roldan
 * @author Justin Deoliveira
 * 
 * @version $Id$
 */
public class FeatureFactoryImpl implements FeatureFactory {
 
	/**
	 * Factory used to create CRS objects
	 */
    CRSFactory crsFactory;
    /**
     * Factory used to create geomtries
     */
    GeometryFactory  geometryFactory;
    
    public CRSFactory getCRSFactory() {
        return crsFactory;
    }

    public void setCRSFactory(CRSFactory crsFactory) {
        this.crsFactory = crsFactory;
    }

    public GeometryFactory getGeometryFactory() {
        return geometryFactory;
    }

    public void setGeometryFactory(GeometryFactory geometryFactory) {
        this.geometryFactory = geometryFactory;
    }
    
    public Association createAssociation(Attribute related, AssociationDescriptor descriptor) {
        return new AssociationImpl(related,descriptor);
    }
	
	public Attribute createAttribute( Object value, AttributeDescriptor descriptor, String id ) {
		return new AttributeImpl(value,descriptor,id);
	}
	
	public GeometryAttribute createGeometryAttribute(
		Object value, GeometryDescriptor descriptor, String id, CoordinateReferenceSystem crs
	) {
	
		return new GeometryAttributeImpl(value,descriptor,id);
	}
	
	public ComplexAttribute createComplexAttribute( 
		Collection value, AttributeDescriptor descriptor, String id
	) {
		return new ComplexAttributeImpl(value, descriptor, id );
	}

	public ComplexAttribute createComplexAttribute( Collection value, ComplexType type, String id ) 
	{
		return new ComplexAttributeImpl(value, type, id );
	}
	
	public Feature createFeature(Collection value, AttributeDescriptor descriptor, String id) {
		return new FeatureImpl(value,descriptor,id);
	}

	public Feature createFeature(Collection value, FeatureType type, String id) {
		return new FeatureImpl(value,type,id);
	}
	
	public SimpleFeature createSimpleFeautre(List<Attribute> value,
	        AttributeDescriptor descriptor, String id) {
	    // return new SimpleFeatureImpl(value,descriptor,id);
	    return null;
	}
	
	public SimpleFeature createSimpleFeature(List<Attribute> value,
	        SimpleFeatureType type, String id) {
	    SimpleFeatureBuilder sb = new SimpleFeatureBuilder(type);
	    sb.addAll(value);
	    return sb.buildFeature(id);
	}

    public SimpleFeature createSimpleFeature(Object[] array,
            SimpleFeatureType type, String id) {
        List<Attribute> attributes = new ArrayList<Attribute>();
        for( int i=0; i<type.getAttributeCount(); i++){
            AttributeDescriptor field = type.getDescriptor(i);
            Attribute attribute = createAttribute( array[i], field, null );
            attributes.add( attribute );
        }
        return createSimpleFeature( attributes, type, id );
    }

    public SimpleFeature createSimpleFeautre(Object[] array,
            AttributeDescriptor decsriptor, String id) {
        SimpleFeatureType type = (SimpleFeatureType) decsriptor;
        List<Attribute> attributes = new ArrayList<Attribute>();
       
        for( int i=0; i<type.getAttributeCount(); i++){
            AttributeDescriptor field = type.getDescriptor(i);
            Attribute attribute = createAttribute( array[i], field, null );
            attributes.add( attribute );
        }
        return createSimpleFeautre( attributes, decsriptor, id );
    }
   
}

