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
package org.geotools.caching.firstdraft.quadtree;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Stack;
import org.opengis.filter.Filter;
import org.geotools.caching.firstdraft.QueryTracker;
import org.geotools.caching.firstdraft.spatialindex.spatialindex.Region;
import org.geotools.data.DefaultQuery;
import org.geotools.data.Query;
import org.geotools.feature.FeatureType;
import org.geotools.filter.FilterFactoryImpl;
import org.geotools.filter.spatial.BBOXImpl;


public class QuadTreeQueryTracker implements QueryTracker {
    int max_tiles = 10;
    QuadTree tree;
    FeatureType type;

    public QuadTreeQueryTracker(Region r, FeatureType type) {
        this.tree = new QuadTree(r);
        this.type = type;
    }

    public void clear() {
        // TODO Auto-generated method stub
    }

    public Filter match(Filter f) {
        //TODO: change Filter to BBOXImpl in interface,
        //      this is the only type of feature we should accept
        //      then next line is useless
        BBOXImpl filter = (BBOXImpl) f;

        //
        Region r = new Region(new double[] { filter.getMinX(), filter.getMinY() },
                new double[] { filter.getMaxX(), filter.getMaxY() });
        ValidTileVisitor v = new ValidTileVisitor();
        tree.containmentQuery(r, v);

        if (v.isCovered) {
            // we do not need extra data
            HitEntryVisitor h = new HitEntryVisitor();
            tree.intersectionQuery(r, h);

            return Filter.EXCLUDE;
        } else { // we need extra data
                 // find missing tiles

            if (v.lastNode == null) { // case 1: outside of quadtree, ie of root node MBR

                return f; // in first approximation, we can't answer this query
                          // TODO: handle in a more subtle manner
            }

            // case 2: inside of quadtree, ie of root node MBR
            Stack regions = new Stack();
            searchMissingTiles(r, v.lastNode, regions);

            if (regions.size() > 1) {
                ArrayList filters = new ArrayList();
                FilterFactoryImpl ff = new FilterFactoryImpl();

                while (!regions.isEmpty()) {
                    Region rg = (Region) regions.pop();
                    Filter missing = ff.bbox(filter.getPropertyName(), rg.getLow(0), rg.getLow(1),
                            rg.getHigh(0), rg.getHigh(1), filter.getSRS());
                    filters.add(missing);
                }

                return ff.or(filters);
            } else if (regions.size() == 1) {
                FilterFactoryImpl ff = new FilterFactoryImpl();
                Region rg = (Region) regions.pop();

                return ff.bbox(filter.getPropertyName(), rg.getLow(0), rg.getLow(1), rg.getHigh(0),
                    rg.getHigh(1), filter.getSRS());
            } else {
                return Filter.EXCLUDE;
            }
        }
    }

    public Query match(Query q) {
        if (accept(q)) {
            Filter f = match(q.getFilter());

            return new DefaultQuery(q.getTypeName(), f);
        } else {
            return q;
        }
    }

    public void register(Query q) {
        if (accept(q)) {
            register(q.getFilter());
        }
    }

    /* (non-Javadoc)
     * @see org.geotools.caching.QueryTracker#register(org.opengis.filter.Filter)
     *
     * You should not register an already register filter, although this will not cause an error
     * Use match(Filter) to check before.
     */
    public void register(Filter f) {
        // TODO: change Filter to BBOXImpl in interface,
        //      this is the only type of feature we should accept
        //      then next line is useless
        BBOXImpl filter = (BBOXImpl) f;

        //
        Region r = new Region(new double[] { filter.getMinX(), filter.getMinY() },
                new double[] { filter.getMaxX(), filter.getMaxY() });
        tree.insertData(null, r, filter.hashCode());

        ValidatingVisitor v = new ValidatingVisitor(r);
        tree.intersectionQuery(r, v);
        v.updateTree();
    }

    public void unregister(Query q) {
        if (accept(q)) {
            unregister(q.getFilter());
        }
    }

    public void unregister(Filter f) {
        BBOXImpl filter = (BBOXImpl) f;
        InvalidatingVisitor v = new InvalidatingVisitor();
        Region r = getRegion(filter);
        tree.containmentQuery(r, v);
        v.updateTree();
    }

    // Public methods not in interface QueryTracker
    public Region getNextEvictionRegion() {
        return null;
    }

