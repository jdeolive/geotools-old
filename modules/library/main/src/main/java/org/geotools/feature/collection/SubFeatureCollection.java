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

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.geotools.data.FeatureReader;
import org.geotools.data.collection.DelegateFeatureReader;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.sort.SortBy;

/**
 * Used as a reasonable default implementation for subCollection.
 * <p>
 * Note: to implementors, this is not optimal, please do your own
 * thing - your users will thank you.
 * </p>
 * 
 * @author Jody Garnett, Refractions Research, Inc.
 *
 * @source $URL$
 */
public class SubFeatureCollection extends BaseFeatureCollection implements FeatureCollection<SimpleFeatureType, SimpleFeature> {
	/** Filter */
    protected Filter filter;
    
    /** Origional Collection */
	protected FeatureCollection<SimpleFeatureType, SimpleFeature> collection;    
    //protected FeatureState state;
    protected FilterFactory ff = CommonFactoryFinder.getFilterFactory( null );
    
    protected AbstractResourceCollection rc; 
    
    public SubFeatureCollection(FeatureCollection<SimpleFeatureType, SimpleFeature> collection ) {
        this( collection, Filter.INCLUDE );
    }
	public SubFeatureCollection(FeatureCollection<SimpleFeatureType, SimpleFeature> collection, Filter subfilter ){
		super(null,collection.getSchema());
		
		if (subfilter == null ) subfilter = Filter.INCLUDE;		
		if (subfilter.equals(Filter.EXCLUDE)) {
			throw new IllegalArgumentException("A subcollection with Filter.EXCLUDE is a null operation");
		}
		
        if( collection instanceof SubFeatureCollection){
			SubFeatureCollection filtered = (SubFeatureCollection) collection;
			if( subfilter.equals(Filter.INCLUDE)){
                this.collection = filtered.collection;
			    this.filter = filtered.filter();
			}
			else {
			    this.collection = filtered.collection;	            			    
			    this.filter = ff.and( filtered.filter(), subfilter );
			}
		} else {
			this.collection = collection;
			this.filter = subfilter;
		}
        
        rc = createResourceCollection();
    }
	
	AbstractResourceCollection createResourceCollection() {
		return new AbstractResourceCollection() {
			public Iterator openIterator() {
    			return new FilteredIterator<SimpleFeature>( collection, filter() );
    		}

    		public void closeIterator(Iterator iterator) {
    			if( iterator == null ) return;
    			
    			if( iterator instanceof FilteredIterator){
    				FilteredIterator filtered = (FilteredIterator) iterator;			
    				filtered.close();
    			}
    		}
    		
    		public int size() {
    			int count = 0;
    			Iterator i = null;		
    			try {
    				for( i = iterator(); i.hasNext(); count++) i.next();
    			}
    			finally {
    				close( i );
    			}
    			return count;
    		}
			
		};
	}
    
	protected Filter filter(){
	    if( filter == null ){
            filter = createFilter();
        }
        return filter;
    }
    /** Override to implement subsetting */
    protected Filter createFilter(){
        return Filter.INCLUDE;
    }
    
	public FeatureIterator<SimpleFeature> features() {
		return new DelegateFeatureIterator<SimpleFeature>( this, iterator() );		
	}	
	
	
	public void close(FeatureIterator<SimpleFeature> close) {
		if( close != null ) close.close();
	}

    //
    //
    //
	public FeatureCollection<SimpleFeatureType, SimpleFeature> subCollection(Filter filter) {
		if (filter.equals(Filter.INCLUDE)) {
			return this;
		}
		if (filter.equals(Filter.EXCLUDE)) {
			// TODO implement EmptyFeatureCollection( schema )
		}
		return new SubFeatureCollection(this, filter);
	}

	
	public boolean isEmpty() {
		Iterator iterator = iterator();
		try {
			return !iterator.hasNext();
		}
		finally {
			close( iterator );
		}
	}
	

	public SimpleFeatureType getSchema() {
        return collection.getSchema();
	}

	public void accepts(org.opengis.feature.FeatureVisitor visitor, org.opengis.util.ProgressListener progress) {
		Iterator iterator = null;
        // if( progress == null ) progress = new NullProgressListener();
        try{
            float size = size();
            float position = 0;            
            progress.started();
            for( iterator = iterator(); !progress.isCanceled() && iterator.hasNext(); progress.progress( position++/size )){
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

	public  FeatureReader<SimpleFeatureType, SimpleFeature> reader() throws IOException {
		return new DelegateFeatureReader<SimpleFeatureType, SimpleFeature>( getSchema(), features() );
	}

	public int getCount() throws IOException {
		return size();
	}

	public FeatureCollection<SimpleFeatureType, SimpleFeature> collection() throws IOException {
		return this;
	}

	public FeatureCollection<SimpleFeatureType, SimpleFeature> sort(SortBy order) {
		return null;
	}

	public void purge() {
		collection.purge();
	}
	public void close(Iterator close) {
		rc.close(close);
	}
	public Iterator iterator() {
		return rc.iterator();
	}
	public boolean add(SimpleFeature o) {
		return rc.add(o); 
	}
	public boolean addAll(Collection c) {
		return rc.addAll(c);
	}
	public void clear() {
		rc.clear();
	}
	public boolean contains(Object o) {
		return rc.contains(o);
	}
	public boolean containsAll(Collection c) {
		return rc.containsAll(c);
	}
	public boolean remove(Object o) {
		return rc.remove(o);
	}
	public boolean removeAll(Collection c) {
		return rc.removeAll(c);
	}
	public boolean retainAll(Collection c) {
		return rc.retainAll(c);
	}
	public int size() {
		return rc.size();
	}
	public Object[] toArray() {
		return rc.toArray();
	}
	public Object[] toArray(Object[] a) {
		return rc.toArray(a);
	}

}
