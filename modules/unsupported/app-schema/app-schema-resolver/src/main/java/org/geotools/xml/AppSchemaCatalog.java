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
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.xml.resolver.Catalog;
import org.apache.xml.resolver.CatalogManager;

/**
 * Support for application schema resolution in an <a
 * href="http://www.oasis-open.org/committees/entity/spec-2001-08-06.html">OASIS Catalog</a> (with
 * URI resolution semantics).
 * 
 * @author Ben Caradoc-Davies, CSIRO Earth Science and Resource Engineering
 * @see
 */
public class AppSchemaCatalog {

    private static final Logger LOGGER = org.geotools.util.logging.Logging
            .getLogger(AppSchemaCatalog.class.getPackage().getName());

    private final Catalog catalog;

    /**
     * Use {@link #build(URL)} to construct an instance.
     * 
     * @param catalog
     */
    private AppSchemaCatalog(Catalog catalog) {
        this.catalog = catalog;
    }

    /**
     * Return schema location resolved to local file if possible.
     * 
     * @param location
     *            typically an absolute http/https URL.
     * @return null if catalog is null or location not found in catalog
     */
    public String resolveLocation(String location) {
        String resolvedLocation = null;
        try {
            LOGGER.finest("Resolving " + location);
            /*
             * See discussion of rewriteSystem versus rewriteURI:
             * https://www.seegrid.csiro.au/twiki/bin/view/AppSchemas/ConfiguringXMLProcessors Old
             * version used rewriteSystem.
             */
            resolvedLocation = catalog.resolveURI(location);
            if (resolvedLocation != null) {
                LOGGER.finer("Verifying existence of " + "catalog resolved location "
                        + resolvedLocation);
                try {
                    File f = new File(new URI(resolvedLocation));
                    if (!f.exists()) {
                        // catalog miss
                        LOGGER.finer("Cannot locate " + resolvedLocation);
                        resolvedLocation = null;
                    }
                } catch (URISyntaxException e) {
                    resolvedLocation = null;
                    LOGGER.log(Level.WARNING, "Exception resolving " + resolvedLocation, e);
                }
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return resolvedLocation;
    }

    /**
     * Build a private {@link Catalog}, that is, not the static instance that {@link CatalogManager}
     * returns by default.
     * 
     * <p>
     * 
     * Care must be taken to use only private {@link Catalog} instances if there will ever be more
     * than one OASIS Catalog used in a single class loader (i.e. a single maven test run),
     * otherwise {@link Catalog} contents will be an amalgam of the entries of both OASIS Catalog
     * files, with likely unintended or incorrect results. See GEOT-2497.
     * 
     * @param catalogLocation
     *            URL of OASIS Catalog
     * @return a private Catalog
     */
    static Catalog buildPrivateCatalog(URL catalogLocation) {
        CatalogManager catalogManager = new CatalogManager();
        catalogManager.setUseStaticCatalog(false);
        catalogManager.setVerbosity(0);
        catalogManager.setIgnoreMissingProperties(true);
        Catalog catalog = catalogManager.getCatalog();
        try {
            catalog.parseCatalog(catalogLocation);
        } catch (IOException e) {
            throw new RuntimeException("Error trying to load OASIS catalog from URL "
                    + catalogLocation.toString(), e);
        }
        return catalog;
    }

    /**
     * Build an catalog using the given OASIS Catalog file URL.
     * 
     * @param catalogLocation
     *            local file URL to an OASIS cCtalog
     * @return
     */
    public static AppSchemaCatalog build(URL catalogLocation) {
        return new AppSchemaCatalog(buildPrivateCatalog(catalogLocation));
    }

}
