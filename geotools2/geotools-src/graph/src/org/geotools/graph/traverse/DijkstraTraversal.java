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
package org.geotools.graph.traverse;

import org.geotools.graph.Graph;
import org.geotools.graph.GraphComponent;
import org.geotools.graph.util.Heap;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;


/**
 * Traverses a graph using Dijkstras shortest path alorithm.
 *
 * @author Justin Deoliveira
 */
public class DijkstraTraversal extends SourceGraphTraversal {
    private CostFunction m_cf;
    private Heap m_processed;
    private HashMap m_ge2costNode;

    public DijkstraTraversal(Graph graph, GraphWalker walker,
        GraphComponent source, CostFunction cf) {
        super(graph, walker, source);
        m_cf = cf;
        m_processed = new Heap(new Comparator() {
                    public int compare(Object o1, Object o2) {
                        CostNode n1 = (CostNode) o1;
                        CostNode n2 = (CostNode) o2;

                        if (n1.m_cost < n2.m_cost) {
                            return (-1);
                        }

                        if (n1.m_cost > n2.m_cost) {
                            return (1);
                        }

                        return (0);
                    }
                });
        m_ge2costNode = new HashMap();
    }

    public void initNodes() {
        super.initNodes();
        initHeap(getGraph().getNodes());
    }

    public void initEdges() {
        super.initEdges();
        initHeap(getGraph().getEdges());
    }

    public CostNode getCostNode(GraphComponent element) {
        return ((CostNode) m_ge2costNode.get(element));
    }

    public Collection getActiveElements() {
        return (m_processed);
    }

    protected void initHeap(Collection elements) {
        m_processed.init(elements.size());

        for (Iterator itr = elements.iterator(); itr.hasNext();) {
            GraphComponent ge = (GraphComponent) itr.next();
            CostNode cn = new CostNode(ge, Double.MAX_VALUE);

            if (ge == getSource()) {
                cn.m_cost = 0;
            }

            m_processed.insert(cn);
            m_ge2costNode.put(ge, cn);
        }
    }

    protected void walk() {
        while (!m_processed.isEmpty()) {
            CostNode current = (CostNode) m_processed.extract();

            if (getWalker().visit(current.m_element, this) == STOP) {
                return;
            }

            for (Iterator itr = current.m_element.getAdjacentElements()
                                                 .iterator(); itr.hasNext();) {
                GraphComponent adjacent = (GraphComponent) itr.next();

                if (getWalker().isVisited(adjacent)) {
                    continue;
                }

                CostNode cn = (CostNode) m_ge2costNode.get(adjacent);
                double cost = current.m_cost
                    + m_cf.getCost(current.m_element, adjacent);

                if (cost < cn.m_cost) {
                    cn.m_cost = cost;
                    cn.m_parent = current;
                    m_processed.update(cn);
                }
            }
        }
    }

    /**
     * Represents a cost function to be used by dijkstras algorithm to
     * calculate node costs.
     */
    public interface CostFunction {
        public double getCost(GraphComponent srcElement,
            GraphComponent destElement);
    }

    /**
     * Simple data structure used to track cost of nodes and path from source.
     */
    public class CostNode {
        public GraphComponent m_element;
        public double m_cost;
        public CostNode m_parent;

        public CostNode(GraphComponent element, double cost) {
            m_element = element;
            m_cost = cost;
            m_parent = null;
        }
    }
}
