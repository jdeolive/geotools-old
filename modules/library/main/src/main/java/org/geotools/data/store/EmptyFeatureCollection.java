/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2008, GeoTools Project Managment Committee (PMC)
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
package org.geotools.data.store;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.Envelope;

public class EmptyFeatureCollection extends DataFeatureCollection {

	/**
	 * null bounds
	 */
	static ReferencedEnvelope bounds = new ReferencedEnvelope(new Envelope(),null);
	static {
		bounds.setToNull();
	}
	
	public EmptyFeatureCollection( SimpleFeatureType schema ) {
		super(null,schema);
	}
	
	
	public ReferencedEnvelope getBounds() {
		return bounds;
	}

	public int getCount() throws IOException {
		return 0;
	}

	protected Iterator openIterator() {
		return new EmptyIterator();
	}
	
	protected void closeIterator(Iterator close) {
		//do nothing
	}
	
	
	
	//read only access
	public boolean add(SimpleFeature object) {
		return false;
	}
	
	public boolean addAll(Collection collection) {
		return false;
	}
	
	public boolean remove(Object object) {
		return false;
	}
	
	public boolean removeAll(Collection collection) {
		return false;
	}
	
//	
//	public FeatureIterator<SimpleFeature> features() {
//		return new IteratorFeatureIterator( iterator() );
//	}
//
//	public void close(FeatureIterator close) {
//		close.close();
//	}
//
//	public Iterator iterator() {
//		return new EmptyIterator();
//	}
//	
//	public void close(Iterator close) {
//	}
//
//	public void addListener(CollectionListener listener)
//			throws NullPointerException {
//		// TODO Auto-generated method stub
//
//	}
//
//	public void removeListener(CollectionListener listener)
//			throws NullPointerException {
//		// TODO Auto-generated method stub
//
//	}
//
//	public FeatureType getFeatureType() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	public FeatureType getSchema() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	public void accepts(FeatureVisitor visitor, ProgressListener progress)
//			throws IOException {
//		// TODO Auto-generated method stub
//
//	}
//
//	public FeatureCollection<SimpleFeatureType, SimpleFeature> subCollection(Filter filter) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	public FeatureList sort(SortBy order) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//
//
//	public void purge() {
//		// TODO Auto-generated method stub
//
//	}
//
//	public int size() {
//		// TODO Auto-generated method stub
//		return 0;
//	}
//
//	public void clear() {
//		// TODO Auto-generated method stub
//
//	}
//
//	public boolean isEmpty() {
//		// TODO Auto-generated method stub
//		return false;
//	}
//
//	public Object[] toArray() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	public boolean add(Object o) {
//		// TODO Auto-generated method stub
//		return false;
//	}
//
//	public boolean contains(Object o) {
//		// TODO Auto-generated method stub
//		return false;
//	}
//
//	public boolean remove(Object o) {
//		// TODO Auto-generated method stub
//		return false;
//	}
//
//	public boolean addAll(Collection c) {
//		// TODO Auto-generated method stub
//		return false;
//	}
//
//	public boolean containsAll(Collection c) {
//		// TODO Auto-generated method stub
//		return false;
//	}
//
//	public boolean removeAll(Collection c) {
//		// TODO Auto-generated method stub
//		return false;
//	}
//
//	public boolean retainAll(Collection c) {
//		// TODO Auto-generated method stub
//		return false;
//	}
//
//	public Object[] toArray(Object[] a) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	public  FeatureReader<SimpleFeatureType, SimpleFeature> reader() throws IOException {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	public String getID() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	public Object[] getAttributes(Object[] attributes) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	public Object getAttribute(String xPath) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	public Object getAttribute(int index) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	public void setAttribute(int position, Object val)
//			throws IllegalAttributeException, ArrayIndexOutOfBoundsException {
//		// TODO Auto-generated method stub
//
//	}
//
//	public int getNumberOfAttributes() {
//		// TODO Auto-generated method stub
//		return 0;
//	}
//
//	public void setAttribute(String xPath, Object attribute)
//			throws IllegalAttributeException {
//		// TODO Auto-generated method stub
//
//	}
//
//	public Geometry getDefaultGeometry() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	public void setDefaultGeometry(Geometry geometry)
//			throws IllegalAttributeException {
//		// TODO Auto-generated method stub
//
//	}
//
//	public Envelope getBounds() {
//		Envelope bounds = new Envelope();
//		bounds.setToNull();
//		
//		return bounds;
//	}

}
