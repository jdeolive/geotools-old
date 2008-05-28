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


/**
 * Controls the way {@link MosaicImageWriter} writes the tiles. This include the behavior
 * when a file to be written already exists.
 *
 * @since 2.5
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @see MosaicImageWriteParam#setTileWritingPolicy
 * @see MosaicBuilder#createTileManager
 */
public enum TileWritingPolicy {
    /**
     * Overwrite existing tiles inconditionnaly.
     * This is the default behavior.
     */
    OVERWRITE,

    /**
     * Skip existing tiles. This option works only for {@linkplain Tile#getInput tile input}
     * of type {@link java.io.File}. Other types like {@link java.net.URL} are not garanteed
     * to be checked for existence; they may be always overwritten.
     */
    WRITE_NEWS_ONLY,

    /**
     * Do not write any tile. This option can be given to {@link MosaicBuilder#createTileManager}.
     * While it is legal to {@linkplain MosaicImageWriteParam#setTileWritingPolicy give this option
     * as a parameter} to the writer, this is typically useless except for testing purpose.
     */
    NO_WRITE
}
