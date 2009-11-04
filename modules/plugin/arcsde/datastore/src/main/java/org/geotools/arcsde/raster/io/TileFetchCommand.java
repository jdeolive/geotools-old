package org.geotools.arcsde.raster.io;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.geotools.arcsde.session.Command;
import org.geotools.arcsde.session.ISession;

import com.esri.sde.sdk.client.SeConnection;
import com.esri.sde.sdk.client.SeException;
import com.esri.sde.sdk.client.SeQuery;
import com.esri.sde.sdk.client.SeRaster;
import com.esri.sde.sdk.client.SeRasterTile;
import com.esri.sde.sdk.client.SeRow;

/**
 * Command to fetch an {@link SeRasterTile tile}
 * 
 * @author Gabriel Roldan (OpenGeo)
 * @since 2.5.8
 * @source $URL:
 *         http://svn.osgeo.org/geotools/trunk/modules/plugin/arcsde/datastore/src/main/java/org
 *         /geotools/arcsde/raster/io/TileFetchCommand.java $
 */
class TileFetchCommand extends Command<TileInfo[]> {

    private final SeQuery preparedQuery;

    private final SeRow row;

    private final int pixelsPerTile;

    private final int numberOfBands;

    private TileDataFetcher dataFetcher;

    public TileFetchCommand(final SeQuery preparedQuery, final SeRow row, final int pixelsPerTile,
            final int numberOfBands) {
        this.preparedQuery = preparedQuery;
        this.row = row;
        this.pixelsPerTile = pixelsPerTile;
        this.numberOfBands = numberOfBands;
    }

    public SeQuery getQuery() {
        return preparedQuery;
    }

    @Override
    public TileInfo[] execute(ISession session, SeConnection connection) throws SeException,
            IOException {

        TileInfo[] tilesPerBand = new TileInfo[numberOfBands];

        for (int bandN = 0; bandN < numberOfBands; bandN++) {
            SeRasterTile tile = row.getRasterTile();
            if (tile == null) {
                // EOF
                return null;
            }
            final byte[] bitMaskData = tile.getBitMaskData();
            final int numPixelsRead = tile.getNumPixels();

            long bandId = tile.getBandId().longValue();
            int colIndex = tile.getColumnIndex();
            int rowIndex = tile.getRowIndex();

            TileInfo tileInfo = new TileInfo(bandId, colIndex, rowIndex, numPixelsRead, bitMaskData);

            if (dataFetcher == null) {
                final int sePixelType = tile.getPixelType();
                dataFetcher = getTileDataFetcher(sePixelType);
            }

            dataFetcher.setTileData(pixelsPerTile, tile, tileInfo);

            tilesPerBand[bandN] = tileInfo;
        }
        return tilesPerBand;
    }

    private static Map<Integer, TileDataFetcher> tileDataSetters = new HashMap<Integer, TileDataFetcher>();
    static {
        {
            ByteTileSetter byteTileSetter = new ByteTileSetter();
            tileDataSetters.put(SeRaster.SE_PIXEL_TYPE_1BIT, byteTileSetter);
            tileDataSetters.put(SeRaster.SE_PIXEL_TYPE_4BIT, byteTileSetter);
            tileDataSetters.put(SeRaster.SE_PIXEL_TYPE_8BIT_S, byteTileSetter);
            tileDataSetters.put(SeRaster.SE_PIXEL_TYPE_8BIT_U, byteTileSetter);
        }
    }

    private TileDataFetcher getTileDataFetcher(final int sePixelType) {
        TileDataFetcher tileDataFetcher = tileDataSetters.get(Integer.valueOf(sePixelType));
        if (tileDataFetcher == null) {
            throw new IllegalArgumentException("No registered TileDataFetcher for pixel type "
                    + sePixelType);
        }
        return tileDataFetcher;
    }

    /**
     * 
     * @author Gabriel Roldan
     */
    private static abstract class TileDataFetcher {
        public abstract void setTileData(int numPixels, SeRasterTile tile, TileInfo tileInfo);
    }

    private static final class ByteTileSetter extends TileDataFetcher {
        @Override
        public void setTileData(final int numPixels, final SeRasterTile tile, TileInfo tileInfo) {

            byte[] tileData = new byte[numPixels];

            final int numPixelsRead = tile.getNumPixels();

            if (numPixelsRead > 0) {
                if (numPixelsRead != numPixels) {
                    throw new IllegalStateException("Expected num pixels read to be " + numPixels
                            + " but got " + numPixelsRead);
                }
                try {
                    tile.getPixels(tileData);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            tileInfo.setTileData(tileData);
        }
    }
}