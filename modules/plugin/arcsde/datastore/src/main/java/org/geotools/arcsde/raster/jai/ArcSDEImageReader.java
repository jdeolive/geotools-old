package org.geotools.arcsde.raster.jai;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;

import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.media.jai.PlanarImage;

import org.geotools.arcsde.raster.io.TileInfo;
import org.geotools.arcsde.raster.io.TileReader;

import com.sun.media.imageioimpl.common.SimpleRenderedImage;

public class ArcSDEImageReader extends ImageReader {

    private TileReader tileReader;

    private ImageTypeSpecifier typeSpec;

    public ArcSDEImageReader(final ImageTypeSpecifier typeSpec) {
        super(null);
        this.typeSpec = typeSpec;
    }

    @Override
    public int getWidth(int imageIndex) throws IOException {
        int w = tileReader.getTilesWide() * tileReader.getTileWidth();
        return w;
    }

    @Override
    public int getHeight(int imageIndex) throws IOException {
        int h = tileReader.getTilesHigh() * tileReader.getTileHeight();
        return h;
    }

    @Override
    public IIOMetadata getImageMetadata(int imageIndex) throws IOException {
        return null;
    }

    @Override
    public Iterator<ImageTypeSpecifier> getImageTypes(int imageIndex) throws IOException {
        return Collections.singleton(typeSpec).iterator();
    }

    @Override
    public int getNumImages(boolean allowSearch) throws IOException {
        return 1;
    }

    @Override
    public IIOMetadata getStreamMetadata() throws IOException {
        return null;
    }

    @Override
    public RenderedImage readAsRenderedImage(int imageIndex, ImageReadParam param)
            throws IOException {
        // return read(imageIndex, param);

        RenderedImage image = new ArcSDETiledRenderedImage(tileReader, typeSpec);

        // BufferedImage bufferedImage = PlanarImage.wrapRenderedImage(image).getAsBufferedImage();
        // return bufferedImage;
        return image;
    }

    @Override
    public BufferedImage read(int imageIndex, ImageReadParam param) throws IOException {
        if (param == null) {
            param = getDefaultReadParam();
        }

        RenderedImage rendered = readAsRenderedImage(imageIndex, param);

        BufferedImage bufferedImage = PlanarImage.wrapRenderedImage(rendered).getAsBufferedImage();

        return bufferedImage;
    }

    @Override
    public void setInput(Object input, boolean seekForwardOnly, boolean ignoreMetadata) {
        this.tileReader = (TileReader) input;
    }

    @SuppressWarnings("unchecked")
    private static class ArcSDETiledRenderedImage extends SimpleRenderedImage {

        private TileReader tileReader;

        public ArcSDETiledRenderedImage(TileReader tileReader, ImageTypeSpecifier typeSpec) {
            this.tileReader = tileReader;
            super.colorModel = typeSpec.getColorModel();
            super.sampleModel = typeSpec.getSampleModel();
            super.height = tileReader.getTilesHigh() * tileReader.getTileHeight();
            super.width = tileReader.getTilesWide() * tileReader.getTileWidth();
            super.minX = 0;
            super.minY = 0;
            super.tileGridXOffset = 0;
            super.tileGridYOffset = 0;
            super.tileHeight = tileReader.getTileHeight();
            super.tileWidth = tileReader.getTileWidth();
        }

        private WritableRaster[][] tileCache = null;

        /**
         * @see java.awt.image.RenderedImage#getTile(int, int)
         */
        public Raster getTile(final int tileX, final int tileY) {
            System.err.printf("getTile(%d, %d) %s\n", tileX, tileY, this.toString());
            if (tileCache == null) {
                tileCache = new WritableRaster[tileReader.getTilesWide()][tileReader.getTilesHigh()];
            }

            WritableRaster currentTile = tileCache[tileX][tileY];
            if (currentTile == null) {
                final int tilesWide = tileReader.getTilesWide();
                final int tilesHigh = tileReader.getTilesHigh();

                for (int ty = 0; ty < tilesHigh; ty++) {
                    for (int tx = 0; tx < tilesWide; tx++) {

                        currentTile = tileCache[tx][ty];

                        if (currentTile == null) {
                            int x = tileXToX(tx);
                            int y = tileYToY(ty);
                            // System.err.println("fetching tile " + tx + "," + ty);
                            currentTile = fetchTile(x, y);
                            tileCache[tx][ty] = currentTile;
                        }
                        if (tx == tileX && ty == tileY) {
                            return currentTile;
                        }
                    }
                }
            }
            // currentTile = tileCache[tileX][tileY];
            return currentTile;
        }

        private WritableRaster fetchTile(final int xOrigin, final int yOrigin) {
            final int numBands = sampleModel.getNumBands();

            final SampleModel tileSampleModel = super.sampleModel.createCompatibleSampleModel(
                    tileWidth, tileHeight);

            DataBuffer dataBuffer = sampleModel.createDataBuffer();
            TileInfo[] tileInfo;
            try {
                tileInfo = tileReader.next();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            
            for (int bandN = 0; bandN < numBands; bandN++) {
                TileInfo bandData = tileInfo[bandN];
                bandData.fill(dataBuffer, bandN);
//                byte[] rawBandData = tileInfo.getTileData();
//
//                final int numPixels = tileWidth * tileHeight;
//                for (int pixelN = 0; pixelN < numPixels; pixelN++) {
//                    int val = rawBandData[2 * pixelN + 1] & 0xFF;
//                    dataBuffer.setElem(bandN, pixelN, val);
//                }
            }

            WritableRaster currentTile;
            currentTile = Raster.createWritableRaster(tileSampleModel, dataBuffer, new Point(
                    xOrigin, yOrigin));
            return currentTile;
        }

    }
}
