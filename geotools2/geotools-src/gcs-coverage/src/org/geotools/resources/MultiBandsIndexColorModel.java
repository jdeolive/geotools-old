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
import java.awt.image.ComponentSampleModel;


/**
 * An {@link IndexColorModel} tolerant with image having more than one band.
 *
 * @version $Id: MultiBandsIndexColorModel.java,v 1.2 2003/03/10 06:35:44 aaime Exp $
 * @author Martin Desruisseaux
 * @author Andrea Aime
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
        return (sm instanceof ComponentSampleModel)                     &&
                sm.getTransferType()                 == transferType &&
                sm.getNumBands()                     == numBands     &&
                (1 << sm.getSampleSize(visibleBand)) >= getMapSize();
    }
    
    
    /**
     * Returns the red color component for the specified pixel, scaled
     * from 0 to 255 in the default RGB <code>ColorSpace</code>, sRGB.  A
     * color conversion is done if necessary.  The pixel value is
     * specified by an array of data elements of type transferType passed
     * in as an object reference.  The returned value is a non
     * pre-multiplied value.  For example, if alpha is premultiplied,
     * this method divides it out before returning
     * the value.  If the alpha value is 0, the red value is 0.
     * If <code>inData</code> is not a primitive array of type
     * transferType, a <code>ClassCastException</code> is thrown.  An
     * <code>ArrayIndexOutOfBoundsException</code> is thrown if 
     * <code>inData</code> is not large enough to hold a pixel value for
     * this <code>ColorModel</code>.
     * If this <code>transferType</code> is not supported, a       
     * <code>UnsupportedOperationException</code> will be
     * thrown.  Since
     * <code>ColorModel</code> is an abstract class, any instance
     * must be an instance of a subclass.  Subclasses inherit the
     * implementation of this method and if they don't override it, this
     * method throws an exception if the subclass uses a
     * <code>transferType</code> other than
     * <code>DataBuffer.TYPE_BYTE</code>,
     * <code>DataBuffer.TYPE_USHORT</code>, or 
     * <code>DataBuffer.TYPE_INT</code>. 
     * @param inData an array of pixel values
     * @return the value of the red component of the specified pixel.
     * @throws ClassCastException if <code>inData</code>
     * 	is not a primitive array of type <code>transferType</code>
     * @throws ArrayIndexOutOfBoundsException if
     *	<code>inData</code> is not large enough to hold a pixel value
     *	for this <code>ColorModel</code>
     * @throws UnsupportedOperationException if this
     *	<code>tranferType</code> is not supported by this
     *	<code>ColorModel</code>
     */
    public int getRed(Object inData) {
        int pixel=0,length=0;
        switch (transferType) {
            case DataBuffer.TYPE_BYTE:
               byte bdata[] = (byte[])inData;
               pixel = bdata[visibleBand] & 0xff;
               length = bdata.length;
            break;
            case DataBuffer.TYPE_USHORT:
               short sdata[] = (short[])inData;
               pixel = sdata[visibleBand] & 0xffff;
               length = sdata.length;
            break;
            case DataBuffer.TYPE_INT:
               int idata[] = (int[])inData;
               pixel = idata[visibleBand];
               length = idata.length;
            break;
            default:
               throw new UnsupportedOperationException("This method has not been "+
                   "implemented for transferType " + transferType);
        }
        return getRed(pixel);
    }

    /**
     * Returns the green color component for the specified pixel, scaled
     * from 0 to 255 in the default RGB <code>ColorSpace</code>, sRGB.  A
     * color conversion is done if necessary.  The pixel value is
     * specified by an array of data elements of type transferType passed
     * in as an object reference.  The returned value will be a non
     * pre-multiplied value.  For example, if the alpha is premultiplied,
     * this method divides it out before returning the value.  If the
     * alpha value is 0, the green value is 0.  If <code>inData</code> is
     * not a primitive array of type transferType, a
     * <code>ClassCastException</code> is thrown.  An
     * <code>ArrayIndexOutOfBoundsException</code> is thrown if 
     * <code>inData</code> is not large enough to hold a pixel value for
     * this <code>ColorModel</code>.
     * If this <code>transferType</code> is not supported, a
     * <code>UnsupportedOperationException</code> will be
     * thrown.  Since
     * <code>ColorModel</code> is an abstract class, any instance
     * must be an instance of a subclass.  Subclasses inherit the
     * implementation of this method and if they don't override it, this
     * method throws an exception if the subclass uses a
     * <code>transferType</code> other than 
     * <code>DataBuffer.TYPE_BYTE</code>, 
     * <code>DataBuffer.TYPE_USHORT</code>, or  
     * <code>DataBuffer.TYPE_INT</code>.
     * @param inData an array of pixel values
     * @return the value of the green component of the specified pixel.
     * @throws <code>ClassCastException</code> if <code>inData</code>
     *  is not a primitive array of type <code>transferType</code>
     * @throws <code>ArrayIndexOutOfBoundsException</code> if
     *  <code>inData</code> is not large enough to hold a pixel value
     *  for this <code>ColorModel</code>
     * @throws <code>UnsupportedOperationException</code> if this
     *  <code>tranferType</code> is not supported by this 
     *  <code>ColorModel</code> 
     */
    public int getGreen(Object inData) {
        int pixel=0,length=0;
        switch (transferType) {
            case DataBuffer.TYPE_BYTE:
               byte bdata[] = (byte[])inData;
               pixel = bdata[visibleBand] & 0xff;
               length = bdata.length;
            break;
            case DataBuffer.TYPE_USHORT:
               short sdata[] = (short[])inData;
               pixel = sdata[visibleBand] & 0xffff;
               length = sdata.length;
            break;
            case DataBuffer.TYPE_INT:
               int idata[] = (int[])inData;
               pixel = idata[visibleBand];
               length = idata.length;
            break;
            default:
               throw new UnsupportedOperationException("This method has not been "+
                   "implemented for transferType " + transferType);
        }
        return getGreen(pixel);
    }
    
    /**
     * Returns the blue color component for the specified pixel, scaled
     * from 0 to 255 in the default RGB <code>ColorSpace</code>, sRGB.  A
     * color conversion is done if necessary.  The pixel value is
     * specified by an array of data elements of type transferType passed
     * in as an object reference.  The returned value is a non
     * pre-multiplied value.  For example, if the alpha is premultiplied,
     * this method divides it out before returning the value.  If the
     * alpha value is 0, the blue value will be 0.  If 
     * <code>inData</code> is not a primitive array of type transferType,
     * a <code>ClassCastException</code> is thrown.  An
     * <code>ArrayIndexOutOfBoundsException</code> is
     * thrown if <code>inData</code> is not large enough to hold a pixel
     * value for this <code>ColorModel</code>.
     * If this <code>transferType</code> is not supported, a
     * <code>UnsupportedOperationException</code> will be 
     * thrown.  Since
     * <code>ColorModel</code> is an abstract class, any instance
     * must be an instance of a subclass.  Subclasses inherit the
     * implementation of this method and if they don't override it, this
     * method throws an exception if the subclass uses a
     * <code>transferType</code> other than 
     * <code>DataBuffer.TYPE_BYTE</code>, 
     * <code>DataBuffer.TYPE_USHORT</code>, or  
     * <code>DataBuffer.TYPE_INT</code>.
     * @param inData an array of pixel values
     * @return the value of the blue component of the specified pixel.
     * @throws ClassCastException if <code>inData</code>
     *  is not a primitive array of type <code>transferType</code>
     * @throws ArrayIndexOutOfBoundsException if
     *  <code>inData</code> is not large enough to hold a pixel value
     *  for this <code>ColorModel</code>
     * @throws UnsupportedOperationException if this
     *  <code>tranferType</code> is not supported by this 
     *  <code>ColorModel</code> 
     */
    public int getBlue(Object inData) {
        int pixel=0,length=0;
        switch (transferType) {
            case DataBuffer.TYPE_BYTE:
               byte bdata[] = (byte[])inData;
               pixel = bdata[visibleBand] & 0xff;
               length = bdata.length;
            break;
            case DataBuffer.TYPE_USHORT:
               short sdata[] = (short[])inData;
               pixel = sdata[visibleBand] & 0xffff;
               length = sdata.length;
            break;
            case DataBuffer.TYPE_INT:
               int idata[] = (int[])inData;
               pixel = idata[visibleBand];
               length = idata.length;
            break;
            default:
               throw new UnsupportedOperationException("This method has not been "+
                   "implemented for transferType " + transferType);
        }
        return getBlue(pixel);
    }
    
    
    /**
     * Returns the alpha component for the specified pixel, scaled
     * from 0 to 255.  The pixel value is specified by an array of data
     * elements of type transferType passed in as an object reference.
     * If inData is not a primitive array of type transferType, a
     * <code>ClassCastException</code> is thrown.  An
     * <code>ArrayIndexOutOfBoundsException</code> is thrown if 
     * <code>inData</code> is not large enough to hold a pixel value for
     * this <code>ColorModel</code>.
     * If this <code>transferType</code> is not supported, a
     * <code>UnsupportedOperationException</code> will be 
     * thrown.  Since
     * <code>ColorModel</code> is an abstract class, any instance
     * must be an instance of a subclass.  Subclasses inherit the
     * implementation of this method and if they don't override it, this
     * method throws an exception if the subclass uses a
     * <code>transferType</code> other than 
     * <code>DataBuffer.TYPE_BYTE</code>, 
     * <code>DataBuffer.TYPE_USHORT</code>, or  
     * <code>DataBuffer.TYPE_INT</code>.
     * @param inData the specified pixel
     * @return the alpha component of the specified pixel, scaled from
     * 0 to 255.
     * @throws ClassCastException if <code>inData</code> 
     *  is not a primitive array of type <code>transferType</code>
     * @throws ArrayIndexOutOfBoundsException if
     *  <code>inData</code> is not large enough to hold a pixel value   
     *  for this <code>ColorModel</code>
     * @throws UnsupportedOperationException if this
     *  <code>tranferType</code> is not supported by this
     *  <code>ColorModel</code>
     */
    public int getAlpha(Object inData) {
        int pixel=0,length=0;
        switch (transferType) {
            case DataBuffer.TYPE_BYTE:
               byte bdata[] = (byte[])inData;
               pixel = bdata[visibleBand] & 0xff;
               length = bdata.length;
            break;
            case DataBuffer.TYPE_USHORT:
               short sdata[] = (short[])inData;
               pixel = sdata[visibleBand] & 0xffff;
               length = sdata.length;
            break;
            case DataBuffer.TYPE_INT:
               int idata[] = (int[])inData;
               pixel = idata[visibleBand];
               length = idata.length;
            break;
            default:
               throw new UnsupportedOperationException("This method has not been "+
                   "implemented for transferType " + transferType);
        }
        return getAlpha(pixel);
    }
    
    
}
