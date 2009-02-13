/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2002-2009, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.arcsde.gce;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.ColorModel;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.awt.image.renderable.ParameterBlock;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.stream.ImageInputStream;
import javax.media.jai.ImageLayout;
import javax.media.jai.InterpolationNearest;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.RenderedOp;
import javax.media.jai.operator.MosaicDescriptor;

import org.geotools.arcsde.ArcSdeException;
import org.geotools.arcsde.gce.RasterUtils.QueryInfo;
import org.geotools.arcsde.pool.ArcSDEConnectionPool;
import org.geotools.arcsde.pool.ArcSDEPooledConnection;
import org.geotools.coverage.CoverageFactoryFinder;
import org.geotools.coverage.GridSampleDimension;
import org.geotools.coverage.grid.GeneralGridEnvelope;
import org.geotools.coverage.grid.GeneralGridRange;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.coverage.grid.io.OverviewPolicy;
import org.geotools.data.DefaultServiceInfo;
import org.geotools.data.ServiceInfo;
import org.geotools.factory.Hints;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.util.logging.Logging;
import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.coverage.grid.GridCoverageReader;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.NoninvertibleTransformException;
import org.opengis.referencing.operation.TransformException;

import com.esri.sde.sdk.client.SeException;
import com.esri.sde.sdk.client.SeQuery;
import com.esri.sde.sdk.client.SeRaster;
import com.esri.sde.sdk.client.SeRasterAttr;
import com.esri.sde.sdk.client.SeRasterConstraint;
import com.esri.sde.sdk.client.SeRow;
import com.esri.sde.sdk.client.SeSqlConstruct;
import com.sun.media.imageio.stream.RawImageInputStream;
import com.sun.media.imageioimpl.plugins.raw.RawImageReaderSpi;

/**
 * 
 * @author Gabriel Roldan (OpenGeo)
 * @since 2.5.4
 * @version $Id$
 * @source $URL$
 */
@SuppressWarnings( { "deprecation", "nls" })
class ArcSDEGridCoverage2DReaderJAI extends AbstractGridCoverage2DReader {

    private static final Logger LOGGER = Logging.getLogger("org.geotools.arcsde.gce");

    private final ArcSDERasterFormat parent;

    private final ArcSDEConnectionPool connectionPool;

    private final RasterInfo rasterInfo;

    private DefaultServiceInfo serviceInfo;

    public ArcSDEGridCoverage2DReaderJAI(final ArcSDERasterFormat parent,
            final ArcSDEConnectionPool connectionPool, final RasterInfo rasterInfo,
            final Hints hints) throws IOException {
        // check it's a supported format
        {
            final int bitsPerSample = rasterInfo.getBand(0, 0).getCellType().getBitsPerSample();
            if (rasterInfo.getNumBands() > 1 && (bitsPerSample == 1 || bitsPerSample == 4)) {
                throw new IllegalArgumentException(bitsPerSample
                        + "-bit rasters with more than one band are not supported");
            }
        }
        this.parent = parent;
        this.connectionPool = connectionPool;
        this.rasterInfo = rasterInfo;

        super.hints = hints;
        super.coverageFactory = CoverageFactoryFinder.getGridCoverageFactory(this.hints);
        super.crs = rasterInfo.getCoverageCrs();
        super.originalEnvelope = rasterInfo.getOriginalEnvelope();

        GeneralGridEnvelope gridRange = rasterInfo.getOriginalGridRange();
        super.originalGridRange = new GeneralGridRange(gridRange.toRectangle());

        super.coverageName = rasterInfo.getRasterTable();
        final int numLevels = rasterInfo.getNumPyramidLevels(0);

        // level 0 is not an overview, but the raster itself
        super.numOverviews = numLevels - 1;

        // ///
        // 
        // setting the higher resolution avalaible for this coverage
        //
        // ///
        highestRes = super.getResolution(originalEnvelope, originalGridRange.toRectangle(), crs);
        // //
        //
        // get information for the successive images
        //
        // //
        // REVISIT may the different rasters in the raster dataset have different pyramid levels? I
        // guess so
        if (numOverviews > 0) {
            overViewResolutions = new double[numOverviews][2];
            for (int pyramidLevel = 1; pyramidLevel <= numOverviews; pyramidLevel++) {
                Rectangle levelGridRange = rasterInfo.getGridRange(0, pyramidLevel);
                GeneralEnvelope levelEnvelope = rasterInfo.getGridEnvelope(0, pyramidLevel);
                overViewResolutions[pyramidLevel - 1] = super.getResolution(levelEnvelope,
                        levelGridRange, crs);
            }
        } else {
            overViewResolutions = null;
        }
    }

