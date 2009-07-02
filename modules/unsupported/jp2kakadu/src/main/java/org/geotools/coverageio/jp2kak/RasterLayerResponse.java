/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2007-2008, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.coverageio.jp2kak;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.media.jai.operator.ConstantDescriptor;

import org.geotools.coverage.GridSampleDimension;
import org.geotools.coverage.TypeMap;
import org.geotools.coverage.grid.GeneralGridEnvelope;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.coverage.grid.io.OverviewPolicy;
import org.geotools.coverageio.jp2kak.RasterManager.OverviewLevel;
import org.geotools.data.DataSourceException;
import org.geotools.factory.Hints;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.image.ImageWorker;
import org.geotools.referencing.CRS;
import org.geotools.referencing.operation.transform.AffineTransform2D;
import org.opengis.coverage.ColorInterpretation;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.geometry.BoundingBox;
import org.opengis.referencing.datum.PixelInCell;
import org.opengis.referencing.operation.MathTransform2D;
import org.opengis.referencing.operation.TransformException;

import com.sun.media.jai.codecimpl.util.ImagingException;
/**
 * A RasterLayerResponse. An instance of this class is produced everytime a
 * requestCoverage is called to a reader.
 * 
 * @author Daniele Romagnoli, GeoSolutions
 */
@SuppressWarnings("deprecation")
class RasterLayerResponse{
	
	/**
	 * 
	 * @author Simone Giannecchini, GeoSolutions SAS
	 *
	 */
	class GranuleLoader implements Callable<RenderedImage>{

		final ReferencedEnvelope cropBBox;
		
		final MathTransform2D worldToGrid;
		
		final Granule granule;
		
		final ImageReadParam readParameters;
		
		final int imageIndex;

		final Dimension tilesDimension;
		
		GranuleLoader(
				final ImageReadParam readParameters, 
				final int imageIndex,
				final ReferencedEnvelope cropBBox, 
				final MathTransform2D worldToGrid,
				final Granule granule,
				final Dimension tilesDimension) {
			super();
			this.readParameters = Utils.cloneImageReadParam(readParameters);
			this.imageIndex = imageIndex;
			this.cropBBox = cropBBox;
			this.worldToGrid = worldToGrid;
			this.granule = granule;
			this.tilesDimension= tilesDimension!=null?(Dimension) tilesDimension.clone():null;
		}
		
		public BoundingBox getCropBBox() {
			return cropBBox;
		}


		public MathTransform2D getWorldToGrid() {
			return worldToGrid;
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
			
			return granule.loadRaster(readParameters, imageIndex, cropBBox, worldToGrid, request,tilesDimension);
		}

	}
	
	class GranuleWorker {

		
		/**
		 * Default {@link Constructor}
		 */
		public GranuleWorker() {

		}

		private final List<Future<RenderedImage>> tasks= new ArrayList<Future<RenderedImage>>();
		private int   granulesNumber;
		private boolean doInputTransparency;
		private Color inputTransparentColor;

