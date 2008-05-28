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
package org.geotools.gce.imagemosaic.jdbc;

import com.vividsolutions.jts.geom.Envelope;

import org.geotools.coverage.CoverageFactoryFinder;
import org.geotools.coverage.grid.GeneralGridRange;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.processing.Operations;

import org.geotools.data.DataSourceException;

import org.geotools.factory.Hints;

import org.geotools.geometry.GeneralEnvelope;

import org.geotools.parameter.Parameter;

import org.geotools.referencing.CRS;
import org.geotools.referencing.operation.BufferedCoordinateOperationFactory;

import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridCoverage;

import org.opengis.parameter.GeneralParameterValue;

import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.CoordinateOperationFactory;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import java.net.MalformedURLException;
import java.net.URL;

import java.util.Date;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageReadParam;

import javax.media.jai.RenderedImageAdapter;
import javax.media.jai.operator.MosaicDescriptor;


/**
 * This reader is repsonsible for providing access to images and image pyramids 
 * stored in a JDBC datbase as tiles. 
 *
 * All jdbc databases which are able to handle blobs are supported.
 * 
 * Additonally, spatial extensions for mysql,postgis,db2 and oracle are supported
 *
 *
 * @author mcr
 * @since 2.5
 *
 */
public class ImageMosaicJDBCReader extends AbstractGridCoverage2DReader {
    private final static Logger LOGGER = Logger.getLogger(ImageMosaicJDBCReader.class.getPackage()
                                                                                     .getName());
    protected final static CoordinateOperationFactory operationFactory = new BufferedCoordinateOperationFactory(new Hints(
                Hints.LENIENT_DATUM_SHIFT, Boolean.TRUE));
    public final static String QUEUE_END = "QueueEnd";
    private final JDBCAccess jdbcAccess;
    private final LinkedBlockingQueue<Object> tileQueue;
    private GeneralEnvelope requestedEnvelope = null;
    private GeneralEnvelope requestEnvelopeTransformed = null;
    private Config config;

    /**
     * COnstructor.
     *
     * @param source
     *            The source object.
     * @throws IOException
     * @throws UnsupportedEncodingException
     *
     */
    public ImageMosaicJDBCReader(Object source, Hints uHints)
        throws IOException, MalformedURLException {
        this.source = source;

        URL url = ImageMosaicJDBCFormat.getURLFromSource(source);

        if (url == null) {
            throw new MalformedURLException(source.toString());
        }

        try {
            config = Config.readFrom(url);
        } catch (Exception e) {
            LOGGER.severe(e.getMessage());
            throw new IOException(e.getMessage());
        }

        // /////////////////////////////////////////////////////////////////////
        // 
        // Forcing longitude first since the geotiff specification seems to
        // assume that we have first longitude the latitude.
        //
        // /////////////////////////////////////////////////////////////////////
        if (uHints != null) {
            // prevent the use from reordering axes
            this.hints.add(uHints);
        }

        coverageName = config.getCoverageName();
        this.coverageFactory = CoverageFactoryFinder.getGridCoverageFactory(this.hints);

        // /////////////////////////////////////////////////////////////////////
        //
        // Load tiles informations, especially the bounds, which will be
        // reused
        //
        // /////////////////////////////////////////////////////////////////////
        try {
            jdbcAccess = JDBCAccessFactory.getJDBCAcess(config);
        } catch (Exception e1) {
            LOGGER.severe(e1.getLocalizedMessage());
            throw new IOException(e1.getLocalizedMessage());
        }

        tileQueue = new LinkedBlockingQueue<Object>();

        // get the crs if able to
        final Object tempCRS = this.hints.get(Hints.DEFAULT_COORDINATE_REFERENCE_SYSTEM);

        if (tempCRS != null) {
            this.crs = (CoordinateReferenceSystem) tempCRS;
            LOGGER.log(Level.WARNING,
                new StringBuffer("Using forced coordinate reference system ").append(
                    crs.toWKT()).toString());
        } else if (config.getCoordsys() != null) {
            String srsString = config.getCoordsys();

            try {
                crs = CRS.decode(srsString);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Could not find " + srsString, e);

                return;
            }
        } else {
            CoordinateReferenceSystem tempcrs = jdbcAccess.getLevelInfo(0)
                                                          .getCrs();

            if (tempcrs == null) {
                crs = AbstractGridFormat.getDefaultCRS();
                LOGGER.log(Level.WARNING,
                    new StringBuffer(
                        "Unable to find a CRS for this coverage, using a default one: ").append(
                        crs.toWKT()).toString());
            } else {
                crs = tempcrs;
            }
        }

        if (jdbcAccess.getNumOverviews() == -1) {
            String msg = "No levels found fond for coverage: " +
                config.getCoverageName();
            LOGGER.severe(msg);
            throw new IOException(msg);
        }

        Envelope env = jdbcAccess.getLevelInfo(0).getEnvelope();

        if (env == null) {
            String msg = "Coverage: " + config.getCoverageName() +
                " is not caluclated";
            LOGGER.severe(msg);
            throw new IOException(msg);
        }

        this.originalEnvelope = new GeneralEnvelope(new Rectangle2D.Double(
                    env.getMinX(), env.getMinY(),
                    env.getMaxX() - env.getMinX(), env.getMaxY() -
                    env.getMinY()));
        this.originalEnvelope.setCoordinateReferenceSystem(crs);

        highestRes = jdbcAccess.getLevelInfo(0).getResolution();
        numOverviews = jdbcAccess.getNumOverviews();
        overViewResolutions = new double[numOverviews][];

        for (int i = 0; i < numOverviews; i++)
            overViewResolutions[i] = jdbcAccess.getLevelInfo(i + 1)
                                               .getResolution();


        originalGridRange = new GeneralGridRange(new Rectangle(
                    (int) Math.round(
                        originalEnvelope.getLength(0) / highestRes[0]),
                    (int) Math.round(
                        originalEnvelope.getLength(1) / highestRes[1])));
    }

