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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import com.vividsolutions.jts.geom.Envelope;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.geotools.caching.firstdraft.FeatureCache;
import org.geotools.caching.firstdraft.FeatureCacheException;
import org.geotools.caching.firstdraft.spatialindex.rtree.Index;
import org.geotools.caching.firstdraft.spatialindex.rtree.Leaf;
import org.geotools.caching.firstdraft.spatialindex.rtree.RTree;
import org.geotools.caching.firstdraft.spatialindex.spatialindex.IData;
import org.geotools.caching.firstdraft.spatialindex.spatialindex.IEntry;
import org.geotools.caching.firstdraft.spatialindex.spatialindex.INode;
import org.geotools.caching.firstdraft.spatialindex.spatialindex.INodeCommand;
import org.geotools.caching.firstdraft.spatialindex.spatialindex.IQueryStrategy;
import org.geotools.caching.firstdraft.spatialindex.spatialindex.IVisitor;
import org.geotools.caching.firstdraft.spatialindex.spatialindex.Region;
import org.geotools.caching.firstdraft.spatialindex.spatialindex.SpatialIndex;
import org.geotools.caching.firstdraft.spatialindex.storagemanager.MemoryStorageManager;
import org.geotools.caching.firstdraft.spatialindex.storagemanager.PropertySet;
import org.geotools.caching.firstdraft.util.FilterSplitter;
import org.geotools.data.DataStore;
import org.geotools.data.DataUtilities;
import org.geotools.data.FeatureListener;
import org.geotools.data.FeatureReader;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.data.store.EmptyFeatureCollection;
import org.geotools.feature.AttributeType;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.FeatureType;
import org.geotools.feature.SchemaException;
import org.geotools.filter.FilterFactoryImpl;
import org.geotools.filter.spatial.BBOXImpl;


/** An implementation of FeatureCache :
 *  <ul><li>with in memory storage
 *      <li>uses a RTree to index features
 *      <li>uses a SpatialQueryTracker to track query bounds
 *  </ul>
 *
 * @param ds the DataStore to cache
 * @param t the FeatureType
 *
 * TODO: add constructor InMemoryFeatureCache(FeatureStore)
 *
 * @author Christophe Rousson, SoC 2007, CRG-ULAVAL
 *
 */
public class InMemoryFeatureCache implements FeatureCache {
    protected Transaction transaction = Transaction.AUTO_COMMIT;
    protected final DataStore ds;
    protected final HashMap store;
    protected final Hashtable nodes;
    protected final FeatureType type;
    protected final SpatialQueryTracker tracker;
    protected final RTree index;
    protected int capacity;
    protected int cacheReads = 0;
    protected int storeReads = 0;
    protected int evictions = 0;

    /** Create a new InMemoryFeatureCache
     *
     * @param ds the source DataStore for features
     * @param t FeatureType to cache
     * @throws FeatureCacheException if DataStore does not have type t, or if IOException occurs
     */
    public InMemoryFeatureCache(DataStore ds, FeatureType t, int capacity)
        throws FeatureCacheException {
        FeatureType dstype = null;

        try {
            dstype = ds.getSchema(t.getTypeName());
        } catch (IOException e) {
            throw (FeatureCacheException) new FeatureCacheException().initCause(e);
        }

        if ((dstype == null) || !dstype.equals(t)) {
            throw new FeatureCacheException(new SchemaException("Datastore does not have type "
                    + t.getTypeName()));
        }

        this.ds = ds;
        this.type = t;
        this.store = new HashMap();
        this.nodes = new Hashtable();
        this.tracker = new SpatialQueryTracker();
        this.capacity = capacity;

        PropertySet ps = new PropertySet();
        ps.setProperty("TreeVariant", new Integer(SpatialIndex.RtreeVariantLinear));
        ps.setProperty("LeafCapacity", new Integer(capacity / 10));

        MemoryStorageManager sm = new MemoryStorageManager();
        this.index = new RTree(ps, sm);
        this.index.addDeleteNodeCommand(new INodeCommand() {
                public void execute(INode n) {
                    nodes.remove(new Integer(n.getIdentifier()));
                }
            });
        this.index.addReadNodeCommand(new INodeCommand() {
                public void execute(INode n) {
                    NodeCacheEntry entry = (NodeCacheEntry) nodes.get(new Integer(n.getIdentifier()));

                    if (entry == null) {
                        entry = new NodeCacheEntry(n);
                        nodes.put(new Integer(n.getIdentifier()), entry);
                    }

                    entry.hit();
                }
            });
        this.index.addWriteNodeCommand(new INodeCommand() {
                public void execute(INode n) {
                    NodeCacheEntry entry = new NodeCacheEntry(n);
                    nodes.put(new Integer(n.getIdentifier()), entry);
                }
            });
    }