		public void init(final ReferencedEnvelope aoi) {

			// Get location and envelope of the image to load.
			final ReferencedEnvelope granuleBBox = aoi;
			

			// Load a granule from disk as requested.
			if (LOGGER.isLoggable(Level.FINE))
				LOGGER.fine("About to read image number " + granulesNumber);

			// If the granule is not there, dump a message and continue
			final File rasterFile = new File(location);
			if (rasterFile == null) {
				return;
			}
			if (LOGGER.isLoggable(Level.FINE))
				LOGGER.fine("File found "+ location);
			
			// granule cache
			Granule granule=null;
			synchronized (rasterManager.granulesCache) {
				if(rasterManager.granulesCache.containsKey(rasterFile.toURI().toString()))
				{
					granule=rasterManager.granulesCache.get(rasterFile.toURI().toString());
				}
				else
				{
					granule=new Granule(granuleBBox,rasterFile);
					rasterManager.granulesCache.put(rasterFile.toURI().toString(),granule);
				}
			}
			
			//
			// load raster data
			//
			//create a granule loader
			final GranuleLoader loader = new GranuleLoader(baseReadParameters, imageChoice, bbox, finalWorldToGridCorner,granule,request.getTileDimensions());
			if(!multithreadingAllowed)
				tasks.add(new FutureTask<RenderedImage>(loader));
			else
				tasks.add(JP2KReader.multiThreadedLoader.submit(loader));
			
			granulesNumber++;
		}
		
		
		public void produce(){
			
			// reusable parameters
			int granuleIndex=0;
			inputTransparentColor = request.getInputTransparentColor();
			doInputTransparency = inputTransparentColor != null;
			// execute them all
			boolean firstGranule=true;
			
			for (Future<RenderedImage> future :tasks) {
				final RenderedImage loadedImage;
				try {
					if(!multithreadingAllowed)
					{
						//run the loading in this thread
						final FutureTask<RenderedImage> task=(FutureTask<RenderedImage>) future;
						task.run();
					}
					loadedImage=future.get();
					if(loadedImage==null)
					{
						if(LOGGER.isLoggable(Level.FINE))
							LOGGER.log(Level.FINE,"Unable to load the raster for granule " +granuleIndex+ " with request "+request.toString());
						continue;
					}
					if(firstGranule){
						//
						// We check here if the images have an alpha channel or some
						// other sort of transparency. In case we have transparency
						// I also save the index of the transparent channel.
						//
						final ColorModel cm = loadedImage.getColorModel();
						alphaIn = cm.hasAlpha();

						//
						// we set the input threshold accordingly to the input
						// image data type. I find the default value (which is 0) very bad
						// for data type other than byte and ushort. With float and double
						// it can cut off a large par of the dynamic.
						//
//						if(!Double.isNaN(request.getThreshold()))
//							pbjMosaic.setParameter("sourceThreshold", new double[][]{{request.getThreshold()}});
//						else
//							pbjMosaic.setParameter("sourceThreshold",
//								new double[][] { { Utils.getThreshold(loadedImage.getSampleModel().getDataType()) } });
						
						firstGranule=false;
										
					}					
					
				} catch (InterruptedException e) {
					if(LOGGER.isLoggable(Level.SEVERE))
						LOGGER.log(Level.SEVERE,"Unable to load the raster for granule " +granuleIndex,e);
					continue;
				} catch (ExecutionException e) {
					if(LOGGER.isLoggable(Level.SEVERE))
						LOGGER.log(Level.SEVERE,"Unable to load the raster for granule " +granuleIndex,e);
					continue;
				}

				catch (ImagingException e) {
					if (LOGGER.isLoggable(Level.FINE))
						LOGGER.fine("Adding to mosaic image number " + granuleIndex+ " failed, original request was "+request);
					continue;
				}
				catch (javax.media.jai.util.ImagingException e) {
					if (LOGGER.isLoggable(Level.FINE))
						LOGGER.fine("Adding to mosaic image number " + granuleIndex+ " failed, original request was "+request);
					continue;
				}


				if (LOGGER.isLoggable(Level.FINE))
					LOGGER.fine("Adding to mosaic image number " + granuleIndex);
				
				//
				// add to the mosaic collection, with preprocessing
				//
				final RenderedImage raster = processGranuleRaster(
						loadedImage,
						granuleIndex, 
						alphaIn, 
						doInputTransparency,
						inputTransparentColor);
				
				theImage = raster;
				
				//increment index 
				granuleIndex++;
			}

			granulesNumber=granuleIndex;
			if(granulesNumber==0)
			{
				if(LOGGER.isLoggable(Level.FINE))
					LOGGER.log(Level.FINE,"Unable to load any data ");
				return;
			}

		}
		
	}

	/** Logger. */
	private final static Logger LOGGER = org.geotools.util.logging.Logging.getLogger(RasterLayerResponse.class);
	
	/**
	 * The GridCoverage produced after a {@link #compute()} method call
	 */
	private GridCoverage2D gridCoverage;

	/** The {@link RasterLayerRequest} originating this response */
	private RasterLayerRequest request;

	/** The coverage factory producing a {@link GridCoverage} from an image */
	private GridCoverageFactory coverageFactory;

	/** The base envelope related to the input coverage */
	private GeneralEnvelope coverageEnvelope;

	private URL inputURL;

	private boolean frozen = false;

	private RasterManager rasterManager;

	private Color transparentColor;

