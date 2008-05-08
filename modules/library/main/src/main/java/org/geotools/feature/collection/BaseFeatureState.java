/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2005-2006, GeoTools Project Managment Committee (PMC)
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.geotools.data.collection.ResourceCollection;
import org.geotools.feature.CollectionEvent;
import org.geotools.feature.CollectionListener;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureTypes;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

/**
 * This is *not* a Feature - it is a Delegate used by FeatureCollection
 * implementations as "mix-in", provides implementation of featureCollection
 * events, featureType, and attribute access.
 * <p>
 * To use cut&paste the following code exactly:<pre>
 * <code>
 * 
 * </code>
 * </p>
 * <p>
 * On the bright side this means we can "fix" all the FeatureCollection<SimpleFeatureType, SimpleFeature> implementations
 * in one fell-swoop.
 * </p>
 * 
 * @author Jody Garnett, Refractions Reserach, Inc.
 * @since GeoTools 2.2
 * @source $URL$
 */
public class BaseFeatureState extends FeatureState {
    //final ResourceCollection collection;
	final SimpleFeatureType featureType;
	final SimpleFeatureType schema;	
	String id;
    
    /** Internal listener storage list */
    private List listeners = new ArrayList(2);

    
	/**
 	 * Construct a fake FeatureType of this FeatureCollection.
  	 * <p>
	 * Unless a FeatureType was provided during consturction (or this method is
	 * overriden) a FeatureType will be generated based on getSchmea according
	 * to the following assumptions:
	 * <ul>
	 * <li>FeatureType is gml:AbstractFeatureCollectionType
	 * <li>first attribute is getSchema.typeName
	 * <li>the attribute FeatureType the same as returned by getSchema()
	 * </ul>
	 * </p>
	 * 
	 */
	public static SimpleFeatureType featureType( SimpleFeatureType schema ){
	    SimpleFeatureTypeBuilder tb = new SimpleFeatureTypeBuilder();
        tb.setName("AbstractFeatureColletionType");
        tb.setNamespaceURI( FeatureTypes.DEFAULT_NAMESPACE.toString() );
        tb.add( "AbstractFeatureType", SimpleFeature.class );
	    
        return tb.buildFeatureType();
	}

	public BaseFeatureState( ResourceCollection collection, SimpleFeatureType schema ){
		this( collection, featureType( schema ), schema );				
	}
	public BaseFeatureState( ResourceCollection collection, SimpleFeatureType featureType, SimpleFeatureType schema ){
        super( collection );
		//this.collection = collection;
		this.featureType = featureType;
		this.schema = schema;
	}
	//
	// FeatureCollection<SimpleFeatureType, SimpleFeature> Event Support
	//

    /**
     * Adds a listener for collection events.
     *
     * @param listener The listener to add
     */
    public void addListener(CollectionListener listener) {
    	listeners.add(listener);
    }

    /**
     * Removes a listener for collection events.
     *
     * @param listener The listener to remove
     */
    public void removeListener(CollectionListener listener) {
    	listeners.remove(listener);
    }
    
    /**
     * To let listeners know that something has changed.
     */
    protected void fireChange(SimpleFeature[] features, int type) {
    	bounds = null; // must recalculate bounds

        CollectionEvent cEvent = new CollectionEvent( (FeatureCollection<SimpleFeatureType, SimpleFeature>) data, features, type);
        
        for (int i = 0, ii = listeners.size(); i < ii; i++) {
            ((CollectionListener) listeners.get(i)).collectionChanged(cEvent);
        }
    }
        
    protected void fireChange(SimpleFeature feature, int type) {
        fireChange(new SimpleFeature[] {feature}, type);
    }
    
    protected void fireChange(Collection coll, int type) {
        SimpleFeature[] features = new SimpleFeature[coll.size()];
        features = (SimpleFeature[]) coll.toArray(features);
        fireChange(features, type);
    }
	//
	// SimpleFeature Methods
    //
    public SimpleFeatureType getFeatureType() {
        return featureType;
    }
    public SimpleFeatureType getChildFeatureType() {
        return schema;
    }    
    public String getId() {
        return id;
    }
    public void setId( String id ){
        this.id = id;
    }    
}
