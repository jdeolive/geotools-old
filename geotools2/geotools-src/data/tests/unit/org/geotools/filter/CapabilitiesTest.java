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
package org.geotools.filter;

import com.vividsolutions.jts.geom.*;
import junit.framework.*;
import org.geotools.data.*;
import org.geotools.feature.*;

import org.w3c.dom.*;
import java.io.*;
import java.util.*;
import java.util.logging.Logger;
import javax.xml.parsers.*;


/**
 * Unit test for FilterCapabilities.
 *
 * @author Chris Holmes, TOPP
 */
public class CapabilitiesTest extends TestCase {
    /** Standard logging instance */
    private static final Logger LOGGER = Logger.getLogger(
            "org.geotools.defaultcore");

    /** Feature on which to preform tests */
    private Filter gFilter;
    private Filter compFilter;
    private Filter logFilter;
    private FilterCapabilities capabilities;
    private FilterFactory fact = FilterFactory.createFilterFactory();

    /** Test suite for this test case */
    TestSuite suite = null;

    /** Constructor with test name. */
    String dataFolder = "";
    boolean setup = false;

    public CapabilitiesTest(String testName) {
        super(testName);
    }

    /**
     * Main for test runner.
     *
     * @param args DOCUMENT ME!
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    /**
     * Required suite builder.
     *
     * @return A test suite for this unit test.
     */
    public static Test suite() {
        //_log.getLoggerRepository().setThreshold(Level.DEBUG);
        TestSuite suite = new TestSuite(CapabilitiesTest.class);

        return suite;
    }

    /**
     * Sets up a schema and a test feature.
     */
    protected void setUp() {
        LOGGER.finer("Setting up FilterCapabilitiesTest");

        if (setup) {
            return;
        }

        setup = true;
        capabilities = new FilterCapabilities();

        try {
            gFilter = fact.createGeometryFilter(AbstractFilter.GEOMETRY_WITHIN);
            compFilter = fact.createCompareFilter(AbstractFilter.COMPARE_LESS_THAN);
        } catch (IllegalFilterException ife) {
            LOGGER.fine("Bad filter " + ife);
        }

        capabilities.addType(AbstractFilter.LOGIC_OR);
        capabilities.addType(AbstractFilter.LOGIC_AND);
        capabilities.addType(AbstractFilter.LOGIC_NOT);
        capabilities.addType(AbstractFilter.COMPARE_EQUALS);
        capabilities.addType(AbstractFilter.COMPARE_LESS_THAN);
        capabilities.addType(AbstractFilter.BETWEEN);
    }

    public void testAdd() {
        capabilities.addType(AbstractFilter.COMPARE_GREATER_THAN);
        capabilities.addType(AbstractFilter.COMPARE_LESS_THAN_EQUAL);
        capabilities.addType(AbstractFilter.NULL);
        assertTrue(capabilities.supports(AbstractFilter.NULL));
    }

    public void testShortSupports() {
        assertTrue(capabilities.supports(AbstractFilter.LOGIC_AND));
        assertTrue(!(capabilities.supports(AbstractFilter.LIKE)));
    }

    public void testFilterSupports() {
        assertTrue(capabilities.supports(compFilter));
        assertTrue(!(capabilities.supports(gFilter)));
    }

    public void testFullySupports() {
        try {
            logFilter = gFilter.and(compFilter);
            assertTrue(capabilities.fullySupports(compFilter));
            assertTrue(!(capabilities.fullySupports(gFilter)));
            assertTrue(!(capabilities.fullySupports(logFilter)));
            logFilter = compFilter.and(fact.createBetweenFilter());
            assertTrue(capabilities.fullySupports(logFilter));
            logFilter = logFilter.or(fact.createBetweenFilter());
            assertTrue(capabilities.fullySupports(logFilter));
            logFilter = logFilter.and(gFilter);
            assertTrue(!(capabilities.fullySupports(logFilter)));
        } catch (IllegalFilterException e) {
            LOGGER.fine("Bad filter " + e);
        }
    }
}
