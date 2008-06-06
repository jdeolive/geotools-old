/*
 *    GeoTools - The Open Source Java GIS Tookit
 *    http://geotools.org
 * 
 *    (C) 2003-2008, Open Source Geospatial Foundation (OSGeo)
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

import org.geotools.data.Query;
import org.geotools.data.jdbc.fidmapper.FIDMapper;
import org.geotools.factory.Hints;
import org.geotools.filter.FilterCapabilities;
import org.geotools.filter.SQLEncoder;
import org.geotools.filter.SQLEncoderException;
import org.geotools.filter.visitor.ClientTransactionAccessor;
import org.geotools.filter.visitor.PostPreProcessFilterSplittingVisitor;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.filter.Filter;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.sort.SortBy;
import org.opengis.filter.sort.SortOrder;


/**
 * Builds a complete SQL query to select the specified attributes for the
 * specified feature type, using a specified filter to generate a WHERE
 * clause.
 * 
 * <p>
 * The actual WHERE clause is generated by the SQLEncoder class or appropriate
 * subclass for a particular database.  If a specific encoder is to be used,
 * it must be specified to the constructor for this class.
 * </p>
 * 
 * <p>
 * In order to implement the functionality of the application-specified Filter,
 * this is split into a 'preQueryFilter' which can be incorporated into the
 * SQL query itself and a 'postQueryFilter.  The encoder capabilities are used
 * to determine how much of the function can be performed by the database
 * directly and how much has to be performed on the result set.
 * </p>
 *
 * @author Sean Geoghegan, Defence Science and Technology Organisation.
 * @source $URL$
 * @deprecated Use GeoAPISQLBuilder instead
 */
public class DefaultSQLBuilder implements SQLBuilder {
    // The instance of the encoder to be used to generate the WHERE clause
    protected SQLEncoder encoder;

    protected SimpleFeatureType ft;
    
    protected ClientTransactionAccessor accessor;
    
    private Filter lastFilter = null;
    private Filter lastPreFilter = null;
    private Filter lastPostFilter = null;
    
    private Hints hints;
    
	/**
	 * Constructs an instance of this class with a default SQLEncoder
	 */
	 public DefaultSQLBuilder() {
	     this(new SQLEncoder());
	 }

   /**
     * Constructs an instance of this class using the encoder class specified.
     * This will typically be from the getSqlBuilder method of a JDBCDataStore
     * subclass.
     * <p>
     * This constructor should not be used to obtain Pre/Post filters, as these
     * methods require a SimpleFeatureType to function properly.
     *
     * @deprecated
     * @param encoder the specific encoder to be used.
     */
    public DefaultSQLBuilder(SQLEncoder encoder) {
    	this(encoder, null, null);
    }

    /**
     * Constructs an instance of this class using the encoder class specified.
     * This will typically be from the getSqlBuilder method of a JDBCDataStore
     * subclass.
     * 
     * @param encoder the specific encoder to be used.
     * @param featureType
     * @param accessor client-side transaction handler; may be null.
     */
    public DefaultSQLBuilder(SQLEncoder encoder, SimpleFeatureType featureType, ClientTransactionAccessor accessor) {
    	this.encoder = encoder;
    	this.ft = featureType;
    	this.accessor = accessor;
    	
    	//set the feature type on teh encoders
    	encoder.setFeatureType( featureType );
    }
    
    public void setHints(Hints hints) {
        this.hints = hints;
    }
    
    /** Check the hints to see if we are forced into 2D */
    public synchronized boolean isForce2D(){
        if( hints == null ){
            return false;
        }
        Boolean force2d = (Boolean) hints.get( Hints.FEATURE_2D );
        if(force2d == null ){
            return false;
        }
        return force2d.booleanValue();      
    }
    
    /**
     * Return the postQueryFilter that must be applied to the database query
     * result set.
     *
     * @param filter the application filter which must be applied
     *
     * @return the filter representing the functionality that must be performed
     *         on the result set.
     */
    public Filter getPostQueryFilter(Filter filter) {
    	if (filter != null && ( lastFilter == null || filter != lastFilter) ) {
    		splitFilter(filter);
    	}
    	return lastPostFilter;
    	
//        SQLUnpacker unpacker = new SQLUnpacker(cap);
//
//        //figure out which of the filter we can use.
//        unpacker.unPackAND(filter);
//
//        return unpacker.getUnSupported();
    }

