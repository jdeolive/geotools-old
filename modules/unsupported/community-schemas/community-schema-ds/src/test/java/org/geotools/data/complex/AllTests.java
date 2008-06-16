/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2007-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.data.complex;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.geotools.data.complex.config.EmfAppSchemaReaderTest;
import org.geotools.data.complex.config.XMLConfigReaderTest;
import org.geotools.data.complex.filter.UnmappingFilterVisitorTest;
import org.geotools.data.feature.memory.MemoryDataAccessTest;
import org.geotools.filter.IDFunctionExpressionTest;

/**
 * 
 * @author Gabriel Roldan, Axios Engineering
 * @version $Id$
 * @source $URL$
 * @since 2.4
 */
public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for org.geotools.data.complex");
		//$JUnit-BEGIN$
		suite.addTestSuite(UnmappingFilterVisitorTest.class);
		suite.addTestSuite(ComplexDataStoreFactoryTest.class);
		suite.addTestSuite(ComplexDataStoreTest.class);
		suite.addTestSuite(MemoryDataAccessTest.class);
		suite.addTestSuite(IDFunctionExpressionTest.class);
		suite.addTestSuite(EmfAppSchemaReaderTest.class);
		suite.addTestSuite(XMLConfigReaderTest.class);
		suite.addTestSuite(BoreholeTest.class);
		
		//$JUnit-END$
		return suite;
	}

}
