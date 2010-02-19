package org.geotools.data.ingres;

import org.geotools.jdbc.JDBCDataStore;
import org.geotools.jdbc.JDBCDataStoreFactory;
import org.geotools.jdbc.JDBCTestSetup;

public class IngresTestSetup extends JDBCTestSetup {

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