    public void clear() {
        throw new UnsupportedOperationException();
    }

    public void evict() {
        //System.out.println("before = " + store.size()) ;
        //System.out.println(index.getStatistics()) ;
        EvictionQueryStrategy strategy = new EvictionQueryStrategy();
        index.queryStrategy(strategy);
        strategy.doDelete();

        //System.out.println("after = " + store.size()) ;
        //System.out.println(index.getStatistics()) ;
        //System.out.println("======================") ;
    }

    /* (non-Javadoc)
     * @see org.geotools.caching.FeatureCache#get(java.lang.String)
     */
    public Feature get(String id) throws FeatureCacheException {
        /*SimpleFeatureCacheEntry entry = (SimpleFeatureCacheEntry) store.get(id) ;
           Feature f = null ;*/
        Feature f = (Feature) store.get(id);

        if (f == null) {
            Filter filter = new FilterFactoryImpl().createFidFilter(id);

            try {
                FeatureCollection fc = getFeatures(filter);

                if (fc.size() > 0) {
                    FeatureIterator it = fc.features();
                    f = it.next();
                }
            } catch (IOException e) {
                throw (FeatureCacheException) new FeatureCacheException().initCause(e);
            }

            /*} else {
               f = (Feature) entry.getValue() ; */
        }

        return f;
    }

    /**
     * @param id
     * @return
     */
    public Feature peek(String id) {
        /*SimpleFeatureCacheEntry entry = (SimpleFeatureCacheEntry) store.get(id) ;
           if (entry == null) {
                   return null ;
           } else {
                   return (Feature) entry.getValue() ;
           }*/
        return (Feature) store.get(id);
    }

    /** Transform a JTS Envelope to a Region
     *
     * @param e JTS Envelope
     * @return
     */
    protected static Region toRegion(final Envelope e) {
        Region r = new Region(new double[] { e.getMinX(), e.getMinY() },
                new double[] { e.getMaxX(), e.getMaxY() });

        return r;
    }

    public void put(Feature f) {
        if (store.containsKey(f.getID())) {
            return;
        }

        //store.put(f.getID(), new SimpleFeatureCacheEntry(f));
        store.put(f.getID(), f);

        Region r = toRegion(f.getBounds());
        index.insertData(f.getID().getBytes(), r, f.getID().hashCode());
    }

    public void putAll(FeatureCollection fc, Filter f) {
        if (fc.size() > capacity) {
            return;
        }

        tracker.register(f);

        FeatureIterator it = fc.features();

        while (it.hasNext()) {
            if (store.size() >= capacity) {
                evict();
            }

            put(it.next());
        }

        it.close();
    }

    public Feature remove(String id) {
        Feature f = peek(id);

        if (f == null) {
            return null;
        }

        index.deleteData(toRegion(f.getBounds()), f.getID().hashCode());
        store.remove(id);

        return f;
    }

    public int size() {
        return store.size();
    }

