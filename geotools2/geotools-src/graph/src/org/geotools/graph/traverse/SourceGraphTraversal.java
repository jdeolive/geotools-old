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


/**
 * A GraphTraversal that is intened to start from a single graph componenet.
 * This component is known as the source of the traversal.
 *
 * @author Justin Deoliveira
 */
public abstract class SourceGraphTraversal extends AbstractGraphTraversal {
    /** source of traversal */
    private GraphComponent m_source;

    public SourceGraphTraversal(Graph graph, GraphWalker walker,
        GraphComponent source) {
        super(graph, walker);
        m_source = source;
    }

    /**
     * Returns the source of the traversal.
     *
     * @return DOCUMENT ME!
     */
    public GraphComponent getSource() {
        return (m_source);
    }

    /**
     * @see GraphTraversal#walkNodes()
     */
    public void walkNodes() {
        walk();
    }

    /**
     * @see GraphTraversal#walkEdges()
     */
    public void walkEdges() {
        walk();
    }

    /**
     * Returns the active elements of the traversal. That is elements that  are
     * in the process of being visited, or are queued to be visited.
     *
     * @return DOCUMENT ME!
     */
    public abstract Collection getActiveElements();

    protected abstract void walk();
}