	private RenderedImage theImage;

	private ReferencedEnvelope bbox;

	private Rectangle rasterBounds;

	private MathTransform2D finalGridToWorldCorner;

	private MathTransform2D finalWorldToGridCorner;

	private int imageChoice=0;

	private ImageReadParam baseReadParameters= new ImageReadParam();

	private boolean multithreadingAllowed=false;
	
	private boolean alphaIn=false;

	private String location;

	/**
	 * Construct a {@code RasterLayerResponse} given a specific
	 * {@link RasterLayerRequest}, a {@code GridCoverageFactory} to produce
	 * {@code GridCoverage}s and an {@code ImageReaderSpi} to be used for
	 * instantiating an Image Reader for a read operation,
	 * 
	 * @param request
	 *            a {@link RasterLayerRequest} originating this response.
	 * @param coverageFactory
	 *            a {@code GridCoverageFactory} to produce a {@code
	 *            GridCoverage} when calling the {@link #compute()} method.
	 * @param readerSpi
	 *            the Image Reader Service provider interface.
	 */
	public RasterLayerResponse(final RasterLayerRequest request,
			final RasterManager rasterManager) {
		this.request = request;
		inputURL = rasterManager.getInputURL();
		File tempFile;
		try {
			tempFile = new File(this.inputURL.toURI());
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException(e);
		}// TODO improve me
		location = tempFile.getAbsolutePath();
		coverageEnvelope = rasterManager.getCoverageEnvelope();
		this.coverageFactory = rasterManager.getCoverageFactory();
		this.rasterManager = rasterManager;
		transparentColor=request.getInputTransparentColor();
//		finalTransparentColor=request.getOutputTransparentColor();
		// are we doing multithreading?
		multithreadingAllowed= request.isMultithreadingAllowed();

	}

	/**
	 * Compute the coverage request and produce a grid coverage which will be
	 * returned by {@link #createResponse()}. The produced grid coverage may be
	 * {@code null} in case of empty request.
	 * 
	 * @return the {@link GridCoverage} produced as computation of this response
	 *         using the {@link #compute()} method.
	 * @throws IOException
	 * @uml.property name="gridCoverage"
	 */
	public GridCoverage2D createResponse() throws IOException {
		processRequest();
		return gridCoverage;
	}

	/**
	 * @return the {@link RasterLayerRequest} originating this response.
	 * 
	 * @uml.property name="request"
	 */
	public RasterLayerRequest getOriginatingCoverageRequest() {
		return request;
	}

	/**
	 * This method creates the GridCoverage2D from the underlying file given a
	 * specified envelope, and a requested dimension.
	 * 
	 * @param iUseJAI
	 *            specify if the underlying read process should leverage on a
	 *            JAI ImageRead operation or a simple direct call to the {@code
	 *            read} method of a proper {@code ImageReader}.
	 * @param overviewPolicy
	 *            the overview policy which need to be adopted
	 * @return a {@code GridCoverage}
	 * 
	 * @throws java.io.IOException
	 */
	private  synchronized void processRequest() throws IOException {

		if (request.isEmpty())
			throw new IOException("Empty request " + request.toString());

		if (frozen)
			return;
		
		// assemble granules
		final RenderedImage image = assembleGranules();
		
//		RenderedImage finalRaster = postProcessRaster(image);
		
		//create the coverage
		gridCoverage=prepareCoverage(image);
		
		//freeze
		frozen = true;
		
	}

