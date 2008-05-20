/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2008, GeoTools Project Managment Committee (PMC)
 *    (C) 2008, Geomatys
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
 */
package org.geotools.image.io.mosaic;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.ImageReaderSpi;

import org.opengis.geometry.Envelope;
import org.opengis.referencing.datum.PixelInCell;

import org.geotools.math.XMath;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.coverage.grid.GridRange2D;
import org.geotools.coverage.grid.ImageGeometry;
import org.geotools.referencing.operation.builder.GridToEnvelopeMapper;
import org.geotools.resources.XArray;
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.image.ImageUtilities;


/**
 * A convenience class for building tiles using the same {@linkplain ImageReader image reader}
 * and organized according some common {@linkplain TileLayout tile layout}. Optionally, this
 * builder can also write the tiles to disk from an initially untiled image.
 *
 * @since 2.5
 * @source $URL$
 * @version $Id$
 * @author Cédric Briançon
 * @author Martin Desruisseaux
 */
public class MosaicBuilder {
    /**
     * The default tile size in pixels.
     */
    private static final int DEFAULT_TILE_SIZE = 512;

    /**
     * Minimum tile size when using {@link TileLayout#CONSTANT_GEOGRAPHIC_AREA} without
     * explicit subsamplings provided by user.
     */
    private static final int MIN_TILE_SIZE = 64;

    /**
     * The factory to use for creating {@link TileManager} instances.
     */
    protected final TileManagerFactory factory;

    /**
     * The desired layout.
     */
    private TileLayout layout;

    /**
     * The tile directory, or {@code null} for current directory.
     * It may be either a relative or absolute path.
     */
    private File directory;

    /**
     * The image reader provider. The initial value is {@code null}.
     * This value must be set before {@link Tile} objects are created.
     */
    private ImageReaderSpi tileReaderSpi;

    /**
     * The envelope for the mosaic as a whole, or {@code null} if none. This is optional, but
     * if specified this builder uses it for assigning values to {@link Tile#getGridToCRS}.
     */
    private GeneralEnvelope mosaicEnvelope;

    /**
     * The raster bounding box in pixel coordinates. The initial value is {@code null}.
     * This value must be set before {@link Tile} objects are created.
     */
    private Rectangle untiledBounds;

    /**
     * The desired tile size. The initial value is {@code null}.
     * This value must be set before {@link Tile} objects are created.
     */
    private Dimension tileSize;

    /**
     * The subsamplings to use when creating a new overview. Values at even index are
     * <var>x</var> subsamplings and values at odd index are <var>y</var> subsamplings.
     * If {@code null}, subsampling will be computed automatically from the image and
     * tile size in order to get only entire tiles.
     */
    private int[] subsamplings;

    /**
     * The filename formatter.
     */
    private final FilenameFormatter formatter;

    /**
     * The logging level for tiling information during reads and writes.
     */
    private Level level = Level.FINE;

    /**
     * Generates tiles using the default factory.
     */
    public MosaicBuilder() {
        this(null);
    }

    /**
     * Generates tiles using the specified factory.
     *
     * @param factory The factory to use, or {@code null} for the
     *        {@linkplain TileManagerFactory#DEFAULT default} one.
     */
    public MosaicBuilder(final TileManagerFactory factory) {
        this.factory = (factory != null) ? factory : TileManagerFactory.DEFAULT;
        layout = TileLayout.CONSTANT_TILE_SIZE;
        formatter = new FilenameFormatter();
    }

    /**
     * Returns the logging level for tile information during read and write operations.
     *
     * @return The current logging level.
     */
    public Level getLogLevel() {
        return level;
    }

    /**
     * Sets the logging level for tile information during read and write operations.
     * The default value is {@link Level#FINE}. A {@code null} value restore the default.
     *
     * @param level The new logging level.
     */
    public void setLogLevel(Level level) {
        if (level == null) {
            level = Level.FINE;
        }
        this.level = level;
    }

    /**
     * Returns the tile layout. The default value is
     * {@link TileLayout#CONSTANT_TILE_SIZE CONSTANT_TILE_SIZE}, which is the most efficient
     * layout available in {@code org.geotools.image.io.mosaic} implementation.
     *
     * @return An identification of current tile layout.
     */
    public TileLayout getTileLayout() {
        return layout;
    }

