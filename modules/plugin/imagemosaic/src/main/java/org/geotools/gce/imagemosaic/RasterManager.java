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
package org.geotools.gce.imagemosaic;

import java.awt.Rectangle;
import java.awt.image.ColorModel;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.io.File;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageReadParam;
import javax.media.jai.ImageLayout;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.coverage.grid.GridEnvelope2D;
import org.geotools.coverage.grid.io.DecimationPolicy;
import org.geotools.coverage.grid.io.OverviewPolicy;
import org.geotools.data.DataSourceException;
import org.geotools.data.DataUtilities;
import org.geotools.data.Query;
import org.geotools.factory.Hints;
import org.geotools.gce.imagemosaic.catalog.GranuleCatalog;
import org.geotools.gce.imagemosaic.catalog.GranuleCatalog.GranuleCatalogVisitor;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.util.Utilities;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.geometry.BoundingBox;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.datum.PixelInCell;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.MathTransform2D;
import org.opengis.referencing.operation.TransformException;
class RasterManager {
	/** Logger. */
	private final static Logger LOGGER = org.geotools.util.logging.Logging.getLogger(RasterManager.class);
	
	/**
	 * Simple support class for sorting overview resolutions
	 * @author Andrea Aime
	 * @author Simone Giannecchini, GeoSolutions.
	 * @since 2.5
	 */
	static class OverviewLevel implements Comparable<OverviewLevel> {
		
		double scaleFactor;

	    double resolutionX;
	    double resolutionY;
	    int imageChoice;
	    
        public OverviewLevel(
        		final double scaleFactor,
        		final double resolutionX,
        		final double resolutionY,
        		int imageChoice) {
            this.scaleFactor = scaleFactor;
            this.resolutionX=resolutionX;
            this.resolutionY=resolutionY;
            this.imageChoice = imageChoice;
        }
	    
	    public int compareTo(final OverviewLevel other) {
	        if(scaleFactor > other.scaleFactor)
	            return 1;
	        else if(scaleFactor < other.scaleFactor)
	            return -1;
	        else 
	        	return 0;
	    }
	    
	    @Override
	    public String toString() {
	        return "OverviewLevel[Choice=" + imageChoice + ",scaleFactor=" + scaleFactor + "]";
	    }
	    

		@Override
		public int hashCode() {
			int hash= Utilities.hash(imageChoice, 31);
			hash=Utilities.hash(resolutionX, hash);
			hash=Utilities.hash(resolutionY, hash);
			hash=Utilities.hash(scaleFactor, hash);
			return hash;
		}	    
	    
	    
	}
	
	static class OverviewsController  {
		final ArrayList<org.geotools.gce.imagemosaic.RasterManager.OverviewLevel> resolutionsLevels = new ArrayList<OverviewLevel>();
	
		public OverviewsController(
        		final double []highestRes,
        		final int numberOfOvervies,
        		final double [][] overviewsResolution) {
				
			// notice that we assume what follows:
			// -highest resolution image is at level 0.
			// -all the overviews share the same envelope
			// -the aspect ratio for the overviews is constant
			// -the provided resolutions are taken directly from the grid
			resolutionsLevels.add(new OverviewLevel(1, highestRes[0],highestRes[1], 0));
			if (numberOfOvervies > 0) {
				for (int i = 0; i < overviewsResolution.length; i++)
					resolutionsLevels.add(
								new OverviewLevel(
												overviewsResolution[i][0] / highestRes[0],
												overviewsResolution[i][0],
												overviewsResolution[i][1], i + 1)
					);
				Collections.sort(resolutionsLevels);
			}
		}

