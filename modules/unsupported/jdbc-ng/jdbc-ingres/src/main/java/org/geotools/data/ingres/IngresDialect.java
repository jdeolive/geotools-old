package org.geotools.data.ingres;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

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

    @Override
    public Envelope decodeGeometryEnvelope(ResultSet rs, int column, Connection cx)
            throws SQLException, IOException {
        return null;
    }

    @Override
    public Geometry decodeGeometryValue(GeometryDescriptor descriptor, ResultSet rs, String column,
            GeometryFactory factory, Connection cx) throws IOException, SQLException {
        return null;
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
    
    
}
