/*
 * Geotools - OpenSource mapping toolkit
 * (C) 2002, Centre for Computational Geography
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
package org.geotools.io.coverage;

// Input/output
import java.net.URL;
import java.io.File;
import java.io.IOException;

// Image input/output
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.IIOException;
import javax.imageio.ImageReadParam;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;
import org.geotools.io.image.RawBinaryImageReadParam;

// Images and geometry
import java.awt.Dimension;
import java.awt.image.RenderedImage;

// Miscellaneous
import java.util.Locale;
import java.util.Arrays;
import java.util.Iterator;
import java.util.MissingResourceException;

// Geotools dependencies
import org.geotools.pt.Envelope;
import org.geotools.gc.GridRange;
import org.geotools.gc.GridCoverage;
import org.geotools.cv.SampleDimension;
import org.geotools.cs.CoordinateSystem;
import org.geotools.resources.gcs.Resources;
import org.geotools.resources.gcs.ResourceKeys;


/**
 * Base class for reading {@link GridCoverage} objects.
 * Reading is a two steps process: The input file must
 * be set first, then the actual reading is performed
 * with the {@link #getGridCoverage}. Example:
 *
 * <blockquote><pre>
 * GridCoverageReader reader = ...
 * reader.{@link #setInput setInput}(new File("MyCoverage.dat"), true);
 * GridCoverage coverage = reader.{@link #getGridCoverage getGridCoverage}(0);
 * </pre></blockquote>
 *
 * Subclasses needs to implements at least the following methods:
 *
 * <ul>
 *   <li>{@link #getCoordinateSystem}</li>
 *   <li>{@link #getEnvelope}</li>
 * </ul>
 *
 * The default implementation should be able to create acceptable grid
 * coverage using informations provided by the two above-mentioned methods.
 * However, other methods may be overriden too in order to get finner control
 * on the result.
 *
 * @version $Id: GridCoverageReader.java,v 1.2 2002/07/17 23:30:56 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public abstract class GridCoverageReader {
    /**
     * The format name (e.g. "PNG" or "GeoTIFF"). This format name should
     * be understood by {@link ImageIO#getImageReadersByFormatName(String)},
     * unless {@link #getImageReaders} is overriden.
     */
    public final String formatName;
    
    /**
     * The {@link ImageReader} to use for decoding {@link RenderedImage}s.
     * This reader is initially <code>null</code> and lazily created the
     * first time {@link #setInput} is invoked. Once created, it is reused
     * as much as possible. Invoking {@link #reset} dispose the reader and
     * set it back to <code>null</code>.
     */
    protected ImageReader reader;
    
    /**
     * The input {@link File} or {@link URL}, or
     * <code>null</code> if input is not set.
     */
    private Object input;
    
    /**
     * The input stream, or <code>null</code> if input
     * is not set of if {@link #reader} accepted directly
     * {@link #input}.
     */
    private ImageInputStream stream;
    
    /**
     * The locale to use for formatting messages,
     * or <code>null</code> for a default locale.
     */
    private Locale locale;
    
    /**
     * Construct a <code>GridCoverageReader</code>
     * for the specified format name.
     */
    public GridCoverageReader(final String formatName) {
        this.formatName = formatName;
    }
    
    /**
     * Restores the <code>GridCoverageReader</code> to its initial state.
     *
     * @throws IOException if an error occurs while disposing resources.
     */
    public synchronized void reset() throws IOException {
        clear();
        locale = null;
        if (reader!=null) {
            reader.reset();
            reader.dispose();
            reader=null;
        }
    }
    
    /**
     * Clear this <code>GridCoverageReader</code>. This method is
     * similar to {@link #reset},  except that it doesn't destroy
     * the current {@link ImageReader} and keeps the locale setting.
     *
     * @throws IOException if an error occurs while disposing resources.
     */
    private void clear() throws IOException {
        assert Thread.holdsLock(this);
        input = null;
        if (reader!=null) {
            reader.setInput(null);
        }
        if (stream!=null) {
            stream.close();
            stream=null;
        }
    }
    
    /**
     * Sets the input source to the given object. The input is usually a
     * {@link File} or an {@link URL} object. But some other types (e.g.
     * {@link ImageInputStream}) may be accepted too.
     * <br><br>
     * If this method is invoked for the first time or after a call to
     * {@link #reset}, then it will queries @link #getImageReaders} for
     * a list of {@link ImageReader}s and select the first one that accept
     * the input.
     *
     * @param  input The {@link File} or {@link URL} to be read.
     * @param  seekForwardOnly if <code>true</code>, grid coverages
     *         and metadata may only be read in ascending order from
     *         the input source.
     * @throws IOException if an I/O operation failed.
     * @throws IllegalArgumentException if input is not an instance
     *         of one of the classes declared by the {@link ImageReader}
     *         service provider.
     */
    public synchronized void setInput(final Object input,
                                      final boolean seekForwardOnly)
        throws IOException
    {
        clear();
        if (input!=null) {
            ImageReader reader = this.reader;
            boolean reuseLast = (reader!=null);
            for (final Iterator it=getImageReaders(input); it.hasNext();) {
                if (!reuseLast) {
                    reader = (ImageReader) it.next();
                    setReaderLocale(locale);
                }
                reuseLast = false;
                final Class[] types = reader.getOriginatingProvider().getInputTypes();
                if (contains(types, input.getClass())) {
                    reader.setInput(input, seekForwardOnly);
                    this.input  = input;
                    this.reader = reader;
                    return;
                }
                if (contains(types, ImageInputStream.class)) {
                    assert stream==null;
                    stream = ImageIO.createImageInputStream(input);
                    if (stream!=null) {
                        reader.setInput(stream, seekForwardOnly);
                        this.input  = input;
                        this.reader = reader;
                        return;
                    }
                }
            }
            throw new IllegalArgumentException(getString(ResourceKeys.ERROR_NO_IMAGE_READER));
        }
    }
    
    /**
     * Check if the array <code>types</code> contains
     * <code>type</code> or a super-class of <code>type</code>.
     */
    private static boolean contains(final Class[] types, final Class type) {
        for (int i=0; i<types.length; i++) {
            if (types[i].isAssignableFrom(type)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Returns the number of images available from the current input source.
     * Note that some image formats do not specify how many images are present
     * in the stream. Thus determining the number of images will require the
     * entire stream to be scanned and may require memory for buffering.
     * The <code>allowSearch</code> parameter may be set to <code>false</code>
     * to indicate that an exhaustive search is not desired.
     *
     * @param  allowSearch If <code>true</code>, the true number of images will
     *         be returned even if a search is required. If <code>false</code>,
     *         the reader may return -1 without performing the search.
     * @return The number of images, or -1 if <code>allowSearch</code> is
     *         <code>false</code> and a search would be required.
     * @throws IllegalStateException If the input source has not been set, or if
     *         the input has been specified with <code>seekForwardOnly</code> set
     *         to <code>true</code>.
     * @throws IOException If an error occurs reading the information from the input
     *         source.
     */
    public int getNumImages(final boolean allowSearch) throws IOException {
        if (reader==null) {
            throw new IllegalStateException(getString(ResourceKeys.ERROR_NO_IMAGE_INPUT));
        }
        return reader.getNumImages(allowSearch);
    }
    
    /**
     * Vérifie si l'index de l'image est dans la plage des valeurs
     * autorisées. L'index maximal autorisé est obtenu en appelant
     * <code>{@link #getNumImages getNumImages}(false)</code>.
     *
     * @param  imageIndex Index dont on veut vérifier la validité.
     * @throws IndexOutOfBoundsException si l'index spécifié n'est pas valide.
     * @throws IOException si l'opération a échouée à cause d'une erreur d'entrés/sorties.
     */
    final void checkImageIndex(final int imageIndex) throws IOException, IndexOutOfBoundsException {
        if (reader==null) {
            throw new IllegalStateException(getString(ResourceKeys.ERROR_NO_IMAGE_INPUT));
        }
        final int numImages = getNumImages(false);
        if (imageIndex<reader.getMinIndex() || (imageIndex>=numImages && numImages>=0)) {
            throw new IndexOutOfBoundsException(String.valueOf(imageIndex));
        }
    }
    
    /**
     * Gets the {@link GridCoverage} name at the specified index.
     * Default implementation returns the input path, or the
     * "Untitled" string if input is not a {@link File} or an
     * {@link URL} object.
     *
     * @param  index The index of the image to be queried.
     * @return The name for the {@link GridCoverage} at the specified index.
     * @throws IllegalStateException if the input source has not been set.
     * @throws IndexOutOfBoundsException if the supplied index is out of bounds.
     * @throws IOException if an error occurs reading the width information from the input source.
     */
    public String getName(final int index) throws IOException {
        checkImageIndex(index);
        if (input instanceof File) {
            return ((File) input).getName();
        }
        if (input instanceof URL) {
            return new File(((URL) input).getPath()).getName();
        }
        return getString(ResourceKeys.UNTITLED);
    }
    
    /**
     * Returns the coordinate system for the {@link GridCoverage} to be read.
     *
     * @param  index The index of the image to be queried.
     * @return The coordinate system for the {@link GridCoverage} at the specified index.
     * @throws IllegalStateException if the input source has not been set.
     * @throws IndexOutOfBoundsException if the supplied index is out of bounds.
     * @throws IOException if an error occurs reading the width information from the input source.
     */
    public abstract CoordinateSystem getCoordinateSystem(int index) throws IOException;
    
    /**
     * Returns the envelope for the {@link GridCoverage} to be read.
     * The envelope must have the same number of dimensions than the
     * coordinate system.
     *
     * @param  index The index of the image to be queried.
     * @return The envelope for the {@link GridCoverage} at the specified index.
     * @throws IllegalStateException if the input source has not been set.
     * @throws IndexOutOfBoundsException if the supplied index is out of bounds.
     * @throws IOException if an error occurs reading the width information from
     *         the input source.
     */
    public abstract Envelope getEnvelope(int index) throws IOException;
    
    /**
     * Returns the grid range for the {@link GridCoverage} to be read.
     * The grid range must have the same number of dimensions than the
     * envelope.
     *
     * The default implementation construct a {@link GridRange} object
     * using information provided by {@link ImageReader#getWidth} and
     * {@link ImageReader#getHeight}.
     *
     * @param  index The index of the image to be queried.
     * @return The grid range for the {@link GridCoverage} at the specified index.
     * @throws IllegalStateException if the input source has not been set.
     * @throws IndexOutOfBoundsException if the supplied index is out of bounds.
     * @throws IOException if an error occurs reading the width information from
     *         the input source.
     */
    public synchronized GridRange getGridRange(final int index) throws IOException {
        checkImageIndex(index);
        final int dimension = getCoordinateSystem(index).getDimension();
        final int[]   lower = new int[dimension];
        final int[]   upper = new int[dimension];
        Arrays.fill(upper, 1);
        upper[0] = reader.getWidth(index);
        upper[1] = reader.getHeight(index);
        return new GridRange(lower, upper);
    }
    
    /**
     * Returns the sample dimensions for each band of the {@link GridCoverage}
     * to be read. If sample dimensions are not known, then this method returns
     * <code>null</code>. The default implementation always returns <code>null</code>.
     *
     * @param  index The index of the image to be queried.
     * @return The category lists for the {@link GridCoverage} at the specified index.
     *         This array's length must be equals to the number of bands in {@link GridCoverage}.
     * @throws IllegalStateException if the input source has not been set.
     * @throws IndexOutOfBoundsException if the supplied index is out of bounds.
     * @throws IOException if an error occurs reading the width information from
     *         the input source.
     */
    public synchronized SampleDimension[] getSampleDimensions(final int index) throws IOException {
        checkImageIndex(index);
        return null;
    }
    
    /**
     * Tells if pixel values map directly geophysics values. This method
     * Returns <code>true</code> if pixel values map directly geophysics
     * values, or <code>false</code> if they must be translated first
     * using {@link SampleDimension}. The default implementation returns
     * <code>true</code>.
     *
     * @param  index The index of the image to be queried.
     * @return <code>true</code> if pixel values map directly geophysics values.
     * @throws IllegalStateException if the input source has not been set.
     * @throws IndexOutOfBoundsException if the supplied index is out of bounds.
     * @throws IOException if an error occurs reading the width information from
     *         the input source.
     */
    public synchronized boolean isGeophysics(final int index) throws IOException {
        checkImageIndex(index);
        return true;
    }
    
    /**
     * Read the grid coverage. The default implementation gets the
     * default {@link ImageReadParam} and checks if it is an instance of
     * {@link RawBinaryImageReadParam}. If it is, this method then invokes
     * {@link RawBinaryImageReadParam#setStreamImageSize} with informations
     * provided by {@link #getGridRange}. Finally, a grid coverage is
     * constructed using informations provided by {@link #getName},
     * {@link #getCoordinateSystem} and {@link #getEnvelope}.
     *
     * @param  index The index of the image to be queried.
     * @return The {@link GridCoverage} at the specified index.
     * @throws IllegalStateException if the input source has not been set.
     * @throws IndexOutOfBoundsException if the supplied index is out of bounds.
     * @throws IOException if an error occurs reading the width information from
     *         the input source.
     */
    public synchronized GridCoverage getGridCoverage(final int index) throws IOException {
        checkImageIndex(index);
        final ImageReadParam param = reader.getDefaultReadParam();
        if (param instanceof RawBinaryImageReadParam) {
            final RawBinaryImageReadParam rawParam = (RawBinaryImageReadParam) param;
            final GridRange range = getGridRange(index);
            final Dimension  size = new Dimension(range.getLength(0), range.getLength(1));
            rawParam.setStreamImageSize(size);
        }
        final String          name = getName(index);
        final Envelope    envelope = getEnvelope(index);
        final CoordinateSystem  cs = getCoordinateSystem(index);
        final SampleDimension[] sd = getSampleDimensions(index);
        final boolean isGeophysics = isGeophysics(index);
        final RenderedImage  image = reader.readAsRenderedImage(index, param);
        return new GridCoverage(name, image, cs, envelope, sd, isGeophysics, null, null);
    }
    
    /**
     * Returns an {@link Iterator} containing all currently registered
     * {@link ImageReader}s that claim to be able to decode the image.
     * The default implementation returns
     * <code>ImageIO.getImageReadersByFormatName({@link #formatName})</code>.
     *
     * @param input The input source.
     */
    protected Iterator getImageReaders(final Object input) {
        return ImageIO.getImageReadersByFormatName(formatName);
    }
    
    /**
     * Returns a localized string for the specified key.
     */
    final String getString(final int key) {
        return Resources.getResources(locale).getString(key);
    }
    
    /**
     * Sets the current {@link Locale} of this <code>GridCoverageReader</code>
     * to the given value. A value of <code>null</code> removes any previous
     * setting, and indicates that the reader should localize as it sees fit.
     */
    public synchronized void setLocale(final Locale locale) {
        this.locale = locale;
        setReaderLocale(locale);
    }
    
    /**
     * Set the locale for the {@link ImageReader}.
     */
    private void setReaderLocale(final Locale locale) {
        if (reader!=null) {
            final Locale[] list = reader.getAvailableLocales();
            for (int i=list.length; --i>=0;) {
                if (locale.equals(list[i])) {
                    reader.setLocale(locale);
                    return;
                }
            }
            final String language = getISO3Language(locale);
            if (language!=null) {
                for (int i=list.length; --i>=0;) {
                    if (language.equals(getISO3Language(list[i]))) {
                        reader.setLocale(list[i]);
                        return;
                    }
                }
            }
            reader.setLocale(null);
        }
    }
    
    /**
     * Returns the ISO language code for the specified
     * locale, or <code>null</code> if not available.
     */
    private static String getISO3Language(final Locale locale) {
        try {
            return locale.getISO3Language();
        } catch (MissingResourceException exception) {
            return null;
        }
    }
    
    /**
     * Returns the currently set {@link Locale},
     * or <code>null</code> if none has been set.
     */
    public Locale getLocale() {
        return locale;
    }
}
