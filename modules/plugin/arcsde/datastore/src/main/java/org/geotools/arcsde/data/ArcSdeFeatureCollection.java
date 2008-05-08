/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2008, Geotools Project Managment Committee (PMC)
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
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.arcsde.data.versioning.ArcSdeVersionHandler;
import org.geotools.arcsde.pool.Session;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureReader;
import org.geotools.data.Query;
import org.geotools.data.store.DataFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureReaderIterator;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.util.logging.Logging;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * FeatureCollection implementation that works over an {@link ArcSDEFeatureReader} or one of the
 * decorators over it returned by
 * {@link ArcSDEDataStore#getFeatureReader(Query, Session, boolean)}.
 * <p>
 * Note this class and the iterators it returns are thread safe.
 * </p>
 * 
 * @author Gabriel Roldan (TOPP)
 * @version $Id$
 * @since 2.5
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/gt/modules/plugin/arcsde/datastore/src/main/java/org/geotools/arcsde/data/ArcSdeFeatureCollection.java $
 * @see FeatureCollection
 */
public class ArcSdeFeatureCollection extends DataFeatureCollection {

    private static final Logger LOGGER = Logging.getLogger("org.geotools.arcsde.data");

    private final ArcSdeFeatureSource featureSource;

    private final Query query;

    private final Set<ArcSdeFeatureReaderIterator> openIterators;

    private Session session;

    private SimpleFeatureType childrenSchema;

    public ArcSdeFeatureCollection(final ArcSdeFeatureSource featureSource, final Query namedQuery) {
        this.featureSource = featureSource;
        this.query = namedQuery;

        final Set<ArcSdeFeatureReaderIterator> iterators;
        iterators = new HashSet<ArcSdeFeatureReaderIterator>();
        this.openIterators = Collections.synchronizedSet(iterators);
    }

    /**
     * @see FeatureCollection#getSchema()
     */
    @Override
    public final synchronized SimpleFeatureType getSchema() {
        if (childrenSchema == null) {
            final Session session = getLockedConnection();
            try {
                final ArcSDEDataStore dataStore = featureSource.getDataStore();
                DefaultQuery excludeFilterQuery = new DefaultQuery(this.query);
                excludeFilterQuery.setFilter(Filter.EXCLUDE);

                final FeatureReader<SimpleFeatureType, SimpleFeature> reader;
                reader = dataStore.getFeatureReader(excludeFilterQuery, session, false,
                        ArcSdeVersionHandler.NONVERSIONED_HANDLER);

                try {
                    this.childrenSchema = reader.getFeatureType();
                } finally {
                    reader.close();
                }
            } catch (IOException e) {
                throw new RuntimeException("Can't fetch schema for query " + query, e);
            } finally {
                unlockConnection();
                closeConnectionIfNeedBe();
            }
        }
        return childrenSchema;
    }

    /**
     * @see FeatureCollection#getBounds()
     */
    @Override
    public final ReferencedEnvelope getBounds() {
        ReferencedEnvelope bounds;
        final Session session = getLockedConnection();

        LOGGER.info("Getting collection bounds");
        try {
            bounds = featureSource.getBounds(query, session);
            if (bounds == null) {
                LOGGER.info("FeatureSource returned null bounds, going to return an empty one");
                bounds = new ReferencedEnvelope(getCRS());
            }
        } catch (IOException e) {
            LOGGER.log(Level.INFO, "Error getting collection bounts", e);
            bounds = new ReferencedEnvelope(getCRS());
        } finally {
            unlockConnection();
            closeConnectionIfNeedBe();
        }
        return bounds;
    }

    private CoordinateReferenceSystem getCRS() {
        GeometryDescriptor defaultGeometry = this.featureSource.getSchema().getDefaultGeometry();
        return defaultGeometry == null ? null : defaultGeometry.getCRS();
    }

    @Override
    public final int getCount() throws IOException {
        final Session session = getLockedConnection();
        try {
            return featureSource.getCount(query, session);
        } finally {
            unlockConnection();
            closeConnectionIfNeedBe();
        }
    }

    /**
     * @param openIterator an {@link ArcSdeFeatureReaderIterator}
     */
    @Override
    protected final void closeIterator(Iterator<SimpleFeature> openIterator) throws IOException {
        ArcSdeFeatureReaderIterator iterator = (ArcSdeFeatureReaderIterator) openIterator;
        iterator.close();
    }

    private void releaseIterator(ArcSdeFeatureReaderIterator iterator) {
        this.openIterators.remove(iterator);
    }

    private static class ArcSdeFeatureReaderIterator extends FeatureReaderIterator<SimpleFeature> {
        private Session session;

        private final ArcSdeFeatureCollection parent;

        /**
         * Grabs a lock over the connection for the iterator's life time (until close())
         * 
         * @param reader
         * @param session
         * @param parent
         */
        public ArcSdeFeatureReaderIterator(final FeatureReader<SimpleFeatureType, SimpleFeature> reader,
                                           final Session session,
                                           final ArcSdeFeatureCollection parent) {
            super(reader);
            session.getLock().lock();
            this.session = session;
            this.parent = parent;
        }

        @Override
        public void close() {
            if (session == null) {
                return;
            }
            // /connection.getLock().lock();
            try {
                // close the underlying feature reader
                super.close();
                parent.releaseIterator(this);
            } finally {
                session.getLock().unlock();
                parent.closeConnectionIfNeedBe();
                session = null;
            }
        }

        @Override
        public boolean hasNext() {
            if (session == null) {
                throw new IllegalStateException("Iterator already closed");
            }
            ///connection.getLock().lock();
            try {
                return super.hasNext();
            } finally {
               /// connection.getLock().unlock();
            }
        }

        @Override
        public SimpleFeature next() {
            ///connection.getLock().lock();
            try {
                return super.next();
            } finally {
               /// connection.getLock().unlock();
            }
        }
    }

    /**
     * Returns
     */
    @Override
    protected synchronized final Iterator<SimpleFeature> openIterator() throws IOException {
        final Session session = getLockedConnection();

        final FeatureReader<SimpleFeatureType, SimpleFeature> reader;
        try {
            final ArcSDEDataStore dataStore = featureSource.getDataStore();
            final ArcSdeVersionHandler versionHandler = featureSource.getVersionHandler();
            final boolean readerClosesConnection = false;
            reader = dataStore.getFeatureReader(query, session, readerClosesConnection,
                    versionHandler);
        } finally {
            unlockConnection();
        }
        // slight optimization here: store the child features schema if not yet
        // done by getSchema()
        if (this.childrenSchema == null) {
            this.childrenSchema = reader.getFeatureType();
        }

        final ArcSdeFeatureReaderIterator iterator;
        // give the itertor the connection unlocked, it will lock/unlock on a more granular basis
        iterator = new ArcSdeFeatureReaderIterator(reader, session, this);
        this.openIterators.add(iterator);
        return iterator;
    }

    /**
     * Returns the underlying feature source connection priorly locking it for thread safety. Relies
     * on the feature source to return an appropriate connection depending on whether it is under a
     * transaction or not.
     * 
     * @return
     * @throws RuntimeException if the connection can't be acquired
     */
    private synchronized Session getLockedConnection() {
        if (this.session == null) {
            try {
                session = featureSource.getConnection();
            } catch (IOException e) {
                throw new RuntimeException("Can't acquire connection", e);
            }
        }
        this.session.getLock().lock();
        return this.session;
    }

    private synchronized void unlockConnection() {
        session.getLock().unlock();
    }

    /**
     * Closes the connection only if there are no open iterators generated by this collection. The
     * connection shall be already {@link #unlockConnection(Session) unlocked}. This
     * method is intended to be called both by {@link #getBounds()}, {@link #getCount()} and
     * {@link ArcSdeFeatureReaderIterator#close()}
     */
    private synchronized void closeConnectionIfNeedBe() {
        if (openIterators.size() == 0) {
            // only close if its not already returned to the pool (ie, already closed)
            if (!session.isPassivated()) {
                // and there's no a transaction being run over that connection
                if (!session.isTransactionActive()) {
                    session.close();
                    session = null;
                }
            }
        }
    }
}
