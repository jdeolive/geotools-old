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

import java.util.Stack;
import java.util.logging.Logger;
import org.geotools.caching.firstdraft.spatialindex.spatialindex.IData;
import org.geotools.caching.firstdraft.spatialindex.spatialindex.INearestNeighborComparator;
import org.geotools.caching.firstdraft.spatialindex.spatialindex.INodeCommand;
import org.geotools.caching.firstdraft.spatialindex.spatialindex.IQueryStrategy;
import org.geotools.caching.firstdraft.spatialindex.spatialindex.IShape;
import org.geotools.caching.firstdraft.spatialindex.spatialindex.ISpatialIndex;
import org.geotools.caching.firstdraft.spatialindex.spatialindex.IStatistics;
import org.geotools.caching.firstdraft.spatialindex.spatialindex.IVisitor;
import org.geotools.caching.firstdraft.spatialindex.spatialindex.Region;
import org.geotools.caching.firstdraft.spatialindex.storagemanager.PropertySet;


/** A QuadTree implementation, inspired by the shapefile quadtree in org.geotools.index.quadtree,
 * but using visitors and query strategies to customize how the tree is visited or run specialized queries.
 *
 * Other noticeable changes from original QuadTree :
 * <ul><li>tree delegates splitting to nodes
 * </ul>
 *
 * @see org.geotools.index.quadtree.QuadTree
 * @see http://research.att.com/~marioh/spatialindex
 *
 * @author Christophe Rousson, SoC 2007, CRG-ULAVAL
 *
 * 2007-07-10: implemented maximum depth : allow to specify that the tree must not grow more than n levels
 * TODO: implement full interface
 * 2007-07-11: allowed to extend the tree from top, by changing root node
 * TODO: make tree serializable or loadable from disk
 *
 */
public class QuadTree implements ISpatialIndex {
    /**
     * Control how much sub-quadrants do overlap.
     * if ratio = 0.5, quadrants will not overlap at all.
     * I guess that we want quadrants to overlap a bit, due to roundoff errors.
     * Defaults to orginial value picked in org.geotools.index.quadtree.QuadTree
     */
    protected static final double SPLITRATIO = 0.55d;
    protected static final Logger LOGGER = org.geotools.util.logging.Logging.getLogger("org.geotools.caching.quadtree");

    /**
     * First node of the tree, pointing recursively to all other nodes.
     */
    protected Node root;

    // Constructors

    /** Creates a new QuadTree with first node with given bounds.
     *
     * @param bounds root node bounds.
     * @param maxHeight is the maximum height of the tree
     */
    public QuadTree(Region bounds, int maxHeight) {
        this.root = new Node(new Region(bounds), 0, maxHeight);
    }

    /** Creates a new QuadTree with first node with given bounds.
     * Tree height defaults to 20 levels.
     * Use @see QuadTree(Region, int) to specify height.
     *
     * @param root node bounds
     */
    public QuadTree(Region bounds) {
        this(bounds, 20);
    }

    // Interface
    public void addDeleteNodeCommand(INodeCommand nc) {
        // TODO Auto-generated method stub
    }

    public void addReadNodeCommand(INodeCommand nc) {
        // TODO Auto-generated method stub
    }

    public void addWriteNodeCommand(INodeCommand nc) {
        // TODO Auto-generated method stub
    }

    public void containmentQuery(IShape query, IVisitor v) {
        Node current = this.root;
        current.visited = false;

        Stack nodes = new Stack();

        if (current.getShape().contains(query)) {
            nodes.push(current);
        }

        while (!nodes.isEmpty()) {
            current = (Node) nodes.pop();

            if (!current.visited) {
                v.visitNode(current);

                for (int i = 0; i < current.getChildrenCount(); i++) {
                    current.getSubNode(i).visited = false;
                }

                for (int i = 0; i < current.numShapes; i++) {
                    v.visitData(new Data(current.shapesData[i], null, current.shapesId[i]));
                }

                current.visited = true;
            }

            for (int i = 0; i < current.getChildrenCount(); i++) {
                Node child = current.getSubNode(i);

                if (!child.visited) {
                    if (child.getShape().contains(query)) {
                        // we will go back to this one to examine other children
                        nodes.push(current);
                        nodes.push(child);

                        break;
                    } else {
                        child.visited = true;
                    }
                }
            }
        }
    }

    public boolean deleteData(IShape shape, int id) {
        // TODO Auto-generated method stub
        return false;
    }

    public void flush() throws IllegalStateException {
        // TODO Auto-generated method stub
    }

    public PropertySet getIndexProperties() {
        // TODO Auto-generated method stub
        return null;
    }

    public IStatistics getStatistics() {
        // TODO Auto-generated method stub
        return null;
    }

    public void insertData(byte[] data, IShape shape, int id) {
        if (this.root.getShape().contains(shape)) {
            insertData(this.root, data, shape, id);
        } else {
            createNewRoot(shape);
            assert (this.root.getShape().contains(shape));
            insertData(this.root, data, shape, id);
        }
    }