    /**
     * Sets the tile layout to the specified value. Valid values are
     * {@link TileLayout#CONSTANT_TILE_SIZE CONSTANT_TILE_SIZE} and
     * {@link TileLayout#CONSTANT_GEOGRAPHIC_AREA CONSTANT_GEOGRAPHIC_AREA}.
     *
     * @param layout An identification of new tile layout.
     */
    public void setTileLayout(final TileLayout layout) {
        if (layout != null) {
            switch (layout) {
                case CONSTANT_TILE_SIZE:
                case CONSTANT_GEOGRAPHIC_AREA: {
                    this.layout = layout;
                    return;
                }
            }
        }
        throw new IllegalArgumentException(Errors.format(
                ErrorKeys.ILLEGAL_ARGUMENT_$2, "layout", layout));
    }

    /**
     * Returns the tile directory, or {@code null} for current directory. The directory
     * may be either relative or absolute. The default value is {@code null}.
     *
     * @return The current tiles directory.
     */
    public File getTileDirectory() {
        return directory;
    }

    /**
     * Sets the directory where tiles will be read or written. May be a relative or absolute
     * path, or {@code null} (the default) for current directory.
     *
     * @param directory The new tiles directory.
     */
    public void setTileDirectory(final File directory) {
        this.directory = directory;
    }

    /**
     * Returns the {@linkplain ImageReader image reader} provider to use for reading tiles.
     * The initial value is {@code null}, which means that the provider should be the same
     * than the one detected by {@link #writeFromUntiledImage writeFromUntiledImage}.
     *
     * @return The current image reader provider for tiles.
     */
    public ImageReaderSpi getTileReaderSpi() {
        return tileReaderSpi;
    }

    /**
     * Sets the {@linkplain ImageReader image reader} provider for each tiles to be read.
     * A {@code null} value means that the provider should be automatically detected by
     * {@link #writeFromUntiledImage writeFromUntiledImage}.
     *
     * @param provider The new image reader provider for tiles.
     */
    public void setTileReaderSpi(final ImageReaderSpi provider) {
        this.tileReaderSpi = provider;
    }

    /**
     * Sets the {@linkplain ImageReader image reader} provider by name. This convenience method
     * searchs a provider for the given name in the default {@link IIORegistry} and delegates to
     * {@link #setTileReaderSpi(ImageReaderSpi)}.
     *
     * @param format The image format name for tiles.
     * @throws IllegalArgumentException if no provider was found for the given name.
     */
    public void setTileReaderSpi(String format) throws IllegalArgumentException {
        ImageReaderSpi spi = null;
        if (format != null) {
            format = format.trim();
            final IIORegistry registry = IIORegistry.getDefaultInstance();
            final Iterator<ImageReaderSpi> it=registry.getServiceProviders(ImageReaderSpi.class, true);
            do {
                if (!it.hasNext()) {
                    throw new IllegalArgumentException(Errors.format(
                            ErrorKeys.UNKNOW_IMAGE_FORMAT_$1, format));
                }
                spi = it.next();
            } while (!XArray.contains(spi.getFormatNames(), format));
        }
        setTileReaderSpi(spi);
    }

    /**
     * Returns the envelope for the mosaic as a whole, or {@code null} if none. This is optional,
     * but if specified this builder uses it for assigning values to {@link Tile#getGridToCRS}.
     *
     * @return The current envelope, or {@code null} if none.
     */
    public Envelope getMosaicEnvelope() {
        return (mosaicEnvelope != null) ? mosaicEnvelope.clone() : null;
    }

    /**
     * Sets the envelope for the mosaic as a whole, or {@code null} if none. This is optional,
     * but if specified this builder uses it for assigning values to {@link Tile#getGridToCRS}.
     * <p>
     * This is merely a convenient way to invoke {@link TileManager#setGridToCRS} with a transform
     * computed from the envelope and the {@linkplain #getUntiledImageBounds untiled image bounds},
     * where the later may be known only at reading time. As always, creating "grid to CRS" from an
     * envelope is ambiguous, since we don't know if axis need to be interchanged, <var>y</var> axis
     * flipped, <cite>etc.</cite> Subclasses can gain more control by overriding the
     * {@link #createGridToEnvelopeMapper createGridToEnvelopeMapper} method. The default behavior
     * fits most typical cases however.
     *
     * @param envelope The new envelope, or {@code null} if none.
     *
     * @see #createGridToEnvelopeMapper
     */
    public void setMosaicEnvelope(final Envelope envelope) {
        mosaicEnvelope = (envelope != null) ? new GeneralEnvelope(envelope) : null;
    }

