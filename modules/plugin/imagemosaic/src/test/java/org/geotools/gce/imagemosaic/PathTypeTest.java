/**
 * 
 */
package org.geotools.gce.imagemosaic;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.geotools.test.TestData;
import org.junit.Assert;
import org.junit.Test;

/**
 * Testing {@link PathType} class.
 * 
 * @author Simone Giannecchini, GeoSolutions SAS
 *
 */
public class PathTypeTest extends Assert {

	
	public PathTypeTest() {
	}
	
	@Test
	public void relative() throws FileNotFoundException, IOException{
		
		//get some test data
		final File testFile= TestData.file(this, "/rgb/global_mosaic_0.pgw");
		assertTrue(testFile.exists());
		
		// test it as a relative path to the test-data directory
		final File temp=PathType.RELATIVE.resolvePath(TestData.file(this, ".").getAbsolutePath(), "rgb/global_mosaic_0.pgw");
		assertNotNull(temp);
		assertTrue(temp.exists());
		
		// test error checks
		final File temp1=PathType.RELATIVE.resolvePath(TestData.file(this, ".").getAbsolutePath(), "rgb/global_mosaic_0.pg");
		assertNull(temp1);
		
	}
	
	@Test
	public void absolute() throws FileNotFoundException, IOException{
		//get some test data
		final File testFile= TestData.file(this, "/rgb/global_mosaic_0.pgw");
		assertTrue(testFile.exists());
		
		// test it as a relative path to the test-data directory
		final File temp=PathType.ABSOLUTE.resolvePath(TestData.file(this, ".").getAbsolutePath(), testFile.getAbsolutePath());
		assertNotNull(temp);
		assertTrue(temp.exists());
		final File temp1=PathType.ABSOLUTE.resolvePath(null, testFile.getAbsolutePath());
		assertNotNull(temp1);
		assertTrue(temp1.exists());		
		
		// test error checks using relative call
		final File temp2=PathType.ABSOLUTE.resolvePath(TestData.file(this, ".").getAbsolutePath(), "rgb/global_mosaic_0.pg");
		assertNull(temp2);
		
	}

}
