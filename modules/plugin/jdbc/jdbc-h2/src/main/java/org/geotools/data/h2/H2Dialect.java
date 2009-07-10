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
package org.geotools.data.h2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Map;

import org.geotools.jdbc.JDBCDataStore;
import org.geotools.jdbc.SQLDialect;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.PropertyDescriptor;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKBReader;
import com.vividsolutions.jts.io.WKTWriter;

/**
 * Delegate for {@link H2DialectBasic} and {@link H2DialectPrepared} which implements
 * the common parts of the dialect api.
 * 
 * @author Justin Deoliveira, OpenGEO
 *
 */
public class H2Dialect extends SQLDialect {
    
    public H2Dialect( JDBCDataStore dataStore ) {
        super( dataStore );
    }
    
    public String getNameEscape() {
        return "\"";
    }

    public void registerSqlTypeToClassMappings(Map<Integer, Class<?>> mappings) {
        super.registerSqlTypeToClassMappings(mappings);

        //geometries
        //mappings.put(new Integer(Types.OTHER), Geometry.class);
        mappings.put(new Integer(Types.BLOB), Geometry.class);
    }

    public void registerClassToSqlMappings(Map<Class<?>, Integer> mappings) {
        super.registerClassToSqlMappings(mappings);

        //geometries

        //TODO: only map geometry?
        mappings.put(Geometry.class, new Integer(Types.BLOB));
        mappings.put(Point.class, new Integer(Types.BLOB));
        mappings.put(LineString.class, new Integer(Types.BLOB));
        mappings.put(Polygon.class, new Integer(Types.BLOB));
        mappings.put(GeometryCollection.class, new Integer(Types.BLOB));
        mappings.put(MultiPoint.class, new Integer(Types.BLOB));
        mappings.put(MultiLineString.class, new Integer(Types.BLOB));
        mappings.put(MultiPolygon.class, new Integer(Types.BLOB));
    }

    @Override
    public void initializeConnection(Connection cx) throws SQLException {
        //spatialize this database (if neccessary)
        Statement st = cx.createStatement();
        try {
            try {
                st.execute( "SELECT GeoToolsVersion()");
                
                //db already spatialized
                return;
            }
            catch( SQLException e ) {
                //continue on, means database has not been spatialized
            }
            
            BufferedReader r = new BufferedReader( 
                    new InputStreamReader( getClass().getResourceAsStream( "h2.sql" ) ) );
            
            String line = null;
            while( (line = r.readLine() ) != null ) {
                st.execute( line );
            }    
            
            r.close();
        }
        catch( IOException e ) {
            throw new RuntimeException( e );
        }
        finally {
            dataStore.closeSafe( st );
        }
        
    }
    
    @Override
    public Class<?> getMapping(ResultSet columnMetaData, Connection cx)
            throws SQLException {
        
        //do a check for a column remark which marks this as a geometry
        String remark = columnMetaData.getString( "REMARKS" );
        if ( remark != null ) {
            if ( "POINT".equalsIgnoreCase( remark ) ) {
                return Point.class;
            }
            if ( "LINESTRING".equalsIgnoreCase( remark ) ) {
                return LineString.class;
            }
            if ( "POLYGON".equalsIgnoreCase( remark ) ) {
                return Polygon.class;
            }
            if ( "MULTIPOINT".equalsIgnoreCase( remark ) ) {
                return MultiPoint.class;
            }
            if ( "MULTILINESTRING".equalsIgnoreCase( remark ) ) {
                return MultiLineString.class;
            }
            if ( "MULTIPOLYGON".equalsIgnoreCase( remark ) ) {
                return MultiPolygon.class;
            }
        }
        
        return null;
    }
    
    @Override
    public void encodePostColumnCreateTable(AttributeDescriptor att,
            StringBuffer sql) {
        if ( att instanceof GeometryDescriptor ) {
            //try to narrow down the type with a comment
            Class binding = att.getType().getBinding();
            if (isConcreteGeometry(binding)) {
                sql.append( " COMMENT '").append( binding.getSimpleName().toUpperCase() )
                    .append( "'");
            }
        }
    }
    
