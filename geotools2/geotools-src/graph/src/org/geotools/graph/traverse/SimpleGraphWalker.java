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

import org.geotools.graph.GraphComponent;


/**
 * A simple implentation of GraphWalker that decorates a   GraphVisitor.
 *
 * @author Justin Deoliveira
 */
public class SimpleGraphWalker implements GraphWalker {
    /** Underlying visitor */
    private GraphVisitor m_visitor;

    /**
     * Creates a GraphWalker from a preexising GraphVisitor
     *
     * @param visitor DOCUMENT ME!
     */
    public SimpleGraphWalker(GraphVisitor visitor) {
        m_visitor = visitor;
    }

    /**
     * Resets visited flag and counter.
     *
     * @see GraphWalker#init(GraphComponent)
     */
    public void init(GraphComponent element) {
        element.setVisited(false);
        element.setCount(0);
    }

    /**
     * Returns the visited flag of the graph component.
     *
     * @see GraphWalker#isVisited(GraphComponent)
     */
    public boolean isVisited(GraphComponent element) {
        return (element.isVisited());
    }

    /**
     * Sets the visted flag, and passes the element on to underlying visitor.
     *
     * @param element DOCUMENT ME!
     * @param traversal DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int visit(GraphComponent element, GraphTraversal traversal) {
        element.setVisited(true);

        return (m_visitor.visit(element));
    }

    /**
     * Does nothing.
     *
     * @see GraphWalker#finish()
     */
    public void finish() {
    }
}
