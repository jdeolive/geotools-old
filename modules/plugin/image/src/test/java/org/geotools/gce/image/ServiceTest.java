package org.geotools.gce.image;

import java.util.Iterator;

import junit.framework.TestCase;

import org.geotools.coverage.grid.io.GridFormatFactorySpi;
import org.geotools.coverage.grid.io.GridFormatFinder;

public class ServiceTest extends TestCase {

	public ServiceTest() {
		super();
		// TODO Auto-generated constructor stub
	}

	public ServiceTest(String arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	public static void main(java.lang.String[] args) {
		junit.textui.TestRunner.run(ServiceTest.class);
	}

	public void testIsAvailable() {
		Iterator list = GridFormatFinder.getAvailableFormats().iterator();
		boolean found = false;
		GridFormatFactorySpi fac;
		while (list.hasNext()) {
			fac = (GridFormatFactorySpi) list.next();

			if (fac instanceof WorldImageFormatFactory) {
				found = true;

				break;
			}
		}

		assertTrue("WorldImageFormatFactory not registered", found);
	}
}
