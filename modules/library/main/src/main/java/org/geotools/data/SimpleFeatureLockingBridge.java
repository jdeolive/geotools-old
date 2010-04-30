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

import org.geotools.data.simple.SimpleFeatureLocking;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;

/**
 * Bridges between {@link FeatureLocking<SimpleFeatureType, SimpleFeature>} and {@link SimpleFeatureLocking}
 */
class SimpleFeatureLockingBridge extends SimpleFeatureStoreBridge implements
        SimpleFeatureLocking {

    public SimpleFeatureLockingBridge(FeatureLocking<SimpleFeatureType, SimpleFeature> delegate) {
        super(delegate);
    }

    public int lockFeatures(Query query) throws IOException {
        return ((FeatureLocking<SimpleFeatureType, SimpleFeature>) delegate).lockFeatures(query);
    }

    public int lockFeatures(Filter filter) throws IOException {
        return ((FeatureLocking<SimpleFeatureType, SimpleFeature>) delegate).lockFeatures(filter);
    }

    public int lockFeatures() throws IOException {
        return ((FeatureLocking<SimpleFeatureType, SimpleFeature>) delegate).lockFeatures();
    }

    public void setFeatureLock(FeatureLock lock) {
        ((FeatureLocking<SimpleFeatureType, SimpleFeature>) delegate).setFeatureLock(lock);
    }

    public void unLockFeatures() throws IOException {
        ((FeatureLocking<SimpleFeatureType, SimpleFeature>) delegate).unLockFeatures();
        
    }

    public void unLockFeatures(Filter filter) throws IOException {
        ((FeatureLocking<SimpleFeatureType, SimpleFeature>) delegate).unLockFeatures(filter);
    }

    public void unLockFeatures(Query query) throws IOException {
        ((FeatureLocking<SimpleFeatureType, SimpleFeature>) delegate).unLockFeatures(query);
    }

}
