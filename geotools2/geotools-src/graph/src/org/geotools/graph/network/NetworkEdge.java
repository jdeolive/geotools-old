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

import com.vividsolutions.jts.geom.Geometry;
import org.geotools.feature.Feature;
import org.geotools.graph.Edge;
import org.geotools.graph.Node;


/**
 * An Edge connects two nodes in the network.
 *
 * @author Justin Deoliveira
 */
public class NetworkEdge extends Edge {
    /**
     * Creates a NetworkEdge instance.
     *
     * @param feature Underlying Feature represented by the edge.
     * @param nodeA Node at the source of the edge.
     * @param nodeB Node at the destination of the edge.
     */
    public NetworkEdge(Feature feature, Node nodeA, Node nodeB) {
        super(feature, nodeA, nodeB);
    }

    /**
     * Builds a Geometry object to represent the network edge spatially.
     *
     * @see Geometry
     */
    public Geometry buildGeometry() {
        return (getFeature().getDefaultGeometry());
    }
}
