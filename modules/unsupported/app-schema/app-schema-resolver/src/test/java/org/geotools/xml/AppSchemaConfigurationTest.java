/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2004-2010, Open Source Geospatial Foundation (OSGeo)
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

package org.geotools.xml;

import java.io.File;
import java.net.URL;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import junit.framework.Assert;

import org.geotools.data.DataUtilities;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests of {@link AppSchemaConfiguration} used with {@link Schemas#findSchemas(Configuration)}.
 * 
 * @author Ben Caradoc-Davies, CSIRO Earth Science and Resource Engineering
 */
public class AppSchemaConfigurationTest {

    private static final Logger LOGGER = org.geotools.util.logging.Logging
            .getLogger(AppSchemaConfigurationTest.class.getPackage().getName());
    
    /**
     * Set this to true if you want to see all the resolved locations. 
     */
    private static final boolean ADJUST_LOGLEVEL = false;

    private static final Level LOGLEVEL = Level.FINE;

    private Level logLevel;

    private Level rootLogLevel;

    /**
     * Hack the log level so we can see schemas loading.
     */
    @Before
    public void before() {
        if (ADJUST_LOGLEVEL) {
            logLevel = LOGGER.getLevel();
            LOGGER.setLevel(LOGLEVEL);
            rootLogLevel = getRootLogHandler().getLevel();
            LogManager.getLogManager().getLogger("").getHandlers()[0].setLevel(LOGLEVEL);
        }
    }

    /**
     * Restore the log level.
     */
    @After
    public void after() {
        if (ADJUST_LOGLEVEL) {
            getRootLogHandler().setLevel(rootLogLevel);
            LOGGER.setLevel(logLevel);
        }
    }

    /**
     * Return the root log handler, needed to hack the log level.
     */
    private static Handler getRootLogHandler() {
        return LogManager.getLogManager().getLogger("").getHandlers()[0];
    }

    /**
     * Test we can {@link Schemas#findSchemas(Configuration)} with a catalog plus classpath.
     */
    @Test
    public void catalog() throws Exception {
        Configuration configuration = new AppSchemaConfiguration(
                "http://schemas.example.org/catalog-test",
                "http://schemas.example.org/catalog-test/catalog-test.xsd", new AppSchemaResolver(
                        AppSchemaCatalog.build(getClass().getResource("/test-data/catalog.xml"))));
        SchemaIndex schemaIndex = Schemas.findSchemas(configuration);
        Assert.assertEquals(3, schemaIndex.getSchemas().length);
        String schemaLocation = schemaIndex.getSchemas()[0].getSchemaLocation();
        Assert.assertTrue(schemaLocation.startsWith("file:"));
        Assert.assertTrue(schemaLocation.endsWith("catalog-test.xsd"));
        Assert.assertTrue(DataUtilities.urlToFile(new URL(schemaLocation)).exists());
    }

    /**
     * Test we can {@link Schemas#findSchemas(Configuration)} with classpath only.
     */
    @Test
    public void classpath() {
        Configuration configuration = new AppSchemaConfiguration("urn:cgi:xmlns:CGI:GeoSciML:2.0",
                "http://www.geosciml.org/geosciml/2.0/xsd/geosciml.xsd", new AppSchemaResolver());
        SchemaIndex schemaIndex = Schemas.findSchemas(configuration);
        Assert.assertEquals(3, schemaIndex.getSchemas().length);
        String schemaLocation = schemaIndex.getSchemas()[0].getSchemaLocation();
        Assert.assertTrue(schemaLocation.startsWith("jar:file:"));
        Assert.assertTrue(schemaLocation.endsWith("geosciml.xsd"));
   }

    /**
     * Test we can {@link Schemas#findSchemas(Configuration)} with cache and classpath.
     */
    @Test
    public void cache() throws Exception {
        File cacheDirectory = DataUtilities.urlToFile(AppSchemaCacheTest.class
                .getResource("/test-data/cache"));
        AppSchemaResolver resolver = new AppSchemaResolver(
                new AppSchemaCache(cacheDirectory, false));
        Configuration configuration = new AppSchemaConfiguration(
                "http://schemas.example.org/cache-test",
                "http://schemas.example.org/cache-test/cache-test.xsd", resolver);
        SchemaIndex schemaIndex = Schemas.findSchemas(configuration);
        Assert.assertEquals(3, schemaIndex.getSchemas().length);
        String schemaLocation = schemaIndex.getSchemas()[0].getSchemaLocation();
        Assert.assertTrue(schemaLocation.startsWith("file:"));
        Assert.assertTrue(schemaLocation.endsWith("cache-test.xsd"));
        Assert.assertTrue(DataUtilities.urlToFile(new URL(schemaLocation)).exists());
    }

}
