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
import java.util.Iterator;


/**
 * An abstract implementation of GraphTraversal.
 *
 * @author Justin Deoliveira
 */
public abstract class AbstractGraphTraversal implements GraphTraversal {
    /** graph to traverse */
    private Graph m_graph;

    /** walker to walk along graph */
    private GraphWalker m_walker;

    public AbstractGraphTraversal(Graph graph, GraphWalker walker) {
        m_graph = graph;
        m_walker = walker;
    }

    /**
     * @see GraphTraversal#getGraph()
     */
    public Graph getGraph() {
        return (m_graph);
    }

    /**
     * @see GraphTraversal#getWalker()
     */
    public GraphWalker getWalker() {
        return (m_walker);
    }

    /**
     * Initializes the nodes of a graph by delegating initialization to the
     * walker.
     *
     * @see GraphTraversal#initNodes()
     */
    public void initNodes() {
        for (Iterator itr = getGraph().getNodes().iterator(); itr.hasNext();) {
            getWalker().init((GraphComponent) itr.next());
        }
    }

    /**
     * Initializes the edges of a graph by delegating initialization to the
     * walker.
     *
     * @see GraphTraversal#initNodes()
     */
    public void initEdges() {
        for (Iterator itr = getGraph().getEdges().iterator(); itr.hasNext();) {
            getWalker().init((GraphComponent) itr.next());
        }
    }

    /**
     * Signals to the walker that the traversal is complete.
     *
     * @see GraphTraversal#finish()
     */
    public void finish() {
        getWalker().finish();
    }

    /**
     * Performs the walking algorithm among the nodes of the graph.
     */
    public abstract void walkNodes();

    /**
     * Performs the walking algorithm among the edges of the graph.
     */
    public abstract void walkEdges();
}
