/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Management Committee (PMC)
 * (C) 2001, Institut de Recherche pour le Développement
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
 */
package org.geotools.io.image;

// Input/output
import java.io.IOException;
import java.io.BufferedReader;
import java.text.ParseException;
import javax.imageio.ImageReader;
import javax.imageio.IIOException;

// Image
import java.awt.image.Raster;
import java.awt.image.DataBuffer;
import java.awt.image.SampleModel;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.awt.image.DataBufferFloat;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.spi.ImageReaderSpi;

// Miscellaneous
import java.awt.Point;
import java.awt.Rectangle;
import javax.media.jai.util.Range;
import java.util.Locale;

// Resources
import org.geotools.io.LineFormat;
import org.geotools.resources.XArray;
import org.geotools.resources.gcs.Resources;
import org.geotools.resources.gcs.ResourceKeys;


/**
 * An image decoder for matrix of floating-point numbers.
 *
 * @version 1.0
 * @author Martin Desruisseaux
 */
public class TextMatrixImageReader extends TextImageReader {
    /**
     * The matrix data.
     */
    private float[] data;
    
    /**
     * The image width. This number has no
     * signification if {@link #data} is null.
     */
    private int width;
    
    /**
     * The image height. This number is valid
     * only if {@link #completed} is true.
     */
    private int height;
    
    /**
     * The expected height, or 0 if unknow. This number
     * has no signification if {@link #data} is null.
     */
    private int expectedHeight;
    
    /**
     * <code>true</code> if {@link #data} contains all data, or <code>false</code>
     * if {@link #data} contains only the first line. This field has no signification
     * if {@link #data} is null.
     */
    private boolean completed;
    
    /**
     * The cached range.
     */
    private Range range;
    
    /**
     * Construct a new image reader storing pixels
     * as {@link DataBuffer#TYPE_FLOAT}.
     *
     * @param provider the {@link ImageReaderSpi} that is
     *                 invoking this constructor, or null.
     */
    protected TextMatrixImageReader(final ImageReaderSpi provider) {
        super(provider, DataBuffer.TYPE_FLOAT);
    }
    
    /**
     * Clear all cached data.
     */
    private void clear() {
        completed      = false;
        range          = null;
        data           = null;
        width          = 0;
        height         = 0;
        expectedHeight = 0;
    }
    
    /**
     * Restores the image reader to its initial state.
     */
    public void reset() {
        clear();
        super.reset();
    }
    
    /**
     * Sets the input source to use. Input may be one of the following object (in preference
     * order): {@link java.io.File}, {@link java.net.URL}, {@link java.io.BufferedReader},
     * {@link java.io.Reader}, {@link java.io.InputStream} or {@link javax.imageio.stream.ImageInputStream}.
     */
    public void setInput(final Object  input,
                         final boolean seekForwardOnly,
                         final boolean ignoreMetadata)
    {
        clear();
        super.setInput(input, seekForwardOnly, ignoreMetadata);
    }
    
    /**
     * Load data. No subsampling is performed.
     *
     * @param  imageIndex the index of the image to be read.
     * @param  all <code>true</code> to read all data, or <code>false</code> to read only the first line.
     * @return <code>true</code> if reading has been aborted.
     * @throws IOException If an error occurs reading the width information from the input source.
     */
    private boolean load(final int imageIndex, final boolean all) throws IOException {
        clearAbortRequest();
        if (all) {
            processImageStarted(imageIndex);
        }
        float[] values = (data!=null) ? new float[width] : null;
        int     offset = width*height;
        
        final BufferedReader input = getReader();
        final LineFormat    format = getLineFormat(imageIndex);
        final float       padValue = (float)getPadValue(imageIndex);
        String line; while ((line=input.readLine())!=null) {
            try {
                format.setLine(line);
                values = format.getValues(values);
                for (int i=values.length; --i>=0;) {
                    if (values[i]==padValue) {
                        values[i]=Float.NaN;
                    }
                }
            } catch (ParseException exception) {
                throw new IIOException(getPositionString(exception.getLocalizedMessage()), exception);
            }
            if (data==null) {
                data = new float[1024];
            }
            final int newOffset = offset + (width=values.length);
            if (newOffset > data.length) {
                data = XArray.resize(data, newOffset+Math.min(newOffset, 65536));
            }
            System.arraycopy(values, 0, data, offset, width);
            offset = newOffset;
            height++;
            /*
             * If only one line was requested, try
             * to guess the expected height.
             */
            if (!all) {
                final long streamLength = getStreamLength(imageIndex, imageIndex+1);
                if (streamLength >= 0) {
                    expectedHeight = (int) (streamLength / (line.length()+1));
                }
                break;
            }
            /*
             * Update progress.
             */
            if (height<=expectedHeight) {
                processImageProgress(height*100f/expectedHeight);
                if (abortRequested()) {
                    processReadAborted();
                    return true;
                }
            }
        }
        if (completed=all) {
            data = XArray.resize(data, offset);
            expectedHeight = height;
        }
        if (all) {
            processImageComplete();
        }
        return false;
    }
    
