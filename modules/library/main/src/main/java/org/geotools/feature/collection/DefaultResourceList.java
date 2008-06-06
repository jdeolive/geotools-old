/*
 *    GeoTools - The Open Source Java GIS Tookit
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


public class DefaultResourceList extends AbstractResourceList {


	Strategy strategy;
	
	public DefaultResourceList(Strategy strategy) {
		this.strategy = strategy;
	}
	
	public static abstract class Strategy {
		
		 /**
	     * Replaces item in position index (optional operation).
	     * <p>
	     * This implementation always throws an
	     * <tt>UnsupportedOperationException</tt>.
	     * 
	     * @param index
	     *            index of element to replace.
	     * @param item
	     *            the Object element to be stored at the specified position.
	     * 
	     * @return the element previously at the specified position.
	     * 
	     * @throws UnsupportedOperationException
	     *             if the <tt>set</tt> method is not supported by this List.
	     * @throws ClassCastException
	     *             if the class of the specified element prevents it from being
	     *             added to this list.
	     * @throws IllegalArgumentException
	     *             if some aspect of the specified element prevents it from
	     *             being added to this list.
	     * @throws IndexOutOfBoundsException
	     *             if the specified index is out of range (<tt>index &lt; 0 || index &gt;= size()</tt>).
	     */

	    public Object set(int index, Object item) {
	        throw new UnsupportedOperationException();
	    }

	    /**
	     * Inserts the specified element at the specified position in this list
	     * (optional operation). Shifts the element currently at that position (if
	     * any) and any subsequent elements to the right (adds one to their
	     * indices).
	     * <p>
	     * This implementation always throws an UnsupportedOperationException.
	     * 
	     * @param index
	     *            index at which the specified element is to be inserted.
	     * @param element
	     *            element to be inserted.
	     * @throws UnsupportedOperationException
	     *             if the <tt>add</tt> method is not supported by this list.
	     * @throws ClassCastException
	     *             if the class of the specified element prevents it from being
	     *             added to this list.
	     * @throws IllegalArgumentException
	     *             if some aspect of the specified element prevents it from
	     *             being added to this list.
	     * @throws IndexOutOfBoundsException
	     *             index is out of range (<tt>index &lt;
	     *		  0 || index &gt; size()</tt>).
	     */
	    public void add(int index, Object element) {
	        throw new UnsupportedOperationException();
	    }

	    /**
	     * item at the specified index.
	     * 
	     * @param index
	     *            index of item
	     * @return the item at the specified index.
	     * @throws IndexOutOfBoundsException
	     *             if index is not between 0 and size
	     */
	    abstract public Object get(int index);
		
	    /**
	     * Returns the number of elements in this list.
	     * 
	     * @return Number of items, or Interger.MAX_VALUE
	     */
	    abstract public int size();
		
	}

	public Object get(int index) {
		return strategy.get(index);
	}

	public int size() {
		return strategy.size();
	}
}
