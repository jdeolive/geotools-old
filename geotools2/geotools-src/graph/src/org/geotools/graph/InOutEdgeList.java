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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * An implementaion of EdgeList that groups edges into two categories, incoming
 * and outgoing.
 *
 * @author Justin Deoliveira
 */
public class InOutEdgeList implements EdgeList {
    /** the list on incoming edges */
    private BasicEdgeList m_in;

    /** the list on outgoing edges */
    private BasicEdgeList m_out;

    /**
     * Creates a new InOutEdgeList object.
     */
    public InOutEdgeList() {
        m_in = new BasicEdgeList();
        m_out = new BasicEdgeList();
    }

    /**
     * Operation not supported. Use addIn(Edge) or addOut(Edge).
     *
     * @see EdgeList#add(Edge)
     */
    public void add(Edge edge) {
        throw new UnsupportedOperationException(
            "This EdgeList is an InOutEdgeList. Use addIn(Edge) or addOut(Edge).");
    }

    /**
     * Adds an incoming edge to the edge list.
     *
     * @param edge Edge ME!
     */
    public void addIn(Edge edge) {
        m_in.add(edge);
    }

    /**
     * Adds an outgoing edge to the edge list.
     *
     * @param edge Edge
     */
    public void addOut(Edge edge) {
        m_out.add(edge);
    }

    /**
     * Removes both an incoming and outgoing edge.
     *
     * @see EdgeList#remove(Edge)
     */
    public void remove(Edge edge) {
        removeIn(edge);
        removeOut(edge);
    }

    /**
     * Removes an incoming edge from the edge list.
     *
     * @param edge Edge
     */
    public void removeIn(Edge edge) {
        m_in.remove(edge);
    }

    /**
     * Removes an outgoing edge from the edge list.
     *
     * @param edge
     */
    public void removeOut(Edge edge) {
        m_out.remove(edge);
    }

    /**
     * Returns all the edges contained in the edge list. Incoming and outgoing.
     *
     * @return List
     */
    public List getEdges() {
        ArrayList edges = new ArrayList();
        edges.addAll(m_in.getEdges());
        edges.addAll(m_out.getEdges());

        return (edges);
    }

    /**
     * Returns the incoming edges contained in the edge list.
     *
     * @return List
     */
    public List getInEdges() {
        return (m_in.getEdges());
    }

    /**
     * Returns the outgoing edges contained in the edge list.
     *
     * @return List
     */
    public List getOutEdges() {
        return (m_out.getEdges());
    }

    /**
     * Returns an edge containted in the list specified by the two nodes. First
     * the incoming edges are examined. If no incoming edge is found, the
     * outgoing edges are examined.
     *
     * @param n1 Node
     * @param n2 Node
     *
     * @return The edge if found, otherwise null.
     */
    public Edge getEdge(Node n1, Node n2) {
        Edge edge = getInEdge(n1, n2);

        if (edge == null) {
            edge = getInEdge(n2, n1);
        }

        if (edge == null) {
            edge = getOutEdge(n1, n2);
        }

        if (edge == null) {
            edge = getOutEdge(n2, n1);
        }

        return (edge);
    }

    /**
     * Returns an incoming edge contained in the list specified by the two
     * nodes.
     *
     * @param n1 The source node of the edge.
     * @param n2 The terminal node of the edge.
     *
     * @return The edge if found, otherwise null.
     */
    public Edge getInEdge(Node n1, Node n2) {
        for (Iterator itr = m_in.getEdges().iterator(); itr.hasNext();) {
            Edge edge = (Edge) itr.next();

            if (edge.getNodeA().equals(n1) && edge.getNodeB().equals(n2)) {
                return (edge);
            }
        }

        return (null);
    }

    /**
     * Returns an outgoing edge contained in the list specified by the two
     * nodes.
     *
     * @param n1 The source node of the edge.
     * @param n2 The terminal node of the edge.
     *
     * @return The edge if found, otherwise null.
     */
    public Edge getOutEdge(Node n1, Node n2) {
        for (Iterator itr = m_out.getEdges().iterator(); itr.hasNext();) {
            Edge edge = (Edge) itr.next();

            if (edge.getNodeA().equals(n1) && edge.getNodeB().equals(n2)) {
                return (edge);
            }
        }

        return (null);
    }

    /**
     * Returns the list of nodes that are incident to edges in the list which
     * are not equal to the specified node.
     *
     * @see EdgeList#getOtherNodes(Node)
     */
    public List getOtherNodes(Node node) {
        List others = getOtherInNodes(node);
        others.addAll(getOtherOutNodes(node));

        return (others);
    }

    /**
     * Returns the list of nodes that are incident to the set of incoming edges
     * and not equal to the specified node.
     *
     * @param node Node
     *
     * @return List
     */
    public List getOtherInNodes(Node node) {
        ArrayList others = new ArrayList();

        for (Iterator itr = m_in.getEdges().iterator(); itr.hasNext();) {
            Edge edge = (Edge) itr.next();

            if (edge.getNodeB().equals(node)) {
                others.add(edge.getNodeA());
            }
        }

        return (others);
    }

    /**
     * Returns the list of nodes that are incident to the set of outgoing edges
     * and not equal to the specified node.
     *
     * @param node Node
     *
     * @return List
     */
    public List getOtherOutNodes(Node node) {
        ArrayList others = new ArrayList();

        for (Iterator itr = m_out.getEdges().iterator(); itr.hasNext();) {
            Edge edge = (Edge) itr.next();

            if (edge.getNodeA().equals(node)) {
                others.add(edge.getNodeB());
            }
        }

        return (others);
    }

    /**
     * Returns all edges present in the list minus the specified edge.
     *
     * @see EdgeList#getOtherEdges(Edge)
     */
    public List getOtherEdges(Edge edge) {
        List others = getOtherInEdges(edge);
        others.addAll(getOtherOutEdges(edge));

        return (others);
    }

    /**
     * Returns all incoming edges minus the specified edge.
     *
     * @param edge Edge
     *
     * @return Node
     */
    public List getOtherInEdges(Edge edge) {
        ArrayList others = new ArrayList();

        for (Iterator itr = m_in.getEdges().iterator(); itr.hasNext();) {
            Edge other = (Edge) itr.next();

            if (!edge.equals(other)) {
                others.add(other);
            }
        }

        return (others);
    }

    /**
     * Returns all outgoing edges minus the specified edge.
     *
     * @param edge Edge
     *
     * @return List
     */
    public List getOtherOutEdges(Edge edge) {
        ArrayList others = new ArrayList();

        for (Iterator itr = m_out.getEdges().iterator(); itr.hasNext();) {
            Edge other = (Edge) itr.next();

            if (!edge.equals(other)) {
                others.add(other);
            }
        }

        return (others);
    }

    /**
     * @see EdgeList#contains(Edge)
     */
    public boolean contains(Edge edge) {
        return (m_in.contains(edge) || m_out.contains(edge));
    }

    /**
     * Returns the sum of the number of incoming and outgoing edges.
     *
     * @see EdgeList#getSize()
     */
    public int getSize() {
        return (m_in.getSize() + m_out.getSize());
    }
}