    /**
     * @see GridCoverageReader#getFormat()
     */
    public Format getFormat() {
        return parent;
    }

    @Override
    public ServiceInfo getInfo() {
        if (serviceInfo == null) {
            serviceInfo = new DefaultServiceInfo();
            serviceInfo.setTitle(rasterInfo.getRasterTable() + " is an ArcSDE Raster");
            serviceInfo.setDescription(rasterInfo.toString());
            Set<String> keywords = new HashSet<String>();
            keywords.add("ArcSDE");
            serviceInfo.setKeywords(keywords);
        }
        return serviceInfo;
    }

    /**
     * @see GridCoverageReader#read(GeneralParameterValue[])
     */
    public GridCoverage read(GeneralParameterValue[] params) throws IOException {

        final GeneralEnvelope requestedEnvelope;
        final Rectangle requestedDim;
        final OverviewPolicy overviewPolicy;
        {
            final ReadParameters opParams = RasterUtils.parseReadParams(getOriginalEnvelope(),
                    params);
            overviewPolicy = opParams.overviewPolicy;
            requestedEnvelope = opParams.requestedEnvelope;
            requestedDim = opParams.dim;
        }

        /*
         * For each raster in the raster dataset, obtain the tiles, pixel range, and resulting
         * envelope
         */
        final List<QueryInfo> queries;
        queries = findMatchingRasters(requestedEnvelope, requestedDim, overviewPolicy);

        final GeneralEnvelope resultEnvelope = getResultEnvelope(queries);

        /*
         * Once we collected the matching rasters and their image subsets, find out where in the
         * overall resulting mosaic they fit. If the rasters does not share the spatial resolution,
         * the QueryInfo.resultDimension and QueryInfo.mosaicLocation width or height won't match
         */
        final Rectangle mosaicDimension;
        mosaicDimension = RasterUtils.setMosaicLocations(rasterInfo, resultEnvelope, queries);

        /*
         * Gather the rendered images for each of the rasters that match the requested envelope
         */
        final ArcSDEPooledConnection conn = connectionPool.getConnection();
        final SeQuery preparedQuery = createSeQuery(conn);
        try {
            preparedQuery.execute();

            final Map<Long, QueryInfo> byRasterdIdQueries = new HashMap<Long, QueryInfo>();
            for (QueryInfo q : queries) {
                byRasterdIdQueries.put(q.getRasterId(), q);
            }

            SeRow row = preparedQuery.fetch();
            StringBuilder sbExtent = new StringBuilder("MULTIPOLYGON (");
            StringBuilder sbMosaic = new StringBuilder("MULTIPOLYGON (");
            while (row != null) {
                final SeRasterAttr rAttr = row.getRaster(0);
                final Long rasterId = Long.valueOf(rAttr.getRasterId().longValue());
                final RenderedImage rasterImage;

                if (byRasterdIdQueries.containsKey(rasterId)) {

                    final QueryInfo rasterQueryInfo = byRasterdIdQueries.get(rasterId);
                    LOGGER.finer(rasterQueryInfo.toString());
                    if (LOGGER.isLoggable(Level.FINEST)) {
                        GeneralEnvelope env = rasterQueryInfo.getResultEnvelope();
                        Rectangle mosaicLocation = rasterQueryInfo.getMosaicLocation();
                        appendLoggingGeometries(sbExtent, sbMosaic, env, mosaicLocation);
                    }
                    final int pyramidLevelChoice = rasterQueryInfo.getPyramidLevel();

                    rasterImage = getRaster(pyramidLevelChoice, preparedQuery, row, rAttr,
                            rasterQueryInfo);

                    rasterQueryInfo.setResultImage(rasterImage);

                    // ImageIO
                    // .write(rasterImage, "TIFF", new File(
                    // "/home/groldan/arcsde_test/mosaicTile"
                    // + rasterQueryInfo.getRasterId()));
                }
                // advance to the next raster in the dataset
                row = preparedQuery.fetch();
            }
            if (LOGGER.isLoggable(Level.FINEST)) {
                sbExtent.setLength(sbExtent.length() - 1);
                sbExtent.append("\n)");
                sbMosaic.setLength(sbMosaic.length() - 1);
                sbMosaic.append("\n)");
                LOGGER.finest("Grid geometry:\n" + sbExtent.toString() + "\n Mosaic geometry:\n"
                        + sbMosaic.toString());
            }
        } catch (SeException e) {
            throw new ArcSdeException(e);
        } finally {
            try {
                preparedQuery.close();
            } catch (SeException e) {
                throw new ArcSdeException(e);
            } finally {
                conn.close();
            }
        }

        /*
         * BUILDING COVERAGE
         */
        final GridSampleDimension[] bands = rasterInfo.getGridSampleDimensions();

        final RenderedImage coverageRaster = createMosaic(mosaicDimension, queries);

        return coverageFactory.create(coverageName, coverageRaster, resultEnvelope, bands, null,
                null);
    }

