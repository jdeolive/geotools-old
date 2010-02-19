package org.geotools.data.ingres;

import org.geotools.jdbc.JDBCEmptyTest;
import org.geotools.jdbc.JDBCEmptyTestSetup;

public class IngresEmptyTest extends JDBCEmptyTest {

	@Override
	protected JDBCEmptyTestSetup createTestSetup() {
		return new IngresEmptyTestSetup(new IngresTestSetup());
	}
}