    public Filter[] splitFilter(Filter f) {
        Filter[] filters = new Filter[3];
        FilterSplitter splitter = new FilterSplitter();
        f.accept(splitter, null);

        Filter sr = splitter.getSpatialRestriction();

        /*if (f instanceof BBOXImpl) {
           Filter missing = tracker.match(sr);
           Filter cached;
           if (missing.equals(f)) {
               cached = Filter.EXCLUDE;
           } else {
               cached = f;
           }
           filters[SPATIAL_RESTRICTION_CACHED] = cached;
           filters[SPATIAL_RESTRICTION_MISSING] = missing;
           filters[OTHER_RESTRICTIONS] = Filter.INCLUDE;
           } else {
               filters[SPATIAL_RESTRICTION_CACHED] = Filter.EXCLUDE;
               filters[SPATIAL_RESTRICTION_MISSING] = f;
               filters[OTHER_RESTRICTIONS] = Filter.INCLUDE;
           }*/
        assert ((sr == Filter.INCLUDE) || sr instanceof BBOXImpl);

        //System.out.println(sr.getClass()) ;
        Filter missing = tracker.match(sr);
        Filter cached;

        if (missing == sr) {
            cached = Filter.EXCLUDE;
        } else {
            cached = sr;
        }

        filters[SPATIAL_RESTRICTION_CACHED] = cached;
        filters[SPATIAL_RESTRICTION_MISSING] = missing;
        filters[OTHER_RESTRICTIONS] = splitter.getOtherRestriction();

        return filters;
    }

    public Set addFeatures(FeatureCollection collection)
        throws IOException {
        throw new UnsupportedOperationException();
    }

    public Transaction getTransaction() {
        // TODO Auto-generated method stub
        return transaction;
    }

    public void modifyFeatures(AttributeType[] type, Object[] value, Filter filter)
        throws IOException {
        throw new UnsupportedOperationException();
    }

    public void modifyFeatures(AttributeType type, Object value, Filter filter)
        throws IOException {
        throw new UnsupportedOperationException();
    }

    public void removeFeatures(Filter filter) throws IOException {
        throw new UnsupportedOperationException();
    }

    public void setFeatures(FeatureReader reader) throws IOException {
        throw new UnsupportedOperationException();
    }

    public void setTransaction(Transaction transaction) {
        if (transaction == null) {
            throw new IllegalArgumentException(
                "Transaction cannot be null, did you mean Transaction.AUTO_COMMIT?");
        }

        this.transaction = transaction;
    }

    public void addFeatureListener(FeatureListener listener) {
        // TODO Auto-generated method stub
    }

    public Envelope getBounds() throws IOException {
        // TODO Auto-generated method stub
        return getDataStore().getFeatureSource(type.getTypeName()).getBounds();
    }

    public Envelope getBounds(Query query) throws IOException {
        return getDataStore().getFeatureSource(type.getTypeName()).getBounds(query);
    }

    public int getCount(Query query) throws IOException {
        // may be we should return -1 if this is too expensive, or an estimate ?
        return getDataStore().getFeatureSource(type.getTypeName()).getCount(query);
    }

    public DataStore getDataStore() {
        return ds;
    }

    public FeatureCollection getFeatures() throws IOException {
        return getFeatures(Filter.INCLUDE);
    }

    public FeatureCollection getFeatures(Query query) throws IOException {
        if ((query.getTypeName() != null) && (query.getTypeName() != type.getTypeName())) {
            return new EmptyFeatureCollection(getSchema());
        }

        return getFeatures(query.getFilter());
    }

    public FeatureCollection getFeatures(Filter filter)
        throws IOException {
        Filter[] filters = splitFilter(filter);
        FeatureCollection fromCache = loadFromCache(filters[SPATIAL_RESTRICTION_CACHED]);

        //System.out.println("from cache = " + fromCache.size()) ;
        //fromCache.subCollection(filters[OTHER_RESTRICTIONS]) ;
        //System.out.println("from cache = " + fromCache.size()) ;
        cacheReads += fromCache.size();

        FilterFactory ff = new FilterFactoryImpl();
        Filter missing = filters[SPATIAL_RESTRICTION_MISSING];

        if (missing != Filter.EXCLUDE) {
            FeatureCollection fromStore = loadFromStore(ff.and(missing, filters[OTHER_RESTRICTIONS]));
            //tracker.register(missing);
            putAll(fromStore, missing);
            //System.out.println("from store = " + fromStore.size()) ;
            storeReads += fromStore.size();
            fromCache.addAll(fromStore);

            //System.out.println("Added data to cache") ;
        }

        return fromCache;
    }