    public void intersectionQuery(IShape query, IVisitor v) {
        Node current = this.root;
        current.visited = false;

        Stack nodes = new Stack();

        if (current.getShape().intersects(query)) {
            nodes.push(current);
        }

        while (!nodes.isEmpty()) {
            current = (Node) nodes.pop();

            if (!current.visited) {
                v.visitNode(current);

                for (int i = 0; i < current.getChildrenCount(); i++) {
                    current.getSubNode(i).visited = false;
                }

                for (int i = 0; i < current.numShapes; i++) {
                    v.visitData(new Data(current.shapesData[i], null, current.shapesId[i]));
                }

                current.visited = true;
            }

            for (int i = 0; i < current.getChildrenCount(); i++) {
                Node child = current.getSubNode(i);

                if (!child.visited) {
                    if (child.getShape().intersects(query)) {
                        // we will go back to this one later to examine other children
                        nodes.push(current);
                        // meanwhile, we put one child at a time into stack, so we do not waste space
                        nodes.push(child);

                        break;
                    } else {
                        // we won't have to compute intersection again and again
                        child.visited = true;
                    }
                }
            }
        }
    }

    public boolean isIndexValid() {
        // TODO Auto-generated method stub
        return false;
    }

    public void nearestNeighborQuery(int k, IShape query, IVisitor v, INearestNeighborComparator nnc) {
        // TODO Auto-generated method stub
    }

    public void nearestNeighborQuery(int k, IShape query, IVisitor v) {
        // TODO Auto-generated method stub
    }

    public void pointLocationQuery(IShape query, IVisitor v) {
        // TODO Auto-generated method stub
    }

    public void queryStrategy(IQueryStrategy qs) {
        int[] next = new int[] { this.root.id };

        Node current = this.root;

        while (true) {
            boolean[] hasNext = new boolean[] { false };
            qs.getNextEntry(current, next, hasNext);

            if (!hasNext[0]) {
                break;
            } else {
                if (next[0] < 0) {
                    current = current.parent;
                } else {
                    current = current.getSubNode(next[0]);
                }
            }
        }
    }

    /** This a variant of the original interface method, using nodes directly rather than references to nodes using ids,
     * as in this implementation nodes does not have a unique ID in the tree, they have a unique ID in their quadrant.
     *
     * @see org.geotools.caching.spatialindex.spatialindex.SpatialIndex#queryStrategy(IQueryStrategy) ;
     *
     * @param qs
     */
    public void queryStrategy(QueryStrategy qs) {
        Node current = this.root;

        while (true) {
            boolean[] hasNext = new boolean[] { false };
            current = qs.getNextNode(current, hasNext);

            if (hasNext[0] == false) {
                break;
            }
        }
    }

    // Internals

    /** Insert new data into node.
     * Does not check data MBR fits into node MBR,
     * but this is what is expected. This is why method is kept private.
     *
     * @param n target node
     * @param data to insert
     * @param MBR of new data
     * @param id of data
     */
    private void insertData(Node n, byte[] data, IShape shape, int id) {
        if (n.isIndex()) {
            /* If there are subnodes, then consider whether this object
             * will fit in them.
             */
            for (int i = 0; i < n.getChildrenCount(); i++) {
                Node subNode = n.getSubNode(i);
                boolean done = false;

                if (subNode.getShape().contains(shape)) {
                    insertData(subNode, data, shape, id);
                    // we allow for multiple insertion, so we postpone returning from method
                    done = true;
                }

                if (done) {
                    return;
                }
            }
        } else if (n.level > 0) { // we do not want the tree to grow much too tall
                                  // if level == 0, we will add data to this node rather than splitting
                                  /* Otherwise, consider creating four subnodes if could fit into
             * them, and adding to the appropriate subnode.
             */
            n.split(SPLITRATIO);
            // recurse
            insertData(n, data, shape, id);

            return;
        }

        // If none of that worked, just add it to this nodes list.
        n.insertData(data, id);
    }

