/* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geotools.data.arcsde;

import com.esri.sde.sdk.client.*;
import com.vividsolutions.jts.geom.Envelope;
import org.geotools.data.*;
import org.geotools.feature.*;
import org.geotools.filter.*;
import java.io.*;
import java.util.*;
import java.util.logging.Logger;


/**
 * Implements a DataStore to work upon an ArcSDE spatial database gateway.
 * String[] getTypeNames() FeatureType getSchema(String typeName)
 * FeatureReader getFeatureReader( typeName ) FeatureWriter getFeatureWriter(
 * typeName ) Filter getUnsupportedFilter(String typeName, Filter filter)
 * FeatureReader getFeatureReader(String typeName, Query query)
 * 
 * <p>
 * All remaining functionality is implemented against these methods, including
 * Transaction and Locking Support. These implementations will not be optimal
 * but they will work.
 * </p>
 * 
 * <p>
 * Pleae note that there may be a better place for you to start out from, (like
 * JDBCDataStore).
 * </p>
 *
 * @author Gabriel Rold?n
 * @version $Id: ArcSDEDataStore.java,v 1.1 2004/03/11 00:17:09 groldan Exp $
 */
public class ArcSDEDataStore extends AbstractDataStore {
    /** DOCUMENT ME!  */
    private static final Logger LOGGER = Logger.getLogger(ArcSDEDataStore.class.getPackage()
                                                                               .getName());
    private ArcSDEConnectionPool connectionPool;

    /** <code>Map&lt;typeName/FeatureType&gt;</code> of feature type schemas */
    private Map schemasCache = new HashMap();

