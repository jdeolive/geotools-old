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

import org.geotools.graph.GraphComponent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class Path {
    ArrayList m_elements;

    public Path() {
        m_elements = new ArrayList();
    }

    public void add(GraphComponent element) {
        m_elements.add(element);
    }

    public List getElements() {
        return (m_elements);
    }

    public int size() {
        return (m_elements.size());
    }

    public Iterator iterator() {
        return (new Iterator() {
                int i = m_elements.size() - 1;

                public void remove() {
                    throw new UnsupportedOperationException();
                }

                public boolean hasNext() {
                    return (i > -1);
                }

                public Object next() {
                    return (m_elements.get(i--));
                }
            });
    }

    public boolean isEmpty() {
        return (m_elements.isEmpty());
    }
}
