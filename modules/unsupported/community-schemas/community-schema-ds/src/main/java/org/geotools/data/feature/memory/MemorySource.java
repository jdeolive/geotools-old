/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2005-2006, GeoTools Project Managment Committee (PMC)
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
 */
package org.geotools.data.feature.memory;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import org.geotools.catalog.GeoResourceInfo;
import org.geotools.data.DataStore;
import org.geotools.data.FeatureListener;
import org.geotools.data.Query;
import org.geotools.data.QueryCapabilities;
import org.geotools.data.Transaction;
import org.geotools.data.feature.FeatureSource2;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.iso.collection.AbstractFeatureCollection;
import org.geotools.feature.iso.collection.MemorySimpleFeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.Feature;
import org.opengis.feature.type.FeatureCollectionType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.opengis.filter.capability.FilterCapabilities;

import com.vividsolutions.jts.geom.Envelope;

/**
 * 
 * @author Gabriel Roldan, Axios Engineering
 * @version $Id$
 * @source $URL$
 * @since 2.4
 */
public class MemorySource implements FeatureSource2 {

    private FeatureType type;

    private Collection content;

    private MemoryDataAccess dataStore;

    public MemorySource(MemoryDataAccess dataStore, FeatureType type, Collection collection) {
        this.dataStore = dataStore;
        this.type = type;
        this.content = collection;
    }

    public Collection content() {
        MemorySimpleFeatureCollection collection = new MemorySimpleFeatureCollection(null, null);
        collection.addAll(content);
        return collection;
    }

    public Collection content(String query, String queryLanguage) {
        throw new UnsupportedOperationException();
    }

    public Collection content(Filter filter) {
        return content(filter, Integer.MAX_VALUE);
    }

    private class LimitingFeatureCollection extends AbstractFeatureCollection {

        private final Collection contents;

        private final int maxFeatures;

        private int cachedSize = Integer.MIN_VALUE;

        public LimitingFeatureCollection(Collection contents,
                int maxFeatures) {
            super(null, (FeatureCollectionType) null, null);
            this.contents = contents;
            this.maxFeatures = maxFeatures;
        }

        protected Iterator openIterator() throws IOException {
            return new LimitingIterator();
        }

        protected void closeIterator(Iterator close) throws IOException {
            LimitingIterator iterator = (LimitingIterator) close;
            iterator.close();
        }

        public int size() {
            if (cachedSize == Integer.MIN_VALUE) {
                int contentSize = contents.size();
                cachedSize = Math.min(contentSize, maxFeatures);
            }
            return cachedSize;
        }

        /**
         * Iterator wraper that limits the number of features to maxFeatures
         */
        class LimitingIterator implements Iterator {
            private int count = 0;

            Iterator subject = LimitingFeatureCollection.this.contents.iterator();

            public boolean hasNext() {
                boolean hasNext = subject.hasNext();
                return hasNext && count <= LimitingFeatureCollection.this.maxFeatures;
            }

            public Object next() {
                Object next = subject.next();
                count++;
                return next;
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }

            public void close() {
                ((org.opengis.feature.FeatureCollection)LimitingFeatureCollection.this.contents).close(subject);
            }
        }
    }

    public Collection content(Filter filter, final int countLimit) {
        org.opengis.feature.FeatureCollection collection = (org.opengis.feature.FeatureCollection) content();
        if(!Filter.INCLUDE.equals(filter)){
            collection = collection.subCollection(filter);
        }
        if(countLimit < Integer.MAX_VALUE){
            collection = new LimitingFeatureCollection((Collection)collection, countLimit);
        }
        return (Collection)collection;
    }

    public Object describe() {
        return dataStore.describe(type.getName());
    }

    public void dispose() {
    }

    public FilterCapabilities getFilterCapabilities() {
        throw new UnsupportedOperationException();
    }

    public GeoResourceInfo getInfo() {
        throw new UnsupportedOperationException();
    }

    public Name getName() {
        return type.getName();
    }

    public void setTransaction(Transaction t) {
        throw new UnsupportedOperationException();
    }

    public void addFeatureListener(FeatureListener listener) {
        throw new UnsupportedOperationException();
    }

    public Envelope getBounds() throws IOException {
        return getBounds(Filter.INCLUDE);
    }

    public Envelope getBounds(Query query) throws IOException {
        return getBounds(query.getFilter());
    }

    private Envelope getBounds(Filter filter) throws IOException {
        org.opengis.feature.FeatureCollection collection = (org.opengis.feature.FeatureCollection) content(filter);
        Feature f;
        ReferencedEnvelope env = new ReferencedEnvelope(this.type.getCRS());
        Iterator it = collection.iterator();
        for (; it.hasNext();) {
            f = (Feature) it.next();
            env.include(f.getBounds());
        }
        collection.close(it);
        return env;
    }

    public int getCount(Query query) throws IOException {
        org.opengis.feature.FeatureCollection collection;
        collection = (org.opengis.feature.FeatureCollection) content(query.getFilter());
        int count = 0;
        Iterator it = collection.iterator();
        for (; it.hasNext();) {
            it.next();
            count++;
        }
        collection.close(it);
        return count;
    }

    public DataStore getDataStore() {
        return dataStore;
    }

    public FeatureCollection getFeatures(Query query) throws IOException {
        throw new UnsupportedOperationException();
    }

    public FeatureCollection getFeatures(Filter filter) throws IOException {
        throw new UnsupportedOperationException();
    }

    public FeatureCollection getFeatures() throws IOException {
        throw new UnsupportedOperationException();
    }

    public org.geotools.feature.FeatureType getSchema() {
        throw new UnsupportedOperationException();
    }

    public void removeFeatureListener(FeatureListener listener) {
        throw new UnsupportedOperationException();
    }

    public Set getSupportedHints() {
        return Collections.EMPTY_SET;
    }

    public QueryCapabilities getQueryCapabilities() {
        return new QueryCapabilities();
    }

}