		int pickOverviewLevel(final OverviewPolicy policy,final RasterLayerRequest request) {
    
			// //
			//
			// If this file has only
			// one page we use decimation, otherwise we use the best page available.
			// Future versions should use both.
			//
			// //
			if (resolutionsLevels==null||resolutionsLevels.size() <=0) 
				return 0;
			
			// Now search for the best matching resolution. 
	        // Check also for the "perfect match"... unlikely in practice unless someone
	        // tunes the clients to request exactly the resolution embedded in
	        // the overviews, something a perf sensitive person might do in fact
	        
            
	        // requested scale factor for least reduced axis
	        final OverviewLevel max = (OverviewLevel) resolutionsLevels.get(0);
	        
	        // the requested resolutions
			final double requestedScaleFactorX;
			final double requestedScaleFactorY;
			final double[] requestedRes = request.getRequestedResolution();
			if (requestedRes != null)
			{	
		        final double reqx = requestedRes[0];
		        final double reqy = requestedRes[1];
		        
				requestedScaleFactorX = reqx / max.resolutionX;
				requestedScaleFactorY = reqy / max.resolutionY;		        
			}
			else
				return 0;
//			{
//				final double[] scaleFactors = request.getRequestedRasterScaleFactors();
//				if(scaleFactors==null)
//					return 0;
//				requestedScaleFactorX=scaleFactors[0];
//				requestedScaleFactorY=scaleFactors[1];
//			}
			final int leastReduceAxis = requestedScaleFactorX <= requestedScaleFactorY ? 0: 1;
			final double requestedScaleFactor = leastReduceAxis == 0 ? requestedScaleFactorX: requestedScaleFactorY;
	        
	        
			// are we looking for a resolution even higher than the native one?
	        if(requestedScaleFactor<=1)
	            return max.imageChoice;
	        // are we looking for a resolution even lower than the smallest overview?
	        final OverviewLevel min = (OverviewLevel) resolutionsLevels.get(resolutionsLevels.size() - 1);
	        if(requestedScaleFactor>=min.scaleFactor)
	            return min.imageChoice;
	        // Ok, so we know the overview is between min and max, skip the first
	        // and search for an overview with a resolution lower than the one requested,
	        // that one and the one from the previous step will bound the searched resolution
	        OverviewLevel prev = max;
	        final int size=resolutionsLevels.size();
	        for (int i = 1; i <size; i++) {
	            final OverviewLevel curr = resolutionsLevels.get(i);
	            // perfect match check
	            if(curr.scaleFactor==requestedScaleFactor) {
	                return curr.imageChoice;
	            }
	            
	            // middle check. The first part of the condition should be sufficient, but
	            // there are cases where the x resolution is satisfied by the lowest resolution, 
	            // the y by the one before the lowest (so the aspect ratio of the request is 
	            // different than the one of the overviews), and we would end up going out of the loop
	            // since not even the lowest can "top" the request for one axis 
	            if(curr.scaleFactor>requestedScaleFactor|| i == size - 1) {
	                if(policy ==OverviewPolicy.QUALITY)
	                    return prev.imageChoice;
	                else if(policy == OverviewPolicy.SPEED)
	                    return curr.imageChoice;
	                else if(requestedScaleFactor - prev.scaleFactor < curr.scaleFactor - requestedScaleFactor)
	                    return prev.imageChoice;
	                else
	                    return curr.imageChoice;
	            }
	            prev = curr;
	        }
	        //fallback
	        return max.imageChoice;
	    }
		
	}
	/**
	 * 
	 * @author Simone Giannecchini, GeoSolutions S.A.S.
	 *
	 */
	static class  DecimationController  {
		
		public DecimationController() {

		}