    private void appendLoggingGeometries(StringBuilder sbExtent, StringBuilder sbMosaic,
            GeneralEnvelope env, Rectangle mosaicLocation) {
        sbExtent.append("\n  ((" + env.getMinimum(0) + " " + env.getMinimum(1) + ", "
                + env.getMaximum(0) + " " + env.getMinimum(1) + ", " + env.getMaximum(0) + " "
                + env.getMaximum(1) + ", " + env.getMinimum(0) + " " + env.getMaximum(1) + ", "
                + env.getMinimum(0) + " " + env.getMinimum(1) + ")),");
        sbMosaic.append("\n  ((" + mosaicLocation.getMinX() + " " + mosaicLocation.getMinY() + ", "
                + mosaicLocation.getMaxX() + " " + mosaicLocation.getMinY() + ", "
                + mosaicLocation.getMaxX() + " " + mosaicLocation.getMaxY() + ", "
                + mosaicLocation.getMinX() + " " + mosaicLocation.getMaxY() + ", "
                + mosaicLocation.getMinX() + " " + mosaicLocation.getMinY() + ")),");
    }

    private List<QueryInfo> findMatchingRasters(final GeneralEnvelope requestedEnvelope,
            final Rectangle requestedDim, final OverviewPolicy overviewPolicy) {

        final int numRasters = rasterInfo.getNumRasters();

        final List<QueryInfo> byRasterdIdQueries = new ArrayList<QueryInfo>(numRasters);

        MathTransform modelToRaster;
        {
            int pyramidLevel = rasterInfo.getOptimalPyramidLevel(0, overviewPolicy,
                    requestedEnvelope, requestedDim);
            GeneralEnvelope gridEnvelope = rasterInfo.getGridEnvelope(0, pyramidLevel);
            Rectangle gridRange = rasterInfo.getGridRange(0, pyramidLevel);
            MathTransform rasterToModel = RasterUtils.createRasterToModel(gridRange, gridEnvelope);
            try {
                modelToRaster = rasterToModel.inverse();
            } catch (NoninvertibleTransformException e) {
                throw new RuntimeException(e);
            }
        }
        for (int rasterN = 0; rasterN < numRasters; rasterN++) {

            final QueryInfo rasterQueryInfo = RasterUtils.fitRequestToRaster(requestedEnvelope,
                    requestedDim, rasterInfo, rasterN, overviewPolicy);

            if (rasterQueryInfo.getResultDimension().getWidth() > 0) {
                // the requested envelope overlaps the raster envelope
                byRasterdIdQueries.add(rasterQueryInfo);
            }
        }
        return byRasterdIdQueries;
    }

    private GeneralEnvelope getResultEnvelope(final List<QueryInfo> queryInfos) {

        GeneralEnvelope finalEnvelope = null;

        for (QueryInfo rasterQueryInfo : queryInfos) {
            // gather resulting envelope
            if (finalEnvelope == null) {
                finalEnvelope = new GeneralEnvelope(rasterQueryInfo.getResultEnvelope());
            } else {
                finalEnvelope.add(rasterQueryInfo.getResultEnvelope());
            }
        }
        if (finalEnvelope == null) {
            throw new IllegalStateException("Restult envelope is null, this shouldn't happen!! "
                    + "we checked the request overlaps the coverage envelope before!");
        }
        return finalEnvelope;
    }

