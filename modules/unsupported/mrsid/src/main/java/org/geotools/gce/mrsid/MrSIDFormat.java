/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2007, Geotools Project Managment Committee (PMC)
 *	  (C) 2007, GeoSolutions S.A.S.
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

import it.geosolutions.imageio.plugins.mrsid.MrSIDImageReaderSpi;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.imageio.GeoToolsWriteParams;
import org.geotools.data.DataSourceException;
import org.geotools.factory.Hints;
import org.geotools.parameter.DefaultParameterDescriptorGroup;
import org.geotools.parameter.ParameterGroup;
import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridCoverageReader;
import org.opengis.coverage.grid.GridCoverageWriter;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.parameter.GeneralParameterDescriptor;

/**
 * An implementation a {@link Format} for the MrSID format.
 * 
 * @author Daniele Romagnoli, GeoSolutions
 * @author Simone Giannecchini (simboss), GeoSolutions
 */
public final class MrSIDFormat extends AbstractGridFormat implements Format {
	private final static Logger LOGGER = org.geotools.util.logging.Logging
			.getLogger("org.geotools.gce.mrsid");

	/** Caching the {@link MrSIDImageReaderSpi} factory. */
	private final MrSIDImageReaderSpi spi = new MrSIDImageReaderSpi();

	/**
	 * Creates an instance and sets the metadata.
	 */
	public MrSIDFormat() {
		if (LOGGER.isLoggable(Level.FINE))
			LOGGER.fine("Creating a new MrSIDFormat.");
		setInfo();
	}

	/**
	 * Sets the metadata information.
	 */
	private void setInfo() {
		final HashMap<String, String> info = new HashMap<String, String>();
		info.put("name", "MrSID");
		info.put("description", "MrSID Coverage Format");
		info.put("vendor", "Geotools");
		info.put("docURL", "");// TODO: set something
		info.put("version", "1.0");
		mInfo = Collections.unmodifiableMap(info);

		// writing parameters
		writeParameters = null;

		// reading parameters
		readParameters = new ParameterGroup(
				new DefaultParameterDescriptorGroup(mInfo,
						new GeneralParameterDescriptor[] { READ_GRIDGEOMETRY2D,
								USE_JAI_IMAGEREAD }));
	}

	/**
	 * @see org.geotools.data.coverage.grid.AbstractGridFormat#getReader(Object
	 *      source)
	 */
	public GridCoverageReader getReader(Object source) {
		return getReader(source, null);
	}

	/**
	 * @see org.geotools.data.coverage.grid.AbstractGridFormat#createWriter(java.lang.Object
	 *      destination)
	 */
	public GridCoverageWriter getWriter(Object destination) {
		throw new UnsupportedOperationException(
				"This plugin does not support writing at this time.");
	}

	/**
	 * @see org.geotools.data.coverage.grid.AbstractGridFormat#createWriter(java.lang.Object
	 *      destination,Hints hints)
	 */
	public GridCoverageWriter getWriter(Object destination, Hints hints) {
		throw new UnsupportedOperationException(
				"This plugin does not support writing at this time.");
	}

	/**
	 * @see org.geotools.data.coverage.grid.AbstractGridFormat#accepts(Object
	 *      input)
	 */
	public boolean accepts(Object input) {
		try {
			return spi.canDecodeInput(input);
		} catch (IOException e) {
			if (LOGGER.isLoggable(Level.FINE))
				LOGGER.log(Level.FINE, e.getLocalizedMessage(), e);
			return false;
		}
	}

	/**
	 * @see org.geotools.data.coverage.grid.AbstractGridFormat#getReader(Object,
	 *      Hints)
	 */
	public GridCoverageReader getReader(Object source, Hints hints) {
		try {
			return new MrSIDReader(source, hints);
		} catch (MismatchedDimensionException e) {
			final RuntimeException re = new RuntimeException();
			re.initCause(e);
			throw re;
		} catch (DataSourceException e) {
			final RuntimeException re = new RuntimeException();
			re.initCause(e);
			throw re;
		}
	}

	public GeoToolsWriteParams getDefaultImageIOWriteParameters() {
		throw new UnsupportedOperationException(
				"This plugin does not support writing at this time.");
	}
}
