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

import java.io.*;
import java.util.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.apache.log4j.Level;
import org.apache.log4j.Hierarchy;
import org.apache.log4j.Logger;
import org.apache.log4j.BasicConfigurator;
import junit.framework.*;
import com.vividsolutions.jts.geom.*;
import org.geotools.datasource.extents.*;
import org.geotools.feature.*;
import org.geotools.data.*;
import org.geotools.gml.GMLFilterGeometry;
import org.geotools.gml.GMLFilterDocument;


/**
 * Unit test for FilterCapabilities.
 *
 * @author Chris Holmes, TOPP
 */
public class CapabilitiesTest extends TestCase {
    
    /** Standard logging instance */
    private static Logger _log = Logger.getLogger("filter");
    
    /** Feature on which to preform tests */
    private Filter gFilter;
    private Filter compFilter;
    private Filter logFilter;

    FilterCapabilities capabilities;

    /** Test suite for this test case */
    TestSuite suite = null;
    
    
    /**
     * Constructor with test name.
     */
    String dataFolder = "";
    boolean setup = false;
    public CapabilitiesTest(String testName) {
        super(testName);
        _log.info("running CapabilitiesTests");
    }
    
    /**
     * Main for test runner.
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    /**
     * Required suite builder.
     * @return A test suite for this unit test.
     */
    public static Test suite() {
        BasicConfigurator.configure();
        //_log.getLoggerRepository().setThreshold(Level.DEBUG);
        
        TestSuite suite = new TestSuite(CapabilitiesTest.class);
        return suite;
    }
    
    
    /**
     * Sets up a schema and a test feature.
     * @throws SchemaException If there is a problem setting up the schema.
     * @throws IllegalFeatureException If problem setting up the feature.
     */
    protected void setUp() {
        _log.info("Setting up FilterCapabilitiesTest");
	if(setup) return;
        setup=true;
	capabilities = new FilterCapabilities();
	try {
	    gFilter = new GeometryFilter(AbstractFilter.GEOMETRY_WITHIN);
	    compFilter = new CompareFilter(AbstractFilter.COMPARE_LESS_THAN);
	} catch (IllegalFilterException e) {
	    _log.info("Bad filter " + e);
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
	    logFilter = compFilter.and(new BetweenFilter());
	    assertTrue(capabilities.fullySupports(logFilter));
	    logFilter = logFilter.or(new BetweenFilter());
	    assertTrue(capabilities.fullySupports(logFilter));
	    logFilter = logFilter.and(gFilter);
	    assertTrue(!(capabilities.fullySupports(logFilter)));
	} catch (IllegalFilterException e) {
	    _log.info("Bad filter " + e);
	}
    }
		   
    
}