    /**
     * Creates a new ArcSDEDataStore object.
     *
     * @param connectionPool DOCUMENT ME!
     */
    public ArcSDEDataStore(ArcSDEConnectionPool connectionPool) {
        super(true);
        this.connectionPool = connectionPool;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public ArcSDEConnectionPool getConnectionPool() {
        return this.connectionPool;
    }

    /**
     * DOCUMENT ME!
     *
     * @return the list of full qualified feature class names on the ArcSDE
     *         database this DataStore works on. An ArcSDE full qualified
     *         class name is composed of three dot separated strings:
     *         "DATABASE.USER.CLASSNAME", wich is usefull enough to use it as
     *         namespace
     *
     * @throws RuntimeException if an exception occurs while retrieving the
     *         list of registeres feature classes on the backend, or while
     *         obtaining the full qualified name of one of them
     */
    public String[] getTypeNames() {
        String[] featureTypesNames = null;

        try {
            List sdeLayers = connectionPool.getAvailableSdeLayers();
            featureTypesNames = new String[sdeLayers.size()];

            String typeName;
            int i = 0;

            for (Iterator it = sdeLayers.iterator(); it.hasNext(); i++) {
                typeName = ((SeLayer) it.next()).getQualifiedName();
                featureTypesNames[i] = typeName;
            }
        } catch (SeException ex) {
            throw new RuntimeException("Exception while fetching layer name: "
                + ex.getMessage(), ex);
        } catch (DataSourceException ex) {
            throw new RuntimeException("Exception while getting layers list: "
                + ex.getMessage(), ex);
        }

        return featureTypesNames;
    }

    /**
     * DOCUMENT ME!
     *
     * @param typeName DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws java.io.IOException DOCUMENT ME!
     */
    public FeatureType getSchema(String typeName) throws java.io.IOException {
        FeatureType schema = (FeatureType) schemasCache.get(typeName);

        if (schema == null) {
            schema = ArcSDEAdapter.createSchema(getConnectionPool(), typeName);
            schemasCache.put(typeName, schema);
        }

        return schema;
    }

    /**
     * DOCUMENT ME!
     *
     * @param typeName DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws java.io.IOException DOCUMENT ME!
     */
    protected FeatureReader getFeatureReader(String typeName)
        throws java.io.IOException {
        return getFeatureReader(typeName, Query.ALL);
    }

    /**
     * GR: this method is called from inside getFeatureReader(Query
     * ,Transaction ) to allow subclasses return an optimized FeatureReader
     * wich supports the filter and attributes truncation specified in
     * <code>query</code>
     * 
     * <p>
     * A subclass that supports the creation of such an optimized FeatureReader
     * shold override this method. Otherwise, it just returns
     * <code>getFeatureReader(typeName)</code>
     * </p>
     * 
     * <p></p>
     *
     * @param typeName DOCUMENT ME!
     * @param query DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     * @throws DataSourceException DOCUMENT ME!
     */
    protected FeatureReader getFeatureReader(String typeName, Query query)
        throws IOException {
        ArcSDEQuery sdeQuery = null;
        FeatureReader reader = null;

        try {
            FeatureType schema = getSchema(typeName);
            sdeQuery = ArcSDEAdapter.createSeQuery(this, schema, query);
            sdeQuery.prepareQuery();
            sdeQuery.execute();

            AttributeReader attReader = new ArcSDEAttributeReader(sdeQuery);
            final FeatureType resultingSchema = sdeQuery.getSchema();
            reader = new DefaultFeatureReader(attReader, resultingSchema) {
                        protected Feature readFeature(AttributeReader atts)
                            throws IllegalAttributeException, IOException {
                            for (int i = 0, ii = atts.getAttributeCount();
                                    i < ii; i++) {
                                attributes[i] = atts.read(i);
                            }

                            return resultingSchema.create(attributes,
                                ((ArcSDEAttributeReader) atts).readFID());
                        }
                    };
        } catch (SchemaException ex) {
            throw new DataSourceException("Types do not match: "
                + ex.getMessage(), ex);
        } catch (Throwable t) {
            if (sdeQuery != null) {
                sdeQuery.close();
            }
        }

        return reader;
    }

    /**
     * GR: if a subclass supports filtering, it should override this method to
     * return the unsupported part of the passed filter, so a
     * FilteringFeatureReader will be constructed upon it. Otherwise it will
     * just return the same filter.
     * 
     * <p>
     * If the complete filter is supported, the subclass must return
     * <code>Filter.NONE</code>
     * </p>
     *
     * @param typeName DOCUMENT ME!
     * @param filter DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    protected Filter getUnsupportedFilter(String typeName, Filter filter) {
        try {
            FilterSet filters = ArcSDEAdapter.computeFilters(this, typeName,
                    filter);

            return filters.getUnsupportedFilter();
        } catch (IOException ex) {
            LOGGER.warning(ex.getMessage());
        }

        return filter;
    }

    /**
     * DOCUMENT ME!
     *
     * @param typeName
     *
     * @return FeatureWriter over contents of typeName
     *
     * @throws IOException Subclass may throw IOException
     */
    protected FeatureWriter getFeatureWriter(String typeName)
        throws IOException {
        SeLayer layer = connectionPool.getSdeLayer(typeName);

        return new ArcSDEFeatureWriter(this, layer);
    }

    /**
     * Gets the number of the features that would be returned by this query for
     * the specified feature type.
     * 
     * <p>
     * If getBounds(Query) returns <code>-1</code> due to expense consider
     * using <code>getFeatures(Query).getCount()</code> as a an alternative.
     * </p>
     *
     * @param query Contains the Filter and MaxFeatures to find the bounds for.
     *
     * @return The number of Features provided by the Query or <code>-1</code>
     *         if count is too expensive to calculate or any errors or occur.
     *
     * @throws IOException if there are errors getting the count
     */
    protected int getCount(Query query) throws IOException {
        ArcSDEQuery sdeQuery = null;

        try {
            sdeQuery = ArcSDEAdapter.createSeQuery(this, query);

            return sdeQuery.calculateResultCount();
        } catch (DataSourceException ex) {
            throw ex;
        } finally {
            if (sdeQuery != null) {
                sdeQuery.close();
            }
        }
    }

    /**
     * Computes the bounds of the features for the specified feature type that
     * satisfy the query provided that there is a fast way to get that result.
     * 
     * <p>
     * Will return null if there is not fast way to compute the bounds. Since
     * it's based on some kind of header/cached information, it's not
     * guaranteed to be real bound of the features
     * </p>
     *
     * @param query
     *
     * @return the bounds, or null if too expensive
     *
     * @throws IOException
     */
    protected Envelope getBounds(Query query) throws IOException {
        ArcSDEQuery sdeQuery = null;

        try {
            sdeQuery = ArcSDEAdapter.createSeQuery(this, query);

            return sdeQuery.calculateQueryExtent();
        } catch (DataSourceException ex) {
            throw ex;
        } finally {
            if (sdeQuery != null) {
                sdeQuery.close();
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param argv DOCUMENT ME!
     */
    public static void main(String[] argv) {
        ConnectionConfig cc = new ConnectionConfig("arcsde", "postgis", "5151",
                "sde", "sde", "carto");
        FeatureSource fs = null;

        try {
            ArcSDEConnectionPool pool = ConnectionPoolFactory.getInstance()
                                                             .getPoolFor(cc);
            DataStore ds = new ArcSDEDataStore(pool);
            fs = ds.getFeatureSource("SDE.SDE.MG_PORTALES_K");
            System.out.println("BBOX=" + fs.getBounds());
            System.out.println("COUNT=" + fs.getCount(Query.ALL));
        } catch (IOException ex) {
            ex.printStackTrace();

            return;
        }

        Feature f;
        FeatureReader r = null;
        long t = System.currentTimeMillis();

        try {
            FilterFactory ff = FilterFactory.createFilterFactory();
            CompareFilter filter = ff.createCompareFilter(AbstractFilter.COMPARE_LESS_THAN);
            filter.addLeftValue(ff.createAttributeExpression(fs.getSchema(),
                    "SHAPE"));
            filter.addRightValue(ff.createLiteralExpression(1000));

            Query q = new DefaultQuery(filter);
            r = fs.getFeatures(q).reader();
            t = System.currentTimeMillis() - t;
            System.out.println("reader obtenido en " + t + "ms");

            int count = 0;
            t = System.currentTimeMillis();

            while (r.hasNext()) {
                f = r.next();
                ++count;
            }

            r.close();
            t = System.currentTimeMillis() - t;
            System.out.println(count + " features obtenidas en " + t + "ms");
        } catch (NoSuchElementException ex) {
            ex.printStackTrace();
        } catch (IllegalAttributeException ex) {
            ex.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                r.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
