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
package org.geotools.gce.imagepyramid;

import java.util.Collections;
import java.util.Map;

import org.geotools.coverage.grid.io.GridFormatFactorySpi;
import org.opengis.coverage.grid.Format;

/**
 * Implementation of the GridCoverageFormat service provider interface for arc
 * grid files.
 * 
 * @author Simone Giannecchini (simboss)
 * @since 2.3
 */
public final class ImagePyramidFormatFactory implements GridFormatFactorySpi {
	/**
	 * Tells us if this plugin is avaialble or not. Since usually coverage
	 * plugins depend on JAI and ImageIO classes this method is suitable for
	 * understanding if such a plugin is available or not, preventing users from
	 * having problems later on when trying to instantiate it.
	 * 
	 * @return False if this plugin is not availaible true otherwise.
	 */
	public boolean isAvailable() {
		boolean available = true;

		// it needs ImageMosaic and other things inside it
		try {
			Class.forName("org.geotools.gce.imagemosaic.ImageMosaicReader");
		} catch (ClassNotFoundException cnf) {
			available = false;
		}

		return available;
	}

	/**
	 * Creates a new {@link ImagePyramidFormat}.
	 * @return an OpenGIS {@link Format} subclass for this coverage.
	 */
	public Format createFormat() {
		return new ImagePyramidFormat();
	}

	/**
	 * Returns the implementation hints. The default implementation returns en
	 * empty map.
	 * 
	 * @return an empty map.
	 */
	public Map getImplementationHints() {
		return Collections.EMPTY_MAP;
	}
}
