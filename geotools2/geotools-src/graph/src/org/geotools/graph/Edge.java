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
import org.geotools.feature.IllegalAttributeException;
import java.util.ArrayList;
import java.util.Collection;


/**
 * An Edge connects two nodes in the graph.
 *
 * @author Justin Deoliveira
 */
public class Edge extends GraphComponent {
    /** nodes of the egde */
    private Node m_nodeA;

    /** DOCUMENT ME! */
    private Node m_nodeB;

    /**
     * Creates an edge.
     *
     * @param feature Underlying Feature represented by the edge.
     * @param nodeA Node at the source of the edge.
     * @param nodeB Node at the destination of the edge.
     *
     * @see Node
     * @see Feature
     */
    public Edge(Feature feature, Node nodeA, Node nodeB) {
        super(feature);
        m_nodeA = nodeA;
        m_nodeB = nodeB;
    }

    /**
     * Returns the node at the source of the Edge.
     *
     * @return Node
     */
    public Node getNodeA() {
        return (m_nodeA);
    }

    /**
     * Returns the node at the destination of the edge.
     *
     * @return
     */
    public Node getNodeB() {
        return (m_nodeB);
    }

    /**
     * Returns the node of the edge opposite of the specified node.
     *
     * @param node The node opposite of the desired node to be returned.
     *
     * @return If the specified node is the source node, the destination node
     *         is returned, and vice versa.
     */
    public Node getOtherNode(Node node) {
        if (node.equals(m_nodeA)) {
            return (m_nodeB);
        }

        if (node.equals(m_nodeB)) {
            return (m_nodeA);
        }

        return (null);
    }

    /**
     * Returns the edges adjacent to this edge. More specifically, edges
     * adjacent to the source and destination node, minus this edge, are
     * returned.
     *
     * @return Collection
     */
    public Collection getAdjacentElements() {
        ArrayList adj = new ArrayList();
        adj.addAll(m_nodeA.getOtherEdges(this));
        adj.addAll(m_nodeB.getOtherEdges(this));

        return (adj);
    }

    /**
     * Reverses the direction of the edge by swapping the two nodes. As well,
     * the  geometry used to represent the edge spatially is also reversed.
     *
     * @param deep Indicates wether the geometry of the underlying feature
     *        represented by the edge should be reversed.
     */
    public void reverse(boolean deep) {
        Node nodeB = m_nodeB;
        m_nodeB = m_nodeA;
        m_nodeA = nodeB;

        try {
            setDefaultGeometry(GeometryUtil.reverseGeometry(
                    getDefaultGeometry()));
        } catch (IllegalAttributeException e1) {
            // TODO Auto-generated catch block
            //TODO:visit
        }

        if (deep) {
            Geometry reverse = GeometryUtil.reverseGeometry(getFeature()
                                                                .getDefaultGeometry());

            try {
                getFeature().setDefaultGeometry(reverse);
            } catch (IllegalAttributeException e) {
                // very bad should not happen
                // TODO: revist
            }
        }
    }

    /**
     * Compares the nodes of the edge with the nodes of another edge.
     *
     * @param other Edge to be compared.
     *
     * @return True if the two sets of nodes are equal in correct order. That
     *         is that both A nodes are equals, and both B nodes are equal.
     */
    public boolean endpointEquals(Edge other) {
        return (m_nodeA.equals(other.m_nodeA) && m_nodeB.equals(other.m_nodeB));
    }

    /**
     * Compares the nodes of the edge with the nodes of another edge in reverse
     * order.
     *
     * @param other Edge to be compared to in reverse.
     *
     * @return True if the nodes in both pairs of A,B nodes are equal.
     */
    public boolean endpointEqualsReverse(Edge other) {
        return (m_nodeA.equals(other.m_nodeB) && m_nodeB.equals(other.m_nodeA));
    }

    /**
     * Calls equals(Edge).
     *
     * @see Object#equals(Object)
     */
    public boolean equals(Object other) {
        return (equals((Edge) other));
    }

    /**
     * Determines equality between two edges.
     *
     * @param other Edge to be compared.
     *
     * @return True if the two nodes of the edges are equal in correct order
     *         and if the geometries used to represent the edges are equal,
     *         otherwise  false.
     */
    public boolean equals(Edge other) {
        if (this == other) {
            return (true);
        }

        return (endpointEquals(other)
        && GeometryUtil.isEqual(getDefaultGeometry().getCoordinates(),
            other.getDefaultGeometry().getCoordinates()));
    }

    /**
     * Determines equality between two edges in reverse.
     *
     * @param other Edge to be compared.
     *
     * @return True if the two nodes of the edges are equal in reverse order
     *         and if the geomtries used to represent the edges spatially are
     *         the reverse of each other, otherwise false is returned.
     */
    public boolean equalsReverse(Edge other) {
        return (m_nodeA.equals(other.m_nodeA) && m_nodeB.equals(other.m_nodeB)
        && GeometryUtil.isEqual(getDefaultGeometry().getCoordinates(),
            other.getDefaultGeometry().getCoordinates(), true));
    }

    /**
     * Builds a Geometry object to represent the edge spatially.
     *
     * @see Geometry
     */
    public Geometry buildGeometry() {
        //simple create line segment between the two nodes
        //    return(
        //      new GeometryFactory().createLineString(
        //        new Coordinate[]{
        //          m_nodeA.getGeometry().getCoordinate(),
        //          m_nodeB.getGeometry().getCoordinate(), 
        //        }
        //      )
        //    );
        return (getFeature().getDefaultGeometry());
    }

    /**
     * Returns the string representation of the edge.
     *
     * @see Object#toString()
     */
    public String toString() {
        return ("(" + getNodeA().toString() + "," + getNodeB().toString() + ")");
    }
}
