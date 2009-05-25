package org.geotools.data.h2.jndi;

import org.geotools.data.h2.H2TestSetup;
import org.geotools.jdbc.JDBCDataStoreTest;
import org.geotools.jdbc.JDBCJNDITestSetup;
import org.geotools.jdbc.JDBCTestSetup;

public class H2DataStoreTest extends JDBCDataStoreTest {

    @Override
    protected JDBCTestSetup createTestSetup() {
        return new JDBCJNDITestSetup(new H2TestSetup());
    }

}