    @Override
    public void postCreateTable(String schemaName,
            SimpleFeatureType featureType, Connection cx) throws SQLException {
        
        Statement st = cx.createStatement();
        try {
            //post process the feature type and set up constraints based on geometry type
            for ( PropertyDescriptor ad : featureType.getDescriptors() ) {
                if ( ad instanceof GeometryDescriptor ) {
                    Class binding = ad.getType().getBinding();
                    if ( isConcreteGeometry( binding ) ) {
                        String tableName = featureType.getTypeName();
                        String propertyName = ad.getName().getLocalPart();
                        StringBuffer sql = new StringBuffer();
                        sql.append( "ALTER TABLE ");
                        encodeTableName(tableName, sql);
                        sql.append( " ADD CONSTRAINT " );
                        encodeTableName( tableName + "_"+propertyName + "GeometryType", sql );
                        sql.append( " CHECK ");
                        encodeColumnName( propertyName, sql );
                        sql.append( " IS NULL OR");
                        sql.append( " GeometryType(");
                        encodeColumnName( propertyName, sql );
                        sql.append( ") = '").append( binding.getSimpleName().toUpperCase() )
                            .append( "'");
                            
                        LOGGER.fine( sql.toString() );
                        st.execute( sql.toString() );
                    }
                }
            }
        }
        finally {
            dataStore.closeSafe( st );
        }
    }
    
    boolean isConcreteGeometry( Class binding ) {
        return Point.class.isAssignableFrom(binding) 
            || LineString.class.isAssignableFrom(binding)
            || Polygon.class.isAssignableFrom(binding) 
            || MultiPoint.class.isAssignableFrom( binding ) 
            || MultiLineString.class.isAssignableFrom(binding) 
            || MultiPolygon.class.isAssignableFrom( binding );
    }
    
