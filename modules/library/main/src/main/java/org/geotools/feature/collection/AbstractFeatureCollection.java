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

import java.util.Collection;
import java.util.Iterator;

import org.geotools.data.collection.ResourceCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.sort.SortBy;

/**
 * Implement a feature collection just based on provision of iterator.
 * <p>
 * Your subclass will need to provide an internal "state" stratagy object
 * used to access collection attributes - see the two protected constructors
 * for details.
 * </p>
 * @author Jody Garnett, Refractions Research Inc.
 */
public abstract class AbstractFeatureCollection extends BaseFeatureCollection
/* extends AbstractResourceCollection */implements
        FeatureCollection<SimpleFeatureType, SimpleFeature>, ResourceCollection<SimpleFeature> {
    
	AbstractResourceCollection rc;

	protected AbstractFeatureCollection( SimpleFeatureType memberType ) {
		super(null,memberType);
	}
	
	protected AbstractFeatureCollection( SimpleFeatureType memberType, AbstractResourceCollection rc ) {
		super(null,memberType);
		this.rc = rc;
	}
	
	protected void setResourceCollection( AbstractResourceCollection rc ) {
		this.rc = rc;
	}
	
    //
    // FeatureCollection<SimpleFeatureType, SimpleFeature> - Feature Access
    // 
    public FeatureIterator<SimpleFeature> features() {
        FeatureIterator<SimpleFeature> iter = new DelegateFeatureIterator<SimpleFeature>( this, rc.openIterator() );
        rc.getOpenIterators().add( iter );
        return iter; 
    }
    public void close( FeatureIterator<SimpleFeature> close ) {     
        closeIterator( close );
        rc.getOpenIterators().remove( close );
    }
    public void closeIterator( FeatureIterator<SimpleFeature> close ) {
        DelegateFeatureIterator<SimpleFeature> iter = (DelegateFeatureIterator<SimpleFeature>) close;
        rc.closeIterator( iter.delegate );
        iter.close(); 
    }
    public void purge() {
        for( Iterator i = rc.getOpenIterators().iterator(); i.hasNext(); ){
            Object resource = i.next();
            if( resource instanceof FeatureIterator ){
                FeatureIterator<SimpleFeature> resourceIterator = (FeatureIterator<SimpleFeature>) resource;
                try {
                    closeIterator( resourceIterator );
                }
                catch( Throwable e){
                    // TODO: Log e = ln
                }
                finally {
                    i.remove();
                }
            }
        }        

        rc.purge();
    }
    
    final public int size() {
    	return rc.size();
    }
    
    final public Iterator iterator() {
    	return rc.iterator();
    }
    
    final public void close(Iterator close) {
    	rc.close(close);
    };
    
    final public boolean add(SimpleFeature o) {
		return rc.add(o);
	}

    final public boolean addAll(Collection c) {
		return rc.addAll(c);
	}

    final public void clear() {
    	rc.clear();
	}

    final public boolean contains(Object o) {
    	return rc.contains(o);
	}

    final public boolean containsAll(Collection c) {
		return rc.containsAll(c);
	}

    final public boolean isEmpty() {
		return rc.isEmpty();
	}

    final public boolean remove(Object o) {
		return rc.remove(o);
	}

    final public boolean removeAll(Collection c) {
		return rc.removeAll(c);
	}

    final public boolean retainAll(Collection c) {
		return rc.retainAll(c);
	}

    final public Object[] toArray() {
		return rc.toArray();
	}

    final public Object[] toArray(Object[] a) {
		return rc.toArray(a);
	}

	public void accepts(org.opengis.feature.FeatureVisitor visitor, org.opengis.util.ProgressListener progress) {
    	Iterator iterator = null;
        // if( progress == null ) progress = new NullProgressListener();
        try{
            float size = size();
            float position = 0;            
            progress.started();
            for( iterator = iterator(); !progress.isCanceled() && iterator.hasNext();){
                if (size > 0) progress.progress( position++/size );
                try {
                    SimpleFeature feature = (SimpleFeature) iterator.next();
                    visitor.visit(feature);
                }
                catch( Exception erp ){
                    progress.exceptionOccurred( erp );
                }
            }            
        }
        finally {
            progress.complete();            
            close( iterator );
        }
    }
    
    //
    // Feature Collections API
    //
    public FeatureCollection<SimpleFeatureType, SimpleFeature> subList( Filter filter ) {
        return new SubFeatureList(this, filter );
    }
    
    public FeatureCollection<SimpleFeatureType, SimpleFeature> subCollection( Filter filter ) {
        if( filter == Filter.INCLUDE ){
            return this;
        }        
        return new SubFeatureCollection( this, filter );
    }

    public FeatureCollection<SimpleFeatureType, SimpleFeature> sort( SortBy order ) {
        return new SubFeatureList(this, order );
    }
    
}