	/**
	 * This method loads the granules which overlap the requested
	 * {@link GeneralEnvelope} using the provided values for alpha and input
	 * ROI.
	 */
	private RenderedImage assembleGranules() throws DataSourceException {

		try {

			
			//
			// prepare the params for executing a mosaic operation.
			//
//			final double[] backgroundValues = request.getBackgroundValues();

			// select the relevant overview, notice that at this time we have
			// relaxed a bit the requirement to have the same exact resolution
			// for all the overviews, but still we do not allow for reading the
			// various grid to world transform directly from the input files,
			// therefore we are assuming that each granule has a scale and
			// translate only grid to world that can be deduced from its base
			// level dimension and envelope. The grid to world transforms for
			// the other levels can be computed accordingly knowning the scale
			// factors.
			if (request.getRequestedBBox() != null&& request.getRequestedRasterArea() != null)
				imageChoice = setReadParams(request.getOverviewPolicy(), baseReadParameters,request);
			else
				imageChoice = 0;
			assert imageChoice>=0;
			if (LOGGER.isLoggable(Level.FINE))
				LOGGER.fine(new StringBuffer("Loading level ").append(
						imageChoice).append(" with subsampling factors ")
						.append(baseReadParameters.getSourceXSubsampling()).append(" ")
						.append(baseReadParameters.getSourceYSubsampling()).toString());			
			
			
			final BoundingBox cropBBOX = request.getCropBBox();
			if (cropBBOX != null)
				bbox = ReferencedEnvelope.reference(cropBBOX);
			else
				bbox = new ReferencedEnvelope(coverageEnvelope);
			
			
			//compute final world to grid
			OverviewLevel level = rasterManager.overviewsController.resolutionsLevels.get(imageChoice);
			finalGridToWorldCorner = new AffineTransform2D(
					level.resolutionX*baseReadParameters.getSourceXSubsampling(),
					0,
					0,
					-level.resolutionY*baseReadParameters.getSourceYSubsampling(),
					bbox.getLowerCorner().getOrdinate(0),
					bbox.getUpperCorner().getOrdinate(1));
			finalWorldToGridCorner = finalGridToWorldCorner.inverse();// compute raster bounds
			rasterBounds=new GeneralGridEnvelope(CRS.transform(finalWorldToGridCorner, bbox),PixelInCell.CELL_CORNER,false).toRectangle();
			
			
			// create the index visitor and visit the feature
			final GranuleWorker worker = new GranuleWorker();
			worker.init(bbox);
			worker.produce();
			
			//
			// Did we actually load anything?? Notice that it might happen that
			// either we have wholes inside the definition area for the mosaic
			// or we had some problem with missing tiles, therefore it might
			// happen that for some bboxes we don't have anything to load.
			//
			if (worker.granulesNumber>=1) {

				//
				// Create the mosaic image by doing a crop if necessary and also
				// managing the transparent color if applicable. Be aware that
				// management of the transparent color involves removing
				// transparency information from the input images.
				// 
				if (LOGGER.isLoggable(Level.FINE))
					LOGGER.fine(new StringBuilder("Loaded bbox ").append(
							bbox.toString()).append(" while crop bbox ")
							.append(request.getCropBBox().toString())
							.toString());
				
				
				return theImage;				
			
			}
			else{
				// if we get here that means that we do not have anything to load
				// but still we are inside the definition area for the mosaic,
				// therefore we create a fake coverage using the background values,
				// if provided (defaulting to 0), as well as the compute raster
				// bounds, envelope and grid to world.
				
	
//				if (backgroundValues == null)
					
					//we don't have background values available
					return ConstantDescriptor.create(
									Float.valueOf(rasterBounds.width), 
									Float.valueOf(rasterBounds.height),
									new Byte[] { 0 },
									this.rasterManager.getHints());
//				else {
//					
//					//we have background values available
//					final Double[] values = new Double[backgroundValues.length];
//					for (int i = 0; i < values.length; i++)
//						values[i] = backgroundValues[i];
//					return ConstantDescriptor.create(
//									Float.valueOf(rasterBounds.width), 
//									Float.valueOf(rasterBounds.height),
//									values, 
//									this.rasterManager.getHints());
//				}
			}

		} catch (IOException e) {
			throw new DataSourceException("Unable to create this mosaic", e);
		} catch (TransformException e) {
			throw new DataSourceException("Unable to create this mosaic", e);
		} 
	}

