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

import com.vividsolutions.jts.index.SpatialIndex;
import com.vividsolutions.jts.index.strtree.STRtree;
import org.geotools.data.FeatureReader;
import org.geotools.feature.Feature;
import org.geotools.graph.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;


/**
 * General purpose graph builder based on user defined relationships.
 * 
 * <p>
 * In this type of graph, the features are represented by the nodes, and the
 * relationships by the edges. Relationships are defined using the
 * FeatureRelator interface as a Stratagy object (GOF Stratagy pattern).
 * </p>
 */
public class RelationshipGraphBuilder implements GraphBuilder {
    /** DOCUMENT ME! */
    private Graph m_graph;

    /** Defines the relationship on which this graph is based. */
    private FeatureRelator m_relator;

    /** DOCUMENT ME! */
    private Collection m_nodes;

    /** DOCUMENT ME! */
    private Collection m_edges;

    /** DOCUMENT ME! */
    private SpatialIndex m_index;

    /** DOCUMENT ME! */
    private boolean m_directed;

    /**
     * Creates a new RelationshipGraphBuilder object.
     *
     * @param relator DOCUMENT ME!
     * @param index DOCUMENT ME!
     */
    public RelationshipGraphBuilder(FeatureRelator relator, SpatialIndex index) {
        m_relator = relator;
        m_index = index;
        m_nodes = new HashSet();
        m_edges = new HashSet();
        m_directed = false;
    }

    /**
     * Creates a new RelationshipGraphBuilder object.
     *
     * @param relator DOCUMENT ME!
     */
    public RelationshipGraphBuilder(FeatureRelator relator) {
        this(relator, new STRtree());
    }

    /**
     * @see GraphBuilder#add(Feature)
     */
    public GraphComponent add(Feature feature) {
        Node newNode = buildNode(feature);

        addNode(newNode);

        //insert the features geometry into a spatial index, this will save us 
        // from having to compare features to all other features present when the 
        // graph is being built
        m_index.insert(GeometryUtil.expand(feature.getDefaultGeometry()
                                                  .getEnvelopeInternal(), 100),
            newNode);

        return (newNode);
    }

    /**
     * @see GraphBuilder#removeNode(Node)
     */
    public void removeNode(Node node) {
        for (Iterator itr = node.getEdgeList().getEdges().iterator();
                itr.hasNext();) {
            Edge edge = (Edge) itr.next();
            removeEdge(edge);
        }

        m_graph.getNodes().remove(node);
    }

    /**
     * Removes a collection of nodes from the graph.
     *
     * @param nodes DOCUMENT ME!
     */
    public void removeNodes(Collection nodes) {
        for (Iterator itr = nodes.iterator(); itr.hasNext();) {
            removeNode((Node) itr.next());
        }
    }

    /**
     * @see GraphBuilder#removeEdge(Edge)
     */
    public void removeEdge(Edge edge) {
        if (!isDirected()) {
            edge.getNodeA().removeEdge(edge);
            edge.getNodeB().removeEdge(edge);
        } else {
            edge.getNodeA().removeEdge(edge);
        }

        getGraph().getEdges().remove(edge);
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
        for (Iterator itrA = m_nodes.iterator(); itrA.hasNext();) {
            Node node = (Node) itrA.next();
            List possible = m_index.query(node.getFeature().getDefaultGeometry()
                                              .getEnvelopeInternal());

            for (Iterator itrB = possible.iterator(); itrB.hasNext();) {
                Node adj = (Node) itrB.next();

                if (node == adj) {
                    continue; //if same dont create relationship
                }

                relate(node, adj);
            }
        }

        m_graph = new Graph(m_nodes, m_edges);

        return (m_graph);
    }

    /**
     * @see GraphBuilder#getGraph()
     */
    public Graph getGraph() {
        return (m_graph);
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
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    protected Collection getNodes() {
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
     * @return DOCUMENT ME!
     */
    protected FeatureRelator getRelator() {
        return (m_relator);
    }

    /**
     * DOCUMENT ME!
     *
     * @param n1 DOCUMENT ME!
     * @param n2 DOCUMENT ME!
     */
    protected void relate(Node n1, Node n2) {
        if (getRelator().relate(n1.getFeature(), n2.getFeature())) {
            addEdge(buildEdge(null, n1, n2));

            return;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param feature DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    protected Node buildNode(Feature feature) {
        return (new Node(feature, new BasicEdgeList()));
    }

    /**
     * DOCUMENT ME!
     *
     * @param newNode DOCUMENT ME!
     */
    protected void addNode(Node newNode) {
        getNodes().add(newNode);
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

    /**
     * DOCUMENT ME!
     *
     * @param newEdge DOCUMENT ME!
     */
    protected void addEdge(Edge newEdge) {
        //ensure that we dont insert duplicate edges for the same relationship
        if (((BasicEdgeList) newEdge.getNodeA().getEdgeList()).contains(
                    newEdge, true)) {
            return;
        }

        if (!isDirected()) {
            newEdge.getNodeA().addEdge(newEdge);
            newEdge.getNodeB().addEdge(newEdge);
        } else {
            newEdge.getNodeA().addEdge(newEdge);
        }

        getEdges().add(newEdge);
    }
}
