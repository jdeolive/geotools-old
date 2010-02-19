package org.geotools.data.ingres;

import org.geotools.jdbc.JDBCFeatureSourceTest;
import org.geotools.jdbc.JDBCTestSetup;

public class IngresFeatureSourceTest extends JDBCFeatureSourceTest {

	@Override
    protected JDBCTestSetup createTestSetup() {
        return new IngresTestSetup();
    }
}
