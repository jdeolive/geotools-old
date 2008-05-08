/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, GeoTools Project Managment Committee (PMC)
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
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import javax.swing.Icon;

import org.geotools.data.AbstractFeatureSource;
import org.geotools.data.DataSourceException;
import org.geotools.data.DataStore;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureListener;
import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.data.QueryCapabilities;
import org.geotools.data.ResourceInfo;
import org.geotools.data.Transaction;
import org.geotools.factory.Hints;
import org.geotools.feature.FeatureCollection;
import org.geotools.filter.SQLEncoderException;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.opengis.filter.expression.PropertyName;
import org.opengis.filter.sort.SortBy;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;


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
 * @source $URL$
 */
public class JDBCFeatureSource implements FeatureSource<SimpleFeatureType, SimpleFeature> {
    /** The logger for the filter module. */
    private static final Logger LOGGER = org.geotools.util.logging.Logging.getLogger("org.geotools.data.jdbc");
    
    /** FeatureType being provided */
    private SimpleFeatureType featureType;
    
    /** JDBCDataStore based dataStore used to aquire content */
    private JDBC1DataStore dataStore;

    protected QueryCapabilities queryCapabilities;
    
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
    public JDBCFeatureSource(JDBC1DataStore jdbcDataStore,
        SimpleFeatureType featureType) {
        this.featureType = featureType;
        this.dataStore = jdbcDataStore;
        //We assume jdbc datastores can sort by any field. Be sure to override
        //if you support @id, NATURAL_ORDER, or REVERSE_ORDER
        this.queryCapabilities = new QueryCapabilities(){
            @Override
            public boolean supportsSorting(SortBy[] sortAttributes){
                final SimpleFeatureType featureType = JDBCFeatureSource.this.featureType;
                for(int i = 0; i < sortAttributes.length; i++){
                    SortBy sortBy = sortAttributes[i];
                    PropertyName propertyName = sortBy.getPropertyName();
                    String attName = (String)propertyName.evaluate(featureType, String.class);
                    if(attName == null){
                        attName = propertyName.getPropertyName();
                    }
                    if(featureType.getAttribute(attName) == null){
                       return false; 
                    }
                }
                return true;
            }  
        };
    }
    
    /**
     * Returns the same name than the feature type (ie,
     * {@code getSchema().getName()} to honor the simple feature land common
     * practice of calling the same both the Features produces and their types
     * 
     * @since 2.5
     * @see FeatureSource#getName()
     */
    public Name getName() {
        return getSchema().getName();
    }
    
