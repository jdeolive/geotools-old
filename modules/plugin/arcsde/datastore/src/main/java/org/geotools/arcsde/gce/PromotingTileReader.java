package org.geotools.arcsde.gce;

import java.io.IOException;

final class PromotingTileReader implements TileReader {

    private final TileReader nativeReader;

    private final RasterCellType targetType;

    private final SampleDepthPromoter promoter;

    private final BitmaskToNoDataConverter noData;

    private final byte[] nativeTileData;

    public PromotingTileReader(final TileReader nativeTileReader, final RasterCellType sourceType,
            final RasterCellType targetType, final BitmaskToNoDataConverter noData) {

        this.nativeReader = nativeTileReader;
        this.targetType = targetType;
        this.noData = noData;
        this.nativeTileData = new byte[nativeTileReader.getBytesPerTile()];
        this.promoter = SampleDepthPromoter.createFor(sourceType, targetType);

    }

    public int getBitsPerSample() {
        return targetType.getBitsPerSample();
    }

    public int getBytesPerTile() {
        double pixelsPerTile = getPixelsPerTile();
        double bitsPerSample = getBitsPerSample();
        int bytesPerTile = (int) Math.floor((pixelsPerTile * bitsPerSample) / 8D);
        return bytesPerTile;
    }

    public int getNumberOfBands() {
        return nativeReader.getNumberOfBands();
    }

    public int getPixelsPerTile() {
        return nativeReader.getPixelsPerTile();
    }

    public int getTileHeight() {
        return nativeReader.getTileHeight();
    }

    public int getTileWidth() {
        return nativeReader.getTileWidth();
    }

    public int getTilesHigh() {
        return nativeReader.getTilesHigh();
    }

    public int getTilesWide() {
        return nativeReader.getTilesWide();
    }

    public boolean hasNext() throws IOException {
        return nativeReader.hasNext();
    }

    public TileInfo next(byte[] tileData) throws IOException {
        final TileInfo tileInfo = nativeReader.next(nativeTileData);
        final byte[] bitmaskData = tileInfo.getBitmaskData();
        final boolean hasNoDataPixels = bitmaskData.length > 0;
        final Long bandId = tileInfo.getBandId();

        if (tileInfo.getNumPixelsRead() == 0) {
            noData.setAll(bandId, tileData);
        } else {
            final int numSamples = getPixelsPerTile();
            for (int sampleN = 0; sampleN < numSamples; sampleN++) {
                if (hasNoDataPixels && noData.isNoData(sampleN, bitmaskData)) {
                    noData.setNoData(bandId, sampleN, tileData);
                } else {
                    promoter.promote(sampleN, nativeTileData, tileData);
                }
            }
        }

        return tileInfo;
    }

    private static abstract class SampleDepthPromoter {

        public abstract void promote(int sampleN, byte[] nativeTileData, byte[] tileData);

        public static SampleDepthPromoter createFor(final RasterCellType source,
                final RasterCellType target) {

            if (source == RasterCellType.TYPE_8BIT_U && target == RasterCellType.TYPE_16BIT_U) {
                return new UcharToUshort();
            }

            throw new UnsupportedOperationException("Promoting from " + source + " to " + target
                    + " not yet implemented");
        }
    }

    private static class UcharToUshort extends SampleDepthPromoter {

        @Override
        public void promote(int sampleN, byte[] nativeTileData, byte[] tileData) {
            int pixArrayOffset = 2 * sampleN;
            tileData[pixArrayOffset + 1] = (byte) ((nativeTileData[sampleN] >>> 0) & 0xFF);
        }
    }
}
