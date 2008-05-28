package org.geotools.gce.imagemosaic.jdbc;

import com.ibm.jvm.util.ByteArrayOutputStream;

import com.vividsolutions.jts.geom.Geometry;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.data.jdbc.datasource.DataSourceFinder;

import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;


import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.net.URL;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;


public abstract class JDBCSetup {
    protected static String BaseDirURL = "file:src/test/resources/";
    protected DataSource dataSource;

    public abstract String getConfigUrl();

    protected abstract String getBLOBSQLType();

    protected abstract String getMulitPolygonSQLType();

    protected Config getConfig() throws Exception {
        return Configurations.getConfig(getConfigUrl());
    }

    protected String[] getTileTableNames() {
        return new String[] { "TILES0", "TILES1", "TILES2", "TILES3" };
    }

    protected String[] getSpatialTableNames() {
        return new String[] { "SPATIAL0", "SPATIAL1", "SPATIAL2", "SPATIAL3" };
    }

    private DataSource getDataSource() throws Exception {
        if (dataSource != null) {
            return dataSource;
        }

        Config config = getConfig();
        dataSource = DataSourceFinder.getDataSource(config.getDataSourceParams());

        //        dataSource = new BasicDataSource();
        //        dataSource.setUrl(config.getJdbcUrl());
        //        dataSource.setDriverClassName(config.getDriverClassName());
        //        dataSource.setPoolPreparedStatements(false);        
        return dataSource;
    }

    protected Connection getConnection() throws Exception {
        Connection con = getDataSource().getConnection();
        con.setAutoCommit(false);

        return con;
    }

    private void drop(String tableName, Connection con) {
        try {
            con.prepareStatement("drop table " + tableName).execute();
        } catch (SQLException e) {
            //e.printStackTrace();
        }
    }

    protected void registerSpatial(String tn, Connection con)
        throws Exception {
    }

    protected void unregisterSpatial(String tn, Connection con)
        throws Exception {
    }

    protected abstract void createIndex(String tn, Connection con)
        throws Exception;

    protected void dropIndex(String tn, Connection con)
        throws Exception {
        try {
            con.prepareStatement("drop index IX_" + tn).execute();
        } catch (SQLException e) {
        }
    }

    public void dropAll() throws Exception {
        Connection con = getConnection();

        for (String tn : getTileTableNames())
            drop(tn, con);

        con.commit();
        con.close();

        con = getConnection();
        drop(getConfig().getMasterTable(), con);

        for (String tn : getSpatialTableNames())
            dropIndex(tn, con);

        for (String tn : getSpatialTableNames())
            unregisterSpatial(tn, con);

        for (String tn : getSpatialTableNames())
            drop(tn, con);

        con.commit();
        con.close();
    }

    protected String getDoubleSQLType() {
        return "DOUBLE";
    }

    private String getCreateMasterStatement() throws Exception {
        Config config = getConfig();
        String doubleType = getDoubleSQLType();
        String statement = " CREATE TABLE " + config.getMasterTable();
        statement += ("(" + config.getCoverageNameAttribute() +
        " CHARACTER (64)  NOT NULL");
        statement += ("," + config.getSpatialTableNameAtribute() +
        " VARCHAR (256)  NOT NULL");
        statement += ("," + config.getTileTableNameAtribute() +
        " VARCHAR (256)  NOT NULL");
        statement += ("," + config.getResXAttribute() + " " + doubleType + "," +
        config.getResYAttribute() + " " + doubleType);
        statement += ("," + config.getMinXAttribute() + " " + doubleType + "," +
        config.getMinYAttribute() + " " + doubleType);
        statement += ("," + config.getMaxXAttribute() + " " + doubleType + "," +
        config.getMaxYAttribute() + " " + doubleType);
        statement += ",CONSTRAINT MASTER_PK PRIMARY KEY (";
        statement += (config.getCoverageNameAttribute() + "," +
        config.getSpatialTableNameAtribute() + "," +
        config.getTileTableNameAtribute());
        statement += "))";

        return statement;
    }

