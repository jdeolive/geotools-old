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
 */
package org.geotools.caching.firstdraft.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import com.vividsolutions.jts.geom.Envelope;
import org.opengis.filter.Filter;
import org.geotools.caching.firstdraft.DataCache;
import org.geotools.caching.firstdraft.FeatureIndex;
import org.geotools.caching.firstdraft.QueryTracker;
import org.geotools.data.AbstractDataStore;
import org.geotools.data.DataSourceException;
import org.geotools.data.DataStore;
import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureWriter;
import org.geotools.data.InProcessLockingManager;
import org.geotools.data.LockingManager;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.FeatureType;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.SchemaException;
import org.geotools.feature.SimpleFeature;


/** Implementation of DataCache that uses in-memory storage,
 * spatial query tracker and spatial index.
 *
 *  IMPORTANT : for the time being, this class provides cache facility only
 *  when using getView(Query) method. Other methods simply delegate to source DataStore.
 *
 * @task use this class to design AbstractDataCache,
 * which would be the parent of all DataCache implementation.
 *
 * @author Christophe Rousson, SoC 2007, CRG-ULAVAL
 *
 */
public class InMemoryDataCache extends AbstractDataStore implements DataCache {
    /**
     * The source DataStore, from where to get original features
     */
    private final DataStore source;

    /**
     * Trackers to keep relationships between queries and features.
     * engines is a list of CacheInternalEngine, one engine per feature type in DS.
     * Each engine is composed of :
     *  - a query tracker, intance of QueryTracker
     *  - a feature storage, instance of FeatureIndex
     */
    private final HashMap engines;

