/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2003, Geotools Project Managment Committee (PMC)
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
package org.geotools.data.jdbc;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.geotools.data.DataSourceException;
import org.geotools.data.DataStore;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureListener;
import org.geotools.data.FeatureReader;
import org.geotools.data.MaxFeatureReader;
import org.geotools.data.FeatureResults;
import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.data.DefaultFeatureResults;
import org.geotools.feature.FeatureType;
import org.geotools.filter.Filter;
import org.geotools.filter.SQLEncoderException;

import com.vividsolutions.jts.geom.Envelope;

/**
 * A JDBCFeatureSource that can opperate as a starting point for your own
 * implementations.
 * <p>
 * This class is distinct from the AbstractFeatureSource implementations as
 * JDBC provides us with so many opertunities for optimization.
 * </p>
 * Client code must implement:
 * <ul>
 * <li>
 * getJDBCDataStore()
 * </li>
 * </ul>
 * It is recomended that clients implement optimizations for:
 * <ul>
 * <li>
 * getBounds( Query )
 * </li>
 * <li>
 * getCount( Query )
 * </li>
 * </ul>
 * @author Jody Garnett, Refractions Research
 */
public class JDBCFeatureSource implements FeatureSource {

    private FeatureType featureType;
    private JDBCDataStore dataStore;
    /**
     * JDBCFeatureSource creation.
     * <p>
     * Constructs a FeatureStore that opperates against the provided
     * jdbcDataStore to serve up the contents of featureType.
     * </p>
     * @param jdbcDataStore DataStore containing contents
     * @param featureType FeatureType being served
     */
    public JDBCFeatureSource(JDBCDataStore jdbcDataStore, FeatureType featureType){
        this.featureType = featureType;
        this.dataStore = jdbcDataStore;
    }
    /**
     * Retrieve DataStore for this FetureSource.
     * 
     * @see org.geotools.data.FeatureSource#getDataStore()
     * @return
     */
    public DataStore getDataStore() {
        return getJDBCDataStore();
    }
    /**
     * Allows access to JDBCDataStore().
     * Description
     * <p>
     * Subclass must implement
     * </p>
     * @return JDBDataStore managing this FeatureSource
     */
    public JDBCDataStore getJDBCDataStore(){
        return dataStore; 
    }

    /**
     * Adds FeatureListener to the JDBCDataStore against this FeatureSource.
     * 
     * @see org.geotools.data.FeatureSource#addFeatureListener(org.geotools.data.FeatureListener)
     * @param listener
     */
    public void addFeatureListener(FeatureListener listener) {
        getJDBCDataStore().listenerManager.addFeatureListener( this, listener );
    }

    /**
     * Remove FeatureListener to the JDBCDataStore against this FeatureSource.
     * 
     * @see org.geotools.data.FeatureSource#removeFeatureListener(org.geotools.data.FeatureListener)
     * @param listener
     */
    public void removeFeatureListener(FeatureListener listener) {
        getJDBCDataStore().listenerManager.removeFeatureListener( this, listener );
    }

