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
/*
 *    Geotools - OpenSource mapping toolkit
 *    (C) 2002, Centre for Computational Geography
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
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */
package org.geotools.filter;

import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollectionIteration;
import java.util.Iterator;


/**
 * DOCUMENT ME!
 *
 * @author Ian Schneider
 */
public class FilteringIteration extends FeatureCollectionIteration {
    /**
     * Creates a new instance of FilteringIteration
     *
     * @param filter DOCUMENT ME!
     * @param collection DOCUMENT ME!
     */
    public FilteringIteration(Filter filter, FeatureCollection collection) {
        super(new FilterHandler(filter), collection);
    }

    public static void filter(FeatureCollection features, Filter filter) {
        FilteringIteration i = new FilteringIteration(filter, features);
        i.iterate();
    }

    protected void iterate(Iterator iterator) {
        ((FilterHandler) handler).iterator = iterator;
        super.iterate(iterator);
    }

    static class FilterHandler implements Handler {
        Iterator iterator;
        final Filter filter;

        public FilterHandler(Filter filter) {
            this.filter = filter;
        }

        public void endFeature(org.geotools.feature.Feature f) {
        }

        public void endFeatureCollection(
            org.geotools.feature.FeatureCollection fc) {
        }

        public void handleAttribute(org.geotools.feature.AttributeType type,
            Object value) {
        }

        public void handleFeature(org.geotools.feature.Feature f) {
            if (!filter.contains(f)) {
                iterator.remove();
            }
        }

        public void handleFeatureCollection(
            org.geotools.feature.FeatureCollection fc) {
        }
    }
}