    /**
     * Return the preQueryFilter that can be used to generate the WHERE clause.
     *
     * @param filter the application filter which must be applied
     *
     * @return the filter representing the functionality that can be performed
     *         by the database.
     */
    public Filter getPreQueryFilter(Filter filter) {
    	if (filter != null && ( lastFilter == null || !filter.equals(lastFilter) ) ) {
    		splitFilter(filter);
    	}
    	return lastPreFilter;
//        SQLUnpacker unpacker = new SQLUnpacker(encoder.getCapabilities());
//
//        //figure out which of the filter we can use.
//        unpacker.unPackAND(filter);
//
//        return unpacker.getSupported();
    }

    protected void splitFilter(Filter filter) {
    	lastFilter = filter;
        FilterCapabilities cap = encoder.getCapabilities();
        PostPreProcessFilterSplittingVisitor pfv = new PostPreProcessFilterSplittingVisitor(cap, ft, accessor);
        
        filter.accept(pfv, null);

        lastPreFilter = (Filter) pfv.getFilterPre();
        lastPostFilter = (Filter) pfv.getFilterPost();
    }
    
    /**
     * Constructs the FROM clause for a featureType
     * 
     * <p>
     * sql: <code>FROM typeName</code>
     * </p>
     *
     * @param sql the StringBuffer that the WHERE clause should be appended to
     * @param typeName the name of the table (feature type) to be queried
     */
    public void sqlFrom(StringBuffer sql, String typeName) {
        sql.append(" FROM ");
        sql.append(encoder.escapeName(typeName));
    }

    /**
     * Constructs WHERE clause, if needed, for FILTER.
     * 
     * <p>
     * sql: <code>WHERE filter encoding</code>
     * </p>
     *
     * @param sql The StringBuffer that the WHERE clause should be appended to
     * @param preFilter The filter to be used by the encoder class to generate
     *        the WHERE clause
     *
     * @throws SQLEncoderException Not thrown here but may be thrown by the
     *         encoder
     */
    public void sqlWhere(StringBuffer sql, Filter preFilter)
        throws SQLEncoderException {
        if ((preFilter != null) && (preFilter != Filter.INCLUDE)) {
            String where = encoder.encode(preFilter);
            sql.append(" ");
            sql.append(where);
        }
    }

    /**
     * @deprecated
     */
    public String buildSQLQuery(String typeName, FIDMapper mapper,
            AttributeDescriptor[] attrTypes, org.opengis.filter.Filter filter) throws SQLEncoderException {
        return buildSQLQuery(typeName, mapper, attrTypes, filter, (SortBy[])null, null, null);
    }
    
    /**
     * Constructs the full SQL SELECT statement for the supplied Filter.
     * 
     * <p>
     * The statement is constructed by concatenating the SELECT column list,
     * FROM table specification and WHERE clause appropriate to the supplied
     * Filte.
     * </p>
     * 
     * <p>
     * Subclasses that support {@link Query#getStartIndex() startIndex} should override as
     * appropriate.
     * </p>
     *
     * @param typeName The name of the table (feature type) to be queried
     * @param mapper FIDMapper to identify the FID columns in the table
     * @param attrTypes The specific attribute columns to be selected
     * @param filter The Filter that will be used by the encoder to construct
     *        the WHERE clause
     *
     * @return The fully formed SQL SELECT statement
     *
     * @throws SQLEncoderException Not thrown by this method but may be thrown
     *         by the encoder class
     */
    public String buildSQLQuery(String typeName,
            FIDMapper mapper,
            AttributeDescriptor[] attrTypes,
            Filter filter,
            SortBy[] sortBy,
            Integer offset,
            Integer limit) throws SQLEncoderException {

        StringBuffer sqlBuffer = new StringBuffer();

        sqlBuffer.append("SELECT ");
        sqlColumns(sqlBuffer, mapper, attrTypes);
        sqlFrom(sqlBuffer, typeName);
        encoder.setFIDMapper(mapper);
        sqlWhere(sqlBuffer, filter);

        //order by clause
        if ( sortBy != null ) {
            //encode the sortBy clause
            sqlOrderBy( sqlBuffer, mapper, sortBy);
        }
        
        String sqlStmt = sqlBuffer.toString();

        return sqlStmt;
    }
    