    /**
     * Constructor.
     *
     * @param source
     *            The source object.
     * @throws IOException
     * @throws UnsupportedEncodingException
     *
     */
    public ImageMosaicJDBCReader(Object source) throws IOException {
        this(source, null);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.opengis.coverage.grid.GridCoverageReader#getFormat()
     */
    public Format getFormat() {
        return new ImageMosaicJDBCFormat();
    }

    private void logRequestParams(GeneralParameterValue[] params) {
        LOGGER.info("----PARAMS START-------");

        for (int i = 0; i < params.length; i++) {
            Parameter<Object> p = (Parameter<Object>) params[i];
            LOGGER.info(p.getDescriptor().getName().toString() + ": "+ p.getValue());
        }

        LOGGER.info("----PARAMS END-------");
    }

    /*
     * (non-Javadoc)
     *
     * @see org.opengis.coverage.grid.GridCoverageReader#read(org.opengis.parameter.GeneralParameterValue[])
     */
    public GridCoverage read(GeneralParameterValue[] params)
        throws IOException {
        logRequestParams(params);

        Date start = new Date();

        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("Reading mosaic from " + coverageName);
            LOGGER.fine(new StringBuffer("Highest res ").append(highestRes[0])
                                                        .append(" ")
                                                        .append(highestRes[1])
                                                        .toString());
        }

        // /////////////////////////////////////////////////////////////////////
        //
        // Checking params
        //
        // /////////////////////////////////////////////////////////////////////
        Color outputTransparentColor = (Color) ImageMosaicJDBCFormat.OUTPUT_TRANSPARENT_COLOR.getDefaultValue();

        Rectangle dim = null;

        if (params != null) {
            for (GeneralParameterValue generalParameterValue : params) {
                Parameter<Object> param = (Parameter<Object>) generalParameterValue;

                if (param.getDescriptor().getName().getCode()
                             .equals(AbstractGridFormat.READ_GRIDGEOMETRY2D.getName()
                                                                               .toString())) {
                    final GridGeometry2D gg = (GridGeometry2D) param.getValue();
                    requestedEnvelope = (GeneralEnvelope) gg.getEnvelope();
                    dim = gg.getGridRange2D().getBounds();
                } else if (param.getDescriptor().getName().getCode()
                                    .equals(ImageMosaicJDBCFormat.OUTPUT_TRANSPARENT_COLOR.getName()
                                                                                              .toString())) {
                    outputTransparentColor = (Color) param.getValue();
                }
            }
        }

        // /////////////////////////////////////////////////////////////////////
        //
        // Loading tiles trying to optimize as much as possible
        //
        // /////////////////////////////////////////////////////////////////////
        GridCoverage coverage = loadTiles(outputTransparentColor, dim);
        LOGGER.info("Mosaic Reader needs : " +
            ((new Date()).getTime() - start.getTime()) + " millisecs");

        return coverage;
    }

