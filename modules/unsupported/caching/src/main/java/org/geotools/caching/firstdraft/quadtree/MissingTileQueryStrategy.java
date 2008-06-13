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
import org.geotools.caching.firstdraft.spatialindex.spatialindex.Region;


public class MissingTileQueryStrategy implements QueryStrategy {
    private final Region target;
    private final Node startNode;
    private boolean firstCall = true;
    private Stack nodes = new Stack();
    private ArrayList regions = new ArrayList();

    public MissingTileQueryStrategy(Region target, Node startNode) {
        this.target = target;
        this.startNode = startNode;
    }

    public Node getNextNode(Node current, boolean[] hasNext) {
        if (firstCall) {
            firstCall = false;
            hasNext[0] = true;

            return startNode;
        }

        if (current.getShape().intersects(target)) {
            if (current.isLeaf() && !current.isValid()) {
                Region missing = (Region) current.getShape();

                for (Iterator it = regions.iterator(); it.hasNext();) {
                    Region r = (Region) it.next();

                    if (missing.intersects(r)) {
                        missing = missing.combinedRegion(r);
                        it.remove();
                    }
                }

                regions.add(missing);
            } else {
                for (int i = 0; i < current.getChildrenCount(); i++) {
                    nodes.push(current.getSubNode(i));
                }
            }
        }

        if (!nodes.isEmpty()) {
            hasNext[0] = true;

            return (Node) nodes.pop();
        } else {
            hasNext[0] = false;

            return null;
        }
    }
}
