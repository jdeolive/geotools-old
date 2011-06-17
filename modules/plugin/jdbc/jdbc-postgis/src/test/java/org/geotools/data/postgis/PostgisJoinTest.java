package org.geotools.data.postgis;

import org.geotools.jdbc.JDBCJoinTest;
import org.geotools.jdbc.JDBCJoinTestSetup;

public class PostgisJoinTest extends JDBCJoinTest {

    @Override
    protected JDBCJoinTestSetup createTestSetup() {
        return new PostgisJoinTestSetup();
    }

}
