package org.geotools.data.ingres;

import org.geotools.jdbc.JDBCDataStore;
import org.geotools.jdbc.JDBCDataStoreFactory;
import org.geotools.jdbc.JDBCTestSetup;
import org.geotools.jdbc.SQLDialect;

public class IngresTestSetup extends JDBCTestSetup {

	
	// testing my svn commit access
	
	
    @Override
    protected JDBCDataStoreFactory createDataStoreFactory() {
        return null;
    }
    
    @Override
    protected void setUpDataStore(JDBCDataStore dataStore) {
        super.setUpDataStore(dataStore);
        
        
    }
    
    @Override
    protected void setUpData() throws Exception {
       
    }

    
}