    /**
     * Creates the mosaiced image that results from querying all the rasters in the raster dataset
     * for the requested envelope
     * 
     * @param mosaicDimension
     *            the dimensions of the resulting image
     * @param queries
     *            the images to mosaic together
     * @return the mosaiced image
     */
    private RenderedImage createMosaic(final Rectangle mosaicDimension,
            final List<QueryInfo> queries) {
        if (queries.size() == 1) {
            // no need to mosaic at all
            return queries.get(0).getResultImage();
        }

        List<RenderedImage> translated = new ArrayList<RenderedImage>(queries.size());

        for (QueryInfo query : queries) {
            RenderedImage image = query.getResultImage();
            Rectangle mosaicLocation = query.getMosaicLocation();
            ParameterBlock pb = new ParameterBlock();
            pb.addSource(image);
            pb.add(Float.valueOf((float) mosaicLocation.getX()));
            pb.add(Float.valueOf((float) mosaicLocation.getY()));
            pb.add(new InterpolationNearest());
            RenderedOp trans = JAI.create("translate", pb, null);
            translated.add(trans);
        }

        ParameterBlock mosaicParams = new ParameterBlock();
        for (RenderedImage img : translated) {
            System.err.println("--> adding to mosaic: " + img.getMinX() + "," + img.getMinY() + ","
                    + img.getWidth() + "," + img.getHeight());
            mosaicParams.addSource(img);
        }
        mosaicParams.add(MosaicDescriptor.MOSAIC_TYPE_OVERLAY); // mosaic type
        mosaicParams.add(null); // alpha mask
        mosaicParams.add(null); // source ROI mask
        mosaicParams.add(null); // source threshold
        mosaicParams.add(null); // destination background value

        RenderedOp mosaic = JAI.create("Mosaic", mosaicParams, null);
        return mosaic;
    }

    private RenderedImage getRaster(int pyramidLevelChoice, final SeQuery preparedQuery, SeRow row,
            final SeRasterAttr rAttr, final QueryInfo rasterQueryInfo) throws IOException {
        final RenderedImage rasterImage;
        /*
         * Create the prepared query (not executed) stream to fetch the tiles from
         */
        final Rectangle matchingTiles = rasterQueryInfo.getMatchingTiles();

        // covers an area of full tiles
        final RenderedImage fullTilesRaster;

        /*
         * Create the tiled raster covering the full area of the matching tiles
         */
        final Rectangle resultDimension = rasterQueryInfo.getResultDimension();
        fullTilesRaster = createTiledRaster(preparedQuery, row, rAttr, matchingTiles,
                pyramidLevelChoice, resultDimension.getLocation());

        /*
         * REVISIT: This is odd, we need to force the data to be loaded so we're free to release the
         * stream, which gives away the streamed, tiled nature of this rasters, but I don't see the
         * GCE api having a very clear usage workflow that ensures close() is always being called to
         * the underlying ImageInputStream so we could let it close the SeQuery when done.
         */
        try {
            fullTilesRaster.getData();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Fetching data for " + rasterQueryInfo.toString(), e);
        }

        /*
         * now crop it to the desired dimensions
         */
        // where the resulting raster fits into the overal mosaic being built for the raster dataset
        // final Rectangle imageLayout = rasterQueryInfo.getResultDimension();
        rasterImage = cropToRequiredDimension(fullTilesRaster, resultDimension);

        return rasterImage;
    }

    private RenderedImage cropToRequiredDimension(final RenderedImage fullTilesRaster,
            final Rectangle cropTo) {

        int minX = fullTilesRaster.getMinX();
        int minY = fullTilesRaster.getMinY();
        int width = fullTilesRaster.getWidth();
        int height = fullTilesRaster.getHeight();

        Rectangle origDim = new Rectangle(minX, minY, width, height);
        if (!origDim.contains(cropTo)) {
            throw new IllegalArgumentException("Original image (" + origDim
                    + ") does not contain desired dimension (" + cropTo + ")");
        } else if (origDim.equals(cropTo)) {
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("No need to crop image, full tiled dimension and target one "
                        + "do match: original: " + width + "x" + height + ", target: "
                        + cropTo.width + "x" + cropTo.height);
            }
            return fullTilesRaster;
        }

        ParameterBlock cropParams = new ParameterBlock();

        cropParams.addSource(fullTilesRaster);// Source
        cropParams.add(Float.valueOf(cropTo.x)); // x origin for each band
        cropParams.add(Float.valueOf(cropTo.y)); // y origin for each band
        cropParams.add(Float.valueOf(cropTo.width));// width for each band
        cropParams.add(Float.valueOf(cropTo.height));// height for each band

