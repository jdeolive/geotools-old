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

import static org.geotools.arcsde.gce.RasterCellType.TYPE_16BIT_S;
import static org.geotools.arcsde.gce.RasterCellType.TYPE_16BIT_U;
import static org.geotools.arcsde.gce.RasterCellType.TYPE_32BIT_S;
import static org.geotools.arcsde.gce.RasterCellType.TYPE_32BIT_U;
import static org.geotools.arcsde.gce.RasterCellType.TYPE_64BIT_REAL;
import static org.geotools.arcsde.gce.RasterCellType.TYPE_8BIT_U;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BandedSampleModel;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.IndexColorModel;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.imageio.ImageTypeSpecifier;

import org.geotools.coverage.GridSampleDimension;
import org.geotools.coverage.grid.GeneralGridEnvelope;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.OverviewPolicy;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.referencing.operation.builder.GridToEnvelopeMapper;
import org.geotools.resources.image.ColorUtilities;
import org.geotools.resources.image.ComponentColorModelJAI;
import org.geotools.util.NumberRange;
import org.geotools.util.logging.Logging;
import org.opengis.geometry.Envelope;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterNotFoundException;
import org.opengis.parameter.ParameterValue;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.datum.PixelInCell;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.NoninvertibleTransformException;
import org.opengis.referencing.operation.TransformException;

import com.sun.imageio.plugins.common.BogusColorSpace;

/**
 * 
 * @author Gabriel Roldan (OpenGeo)
 * @since 2.5.4
 * @version $Id$
 * @source $URL$
 */
@SuppressWarnings( { "nls" })
class RasterUtils {

    private static final Logger LOGGER = Logging.getLogger("org.geotools.arcsde.gce");

    private RasterUtils() {
        // do nothing
    }

    public static ReferencedEnvelope toReferencedEnvelope(GeneralEnvelope envelope) {
        double minx = envelope.getMinimum(0);
        double maxx = envelope.getMaximum(0);
        double miny = envelope.getMinimum(1);
        double maxy = envelope.getMaximum(1);
        CoordinateReferenceSystem crs = envelope.getCoordinateReferenceSystem();

        ReferencedEnvelope refEnv = new ReferencedEnvelope(minx, maxx, miny, maxy, crs);
        return refEnv;
    }

    public static ReferencedEnvelope toNativeCrs(final GeneralEnvelope requestedEnvelope,
            final CoordinateReferenceSystem nativeCRS) throws IllegalArgumentException {

        ReferencedEnvelope reqEnv = toReferencedEnvelope(requestedEnvelope);

        if (!CRS.equalsIgnoreMetadata(nativeCRS, reqEnv.getCoordinateReferenceSystem())) {
            // we're being reprojected. We'll need to reproject reqEnv into
            // our native coordsys
            try {
                // ReferencedEnvelope origReqEnv = reqEnv;
                reqEnv = reqEnv.transform(nativeCRS, true);
            } catch (FactoryException fe) {
                // unable to reproject?
                throw new IllegalArgumentException("Unable to find a reprojection from requested "
                        + "coordsys to native coordsys for this request", fe);
            } catch (TransformException te) {
                throw new IllegalArgumentException("Unable to perform reprojection from requested "
                        + "coordsys to native coordsys for this request", te);
            }
        }
        return reqEnv;
    }

    public static class QueryInfo {

        private GeneralEnvelope requestedEnvelope;

        private Rectangle requestedDim;

        private int pyramidLevel;

        /**
         * The two-dimensional range of tile indices whose envelope intersect the requested extent.
         * Will have negative width and height if none of the tiles do.
         */
        private Rectangle matchingTiles;

        private GeneralEnvelope resultEnvelope;

        private Rectangle resultDimension;

        private Long rasterId;

        private Rectangle mosaicLocation;

        private RenderedImage resultImage;

        private Rectangle tiledImageSize;

        private double[] resolution;

        private int rasterIndex;

        /**
         * The full tile range for the matching pyramid level
         */
        private Rectangle levelTileRange;

        public QueryInfo() {
            setResultDimensionInsideTiledImage(new Rectangle(0, 0, 0, 0));
            setMatchingTiles(new Rectangle(0, 0, 0, 0));
            setResultEnvelope(null);
        }

        @Override
        public String toString() {
            StringBuilder s = new StringBuilder("[Raster query info:");
            s.append("\n\tRaster ID            : ").append(getRasterId());
            s.append("\n\tPyramid level        : ").append(getPyramidLevel());
            s.append("\n\tResolution           : ").append(
                    getResolution()[0] + "," + getResolution()[1]);
            s.append("\n\tRequested envelope   : ").append(getRequestedEnvelope());
            s.append("\n\tRequested dimension  : ").append(getRequestedDim());
            Rectangle mt = getMatchingTiles();
            Rectangle ltr = getLevelTileRange();
            String matching = "x=" + mt.x + "-" + (mt.width - 1) + ", y=" + mt.y + "-"
                    + (mt.height - 1);
            String level = "x=" + ltr.x + "-" + (ltr.width - 1) + ", y=" + ltr.y + "-"
                    + (ltr.height - 1);
            s.append("\n\tMatching tiles       : ").append(matching).append(" out of ").append(
                    level);
            s.append("\n\tTiled image size     : ").append(getTiledImageSize());
            s.append("\n\tResult dimension     : ").append(getResultDimensionInsideTiledImage());
            s.append("\n\tMosaiced dimension   : ").append(getMosaicLocation());
            s.append("\n\tResult envelope      : ").append(getResultEnvelope());
            s.append("\n]");
            return s.toString();
        }

