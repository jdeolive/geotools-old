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
package org.geotools.coverageio.gdal.erdasimg;

import it.geosolutions.imageio.plugins.erdasimg.ErdasImgImageReaderSpi;

import java.util.logging.Logger;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverageio.gdal.BaseGDALGridCoverage2DReader;
import org.geotools.data.DataSourceException;
import org.geotools.factory.Hints;
import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridCoverageReader;


/**
 * This class can read a ERDAS Imagine data source and create a {@link GridCoverage2D}
 * from the data.
 *
 * @author Daniele Romagnoli, GeoSolutions.
 * @author Simone Giannecchini (simboss), GeoSolutions
 * @since 2.5.x
 */
@SuppressWarnings("deprecation")
public final class ErdasImgReader extends BaseGDALGridCoverage2DReader implements GridCoverageReader {
    /** Logger. */
    private final static Logger LOGGER = org.geotools.util.logging.Logging.getLogger(
            "org.geotools.coverageio.gdal.erdasimg");
    private final static String worldFileExt = "";

    /**
     * Creates a new instance of a {@link ErdasImgReader}. I assume nothing about
     * file extension.
     *
     * @param input
     *            Source object for which we want to build an {@link ErdasImgReader}.
     * @throws DataSourceException
     */
    public ErdasImgReader(Object input) throws DataSourceException {
        this(input, null);
    }

    /**
     * Creates a new instance of a {@link ErdasImgReader}. I assume nothing about
     * file extension.
     *
     * @param input
     *            Source object for which we want to build an {@link ErdasImgReader}.
     * @param hints
     *            Hints to be used by this reader throughout his life.
     * @throws DataSourceException
     */
    public ErdasImgReader(Object input, Hints hints) throws DataSourceException {
        super(input, hints, worldFileExt, new ErdasImgImageReaderSpi());
    }

    /**
     * @see org.opengis.coverage.grid.GridCoverageReader#getFormat()
     */
    public Format getFormat() {
        return new ErdasImgFormat();
    }
}
