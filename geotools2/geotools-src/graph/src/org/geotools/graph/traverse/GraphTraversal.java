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


/**
 * The GraphTraversal interface represents an algorithm with which to traverse
 * or walk the graph. Using a visitor pattern, the GraphTraversal dispatches a
 * GraphWalker to elements in the graph based on the traversing algorithm.
 *
 * @author Justin Deoliveira
 */
public interface GraphTraversal {
    /** flag to indicate that a traversal should continue */
    public static final int CONTINUE = 0;

    /** flag to indivate that a traversal should top */
    public static final int STOP = 1;

    /**
     * Initializes each {@link Node} in the graph in preparation for the
     * traversal.
     */
    public void initNodes();

    /**
     * Initializes each {@link Edge} in the graph in preparation for the
     * traversal.
     */
    public void initEdges();

    /**
     * Performs the traversal over the nodes of the graph.
     */
    public void walkNodes();

    /**
     * Performs the traversal over the edges of the graph.
     */
    public void walkEdges();

    /**
     * Called when the traversal has been completed.
     */
    public void finish();

    /**
     * Returns the graph the traversal is being made upon.
     *
     * @return DOCUMENT ME!
     */
    public Graph getGraph();

    /**
     * Returns the GraphWalker whom the traversal is dispatching to elements in
     * the graph.
     *
     * @return DOCUMENT ME!
     */
    public GraphWalker getWalker();
}
