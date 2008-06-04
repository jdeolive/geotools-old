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
package org.geotools.feature.collection;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.geotools.feature.CollectionListener;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;

import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.visitor.FeatureVisitor;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.util.ProgressListener;
import org.opengis.feature.Feature;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.opengis.filter.sort.SortBy;

import com.vividsolutions.jts.geom.Geometry;

/**
 * A FeatureCollection which completley delegates to another FeatureCollection.
 * <p>
 * This class should be subclasses by classes which must somehow decorate 
 * another FeatureCollection<SimpleFeatureType, SimpleFeature> and override the relevant methods. 
 * </p>
 * @author Justin Deoliveira, The Open Planning Project, jdeolive@openplans.org
 * @since 2.5
 *
 */
public class DecoratingFeatureCollection<T extends FeatureType, F extends Feature> implements
        FeatureCollection<T, F> {

    /**
     * the delegate
     */
	protected FeatureCollection<T, F> delegate;

	protected DecoratingFeatureCollection(FeatureCollection<T, F> delegate) {
		this.delegate = delegate;
	}

    public void accepts(FeatureVisitor visitor, ProgressListener progress)
            throws IOException {
        delegate.accepts(visitor, progress);
    }

    public void accepts(org.opengis.feature.FeatureVisitor visitor,
            org.opengis.util.ProgressListener progress) {
    }
    
    public boolean add(F o) {
        return delegate.add(o);
    }

    public boolean addAll(Collection c) {
        return delegate.addAll(c);
    }

    public void addListener(CollectionListener listener)
            throws NullPointerException {
        delegate.addListener(listener);
    }

    public void clear() {
        delegate.clear();
    }

    public void close(FeatureIterator<F> close) {
        delegate.close(close);
    }

    public void close(Iterator<F> close) {
        delegate.close(close);
    }

    public boolean contains(Object o) {
        return delegate.contains(o);
    }

    public boolean containsAll(Collection c) {
        return delegate.containsAll(c);
    }

    public boolean equals(Object o) {
        return delegate.equals(o);
    }

    public FeatureIterator<F> features() {
        return delegate.features();
    }

    public Object getAttribute(int index) {
        return delegate.getAttribute(index);
    }

    public Object getAttribute(Name name) {
        return delegate.getAttribute(name);
    }

    public Object getAttribute(String path) {
        return delegate.getAttribute(path);
    }

    public int getAttributeCount() {
        return delegate.getAttributeCount();
    }

    public List<Object> getAttributes() {
        return delegate.getAttributes();
    }

//    public Object[] getAttributes(Object[] attributes) {
//        return delegate.getAttributes(attributes);
//    }

    public ReferencedEnvelope getBounds() {
        return delegate.getBounds();
    }

    public GeometryAttribute getDefaultGeometryProperty() {
        return delegate.getDefaultGeometryProperty();
    }

    public AttributeDescriptor getDescriptor() {
        return delegate.getDescriptor();
    }

    public SimpleFeatureType getFeatureType() {
        return delegate.getFeatureType();
    }

    public String getID() {
        return delegate.getID();
    }

    public Name getName() {
        return delegate.getName();
    }
    
    public Collection<Property> getProperties() {
        return delegate.getProperties();
    }

    public int getNumberOfAttributes() {
        return delegate.getAttributeCount();
    }

    public Collection<Property> getProperties(Name name) {
        return delegate.getProperties(name);
    }

    public Collection<Property> getProperties(String name) {
        return delegate.getProperties(name);
    }

    public Property getProperty(Name name) {
        return delegate.getProperty(name);
    }

    public Property getProperty(String name) {
        return delegate.getProperty(name);
    }

    public T getSchema() {
        return delegate.getSchema();
    }

    public SimpleFeatureType getType() {
        return delegate.getType();
    }

    public Map<Object, Object> getUserData() {
        return delegate.getUserData();
    }

    public Collection<? extends Property> getValue() {
        return delegate.getValue();
    }

    public int hashCode() {
        return delegate.hashCode();
    }

    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    public boolean isNillable() {
        return delegate.isNillable();
    }

    public Iterator iterator() {
        return delegate.iterator();
    }

    public void purge() {
        delegate.purge();
    }

    public boolean remove(Object o) {
        return delegate.remove(o);
    }

    public boolean removeAll(Collection c) {
        return delegate.removeAll(c);
    }

    public void removeListener(CollectionListener listener)
            throws NullPointerException {
        delegate.removeListener(listener);
    }

    public boolean retainAll(Collection c) {
        return delegate.retainAll(c);
    }

    public void setAttribute(int position, Object val) {
        delegate.setAttribute(position, val);
    }

    public void setAttribute(Name name, Object value) {
        delegate.setAttribute(name, value);
    }

    public void setAttribute(String path, Object attribute) {
        delegate.setAttribute(path, attribute);
    }

    public void setAttributes(List<Object> values) {
        delegate.setAttributes(values);
    }

    public void setAttributes(Object[] values) {
        delegate.setAttributes(values);
    }

    public void setDefaultGeometry(Geometry geometry)
            throws IllegalAttributeException {
        delegate.setDefaultGeometry(geometry);
    }

    public void setDefaultGeometry(Object geometry) {
        delegate.setDefaultGeometry(geometry);
    }

    public void setDefaultGeometryProperty(GeometryAttribute geometryAttribute) {
        delegate.setDefaultGeometryProperty(geometryAttribute);
    }

    public void setValue(Collection<Property> values) {
        delegate.setValue(values);
    }

    public void setValue(Object newValue) {
        delegate.setValue(newValue);
    }

    public int size() {
        return delegate.size();
    }

    public FeatureCollection<T, F> sort(SortBy order) {
        return delegate.sort(order);
    }

    public FeatureCollection<T, F> subCollection(Filter filter) {
        return delegate.subCollection(filter);
    }

    public Object[] toArray() {
        return delegate.toArray();
    }

    public Object[] toArray(Object[] a) {
        return delegate.toArray(a);
    }
	
	public Object getDefaultGeometry() {
	    return delegate.getDefaultGeometry();
	}
}
