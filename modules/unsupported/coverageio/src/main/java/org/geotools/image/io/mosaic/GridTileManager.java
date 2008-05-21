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

import java.util.Map;
import java.util.Arrays;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collection;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.IOException;
import org.geotools.util.FrequencySortedSet;


/**
 * A tile manager for the particular case of tile distributed on a regular grid.
 *
 * @since 2.5
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class GridTileManager extends TileManager {
    /**
     * For cross-version interoperability.
     */
    private static final long serialVersionUID = -3140767174475649400L;

    /**
     * The levels of overview sorted by finest levels first.
     */
    private final OverviewLevel[] levels;

    /**
     * The region enclosing all tiles in absolute coordinates. This is the coordinates
     * relative to the tiles having a subsampling of 1.
     */
    private final Rectangle region;

    /**
     * The number of tiles.
     */
    private final int count;

    /**
     * Creates a new tile manager for the given tiles, which must be distributed on a grid.
     * This constructor is protected for subclassing, but should not be invoked directly.
     * {@code GridTileManager} instances should be created by {@link TileManagerFactory}.
     *
     * @param  tiles The tiles.
     * @throws IOException if an I/O operation was required and failed.
     * @throws IllegalArgumentException if this class can not handle the given tiles.
     */
    protected GridTileManager(final Tile[] tiles)
            throws IOException, IllegalArgumentException
    {
        Tile.ensureNonNull("tiles", tiles);
        final Map<Dimension,OverviewLevel> levelsBySubsampling = new HashMap<Dimension,OverviewLevel>();
        for (final Tile tile : tiles) {
            final Dimension subsampling = tile.getSubsampling();
            OverviewLevel level = levelsBySubsampling.get(subsampling);
            if (level == null) {
                level = new OverviewLevel(tile, subsampling);
                levelsBySubsampling.put(subsampling, level);
            } else {
                level.add(tile, subsampling);
            }
        }
        levels = levelsBySubsampling.values().toArray(new OverviewLevel[levelsBySubsampling.size()]);
        Arrays.sort(levels);
        region = new Rectangle(-1, -1);
        int count = 0;
        for (int i=0; i<levels.length; i++) {
            final OverviewLevel level = levels[i];
            level.process(i);
            region.add(level.getAbsoluteRegion());
            count += level.getNumTiles();
        }
        this.count = count;
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
            final Dimension size = levels[i].getTileSize();
            if (size != null) {
                return size;
            }
        }
        return region.getSize();
    }

    /**
     * Returns {@code true} if there is more than one tile.
     *
     * @return {@code true} if the image is tiled.
     * @throws IOException If an I/O operation was required and failed.
     */
    @Override
    final boolean isImageTiled() throws IOException {
        return count >= 2;
    }

    /**
     * Returns a reference to the tiles used internally by this tile manager.
     * This implementation returns an instance of {@link FrequencySortedSet} whith
     * {@linkplain FrequencySortedSet#frequencies frequency values} greater than 1
     * for the tiles that actually represent a pattern.
     */
    @Override
    final Collection<Tile> getInternalTiles() {
        final FrequencySortedSet<Tile> tiles = new FrequencySortedSet<Tile>();
        for (final OverviewLevel level : levels) {
            level.addInternalTiles(tiles);
        }
        return tiles;
    }

    /**
     * Returns all tiles. The list is generated on the fly every time this method is invoked.
     * The list is not and should not be cached since it may be large.
     *
     * @throws IOException If an I/O operation was required and failed.
     */
    public Collection<Tile> getTiles() throws IOException {
        final ArrayList<Tile> tiles = new ArrayList<Tile>(count);
        for (final OverviewLevel level : levels) {
            level.addTiles(tiles);
        }
        return tiles;
    }

    /**
     * Returns every tiles that intersect the given region.
     *
     * @throws IOException If it was necessary to fetch an image dimension from its
     *         {@linkplain Tile#getImageReader reader} and this operation failed.
     */
    public Collection<Tile> getTiles(final Rectangle region, final Dimension subsampling,
                                     final boolean subsamplingChangeAllowed) throws IOException
    {
        int[] tileCosts = null;
        long lowestCost = Long.MAX_VALUE;
        OverviewLevel bestLevel = null;
        Dimension bestSubsampling = null;
        ArrayList<Tile> tiles = null;
        final Rectangle tmpRegion = new Rectangle();
        final Dimension tmpSubsampling = new Dimension();
        for (int ordinal=levels.length; --ordinal>=0;) {
            final OverviewLevel level = levels[ordinal];
            final Tile sample = level.getSample();
            final Dimension doable = sample.getSubsamplingFloor(subsampling);
            if (doable == null) {
                // The current level can not handle the given subsampling or any finer one.
                continue;
            }
            if (doable != subsampling) {
                if (!subsamplingChangeAllowed) {
                    // The current level can not handle the given subsampling
                    // and we are not allowed to use a finer one.
                    continue;
                }
            }
            /**
             * Gets the tiles at current level and checks if the cost of reading them is lower
             * than the cost of reading the tiles at the previous (coarser) level. They could
             * be lower if the region to read is small enough so that reading smaller tiles
             * compensate the cost of applying a higher subsampling.
             */
            final ArrayList<Tile> finers = level.getTiles(region, subsampling, levels, ordinal);
            if (ordinal == 0 && tiles == null) {
                subsampling.setSize(doable);
                return finers; // Optimization when we known there is nothing more to analyze.
            }
            long cost = 0;
            final int[] costs = new int[finers.size()];
            for (int i=0; i<costs.length; i++) {
                final Tile tile = finers.get(i);
                tmpRegion.setRect(region);
                tmpSubsampling.setSize(doable);
                cost += (costs[i] = tile.countUnwantedPixelsFromAbsolute(tmpRegion, tmpSubsampling));
            }
            if (cost <= lowestCost) {
                lowestCost = cost;
                tileCosts = costs;
                bestLevel = level;
                bestSubsampling = doable;
                tiles = finers;
                continue;
            }
            /*
             * We have a set of tiles which is assumed to be the least costly one, since we
             * iterated over the overview levels starting with coarser level first. Now for
             * each tiles, checks if the tiles at the next (finer) level would be less costly.
             * This is usually not the case, except on the region border where reading smaller
             * tiles may be less costly even if they require higher subsampling.
             */
            if (!level.isDivisorOf(bestLevel)) {
                continue;
            }
            for (int i=0; i<tileCosts.length; i++) {
                final Tile tile = tiles.get(i);
                
            }
            break;
        }
        if (bestSubsampling != null) {
            subsampling.setSize(bestSubsampling);
        }
        return tiles;
    }
}
