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

import org.geotools.graph.Graph;
import org.geotools.graph.GraphComponent;
import org.geotools.graph.Node;
import org.geotools.graph.traverse.GraphTraversal;
import org.geotools.graph.traverse.GraphVisitor;
import org.geotools.graph.traverse.GraphWalker;
import org.geotools.graph.traverse.ReverseDepthFirstTraversal;
import org.geotools.graph.traverse.SimpleGraphWalker;
import org.geotools.graph.traverse.TopologicalTraversal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;


//TODO:change so that this class does not implement GraphWalker, since it 
// performs two different types of traversals, it should create walkers for 
// them instead of creaing a walker for one, and being the walker for another
public class CycleFinder implements GraphWalker {
    private Graph m_graph;
    private int m_nvisited;
    private Set m_visited;
    private ReverseDepthFirstTraversal m_pot;
    private boolean m_found;

    public CycleFinder(Graph graph) {
        m_graph = graph;
        m_nvisited = 0;
    }

    public boolean containsCycle() {
        m_nvisited = 0;

        GraphVisitor visitor = new GraphVisitor() {
                public int visit(GraphComponent element) {
                    m_nvisited++;

                    return (0);
                }
            };

        TopologicalTraversal traversal = new TopologicalTraversal(m_graph,
                new SimpleGraphWalker(visitor));

        m_graph.traverseNodes(traversal);

        if (m_graph.getNodes().size() == m_nvisited) {
            return (false);
        }

        return (true);
    }

    public void calculateCycle() {
        m_found = false;

        while (!m_found) {
            m_visited = new LinkedHashSet();

            //find a node that has not been visited
            for (Iterator itr = m_graph.getNodes().iterator(); itr.hasNext();) {
                Node node = (Node) itr.next();

                if (!node.isVisited()) {
                    m_pot = new ReverseDepthFirstTraversal(m_graph, this, node);

                    break;
                }
            }

            if (m_pot == null) {
                return;
            }

            m_visited.add(m_pot.getSource());
            m_graph.traverseNodes(m_pot);
        }
    }

    public List getCycleElements() {
        if (m_found) {
            ArrayList cycle = new ArrayList();
            cycle.addAll(m_pot.getActiveElements());

            return (cycle);
        }

        return (null);
    }

    public int visit(GraphComponent element, GraphTraversal traversal) {
        if (m_visited.contains(element)) {
            //we have found a cycle
            m_found = true;

            boolean add = false;

            return (GraphTraversal.STOP);
        }

        m_visited.add(element);
        element.setVisited(true);

        return (GraphTraversal.CONTINUE);
    }

    public boolean isVisited(GraphComponent element) {
        return (element.isVisited());
    }

    public void init(GraphComponent element) {
    }

    public void finish() {
        if (!m_found) {
            //could not find a cycle in this iteration, mark all visited
            for (Iterator itr = m_visited.iterator(); itr.hasNext();) {
                GraphComponent element = (GraphComponent) itr.next();
                element.setVisited(true);
                m_nvisited++;
            }
        }
    }
}
