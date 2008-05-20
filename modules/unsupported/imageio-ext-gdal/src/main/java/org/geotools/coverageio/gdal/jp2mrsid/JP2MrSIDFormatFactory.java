/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2007, GeoTools Project Management Committee (PMC)
 *    (C) 2007, GeoSolutions S.A.S.
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
package org.geotools.coverageio.gdal.jp2mrsid;

import it.geosolutions.imageio.plugins.jp2mrsid.JP2GDALMrSidImageReaderSpi;

import java.util.Collections;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.coverage.grid.io.GridFormatFactorySpi;
import org.opengis.coverage.grid.Format;


/**
 * Implementation of the {@link Format} service provider interface for JP2K
 * files.
 *
 * @author Daniele Romagnoli, GeoSolutions
 * @author Simone Giannecchini (simboss), GeoSolutions
 * @since 2.5.x
 */
@SuppressWarnings("deprecation")
public final class JP2MrSIDFormatFactory implements GridFormatFactorySpi {
    /** Logger. */
    private final static Logger LOGGER = org.geotools.util.logging.Logging.getLogger(
            "org.geotools.coverageio.gdal.jp2mrisd");

    /**
     * Tells me if the coverage plugin to access JP2K via MrSID driver is available or not.
     *
     * @return {@code true} if the plugin is available, {@code false} otherwise.
     */
    public boolean isAvailable() {
        boolean available = true;

        // if these classes are here, then the runtime environment has
        // access to JAI and the JAI ImageI/O toolbox.
        try {
            Class.forName("javax.media.jai.JAI");
            Class.forName("com.sun.media.jai.operator.ImageReadDescriptor");
            Class.forName("it.geosolutions.imageio.plugins.jp2mrsid.JP2GDALMrSidImageReaderSpi");
            available = new JP2GDALMrSidImageReaderSpi().isAvailable();

            if (LOGGER.isLoggable(Level.FINE)) {
                if (available) {
                    LOGGER.fine("JP2MrSIDFormatFactory is availaible.");
                } else {
                    LOGGER.fine("JP2MrSIDFormatFactory is not availaible.");
                }
            }
        } catch (ClassNotFoundException cnf) {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("JP2MrSIDFormatFactory is not availaible.");
            }

            available = false;
        }

        return available;
    }

    /**
     * Creating a {@link JP2MrSIDFormat}.
     *
     * @return A {@link JP2MrSIDFormat}.;
     */
    public Format createFormat() {
        return new JP2MrSIDFormat();
    }

    /**
     * Returns the implementation hints. The default implementation returns an
     * empty map.
     *
     * @return DOCUMENT ME!
     */
    public Map getImplementationHints() {
        return Collections.EMPTY_MAP;
    }
}