    /**
     * Returns the bounds of the untiled image, or {@code null} if not set. In the later case, the
     * bounds will be inferred from the input image when {@link #writeFromUntiledImage} is invoked.
     *
     * @return The current untiled image bounds.
     */
    public Rectangle getUntiledImageBounds() {
        return (untiledBounds != null) ? (Rectangle) untiledBounds.clone() : null;
    }

    /**
     * Sets the bounds of the untiled image to the specified value.
     * A {@code null} value discarts any value previously set.
     *
     * @param bounds The new untiled image bounds.
     */
    public void setUntiledImageBounds(final Rectangle bounds) {
        untiledBounds = (bounds != null) ? new Rectangle(bounds) : null;
    }

    /**
     * Returns the tile size. If no tile size has been explicitly set, then a default tile size
     * will be computed from the {@linkplain #getUntiledImageBounds untiled image bounds}. If no
     * size can be computed, then this method returns {@code null}.
     *
     * @return The current tile size.
     *
     * @see #suggestTileSize
     */
    public Dimension getTileSize() {
        if (tileSize == null) {
            final Rectangle untiledBounds = getUntiledImageBounds();
            if (untiledBounds == null) {
                return null;
            }
            int width  = untiledBounds.width;
            int height = untiledBounds.height;
            width  = suggestTileSize(width);
            height = (height == untiledBounds.width) ? width : suggestTileSize(height);
            tileSize = new Dimension(width, height);
        }
        return (Dimension) tileSize.clone();
    }

    /**
     * Sets the tile size. A {@code null} value discarts any value previously set.
     *
     * @param size The new tile size.
     */
    public void setTileSize(final Dimension size) {
        if (size == null) {
            tileSize = null;
        } else {
            if (size.width < 2 || size.height < 2) {
                throw new IllegalArgumentException(Errors.format(
                        ErrorKeys.ILLEGAL_ARGUMENT_$1, "size"));
            }
            tileSize = new Dimension(size);
        }
    }

    /**
     * Suggests a tile size using default values.
     */
    private static int suggestTileSize(final int imageSize) {
        return suggestTileSize(imageSize, DEFAULT_TILE_SIZE,
                DEFAULT_TILE_SIZE - DEFAULT_TILE_SIZE/4, DEFAULT_TILE_SIZE + DEFAULT_TILE_SIZE/4);
    }

    /**
     * Suggests a tile size ({@linkplain Dimension#width width} or {@linkplain Dimension#height
     * height}) for the given image size. This methods search for a value <var>x</var> inside the
     * {@code [minSize...maxSize]} range where {@code imageSize}/<var>x</var> has the largest amount
     * of {@linkplain XMath#divisors divisors}. If more than one value have the same amount of
     * divisors, then the one which is the closest to {@code tileSize} is returned.
     *
     * @param  imageSize The image size.
     * @param  tileSize  The preferred tile size. Must be inside the {@code [minSize...maxSize]} range.
     * @param  minSize   The minimum size, inclusive. Must be greater than 0.
     * @param  maxSize   The maximum size, inclusive. Must be equals or greater that {@code minSize}.
     * @return The suggested tile size. Inside the {@code [minSize...maxSize]} range except
     *         if {@code imageSize} was smaller than {@link minSize}.
     * @throws IllegalArgumentException if any argument doesn't meet the above-cited conditions.
     */
    public static int suggestTileSize(final int imageSize, final int tileSize,
                                      final int minSize,   final int maxSize)
            throws IllegalArgumentException
    {
        if (minSize <= 1 || minSize > maxSize) {
            throw new IllegalArgumentException(Errors.format(
                    ErrorKeys.BAD_RANGE_$2, minSize, maxSize));
        }
        if (tileSize < minSize || tileSize > maxSize) {
            throw new IllegalArgumentException(Errors.format(
                    ErrorKeys.VALUE_OUT_OF_BOUNDS_$3, tileSize, minSize, maxSize));
        }
        if (imageSize <= minSize) {
            return imageSize;
        }
        int numDivisors = 0;
        int best = tileSize;
        for (int i=minSize; i<=maxSize; i++) {
            if (imageSize % i != 0) {
                continue;
            }
            final int n = XMath.divisors(imageSize / i).length;
            if (n < numDivisors) {
                continue;
            }
            if (n == numDivisors) {
                if (Math.abs(i - tileSize) >= Math.abs(best - tileSize)) {
                    continue;
                }
            }
            best = i;
            numDivisors = n;
        }
        return best;
    }

