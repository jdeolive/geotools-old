package org.geotools.coverage.io.service;

import java.util.List;

public interface FileBasedRasterService<T> extends RasterService<T> {

	/**
	 * The list of filename extensions handled by this driver.
	 * <p>
	 * This List may be empty if the RasterService is not file based.
	 * <p>
	 * 
	 * @return List of file extensions which can be read by this dataStore.
	 */
	public List<String> getFileExtensions();

}
