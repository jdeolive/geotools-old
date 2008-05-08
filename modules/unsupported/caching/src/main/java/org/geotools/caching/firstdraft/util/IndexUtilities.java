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
package org.geotools.caching.firstdraft.util;

import com.vividsolutions.jts.geom.Envelope;
import org.geotools.caching.firstdraft.spatialindex.spatialindex.Region;


public class IndexUtilities {
    /** Transform a JTS Envelope to a Region
     *
     * @param JTS Envelope
     * @return Region
     */
    public static Region toRegion(final Envelope e) {
        Region r = new Region(new double[] { e.getMinX(), e.getMinY() },
                new double[] { e.getMaxX(), e.getMaxY() });

        return r;
    }
}
