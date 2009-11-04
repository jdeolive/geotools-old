/**
 * 
 */
package org.geotools.arcsde.raster.io;

import java.awt.image.DataBuffer;

public final class TileInfo {
    private final long bandId;

    private final byte[] bitmaskData;

    private final int numPixelsRead;

    private final int columnIndex;

    private final int rowIndex;

    private byte[] tileDataBytes;

    private short[] tileDataShorts;

    private int[] tileDataInts;

    private float[] tileDataFloats;

    private double[] tileDataDoubles;

    public TileInfo(long bandId, int colIndex, int rowIndex, int numPixelsRead, byte[] bitMaskData) {
        this.bandId = bandId;
        this.columnIndex = colIndex;
        this.rowIndex = rowIndex;
        this.numPixelsRead = numPixelsRead;
        this.bitmaskData = bitMaskData;
    }

    public Long getBandId() {
        return bandId;
    }

    public byte[] getBitmaskData() {
        return bitmaskData;
    }

    public int getNumPixelsRead() {
        return numPixelsRead;
    }

    public int getColumnIndex() {
        return columnIndex;
    }

    public int getRowIndex() {
        return rowIndex;
    }

    public void setTileData(final byte[] pixelData) {
        this.tileDataBytes = pixelData;
    }

    public void setTileData(short[] pixelData) {
        this.tileDataShorts = pixelData;
    }

    public void setTileData(int[] pixelData) {
        this.tileDataInts = pixelData;
    }

    public void setTileData(float[] pixelData) {
        this.tileDataFloats = pixelData;
    }

    public void setTileData(double[] pixelData) {
        this.tileDataDoubles = pixelData;
    }

    public byte[] getTileDataAsBytes() {
        if (tileDataBytes != null) {
            return tileDataBytes;
        }
        throw new UnsupportedOperationException();
    }

    public short[] getTileDataAsUnsignedShorts() {
        if (tileDataShorts != null) {
            return tileDataShorts;
        }
        if (tileDataBytes != null) {
            final int length = tileDataBytes.length;
            short[] data = new short[length];
            short val;
            for (int i = 0; i < length; i++) {
                val = (short) (tileDataBytes[i] & 0xFF);
                data[i] = val;
            }
            tileDataShorts = data;
            return tileDataShorts;
        }
        throw new UnsupportedOperationException();
    }

    public int[] getTileDataAsIntegers() {
        if (tileDataInts != null) {
            return tileDataInts;
        }
        if (tileDataBytes != null) {
            final int length = tileDataBytes.length;
            int[] data = new int[length];
            for (int i = 0; i < length; i++) {
                data[i] = tileDataBytes[i];
            }
            tileDataInts = data;
        }
        if (tileDataShorts != null) {
            final int length = tileDataShorts.length;
            int[] data = new int[length];
            for (int i = 0; i < length; i++) {
                data[i] = tileDataShorts[i];
            }
            tileDataInts = data;
        }
        if (tileDataInts == null) {
            throw new UnsupportedOperationException();
        }
        return tileDataInts;
    }

    public void setValue(int sampleN, Number value) {
        if (tileDataBytes != null) {
            tileDataBytes[sampleN] = value.byteValue();
        }
        if (tileDataShorts != null) {
            tileDataShorts[sampleN] = value.shortValue();
        }
        if (tileDataInts != null) {
            tileDataInts[sampleN] = value.intValue();
        }
        if (tileDataFloats != null) {
            tileDataFloats[sampleN] = value.floatValue();
        }
        if (tileDataDoubles != null) {
            tileDataDoubles[sampleN] = value.doubleValue();
        }
    }

    public void fill(DataBuffer dataBuffer, final int bank) {
        if (tileDataDoubles != null) {
            final int length = tileDataDoubles.length;
            double val;
            for (int i = 0; i < length; i++) {
                val = tileDataDoubles[i];
                dataBuffer.setElemDouble(bank, i, val);
            }
        } else if (tileDataFloats != null) {
            final int length = tileDataFloats.length;
            float val;
            for (int i = 0; i < length; i++) {
                val = tileDataFloats[i];
                dataBuffer.setElemFloat(bank, i, val);
            }
        } else if (tileDataInts != null) {
            final int length = tileDataInts.length;
            int val;
            for (int i = 0; i < length; i++) {
                val = tileDataInts[i];
                dataBuffer.setElem(bank, i, val);
            }
        } else if (tileDataShorts != null) {
            final int length = tileDataShorts.length;
            int val;
            for (int i = 0; i < length; i++) {
                val = tileDataShorts[i];
                dataBuffer.setElem(bank, i, val);
            }
        } else if (tileDataBytes != null) {
            final int length = tileDataBytes.length;
            int val;
            for (int i = 0; i < length; i++) {
                val = tileDataBytes[i];
                dataBuffer.setElem(bank, i, val);
            }
        }
    }
}