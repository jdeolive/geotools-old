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
import org.geotools.graph.InOutNode;
import java.util.Iterator;
import java.util.LinkedList;


/**
 * DOCUMENT ME!
 *
 * @author Justin Deoliveira
 * @version $Revision: 1.1 $
 */
public class TopologicalTraversal extends AbstractGraphTraversal {
    /** DOCUMENT ME! */
    private LinkedList m_queue;

    /**
     * Creates a new TopologicalTraversal object.
     *
     * @param graph DOCUMENT ME!
     * @param walker DOCUMENT ME!
     */
    public TopologicalTraversal(Graph graph, GraphWalker walker) {
        super(graph, walker);
        m_queue = new LinkedList();
    }

    /**
     * DOCUMENT ME!
     */
    public void initNodes() {
        super.initNodes();

        for (Iterator itr = getGraph().getNodes().iterator(); itr.hasNext();) {
            InOutNode node = (InOutNode) itr.next();

            if (node.getInDegree() == 0) {
                m_queue.addLast(node);
            }
        }
    }

    /**
     * DOCUMENT ME!
     */
    public void walkNodes() {
        walk();
    }

    /**
     * DOCUMENT ME!
     *
     * @throws UnsupportedOperationException DOCUMENT ME!
     */
    public void walkEdges() {
        throw new UnsupportedOperationException("Edge walking not supported.");
    }

    /**
     * DOCUMENT ME!
     */
    protected void walk() {
        while (!m_queue.isEmpty()) {
            InOutNode node = (InOutNode) m_queue.removeFirst();
            getWalker().visit(node, this);

            Iterator itr;

            for (itr = node.getOutNodes().iterator(); itr.hasNext();) {
                InOutNode out = (InOutNode) itr.next();
                out.setCount(out.getCount() + 1);

                if (out.getCount() == out.getInNodes().size()) {
                    m_queue.addLast(out);
                }
            }
        }
    }
}
