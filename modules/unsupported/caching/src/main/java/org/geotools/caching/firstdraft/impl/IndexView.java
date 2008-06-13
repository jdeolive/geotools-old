/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2007-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.caching.firstdraft.impl;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import com.vividsolutions.jts.geom.Envelope;
import org.opengis.filter.Filter;
import org.geotools.caching.firstdraft.FeatureIndex;
import org.geotools.data.DataStore;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureListener;
import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureType;


/** Associates a FeatureIndex with a query.
 * This is used to get a view from the index,
 * ie. a subset of the features containes in the index.
 *
 * If more features are added to the underlying FeatureIndex,
 * after instance is created, instance will reflect new set,
 * as it delegates actual process to the FeatureIndex.
 *
 * @task implements private method : Query restrict(Query q)
 *
 * @author Christophe Rousson, SoC 2007, CRG-ULAVAL
 *
 */
public class IndexView implements FeatureSource {
    private final FeatureIndex index;
    private final Query view;

    /** Associates FeatureIndex with Query.
     *
     * @param index a FeatureIndex
     * @param q Query
     */
    public IndexView(FeatureIndex index, Query q) {
        this.index = index;
        this.view = q;
    }

    /** Creates a dummy view with all features,
     * yielding all features within the FeatureIndex
     *
     * @param index FeatureIndex on which to build the view.
     *
     */
    public IndexView(FeatureIndex index) {
        this(index, Query.ALL);
    }

    /********************************************************
     **
     ** Next methods delegate process to FeatureIndex.
     **
     ********************************************************/
    public void addFeatureListener(FeatureListener arg0) {
        // TODO Auto-generated method stub
    }

    public Envelope getBounds() throws IOException {
        // TODO Auto-generated method stub
        return index.getBounds(view);
    }

    public Envelope getBounds(Query q) throws IOException {
        // TODO Auto-generated method stub
        return index.getBounds(restrict(q));
    }

    public int getCount(Query q) throws IOException {
        // TODO Auto-generated method stub
        return index.getCount(restrict(q));
    }

    public DataStore getDataStore() {
        // TODO Auto-generated method stub
        return index.getDataStore();
    }

    /* (non-Javadoc)
     * @see org.geotools.data.FeatureSource#getFeatures()
     *
     * Get FeatureCollection associated with view from the FeatureIndex.
     *
     */
    public FeatureCollection getFeatures() throws IOException {
        // TODO Auto-generated method stub
        return index.getFeatures(view);
    }

    public FeatureCollection getFeatures(Query q) throws IOException {
        return index.getFeatures(restrict(q));
    }

    public FeatureCollection getFeatures(Filter f) throws IOException {
        // TODO Auto-generated method stub
        return index.getFeatures(new DefaultQuery("", f));
    }

    public FeatureType getSchema() {
        return index.getSchema();
    }

    public void removeFeatureListener(FeatureListener arg0) {
        // TODO Auto-generated method stub
    }

    /** Actually does nothing,
     * but ideally would return a new query
     * defined by ((view query) AND (input query))
     *
     * @param q a Query
     * @return new Query
     */
    private Query restrict(Query q) {
        // TODO combine view query and new query
        return q;
    }

    public Set getSupportedHints() {
        return new HashSet();
    }
}
