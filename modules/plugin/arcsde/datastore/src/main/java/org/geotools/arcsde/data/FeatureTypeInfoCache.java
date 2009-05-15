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
package org.geotools.arcsde.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.jsqlparser.statement.select.PlainSelect;

import org.geotools.arcsde.pool.ISession;
import org.geotools.arcsde.pool.SessionPool;
import org.geotools.data.DataSourceException;
import org.geotools.data.Transaction;
import org.geotools.feature.NameImpl;
import org.geotools.util.logging.Logging;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;

import com.esri.sde.sdk.client.SeLayer;

/**
 * Maintains a cache of {@link FeatureTypeInfo} objects for fast retrieval of ArcSDE vector layer
 * information and its corresponding geotools {@link FeatureType}.
 * <p>
 * {@link SeLayer} objects are not cached, they hold a reference to its connection and hence may
 * only be used inside the connection's context. Instead, a set of layer names is kept and the set
 * of actual {@link FeatureTypeInfo}s is lazily loaded on demand.
 * </p>
 * <p>
 * This class may set up a background process to periodically update the list of available layer
 * names in the server and clear the feature type cache. See the constructor's javadoc for more
 * info.
 * </p>
 * 
 * @author Gabriel Roldan
 * @version $Id$
 * @since 2.5.6
 * @source $URL$
 */
final class FeatureTypeInfoCache {

    private static final Logger LOGGER = Logging.getLogger("org.geotools.arcsde.data");

    /**
     * ArcSDE registered layers definitions
     */
    public Map<String, FeatureTypeInfo> featureTypeInfos;

    /**
     * In process view definitions. This map is populated through
     * {@link #registerView(String, PlainSelect)}
     */
    public final Map<String, FeatureTypeInfo> inProcessFeatureTypeInfos;

    private final SessionPool sessionPool;

    /**
     * list of available featureclasses in the database. Does not contain in-process view type
     * names. SeLayer objects are not cached because they hold a reference to its SeConnection and
     * hence need to be used only inside its connection context.
     */
    private final Set<String> availableLayerNames;

    /**
     * Namespace URI to construct FeatureTypes and AttributeTypes with
     */
    private final String namespace;

    /**
     * Scheduler for cache updating.
     */
    private final ScheduledExecutorService cacheUpdateScheduler;

    /**
     * Lock for protecting featureTypeInfos cache.
     */
    private final ReentrantReadWriteLock cacheLock;

    /**
     * Creates a FeatureTypeInfoCache
     * <p>
     * The provided {@link SessionPool} is used to grab an {@link ISession} when the list of
     * available layers needs to be updated. This update happens at this class' construction time
     * and, optionally, every {@code cacheUpdateFreqSecs} seconds.
     * </p>
     * 
     * @param sessionPool
     * @param namespace
     *            the namespace {@link FeatureType}s are created with, may be {@code null}
     * @param cacheUpdateFreqSecs
     *            layer name cache update frequency, in seconds. {@code <= 0} means do never update.
     * @throws IOException
     */
    public FeatureTypeInfoCache(final SessionPool sessionPool, final String namespace,
            final int cacheUpdateFreqSecs) throws IOException {

        availableLayerNames = new TreeSet<String>(sessionPool.getAvailableLayerNames());

        featureTypeInfos = new HashMap<String, FeatureTypeInfo>();
        inProcessFeatureTypeInfos = new HashMap<String, FeatureTypeInfo>();
        this.sessionPool = sessionPool;
        this.namespace = namespace;
        this.cacheLock = new ReentrantReadWriteLock();

        if (cacheUpdateFreqSecs > 0) {
            cacheUpdateScheduler = Executors.newScheduledThreadPool(1);
            CacheUpdater cacheUpdater = new CacheUpdater();
            LOGGER.info("Scheduling the layer name cache to be updated every "
                    + cacheUpdateFreqSecs + " seconds.");
            cacheUpdateScheduler.scheduleWithFixedDelay(cacheUpdater, cacheUpdateFreqSecs,
                    cacheUpdateFreqSecs, TimeUnit.SECONDS);
        } else {
            cacheUpdateScheduler = null;
        }
    }

    public void dispose() {
        if (cacheUpdateScheduler != null) {
            LOGGER.info("Shutting down cache update scheduler");
            cacheUpdateScheduler.shutdownNow();
        }
    }

    public void addInprocessViewInfo(final FeatureTypeInfo typeInfo) {
        inProcessFeatureTypeInfos.put(typeInfo.getFeatureTypeName(), typeInfo);
    }

    public String getNamesapceURI() {
        return namespace;
    }

