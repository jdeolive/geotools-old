/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.data.sqlserver;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Types;
import java.util.Map;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.jdbc.BasicSQLDialect;
import org.geotools.jdbc.JDBCDataStore;
import org.geotools.referencing.CRS;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

/**
 * Dialect implementation for Microsoft SQL Server.
 * 
 * @author Justin Deoliveira, OpenGEO
 *
 */
public class SQLServerDialect extends BasicSQLDialect {

    public SQLServerDialect(JDBCDataStore dataStore) {
        super(dataStore);
    }

    @Override
    public boolean includeTable(String schemaName, String tableName,
            Connection cx) throws SQLException {
        return !("INFORMATION_SCHEMA".equals( schemaName ) || "sys".equals( schemaName ) );
    }
    
    @Override
    public String getGeometryTypeName(Integer type) {
        return "geometry";
    }
    
    @Override
    public void registerClassToSqlMappings(Map<Class<?>, Integer> mappings) {
        super.registerClassToSqlMappings(mappings);
        
        //override since sql server maps all date times to timestamp
        mappings.put( Date.class, Types.TIMESTAMP );
        mappings.put( Time.class, Types.TIMESTAMP );
    }
    
    @Override
    public void registerSqlTypeNameToClassMappings(
            Map<String, Class<?>> mappings) {
        super.registerSqlTypeNameToClassMappings(mappings);
        
        
        mappings.put( "geometry", Geometry.class );
    }
    
    @Override
    public void registerSqlTypeToSqlTypeNameOverrides(
            Map<Integer, String> overrides) {
        super.registerSqlTypeToSqlTypeNameOverrides(overrides);
        
        //force varchar, if not it will default to nvarchar which won't support length restrictions
        overrides.put( Types.VARCHAR, "varchar");
    }
    
    @Override
    public Integer getGeometrySRID(String schemaName, String tableName,
            String columnName, Connection cx) throws SQLException {
        
        StringBuffer sql = new StringBuffer("SELECT ");
        encodeColumnName(columnName, sql);
        sql.append( ".STSrid");
        
        sql.append( " FROM ");
        encodeTableName(tableName, sql );
        
        sql.append( " WHERE ");
        encodeColumnName(columnName, sql );
        sql.append( " IS NOT NULL");
        
        dataStore.getLogger().fine( sql.toString() );
        
        Statement st = cx.createStatement();
        try {
            
            ResultSet rs = st.executeQuery( sql.toString() );
            try {
                if ( rs.next() ) {
                    return rs.getInt( 1 );
                }
                return -1;
            }
            finally {
                dataStore.closeSafe( rs );
            }
        }
        finally {
            dataStore.closeSafe( st );
        }
    }
    
    @Override
    public void encodeGeometryColumn(GeometryDescriptor gatt, int srid, StringBuffer sql) {
        sql.append( "CAST(");
        encodeColumnName( gatt.getLocalName(), sql );
        sql.append( ".STSrid as VARCHAR)");
        
        sql.append( " + ':' + " );
        
        encodeColumnName( gatt.getLocalName(), sql );
        sql.append( ".STAsText()");
        
        //encodeColumnName(gatt.getLocalName(), sql);
        //sql.append( ".STAsText()");
    }
    
    @Override
    public void encodeGeometryValue(Geometry value, int srid, StringBuffer sql)
            throws IOException {
        
        if ( value == null ) {
            sql.append( "NULL");
            return;
        }
        
        sql.append( "geometry::STGeomFromText('").append( value.toText() ).append( "',").append( srid ).append(")");
    }
    
    @Override
    public Geometry decodeGeometryValue(GeometryDescriptor descriptor,
            ResultSet rs, String column, GeometryFactory factory, Connection cx)
            throws IOException, SQLException {
        String s = rs.getString( column );
        return decodeGeometry ( s, factory );
    }
    
    Geometry decodeGeometry( String s, GeometryFactory factory ) throws IOException {
        if ( s == null ) {
            return null;
        }
        if ( factory == null ) {
            factory = new GeometryFactory();
        }
        
        String[] split = s.split( ":" );
        
        String  srid = split[0];
        
        Geometry g = null;
        try {
            g = new WKTReader(factory).read( split[1] );
        }
        catch ( ParseException e ) {
            throw (IOException) new IOException().initCause( e );
        }
        
        CoordinateReferenceSystem crs;
        try {
            crs = CRS.decode( "EPSG:" + srid );
        } 
        catch (Exception e ) {
            throw (IOException) new IOException().initCause( e );
        }
        
        g.setUserData( crs );
        return g;
    }

    @Override
    public void encodeGeometryEnvelope(String tableName, String geometryColumn,
            StringBuffer sql) {
        sql.append( "CAST(");
        encodeColumnName( geometryColumn, sql );
        sql.append( ".STSrid as VARCHAR)");
        
        sql.append( " + ':' + " );
        
        encodeColumnName( geometryColumn, sql );
        sql.append( ".STEnvelope().ToString()");
    }
    
    @Override
    public Envelope decodeGeometryEnvelope(ResultSet rs, int column,
            Connection cx) throws SQLException, IOException {
        String s = rs.getString( column );
        Geometry g = decodeGeometry( s, null );
        if ( g == null ) {
            return null;
        }
        
        return new ReferencedEnvelope( g.getEnvelopeInternal(), (CoordinateReferenceSystem) g.getUserData() );
    }
    
    @Override
    public Object getNextAutoGeneratedValue(String schemaName,
            String tableName, String columnName, Connection cx)
            throws SQLException {
        
        StringBuffer sql = new StringBuffer("SELECT");
        sql.append( " IDENT_CURRENT('").append( tableName ).append("')");
        sql.append( " + ");
        sql.append( " IDENT_INCR('").append( tableName ).append("')");
        
        dataStore.getLogger().fine( sql.toString() );
        
        Statement st = cx.createStatement();
        try {
            ResultSet rs = st.executeQuery( sql.toString() );
            try {
                rs.next();
                return rs.getInt( 1 );
            }
            finally {
                dataStore.closeSafe( rs );
            }
        }
        finally {
            dataStore.closeSafe( st );
        }
         
    }
}