        /**
         * @return the rasterId (as in SeRaster.getId()) for the raster in the raster dataset this
         *         query works upon
         */
        public Long getRasterId() {
            return rasterId;
        }

        public GeneralEnvelope getRequestedEnvelope() {
            return requestedEnvelope;
        }

        public Rectangle getRequestedDim() {
            return requestedDim;
        }

        public int getPyramidLevel() {
            return pyramidLevel;
        }

        public Rectangle getMatchingTiles() {
            return matchingTiles;
        }

        public GeneralEnvelope getResultEnvelope() {
            return resultEnvelope;
        }

        public Rectangle getResultDimensionInsideTiledImage() {
            return resultDimension;
        }

        void setRasterId(Long rasterId) {
            this.rasterId = rasterId;
        }

        void setPyramidLevel(int pyramidLevel) {
            this.pyramidLevel = pyramidLevel;
        }

        void setRequestedEnvelope(GeneralEnvelope requestedEnvelope) {
            this.requestedEnvelope = requestedEnvelope;
        }

        void setRequestedDim(Rectangle requestedDim) {
            this.requestedDim = requestedDim;
        }

        void setResultEnvelope(GeneralEnvelope resultEnvelope) {
            this.resultEnvelope = resultEnvelope;
        }

        void setMatchingTiles(Rectangle matchingTiles) {
            this.matchingTiles = matchingTiles;
        }

        void setResultDimensionInsideTiledImage(Rectangle resultDimension) {
            this.resultDimension = resultDimension;
        }

        void setMosaicLocation(Rectangle rasterMosaicLocation) {
            this.mosaicLocation = rasterMosaicLocation;
        }

        public Rectangle getMosaicLocation() {
            return mosaicLocation;
        }

        public void setResultImage(RenderedImage rasterImage) {
            this.resultImage = rasterImage;
            if (rasterImage.getWidth() != tiledImageSize.width
                    || rasterImage.getHeight() != tiledImageSize.height) {
                LOGGER.warning("Result image and expected dimensions don't match: image="
                        + resultImage.getWidth() + "x" + resultImage.getHeight() + ", expected="
                        + tiledImageSize.width + "x" + tiledImageSize.height);
            }
        }

        public RenderedImage getResultImage() {
            return resultImage;
        }

        void setTiledImageSize(Rectangle tiledImageSize) {
            this.tiledImageSize = tiledImageSize;
        }

        public Rectangle getTiledImageSize() {
            return tiledImageSize;
        }

        void setResolution(double[] resolution) {
            this.resolution = resolution;
        }

        public double[] getResolution() {
            return resolution == null ? new double[] { -1, -1 } : resolution;
        }

        void setRasterIndex(int rasterN) {
            this.rasterIndex = rasterN;
        }

        public int getRasterIndex() {
            return rasterIndex;
        }

        void setLevelTileRange(Rectangle levelTileRange) {
            this.levelTileRange = levelTileRange;
        }

        public Rectangle getLevelTileRange() {
            return levelTileRange;
        }
    }

    public static MathTransform createRasterToModel(final Rectangle levelGridRange,
            final GeneralEnvelope levelEnvelope) {
        // create a raster to model transform, from this tile pixel space to the tile's geographic
        // extent
        GeneralGridEnvelope gridRange = new GeneralGridEnvelope(levelGridRange, 2);
        GridToEnvelopeMapper geMapper = new GridToEnvelopeMapper(gridRange, levelEnvelope);
        geMapper.setPixelAnchor(PixelInCell.CELL_CORNER);

        final MathTransform rasterToModel = geMapper.createTransform();
        return rasterToModel;
    }

    private static Rectangle getResultDimensionForTileRange(final Rectangle tiledImageGridRange,
            final Rectangle matchingLevelRange) {

        int minx = Math.max(tiledImageGridRange.x, matchingLevelRange.x);
        int miny = Math.max(tiledImageGridRange.y, matchingLevelRange.y);
        int maxx = (int) Math.min(tiledImageGridRange.getMaxX(), matchingLevelRange.getMaxX());
        int maxy = (int) Math.min(tiledImageGridRange.getMaxY(), matchingLevelRange.getMaxY());

        return new Rectangle(minx, miny, maxx - minx, maxy - miny);
    }

