package org.geotools.data.ingres;

import org.geotools.jdbc.JDBCDataStoreFactory;
import org.geotools.jdbc.JDBCTestSetup;

public class IngresTestSetup extends JDBCTestSetup {

    @Override
    protected JDBCDataStoreFactory createDataStoreFactory() {
        return null;
    }

}
