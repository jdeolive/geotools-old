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

import java.util.*;
import java.awt.Point;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.MalformedURLException;
import org.geotools.util.IntegerList;
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.UnmodifiableArrayList;


/**
 * A level of overview in a {@link GridTileManager}.
 * <p>
 * <b>Note:</b> This class as an {@link #compareTo} method inconsistent with {@link #equals}.
 *
 * @since 2.5
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
final class GridLevel implements Comparable<GridLevel>, Serializable {
    /**
     * For cross-version compatibility.
     */
    private static final long serialVersionUID = -1441934881339348L;

    /**
     * The input types for which we will try to find a pattern.
     */
    private static final Set<Class<?>> INPUT_TYPES = new HashSet<Class<?>>(8);
    static {
        INPUT_TYPES.add(String.class);
        INPUT_TYPES.add(File  .class);
        INPUT_TYPES.add(URL   .class);
        INPUT_TYPES.add(URI   .class);
    }

    /**
     * The overview level of this {@code GridLevel}. 0 is finest subsampling. Must match the
     * element index in the sorted {@code GridTileManager.levels} array. Will be set by
     * {@link #process}.
     */
    private int ordinal;

    /**
     * The number of tiles along <var>x</var> and <var>y</var> axis.
     * Will be computed by {@link #process}.
     */
    private int nx, ny;

    /**
     * Subsampling of every tiles at this level.
     */
    private final int sx, sy;

    /**
     * The location of the tile closest to origin, positive.
     */
    private final int x, y;

    /**
     * Size of every tiles at this level.
     */
    final int width, height;

    /**
     * The region of every tiles in this level. The {@linkplain Rectangle#x x} and
     * {@linkplain Rectangle#y y} coordinates are the upper-left corner of the (0,0)
     * tile. The {@linkplain Rectangle#width width} and {@linkplain Rectangle#height height}
     * are big enough for including every tiles.
     */
    final Rectangle region;

    /**
     * On construction, the list of tiles {@linkplain #add added} in this level in no particular
     * order. After {@linkplain #process processing}, the tiles that need to be retained because
     * they can not be created on the fly from the {@linkplain #patterns}, or {@code null} if none.
     */
    private List<Tile> tiles;

    /**
     * The tiles to use as a pattern for creating tiles on the fly, or {@code null} if none.
     * If non-null, then the array length is typically 1. If greater than one, then the
     * {@linkplain #usePattern} field needs to be non-null un order to specify which pattern
     * is used.
     */
    private Tile[] patterns;

    /**
     * If there is more than one pattern, the index of pattern to use. Also used for signaling
     * holes in the mosaic if there is any.
     */
    private IntegerList patternUsed;

    /**
     * Index of last pattern used. Used in order to avoid reinitializing
     * the {@linkplain #formatter} more often than needed.
     */
    private transient int lastPattern;

    /**
     * The formatter used for parsing and creating filename.
     */
    private transient FilenameFormatter formatter;

    /**
     * Creates a new level from the given tile.
     *
     * @param tile The tile to wrap.
     * @param subsampling The tile subsampling, provided as an explicit argument only
     *        in order to avoid creating a temporary {@link Dimension} object again.
     */
    GridLevel(final Tile tile, final Dimension subsampling) throws IOException {
        region = tile.getRegion();
        int x = region.x % (width  = region.width);
        int y = region.y % (height = region.height);
        if (x < 0) x += width;
        if (y < 0) y += height;
        this.x = x;
        this.y = y;
        assert subsampling.equals(tile.getSubsampling()) : subsampling;
        sx = subsampling.width;
        sy = subsampling.height;
        tiles = new ArrayList<Tile>();
        tiles.add(tile);
    }

    /**
     * Adds a tile to the list of tiles in this level, provided that they are aligned on the same
     * grid.
     *
     * @param tile The tile to add.
     * @param subsampling The tile subsampling, provided as an explicit argument only
     *        in order to avoid creating a temporary {@link Dimension} object again.
     * @throws IOException if an I/O operation was required and failed.
     * @throws IllegalArgumentException if the tiles are not aligned on the same grid.
     */
    final void add(final Tile tile, final Dimension subsampling)
            throws IOException, IllegalArgumentException
    {
        assert subsampling.equals(tile.getSubsampling()) : subsampling;
        assert subsampling.width == sx && subsampling.height == sy : subsampling;
        final Rectangle toAdd = tile.getRegion();
        int ox = region.x % width;
        int oy = region.y % height;
        if (ox < 0) ox += width;
        if (oy < 0) oy += height;
        if (ox != x || oy != y) {
            throw new IllegalArgumentException(Errors.format(ErrorKeys.NOT_A_GRID));
        }
        if (toAdd.width != width || toAdd.height != height) {
            throw new IllegalArgumentException(Errors.format(ErrorKeys.UNEXPECTED_IMAGE_SIZE));
        }
        region.add(toAdd);
    }

    /**
     * Once every tiles have been {@linkplain #add added} to this grid level, search for a pattern.
     *
     * @param ordinal The overview level of this {@code GridLevel}. 0 is finest subsampling.
     */
    final void process(final int ordinal) {
        this.ordinal = ordinal;
        assert (region.width % width == 0) && (region.height % height == 0) : region;
        nx = region.width  / width;
        ny = region.height / height;
        /*
         * Searchs for the most common tuple of ImageReaderSpi, imageIndex, input pattern. The
         * rectangle below is named "size" because the (x,y) location is not representative.
         * The tiles that we failed to modelize by a pattern will be stored under the null key.
         */
        formatter = new FilenameFormatter();
        final Rectangle size = new Rectangle(x, y, width, height);
        final Map<Tile,List<Tile>> models = new HashMap<Tile,List<Tile>>();
        for (final Tile tile : tiles) {
            final String input = inputPattern(tile);
            final Tile model = (input != null) ? new Tile(tile, input, size) : null;
            List<Tile> similar = models.get(model);
            if (similar == null) {
                similar = new ArrayList<Tile>();
                models.put(model, similar);
            }
            similar.add(tile);
        }
        /*
         * If there is at least one tile that can not be processed, keep them in an array.
         * The array length is exactly (nx*ny) but contains only the elements that should
         * not be computed on the fly (other elements are null). Note that if the number
         * of elements to be computed on the fly is less than some arbitrary threshold,
         * it is not worth to compute them on the fly so we move them to the tiles list.
         */
        tiles = models.remove(null);
        for (final Iterator<List<Tile>> it = models.values().iterator(); it.hasNext();) {
            final List<Tile> similar = it.next();
            if (similar.size() < 4) {
                if (tiles == null) {
                    tiles = similar;
                } else {
                    tiles.addAll(similar);
                }
                it.remove();
            }
        }
        if (tiles != null) {
            tiles = UnmodifiableArrayList.wrap(toArray(tiles));
        }
        /*
         * If there is no recognized pattern, clears the unused fields and finish immediately
         * this method, so we skip the construction of "pattern used" list (which may be large).
         */
        if (models.isEmpty()) {
            formatter = null;
            return;
        }
        /*
         * Sets the pattern index. Index in the 'tile' array are numbered from 0 (like usual),
         * but values in the 'patternUsed' list are numbered from 1 because we reserve the 0
         * value for non-existant tiles.
         */
        patterns = new Tile[models.size()];
        patternUsed = new IntegerList(0, nx*ny, patterns.length);
        int index = 0;
        for (final Map.Entry<Tile,List<Tile>> entry : models.entrySet()) {
            patterns[index++] = entry.getKey();
            for (final Tile tile : entry.getValue()) {
                final Point pt = getIndex2D(tile);
                final int i = getIndex(pt);
                final int p = patternUsed.getInteger(i);
                if ((p != 0 && p != index) || (tiles != null && tiles.get(i) != null)) {
                    throw duplicatedTile(pt);
                }
                patternUsed.setInteger(i, index);
            }
        }
        /*
         * In the common case where there is only one pattern and no missing tiles,
         * clears the 'patternUsed' construct since we don't need it.
         */
        if (patterns.length == 1) {
            for (int i=patternUsed.size(); --i >= 0;) {
                if (patternUsed.getInteger(i) == 0) {
                    if (tiles == null || tiles.get(i) == null) {
                        // We have at least one hole, so we need to keep the list of them.
                        return;
                    }
                }
            }
            patternUsed = null;
        }
    }

    /**
     * Returns a pattern for the given tile. If no pattern can be found, returns {@code null}.
     * This method accepts only tile and input of specific types in order to be able to rebuild
     * later an exactly equivalent object from the pattern.
     *
     * @param  tile The tile to inspect for a pattern in the input object.
     * @return The pattern, or {@code null} if none.
     */
    private String inputPattern(final Tile tile) {
        if (!Tile.class.equals(tile.getClass())) {
            return null;
        }
        final Object input = tile.getInput();
        final Class<?> type = input.getClass();
        if (!INPUT_TYPES.contains(type)) {
            return null;
        }
        final Point index = getIndex2D(tile);
        String pattern = input.toString();
        pattern = formatter.guessPattern(ordinal, index.x, index.y, pattern);
        if (pattern != null) {
            pattern = type.getSimpleName() + ':' + pattern;
        }
        return pattern;
    }

    /**
     * Returns the index of the given tile. The tile in the upper-left corner has index (0,0).
     *
     * @param tile The tile for which to get the index.
     * @return The index in a two-dimensional grid.
     */
    private Point getIndex2D(final Tile tile) {
        final Point location = tile.getLocation();
        location.x -= region.x;
        location.y -= region.y;
        assert (location.x % width == 0) && (location.y % height == 0) : location;
        location.x /= width;
        location.y /= height;
        return location;
    }

    /**
     * Returns the flat index for the given 2D index.
     *
     * @param pt The 2D index obtained by {@link #getIndex2D}.
     * @return The corresponding index in a flat array.
     * @throws IndexOutOfBoundsException if the given index is out of bounds.
     */
    private int getIndex(final Point pt) throws IndexOutOfBoundsException {
        if (pt.x < 0 || pt.x >= nx || pt.y < 0 || pt.y >= ny) {
            throw new IndexOutOfBoundsException(Errors.format(
                    ErrorKeys.INDEX_OUT_OF_BOUNDS_$1, "(" + pt.x + ',' + pt.y + ')'));
        }
        return pt.y * nx + pt.x;
    }

    /**
     * Formats an exception for a duplicated tile.
     *
     * @param  pt The upper-left corner coordinate.
     * @return An exception formatted for a duplicated tile at the given coordinate.
     */
    private static IllegalArgumentException duplicatedTile(final Point pt) {
        return new IllegalArgumentException(Errors.format(
                ErrorKeys.DUPLICATED_VALUES_$1, "location=" + pt.x + ',' + pt.y));
    }

    /**
     * Expands the given tiles in a flat array. Tiles are stored by their index, with
     * <var>x</var> index varying faster.
     */
    private Tile[] toArray(final Collection<Tile> tiles) {
        final Tile[] array = new Tile[nx * ny];
        for (final Tile tile : tiles) {
            final Point pt = getIndex2D(tile);
            final int index = getIndex(pt);
            if (array[index] != null && !tile.equals(array[index])) {
                throw duplicatedTile(pt);
            }
            array[index] = tile;
        }
        return array;
    }

    /**
     * Returns the tile at the given index.
     *
     * @param The tile location, with (0,0) as the upper-left tile.
     * @return The tile at the given location.
     * @throws IndexOutOfBoundsException if the given index is out of bounds.
     * @throws MalformedURLException if an error occured while creating the URL for the tile.
     */
    public Tile getTile(final Point location)
            throws IndexOutOfBoundsException, MalformedURLException
    {
        Tile tile;
        final int index = getIndex(location);
        /*
         * Checks for fully-created instance. Those instances are expected to exist if
         * some tile do not comply to a general pattern that this class can recognize.
         */
        if (tiles != null) {
            tile = tiles.get(index);
            if (tile != null) {
                return tile;
            }
        }
        /*
         * The requested tile does not need to be handled in a special way, so now get the
         * pattern for this tile and generate the filename of the fly. Doing so avoid the
         * consumption of memory for the thousands of tiles we may have.
         */
        int p = 0;
        if (patternUsed != null) {
            p = patternUsed.get(index);
            if (p == 0) {
                return null;
            }
            p--;
        }
        tile = patterns[p];
        final String pattern = tile.getInput().toString();
        if (formatter == null) {
            formatter = new FilenameFormatter();
            lastPattern = -1;
        }
        if (p != lastPattern) {
            formatter.applyPattern(pattern.substring(pattern.indexOf(':') + 1));
            lastPattern = p;
        }
        final String filename = formatter.generateFilename(ordinal, location.x, location.y);
        /*
         * We now have the filename to be given to the tile. Creates the appropriate object
         * (File, URL, URI or String) from it.
         */
        final Object input;
        if (pattern.startsWith("File")) {
            input = new File(filename);
        } else if (pattern.startsWith("URL")) {
            input = new URL(filename);
        } else if (pattern.startsWith("URI")) try {
            input = new URI(filename);
        } catch (URISyntaxException cause) { // Rethrown as an IOException subclass.
            MalformedURLException e = new MalformedURLException(cause.getLocalizedMessage());
            e.initCause(cause);
            throw e;
        } else {
            input = filename;
        }
        assert INPUT_TYPES.contains(input.getClass()) : input;
        /*
         * Now creates the definitive tile.
         */
        return new Tile(tile, input, new Rectangle(
                region.x + location.x * width,
                region.y + location.y * height,
                width, height));
    }

    /**
     * Adds all internal tiles to the given collection.
     *
     * @param list The collection where to add the internal tiles.
     */
    final void addInternalTiles(final List<? super Tile> list) {
        if (tiles != null) {
            for (final Tile tile : tiles) {
                if (tile != null) {
                    list.add(tile);
                }
            }
        }
        if (patterns != null) {
            for (final Tile tile : patterns) {
                if (tile != null) {
                    list.add(tile);
                }
            }
        }
    }

    /**
     * Compares subsamplings, sorting smallest areas first. If two subsamplings
     * have the same area, sorts by <var>sx</var> first then by <var>sy</var>.
     */
    public int compareTo(final GridLevel other) {
        int c = (sx * sy) - (other.sx * other.sy);
        if (c == 0) {
            c = sx - other.sx;
            if (c == 0) {
                c = sy - other.sy;
            }
        }
        return c;
    }

    /**
     * Returns a string representation for debugging purpose.
     */
    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder(getClass().getSimpleName())
                .append('[').append(sx).append(',').append(sy);
        if (tiles != null) {
            buffer.append(": ").append(tiles.size()).append(" tiles");
        }
        return buffer.append(']').toString();
    }
}