    /**
     * Returns the subsampling for overview computations. If no subsamplings were {@linkplain
     * #setSubsamplings(Dimension[]) explicitly set}, then this method computes automatically
     * some subsamplings from the {@linkplain #getUntiledImageBounds untiled image bounds} and
     * {@linkplain #getTileSize tile size}. If no subsampling can be computed, then this method
     * returns {@code null}.
     *
     * @return The current subsamplings for each overview levels.
     */
    public Dimension[] getSubsamplings() {
        if (subsamplings == null) {
            final Rectangle untiledBounds = getUntiledImageBounds();
            if (untiledBounds == null) {
                return null;
            }
            final Dimension tileSize = getTileSize();
            if (tileSize == null) {
                return null;
            }
            /*
             * If the tile layout is CONSTANT_GEOGRAPHIC_AREA, increasing the subsampling will have
             * the effect of reducing the tile size by the same amount, so we are better to choose
             * subsamplings that are divisors of the tile size.
             *
             * If the tile layout is CONSTANT_TILE_SIZE, increasing the subsampling will have the
             * effect of reducing the number of tiles required for covering the whole image. So we
             * are better to choose subsamplings that are divisors of the number of tiles. However
             * if the number of tiles are not integers, we can't do much.
             *
             * In the later case (non-integer amount of tiles) and in the case where the tile layout
             * is unknown, we don't really know what to choose. We fallback on some values that seem
             * reasonable, but our fallback may change in future version. It doesn't hurt any code
             * in this module - the only consequence is that tiling may be suboptimal.
             */
            final boolean constantArea = TileLayout.CONSTANT_GEOGRAPHIC_AREA.equals(layout);
            int nx = tileSize.width;
            int ny = tileSize.height;
            if (!constantArea) {
                if (untiledBounds.width  % nx == 0) nx = untiledBounds.width  / nx;
                if (untiledBounds.height % ny == 0) ny = untiledBounds.height / ny;
            }
            int[] xSubsamplings = XMath.divisors(nx);
            int[] ySubsamplings;
            if (nx == ny) {
                ySubsamplings = xSubsamplings;
            } else {
                ySubsamplings = XMath.divisors(ny);
                /*
                 * Subsamplings are different along x and y axis. We need at least arrays of the
                 * same length. First (as a help for further processing) computes the union of all
                 * subsamplings, together with subsamplings that are common to both axis.
                 */
                final int[] union  = new int[xSubsamplings.length + ySubsamplings.length];
                final int[] common = new int[union.length];
                int nu=0, nc=0, no;
                for (int ix=0, iy=0;;) {
                    if (ix == xSubsamplings.length) {
                        no = ySubsamplings.length - iy;
                        System.arraycopy(ySubsamplings, iy, union, nu, no);
                        break;
                    }
                    if (iy == ySubsamplings.length) {
                        no = xSubsamplings.length - ix;
                        System.arraycopy(xSubsamplings, ix, union, nu, no);
                        break;
                    }
                    final int sx = xSubsamplings[ix];
                    final int sy = ySubsamplings[iy];
                    int s = 0;
                    if (sx <= sy) {s = sx; ix++;}
                    if (sy <= sx) {s = sy; iy++;}
                    if (sx == sy) common[nc++] = s;
                    union[nu++] = s;
                }
                /*
                 * If there is a fair amount of subsampling values that are common in both axis
                 * (the threshold is totally arbitrary in current implementation), retains only
                 * the common values. Otherwise we will try some merge.
                 */
                if (nc >= nu / 2) {
                    ySubsamplings = xSubsamplings = new int[nc + no];
                    System.arraycopy(common, 0, xSubsamplings,  0, nc);
                    System.arraycopy(union, nu, xSubsamplings, nc, no);
                } else {
                    nu += no;
                    int j = 0;
                    final int[] newX = new int[nu];
                    final int[] newY = new int[nu];
                    for (int i=0; i<nu; i++) {
                        final int s  = union[i];
                        final int sx = closest(xSubsamplings, s);
                        final int sy = closest(ySubsamplings, s);
                        if (j == 0 || newX[j-1] != sx || newY[j-1] != sy) {
                            newX[j] = sx;
                            newY[j] = sy;
                            j++;
                        }
                    }
                    xSubsamplings = XArray.resize(newX, j);
                    ySubsamplings = XArray.resize(newY, j);
                }
            }
            /*
             * Trims the subsamplings which would produce tiles smaller than the minimum size
             * (for CONSTANT_GEOGRAPHIC_AREA layout) or which would produce more than one tile
             * enclosing the whole image (for CONSTANT_TILE_SIZE layout). First, we calculate
             * as (nx,ny) the maximum subsamplings expected (inclusive). Then we search those
             * maximum in the actual subsampling and assign to (nx,ny) the new array length.
             */
            if (constantArea) {
                nx = tileSize.width  / MIN_TILE_SIZE;
                ny = tileSize.height / MIN_TILE_SIZE;
            } else {
                nx = (untiledBounds.width  - 1) / tileSize.width  + 1;
                ny = (untiledBounds.height - 1) / tileSize.height + 1;
            }
            nx = Arrays.binarySearch(xSubsamplings, nx); if (nx < 0) nx = ~nx; else nx++;
            ny = Arrays.binarySearch(ySubsamplings, ny); if (ny < 0) ny = ~ny; else ny++;
            final int length = Math.max(nx, ny); // 'max' is safe if arrays have the same length.
            subsamplings = new int[length * 2];
            int source = 0;
            for (int i=0; i<length; i++) {
                subsamplings[source++] = xSubsamplings[i];
                subsamplings[source++] = ySubsamplings[i];
            }
        }
        final Dimension[] dimensions = new Dimension[subsamplings.length / 2];
        int source = 0;
        for (int i=0; i<dimensions.length; i++) {
            dimensions[i] = new Dimension(subsamplings[source++], subsamplings[source++]);
        }
        return dimensions;
    }

