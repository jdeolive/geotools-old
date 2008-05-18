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
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import javax.imageio.spi.ImageReaderSpi;


/**
 * A tile manager for the particular case of tile distributed on a regular grid.
 *
 * @since 2.5
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public abstract class GridTileManager extends TileManager {
    /**
     * The levels of overview sorted by finest levels first.
     */
    private final GridLevel[] levels;

    /**
     * The region enclosing all tiles.
     */
    private final Rectangle region;

    /**
     * Creates a new tile manager for the given tiles, which must be distributed on a grid.
     * This constructor is protected for subclassing, but should not be invoked directly.
     * {@code GridTileManager} instances should be created by {@link TileManagerFactory}.
     *
     * @param  tiles The tiles.
     * @throws IOException if an I/O operation was required and failed.
     * @throws IllegalArgumentException if this class can not handle the given tiles.
     */
    protected GridTileManager(final Collection<Tile> tiles)
            throws IOException, IllegalArgumentException
    {
        Tile.ensureNonNull("tiles", tiles);
        final Map<Dimension,GridLevel> levelsBySubsampling = new HashMap<Dimension,GridLevel>();
        for (final Tile tile : tiles) {
            final Dimension subsampling = tile.getSubsampling();
            GridLevel level = levelsBySubsampling.get(subsampling);
            if (level == null) {
                level = new GridLevel(tile, subsampling);
                levelsBySubsampling.put(subsampling, level);
            } else {
                level.add(tile, subsampling);
            }
        }
        levels = levelsBySubsampling.values().toArray(new GridLevel[levelsBySubsampling.size()]);
        Arrays.sort(levels);
        region = new Rectangle(-1, -1);
        for (int i=0; i<levels.length; i++) {
            final GridLevel level = levels[i];
            level.process(i);
            region.add(level.region);
        }
    }

    /**
     * Returns the region enclosing all tiles.
     *
     * @return The region. <strong>Do not modify</strong> since it is a direct reference to
     *         internal structures.
     */
    @Override
    final Rectangle getRegion() {
        return region;
    }

    /**
     * Returns an estimation of tiles dimension. This method looks only to the first level
     * having more than 1 tile.
     *
     * @return The tiles dimension.
     */
    @Override
    final Dimension getTileSize() {
        for (int i=levels.length; --i >= 0;) {
            final GridLevel level = levels[i];
            if (level.region.width > level.width || level.region.height > level.height) {
                return new Dimension(level.width, level.height);
            }
        }
        return region.getSize();
    }

    /**
     * Returns a reference to the tiles used internally by this tile manager.
     */
    @Override
    final Collection<Tile> getInternalTiles() {
        final List<Tile> tiles = new ArrayList<Tile>();
        for (final GridLevel level : levels) {
            level.addInternalTiles(tiles);
        }
        return tiles;
    }
}
