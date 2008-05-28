/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2006, Geotools Project Managment Committee (PMC)
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
 */
package org.geotools.gce.imagemosaic.jdbc;

import org.geotools.coverage.grid.io.GridFormatFactorySpi;

import org.opengis.coverage.grid.Format;

import java.util.Collections;
import java.util.Map;


/**
 * Implementation of the GridCoverageFormat service provider interface for
 * mosaicing of georeferenced images and image pyramids  stored in a jdbc database 
 *
 * @author mcr
 * @since 2.5
 */
public class ImageMosaicJDBCFormatFactory implements GridFormatFactorySpi {
    /**
     * Tells me if this plugin will work on not given the actual installation.
     *
     * <p>
     * Dependecies are mostly from JAI and ImageIO so if they are installed you
     * should not have many problems.
     *
     * @return False if something's missing, true otherwise.
     */
    public boolean isAvailable() {
        boolean available = true;

        try {
            Class.forName("javax.media.jai.JAI");
            Class.forName(
                "org.geotools.gce.imagemosaic.jdbc.ImageMosaicJDBCReader");
        } catch (ClassNotFoundException cnf) {
            available = false;
        }

        return available;
    }

    /**
     * @see GridFormatFactorySpi#createFormat().
     */
    public Format createFormat() {
        return new ImageMosaicJDBCFormat();
    }

    /**
     * Returns the implementation hints. The default implementation returns an
     * empty map.
     *
     * @return An empty map.
     */
    public Map getImplementationHints() {
        return Collections.EMPTY_MAP;
    }
}
