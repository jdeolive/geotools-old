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
package org.geotools.data.sde;

import junit.framework.*;


/**
 * geotools2 ArcSDE test suite
 *
 * @author Gabriel Roldán
 * @version $Id: SdeTestSuite.java,v 1.4 2003/11/14 17:21:05 groldan Exp $
 */
public class SdeTestSuite extends TestCase
{
    /**
     * Creates a new SdeTestSuite object.
     *
     * @param s suite's name
     */
    public SdeTestSuite(String s)
    {
        super(s);
    }

    /**
     * adds and returns all arcsde datasource relates tests
     *
     * @return test suite for sde datasource
     */
    public static Test suite()
    {
        TestSuite suite = new TestSuite();

        suite.addTestSuite(org.geotools.data.sde.GeometryBuilderTest.class);

        suite.addTestSuite(org.geotools.data.sde.SdeDataStoreTest.class);

        return suite;
    }
}
