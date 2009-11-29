package org.geotools.gce.imagemosaic;

import java.awt.Dimension;
import java.awt.image.RenderedImage;
import java.util.concurrent.Callable;

import javax.imageio.ImageReadParam;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.geometry.BoundingBox;
import org.opengis.referencing.operation.MathTransform2D;

/**
 * 
 * @author Simone Giannecchini, GeoSolutions SAS
 *
 */
class GranuleLoader implements Callable<RenderedImage>{

	final ReferencedEnvelope cropBBox;
	
	final MathTransform2D mosaicWorldToGrid;
	
	final Granule granule;
	
	final ImageReadParam readParameters;
	
	final int imageIndex;

	final Dimension tilesDimension;

	RasterLayerRequest request;
	
	GranuleLoader(
			final ImageReadParam readParameters, 
			final int imageIndex,
			final ReferencedEnvelope cropBBox, 
			final MathTransform2D mosaicWorldToGrid,
			final Granule granule,
			final RasterLayerRequest request) {
		this.readParameters = Utils.cloneImageReadParam(readParameters);
		this.imageIndex = imageIndex;
		this.cropBBox = cropBBox;
		this.mosaicWorldToGrid = mosaicWorldToGrid;
		this.granule = granule;
		this.request=request;
		this.tilesDimension= request.getTileDimensions()!=null?(Dimension) request.getTileDimensions().clone():null;
	}
	
	public BoundingBox getCropBBox() {
		return cropBBox;
	}

	public MathTransform2D getMosaicWorldToGrid() {
		return mosaicWorldToGrid;
	}

	public Granule getGranule() {
		return granule;
	}

	public ImageReadParam getReadParameters() {
		return readParameters;
	}

	public int getImageIndex() {
		return imageIndex;
	}
	
	public RenderedImage call() throws Exception {
		return granule.loadRaster(readParameters, imageIndex, cropBBox, mosaicWorldToGrid, request,tilesDimension);
	}

}