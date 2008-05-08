/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, GeoTools Project Managment Committee (PMC)
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
 */
package org.geotools.data.collection;

import java.util.Collection;
import java.util.Iterator;


/**
 * Collection supporting close( Iterator ), used to grok resources.
 * <p>
 * This implementation is a port of java.util.Collection with support for
 * the use of close( Iterator ). This will allow subclasses that make use of
 * resources during iterator() to be uses safely.
 * </p>
 * @author Jody Garnett, Refractions Research, Inc.
 * @since GeoTools 2.2
 * @source $URL$
 */
public interface ResourceCollection<R> extends Collection<R> {
    /**
     * An iterator over this collection, which must be closeed after use.
     * <p>
     * Collection is not guarneteed to be ordered in any manner.
     * </p>
     * <p>
     * The implementation of Collection must adhere to the rules of
     * fail-fast concurrent modification. In addition (to allow for
     * resource backed collections, the <code>close( Iterator )</code>
     * method must be called.
     * <p>
     * </p>
     * Example (safe) use:<pre><code>
     * Iterator iterator = collection.iterator();
     * try {
     *     while( iterator.hasNext();){
     *          Feature feature = (Feature) iterator.hasNext();
     *          System.out.println( feature.getID() );
     *     }
     * }
     * finally {
     *     collection.close( iterator );
     * }
     * </code></pre>
     * </p>
     * @return Iterator
     */
    public Iterator<R> iterator();

    /**
     * Clean up after any resources assocaited with this itterator in a manner similar to JDO collections.
     * </p>
     * Example (safe) use:<pre><code>
     * Iterator iterator = collection.iterator();
     * try {
     *     for( Iterator i=collection.iterator(); i.hasNext();){
     *          Feature feature = (Feature) i.hasNext();
     *          System.out.println( feature.getID() );
     *     }
     * }
     * finally {
     *     collection.close( iterator );
     * }
     * </code></pre>
     * </p>
     * @param close
     */
    public void close(Iterator<R> close);

    /**
     * Close any outstanding resources released by this resources.
     * <p>
     * This method should be used with great caution, it is however available
     * to allow the use of the ResourceCollection with algorthims that are
     * unaware of the need to close iterators after use.
     * </p>
     * <p>
     * Example of using a normal Collections utility method:<pre><code>
     * Collections.sort( collection );
     * collection.purge();
     * </code></pre>
     */
    public void purge();
}
