/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, GeoTools Project Managment Committee (PMC)
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import com.vividsolutions.jts.geom.Envelope;
import org.opengis.filter.Filter;
import org.geotools.caching.firstdraft.DataCache;
import org.geotools.caching.firstdraft.FeatureIndex;
import org.geotools.caching.firstdraft.InternalStore;
import org.geotools.data.DataStore;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureListener;
import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.data.view.DefaultView;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureType;
import org.geotools.feature.SchemaException;
import org.geotools.filter.spatial.BBOXImpl;
import org.geotools.index.Data;
import org.geotools.index.DataDefinition;
import org.geotools.index.LockTimeoutException;
import org.geotools.index.TreeException;
import org.geotools.index.rtree.PageStore;
import org.geotools.index.rtree.RTree;
import org.geotools.index.rtree.memory.MemoryPageStore;


/** An implementation of FeatureIndex, that stores every thing needed in memory.
 *
 * @task handle size limit properly (currently, we do nothing).
 *
 * @author Christophe Rousson, SoC 2007, CRG-ULAVAL
 *
 */
public class MemoryFeatureIndex implements FeatureIndex {
    private static final DataDefinition df = createDataDefinition();
    private final DataCache parent;
    private RTree tree = createTree();
    private final InternalStore internalStore;
    private final int capacity;
    private int indexCount = 0;
    private final FeatureType type;
    private Query currentQuery = Query.ALL;

    /** Creates a new index that can store features of given type.
     *
     * @param type FeatureType of features this index will store
     * @param capacity maximum number of features we can store.
     */
    public MemoryFeatureIndex(DataCache parent, FeatureType type, int capacity) {
        this.parent = parent;
        this.internalStore = new SimpleHashMapInternalStore();
        this.capacity = capacity;
        this.type = type;
    }

