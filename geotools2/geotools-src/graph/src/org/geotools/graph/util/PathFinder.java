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

import org.geotools.graph.*;
import org.geotools.graph.traverse.*;


public class PathFinder implements GraphVisitor {
    private DijkstraTraversal m_dt;

    public PathFinder(Graph graph, GraphComponent source,
        DijkstraTraversal.CostFunction cf) {
        m_dt = new DijkstraTraversal(graph, new SimpleGraphWalker(this),
                source, cf);
    }

    public void calculate() {
        if (m_dt.getSource() instanceof Node) {
            m_dt.getGraph().traverseNodes(m_dt);
        } else if (m_dt.getSource() instanceof Edge) {
            m_dt.getGraph().traverseNodes(m_dt);
        }
    }

    public Path getPath(GraphComponent element) {
        Path path = new Path();
        DijkstraTraversal.CostNode costNode = m_dt.getCostNode(element);

        if (costNode == null) {
            return (path);
        }

        while (costNode.m_element != m_dt.getSource()) {
            path.add(costNode.m_element);
            costNode = costNode.m_parent;

            if (costNode == null) {
                return (new Path());
            }
        }

        path.add(m_dt.getSource());

        return (path);
    }

    public double getCost(GraphComponent element) {
        double cost = 0d;
        DijkstraTraversal.CostNode costNode = m_dt.getCostNode(element);

        while (costNode.m_element != m_dt.getSource()) {
            cost += costNode.m_cost;
            costNode = costNode.m_parent;
        }

        return (cost);
    }

    public int visit(GraphComponent element) {
        return 0;
    }
}