    /**
     * Returns the rectangle specifying the matching tiles for a given pyramid level and rectangle
     * specifying the overlapping area to request in the level's pixel space.
     * 
     * @param pixelRange
     * @param tilesHigh
     * @param tilesWide
     * @param tileSize
     * @param numTilesHigh
     * @param numTilesWide
     * 
     * @param pixelRange
     * @param level
     * 
     * @return a rectangle holding the coordinates in tile space that fully covers the requested
     *         pixel range for the given pyramid level, or a negative area rectangle
     */
    private static Rectangle findMatchingTiles(final Dimension tileSize, int numTilesWide,
            int numTilesHigh, final Rectangle pixelRange) {

        final int minPixelX = pixelRange.x;
        final int minPixelY = pixelRange.y;

        // TODO: WARNING, we're not considering the possible x/y offsets on the level range for the
        // given pyramid level here!

        int minTileX = (int) Math.floor(minPixelX / tileSize.getWidth());
        int minTileY = (int) Math.floor(minPixelY / tileSize.getHeight());

        int numTilesX = (int) Math.ceil(pixelRange.getWidth() / tileSize.getWidth());
        int numTilesY = (int) Math.ceil(pixelRange.getHeight() / tileSize.getHeight());

        int maxTiledX = (minTileX + numTilesX) * tileSize.width;
        int maxTiledY = (minTileY + numTilesY) * tileSize.height;

        if (maxTiledX < pixelRange.getMaxX() && (minTileX + numTilesX) < numTilesWide) {
            numTilesX++;
        }

        if (maxTiledY < pixelRange.getMaxY() && (minTileY + numTilesY) < numTilesHigh) {
            numTilesY++;
        }

        Rectangle matchingTiles = new Rectangle(minTileX, minTileY, numTilesX, numTilesY);
        return matchingTiles;
    }

    private static Rectangle getTargetGridRange(final MathTransform modelToRaster,
            final Envelope requestedEnvelope) {
        Rectangle levelOverlappingPixels;
        int levelMinPixelX;
        int levelMaxPixelX;
        int levelMinPixelY;
        int levelMaxPixelY;
        {
            // use a model to raster transform to find out which pixel range at the specified level
            // better match the requested extent
            GeneralEnvelope requestedPixels;
            try {
                requestedPixels = CRS.transform(modelToRaster, requestedEnvelope);
            } catch (NoninvertibleTransformException e) {
                throw new IllegalArgumentException(e);
            } catch (TransformException e) {
                throw new IllegalArgumentException(e);
            }

            levelMinPixelX = (int) Math.floor(requestedPixels.getMinimum(0));
            levelMaxPixelX = (int) Math.floor(requestedPixels.getMaximum(0));

            levelMinPixelY = (int) Math.ceil(requestedPixels.getMinimum(1));
            levelMaxPixelY = (int) Math.ceil(requestedPixels.getMaximum(1));

            final int width = levelMaxPixelX - levelMinPixelX;
            final int height = levelMaxPixelY - levelMinPixelY;
            levelOverlappingPixels = new Rectangle(levelMinPixelX, levelMinPixelY, width, height);
        }
        return levelOverlappingPixels;
    }

    /**
     * Creates an IndexColorModel out of a DataBuffer obtained from an ArcSDE's raster color map.
     * 
     * @param colorMapData
     * @return
     */
    public static IndexColorModel sdeColorMapToJavaColorModel(final DataBuffer colorMapData,
            final int bitsPerSample) {
        if (colorMapData == null) {
            throw new NullPointerException("colorMapData");
        }

        if (colorMapData.getNumBanks() < 3 || colorMapData.getNumBanks() > 4) {
            throw new IllegalArgumentException("colorMapData shall have 3 or 4 banks: "
                    + colorMapData.getNumBanks());
        }

        if (bitsPerSample != 8 && bitsPerSample != 16) {
            throw new IllegalAccessError("bits per sample shall be either 8 or 16. Got "
                    + bitsPerSample);
        }

        final int transferType = colorMapData.getDataType();
        final int numBanks = colorMapData.getNumBanks();
        final int mapSize = colorMapData.getSize();
        final int maxMapSize = DataBuffer.TYPE_USHORT == transferType ? 65536 : 256;

        byte[] r = new byte[maxMapSize];
        byte[] g = new byte[maxMapSize];
        byte[] b = new byte[maxMapSize];
        byte[] a = new byte[maxMapSize];

        for (int i = 0; i < mapSize; i++) {
            r[i] = (byte) (colorMapData.getElem(0, i) & 0xFF);
            g[i] = (byte) (colorMapData.getElem(1, i) & 0xFF);
            b[i] = (byte) (colorMapData.getElem(2, i) & 0xFF);
            a[i] = (byte) (numBanks == 3 ? 255 : colorMapData.getElem(3, i));
        }

        IndexColorModel colorModel = new IndexColorModel(bitsPerSample, mapSize, r, g, b, a);

        return colorModel;
    }