    public Integer getGeometrySRID(String schemaName, String tableName, String columnName,
        Connection cx) throws SQLException {
        //execute SELECT srid(<columnName>) FROM <tableName> LIMIT 1;
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT getSRID(");
        encodeColumnName(columnName, sql);
        sql.append(") ");
        sql.append("FROM ");

        if (schemaName != null) {
            encodeTableName(schemaName, sql);
            sql.append(".");
        }

        encodeSchemaName(tableName, sql);
        sql.append(" WHERE ");
        encodeColumnName(columnName, sql);
        sql.append(" is not null LIMIT 1");

        dataStore.getLogger().fine(sql.toString());
        Statement st = cx.createStatement();

        try {
            ResultSet rs = st.executeQuery(sql.toString());

            try {
                if (rs.next()) {
                    return new Integer(rs.getInt(1));
                } else {
                    //could not find out
                    return null;
                }
            } finally {
                dataStore.closeSafe(rs);
            }
        } finally {
            dataStore.closeSafe(st);
        }
    }

    public void encodeGeometryEnvelope(String tableName, String geometryColumn, StringBuffer sql) {
        //TODO: change spatialdbbox to use envelope
        sql.append("envelope(");
        encodeColumnName(geometryColumn, sql);
        sql.append(")");
    }

    @Override
    public Envelope decodeGeometryEnvelope(ResultSet rs, int column,
            Connection cx) throws SQLException, IOException {
        
        //TODO: change spatialdb in a box to return ReferencedEnvelope
        return (Envelope) rs.getObject(column);
    }

    public void encodeGeometryValue(Geometry value, int srid, StringBuffer sql)
        throws IOException {
        sql.append("GeomFromText ('");
        sql.append(new WKTWriter().write(value));
        sql.append("',");
        sql.append(srid);
        sql.append(")");
    }
    
    

    public Geometry decodeGeometryValue(GeometryDescriptor descriptor, ResultSet rs, String column,
        GeometryFactory factory, Connection cx ) throws IOException, SQLException {
        byte[] bytes = rs.getBytes(column);

        if (bytes == null) {
            return null;
        }

        try {
            return new WKBReader(factory).read(bytes);
        } catch (ParseException e) {
            throw (IOException) new IOException().initCause(e);
        }

        //return JTS.geometryFromBytes( bytes );
    }

    public void encodePrimaryKey(String column, StringBuffer sql) {
        encodeColumnName(column, sql);
        sql.append(" int AUTO_INCREMENT(1) PRIMARY KEY");
    }

    @Override
    public String getSequenceForColumn(String schemaName, String tableName,
            String columnName, Connection cx) throws SQLException {
        
        String sequenceName = tableName + "_" + columnName + "_SEQUENCE"; 
        
        //sequence names have to be upper case to select values from them
        sequenceName = sequenceName.toUpperCase();
        Statement st = cx.createStatement();
        try {
            StringBuffer sql = new StringBuffer();
            sql.append( "SELECT * FROM INFORMATION_SCHEMA.SEQUENCES ");
            sql.append( "WHERE SEQUENCE_NAME = '").append( sequenceName ).append( "'" );
            
            dataStore.getLogger().fine( sql.toString() );
            ResultSet rs = st.executeQuery( sql.toString() );
            try {
                if ( rs.next() ) {
                    return sequenceName;
                }
            } finally {
                dataStore.closeSafe(rs);
            }
        }
        finally {
            dataStore.closeSafe(st);
        }
        
        return null;
    }
    
    @Override
    public Object getNextSequenceValue(String schemaName, String sequenceName,
            Connection cx) throws SQLException {
        
        Statement st = cx.createStatement();
        try {
            String sql = "SELECT NEXTVAL('" + sequenceName + "')";
            dataStore.getLogger().fine( sql );
            ResultSet rs = st.executeQuery( sql );
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
    
    @Override
    public Object getNextAutoGeneratedValue(String schemaName,
            String tableName, String columnName, Connection cx)
            throws SQLException {
        
        Statement st = cx.createStatement();
        try {
            ResultSet rs = st.executeQuery("SELECT b.COLUMN_DEFAULT "
                    + " FROM INFORMATION_SCHEMA.INDEXES A, INFORMATION_SCHEMA.COLUMNS B "
                    + "WHERE a.TABLE_NAME = b.TABLE_NAME " + " AND a.COLUMN_NAME = b.COLUMN_NAME "
                    + " AND a.TABLE_NAME = '" + tableName + "' " + " AND a.COLUMN_NAME = '"
                    + columnName + "' " + " AND a.PRIMARY_KEY = TRUE");

            //figure out which sequence to query
            String sequence = null;

            try {
                //TODO: there has to be a better way to do this
                rs.next();

                String string = rs.getString(1);
                sequence = string.substring(string.indexOf("SYSTEM_SEQUENCE"), string.length() - 1);
            } finally {
                dataStore.closeSafe(rs);
            }

            try {
                if (schemaName != null) {
                    rs = st.executeQuery("SELECT CURRVAL('" + schemaName + "','" + sequence + "')");
                } else {
                    rs = st.executeQuery("SELECT CURRVAL('" + sequence + "')");
                }

                rs.next();

                int value = rs.getInt(1);

                return new Integer(value + 1);
            } finally {
                dataStore.closeSafe(rs);
            }
        } finally {
            dataStore.closeSafe(st);
        }
    }
    
    @Override
    public boolean isLimitOffsetSupported() {
        return true;
    }
    
    @Override
    public void applyLimitOffset(StringBuffer sql, int limit, int offset) {
        if(limit > 0 && limit < Integer.MAX_VALUE) {
            sql.append(" LIMIT " + limit);
            if(offset > 0) {
                sql.append(" OFFSET " + offset);
            }
        } else if(offset > 0) {
            // H2 pretends to have limit specified along with offset
            sql.append(" LIMIT " + Integer.MAX_VALUE);
            sql.append(" OFFSET " + offset);
        }
    }
}