    /**
     * Sets the subsamplings for overview computations. The number of overview levels created
     * by this {@code MosaicBuilder} will be equals to the {@code subsamplings} array length.
     * <p>
     * Subsamplings most be explicitly provided for {@link TileLayout#CONSTANT_GEOGRAPHIC_AREA},
     * but is optional for {@link TileLayout#CONSTANT_TILE_SIZE}. In the later case subsamplings
     * may be {@code null} (the default), in which case they will be automatically computed from
     * the {@linkplain #getUntiledImageBounds untiled image bounds} and {@linkplain #getTileSize
     * tile size} in order to have only entire tiles (i.e. tiles in last columns and last rows
     * don't need to be cropped).
     *
     * @param subsamplings The new subsamplings for each overview levels.
     */
    public void setSubsamplings(final Dimension[] subsamplings) {
        final int[] newSubsamplings;
        if (subsamplings == null) {
            newSubsamplings = null;
        } else {
            int target = 0;
            newSubsamplings = new int[subsamplings.length * 2];
            for (int i=0; i<subsamplings.length; i++) {
                final Dimension subsampling = subsamplings[i];
                final int xSubsampling = subsampling.width;
                final int ySubsampling = subsampling.height;
                if (xSubsampling < 1 || ySubsampling < 1) {
                    throw new IllegalArgumentException(Errors.format(
                            ErrorKeys.ILLEGAL_ARGUMENT_$1, "subsamplings[" + i + ']'));
                }
                newSubsamplings[target++] = xSubsampling;
                newSubsamplings[target++] = ySubsampling;
            }
        }
        this.subsamplings = newSubsamplings;
    }

