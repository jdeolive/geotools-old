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
package org.geotools.data.jdbc;

import com.vividsolutions.jts.geom.Envelope;
import org.geotools.data.DataSourceException;
import org.geotools.data.DataStore;
import org.geotools.data.DefaultFeatureResults;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureListener;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureResults;
import org.geotools.data.FeatureSource;
import org.geotools.data.MaxFeatureReader;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.feature.FeatureType;
import org.geotools.filter.Filter;
import org.geotools.filter.SQLEncoderException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


/**
 * A JDBCFeatureSource that can opperate as a starting point for your own
 * implementations.
 * 
 * <p>
 * This class is distinct from the AbstractFeatureSource implementations as
 * JDBC provides us with so many opertunities for optimization.
 * </p>
 * Client code must implement:
 * 
 * <ul>
 * <li>
 * getJDBCDataStore()
 * </li>
 * </ul>
 * 
 * It is recomended that clients implement optimizations for:
 * 
 * <ul>
 * <li>
 * getBounds( Query )
 * </li>
 * <li>
 * getCount( Query )
 * </li>
 * </ul>
 * 
 *
 * @author Jody Garnett, Refractions Research
 */
public class JDBCFeatureSource implements FeatureSource {
    private FeatureType featureType;
    private JDBCDataStore dataStore;

    /**
     * JDBCFeatureSource creation.
     * 
     * <p>
     * Constructs a FeatureStore that opperates against the provided
     * jdbcDataStore to serve up the contents of featureType.
     * </p>
     *
     * @param jdbcDataStore DataStore containing contents
     * @param featureType FeatureType being served
     */
    public JDBCFeatureSource(JDBCDataStore jdbcDataStore,
        FeatureType featureType) {
        this.featureType = featureType;
        this.dataStore = jdbcDataStore;
    }

    /**
     * Retrieve DataStore for this FetureSource.
     *
     * @return
     *
     * @see org.geotools.data.FeatureSource#getDataStore()
     */
    public DataStore getDataStore() {
        return getJDBCDataStore();
    }

    /**
     * Allows access to JDBCDataStore(). Description
     * 
     * <p>
     * Subclass must implement
     * </p>
     *
     * @return JDBDataStore managing this FeatureSource
     */
    public JDBCDataStore getJDBCDataStore() {
        return dataStore;
    }

    /**
     * Adds FeatureListener to the JDBCDataStore against this FeatureSource.
     *
     * @param listener
     *
     * @see org.geotools.data.FeatureSource#addFeatureListener(org.geotools.data.FeatureListener)
     */
    public void addFeatureListener(FeatureListener listener) {
        getJDBCDataStore().listenerManager.addFeatureListener(this, listener);
    }

    /**
     * Remove FeatureListener to the JDBCDataStore against this FeatureSource.
     *
     * @param listener
     *
     * @see org.geotools.data.FeatureSource#removeFeatureListener(org.geotools.data.FeatureListener)
     */
    public void removeFeatureListener(FeatureListener listener) {
        getJDBCDataStore().listenerManager.removeFeatureListener(this, listener);
    }

    /**
     * Retrieve the Transaction this FeatureSource is opperating against.
     * 
     * <p>
     * For a plain JDBCFeatureSource that cannot modify this will always be
     * Transaction.AUTO_COMMIT.
     * </p>
     *
     * @return DOCUMENT ME!
     */
    public Transaction getTransaction() {
        return Transaction.AUTO_COMMIT;
    }

