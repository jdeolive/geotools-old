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
 */
package org.geotools.arcsde.gce;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.ColorModel;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.awt.image.renderable.ParameterBlock;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.stream.ImageInputStream;
import javax.media.jai.ImageLayout;
import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;

import org.geotools.arcsde.ArcSdeException;
import org.geotools.arcsde.session.ArcSDEPooledConnection;
import org.geotools.data.DataSourceException;
import org.geotools.util.logging.Logging;

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
 * The default implementation for {@link TiledRasterReader}.
 * <p>
 * This implementation holds a connection and an open {@link SeQuery query} until the reader is
 * exhausted or {@link #dispose()} is called.
 * </p>
 * 
 * @author Gabriel Roldan
 * @version $Id$
 * @since 2.5.7
 */
class DefaultTiledRasterReader implements TiledRasterReader {

    private static final Logger LOGGER = Logging.getLogger("org.geotools.arcsde.gce");

    private ArcSDEPooledConnection conn;

    private RasterDatasetInfo rasterInfo;

    private final SeQuery preparedQuery;

    private SeRow row;

    private SeRasterAttr rAttr;

    private Long rasterId;

    /**
     * Creates an {@link DefaultTiledRasterReader} that uses the given connection to fetch raster
     * data for the given {@link RasterDatasetInfo rasterInfo}.
     * <p>
     * </p>
     * 
     * @param conn
     * @param rasterInfo
     * @throws IOException
     */
    public DefaultTiledRasterReader(final ArcSDEPooledConnection conn,
            final RasterDatasetInfo rasterInfo) throws IOException {
        this.conn = conn;
        this.rasterInfo = rasterInfo;

        preparedQuery = createSeQuery(conn);

        try {
            preparedQuery.execute();
        } catch (SeException e) {
            dispose();
            throw new ArcSdeException(e);
        }
    }

    /**
     * @see org.geotools.arcsde.gce.TiledRasterReader#dispose()
     */
    public void dispose() {
        if (conn != null) {
            try {
                preparedQuery.close();
            } catch (SeException e) {
                e.printStackTrace();
            }
            conn.close();
            conn = null;
        }
    }

    /**
     * @see org.geotools.arcsde.gce.TiledRasterReader#nextRaster()
     */
    public Long nextRaster() throws IOException {
        try {
            this.row = preparedQuery.fetch();
            if (row == null) {
                dispose();
                return null;
            }

            // we don't work with datasets with more than one raster column
            final int rasterColumnIndex = 0;
            this.rAttr = row.getRaster(rasterColumnIndex);
            this.rasterId = Long.valueOf(rAttr.getRasterId().longValue());
        } catch (SeException e) {
            throw new ArcSdeException(e);
        }
        return this.rasterId;
    }

    /**
     * @see org.geotools.arcsde.gce.TiledRasterReader#read(int, java.awt.Rectangle)
     */
    public RenderedImage read(final int pyramidLevel, final Rectangle tileRange) throws IOException {
        final RenderedImage rasterImage;

        // final Point imageLocation = rasterQueryInfo.getTiledImageSize().getLocation();

        try {
            rasterImage = getRasterMatchingTileRange(pyramidLevel, tileRange);
        } catch (IOException e) {
            dispose();
            throw e;
        } catch (RuntimeException e) {
            dispose();
            throw e;
        }
        return rasterImage;
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

    private RenderedImage getRasterMatchingTileRange(int pyramidLevel, final Rectangle matchingTiles)
            throws IOException {

        /*
         * Create the prepared query (not executed) stream to fetch the tiles from
         */

        // covers an area of full tiles
        final RenderedImage fullTilesRaster;

        /*
         * Create the tiled raster covering the full area of the matching tiles
         */

        fullTilesRaster = createTiledRaster(pyramidLevel, matchingTiles);

        /*
         * REVISIT: This is odd, we need to force the data to be loaded so we're free to release the
         * stream, which gives away the streamed, tiled nature of this rasters, but I don't see the
         * GCE api having a very clear usage workflow that ensures close() is always being called to
         * the underlying ImageInputStream so we could let it close the SeQuery when done.
         */
        try {
            fullTilesRaster.getData();
        } catch (RuntimeException e) {
            throw new DataSourceException("Error fetching arcsde raster", e);
        }
        return fullTilesRaster;
    }

    private RenderedOp createTiledRaster(final int pyramidLevel, final Rectangle matchingTiles)
            throws IOException {

        final int rasterIndex = rasterInfo.getRasterIndex(rasterId);

        final int tileWidth;
        final int tileHeight;
        final int numberOfBands;
        try {
            numberOfBands = rAttr.getNumBands();
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
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("Requesting tiles [x=" + minTileX + "-" + maxTileX + ", y=" + minTileY
                        + "-" + maxTileY + "] from tile range [x=0-"
                        + (rAttr.getTilesPerRowByLevel(pyramidLevel) - 1) + ", y=0-"
                        + (rAttr.getTilesPerColByLevel(pyramidLevel) - 1) + "]");
            }
            // SDEPoint tileOrigin = rAttr.getTileOrigin();

            if (LOGGER.isLoggable(Level.FINE)) {
                Rectangle tiledImageSize = new Rectangle(0, 0, tileWidth * matchingTiles.width,
                        tileHeight * matchingTiles.height);

                LOGGER.fine("Tiled image size: " + tiledImageSize);
            }

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

        final TileReader tileReader;
        {
            final Dimension tileSize = new Dimension(tileWidth, tileHeight);
            tileReader = TileReaderFactory.getInstance(row, rasterInfo, rasterIndex, matchingTiles,
                    tileSize);
        }

        // Prepare temporary colorModel and sample model, needed to build the final
        // ArcSDEPyramidLevel level;
        final Dimension tiledImageSize;
        final ColorModel colorModel;
        final SampleModel sampleModel;
        {
            final int tiledImageWidth = tileReader.getTilesWide() * tileReader.getTileWidth();
            final int tiledImageHeight = tileReader.getTilesHigh() * tileReader.getTileHeight();
            tiledImageSize = new Dimension(tiledImageWidth, tiledImageHeight);

            final ImageTypeSpecifier fullImageSpec = rasterInfo.getRenderedImageSpec(rasterIndex);
            colorModel = fullImageSpec.getColorModel();
            sampleModel = fullImageSpec.getSampleModel(tiledImageWidth, tiledImageHeight);
        }

        // Finally, build the image input stream
        final RawImageInputStream raw;
        {
            final long[] imageOffsets = new long[] { 0 };
            final Dimension[] imageDimensions = new Dimension[] { tiledImageSize };

            final ImageTypeSpecifier its = new ImageTypeSpecifier(colorModel, sampleModel);

            final ImageInputStream tiledImageInputStream;
            tiledImageInputStream = new ArcSDETiledImageInputStream(tileReader);

            raw = new RawImageInputStream(tiledImageInputStream, its, imageOffsets, imageDimensions);
        }

        // building the final image layout
        final ImageLayout imageLayout;
        {
            // the value for the resulting image.getMinX() and image.getMinY() reflecting its
            // position in the whole image at the pyramid level
            int minX = (matchingTiles.x * tileWidth);
            int minY = (matchingTiles.y * tileHeight);

            int width = tiledImageSize.width;
            int height = tiledImageSize.height;

            int tileGridXOffset = minX;
            int tileGridYOffset = minY;

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
