package org.geotools.data.teradata;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.geotools.jdbc.JDBCDataStore;
import org.geotools.jdbc.NullPrimaryKey;
import org.geotools.jdbc.PreparedFilterToSQL;
import org.geotools.jdbc.PreparedStatementSQLDialect;
import org.geotools.jdbc.PrimaryKey;
import org.geotools.jdbc.PrimaryKeyColumn;
import org.geotools.referencing.CRS;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.GeometryDescriptor;

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
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.io.WKTWriter;

public class TeradataDialect extends PreparedStatementSQLDialect {

    /**
     * key for spatial index table
     */
    final static String SPATIAL_INDEX = "org.geotools.data.teradata.spatialIndex";
    
    final static Map<String, Class<?>> TYPE_TO_CLASS = new HashMap<String, Class<?>>() {
        {
            put("GEOMETRY", Geometry.class);
            put("POINT", Point.class);
            put("LINESTRING", LineString.class);
            put("POLYGON", Polygon.class);

            put("MULTIPOINT", MultiPoint.class);
            put("MULTILINESTRING", MultiLineString.class);
            put("MULTIPOLYGON", MultiPolygon.class);
            put("GEOMETRYCOLLECTION", GeometryCollection.class);
            put("GEOSEQUENCE", Geometry.class);
        }
    };

    final static Map<Class<?>, String> CLASS_TO_TYPE = new HashMap<Class<?>, String>() {
        {
            put(Geometry.class, "GEOMETRY");
            put(Point.class, "POINT");
            put(LineString.class, "LINESTRING");
            put(Polygon.class, "POLYGON");
            put(MultiPoint.class, "MULTIPOINT");
            put(MultiLineString.class, "MULTILINESTRING");
            put(MultiPolygon.class, "MULTIPOLYGON");
            put(GeometryCollection.class, "GEOMETRYCOLLECTION");
        }
    };

    /** tessellation table */
    String tTableName;
    
    /** loose bbox flag */
    boolean looseBBOXEnabled = false;

    public TeradataDialect(JDBCDataStore store) {
        super(store);
    }
    
    public void setLooseBBOXEnabled(boolean looseBBOXEnabled) {
        this.looseBBOXEnabled = looseBBOXEnabled;
    }

    public boolean isLooseBBOXEnabled() {
        return looseBBOXEnabled;
    }

    public void setTessellationTableName(String tTableName) {
        this.tTableName = tTableName;
    }
    
    public String getTessellationTableName() {
        return tTableName;
    }
    
    @Override
    public boolean includeTable(String schemaName, String tableName, Connection cx)
            throws SQLException {
        
        if (tableName.equalsIgnoreCase("geometry_columns")) {
            return false;
        } else if (tableName.toLowerCase().startsWith("spatial_ref_sys")) {
            return false;
        } else if (tableName.equalsIgnoreCase("geography_columns")) {
            return false;
        } else if (tableName.endsWith("_idx")) {
            return false;
        }

        // others?
        return dataStore.getDatabaseSchema() == null
                || dataStore.getDatabaseSchema().equals(schemaName);
    }

    @Override
    public void setGeometryValue(Geometry g, int srid, Class binding,
            PreparedStatement ps, int column) throws SQLException {
        if (g != null) {
            if (g instanceof LinearRing ) {
                //teradata does not handle linear rings, convert to just a line string
                g = g.getFactory().createLineString(((LinearRing) g).getCoordinateSequence());
            }
            
            //TODO: use WKB instead of WKT
            ByteArrayInputStream bin = 
                new ByteArrayInputStream(new WKTWriter().write(g).getBytes());
            ps.setAsciiStream(column, bin, bin.available());
        }
        else {
            ps.setNull(column, Types.OTHER, "Geometry");
        }
    }

    @Override
    public Geometry decodeGeometryValue(GeometryDescriptor descriptor,
            ResultSet rs, String column, GeometryFactory factory, Connection cx)
            throws IOException, SQLException {


        Clob clob = rs.getClob(column);
        InputStream wkt = clob.getAsciiStream();
        try {
            return new WKTReader(factory).read(new InputStreamReader(wkt));
        } 
        catch (ParseException e) {
            throw (IOException) new IOException().initCause(e);
        }
        finally {
            wkt.close();
        }
        /*WKBAttributeIO reader = getWkbReader(factory);

        Geometry g = (Geometry) reader.read(rs, column);
        return g;*/
    }
    