    /**
     * Sets uniform subsamplings for overview computations. This convenience method delegates to
     * {@link #setSubsamplings(Dimension[])} with the same value affected to both
     * {@linkplain Dimension#width width} and {@linkplain Dimension#height height}.
     *
     * @param subsamplings The new subsamplings for each overview levels.
     */
    public void setSubsamplings(final int[] subsamplings) {
        final Dimension[] newSubsamplings;
        if (subsamplings == null) {
            newSubsamplings = null;
        } else {
            newSubsamplings = new Dimension[subsamplings.length];
            for (int i=0; i<subsamplings.length; i++) {
                final int subsampling = subsamplings[i];
                newSubsamplings[i] = new Dimension(subsampling, subsampling);
            }
        }
        // Delegates to setSubsamplings(Dimension[]) instead of performing the same work in-place
        // (which would have been more efficient) because the user may have overriden the former.
        setSubsamplings(newSubsamplings);
    }

    /**
     * Creates a tile manager from the informations supplied in above setters.
     * The following methods must be invoked prior this one:
     * <p>
     * <ul>
     *   <li>{@link #setUntiledImageBounds}</li>
     *   <li>{@link #setTileReaderSpi}</li>
     * </ul>
     *
     * @return The tile manager created from the information returned by getter methods.
     * @throws IOException if an I/O operation was required and failed. The default implementation
     *         does not perform any I/O, but subclasses are allowed to do so.
     */
    public TileManager createTileManager() throws IOException {
        tileReaderSpi = getTileReaderSpi();
        if (tileReaderSpi == null) {
            // TODO: We may try to detect automatically the Spi in a future version.
            throw new IllegalStateException(Errors.format(ErrorKeys.NO_IMAGE_READER));
        }
        untiledBounds = getUntiledImageBounds(); // Forces computation, if any.
        if (untiledBounds == null) {
            throw new IllegalStateException(Errors.format(ErrorKeys.UNSPECIFIED_IMAGE_SIZE));
        }
        tileSize = getTileSize(); // Forces computation
        if (tileSize == null) {
            tileSize = ImageUtilities.toTileSize(untiledBounds.getSize());
        }
        formatter.initialize(tileReaderSpi);
        final TileManager tiles;
        switch (layout) {
            case CONSTANT_GEOGRAPHIC_AREA: tiles = createTileManager(true);  break;
            case CONSTANT_TILE_SIZE:       tiles = createTileManager(false); break;
            default: throw new IllegalStateException(layout.toString());
        }
        if (mosaicEnvelope != null && !mosaicEnvelope.isNull()) {
            final GridToEnvelopeMapper mapper = createGridToEnvelopeMapper(tiles);
            mapper.setGridRange(new GridRange2D(untiledBounds));
            mapper.setEnvelope(mosaicEnvelope);
            tiles.setGridToCRS((AffineTransform) mapper.createTransform());
        }
        return tiles;
    }

    /**
     * Creates tiles for the following cases:
     * <ul>
     *   <li>covering a constant geographic region. The tile size will reduce as we progress into
     *       overviews levels. The {@link #minimumTileSize} value is the stop condition - no smaller
     *       tiles will be created.</li>
     *   <li>tiles of constant size in pixels. The stop condition is when a single tile cover
     *       the whole image.</li>
     * </ul>
     *
     * @throws IOException if an I/O operation was requested and failed.
     */
    private TileManager createTileManager(final boolean constantArea) throws IOException {
        final List<Tile> tiles       = new ArrayList<Tile>();
        final Rectangle  tileBounds  = new Rectangle(tileSize);
        final Rectangle  imageBounds = new Rectangle(untiledBounds);
        Dimension[] subsamplings = getSubsamplings();
        if (subsamplings == null) {
            final int n;
            if (constantArea) {
                n = Math.max(tileBounds.width, tileBounds.height) / MIN_TILE_SIZE;
            } else {
                n = Math.max(imageBounds.width  / tileBounds.width,
                             imageBounds.height / tileBounds.height);
            }
            subsamplings = new Dimension[n];
            for (int i=1; i<=n; i++) {
                subsamplings[i-1] = new Dimension(i,i);
            }
        }
        formatter.computeOverviewFieldSize(subsamplings.length);
        for (int overview=0; overview<subsamplings.length; overview++) {
            final Dimension subsampling = subsamplings[overview];
            imageBounds.setRect(untiledBounds);
            divide(imageBounds, subsampling);
            if (constantArea) {
                tileBounds.setSize(tileSize);
                divide(tileBounds, subsampling);
            }
            final int xmin = imageBounds.x;
            final int ymin = imageBounds.y;
            final int xmax = imageBounds.x + imageBounds.width;
            final int ymax = imageBounds.y + imageBounds.height;
            formatter.computeFieldSizes(imageBounds, tileBounds);
            int x=0, y=0;
            for (tileBounds.y = ymin; tileBounds.y < ymax; tileBounds.y += tileBounds.height) {
                x = 0;
                for (tileBounds.x = xmin; tileBounds.x < xmax; tileBounds.x += tileBounds.width) {
                    final Rectangle clippedBounds = tileBounds.intersection(imageBounds);
                    final File file = new File(directory, generateFilename(overview, x, y));
                    final Tile tile = new Tile(tileReaderSpi, file, 0, clippedBounds, subsampling);
                    tiles.add(tile);
                    x++;
                }
                y++;
            }
        }
        final TileManager[] managers = factory.create(tiles);
        return managers[0];
    }

