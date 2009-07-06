package org.geotools.coverage.io;

import java.util.Map;

import org.geotools.data.Parameter;
import org.geotools.data.ServiceInfo;
import org.opengis.util.ProgressListener;

public interface RasterDatasetIO {

	/**
	 * This will free any cached info object or header information.
	 * <p>
	 * Often a {@link RasterStorage} will keep a file channel open, this will
	 * clean that sort of thing up.
	 * 
	 * <p>
	 * Once a {@link RasterStorage} has been disposed it can be seen as being
	 * in unspecified state, hence calling a method on it may have unpredictable
	 * results.
	 * 
	 */
	public void close();

	public Map<String, Parameter<?>> getDefaultParameterInfo();

	/**
	 * Description of the RasterStorage we are connected to here.
	 * <p>
	 * @todo TODO think about the equivalence with StreamMetadata once we define
	 *       them
	 * 
	 * @return Description of the RasterStorage we are connected to here.
	 */
	public ServiceInfo getInfo(final ProgressListener listener);

}