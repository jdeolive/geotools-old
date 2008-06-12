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
package org.geotools.feature.simple;

import java.rmi.server.UID;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.geotools.data.DataUtilities;
import org.geotools.feature.FeatureFactoryImpl;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.util.Converters;
import org.opengis.feature.Attribute;
import org.opengis.feature.FeatureFactory;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.Name;

import com.vividsolutions.jts.geom.Geometry;

/**
 * A builder for features.
 * <p>
 * Simple Usage:
 * <code>
 * 	<pre>
 *  //type of features we would like to build ( assume schema = (geom:Point,name:String) )
 *  SimpleFeatureType featureType = ...  
 * 
 *   //create the builder
 *  SimpleFeatureBuilder builder = new SimpleFeatureBuilder();
 *  
 *  //set the type of created features
 *  builder.setType( featureType );
 *  
 *  //add the attributes
 *  builder.add( new Point( 0 , 0 ) );
 *  builder.add( "theName" );
 *  
 *  //build the feature
 *  SimpleFeature feature = builder.buildFeature( "fid" );
 * 	</pre>
 * </code>
 * </p>
 * <p>
 * This builder builds a feature by maintaining state. Each call to {@link #add(Object)}
 * creates a new attribute for the feature and stores it locally. When using the 
 * add method to add attributes to the feature, values added must be added in the 
 * same order as the attributes as defined by the feature type. The methods 
 * {@link #set(String, Object)} and {@link #set(int, Object)} are used to add 
 * attributes out of order.
 * </p>
 * <p>
 * Each time the builder builds a feature with a call to {@link #buildFeature(String)}
 * the internal state is reset. 
 * </p>
 * <p>
 * This builder can be used to copy features as well. The following code sample
 * demonstrates:
 * <code>
 * <pre>
 *  //original feature
 *  SimpleFeature original = ...;
 * 
 *  //create and initialize the builder
 *  SimpleFeatureBuilder builder = new SimpleFeatureBuilder();
 *  builder.init(original);
 * 
 *  //create the new feature
 *  SimpleFeature copy = builder.buildFeature( original.getID() );
 * 
 *  </pre>
 * </code>
 * </p>
 * <p>
 * The builder also provides a number of static "short-hand" methods which can 
 * be used when its not ideal to instantiate a new builder.
 * <code>
 *   <pre>
 *   SimpleFeatureType type = ..;
 *   Object[] values = ...;
 *   
 *   //build a new feature
 *   SimpleFeature feature = SimpleFeatureBuilder.build( type, values, "fid" );
 *   
 *   ...
 *   
 *   SimpleFeature original = ...;
 *   
 *   //copy the feature
 *   SimpleFeature feature = SimpleFeatureBuilder.copy( original );
 *   </pre>
 * </code>
 * </p>
 * <p>
 * This class is not thread safe nor should instances be shared across multiple 
 * threads.
 * </p>
 *  
 * @author Justin Deoliveira
 * @author Jody Garnett
 */
public class SimpleFeatureBuilder {
	/**
	 * logger
	 */
    static Logger LOGGER = org.geotools.util.logging.Logging.getLogger("org.geotools.feature");
    
	/**
	 * factory
	 */
	protected FeatureFactory factory;
	/**
	 * list of attributes
	 */
	protected List attributes = new ArrayList();
	/**
	 * feature type
	 */
	protected SimpleFeatureType featureType;
	/**
	 * default geometry
	 */
	protected GeometryAttribute defaultGeometry;
	/**
	 * user data
	 */
	protected Map userData = new HashMap();
	
	/**
     * Constructs the builder.
     */
    public SimpleFeatureBuilder(SimpleFeatureType type) {
        this( type, new FeatureFactoryImpl());
    }
    
	/**
	 * Constructs the builder specifying the factory to use for creating features.
	 */
    public SimpleFeatureBuilder(SimpleFeatureType simpleFeatureType, FeatureFactory factory) {
        this.factory = factory;
        this.featureType = simpleFeatureType;
	}

