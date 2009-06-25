package org.geotools.coverage.io;

import java.io.IOException;
import java.util.Map;

import org.geotools.coverage.io.range.RangeManager;
import org.geotools.coverage.io.request.CoverageReadRequest;
import org.geotools.coverage.io.request.CoverageRequest;
import org.geotools.coverage.io.request.CoverageResponse;
import org.geotools.coverage.io.request.CoverageRequest.RequestType;
import org.geotools.data.Parameter;
import org.omg.CORBA.DomainManager;
import org.opengis.util.ProgressListener;
/**
 * A {@link RasterSlice} is a 2D element in space/time/elevation with a certain number of Bands.
 * It may also have its own pyramid.
 * 
 * @author Simone Giannecchini, GeoSolutions SAS
 *
 */
public interface RasterSlice {

	/**
	 * Retrieves a {@link RangeManager} instance which can be used to describe the
	 * codomain for the underlying coverage.
	 * 
	 * @param listener
	 * @return a {@link RangeManager} instance which can be used to describe the
	 * 			codomain for the underlying coverage.
	 * @throws IOException in case something bad occurs
	 */
	public RangeManager getRangeManager(final ProgressListener listener) throws IOException;

	public DomainManager getDomainManager(final ProgressListener listener) throws IOException;

	/**
	 * Obtain a {@link CoverageResponse} from this {@link RasterDataset} given a specified {@link DefaultCoverageRequest}.
	 * 
	 * @param request the input {@link DefaultCoverageRequest}.
	 * @param listener
	 * @return
	 * @throws IOException
	 */
	public <T extends CoverageRequest> CoverageResponse performRequest(final T request,final ProgressListener listener) throws IOException;

	/**
	 * Describes the required (and optional) parameters that
	 * can be passed to the {@link #update(CoverageReadRequest, ProgressListener)} method.
	 * <p>
	 * @return Param a {@link Map} describing the {@link Map} for {@link #update(CoverageReadRequest, ProgressListener)}.
	 */
	public Map<String, Parameter<?>> getDefaultParameterInfo(RequestType requestType);

	/**
	 * Closes this {@link RasterDataset} and releases any lock or cached information it holds.
	 * 
	 * <p>
	 * Once a {@link RasterStorage} has been disposed it can be seen as being in unspecified state, 
	 * hence calling a method on it may have unpredictable results.
	 */
	public void close();

}
