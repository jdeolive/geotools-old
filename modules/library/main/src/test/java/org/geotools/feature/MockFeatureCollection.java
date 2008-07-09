/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2003-2008, Open Source Geospatial Foundation (OSGeo)
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
 *
 *    Created on August 12, 2003, 7:29 PM
 */
package org.geotools.feature;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.geotools.feature.visitor.FeatureVisitor;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.util.ProgressListener;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.opengis.filter.sort.SortBy;

import com.vividsolutions.jts.geom.Geometry;

/**
 * @author jamesm
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/gt/modules/library/main/src/test/java/org/geotools/feature/MockFeatureCollection.java $
 */
public class MockFeatureCollection implements org.geotools.feature.FeatureCollection<SimpleFeatureType, SimpleFeature> {

    /** Creates a new instance of MockFeatureCollection */
    public MockFeatureCollection() {
    }

    public void accepts(FeatureVisitor visitor, ProgressListener progress)
            throws IOException {
    }
    
    public void accepts(org.opengis.feature.FeatureVisitor visitor,
            org.opengis.util.ProgressListener progress) {
    }

    public void addListener(CollectionListener listener)
            throws NullPointerException {
    }

    public void close(FeatureIterator<SimpleFeature> close) {
    }

    public void close(Iterator close) {
    }

    public FeatureIterator<SimpleFeature> features() {
        return null;
    }

    public SimpleFeatureType getSchema() {
        return null;
    }

    public void removeListener(CollectionListener listener)
            throws NullPointerException {
    }

    public FeatureCollection<SimpleFeatureType, SimpleFeature> sort(SortBy order) {
        return null;
    }

    public FeatureCollection<SimpleFeatureType, SimpleFeature> subCollection(Filter filter) {
        return null;
    }

    public Iterator iterator() {
        return null;
    }

    public void purge() {
    }

    public boolean add(SimpleFeature o) {
        return false;
    }

    public boolean addAll(Collection c) {
        return false;
    }

    public void clear() {
    }

    public boolean contains(Object o) {
        return false;
    }

    public boolean containsAll(Collection c) {
        return false;
    }

    public boolean isEmpty() {
        return false;
    }

    public boolean remove(Object o) {
        return false;
    }

    public boolean removeAll(Collection c) {
        return false;
    }

    public boolean retainAll(Collection c) {
        return false;
    }

    public int size() {
        return 0;
    }

    public Object[] toArray() {
        return null;
    }

    public Object[] toArray(Object[] a) {
        return null;
    }

    public Object getAttribute(String path) {
        return null;
    }

    public Object getAttribute(int index) {
        return null;
    }

    public Object[] getAttributes(Object[] attributes) {
        return null;
    }

    public ReferencedEnvelope getBounds() {
        return null;
    }

    public Geometry getDefaultGeometry() {
        return null;
    }

    public SimpleFeatureType getFeatureType() {
        return null;
    }

    public String getID() {
        return null;
    }

    public int getNumberOfAttributes() {
        return 0;
    }

    public void setAttribute(int position, Object val) {
    }

    public void setAttribute(String path, Object attribute)
            throws IllegalAttributeException {
    }

    public void setDefaultGeometry(Geometry geometry)
            throws IllegalAttributeException {
    }

    public Object getAttribute(Name name) {
        return null;
    }

    public int getAttributeCount() {
        return 0;
    }

    public List<Object> getAttributes() {
        return null;
    }

    public SimpleFeatureType getType() {
        return null;
    }

    public void setAttribute(Name name, Object value) {
    }

    public void setAttributes(List<Object> values) {
    }

    public void setAttributes(Object[] values) {
    }

    public void setDefaultGeometry(Object geometry) {
    }

    public GeometryAttribute getDefaultGeometryProperty() {
        return null;
    }

    public void setDefaultGeometryProperty(GeometryAttribute geometryAttribute) {
    }

    public Collection<Property> getProperties() {
        return null;
    }
    
    public Collection<Property> getProperties(Name name) {
        return null;
    }

    public Collection<Property> getProperties(String name) {
        return null;
    }

    public Property getProperty(Name name) {
        return null;
    }

    public Property getProperty(String name) {
        return null;
    }

    public Collection<? extends Property> getValue() {
        return null;
    }

    public void setValue(Collection<Property> values) {
    }

    public AttributeDescriptor getDescriptor() {
        return null;
    }

    public Name getName() {
        return null;
    }

    public Map<Object, Object> getUserData() {
        return null;
    }

    public boolean isNillable() {
        return false;
    }

    public void setValue(Object newValue) {
    }
    public void validate() {
    }
   
}