    public static ImageTypeSpecifier createFullImageTypeSpecifier(
            final RasterDatasetInfo rasterInfo, final int rasterIndex) {

        final int numberOfBands = rasterInfo.getNumBands();
        final RasterCellType pixelType = rasterInfo.getTargetCellType(rasterIndex);

        // Prepare temporary colorModel and sample model, needed to build the final
        // ArcSDEPyramidLevel level;
        int sampleImageWidth = 1;// rasterInfo.getImageWidth();
        int sampleImageHeight = 1;// rasterInfo.getImageHeight();

        final ImageTypeSpecifier its;
        // treat special cases...
        final int bitsPerSample = pixelType.getBitsPerSample();
        final int dataType = pixelType.getDataBufferType();
        final boolean hasColorMap = rasterInfo.isColorMapped();

        if (hasColorMap) {
            // special case, a single band colormapped imaged
            its = createColorMappedImageSpec(rasterInfo, rasterIndex, sampleImageWidth,
                    sampleImageHeight);

        } else if (bitsPerSample == 1 || bitsPerSample == 4) {
            // special case, a single band 1-bit or 4-bit image
            its = createOneOrFoutBitImageSpec(rasterInfo, numberOfBands, sampleImageWidth,
                    sampleImageHeight, bitsPerSample, dataType);

        } else if (numberOfBands == 1) {
            // special case, a single band grayscale image, no matter the pixel depth
            its = createGrayscaleImageSpec(sampleImageWidth, sampleImageHeight, dataType,
                    bitsPerSample);

        } else if (numberOfBands == 3 && pixelType == TYPE_8BIT_U) {
            // special case, an optimizable RGB image
            its = createRGBImageSpec(sampleImageWidth, sampleImageHeight, dataType);

        } else if (numberOfBands == 4 && pixelType == TYPE_8BIT_U) {
            // special case, an optimizable RGBA image
            its = createRGBAImageSpec(sampleImageWidth, sampleImageHeight, dataType);

        } else {
            /*
             * not an special case, go for a more generic sample model, potentially slower than the
             * special case ones, but that'll work anyway
             */

            final ColorModel colorModel;
            final SampleModel sampleModel;
            {
                final ColorSpace colorSpace;
                colorSpace = new BogusColorSpace(numberOfBands);
                int[] numBits = new int[numberOfBands];
                for (int i = 0; i < numberOfBands; i++) {
                    numBits[i] = bitsPerSample;
                }
                colorModel = new ComponentColorModelJAI(colorSpace, numBits, false, false,
                        Transparency.OPAQUE, dataType);
            }
            {
                int[] bankIndices = new int[numberOfBands];
                int[] bandOffsets = new int[numberOfBands];
                // int bandOffset = (tileWidth * tileHeight * pixelType.getBitsPerSample()) / 8;
                for (int i = 0; i < numberOfBands; i++) {
                    bankIndices[i] = i;
                    bandOffsets[i] = 0;// (i * bandOffset);
                }
                sampleModel = new BandedSampleModel(dataType, sampleImageWidth, sampleImageHeight,
                        sampleImageWidth, bankIndices, bandOffsets);
            }
            its = new ImageTypeSpecifier(colorModel, sampleModel);
        }

        return its;
    }

    private static ImageTypeSpecifier createRGBAImageSpec(int sampleImageWidth,
            int sampleImageHeight, final int dataType) {

        final ImageTypeSpecifier its;

        ColorSpace colorSpace = ColorSpace.getInstance(ColorSpace.CS_sRGB);
        boolean hasAlpha = true;
        boolean isAlphaPremultiplied = false;
        int transparency = Transparency.TRANSLUCENT;
        int transferType = dataType;

        int[] nBits = { 8, 8, 8, 8 };
        ColorModel colorModel = new ComponentColorModelJAI(colorSpace, nBits, hasAlpha,
                isAlphaPremultiplied, transparency, transferType);

        /*
         * Do not use colorModel.createCompatibleSampleModel cause it creates a
         * PixelInterleavedSampleModel and we need a BandedSampleModel so it matches how the data
         * comes out of ArcSDE
         */
        SampleModel sampleModel = new BandedSampleModel(dataType, sampleImageWidth,
                sampleImageHeight, 4);

        its = new ImageTypeSpecifier(colorModel, sampleModel);
        return its;
    }

    private static ImageTypeSpecifier createRGBImageSpec(int sampleImageWidth,
            int sampleImageHeight, final int dataType) {

        final ImageTypeSpecifier its;
        ColorSpace colorSpace = ColorSpace.getInstance(ColorSpace.CS_sRGB);
        boolean hasAlpha = false;
        boolean isAlphaPremultiplied = false;
        int transparency = Transparency.OPAQUE;
        int transferType = dataType;
        ColorModel colorModel = new ComponentColorModel(colorSpace, hasAlpha, isAlphaPremultiplied,
                transparency, transferType);

        SampleModel sampleModel = new BandedSampleModel(dataType, sampleImageWidth,
                sampleImageHeight, 3);

        its = new ImageTypeSpecifier(colorModel, sampleModel);
        return its;
    }

    private static ImageTypeSpecifier createGrayscaleImageSpec(int sampleImageWidth,
            int sampleImageHeight, final int dataType, int bitsPerPixel) {
        final ImageTypeSpecifier its;
        ColorSpace colorSpace = ColorSpace.getInstance(ColorSpace.CS_GRAY);
        boolean hasAlpha = false;
        boolean isAlphaPremultiplied = false;
        int transparency = Transparency.OPAQUE;
        int transferType = dataType;
        int[] nbits = { bitsPerPixel };
        ColorModel colorModel = new ComponentColorModelJAI(colorSpace, nbits, hasAlpha,
                isAlphaPremultiplied, transparency, transferType);

        SampleModel sampleModel = colorModel.createCompatibleSampleModel(sampleImageWidth,
                sampleImageHeight);
        its = new ImageTypeSpecifier(colorModel, sampleModel);
        return its;
    }

