package org.geotools.coverage.io.driver;

import java.util.List;

public interface FileBasedDriver extends Driver {

	/**
	 * The list of filename extensions handled by this driver.
	 * <p>
	 * This List may be empty if the Driver is not file based.
	 * <p>
	 * 
	 * @return List of file extensions which can be read by this dataStore.
	 */
	public List<String> getFileExtensions();

}