    /**
     * Returns the width in pixels of the given image within the input source.
     *
     * @param  imageIndex the index of the image to be queried.
     * @return Image width.
     * @throws IOException If an error occurs reading the width information
     *         from the input source.
     */
    public int getWidth(final int imageIndex) throws IOException {
        checkImageIndex(imageIndex);
        if (data==null) {
            load(imageIndex, false);
        }
        return width;
    }
    
    /**
     * Returns the height in pixels of the given image within the input source.
     * Calling this method may force loading of full image.
     *
     * @param  imageIndex the index of the image to be queried.
     * @return Image height.
     * @throws IOException If an error occurs reading the height information
     *         from the input source.
     */
    public int getHeight(final int imageIndex) throws IOException {
        checkImageIndex(imageIndex);
        if (data==null || !completed) {
            load(imageIndex, true);
        }
        return height;
    }
    
    /**
     * Reads the image indexed by <code>imageIndex</code>.
     *
     * @param  imageIndex  The index of the image to be retrieved.
     * @param  param       Parameters used to control the reading process, or null.
     * @return The desired portion of the image.
     * @throws IOException if an input operation failed.
     */
    public BufferedImage read(final int imageIndex, final ImageReadParam param) throws IOException {
        /*
         * Parameters check.
         */
        final int numSrcBands = 1;
        final int numDstBands = 1;
        checkImageIndex(imageIndex);
        checkReadParamBandSettings(param, numSrcBands, numDstBands);
        /*
         * Extract user's parameters.
         */
        final int[]      sourceBands;
        final int[] destinationBands;
        final int sourceXSubsampling;
        final int sourceYSubsampling;
        final int subsamplingXOffset;
        final int subsamplingYOffset;
        final int destinationXOffset;
        final int destinationYOffset;
        if (param != null) {
            sourceBands        = param.getSourceBands();
            destinationBands   = param.getDestinationBands();
            final Point offset = param.getDestinationOffset();
            sourceXSubsampling = param.getSourceXSubsampling();
            sourceYSubsampling = param.getSourceYSubsampling();
            subsamplingXOffset = param.getSubsamplingXOffset();
            subsamplingYOffset = param.getSubsamplingYOffset();
            destinationXOffset = offset.x;
            destinationYOffset = offset.y;
        } else {
            sourceBands        = null;
            destinationBands   = null;
            sourceXSubsampling = 1;
            sourceYSubsampling = 1;
            subsamplingXOffset = 0;
            subsamplingYOffset = 0;
            destinationXOffset = 0;
            destinationYOffset = 0;
        }
        /*
         * Compute source region and check for possible optimization.
         */
        final Rectangle srcRegion = getSourceRegion(param, width, height);
        final boolean isDirect = sourceXSubsampling==1 && sourceYSubsampling==1    &&
        subsamplingXOffset==0 && subsamplingYOffset==0    &&
        destinationXOffset==0 && destinationYOffset==0    &&
        srcRegion.x       ==0 && srcRegion.width ==width  &&
        srcRegion.y       ==0 && srcRegion.height==height;
        /*
         * Read data if it was not already done.
         */
        if (data==null || !completed) {
            if (load(imageIndex, true)) {
                return null;
            }
        }
        /*
         * If a direct mapping is possible, perform it.
         */
        if (isDirect && (param==null || param.getDestination()==null)) {
            final ImageTypeSpecifier type = getRawImageType(imageIndex);
            final SampleModel       model = type.getSampleModel().createCompatibleSampleModel(width,height);
            final DataBuffer       buffer = new DataBufferFloat(data, data.length);
            final WritableRaster   raster = Raster.createWritableRaster(model, buffer, null);
            return new BufferedImage(type.getColorModel(), raster, false, null);
        }
        /*
         * Copy data into a new image.
         */
        final int              dstBand = 0;
        final BufferedImage      image = getDestination(param, getImageTypes(imageIndex), width, height);
        final WritableRaster dstRaster = image.getRaster();
        final Rectangle      dstRegion = new Rectangle();
        computeRegions(param, width, height, image, srcRegion, dstRegion);
        final int dstXMin = dstRegion.x;
        final int dstYMin = dstRegion.y;
        final int dstXMax = dstRegion.width  + dstXMin;
        final int dstYMax = dstRegion.height + dstYMin;
        
        int srcY = srcRegion.y;
        for (int y=dstYMin; y<dstYMax; y++) {
            assert(srcY < srcRegion.y+srcRegion.height);
            int srcX = srcRegion.x;
            for (int x=dstXMin; x<dstXMax; x++) {
                assert(srcX < srcRegion.x+srcRegion.width);
                final float value = data[srcY*width+srcX];
                dstRaster.setSample(x, y, dstBand, value);
                srcX += sourceXSubsampling;
            }
            srcY += sourceYSubsampling;
        }
        return image;
    }
    