    private String getCreateTileTableStatement(String tableName)
        throws Exception {
        String statement = " CREATE TABLE " + tableName;
        statement += ("(" + getConfig().getKeyAttributeNameInTileTable() +
        " CHAR(64) NOT NULL ");
        statement += ("," + getConfig().getBlobAttributeNameInTileTable() +
        " " + getBLOBSQLType());
        statement += (",CONSTRAINT " + tableName + "_PK PRIMARY KEY(" +
        getConfig().getKeyAttributeNameInTileTable());
        statement += "))";

        return statement;
    }

    protected String getCreateSpatialTableStatement(String tableName)
        throws Exception {
        String statement = " CREATE TABLE " + tableName;
        statement += (" ( " + getConfig().getKeyAttributeNameInSpatialTable() +
        " CHAR(64) NOT NULL, " +
        getConfig().getGeomAttributeNameInSpatialTable() + " " +
        getMulitPolygonSQLType() +" NOT NULL ");
        statement += (",CONSTRAINT " + tableName + "_PK PRIMARY KEY(" +
        getConfig().getKeyAttributeNameInSpatialTable());
        statement += "))";

        return statement;
    }

    protected String getCreateSpatialTableStatementJoined(String tableName)
        throws Exception {
        String statement = " CREATE TABLE " + tableName;
        statement += (" ( " + getConfig().getKeyAttributeNameInSpatialTable() +
        " CHAR(64) NOT NULL, " +
        getConfig().getGeomAttributeNameInSpatialTable() + " " +
        getMulitPolygonSQLType() + " NOT NULL " );
        statement += ("," + getConfig().getBlobAttributeNameInTileTable() +
        " " + getBLOBSQLType());
        statement += (",CONSTRAINT " + tableName + "_PK PRIMARY KEY(" +
        getConfig().getKeyAttributeNameInSpatialTable());
        statement += "))";

        return statement;
    }

    public void createAll() throws Exception {
        String createMaster = getCreateMasterStatement();
        Connection con = getConnection();
        con.prepareStatement(createMaster).execute();

        for (String tn : getTileTableNames()) {
            con.prepareStatement(getCreateTileTableStatement(tn)).execute();
        }

        for (String tn : getSpatialTableNames()) {
            con.prepareStatement(getCreateSpatialTableStatement(tn)).execute();
            registerSpatial(tn, con);            
        }

        con.commit();

        for (int i = 0; i < getTileTableNames().length; i++) {
            fillTileTable(getTileTableNames()[i], i, con);
            fillSpatialTable(getSpatialTableNames()[i], i, con);
            insertMasterRecord(getTileTableNames()[i],
                getSpatialTableNames()[i], con);
        }

        for (String tn : getSpatialTableNames()) {
        	createIndex(tn, con);
        }
        con.commit();
        con.close();
    }

    public void createAllJoined() throws Exception {
        String createMaster = getCreateMasterStatement();
        Connection con = getConnection();
        con.prepareStatement(createMaster).execute();

        for (String tn : getSpatialTableNames()) {
            con.prepareStatement(getCreateSpatialTableStatementJoined(tn))
               .execute();
            registerSpatial(tn, con);
        }

        con.commit();

        for (int i = 0; i < getTileTableNames().length; i++) {
            fillSpatialTableJoined(getSpatialTableNames()[i], i, con);
            insertMasterRecord(getSpatialTableNames()[i],
                getSpatialTableNames()[i], con);
        }

        for (String tn : getSpatialTableNames()) {
            createIndex(tn, con);
        }
        con.commit();
        con.close();
    }

    private void fillTileTable(String tileTableName, int index, Connection con)
        throws Exception {
        Config config = getConfig();
        String statmentString = "INSERT INTO " + tileTableName + "(" +
            config.getKeyAttributeNameInTileTable() + "," +
            config.getBlobAttributeNameInTileTable() + ") values (?,?)";
        PreparedStatement ps = con.prepareStatement(statmentString);
        File dir = new File(new URL(BaseDirURL + index).getPath());
        File[] files = dir.listFiles();

        for (File file : files) {
            String fnName = file.getName();

            if ((fnName.endsWith(".tif") || fnName.endsWith(".TIF")) == false) {
                continue;
            }

            ps.setString(1, fnName);

            //ps.setBinaryStream(2, new FileInputStream(file), (int)(file.length()));
            ps.setBytes(2, getImageBytes(file));
            ps.execute();
            con.commit();
        }

        ps.close();
    }

