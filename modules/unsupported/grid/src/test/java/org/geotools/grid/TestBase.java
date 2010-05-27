/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2010, Open Source Geospatial Foundation (OSGeo)
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

package org.geotools.grid;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

import static org.junit.Assert.*;

/**
 * Base class for vector grid unit tests.
 *
 * @author mbedward
 * @since 2.7
 * @source $URL$
 * @version $Id$
 */
public class TestBase {

    protected static final double TOL = 1.0E-8d;

    protected void assertEnvelope(Envelope expected, Envelope actual) {
        assertEquals((expected == null), (actual == null));
        if (expected != null) {
            assertEquals(expected.getMinX(), actual.getMinX(), TOL);
            assertEquals(expected.getMinY(), actual.getMinY(), TOL);
            assertEquals(expected.getMaxX(), actual.getMaxX(), TOL);
            assertEquals(expected.getMaxY(), actual.getMaxY(), TOL);
        }
    }

    protected void assertCoordinate(Coordinate expected, Coordinate actual) {
        assertEquals((expected == null), (actual == null));
        if (expected != null) {
            assertEquals(expected.x, actual.x, TOL);
            assertEquals(expected.y, actual.y, TOL);
        }
    }

}