    private static ImageTypeSpecifier createOneOrFoutBitImageSpec(
            final RasterDatasetInfo rasterInfo, final int numberOfBands, int sampleImageWidth,
            int sampleImageHeight, final int bitsPerSample, final int dataType) {
        final ColorModel colorModel;
        final SampleModel sampleModel;
        if (numberOfBands != 1) {
            throw new IllegalArgumentException(bitsPerSample
                    + "-Bit rasters are only supported for one band");
        }
        int[] argb = new int[(int) Math.pow(2, bitsPerSample)];
        ColorUtilities.expand(new Color[] { Color.WHITE, Color.BLACK }, argb, 0, argb.length);
        GridSampleDimension gridSampleDimension = rasterInfo.getGridSampleDimensions()[0];
        colorModel = gridSampleDimension.getColorModel(0, numberOfBands, dataType);
        sampleModel = colorModel.createCompatibleSampleModel(sampleImageWidth, sampleImageHeight);

        ImageTypeSpecifier its = new ImageTypeSpecifier(colorModel, sampleModel);
        return its;
    }

    private static ImageTypeSpecifier createColorMappedImageSpec(
            final RasterDatasetInfo rasterInfo, final int rasterIndex, int sampleImageWidth,
            int sampleImageHeight) {

        final ColorModel colorModel;
        final SampleModel sampleModel;
        final ImageTypeSpecifier its;
        LOGGER.fine("Found single-band colormapped raster, using its index color model");
        colorModel = rasterInfo.getColorMap(rasterIndex);
        sampleModel = colorModel.createCompatibleSampleModel(sampleImageWidth, sampleImageHeight);
        its = new ImageTypeSpecifier(colorModel, sampleModel);
        return its;

    }

    public static ArcSDEGridCoverage2DReaderJAI.ReadParameters parseReadParams(
            final GeneralEnvelope coverageEnvelope, final GeneralParameterValue[] params)
            throws IllegalArgumentException {
        if (params == null) {
            throw new IllegalArgumentException("No GeneralParameterValue given to read operation");
        }

        GeneralEnvelope reqEnvelope = null;
        Rectangle dim = null;
        OverviewPolicy overviewPolicy = null;

        // /////////////////////////////////////////////////////////////////////
        //
        // Checking params
        //
        // /////////////////////////////////////////////////////////////////////
        for (int i = 0; i < params.length; i++) {
            final ParameterValue<?> param = (ParameterValue<?>) params[i];
            final String name = param.getDescriptor().getName().getCode();
            if (name.equals(AbstractGridFormat.READ_GRIDGEOMETRY2D.getName().toString())) {
                final GridGeometry2D gg = (GridGeometry2D) param.getValue();
                reqEnvelope = new GeneralEnvelope((Envelope) gg.getEnvelope2D());

                CoordinateReferenceSystem nativeCrs = coverageEnvelope
                        .getCoordinateReferenceSystem();
                CoordinateReferenceSystem requestCrs = reqEnvelope.getCoordinateReferenceSystem();
                if (!CRS.equalsIgnoreMetadata(nativeCrs, requestCrs)) {
                    LOGGER.info("Request CRS and native CRS differ, "
                            + "reprojecting request envelope to native CRS");
                    ReferencedEnvelope nativeCrsEnv;
                    nativeCrsEnv = toNativeCrs(reqEnvelope, nativeCrs);
                    reqEnvelope = new GeneralEnvelope(nativeCrsEnv);
                }

                dim = gg.getGridRange2D().getBounds();
                continue;
            }
            if (name.equals(AbstractGridFormat.OVERVIEW_POLICY.getName().toString())) {
                overviewPolicy = (OverviewPolicy) param.getValue();
                continue;
            }
        }

        if (dim == null && reqEnvelope == null) {
            throw new ParameterNotFoundException("Parameter is mandatory and shall provide "
                    + "the extent and dimension to request", AbstractGridFormat.READ_GRIDGEOMETRY2D
                    .getName().toString());
        }

        if (!reqEnvelope.intersects(coverageEnvelope, true)) {
            throw new IllegalArgumentException(
                    "The requested extend does not overlap the coverage extent: "
                            + coverageEnvelope);
        }

        if (dim.width <= 0 || dim.height <= 0) {
            throw new IllegalArgumentException("The requested coverage dimension can't be null: "
                    + dim);
        }

        if (overviewPolicy == null) {
            LOGGER.finer("No overview policy requested, defaulting to QUALITY");
            overviewPolicy = OverviewPolicy.QUALITY;
        }
        LOGGER.fine("Overview policy is " + overviewPolicy);

        LOGGER.info("Reading raster for " + dim.getWidth() + "x" + dim.getHeight()
                + " requested dim and " + reqEnvelope.getMinimum(0) + ","
                + reqEnvelope.getMaximum(0) + " - " + reqEnvelope.getMinimum(1)
                + reqEnvelope.getMaximum(1) + " requested extent");

        ArcSDEGridCoverage2DReaderJAI.ReadParameters parsedParams = new ArcSDEGridCoverage2DReaderJAI.ReadParameters();
        parsedParams.requestedEnvelope = reqEnvelope;
        parsedParams.dim = dim;
        parsedParams.overviewPolicy = overviewPolicy;
        return parsedParams;
    }