    /* (non-Javadoc)
     * @see org.geotools.caching.FeatureIndex#add(org.geotools.feature.Feature)
     */
    public void add(Feature f) {
        if (internalStore.contains(f)) {
            return;
        }

        Data d = new Data(df);

        try {
            d.addValue(f.getID());
            tree.insert(f.getBounds(), d);
            internalStore.put(f);
            indexCount++;
        } catch (TreeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (LockTimeoutException e) {
            e.printStackTrace();
        }
    }

    /* (non-Javadoc)
     * @see org.geotools.caching.FeatureIndex#clear()
     */
    public void clear() {
        try {
            tree.close();
        } catch (TreeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        tree = createTree();
        internalStore.clear();
    }

    public void flush() {
        // TODO Auto-generated method stub
    }

    /* (non-Javadoc)
     * @see org.geotools.caching.FeatureIndex#get(java.lang.String)
     */
    public Feature get(String featureID) {
        /*Filter f = FilterFactoryFinder.createFilterFactory().createFidFilter(featureID) ;
           try {
                   FeatureSource fs = internalStore.getView(new DefaultQuery(type.getTypeName(), f)) ;
                   FeatureCollection fc = fs.getFeatures() ;
                   if (fc.isEmpty())
                           // TODO throw appropriate exception, so we can handle the case
                           return null ;
                   FeatureIterator i = fc.features() ;
                   Feature ret = i.next() ;
                   fc.close(i) ;
                   return ret ;
           } catch (IOException e) {
                   // TODO Auto-generated catch block
                   e.printStackTrace();
           } catch (SchemaException e) {
                   // TODO Auto-generated catch block
                   e.printStackTrace();
           }
           return null ;*/
        Feature f = (Feature) internalStore.get(featureID);

        // TODO test if we get null, and do something ...
        return f;
    }

    /* (non-Javadoc)
     * @see org.geotools.caching.FeatureIndex#getFeatures(org.geotools.data.Query)
     */
    public FeatureCollection getFeatures(Query q) throws IOException {
        Filter f = q.getFilter();

        return getFeatures(f);
    }

    /** Preselects features from the spatial index.
     * If query is not a BBox query, returns all features in the cache.
     *
     * @param q a Query
     * @return a collection of features within or intersecting query bounds.
     */
    private Collection getCandidates(Filter f) {
        if (f instanceof BBOXImpl) {
            List candidates = new ArrayList();
            BBOXImpl bb = (BBOXImpl) f;
            Envelope env = new Envelope(bb.getMinX(), bb.getMaxX(), bb.getMinY(), bb.getMaxY());

            try {
                List results = tree.search(env);

                for (Iterator r = results.iterator(); r.hasNext();) {
                    Data d = (Data) r.next();
                    String fid = (String) d.getValue(0);
                    candidates.add(internalStore.get(fid));
                }

                return candidates;
            } catch (TreeException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (LockTimeoutException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        return internalStore.getAll();
    }

    /* (non-Javadoc)
     * @see org.geotools.caching.FeatureIndex#remove(java.lang.String)
     */
    public void remove(String featureID) {
        Envelope env = ((Feature) internalStore.get(featureID)).getBounds();

        try {
            tree.delete(env);
        } catch (TreeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (LockTimeoutException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        internalStore.remove(featureID);
        indexCount--;
    }

    /* (non-Javadoc)
     * @see org.geotools.caching.FeatureIndex#getView(org.geotools.data.Query)
     */
    public FeatureSource getView(Query q) throws SchemaException {
        return new DefaultView(this, q);
    }

    /** R-tree to keep envelopes of stored features.
     *
     * @return a R-tree, memory mapped.
     */
    private static RTree createTree() {
        try {
            PageStore ps = new MemoryPageStore(df, 8, 4, PageStore.SPLIT_QUADRATIC);
            RTree tree = new RTree(ps);

            return tree;
        } catch (TreeException e) {
            throw (RuntimeException) new RuntimeException().initCause(e);
        }
    }

    /** Data definition of data we feed into the R-tree.
     * What we store is the ID of features (we assume ID are less than 256 chars).
     * @return data definition
     */
    private static DataDefinition createDataDefinition() {
        DataDefinition df = new DataDefinition("US-ASCII");
        df.addField(256);

        return df;
    }

    /* (non-Javadoc)
     * @see org.geotools.data.FeatureSource#addFeatureListener(org.geotools.data.FeatureListener)
     */
    public void addFeatureListener(FeatureListener arg0) {
        // TODO Auto-generated method stub
    }

    /* (non-Javadoc)
     * @see org.geotools.data.FeatureSource#getBounds()
     */
    public Envelope getBounds() throws IOException {
        /*try {
           return tree.getBounds();
           } catch (TreeException e) {
               throw (IOException) new IOException().initCause(e);
           }*/
        return parent.getFeatureSource(type.getTypeName()).getBounds();
    }

    /* (non-Javadoc)
     * @see org.geotools.data.FeatureSource#getBounds(org.geotools.data.Query)
     */
    public Envelope getBounds(Query q) throws IOException {
        FeatureCollection fc = this.getFeatures(q);

        return fc.getBounds();
    }

    /* (non-Javadoc)
     * @see org.geotools.data.FeatureSource#getCount(org.geotools.data.Query)
     */
    public int getCount(Query q) throws IOException {
        Query qprime = adapt(q);

        try {
            this.parent.getView(qprime);
        } catch (SchemaException e) {
            throw (IOException) new IOException().initCause(e);
        }

        FeatureCollection fc = this.getFeatures(qprime);

        return fc.size();
    }

    /* (non-Javadoc)
     * @see org.geotools.data.FeatureSource#getDataStore()
     */
    public DataStore getDataStore() {
        // TODO Auto-generated method stub
        return this.parent;
    }

    /* (non-Javadoc)
     * @see org.geotools.data.FeatureSource#getFeatures()
     */
    public FeatureCollection getFeatures() throws IOException {
        // TODO Auto-generated method stub
        return this.getFeatures(Query.ALL);
    }

    /* (non-Javadoc)
     * @see org.geotools.data.FeatureSource#getFeatures(org.opengis.filter.Filter)
     */
    public FeatureCollection getFeatures(Filter f) throws IOException {
        FeatureCollection fc = new DefaultFeatureCollection(null, type);
        boolean refine = (f instanceof BBOXImpl);

        for (Iterator i = getCandidates(f).iterator(); i.hasNext();) {
            Feature next = (Feature) i.next();

            if (refine || f.evaluate(next)) {
                fc.add(next);
            }
        }

        return fc;
    }

    /* (non-Javadoc)
     * @see org.geotools.data.FeatureSource#getSchema()
     */
    public FeatureType getSchema() {
        // TODO Auto-generated method stub
        return type;
    }

    /* (non-Javadoc)
     * @see org.geotools.data.FeatureSource#removeFeatureListener(org.geotools.data.FeatureListener)
     */
    public void removeFeatureListener(FeatureListener arg0) {
        // TODO Auto-generated method stub
    }

    private Query adapt(Query q) {
        if (q.getTypeName() == null) {
            DefaultQuery r = new DefaultQuery(q);
            r.setTypeName(this.type.getTypeName());

            return r;
        } else {
            return q;
        }
    }

    public Set getSupportedHints() {
        return new HashSet();
    }
}