	private RenderedImage processGranuleRaster(
			RenderedImage granule, 
			final int granuleIndex, 
			final boolean alphaIn,
			final boolean doTransparentColor, final Color transparentColor) {

		//
		// INDEX COLOR MODEL EXPANSION
		//
		// Take into account the need for an expansions of the original color
		// model.
		//
		// If the original color model is an index color model an expansion
		// might be requested in case the different palettes are not all the
		// same. In this case the mosaic operator from JAI would provide wrong
		// results since it would take the first palette and use that one for
		// all the other images.
		//
		// There is a special case to take into account here. In case the input
		// images use an IndexColorModel it might happen that the transparent
		// color is present in some of them while it is not present in some
		// others. This case is the case where for sure a color expansion is
		// needed. However we have to take into account that during the masking
		// phase the images where the requested transparent color was present
		// will have 4 bands, the other 3. If we want the mosaic to work we
		// have to add an extra band to the latter type of images for providing
		// alpha information to them.
		//
		//
		if (rasterManager.expandMe && granule.getColorModel() instanceof IndexColorModel) {
			granule = new ImageWorker(granule).forceComponentColorModel().getRenderedImage();
		}

		//
		// TRANSPARENT COLOR MANAGEMENT
		//
		//
		if (doTransparentColor) {
			if (LOGGER.isLoggable(Level.FINE))
				LOGGER.fine("Support for alpha on input image number "+ granuleIndex);
			granule = Utils.makeColorTransparent(transparentColor, granule);
		}
		return granule;

	}

	private GridCoverage2D prepareCoverage(
			RenderedImage image) throws IOException {
		// creating bands
        final SampleModel sm=image.getSampleModel();
        final ColorModel cm=image.getColorModel();
		final int numBands = sm.getNumBands();
		final GridSampleDimension[] bands = new GridSampleDimension[numBands];
		// setting bands names.
		for (int i = 0; i < numBands; i++) {
		        final ColorInterpretation colorInterpretation=TypeMap.getColorInterpretation(cm, i);
		        if(colorInterpretation==null)
		               throw new IOException("Unrecognized sample dimension type");
			bands[i] = new GridSampleDimension(colorInterpretation.name()).geophysics(true);
		}

        return coverageFactory.create(rasterManager.getCoverageIdentifier(), image,new GeneralEnvelope(bbox), bands, null, null);		

	}

	/**
	 * This method is responsible for preparing the read param for doing an
	 * {@link ImageReader#read(int, ImageReadParam)}.
	 * 
	 * 
	 * <p>
	 * This method is responsible for preparing the read param for doing an
	 * {@link ImageReader#read(int, ImageReadParam)}. It sets the passed
	 * {@link ImageReadParam} in terms of decimation on reading using the
	 * provided requestedEnvelope and requestedDim to evaluate the needed
	 * resolution. It also returns and {@link Integer} representing the index of
	 * the raster to be read when dealing with multipage raster.
	 * 
	 * @param overviewPolicy
	 *            it can be one of {@link Hints#VALUE_OVERVIEW_POLICY_IGNORE},
	 *            {@link Hints#VALUE_OVERVIEW_POLICY_NEAREST},
	 *            {@link Hints#VALUE_OVERVIEW_POLICY_QUALITY} or
	 *            {@link Hints#VALUE_OVERVIEW_POLICY_SPEED}. It specifies the
	 *            policy to compute the overviews level upon request.
	 * @param readParams
	 *            an instance of {@link ImageReadParam} for setting the
	 *            subsampling factors.
	 * @param requestedEnvelope
	 *            the {@link GeneralEnvelope} we are requesting.
	 * @param requestedDim
	 *            the requested dimensions.
	 * @return the index of the raster to read in the underlying data source.
	 * @throws IOException
	 * @throws TransformException
	 */
	private int setReadParams(final OverviewPolicy overviewPolicy,
			final ImageReadParam readParams, final RasterLayerRequest request)
			throws IOException, TransformException {

		// Default image index 0
		int imageChoice = 0;
		// default values for subsampling
		readParams.setSourceSubsampling(1, 1, 0, 0);

		//
		// Init overview policy
		//
		// //
		// when policy is explictly provided it overrides the policy provided
		// using hints.
		final OverviewPolicy policy;
		if (overviewPolicy == null)
			policy = rasterManager.overviewPolicy;
		else
			policy = overviewPolicy;


		// requested to ignore overviews
		if (policy.equals(OverviewPolicy.IGNORE))
			return imageChoice;

		// overviews and decimation
		imageChoice = rasterManager.overviewsController.pickOverviewLevel(overviewPolicy,request);

		// DECIMATION ON READING
		rasterManager.decimationController.performDecimation(imageChoice,readParams, request);
		return imageChoice;
	}
}
