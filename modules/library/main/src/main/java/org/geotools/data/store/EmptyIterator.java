package org.geotools.data.store;

import java.util.Iterator;

/**
 * An iterator that returns no content.
 * 
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class EmptyIterator implements Iterator {

	public void remove() {
	}

	public boolean hasNext() {
		return false;
	}

	public Object next() {
		return null;
	}

}
