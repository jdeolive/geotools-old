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

import com.vividsolutions.jts.geom.Geometry;
import org.geotools.feature.Feature;
import java.util.Collection;


/**
 * A Node is a point in the graph which which is incident 0 or more edges.
 *
 * @author Justin Deoliveira
 */
public class Node extends GraphComponent {
    /** List of edges for this node. */
    private EdgeList m_edgeList;

    /**
     * Creates a node.
     *
     * @param feature The underlying Feature represented by the node.
     * @param edgeList A list of edges adjacent to the node.
     *
     * @see GraphComponent(Feature)
     * @see Feature
     */
    public Node(Feature feature, EdgeList edgeList) {
        super(feature);
        m_edgeList = edgeList;
    }

    /**
     * Adds an edge to the adjacency list of the node.
     *
     * @param edge An edge adjacent to the node.
     */
    public void addEdge(Edge edge) {
        getEdgeList().add(edge);
    }

    /**
     * Removes an edge from the adjacency list of the node.
     *
     * @param edge Edge to be removed.
     */
    public void removeEdge(Edge edge) {
        getEdgeList().remove(edge);
    }

    /**
     * Returns the adjacency list of the node.
     *
     * @return A collection of edges adjacent to the node.
     */
    public EdgeList getEdgeList() {
        return (m_edgeList);
    }

    /**
     * Returns an edge incident with this node and another node.
     *
     * @param other A node at the other end of an adjacent edge.
     *
     * @return A single edge object adjacent to the two nodes.
     */
    public Edge getEdge(Node other) {
        return (getEdgeList().getEdge(this, other));
    }

    /**
     * Returns the degree of the node.
     *
     * @return An integer representing the number of adjacent edges to the
     *         node.
     */
    public int getDegree() {
        return (getEdgeList().getSize());
    }

    /**
     * Returns a collection of edges adjacent minus a specified edge.
     *
     * @param edge the edge not to be returned.
     *
     * @return All adjacent edges - specified edge.
     */
    public Collection getOtherEdges(Edge edge) {
        return (getEdgeList().getOtherEdges(edge));
    }

    /**
     * Returns a collection of nodes adjacent to this node. More precisely, it
     * returns a collection of nodes that are connected via an edge.
     *
     * @see GraphComponent#getAdjacentElements()
     */
    public Collection getAdjacentElements() {
        return (getEdgeList().getOtherNodes(this));
    }

    /**
     * Builds a Geometry object to represent the node spatially.
     *
     * @return Geometry
     *
     * @see GraphComponent#buildGeometry()
     * @see Geometry
     */
    public Geometry buildGeometry() {
        return (getFeature().getDefaultGeometry().getCentroid());
    }

    /**
     * Returns a string representation of the node.
     *
     * @see Object#toString()
     */
    public String toString() {
        return (String.valueOf(getID()));
    }
}
