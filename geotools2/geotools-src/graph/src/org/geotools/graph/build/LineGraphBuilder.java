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
package org.geotools.graph.build;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import org.geotools.feature.Feature;
import org.geotools.graph.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;


/**
 * Implentation of GraphBuilder that builds graphs from linear features.
 * 
 * <p>
 * In this type of graph, Features are represented by the edges, and
 * relationships by the nodes. A feature is related to another feature when
 * one of its enpoints is shared with one of the endpoints of another feature.
 * </p>
 */
public class LineGraphBuilder implements GraphBuilder {
    /** DOCUMENT ME! */
    private HashMap m_nodes;

    /** DOCUMENT ME! */
    private HashSet m_edges;

    /** DOCUMENT ME! */
    private Graph m_graph;

    /** DOCUMENT ME! */
    private Coordinate m_dummy;

    /** DOCUMENT ME! */
    private boolean m_reversed;

    /** DOCUMENT ME! */
    private boolean m_directed;

    /**
     * Creates a new LineGraphBuilder object.
     */
    public LineGraphBuilder() {
        m_nodes = new HashMap();
        m_edges = new HashSet();
        m_dummy = new Coordinate(0, 0);
    }

    /**
     * Sets the relationships in the graph to be reversed.
     *
     * @param reversed True for reversed, false for normal.
     */
    public void setReversed(boolean reversed) {
        m_reversed = reversed;
    }

    /**
     * Determines if the relationships in the graph are reversed.
     *
     * @return True is reversed, otherwise false.
     */
    public boolean isReversed() {
        return (m_reversed);
    }

    /**
     * @see GraphBuilder#setDirected(boolean)
     */
    public void setDirected(boolean directed) {
        m_directed = directed;
    }

    /**
     * @see GraphBuilder#isDirected()
     */
    public boolean isDirected() {
        return (m_directed);
    }

    /**
     * Adds feature to the graph.
     * 
     * <p>
     * This is a custom implementaiton in which only the end points of the
     * feature are considered (LineString geometry is required ).
     * </p>
     *
     * @param feature Feature with LineString geometry
     *
     * @return DOCUMENT ME!
     *
     * @throws IllegalArgumentException DOCUMENT ME!
     *
     * @see GraphBuilder#add(Feature)
     */
    public GraphComponent add(Feature feature) {
        Geometry geometry = feature.getDefaultGeometry();

        if ((geometry == null) || !(geometry instanceof LineString)) {
            throw new IllegalArgumentException(
                "LineString geometry required for graph");
        }

        LineString lineString = (LineString) geometry;

        m_dummy.x = lineString.getCoordinateN(0).x;
        m_dummy.y = lineString.getCoordinateN(0).y;

        Node n1 = (Node) createNode(null, m_dummy);

        m_dummy.x = lineString.getCoordinateN(lineString.getNumPoints() - 1).x;
        m_dummy.y = lineString.getCoordinateN(lineString.getNumPoints() - 1).y;

        Node n2 = (Node) createNode(null, m_dummy);

        Edge edge = null;

        if (!m_reversed) {
            edge = buildEdge(feature, n1, n2);
        } else {
            edge = buildEdge(feature, n2, n1);
        }

        addEdge(edge);

        return (edge);
    }

    /**
     * @see GraphBuilder#removeNode(Node)
     */
    public void removeNode(Node node) {
        ArrayList edgesToRemove = new ArrayList(node.getEdgeList().getSize());
        edgesToRemove.addAll(node.getEdgeList().getEdges());
        removeEdges(edgesToRemove);

        m_dummy.x = ((PointNode) node).x();
        m_dummy.y = ((PointNode) node).y();
        getNodes().remove(m_dummy);
    }

    /**
     * Removes an edge from the graph.
     *
     * @param edge DOCUMENT ME!
     */
    public void removeEdge(Edge edge) {
        edge.getNodeA().getEdgeList().remove(edge);

        if (!isDirected()) {
            edge.getNodeB().getEdgeList().remove(edge);
        }

        getEdges().remove(edge);
    }

    /**
     * Removes a collection of edges from the graph.
     *
     * @param edges DOCUMENT ME!
     */
    public void removeEdges(Collection edges) {
        for (Iterator itr = edges.iterator(); itr.hasNext();) {
            removeEdge((Edge) itr.next());
        }
    }

    /**
     * @see GraphBuilder#build()
     */
    public Graph build() {
        m_graph = new Graph(m_nodes.values(), m_edges);

        return (m_graph);
    }

    /**
     * @see GraphBuilder#getGraph()
     */
    public Graph getGraph() {
        return (m_graph);
    }

    /**
     * Returns a node that has been built at a specific coordinate.
     *
     * @param coord DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Node getNode(Coordinate coord) {
        m_dummy.x = coord.x;
        m_dummy.y = coord.y;

        return ((Node) getNodes().get(m_dummy));
    }

    /**
     * DOCUMENT ME!
     *
     * @param newEdge DOCUMENT ME!
     */
    protected void addEdge(Edge newEdge) {
        newEdge.getNodeA().addEdge(newEdge);

        if (!isDirected()) {
            newEdge.getNodeB().addEdge(newEdge);
        }

        getEdges().add(newEdge);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    protected Map getNodes() {
        return (m_nodes);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    protected Collection getEdges() {
        return (m_edges);
    }

    /**
     * DOCUMENT ME!
     *
     * @param feature DOCUMENT ME!
     * @param coordinate DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    protected Node createNode(Feature feature, Coordinate coordinate) {
        Node node = (Node) m_nodes.get(coordinate);

        if (node == null) {
            node = buildNode(feature, coordinate);
            m_nodes.put(coordinate, node);
        }

        return (node);
    }

    /**
     * DOCUMENT ME!
     *
     * @param feature DOCUMENT ME!
     * @param coordinate DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    protected Node buildNode(Feature feature, Coordinate coordinate) {
        return (new PointNode(feature, new BasicEdgeList(), coordinate));
    }

    /**
     * DOCUMENT ME!
     *
     * @param feature DOCUMENT ME!
     * @param n1 DOCUMENT ME!
     * @param n2 DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    protected Edge buildEdge(Feature feature, Node n1, Node n2) {
        return (new Edge(feature, n1, n2));
    }
}