    /** Creates a new DataCache on top of DataStore ds.
     *
     * @param ds the DataStore to cache.
     */
    public InMemoryDataCache(DataStore ds) {
        this.source = ds;
        this.engines = new HashMap();

        try {
            String[] types = ds.getTypeNames();

            for (int i = 0; i < types.length; i++) {
                CacheInternalEngine engine = new CacheInternalEngine(this, ds.getSchema(types[i]));
                engines.put(types[i], engine);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            throw (RuntimeException) new RuntimeException().initCause(e);
        }
    }

    public void clear() {
        // TODO Auto-generated method stub
    }

    public void flush() throws IllegalStateException {
        // TODO Auto-generated method stub
    }

    public long getHits() {
        // TODO Auto-generated method stub
        return 0;
    }

    public void createSchema(FeatureType ft) throws IOException {
        source.createSchema(ft);

        CacheInternalEngine engine = new CacheInternalEngine(this, ft);
        engines.put(ft.getTypeName(), engine);
    }

    /*public FeatureReader getFeatureReader(Query q, Transaction t)
       throws IOException {
           t.
       return source.getFeatureReader(q, t);
       }*/

    /*public FeatureSource getFeatureSource(String ft)
       throws IOException {
       // TODO Auto-generated method stub
       return getEngine(ft).getIndex() ;
       }*/
    private CacheInternalEngine getEngine(String ft) {
        return (CacheInternalEngine) engines.get(ft);
    }

    /*public FeatureWriter getFeatureWriter(String arg0, Transaction arg1)
       throws IOException {
       return source.getFeatureWriter(arg0, arg1);
       }*/

    /*public FeatureWriter getFeatureWriter(String arg0, Filter arg1, Transaction arg2)
       throws IOException {
       // TODO Auto-generated method stub
       return source.getFeatureWriter(arg0, arg1, arg2);
       }*/

    /*public FeatureWriter getFeatureWriterAppend(String arg0, Transaction arg1)
       throws IOException {
       return source.getFeatureWriterAppend(arg0, arg1);
       }*/

    /*public LockingManager getLockingManager() {
       return source.getLockingManager();
       }*/
    public FeatureType getSchema(String ft) throws IOException {
        return getEngine(ft).getType();
    }

    public String[] getTypeNames() throws IOException {
        return (String[]) engines.keySet().toArray(new String[engines.keySet().size()]);
    }

    /* (non-Javadoc)
     * @see org.geotools.data.DataStore#getView(org.geotools.data.Query)
     *
     * This is the important method :
     *
     * Sequence proposed to process user query :
     *
     * user query
     *  -> match query in tracker
     *  -> dowload missing data from source
     *  -> add new data to cache
     *  -> register query in tracker
     *  -> read cache
     *     -> answer to query
     *
     */
    public FeatureSource getView(Query q) throws IOException, SchemaException {
        assert (q.getTypeName() != null);

        CacheInternalEngine engine = getEngine(q.getTypeName());

        if (engine == null) {
            throw new SchemaException("Type not found : " + q.getTypeName());
        }

        QueryTracker tracker = engine.getTracker();
        FeatureIndex index = getEngine(q.getTypeName()).getIndex();
        Query m = tracker.match(q);
        FeatureSource in = source.getView(m);
        FeatureCollection fc = in.getFeatures();

        // FIXME what if the query oversize the cache ?
        if (fc.size() > 0) {
            FeatureIterator i = fc.features();

            while (i.hasNext()) {
                index.add((Feature) i.next());
            }

            fc.close(i);
        }

        tracker.register(m);

        // if query q could not be turned into a "smaller" query, ie a query that yield a smaller set of features,
        // returns directly collection obtained from source, rather than reading the cache.
        if (m.equals(q)) {
            return in;
        } else {
            return index.getView(q);
        }
    }

    public void updateSchema(String ftname, FeatureType ft) {
        try {
            source.updateSchema(ftname, ft);
            engines.remove(ftname);

            CacheInternalEngine engine = new CacheInternalEngine(this, ft);
            engines.put(ftname, engine);
        } catch (IOException e) {
            AbstractDataStore.LOGGER.log(Level.SEVERE, "Exception when updating schema", e);
        }
    }

    /* (non-Javadoc)
     * @see org.geotools.data.AbstractDataStore#getFeatureReader(java.lang.String)
     * Copied from MemoryDataStore#getFeatureReader(java.lang.String)
     */
    protected FeatureReader getFeatureReader(final String typeName)
        throws IOException {
        return new FeatureReader() {
                FeatureType featureType = source.getSchema(typeName);
                Iterator iterator = source.getFeatureSource(typeName).getFeatures().iterator();

                public FeatureType getFeatureType() {
                    return featureType;
                }

                public Feature next()
                    throws IOException, IllegalAttributeException, NoSuchElementException {
                    if (iterator == null) {
                        throw new IOException("Feature Reader has been closed");
                    }

                    try {
                        return featureType.duplicate((Feature) iterator.next());
                    } catch (NoSuchElementException end) {
                        throw new DataSourceException("There are no more Features", end);
                    }
                }

                public boolean hasNext() {
                    return (iterator != null) && iterator.hasNext();
                }

                public void close() {
                    if (iterator != null) {
                        iterator = null;
                    }

                    if (featureType != null) {
                        featureType = null;
                    }
                }
            };
    }

    protected Envelope getBounds(Query query) throws IOException {
        try {
            return source.getView(query).getBounds();
        } catch (SchemaException e) {
            throw (IOException) new IOException().initCause(e);
        }
    }

    protected int getCount(Query query) throws IOException {
        try {
            return source.getView(query).getCount(Query.ALL);
        } catch (SchemaException e) {
            throw (IOException) new IOException().initCause(e);
        }
    }

    protected FeatureWriter createFeatureWriter(final String typeName, final Transaction transaction)
        throws IOException {
        // Not sure of what I am doing
        // If I pass provided transaction to source.getFeatureWriter, two transaction.commit() are needed to complete transaction.
        FeatureWriter writer = source.getFeatureWriter(typeName, Transaction.AUTO_COMMIT);

        return writer;

        /*return new FeatureWriter() {
           FeatureType featureType = getSchema(typeName);
           FeatureCollection contents = source.getFeatureSource(typeName).getFeatures() ;
           Iterator iterator = contents.iterator();
           SimpleFeature live = null;
           Feature current = null; // current Feature returned to user
           public FeatureType getFeatureType() {
               return featureType;
           }
           public Feature next() throws IOException, NoSuchElementException {
               if (hasNext()) {
                   // existing content
                   live = (SimpleFeature) iterator.next();
                   try {
                       current = featureType.duplicate(live);
                   } catch (IllegalAttributeException e) {
                       throw new DataSourceException("Unable to edit " + live.getID() + " of "
                           + typeName);
                   }
               } else {
                   // new content
                   live = null;
                   try {
                       current = DataUtilities.template(featureType);
                   } catch (IllegalAttributeException e) {
                       throw new DataSourceException("Unable to add additional Features of "
                           + typeName);
                   }
               }
               return current;
           }
           public void remove() throws IOException {
               if (contents == null) {
                   throw new IOException("FeatureWriter has been closed");
               }
               if (current == null) {
                   throw new IOException("No feature available to remove");
               }
               if (live != null) {
                   // remove existing content
                   iterator.remove();
                   listenerManager.fireFeaturesRemoved(typeName, transaction,
                       live.getBounds(), true);
                   live = null;
                   current = null;
               } else {
                   // cancel add new content
                   current = null;
               }
           }
           public void write() throws IOException {
               if (contents == null) {
                   throw new IOException("FeatureWriter has been closed");
               }
               if (current == null) {
                   throw new IOException("No feature available to write");
               }
               if (live != null) {
                   if (live.equals(current)) {
                       // no modifications made to current
                       //
                       live = null;
                       current = null;
                   } else {
                       // accept modifications
                       //
                       try {
                           live.setAttributes(current.getAttributes(null));
                       } catch (IllegalAttributeException e) {
                           throw new DataSourceException("Unable to accept modifications to "
                               + live.getID() + " on " + typeName);
                       }
                       Envelope bounds = new Envelope();
                       bounds.expandToInclude(live.getBounds());
                       bounds.expandToInclude(current.getBounds());
                       listenerManager.fireFeaturesChanged(typeName, transaction,
                           bounds, true);
                       live = null;
                       current = null;
                   }
               } else {
                   // add new content
                   //
                   //contents.put(current.getID(), current);
                       contents.add(current) ;
                   listenerManager.fireFeaturesAdded(typeName, transaction,
                       current.getBounds(), true);
                   current = null;
               }
           }
           public boolean hasNext() throws IOException {
               if (contents == null) {
                   throw new IOException("FeatureWriter has been closed");
               }
               return (iterator != null) && iterator.hasNext();
           }
           public void close(){
               if (iterator != null) {
                   iterator = null;
               }
               if (featureType != null) {
                   featureType = null;
               }
               contents = null;
               current = null;
               live = null;
           }
           };*/
    }

    /*protected InProcessLockingManager createLockingManager() {
       return null ;
       }*/

    /*protected FeatureWriter getFeatureWriter(String typeName) throws IOException {
       }*/
}
