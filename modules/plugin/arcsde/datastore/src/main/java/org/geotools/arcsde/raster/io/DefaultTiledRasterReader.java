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
package org.geotools.arcsde.raster.io;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.image.ColorModel;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.io.IOException;
import java.util.logging.Logger;

import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.stream.ImageInputStream;
import javax.media.jai.ImageLayout;

import org.geotools.arcsde.raster.info.RasterDatasetInfo;
import org.geotools.arcsde.raster.jai.ArcSDEImageReader;
import org.geotools.arcsde.session.ISessionPool;
import org.geotools.util.logging.Logging;

import com.esri.sde.sdk.client.SeQuery;

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

    private final ISessionPool sessionPool;

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
    }

    /**
     * @see org.geotools.arcsde.raster.io.TiledRasterReader#read
     */
    public RenderedImage read(final long rasterId, final int pyramidLevel, final Rectangle tileRange)
            throws IOException {
        final RenderedImage rasterImage;

        rasterImage = getRasterMatchingTileRange(rasterId, pyramidLevel, tileRange);

        return rasterImage;
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

        return fullTilesRaster;
    }

    /**
     * @param tileReader
     * @param matchingTiles
     * @param rasterId
     * @return
     * @throws IOException
     */
    private RenderedImage createTiledRaster(final TileReader tileReader,
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

            // imageLayout = new ImageLayout(minX, minY, width, height, tileGridXOffset,
            // tileGridYOffset, tileWidth, tileHeight, sampleModel, colorModel);
            imageLayout = new ImageLayout(0, 0, width, height, 0, 0, tileWidth, tileHeight,
                    sampleModel, colorModel);
        }

        // Finally, build the image input stream
        final ImageInputStream raw;
        final ImageReader readerInstance;
        {
            final long[] imageOffsets = new long[] { 0 };
            final Dimension[] imageDimensions = new Dimension[] { tiledImageSize };

            final ImageTypeSpecifier its = new ImageTypeSpecifier(colorModel, sampleModel);
            //
            // final ImageInputStream tiledImageInputStream;
            // tiledImageInputStream = new ArcSDETiledImageInputStream(tileReader);
            // raw = new RawImageInputStream(tiledImageInputStream, its, imageOffsets,
            // imageDimensions);
            // ImageReaderSpi imageIOSPI = new RawImageReaderSpi();
            // readerInstance = imageIOSPI.createReaderInstance();

            // final InputStream is = new RasterInputStream(tileReader);
            // final ImageInputStream tiledImageInputStream;
            // tiledImageInputStream = ImageIO.createImageInputStream(is);
            //
            // raw = new RawImageInputStream(tiledImageInputStream, its, imageOffsets,
            // imageDimensions);
            // ImageReaderSpi imageIOSPI = new RawImageReaderSpi();
            // readerInstance = imageIOSPI.createReaderInstance();

            ArcSDEImageReader reader = new ArcSDEImageReader(its);
            reader.setInput(tileReader, true, true);

            RenderedImage image = reader.readAsRenderedImage(0, null);
            // image.getData();
            return image;
        }

        // First operator: read the image
        // final RenderingHints hints = new RenderingHints(JAI.KEY_IMAGE_LAYOUT, imageLayout);
        //
        // ParameterBlock pb = new ParameterBlock();
        // pb.add(raw);// Input
        // /*
        // * image index, always 0 since we're already fetching the required pyramid level
        // */
        // pb.add(Integer.valueOf(0)); // Image index
        // pb.add(Boolean.FALSE); // Read metadata
        // pb.add(Boolean.FALSE);// Read thumbnails
        // pb.add(Boolean.FALSE);// Verify input
        // pb.add(null);// Listeners
        // pb.add(null);// Locale
        // final ImageReadParam rParam = new ImageReadParam();
        // pb.add(rParam);// ReadParam
        // pb.add(readerInstance);// Reader
        //
        // RenderedImage image = JAI.create("ImageRead", pb, hints);
        // // image.getData();
        // // // translate
        // // int minX = (matchingTiles.x * tileWidth);
        // // int minY = (matchingTiles.y * tileHeight);
        // // pb = new ParameterBlock();
        // // pb.addSource(image);
        // // pb.add(Float.valueOf(minX));
        // // pb.add(Float.valueOf(minY));
        // // pb.add(null);
        // //
        // // image = JAI.create("translate", pb);
        //
        // return image;

    }

}
