/**
 * 
 */
package org.geotools.gce.imagemosaic;


import java.io.IOException;
import java.util.Arrays;
import java.util.Set;

import org.geotools.TestData;
import org.geotools.gce.imagemosaic.properties.PropertiesCollector;
import org.geotools.gce.imagemosaic.properties.PropertiesCollectorFinder;
import org.geotools.gce.imagemosaic.properties.PropertiesCollectorSPI;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Simone Giannecchini, GeoSolutions SAS
 *
 */
public class PropertiesCollectorTest extends Assert {

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testTime() throws IOException{
		
		// get the spi
		final Set<PropertiesCollectorSPI> spis = PropertiesCollectorFinder.getPropertiesCollectorSPI();
		assertNotNull(spis);
		assertTrue(!spis.isEmpty());
		
		// test a regex
		final PropertiesCollectorSPI spi = spis.iterator().next();
		final PropertiesCollector pc = spi.create(TestData.url(this,"time_geotiff/regex.properties"), Arrays.asList("time_attr"));
		pc.collect(TestData.file(this,"time_geotiff/world.200401.3x5400x2700.tiff"));
	}

}