    /**
     * Divides a rectangle by the given subsampling.
     */
    private static void divide(final Rectangle bounds, final Dimension subsampling) {
        bounds.x      /= subsampling.width;
        bounds.y      /= subsampling.height;
        bounds.width  /= subsampling.width;
        bounds.height /= subsampling.height;
    }

    /**
     * Returns the value from the specified array which is the closest to the specified value.
     * If the specified value is out of upper bounds, then it is returned unchanged. The array
     * values must be sorted in increasing order.
     */
    private static int closest(final int[] subsamplings, final int s) {
        int i = Arrays.binarySearch(subsamplings, s);
        if (i < 0) {
            i = ~i;
            if (i == subsamplings.length) {
                return s;
            }
            if (i != 0 && (s - subsamplings[i-1]) <= (subsamplings[i] - s)) {
                i--;
            }
        }
        return subsamplings[i];
    }

    /**
     * The mosaic image writer to be used by {@link MosaicBuilder#writeFromUntiledImage}.
     */
    private final class Writer extends MosaicImageWriter {
        /**
         * Index of the untiled image to read.
         */
        private final int inputIndex;

        /**
         * {@code true} if tiles should be written.
         */
        private final boolean writeTiles;

        /**
         * The input tile managers, or {@code null} if none.
         */
        TileManager[] inputTiles;

        /**
         * The tiles created by {@link MosaicBuilder#createTileManager}.
         * Will be set by {@link #filter} and read by {@link MosaicBuilder}.
         */
        TileManager tiles;

        /**
         * Creates a writer for an untiled image to be read at the given index.
         */
        Writer(final int inputIndex, final boolean writeTiles) {
            this.inputIndex = inputIndex;
            this.writeTiles = writeTiles;
        }

        /**
         * Returns {@code true} if tiles should be written.
         */
        @Override
        boolean isWriteEnabled() {
            return writeTiles;
        }

        /**
         * Creates the tiles for the specified untiled images.
         */
        @Override
        protected boolean filter(ImageReader reader) throws IOException {
            final Rectangle bounds = new Rectangle();
            bounds.width  = reader.getWidth (inputIndex);
            bounds.height = reader.getHeight(inputIndex);
            // Sets only after successful reading of image size.
            if (reader instanceof MosaicImageReader) {
                final MosaicImageReader mosaic = (MosaicImageReader) reader;
                inputTiles = mosaic.getInput();
                reader = mosaic.getTileReader();
            }
            if (reader != null) { // May be null as a result of above line.
                final ImageReaderSpi spi = reader.getOriginatingProvider();
                if (spi != null && getTileReaderSpi() == null) {
                    setTileReaderSpi(spi);
                }
            }
            setUntiledImageBounds(bounds);
            tiles = createTileManager();
            try {
                setOutput(tiles);
            } catch (IllegalArgumentException exception) {
                final Throwable cause = exception.getCause();
                if (cause instanceof IOException) {
                    throw (IOException) cause;
                }
                throw exception;
            }
            return true;
        }

        /**
         * Invoked when a tile is about to be written. Delegates to a method that users can
         * override.
         */
        @Override
        protected void onTileWrite(Tile tile, ImageWriteParam parameters) throws IOException {
            MosaicBuilder.this.onTileWrite(tile, parameters);
        }
    }