		/**
		 * This method is responsible for evaluating possible subsampling factors
		 * once the best resolution level has been found, in case we have support
		 * for overviews, or starting from the original coverage in case there are
		 * no overviews available.
		 * 
		 * Anyhow this method should not be called directly but subclasses should
		 * make use of the setReadParams method instead in order to transparently
		 * look for overviews.
		 * 
		 * @param imageIndex
		 * @param readParameters
		 * @param requestedRes
		 */
		void performDecimation(
				final int imageIndex,
				final ImageReadParam readParameters, 
				final RasterLayerRequest request,
				final OverviewsController overviewsController,
				final SpatialDomainManager spatialDomainManager) {
			{
		
				// the read parameters cannot be null
				Utilities.ensureNonNull("readParameters", readParameters);
				Utilities.ensureNonNull("request", request);
				
				//get the requested resolution
				final double[] requestedRes=request.getRequestedResolution();
				if(requestedRes==null)
				{
					// if there is no requested resolution we don't do any subsampling
					readParameters.setSourceSubsampling(1, 1, 0, 0);
					return;
				}

				double selectedRes[] = new double[2];
				final OverviewLevel level=overviewsController.resolutionsLevels.get(imageIndex);
				selectedRes[0] = level.resolutionX;
				selectedRes[1] = level.resolutionY;
				
				final int rasterWidth, rasterHeight;
				if (imageIndex == 0) {
					// highest resolution
					rasterWidth = spatialDomainManager.coverageRasterArea.width;
					rasterHeight = spatialDomainManager.coverageRasterArea.height;
				} else {
					// work on overviews
					//TODO this is bad side effect of how the Overviews are managed right now. There are two problems here,
					// first we are assuming that we are working with LON/LAT, second is that we are getting just an approximation of 
					// raster dimensions. The solution is to have the rater dimensions on each level and to confront raster dimensions,
					//which means working
					rasterWidth = (int) Math.round(spatialDomainManager.coverageBBox.getSpan(0)/ selectedRes[0]);
					rasterHeight = (int) Math.round(spatialDomainManager.coverageBBox.getSpan(1)/ selectedRes[1]);
		
				}
				// /////////////////////////////////////////////////////////////////////
				// DECIMATION ON READING
				// Setting subsampling factors with some checks
				// 1) the subsampling factors cannot be zero
				// 2) the subsampling factors cannot be such that the w or h are
				// zero
				// /////////////////////////////////////////////////////////////////////
				int subSamplingFactorX = (int) Math.floor(requestedRes[0]/ selectedRes[0]);
				subSamplingFactorX = subSamplingFactorX == 0 ? 1: subSamplingFactorX;
	
				while (rasterWidth / subSamplingFactorX <= 0 && subSamplingFactorX >= 0)
					subSamplingFactorX--;
				subSamplingFactorX = subSamplingFactorX <= 0 ? 1: subSamplingFactorX;
	
				int subSamplingFactorY = (int) Math.floor(requestedRes[1]/ selectedRes[1]);
				subSamplingFactorY = subSamplingFactorY == 0 ? 1: subSamplingFactorY;
	
				while (rasterHeight / subSamplingFactorY <= 0 && subSamplingFactorY >= 0)subSamplingFactorY--;
				subSamplingFactorY = subSamplingFactorY <= 0 ? 1: subSamplingFactorY;
	
				readParameters.setSourceSubsampling(subSamplingFactorX,subSamplingFactorY, 0, 0);
				
		
			}
		}
		
	}

	/**
	 * This class is responsible for putting together all the 2D spatial information needed for a certain raster.
	 * 
	 * <p>
	 * Notice that when this structure will be extended to work in ND this will become much more complex or as an 
	 * alternative a sibling TemporalDomainManager will be created.
	 * 
	 * @author Simone Giannecchini, GeoSolutions SAS
	 *
	 */
	static class SpatialDomainManager{

		/** The base envelope 2D */
		ReferencedEnvelope coverageBBox;
		/** The CRS for the coverage */
		CoordinateReferenceSystem coverageCRS;
		/** The CRS related to the base envelope 2D */
		CoordinateReferenceSystem coverageCRS2D;
		// ////////////////////////////////////////////////////////////////////////
		//
		// Base coverage properties
		//
		// ////////////////////////////////////////////////////////////////////////
		/** The base envelope read from file */
		GeneralEnvelope coverageEnvelope = null;
		double[] coverageFullResolution;
		/** WGS84 envelope 2D for this coverage */
		ReferencedEnvelope coverageGeographicBBox;
		CoordinateReferenceSystem coverageGeographicCRS2D;
		MathTransform2D coverageGridToWorld2D;
		/** The base grid range for the coverage */
		 Rectangle coverageRasterArea;
		 

		public SpatialDomainManager(final GeneralEnvelope envelope,
				final GridEnvelope2D coverageGridrange,
				final CoordinateReferenceSystem crs,
				final MathTransform coverageGridToWorld2D,
				final OverviewsController overviewsController) throws TransformException, FactoryException {
		    this.coverageEnvelope = envelope.clone();
		    this.coverageRasterArea =coverageGridrange.clone();
		    this.coverageCRS = crs;
		    this.coverageGridToWorld2D = (MathTransform2D) coverageGridToWorld2D;
		    this.coverageFullResolution = new double[2];
		    final OverviewLevel highestLevel= overviewsController.resolutionsLevels.get(0);
		    coverageFullResolution[0] = highestLevel.resolutionX;
		    coverageFullResolution[1] = highestLevel.resolutionY;
		    
			prepareCoverageSpatialElements();
		}
			
			
        /**
         * Initialize the 2D properties (CRS and Envelope) of this coverage
         * 
         * @throws TransformException
         * 
         * @throws FactoryException
         * @throws TransformException
         * @throws FactoryException
         */
        private void prepareCoverageSpatialElements() throws TransformException, FactoryException {
            //
            // basic initialization
            //
            coverageGeographicBBox = Utils.getWGS84ReferencedEnvelope(coverageEnvelope);
            coverageGeographicCRS2D = coverageGeographicBBox==null?coverageGeographicBBox.getCoordinateReferenceSystem():null;

            //
            // Get the original envelope 2d and its spatial reference system
            //
            coverageCRS2D = CRS.getHorizontalCRS(coverageCRS);
            assert coverageCRS2D.getCoordinateSystem().getDimension() == 2;
            if (coverageCRS.getCoordinateSystem().getDimension() != 2) {
                final MathTransform transform = CRS.findMathTransform(coverageCRS,
                        (CoordinateReferenceSystem) coverageCRS2D);
                final GeneralEnvelope bbox = CRS.transform(transform, coverageEnvelope);
                bbox.setCoordinateReferenceSystem(coverageCRS2D);
                coverageBBox = new ReferencedEnvelope(bbox);
            } else {
                // it is already a bbox
                coverageBBox = new ReferencedEnvelope(coverageEnvelope);
            }

        }

    }
	