    WKBAttributeIO getWkbReader(GeometryFactory factory) {
        factory = factory != null ? factory : dataStore.getGeometryFactory();
        return new WKBAttributeIO(factory);
    }

    @Override
    public Envelope decodeGeometryEnvelope(ResultSet rs, int column,
            Connection cx) throws SQLException, IOException {
        Geometry envelope = getWkbReader(null).read(rs, column);
        if (envelope != null) {
            return envelope.getEnvelopeInternal();
        } else {
            // empty one
            return new Envelope();
        }
    }

    @Override
    public void encodeColumnType(String sqlTypeName, StringBuffer sql) {
        if ("DECIMAL".equals(sqlTypeName)) {
            sql.append(sqlTypeName).append("(10,2)");
        }
        else {
            super.encodeColumnType(sqlTypeName, sql);
        }
    }
    
    @Override
    public void encodeGeometryEnvelope(String tableName, String geometryColumn,
            StringBuffer sql) {
        encodeColumnName(geometryColumn, sql);
        sql.append(".ST_Envelope().ST_AsBinary()");
    }
    
    
    public void encodePrimaryKey(String column, StringBuffer sql) {
       encodeColumnName(column, sql);
       sql.append(" PRIMARY KEY not null generated always as identity (start with 0) integer");
    }

    public Integer getGeometrySRID(String schemaName, String tableName,
            String columnName, Connection cx) throws SQLException {
        // first attempt, try with the geometry metadata
        Statement statement = null;
        ResultSet result = null;
        Integer srid = null;
        try {
            if (schemaName == null)
                schemaName = "public";

            String sqlStatement = "SELECT ref.AUTH_SRID FROM SYSSPATIAL.GEOMETRY_COLUMNS as col, SYSSPATIAL.spatial_ref_sys as ref WHERE "
                    + "col.F_TABLE_SCHEMA = '"
                    + schemaName
                    + "' "
                    + "AND col.F_TABLE_NAME = '"
                    + tableName
                    + "' "
                    + "AND col.F_GEOMETRY_COLUMN = '"
                    + columnName
                    + "' "
                    + "AND col.SRID = ref.SRID";

            LOGGER.log(Level.FINE, "Geometry srid check; {0} ", sqlStatement);
            statement = cx.createStatement();
            result = statement.executeQuery(sqlStatement);

            if (result.next()) {
                srid = result.getInt(1);
            } else {
                dataStore.closeSafe(result);
                sqlStatement = "SELECT ref.AUTH_SRID FROM SYSSPATIAL.GEOMETRY_COLUMNS as col, SYSSPATIAL.spatial_ref_sys as ref WHERE "
                        + "col.F_TABLE_NAME = '"
                        + tableName
                        + "' "
                        + "AND col.F_GEOMETRY_COLUMN = '"
                        + columnName
                        + "' "
                        + "AND col.SRID = ref.SRID";
                result = statement.executeQuery(sqlStatement);

                if (result.next()) {
                    srid = result.getInt(1);
                }
            }
        } finally {
            dataStore.closeSafe(result);
            dataStore.closeSafe(statement);
        }

        return srid;
    }

    public String getGeometryTypeName(Integer type) {
        return "ST_Geometry";
    }

    public Class<?> getMapping(ResultSet columnMetaData, Connection cx)
            throws SQLException {
        String typeName = columnMetaData.getString("TYPE_NAME");
        String gType = null;
        if ("SYSUDTLIB.ST_GEOMETRY".equalsIgnoreCase(typeName)) {
            gType = lookupGeometryType(columnMetaData, cx, "SYSSPATIAL.GEOMETRY_COLUMNS",
                    "F_GEOMETRY_COLUMN");
        } else if ("SYSUDTLIB.ST_GEOGRAPHY".equalsIgnoreCase(typeName)) {
            gType = lookupGeometryType(columnMetaData, cx, "SYSSPATIAL.GEOGRAPHY_COLUMNS",
                    "G_GEOGRAPHY_COLUMN");
        } else {
            return null;
        }

        // decode the type into
        if (gType == null) {
            // it's either a generic geography or geometry not registered in the
            // medatata tables
            return Geometry.class;
        } else {
            Class<?> geometryClass = TYPE_TO_CLASS.get(gType.toUpperCase());
            if (geometryClass == null) {
                geometryClass = Geometry.class;
            }

            return geometryClass;
        }
    }

