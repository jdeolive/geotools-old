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
package org.geotools.graph.util;

import org.geotools.feature.Feature;
import org.geotools.graph.*;
import org.geotools.graph.build.GraphBuilder;
import org.geotools.graph.traverse.*;
import java.util.Collection;
import java.util.Iterator;


public class GraphFuser implements GraphWalker {
    private GraphBuilder m_builder;
    private FeatureJoiner m_joiner;
    private boolean m_continue;

    public GraphFuser(GraphBuilder builder, FeatureJoiner joiner) {
        m_builder = builder;
        m_joiner = joiner;
        m_continue = true;
    }

    public void fuse() {
        GraphTraversal traversal = new BasicGraphTraversal(m_builder.getGraph(),
                this);

        while (m_continue)
            m_builder.getGraph().traverseNodes(traversal);
    }

    public void init(GraphComponent element) {
        element.setVisited(false);
    }

    public boolean isVisited(GraphComponent element) {
        return (element.isVisited());
    }

    public int visit(GraphComponent element, GraphTraversal traversal) {
        Node node = (Node) element;

        if (node.getDegree() == 2) {
            for (Iterator itr = node.getAdjacentElements().iterator();
                    itr.hasNext();) {
                Node adj = (Node) itr.next();

                if (adj.isVisited()) {
                    return (GraphTraversal.CONTINUE);
                }
            }

            element.setVisited(true);
        }

        return (GraphTraversal.CONTINUE);
    }

    public void finish() {
        Collection visited = m_builder.getGraph().getVisitedNodes(true);

        if (visited.size() == 0) {
            m_continue = false;

            return;
        }

        for (Iterator itr = visited.iterator(); itr.hasNext();) {
            Node node = (Node) itr.next();
            Edge e1 = (Edge) node.getEdgeList().getEdges().get(0);
            Edge e2 = (Edge) node.getEdgeList().getEdges().get(1);
            Node n1 = e1.getOtherNode(node);
            Node n2 = e2.getOtherNode(node);

            Feature joined = m_joiner.join(e1.getFeature(), e2.getFeature());
            m_builder.removeNode(node);
            m_builder.add(joined);
        }
    }

    public interface FeatureJoiner {
        public Feature join(Feature f1, Feature f2);
    }
}
