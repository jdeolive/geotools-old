/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
 *
 */
package org.geotools.graph.traverse;

import org.geotools.graph.Graph;
import org.geotools.graph.GraphComponent;
import java.util.Collection;
import java.util.Iterator;


/**
 * A very simple traversal in which a single pass is made over the components
 * of the graph.
 *
 * @author Justin Deoliveira
 */
public class BasicGraphTraversal extends AbstractGraphTraversal {
    public BasicGraphTraversal(Graph graph, GraphWalker walker) {
        super(graph, walker);
    }

    /**
     * Iterates over all nodes in the graph.
     */
    public void walkNodes() {
        walk(getGraph().getNodes());
    }

    /**
     * Iterates over all edges in the graph.
     */
    public void walkEdges() {
        walk(getGraph().getEdges());
    }

    protected void walk(Collection elements) {
        for (Iterator itr = elements.iterator(); itr.hasNext();) {
            if (getWalker().visit((GraphComponent) itr.next(), this) == STOP) {
                return;
            }
        }
    }
}