    /**
     * Given a collection of {@link QueryInfo} instances holding information about how a request
     * fits for each individual raster composing a catalog, figure out where their resulting images
     * fit into the overall mosaic that's gonna be the result of the request.
     * 
     * @param rasterInfo
     * @param resultEnvelope
     * @param results
     * @return
     */
    public static Rectangle setMosaicLocations(final RasterDatasetInfo rasterInfo,
            final GeneralEnvelope resultEnvelope, final List<QueryInfo> results) {
        final Rectangle mosaicDimension;
        final MathTransform modelToRaster;
        final MathTransform rasterToModel;
        {
            /*
             * Of all the rasters that match the requested envelope, chose the one with the lowest
             * resolution as the base to compute the final mosaic layout, so we avoid JAI upsamples,
             * which are buggy and produce repeated patterns over the x axis instead of just scaling
             * up the image.
             */
            QueryInfo dimensionChoice = findLowestResolution(results);
            Long rasterId = dimensionChoice.getRasterId();
            int pyramidLevel = dimensionChoice.getPyramidLevel();
            int rasterIndex = rasterInfo.getRasterIndex(rasterId);
            Rectangle levelRange = rasterInfo.getGridRange(rasterIndex, pyramidLevel);
            GeneralEnvelope levelEnvelope = rasterInfo.getGridEnvelope(rasterIndex, pyramidLevel);
            rasterToModel = createRasterToModel(levelRange, levelEnvelope);
            try {
                modelToRaster = rasterToModel.inverse();
            } catch (NoninvertibleTransformException e) {
                throw new RuntimeException(e);
            }
            mosaicDimension = getTargetGridRange(modelToRaster, resultEnvelope);
        }

        for (QueryInfo rasterResultInfo : results) {
            final GeneralEnvelope rasterResultEnvelope = rasterResultInfo.getResultEnvelope();

            final Rectangle targetRasterGridRange;
            targetRasterGridRange = getTargetGridRange(modelToRaster, rasterResultEnvelope);

            rasterResultInfo.setMosaicLocation(targetRasterGridRange);
        }

        return mosaicDimension;
    }

    private static QueryInfo findLowestResolution(List<QueryInfo> results) {
        double[] prev = { Double.MIN_VALUE, Double.MIN_VALUE };
        QueryInfo lowestResQuery = null;

        double[] curr;
        for (QueryInfo query : results) {
            curr = query.getResolution();
            if (curr[0] > prev[0]) {
                prev = curr;
                lowestResQuery = query;
            }
        }
        return lowestResQuery;
    }

    /**
     * Find out the raster ids and their pyramid levels in the raster dataset for the rasters whose
     * envelope overlaps the requested one
     * 
     * @param rasterInfo
     * @param requestedEnvelope
     * @param requestedDim
     * @param overviewPolicy
     * @return
     */
    public static List<QueryInfo> findMatchingRasters(final RasterDatasetInfo rasterInfo,
            final GeneralEnvelope requestedEnvelope, final Rectangle requestedDim,
            final OverviewPolicy overviewPolicy) {

        final int numRasters = rasterInfo.getNumRasters();
        List<QueryInfo> matchingRasters = new ArrayList<QueryInfo>(numRasters);

        int optimalPyramidLevel;
        GeneralEnvelope gridEnvelope;
        for (int rasterN = 0; rasterN < numRasters; rasterN++) {
            optimalPyramidLevel = rasterInfo.getOptimalPyramidLevel(rasterN, overviewPolicy,
                    requestedEnvelope, requestedDim);
            gridEnvelope = rasterInfo.getGridEnvelope(rasterN, optimalPyramidLevel);
            final boolean edgesInclusive = true;
            if (requestedEnvelope.intersects(gridEnvelope, edgesInclusive)) {
                QueryInfo match = new QueryInfo();
                match.setRequestedEnvelope(requestedEnvelope);
                match.setRequestedDim(requestedDim);

                match.setRasterId(rasterInfo.getRasterId(rasterN));
                match.setRasterIndex(rasterN);
                match.setPyramidLevel(optimalPyramidLevel);
                match.setResolution(rasterInfo.getResolution(rasterN, optimalPyramidLevel));
                matchingRasters.add(match);
            }
        }
        return matchingRasters;
    }

