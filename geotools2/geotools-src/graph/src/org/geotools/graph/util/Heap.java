/* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geotools.graph.util;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;


public class Heap implements Collection {
    private Comparator m_comparator = null;
    private Object[] m_values = null;
    private int m_count = 0;

    public Heap(Comparator comparator) {
        m_comparator = comparator;
    }

    public void init(int size) {
        if ((m_values == null) || (size > m_values.length)) {
            m_values = new Object[size + 1];
        }

        for (int i = 0; i < m_values.length; i++) {
            m_values[i] = null;
        }

        m_count = 0;
    }

    public void insert(Object value) {
        ++m_count;

        if (m_count >= m_values.length) {
            throw new IllegalStateException("Heap full.");
        }

        m_values[m_count] = value;
        moveUp(m_count);
    }

    public Object extract() {
        if (m_count == 0) {
            throw new IllegalStateException("Heap empty.");
        }

        Object value = m_values[1];
        swap(1, m_count--);
        moveDown(1);

        return (value);
    }

    public void update(Object value) {
        //TODO: improve performance, dont use a linear search
        for (int i = 1; i < m_values.length; i++) {
            if (m_values[i] == value) {
                //check parent, if value is less, propogate value up
                if ((i > 1) && (compare(i, i / 2) < 0)) {
                    moveUp(i);

                    return;
                }

                //check children, if value more, propogate value down
                if ((((2 * i) <= size()) && (compare(i, (2 * i) + 1) > 0))
                        || ((((2 * i) + 1) <= size())
                        && (compare(i, (2 * i) + 1) > 0))) {
                    moveDown(i);
                }
            }
        }
    }

    public boolean isEmpty() {
        return (m_count == 0);
    }

    public int size() {
        return (m_count);
    }

    private void moveDown(int n) {
        int minchild = 0;

        if ((2 * n) < (m_count + 1)) {
            minchild = 2 * n;

            if (((2 * n) + 1) <= m_count) {
                minchild = (compare(2 * n, (2 * n) + 1) < 0) ? (2 * n)
                                                             : ((2 * n) + 1);
            }

            if (compare(minchild, n) < 0) {
                swap(minchild, n);
                moveDown(minchild);
            }
        }
    }

    private void moveUp(int n) {
        int parent = ((n % 2) == 0) ? (n / 2) : ((n - 1) / 2);

        if ((parent > 0) && (compare(n, parent) < 0)) {
            swap(n, parent);
            moveUp(parent);
        }
    }

    private int compare(int i, int j) {
        return (m_comparator.compare(m_values[i], m_values[j]));
    }

    private void swap(int i, int j) {
        Object tmp = m_values[i];
        m_values[i] = m_values[j];
        m_values[j] = tmp;
    }

    public void clear() {
        init(0);
    }

    public Object[] toArray() {
        return (m_values);
    }

    public boolean add(Object o) {
        insert(o);

        return (true);
    }

    public boolean contains(Object o) {
        for (int i = 0; i < m_values.length; i++) {
            if (m_values[i].equals(o)) {
                return (true);
            }
        }

        return (false);
    }

    public boolean remove(Object o) {
        throw new UnsupportedOperationException(
            "Heap#remove(Object) not supported");
    }

    public boolean addAll(Collection c) {
        for (Iterator itr = c.iterator(); itr.hasNext();) {
            add(itr.next());
        }

        return (true);
    }

    public boolean containsAll(Collection c) {
        for (Iterator itr = c.iterator(); itr.hasNext();) {
            if (!contains(itr.next())) {
                return (false);
            }
        }

        return (true);
    }

    public boolean removeAll(Collection c) {
        throw new UnsupportedOperationException(
            "Heap#removeAll(Collection) not supported");
    }

    public boolean retainAll(Collection c) {
        throw new UnsupportedOperationException(
            "Heap#retainAll(Collection) not supported");
    }

    public Iterator iterator() {
        return (new Iterator() {
                int i = 0;

                public void remove() {
                    throw new UnsupportedOperationException(
                        "Iterator#remove() not supported");
                }

                public boolean hasNext() {
                    return (i < m_values.length);
                }

                public Object next() {
                    return (m_values[i++]);
                }
            });
    }

    public Object[] toArray(Object[] a) {
        if (a.length < m_values.length) {
            a = (Object[]) Array.newInstance(a.getClass().getComponentType(),
                    m_values.length);
        }

        for (int i = 0; i < m_values.length; i++) {
            a[i] = m_values[i];
        }

        if (a.length > m_values.length) {
            a[m_values.length] = null;
        }

        return (a);
    }
}
