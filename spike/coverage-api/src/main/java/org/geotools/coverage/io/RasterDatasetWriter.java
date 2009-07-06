package org.geotools.coverage.io;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

import org.geotools.coverage.io.metadata.MetadataNode;
import org.geotools.coverage.io.service.RasterService;
import org.geotools.data.DataAccessFactory.Param;
import org.opengis.feature.type.Name;
import org.opengis.util.ProgressListener;

public interface RasterDatasetWriter extends RasterDatasetReader{

	enum Action{
		DELETE,
		UPDATE_DATA,
		UPDATE_METADATA,
		UPDATE,
		INSERT,
		INSERT_EMPTY,
		REMOVE;
	}

	public void setMetadata(String metadataDomain, MetadataNode root);

	/**
	 * Returns the {@link RasterService} which has been used to connect to this
	 * RasterStorage.
	 * 
	 * @return {@link RasterService} used to connect
	 */
	public RasterService<? extends RasterDatasetWriter> getRasterService();

	public boolean isActionSupported(Action action);

	public Map<String, Param> getDefaultParameters(Action action);
	
	public boolean remove(Name rasterDatasetName, Map<String, Serializable> params)throws IOException;
	public boolean remove(int index, Map<String, Serializable> params)throws IOException;	
	/**
	 * Delete entirely the underlying storage
	 * 
	 * @param params
	 * @param progress
	 * @return
	 * @throws IOException
	 */
	public boolean delete( Map<String, Serializable> params, ProgressListener progress)throws IOException;
	
	public boolean insert(int index, Map<String, Serializable> params, RasterDataset rasterDataset,ProgressListener progress)throws IOException;
	/**
	 * Perform an append to the underlying storage.
	 * 
	 * @param params
	 * @param rasterDataset
	 * @param progress
	 * @return
	 * @throws IOException
	 */
	public boolean insert( Map<String, Serializable> params, RasterDataset rasterDataset,ProgressListener progress)throws IOException;
	public RasterDataset insertEmpty(int index, Map<String, Serializable> params, ProgressListener progress)throws IOException;
	public RasterDataset insertEmpty( Map<String, Serializable> params, ProgressListener progress)throws IOException;
	
	public boolean updateMetadata(Name rasterDatasetName, Map<String, Serializable> params, Map<String,MetadataNode> metadataDomains,ProgressListener progress)throws IOException;	
	public boolean updateData(Name rasterDatasetName, Map<String, Serializable> params, RasterDataset rasterDataset,ProgressListener progress)throws IOException;
	public boolean update(Name rasterDatasetName, Map<String, Serializable> params, RasterDataset rasterDataset,ProgressListener progress)throws IOException;

}
