/*
 * Geotools - OpenSource mapping toolkit
 * (C) 2003, Institut de Recherche pour le Développement
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *
 * Contacts:
 *     UNITED KINGDOM: James Macgill
 *             mailto:j.macgill@geog.leeds.ac.uk
 *
 *     FRANCE: Surveillance de l'Environnement Assistée par Satellite
 *             Institut de Recherche pour le Développement / US-Espace
 *             mailto:seasnet@teledetection.fr
 *
 *     CANADA: Observatoire du Saint-Laurent
 *             Institut Maurice-Lamontagne
 *             mailto:osl@osl.gc.ca
 *
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package org.geotools.resources;

// J2SE dependencies
import java.util.Arrays;
import java.awt.image.Raster;
import java.awt.image.DataBuffer;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.awt.image.IndexColorModel;
import java.awt.image.BandedSampleModel;


/**
 * An {@link IndexColorModel} tolerant with image having more than one band.
 *
 * @version $Id: MultiBandsIndexColorModel.java,v 1.1 2003/03/09 19:45:14 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
final class MultiBandsIndexColorModel extends IndexColorModel {
    /**
     * The number of bands.
     */
    private final int numBands;

    /**
     * The visible band.
     */
    private final int visibleBand;

    /**
     * Construct an object with the specified properties.
     *
     * @param bits      The number of bits each pixel occupies.
     * @param size      The size of the color component arrays.
     * @param cmap      The array of color components.
     * @param start     The starting offset of the first color component.
     * @param hasalpha  Indicates whether alpha values are contained in the <code>cmap</code> array.
     * @param transparent  The index of the fully transparent pixel.
     * @param transferType The data type of the array used to represent pixel values. The
     *                     data type must be either <code>DataBuffer.TYPE_BYTE</code> or
     *                     <code>DataBuffer.TYPE_USHORT</code>.
     * @param numBands     The number of bands.
     * @param visibleBands The band to display.
     *
     * @throws IllegalArgumentException if <code>bits</code> is less than 1 or greater than 16.
     * @throws IllegalArgumentException if <code>size</code> is less than 1.
     * @throws IllegalArgumentException if <code>transferType</code> is not one of
     *         <code>DataBuffer.TYPE_BYTE</code> or <code>DataBuffer.TYPE_USHORT</code>.
     */
    public MultiBandsIndexColorModel(final int bits,
                                     final int size,
                                     final int[] cmap,
                                     final int start,
                                     final boolean hasAlpha,
                                     final int transparent,
                                     final int transferType,
                                     final int numBands,
                                     final int visibleBand)
    {
        super(bits, size, cmap, start, hasAlpha, transparent, transferType);
        this.numBands    = numBands;
        this.visibleBand = visibleBand;
    }

    /**
     * Returns a data element array representation of a pixel in this color model,
     * given an integer pixel representation in the default RGB color model.
     */
    public Object getDataElements(final int RGB, Object pixel) {
        if (pixel == null) {
            switch (getTransferType()) {
                case DataBuffer.TYPE_SHORT:  // fall through
                case DataBuffer.TYPE_USHORT: pixel=new short[numBands]; break;
                case DataBuffer.TYPE_BYTE:   pixel=new byte [numBands]; break;
                case DataBuffer.TYPE_INT:    pixel=new int  [numBands]; break;
                default: throw new UnsupportedOperationException();
            }
        }
        pixel = super.getDataElements(RGB, pixel);
        switch (getTransferType()) {
            case DataBuffer.TYPE_SHORT:  // fall through
            case DataBuffer.TYPE_USHORT: {
                final short[] array = (short[]) pixel;
                Arrays.fill(array, 1, numBands, array[0]);
                break;
            }
            case DataBuffer.TYPE_BYTE: {
                final byte[] array = (byte[]) pixel;
                Arrays.fill(array, 1, numBands, array[0]);
                break;
            }
            case DataBuffer.TYPE_INT: {
                final int[] array = (int[]) pixel;
                Arrays.fill(array, 1, numBands, array[0]);
                break;
            }
            default: throw new UnsupportedOperationException();
        }
        return pixel;
    }

    /**
     * Returns an array of unnormalized color/alpha components for a specified pixel
     * in this color model.
     */
    public int[] getComponents(final Object pixel, final int[] components, final int offset) {
        final int i;
        switch (getTransferType()) {
            case DataBuffer.TYPE_SHORT:  // Fall through
            case DataBuffer.TYPE_USHORT: i=((short[])pixel)[visibleBand] & 0xffff; break;
            case DataBuffer.TYPE_BYTE:   i=((byte [])pixel)[visibleBand] & 0xff;   break;
            case DataBuffer.TYPE_INT:    i=((int  [])pixel)[visibleBand];          break;
            default: throw new UnsupportedOperationException();
        }
        return getComponents(i, components, offset);
    }

    /**
     * Creates a <code>WritableRaster</code> with the specified width 
     * and height that has a data layout (<code>SampleModel</code>) 
     * compatible with this <code>ColorModel</code>.
     */
    public WritableRaster createCompatibleWritableRaster(final int width, final int height) {
        return Raster.createBandedRaster(getTransferType(), width, height, numBands, null);
    }

    /**
     * Returns <code>true</code> if <code>raster</code> is compatible 
     * with this <code>ColorModel</code>.
     */
    public boolean isCompatibleRaster(final Raster raster) {
        return isCompatibleSampleModel(raster.getSampleModel());
    }
    
    /**
     * Creates a <code>SampleModel</code> with the specified 
     * width and height that has a data layout compatible with 
     * this <code>ColorModel</code>.  
     */
    public SampleModel createCompatibleSampleModel(final int width, final int height) {
        return new BandedSampleModel(getTransferType(), width, height, numBands);
    }
    
    /** 
     * Checks if the specified <code>SampleModel</code> is compatible 
     * with this <code>ColorModel</code>.
     */
    public boolean isCompatibleSampleModel(final SampleModel sm) {
        return (sm instanceof BandedSampleModel)                     &&
                sm.getTransferType()                 == transferType &&
                sm.getNumBands()                     == numBands     &&
                (1 << sm.getSampleSize(visibleBand)) >= getMapSize();
    }
}
