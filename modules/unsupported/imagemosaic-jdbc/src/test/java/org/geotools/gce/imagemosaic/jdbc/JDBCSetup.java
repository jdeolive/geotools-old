package org.geotools.gce.imagemosaic.jdbc;

import org.geotools.data.jdbc.datasource.DataSourceFinder;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.net.URL;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;


public abstract class JDBCSetup {
    protected DataSource dataSource;

    public abstract String getConfigUrl();

    protected abstract String getBLOBSQLType();

    protected abstract String getMulitPolygonSQLType();

    protected abstract String getDriverClassName();

    protected abstract String getJDBCUrl(String host, Integer port,
        String dbName);

    protected abstract String getXMLConnectFragmentName();

    protected Config getConfig() throws Exception {
        return Configurations.getConfig(getConfigUrl());
    }

    protected String[] getTileTableNames() {
        return new String[] {  /* "TILES0", */"TILES1", "TILES2", "TILES3" };
    }

    protected String[] getSpatialTableNames() {
        return new String[] {  /* "SPATIAL0", */"SPATIAL1", "SPATIAL2", "SPATIAL3" };
    }

    private DataSource getDataSource() throws Exception {
        if (dataSource != null) {
            return dataSource;
        }

        Config config = getConfig();
        dataSource = DataSourceFinder.getDataSource(config.getDataSourceParams());

        // dataSource = new BasicDataSource();
        // dataSource.setUrl(config.getJdbcUrl());
        // dataSource.setDriverClassName(config.getDriverClassName());
        // dataSource.setPoolPreparedStatements(false);
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
            // e.printStackTrace();
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
        Connection con = null;

        try {
            con = getConnection();
        } catch (Exception e) {
            return;
        }

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
        getMulitPolygonSQLType() + " NOT NULL ");
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
        getMulitPolygonSQLType() + " NOT NULL ");
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
            URL shapeFileUrl = new URL("file:" +
                    AbstractTest.OUTPUTDIR_RESOURCES + i + File.separator +
                    "index.shp");
            Import imp = new Import(getConfig(), getSpatialTableNames()[i],
                    getTileTableNames()[i], shapeFileUrl, "LOCATION", 2, con,
                    true);
            imp.fillSpatialTable();

            // fillTileTable(getTileTableNames()[i], i, con);
            // fillSpatialTable(getSpatialTableNames()[i], i, con);
            // insertMasterRecord(getTileTableNames()[i],
            // getSpatialTableNames()[i], con);
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
            URL csvFileUrl = new URL("file:" +
                    AbstractTest.OUTPUTDIR_RESOURCES + i + File.separator +
                    "index.csv");
            Import imp = new Import(getConfig(), getSpatialTableNames()[i],
                    getSpatialTableNames()[i], csvFileUrl, ";", 2, con, false);
            imp.fillSpatialTable();

            // fillSpatialTableJoined(getSpatialTableNames()[i], i, con);
            // insertMasterRecord(getSpatialTableNames()[i],
            // getSpatialTableNames()[i], con);
        }

        for (String tn : getSpatialTableNames()) {
            createIndex(tn, con);
        }

        con.commit();
        con.close();
    }

    protected void run(InputStream script) throws Exception {
        // load the script
        BufferedReader reader = new BufferedReader(new InputStreamReader(script));

        // connect
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
