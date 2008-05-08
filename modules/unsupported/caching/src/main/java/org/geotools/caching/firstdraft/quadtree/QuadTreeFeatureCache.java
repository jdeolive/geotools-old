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
package org.geotools.caching.firstdraft.quadtree;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Stack;
import com.vividsolutions.jts.geom.Envelope;
import org.opengis.filter.Filter;
import org.geotools.caching.firstdraft.FeatureCache;
import org.geotools.caching.firstdraft.FeatureCacheException;
import org.geotools.caching.firstdraft.QueryTracker;
import org.geotools.caching.firstdraft.spatialindex.spatialindex.Region;
import org.geotools.caching.firstdraft.util.FilterSplitter;
import org.geotools.caching.firstdraft.util.IndexUtilities;
import org.geotools.data.DataStore;
import org.geotools.data.FeatureListener;
import org.geotools.data.FeatureReader;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.data.store.EmptyFeatureCollection;
import org.geotools.feature.AttributeType;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureType;
import org.geotools.filter.spatial.BBOXImpl;


public class QuadTreeFeatureCache implements FeatureCache, QueryTracker {
    private final DataStore ds;
    private final FeatureType type;
    protected QuadTree tree;
    protected int capacity;

    // == Contructors ===================
    public QuadTreeFeatureCache(DataStore ds, FeatureType type, int capacity)
        throws FeatureCacheException {
        this.ds = ds;
        this.type = type;
        this.capacity = capacity;

        try {
            this.tree = new QuadTree(IndexUtilities.toRegion(
                        ds.getFeatureSource(type.getTypeName()).getBounds()));
        } catch (IOException e) {
            throw new FeatureCacheException(e);
        }
    }

    // == FeatureCache interface ===================
    public void clear() {
        // TODO Auto-generated method stub
    }

    public void evict() {
        // TODO Auto-generated method stub
    }

    public Feature get(String id) throws FeatureCacheException {
        // TODO Auto-generated method stub
        return null;
    }

    public void put(Feature f) {
        // TODO Auto-generated method stub
    }

    public void putAll(FeatureCollection fc, Filter f) {
        // TODO Auto-generated method stub
    }

    public Feature remove(String id) {
        // TODO Auto-generated method stub
        return null;
    }

    public int size() {
        // TODO Auto-generated method stub
        return 0;
    }