    public static void fitRequestToRaster(final GeneralEnvelope requestedEnvelope,
            final RasterDatasetInfo rasterInfo, final QueryInfo query) {

        final int rasterIndex = query.getRasterIndex();
        final int pyramidLevel = query.getPyramidLevel();
        final Rectangle rasterGridRange = rasterInfo.getGridRange(rasterIndex, pyramidLevel);
        final GeneralEnvelope rasterEnvelope = rasterInfo
                .getGridEnvelope(rasterIndex, pyramidLevel);

        double delta = requestedEnvelope.getMinimum(0) - rasterEnvelope.getMinimum(0);
        double resX = rasterInfo.getResolution(rasterIndex, pyramidLevel)[0];
        int xMinPixel = (int) Math.floor(delta / resX);

        delta = requestedEnvelope.getMaximum(0) - rasterEnvelope.getMinimum(0);
        int xMaxPixel = (int) Math.ceil(delta / resX);

        delta = rasterEnvelope.getMaximum(1) - requestedEnvelope.getMaximum(1);
        double resY = rasterInfo.getResolution(rasterIndex, pyramidLevel)[1];
        // Distance in pixels from the top of the whole pyramid image to the top
        // of our AOI.
        // If we're off the top, this number will be negative.
        int yMinPixel = (int) Math.floor(delta / resY);

        delta = rasterEnvelope.getMaximum(1) - requestedEnvelope.getMinimum(1);
        int yMaxPixel = (int) Math.ceil(delta / resY);

        xMinPixel = Math.max(xMinPixel, rasterGridRange.x);
        yMinPixel = Math.max(yMinPixel, rasterGridRange.y);
        xMaxPixel = Math.min(xMaxPixel, rasterGridRange.x + rasterGridRange.width);
        yMaxPixel = Math.min(yMaxPixel, rasterGridRange.y + rasterGridRange.height);

        final int widthPixel = xMaxPixel - xMinPixel;
        final int heightPixel = yMaxPixel - yMinPixel;

        final double xMinGeo = rasterEnvelope.getMinimum(0) + resX * xMinPixel;
        final double yMinGeo = rasterEnvelope.getMaximum(1) - resY * (yMinPixel + heightPixel);
        final double widthGeo = resX * widthPixel;
        final double heightGeo = resY * heightPixel;

        final Rectangle resultGridRange;
        final GeneralEnvelope resultEnvelope;

        resultEnvelope = new GeneralEnvelope(new double[] { xMinGeo, yMinGeo }, new double[] {
                xMinGeo + widthGeo, yMinGeo + heightGeo });
        resultEnvelope.setCoordinateReferenceSystem(rasterEnvelope.getCoordinateReferenceSystem());

        resultGridRange = new Rectangle(xMinPixel, yMinPixel, widthPixel, heightPixel);

        final Rectangle matchingTiles;
        final Rectangle levelTileRange;
        final Rectangle tiledImageGridRange;
        {
            final Dimension tileSize = rasterInfo.getTileDimension(rasterIndex);
            final int numTilesWide = rasterInfo.getNumTilesWide(rasterIndex, pyramidLevel);
            final int numTilesHigh = rasterInfo.getNumTilesHigh(rasterIndex, pyramidLevel);
            final Point tileOffset = rasterInfo.getTileOffset(rasterIndex, pyramidLevel);
            levelTileRange = new Rectangle(0, 0, numTilesWide, numTilesHigh);
            matchingTiles = findMatchingTiles(tileSize, numTilesWide, numTilesHigh, resultGridRange);

            int tiledImageMinX = (matchingTiles.x * tileSize.width);
            int tiledImageMinY = (matchingTiles.y * tileSize.height);

            int tiledWidth = (matchingTiles.width * tileSize.width);
            int tiledHeight = (matchingTiles.height * tileSize.height);

            tiledImageGridRange = new Rectangle(tiledImageMinX, tiledImageMinY, tiledWidth,
                    tiledHeight);
        }

        /*
         * What is the grid range inside the whole level grid range that fits into the matching
         * tiles
         */
        Rectangle resultDimensionInsideTiledImage;
        resultDimensionInsideTiledImage = getResultDimensionForTileRange(tiledImageGridRange,
                resultGridRange);

        query.setResultEnvelope(resultEnvelope);
        query.setResultDimensionInsideTiledImage(resultDimensionInsideTiledImage);
        query.setTiledImageSize(tiledImageGridRange);
        query.setLevelTileRange(levelTileRange);
        query.setMatchingTiles(matchingTiles);
    }

    /**
     * Returns a color model based on {@code colorMap} that's guaranteed to have at least one
     * transparent pixel whose index can be used as no-data value for colormapped rasters, even if
     * the returned IndexColorModel needs to be of a higher sample depth (ie, 16 instead of 8 bit)
     * to satisfy that.
     * 
     * @param colorMap
     *            the raster's native color map the returned one will be based on
     * @return the same {@code colorMap} if it has a transparent pixel, another, possibly of a
     *         higher depth one if not, containing all the colors from {@code colorMap} and a newly
     *         allocated cell for the transparent pixel if necessary
     */
    public static IndexColorModel ensureNoDataPixelIsAvailable(final IndexColorModel colorMap) {
        int transparentPixel = colorMap.getTransparentPixel();
        if (transparentPixel > -1) {
            return colorMap;
        }

        final int transferType = colorMap.getTransferType();
        final int mapSize = colorMap.getMapSize();
        final int maxSize = 65536;// true for either transfer type

        if (mapSize == maxSize) {
            LOGGER.fine("There's no room for a new transparent pixel, "
                    + "returning the original colorMap as is");
            return colorMap;
        }

        /*
         * The original map size is lower than the maximum allowed by a UShort color map, so expand
         * the colormap by one and make that new entry transparent
         */
        final int newMapSize = mapSize + 1;
        final int[] argb = new int[newMapSize];
        colorMap.getRGBs(argb);

        // set the last entry as transparent
        argb[newMapSize - 1] = ColorUtilities.getIntFromColor(0, 0, 0, 0);

        IndexColorModel targetColorModel;
        final int significantBits;
        final int newTransferType;

        {
            if (DataBuffer.TYPE_BYTE == transferType && newMapSize <= 256) {
                /*
                 * REVISIT: check if this needs to be promoted depending on whether I decide to
                 * treat 1 and 4 bit images as indexed with 1 and 4 significant bits respectively
                 */
                significantBits = colorMap.getPixelSize();
                newTransferType = DataBuffer.TYPE_BYTE;
            } else {
                // it's either being promoted or was already 16-bit
                significantBits = 16;
                newTransferType = DataBuffer.TYPE_USHORT;
            }
        }

        final int transparentPixelIndex = newMapSize - 1;
        final boolean hasalpha = true;
        final int startIndex = 0;

        targetColorModel = new IndexColorModel(significantBits, newMapSize, argb, startIndex,
                hasalpha, transparentPixelIndex, newTransferType);

        return targetColorModel;
    }

