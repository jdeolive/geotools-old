/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2006, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.geotools.gce.imagemosaic;

import java.util.Iterator;

import junit.framework.TestCase;
import junit.textui.TestRunner;

import org.geotools.coverage.grid.io.GridFormatFactorySpi;
import org.geotools.coverage.grid.io.GridFormatFinder;

/**
 * @author Simone Giannecchini
 * 
 */
public class ImageMosaicServiceTest extends TestCase {

	/**
	 * 
	 */
	public ImageMosaicServiceTest() {

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		TestRunner.run(ImageMosaicServiceTest.class);

	}

	public void testIsAvailable() {
		Iterator list = GridFormatFinder.getAvailableFormats().iterator();
		boolean found = false;

		while (list.hasNext()) {
			final GridFormatFactorySpi fac = (GridFormatFactorySpi) list.next();

			if (fac instanceof ImageMosaicFormatFactory) {
				found = true;

				break;
			}
		}

		assertTrue("ImageMosaicFormatFactorySpi not registered", found);
	}
}