    protected FeatureCollection loadFromStore(Filter f)
        throws IOException {
        FeatureCollection c = ds.getFeatureSource(type.getTypeName()).getFeatures(f);

        //System.out.println(index.getStatistics()) ;
        return c;
    }

    protected FeatureCollection loadFromCache(Filter f) {
        if (f == Filter.EXCLUDE) {
            return new DefaultFeatureCollection("cached", type);
        } else {
            final List features = new ArrayList();
            BBOXImpl bb = (BBOXImpl) f;
            Region r = new Region(new double[] { bb.getMinX(), bb.getMinY() },
                    new double[] { bb.getMaxX(), bb.getMaxY() });
            index.intersectionQuery(r,
                new IVisitor() {
                    public void visitData(final IData d) {
                        String id = new String(d.getData());
                        features.add(peek(id));

                        //System.out.println("Data = " + d.getIdentifier() + " fid = " + get(id)) ;
                    }

                    public void visitNode(final INode n) {
                        NodeCacheEntry e = (NodeCacheEntry) nodes.get(new Integer(n.getIdentifier()));

                        if (e == null) {
                            e = new NodeCacheEntry(n);
                            nodes.put(new Integer(n.getIdentifier()), e);

                            //System.out.println("created node " + n.getIdentifier()) ;
                        }

                        e.hit();

                        //System.out.println("hitted node " + n.getIdentifier()) ;
                    }
                });

            return DataUtilities.collection((Feature[]) features.toArray(new Feature[1]));
        }
    }

    public FeatureType getSchema() {
        return type;
    }

    public void removeFeatureListener(FeatureListener listener) {
        throw new UnsupportedOperationException();
    }

    public int getCacheReads() {
        return cacheReads;
    }

    public int getStoreReads() {
        return storeReads;
    }

    public int getEvictions() {
        return evictions;
    }

    public Set getSupportedHints() {
        return new HashSet();
    }

    class EvictionQueryStrategy implements IQueryStrategy {
        Leaf leaf = null;

        public void getNextEntry(IEntry e, int[] nextEntry, boolean[] hasNext) {
            if (e instanceof Index) {
                // TODO handle case there is no child
                Index n = (Index) e;
                NodeCacheEntry entry = (NodeCacheEntry) nodes.get(new Integer(n.getChildIdentifier(
                                0)));
                long accessTime;

                if (entry == null) {
                    accessTime = System.currentTimeMillis();
                } else {
                    accessTime = entry.getLastAccessTime();
                }

                for (int i = 1; i < n.getChildrenCount(); i++) {
                    NodeCacheEntry next = (NodeCacheEntry) nodes.get(new Integer(
                                n.getChildIdentifier(i)));

                    if ((next != null) && (next.getLastAccessTime() < accessTime)) {
                        accessTime = next.getLastAccessTime();
                        entry = next;
                    }
                }
                assert (entry != null);
                nextEntry[0] = ((INode) entry.getValue()).getIdentifier();
                hasNext[0] = true;

                return;
            } else if (e instanceof Leaf) {
                leaf = (Leaf) e;
                /*for (int i = 0 ; i < leaf.getChildrenCount() ; i++ ) {
                   // can't do that cause read lock !
                   //index.deleteData(leaf.getChildShape(i), leaf.getChildIdentifier(i)) ;
                   System.out.println("Should delete data " + leaf.getChildIdentifier(i)) ;
                   }*/
                hasNext[0] = false;
            }
        }

        public void doDelete() {
            if (leaf != null) {
                //System.out.println("deleting") ;
                //index.deleteLeaf(leaf) ;
                //index.deleteLeaf(node, leafIndex) ;
                List ids = index.readLeaf(leaf);

                for (Iterator it = ids.iterator(); it.hasNext();) {
                    String id = (String) it.next();
                    remove(id);
                    evictions++;
                }

                Region r = (Region) leaf.getShape();
                Envelope e = new Envelope(r.getLow(0), r.getHigh(0), r.getLow(1), r.getHigh(1));
                tracker.unregister(e);
            }
        }
    }
}
