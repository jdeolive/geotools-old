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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.geotools.data.collection.ResourceCollection;
import org.geotools.feature.CollectionListener;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.geometry.BoundingBox;

import com.vividsolutions.jts.geom.Geometry;

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
public abstract class FeatureState {
	    
    protected ReferencedEnvelope bounds = null;
    protected FeatureCollection data;

    protected FeatureState( FeatureCollection collection ){
        data = collection;
    }
	//
	// FeatureCollection<SimpleFeatureType, SimpleFeature> Event Support
	//

    /**
     * Adds a listener for collection events.
     *
     * @param listener The listener to add
     */
    abstract public void addListener(CollectionListener listener);

    /**
     * Removes a listener for collection events.
     *
     * @param listener The listener to remove
     */
    abstract public void removeListener(CollectionListener listener);
    	
    
    /**
     * To let listeners know that something has changed.
     */
    abstract protected void fireChange(SimpleFeature[] features, int type);
    
    protected void fireChange(SimpleFeature feature, int type) {
        fireChange(new SimpleFeature[] {feature}, type);
    }
    
    protected void fireChange(Collection coll, int type) {
        SimpleFeature[] features = new SimpleFeature[coll.size()];
        features = (SimpleFeature[]) coll.toArray(features);
        fireChange(features, type);
    }
    
	//
	// Feature Methods
    //    
    /**
     * Gets the bounding box for the features in this feature collection.
     * 
     * @return the envelope of the geometries contained by this feature
     *         collection.
     */
    public ReferencedEnvelope getBounds() {
        if (bounds == null) {
            bounds = new ReferencedEnvelope();
            Iterator i = data.iterator();
            try {            	
	            while(i.hasNext()) {
	                BoundingBox geomBounds = ((SimpleFeature) i.next()).getBounds();                
	                if ( ! geomBounds.isEmpty() ) {
	                    bounds.include(geomBounds);
	                }
	            }
            }
            finally {
            	data.close( i );
            }
        }
        return bounds;
    }

    //public abstract SimpleFeatureType getFeatureType();
    public abstract SimpleFeatureType getChildFeatureType();
    
    public abstract String getId();

    /*
    public Object[] getAttributes( Object[] attributes ) {
        List list = (List) getAttribute( 0 );
        return list.toArray( attributes );
    }
    public Object getAttribute( String xPath ) {
        if(xPath.indexOf(getFeatureType().getTypeName())>-1)
            if(xPath.endsWith("]")){
                return getAttribute(0);
            } else {
                return getAttribute(0);
            }
        return null;
    }
    
    public Object getAttribute( int index ) {
        if(index == 0){
        	Iterator i = data.iterator();
        	List list = new ArrayList();        	
            try {                
                while( i.hasNext() ){
                    SimpleFeature feature = (SimpleFeature) i.next();                    
                    list.add( feature );
                }
                return list;
            } catch (NoSuchElementException e) {
                return Collections.EMPTY_LIST; // could not find contents
            }
            finally {
                data.close( i );
            }
        }
        return null;
    }
    
    public void setAttribute(int position, Object val) throws IllegalAttributeException, ArrayIndexOutOfBoundsException {
		if(position == 0 && val instanceof Collection){
            Collection newStuff = (Collection) val;
			if( !isFeatures( newStuff )) {
				throw new IllegalAttributeException("Content must be features");
			}            
            data.clear(); // clean out previous contents!
            Iterator i = newStuff.iterator();
            try {
	            while( i.hasNext() ){
	                SimpleFeature feature = (SimpleFeature) i.next();                
	                data.add( feature );
	            }
            }
            finally {
                data.close( i );
            }
			//fireChange(nw,0);
		}
	}
       
	public int getNumberOfAttributes() {
		return getFeatureType().getAttributeCount();		
	}

    public void setAttribute( String xPath, Object attribute ) throws IllegalAttributeException {
        if(xPath.indexOf(getFeatureType().getTypeName())>-1){
            if(xPath.endsWith("]")){
                // TODO get index and grab it
            } else {
                setAttribute(0,attribute);
            }
        }
    }

    public Geometry getDefaultGeometry() {
        return null;
    }

    public void setDefaultGeometry( Geometry geometry ) throws IllegalAttributeException {
        throw new IllegalAttributeException( "DefaultGeometry not supported" );
    }
    */
    //
    // Utility Methods
    //
    /** 
     * Get the set of fids for the provided collection.
     * <p>
     * By doing a quick pass through the collection we can  do
     * comparisons based on Feature ID (rather then collection
     * membership).
     * </p>
     * <p>
     * A subclass that tracks its FID information may wish to override
     * this method.
     * </p>
     */
    public static Set fids( Collection stuff ){
        if( stuff instanceof DefaultFeatureCollection ){
            DefaultFeatureCollection features = (DefaultFeatureCollection) stuff;
            return features.fids();
        }
        
        Iterator iterator = stuff.iterator();
        Set fids = new HashSet();
        try {
            while( iterator.hasNext() ){
                SimpleFeature feature = (SimpleFeature) iterator.next();
                fids.add( feature.getID() );
            }
        }
        finally {
            if( stuff instanceof ResourceCollection){
                ((ResourceCollection) stuff).close( iterator );
            }
        }
        return fids;
    }
    
    /** Test if collection is all features! */
    public static boolean isFeatures( Collection stuff ){
        if( stuff instanceof FeatureCollection ) return true;
        
        Iterator i = stuff.iterator();
        try {
	        while( i.hasNext() ){
	            if(!(i.next() instanceof SimpleFeature))
	                return false;
	        }
        }
        finally {
            if( stuff instanceof ResourceCollection){
                ((ResourceCollection) stuff).close( i );
            }
        }
        return true;
    }    
    
}
