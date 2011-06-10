package org.geotools.data.h2;

import org.geotools.jdbc.JDBCJoinTest;
import org.geotools.jdbc.JDBCTestSetup;

public class H2JoinTest extends JDBCJoinTest {

    @Override
    protected JDBCTestSetup createTestSetup() {
        return new H2TestSetup();
    }

}
