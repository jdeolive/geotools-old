package org.geotools.data.h2;

import org.geotools.jdbc.JDBCPrimaryKeyTest;
import org.geotools.jdbc.JDBCPrimaryKeyTestSetup;

public class H2PrimaryKeyTest extends JDBCPrimaryKeyTest {

    @Override
    protected JDBCPrimaryKeyTestSetup createTestSetup() {
        return new H2PrimaryKeyTestSetup();
    }

}
