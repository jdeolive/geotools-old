/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2002-2009, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.arcsde;

import static org.geotools.arcsde.ArcSDEDataStoreFactory.ALLOW_NON_SPATIAL_PARAM;
import static org.geotools.arcsde.ArcSDEDataStoreFactory.NAMESPACE_PARAM;
import static org.geotools.arcsde.ArcSDEDataStoreFactory.VERSION_PARAM;

import java.awt.RenderingHints.Key;
import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.geotools.arcsde.data.ArcSDEDataStore;
import org.geotools.arcsde.pool.ArcSDEConnectionConfig;
import org.geotools.arcsde.pool.ArcSDEDataStoreConfig;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFactorySpi;
import org.geotools.factory.GeoTools;
import org.geotools.util.logging.Logging;

/**
 * @author Gabriel Roldan (OpenGeo)
 * @version $Id$
 * @since 2.5.7
 */
public class ArcSDEJNDIDataStoreFactory implements DataStoreFactorySpi {

    private static final Logger LOGGER = Logging.getLogger("org.geotools.arcsde");

    private final ArcSDEDataStoreFactory delegateFactory;

    /**
     * JNDI context path name
     */
    public static final Param JNDI_REFNAME = new Param("jndiReferenceName", String.class,
            "JNDI context path", true, "java:comp/env/geotools/arcsde");

    private static final String J2EERootContext = "java:comp/env";

    public ArcSDEJNDIDataStoreFactory() {
        this.delegateFactory = new ArcSDEDataStoreFactory();
    }

    /**
     * @see org.geotools.data.DataStoreFactorySpi#createDataStore(java.util.Map)
     */
    public DataStore createDataStore(Map<String, Serializable> params) throws IOException {
        final String jndiName = (String) JNDI_REFNAME.lookUp(params);
        if (jndiName == null) {
            throw new IOException("Missing " + JNDI_REFNAME.description);
        }

        final Context ctx;

        try {
            ctx = GeoTools.getInitialContext(GeoTools.getDefaultHints());
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }

        Object lookup = null;
        try {
            lookup = ctx.lookup(jndiName);
        } catch (NamingException e1) {
            // check if the user did not specify "java:comp/env"
            // and this code is running in a J2EE environment
            try {
                if (jndiName.startsWith(J2EERootContext) == false) {
                    lookup = (DataSource) ctx.lookup(J2EERootContext + jndiName);
                    // success --> issue a waring
                    LOGGER.warning("Using " + J2EERootContext + jndiName + " instead of "
                            + jndiName + " would avoid an unnecessary JNDI lookup");
                }
            } catch (NamingException e2) {
                // do nothing, was only a try
            }
        }

        if (lookup == null) {
            throw new IOException("Cannot find JNDI data source: " + jndiName);
        }

        ArcSDEConnectionConfig config = (ArcSDEConnectionConfig) lookup;

        String nsUri = (String) NAMESPACE_PARAM.lookUp(params);
        String version = (String) VERSION_PARAM.lookUp(params);
        Boolean allowNonSpatialTables = (Boolean) ALLOW_NON_SPATIAL_PARAM.lookUp(params);

        boolean nonSpatial = allowNonSpatialTables == null ? false : allowNonSpatialTables
                .booleanValue();

        ArcSDEDataStoreConfig dsconfig = new ArcSDEDataStoreConfig(config, nsUri, version,
                nonSpatial);
        ArcSDEDataStore dataStore = delegateFactory.createDataStore(dsconfig);
        return dataStore;
    }

    /**
     * @see org.geotools.data.DataAccessFactory#canProcess(java.util.Map)
     */
    public boolean canProcess(Map<String, Serializable> params) {
        if (params == null) {
            return false;
        }
        String lookUpKey;
        try {
            lookUpKey = (String) JNDI_REFNAME.lookUp(params);
        } catch (IOException e) {
            return false;
        }
        if (lookUpKey == null) {
            return false;
        }
        try {
            Context ctx;
            ctx = GeoTools.getInitialContext(GeoTools.getDefaultHints());
            Object lookup = ctx.lookup(lookUpKey);
            return lookup != null;
        } catch (NamingException e) {
            LOGGER.log(Level.INFO, "Error in context look up for arcsde JNDI path", e);
        }
        return false;
    }

    /**
     * @see org.geotools.data.DataAccessFactory#getDescription()
     */
    public String getDescription() {
        return delegateFactory.getDescription() + " (JNDI)";
    }

    /**
     * @see org.geotools.data.DataAccessFactory#getDisplayName()
     */
    public String getDisplayName() {
        return delegateFactory.getDisplayName() + " (JNDI)";
    }

    /**
     * @see org.geotools.data.DataAccessFactory#getParametersInfo()
     */
    public Param[] getParametersInfo() {
        return new Param[] { JNDI_REFNAME, NAMESPACE_PARAM, VERSION_PARAM, ALLOW_NON_SPATIAL_PARAM };
    }

    /**
     * Determines if the datastore is available.
     * <p>
     * Check in an Initial Context is available, that is all what can be done Checking for the right
     * jdbc jars in the classpath is not possible here
     * </p>
     * 
     * @see org.geotools.data.DataAccessFactory#isAvailable()
     */
    public boolean isAvailable() {
        try {
            InitialContext context = GeoTools.getInitialContext(GeoTools.getDefaultHints());
            return context != null && delegateFactory.isAvailable();
        } catch (NamingException e) {
            return false;
        }
    }

    /**
     * @see org.geotools.factory.Factory#getImplementationHints()
     */
    @SuppressWarnings("unchecked")
    public Map<Key, ?> getImplementationHints() {
        return delegateFactory.getImplementationHints();
    }

    /**
     * @see org.geotools.data.DataStoreFactorySpi#createNewDataStore(java.util.Map)
     */
    public DataStore createNewDataStore(Map<String, Serializable> params) throws IOException {
        throw new UnsupportedOperationException("ArcSDE PlugIn does not support createNewDataStore");
    }
}
