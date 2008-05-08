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
package org.geotools.caching.grid;

import org.geotools.caching.spatialindex.Data;
import org.geotools.caching.spatialindex.Node;
import org.geotools.caching.spatialindex.Visitor;


class InvalidatingVisitor implements Visitor {
    public boolean isDataVisitor() {
        return false;
    }

    public void visitData(Data d) {
        // do nothing
    }

    public void visitNode(Node n) {
        n.getIdentifier().setValid(false);

        if (n instanceof GridCacheNode) {
            GridCacheNode node = (GridCacheNode) n;
            node.clear();
        }
    }
}
