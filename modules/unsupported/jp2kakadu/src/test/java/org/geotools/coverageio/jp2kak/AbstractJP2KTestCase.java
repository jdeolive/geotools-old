/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2007-2008, Open Source Geospatial Foundation (OSGeo)
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
 *
 */
package org.geotools.coverageio.jp2kak;

import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.media.jai.JAI;

import junit.framework.TestCase;

import org.geotools.coverage.grid.io.GridFormatFactorySpi;

/**
 * @author Daniele Romagnoli, GeoSolutions
 * @author Simone Giannecchini (simboss), GeoSolutions
 * 
 * Base JP2K testing class.
 */
public abstract class AbstractJP2KTestCase extends TestCase {
    protected final static Logger LOGGER = org.geotools.util.logging.Logging
            .getLogger("org.geotools.coverageio.jp2kak");

    /**
     * A String containing the name of the supported format. It will be used to
     * customize the messages.
     */
    private String supportedFormat;

    /**
     * The {@code GridFormatFactorySpi} provided by the specific subclass to
     * handle a specific format.
     */
    private GridFormatFactorySpi factorySpi;

    public AbstractJP2KTestCase(String name) {
        super(name);
        this.supportedFormat = "JP2K";
        this.factorySpi = new JP2KFormatFactory();
    }

    protected void setUp() throws Exception {
        super.setUp();
        ImageIO.setUseCache(false);
        JAI.getDefaultInstance().getTileCache().setMemoryCapacity(
                10 * 1024 * 1024);
        JAI.getDefaultInstance().getTileCache().setMemoryThreshold(1.0f);
        JAI.getDefaultInstance().getTileScheduler().setParallelism(2);
        JAI.getDefaultInstance().getTileScheduler().setPrefetchParallelism(2);
        JAI.getDefaultInstance().getTileScheduler().setPrefetchPriority(5);
        JAI.getDefaultInstance().getTileScheduler().setPriority(5);
    }

    protected boolean testingEnabled() {
        boolean available = factorySpi.isAvailable();

        if (!available) {
            LOGGER.warning(supportedFormat
                    + " libraries are not available, skipping tests!");
        }

        return available;
    }
}