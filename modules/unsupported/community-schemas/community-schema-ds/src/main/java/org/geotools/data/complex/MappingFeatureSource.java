/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2004-2008, Open Source Geospatial Foundation (OSGeo)
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

package org.geotools.data.complex;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import org.geotools.catalog.GeoResourceInfo;
import org.geotools.data.DataStore;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureListener;
import org.geotools.data.Query;
import org.geotools.data.QueryCapabilities;
import org.geotools.data.Transaction;
import org.geotools.data.feature.FeatureSource2;
import org.geotools.data.feature.adapter.GTFeatureTypeAdapter;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.iso.collection.AbstractFeatureCollection;
import org.opengis.feature.type.FeatureCollectionType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.opengis.filter.capability.FilterCapabilities;

import com.vividsolutions.jts.geom.Envelope;

/**
 * A FeatureSource2 that uses a
 * {@linkplain org.geotools.data.complex.FeatureTypeMapping} to perform Feature
 * fetching.
 * 
 * <p>
 * Note that the number of Features available from a MappingFeatureReader may
 * not match the number of features that resulted of executing the incoming
 * query over the surrogate FeatureSource. This will be the case when grouping
 * attributes has configured on the FeatureTypeMapping this reader is based on.
 * </p>
 * <p>
 * When a MappingFeatureReader is created, a delegated FeatureIterator will be
 * created based on the information provided by the FeatureTypeMapping object.
 * That delegate reader will be specialized in applying the appropiate mapping
 * stratagy based on wether grouping has to be performed or not.
 * </p>
 * 
 * @author Gabriel Roldan, Axios Engineering
 * @version $Id$
 * @source $URL:
 *         http://svn.geotools.org/geotools/branches/2.4.x/modules/unsupported/community-schemas/community-schema-ds/src/main/java/org/geotools/data/complex/MappingFeatureSource.java $
 * @since 2.4
 * @see org.geotools.data.complex.DefaultMappingFeatureIterator
 * @see org.geotools.data.complex.GroupingFeatureIterator
 */
class MappingFeatureSource implements FeatureSource2 {

    private ComplexDataStore store;

    private FeatureTypeMapping mappings;

    private org.geotools.feature.FeatureType gtType;

    public MappingFeatureSource(ComplexDataStore store, FeatureTypeMapping mapping) {
        this.store = store;
        this.mappings = mapping;
        FeatureType type = (FeatureType) mapping.getTargetFeature().type();
        gtType = new GTFeatureTypeAdapter(type);
    }

    public void addFeatureListener(FeatureListener listener) {
        throw new UnsupportedOperationException();
    }

    public Envelope getBounds() throws IOException {
        Envelope bounds = store.getBounds(namedQuery(Filter.INCLUDE, Integer.MAX_VALUE));
        return bounds;
    }

    private DefaultQuery namedQuery(Filter filter, int countLimit) {
        DefaultQuery query = new DefaultQuery();
        query.setTypeName(getName().getLocalPart());
        query.setFilter(filter);
        query.setMaxFeatures(countLimit);
        return query;
    }

    private DefaultQuery namedQuery(Query query) {
        DefaultQuery namedQuery = namedQuery(query.getFilter(), query.getMaxFeatures());
        namedQuery.setPropertyNames(query.getPropertyNames());
        namedQuery.setCoordinateSystem(query.getCoordinateSystem());
        namedQuery.setHandle(query.getHandle());
        namedQuery.setMaxFeatures(query.getMaxFeatures());
        namedQuery.setSortBy(query.getSortBy());
        return namedQuery;
    }

    public Envelope getBounds(Query query) throws IOException {
        DefaultQuery namedQuery = namedQuery(query);
        Envelope bounds = store.getBounds(namedQuery);
        return bounds;
    }

    public int getCount(Query query) throws IOException {
        DefaultQuery namedQuery = namedQuery(query);
        int count = store.getCount(namedQuery);
        return count;
    }

    public DataStore getDataStore() {
        return store;
    }

    public org.geotools.feature.FeatureType getSchema() {
        return gtType;
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

    public void removeFeatureListener(FeatureListener listener) {
        throw new UnsupportedOperationException("this is a read only feature source");
    }

    public Collection content() {
        return content(Filter.INCLUDE);
    }

    public Collection content(String query, String queryLanguage) {
        throw new UnsupportedOperationException();
    }

    public Collection content(final Filter filter) {
        return content(filter, Integer.MAX_VALUE);
    }

    /**
     * @return {@link org.opengis.feature.FeatureCollection}
     */
    public Collection content(final Filter filter, final int countLimit) {
        Collection collection = new AbstractFeatureCollection(null, (FeatureCollectionType) null,
                null) {
            public int size() {
                int count;
                try {
                    count = store.getCount(namedQuery(filter, countLimit));
                } catch (IOException e) {
                    e.printStackTrace();
                    count = -1;
                }
                return count;
            }

            /**
             * @param close
             *            AbstractMappingFeatureIterator as returned by
             *            openIterator()
             */
            protected void closeIterator(Iterator close) throws IOException {
                AbstractMappingFeatureIterator iterator = (AbstractMappingFeatureIterator) close;
                iterator.close();
            }

            /**
             * @return an AbstractMappingFeatureIterator
             */
            protected Iterator openIterator() throws IOException {
                AbstractMappingFeatureIterator iterator;
                Query query = namedQuery(filter, countLimit);
                try {
                    if (0 == mappings.getGroupByAttNames().size()) {
                        iterator = new DefaultMappingFeatureIterator(store, mappings, query);
                    } else {
                        iterator = new GroupingFeatureIterator3(store, mappings, query);
                    }
                } catch (IOException e) {
                    throw (RuntimeException) new RuntimeException().initCause(e);
                }
                return iterator;
            }
        };
        return collection;
    }

    public Object describe() {
        return mappings.getTargetFeature();
    }

    public void dispose() {
        // TODO Auto-generated method stub

    }

    public FilterCapabilities getFilterCapabilities() {
        throw new UnsupportedOperationException();
    }

    public GeoResourceInfo getInfo() {
        throw new UnsupportedOperationException();
    }

    public Name getName() {
        Name name = mappings.getTargetFeature().getName();
        return name;
    }

    public void setTransaction(Transaction t) {
        throw new UnsupportedOperationException();
    }

    public Set getSupportedHints() {
        return Collections.EMPTY_SET;
    }

    public QueryCapabilities getQueryCapabilities() {
        return new QueryCapabilities();
    }

}