    public List<String> getTypeNames() {
        cacheLock.readLock().lock();

        List<String> layerNames;
        try {
            layerNames = new ArrayList<String>(availableLayerNames);
        } finally {
            cacheLock.readLock().unlock();
        }
        layerNames.addAll(this.inProcessFeatureTypeInfos.keySet());
        Collections.sort(layerNames);
        return layerNames;
    }

    public List<Name> getNames() {
        final List<String> typeNames = getTypeNames();
        List<Name> names = new ArrayList<Name>(typeNames.size());
        for (String typeName : typeNames) {
            NameImpl name = namespace == null ? new NameImpl(typeName) : new NameImpl(namespace,
                    typeName);
            names.add(name);
        }
        return names;
    }

    /**
     * Check inProcessFeatureTypeInfos and featureTypeInfos for the provided typeName, checking the
     * ArcSDE server as a last resort.
     * 
     * @param typeName
     * @return
     */
    public FeatureTypeInfo getFeatureTypeInfo(final String typeName) throws IOException {
        FeatureTypeInfo typeInfo = getCachedTypeInfo(typeName);
        if (typeInfo != null) {
            return typeInfo;
        }

        ISession session = sessionPool.getSession(Transaction.AUTO_COMMIT);
        try {
            typeInfo = getFeatureTypeInfo(typeName, session);
        } finally {
            session.dispose();
        }
        return typeInfo;
    }

    /**
     * Used by feature reader and writer to get the schema information.
     * <p>
     * They are making use of this function because they already have their own Session to request
     * the ftInfo if needed.
     * </p>
     * 
     * @param typeName
     * @param session
     * @return
     * @throws IOException
     */
    public FeatureTypeInfo getFeatureTypeInfo(final String typeName, final ISession session)
            throws IOException {

        FeatureTypeInfo typeInfo = getCachedTypeInfo(typeName);
        if (typeInfo != null) {
            return typeInfo;
        }

        cacheLock.writeLock().lock();
        // Recheck so it hasn't been done already.
        try {
            typeInfo = featureTypeInfos.get(typeName);
            if (typeInfo == null) {
                typeInfo = ArcSDEAdapter.fetchSchema(typeName, this.namespace, session);
                featureTypeInfos.put(typeName, typeInfo);
            }
        } finally {
            cacheLock.writeLock().unlock();
        }
        return typeInfo;
    }

    /**
     * @param typeName
     * @return the cached type info if there's one for typeName, {@code null} otherwise
     * @throws DataSourceException
     */
    private FeatureTypeInfo getCachedTypeInfo(final String typeName) throws DataSourceException {
        FeatureTypeInfo typeInfo = inProcessFeatureTypeInfos.get(typeName);
        if (typeInfo != null) {
            return typeInfo;
        }

        // Check if this is a known featureType
        cacheLock.readLock().lock();
        try {
            if (!availableLayerNames.contains(typeName)) {
                throw new DataSourceException(typeName + " does not exist");
            }
            typeInfo = featureTypeInfos.get(typeName);
        } finally {
            cacheLock.readLock().unlock();
        }

        return typeInfo;
    }

    private final class CacheUpdater implements Runnable {

        public void run() {
            List<String> typeNames = null;
            LOGGER.fine("FeatureTypeCache background process running...");
            try {
                typeNames = sessionPool.getAvailableLayerNames();
                {// just some logging..
                    Set<String> added = new TreeSet<String>(typeNames);
                    if (added.removeAll(availableLayerNames)) {
                        LOGGER.finer("FeatureTypeCache: added the following layers: " + added);
                    }
                    Set<String> removed = new TreeSet<String>(availableLayerNames);
                    if (removed.removeAll(typeNames)) {
                        LOGGER.finer("FeatureTypeCache: the following layers are no "
                                + "longer available: " + removed);
                    }
                }
                availableLayerNames.clear();
                availableLayerNames.addAll(typeNames);
                LOGGER.fine("FeatureTypeCache: updated server layer list: " + typeNames);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Updating TypeNameCache failed.", e);
                return;
            }

            HashMap<String, FeatureTypeInfo> aNewCache = new HashMap<String, FeatureTypeInfo>(
                    2 * typeNames.size());
            cacheLock.readLock().lock();
            for (String typeName : typeNames) {
                aNewCache.put(typeName, featureTypeInfos.get(typeName));
            }
            cacheLock.readLock().unlock();

            cacheLock.writeLock().lock();
            featureTypeInfos = aNewCache;
            cacheLock.writeLock().unlock();
        }
    }

}
