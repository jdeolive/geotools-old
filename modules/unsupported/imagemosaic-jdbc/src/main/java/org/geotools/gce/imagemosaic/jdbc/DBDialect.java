/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.gce.imagemosaic.jdbc;

import org.geotools.data.jdbc.datasource.DataSourceFinder;

import java.sql.Connection;

import javax.sql.DataSource;


public abstract class DBDialect {
    protected DataSource dataSource;
    protected Config config;

    public DBDialect(Config config) {
        super();
        this.config = config;
    }

    static DBDialect getDBDialect(Config config) {
        SpatialExtension type = config.getSpatialExtension();

        if (type == null) {
            return null;
        }

        if (type == SpatialExtension.DB2) {
            return new DB2Dialect(config);
        } else if (type == SpatialExtension.POSTGIS) {
            return new PostgisDialect(config);
        } else if (type == SpatialExtension.MYSQL) {
            return new MySqlDialect(config);
        } else if (type == SpatialExtension.UNIVERSAL) {
            return new UniversalDialect(config);
        } else if (type == SpatialExtension.ORACLE) {
            return new OracleDialect(config);
        } else {
            return null;
        }
    }

    protected abstract String getBLOBSQLType();

    protected abstract String getMulitPolygonSQLType();

    protected abstract String getDriverClassName();

    protected abstract String getJDBCUrl(String host, Integer port,
        String dbName);

    protected abstract String getXMLConnectFragmentName();

    protected Config getConfig() {
        return config;
    }

    private DataSource getDataSource() throws Exception {
        if (dataSource != null) {
            return dataSource;
        }

        Config config = getConfig();
        dataSource = DataSourceFinder.getDataSource(config.getDataSourceParams());

        return dataSource;
    }

    protected Connection getConnection() throws Exception {
        Connection con = getDataSource().getConnection();
        con.setAutoCommit(false);

        return con;
    }

    String getDropTableStatement(String tableName) {
        return "drop table " + tableName;
    }

    protected String getUnregisterSpatialStatement(String tn) {
        return null;
    }

    protected String getRegisterSpatialStatement(String tn, String srs) {
        return null;
    }

    protected abstract String getCreateIndexStatement(String tn)
        throws Exception;

    String getDropIndexStatment(String tn) {
        return "drop index IX_" + tn;
    }

    protected String getDoubleSQLType() {
        return "DOUBLE";
    }

    String getCreateMasterStatement() throws Exception {
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

    String getCreateTileTableStatement(String tableName)
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
}
