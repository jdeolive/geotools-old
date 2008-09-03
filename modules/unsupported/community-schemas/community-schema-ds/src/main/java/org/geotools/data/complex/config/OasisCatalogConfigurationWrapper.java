/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2004-2008, Open Source Geospatial Foundation (OSGeo)
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

package org.geotools.data.complex.config;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.xml.resolver.Catalog;
import org.eclipse.xsd.XSDSchema;
import org.eclipse.xsd.util.XSDSchemaLocationResolver;
import org.geotools.xml.BindingConfiguration;
import org.geotools.xml.Configuration;

public class OasisCatalogConfigurationWrapper extends Configuration {

    private static final Logger LOGGER = org.geotools.util.logging.Logging.getLogger(OasisCatalogConfigurationWrapper.class
            .getPackage().getName());

    private Catalog catalog;

    private Configuration configuration;

    public OasisCatalogConfigurationWrapper(final Catalog catalog, final Configuration configuration) {
        this.catalog = catalog;
        this.configuration = configuration;
        addWrappedDepencencies();
    }

    private void addWrappedDepencencies() {
        Configuration dependency;
        for (Iterator it = configuration.allDependencies().iterator(); it.hasNext();) {
            dependency = (Configuration) it.next();
            addDependency(dependency);
        }
    }

    public BindingConfiguration getBindingConfiguration() {
        return configuration.getBindingConfiguration();
    }

    public String getNamespaceURI() {
        return configuration.getNamespaceURI();
    }

    public String getSchemaFileURL() {
        String schemaFileURL = configuration.getSchemaFileURL();
        try {
            String resolvedLocation = catalog.resolveSystem(schemaFileURL);
            if(resolvedLocation != null){
                schemaFileURL = resolvedLocation;
            }
        } catch (MalformedURLException e) {
            LOGGER.log(Level.SEVERE, "Illegal schema URL: " + schemaFileURL, e);
            throw new RuntimeException(e);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error reading Oasis Catalog: " + e.getMessage(), e);
            throw new RuntimeException(e);
        }
        return schemaFileURL;
    }

    public XSDSchemaLocationResolver getSchemaLocationResolver() {
        final XSDSchemaLocationResolver resolver = configuration.getSchemaLocationResolver();
        final XSDSchemaLocationResolver lastResortCatalogResolver;
        lastResortCatalogResolver = new CatalogSchemaLocationResolverWrapper(catalog, resolver);
        return lastResortCatalogResolver;
    }

    private static class CatalogSchemaLocationResolverWrapper implements XSDSchemaLocationResolver {

        private Catalog catalog;

        private XSDSchemaLocationResolver resolver;

        public CatalogSchemaLocationResolverWrapper(final Catalog catalog,
                final XSDSchemaLocationResolver resolver) {
            this.catalog = catalog;
            this.resolver = resolver;
        }

        /**
         * @param schema
         *            the schema being resolved
         * @param uri
         *            the namespace being resolved. If its an empty string (i.e.
         *            the location refers to an include, and thus the uri to the
         *            same one than the schema), the schema one is used.
         * @param location
         *            the xsd location, either of <code>schema</code>, an
         *            import or an include, for which to try resolving it as a
         *            relative path of the <code>schema</code> location.
         * @return
         * 
         */
        public String resolveSchemaLocation(final XSDSchema schema, final String url,
                final String location) {
            String schemaLocation = resolver.resolveSchemaLocation(schema, url, location);
            if (schemaLocation == null) {
                try {
                    LOGGER.finest("resolving " + location);
                    schemaLocation = catalog.resolveSystem(location);
                    if (schemaLocation != null) {
                        LOGGER.finer("Verifying existence of catalog resolved location "
                                + schemaLocation);
                        File f;
                        try {
                            f = new File(new URI(schemaLocation));
                            if (!f.exists()) {
                                LOGGER.info("Cannot locate " + schemaLocation);
                                schemaLocation = null;
                            }
                        } catch (URISyntaxException e) {
                            schemaLocation = null;
                            LOGGER.log(Level.WARNING, "Exception resolving " + schemaLocation, e);
                        }
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return schemaLocation;
        }
    }
}