	/** Default {@link ColorModel}.*/
	ColorModel defaultCM;
	
	/** Default {@link SampleModel}.*/
	SampleModel defaultSM;
	
	/** The coverage factory producing a {@link GridCoverage} from an image */
	private GridCoverageFactory coverageFactory;

	/** The name of the input coverage 
	 * TODO consider URI
	 */
	private String coverageIdentifier;

	
	/** The hints to be used to produce this coverage */
	private Hints hints;
	OverviewsController overviewsController;
	OverviewPolicy overviewPolicy;
	DecimationPolicy decimationPolicy;
	DecimationController decimationController;
	ImageMosaicReader parent;
	private PathType pathType;
	boolean expandMe;
	SpatialDomainManager spatialDomainManager;

	/** {@link SoftReference} to the index holding the tiles' envelopes. */
	final GranuleCatalog index;

	String timeAttribute;
	
	String elevationAttribute;
	
	String runtimeAttribute;

	ImageLayout defaultImageLayout;

	public RasterManager(final ImageMosaicReader reader) throws DataSourceException {
		
		Utilities.ensureNonNull("ImageMosaicReader", reader);
		
		this.parent=reader;
		this.expandMe=parent.expandMe;
        
        //take ownership of the index
		index= parent.catalog;
		parent.catalog=null;
		
        timeAttribute=parent.timeAttribute;
        elevationAttribute=parent.elevationAttribute;
        runtimeAttribute=parent.runtimeAttribute;
        coverageIdentifier=reader.getName();
        hints = reader.getHints();
        this.coverageIdentifier =reader.getName();
        this.coverageFactory = reader.getGridCoverageFactory();
        this.pathType=parent.pathType;
        
        //resolution values
        
        //instantiating controller for subsampling and overviews
        overviewsController=new OverviewsController(
        		reader.getHighestRes(),
        		reader.getNumberOfOvervies(),
        		reader.getOverviewsResolution());
        decimationController= new DecimationController();
        try {
			spatialDomainManager= new SpatialDomainManager(
					reader.getOriginalEnvelope(),
					(GridEnvelope2D)reader.getOriginalGridRange(),
					reader.getCrs(),
					reader.getOriginalGridToWorld(PixelInCell.CELL_CENTER),
					overviewsController);
		} catch (TransformException e) {
			throw new DataSourceException(e);
		} catch (FactoryException e) {
			throw new DataSourceException(e);
		}
        extractOverviewPolicy();
        extractDecimationPolicy();
        
        // load defaultSM and defaultCM by using the sample_image if it was provided
        loadSampleImage();        
		
	}

 	/**
	 * This code tries to load the sample image from which we can extract SM and CM to use when answering to requests
	 * that falls within a hole in the mosaic.
	 */
	private void loadSampleImage() {
	    if (this.parent.sourceURL == null) {
	        //TODO: I need to define the sampleImage somehow for the ImageMosaicDescriptor case
	        return;
	    }
		
			final URL baseURL=this.parent.sourceURL;
			final File baseFile= DataUtilities.urlToFile(baseURL);
			// in case we do not manage to convert the source URL we leave right awaycd sr
			if (baseFile==null){
				if(LOGGER.isLoggable(Level.FINE))
					LOGGER.fine("Unable to find sample image for path "+baseURL);
				return;
			}
			final File sampleImageFile= new File(baseFile.getParent() + "/sample_image");			
			final RenderedImage sampleImage = Utils.loadSampleImage(sampleImageFile);
			if(sampleImage!=null){
				
				// load SM and CM
				defaultCM= sampleImage.getColorModel();
				defaultSM= sampleImage.getSampleModel();
				
				// default ImageLayout
				defaultImageLayout= new ImageLayout().setColorModel(defaultCM).setSampleModel(defaultSM);
			}
			else
				if(LOGGER.isLoggable(Level.WARNING))
					LOGGER.warning("Unable to find sample image for path "+baseURL);
	}

