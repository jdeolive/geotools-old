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

import java.awt.Rectangle;
import java.io.File;
import java.io.Serializable;
import java.net.URI;
import java.net.URL;
import javax.imageio.spi.ImageReaderSpi;


/**
 * Formats the filename of tiles to be created by {@link MosaicBuilder}.
 *
 * @since 2.5
 * @source $URL$
 * @version $Id$
 * @author Cédric Briançon
 * @author Martin Desruisseaux
 */
final class FilenameFormatter implements Serializable {
    /**
     * For cross-version interoperability.
     */
    private static final long serialVersionUID = -3419303061626601549L;

    /**
     * Default value for {@link #prefix}. Current implementation uses "L" as in "Level".
     */
    private static final String DEFAULT_PREFIX = "L";

    /**
     * The expected size of row and column filed in generated names.
     */
    private int overviewFieldSize, rowFieldSize, columnFieldSize;

    /**
     * The prefix to put before tile filenames. If {@code null}, will be inferred
     * from the source filename.
     */
    private String prefix;

    /**
     * File extension to be given to filenames. Computed automatically by
     * {@link MosaicBuilder#createTileManager}.
     */
    private String extension;

    /**
     * Creates a default filename formatter.
     */
    public FilenameFormatter() {
    }

    /**
     * Initializes the formatter with a suffix inferred from the given image reader provider
     * if none was explicitly set. The longuest file extension is choosen (e.g. {@code "tiff"}
     * instead of {@code "tif"}).
     *
     * @param tileReaderSpi The image reader provider to be used for reading tiles.
     */
    public void initialize(final ImageReaderSpi tileReaderSpi) {
        if (prefix == null) {
            prefix = DEFAULT_PREFIX;
        }
        extension = "";
        final String[] suffix = tileReaderSpi.getFileSuffixes();
        if (suffix != null) {
            for (int i=0; i<suffix.length; i++) {
                final String s = suffix[i];
                if (s.length() > extension.length()) {
                    extension = s;
                }
            }
        }
        overviewFieldSize = 0;
        rowFieldSize      = 0;
        columnFieldSize   = 0;
    }

    /**
     * Computes the value for {@link #overviewFieldSize}.
     * It will be used by {@link #generateFilename}.
     *
     * @param n The expected number of overviews.
     */
    public void computeOverviewFieldSize(final int n) {
        overviewFieldSize = ((int) Math.log10(n)) + 1;
    }

    /**
     * Computes the values for {@link #columnFieldSize} and {@link #rowFieldSize}.
     * They will be used by {@link #generateFilename}.
     */
    public void computeFieldSizes(final Rectangle imageBounds, final Rectangle tileBounds) {
        final StringBuilder buffer = new StringBuilder();
        format26(buffer, imageBounds.width / tileBounds.width, 0);
        columnFieldSize = buffer.length();
        buffer.setLength(0);
        format10(buffer, imageBounds.height / tileBounds.height, 0);
        rowFieldSize = buffer.length();
    }

    /**
     * Formats a number in base 10.
     *
     * @param buffer The buffer where to write the row number.
     * @param n      The row number to format, starting at 0.
     * @param size   The expected width (for padding with '0').
     */
    private static void format10(final StringBuilder buffer, final int n, final int size) {
        final String s = Integer.toString(n + 1);
        for (int i=size-s.length(); --i >= 0;) {
            buffer.append('0');
        }
        buffer.append(s);
    }

    /**
     * Formats a column in base 26. For example the first column is {@code 'A'}. If there is
     * more columns than alphabet has letters, then another letter is added in the same way.
     *
     * @param buffer The buffer where to write the column number.
     * @param n      The column number to format, starting at 0.
     * @param size   The expected width (for padding with 'A').
     */
    private static void format26(final StringBuilder buffer, int n, final int size) {
        if (size > 1 || n >= 26) {
            format26(buffer, n / 26, size - 1);
            n %= 26;
        }
        buffer.append((char) ('A' + n));
    }

    /**
     * If the {@linkplain #prefix} is not already set, build a default value from the given input.
     *
     * @param input The image input, typically as a {@link File} or an other {@link TileManager}.
     */
    public void ensurePrefixSet(final Object input) {
        if (prefix == null) {
            String filename;
            if (input instanceof File) {
                filename = ((File) input).getName();
            } else if (input instanceof URI || input instanceof URL || input instanceof CharSequence) {
                filename = input.toString();
                filename = filename.substring(filename.lastIndexOf('/') + 1);
            } else {
                filename = DEFAULT_PREFIX;
            }
            int length = filename.lastIndexOf('.');
            if (length < 0) {
                length = filename.length();
            }
            int i;
            for (i=0; i<length; i++) {
                if (!Character.isLetter(filename.charAt(i))) {
                    break;
                }
            }
            prefix = filename.substring(0, i);
        }
    }

    /**
     * Generates a filename for the current tile based on the position of this tile in the raster.
     * For example, a tile in the first overview level, which is localized on the 5th column and
     * 2nd row may have a name like "{@code L1_E2.png}".
     *
     * @param  overview  The level of overview. First overview is 0.
     * @param  column    The index of columns. First column is 0.
     * @param  row       The index of rows. First row is 0.
     * @return A filename based on the position of the tile in the whole raster.
     */
    public String generateFilename(final int overview, final int column, final int row) {
        final StringBuilder buffer = new StringBuilder(prefix);
        format10(buffer, overview, overviewFieldSize); buffer.append('_');
        format26(buffer, column,   columnFieldSize);
        format10(buffer, row,      rowFieldSize);
        return buffer.append('.').append(extension).toString();
    }
}
