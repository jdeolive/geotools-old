/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
package org.geotools.data.sde;

import com.esri.sde.sdk.client.*;
import com.vividsolutions.jts.geom.Envelope;
import org.geotools.data.*;
import org.geotools.feature.*;
import org.geotools.filter.*;
import java.io.IOException;
import java.util.logging.Logger;


/**
 * DOCUMENT ME!
 *
 * @author Gabriel Roldán
 * @version 0.1
 */
public class SdeFeatureSource implements FeatureSource
{
    /** DOCUMENT ME! */
    private static final Logger LOGGER = Logger.getLogger(
            "org.geotools.data.sde");

    /** DOCUMENT ME! */
    private SdeAdapter adapter = new SdeAdapter();

    /** DOCUMENT ME! */
    private SdeDataStore dataStore;

    /** DOCUMENT ME! */
    private String typeName;

    /** DOCUMENT ME! */
    private FeatureType schema;

    /** DOCUMENT ME! */
    private SeLayer seLayer = null;

    /**
     * Creates a new SdeFeatureSource object.
     *
     * @param store DOCUMENT ME!
     * @param typeName DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public SdeFeatureSource(SdeDataStore store, String typeName)
        throws IOException
    {
        this(store, typeName, null);
    }

    /**
     * Creates a new SdeFeatureSource object.
     *
     * @param store DOCUMENT ME!
     * @param typeName DOCUMENT ME!
     * @param schema may be used to constraint the FeatureType queried to a
     *        subset
     *
     * @throws IOException if <code>typeName</code> can't be located on the
     *         backend SDE database
     * @throws NullPointerException DOCUMENT ME!
     * @throws IllegalArgumentException if a default FeatureType is passed and
     *         it's type name differs from <code>typeName</code>
     */
    protected SdeFeatureSource(SdeDataStore store, String typeName,
        FeatureType schema) throws IOException
    {
        if (store == null)
            throw new NullPointerException("no parent datastore supplied");

        if (typeName == null)
            throw new NullPointerException("no featuretype name is provided");

        if ((schema != null) && !schema.getTypeName().equals(typeName))
            throw new IllegalArgumentException(
                "typname and schema.getTypeName() must be equal");

        this.dataStore = store;
        this.typeName = typeName;

        this.schema = schema;
        this.seLayer = dataStore.getConnectionPool().getSdeLayer(typeName);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public SeLayer getLayer()
    {
        return this.seLayer;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public DataStore getDataStore()
    {
        return dataStore;
    }

    /**
     * DOCUMENT ME!
     *
     * @param listener DOCUMENT ME!
     *
     * @throws java.lang.UnsupportedOperationException DOCUMENT ME!
     */
    public void addFeatureListener(FeatureListener listener)
    {
        /**
         * @todo Implement this org.geotools.data.FeatureSource method
         */
        throw new java.lang.UnsupportedOperationException(
            "Method addFeatureListener() not yet implemented.");
    }

    /**
     * DOCUMENT ME!
     *
     * @param listener DOCUMENT ME!
     *
     * @throws java.lang.UnsupportedOperationException DOCUMENT ME!
     */
    public void removeFeatureListener(FeatureListener listener)
    {
        /**
         * @todo Implement this org.geotools.data.FeatureSource method
         */
        throw new java.lang.UnsupportedOperationException(
            "Method removeFeatureListener() not yet implemented.");
    }

    /**
     * DOCUMENT ME!
     *
     * @param query DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public FeatureResults getFeatures(Query query) throws IOException
    {
        FeatureResults results = new SdeFeatureResults(this, query);
        return results;
    }

    /**
     * Retrieve all Feature matching the Filter
     *
     * @param filter DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public FeatureResults getFeatures(Filter filter) throws IOException
    {
        return getFeatures(new DefaultQuery(filter));
    }

    /**
     * Retrieve all Features
     *
     * @return DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public FeatureResults getFeatures() throws IOException
    {
        return getFeatures(Filter.NONE);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws RuntimeException DOCUMENT ME!
     */
    public FeatureType getSchema()
    {
        if (this.schema == null)
        {
            try
            {
                this.schema = dataStore.getSchema(typeName);
            }
            catch (IOException ex)
            {
                throw new RuntimeException("can't get schema for " + typeName
                    + ": " + ex.getMessage(), ex);
            }
        }

        return this.schema;
    }

    /**
     * queries the underlying ArcSDE server about the extent of the layer
     * referenced by this FeatureSource and returns it
     *
     * @return DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public Envelope getBounds() throws IOException
    {
        Envelope bounds = null;
        SeExtent ext = seLayer.getExtent();

        bounds = new Envelope(ext.getMinX(), ext.getMaxX(), ext.getMinY(),
                ext.getMaxY());

        return bounds;
    }

    /**
     * DOCUMENT ME!
     *
     * @param query DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Envelope getBounds(Query query)
    {
        Envelope bounds = null;

        try
        {
            if (query == Query.ALL)
                bounds = getBounds();
            else
            {
                SeQuery sdeQuery = createSeQuery(query);
                bounds = getBounds(sdeQuery);
            }
        }
        catch (IOException ex)
        {
            LOGGER.warning(ex.getMessage());
        }

        return bounds;
    }

    /**
     * DOCUMENT ME!
     *
     * @param query DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws DataSourceException DOCUMENT ME!
     */
    public SeQuery createSeQuery(Query query) throws DataSourceException
    {
        return adapter.createSeQuery(this, query);
    }

    /**
     * DOCUMENT ME!
     *
     * @param query DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Envelope getBounds(SeQuery query)
    {
        try
        {
            SeQueryInfo sdeQueryInfo = new SeQueryInfo();
            sdeQueryInfo.setConstruct(adapter.getSdeSqlConstruct());

            SeExtent extent = query.calculateLayerExtent(sdeQueryInfo);
            Envelope env = new Envelope(extent.getMinX(), extent.getMinY(),
                    extent.getMaxX(), extent.getMaxY());

            return env;
        }
        catch (Exception ex)
        {
            LOGGER.info("error calculating query envelope: " + ex.getMessage());
        }

        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param query DOCUMENT ME!
     *
     * @return the minimun value between the number of rows that
     *         <code>query</code> should return and
     *         <code>query.getMaxfeatures()</code>, or <code>-1</code> if
     *         something goes wrong while calculating it.
     */
    public int getCount(Query query)
    {
        int size = -1;

        String []qcols = {getLayer().getSpatialColumn()};

        try {
          SeQuery sdeQuery = adapter.createSeQuery(this, qcols, query);
          sdeQuery.prepareQuery();
          sdeQuery.execute();
          int count = 0;
          while (sdeQuery.fetch() != null) {
            ++count;
          }

          size = count;
        }
        catch (Exception ex)
        {
            LOGGER.info("Error calculating result count: " + ex.getMessage());
            ex.printStackTrace();
        }


        /**@task REVISIT: find out why the "count(*)" approach thoes not
         * works if query contains spatial filters
        try
        {
            String[] qcols = { "count(*)" };
            //String[] qcols = new String[0];
            SeQuery sdeQuery = adapter.createSeQuery(this, qcols, query);
            sdeQuery.prepareQuery();
            sdeQuery.execute();

            SeRow sdeRow = sdeQuery.fetch();
            size = sdeRow.getInteger(0).intValue();
            size = Math.min(size, query.getMaxFeatures());
        }
        catch (Exception ex)
        {
            LOGGER.info("Error calculating result count: " + ex.getMessage());
            ex.printStackTrace();
        }
        */
        return size;
    }

    /**
     * DOCUMENT ME!
     *
     * @param query DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws DataSourceException DOCUMENT ME!
     */
    public FeatureType getSchema(Query query) throws DataSourceException
    {
        return adapter.getSchema(query, getSchema());
    }
}
