package org.geotools.arcsde.gce;

import java.awt.Dimension;
import java.awt.Rectangle;

import com.esri.sde.sdk.client.SeRow;

public class TileReaderFactory {

    /**
     * 
     * @param row
     * @param nativeType
     * @param targetType
     * @param noDataValues
     * @param numberOfBands
     * @param requestedTiles
     * @param tileSize
     * @return
     */
    public static TileReader getInstance(final SeRow row, final RasterDatasetInfo rasterInfo,
            final int rasterIndex, final Rectangle requestedTiles, Dimension tileSize) {

        final TileReader tileReader;

        final RasterCellType nativeType = rasterInfo.getNativeCellType();
        final RasterCellType targetType = rasterInfo.getTargetCellType(rasterIndex);
        final int numberOfBands = rasterInfo.getNumBands();

        final BitmaskToNoDataConverter noData;
        noData = BitmaskToNoDataConverter.getInstance(rasterInfo, rasterIndex);

        final int nativeBitsPerPixel = nativeType.getBitsPerSample();

        if (targetType == nativeType) {

            TileReader nativeTileReader = new NativeTileReader(row, nativeBitsPerPixel,
                    numberOfBands, requestedTiles, tileSize, noData);

            tileReader = nativeTileReader;

        } else {
            // need to promote native to target sample depth
            TileReader nativeTileReader;
            nativeTileReader = new NativeTileReader(row, nativeBitsPerPixel, numberOfBands,
                    requestedTiles, tileSize, BitmaskToNoDataConverter.NO_ACTION_CONVERTER);

            TileReader promotingTileReader = new PromotingTileReader(nativeTileReader, nativeType,
                    targetType, noData);
            tileReader = promotingTileReader;
        }
        return tileReader;
    }
}
