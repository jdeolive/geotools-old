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
package org.geotools.graph;

import java.util.List;


/**
 * Represents the edge adjacency list for a node.
 *
 * @author Justin Deoliveira
 */
public interface EdgeList {
    /**
     * Returns the edges contained in the edge list.
     *
     * @return List
     */
    public List getEdges();

    /**
     * Adds an edge to the edge list.
     *
     * @param edge the edge to be added.
     */
    public void add(Edge edge);

    /**
     * Removed an edge from the edge list.
     *
     * @param edge the edge to be removed.
     */
    public void remove(Edge edge);

    /**
     * Returns an edge in the list ended by specfic nodes.
     *
     * @param n1 Starting Edge Node
     * @param n2 Ending Edge Node
     *
     * @return The Edge
     */
    public Edge getEdge(Node n1, Node n2);

    /**
     * Returns the size of the edge list.
     *
     * @return The number of edges contained in the list.
     */
    public int getSize();

    /**
     * Returns the edges in the list minus a specific edge.
     *
     * @param edge The edge not to be returned.
     *
     * @return List
     */
    public List getOtherEdges(Edge edge);

    /**
     * Returns a collection of nodes adjacent to edges in the list minus a
     * specifc node.
     *
     * @param node The node not to be returned.
     *
     * @return List
     */
    public List getOtherNodes(Node node);

    /**
     * Determines if the edge list contains a certain edge.
     *
     * @return True if the edge is contained, false otherwise.
     */
    public boolean contains(Edge edge);
}
