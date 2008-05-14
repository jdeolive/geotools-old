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

import org.geotools.referencing.operation.matrix.XAffineTransform;


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
     * A single tile representing the whole mosaic. This tile will never be loaded, but the
     * information contained in it will be used for inferring other tiles.
     */
    private final Tile mosaic;

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
        for (int i=0; i<levels.length; i++) {
            levels[i].process(i);
        }
        mosaic = null; // TODO
    }

    /**
     * Sets the {@linkplain Tile#getGridTocRS grid to CRS} transform for every tiles.
     * This method can be invoked only once.
     *
     * @param gridToCRS The "grid to CRS" transform.
     * @throws IllegalStateException if a transform was already assigned to at least one tile.
     * @throws IOException if an I/O operation was required and failed.
     */
    @Override
    public synchronized void setGridToCRS(AffineTransform gridToCRS)
            throws IllegalStateException, IOException
    {
        mosaic.setGridToCRS(new XAffineTransform(gridToCRS));
    }
}
