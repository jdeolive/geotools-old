/**
 * 
 */
package org.geotools.arcsde.raster.jai;

import java.awt.Point;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.IOException;

import javax.imageio.ImageTypeSpecifier;

import org.apache.commons.collections.map.LRUMap;
import org.geotools.arcsde.raster.io.TileInfo;
import org.geotools.arcsde.raster.io.TileReader;

import com.sun.media.imageioimpl.common.SimpleRenderedImage;

@SuppressWarnings("unchecked")
class ArcSDETiledRenderedImage extends SimpleRenderedImage {

    private TileReader tileReader;

    public ArcSDETiledRenderedImage(final TileReader tileReader, final ImageTypeSpecifier typeSpec) {
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

    /**
     * @see java.awt.image.RenderedImage#getTile(int, int)
     */
    public Raster getTile(final int tileX, final int tileY) {
        // System.err.printf("getTile(%d, %d) %s\n", tileX, tileY, this.toString());

        final TileKey key = newKey(tileX, tileY);
        WritableRaster currentTile = getCached(key);
        if (currentTile != null) {
            return currentTile;
        }

        final int xOrigin = tileXToX(tileX);
        final int yOrigin = tileYToY(tileY);

        final int numBands = sampleModel.getNumBands();

        final SampleModel tileSampleModel = super.sampleModel.createCompatibleSampleModel(
                tileWidth, tileHeight);

        DataBuffer dataBuffer = sampleModel.createDataBuffer();
        TileInfo[] tileInfo;
        try {
            tileInfo = tileReader.getTile(tileX, tileY);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        for (int bandN = 0; bandN < numBands; bandN++) {
            TileInfo bandData = tileInfo[bandN];
            bandData.fill(dataBuffer, bandN);
        }

        currentTile = Raster.createWritableRaster(tileSampleModel, dataBuffer, new Point(xOrigin,
                yOrigin));
        cache(key, currentTile);

        return currentTile;
    }

    private TileKey newKey(final int tileX, final int tileY) {
        final long rasterId = tileReader.getRasterId();
        final int pyramidLevel = tileReader.getPyramidLevel();
        final int rasterTileX = tileReader.getMinTileX() + tileX;
        final int rasterTileY = tileReader.getMinTileY() + tileY;

        TileKey tileKey = new TileKey(rasterId, pyramidLevel, tileX, tileY, rasterTileX,
                rasterTileY);
        return tileKey;
    }

    private static final LRUMap cache = new LRUMap(5);

    private void cache(TileKey key, WritableRaster tile) {
        cache.put(key, tile);
    }

    private WritableRaster getCached(TileKey key) {
        WritableRaster tile = (WritableRaster) cache.get(key);
        return tile;
    }

    private static class TileKey {
        private int tileX, tileY;

        private long rasterId;

        private int pyramidLevel;

        private int rasterTileX;

        private int rasterTileY;

        public TileKey(long rasterId, int pyramidLevel, int tileX, int tileY, int rasterTileX,
                int rasterTileY) {
            this.rasterId = rasterId;
            this.pyramidLevel = pyramidLevel;
            this.tileX = tileX;
            this.tileY = tileY;
            this.rasterTileX = rasterTileX;
            this.rasterTileY = rasterTileY;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof TileKey)) {
                return false;
            }
            TileKey t = (TileKey) o;
            return rasterId == t.rasterId && pyramidLevel == t.pyramidLevel
                    && rasterTileX == t.rasterTileX && rasterTileY == t.rasterTileY;
        }

        @Override
        public int hashCode() {
            return (17 ^ pyramidLevel) + rasterTileX * rasterTileY;
        }

        public int getTileX() {
            return tileX;
        }

        public int getTileY() {
            return tileY;
        }

    }
}