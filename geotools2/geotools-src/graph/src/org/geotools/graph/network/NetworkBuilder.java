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
package org.geotools.graph.network;

import com.vividsolutions.jts.geom.Coordinate;
import org.geotools.feature.Feature;
import org.geotools.graph.*;
import org.geotools.graph.build.LineGraphBuilder;


/**
 * Network builder
 *
 * @author Justin Deoliveira
 * @version $Revision: 1.1 $
 */
public class NetworkBuilder extends LineGraphBuilder {
    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean isDirected() {
        return (true);
    }

    /**
     * DOCUMENT ME!
     *
     * @param node DOCUMENT ME!
     */
    public void removeNode(Node node) {
        super.removeNode(node);
    }

    /**
     * DOCUMENT ME!
     *
     * @param newEdge DOCUMENT ME!
     */
    public void addEdge(Edge newEdge) {
        ((InOutEdgeList) newEdge.getNodeA().getEdgeList()).addOut(newEdge);
        ((InOutEdgeList) newEdge.getNodeB().getEdgeList()).addIn(newEdge);
        getEdges().add(newEdge);
    }

    /**
     * DOCUMENT ME!
     *
     * @param edge DOCUMENT ME!
     */
    public void removeEdge(Edge edge) {
        ((InOutEdgeList) edge.getNodeA().getEdgeList()).removeOut(edge);
        ((InOutEdgeList) edge.getNodeB().getEdgeList()).removeIn(edge);
        getEdges().remove(edge);
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
        return (new InOutNode(feature, new InOutEdgeList(), coordinate));
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
        return (new NetworkEdge(feature, n1, n2));
    }
}
