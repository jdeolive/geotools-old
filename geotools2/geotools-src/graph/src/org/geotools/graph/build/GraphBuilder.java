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

import org.geotools.feature.Feature;
import org.geotools.graph.*;


/**
 * Interface for building graph components. Graphs are built by continously
 * supplying features to the builder, see {@link FeatureReader}
 */
public interface GraphBuilder {
    /**
     * Adds a feature to the graph.
     *
     * @param feature Feature to be added. The feature will be encapsulated
     *        with  a {@link GraphComponent}, either a {@link Node} or a
     *        {@link Edge}. Which  one is implentation dependant.
     *
     * @return The graph component that encapsulates the added feature.
     */
    public GraphComponent add(Feature feature);

    /**
     * Removes a node from the graph.
     */
    public void removeNode(Node node);

    /**
     * Removes an edge from the graph.
     *
     * @param edge
     */
    public void removeEdge(Edge edge);

    /**
     * Signals the builder to complete the graph build.
     *
     * @return The built Graph object.
     */
    public Graph build();

    /**
     * Returns the graph built by the builder.
     *
     * @return Graph
     *
     * @see Graph
     */
    public Graph getGraph();

    /**
     * Sets the graph to be directed/undirected.
     *
     * @param directed True if directed, false if undirected.
     */
    public void setDirected(boolean directed);

    /**
     * Indicates wether or not the graph is directed.
     *
     * @return True if directed, false if undirected.
     */
    public boolean isDirected();
}