    private void transformRequestEnvelope() throws DataSourceException {
        if (CRS.equalsIgnoreMetadata(
                    requestedEnvelope.getCoordinateReferenceSystem(), this.crs)) {
            requestEnvelopeTransformed = requestedEnvelope; // identical CRS

            return; // and finish
        }

        try {
            /** Buffered factory for coordinate operations. */

            // transforming the envelope back to the dataset crs in					
            final MathTransform transform = operationFactory.createOperation(requestedEnvelope.getCoordinateReferenceSystem(),
                    crs).getMathTransform();

            if (transform.isIdentity()) { // Identity Transform ?
                requestEnvelopeTransformed = requestedEnvelope;

                return; // and finish
            }

            requestEnvelopeTransformed = CRS.transform(transform,
                    requestedEnvelope);
            requestEnvelopeTransformed.setCoordinateReferenceSystem(crs);

            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine(new StringBuffer("Reprojected envelope ").append(
                        requestedEnvelope.toString()).append(" crs ")
                                                                     .append(crs.toWKT())
                                                                     .toString());
            }
        } catch (Exception e) {
            throw new DataSourceException("Unable to create a coverage for this source",
                e);
        }
    }

    /**
     * Loading the tiles which overlap with the requested envelope with control
     * over the <code>inputImageThresholdValue</code>, the fading effect
     * between different images, abd the <code>transparentColor</code> for the
     * input images.
     *
     *
     *
     * @param transparentColor
     *            should be used to control transparency on input images.
     * @param outputTransparentColor
     * @param inputImageThresholdValue
     *            should be used to create ROIs on the input images
     * @param pixelDimension
     *            is the dimension in pixels of the requested coverage.
     * @param fading
     *            tells to ask for {@link MosaicDescriptor#MOSAIC_TYPE_BLEND}
     *            instead of the classic
     *            {@link MosaicDescriptor#MOSAIC_TYPE_OVERLAY}.
     * @return a {@link GridCoverage2D} matching as close as possible the
     *         requested {@link GeneralEnvelope} and <code>pixelDimension</code>,
     *         or null in case nothing existed in the requested area.
     * @throws IOException
     */
    private GridCoverage loadTiles(Color outputTransparentColor,
        Rectangle pixelDimension) throws IOException {
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine(new StringBuffer(
                    "Creating mosaic to comply with envelope ").append((requestedEnvelope != null)
                    ? requestedEnvelope.toString() : null).append(" crs ")
                                                                                    .append(crs.toWKT())
                                                                                    .append(" dim ")
                                                                                    .append((pixelDimension == null)
                    ? " null" : pixelDimension.toString()).toString());
        }

        transformRequestEnvelope();

        // /////////////////////////////////////////////////////////////////////
        //
        // Check if we have something to load by intersecting the requested
        // envelope with the bounds of the data set. If not, give warning
        //
        // /////////////////////////////////////////////////////////////////////
        if (!requestEnvelopeTransformed.intersects(this.originalEnvelope, true)) {
            LOGGER.warning(
                "The requested envelope does not intersect the envelope of this mosaic, result is a nodata image");
            LOGGER.warning(requestEnvelopeTransformed.toString());
            LOGGER.warning(originalEnvelope.toString());

            return coverageFactory.create(coverageName,
                getEmptyImage((int) pixelDimension.getWidth(),
                    (int) pixelDimension.getHeight(), outputTransparentColor),
                requestedEnvelope);
        }

        // /////////////////////////////////////////////////////////////////////
        //
        // Load feaures from the index
        // In case there are no features under the requested bbox which is legal
        // in case the mosaic is not a real sqare, we return a fake mosaic.
        //
        // /////////////////////////////////////////////////////////////////////
        final ImageReadParam readP = new ImageReadParam();
        final Integer imageChoice;

        if (pixelDimension != null) {
            try {
                imageChoice = setReadParams(readP, requestEnvelopeTransformed,
                        pixelDimension);
                readP.setSourceSubsampling(1, 1, 0, 0);
            } catch (TransformException e) {
                LOGGER.severe(e.getLocalizedMessage());

                return coverageFactory.create(coverageName,
                    getEmptyImage((int) pixelDimension.getWidth(),
                        (int) pixelDimension.getHeight(), outputTransparentColor),
                    requestedEnvelope);
            }
        } else {
            imageChoice = new Integer(0);
        }

        ImageLevelInfo info = jdbcAccess.getLevelInfo(imageChoice.intValue());
        LOGGER.info("Coverage " + info.getCoverageName() +
            " using spatial table " + info.getSpatialTableName() +
            ", image table " + info.getTileTableName());

        ImageComposerThread imageComposerThread = new ImageComposerThread(outputTransparentColor,
                pixelDimension, requestEnvelopeTransformed, info, tileQueue,
                config);
        imageComposerThread.start();

        jdbcAccess.startTileDecoders(pixelDimension,
            requestEnvelopeTransformed, info, tileQueue);

        try {
            imageComposerThread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        GridCoverage2D result = imageComposerThread.getGridCoverage2D();

        return transformResult(result, pixelDimension);
    }

    private GridCoverage2D transformResult(GridCoverage2D coverage,
        Rectangle pixelDimension) {
        if (requestEnvelopeTransformed == requestedEnvelope) {
            return coverage; // nothing to do
        }

        GridCoverage2D result = null;
        LOGGER.info("Image reprojection necessairy");
        //coverage.show();
        result = (GridCoverage2D) Operations.DEFAULT.resample(coverage,
                requestedEnvelope.getCoordinateReferenceSystem());
        //result.show();
        result = (GridCoverage2D) Operations.DEFAULT.crop(result,
                requestedEnvelope);
        //		result.show();
        
        result = (GridCoverage2D) Operations.DEFAULT.scale(result, 1, 1,
                -result.getRenderedImage().getMinX(),
                -result.getRenderedImage().getMinY());

        //		result.show();
        double scalex = pixelDimension.getWidth() / result.getRenderedImage()
                                                          .getWidth();
        double scaley = pixelDimension.getHeight() / result.getRenderedImage()
                                                           .getHeight();
        result = (GridCoverage2D) Operations.DEFAULT.scale(result, scalex,
                scaley, 0, 0);

        // avoid lazy calculation
        RenderedImageAdapter adapter = new RenderedImageAdapter(result.getRenderedImage());
        BufferedImage resultImage = adapter.getAsBufferedImage();

        return coverageFactory.create(result.getName(), resultImage,
            result.getEnvelope());

        //		result.show();

        //		AffineTransform transform = (AffineTransform) result.getGridGeometry().getGridToCRS();

        // return result;
    }

    /**
     * @param width
     * @param height
     * @param outputTransparentColor
     * @return BufferdImage filled with outputTransparentColor
     */
    private BufferedImage getEmptyImage(int width, int height,
        Color outputTransparentColor) {
        BufferedImage emptyImage = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2D = (Graphics2D) emptyImage.getGraphics();
        Color save = g2D.getColor();
        g2D.setColor(outputTransparentColor);
        g2D.fillRect(0, 0, emptyImage.getWidth(), emptyImage.getHeight());
        g2D.setColor(save);

        return emptyImage;
    }
}