    public Filter[] splitFilter(Filter f) {
        //TODO: move to AbstractFeatureCache
        //TODO: remove from interface ; this shoud not be public
        Filter[] filters = new Filter[3];
        FilterSplitter splitter = new FilterSplitter();
        f.accept(splitter, null);

        Filter sr = splitter.getSpatialRestriction();
        assert ((sr == Filter.INCLUDE) || sr instanceof BBOXImpl);

        Filter missing = match(sr);
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

    // == FeatureStore interface =================
    public Set addFeatures(FeatureCollection collection)
        throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    public Transaction getTransaction() {
        // TODO Auto-generated method stub
        return null;
    }

    public void modifyFeatures(AttributeType[] type, Object[] value, Filter filter)
        throws IOException {
        // TODO Auto-generated method stub
    }

    public void modifyFeatures(AttributeType type, Object value, Filter filter)
        throws IOException {
        // TODO Auto-generated method stub
    }

    public void removeFeatures(Filter filter) throws IOException {
        // TODO Auto-generated method stub
    }

    public void setFeatures(FeatureReader reader) throws IOException {
        // TODO Auto-generated method stub
    }

    public void setTransaction(Transaction transaction) {
        // TODO Auto-generated method stub
    }

    public void addFeatureListener(FeatureListener listener) {
        // TODO Auto-generated method stub
    }

    public Envelope getBounds() throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    public Envelope getBounds(Query query) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    public int getCount(Query query) throws IOException {
        // TODO Auto-generated method stub
        return 0;
    }

    public DataStore getDataStore() {
        return ds;
    }

    public FeatureCollection getFeatures() throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.geotools.data.FeatureSource#getFeatures(org.geotools.data.Query)
     * TODO: move to AbstractFeatureCache
     */
    public FeatureCollection getFeatures(Query query) throws IOException {
        if ((query.getTypeName() != null) && (query.getTypeName() != type.getTypeName())) {
            return new EmptyFeatureCollection(getSchema());
        }

        return getFeatures(query.getFilter());
    }

    public FeatureCollection getFeatures(Filter filter)
        throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    public FeatureType getSchema() {
        return type;
    }

    public void removeFeatureListener(FeatureListener listener) {
        // TODO Auto-generated method stub
    }

    // == QueryTracker interface ==============
    public Filter match(Filter f) {
        if (!(f instanceof BBOXImpl)) {
            return f;
        }

        BBOXImpl bbox = (BBOXImpl) f;
        Region r = new Region(new double[] { bbox.getMinX(), bbox.getMinY() },
                new double[] { bbox.getMaxX(), bbox.getMaxY() });
        ValidTileVisitor v = new ValidTileVisitor();
        tree.containmentQuery(r, v);

        if (v.isCovered) {
            // we do not need extra data
            return Filter.EXCLUDE;
        } else {
            // find missing tiles

            /* v2 */
            register(f);
            v = new ValidTileVisitor();
            tree.containmentQuery(r, v);

            Stack regions = new Stack();
            match(r, v.lastNode, regions);

            /* v1
               Stack regions = new Stack() ;
               if (v.lastNode == null) {
                       // Query is bigger than the known part of the universe
                       Region unknown = intersection(r, (Region) tree.root.getShape()) ;
                       regions.push(unknown) ;
                       match(r, tree.root, regions) ;
               } else {
                       match(r, v.lastNode, regions) ;
               }*/
        }

        return null;
    }

    protected void match(final Region r, Node current, Stack s) {
        if (current.isLeaf()) {
            // we have no data
            // we must get corresponding normalized region from DataStore
            Region normalized = normalize(r, current);
            // add region to stack of regions to retrieve
            s.push(normalized);
        } else {
            for (int i = 0; i < current.getChildrenCount(); i++) {
                Node child = current.getSubNode(i);

                if (child.getShape().intersects(r)) {
                    if (!child.isValid()) {
                        match(r, child, s);
                    }
                }
            }

            // is that the right place to do that ?
            // s = groupRegions(s) ;
        }
    }

    protected Stack groupRegions(Stack s) {
        Stack r = new Stack();

        while (!s.isEmpty()) {
            Region r1 = (Region) s.pop();

            for (Iterator it = s.iterator(); it.hasNext();) {
                Region r2 = (Region) it.next();

                if (r1.intersects(r2)) {
                    it.remove();
                    r1 = r1.combinedRegion(r2);
                }
            }

            r.push(r1);
        }

        return r;
    }

    protected static Region normalize(final Region r, final Node node) {
        Region noderegion = (Region) node.getShape();
        Region inter = intersection(r, noderegion);
        int level = node.getLevel();

        while (level > 0) {
            Region[] splits = Node.splitBounds(noderegion, QuadTree.SPLITRATIO);

            //Region[] splits1 = Node.splitBounds(splits[0], QuadTree.SPLITRATIO) ;
            //Region[] splits2 = Node.splitBounds(splits[1], QuadTree.SPLITRATIO) ;
            if (splits[0].contains(inter)) {
                splits = Node.splitBounds(splits[0], QuadTree.SPLITRATIO);

                if (splits[0].contains(inter)) {
                    noderegion = splits[0];
                    level--;
                } else if (splits[1].contains(inter)) {
                    noderegion = splits[1];
                    level--;
                } else {
                    break;
                }
            } else if (splits[1].contains(inter)) {
                splits = Node.splitBounds(splits[1], QuadTree.SPLITRATIO);

                if (splits[0].contains(inter)) {
                    noderegion = splits[0];
                    level--;
                } else if (splits[1].contains(inter)) {
                    noderegion = splits[1];
                    level--;
                } else {
                    break;
                }
            } else {
                break;
            }
        }

        return noderegion;
    }

    protected static Region intersection(final Region r1, final Region r2) {
        double xmin = (r1.getLow(0) > r2.getLow(0)) ? r1.getLow(0) : r2.getLow(0);
        double ymin = (r1.getLow(1) > r2.getLow(1)) ? r1.getLow(1) : r2.getLow(1);
        double xmax = (r1.getHigh(0) < r2.getHigh(0)) ? r1.getHigh(0) : r2.getHigh(0);
        double ymax = (r1.getHigh(1) < r2.getHigh(1)) ? r1.getHigh(1) : r2.getHigh(1);

        return new Region(new double[] { xmin, ymin }, new double[] { xmax, ymax });
    }

    public Query match(Query q) {
        // TODO Auto-generated method stub
        return null;
    }

    public void register(Query q) {
        // TODO Auto-generated method stub
    }

    public void register(Filter f) {
        if (f instanceof BBOXImpl) {
            BBOXImpl bbox = (BBOXImpl) f;
            Region r = new Region(new double[] { bbox.getMinX(), bbox.getMinY() },
                    new double[] { bbox.getMaxX(), bbox.getMaxY() });
            tree.insertData(null, r, 0);
        }
    }

    public void unregister(Query q) {
        // TODO Auto-generated method stub
    }

    public void unregister(Filter f) {
        // TODO Auto-generated method stub
        // intersectionQuery
        // for each node, invalidate entry
        //                push node in stack
        // while stack not empty
        //     get node from stack
        //     if four invalid quadrants, make node an invalid leaf
    }

    public Set getSupportedHints() {
        return new HashSet();
    }
}
