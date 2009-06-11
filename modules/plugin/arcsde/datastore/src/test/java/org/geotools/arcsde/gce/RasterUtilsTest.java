/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.awt.Transparency;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferUShort;
import java.awt.image.IndexColorModel;

import org.junit.Test;

public class RasterUtilsTest {

    @Test
    public void testSdeColormapToJavaColorModel() {

        testSdeColormapToJavaColorModel(256, 3, DataBuffer.TYPE_BYTE);

        testSdeColormapToJavaColorModel(10, 3, DataBuffer.TYPE_BYTE);

        testSdeColormapToJavaColorModel(256, 4, DataBuffer.TYPE_BYTE);

        testSdeColormapToJavaColorModel(10, 4, DataBuffer.TYPE_BYTE);

        testSdeColormapToJavaColorModel(65536, 3, DataBuffer.TYPE_USHORT);

        testSdeColormapToJavaColorModel(10, 3, DataBuffer.TYPE_USHORT);

        testSdeColormapToJavaColorModel(65536, 4, DataBuffer.TYPE_USHORT);

        testSdeColormapToJavaColorModel(10, 4, DataBuffer.TYPE_USHORT);
    }

    private void testSdeColormapToJavaColorModel(final int size, final int numBanks,
            final int transferType) {

        DataBuffer colorMapData;
        IndexColorModel colorModel;
        colorMapData = newColorMap(size, numBanks, transferType);

        int bitsPerSample = DataBuffer.getDataTypeSize(transferType);
        colorModel = RasterUtils.sdeColorMapToJavaColorModel(colorMapData, bitsPerSample);

        assertColorModel(colorMapData, colorModel);
    }

    private void assertColorModel(DataBuffer expected, IndexColorModel actual) {
        int size = expected.getSize();
        int numBanks = expected.getNumBanks();
        int dataType = expected.getDataType();

        assertEquals(dataType, actual.getTransferType());
        assertEquals(numBanks, actual.getNumComponents());

        if (numBanks == 3) {
            assertEquals(-1, actual.getTransparentPixel());
            assertFalse(actual.hasAlpha());
            assertEquals(Transparency.OPAQUE, actual.getTransparency());
        } else if (numBanks == 4) {
            // transparent pixel is looked up by IndexColorModel
            assertTrue(actual.getTransparentPixel() > -1);
            assertTrue(actual.hasAlpha());
            assertEquals(Transparency.TRANSLUCENT, actual.getTransparency());
        }
        assertEquals(size, actual.getMapSize());

        for (int elem = 0; elem < size; elem++) {
            for (int bank = 0; bank < numBanks; bank++) {
                int actualValue = 0;
                switch (bank) {
                case 0:
                    actualValue = actual.getRed(elem);
                    break;
                case 1:
                    actualValue = actual.getGreen(elem);
                    break;
                case 2:
                    actualValue = actual.getBlue(elem);
                    break;
                case 3:
                    actualValue = actual.getAlpha(elem);
                    break;
                }
                assertEquals("at index " + elem + ", bank " + bank, expected.getElem(bank, elem),
                        actualValue);
            }
        }
    }

    private DataBuffer newColorMap(int size, int numBanks, int transferType) {
        DataBuffer colorMapData;
        switch (transferType) {
        case DataBuffer.TYPE_BYTE:
            colorMapData = new DataBufferByte(size, numBanks);
            break;
        case DataBuffer.TYPE_USHORT:
            colorMapData = new DataBufferUShort(size, numBanks);
            break;
        default:
            throw new IllegalArgumentException();
        }
        for (int elem = 0; elem < size; elem++) {
            for (int bank = 0; bank < numBanks; bank++) {
                // cast to byte
                int value = elem & 0xFF;
                colorMapData.setElem(bank, elem, value);
            }
        }
        return colorMapData;
    }

}