    /**
     * Returns the expected range of values for a band.
     * Calling this method may force loading of full image.
     *
     * @param  imageIndex The image index.
     * @param  bandIndex The band index.
     * @return The expected range of values, or <code>null</code> if unknow.
     * @throws IOException If an error occurs reading the data information from the input source.
     */
    public Range getExpectedRange(final int imageIndex, final int bandIndex) throws IOException {
        checkBandIndex(imageIndex, bandIndex);
        if (range==null) {
            load(imageIndex, true);
            float minimum = Float.POSITIVE_INFINITY;
            float maximum = Float.NEGATIVE_INFINITY;
            for (int i=0; i<data.length; i++) {
                final float value = data[i];
                if (value<minimum) minimum=value;
                if (value>maximum) maximum=value;
            }
            if (minimum<maximum) {
                range = new Range(Float.class, new Float(minimum), new Float(maximum));
            }
        }
        return range;
    }
    
    
    
    
    /**
     * Service provider interface (SPI) for {@link TextMatrixImageReader}s.
     * This SPI provides all necessary implementations for creating default
     * {@link TextMatrixImageReader}. Subclasses only have to set some fields
     * at construction time, e.g.:
     *
     * <blockquote><pre>
     * public final class MyCustomSpi extends TextMatrixImageReader.Spi
     * {
     *     public MyCustomSpi()
     *     {
     *         super("MyCustomFormat", "text/matrix");
     *         {@link #vendorName vendorName} = "Institut de Recherche pour le Développement";
     *         {@link #version    version}    = "1.0";
     *         {@link #locale     locale}     = Locale.US;
     *         {@link #charset    charset}    = Charset.forName("ISO-LATIN-1");
     *         {@link #padValue   padValue}   = 9999;
     *     }
     * }
     * </pre></blockquote>
     *
     * (Note: fields <code>vendorName</code> and <code>version</code> are only informatives).
     * There is no need to override any method in this example. However, developers
     * can gain more control by creating subclasses of {@link TextMatrixImageReader}
     * <strong>and</strong> <code>Spi</code> and overriding some of their methods.
     *
     * @version 1.0
     * @author Martin Desruisseaux
     */
    public static class Spi extends TextImageReader.Spi {
        /**
         * Construct a new SPI with name "matrix" and MIME type "text/matrix".
         */
        public Spi() {
            this("matrix", "text/matrix");
        }
        
        /**
         * Construct a new SPI for {@link TextMatrixImageReader}. This
         * constructor initialize the following fields to default
         * values:
         *
         * <ul>
         *   <li>Image format names ({@link #names}):
         *       An array of lenght 1 containing the <code>name</code> argument.
         *
         *   <li>MIME type ({@link #MIMETypes}):
         *       An array of length 1 containing the <code>mime</code> argument.
         *
         *   <li>File suffixes ({@link #suffixes}):
         *       "<code>.txt</code>", "<code>.asc</code>" et "<code>.dat</code>"
         *       (uppercase and lowercase).</li>
         *
         *   <li>Input types ({@link #inputTypes}):
         *       {@link java.io.File}, {@link java.net.URL}, {@link java.io.Reader},
         *       {@link java.io.InputStream} et {@link javax.imageio.stream.ImageInputStream}.</li>
         * </ul>
         *
         * @param name Format name, or <code>null</code> to let {@link #names} unset.
         * @param mime MIME type, or <code>null</code> to let {@link #MIMETypes} unset.
         */
        public Spi(final String name, final String mime) {
            super(name, mime);
            pluginClassName = "org.geotools.io.image.TextMatrixImageReader";
        }
        
        /**
         * Returns a brief, human-readable description of this service provider
         * and its associated implementation. The resulting string should be
         * localized for the supplied locale, if possible.
         *
         * @param  locale A Locale for which the return value should be localized.
         * @return A String containing a description of this service provider.
         */
        public String getDescription(final Locale locale) {
            return Resources.getResources(locale).getString(ResourceKeys.CODEC_MATRIX);
        }
        
        /**
         * Returns an instance of the ImageReader implementation associated
         * with this service provider.
         *
         * @param  extension An optional extension object, which may be null.
         * @return An image reader instance.
         * @throws IOException if the attempt to instantiate the reader fails.
         */
        public ImageReader createReaderInstance(final Object extension) throws IOException {
            return new TextMatrixImageReader(this);
        }
        
        /**
         * Vérifie si la ligne a un nombre de valeurs acceptable. Cette méthode est appelée
         * automatiquement par {@link #canDecodeLine} avec en argument le nombre de valeurs
         * dans une des premières lignes trouvées dans la source. Cette indication n'est
         * qu'approximative et il est correct de retourner {@link Boolean#FALSE} de façon
         * conservative.
         */
        Boolean isValueCountAcceptable(final int count) {
            return count>10 ? Boolean.TRUE : Boolean.FALSE;
        }
    }
}