        final RenderingHints hints = null;
        RenderedOp image = JAI.create("Crop", cropParams, hints);
        return image;
    }

    static class ReadParameters {
        GeneralEnvelope requestedEnvelope;

        Rectangle dim;

        OverviewPolicy overviewPolicy;
    }

    /**
     * Creates a prepared query for the coverage's table, does not set any constraint nor executes
     * it.
     */
    private SeQuery createSeQuery(final ArcSDEPooledConnection conn) throws IOException {
        final SeQuery seQuery;
        final String[] rasterColumns = rasterInfo.getRasterColumns();
        final String tableName = rasterInfo.getRasterTable();
        try {
            seQuery = new SeQuery(conn, rasterColumns, new SeSqlConstruct(tableName));
            seQuery.prepareQuery();
        } catch (SeException e) {
            throw new ArcSdeException(e);
        }
        return seQuery;
    }

    private RenderedOp createTiledRaster(final SeQuery preparedQuery, final SeRow row,
            final SeRasterAttr rAttr, final Rectangle matchingTiles, final int pyramidLevel,
            final Point mosaicLocation) throws IOException {

        final int tileWidth;
        final int tileHeight;
        final int numberOfBands;
        final RasterCellType pixelType;
        try {
            numberOfBands = rAttr.getNumBands();
            pixelType = RasterCellType.valueOf(rAttr.getPixelType());
            tileWidth = rAttr.getTileWidth();
            tileHeight = rAttr.getTileHeight();

            int[] bandsToQuery = new int[numberOfBands];
            for (int bandN = 1; bandN <= numberOfBands; bandN++) {
                bandsToQuery[bandN - 1] = bandN;
            }

            int minTileX = matchingTiles.x;
            int minTileY = matchingTiles.y;
            int maxTileX = minTileX + matchingTiles.width - 1;
            int maxTileY = minTileY + matchingTiles.height - 1;
            LOGGER.fine("Requesting tiles [" + minTileX + "," + minTileY + ":" + maxTileX + ","
                    + maxTileY + "]");

            final int interleaveType = SeRaster.SE_RASTER_INTERLEAVE_BIP;

            SeRasterConstraint rConstraint = new SeRasterConstraint();
            rConstraint.setBands(bandsToQuery);
            rConstraint.setLevel(pyramidLevel);
            rConstraint.setEnvelope(minTileX, minTileY, maxTileX, maxTileY);
            rConstraint.setInterleave(interleaveType);

            preparedQuery.queryRasterTile(rConstraint);

        } catch (SeException se) {
            throw new ArcSdeException(se);
        }

        // Finally, build the image input stream
        final ImageInputStream tiledImageInputStream;

        final TileReader tileReader = TileReader.getInstance(row, pixelType.getBitsPerSample(),
                numberOfBands, matchingTiles, new Dimension(tileWidth, tileHeight));
        tiledImageInputStream = new ArcSDETiledImageInputStream(tileReader);

        final int tiledImageWidth = tileReader.getTilesWide() * tileReader.getTileWidth();
        final int tiledImageHeight = tileReader.getTilesHigh() * tileReader.getTileHeight();

        // Prepare temporary colorModel and sample model, needed to build the final
        // ArcSDEPyramidLevel level;
        final ColorModel colorModel;
        final SampleModel sampleModel;
        {
            final ImageTypeSpecifier fullImageSpec = rasterInfo.getRenderedImageSpec();
            colorModel = fullImageSpec.getColorModel();
            sampleModel = fullImageSpec.getSampleModel(tiledImageWidth, tiledImageHeight);
        }

        final long[] imageOffsets = new long[] { 0 };
        final Dimension[] imageDimensions = new Dimension[] { new Dimension(tiledImageWidth,
                tiledImageHeight) };

        final ImageTypeSpecifier its = new ImageTypeSpecifier(colorModel, sampleModel);
        final RawImageInputStream raw = new RawImageInputStream(tiledImageInputStream, its,
                imageOffsets, imageDimensions);

        // building the final image layout
        final ImageLayout imageLayout;
        {
            int minX = mosaicLocation.x;
            int minY = mosaicLocation.y;
            int width = tiledImageWidth;
            int height = tiledImageHeight;

            int tileGridXOffset = 0;
            int tileGridYOffset = 0;

            imageLayout = new ImageLayout(minX, minY, width, height, tileGridXOffset,
                    tileGridYOffset, tileWidth, tileHeight, sampleModel, colorModel);
        }

        // First operator: read the image
        final RenderingHints hints = new RenderingHints(JAI.KEY_IMAGE_LAYOUT, imageLayout);

        ParameterBlock pb = new ParameterBlock();
        pb.add(raw);// Input
        /*
         * image index, always 0 since we're already fetching the required pyramid level
         */
        pb.add(Integer.valueOf(0)); // Image index
        pb.add(Boolean.TRUE); // Read metadata
        pb.add(Boolean.TRUE);// Read thumbnails
        pb.add(Boolean.TRUE);// Verify input
        pb.add(null);// Listeners
        pb.add(null);// Locale
        final ImageReadParam rParam = new ImageReadParam();
        pb.add(rParam);// ReadParam
        RawImageReaderSpi imageIOSPI = new RawImageReaderSpi();
        ImageReader readerInstance = imageIOSPI.createReaderInstance();
        pb.add(readerInstance);// Reader

        RenderedOp image = JAI.create("ImageRead", pb, hints);
        return image;
    }
}
