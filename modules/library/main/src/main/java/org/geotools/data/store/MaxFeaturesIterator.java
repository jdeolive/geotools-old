package org.geotools.data.store;

import java.util.Iterator;

import org.opengis.feature.Feature;

/**
 * Iterator wrapper which caps the number of returned features;
 * 
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class MaxFeaturesIterator<F extends Feature> implements Iterator<F> {

	Iterator<F> delegate;
	long max;
	long counter;
	
	public MaxFeaturesIterator( Iterator<F> delegate, long max ) {
		this.delegate = delegate;
		this.max = max;
		counter = 0;
	}
	
	public Iterator<F> getDelegate() {
		return delegate;
	}
	
	public void remove() {
		delegate.remove();
	}

	public boolean hasNext() {
		return delegate.hasNext() && counter < max; 
	}

	public F next() {
		if ( counter++ <= max ) {
			return delegate.next();
		}
		
		return null;
	}

}
