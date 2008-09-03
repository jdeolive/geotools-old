/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2005-2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.data.feature;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.opengis.feature.Feature;
import org.opengis.filter.Filter;

/**
 * Decorates a {@link org.geotools.feature.Feature} iterator with one that
 * filters content.
 * 
 * <p>
 * This class is a dumb copy of
 * {@link org.geotools.data.store.FilteringIterator} by Justin Deoliveira, but
 * meant to iterate over any kind of <code>Object</code> as it is possible to
 * {@link Filter}, though the target usage is to iterate over ISO
 * {@link Feature}s.
 * </p>
 * 
 * @author Gabriel Roldan, Axios Engineering
 * @version $Id$
 * @source $URL$
 * @since 2.4
 */
public class FilteringIterator implements Iterator {

    /**
     * Delegate iterator
     */
    Iterator delegate;

    /**
     * The Filter
     */
    Filter filter;

    /**
     * Next feature
     */
    Object next;

    public FilteringIterator(Iterator delegate, Filter filter) {
        this.delegate = delegate;
        this.filter = filter;
    }

    public Iterator getDelegate() {
        return delegate;
    }

    public void remove() {
        delegate.remove();
    }

    public boolean hasNext() {
        if (next != null) {
            return true;
        }

        while (delegate.hasNext()) {
            Object peek = delegate.next();
            if (filter.evaluate(peek)) {
                next = peek;
                break;
            }
        }

        return next != null;
    }

    public Object next() {
        if (!hasNext()) {
            throw new NoSuchElementException("There are no more elements");
        }
        Object f = next;
        next = null;
        return f;
    }

}