    /**
     * For a color-mapped raster, the no-data value is set to the
     * {@link IndexColorModel#getTransparentPixel() transparent pixel}
     * 
     * @param colorMap
     * @return the index in the colorMap that's the transparent pixel as is to be used as no-data
     *         value
     */
    public static Number determineNoDataValue(IndexColorModel colorMap) {
        int noDataPixel = colorMap.getTransparentPixel();
        if (-1 == noDataPixel) {
            // there were no room for a transparent pixel, find out the closest match
            noDataPixel = ColorUtilities.getTransparentPixel(colorMap);
        }
        return Integer.valueOf(noDataPixel);
    }

    /**
     * 
     * @param statsMin
     *            the minimum sample value for the band as reported by the band's statistics, or
     *            {@code NaN}
     * @param statsMax
     *            the maximum sample value for the band as reported by the band's statistics, or
     *            {@code NaN}
     * @param cellType
     *            the band's native cell type
     * @return
     */
    public static Number determineNoDataValue(final double statsMin, final double statsMax,
            final RasterCellType cellType) {

        final Number nodata;

        final NumberRange<?> sampleValueRange = cellType.getSampleValueRange();

        double lower;
        double upper;
        if (Double.isNaN(statsMin) || Double.isNaN(statsMax)) {
            // no way to know, there's no statistics generated, so we need to promote just to be
            // safe
            if (cellType.getBitsPerSample() == 64) {
                // can't promote a double to a higher depth
                nodata = Double.valueOf(Double.MAX_VALUE);
                return nodata;
            }
            lower = Math.ceil(sampleValueRange.getMinimum(true) - 1);
            upper = Math.floor(sampleValueRange.getMaximum(true) + 1);
        } else {
            lower = Math.ceil(statsMin - 1);
            upper = Math.floor(statsMax + 1);
        }

        if (sampleValueRange.contains((Number) Double.valueOf(lower))) {
            // lower is ok
            nodata = lower;
        } else if (sampleValueRange.contains((Number) Double.valueOf(upper))) {
            // upper is ok
            nodata = upper;
        } else if (sampleValueRange.getMinimum(true) == 0) {
            // need to set no-data to the higher value, floor is zero
            nodata = upper;
        } else {
            // no-data as the lower value is ok, floor is non zero (the celltype is signed)
            nodata = lower;
        }

        return nodata;
    }

    public static RasterCellType determineTargetCellType(final RasterDatasetInfo info,
            final int rasterIndex) {

        if (info.isColorMapped()) {
            final IndexColorModel targetColorMap = info.getColorMap(rasterIndex);
            final int transferType = targetColorMap.getTransferType();
            switch (transferType) {
            case DataBuffer.TYPE_BYTE:
                return RasterCellType.TYPE_8BIT_U;
            case DataBuffer.TYPE_USHORT:
                return RasterCellType.TYPE_16BIT_U;
            default:
                throw new IllegalArgumentException("DataBuffer transfer type in"
                        + " IndexColorModel is not recognized: " + transferType);
            }
        }

        // find a cell type that's deep enough for all the bands in the given raster
        double noDataMin = Double.MAX_VALUE, noDataMax = Double.MIN_VALUE;
        {
            final int numBands = info.getNumBands();
            Number noDataValue;
            for (int bandN = 0; bandN < numBands; bandN++) {
                noDataValue = info.getNoDataValue(rasterIndex, bandN);
                noDataMin = Math.min(noDataMin, noDataValue.doubleValue());
                noDataMax = Math.max(noDataMax, noDataValue.doubleValue());
            }
        }
        final RasterCellType nativeCellType = info.getNativeCellType();
        final NumberRange<Double> sampleValueRange;
        sampleValueRange = nativeCellType.getSampleValueRange().castTo(Double.class);

        final RasterCellType targetCellType;

        if (sampleValueRange.contains((Number) Double.valueOf(noDataMin))
                && sampleValueRange.contains((Number) Double.valueOf(noDataMax))) {
            /*
             * The native cell type can hold the no-data values for all bands in the raster
             */
            targetCellType = nativeCellType;
        } else {
            targetCellType = promote(nativeCellType);
        }
        return targetCellType;
    }

    private static RasterCellType promote(final RasterCellType nativeCellType) {
        switch (nativeCellType) {
        case TYPE_1BIT:
        case TYPE_4BIT:
            return TYPE_8BIT_U;
        case TYPE_8BIT_U:
            return TYPE_16BIT_U;
        case TYPE_8BIT_S:
            return TYPE_16BIT_S;
        case TYPE_16BIT_U:
            return TYPE_32BIT_U;
        case TYPE_16BIT_S:
            return TYPE_32BIT_S;
        case TYPE_32BIT_S:
        case TYPE_32BIT_REAL:
        case TYPE_32BIT_U:
            return TYPE_64BIT_REAL;
        default:
            throw new IllegalArgumentException(
                    "Can't promote a raster of type 64-bit-real, there's "
                            + "no higher pixel depth than that!");
        }
    }
}
