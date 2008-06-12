/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2005-2008, Open Source Geospatial Foundation (OSGeo)
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

import org.geotools.feature.CollectionEvent;
import org.geotools.feature.CollectionListener;
import org.geotools.feature.FeatureCollection;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

/**
 * This is *not* a Feature - it is a Delegate used by FeatureCollection
 * implementations as "mix-in", provides implementation of featureCollection
 * events, featureType, and attribute access backed by an origional
 * FeatureCollection.
 * <p>
 * To use cut&paste the following code exactly:<pre>
 * <code>
 * TBA
 * </code>
 * </p>
 * <p>
 * On the bright side this means we can "fix" all the SubFeatureCollection
 * implementationsin one fell-swoop.
 * </p>
 * 
 * @author Jody Garnett, Refractions Reserach, Inc.
 * @since GeoTools 2.2
 * @source $URL$
 */
public class SubFeatureState extends FeatureState {
	final FeatureCollection<SimpleFeatureType, SimpleFeature> collection;
    
    final CollectionListener listener = new CollectionListener(){        
        public void collectionChanged( CollectionEvent tce ) {
            bounds = null;
        }        
    };
    
	public SubFeatureState( FeatureCollection<SimpleFeatureType, SimpleFeature> collection, FeatureCollection<SimpleFeatureType, SimpleFeature> sub ){
        super( sub );
		this.collection = collection; 
        collection.addListener( listener );
	}
    
    public void close(){
        if( listener != null ){
            collection.removeListener( listener );
        }        
        bounds = null;
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
        collection.addListener( listener );
    }

    /**
     * Removes a listener for collection events.
     *
     * @param listener The listener to remove
     */
    public void removeListener(CollectionListener listener) {
        collection.removeListener( listener );
    }
    protected void fireChange( SimpleFeature[] features, int type ) {
        // 
    }
	//
	// Feature Methods
    //
    public SimpleFeatureType getFeatureType() {
        return collection.getFeatureType();
    }
    public SimpleFeatureType getChildFeatureType() {
        return collection.getSchema();
    }
    public String getId(){
        return collection.getID();
    }
    
}