    /**
     * Appends the names of the columns to be selected.
     * 
     * <p>
     * sqlGeometryColumn is invoked for any special handling for geometry
     * columns.
     * </p>
     *
     * @param sql StringBuffer to be appended to
     * @param mapper FIDMapper to provide the name(s) of the FID columns
     * @param attributes Array of columns to be selected
     */
    public void sqlColumns(StringBuffer sql, FIDMapper mapper,
        AttributeDescriptor[] attributes) {
        for (int i = 0; i < mapper.getColumnCount(); i++) {
            sql.append(encoder.escapeName(mapper.getColumnName(i)) + ", ");
        }

        for (int i = 0; i < attributes.length; i++) {
            if (attributes[i] instanceof GeometryDescriptor) {
                sqlGeometryColumn(sql, attributes[i]);
            } else {
                sql.append(encoder.escapeName(attributes[i].getLocalName()));
            }

            if (i < (attributes.length - 1)) {
                sql.append(", ");
            }
        }
    }

    /**
     * Generates the select column specification for a geometry column.
     * 
     * <p>
     * This should typically be overridden in the subclass to return a
     * meaningful value that the attribute i/o handler can process.
     * </p>
     *
     * @param sql A StringBuffer that the column specification can be appended
     *        to
     * @param geomAttribute An AttributeDescriptor for a geometry attribute
     */
    public void sqlGeometryColumn(StringBuffer sql, AttributeDescriptor geomAttribute) {
        sql.append(encoder.escapeName(geomAttribute.getLocalName()));
    }
    

    /**
     * @deprecated
     */
    public void sqlOrderBy(StringBuffer sql, SortBy[] sortBy) throws SQLEncoderException {
        sqlOrderBy(sql, null, sortBy);
    }

    /**
     * Generates the order by clause.
     * <p>
     * This uses the standard ASC,DESC sql keywords to denote ascending,descending
     * sort respectivley.
     * </p>
     */
    public void sqlOrderBy(StringBuffer sql, FIDMapper mapper, SortBy[] sortBy)
            throws SQLEncoderException {
        if (sortBy == null || sortBy.length == 0)
            return; // nothing to sort on

        sql.append(" ORDER BY ");
        for (int i = 0; i < sortBy.length; i++) {
            final SortBy sortAttribute = sortBy[i];
            if (SortBy.NATURAL_ORDER.equals(sortAttribute)
                    || SortBy.REVERSE_ORDER.equals(sortAttribute)) {
                addOrderByPK(sql, mapper, sortAttribute.getSortOrder());
            } else {
                AttributeDescriptor type = (AttributeDescriptor) sortAttribute.getPropertyName()
                        .evaluate(ft);
                if (type != null) {
                    sql.append(encoder.escapeName(type.getLocalName()));
                } else {
                    sql.append(encoder
                            .escapeName(sortAttribute.getPropertyName().getPropertyName()));
                }

                if (SortOrder.DESCENDING.equals(sortAttribute.getSortOrder())) {
                    sql.append(" DESC");
                } else {
                    sql.append(" ASC");
                }
            }

            if (i < sortBy.length - 1) {
                sql.append(", ");
            }
        }
    }

    public void encode(StringBuffer sql, Expression expression) throws SQLEncoderException {
        sql.append(encoder.encode(expression));
    }

    public void encode(StringBuffer sql, Filter filter) throws SQLEncoderException {
        // we reuse the encoder method already available and get rid of the first where
        // statement encountered
        sql.append(encoder.encode(filter).replaceAll("^\\s*WHERE\\s*", ""));
    }
    

    /**
     * @param sql the buffer where the select statement is being built, already contains the "ORDER
     *            BY " clause, only needs to be appended with the PK fields.
     * @param mapper the fid mapper where to get the PK fields from, may be null, in which case
     *            implementations should either throw an exception or not, depending on whether they
     *            actually need it.
     * @param sortOrder the order in which to encode the PK fields (eg,
     *            <code>"pkAtt1 DESC, pkAtt2 DESC"</code>)
     * @throws SQLEncoderException by default, subclasses shall override if pk ordering is supported
     */
    protected void addOrderByPK(StringBuffer sql, FIDMapper mapper, SortOrder sortOrder)
            throws SQLEncoderException {
        throw new SQLEncoderException("NATURAL_ORDER or REVERSE_ORDER "
                + "ordering is not supported for this FeatureType");
    }
}
