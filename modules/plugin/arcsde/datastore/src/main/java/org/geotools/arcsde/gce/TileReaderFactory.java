package org.geotools.arcsde.gce;

import java.awt.Rectangle;

import org.geotools.arcsde.session.ISessionPool;

public class TileReaderFactory {

    /**
     * 
     * @param preparedQuery
     * @param row
     * @param nativeType
     * @param targetType
     * @param noDataValues
     * @param numberOfBands
     * @param requestedTiles
     * @param tileSize
     * @return
     */
    public static TileReader getInstance(final ISessionPool sessionPool,
            final RasterDatasetInfo rasterInfo, final long rasterId, final int pyramidLevel,
            final Rectangle requestedTiles) {

        final TileReader tileReader;

        final RasterCellType nativeType = rasterInfo.getNativeCellType();
        final RasterCellType targetType = rasterInfo.getTargetCellType(rasterId);

        final BitmaskToNoDataConverter noData;
        noData = BitmaskToNoDataConverter.getInstance(rasterInfo, rasterId);

        if (targetType == nativeType) {

            TileReader nativeTileReader = new NativeTileReader(sessionPool, rasterInfo, rasterId,
                    pyramidLevel, requestedTiles, noData);

            tileReader = nativeTileReader;

        } else {
            // need to promote native to target sample depth
            TileReader nativeTileReader;
            nativeTileReader = new NativeTileReader(sessionPool, rasterInfo, rasterId,
                    pyramidLevel, requestedTiles, BitmaskToNoDataConverter.NO_ACTION_CONVERTER);

            TileReader promotingTileReader = new PromotingTileReader(nativeTileReader, nativeType,
                    targetType, noData);
            tileReader = promotingTileReader;
        }
        return tileReader;
    }
}
