/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2007, GeoTools Project Management Committee (PMC)
 *    (C) 2007, GeoSolutions S.A.S.
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
package org.geotools.coverageio.gdal;

import it.geosolutions.imageio.gdalframework.GDALCommonIIOImageMetadata;
import it.geosolutions.imageio.imageioimpl.imagereadmt.DefaultCloneableImageReadParam;
import it.geosolutions.imageio.imageioimpl.imagereadmt.ImageReadDescriptorMT;
import it.geosolutions.imageio.stream.input.FileImageInputStreamExt;

import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.renderable.ParameterBlock;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageReaderSpi;
import javax.media.jai.IHSColorSpace;
import javax.media.jai.ImageLayout;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;

import org.geotools.coverage.CoverageFactoryFinder;
import org.geotools.coverage.GridSampleDimension;
import org.geotools.coverage.grid.GeneralGridRange;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.OverviewPolicy;
import org.geotools.data.DataSourceException;
import org.geotools.data.DefaultResourceInfo;
import org.geotools.data.DefaultServiceInfo;
import org.geotools.data.PrjFileReader;
import org.geotools.data.ResourceInfo;
import org.geotools.data.ServiceInfo;
import org.geotools.data.WorldFileReader;
import org.geotools.factory.Hints;
import org.geotools.geometry.Envelope2D;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.metadata.iso.spatial.PixelTranslation;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.referencing.operation.builder.GridToEnvelopeMapper;
import org.geotools.referencing.operation.transform.ConcatenatedTransform;
import org.geotools.referencing.operation.transform.IdentityTransform;
import org.geotools.referencing.operation.transform.ProjectiveTransform;
import org.geotools.resources.coverage.CoverageUtilities;
import org.geotools.resources.geometry.XRectangle2D;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.coverage.grid.GridCoverageReader;
import org.opengis.coverage.grid.GridRange;
import org.opengis.geometry.Envelope;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValue;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.datum.PixelInCell;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.MathTransform2D;
import org.opengis.referencing.operation.TransformException;

/**
 * Base class for GridCoverage data access, leveraging on GDAL Java bindings
 * provided by the ImageIO-Ext project. See <a
 * href="http://imageio-ext.dev.java.net">ImageIO-Ext project</a>.
 * 
 * @author Daniele Romagnoli, GeoSolutions
 * @author Simone Giannecchini, GeoSolutions
 * 
 */
