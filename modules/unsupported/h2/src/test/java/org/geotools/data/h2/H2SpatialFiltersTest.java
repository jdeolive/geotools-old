package org.geotools.data.h2;

import org.geotools.jdbc.JDBCDataStoreAPITestSetup;
import org.geotools.jdbc.JDBCSpatialFiltersTest;

public class H2SpatialFiltersTest extends JDBCSpatialFiltersTest {

    @Override
    protected JDBCDataStoreAPITestSetup createTestSetup() {
        return new H2DataStoreAPITestSetup();
    }

}