    /**
     * Sets the factory used to create features.
     */
	public void setFeatureFactory(FeatureFactory factory) {
		this.factory = factory;
	}
    
    /**
     * Initialize the builder with the provided feature.
     * <p>
     * This method adds all the attributes from the provided feature. It is 
     * useful when copying a feature. 
     * </p>
     */
    public void init( SimpleFeature feature ) {
		init();
		for ( Iterator p = feature.getProperties().iterator(); p.hasNext(); ) {
		    Property original = (Property) p.next();
		    
		    //add its value
		    add( original.getValue() );
		    
		    //copy over the user data
		    Attribute last = (Attribute) attributes().get( attributes().size() -1 );
		    last.getUserData().putAll( original.getUserData() );
		}
		
		//defaultGeometry = feature.getDefaultGeometryProperty();
		//crs = feature.getType().getCRS();
	}
    /**
     * Internal method which initializes builder state.
     */
    protected void init() {
    	attributes = null; 
    	userData = new HashMap();
    }
    
    /**
     * Adds some user data to the next attributed added to the feature.
     * <p>
     * This value is reset when the next attribute is added. 
     * </p>
     * @param key The key of the user data
     * @param value The value of the user data.
    */
    public SimpleFeatureBuilder userData( Object key, Object value ) {
        userData.put( key, value );
        return this;
    }
    /**
     * Adds an attribute.
     * <p>
     * This method should be called repeatedly for the number of attributes as 
     * specified by the type of the feature.
     * </p>
     */
    public void add(Object value) {
    	//get the descriptor from the type
    	if(attributes().size() >= featureType.getAttributeCount())
    		throw new IllegalArgumentException("Too many attribute values, this feature " +
    				"type has only " + featureType.getAttributeCount() + " attributes");
    	AttributeDescriptor descriptor = featureType.getAttribute(attributes().size());
    	Attribute attribute = null;
    	
    	//make sure the type of the value and the binding of the type match up
    	if ( value != null ) {
    	    Class target = descriptor.getType().getBinding(); 
    	    if ( !target.isAssignableFrom(value.getClass()) ) {
    	        //try to convert
    	        LOGGER.fine("value: " + value + " does not match type: " + target.getName() + ". Converting.");
    	        Object converted = Converters.convert(value, target);
    	        if ( converted != null ) {
    	            value = converted;
    	        }
    	    }
    	}
    	else {
    	    //if the content is null and the descriptor says isNillable is false, 
            // then set the default value
            if (!descriptor.isNillable()) {
                value = descriptor.getDefaultValue();
                if ( value == null ) {
                    //no default value, try to generate one
                    value = DataUtilities.defaultValue(descriptor.getType().getBinding());
                }
            }
    	}
    	
    	//check if the attribute type is identifiable
    	String id = null;
    	if ( descriptor.getType().isIdentified() ) {
    	    id = createDefaultFeatureId();
    	}
    	if ( descriptor instanceof GeometryDescriptor ) {
    		//TODO: set crs on teh builder
    		attribute = factory.createGeometryAttribute(value, (GeometryDescriptor) descriptor, id, featureType.getCRS() );
    		
    		//is this the default geometry?
    		if ( descriptor.equals( featureType.getDefaultGeometry() ) ) {
    			defaultGeometry = (GeometryAttribute) attribute;
    		}
    	}
    	else {
    		attribute = factory.createAttribute( value, descriptor, id ) ;
    	}
    	
    	//user data
    	attribute.getUserData().putAll( userData );
    	
    	//add it
    	attributes().add(attributes().size(),attribute);
	}
    
    /**
     * Adds an array of attributes.
     * <p>
     * This method is convenience for: 
     * <code>
     *   <pre>
     *   for (int i = 0; i < values.length; i++ ) {
     *      add( values[i] );
     *   }
     *   </pre>
     * </code>
     * </p>
     */
    public void addAll(List values ) {
        if ( values == null ) {
            return;
        }
        for ( Object value : values ) {
            add( values );
        }
    }
    
