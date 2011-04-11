package org.geotools.data.teradata;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Map;

import org.geotools.jdbc.ColumnMetadata;
import org.geotools.jdbc.JDBCDataStore;
import org.geotools.jdbc.PreparedFilterToSQL;
import org.geotools.jdbc.PreparedStatementSQLDialect;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.GeometryDescriptor;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.io.WKBWriter;

public class TeradataPSDialect extends PreparedStatementSQLDialect {
	private TeradataGISDialect delegate;

	public TeradataPSDialect(JDBCDataStore store, TeradataGISDialect delegate) {
		super(store);
		this.delegate = delegate;
	}

    @Override
    public void prepareGeometryValue(Geometry g, int srid, Class binding,
            StringBuffer sql) {
        if (g != null) {
            sql.append("ST_GeomFromText.ST_GEOMFROMWKB(?, " + srid + ")");
        } else {
            sql.append("?");
        }
    }
    
    @Override
	public void setGeometryValue(Geometry g, int srid, Class binding,
			PreparedStatement ps, int column) throws SQLException {
        if (g != null) {
            if (g instanceof LinearRing ) {
                //teradata does not handle linear rings, convert to just a line string
                g = g.getFactory().createLineString(((LinearRing) g).getCoordinateSequence());
            }
            
            byte[] bytes = new WKBWriter().write(g);
            ps.setBytes(column, bytes);
        } else {
            ps.setNull(column, Types.OTHER, "Geometry");
        }
	}

	@Override
	public Geometry decodeGeometryValue(GeometryDescriptor descriptor,
			ResultSet rs, String column, GeometryFactory factory, Connection cx)
			throws IOException, SQLException {
        return delegate.decodeGeometryValue(descriptor, rs, column, factory, cx);
	}

    @Override
    public Geometry decodeGeometryValue(GeometryDescriptor descriptor, ResultSet rs, int column,
            GeometryFactory factory, Connection cx) throws IOException, SQLException {
        return delegate.decodeGeometryValue(descriptor, rs, column, factory, cx);
    }

    public void encodeGeometryColumn(GeometryDescriptor gatt, int srid,
            StringBuffer sql) {
        delegate.encodeGeometryColumn(gatt, srid, sql);
    }

    @Override
	public Envelope decodeGeometryEnvelope(ResultSet rs, int column,
			Connection cx) throws SQLException, IOException {
        return delegate.decodeGeometryEnvelope(rs, column, cx);
	}

	@Override
	public void encodeGeometryEnvelope(String tableName, String geometryColumn,
			StringBuffer sql) {
        delegate.encodeGeometryEnvelope(tableName, geometryColumn, sql);		
	}
	
    @Override
    public boolean includeTable(String schemaName, String tableName, Connection cx)
            throws SQLException {
        return delegate.includeTable(schemaName, tableName, cx);
    }

    public void encodePrimaryKey(String column, StringBuffer sql) {
        delegate.encodePrimaryKey(column, sql);
    }

    public Integer getGeometrySRID(String schemaName, String tableName,
            String columnName, Connection cx) throws SQLException {
        return delegate.getGeometrySRID(schemaName, tableName, columnName, cx);
    }

    public String getGeometryTypeName(Integer type) {
        return delegate.getGeometryTypeName(type);
    }

    public Class<?> getMapping(ResultSet columnMetaData, Connection cx)
            throws SQLException {
        return delegate.getMapping(columnMetaData, cx);
    }

    @Override
    public boolean lookupGeneratedValuesPostInsert() {
        return delegate.lookupGeneratedValuesPostInsert();
    }

    public Object getNextAutoGeneratedValue(String schemaName,
            String tableName, String columnName, Connection cx)
            throws SQLException {
        return delegate.getNextAutoGeneratedValue(schemaName, tableName,
                columnName, cx);
    }

    @Override
    public Object getLastAutoGeneratedValue(String schemaName, String tableName, String columnName,
            Connection cx) throws SQLException {
        return delegate.getLastAutoGeneratedValue(schemaName, tableName, columnName, cx);
    }
    
    public Object getNextSequenceValue(String schemaName, String sequenceName,
            Connection cx) throws SQLException {
        return delegate.getNextSequenceValue(schemaName, sequenceName, cx);
    }


    public String getSequenceForColumn(String schemaName, String tableName,
            String columnName, Connection cx) throws SQLException {
        return delegate.getSequenceForColumn(schemaName, tableName, columnName,
                cx);
    }

    public void postCreateTable(String schemaName,
            SimpleFeatureType featureType, Connection cx) throws SQLException {
        delegate.postCreateTable(schemaName, featureType, cx);
    }

    public void registerClassToSqlMappings(Map<Class<?>, Integer> mappings) {
        delegate.registerClassToSqlMappings(mappings);
    }

    public void registerSqlTypeNameToClassMappings(
            Map<String, Class<?>> mappings) {
        delegate.registerSqlTypeNameToClassMappings(mappings);
    }
    
    @Override
    public void registerSqlTypeToSqlTypeNameOverrides(
            Map<Integer, String> overrides) {
        delegate.registerSqlTypeToSqlTypeNameOverrides(overrides);
    }

    @Override
    public void handleUserDefinedType(ResultSet columnMetaData, ColumnMetadata metadata,
            Connection cx) throws SQLException {
        delegate.handleUserDefinedType(columnMetaData, metadata, cx);
    }

    @Override
    public boolean isLimitOffsetSupported() {
        return delegate.isLimitOffsetSupported();
    }
    
    @Override
    public void applyLimitOffset(StringBuffer sql, int limit, int offset) {
        delegate.applyLimitOffset(sql, limit, offset);
    }

    public void setLooseBBOXEnabled(boolean looseBBOXEnabled) {
        delegate.setLooseBBOXEnabled(looseBBOXEnabled);
    }

    @Override
    public void initializeConnection(Connection cx) throws SQLException {
        delegate.initializeConnection(cx);
    }
}
