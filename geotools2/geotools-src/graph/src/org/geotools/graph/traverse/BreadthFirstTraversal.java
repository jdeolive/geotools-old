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
import java.util.LinkedList;


/**
 * Performs a Breadth First Traversal of the graph.
 *
 * @author Justin Deoliveira
 */
public class BreadthFirstTraversal extends SourceGraphTraversal {
    private LinkedList m_active;

    public BreadthFirstTraversal(Graph graph, GraphWalker walker,
        GraphComponent source) {
        super(graph, walker, source);
    }

    public Collection getActiveElements() {
        return (m_active);
    }

    protected void walk() {
        m_active = new LinkedList();
        m_active.add(getSource());

        GraphComponent element = null;
        GraphComponent adjacent = null;

        while (!m_active.isEmpty()) {
            element = (GraphComponent) m_active.removeFirst();

            if (getWalker().isVisited(element)) {
                continue;
            }

            if (getWalker().visit(element, this) == STOP) {
                return;
            }

            Iterator itr;

            for (itr = element.getAdjacentElements().iterator(); itr.hasNext();) {
                adjacent = (GraphComponent) itr.next();

                if (!getWalker().isVisited(adjacent)) {
                    m_active.addLast(adjacent);
                }
            }
        }
    }
}