	/**
	 * This method is responsible for checking the overview policy as defined by
	 * the provided {@link Hints}.
	 * 
	 * @return the overview policy which can be one of
	 *         {@link OverviewPolicy#IGNORE},
	 *         {@link OverviewPolicy#NEAREST},
	 *         {@link OverviewPolicy#SPEED}, {@link OverviewPolicy#QUALITY}.
	 *         Default is {@link OverviewPolicy#NEAREST}.
	 */
	private OverviewPolicy extractOverviewPolicy() {
		
		// check if a policy was provided using hints (check even the
		// deprecated one)
		if (this.hints != null)
			if (this.hints.containsKey(Hints.OVERVIEW_POLICY))
				overviewPolicy = (OverviewPolicy) this.hints.get(Hints.OVERVIEW_POLICY);
	
		// use default if not provided. Default is nearest
		if (overviewPolicy == null) {
			overviewPolicy = OverviewPolicy.getDefaultPolicy();
		}
		assert overviewPolicy != null;
		return overviewPolicy;
	}
	
	/**
         * This method is responsible for checking the decimation policy as defined by
         * the provided {@link Hints}.
         * 
         * @return the decimation policy which can be one of
         *         {@link DecimationPolicy#ALLOW},
         *         {@link DecimationPolicy#DISALLOW}.
         *         Default is {@link DecimationPolicy#ALLOW}.
         */
	private DecimationPolicy extractDecimationPolicy() {
            if (this.hints != null)
                if (this.hints.containsKey(Hints.DECIMATION_POLICY))
                    decimationPolicy = (DecimationPolicy) this.hints.get(Hints.DECIMATION_POLICY);
    
            // use default if not provided. Default is allow
            if (decimationPolicy == null) {
                decimationPolicy = DecimationPolicy.getDefaultPolicy();
            }
            assert decimationPolicy != null;
            return decimationPolicy;

        }

	public Collection<GridCoverage2D> read(final GeneralParameterValue[] params) throws IOException {

		// create a request
		final RasterLayerRequest request= new RasterLayerRequest(params,this);
		if (request.isEmpty()){
			if(LOGGER.isLoggable(Level.FINE))
				LOGGER.log(Level.FINE,"Request is empty: "+ request.toString());
			return Collections.emptyList();		
		}
		
		// create a response for the provided request
		final RasterLayerResponse response= new RasterLayerResponse(request,this);
		
		// execute the request
		final GridCoverage2D elem = response.createResponse();
		if (elem != null){
			return Collections.singletonList(elem);
		}
		return Collections.emptyList();
		
		
	}
	
	public void dispose() {
		
	}

	/**
	 * Retrieves the list of features that intersect the provided envelope
	 * loading them inside an index in memory where needed.
	 * 
	 * @param envelope
	 *            Envelope for selecting features that intersect.
	 * @return A list of features.
	 * @throws IOException
	 *             In case loading the needed features failes.
	 */
	Collection<GranuleDescriptor> getGranules(final BoundingBox envelope)throws IOException {
		final Collection<GranuleDescriptor> granules = index.getGranules(envelope);
		if (granules != null)
			return granules;
		else
			return Collections.emptyList();
	}
	
	Collection<GranuleDescriptor> getGranules(final Query q)throws IOException {
		final Collection<GranuleDescriptor> granules = index.getGranules(q);
		if (granules != null)
			return granules;
		else
			return Collections.emptyList();
	}
	
	void getGranules(final Query q,final GranuleCatalogVisitor visitor)throws IOException {
		index.getGranules(q,visitor);

	}

	/**
	 * Retrieves the list of features that intersect the provided envelope
	 * loading them inside an index in memory where needed.
	 * 
	 * @param envelope
	 *            Envelope for selecting features that intersect.
	 * @return A list of features.
	 * @throws IOException
	 *             In case loading the needed features failes.
	 */
	void getGranules(final BoundingBox envelope,final GranuleCatalogVisitor visitor)throws IOException {
		index.getGranules(envelope,visitor);
	}

	public PathType getPathType() {
		return pathType;
	}


	public String getCoverageIdentifier() {
		return coverageIdentifier;
	}

	
	public Hints getHints() {
		return hints;
	}

	public GridCoverageFactory getCoverageFactory() {
		return coverageFactory;
	}

}