    private void createNewRoot(IShape s) {
        // TODO: take care of tree maximum height
        final Region old = this.root.getShape().getMBR();
        final Region r = enlargeRootRegion(old, s.getMBR());
        final Node oldRoot = this.root;
        this.root = new Node(r, 0, this.root.level);
        this.queryStrategy(new QueryStrategy() {
                Stack nodes = new Stack();
                boolean insertionMode = true;
                boolean inserted = false;
                int targetNode = -1;

                public Node getNextNode(Node current, boolean[] hasNext) {
                    if (!insertionMode) {
                        if (!inserted) {
                            assert (targetNode > -1);
                            current.subNodes.remove(targetNode);
                            current.addSubNode(oldRoot);
                            hasNext[0] = true;
                            oldRoot.parent = current;
                            inserted = true;

                            return oldRoot;
                        } else {
                            current.level = current.parent.level - 1;

                            for (int i = 0; i < current.getChildrenCount(); i++) {
                                nodes.add(0, current.getSubNode(i));
                            }

                            if (!nodes.isEmpty()) {
                                hasNext[0] = true;

                                return (Node) nodes.pop();
                            } else {
                                hasNext[0] = false;

                                return null;
                            }
                        }
                    } else if (current.getShape().contains(old)) {
                        if (current.isLeaf()) {
                            current.split(SPLITRATIO);
                        }

                        insertionMode = false;

                        for (int i = 0; i < current.getChildrenCount(); i++) {
                            if (current.getSubNode(i).getShape().contains(old)) {
                                hasNext[0] = true;
                                insertionMode = true;
                                targetNode = i;

                                return current.getSubNode(i);
                            }
                        }

                        hasNext[0] = true;

                        return current.parent;
                    } else {
                        hasNext[0] = false;

                        return null;
                    }
                }
            });
    }

    private Region enlargeRootRegion(Region old, final Region regionToInclude) {
        Region r = old.combinedRegion(regionToInclude);

        /* we actually make tiles a little bigger than how nodes do normally split
           so we use a slightly smaller ratio */
        double SPLITRATIO = QuadTree.SPLITRATIO - 0.02d;

        /*double xmin = (r.getLow(0) == old.getLow(0)) ? old.getLow(0) : old.getHigh(0) - (old.getHigh(0) - old.getLow(0))/SPLITRATIO ;
           double ymin = (r.getLow(1) == old.getLow(1)) ? old.getLow(1) : old.getHigh(1) - (old.getHigh(1) - old.getLow(1))/SPLITRATIO ;
           double xmax = (r.getHigh(0) == old.getHigh(0)) ? old.getHigh(0) : old.getLow(0) + (old.getHigh(0) - old.getLow(0))/SPLITRATIO ;
               double ymax = (r.getHigh(1) == old.getHigh(1)) ? old.getHigh(1) : old.getLow(1) + (old.getHigh(1) - old.getLow(1))/SPLITRATIO ;
               r = new Region(new double[] {xmin, ymin}, new double[] {xmax, ymax}) ;*/
        if ((r.getLow(0) == old.getLow(0)) && (r.getLow(1) == old.getLow(1))) {
            double xmin = old.getLow(0);
            double ymin = old.getLow(1);
            double xmax = old.getLow(0) + ((old.getHigh(0) - old.getLow(0)) / SPLITRATIO);
            double ymax = old.getLow(1) + ((old.getHigh(1) - old.getLow(1)) / SPLITRATIO);
            r = new Region(new double[] { xmin, ymin }, new double[] { xmax, ymax });
        } else if ((r.getLow(0) == old.getLow(0)) && (r.getHigh(1) == old.getHigh(1))) {
            double xmin = old.getLow(0);
            double ymin = old.getHigh(1) - ((old.getHigh(1) - old.getLow(1)) / SPLITRATIO);
            double xmax = old.getLow(0) + ((old.getHigh(0) - old.getLow(0)) / SPLITRATIO);
            double ymax = old.getHigh(1);
            r = new Region(new double[] { xmin, ymin }, new double[] { xmax, ymax });
        } else if ((r.getHigh(0) == old.getHigh(0)) && (r.getHigh(1) == old.getHigh(1))) {
            double xmin = old.getHigh(0) - ((old.getHigh(0) - old.getLow(0)) / SPLITRATIO);
            double ymin = old.getHigh(1) - ((old.getHigh(1) - old.getLow(1)) / SPLITRATIO);
            double xmax = old.getHigh(0);
            double ymax = old.getHigh(1);
            r = new Region(new double[] { xmin, ymin }, new double[] { xmax, ymax });
        } else {
            assert ((r.getHigh(0) == old.getHigh(0)) && (r.getLow(1) == old.getLow(1)));

            double xmin = old.getHigh(0) - ((old.getHigh(0) - old.getLow(0)) / SPLITRATIO);
            double ymin = old.getLow(1);
            double xmax = old.getHigh(0);
            double ymax = old.getLow(1) + ((old.getHigh(1) - old.getLow(1)) / SPLITRATIO);
            r = new Region(new double[] { xmin, ymin }, new double[] { xmax, ymax });
        }

        if (r.contains(regionToInclude)) {
            return r;
        } else {
            return enlargeRootRegion(r, regionToInclude);
        }
    }

    /** Utility class to expose data records outside of the tree.
     *
     * @author Christophe Rousson, SoC 2007, CRG-ULAVAL
     *
     */
    class Data implements IData {
        private byte[] data;
        private int id;
        private IShape shape;

        public Data(byte[] data, Region mbr, int id) {
            this.data = data;
            this.shape = mbr;
            this.id = id;
        }

        public byte[] getData() {
            return data;
        }

        public int getIdentifier() {
            return id;
        }

        public IShape getShape() {
            return shape;
        }
    }
}
