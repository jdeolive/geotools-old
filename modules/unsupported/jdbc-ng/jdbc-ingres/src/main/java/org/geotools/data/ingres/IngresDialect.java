package org.geotools.data.ingres;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;

import org.geotools.jdbc.BasicSQLDialect;
import org.geotools.jdbc.JDBCDataStore;
import org.opengis.feature.type.GeometryDescriptor;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

public class IngresDialect extends BasicSQLDialect {

    public IngresDialect(JDBCDataStore dataStore) {
        super(dataStore);
        
    }

    @Override
    public void encodeGeometryValue(Geometry value, int srid, StringBuffer sql) throws IOException {
    }
    
    public Geometry decodeGeometryValue(GeometryDescriptor descriptor, ResultSet rs,
            String column, GeometryFactory factory, Connection cx ) throws IOException, SQLException {
    	return null;
    }
    
    @Override
    public Envelope decodeGeometryEnvelope(ResultSet rs, int column, Connection cx)
            throws SQLException, IOException {
        return null;
    }

    @Override
    public Integer getGeometrySRID(String schemaName, String tableName, String columnName,
        Connection cx) throws SQLException {
    	
    	Integer srid = null;
    	PreparedStatement stmt = null;
    	return srid;
    }
    
    
    @Override
    public void encodePrimaryKey(String column, StringBuffer sql) {
    	super.encodePrimaryKey(column, sql);
    	sql.append(" NOT NULL");
    }

    @Override
    public void encodeGeometryEnvelope(String tableName, String geometryColumn, StringBuffer sql) {
    }

    /**Determines the class mapping for a particular column of a table.*/
    @Override
    public Class<?> getMapping(ResultSet columnMetaData, Connection cx)
            throws SQLException {
    	
    	return null;
    }
    
    
    @Override
    //to register additional mappings if necessary
    public void registerSqlTypeNameToClassMappings(Map<String, Class<?>> mappings) {
        super.registerSqlTypeNameToClassMappings(mappings);

    }
    
    @Override
    public void registerClassToSqlMappings(Map<Class<?>, Integer> mappings) {
        super.registerClassToSqlMappings(mappings);
    }

    @Override
    public void registerSqlTypeToClassMappings(Map<Integer, Class<?>> mappings) {
        super.registerSqlTypeToClassMappings(mappings);
    }
        	
    @Override
    public String getSequenceForColumn(String schemaName, String tableName,
            String columnName, Connection cx) throws SQLException {
    	
        return null;
    }
    
        
    @Override
    public Object getNextSequenceValue(String schemaName, String sequenceName,
            Connection cx) throws SQLException {
    	return null;
    }

    
	@Override
	public boolean includeTable(String schemaName, String tableName, Connection cx) throws SQLException {
		return false;
	}

    @Override   
    public boolean isLimitOffsetSupported() {
    	return false;
    }
    
    @Override
    public void applyLimitOffset(StringBuffer sql, int limit, int offset) {
    }
}
