package org.geotools.data.ingres;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Map;

import org.geotools.data.DataAccessFactory.Param;
import org.geotools.jdbc.JDBCDataStore;
import org.geotools.jdbc.JDBCDataStoreFactory;
import org.geotools.jdbc.SQLDialect;

public class IngresNGDataStoreFactory extends JDBCDataStoreFactory {

	public static final Param DBTYPE = new Param("dbtype", String.class, "Type", true, "Ingres");
	 
    protected SQLDialect createSQLDialect(JDBCDataStore dataStore) {
    	//NOTE: db2 and postgis return different class here
       return new IngresDialect(dataStore);
    }

    public String getDisplayName() {
        return "Ingres NG";
    }
    
    protected String getDriverClassName() {
    	//TODO:confirm name
        return "org.ingres.Driver";
    }

    protected String getDatabaseID() {
        return (String)DBTYPE.sample;
    }

    public String getDescription() {
        return "Ingres Database";
    }
    
    @Override
    protected String getValidationQuery() {
    	//basically queries the current time in database, need to update the query for Ingres
        return "select current date from sysibm.sysdummy1";
    }
    
    @Override
    protected boolean checkDBType(Map params) {
        //DB2,postgis implementation different 
    	if (super.checkDBType(params)) {
            return true;
        }
        
        return false;
    }
    
    @Override
    protected String getJDBCUrl(Map params) throws IOException {
    	//NOTE:DB2 implmentation different, need to confirm
    	   String host = (String) HOST.lookUp(params);
           String db = (String) DATABASE.lookUp(params);
           int port = (Integer) PORT.lookUp(params);
           //TODO:confirm this string
           return "jdbc:ingressql" + "://" + host + ":" + port + "/" + db;
    }
    
    protected void setupParameters(Map parameters) {
    	//NOTE:postgis puts more info here
        super.setupParameters(parameters);
        parameters.put(DBTYPE.key, DBTYPE);
    }

    @Override
    protected JDBCDataStore createDataStoreInternal(JDBCDataStore dataStore, Map params)
    throws IOException {
    	//TODO:check this
    	return dataStore;
    }

}
