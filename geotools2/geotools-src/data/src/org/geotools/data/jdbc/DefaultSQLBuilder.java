/*
 * Created on 28/10/2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.geotools.data.jdbc;

import org.geotools.feature.AttributeType;
import org.geotools.filter.Filter;
import org.geotools.filter.SQLEncoder;
import org.geotools.filter.SQLUnpacker;
import org.geotools.filter.SQLEncoderException;
import org.geotools.filter.FilterCapabilities;

/** Provides ...
 * 
 *  @author Sean Geoghegan, Defence Science and Technology Organisation.
 */
public class DefaultSQLBuilder implements SQLBuilder {

    protected SQLEncoder encoder;
    
    /**
     * 
     */
    public DefaultSQLBuilder() {
        this(new SQLEncoder());
    }

    /**
     * @param encoder
     */
    public DefaultSQLBuilder(SQLEncoder encoder) {
        this.encoder = encoder;
    }

    /* (non-Javadoc)
     * @see org.geotools.data.jdbc.SQLStatementBuilder#buildSQLQuery(java.lang.String, org.geotools.feature.AttributeType[], org.geotools.filter.Filter)
     */
    public String buildSQLQuery(String typeName, String fidColumnName, AttributeType[] attrTypes, Filter filter) 
                throws SQLEncoderException {        
        StringBuffer sqlBuffer = new StringBuffer();
                
        sqlBuffer.append("SELECT ");            
        sqlColumns( sqlBuffer, fidColumnName, attrTypes );
        sqlFrom( sqlBuffer, typeName );            
        sqlWhere( sqlBuffer, filter );
                
        String sqlStmt = sqlBuffer.toString();
        return sqlStmt;        
    }

    public Filter getPostQueryFilter(Filter filter){
        FilterCapabilities cap = encoder.getCapabilities();
        SQLUnpacker unpacker = new SQLUnpacker(cap);
    	//figure out which of the filter we can use.
    	unpacker.unPackAND(filter);
    	return unpacker.getUnSupported();
    }

    public Filter getPreQueryFilter(Filter filter){
        SQLUnpacker unpacker = new SQLUnpacker(encoder.getCapabilities());
    	//figure out which of the filter we can use.
    	unpacker.unPackAND(filter);
    	return unpacker.getSupported();
    }


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
    public void sqlColumns( StringBuffer sql, String fidColumnName, AttributeType attributes[] ) {
              
        if (fidColumnName != null) {
            sql.append(fidColumnName);
            sql.append(", ");
        }        
        for (int i = 0; i < attributes.length; i++) {
            sql.append(attributes[i].getName());
            if (i < attributes.length - 1) {
                sql.append(", ");
            }
        }        
    }
    
    /**
     * Consutrcts FROM clause for featureType
     * <p>
     * sql: <code>FROM typeName</code>
     * </p>
     * @param sql
     * @param featureType
     */
    public void sqlFrom( StringBuffer sql, String typeName){
        sql.append( " FROM ");
        sql.append( typeName );
    }
    
    /**
     * Constructs WHERE clause, if needed, for FILTER.
     * <p>
     * sql: <code>WHERE filter encoding</code>
     * </p>
     */
    public void sqlWhere( StringBuffer sql, Filter preFilter ) throws SQLEncoderException{
        if (preFilter != null && preFilter != Filter.NONE) {
            String where = encoder.encode( preFilter);
            sql.append(" ");
            sql.append(where);
        }        
    }
}
