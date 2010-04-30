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
 */
package org.geotools.data;

import java.io.IOException;
import java.util.List;

import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.FeatureCollection;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.filter.Filter;
import org.opengis.filter.identity.FeatureId;

/**
 * Bridges between {@link FeatureStore<SimpleFeatureType, SimpleFeature>} and {@link SimpleFeatureStore}
 */
class SimpleFeatureStoreBridge extends SimpleFeatureSourceBridge implements
        SimpleFeatureStore {

    public SimpleFeatureStoreBridge(FeatureStore<SimpleFeatureType, SimpleFeature> delegate) {
        super(delegate);
    }

    public List<FeatureId> addFeatures(
            FeatureCollection<SimpleFeatureType, SimpleFeature> collection) throws IOException {
        return ((FeatureStore<SimpleFeatureType, SimpleFeature>) delegate).addFeatures(collection);
    }

    public Transaction getTransaction() {
        return ((FeatureStore<SimpleFeatureType, SimpleFeature>) delegate).getTransaction();
    }

    public void modifyFeatures(AttributeDescriptor[] type, Object[] value, Filter filter)
            throws IOException {
        ((FeatureStore<SimpleFeatureType, SimpleFeature>) delegate).modifyFeatures(type, value,
                filter);
    }

    public void modifyFeatures(AttributeDescriptor type, Object value, Filter filter)
            throws IOException {
        ((FeatureStore<SimpleFeatureType, SimpleFeature>) delegate).modifyFeatures(type, value,
                filter);
    }

    public void removeFeatures(Filter filter) throws IOException {
        ((FeatureStore<SimpleFeatureType, SimpleFeature>) delegate).removeFeatures(filter);
    }

    public void setFeatures(FeatureReader<SimpleFeatureType, SimpleFeature> reader)
            throws IOException {
        ((FeatureStore<SimpleFeatureType, SimpleFeature>) delegate).setFeatures(reader);
    }

    public void setTransaction(Transaction transaction) {
        ((FeatureStore<SimpleFeatureType, SimpleFeature>) delegate).setTransaction(transaction);
    }

}