    String lookupGeometryType(ResultSet columnMetaData, Connection cx, String gTableName,
            String gColumnName) throws SQLException {

        // grab the information we need to proceed
        String tableName = columnMetaData.getString("TABLE_NAME");
        String columnName = columnMetaData.getString("COLUMN_NAME");
        String schemaName = columnMetaData.getString("TABLE_SCHEM");

        // first attempt, try with the geometry metadata
        Statement statement = null;
        ResultSet result = null;

        try {
            String sqlStatement = "SELECT \"GEOM_TYPE\" FROM " + gTableName + " WHERE "
                    + "F_TABLE_SCHEMA = '" + schemaName + "' " + "AND F_TABLE_NAME = '" + tableName
                    + "' " + "AND " + gColumnName + " = '" + columnName + "'";

            LOGGER.log(Level.FINE, "Geometry type check; {0} ", sqlStatement);
            statement = cx.createStatement();
            result = statement.executeQuery(sqlStatement);

            if (result.next()) {
                return result.getString(1);
            }
        } finally {
            dataStore.closeSafe(result);
            dataStore.closeSafe(statement);
        }

        return null;
    }
    
    @Override
    public boolean lookupGeneratedValuesPostInsert() {
        return true;
    }

    @Override
    public Object getLastAutoGeneratedValue(String schemaName, String tableName, String columnName,
            Connection cx) throws SQLException {

        StringBuffer sql = new StringBuffer("SELECT TOP 1 ");
        encodeColumnName(columnName, sql);
        
        sql.append(" FROM ");
        encodeTableName(schemaName, tableName, sql);
        
        sql.append(" ORDER BY ");
        encodeColumnName(columnName, sql);
        sql.append(" DESC");

        LOGGER.fine(sql.toString());
        
        PreparedStatement ps = cx.prepareStatement(sql.toString());
        try {
            ResultSet rs = ps.executeQuery();
            try {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            finally {
                dataStore.closeSafe(rs);
            }
        } 
        finally {
            dataStore.closeSafe(ps);
        }
        
        return null;
    }
    
//    void createIndex(String schemaName, String tableName, int id, Statement stmt,
//            Connection cx) throws SQLException {
//        if (tableName == null) {
//            return;
//        }
//        String key = getPrimaryKeyName(schemaName, tableName, cx);
//
//        StringBuffer sb = new StringBuffer();
//        if (schemaName != null && !schemaName.equals("")) {
//            encodeSchemaName(schemaName, sb);
//            sb.append(".");
//        }
//        encodeTableName(tableName, sb);
//        String encodedTableName = sb.toString();
//        
//        String geomColumn = null;
//        ResultSet rs = cx.getMetaData().getColumns("", schemaName, tableName, "%");
//        try {
//            while (rs.next()) {
//                String type = rs.getString(6);
//                if ("SYSUDTLIB.ST_GEOMETRY".equals(type) || "SYSUDTLIB.ST_POINT".equals(type)) {
//                    geomColumn = rs.getString(4);
//
//                    sb = new StringBuffer();
//                    if (schemaName != null && !schemaName.equals("")) {
//                        encodeSchemaName(schemaName, sb);
//                        sb.append(".");
//                    }
//                    String indexTableName = tableName + "_" + geomColumn + "_idx";
//                    encodeTableName(indexTableName, sb);
//                    String encodedIdxTableName = sb.toString();
//        
//                    rs = cx.getMetaData().getTables(null, schemaName, indexTableName, new String[]{"TABLE"});
//                    try {
//                        if (rs.next()) {
//                            String sql = MessageFormat.format(
//                                "DELETE FROM {0} WHERE \"{1}\" = {2,number,0};", 
//                                encodedIdxTableName, key, id);
//                            stmt.executeUpdate(sql);
//        
//                            sql = MessageFormat
//                                    .format(
//                                            "INSERT INTO {13} (id, cellid) VALUES ({14,number,0}, SELECT "
//                                                    + "    sysspatial.tessellate_index("
//                                                    + "      \"{1}\".ST_Envelope().ST_ExteriorRing().ST_PointN(1).ST_X(), "
//                                                    + "      \"{1}\".ST_Envelope().ST_ExteriorRing().ST_PointN(1).ST_Y(), "
//                                                    + "      \"{1}\".ST_Envelope().ST_ExteriorRing().ST_PointN(3).ST_X(), "
//                                                    + "      \"{1}\".ST_Envelope().ST_ExteriorRing().ST_PointN(3).ST_Y(), "
//                                                    + "      {3,number,0.0#}, {4,number,0.0#}, {5,number,0.0#}, {6,number,0.0#}, "
//                                                    + "      {7,number,0}, {8,number,0}, {9,number,0}, {10,number,0.0#}, {11,number,0})"
//                                                    + "    FROM {12} WHERE \"{2}\" = {14,number,0};"
//                                                    + "  ) " + "END", tableName, geomColumn, key,
//                                            BasicFeatureTypes.FEATURE.getUserData().get(TeradataDataStoreFactory.U_XMIN),
//                                            BasicFeatureTypes.FEATURE.getUserData().get(TeradataDataStoreFactory.U_YMIN),
//                                            BasicFeatureTypes.FEATURE.getUserData().get(TeradataDataStoreFactory.U_XMAX),
//                                            BasicFeatureTypes.FEATURE.getUserData().get(TeradataDataStoreFactory.U_YMAX),
//                                            BasicFeatureTypes.FEATURE.getUserData().get(TeradataDataStoreFactory.G_NX),
//                                            BasicFeatureTypes.FEATURE.getUserData().get(TeradataDataStoreFactory.G_NY),
//                                            BasicFeatureTypes.FEATURE.getUserData().get(TeradataDataStoreFactory.LEVELS),
//                                            BasicFeatureTypes.FEATURE.getUserData().get(TeradataDataStoreFactory.SCALE),
//                                            BasicFeatureTypes.FEATURE.getUserData().get(TeradataDataStoreFactory.SHIFT), 
//                                            encodedTableName, encodedIdxTableName, id);
//        
//                            stmt.executeUpdate(sql);
//                        }
//                    } catch (SQLException e) {
//                        // geometry is probably null.
//                    }
//                }    
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//    }

    /**
     * Looks up tessellation info for the schema/table/geometry.
     */
    TessellationInfo lookupTessellationInfo(Connection cx, String schemaName, String tableName, 
        String columnName) throws SQLException {
        
        if (tTableName == null) {
            return null;
        }
        
        TessellationInfo tinfo = new TessellationInfo();
        
        //first look up the spatial index table
        ResultSet rs = cx.getMetaData().getTables(null, schemaName, tableName+"_"+columnName+"_idx", 
            new String[]{"TABLE"});
        try {
            if (rs.next()) {
                tinfo.setSchemaName(schemaName);
                tinfo.setIndexTableName(rs.getString("TABLE_NAME"));
            }
            else {
                return null;
            }
        }
        finally {
            dataStore.closeSafe(rs);
        }
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT * FROM ");
        encodeTableName(schemaName, tableName, sql);
        
        sql.append(" WHERE ");
        
        encodeColumnName("F_TABLE_SCHEMA", sql);
        sql.append(" = ?").append(" AND ");
        
        encodeColumnName("F_TABLE_NAME", sql);
        sql.append(" = ? AND ");
        
        encodeColumnName("F_TABLE_COLUMN", sql);
        sql.append(" = ?");
        
        LOGGER.fine(sql.toString());
        PreparedStatement ps = cx.prepareStatement(sql.toString());
        try {
            ps.setString(1, schemaName);
            ps.setString(2, tableName);
            ps.setString(3, columnName);
            
            rs = ps.executeQuery();
            try {
                if (rs.next()) {
                    
                    tinfo.setUBounds(new Envelope(rs.getDouble("UxMin"), rs.getDouble("UxMax"), 
                        rs.getDouble("UyMin"), rs.getDouble("UyMax")));
                    tinfo.setNx(rs.getInt("NX"));
                    tinfo.setNy(rs.getInt("NY"));
                    tinfo.setLevels(rs.getInt("LEVELS"));
                    tinfo.setScale(rs.getDouble("SCALE"));
                    tinfo.setShift(rs.getInt("SHIFT"));
                    return tinfo;
                }
            }
            finally {
                dataStore.closeSafe(rs);
            }
        }
        finally {
            dataStore.closeSafe(ps);
        }
        
        return null;
    }
    
    void encodeTableName(String schemaName, String tableName, StringBuffer sql) {
        if (schemaName != null && !"".equals(schemaName.trim())) {
            encodeSchemaName(schemaName, sql);
            sql.append(".");
        }
        encodeTableName(tableName, sql);
    }
    
    @Override
    public void postCreateAttribute(AttributeDescriptor att, String tableName, String schemaName,
            Connection cx) throws SQLException {
        if (att instanceof GeometryAttribute) {
            //look up tessellation info
            TessellationInfo tinfo = 
                lookupTessellationInfo(cx, schemaName, tableName, att.getLocalName());
            if (tinfo != null) {
                att.getUserData().put(TessellationInfo.KEY, tinfo);
            }
            else {
                LOGGER.fine(String.format("%s.%s.(%s) does not have tessellation entry."));
            }
        }
    }
    
    public void postCreateTable(String schemaName, SimpleFeatureType featureType, Connection cx) 
        throws SQLException {

        String tableName = featureType.getName().getLocalPart();

        // register all geometry columns in the database
        for (AttributeDescriptor att : featureType.getAttributeDescriptors()) {
            if (att instanceof GeometryDescriptor) {
                GeometryDescriptor gd = (GeometryDescriptor) att;
                
                //figure out the native db srid
                int srid = 0;
                    
                Integer epsg = null;
                if (gd.getCoordinateReferenceSystem() != null) {
                    try {
                        epsg = CRS.lookupEpsgCode(gd.getCoordinateReferenceSystem(), true);
                    }
                    catch(Exception e) {
                        LOGGER.log(Level.WARNING, "Error looking up epsg code", e);
                    }
                }

                if (epsg != null) {
                    String sql = "SELECT SRID FROM SYSSPATIAL.spatial_ref_sys"
                            + " WHERE AUTH_SRID = ?";
                    LOGGER.log(Level.FINE, sql + ";{0}", epsg);
                    
                    PreparedStatement ps = cx.prepareStatement(sql);
                    try {
                        ps.setInt(1, epsg);
                        
                        ResultSet rs = ps.executeQuery();
                        try {
                            if (rs.next()) {
                                srid = rs.getInt("SRID");
                            }
                            else {
                                LOGGER.warning("EPSG Code " + epsg + " does not map to SRID");
                            }
                        }
                        finally {
                            dataStore.closeSafe(ps);
                        }
                    }
                    finally {
                        dataStore.closeSafe(ps);
                    }
                }
                
                // grab the geometry type
                String geomType = CLASS_TO_TYPE.get(gd.getType().getBinding());
                geomType = geomType != null ? geomType : "GEOMETRY";

                //insert into geometry columns table
                String sql = 
                    "INSERT INTO SYSSPATIAL.GEOMETRY_COLUMNS (F_TABLE_CATALOG, F_TABLE_SCHEMA, " +
                        "F_TABLE_NAME, F_GEOMETRY_COLUMN, COORD_DIMENSION, SRID, GEOM_TYPE) " +
                    "VALUES (?, ?, ?, ?, 2, ?, ?)";
                LOGGER.log(Level.FINE, sql + ";{0},{1},{2},{3},{4},{5}",
                    new Object[]{"", schemaName, tableName, gd.getLocalName(), srid, geomType});

                PreparedStatement ps = cx.prepareStatement(sql);
                try {
                    ps.setString(1, "");
                    ps.setString(2, schemaName);
                    ps.setString(3, tableName);
                    ps.setString(4, gd.getLocalName());
                    ps.setInt(5, srid);
                    ps.setString(6, geomType);
                    
                    ps.execute();
                }
                finally {
                    dataStore.closeSafe(ps);
                }

                //create the spatial index table
                PrimaryKey pkey = dataStore.getPrimaryKeyFinder()
                    .getPrimaryKey(dataStore, schemaName, tableName, cx);
                if (!(pkey instanceof NullPrimaryKey)) {
                    String indexTableName = tableName + "_" + gd.getLocalName() + "_idx";
                    
                    StringBuffer sb = new StringBuffer("DROP TABLE ");
                    encodeTableName(schemaName, indexTableName, sb);
                    
                    sql = sb.toString();
                    LOGGER.fine(sql);
                    
                    try {
                        ps = cx.prepareStatement(sql);
                        ps.execute();
                    }
                    catch(SQLException e) {
                        //ignore
                    }
                    finally {
                        dataStore.closeSafe(ps);
                    }

                    sb = new StringBuffer("CREATE MULTISET TABLE ");
                    encodeTableName(schemaName, indexTableName, sb);
                    sb.append("( ");
                    
                    for (PrimaryKeyColumn col : pkey.getColumns()) {
                        encodeColumnName(col.getName(), sb);
                        
                        String typeName = 
                            dataStore.getColumnSqlTypeName(cx, schemaName, tableName, col.getName());
                        sb.append(" ").append(typeName).append(", ");
                    }
                    sb.append("cellid INTEGER) PRIMARY INDEX (cellid)");
                    sql = sb.toString();
                    LOGGER.fine(sql);

                    ps = cx.prepareStatement(sql);
                    try {
                        ps.execute();
                    }
                    finally {
                        dataStore.closeSafe(ps);
                    }
                    
                    //TODO: create triggers to keep spatial index in sync
                    /*
                    "CREATE TRIGGER \"{0}_{1}_mi\" AFTER INSERT ON {12}"
                    + "  REFERENCING NEW TABLE AS nt"
                    + "  FOR EACH STATEMENT"
                    + "  BEGIN ATOMIC"
                    + "  ("
                    + "    INSERT INTO {13} SELECT \"{2}\","
                    + "      sysspatial.tessellate_index("
                    + "      \"{1}\".ST_Envelope().ST_ExteriorRing().ST_PointN(1).ST_X(), "
                    + "      \"{1}\".ST_Envelope().ST_ExteriorRing().ST_PointN(1).ST_Y(), "
                    + "      \"{1}\".ST_Envelope().ST_ExteriorRing().ST_PointN(3).ST_X(), "
                    + "      \"{1}\".ST_Envelope().ST_ExteriorRing().ST_PointN(3).ST_Y(), "
                    + "      {3,number,0.0#}, {4,number,0.0#}, {5,number,0.0#}, {6,number,0.0#}, "
                    + "      {7,number,0}, {8,number,0}, {9,number,0}, {10,number,0.0#}, {11,number,0})"
                    // + ")"
                    + "    FROM nt WHERE \"{1}\" IS NOT NULL;"
                    + "  ) " + "END"
                    */
                    
                    /*"CREATE TRIGGER \"{0}_{1}_mu\" AFTER UPDATE OF \"{1}\" ON {12}"
                    + "  REFERENCING NEW AS nt"
                    + "  FOR EACH STATEMENT"
                    + "  BEGIN ATOMIC"
                    + "  ("
                    + "    DELETE FROM {13} WHERE id in (SELECT \"{2}\" from nt); "
                    + "    INSERT INTO {13} SELECT \"{2}\","
                    + "    sysspatial.tessellate_index("
                    + "      \"{1}\".ST_Envelope().ST_ExteriorRing().ST_PointN(1).ST_X(), "
                    + "      \"{1}\".ST_Envelope().ST_ExteriorRing().ST_PointN(1).ST_Y(), "
                    + "      \"{1}\".ST_Envelope().ST_ExteriorRing().ST_PointN(3).ST_X(), "
                    + "      \"{1}\".ST_Envelope().ST_ExteriorRing().ST_PointN(3).ST_Y(), "
                    + "      {3,number,0.0#}, {4,number,0.0#}, {5,number,0.0#}, {6,number,0.0#}, "
                    + "      {7,number,0}, {8,number,0}, {9,number,0}, {10,number,0.0#}, {11,number,0})"
                    + "    FROM nt WHERE \"{1}\" IS NOT NULL;" + "  ) "*/
                    
                    /*"CREATE TRIGGER \"{0}_{1}_md\" AFTER DELETE ON {2}"
                    + "  REFERENCING OLD TABLE AS ot"
                    + "  FOR EACH STATEMENT"
                    + "  BEGIN ATOMIC"
                    + "  ("
                    + "    DELETE FROM \"{0}_{1}_idx\" WHERE ID IN (SELECT \"{1}\" from ot);"
                    + "  )" + "END"*/
                }
                else {
                    LOGGER.warning("No primary key for " + schemaName + "." + tableName + ". Unable"
                       + " to create spatial index.");
                }
            }
            
            //cx.commit();
        }
    }

    public void registerClassToSqlMappings(Map<Class<?>, Integer> mappings) {
        super.registerClassToSqlMappings(mappings);

        // jdbc metadata for geom columns reports DATA_TYPE=1111=Types.OTHER
        mappings.put(Geometry.class, Types.OTHER);
    }

    public void registerSqlTypeNameToClassMappings(
            Map<String, Class<?>> mappings) {
        super.registerSqlTypeNameToClassMappings(mappings);

        mappings.put("ST_Geometry", Geometry.class);
    }
    
    @Override
    public void registerSqlTypeToSqlTypeNameOverrides(
            Map<Integer, String> overrides) {
        overrides.put(Types.VARCHAR, "VARCHAR");
        overrides.put(Types.DOUBLE, "FLOAT");
        overrides.put(Types.NUMERIC, "DECIMAL");
    }

    @Override
    public boolean isLimitOffsetSupported() {
        return false;
    }
    
    @Override
    public PreparedFilterToSQL createPreparedFilterToSQL() {
        return new TeradataFilterToSQL(this);
    }
}