    public ResourceInfo getInfo() {
        return new ResourceInfo(){
            final Set<String> words = new HashSet<String>();
            {
                words.add("features");
                words.add( JDBCFeatureSource.this.getSchema().getTypeName() );
            }
            public ReferencedEnvelope getBounds() {
                try {
                    return JDBCFeatureSource.this.getBounds();
                } catch (IOException e) {
                    return null;
                }
            }
            public CoordinateReferenceSystem getCRS() {
                return JDBCFeatureSource.this.getSchema().getCRS();
            }
    
            public String getDescription() {
                return null;
            }
    
            public Set<String> getKeywords() {
                return words;
            }
    
            public String getName() {
                return JDBCFeatureSource.this.getSchema().getTypeName();
            }
    
            public URI getSchema() {
                Name name = JDBCFeatureSource.this.getSchema().getName();
                URI namespace;
                try {
                    namespace = new URI( name.getNamespaceURI() );
                    return namespace;                    
                } catch (URISyntaxException e) {
                    return null;
                }                
            }
    
            public String getTitle() {
                Name name = JDBCFeatureSource.this.getSchema().getName();
                return name.getLocalPart();
            }
            
        };
    }
    /**
     * Retrieve DataStore for this FetureSource.
     *
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
    public JDBC1DataStore getJDBCDataStore() {
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
     * Retrieve the Transaction this FeatureSource<SimpleFeatureType, SimpleFeature> is opperating against.
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
     *
     * @throws IOException
     *
     * @see org.geotools.data.FeatureSource#getFeatures(org.geotools.data.Query)
     */
    public FeatureCollection<SimpleFeatureType, SimpleFeature> getFeatures(Query request) throws IOException {
        String typeName = featureType.getTypeName();

        if ((request.getTypeName() != null) && !typeName.equals(request.getTypeName())) {
            throw new IOException("Cannot query " + typeName + " with:" + request);
        }
        
        if (request.getTypeName() == null) {
            request = new DefaultQuery(request);
            ((DefaultQuery) request).setTypeName(featureType.getTypeName());
        }
        
        final QueryCapabilities queryCapabilities = getQueryCapabilities();
        if(request.getSortBy() != null){
           if(!queryCapabilities.supportsSorting(request.getSortBy())){
               throw new DataSourceException("DataStore cannot provide the requested sort order");
           }
        }
        
        return new JDBCFeatureCollection(this, request);
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
    public FeatureCollection<SimpleFeatureType, SimpleFeature> getFeatures(Filter filter) throws IOException {
        return getFeatures(new DefaultQuery(featureType.getTypeName(), filter));
    }

    /**
     * Retrieve all Features
     *
     * @return DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public FeatureCollection<SimpleFeatureType, SimpleFeature> getFeatures() throws IOException {
        return getFeatures(Filter.INCLUDE);
    }

    /**
     * Retrieve Bounds of all Features.
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
    public ReferencedEnvelope getBounds() throws IOException {
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
    public ReferencedEnvelope getBounds(Query query) throws IOException {
        if (query.getFilter() == Filter.EXCLUDE) {
            if(featureType!=null)
                return new ReferencedEnvelope(new Envelope(),featureType.getDefaultGeometry().getCRS());
            return new ReferencedEnvelope();
        }               
        return null; // too expensive right now :-)
    }
    /**
     * Retrieve total number of Query results.
     * <p>
     * SQL: SELECT COUNT(*) as cnt FROM table WHERE filter
     * </p>
     * 
     * @param query Query we are requesting the count of
     * @return Count of indicated query
     */
    public int getCount(Query query) throws IOException {
        return count(query, getTransaction() );        
    }

    /**
     * Direct SQL query number of rows in query.
     * 
     * <p>
     * Note this is a low level SQL statement and if it fails the provided
     * Transaction will be rolled back.
     * </p>
     * <p>
     * SQL: SELECT COUNT(*) as cnt FROM table WHERE filter
     * </p>
     * @param query
     * @param transaction
     *
     * @return Number of rows in query, or -1 if not optimizable.
     *
     * @throws IOException Usual on the basis of a filter error
     */
    public int count(Query query, Transaction transaction)
        throws IOException {
        Filter filter = query.getFilter();

        if (Filter.EXCLUDE.equals(filter)) {
            return 0;
        }

        JDBC1DataStore jdbc = getJDBCDataStore();
        SQLBuilder sqlBuilder = jdbc.getSqlBuilder(featureType.getTypeName());

        Filter postFilter = (Filter) sqlBuilder.getPostQueryFilter(filter); 
        if (postFilter != null && !Filter.INCLUDE.equals(postFilter)) {
            // this would require postprocessing the filter
            // so we cannot optimize
            return -1;
        }

        Connection conn = null;

        try {
            conn = jdbc.getConnection(transaction);

            String typeName = getSchema().getTypeName();
            StringBuffer sql = new StringBuffer();
            //chorner: we should hit an indexed column, * will likely tablescan
            sql.append("SELECT COUNT(*) as cnt");
            sqlBuilder.sqlFrom(sql, typeName);
            sqlBuilder.sqlWhere(sql, filter); //safe to assume filter = prefilter
            
            LOGGER.finer("SQL: " + sql);

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
    public SimpleFeatureType getSchema() {
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
    
    /**
     * By default, only detached feature is supported
     */
    
     public Set getSupportedHints() {
            return dataStore.getSupportedHints();
     }

     /**
      * Returns the default jdbc query capabilities, subclasses should 
      * override to advertise their specific capabilities where they differ.
      * <p>
      * For instance, the capabilities returned:
      * <ul>
      * <li>Does not support startIndex
      * <li>Supports sorting by any attribute in the feature type. Be sure to override
      * if that's not the case, or you also support sorting by @id, SortBy.NATURAL_ORDER,
      * or SortBy.REVERSE_ORDER.
      * </ul>
      * </p>
      * 
      * @see FeatureSource#getQueryCapabilities()
      */
    public QueryCapabilities getQueryCapabilities() {
        return queryCapabilities;
    }
}