    // Internals
    protected boolean accept(Query q) {
        return (q.getTypeName().equals(type.getTypeName()));
    }

    protected void searchMissingTiles(final Region r, Node current, Stack s) {
        if (current.isLeaf()) {
            // we have no data
            // we must get corresponding normalized region from DataStore
            Region normalized = normalize(r, current);
            // add region to stack of regions to retrieve
            s.push(normalized);
        } else {
            int new_missing_tiles = s.size();

            for (int i = 0; i < current.getChildrenCount(); i++) {
                Node child = current.getSubNode(i);

                if (child.getShape().intersects(r)) { // falls within our search criteria
                                                      // if valid, we don't need this tile,
                                                      // otherwise recurse to find missing children

                    if (!child.isValid()) {
                        searchMissingTiles(r, child, s);
                    } else {
                        // do not forget to update cache statistics,
                        // as we will access this part of the universe we already know
                        child.hit();
                    }
                }
            }

            // check for new missing tiles and group before returning
            new_missing_tiles = s.size() - new_missing_tiles;

            if (new_missing_tiles > max_tiles) {
                // we have too many new sub-tiles
                for (int i = 0; i < new_missing_tiles; i++) {
                    s.pop();
                }

                // we will fetch whole tile
                s.push(current.getShape());
            } else if (new_missing_tiles > 1) {
                // we have 2 or more new tiles
                // trying to group what can be grouped
                Stack newtiles = new Stack();

                for (int i = 0; i < new_missing_tiles; i++) {
                    newtiles.push(s.pop());
                }

                groupRegions(newtiles, s);
            }
        }
    }

    /** Group intersecting tiles in a list.
     * Brute force algo returning with time n!,
     * we expect a small number of tiles in list ...
     *
     * @param tiles list of tiles to group
     * @param stack where to put resulting grouped tiles
     */
    protected static void groupRegions(Stack tiles, Stack s) {
        assert (tiles.size() > 1);

        while (!tiles.isEmpty()) {
            Region r1 = (Region) tiles.pop();
            boolean combinationHappened = false;

            for (Iterator it = tiles.iterator(); it.hasNext();) {
                Region r2 = (Region) it.next();

                if (r1.intersects(r2)) {
                    it.remove();
                    r1 = r1.combinedRegion(r2);
                    combinationHappened = true;
                }
            }

            if (combinationHappened) {
                // we reinject for other matches
                tiles.push(r1);
            } else {
                s.push(r1);
            }
        }
    }

    protected static Region normalize(final Region r, final Node node) {
        Region noderegion = (Region) node.getShape();
        Region inter = intersection(r, noderegion);
        int level = node.getLevel();

        while (level > 0) {
            boolean done = true;
            Region[] splits = getSplits(noderegion);

            for (int i = 0; i < splits.length; i++) {
                if (splits[i].contains(inter)) {
                    noderegion = splits[i];
                    done = false;

                    break;
                }
            }

            if (done) {
                break;
            }

            level--;
        }

        return noderegion;
    }

    protected static Region[] getSplits(Region r) {
        Region[] ret = new Region[4];
        Region[] tmp = Node.splitBounds(r, QuadTree.SPLITRATIO);
        ret[0] = tmp[0];
        ret[2] = tmp[1];
        tmp = Node.splitBounds(ret[0], QuadTree.SPLITRATIO);
        ret[0] = tmp[0];
        ret[1] = tmp[1];
        tmp = Node.splitBounds(ret[2], QuadTree.SPLITRATIO);
        ret[2] = tmp[0];
        ret[3] = tmp[1];

        return ret;
    }

    protected static Region intersection(final Region r1, final Region r2) {
        double xmin = (r1.getLow(0) > r2.getLow(0)) ? r1.getLow(0) : r2.getLow(0);
        double ymin = (r1.getLow(1) > r2.getLow(1)) ? r1.getLow(1) : r2.getLow(1);
        double xmax = (r1.getHigh(0) < r2.getHigh(0)) ? r1.getHigh(0) : r2.getHigh(0);
        double ymax = (r1.getHigh(1) < r2.getHigh(1)) ? r1.getHigh(1) : r2.getHigh(1);

        return new Region(new double[] { xmin, ymin }, new double[] { xmax, ymax });
    }

    protected static Region getRegion(BBOXImpl f) {
        return new Region(new double[] { f.getMinX(), f.getMinY() },
            new double[] { f.getMaxX(), f.getMaxY() });
    }
}
