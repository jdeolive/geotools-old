package org.geotools.data.ingres;

import org.geotools.jdbc.JDBCPrimaryKeyTest;
import org.geotools.jdbc.JDBCPrimaryKeyTestSetup;

public class IngresPrimaryKeyTest extends JDBCPrimaryKeyTest {

	@Override
	protected JDBCPrimaryKeyTestSetup createTestSetup() {
		return new IngresPrimaryKeyTestSetup(new IngresTestSetup());
	}

}
