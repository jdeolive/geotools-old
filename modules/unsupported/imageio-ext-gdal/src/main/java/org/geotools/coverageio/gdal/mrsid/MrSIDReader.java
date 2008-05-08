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
package org.geotools.coverageio.gdal.mrsid;

import it.geosolutions.imageio.gdalframework.GDALCommonIIOImageMetadata;
import it.geosolutions.imageio.plugins.mrsid.MrSIDIIOImageMetadata;
import it.geosolutions.imageio.plugins.mrsid.MrSIDImageReaderSpi;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.metadata.IIOMetadata;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverageio.gdal.BaseGDALGridCoverage2DReader;
import org.geotools.data.DataSourceException;
import org.geotools.factory.Hints;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridCoverageReader;
import org.opengis.referencing.FactoryException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;


/**
 * This class can read a MrSID data source and create a {@link GridCoverage2D}
 * from the data.
 *
 * @author Daniele Romagnoli, GeoSolutions
 * @author Simone Giannecchini (simboss), GeoSolutions
 * @since 2.5.x
 */
public final class MrSIDReader extends BaseGDALGridCoverage2DReader implements GridCoverageReader {
    /** Logger. */
    private final static Logger LOGGER = org.geotools.util.logging.Logging.getLogger(
            "org.geotools.coverageio.gdal.mrsid");
    private final static String worldFileExt = ".sdw";

    /**
     * Creates a new instance of a {@link MrSIDReader}. I assume nothing about
     * file extension.
     *
     * @param input
     *            Source object for which we want to build a {@link MrSIDReader}.
     * @throws DataSourceException
     */
    public MrSIDReader(Object input) throws DataSourceException {
        this(input, null);
    }

    /**
     * Creates a new instance of a {@link MrSIDReader}. I assume nothing about
     * file extension.
     *
     * @param input
     *            Source object for which we want to build a {@link MrSIDReader}.
     * @param hints
     *            Hints to be used by this reader throughout his life.
     * @throws DataSourceException
     */
    public MrSIDReader(Object input, final Hints hints)
        throws DataSourceException {
        super(input, hints, true, worldFileExt, new MrSIDImageReaderSpi());
    }

    /**
     * This method is responsible for building up an envelope according to the
     * definition of the crs. It assumes that X coordinate on the grid itself
     * maps to longitude and y coordinate maps to latitude.
     *
     * @param gridMetadata
     *            The {@link MrSIDIIOImageMetadata} to parse.
     */
    protected void getPropertiesFromSpecificMetadata(IIOMetadata metadata) {
        final GDALCommonIIOImageMetadata gridMetadata = (GDALCommonIIOImageMetadata) metadata;

        // getting metadata
        final Node root = gridMetadata.getAsTree(MrSIDIIOImageMetadata.mrsidImageMetadataName);

        Node child = root.getFirstChild();

        // getting GeoReferencing Properties
        child = child.getNextSibling();

        final NamedNodeMap attributes = child.getAttributes();

        if ( baseEnvelope== null) {
            final String xResolution = attributes.getNamedItem("IMAGE__X_RESOLUTION").getNodeValue();
            final String yResolution = attributes.getNamedItem("IMAGE__Y_RESOLUTION").getNodeValue();
            final String xyOrigin = attributes.getNamedItem("IMAGE__XY_ORIGIN").getNodeValue();

            if ((xResolution != null) && (yResolution != null) && (xyOrigin != null)
                    && !(xResolution.trim().equalsIgnoreCase(""))
                    && !(yResolution.trim().equalsIgnoreCase(""))
                    && !(xyOrigin.trim().equalsIgnoreCase(""))) {
                double cellsizeX = Double.parseDouble(xResolution);
                double cellsizeY = Double.parseDouble(yResolution);
                final String[] origins = xyOrigin.split(",");
                double xul = Double.parseDouble(origins[0]);
                double yul = Double.parseDouble(origins[1]);

                xul -= (cellsizeX / 2d);
                yul -= (cellsizeY / 2d);

                final double xll = xul;
                final double yur = yul;
                final int width = baseGridRange.getLength(0);
                final int height = baseGridRange.getLength(1);
                final double xur = xul + (cellsizeX * width);
                final double yll = yul - (cellsizeY * height);
                baseEnvelope = new GeneralEnvelope(new double[] { xll, yll },
                        new double[] { xur, yur });
            }
        }

        // Retrieving projection Information
        if (coverageCRS == null) {
            Node attribute = attributes.getNamedItem("IMG__WKT");

            if (attribute != null) {
                String wkt = attribute.getNodeValue();

                if ((wkt != null) && (wkt.trim().length() > 0)) {
                    try {
                    	coverageCRS = CRS.parseWKT(wkt);
                    } catch (FactoryException fe) {
                        if (LOGGER.isLoggable(Level.FINE)) {
                            LOGGER.log(Level.FINE,
                                "Unable to get CRS from" + " WKT contained in metadata."
                                + " Looking for a PRJ.");
                        }

                        // unable to get CRS from WKT
                        coverageCRS = null;
                    }
                }
            }
        }
    }

    /**
     * @see org.opengis.coverage.grid.GridCoverageReader#getFormat()
     */
    public Format getFormat() {
        return new MrSIDFormat();
    }
}
