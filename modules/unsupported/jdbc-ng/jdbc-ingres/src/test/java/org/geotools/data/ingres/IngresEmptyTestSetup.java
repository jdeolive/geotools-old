package org.geotools.data.ingres;

import org.geotools.jdbc.JDBCEmptyTestSetup;
import org.geotools.jdbc.JDBCTestSetup;

public class IngresEmptyTestSetup extends JDBCEmptyTestSetup {

	public IngresEmptyTestSetup(JDBCTestSetup delegate) {
		super(delegate);
	}

	@Override
	protected void createEmptyTable() throws Exception {

	}

	@Override
	protected void dropEmptyTable() throws Exception {

	}

}
