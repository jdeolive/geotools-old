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
 * Basic implentation of an edge adjacency list.
 *
 * @author Justin Deoliveira
 *
 * @see EdgeList
 */
public class BasicEdgeList implements EdgeList {
    /** edges contained by the list */
    private ArrayList m_edges;

    /**
     * Creates the edge list.
     */
    public BasicEdgeList() {
        m_edges = new ArrayList();
    }

    /**
     * @see EdgeList#add(Edge)
     */
    public void add(Edge edge) {
        m_edges.add(edge);
    }

    /**
     * @see EdgeList#remove(Edge)
     */
    public void remove(Edge edge) {
        m_edges.remove(edge);
    }

    /**
     * @see EdgeList#getEdges()
     */
    public List getEdges() {
        return (m_edges);
    }

    /**
     * @see EdgeList#getEdge(Node, Node)
     */
    public Edge getEdge(Node n1, Node n2) {
        for (Iterator itr = m_edges.iterator(); itr.hasNext();) {
            Edge edge = (Edge) itr.next();

            if ((edge.getNodeA().equals(n1) && edge.getNodeB().equals(n2))
                    || (edge.getNodeA().equals(n2)
                    && edge.getNodeB().equals(n1))) {
                return (edge);
            }
        }

        return (null);
    }

    /**
     * @see EdgeList#getSize()
     */
    public int getSize() {
        return (m_edges.size());
    }

    /**
     * @see EdgeList#getOtherNodes(Node)
     */
    public List getOtherNodes(Node node) {
        ArrayList others = new ArrayList();

        for (Iterator itr = m_edges.iterator(); itr.hasNext();) {
            Edge edge = (Edge) itr.next();

            if (edge.getNodeA().equals(node)) {
                others.add(edge.getNodeB());
            } else if (edge.getNodeB().equals(node)) {
                others.add(edge.getNodeA());
            }
        }

        return (others);
    }

    /**
     * @see EdgeList#getOtherEdges(Edge)
     */
    public List getOtherEdges(Edge edge) {
        ArrayList others = new ArrayList();

        for (Iterator itr = m_edges.iterator(); itr.hasNext();) {
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
        return (contains(edge, false));
    }

    /**
     * Determines if an edge is contained in the list.
     *
     * @param edge Edge to test for existance in list.
     * @param reverse Wether or not consider two edges that are the reverse  of
     *        each other equal. True if so, otherwise false.
     *
     * @return True if the edge is contained, false otherwise.
     */
    public boolean contains(Edge edge, boolean reverse) {
        for (Iterator itr = m_edges.iterator(); itr.hasNext();) {
            Edge other = (Edge) itr.next();

            if ((edge.equals(other) || (reverse)) ? edge.equalsReverse(other)
                                                      : false) {
                return (true);
            }
        }

        return (false);
    }
}
