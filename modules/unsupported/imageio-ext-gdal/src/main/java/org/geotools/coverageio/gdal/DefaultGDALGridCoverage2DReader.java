/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2007, GeoTools Project Management Committee (PMC)
 *    (C) 2007, GeoSolutions S.A.S.
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
package org.geotools.coverageio.gdal;

import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageReaderSpi;
import org.opengis.coverage.grid.Format;
import org.geotools.coverage.grid.io.UnknownFormat;
import org.geotools.data.DataSourceException;
import org.geotools.factory.Hints;

/**
 * The default implementation of {@link BaseGDALGridCoverage2DReader}.
 *
 * @author Daniele Romagnoli, GeoSolutions
 * @author Simone Giannecchini, GeoSolutions
 *
 */
public class DefaultGDALGridCoverage2DReader extends BaseGDALGridCoverage2DReader {
    
	/** 
	 * {@code DefaultGDALGridCoverage2DReader} constructor.
	 * 
	 * @see {@link BaseGDALGridCoverage2DReader}
	 */
	protected DefaultGDALGridCoverage2DReader(Object input, final Hints hints,
        boolean isSupportingAdditionalMetadata, final String worldFileExtension,
        final ImageReaderSpi formatSpecificSpi) throws DataSourceException {
        super(input, hints, isSupportingAdditionalMetadata, worldFileExtension, formatSpecificSpi);
    }

    /**
     * Get additional properties from format specific metadata object. The default implementation
     * does nothing.
     */
    protected void getPropertiesFromSpecificMetadata(IIOMetadata metadata) {
        //The default Implementation does nothing
    }

    /**
     * The default implementation return {@code null} since no formats may be directly associated 
     * to this reader.
     */
	public Format getFormat() {
		return new UnknownFormat();
	}
}
