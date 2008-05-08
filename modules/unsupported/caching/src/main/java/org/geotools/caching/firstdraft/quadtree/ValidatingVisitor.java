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

import org.geotools.caching.firstdraft.spatialindex.spatialindex.IData;
import org.geotools.caching.firstdraft.spatialindex.spatialindex.INode;
import org.geotools.caching.firstdraft.spatialindex.spatialindex.IVisitor;
import org.geotools.caching.firstdraft.spatialindex.spatialindex.Region;


public class ValidatingVisitor implements IVisitor {
    private final Region target;
    private Node lastNode = null;

    public ValidatingVisitor(Region target) {
        this.target = target;
    }

    public void visitData(IData d) {
        // do nothing
    }

    public void visitNode(INode n) {
        if (n instanceof Node) {
            Node node = (Node) n;

            if (target.contains(node.getShape())) {
                node.entry.setValid();
            }

            if (node.getShape().contains(target)) {
                lastNode = node;
            }
        }
    }

    public void updateTree() {
        if (lastNode != null) {
            lastNode.entry.setValid();
        }
    }
}
