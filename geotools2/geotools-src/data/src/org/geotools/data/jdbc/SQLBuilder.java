/*
 * Created on 28/10/2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.geotools.data.jdbc;

import org.geotools.feature.AttributeType;
import org.geotools.filter.Filter;
import org.geotools.filter.SQLEncoderException;

/** Provides an interface for SQL statement construction.
 * 
 *  <p>Currently just doing query building, but obviously this can be extended.
 * 
 *  @author Sean Geoghegan, Defence Science and Technology Organisation.
 */
public interface SQLBuilder {

    /**
     * Makes an SQL Select statement.  Constructs an SQL statement that will select the
     * features from the table based on the filter.  The default implementation creates
     * a select statement for the table with the name <tt>typeName</tt>, selecting all
     * the columns with the names in the <tt>attrTypes</tt> array using the filter
     * as a WHERE clause.  The default implementation ignores the maxFeature parameter
     * since this requires DB dependant SQL.  Subclasses can override this to provide 
     * the maxFeatures functionality specific to their DB.
     *
     * @param attrTypes The Attribute types for the select statement
     * @param filter The filter to convert to a where statement.
     * @param maxFeatures The max amount of features to return.
     * @param useMax True if we are to use the maxFeature as the max.
     *
     * @return An SQL statement.
     *
     * @throws SQLEncoderException If an error occurs encoding the SQL
     */
    public String buildSQLQuery(String typeName, String fidColumnName, 
            AttributeType[] attrTypes, Filter filter) throws SQLEncoderException;
    
    /**
     * Returns the Filter required for post processing.
     * <p>
     * The result will be null if no post processing is required.
     * </p>
     * <p>
     * This method is used by DefaultJDBCFeatureSource to see if
     * the a Query can be optimized
     * </p>
     * @param filter
     * @return Filter requried for post processing, or <code>null</code>
     */
    public Filter getPostQueryFilter(Filter filter);
    
    public Filter getPreQueryFilter(Filter filter);
    
    /**
     * Produces the select information required.
     * <p>
     * The featureType, if known, is always requested.
     * </p>
     * <p>
     * sql: <code>featureID (,attributeColumn)*</code>
     * </p>
     * <p>
     * We may need to provide AttributeReaders with a hook
     * so they can request a wrapper function.
     * </p>
     * @param sql
     * @param typeName
     * @param attributes
     */
    public void sqlColumns( StringBuffer sql, String fidColumnName, AttributeType attributes[] );
    
    /**
     * Consutrcts FROM clause for featureType
     * <p>
     * sql: <code>FROM typeName</code>
     * </p>
     * @param sql
     * @param featureType
     */
    public void sqlFrom( StringBuffer sql, String typeName);
    
    /**
     * Constructs WHERE clause, if needed, for FILTER.
     * <p>
     * sql: <code>WHERE filter encoding</code>
     * </p>
     */
    public void sqlWhere( StringBuffer sql, Filter preFilter ) throws SQLEncoderException;
}
