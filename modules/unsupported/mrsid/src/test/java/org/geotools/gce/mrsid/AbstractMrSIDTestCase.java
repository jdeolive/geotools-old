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
package org.geotools.gce.mrsid;

import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.media.jai.JAI;

import junit.framework.TestCase;

/**
 * @author Daniele Romagnoli, GeoSolutions
 * @author Simone Giannecchini (simboss), GeoSolutions
 * 
 * Base testing class initializing JAI properties to be used during tests.
 */
public abstract class AbstractMrSIDTestCase extends TestCase {

	protected final static Logger LOGGER = org.geotools.util.logging.Logging.getLogger("org.geotools.gce.mrsid");

	public AbstractMrSIDTestCase(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();

		ImageIO.setUseCache(false);
		JAI.getDefaultInstance().getTileCache().setMemoryCapacity(
				10 * 1024 * 1024);
		JAI.getDefaultInstance().getTileCache().setMemoryThreshold(1.0f);

		JAI.getDefaultInstance().getTileScheduler().setParallelism(50);
		JAI.getDefaultInstance().getTileScheduler().setPrefetchParallelism(50);
		JAI.getDefaultInstance().getTileScheduler().setPrefetchPriority(5);
		JAI.getDefaultInstance().getTileScheduler().setPriority(5);
	}
	

	
}
