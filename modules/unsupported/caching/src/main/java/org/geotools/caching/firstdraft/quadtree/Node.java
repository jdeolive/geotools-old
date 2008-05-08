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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.geotools.caching.firstdraft.spatialindex.spatialindex.INode;
import org.geotools.caching.firstdraft.spatialindex.spatialindex.IShape;
import org.geotools.caching.firstdraft.spatialindex.spatialindex.Region;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;


public class Node implements INode {
    private Region bounds;
    protected NodeCacheEntry entry;
    protected int numShapes;
    protected int[] shapesId;
    protected byte[][] shapesData;
    protected List subNodes;
    protected Node parent;
    protected boolean visited = false;

    //protected boolean childrenVisited = false;
    protected int id;
    protected int level;

    // Constructors

    /** Constructor : creates a new node descending from another node.
     *
     * @param s envelope of new node
     * @param id of new node
     * @param parent node
     */
    protected Node(Region s, int id, Node parent) {
        this.bounds = s;
        this.id = id;
        this.parent = parent;
        this.subNodes = new ArrayList();
        this.numShapes = 0;
        this.shapesId = new int[4];
        Arrays.fill(shapesId, -1);
        this.shapesData = new byte[4][];
        this.entry = new NodeCacheEntry(this);

        if (parent == null) {
            this.level = 0;
        } else {
            this.level = parent.level - 1;
        }
    }

    /** Constructor : creates a root node.
     * Supplied level parameter indicates the maximum height of the tree.
     *
     * @param s envelope of new node
     * @param id of new node
     * @param maxDepth is the maximum height of the tree. Must be > 0.
     */
    protected Node(Region s, int id, int maxDepth) {
        this(s, id, null);
        this.level = maxDepth;
    }

    // Interface
    public int getChildIdentifier(int index) throws IndexOutOfBoundsException {
        return ((Node) subNodes.get(index)).getIdentifier();
    }

    public IShape getChildShape(int index) throws IndexOutOfBoundsException {
        return ((Node) subNodes.get(index)).getShape();
    }

    public int getChildrenCount() {
        return subNodes.size();
    }

    public int getLevel() {
        return level;
    }

    public boolean isIndex() {
        return !isLeaf();
    }

    public boolean isLeaf() {
        return subNodes.isEmpty();
    }

    public int getIdentifier() {
        return id;
    }

    public IShape getShape() {
        return bounds;
    }

    // Additional public methods, for our caching purpose

    /** Attach features to this node
     *
     * @param feature to attach to this node
     */
    public void attach(Feature f) {
        this.entry.linkedFeatures.add(f);
    }

    /** Attach a list of features to this node
     *
     * @param list of features
     */
    public void attach(FeatureCollection features) {
        FeatureIterator it = features.features();

        try {
            while (it.hasNext()) {
                attach((Feature) it.next());
            }
        } finally {
            features.close(it);
        }
    }

    /** Hit node, cause access statistics to be updated.
     * Actually, hit associated NodeCacheEntry
     */
    public void hit() {
        this.entry.hit();
    }

    public boolean isValid() {
        return this.entry.isValid();
    }

    // Internal
    protected Node getSubNode(int index) {
        return (Node) subNodes.get(index);
    }

    protected void addSubNode(Node n) {
        subNodes.add(n);
    }

    protected void split(double SPLITRATIO) {
        assert (isLeaf());

        Region half1;
        Region half2;
        Region[] quads = new Region[4];
        Region[] tmp = splitBounds(bounds, SPLITRATIO);
        half1 = tmp[0];
        half2 = tmp[1];
        tmp = splitBounds(half1, SPLITRATIO);
        quads[0] = tmp[0];
        quads[1] = tmp[1];
        tmp = splitBounds(half2, SPLITRATIO);
        quads[2] = tmp[0];
        quads[3] = tmp[1];

        for (int i = 0; i < 4; i++) {
            addSubNode(new Node(quads[i], i, this));
        }
    }

    /**
     * Splits the specified Envelope
     * @param in an Envelope to split
     * @return an array of 2 Envelopes
     */
    public static Region[] splitBounds(Region in, double SPLITRATIO) {
        Region[] ret = new Region[2];
        double range;
        double calc;

        if ((in.m_pHigh[0] - in.m_pLow[0]) > (in.m_pHigh[1] - in.m_pLow[1])) {
            // Split in X direction
            range = in.m_pHigh[0] - in.m_pLow[0];

            calc = in.m_pLow[0] + (range * SPLITRATIO);
            ret[0] = new Region(in);
            ret[0].m_pHigh[0] = calc;

            calc = in.m_pHigh[0] - (range * SPLITRATIO);
            ret[1] = new Region(in);
            ret[1].m_pLow[0] = calc;
        } else {
            // Split in Y direction
            range = in.m_pHigh[1] - in.m_pLow[1];

            calc = in.m_pLow[1] + (range * SPLITRATIO);
            ret[0] = new Region(in);
            ret[0].m_pHigh[1] = calc;

            calc = in.m_pHigh[1] - (range * SPLITRATIO);
            ret[1] = new Region(in);
            ret[1].m_pLow[1] = calc;
        }

        return ret;
    }

    protected void insertData(byte[] data, int id) {
        if (shapesId.length == numShapes) {
            // increases storage size
            int[] newIds = new int[shapesId.length * 2];
            byte[][] newData = new byte[shapesData.length * 2][];
            System.arraycopy(this.shapesId, 0, newIds, 0, this.numShapes);
            System.arraycopy(shapesData, 0, newData, 0, this.numShapes);
            this.shapesId = newIds;
            this.shapesData = newData;
        }

        this.shapesId[numShapes] = id;
        this.shapesData[numShapes] = data;
        numShapes++;
    }

    protected void deleteData(int index) throws IndexOutOfBoundsException {
        if ((index < 0) || (index > (numShapes - 1))) {
            throw new IndexOutOfBoundsException("" + index);
        }

        if (index < (numShapes - 1)) {
            this.shapesId[index] = this.shapesId[numShapes - 1];
            this.shapesData[index] = this.shapesData[numShapes - 1];
        }

        this.shapesData[numShapes - 1] = null;
        numShapes--;

        //if (numShapes == 0) {
        // do we have to do something ?
        //}
    }

    public String toString() {
        StringBuffer r = new StringBuffer();
        r.append("Node ID=" + this.id + " ");
        r.append("# of data=" + this.numShapes + " ");
        r.append("MBR=" + this.bounds);

        return r.toString();
    }
}
