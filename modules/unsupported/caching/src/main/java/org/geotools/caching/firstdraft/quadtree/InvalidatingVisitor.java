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

import org.geotools.caching.firstdraft.spatialindex.spatialindex.IData;
import org.geotools.caching.firstdraft.spatialindex.spatialindex.INode;
import org.geotools.caching.firstdraft.spatialindex.spatialindex.IVisitor;


public class InvalidatingVisitor implements IVisitor {
    protected Node lastNode = null;

    public void visitData(IData d) {
        // do nothing
    }

    public void visitNode(INode n) {
        if (n instanceof Node) {
            Node node = (Node) n;
            node.entry.invalidate();
            lastNode = node;
        }
    }

    public void updateTree() {
        lastNode.subNodes.clear();

        Node parent = lastNode.parent;

        while (parent != null) {
            if (isInvalid(parent)) {
                parent.entry.invalidate();
                parent.subNodes.clear();
                parent = parent.parent;
            } else {
                // we don't have to climb the tree any more
                break;
            }
        }
    }

    protected boolean isInvalid(Node n) {
        boolean ret = true;

        for (int i = 0; i < n.getChildrenCount(); i++) {
            Node child = n.getSubNode(i);

            if ((!child.isLeaf()) || child.isValid()) {
                ret = false;

                break;
            }
        }

        return ret;
    }
}
