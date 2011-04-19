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
package org.geotools.data.teradata;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.geotools.data.jdbc.datasource.DBCPDataSource;
import org.geotools.feature.type.BasicFeatureTypes;
import org.geotools.jdbc.CompositePrimaryKeyFinder;
import org.geotools.jdbc.HeuristicPrimaryKeyFinder;
import org.geotools.jdbc.JDBCDataStore;
import org.geotools.jdbc.JDBCDataStoreFactory;
import org.geotools.jdbc.MetadataTablePrimaryKeyFinder;
import org.geotools.jdbc.PrimaryKeyFinder;
import org.geotools.jdbc.SQLDialect;
import org.geotools.util.logging.Logging;
import org.opengis.feature.type.FeatureType;

public class TeradataDataStoreFactory extends JDBCDataStoreFactory {

    public static final Logger LOGGER = Logging.getLogger(TeradataDataStoreFactory.class);

    /**
     * parameter for database type
     */
    public static final Param DBTYPE = new Param("dbtype", String.class, "Type", true, "teradata");

    /**
     * enables using && in bbox queries
     */
    public static final Param LOOSEBBOX = new Param("Loose bbox", Boolean.class,
            "Perform only primary filter on bbox", false, Boolean.TRUE);

    /**
     * parameter for database port
     */
    public static final Param PORT = new Param("port", Integer.class, "Port", true, 1025);

    /**
     * teradata connection mode
     */
    public static final Param TMODE = new Param("tmode", String.class, "tmode", false, "ANSI");

    /**
     * charset to use when reading character data
     */
    public static final Param CHARSET = new Param("charset", String.class, "charset", false, "UTF8");

    /**
     * Tessellation lookup table
     */
    public static final Param TESSELLATION_TABLE = new Param("tessellationTable", String.class, 
        "Tessellation lookup table", false, "sysspatial.tessellation");
    
    // SET QUERY_BAND = 'ApplicationName=TZA-InsuranceService;
    // Version=01.00.00.00;' FOR Session;
    public static final Param QUERY_BANDING_SQL = new Param(
            "queryBandingSQL",
            String.class,
            "SQL to use Query Banding (example: \"SET QUERY_BAND = 'ApplicationName=TZA-InsuranceService; Version=01.00.00.00;' FOR Session;\")",
            false, "");

    private static final PrimaryKeyFinder KEY_FINDER = new CompositePrimaryKeyFinder(
            new MetadataTablePrimaryKeyFinder(), new TeradataPrimaryKeyFinder(),
            new HeuristicPrimaryKeyFinder());

    @Override
    protected SQLDialect createSQLDialect(JDBCDataStore dataStore) {
        return new TeradataDialect(dataStore);
    }

    @Override
    public String getDatabaseID() {
        return (String) DBTYPE.sample;
    }

    @Override
    public String getDisplayName() {
        return "Teradata";
    }

    public String getDescription() {
        return "Teradata Database";
    }

    @Override
    protected String getDriverClassName() {
        return "com.teradata.jdbc.TeraDriver";
    }

    @Override
    protected boolean checkDBType(Map params) {
        return checkDBType(params, "teradata");
    }

    @Override
    protected JDBCDataStore createDataStoreInternal(JDBCDataStore dataStore, final Map params)
            throws IOException {

        // setup loose bbox
        TeradataDialect dialect = (TeradataDialect) dataStore.getSQLDialect();

        Boolean loose = (Boolean) LOOSEBBOX.lookUp(params);
        dialect.setLooseBBOXEnabled(loose == null || Boolean.TRUE.equals(loose));

        if (!params.containsKey(PK_METADATA_TABLE.key)) {
            dataStore.setPrimaryKeyFinder(KEY_FINDER);
        }

        if (params.containsKey(QUERY_BANDING_SQL.key)) {
            //dialect.setQueryBandingSql((String) QUERY_BANDING_SQL.lookUp(params));
        }

        //set schema to be same as user
        String username = null;
        if (params.containsKey(USER.key)) {
            username = (String) USER.lookUp(params);
        }
        else if (params.containsKey(DATASOURCE.key)) {
            DataSource dataSource = (DataSource) DATASOURCE.lookUp(params);
            if (dataSource instanceof BasicDataSource) {
                username = ((BasicDataSource)dataSource).getUsername();
            }
            else if (dataSource instanceof DBCPDataSource) {
                try {
                    username = ((BasicDataSource)((DBCPDataSource)dataSource)
                        .unwrap(DataSource.class)).getUsername();
                } catch (SQLException e) {
                    throw (IOException) new IOException().initCause(e);
                }
            }
        }
        dataStore.setDatabaseSchema(username);
        return dataStore;
    }

    @Override
    protected void setupParameters(Map parameters) {
        super.setupParameters(parameters);
        
        parameters.put(DBTYPE.key, DBTYPE);
        parameters.put(LOOSEBBOX.key, LOOSEBBOX);
        parameters.put(PORT.key, PORT);
        parameters.put(MAX_OPEN_PREPARED_STATEMENTS.key, MAX_OPEN_PREPARED_STATEMENTS);

        
        parameters.put(QUERY_BANDING_SQL.key, QUERY_BANDING_SQL);
    }

    @Override
    protected String getValidationQuery() {
        return "select now()";
    }

    @Override
    protected String getJDBCUrl(Map params) throws IOException {
        String host = (String) HOST.lookUp(params);
        String db = (String) DATABASE.lookUp(params);
        int port = (Integer) PORT.lookUp(params);
        String mode = (String) TMODE.lookUp(params);
        if (mode == null)
            mode = TMODE.sample.toString();
        String charset = (String) CHARSET.lookUp(params);
        if (charset == null)
            charset = CHARSET.sample.toString();
        return "jdbc:teradata://" + host + "/DATABASE=" + db + ",PORT=" + port + ",TMODE=" + mode
                + ",CHARSET=" + charset;
    }
}
