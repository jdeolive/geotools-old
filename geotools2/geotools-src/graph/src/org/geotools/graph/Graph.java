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

import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.graph.traverse.GraphTraversal;
import org.geotools.graph.traverse.GraphVisitor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;


/**
 * A Graph is a structure that represents the relationship between a collection
 * of features. These relationships can be represented as either edges or
 * nodes in the graph.
 *
 * @author Justin Deoliveira
 */
public class Graph {
    /** Nodes contained in the graph */
    private Collection m_nodes;

    /** Edges contained in the graph */
    private Collection m_edges;

    /**
     * Creates a graph that contains a collection of nodes and edges.
     *
     * @param nodes
     * @param edges
     */
    public Graph(Collection nodes, Collection edges) {
        m_nodes = nodes;
        m_edges = edges;
    }

    /**
     * Returns the nodes of the graph.
     *
     * @return A collection of Node objects.
     *
     * @see Node
     */
    public Collection getNodes() {
        return (m_nodes);
    }

    /**
     * Returns the edges of the graph.
     *
     * @return A collection of Edge objects.
     *
     * @see Edge
     */
    public Collection getEdges() {
        return (m_edges);
    }

    /**
     * Performs a query against the nodes of the graph. Each Node object
     * contained in the graph is passed to a GraphVisitor to determine if  it
     * meets the query criteria.
     *
     * @param visitor Returns a non zero integer value if the node meets the
     *        query criteria.
     *
     * @return A collection of nodes that meet the query criteria.
     *
     * @see Node
     * @see GraphVisitor
     */
    public Collection queryNodes(GraphVisitor visitor) {
        ArrayList result = new ArrayList();

        for (Iterator itr = m_nodes.iterator(); itr.hasNext();) {
            Node node = (Node) itr.next();

            if (visitor.visit(node) != 0) {
                result.add(node);
            }
        }

        return (result);
    }

    /**
     * Performs a query against the edges of the graph. Each Edge object
     * contained in the graph is passed to a GraphVisitor to determine if  it
     * meets the query criteria.
     *
     * @param visitor Returns a non zero integer value if the edge meets the
     *        query criteria.
     *
     * @return A collection of edge that meet the query criteria.
     *
     * @see Edge
     * @see GraphVisitor
     */
    public Collection queryEdges(GraphVisitor visitor) {
        ArrayList result = new ArrayList();

        for (Iterator itr = m_edges.iterator(); itr.hasNext();) {
            Edge edge = (Edge) itr.next();

            if (visitor.visit(edge) != 0) {
                result.add(edge);
            }
        }

        return (result);
    }

    /**
     * Returns all the nodes in the graph of a specified degree.
     *
     * @param n degree of nodes to be returned.
     *
     * @return A collection of nodes of degree n.
     */
    public Collection getNodesOfDegree(int n) {
        final int degree = n;

        return (queryNodes(new GraphVisitor() {
                public int visit(GraphComponent element) {
                    if (((Node) element).getDegree() == degree) {
                        return (1);
                    }

                    return (0);
                }
            }));
    }

    /**
     * Returns all the nodes in the graph that have been marked as visited or
     * non-visited.
     *
     * @param visited True if node is visited, false if node is unvisited.
     *
     * @return Collection of nodes marked as visited / non-visited.
     */
    public Collection getVisitedNodes(boolean visited) {
        final boolean isVisited = visited;

        return (queryNodes(new GraphVisitor() {
                public int visit(GraphComponent element) {
                    if (((Node) element).isVisited() == isVisited) {
                        return (1);
                    }

                    return (0);
                }
            }));
    }

    /**
     * Traverses the nodes of a the graph.
     *
     * @param traversal Specifies the algorithm in which to traverse the graph.
     */
    public void traverseNodes(GraphTraversal traversal) {
        traversal.initNodes();
        traversal.walkNodes();
        traversal.finish();
    }

    /**
     * Traverses the edges of the graph.
     *
     * @param traversal Specifies the algorithm in which to traverse the graph.
     */
    public void traverseEdges(GraphTraversal traversal) {
        traversal.initEdges();
        traversal.walkEdges();
        traversal.finish();
    }

    /**
     * Initializes the nodes in the graph by setting all visited flags to false
     * and all counts to zero.
     */
    public void initNodes() {
        for (Iterator itr = m_nodes.iterator(); itr.hasNext();) {
            Node node = (Node) itr.next();
            node.setVisited(false);
            node.setCount(0);
        }
    }

    /**
     * Initializes the edges in the graph by setting all visited flags to false
     * and all counts to zero.
     */
    public void initEdges() {
        for (Iterator itr = m_nodes.iterator(); itr.hasNext();) {
            Edge edge = (Edge) itr.next();
            edge.setVisited(false);
            edge.setCount(0);
        }
    }

    /**
     * Initialises the graph.
     */
    public void init() {
        initNodes();
        initEdges();
    }

    /**
     * Convenience method for wrapping the nodes of the graph in a
     * FeatureCollection.
     *
     * @return FeatureCollection
     *
     * @see FeatureCollection
     */
    public FeatureCollection asNodeFC() {
        FeatureCollection collection = FeatureCollections.newCollection();

        collection.addAll(m_nodes);

        return collection;
    }

    /**
     * Convenience method for wrapping the edges of the graph in a
     * FeatureCollection.
     *
     * @return FeatureCollection
     *
     * @see FeatureCollection
     */
    public FeatureCollection asEdgeFC() {
        FeatureCollection collection = FeatureCollections.newCollection();

        collection.addAll(m_edges);

        return collection;
    }
}
