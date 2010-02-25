package org.geotools.data.ingres;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.geotools.factory.Hints;
import org.geotools.factory.Hints.Key;
import org.geotools.jdbc.JDBCDataStore;
import org.geotools.jdbc.SQLDialect;
import org.geotools.referencing.CRS;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.referencing.ReferenceIdentifier;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;


public class IngresSQLDialect extends SQLDialect {
	  
    public IngresSQLDialect(JDBCDataStore dataStore) {
        super(dataStore);
    }
    

    @Override
    public CoordinateReferenceSystem createCRS(int srid, Connection cx) throws SQLException {
       return null;    
    }
    

    
    @Override
    public void encodePrimaryKey(String column, StringBuffer sql) {
    	super.encodePrimaryKey(column, sql);
    	sql.append(" NOT NULL");
    }
    
    @Override
    public String getGeometryTypeName(Integer type) {
    	return "";
    	        
    }

    @Override
    public Integer getGeometrySRID(String schemaName, String tableName, String columnName,
        Connection cx) throws SQLException {
    	
    	Integer srid = null;
    	PreparedStatement stmt = null;
    	return srid;
    }


    @Override
    public void encodeGeometryEnvelope(String tableName,String geometryColumn, StringBuffer sql) {

    }

    @Override
    public Envelope decodeGeometryEnvelope(ResultSet rs, int column,
                Connection cx) throws SQLException, IOException {
    	return null;
    }

    
    @Override
    public Geometry decodeGeometryValue(GeometryDescriptor descriptor, ResultSet rs, String name,
        GeometryFactory factory, Connection cx ) throws IOException, SQLException {    	
        byte[] bytes = rs.getBytes(name);
        return decodeGeometryValueFromBytes(factory, bytes);
    }

    @Override
    public Geometry decodeGeometryValue(GeometryDescriptor descriptor, ResultSet rs, int column,
            GeometryFactory factory, Connection cx) throws IOException, SQLException {
        byte[] bytes = rs.getBytes(column);
        return decodeGeometryValueFromBytes(factory, bytes);        
    }

    private Geometry decodeGeometryValueFromBytes( GeometryFactory factory,byte[] bytes)  throws IOException{
       return null;
        
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
    public void registerSqlTypeNameToClassMappings(Map<String, Class<?>> mappings) {
        super.registerSqlTypeNameToClassMappings(mappings);
    }


    @Override
    public void postCreateTable(String schemaName, SimpleFeatureType featureType, Connection cx)
    throws SQLException {
    	
    	    	
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

    @Override
    protected void addSupportedHints(Set<Key> hints) {
     
    }
}