    byte[] getImageBytes(File file) throws FileNotFoundException, IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        InputStream in = new FileInputStream(file);
        int value;

        while ((value = in.read()) != -1)
            out.write(value);

        out.close();
        in.close();

        return out.toByteArray();
    }

    protected FeatureCollection<SimpleFeatureType, SimpleFeature> loadIndex(
        int index) throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("url",
            new URL(BaseDirURL + index + File.separator + "index.shp"));

        DataStore shapefile = DataStoreFinder.getDataStore(map);
        FeatureSource<SimpleFeatureType, SimpleFeature> contents = shapefile.getFeatureSource(
                "index");

        return contents.getFeatures();
    }

    protected void fillSpatialTable(String spatialTableName, int index,
        Connection con) throws Exception {
        Config config = getConfig();
        FeatureCollection<SimpleFeatureType, SimpleFeature> coll = loadIndex(index);
        FeatureIterator<SimpleFeature> it = coll.features();

        //int fid = 0;
        while (it.hasNext()) {
            SimpleFeature feature = it.next();

            Geometry geom = (Geometry) feature.getDefaultGeometry();
            String location = (String) feature.getAttribute("LOCATION");

            String statement = "INSERT INTO " + spatialTableName + " (" +
                config.getKeyAttributeNameInSpatialTable() + "," +
                getConfig().getGeomAttributeNameInSpatialTable() +
                ") VALUES  ('" + location + "'," + getToGemoetryClause(geom) +
                ")";

            con.prepareStatement(statement).execute();

            //fid++;						
        }

        coll.close(it);
        con.commit();
    }

    protected void fillSpatialTableJoined(String spatialTableName, int index,
        Connection con) throws Exception {
        Config config = getConfig();
        FeatureCollection<SimpleFeatureType, SimpleFeature> coll = loadIndex(index);
        FeatureIterator<SimpleFeature> it = coll.features();

        //int fid = 0;
        while (it.hasNext()) {
            SimpleFeature feature = it.next();

            Geometry geom = (Geometry) feature.getDefaultGeometry();
            String location = (String) feature.getAttribute("LOCATION");

            String statement = "INSERT INTO " + spatialTableName + " (" +
                config.getKeyAttributeNameInSpatialTable() + "," +
                getConfig().getGeomAttributeNameInSpatialTable() + "," +
                config.getBlobAttributeNameInTileTable() + ") VALUES  ('" +
                location + "'," + getToGemoetryClause(geom) + ",?)";

            PreparedStatement ps = con.prepareStatement(statement);
            File file = new File(new URL(BaseDirURL + index).getPath() +
                    File.separator + location);
            ps.setBytes(1, getImageBytes(file));
            ps.execute();

            //fid++;						
        }

        coll.close(it);
        con.commit();
    }

    private void insertMasterRecord(String tileTableName,
        String spatialTableName, Connection con) throws Exception {
        Config config = getConfig();
        String statmentString = "INSERT INTO " + config.getMasterTable() + "(" +
            config.getCoverageNameAttribute() + "," +
            config.getTileTableNameAtribute() + "," +
            config.getSpatialTableNameAtribute() + ") VALUES (?,?,?)";
        PreparedStatement ps = con.prepareStatement(statmentString);
        ps.setString(1, config.getCoverageName());
        ps.setString(2, tileTableName);
        ps.setString(3, spatialTableName);
        ps.execute();
        ps.close();
    }

    protected abstract String getToGemoetryClause(Geometry g);

    protected void run(InputStream script) throws Exception {
        //load the script
        BufferedReader reader = new BufferedReader(new InputStreamReader(script));

        //connect
        Connection conn = getConnection();

        try {
            Statement st = conn.createStatement();

            try {
                String line = null;

                while ((line = reader.readLine()) != null) {
                    st.execute(line);
                }

                reader.close();
            } finally {
                st.close();
            }
        } finally {
            conn.close();
        }
    }
}
