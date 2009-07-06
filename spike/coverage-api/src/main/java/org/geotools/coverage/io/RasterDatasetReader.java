package org.geotools.coverage.io;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.geotools.coverage.io.metadata.MetadataNode;
import org.geotools.coverage.io.service.RasterService;
import org.geotools.factory.Hints;
import org.opengis.feature.type.Name;
import org.opengis.geometry.Envelope;
import org.opengis.metadata.extent.Extent;
import org.opengis.util.ProgressListener;

public interface RasterDatasetReader extends RasterDatasetIO {

	/**
	 * Names of the available Coverages.
	 * <p>
	 * Each Coverage named here represents a unqiue data product that
	 * may be accessed via the RasterDataset
	 * 
	 * @return Names of the available contents.
	 * @throws IOException
	 */
	public List<Name> getRasterDatasetNames(final ProgressListener listener);

	/**
	 * The number of Coverages made available.
	 * 
	 * @param listener
	 * @return getNames( listener ).size()
	 */
	public int getRasterDatasetNumber(final ProgressListener listener);
	
	public int getRasterDatasetMinimumIndex(final ProgressListener listener);

	/**
	 * Returns the {@link RasterService} which has been used to connect to this
	 * RasterStorage.
	 * 
	 * @return {@link RasterService} used to connect
	 */
	public RasterService<? extends RasterDatasetReader> getRasterService();

	/**
	 * Retrieves the parameters used to connect to this live instance of
	 * {@link RasterStorage}.
	 * 
	 * 
	 * @return the parameters used to connect to this live instance of
	 *         {@link RasterStorage}.
	 */
	public Map<String, Serializable> getRasterServiceParameters();
	
	/**
	 * This method can be used to acquire a rough description of a coverage
	 * spatiotemporal domain.
	 * 
	 * @param coverageName
	 * @param listener
	 * @return {@link Envelope}
	 * 
	 * @todo consider geoapi {@link Extent} which can in turn wrap the Envelope
	 *       we are providing here In order to do so we need to resolve the
	 *       ambiguity of the 3D inseparable CRSs between vertical and horizontal domain.
	 */
	public Envelope getExtent(Name coverageName, final ProgressListener listener);

	public MetadataNode getMetadata(String metadataDomain);

	public Set<Name> getMetadataDomains();

	/**
	 * Retrieve a {@link RasterDataset} to access a Named Coverage.
	 * <p>
	 * This instance will be unmodifiable.
	 * 
	 * @param Name Indicate the coverage to access
	 * @param params Additional parameters as needed to indicate what part of the data set to access
	 * @param accessType Requested level of access 
	 * @param Hints Implementation specific hints; please review the javadocs for your RasterService for details
	 * @param listener used to report progress while obtianing access
	 */
	public RasterDataset acquire(Name name, Map<String, Serializable> params, Hints hints, ProgressListener listener)throws IOException;
	/**
	 * 	 * Retrieve a {@link RasterDataset} to access a Named Coverage.
	 * <p>
	 * This instance will be unmodifiable.
	 * 
	 * @param index
	 * @param params
	 * @param hints
	 * @param listener
	 * @return
	 * @throws IOException
	 */
	public RasterDataset acquire(int index, Map<String, Serializable> params, Hints hints, ProgressListener listener)throws IOException;

	public void setTargetLocation(URI location)throws IOException;

}