    /**
     * Creates a tile manager from an untiled image. The {@linkplain #getUntiledImageBounds
     * untiled image bounds} and {@linkplain #getTileReaderSpi tile reader SPI} are inferred
     * from the input, unless they were explicitly specified.
     * <p>
     * Optionnaly if {@code writeTiles} is {@code true}, then pixel values are read from the
     * untiled images, organized in tiles as specified by the {@link TileManager} to be returned
     * and saved to disk. This work is done using a default {@link MosaicImageWriter}.
     *
     * @param input The image input, typically as a {@link File} or an other {@link TileManager}.
     * @param inputIndex Index of image to read, typically 0.
     * @param writeTiles If {@code true}, tiles are created and saved to disk.
     * @return The tiles, or {@code null} if the process has been aborted while writing tiles.
     * @throws IOException if an error occured while reading the untiled image or (only if
     *         {@code writeTiles} is {@code true}) while writting the tiles to disk.
     */
    public TileManager createTileManager(final Object input, final int inputIndex,
                                         final boolean writeTiles) throws IOException
    {
        formatter.ensurePrefixSet(input);
        final Writer writer = new Writer(inputIndex, writeTiles);
        writer.setLogLevel(getLogLevel());
        try {
            if (!writer.writeFromInput(input, inputIndex, 0)) {
                return null;
            }
        } finally {
            writer.dispose();
        }
        TileManager tiles = writer.tiles;
        /*
         * Before to returns the tile manager, if no geometry has been inferred from the target
         * tiles (typically because no setEnvelope(...) has not been invoked), then inherit the
         * geometry from the source tile, if there is any. This operation is conservative and
         * performed only on a "best effort" basis.
         */
        if (tiles.geometry == null) {
            if (writer.inputTiles != null) {
                for (final TileManager candidate : writer.inputTiles) {
                    final ImageGeometry geometry = candidate.getGridGeometry();
                    if (geometry != null) {
                        tiles.setGridToCRS(geometry.getGridToCRS());
                        break;
                    }
                }
            }
        }
        return tiles;
    }

    /**
     * Generates a filename for the current tile based on the position of this tile in the raster.
     * For example, a tile in the first overview level, which is localized on the 5th column and
     * 2nd row may have a name like "{@code L1_E2.png}".
     * <p>
     * Subclasses may override this method if they want more control on generated tile filenames.
     *
     * @param  overview  The level of overview. First overview is 0.
     * @param  column    The index of columns. First column is 0.
     * @param  row       The index of rows. First row is 0.
     * @return A filename based on the position of the tile in the whole raster.
     */
    protected String generateFilename(final int overview, final int column, final int row) {
        return formatter.generateFilename(overview, column, row);
    }

    /**
     * Invoked automatically when a "<cite>grid to CRS</cite>" transform needs to be computed. The
     * default implementation returns a new {@link GridToEnvelopeMapper} instance in its default
     * configuration, except for the {@linkplain GridToEnvelopeMapper#setPixelAnchor pixel anchor}
     * which is set to {@link PixelInCell#CELL_CORNER CELL_CORNER} (OGC specification maps pixel
     * center, while Java I/O maps pixel upper-left corner).
     * <p>
     * Subclasses may override this method in order to configure the mapper in an other way.
     *
     * @param tiles The tiles for which a "<cite>grid to CRS</cite>" transform needs to be computed.
     * @return An "grid to envelope" mapper having the desired configuration.
     *
     * @see #setMosaicEnvelope
     */
    protected GridToEnvelopeMapper createGridToEnvelopeMapper(final TileManager tiles) {
        final GridToEnvelopeMapper mapper = new GridToEnvelopeMapper();
        mapper.setPixelAnchor(PixelInCell.CELL_CORNER);
        return mapper;
    }

    /**
     * Invoked automatically when a tile is about to be written. The default implementation does
     * nothing. Subclasses can override this method in order to set custom write parameters. The
     * {@linkplain ImageWriteParam#setSourceRegion source region} and
     * {@linkplain ImageWriteParam#setSourceSubsampling source subsampling} should not be set
     * since they will be inconditionnaly overwritten by the caller.
     *
     * @param  tile The tile to be written.
     * @param  parameters The parameters to be given to the {@linkplain ImageWriter image writer}.
     * @throws IOException if an I/O operation was required and failed.
     */
    protected void onTileWrite(Tile tile, ImageWriteParam parameters) throws IOException {
    }
}
