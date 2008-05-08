package org.geotools.data.h2;

import org.geotools.jdbc.JDBCFeatureLockingTest;
import org.geotools.jdbc.JDBCTestSetup;

public class H2FeatureLockingTest extends JDBCFeatureLockingTest {

    protected JDBCTestSetup createTestSetup() {
        return new H2TestSetup();
    }

}
