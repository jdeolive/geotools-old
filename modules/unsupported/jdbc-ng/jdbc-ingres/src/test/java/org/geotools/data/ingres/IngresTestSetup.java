package org.geotools.data.ingres;

import org.geotools.jdbc.JDBCDataStore;
import org.geotools.jdbc.JDBCDataStoreFactory;
import org.geotools.jdbc.JDBCTestSetup;

public class IngresTestSetup extends JDBCTestSetup {

    @Override
    protected JDBCDataStoreFactory createDataStoreFactory() {
        return new IngresDataStoreFactory();
    }
    
    @Override
    protected void setUpDataStore(JDBCDataStore dataStore) {
        super.setUpDataStore(dataStore);
        
     // let's work with the most common schema please
        dataStore.setDatabaseSchema("geotools");
    }
    
    @Override
    protected void setUpData() throws Exception {
        runSafe("DROP TABLE \"ft1\"");

        run("CREATE TABLE \"ft1\"(" //
                + "\"id\" int primary key, " //
                + "\"geometry\" geometry SRID 4326, " //
                + "\"intProperty\" int," //
                + "\"doubleProperty\" double precision, " // 
                + "\"stringProperty\" varchar)");
        
        run("INSERT INTO \"ft1\" VALUES(0, GeometryFromText('POINT(0 0)', 4326), 0, 0.0, 'zero')");
        run("INSERT INTO \"ft1\" VALUES(1, GeometryFromText('POINT(1 1)', 4326), 1, 1.1, 'one')");
        run("INSERT INTO \"ft1\" VALUES(2, GeometryFromText('POINT(2 2)', 4326), 2, 2.2, 'two')");
    }

    
}
