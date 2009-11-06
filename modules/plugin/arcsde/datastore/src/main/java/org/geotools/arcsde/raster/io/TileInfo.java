/**
 * 
 */
package org.geotools.arcsde.raster.io;

import java.awt.image.DataBuffer;

public final class TileInfo {
    private long bandId;

    private byte[] bitmaskData;

    private int numPixelsRead;

    private int columnIndex;

    private int rowIndex;

    private byte[] tileDataBytes;

    private short[] tileDataShorts;

    private int[] tileDataInts;

    private float[] tileDataFloats;

    private double[] tileDataDoubles;

    private final int numPixels;

    public TileInfo(int pixelsPerTile) {
        this.numPixels = pixelsPerTile;
    }

    public Long getBandId() {
        return bandId;
    }

    public byte[] getBitmaskData() {
        return bitmaskData;
    }

    /**
     * @return number of pixels in the tile data
     */
    public int getNumPixels() {
        return numPixels;
    }

    /**
     * @return number of pixels actually read. It shall be either {@code 0} or equal to
     *         {@link #getNumPixels()}
     */
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
        this.tileDataBytes = null;
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
        if (tileDataBytes == null) {
            tileDataBytes = new byte[numPixels];
        }
        return tileDataBytes;
    }

    public short[] getTileDataAsUnsignedShorts() {
        if (tileDataShorts == null) {
            tileDataShorts = new short[numPixels];
        }

        // promote if necessary
        if (tileDataBytes != null) {
            final int length = tileDataBytes.length;
            short val;
            for (int i = 0; i < length; i++) {
                val = (short) (tileDataBytes[i] & 0xFF);
                tileDataShorts[i] = val;
            }
        }
        return tileDataShorts;
    }

    public short[] getTileDataAsShorts() {
        if (tileDataShorts == null) {
            tileDataShorts = new short[numPixels];
        }
        // promote if necessary
        if (tileDataBytes != null) {
            final int length = tileDataBytes.length;
            short val;
            for (int i = 0; i < length; i++) {
                val = (short) tileDataBytes[i];
                tileDataShorts[i] = val;
            }
        }

        return tileDataShorts;
    }

    public int[] getTileDataAsIntegers() {
        if (tileDataInts == null) {
            tileDataInts = new int[numPixels];
        }
        // promote if necessary
        if (tileDataShorts != null) {
            final int length = tileDataShorts.length;
            for (int i = 0; i < length; i++) {
                tileDataInts[i] = tileDataShorts[i];
            }
        } else if (tileDataBytes != null) {
            final int length = tileDataBytes.length;
            for (int i = 0; i < length; i++) {
                tileDataInts[i] = tileDataBytes[i];
            }
        }
        return tileDataInts;
    }

    public float[] getTileDataAsFloats() {
        if (tileDataFloats == null) {
            tileDataFloats = new float[numPixels];
        }
        // promote if necessary
        if (tileDataInts != null) {
            final int length = tileDataInts.length;
            for (int i = 0; i < length; i++) {
                tileDataFloats[i] = tileDataInts[i];
            }
        } else if (tileDataShorts != null) {
            final int length = tileDataShorts.length;
            for (int i = 0; i < length; i++) {
                tileDataFloats[i] = tileDataShorts[i];
            }
        } else if (tileDataBytes != null) {
            final int length = tileDataBytes.length;
            for (int i = 0; i < length; i++) {
                tileDataFloats[i] = tileDataBytes[i];
            }
        }

        return tileDataFloats;
    }

    public double[] getTileDataAsDoubles() {
        if (tileDataDoubles == null) {
            tileDataDoubles = new double[numPixels];
        }
        // promote if necessary
        if (tileDataFloats != null) {
            final int length = tileDataFloats.length;
            for (int i = 0; i < length; i++) {
                tileDataDoubles[i] = tileDataFloats[i];
            }
        } else if (tileDataInts != null) {
            final int length = tileDataInts.length;
            for (int i = 0; i < length; i++) {
                tileDataDoubles[i] = tileDataInts[i];
            }
        } else if (tileDataShorts != null) {
            final int length = tileDataShorts.length;
            for (int i = 0; i < length; i++) {
                tileDataDoubles[i] = tileDataShorts[i];
            }
        } else if (tileDataBytes != null) {
            final int length = tileDataBytes.length;
            for (int i = 0; i < length; i++) {
                tileDataDoubles[i] = tileDataBytes[i];
            }
        }

        return tileDataDoubles;
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

    public void setBandId(final long bandId) {
        this.bandId = bandId;
    }

    public void setColumnIndex(final int colIndex) {
        this.columnIndex = colIndex;
    }

    public void setRowIndex(final int rowIndex) {
        this.rowIndex = rowIndex;
    }

    public void setNumPixelsRead(final int numPixelsRead) {
        this.numPixelsRead = numPixelsRead;
    }

    public void setBitmaskData(final byte[] bitMaskData) {
        this.bitmaskData = bitMaskData;
    }
}