public abstract class BaseGDALGridCoverage2DReader extends
		AbstractGridCoverage2DReader implements GridCoverageReader {

	/** Logger. */
	private final static Logger LOGGER = org.geotools.util.logging.Logging
			.getLogger("org.geotools.coverageio.gdal");

	/** registering ImageReadMT JAI operation (for multithreaded ImageRead) */
	static {
		ImageReadDescriptorMT.register(JAI.getDefaultInstance());
	}

	// ////////////////////////////////////////////////////////////////////////
	//    
	// Constant fields
	//    
	// ////////////////////////////////////////////////////////////////////////
	/** The default world file extension */
	private final static String DEFAULT_WORLDFILE_EXT = ".wld";

	/** The system-dependent default name-separator character. */
	private final static char SEPARATOR = File.separatorChar;

	/**
	 * The format specific world file extension. As an instance, world file
	 * related to ecw datasets have .eww extension while mrsid world file are
	 * .sdw
	 */
	private final String worldFileExt;

	/** Caches an {@code ImageReaderSpi}. */
	private final ImageReaderSpi readerSPI;

	// ////////////////////////////////////////////////////////////////////////
	//  
	// Referencing related fields (CRS and Envelope)
	//  
	// ////////////////////////////////////////////////////////////////////////
	/** WGS84 envelope for this coverage */
	private Envelope2D wgs84BaseEnvelope2D;

	/** Envelope read from file */
	protected GeneralEnvelope baseEnvelope = null;

	/** The base envelope 2D */
	private Envelope2D baseEnvelope2D;

	/** The CRS related to the base envelope 2D */
	private CoordinateReferenceSystem spatialReferenceSystem2D;

	/** The CRS for the coverage */
	protected CoordinateReferenceSystem coverageCRS = null;

	// ////////////////////////////////////////////////////////////////////////
	//  
	// Data source properties and field for management
	//  
	// ////////////////////////////////////////////////////////////////////////

	/** Source given as input to the reader */
	protected File inputFile = null;

	/** Coverage name */
	protected String coverageName = "geotools_coverage";

	/**
	 * The base {@link GridRange} for the {@link GridCoverage2D} of this reader.
	 */
	protected GeneralGridRange baseGridRange = null;

	/** Absolute path to the parent dir for this coverage. */
	private String parentPath;

	/**
	 * Specify if the underlying ImageIO plugin supports additional metadata. In
	 * such a case it could be possible of retrieving important properties from
	 * the specialized version of metadata, using proper method such as, as an
	 * instance,
	 * {@link BaseGDALGridCoverage2DReader#getPropertiesFromSpecificMetadata} to
	 * get the original envelope.
	 */
	private boolean isSupportingAdditionalMetadata;

	/**
	 * Creates a new instance of a {@link BaseGDALGridCoverage2DReader}. I
	 * assume nothing about file extension.
	 * 
	 * @param input
	 *            Source object for which we want to build a
	 *            {@link BaseGDALGridCoverage2DReader}.
	 * @param hints
	 *            Hints to be used by this reader throughout his life.
	 * @param isSupportingAdditionalMetadata
	 *            specifies if the underlying format support additional metadata
	 * @param worldFileExtension
	 *            the specific world file extension of the underlying format
	 * @param formatSpecificSpi
	 *            an instance of a proper {@code ImageReaderSpi}.
	 * @throws DataSourceException
	 */
	protected BaseGDALGridCoverage2DReader(Object input, final Hints hints,
			boolean isSupportingAdditionalMetadata,
			final String worldFileExtension,
			final ImageReaderSpi formatSpecificSpi) throws DataSourceException {

		this.isSupportingAdditionalMetadata = isSupportingAdditionalMetadata;
		// /////////////////////////////////////////////////////////////////////
		//
		// Checking input
		//
		// /////////////////////////////////////////////////////////////////////
		try {
			// //
			//
			// managing hints
			//
			// //
			if (this.hints == null) {
				this.hints = new Hints();
			}

			if (hints != null) {
				this.hints.add(hints);
			}

			this.coverageFactory = CoverageFactoryFinder
					.getGridCoverageFactory(this.hints);

			readerSPI = formatSpecificSpi;
			worldFileExt = worldFileExtension;

			// //
			//
			// Source management
			//
			// //
			checkSource(input);

			// Getting a reader for this format
			final ImageReader reader = readerSPI.createReaderInstance();
			// reader.setInput(inStream);
			reader.setInput(inputFile);

			// //
			//
			// Setting Envelope, GridRange and CRS
			//
			// //
			setOriginalProperties(reader);

			// //
			//
			// Information about multiple levels and such
			//
			// //
			getResolutionInfo(reader);
			reader.reset();
			reader.dispose();
		} catch (IOException e) {
			if (LOGGER.isLoggable(Level.SEVERE)) {
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			}

			throw new DataSourceException(e);
		} catch (TransformException e) {
			if (LOGGER.isLoggable(Level.SEVERE)) {
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			}

			throw new DataSourceException(e);
		}
	}

	/**
	 * Setting Envelope, GridRange and CRS from the given {@code ImageReader}
	 * 
	 * @param reader
	 *            the {@code ImageReader} from which to retrieve metadata (if
	 *            available) for setting properties
	 * @throws IOException
	 */
	private void setOriginalProperties(ImageReader reader) throws IOException {
		// //
		//
		// Getting common metadata from GDAL
		//
		// //
		final IIOMetadata metadata = reader.getImageMetadata(0);

		if (!(metadata instanceof GDALCommonIIOImageMetadata)) {
			throw new DataSourceException(
					"Unexpected error! Metadata should be an instance of the expected class:"
							+ " GDALCommonIIOImageMetadata.");
		}

		getPropertiesFromCommonMetadata((GDALCommonIIOImageMetadata) metadata);

		// //
		//
		// If common metadata doesn't have sufficient information to set CRS
		// envelope, try other ways, such as looking for a PRJ
		//
		// //
		if (coverageCRS == null) {
			getCoordinateReferenceSystemFromPrj();
		}

		if (((baseEnvelope == null) || (coverageCRS == null))
				&& isSupportingAdditionalMetadata) {
			getPropertiesFromSpecificMetadata(metadata);
		}

		if (coverageCRS == null) {
			LOGGER.info("crs not found, proceeding with EPSG:4326");
			coverageCRS = AbstractGridFormat.getDefaultCRS();
		}

		// //
		//
		// If no sufficient information have been found to set the
		// envelope, try other ways, such as looking for a WorldFile
		//
		// //
		if (baseEnvelope == null) {
			checkForWorldFile();

			if (baseEnvelope == null) {
				throw new DataSourceException(
						"Unavailable envelope for this coverage");
			}
		}

		// setting the coordinate reference system for the envelope
		baseEnvelope.setCoordinateReferenceSystem(coverageCRS);

		// Additional settings due to "final" methods getOriginalXXX
		originalEnvelope = baseEnvelope;
		originalGridRange = baseGridRange;
		crs = coverageCRS;
	}

	/**
	 * Get additional properties from format specific metadata object.
	 */
	protected abstract void getPropertiesFromSpecificMetadata(
			IIOMetadata metadata);

	/**
	 * Given a {@link GDALCommonIIOImageMetadata} metadata object, retrieves
	 * several properties to properly set envelope, gridrange and crs.
	 * 
	 * @param metadata
	 *            a {@link GDALCommonIIOImageMetadata} metadata instance from
	 *            where to search needed properties.
	 */
	private void getPropertiesFromCommonMetadata(
			GDALCommonIIOImageMetadata metadata) {
		// ////////////////////////////////////////////////////////////////////
		//
		// setting CRS and Envelope directly from GDAL, if available
		//
		// ////////////////////////////////////////////////////////////////////
		// //
		// 
		// 1) CRS
		//
		// //
		final String wkt = metadata.getProjection();

		if ((wkt != null) && !(wkt.equalsIgnoreCase(""))) {
			try {
				coverageCRS = CRS.parseWKT(wkt);
				final Integer epsgCode = CRS.lookupEpsgCode(coverageCRS, true);

				// Force the creation of the CRS directly from the retrieved
				// EPSG code in order to prevent weird transformation between
				// "same" CRSs having slight differences.
				// TODO: cache epsgCode-CRSs
				if (epsgCode != null) {
					coverageCRS = CRS.decode("EPSG:" + epsgCode);
				}
			} catch (FactoryException fe) {
				// unable to get CRS from WKT
				if (LOGGER.isLoggable(Level.FINE)) {
					// LOGGER.log(Level.WARNING, fe.getLocalizedMessage(), fe);
					LOGGER.log(Level.FINE,
							"Unable to get CRS from WKT contained in metadata."
									+ " Looking for a PRJ.");
				}

				coverageCRS = null;
			}
		}

		// //
		//
		// 2) Grid
		//
		// //
		baseGridRange = new GeneralGridRange(new Rectangle(0, 0, metadata
				.getWidth(), metadata.getHeight()));

		// //
		// 
		// 3) Envelope
		//
		// //
		final double[] geoTransform = metadata.getGeoTransformation();

		if ((geoTransform != null) && (geoTransform.length == 6)) {
			final AffineTransform tempTransform = new AffineTransform(
					geoTransform[1], geoTransform[4], geoTransform[2],
					geoTransform[5], geoTransform[0], geoTransform[3]);
			// ATTENTION: Gdal geotransform does not use the pixel is centre
			// convention like world files.



			try {
				// Envelope setting
				baseEnvelope = CRS.transform(ProjectiveTransform.create(tempTransform), new GeneralEnvelope(
						baseGridRange.toRectangle()));
			} catch (IllegalStateException e) {
				if (LOGGER.isLoggable(Level.WARNING)) {
					LOGGER.log(Level.WARNING, e.getLocalizedMessage(), e);
				}
			} catch (TransformException e) {
				if (LOGGER.isLoggable(Level.WARNING)) {
					LOGGER.log(Level.WARNING, e.getLocalizedMessage(), e);
				}
			}
			
			// Grid2World Transformation
			final double tr= PixelTranslation.getPixelTranslation(PixelInCell.CELL_CORNER);
			tempTransform.translate(tr, tr);
			this.raster2Model = ProjectiveTransform.create(tempTransform);
		}
	}

	/**
	 * Checks the input provided to this {@link BaseGDALGridCoverage2DReader}
	 * and sets all the other objects and flags accordingly.
	 * 
	 * @param input
	 *            provided to this {@link BaseGDALGridCoverage2DReader}.
	 *            Actually supported input types for the underlying ImageIO-Ext
	 *            GDAL framework are: {@code File}, {@code URL} pointing to a
	 *            file and {@link FileImageInputStreamExt}
	 * @param hints
	 *            Hints to be used by this reader throughout his life.
	 * 
	 * @throws UnsupportedEncodingException
	 * @throws DataSourceException
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	private void checkSource(Object input) throws UnsupportedEncodingException,
			DataSourceException, IOException, FileNotFoundException {
		if (input == null) {
			final DataSourceException ex = new DataSourceException(
					"No source set to read this coverage.");

			if (LOGGER.isLoggable(Level.SEVERE)) {
				LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
			}

			throw ex;
		}

		this.source = null;

		// //
		//
		// URL to FIle
		//
		// //
		// if it is a URL pointing to a File I convert it to a file.
		if (input instanceof URL) {
			// URL that point to a file
			final URL sourceURL = ((URL) input);
			this.source = sourceURL;

			if (sourceURL.getProtocol().compareToIgnoreCase("file") == 0) {
				this.inputFile = new File(URLDecoder.decode(
						sourceURL.getFile(), "UTF-8"));
				input = this.inputFile;
			} else {
				throw new IllegalArgumentException("Unsupported input type");
			}
		}

		if (input instanceof FileImageInputStreamExt) {
			if (source == null) {
				source = input;
			}

			// inStream = (ImageInputStream) input;
			inputFile = ((FileImageInputStreamExt) input).getFile();
			input = inputFile;
		}

		// //
		//        
		// File
		//        
		// //
		if (input instanceof File) {
			final File sourceFile = (File) input;

			if (source == null) {
				source = sourceFile;
			}

			if (inputFile == null) {
				inputFile = sourceFile;
			}

			if (!sourceFile.exists() || sourceFile.isDirectory()
					|| !sourceFile.canRead()) {
				throw new DataSourceException(
						"Provided file does not exist or is a directory or is not readable!");
			}

			this.parentPath = sourceFile.getParent();
			coverageName = sourceFile.getName();

			final int dotIndex = coverageName.indexOf(".");
			coverageName = (dotIndex == -1) ? coverageName : coverageName
					.substring(0, dotIndex);

		} else {
			throw new IllegalArgumentException("Unsupported input type");
		}
	}

	/**
	 * Gets resolution information about the coverage itself.
	 * 
	 * @param reader
	 *            an {@link ImageReader} to use for getting the resolution
	 *            information.
	 * @throws IOException
	 * @throws TransformException
	 * @throws DataSourceException
	 */
	private void getResolutionInfo(ImageReader reader) throws IOException,
			TransformException {
		// //
		//
		// get the dimension of the hr image and build the model as well as
		// computing the resolution
		//
		// //
		final Rectangle originalDim = new Rectangle(0, 0, reader.getWidth(0),
				reader.getHeight(0));

		if (baseGridRange == null) {
			baseGridRange = new GeneralGridRange(originalDim);
		}

		// ///
		//
		// setting the higher resolution available for this coverage
		//
		// ///
		highestRes = CoverageUtilities
				.getResolution((AffineTransform) raster2Model);
	}

	/**
	 * Reads a {@link GridCoverage2D} possibly matching as close as possible the
	 * resolution computed by using the input params provided by using the
	 * parameters for this {@link #read(GeneralParameterValue[])}.
	 * 
	 * <p>
	 * To have an idea about the possible read parameters take a look at
	 * {@link AbstractGridFormat} class as well as the
	 * {@link BaseGDALGridFormat}.
	 * 
	 * @param params
	 *            an array of {@link GeneralParameterValue} containing the
	 *            parameters to control this read process.
	 * 
	 * @return a {@link GridCoverage2D}.
	 * 
	 * @see AbstractGridFormat
	 * @see org.opengis.coverage.grid.GridCoverageReader#read(org.opengis.parameter.GeneralParameterValue[])
	 */
	public GridCoverage read(GeneralParameterValue[] params)
			throws IllegalArgumentException, IOException {
		GeneralEnvelope readEnvelope = null;
		OverviewPolicy overviewPolicy = null;
		Rectangle requestedDim = null;
		MathTransform2D readGridToWorld=null;
		// USE JAI ImageRead: -1==no, 0==unset 1==yes
		int iUseJAI = 0;

		// Use Multithreading
		boolean useMultithreading = false;

		// ////////////////////////////////////////////////////////////////////
		//
		// Parsing parameters
		//
		// ////////////////////////////////////////////////////////////////////
		if (params != null) {
			final int length = params.length;

			for (int i = 0; i < length; i++) {
				final ParameterValue param = (ParameterValue) params[i];
				final String name = param.getDescriptor().getName().getCode();

				// //
				//
				// GridGeometry2D parameter
				//
				// //
				if (name.equals(AbstractGridFormat.READ_GRIDGEOMETRY2D
						.getName().toString())) {
					final GridGeometry2D gg = (GridGeometry2D) param.getValue();

					if (gg == null) {
						continue;
					}

					readGridToWorld = gg.getGridToCRS2D();
					readEnvelope = new GeneralEnvelope((Envelope) gg
							.getEnvelope2D());
					requestedDim = gg.getGridRange2D().getBounds();

					if (LOGGER.isLoggable(Level.FINE)) {
						LOGGER.log(Level.FINE, "requested dim: ("
								+ requestedDim.width + ","
								+ requestedDim.height + ")");
					}
					continue;
				}

				// //
				//
				// Use JAI ImageRead parameter
				//
				// //
				if (name.equalsIgnoreCase(AbstractGridFormat.USE_JAI_IMAGEREAD
						.getName().toString())) {
					iUseJAI = param.booleanValue() ? 1 : (-1);
					continue;
				}

				// //
				//
				// Use Multithreading parameter
				//
				// //
				if (name.equalsIgnoreCase(BaseGDALGridFormat.USE_MULTITHREADING
						.getName().toString())) {
					useMultithreading = param.booleanValue();
					continue;
				}

				// //
				//
				// Overview Policy parameter
				//
				// //
				if (name.equals(AbstractGridFormat.OVERVIEW_POLICY.getName()
						.toString())) {
					overviewPolicy = (OverviewPolicy) param.getValue();
					continue;
				}

				// //
				//
				// Suggested tile size parameter. It must be specified with the
				// syntax:
				// "TileWidth,TileHeight" (without quotes where TileWidth and
				// TileHeight are integer values)
				//
				// //
				if (name
						.equalsIgnoreCase(BaseGDALGridFormat.SUGGESTED_TILE_SIZE
								.getName().toString())) {
					final String suggestedTileSize = (String) param.getValue();

					// Preliminary checks on parameter value
					if ((suggestedTileSize != null)
							&& (suggestedTileSize.trim().length() > 0)) {
						boolean valid = false;

						if (suggestedTileSize
								.contains(BaseGDALGridFormat.TILE_SIZE_SEPARATOR)) {
							final String[] tilesSize = suggestedTileSize
									.split(BaseGDALGridFormat.TILE_SIZE_SEPARATOR);

							if (tilesSize.length == 2) {
								try {
									// Getting suggested tile size
									final int tileWidth = Integer
											.parseInt(tilesSize[0]);
									final int tileHeight = Integer
											.parseInt(tilesSize[1]);
									final ImageLayout layout = new ImageLayout();
									layout.setTileGridXOffset(0)
											.setTileGridYOffset(0)
											.setTileHeight(tileHeight)
											.setTileWidth(tileWidth);

									// Adding the image layout to the hints
									hints.add(new RenderingHints(
											JAI.KEY_IMAGE_LAYOUT, layout));
									valid = true;
								} catch (NumberFormatException nfe) {
									// Does nothing since the valid flag is set
									// to false
								}
							}
						}

						if ((valid == false)
								&& LOGGER.isLoggable(Level.WARNING)) {
							LOGGER.log(Level.WARNING,
									"Suggested Tile Size is invalid");
						}
					}

					continue;
				}
			}
		}

		return createCoverage(readEnvelope, requestedDim,readGridToWorld, iUseJAI,
				useMultithreading, overviewPolicy);
	}

	/**
	 * This method creates the GridCoverage2D from the underlying file given a
	 * specified envelope, and a requested dimension.
	 * 
	 * @param requestedEnvelope
	 *            the requested envelope
	 * @param requestedDim
	 *            the requested dimension
	 * @param readGridToWorld 
	 * @param iUseJAI
	 *            specify if the underlying read process should leverage on a
	 *            JAI ImageRead operation or a simple direct call to the
	 *            {@code read} method of a proper {@code ImageReader}.
	 * @param useMultithreading
	 *            specify if the underlying read process should use
	 *            multithreading when a JAI ImageRead operation is requested
	 * @param overviewPolicy
	 *            the overview policy which need to be adopted
	 * @return a {@code GridCoverage}
	 * 
	 * @throws java.io.IOException
	 */
	private GridCoverage createCoverage(
			GeneralEnvelope requestedEnvelope,
			Rectangle requestedDim, 
			MathTransform2D readGridToWorld, final int iUseJAI,
			final boolean useMultithreading, 
			OverviewPolicy overviewPolicy)
			throws IOException {

		// /////////////////////////////////////////////////////////////////////
		//
		// Check if we have something to load by intersecting the requested
		// envelope with the bounds of the data set.
		//
		// /////////////////////////////////////////////////////////////////////
		final Rectangle sourceRegion = new Rectangle();
		final GeneralEnvelope adjustedRequestedEnvelope = evaluateRequestedParams(
				requestedEnvelope, sourceRegion,requestedDim,readGridToWorld);

		// Return null in case requested envelope does not intersect with the
		// coverage
		// envelope.
		if (adjustedRequestedEnvelope == null)
			return null;

		Object input = null;

		if (source instanceof FileImageInputStreamExt) {
			input = source;
		} else if (source instanceof File) {
			input = ImageIO.createImageInputStream(inputFile);
		} else if (source instanceof URL) {
			input = ImageIO.createImageInputStream((URL) source);
		} else {
			throw new IllegalArgumentException(
					"The input source is of invalid type");
		}

		// ////////////////////////////////////////////////////////////////////
		//
		// Doing an image read for reading the coverage.
		//
		// ////////////////////////////////////////////////////////////////////

		// //
		//
		// Setting subsampling factors with some checks
		// 1) the subsampling factors cannot be zero
		// 2) the subsampling factors cannot be such that the w or h are zero
		//
		// //
		final ImageReadParam readP;
		if (useMultithreading) {
			readP = new DefaultCloneableImageReadParam();
		} else {
			readP = new ImageReadParam();
		}

		try {
			final GeneralEnvelope req = (adjustedRequestedEnvelope.isEmpty()) ? requestedEnvelope
					: adjustedRequestedEnvelope;
			setReadParameters(overviewPolicy, readP, req, requestedDim);
		} catch (IOException e) {
			if (LOGGER.isLoggable(Level.SEVERE)) {
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			}

			return null;
		} catch (TransformException e) {
			if (LOGGER.isLoggable(Level.SEVERE)) {
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			}

			return null;
		}

		final PlanarImage coverage;
		if ((sourceRegion != null) && !sourceRegion.isEmpty()) {
			readP.setSourceRegion(sourceRegion);
			System.out.println("sourceRegion:"+sourceRegion);

		}
		System.out.println("sx:"+readP.getSourceXSubsampling());
		System.out.println("sy:"+readP.getSourceYSubsampling());
		// //
		//
		// image and metadata
		//
		// //
		boolean useJAI = (iUseJAI != 0) ? (iUseJAI > 0) : true;
		if ((iUseJAI == 0) && (this.hints != null)) {
			Object o = this.hints.get(Hints.USE_JAI_IMAGEREAD);

			if (o != null) {
				useJAI = ((Boolean) o).booleanValue();
			}
		}

		// Check on the type of Image Read to be performed
		if (useJAI) {
			final ParameterBlock pbjImageRead = new ParameterBlock();
			pbjImageRead.add(input);
			pbjImageRead.add(0);
			pbjImageRead.add(Boolean.FALSE);
			pbjImageRead.add(Boolean.FALSE);
			pbjImageRead.add(Boolean.FALSE);
			pbjImageRead.add(null);
			pbjImageRead.add(null);
			pbjImageRead.add(readP);
			pbjImageRead.add(readerSPI.createReaderInstance());

			if (LOGGER.isLoggable(Level.FINE)) {
				LOGGER.log(Level.FINE, "Using a JAI ImageRead operation");
			}

			// Check if to use a simple JAI ImageRead operation or a
			// multithreaded one
			final String jaiOperation = useMultithreading ? "ImageReadMT"
					: "ImageRead";
			coverage = JAI.create(jaiOperation, pbjImageRead, hints);
		} else {
			final ImageReader reader = readerSPI.createReaderInstance();
			reader.setInput(input, true, true);
			coverage = PlanarImage.wrapRenderedImage(reader.read(0,
					readP));
			reader.dispose();

			if (LOGGER.isLoggable(Level.FINE)) {
				LOGGER.log(Level.FINE, "Using a direct read");
			}
		}

		// /////////////////////////////////////////////////////////////////////
		//
		// Creating the coverage
		//
		// /////////////////////////////////////////////////////////////////////
		if ((adjustedRequestedEnvelope != null)
				&& !adjustedRequestedEnvelope.isEmpty()) {
			// I need to calculate a new transformation (raster2Model)
			// between the cropped image and the required
			// adjustedRequestEnvelope
			final int ssWidth = coverage.getWidth();
			final int ssHeight = coverage.getHeight();
			if (LOGGER.isLoggable(Level.FINE)) {
				LOGGER.log(Level.FINE, "Coverage read: width = " + ssWidth
						+ " height = " + ssHeight);
			}

			// //
			//
			// setting new coefficients to define a new affineTransformation
			// to be applied to the grid to world transformation
			// -----------------------------------------------------------------------------------
			//
			// With respect to the original envelope, the obtained planarImage
			// needs to be rescaled and translated. The scaling factors are
			// computed as the ratio
			// between the cropped source region sizes and the read image sizes.
			// The translate
			// settings are represented by the offsets of the source region.
			//
			// //
			final double scaleX = sourceRegion.width / (1.0 * ssWidth);
			final double scaleY = sourceRegion.height / (1.0 * ssHeight);
			final double translateX = sourceRegion.x;
			final double translateY = sourceRegion.y;
			return createCoverageFromImage(coverage, ConcatenatedTransform
					.create(ProjectiveTransform.create(new AffineTransform(
							scaleX, 0, 0, scaleY, translateX, translateY)),
							raster2Model));
		} else {
			// In case of no adjustedRequestEnvelope (As an instance, when
			// reading the whole image), I can use the transformation. So,
			// no need to specify a raster2model parameter
			return createCoverageFromImage(coverage);
		}
	}



	/**
	 * Evaluates the requested envelope and builds a new adjusted version of it
	 * fitting this coverage envelope.
	 * 
	 * <p>
	 * While adjusting the requested envelope this methods also compute the
	 * source region as a rectangle which is suitable for a successive read
	 * operation with {@link ImageIO} to do crop-on-read.
	 * 
	 * 
	 * @param requestedEnvelope
	 *            is the envelope we are requested to load.
	 * @param sourceRegion
	 *            represents the area to load in raster space. This parameter
	 *            cannot be null since it gets filled with whatever the crop
	 *            region is depending on the <code>requestedEnvelope</code>.
	 * @param requestedDim 
	 * @param readGridToWorld 
	 * @return the adjusted requested envelope, empty if no requestedEnvelope
	 *         has been specified, {@code null} in case the requested envelope
	 *         does not intersect the coverage envelope.
	 * 
	 * @throws DataSourceException
	 *             in case something bad occurs
	 */
	private GeneralEnvelope evaluateRequestedParams(
			GeneralEnvelope requestedEnvelope, 
			Rectangle sourceRegion, 
			Rectangle requestedDim, 
			MathTransform2D readGridToWorld)
			throws DataSourceException {
		GeneralEnvelope adjustedRequestedEnvelope = new GeneralEnvelope(2);

		try {
			// /////////////////////////////////////////////////////////////////////
			//
			// Check if we have something to load by intersecting the requested
			// envelope with the bounds of this data set.
			//
			// /////////////////////////////////////////////////////////////////////
			if (requestedEnvelope != null) {
				initBaseEnvelopes2D();
				final GeneralEnvelope requestedEnvelope2D = getRequestedEnvelope2D(requestedEnvelope);

				// /////////////////////////////////////////////////////////////////////
				//
				// INTERSECT ENVELOPES AND  CROP Destination REGION
				//
				// /////////////////////////////////////////////////////////////////////
				adjustedRequestedEnvelope = getIntersection(requestedEnvelope2D,requestedDim,readGridToWorld);
				if (adjustedRequestedEnvelope == null)
					return null;

				// /////////////////////////////////////////////////////////////////////
				//
				// CROP SOURCE REGION
				//
				// /////////////////////////////////////////////////////////////////////
				sourceRegion.setRect(getCropRegion(adjustedRequestedEnvelope));
				if (!sourceRegion.intersects(this.baseGridRange.toRectangle())
						|| sourceRegion.isEmpty()) 
					throw new DataSourceException("The crop region is invalid.");
				sourceRegion.setRect(sourceRegion
						.intersection(this.baseGridRange.toRectangle()));

	
				
				
				
				
				
				if (LOGGER.isLoggable(Level.FINE)) {
					LOGGER.log(Level.FINE, "Base Envelope = "
							+ baseEnvelope.toString());
					LOGGER.log(Level.FINE, "Adjusted Envelope = "
							+ adjustedRequestedEnvelope.toString());
					LOGGER.log(Level.FINE, "Base Grid Range = "
							+ baseGridRange.toRectangle().toString());
					LOGGER.log(Level.FINE, "Source Region = "
							+ sourceRegion.toString());
				}
			} else {
				// don't use the source region. Set an empty one
				sourceRegion.setBounds(new Rectangle(0, 0, Integer.MIN_VALUE,
						Integer.MIN_VALUE));
			}
		} catch (TransformException e) {
			throw new DataSourceException(
					"Unable to create a coverage for this source", e);
		} catch (FactoryException e) {
			throw new DataSourceException(
					"Unable to create a coverage for this source", e);
		}
		return adjustedRequestedEnvelope;
	}

	/**
	 * Return a 2D version of a requestedEnvelope
	 * 
	 * @param requestedEnvelope
	 *            the {@code GeneralEnvelope} to be returned as 2D.
	 * @return the 2D requested envelope
	 * @throws FactoryException
	 * @throws TransformException
	 */
	private GeneralEnvelope getRequestedEnvelope2D(
			GeneralEnvelope requestedEnvelope) throws FactoryException,
			TransformException {
		GeneralEnvelope requestedEnvelope2D = null;
		final MathTransform transformTo2D;
		CoordinateReferenceSystem requestedEnvelopeCRS2D = requestedEnvelope
				.getCoordinateReferenceSystem();
		if (requestedEnvelopeCRS2D.getCoordinateSystem().getDimension() != 2) {
			transformTo2D = CRS.findMathTransform(requestedEnvelopeCRS2D, CRS
					.getHorizontalCRS(requestedEnvelopeCRS2D));
			requestedEnvelopeCRS2D = CRS
					.getHorizontalCRS(requestedEnvelopeCRS2D);
		} else {
			transformTo2D = IdentityTransform.create(2);
		}

		if (!transformTo2D.isIdentity()) {
			requestedEnvelope2D = CRS.transform(transformTo2D,
					requestedEnvelope);
			requestedEnvelope2D
					.setCoordinateReferenceSystem(requestedEnvelopeCRS2D);
		} else {
			requestedEnvelope2D = new GeneralEnvelope(requestedEnvelope);
		}
		assert requestedEnvelopeCRS2D.getCoordinateSystem().getDimension() == 2;
		return requestedEnvelope2D;
	}

	/**
	 * Returns the intersection between the base envelope and the requested
	 * envelope.
	 * 
	 * @param requestedEnvelope2D
	 *            the requested 2D envelope to be intersected with the base
	 *            envelope.
	 * @param readGridToWorld 
	 * @param requestedDim 
	 * @return the resulting intersection of envelopes. In case of empty
	 *         intersection, this method is allowed to return {@code null}
	 * @throws TransformException
	 * @throws FactoryException
	 */
	private GeneralEnvelope getIntersection(
			GeneralEnvelope requestedEnvelope2D,
			Rectangle requestedDim, 
			MathTransform2D readGridToWorld)
			throws TransformException, FactoryException {

		GeneralEnvelope adjustedRequestedEnvelope =  new GeneralEnvelope(2);
		final CoordinateReferenceSystem requestedEnvelopeCRS2D = requestedEnvelope2D
				.getCoordinateReferenceSystem();
		boolean tryWithWGS84 = false;

		try {
			// convert the requested envelope 2D to this coverage native
			// crs.
			MathTransform transform=null;
			if (!CRS.equalsIgnoreMetadata(requestedEnvelopeCRS2D,
					this.spatialReferenceSystem2D)) 
				transform = CRS
						.findMathTransform(requestedEnvelopeCRS2D,
								this.spatialReferenceSystem2D, true);
			//now transform the requested envelope to source crs
			if(transform!=null&&!transform.isIdentity())
				adjustedRequestedEnvelope = CRS.transform(transform,
						requestedEnvelope2D);
			 else {
				adjustedRequestedEnvelope.setEnvelope(requestedEnvelope2D);
			}
			
			// intersect the requested area with the bounds of this
			// layer in native crs
			if (!adjustedRequestedEnvelope.intersects(baseEnvelope2D, true))
				return null;
			adjustedRequestedEnvelope.intersect(this.baseEnvelope2D);
			adjustedRequestedEnvelope
					.setCoordinateReferenceSystem(this.spatialReferenceSystem2D);
			
			////
			//
			//transform the intersection envelope from the destination world space to the requested raster space
			//
			////
			final Envelope requestedEnvelopeCropped= (transform!=null&&!transform.isIdentity())?CRS.transform(transform.inverse(),
					adjustedRequestedEnvelope):adjustedRequestedEnvelope;
			//TODO half pixel
			final Rectangle2D ordinates = CRS.transform(
					 readGridToWorld.inverse(),
					 requestedEnvelopeCropped).toRectangle2D();
			final GeneralGridRange finalRange = new GeneralGridRange(ordinates
					.getBounds());
			final Rectangle tempRect=finalRange.toRectangle();
			//check that we stay inside the source rectangle
			XRectangle2D.intersect(tempRect, requestedDim,tempRect);
			requestedDim.setRect(tempRect);
		} catch (TransformException te) {
			// something bad happened while trying to transform this
			// envelope. let's try with wgs84
			tryWithWGS84 = true;
		} catch (FactoryException fe) {
			// something bad happened while trying to transform this
			// envelope. let's try with wgs84
			tryWithWGS84 = true;
		}
		
		//TODO IMPLEMENT ME

		// //
		//
		// If this does not work we go back to reproject in the wgs84
		// requested envelope
		//		
		// //
		if (tryWithWGS84) {
			final GeneralEnvelope requestedEnvelopeWGS84 = getRequestedEnvelopeWGS84(requestedEnvelope2D);

			// checking the intersection in wgs84
			if (!requestedEnvelopeWGS84.intersects(wgs84BaseEnvelope2D, true))
				return null;

			// intersect
			adjustedRequestedEnvelope = new GeneralEnvelope(
					requestedEnvelopeWGS84);
			adjustedRequestedEnvelope.intersect(this.wgs84BaseEnvelope2D);
			adjustedRequestedEnvelope = CRS.transform(CRS.findMathTransform(
					requestedEnvelopeWGS84.getCoordinateReferenceSystem(),
					this.spatialReferenceSystem2D, true),
					adjustedRequestedEnvelope);
			
			
			
		} 
		return adjustedRequestedEnvelope;
	}

	/**
	 * Return a crop region from a specified envelope, leveraging on the grid to
	 * world transformation.
	 * 
	 * @param envelope
	 *            the requested envelope
	 * @return a {@code Rectangle} representing the crop region.
	 * @throws TransformException
	 */
	private Rectangle getCropRegion(GeneralEnvelope envelope)
			throws TransformException {
		final Rectangle2D ordinates = CRS.transform(
				this.getOriginalGridToWorld(PixelInCell.CELL_CORNER).inverse(),
				envelope).toRectangle2D();
		final GeneralGridRange finalRange = new GeneralGridRange(ordinates
				.getBounds());
		return finalRange.toRectangle();
	}

	/**
	 * Convert a requested envelope to WGS84.
	 * 
	 * @param requestedEnvelopeCRS
	 *            the CRS of the requested envelope.
	 * @param requestedEnvelope2D
	 *            the 2D envelope
	 * @return the requested envelope as WGS84.
	 * @throws FactoryException
	 * @throws TransformException
	 */
	private GeneralEnvelope getRequestedEnvelopeWGS84(
			GeneralEnvelope requestedEnvelope2D) throws FactoryException,
			TransformException {
		final MathTransform transformToWGS84;
		final CoordinateReferenceSystem requestedEnvelopeCRS = requestedEnvelope2D
				.getCoordinateReferenceSystem();
		if (!CRS.equalsIgnoreMetadata(requestedEnvelopeCRS,
				DefaultGeographicCRS.WGS84)) {
			// get a math transform to go to WGS84
			transformToWGS84 = CRS.findMathTransform(requestedEnvelopeCRS,
					DefaultGeographicCRS.WGS84, true);
		} else {
			transformToWGS84 = IdentityTransform.create(2);
		}

		// do we need to transform the requested envelope?
		final GeneralEnvelope requestedEnvelopeWGS84;
		if (!transformToWGS84.isIdentity()) {
			requestedEnvelopeWGS84 = CRS.transform(transformToWGS84,
					requestedEnvelope2D);
			requestedEnvelopeWGS84
					.setCoordinateReferenceSystem(DefaultGeographicCRS.WGS84);
		} else {
			requestedEnvelopeWGS84 = new GeneralEnvelope(requestedEnvelope2D);
		}
		return requestedEnvelopeWGS84;
	}

	/**
	 * Tries to lazily compute the WGS84 version for the envelope of this
	 * coverage
	 * 
	 * @throws FactoryException
	 * @throws TransformException
	 */
	private void initBaseEnvelopes2D() throws FactoryException,
			TransformException {
		synchronized (this) {
			// //
			//
			// Get the original envelope in WGS84
			//
			// //
			if (wgs84BaseEnvelope2D == null) {
				if (!CRS.equalsIgnoreMetadata(coverageCRS,
						DefaultGeographicCRS.WGS84)) {
					// get a math transform to WGS84 and go there.
					final MathTransform toWGS84 = CRS.findMathTransform(
							coverageCRS, DefaultGeographicCRS.WGS84, true);

					if (!toWGS84.isIdentity()) {
						wgs84BaseEnvelope2D = new Envelope2D(CRS.transform(
								toWGS84, this.baseEnvelope));
						wgs84BaseEnvelope2D
								.setCoordinateReferenceSystem(DefaultGeographicCRS.WGS84);
					}
				}

				// we don't need to do anything
				if (wgs84BaseEnvelope2D == null) {
					wgs84BaseEnvelope2D = new Envelope2D(baseEnvelope);
				}
			}

			// //
			//
			// Get the original envelope 2d and its spatial reference system
			//
			// //
			if (this.spatialReferenceSystem2D == null) {
				if (coverageCRS.getCoordinateSystem().getDimension() != 2) {
					this.spatialReferenceSystem2D = CRS.getHorizontalCRS(this
							.getCrs());
					assert this.spatialReferenceSystem2D.getCoordinateSystem()
							.getDimension() == 2;
					this.baseEnvelope2D = new Envelope2D(
							CRS
									.transform(
											CRS
													.findMathTransform(
															coverageCRS,
															(CoordinateReferenceSystem) this.spatialReferenceSystem2D),
											baseEnvelope));
				} else {
					this.spatialReferenceSystem2D = coverageCRS;
					this.baseEnvelope2D = new Envelope2D(baseEnvelope);
				}
			}
		}
	}

	/**
	 * Gets the coordinate reference system that will be associated to the
	 * {@link GridCoverage} by looking for a related PRJ.
	 */
	private void getCoordinateReferenceSystemFromPrj() {
		String prjPath = null;

		coverageCRS = null;
		prjPath = new StringBuffer(this.parentPath).append(File.separatorChar)
				.append(coverageName).append(".prj").toString();

		// read the prj info from the file
		PrjFileReader projReader = null;

		try {
			final File prj = new File(prjPath);

			if (prj.exists()) {
				projReader = new PrjFileReader(new FileInputStream(prj)
						.getChannel());
				coverageCRS = projReader.getCoordinateReferenceSystem();
			}
			// If some exception occurs, warn about the error but proceed
			// using a default CRS
		} catch (FileNotFoundException e) {
			if (LOGGER.isLoggable(Level.WARNING)) {
				LOGGER.log(Level.WARNING, e.getLocalizedMessage(), e);
			}
		} catch (IOException e) {
			if (LOGGER.isLoggable(Level.WARNING)) {
				LOGGER.log(Level.WARNING, e.getLocalizedMessage(), e);
			}
		} catch (FactoryException e) {
			if (LOGGER.isLoggable(Level.WARNING)) {
				LOGGER.log(Level.WARNING, e.getLocalizedMessage(), e);
			}
		} finally {
			if (projReader != null) {
				try {
					projReader.close();
				} catch (IOException e) {
					if (LOGGER.isLoggable(Level.WARNING)) {
						LOGGER.log(Level.WARNING, e.getLocalizedMessage(), e);
					}
				}
			}
		}
	}

	/**
	 * Checks whether a world file is associated with the data source. If found,
	 * set a proper envelope.
	 * 
	 * @throws IllegalStateException
	 * @throws IOException
	 */
	private void checkForWorldFile() throws IOException {
		final String worldFilePath = new StringBuffer(this.parentPath).append(
				SEPARATOR).append(coverageName).toString();

		File file2Parse = null;
		boolean worldFileExists = false;
		// //
		//
		// Check for a world file with the format specific extension
		//
		// //
		if (worldFileExt != null && worldFileExt.length() > 0) {
			file2Parse = new File(worldFilePath + worldFileExt);
			worldFileExists = file2Parse.exists();
		}

		// //
		//
		// Check for a world file with the default extension
		//
		// //
		if (!worldFileExists) {
			file2Parse = new File(worldFilePath + DEFAULT_WORLDFILE_EXT);
			worldFileExists = file2Parse.exists();
		}

		if (worldFileExists) {
			final WorldFileReader reader = new WorldFileReader(file2Parse);
			raster2Model = reader.getTransform();

			// //
			//
			// In case we read from a real world file we have together the
			// envelope. World file transformation assumes to work in the
			// CELL_CENTER condition
			//
			// //
			final AffineTransform tempTransform = new AffineTransform(
					(AffineTransform) raster2Model);
			tempTransform.translate(-0.5, -0.5);

			try {
				baseEnvelope = CRS.transform(ProjectiveTransform
						.create(tempTransform), new GeneralEnvelope(
						baseGridRange.toRectangle()));
			} catch (TransformException e) {
				if (LOGGER.isLoggable(Level.WARNING)) {
					LOGGER.log(Level.WARNING, e.getLocalizedMessage(), e);
				}
			} catch (IllegalStateException e) {
				if (LOGGER.isLoggable(Level.WARNING)) {
					LOGGER.log(Level.WARNING, e.getLocalizedMessage(), e);
				}
			}
		}
	}

	/**
	 * Prepares the read param for doing an
	 * {@link ImageReader#read(int, ImageReadParam)}.
	 * 
	 * It sets the passed {@link ImageReadParam} in terms of decimation on
	 * reading using the provided requestedEnvelope and requestedDim to evaluate
	 * the needed resolution.
	 * 
	 * @param overviewPolicy
	 *            it can be one of {@link Hints#VALUE_OVERVIEW_POLICY_IGNORE},
	 *            {@link Hints#VALUE_OVERVIEW_POLICY_NEAREST},
	 *            {@link Hints#VALUE_OVERVIEW_POLICY_QUALITY} or
	 *            {@link Hints#VALUE_OVERVIEW_POLICY_SPEED}. It specifies the
	 *            policy to compute the overviews level upon request.
	 * @param readP
	 *            an instance of {@link ImageReadParam} for setting the
	 *            subsampling factors.
	 * @param requestedEnvelope
	 *            the {@link GeneralEnvelope} we are requesting.
	 * @param requestedDim
	 *            the requested dimensions.
	 * @throws IOException
	 * @throws TransformException
	 */
	protected void setReadParameters(OverviewPolicy overviewPolicy,
			ImageReadParam readP, GeneralEnvelope requestedEnvelope,
			Rectangle requestedDim) throws IOException, TransformException {
		// TODO: Actually, returned imageChoice has been removed since GDAL
		// automatically
		// uses the proper overview.
		double[] requestedRes = null;

		// //
		//
		// Init overview policy
		//
		// //
		if (overviewPolicy == null) {
			overviewPolicy = OverviewPolicy.NEAREST;
		}

		// //
		//
		// default values for subsampling
		//
		// //
		readP.setSourceSubsampling(1, 1, 0, 0);

		// //
		//
		// requested to ignore overviews
		//
		// //
		if (overviewPolicy.equals(OverviewPolicy.IGNORE)) {
			return;
		}

		// //
		//
		// Resolution requested. I am here computing the resolution required by
		// the user.
		//
		// //
		if (requestedEnvelope != null) {
			final GridToEnvelopeMapper geMapper = new GridToEnvelopeMapper();
			geMapper.setEnvelope(requestedEnvelope);
			geMapper.setGridRange(new GeneralGridRange(requestedDim));
			geMapper.setGridType(PixelInCell.CELL_CORNER);

			AffineTransform transform = geMapper.createAffineTransform();
			requestedRes = CoverageUtilities.getResolution(transform);

			if (LOGGER.isLoggable(Level.FINE)) {
				LOGGER.log(Level.FINE, "requested resolution: ("
						+ requestedRes[0] + "," + requestedRes[1] + ")");
			}
		}

		if (requestedRes == null) {
			return;
		}

		// /////////////////////////////////////////////////////////////////////
		//
		// DECIMATION ON READING since GDAL will automatically use the overviews
		//
		// /////////////////////////////////////////////////////////////////////
		if ((requestedRes[0] > highestRes[0])
				|| (requestedRes[1] > highestRes[1])) {
			setDecimationParameters(readP, requestedRes);
		}
	}

	/**
	 * Creates a {@link GridCoverage} for the provided {@link PlanarImage} using
	 * the {@link #raster2Model} that was provided for this coverage.
	 * 
	 * <p>
	 * This method is vital when working with coverages that have a raster to
	 * model transformation that is not a simple scale and translate.
	 * 
	 * @param image
	 *            contains the data for the coverage to create.
	 * @param raster2Model
	 *            is the {@link MathTransform} that maps from the raster space
	 *            to the model space.
	 * @return a {@link GridCoverage}
	 * @throws IOException
	 */
	protected GridCoverage createCoverageFromImage(PlanarImage image,
			MathTransform raster2Model) throws IOException {
		// creating bands
		final int numBands = image.getSampleModel().getNumBands();
		final GridSampleDimension[] bands = new GridSampleDimension[numBands];

		// checking the names
		final ColorModel cm = image.getColorModel();
		final String[] names = new String[numBands];

		// in case of index color model we are already done.
		if (cm instanceof IndexColorModel) {
			names[0] = "index band";
		} else {
			// in case of multiband image we are not done yet.
			final ColorSpace cs = cm.getColorSpace();

			if (cs instanceof IHSColorSpace) {
				names[0] = "Intensity band";
				names[1] = "Hue band";
				names[2] = "Saturation band";
			} else {
				// not IHS, let's take the type
				final int type = cs.getType();

				switch (type) {
				case ColorSpace.CS_GRAY:
				case ColorSpace.TYPE_GRAY:
					names[0] = "GRAY";

					break;

				case ColorSpace.CS_sRGB:
				case ColorSpace.CS_LINEAR_RGB:
				case ColorSpace.TYPE_RGB:
					names[0] = "RED";
					names[1] = "GREEN";
					names[2] = "BLUE";

					break;

				case ColorSpace.TYPE_CMY:
					names[0] = "CYAN";
					names[1] = "MAGENTA";
					names[2] = "YELLOW";

					break;

				case ColorSpace.TYPE_CMYK:
					names[0] = "CYAN";
					names[1] = "MAGENTA";
					names[2] = "YELLOW";
					names[3] = "K";

					break;
				}
			}
		}

		// setting bands names.
		for (int i = 0; i < numBands; i++) {
			bands[i] = new GridSampleDimension(names[i]).geophysics(true);
		}

		// creating coverage
		if (raster2Model != null) {
			return coverageFactory.create(coverageName, image, coverageCRS,
					raster2Model, bands, null, null);
		}

		return coverageFactory.create(coverageName, image, new GeneralEnvelope(
				baseEnvelope), bands, null, null);
	}

	/**
	 * Creates a {@link GridCoverage} for the provided {@link PlanarImage} using
	 * the {@link #baseEnvelope} that was provided for this coverage.
	 * 
	 * @param image
	 *            contains the data for the coverage to create.
	 * @return a {@link GridCoverage}
	 * @throws IOException
	 */
	protected final GridCoverage createCoverageFromImage(PlanarImage image)
			throws IOException {
		return createCoverageFromImage(image, null);
	}

	/**
	 * This method is responsible for evaluating possible subsampling factors
	 * once the best resolution level has been found in case we have support for
	 * overviews, or starting from the original coverage in case there are no
	 * overviews available.
	 * 
	 * @param readP
	 *            the imageRead parameter to be set
	 * @param requestedRes
	 *            the requested resolutions from which to determine the
	 *            decimation parameters.
	 */
	protected void setDecimationParameters(ImageReadParam readP,
			double[] requestedRes) {
		{
			final int w = baseGridRange.getLength(0);
			final int h = baseGridRange.getLength(1);

			// /////////////////////////////////////////////////////////////////////
			// DECIMATION ON READING
			// Setting subsampling factors with some checkings
			// 1) the subsampling factors cannot be zero
			// 2) the subsampling factors cannot be such that the w or h are
			// zero
			// /////////////////////////////////////////////////////////////////////
			if (requestedRes == null) {
				readP.setSourceSubsampling(1, 1, 0, 0);
			} else {
				int subSamplingFactorX = (int) Math.floor(requestedRes[0]
						/ highestRes[0]);
				subSamplingFactorX = (subSamplingFactorX == 0) ? 1
						: subSamplingFactorX;

				while (((w / subSamplingFactorX) <= 0)
						&& (subSamplingFactorX >= 0))
					subSamplingFactorX--;

				subSamplingFactorX = (subSamplingFactorX == 0) ? 1
						: subSamplingFactorX;

				int subSamplingFactorY = (int) Math.floor(requestedRes[1]
						/ highestRes[1]);
				subSamplingFactorY = (subSamplingFactorY == 0) ? 1
						: subSamplingFactorY;

				while (((h / subSamplingFactorY) <= 0)
						&& (subSamplingFactorY >= 0))
					subSamplingFactorY--;

				subSamplingFactorY = (subSamplingFactorY == 0) ? 1
						: subSamplingFactorY;

				readP.setSourceSubsampling(subSamplingFactorX,
						subSamplingFactorY, 0, 0);
			}
		}
	}

	/**
	 * Information about this source.
	 * <p>
	 * Subclasses should provide additional format specific information.
	 * 
	 * @return ServiceInfo describing getSource().
	 */
	public ServiceInfo getInfo() {
		DefaultServiceInfo info = new DefaultServiceInfo();
		info.setDescription(source.toString());

		if (source instanceof URL) {
			URL url = (URL) source;
			info.setTitle(url.getFile());

			try {
				info.setSource(url.toURI());
			} catch (URISyntaxException e) {
			}
		} else if (source instanceof File) {
			File file = (File) source;
			String filename = file.getName();

			if ((filename == null) || (filename.length() == 0)) {
				info.setTitle(file.getName());
			}

			info.setSource(file.toURI());
		}

		return info;
	}

	/**
	 * Information about the named gridcoverage.
	 * 
	 * @param subname
	 *            Name indicing grid coverage to describe
	 * @return ResourceInfo describing grid coverage indicated
	 */
	public ResourceInfo getInfo(String subname) {
		DefaultResourceInfo info = new DefaultResourceInfo();
		info.setName(subname);
		info.setBounds(new ReferencedEnvelope(this.getOriginalEnvelope()));
		info.setCRS(this.getCrs());
		info.setTitle(subname);

		return info;
	}
}