    /**
     * Adds an array of attributes.
     * <p>
     * This method is convenience for: 
     * <code>
     *   <pre>
     *   for (int i = 0; i < values.length; i++ ) {
     *      add( values[i] );
     *   }
     *   </pre>
     * </code>
     * </p>
     */
    public void addAll(Object[] values ) {
    	if ( values == null ) {
    		return;
    	}
    	for ( int i = 0; i < values.length; i++) {
    		add( values[ i ] );
    	}
    }
    
    /**
     * Adds an attribute value by name.
     * <p>
     * This method can be used to add attribute values out of order.
     * </p>
     * @param name The name of the attribute.
     * @param value The value of the attribute.
     * 
     * @throws IllegalArgumentException If no such attribute with teh specified
     * name exists.
     */
    public void set(Name name, Object value) {
        int index = featureType.indexOf(name);
        if (index == -1 ) {
            throw new IllegalArgumentException("No such attribute:" + name);
        }
        set(index,value);
    }
    
    /**
     * Adds an attribute value by name.
     * <p>
     * This method can be used to add attribute values out of order.
     * </p>
     * @param name The name of the attribute.
     * @param value The value of the attribute.
     * 
     * @throws IllegalArgumentException If no such attribute with teh specified
     * name exists.
     */
    public void set(String name, Object value) {
        int index = featureType.indexOf(name);
        if (index == -1 ) {
            throw new IllegalArgumentException("No such attribute:" + name);
        }
        set(index,value);
    }
    
    /**
     * Adds an attribute value by index.
     * * <p>
     * This method can be used to add attribute values out of order.
     * </p>
     * @param index The index of the attribute.
     * @param value The value of the attribute.
     */
    public void set(int index, Object value) {
        if (index < attributes().size()) {
            //already an attribute for this index
            Attribute attribute = (Attribute) attributes().get(index);
            attribute.setValue(value);
        }
        else {
            //expand the list of attributes up to the index
            while(attributes.size() < index ) {
                add((Object)null);
            }
            
            add(value);
        } 
    }
  
    /**
     * Builds the feature.
     * <p>
     * The specified <tt>id</tt> may be <code>null</code>. In this case an id
     * will be generated internally by the builder.
     * </p>
     * <p>
     * After this method returns, all internal builder state is reset.
     * </p>
     * @param id The id of the feature, or <code>null</code>.
     * 
     * @return The new feature.
     */
    public SimpleFeature buildFeature(String id) {
        //ensure id
        if ( id == null ) {
            id = createDefaultFeatureId();
        }
        
        //ensure they specified enough values
        int n = featureType.getAttributeCount();
        while( attributes().size() < n ) {
            add((Object)null);
        }
        
        //build the feature
    	SimpleFeature feature = factory.createSimpleFeature( attributes, featureType, id );
    	if ( defaultGeometry != null ) {
    	    feature.setDefaultGeometryProperty(defaultGeometry);
    	}
    	
    	init();
    	return feature;
    }
    
    /**
     * Internal method for creating feature id's when none is specified.
     */
    public static String createDefaultFeatureId() {
          // According to GML and XML schema standards, FID is a XML ID
        // (http://www.w3.org/TR/xmlschema-2/#ID), whose acceptable values are those that match an
        // NCNAME production (http://www.w3.org/TR/1999/REC-xml-names-19990114/#NT-NCName):
        // NCName ::= (Letter | '_') (NCNameChar)* /* An XML Name, minus the ":" */
        // NCNameChar ::= Letter | Digit | '.' | '-' | '_' | CombiningChar | Extender
        // We have to fix the generated UID replacing all non word chars with an _ (it seems
        // they area all ":")
        //return "fid-" + NON_WORD_PATTERN.matcher(new UID().toString()).replaceAll("_");
        // optimization, since the UID toString uses only ":" and converts long and integers
        // to strings for the rest, so the only non word character is really ":"
        return "fid-" + new UID().toString().replace(':', '_');
    }
    
