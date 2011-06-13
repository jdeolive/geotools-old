package org.geotools.data.h2;

import org.geotools.jdbc.JDBCJoinTest;
import org.geotools.jdbc.JDBCJoinTestSetup;

public class H2JoinTest extends JDBCJoinTest {

    @Override
    protected JDBCJoinTestSetup createTestSetup() {
        return new H2JoinTestSetup();
    }

}