    /**
     * Retrieve the Transaction this FeatureSource is opperating against.
     * <p>
     * For a plain JDBCFeatureSource that cannot modify this will always be
     * Transaction.AUTO_COMMIT.
     * </p>
     */
    public Transaction getTransaction() {
        return Transaction.AUTO_COMMIT;
    }
    /**
     * Provides an interface to for the Resutls of a Query.
     * <p>
     * Various queries can be made against the results, the most basic being
     * to retrieve Features.
     * </p>
     * @see org.geotools.data.FeatureSource#getFeatures(org.geotools.data.Query)
     * @param query
     * @return
     * @throws IOException
     */    
    public FeatureResults getFeatures(final Query query)
        throws IOException {
        return new DefaultFeatureResults(this, query) {
            /** JDBCDataStore has a more direct query method */
            public FeatureReader reader() throws IOException {
		int maxFeatures = query.getMaxFeatures();
		FeatureReader reader = getJDBCDataStore().getFeatureReader( query, getTransaction() );                  
		if (maxFeatures == query.DEFAULT_MAX) {
		    return reader;  
		} else {
		    return new MaxFeatureReader(reader, maxFeatures);
		}
            }
            /**
             * Performs optimizated count if possible.
             * 
             * @see org.geotools.data.DefaultFeatureResults#getCount()
             * @return
             * @throws IOException
             */
            public int getCount() throws IOException {
                int count = count( query, getTransaction() );
                if( count != -1 ){
		    int maxFeatures = query.getMaxFeatures();
                    return count < maxFeatures ? count : maxFeatures; 
		    // optimization worked, return maxFeatures if count is
		    // greater.
                }                
                return super.getCount();
            }

        };
    }
    /** Retrieve all Feature matching the Filter */
    public FeatureResults getFeatures(Filter filter) throws IOException {
        return getFeatures(new DefaultQuery(featureType.getTypeName(), filter));
    }
    /** Retrieve all Features */
    public FeatureResults getFeatures() throws IOException {
        return getFeatures(Filter.NONE);
    }
    /** 
     * Retrieve Bounds of all Features.
     * <p>
     * Currently returns null, consider getFeatures().getBounds() instead.
     * </p>
     * <p>
     * Subclasses may override this method to perform the appropriate
     * optimization for this result.
     * </p>
     * 
     * @return null representing the lack of an optimization 
     */    
    public Envelope getBounds() throws IOException {
        return getBounds(Query.ALL);
    }
    /** 
     * Retrieve Bounds of Query results.
     * <p>
     * Currently returns null, consider getFeatures( query ).getBounds() instead.
     * </p>
     * <p>
     * Subclasses may override this method to perform the appropriate
     * optimization for this result.
     * </p>     
     * @param query Query we are requesting the bounds of
     * @return null representing the lack of an optimization
     */
    public Envelope getBounds(Query query) {
        if( query.getFilter() == Filter.ALL ){
            return new Envelope();
        }        
        return null; // to expensive right now :-)
    }
    /** 
     * Retrieve total number of Query results.
     * <p>
     * Currently returns -1, consider getFeatures( query ).getCount() instead.
     * </p>
     * <p>
     * Subclasses may override this method to perform the appropriate
     * optimization for this result.
     * </p>
     * @param query Query we are requesting the count of
     * @return -1 representing the lack of an optimization
     */
    public int getCount(Query query) {        
        if( getTransaction() != Transaction.AUTO_COMMIT){
            // it is too dangerous to issue this optimization
            // against the transaction's connection
            // (If we error out, we would be forced to rollback
            //  the transaction and this method does not let
            //  us report the problem to the user)            
            return -1; // too dangerous
        }
        try {
            return count( query, Transaction.AUTO_COMMIT );
        } catch (IOException e) {
            // could not acomplish optimization
            return -1;
        }                
    }
    /**
     * Direct SQL query number of rows in query.
     * <p>
     * Note this is a low level SQL statment and if it fails
     * the provided Transaction will be rolled back.
     * </p>
     * <p>
     * Will return -1 if optimization can not be used
     * </p>
     * @param query
     * @param transaction
     * @return Number of rows in query, or -1 if not optimizable.
     */
    public int count( Query query, Transaction transaction ) throws IOException{
        Filter filter = query.getFilter();
        if( filter == Filter.ALL ){
            return 0;
        }              
        JDBCDataStore jdbc = getJDBCDataStore();
        SQLBuilder sqlBuilder = jdbc.getSqlBuilder(featureType.getTypeName());
        if( sqlBuilder.getPostQueryFilter( query.getFilter() ) !=  null ){
            // this would require postprocessing the filter
            // so we cannot optimize
            return -1;            
        }        
        Connection conn = null;            
        try{            
            conn = jdbc.getConnection( transaction );
            
            String typeName = getSchema().getTypeName();
            Filter preFilter = sqlBuilder.getPreQueryFilter( query.getFilter() );
            StringBuffer sql = new StringBuffer();
            StringBuffer sqlBuffer = new StringBuffer();
            sql.append("SELECT COUNT(*) as cnt");            
            sqlBuilder.sqlFrom( sql, typeName );            
            sqlBuilder.sqlWhere( sql, filter );
            System.out.println("SQL: " + sql);
            
            Statement statement = conn.createStatement();
            ResultSet results = statement.executeQuery(sql.toString());
            results.next();
            int count = results.getInt("cnt");
            results.close();
            statement.close();                
            return count;                                            
        } catch (SQLException sqlException) {
            JDBCDataStore.close( conn, transaction, sqlException );
            conn = null;
            throw new DataSourceException( "Could not count "+query.getHandle(),  sqlException);            
        } catch (SQLEncoderException e) {
            // could not encode count
            // but at least we did not break the connection
            return -1;            
        } finally {
            JDBCDataStore.close( conn, transaction, null );
        }                  
    }
    /**
     * Retrieve FeatureType represented by this FeatureSource
     *  
     * @see org.geotools.data.FeatureSource#getSchema()
     * @return FeatureType for FeatureSource
     */
    public FeatureType getSchema() {
        return featureType;
    }        
}
