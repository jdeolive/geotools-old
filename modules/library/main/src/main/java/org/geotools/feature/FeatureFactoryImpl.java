package org.geotools.feature;

import java.util.Collection;
import java.util.List;

import org.geotools.feature.simple.SimpleFeatureImpl;
import org.opengis.feature.Association;
import org.opengis.feature.Attribute;
import org.opengis.feature.ComplexAttribute;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureFactory;
import org.opengis.feature.GeometryAttribute;
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
	        AttributeDescriptor decsriptor, String id) {
	    return new SimpleFeatureImpl(value,decsriptor,id);
	}
	
	public SimpleFeature createSimpleFeature(List<Attribute> value,
	        SimpleFeatureType type, String id) {
	    return new SimpleFeatureImpl(value,type,id);
	}
   
}