    /**
     * Internal accessor for attribute list.
     * @return
     */
    protected List attributes() {
		if ( attributes == null ) {
			attributes = newList();
		}
		
		return attributes;
	}
	
	protected List newList() {
		return new ArrayList();
	}
	
	/**
	 * Static method to build a new feature.
	 * <p>
	 * If multiple features need to be created, this method should not be used
	 * and instead an instance should be instantiated directly.
	 * </p>
	 * <p>
	 * This method is a short-hand convenience which creates a builder instance
	 * internally and adds all the specified attributes.
	 * </p>
	 */
	public static SimpleFeature build( SimpleFeatureType type, Object[] values, String id ) {
	    SimpleFeatureBuilder builder = new SimpleFeatureBuilder(type);
	    builder.addAll(values);
	    return builder.buildFeature(id);
	}
	
	/**
	 * * Static method to build a new feature.
     * <p>
     * If multiple features need to be created, this method should not be used
     * and instead an instance should be instantiated directly.
     * </p>
	 */
	public static SimpleFeature build( SimpleFeatureType type, List values, String id ) {
	    return build( type, values.toArray(), id );
	}
	
	/**
     * Copy an existing feature (the values are reused so be careful with mutable values).
     * <p>
     * If multiple features need to be copied, this method should not be used
     * and instead an instance should be instantiated directly.
     * </p>
     * <p>
     * This method is a short-hand convenience which creates a builder instance
     * and initializes it with the attributes from the specified feature.
     * </p>
     */
	public static SimpleFeature copy( SimpleFeature original ) {
	    if( original == null ) return null;
	    
	    SimpleFeatureBuilder builder = new SimpleFeatureBuilder(original.getFeatureType());
	    builder.init(original); // this is a shallow copy
	    return builder.buildFeature(original.getID());
	}
	
	/**
     * Deep copy an existing feature.
     * <p>
     * This method is scary, expensive and will result in a deep copy of
     * Geometry which will be.
     * </p>
     * @param original Content
     * @return copy
     */
    public static SimpleFeature deep( SimpleFeature original ) {
        if( original == null ) return null;
        
        SimpleFeatureBuilder builder = new SimpleFeatureBuilder(original.getFeatureType());
        try {
            for( Property property : original.getProperties() ){
                Object value = property.getValue();
                Object copy = value;
                if( value instanceof Geometry ){
                    Geometry geometry = (Geometry) value;
                    copy = geometry.clone();
                }
                builder.set( property.getName(), copy );
            }
            return builder.buildFeature(original.getID());    
        }
        catch( Exception e ) {
            throw (IllegalAttributeException) new IllegalAttributeException("illegal attribute").initCause(e);
        }
    }
    
	/**
	 * Builds a new feature whose attribute values are the default ones
	 * @param featureType
	 * @param featureId
	 * @return
	 */
	public static SimpleFeature template(SimpleFeatureType featureType, String featureId) {
		SimpleFeatureBuilder builder = new SimpleFeatureBuilder(featureType);
		for (AttributeDescriptor ad : featureType.getAttributes()) {
			builder.add(ad.getDefaultValue());
		}
		return builder.buildFeature(featureId);
	}
        
    /**
     * Copies an existing feature, retyping it in the process.
     * <p>
     * If the feature type contians attributes in which the oringial feature 
     * does not have a value for, the value in the resulting feature is set to
     * <code>null</code>.
     * </p>
     * @param feature The original feature.
     * @param featureType The target feature type.
     *  
     * @return The copied feature, with a new type.
     */
    public static SimpleFeature retype(SimpleFeature feature, SimpleFeatureType featureType) {
        SimpleFeatureBuilder builder = new SimpleFeatureBuilder(featureType);
        for ( Iterator a = featureType.getAttributes().iterator(); a.hasNext(); ) {
            AttributeDescriptor att = (AttributeDescriptor) a.next();
            Object value = feature.getAttribute( att.getName() );
            builder.set(att.getName(), value);
        }
        return builder.buildFeature(feature.getID());
    }
}
