package org.geotools.arcsde.raster.io;

import static org.geotools.arcsde.raster.info.RasterCellType.TYPE_1BIT;
import static org.geotools.arcsde.raster.info.RasterCellType.TYPE_4BIT;
import static org.geotools.arcsde.raster.info.RasterCellType.TYPE_8BIT_S;
import static org.geotools.arcsde.raster.info.RasterCellType.TYPE_8BIT_U;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.geotools.arcsde.raster.info.RasterCellType;
import org.geotools.arcsde.session.Command;
import org.geotools.arcsde.session.ISession;

import com.esri.sde.sdk.client.SeConnection;
import com.esri.sde.sdk.client.SeException;
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

    private final SeRow row;

    private final TileDataFetcher dataFetcher;

    private TileInfo[] target;

    public TileFetchCommand(final SeRow row, final RasterCellType nativeType, TileInfo[] target) {
        this.row = row;
        this.target = target;
        this.dataFetcher = getTileDataFetcher(nativeType);
    }

    @Override
    public TileInfo[] execute(ISession session, SeConnection connection) throws SeException,
            IOException {

        final int numberOfBands = target.length;

        for (int bandN = 0; bandN < numberOfBands; bandN++) {
            SeRasterTile tile = row.getRasterTile();
            if (tile == null) {
                // EOF
                return null;
            }
            final byte[] bitMaskData = tile.getBitMaskData();
            final int numPixelsRead = tile.getNumPixels();
            final long bandId = tile.getBandId().longValue();
            final int colIndex = tile.getColumnIndex();
            final int rowIndex = tile.getRowIndex();

            TileInfo bandData = target[bandN];
            bandData.setBandId(bandId);
            bandData.setColumnIndex(colIndex);
            bandData.setRowIndex(rowIndex);
            bandData.setNumPixelsRead(numPixelsRead);
            bandData.setBitmaskData(bitMaskData);

            final int pixelsPerTile = bandData.getNumPixels();
            dataFetcher.setTileData(pixelsPerTile, tile, bandData);
        }
        return target;
    }

    private static Map<RasterCellType, TileDataFetcher> tileDataSetters = new HashMap<RasterCellType, TileDataFetcher>();
    static {
        final ByteTileSetter byteTileSetter = new ByteTileSetter();
        tileDataSetters.put(TYPE_1BIT, new OneBitTileSetter());
        tileDataSetters.put(TYPE_4BIT, byteTileSetter);
        tileDataSetters.put(TYPE_8BIT_S, byteTileSetter);
        tileDataSetters.put(TYPE_8BIT_U, byteTileSetter);
        tileDataSetters.put(RasterCellType.TYPE_16BIT_U, new UShortTileSetter());
        tileDataSetters.put(RasterCellType.TYPE_16BIT_S, new ShortTileSetter());

        tileDataSetters.put(RasterCellType.TYPE_32BIT_S, new IntegerTileSetter());
        tileDataSetters.put(RasterCellType.TYPE_32BIT_U, new UnsignedIntegerTileSetter());

        tileDataSetters.put(RasterCellType.TYPE_32BIT_REAL, new FloatTileSetter());
        tileDataSetters.put(RasterCellType.TYPE_64BIT_REAL, new DoubleTileSetter());
    }

    private TileDataFetcher getTileDataFetcher(final RasterCellType pixelType) {
        TileDataFetcher tileDataFetcher = tileDataSetters.get(pixelType);
        if (tileDataFetcher == null) {
            throw new IllegalArgumentException("No registered TileDataFetcher for pixel type "
                    + pixelType);
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

    /**
     *
     */
    private static final class OneBitTileSetter extends TileDataFetcher {

        @Override
        public void setTileData(final int numPixels, final SeRasterTile tile, TileInfo tileInfo) {

            byte[] tileData = tileInfo.getTileDataAsBytes();

            final int numPixelsRead = tile.getNumPixels();

            if (numPixelsRead > 0) {
                if (numPixelsRead != numPixels) {
                    throw new IllegalStateException("Expected num pixels read to be " + numPixels
                            + " but got " + numPixelsRead);
                }
                try {
                    tile.getPixels(tileData);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                // getPixels(byte[]) for a 1-bit raster sets set bits to 255 instead of 1, we want
                // them to be 1
                final byte bitSet = (byte) 0xFF;
                for (int i = 0; i < numPixels; i++) {
                    if (bitSet == tileData[i]) {
                        tileData[i] = 1;
                    }
                }
            }
        }
    }

    /**
     *
     */
    private static final class ByteTileSetter extends TileDataFetcher {
        @Override
        public void setTileData(final int numPixels, final SeRasterTile tile, TileInfo tileInfo) {

            byte[] tileData = tileInfo.getTileDataAsBytes();

            final int numPixelsRead = tile.getNumPixels();

            if (numPixelsRead > 0) {
                if (numPixelsRead != numPixels) {
                    throw new IllegalStateException("Expected num pixels read to be " + numPixels
                            + " but got " + numPixelsRead);
                }
                try {
                    tile.getPixels(tileData);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private static final class UShortTileSetter extends TileDataFetcher {
        @Override
        public void setTileData(final int numPixels, final SeRasterTile tile, TileInfo tileInfo) {

            short[] tileData = tileInfo.getTileDataAsUnsignedShorts();

            final int numPixelsRead = tile.getNumPixels();

            if (numPixelsRead > 0) {
                if (numPixelsRead != numPixels) {
                    throw new IllegalStateException("Expected num pixels read to be " + numPixels
                            + " but got " + numPixelsRead);
                }
                try {
                    int[] ints = new int[numPixels];
                    tile.getPixels(ints);
                    for (int i = 0; i < numPixels; i++) {
                        tileData[i] = (short) ints[i];
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private static final class ShortTileSetter extends TileDataFetcher {
        @Override
        public void setTileData(final int numPixels, final SeRasterTile tile, TileInfo tileInfo) {

            short[] tileData = tileInfo.getTileDataAsShorts();

            final int numPixelsRead = tile.getNumPixels();

            if (numPixelsRead > 0) {
                if (numPixelsRead != numPixels) {
                    throw new IllegalStateException("Expected num pixels read to be " + numPixels
                            + " but got " + numPixelsRead);
                }
                try {
                    int[] ints = new int[numPixels];
                    tile.getPixels(ints);
                    for (int i = 0; i < numPixels; i++) {
                        tileData[i] = (short) ints[i];
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private static final class IntegerTileSetter extends TileDataFetcher {
        @Override
        public void setTileData(final int numPixels, final SeRasterTile tile, TileInfo tileInfo) {

            int[] tileData = tileInfo.getTileDataAsIntegers();

            final int numPixelsRead = tile.getNumPixels();

            if (numPixelsRead > 0) {
                if (numPixelsRead != numPixels) {
                    throw new IllegalStateException("Expected num pixels read to be " + numPixels
                            + " but got " + numPixelsRead);
                }
                try {
                    tile.getPixels(tileData);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private static final class UnsignedIntegerTileSetter extends TileDataFetcher {
        @Override
        public void setTileData(final int numPixels, final SeRasterTile tile, TileInfo tileInfo) {

            double[] tileData = tileInfo.getTileDataAsDoubles();

            final int numPixelsRead = tile.getNumPixels();

            if (numPixelsRead > 0) {
                if (numPixelsRead != numPixels) {
                    throw new IllegalStateException("Expected num pixels read to be " + numPixels
                            + " but got " + numPixelsRead);
                }
                try {
                    tile.getPixels(tileData);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private static final class FloatTileSetter extends TileDataFetcher {
        @Override
        public void setTileData(final int numPixels, final SeRasterTile tile, TileInfo tileInfo) {

            float[] tileData = tileInfo.getTileDataAsFloats();

            final int numPixelsRead = tile.getNumPixels();

            if (numPixelsRead > 0) {
                if (numPixelsRead != numPixels) {
                    throw new IllegalStateException("Expected num pixels read to be " + numPixels
                            + " but got " + numPixelsRead);
                }
                try {
                    tile.getPixels(tileData);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private static final class DoubleTileSetter extends TileDataFetcher {
        @Override
        public void setTileData(final int numPixels, final SeRasterTile tile, TileInfo tileInfo) {

            double[] tileData = tileInfo.getTileDataAsDoubles();

            final int numPixelsRead = tile.getNumPixels();

            if (numPixelsRead > 0) {
                if (numPixelsRead != numPixels) {
                    throw new IllegalStateException("Expected num pixels read to be " + numPixels
                            + " but got " + numPixelsRead);
                }
                try {
                    tile.getPixels(tileData);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}