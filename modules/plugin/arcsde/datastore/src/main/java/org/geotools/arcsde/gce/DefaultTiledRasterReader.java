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
import java.util.logging.Logger;

import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.stream.ImageInputStream;
import javax.media.jai.ImageLayout;
import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;

import org.geotools.arcsde.session.Command;
import org.geotools.arcsde.session.ISession;
import org.geotools.arcsde.session.ISessionPool;
import org.geotools.data.DataSourceException;
import org.geotools.util.logging.Logging;

import com.esri.sde.sdk.client.SeConnection;
import com.esri.sde.sdk.client.SeException;
import com.esri.sde.sdk.client.SeQuery;
import com.esri.sde.sdk.client.SeRasterAttr;
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

    private RasterDatasetInfo rasterInfo;

    // private final SeQuery preparedQuery;

    // private final FetchRasterCommand fetchCommand;

    private final ISessionPool sessionPool;

    /**
     * @see DefaultTiledRasterReader#nextRaster()
     */
    private static class FetchRasterCommand extends Command<Long> {

        private SeQuery preparedQuery;

        private SeRow row;

        private SeRasterAttr rAttr;

        public FetchRasterCommand(final SeQuery preparedQuery) {
            this.preparedQuery = preparedQuery;
        }

        @Override
        public Long execute(ISession session, SeConnection connection) throws SeException,
                IOException {
            this.row = preparedQuery.fetch();
            if (row == null) {
                return null;
            }

            // we don't work with datasets with more than one raster column
            final int rasterColumnIndex = 0;
            this.rAttr = row.getRaster(rasterColumnIndex);
            Long rasterId = Long.valueOf(rAttr.getRasterId().longValue());
            return rasterId;
        }

        public SeRasterAttr getRasterAttribute() {
            return rAttr;
        }

        public SeRow getSeRow() {
            return row;
        }
    }

    /**
     * Creates an {@link DefaultTiledRasterReader} that uses the given connection to fetch raster
     * data for the given {@link RasterDatasetInfo rasterInfo}.
     * <p>
     * </p>
     * 
     * @param sessionPool
     *            where to grab sessions from to query the rasters described by {@code rasterInfo}
     * @param rasterInfo
     * @throws IOException
     */
    public DefaultTiledRasterReader(final ISessionPool sessionPool,
            final RasterDatasetInfo rasterInfo) throws IOException {
        this.sessionPool = sessionPool;
        this.rasterInfo = rasterInfo;
        //
        // try {
        // preparedQuery = createAndExecuteSeQuery(conn);
        // } catch (IOException e) {
        // dispose();
        // throw e;
        // } catch (RuntimeException e) {
        // dispose();
        // throw e;
        // } catch (Exception e) {
        // dispose();
        // throw new DataSourceException(e);
        // }

        // this.fetchCommand = new FetchRasterCommand(preparedQuery);
    }

    /**
     * @see org.geotools.arcsde.gce.TiledRasterReader#nextRaster()
     */
    // public Long nextRaster() throws IOException {
    // this.rasterId = session.issue(fetchCommand);
    // // if (this.rasterId == null) {
    // // dispose();
    // // }
    // return this.rasterId;
    // }

    /**
     * Disposes in case of an error
     * 
     * @see org.geotools.arcsde.gce.TiledRasterReader#dispose()
     */
    // private void dispose() {
    // if (session != null) {
    // try {
    // session.close(preparedQuery);
    // } catch (Exception e) {
    // e.printStackTrace();
    // }
    // session.dispose();
    // session = null;
    // }
    // }

    /**
     * @see org.geotools.arcsde.gce.TiledRasterReader#read
     */
    public RenderedImage read(final long rasterId, final int pyramidLevel, final Rectangle tileRange)
            throws IOException {
        final RenderedImage rasterImage;

        // final Point imageLocation = rasterQueryInfo.getTiledImageSize().getLocation();

        // try {
        rasterImage = getRasterMatchingTileRange(rasterId, pyramidLevel, tileRange);
        // } catch (IOException e) {
        // dispose();
        // throw e;
        // } catch (RuntimeException e) {
        // dispose();
        // throw e;
        // }
        return rasterImage;
    }

    /**
     * Creates a prepared query for the coverage's table, does not set any constraint nor executes
     * it.
     * 
     * @param the
     *            id of the raster in the raster catalog to retrieve
     * @param session
     *            the session to use in querying the ArcSDE server
     */
    private SeQuery createAndExecuteSeQuery(final long rasterId, final ISession session)
            throws IOException {
        final SeQuery seQuery;
        final String[] rasterColumns = rasterInfo.getRasterColumns();
        final String tableName = rasterInfo.getRasterTable();

        final SeSqlConstruct sqlConstruct = new SeSqlConstruct(tableName);
        /*
         * Filter by the given raster id
         */
        final String rasterIdFilter = rasterColumns[0] + " = " + rasterId;
        sqlConstruct.setWhere(rasterIdFilter);

        seQuery = session.createAndExecuteQuery(rasterColumns, sqlConstruct);

        return seQuery;
    }

    private RenderedImage getRasterMatchingTileRange(final long rasterId, int pyramidLevel,
            final Rectangle matchingTiles) throws IOException {

        final TileReader tileReader;
        tileReader = TileReaderFactory.getInstance(sessionPool, rasterInfo, rasterId, pyramidLevel,
                matchingTiles);

        // covers an area of full tiles
        final RenderedImage fullTilesRaster;

        /*
         * Create the tiled raster covering the full area of the matching tiles
         */

        fullTilesRaster = createTiledRaster(tileReader, matchingTiles, rasterId);

        /*
         * REVISIT: This is odd, we need to force the data to be loaded so we're free to release the
         * stream, which gives away the streamed, tiled nature of this rasters, but I don't see the
         * GCE api having a very clear usage workflow that ensures close() is always being called to
         * the underlying ImageInputStream so we could let it close the SeQuery when done.
         */
        // try {
        // LOGGER.info("Forcing loading data for " + rasterInfo.getRasterTable() + "#" + rasterId);
        // fullTilesRaster.getData();
        // } catch (RuntimeException e) {
        // throw new DataSourceException("Error fetching arcsde raster", e);
        // }
        return fullTilesRaster;
    }

    /**
     * @param tileReader
     * @param matchingTiles
     * @param rasterId
     * @return
     * @throws IOException
     */
    private RenderedOp createTiledRaster(final TileReader tileReader,
            final Rectangle matchingTiles, final long rasterId) throws IOException {
        // Prepare temporary colorModel and sample model, needed to build the final
        // ArcSDEPyramidLevel level;
        final Dimension tiledImageSize;
        final ColorModel colorModel;
        final SampleModel sampleModel;
        {
            final int tiledImageWidth = tileReader.getTilesWide() * tileReader.getTileWidth();
            final int tiledImageHeight = tileReader.getTilesHigh() * tileReader.getTileHeight();
            tiledImageSize = new Dimension(tiledImageWidth, tiledImageHeight);

            final ImageTypeSpecifier fullImageSpec = rasterInfo.getRenderedImageSpec(rasterId);
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

        final int tileWidth = tileReader.getTileWidth();
        final int tileHeight = tileReader.getTileHeight();

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
