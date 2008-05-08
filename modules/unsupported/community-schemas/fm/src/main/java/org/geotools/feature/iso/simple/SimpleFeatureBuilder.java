package org.geotools.feature.iso.simple;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.opengis.feature.Attribute;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureCollection;
import org.opengis.feature.simple.SimpleFeatureCollectionType;
import org.opengis.feature.simple.SimpleFeatureFactory;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.ComplexType;
import org.opengis.feature.type.FeatureCollectionType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.GeometryType;

/**
 * This builder will help you put together a SimpleFeature.
 * <p>
 * Since the simple feature is well simple, this class
 * is not very complicated either! You are required
 * to provide a SimpleFeatureFactory in order to use this
 * builder.
 * </p>
 * 
 * @author Justin
 */
public class SimpleFeatureBuilder  {
	
	List attributes = new ArrayList();
	private SimpleFeatureFactory factory;
	private SimpleFeatureType featureType;
	private SimpleFeatureCollectionType collectionType;
	
    public SimpleFeatureBuilder(SimpleFeatureFactory factory) {
    	this.factory = factory;
	}
    
    /**
     * Setter injection for SimpleFeatureFactory.
     * XXX Review? If you do not mean for Setter injection please factory final
     * @param factory
     */
	public void setSimpleFeatureFactory(SimpleFeatureFactory factory) {
		this.factory = factory;
	}

    public void init(){
    	attributes.clear();
    	featureType = null;
    	collectionType = null;
    }
    public void setType( SimpleFeatureType featureType ){
    	this.featureType = featureType; 
    }
    public void setType( SimpleFeatureCollectionType collectionType ){
    	this.collectionType = collectionType;
    }
    /** Call to add the next attribute to the builder. */
    public void add(Object value) {
		 attributes.add(value);
	}
    public Object build( String id ){
    	if( featureType != null ){
    		return feature( id );
    	}
    	if( collectionType != null){
    		return  collection( id );
    	}
    	return null;
    }
    public SimpleFeature feature(String id) {
    	return factory.createSimpleFeature( featureType, id, attributes.toArray() );

    }
    
    public SimpleFeatureCollection collection(String id) {
    	return factory.createSimpleFeatureCollection( collectionType, id );
    }
    
    protected Attribute create(
            Object value, AttributeType type, AttributeDescriptor descriptor, String id
        ) {        
            if (descriptor != null) {
                type = descriptor.getType();
            }
            
            Attribute attribute = null;
            if (type instanceof SimpleFeatureCollectionType) {
                attribute = factory.createSimpleFeatureCollection((SimpleFeatureCollectionType)type,id);
            }
            else if (type instanceof FeatureCollectionType) {
                attribute =  descriptor != null ? 
                    factory.createFeatureCollection((Collection)value,descriptor,id) :
                    factory.createFeatureCollection((Collection)value,(FeatureCollectionType)type,id);
            }
            else if (type instanceof SimpleFeatureType) {
                attribute =  factory.createSimpleFeature( (SimpleFeatureType) type, id, null );
            }
            else if (type instanceof FeatureType) {
                attribute = descriptor != null ? 
                    factory.createFeature((Collection)value,descriptor,id) :
                    factory.createFeature((Collection)value,(FeatureType)type,id);
            }
            else if (type instanceof ComplexType) {
                attribute = descriptor != null ?
                    factory.createComplexAttribute((Collection)value, descriptor, id) : 
                    factory.createComplexAttribute((Collection)value, (ComplexType)type,id);
            }
            else if (type instanceof GeometryType) {
                attribute = factory.createGeometryAttribute(value,descriptor,id,null);
            }
            else {
                //use a normal attribute builder to create a "primitive" type
                
                //use the binding to create specific "simple" types
                Class binding = descriptor.getType().getBinding();
                if (Number.class.isAssignableFrom(binding)) {
                    attribute = 
                        factory.createNumericAttribute( (Number) value, descriptor );
                }
                else if (binding.isAssignableFrom(CharSequence.class)) {
                    attribute = 
                        factory.createTextAttribute((CharSequence)value,descriptor);
                }
                else if (binding.isAssignableFrom(Date.class)) {
                    attribute = 
                        factory.createTemporalAttribute((Date) value, descriptor);
                }
                else if (Boolean.class == binding) {
                    attribute = 
                        factory.createBooleanAttribute( (Boolean) value, descriptor );
                }
                else {
                    attribute = factory.createAttribute(value,descriptor,id);    
                }
                
            }
            
            return attribute;
        }

    /**
     * Initialize the builder with the provided feature.
     * <p>
     * This is used to quickly create a "clone", can be used to change
     * between one SimpleFeatureImplementation and another.
     * </p>
     * @param feature
     */
	public void init(SimpleFeature feature) {
		init();
		this.featureType = (SimpleFeatureType) feature.getType();
		for( Iterator i=feature.attributes().iterator(); i.hasNext();){
			this.attributes.add( i.next() ); // TODO: copy
		}		
	}
}
