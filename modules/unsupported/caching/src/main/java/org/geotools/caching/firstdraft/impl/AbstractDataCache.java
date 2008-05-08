/**
 *
 */
package org.geotools.caching.firstdraft.impl;

import java.io.IOException;
import org.opengis.filter.Filter;
import org.geotools.caching.firstdraft.DataCache;
import org.geotools.data.AbstractDataStore;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureWriter;
import org.geotools.data.LockingManager;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.feature.FeatureType;
import org.geotools.feature.SchemaException;


/**
 * @author Christophe Rousson, SoC 2007, CRG-ULAVAL
 *
 */
public abstract class AbstractDataCache extends AbstractDataStore implements DataCache {
    /* (non-Javadoc)
     * @see org.geotools.caching.DataCache#clear()
     */
    public void clear() {
        // TODO Auto-generated method stub
    }

    /* (non-Javadoc)
     * @see org.geotools.caching.DataCache#flush()
     */
    public void flush() throws IllegalStateException {
        // TODO Auto-generated method stub
    }

    /* (non-Javadoc)
     * @see org.geotools.caching.DataCache#getHits()
     */
    public long getHits() {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see org.geotools.data.DataStore#createSchema(org.geotools.feature.FeatureType)
     */
    public void createSchema(FeatureType featureType) throws IOException {
        // TODO Auto-generated method stub
    }

    /* (non-Javadoc)
     * @see org.geotools.data.DataStore#getFeatureReader(org.geotools.data.Query, org.geotools.data.Transaction)
     */
    public FeatureReader getFeatureReader(Query query, Transaction transaction)
        throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.geotools.data.DataStore#getFeatureSource(java.lang.String)
     */
    public FeatureSource getFeatureSource(String typeName)
        throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.geotools.data.DataStore#getFeatureWriter(java.lang.String, org.geotools.data.Transaction)
     */
    public FeatureWriter getFeatureWriter(String typeName, Transaction transaction)
        throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.geotools.data.DataStore#getFeatureWriter(java.lang.String, org.opengis.filter.Filter, org.geotools.data.Transaction)
     */
    public FeatureWriter getFeatureWriter(String typeName, Filter filter, Transaction transaction)
        throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.geotools.data.DataStore#getFeatureWriterAppend(java.lang.String, org.geotools.data.Transaction)
     */
    public FeatureWriter getFeatureWriterAppend(String typeName, Transaction transaction)
        throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.geotools.data.DataStore#getLockingManager()
     */
    public LockingManager getLockingManager() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.geotools.data.DataStore#getSchema(java.lang.String)
     */
    public FeatureType getSchema(String typeName) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.geotools.data.DataStore#getTypeNames()
     */
    public String[] getTypeNames() throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.geotools.data.DataStore#getView(org.geotools.data.Query)
     */
    public FeatureSource getView(Query query) throws IOException, SchemaException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.geotools.data.DataStore#updateSchema(java.lang.String, org.geotools.feature.FeatureType)
     */
    public void updateSchema(String typeName, FeatureType featureType) {
        // TODO Auto-generated method stub
    }

    protected FeatureReader getFeatureReader(String typeName)
        throws IOException {
        // TODO Auto-generated method stub
        return null;
    }
}
