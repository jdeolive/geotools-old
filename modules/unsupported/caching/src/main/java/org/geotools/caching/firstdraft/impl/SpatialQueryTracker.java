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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.geotools.caching.firstdraft.QueryTracker;
import org.geotools.data.DefaultQuery;
import org.geotools.data.Query;
import org.geotools.filter.FilterFactoryImpl;
import org.geotools.filter.spatial.BBOXImpl;
import org.geotools.index.Data;
import org.geotools.index.DataDefinition;
import org.geotools.index.LockTimeoutException;
import org.geotools.index.TreeException;
import org.geotools.index.rtree.*;
import org.geotools.index.rtree.memory.MemoryPageStore;


/** First implementation of QueryTracker to handle BBox queries.
 * Stores the extent of queries in a R-tree,
 * so this tracker can tell what areas are already covered by previous queries.
 * Can compute a rough approximation of the complementary area needed to cover a new query.
 * Uses spatial index from org.geotools.index.rtree, and keeps tree in memory.
 *
 * Currently, does handle only queries made of a BBoxFilter.
 *
 * @task handle queries made a up of a _spatial filter_ and an _attribute filter_
 * Use FilterVisitor ?
 * @task should tree have a size limit ? for the time being, we rely a the cache eviction strategy,
 * and hope for the best. We should easily be able to store thousands query envelopes.
 *
 * @author Christophe Rousson, SoC 2007, CRG-ULAVAL
 *
 */
public class SpatialQueryTracker implements QueryTracker {
    /**
     * We need to feed the tree with some data.
     * What we store is the hashCode of the envelope of queries.
     */
    private static DataDefinition df = createDataDefinition();

    /**
     * The R-tree to keep track of queries bounds.
     */
    private RTree tree = createTree();

    /**
     *  A map to store queries bounds.
     *  As these are stored in the R-tree, why do we have to store these in another place ?
     *  Well, when we search the tree, we get data, not the envelope of data.
     *  Other R-tree implementation might do a better job.
     *
     */
    private final HashMap map = new HashMap();

    /**
     * We will use this instance of FilterFactory to build new queries.
     */
    private final FilterFactory filterFactory = new FilterFactoryImpl();

    /* (non-Javadoc)
     * @see org.geotools.caching.QueryTracker#clear()
     */
    public void clear() {
        try {
            map.clear();
            tree.close();
            tree = createTree();
        } catch (TreeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public Query match(Query q) {
        return new DefaultQuery(q.getTypeName(), match(q.getFilter()));
    }

    /* (non-Javadoc)
     * @see org.geotools.caching.QueryTracker#match(org.geotools.data.Query)
     */
    public Filter match(Filter f) {
        if (!accepts(f)) {
            return f;
        }

        BBOXImpl bb = (BBOXImpl) f;

        try {
            Envelope env = new Envelope(bb.getMinX(), bb.getMaxX(), bb.getMinY(), bb.getMaxY());
            Geometry searchArea = getRectangle(env);

            // find matches in R-tree
            List results = tree.search(env);

            // seems we know nothing about the requested area ... we have to process the whole query.
            if (results.size() == 0) {
                return f;
            }

            // at least part of the requeted area falls within the "known world"
            for (Iterator i = results.iterator(); i.hasNext();) {
                Data d = (Data) i.next();
                Envelope e = (Envelope) map.get(d.getValue(0));
                Polygon rect = getRectangle(e);

                // searchArea within the "known world".
                // We actually don't need any other data.
                if (rect.contains(searchArea)) {
                    return Filter.EXCLUDE;
                }

                // remove known area from search area ...
                searchArea = searchArea.difference(rect);
            }

            // searchArea may be some really complex geometry, with holes and patches.
            // get back to the envelope, to build a new query.
            Envelope se = searchArea.getEnvelopeInternal();
            Filter newbb = filterFactory.bbox(bb.getPropertyName(), se.getMinX(), se.getMinY(),
                    se.getMaxX(), se.getMaxY(), bb.getSRS());

            return newbb;
        } catch (TreeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (LockTimeoutException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return f;
    }

    public void register(Query q) {
        register(q.getFilter());
    }

    /* (non-Javadoc)
     * @see org.geotools.caching.QueryTracker#register(org.geotools.data.Query)
     */
    public void register(Filter f) {
        if (accepts(f)) {
            BBOXImpl bb = (BBOXImpl) f;

            try {
                Envelope env = new Envelope(bb.getMinX(), bb.getMaxX(), bb.getMinY(), bb.getMaxY());
                Data d = new Data(df);
                Integer key = new Integer(env.hashCode());
                d.addValue(key);
                map.put(key, env);
                tree.insert(env, d);
            } catch (TreeException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (LockTimeoutException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public void unregister(Query q) {
        unregister(q.getFilter());
    }

    /* (non-Javadoc)
     * @see org.geotools.caching.QueryTracker#unregister(org.geotools.data.Query)
     */
    public void unregister(Filter f) {
        if (accepts(f)) {
            BBOXImpl bb = (BBOXImpl) f;
            Envelope env = new Envelope(bb.getMinX(), bb.getMaxX(), bb.getMinY(), bb.getMaxY());
            unregister(env);
        }
    }

    public void unregister(Envelope env) {
        try {
            List results = tree.search(env);

            for (Iterator i = results.iterator(); i.hasNext();) {
                Data d = (Data) i.next();
                Envelope e = (Envelope) map.get(d.getValue(0));
                tree.delete(e);
                map.remove(d.getValue(0));
            }
        } catch (TreeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (LockTimeoutException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * @param q
     * @return
     */
    private boolean accepts(Filter f) {
        if (f instanceof BBOXImpl) {
            return true;
        } else {
            return false;
        }
    }

    /** R-tree used to track queries.
     *  R-tree is mapped in memory.
     *
     * @return
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

    /** Type of data to feed in the tree.
     * Holds one interger, which represents the hashCode of the envelope stored in the tree.
     *
     * @return
     */
    private static DataDefinition createDataDefinition() {
        DataDefinition df = new DataDefinition("US-ASCII");
        df.addField(Integer.class);

        return df;
    }

    /** Envelope -> Polygon convenience function.
     *
     * @param e an envelope
     * @return a Rectangle that has the same shape as e
     */
    private static Polygon getRectangle(Envelope e) {
        Coordinate[] coords = new Coordinate[] {
                new Coordinate(e.getMinX(), e.getMinY()), new Coordinate(e.getMaxX(), e.getMinY()),
                new Coordinate(e.getMaxX(), e.getMaxY()), new Coordinate(e.getMinX(), e.getMaxY()),
                new Coordinate(e.getMinX(), e.getMinY())
            };
        CoordinateArraySequence seq = new CoordinateArraySequence(coords);
        LinearRing ls = new LinearRing(seq, new GeometryFactory());
        Polygon ret = new Polygon(ls, null, new GeometryFactory());

        return ret;
    }
}
