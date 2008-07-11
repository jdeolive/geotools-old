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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import org.geotools.feature.CollectionListener;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.simple.SimpleFeatureImpl;
import org.geotools.feature.visitor.FeatureVisitor;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.util.ProgressListener;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Base feature collection for GeoTools feature collection implementations as 
 * the transition to the GeoAPI feature model is made.
 * <p>
 * The primary role of this base collection is to implement the feature aspect 
 * of the feature collection api, leaving the collection aspect to subclasses. 
 * </p>
 * @author Justin Deoliveira, The Open Planning Project, jdeolive@openplans.org
 *
 * @since 2.5
 */
public abstract class BaseFeatureCollection extends SimpleFeatureImpl 
    implements FeatureCollection<SimpleFeatureType, SimpleFeature> {

	/**
	 * logger
	 */
	protected static Logger LOGGER = org.geotools.util.logging.Logging.getLogger("org.geotools.feature");
	/**
	 * listeners
	 */
	protected List listeners = new ArrayList();
	
	/**
	 * Constructs the collection with an id.
	 * <p>
	 * The getSchema() value will need to be aquired
	 * from the first feature added.
	 * </p>
	 * @param id The identifier of the feature collection.
	 */
    protected BaseFeatureCollection( String id ){
    	this(id != null ? id : "featureCollection",(SimpleFeatureType)null);
    }
    
    /**
	 * Constructs the collection with an id and a single member type.
	 * 
	 * @param id The identifier of the feature collection.
	 * @param memberType The type of the members of the collection.
	 */
    protected BaseFeatureCollection( String id, SimpleFeatureType memberType ){
    	super(new Object[0], new BaseFeatureCollectionType(memberType), id != null ? id : "featureCollection", false);
    }
    
//    /**
//     * Constructs the collection with an id and pre-existing type.
//     * 
//     * @param id The identifier of the feature collection.
//     * @param type The type of the feature collection.
//     */
//    protected BaseFeatureCollection( String id, SimpleFeatureType type ) {
//    	super( Collections.EMPTY_LIST, type, id );
//    }

    //
    // FeatureCollection
    //
    final public void addListener(CollectionListener listener) throws NullPointerException {
    	listeners.add(listener);
    }
    
    final public void removeListener(CollectionListener listener) throws NullPointerException {
    	listeners.remove(listener);
    }
    
    public final void accepts(FeatureVisitor visitor, ProgressListener progress)
            throws IOException {
        accepts( (org.opengis.feature.FeatureVisitor)visitor, (org.opengis.util.ProgressListener)progress);
    }
    
    public BaseFeatureCollectionType getType() {
        return (BaseFeatureCollectionType) super.getType();
    }
    
    public SimpleFeatureType getSchema() {
    	return (SimpleFeatureType) getType().getMemberType();
    }
    
    public Object[] getAttributes(Object[] attributes) {
		Object[] retArray;
		
		//no attributes
		return new Object[]{};
    }

	public BaseFeatureCollectionType getFeatureType() {
		return (BaseFeatureCollectionType) getType();
	}

	public Geometry getDefaultGeometry() {
	    return (Geometry) super.getDefaultGeometry();
	}
	
	public void setDefaultGeometry(Geometry geometry)
	        throws IllegalAttributeException {
	    throw new UnsupportedOperationException();
	}
	
	/**
	 * Subclasses need to override this.
	 */
	public ReferencedEnvelope getBounds() {
		throw new UnsupportedOperationException("subclasses should override");
	}
	public void validate() {
	}
	
}
