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
 * A GraphWalker walks a graph via a {@link GraphTraversal}. As well as simply
 * visiting components of the graph when received from a traversal, the walker
 * can also control the traversal by initializing the elements of a graph and
 * determining when a component is considered to be visited.
 *
 * @author Justin Deoliveira
 */
public interface GraphWalker {
    /**
     * Inializes a graph component
     */
    public void init(GraphComponent element);

    /**
     * Determines if a graph component has been visited.
     *
     * @return DOCUMENT ME!
     */
    public boolean isVisited(GraphComponent element);

    /**
     * Visits a graph componenet.
     *
     * @param element The component to visit.
     * @param traversal The traversal controlling the sequence of graph
     *        component visits.
     *
     * @return DOCUMENT ME!
     */
    public int visit(GraphComponent element, GraphTraversal traversal);

    /**
     * Called when the graph traversal is completed.
     */
    public void finish();
}
