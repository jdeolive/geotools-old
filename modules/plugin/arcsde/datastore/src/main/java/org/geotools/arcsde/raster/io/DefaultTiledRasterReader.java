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
import java.awt.RenderingHints;
import java.awt.image.ColorModel;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.awt.image.renderable.ParameterBlock;
import java.io.IOException;
import java.util.logging.Logger;

import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.stream.ImageInputStream;
import javax.media.jai.ImageLayout;
import javax.media.jai.JAI;

import org.geotools.arcsde.raster.info.RasterDatasetInfo;
import org.geotools.arcsde.raster.jai.ArcSDEImageReader;
import org.geotools.arcsde.raster.jai.ArcSDEPlanarImage;
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

        // rasterImage.getData();

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

        fullTilesRaster = createTiledRasterNew2(tileReader, matchingTiles, rasterId);

        return fullTilesRaster;
    }

    /**
     * @param tileReader
     * @param matchingTiles
     * @param rasterId
     * @return
     * @throws IOException
     */
    private RenderedImage createTiledRasterOld(final TileReader tileReader,
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
    }

    /**
     * Creates an image representing the whole pyramid level but with a tile reader ready to read
     * only the required tiles, and returns a crop over it
     * 
     * @param tileReader
     * @param matchingTiles
     * @param rasterId
     * @return
     * @throws IOException
     */
    private RenderedImage createTiledRasterNew(final TileReader tileReader,
            final Rectangle matchingTiles, final long rasterId) throws IOException {
        // Prepare temporary colorModel and sample model, needed to build the final
        // ArcSDEPyramidLevel level;
        final ColorModel colorModel;
        final SampleModel sampleModel;
        final int tileWidth = rasterInfo.getTileWidth(rasterId);
        final int tileHeight = rasterInfo.getTileHeight(rasterId);
        final int rasterIndex = rasterInfo.getRasterIndex(rasterId);
        final int pyramidLevel = tileReader.getPyramidLevel();
        final int numTilesWide = rasterInfo.getNumTilesWide(rasterIndex, pyramidLevel);
        final int numTilesHigh = rasterInfo.getNumTilesHigh(rasterIndex, pyramidLevel);
        int tiledImageWidth = numTilesWide * tileWidth;
        int tiledImageHeight = numTilesHigh * tileHeight;
        {
            final ImageTypeSpecifier fullImageSpec = rasterInfo.getRenderedImageSpec(rasterId);
            colorModel = fullImageSpec.getColorModel();

            // sampleModel = fullImageSpec.getSampleModel(tiledImageWidth, tiledImageHeight);
            sampleModel = fullImageSpec.getSampleModel().createCompatibleSampleModel(tileWidth,
                    tileHeight);
        }

        {
            // final ImageTypeSpecifier its = new ImageTypeSpecifier(colorModel, sampleModel);
            // ArcSDEImageReader reader = new ArcSDEImageReader(its);
            // reader.setInput(tileReader, true, true);
            //
            // RenderedImage image = reader.readAsRenderedImage(0, null);

            RenderedImage image;
            {
                final Rectangle gridRange = rasterInfo.getGridRange(rasterIndex, pyramidLevel);
                int minX = 0;// gridRange.x;
                int minY = 0;// gridRange.y;
                int width = tiledImageWidth;// gridRange.width;
                int height = tiledImageHeight;// gridRange.height;
                int tileGridXOffset = 0;
                int tileGridYOffset = 0;
                SampleModel tileSampleModel = sampleModel;

                // image = new ArcSDETiledImage(tileReader, minX, minY, width, height,
                // tileGridXOffset, tileGridYOffset, tileSampleModel, colorModel);

                image = new ArcSDEPlanarImage(tileReader, minX, minY, width, height,
                        tileGridXOffset, tileGridYOffset, tileSampleModel, colorModel);
            }

            /*
             * Now crop it to the actual tiles subset
             */
            ParameterBlock cropParams = new ParameterBlock();
            // int minX = (matchingTiles.x * tileWidth);
            // int minY = (matchingTiles.y * tileHeight);
            //
            // int width = tiledImageSize.width;
            // int height = tiledImageSize.height;

            Float xOrigin = Float.valueOf(matchingTiles.x * tileWidth);
            Float yOrigin = Float.valueOf(matchingTiles.y * tileHeight);
            Float width = Float.valueOf(matchingTiles.width * tileWidth);
            Float height = Float.valueOf(matchingTiles.height * tileHeight);

            cropParams.addSource(image);// Source
            cropParams.add(xOrigin); // x origin for each band
            cropParams.add(yOrigin); // y origin for each band
            cropParams.add(width);// width for each band
            cropParams.add(height);// height for each band

            // ImageLayout imageLayout = new ImageLayout(0, 0, width.intValue(), height.intValue(),
            // 0,
            // 0, tileWidth, tileHeight, sampleModel, colorModel);

            final RenderingHints hints = null;
            image = JAI.create("Crop", cropParams, hints);

            return image;
        }
    }

    private RenderedImage createTiledRasterNew2(final TileReader tileReader,
            final Rectangle matchingTiles, final long rasterId) throws IOException {

        final ColorModel colorModel;
        final SampleModel sampleModel;
        final int tileWidth = rasterInfo.getTileWidth(rasterId);
        final int tileHeight = rasterInfo.getTileHeight(rasterId);
        {
            final ImageTypeSpecifier fullImageSpec = rasterInfo.getRenderedImageSpec(rasterId);
            colorModel = fullImageSpec.getColorModel();

            // sampleModel = fullImageSpec.getSampleModel(tiledImageWidth, tiledImageHeight);
            sampleModel = fullImageSpec.getSampleModel().createCompatibleSampleModel(tileWidth,
                    tileHeight);
        }

        RenderedImage image;
        {
            final int numTilesWide = matchingTiles.width;
            final int numTilesHigh = matchingTiles.height;
            final int tiledImageWidth = numTilesWide * tileWidth;
            final int tiledImageHeight = numTilesHigh * tileHeight;
            int minX = 0;// gridRange.x;
            int minY = 0;// gridRange.y;
            int tileGridXOffset = 0;
            int tileGridYOffset = 0;
            SampleModel tileSampleModel = sampleModel;

            image = new ArcSDEPlanarImage(tileReader, minX, minY, tiledImageWidth,
                    tiledImageHeight, tileGridXOffset, tileGridYOffset, tileSampleModel, colorModel);
        }

        //image.getData();
        return image;
    }
}
