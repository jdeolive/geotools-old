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

import java.util.Collections;
import java.util.Map;

import org.geotools.coverage.grid.io.GridFormatFactorySpi;
import org.opengis.coverage.grid.Format;

/**
 * Implementation of the GridCoverageFormat service provider interface for
 * mosaic of georeferenced images.
 * 
 * @author Simone Giannecchini (simboss)
 * @since 2.3
 */
public final class ImageMosaicFormatFactory implements GridFormatFactorySpi {
	/**
	 * Tells me if this plugin will work on not given the actual installation.
	 * 
	 * <p>
	 * Dependecies are mostly from JAI and ImageIO so if they are installed you
	 * should not have many problems.
	 * 
	 * @return False if something's missing, true otherwise.
	 */
	public boolean isAvailable() {
		boolean available = true;

		// if these classes are here, then the runtine environment has
		// access to JAI and the JAI ImageI/O toolbox.
		try {
			Class.forName("javax.media.jai.JAI");
			Class.forName("com.sun.media.jai.operator.ImageReadDescriptor");
		} catch (ClassNotFoundException cnf) {
			available = false;
		}

		return available;
	}

	/**
	 * @see GridFormatFactorySpi#createFormat().
	 */
	public Format createFormat() {
		return new ImageMosaicFormat();
	}

	/**
	 * Returns the implementation hints. The default implementation returns en
	 * empty map.
	 * 
	 * @return An empty map.
	 */
	public Map getImplementationHints() {
		return Collections.EMPTY_MAP;
	}
}