    /**
     * Provides an interface to for the Resutls of a Query.
     * 
     * <p>
     * Various queries can be made against the results, the most basic being to
     * retrieve Features.
     * </p>
     *
     * @param request
     *
     * @return
     *
     * @throws IOException
     *
     * @see org.geotools.data.FeatureSource#getFeatures(org.geotools.data.Query)
     */
    public FeatureResults getFeatures(Query request) throws IOException {
        String typeName = featureType.getTypeName();

        if ((request.getTypeName() != null)
                && !typeName.equals(request.getTypeName())) {
            throw new IOException("Cannot query " + typeName + " with:"
                + request);
        }

        if (request.getTypeName() == null) {
            request = new DefaultQuery(featureType.getTypeName(),
                    request.getFilter(), request.getMaxFeatures(),
                    request.getPropertyNames(), request.getHandle());
        }

        final Query query = request;

        return new DefaultFeatureResults(this, query) {
                /**
                 * JDBCDataStore has a more direct query method
                 *
                 * @return DOCUMENT ME!
                 *
                 * @throws IOException DOCUMENT ME!
                 */
                public FeatureReader reader() throws IOException {
                    int maxFeatures = query.getMaxFeatures();
                    FeatureReader reader = getJDBCDataStore().getFeatureReader(query,
                            getTransaction());

                    //TODO: implement Query.UNLIMITED_FEATURES or something
                    //to that effect, instead of constraining ourselves to 
                    //integer max as we do now.  
                    if (maxFeatures == Integer.MAX_VALUE) {
                        return reader;
                    } else {
                        return new MaxFeatureReader(reader, maxFeatures);
                    }
                }

                /**
                 * Performs optimizated count if possible.
                 *
                 * @return
                 *
                 * @throws IOException
                 *
                 * @see org.geotools.data.DefaultFeatureResults#getCount()
                 */
                public int getCount() throws IOException {
                    int count = count(query, getTransaction());

                    if (count != -1) {
                        int maxFeatures = query.getMaxFeatures();

                        return (count < maxFeatures) ? count : maxFeatures;

                        // optimization worked, return maxFeatures if count is
                        // greater.
                    }

                    return super.getCount();
                }
            };
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
    public FeatureResults getFeatures(Filter filter) throws IOException {
        return getFeatures(new DefaultQuery(featureType.getTypeName(), filter));
    }

    /**
     * Retrieve all Features
     *
     * @return DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public FeatureResults getFeatures() throws IOException {
        return getFeatures(Filter.NONE);
    }

    /**
     * Retrieve Bounds of all Features.
     * 
     * <p>
     * Currently returns null, consider getFeatures().getBounds() instead.
     * </p>
     * 
     * <p>
     * Subclasses may override this method to perform the appropriate
     * optimization for this result.
     * </p>
     *
     * @return null representing the lack of an optimization
     *
     * @throws IOException DOCUMENT ME!
     */
    public Envelope getBounds() throws IOException {
        return getBounds(Query.ALL);
    }

    /**
     * Retrieve Bounds of Query results.
     * 
     * <p>
     * Currently returns null, consider getFeatures( query ).getBounds()
     * instead.
     * </p>
     * 
     * <p>
     * Subclasses may override this method to perform the appropriate
     * optimization for this result.
     * </p>
     *
     * @param query Query we are requesting the bounds of
     *
     * @return null representing the lack of an optimization
     *
     * @throws IOException DOCUMENT ME!
     */
    public Envelope getBounds(Query query) throws IOException {
        if (query.getFilter() == Filter.ALL) {
            return new Envelope();
        }

        return null; // to expensive right now :-)
    }

    /**
     * Retrieve total number of Query results.
     * 
     * <p>
     * Currently returns -1, consider getFeatures( query ).getCount() instead.
     * </p>
     * 
     * <p>
     * Subclasses may override this method to perform the appropriate
     * optimization for this result.
     * </p>
     *
     * @param query Query we are requesting the count of
     *
     * @return -1 representing the lack of an optimization
     */
    public int getCount(Query query) {
        if (getTransaction() != Transaction.AUTO_COMMIT) {
            // it is too dangerous to issue this optimization
            // against the transaction's connection
            // (If we error out, we would be forced to rollback
            //  the transaction and this method does not let
            //  us report the problem to the user)            
            return -1; // too dangerous
        }

        try {
            return count(query, Transaction.AUTO_COMMIT);
        } catch (IOException e) {
            // could not acomplish optimization
            return -1;
        }
    }

    /**
     * Direct SQL query number of rows in query.
     * 
     * <p>
     * Note this is a low level SQL statment and if it fails the provided
     * Transaction will be rolled back.
     * </p>
     * 
     * <p>
     * Will return -1 if optimization can not be used
     * </p>
     *
     * @param query
     * @param transaction
     *
     * @return Number of rows in query, or -1 if not optimizable.
     *
     * @throws IOException DOCUMENT ME!
     * @throws DataSourceException DOCUMENT ME!
     */
    public int count(Query query, Transaction transaction)
        throws IOException {
        Filter filter = query.getFilter();

        if (filter == Filter.ALL) {
            return 0;
        }

        JDBCDataStore jdbc = getJDBCDataStore();
        SQLBuilder sqlBuilder = jdbc.getSqlBuilder(featureType.getTypeName());

        if (sqlBuilder.getPostQueryFilter(query.getFilter()) != null) {
            // this would require postprocessing the filter
            // so we cannot optimize
            return -1;
        }

        Connection conn = null;

        try {
            conn = jdbc.getConnection(transaction);

            String typeName = getSchema().getTypeName();
            Filter preFilter = sqlBuilder.getPreQueryFilter(query.getFilter());
            StringBuffer sql = new StringBuffer();
            StringBuffer sqlBuffer = new StringBuffer();
            sql.append("SELECT COUNT(*) as cnt");
            sqlBuilder.sqlFrom(sql, typeName);
            sqlBuilder.sqlWhere(sql, filter);
            System.out.println("SQL: " + sql);

            Statement statement = conn.createStatement();
            ResultSet results = statement.executeQuery(sql.toString());
            results.next();

            int count = results.getInt("cnt");
            results.close();
            statement.close();

            return count;
        } catch (SQLException sqlException) {
            JDBCUtils.close(conn, transaction, sqlException);
            conn = null;
            throw new DataSourceException("Could not count "
                + query.getHandle(), sqlException);
        } catch (SQLEncoderException e) {
            // could not encode count
            // but at least we did not break the connection
            return -1;
        } finally {
            JDBCUtils.close(conn, transaction, null);
        }
    }

    /**
     * Retrieve FeatureType represented by this FeatureSource
     *
     * @return FeatureType for FeatureSource
     *
     * @see org.geotools.data.FeatureSource#getSchema()
     */
    public FeatureType getSchema() {
        return featureType;
    }

    protected Connection getConnection() throws IOException {
        return getJDBCDataStore().getConnection(getTransaction());
    }

    protected void close(Connection conn, Transaction trans, SQLException sqle) {
        JDBCUtils.close(conn, trans, sqle);
    }

    protected void close(ResultSet rs) {
        JDBCUtils.close(rs);
    }

    protected void close(Statement statement) {
        JDBCUtils.close(statement);
    }
}
