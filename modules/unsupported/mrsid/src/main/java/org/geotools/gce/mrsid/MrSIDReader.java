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
 *
 */
package org.geotools.gce.mrsid;

import it.geosolutions.imageio.gdalframework.GDALCommonIIOImageMetadata;
import it.geosolutions.imageio.plugins.mrsid.MrSIDIIOImageMetadata;
import it.geosolutions.imageio.plugins.mrsid.MrSIDImageReaderSpi;
import it.geosolutions.imageio.stream.input.FileImageInputStreamExtImpl;

import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.renderable.ParameterBlock;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.FileCacheImageInputStream;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;

import org.geotools.coverage.CoverageFactoryFinder;
import org.geotools.coverage.grid.GeneralGridRange;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.OverviewPolicy;
import org.geotools.data.DataSourceException;
import org.geotools.data.PrjFileReader;
import org.geotools.data.WorldFileReader;
import org.geotools.factory.Hints;
import org.geotools.geometry.Envelope2D;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.referencing.operation.builder.GridToEnvelopeMapper;
import org.geotools.referencing.operation.transform.ConcatenatedTransform;
import org.geotools.referencing.operation.transform.IdentityTransform;
import org.geotools.referencing.operation.transform.ProjectiveTransform;
import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.coverage.grid.GridCoverageReader;
import org.opengis.geometry.Envelope;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValue;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.datum.PixelInCell;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * This class can read a MrSID data source and create a {@link GridCoverage2D}
 * from the data.
 * 
 * @author Daniele Romagnoli, GeoSolutions
 * @author Simone Giannecchini (simboss), GeoSolutions
 */
public final class MrSIDReader extends AbstractGridCoverage2DReader implements
		GridCoverageReader {
	/** Logger. */
	private final static Logger LOGGER = org.geotools.util.logging.Logging
			.getLogger("org.geotools.gce.mrsid");

	/** Caches an {@code ImageReaderSpi} for a {@code MrSIDImageReader}. */
	private final static ImageReaderSpi readerSPI = new MrSIDImageReaderSpi();

	/** Absolute path to the parent dir for this coverage. */
	private String parentPath;

	/**
	 * WGS84 envelope for this coverage.
	 */
	private Envelope2D WGS84OriginalEnvelope;

	private CoordinateReferenceSystem spatialReferenceSystem2D;

	private Envelope2D originalEnvelope2D;

	/**
	 * Creates a new instance of a {@link MrSIDReader}. I assume nothing about
	 * file extension.
	 * 
	 * @param input
	 *            Source object for which we want to build a {@link MrSIDReader}.
	 * @throws DataSourceException
	 * @throws FactoryException
	 * @throws MismatchedDimensionException
	 */
	public MrSIDReader(Object input) throws DataSourceException,
			MismatchedDimensionException {
		this(input, null);
	}

	/**
	 * Creates a new instance of a {@link MrSIDReader}. I assume nothing about
	 * file extension.
	 * 
	 * @param input
	 *            Source object for which we want to build a {@link MrSIDReader}.
	 * @param hints
	 *            Hints to be used by this reader throughout his life.
	 * @throws DataSourceException
	 * @throws FactoryException
	 * @throws MismatchedDimensionException
	 */
	public MrSIDReader(Object input, final Hints hints)
			throws DataSourceException, MismatchedDimensionException {

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
			if (this.hints == null)
				this.hints = new Hints();
			if (hints != null) {
				// prevent the use from reordering axes
				this.hints.add(hints);
			}
			this.coverageFactory = CoverageFactoryFinder
					.getGridCoverageFactory(this.hints);

			// //
			//
			// Source management
			//
			// //
			checkSource(input);

			// Getting a reader for this format
			final ImageReader reader = readerSPI.createReaderInstance();
			reader.setInput(inStream);

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
			coverageName = (source instanceof File) ? ((File) source).getName()
					: "MrSID_coverage";

			// release the stream if we can.
			finalStreamPreparation();
			reader.dispose();
		} catch (IOException e) {
			if (LOGGER.isLoggable(Level.SEVERE))
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			throw new DataSourceException(e);
		} catch (TransformException e) {
			if (LOGGER.isLoggable(Level.SEVERE))
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
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
	 * @throws IllegalStateException
	 * @throws TransformException
	 * @throws MismatchedDimensionException
	 */
	private void setOriginalProperties(ImageReader reader) throws IOException,
			IllegalStateException, TransformException,
			MismatchedDimensionException {

		// //
		//
		// Getting Common metadata from GDAL
		//
		// //
		IIOMetadata metadata = reader.getImageMetadata(0);
		if (!(metadata instanceof GDALCommonIIOImageMetadata))
			throw new DataSourceException(
					"Unexpected error! Metadata are not of the expected class.");
		getPropertiesFromCommonMetadata(metadata);

		// //
		//
		// If common metadata don't have sufficient information to set CRS
		// envelope, try other ways, such as looking for a PRJ
		//
		// //
		if (crs == null)
			getCoordinateReferenceSystemFromPrj();

		if (originalEnvelope == null || crs == null)
			getOriginalEnvelopeFromMrSIDMetadata(metadata);

		if (crs == null) {
			LOGGER.info("crs not found proceeding with EPSG:4326");
			crs = MrSIDFormat.getDefaultCRS();
		}

		// //
		//
		// If common metadata doesn't have sufficient information to set the
		// envelope, try other ways, such as looking for a WorldFile
		//
		// //
		if (originalEnvelope == null)
			checkForWorldFile();

		// setting the coordinate reference system for the envelope
		originalEnvelope.setCoordinateReferenceSystem(crs);

	}

	/**
	 * Given a {@code IIOMetadata} metadata object, retrieves several properties
	 * to properly set envelope, gridrange and crs.
	 * 
	 * @param metadata
	 */
	private void getPropertiesFromCommonMetadata(IIOMetadata metadata) {
		// casting metadata
		final GDALCommonIIOImageMetadata commonMetadata = (GDALCommonIIOImageMetadata) metadata;

		// setting CRS and Envelope directly from GDAL, if available
		final String wkt = commonMetadata.getProjection();
		if (wkt != null && !(wkt.equalsIgnoreCase("")))
			try {
				crs = CRS.parseWKT(wkt);

			} catch (FactoryException fe) {
				// unable to get CRS from WKT
				if (LOGGER.isLoggable(Level.WARNING))
					LOGGER.log(Level.WARNING, fe.getLocalizedMessage(), fe);
				crs = null;
			}

		final int hrWidth = commonMetadata.getWidth();
		final int hrHeight = commonMetadata.getHeight();
		originalGridRange = new GeneralGridRange(new Rectangle(0, 0, hrWidth,
				hrHeight));

		// getting Grid Properties
		final double geoTransform[] = commonMetadata.getGeoTransformation();
		if (geoTransform != null && geoTransform.length == 6) {
			final AffineTransform tempTransform = new AffineTransform(
					geoTransform[1], geoTransform[4], geoTransform[2],
					geoTransform[5], geoTransform[0], geoTransform[3]);
			// attention gdal geotransform does not uses the pixel is centre
			// convention like world files.
			// tempTransform.translate(-0.5, -0.5);
			this.raster2Model = ProjectiveTransform.create(tempTransform);
			try {

				// Setting Envelope
				originalEnvelope = CRS.transform(raster2Model,
						new GeneralEnvelope(originalGridRange.toRectangle()));
			} catch (IllegalStateException e) {
				if (LOGGER.isLoggable(Level.WARNING))
					LOGGER.log(Level.WARNING, e.getLocalizedMessage(), e);
			} catch (TransformException e) {
				if (LOGGER.isLoggable(Level.WARNING))
					LOGGER.log(Level.WARNING, e.getLocalizedMessage(), e);
			}
		}

	}

	/**
	 * Checks whether a world file is associated with the data source. If found,
	 * set the envelope.
	 * 
	 * @throws IllegalStateException
	 * @throws TransformException
	 * @throws IOException
	 */
	private void checkForWorldFile() throws IllegalStateException,
			TransformException, IOException {

		final String worldFilePath = new StringBuffer(this.parentPath).append(
				File.separatorChar).append(this.coverageName).toString();

		File file2Parse = new File(worldFilePath + ".sdw");
		boolean worldFileExists = file2Parse.exists();
		if (!worldFileExists) {
			file2Parse = new File(worldFilePath + ".wld");
			worldFileExists = file2Parse.exists();
		}
		if (worldFileExists) {
			final WorldFileReader reader = new WorldFileReader(file2Parse);
			raster2Model = reader.getTransform();

			// //
			//
			// In case we read from a real world file we have together the
			// envelope
			//
			// //

			final AffineTransform tempTransform = new AffineTransform(
					(AffineTransform) raster2Model);
			tempTransform.translate(-0.5, -0.5);

			originalEnvelope = CRS.transform(ProjectiveTransform
					.create(tempTransform), new GeneralEnvelope(
					originalGridRange.toRectangle()));
		}
	}

	/**
	 * Close the {@code InStream} {@code ImageInputStream} if if we open it up
	 * on purpose toread header info for this
	 * {@link AbstractGridCoverage2DReader}. If the stream cannot be closed, we
	 * just reset and mark it.
	 * 
	 * @throws IOException
	 */
	private void finalStreamPreparation() throws IOException {
		if (closeMe)
			try {
				inStream.close();
			} catch (Throwable e) {
				if (LOGGER.isLoggable(Level.FINE))
					LOGGER.log(Level.FINE, e.getLocalizedMessage(), e);
			}
		else {
			inStream.reset();
			inStream.mark();
		}
	}

	/**
	 * Checks the input provided to this {@link MrSIDReader} and sets all the
	 * other objects and flags accordingly.
	 * 
	 * @param input
	 *            provided to this {@link MrSIDReader}.
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
			if (LOGGER.isLoggable(Level.SEVERE))
				LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
			throw ex;
		}
		this.source = input;
		closeMe = true;

		// //
		// URL to FIle
		// //
		// if it is a URL ponting to a File I convert it to a file,
		// otherwis, later on, I will try to get an inputstream out of it.
		if (input instanceof URL) {
			// URL that point to a file
			final URL sourceURL = ((URL) input);
			if (sourceURL.getProtocol().compareToIgnoreCase("file") == 0) {
				this.source = input = new File(URLDecoder.decode(sourceURL
						.getFile(), "UTF-8"));
			}
		}

		// //
		// File
		// //
		if (input instanceof File) {
			final File sourceFile = (File) input;
			if (!sourceFile.exists() || sourceFile.isDirectory()
					|| !sourceFile.canRead())
				throw new DataSourceException(
						"Provided file does not exist or is a directory or is not readable!");
			this.parentPath = sourceFile.getParent();
			this.coverageName = sourceFile.getName();
			final int dotIndex = coverageName.indexOf(".");
			coverageName = (dotIndex == -1) ? coverageName : coverageName
					.substring(0, dotIndex);
			inStream = new FileImageInputStreamExtImpl(sourceFile);
			// inStream = ImageIO.createImageInputStream(sourceFile);
		} else

		// //
		// URL
		// //
		if (input instanceof URL) {
			final URL tempURL = ((URL) input);
			inStream = ImageIO.createImageInputStream(tempURL.openConnection()
					.getInputStream());
		} else

		// //
		// InputStream
		// //
		if (input instanceof InputStream) {
			closeMe = false;
			if (ImageIO.getUseCache())
				inStream = new FileCacheImageInputStream((InputStream) input,
						null);
			else
				inStream = new MemoryCacheImageInputStream((InputStream) input);
			// let's mark it
			inStream.mark();
		} else

		// //
		// ImageInputStream
		// //
		if (input instanceof ImageInputStream) {
			closeMe = false;
			inStream = (ImageInputStream) input;
			inStream.mark();
		} else
			throw new IllegalArgumentException("Unsupported input type");

		if (inStream == null)
			throw new DataSourceException(
					"No input stream for the provided source");
	}

	/**
	 * Gets resolution information about the coverage itself.
	 * 
	 * @param reader
	 *            an {@link ImageReader} to use for getting the resolution
	 *            information.
	 * @throws IOException
	 * @throws TransformException
	 */
	private void getResolutionInfo(ImageReader reader) throws IOException,
			TransformException {

		// //
		//
		// get the dimension of the hr image and build the model as well as
		// computing the resolution
		//
		// //
		final Rectangle actualDim = new Rectangle(0, 0, reader.getWidth(0),
				reader.getHeight(0));
		originalGridRange = new GeneralGridRange(actualDim);

		// ///
		//
		// setting the higher resolution avalaible for this coverage
		//
		// ///
		highestRes = getResolution(originalEnvelope, actualDim, crs);

		// ///
		//
		// Setting raster to model transformation
		//
		// ///
		final GridToEnvelopeMapper geMapper = new GridToEnvelopeMapper();
		geMapper.setEnvelope(originalEnvelope);
		geMapper.setGridRange(originalGridRange);
		geMapper.setGridType(PixelInCell.CELL_CENTER);
		this.raster2Model = geMapper.createTransform();

	}

	/**
	 * @see org.opengis.coverage.grid.GridCoverageReader#getFormat()
	 */
	public Format getFormat() {
		return new MrSIDFormat();
	}

	/**
	 * Reads a {@link GridCoverage2D} possibly matching as close as possible the
	 * resolution computed by using the input params provided by using the
	 * parameters for this {@link #read(GeneralParameterValue[])}.
	 * 
	 * <p>
	 * To have an idea about the possible read parameters take a look at
	 * {@link AbstractGridFormat} class and {@link MrSIDFormat} class.
	 * 
	 * @param params
	 *            an array of {@link GeneralParameterValue} containing the
	 *            parameters to control this read process.
	 * 
	 * @return a {@link GridCoverage2D}.
	 * 
	 * @see AbstractGridFormat
	 * @see MrSIDFormat
	 * @see org.opengis.coverage.grid.GridCoverageReader#read(org.opengis.parameter.GeneralParameterValue[])
	 */
	public GridCoverage read(GeneralParameterValue[] params)
			throws IllegalArgumentException, IOException {
		GeneralEnvelope readEnvelope = null;
		OverviewPolicy overviewPolicy = null;
		Rectangle requestedDim = null;
		// USE JAI ImageRead -1==no, 0==unset 1==yes
		int iUseJAI = 0;
		if (params != null) {

			final int length = params.length;
			for (int i = 0; i < length; i++) {
				final ParameterValue param = (ParameterValue) params[i];
				final String name = param.getDescriptor().getName().getCode();
				if (name.equals(AbstractGridFormat.READ_GRIDGEOMETRY2D
						.getName().toString())) {
					final GridGeometry2D gg = (GridGeometry2D) param.getValue();
					if (gg == null)
						continue;
					readEnvelope = new GeneralEnvelope((Envelope) gg
							.getEnvelope2D());
					requestedDim = gg.getGridRange2D().getBounds();
					continue;
				}
				if (name.equalsIgnoreCase(AbstractGridFormat.USE_JAI_IMAGEREAD
						.getName().toString())) {
					iUseJAI = param.booleanValue() ? 1 : -1;
					continue;
				}
				if (name.equals(AbstractGridFormat.OVERVIEW_POLICY.getName()
						.toString())) {
					overviewPolicy = (OverviewPolicy) param.getValue();
					continue;
				}
			}
		}
		return createCoverage(readEnvelope, requestedDim, iUseJAI,
				overviewPolicy);
	}

	/**
	 * This method creates the GridCoverage2D from the underlying file.
	 * 
	 * @param requestedDim
	 * @param iUseJAI
	 * @param overviewPolicy
	 * @param readEnvelope
	 * 
	 * @return a GridCoverage
	 * 
	 * @throws java.io.IOException
	 */
	private GridCoverage createCoverage(GeneralEnvelope requestedEnvelope,
			Rectangle requestedDim, int iUseJAI, OverviewPolicy overviewPolicy)
			throws IOException {

		if (!closeMe) {
			inStream.reset();
			inStream.mark();
		}

		// /////////////////////////////////////////////////////////////////////
		//
		// Check if we have something to load by intersecting the requested
		// envelope with the bounds of the data set.
		//
		// /////////////////////////////////////////////////////////////////////
		final GeneralEnvelope adjustedRequestEnvelope = new GeneralEnvelope(2);
		final Rectangle sourceRegion = new Rectangle();
		evaluateRequestedEnvelope(requestedEnvelope, adjustedRequestEnvelope,
				sourceRegion);

		Object input;
		if (source instanceof File)
			input = new FileImageInputStreamExtImpl((File) source);
		else if (source instanceof ImageInputStream
				|| source instanceof InputStream)
			input = inStream;
		else if (source instanceof URL) {
			input = ImageIO.createImageInputStream(((URL) source)
					.openConnection().getInputStream());
		} else
			throw new IllegalArgumentException();

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
		final ImageReadParam readP = new ImageReadParam();
		final Integer imageChoice;
		try {
			imageChoice = setReadParams(overviewPolicy, readP,
					requestedEnvelope, requestedDim);
		} catch (IOException e) {
			if (LOGGER.isLoggable(Level.SEVERE))
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			return null;
		} catch (TransformException e) {
			if (LOGGER.isLoggable(Level.SEVERE))
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			return null;
		}

		final PlanarImage coverage;
		if (sourceRegion != null && !sourceRegion.isEmpty())
			readP.setSourceRegion(sourceRegion);

		// //
		//
		// image and metadata
		//
		// //
		boolean useJAI = iUseJAI != 0 ? (iUseJAI > 0) : true;
		if (iUseJAI == 0 && this.hints != null) {
			Object o = this.hints.get(Hints.USE_JAI_IMAGEREAD);
			if (o != null)
				useJAI = ((Boolean) o).booleanValue();
		}
		if (useJAI) {
			final ParameterBlock pbjImageRead = new ParameterBlock();
			pbjImageRead.add(input);
			pbjImageRead.add(imageChoice);
			pbjImageRead.add(Boolean.FALSE);
			pbjImageRead.add(Boolean.FALSE);
			pbjImageRead.add(Boolean.FALSE);
			pbjImageRead.add(null);
			pbjImageRead.add(null);
			pbjImageRead.add(readP);
			pbjImageRead.add(readerSPI.createReaderInstance());
			coverage = JAI.create("ImageRead", pbjImageRead, hints);
		} else {
			final ImageReader reader = readerSPI.createReaderInstance();
			reader.setInput(input, true, true);
			coverage = PlanarImage.wrapRenderedImage(reader.read(imageChoice, readP));
		}
		// /////////////////////////////////////////////////////////////////////
		//
		// Creating the coverage
		//
		// /////////////////////////////////////////////////////////////////////
		try {
			if (adjustedRequestEnvelope != null) {
				// I need to calculate a new transformation (raster2Model)
				// between the cropped image and the required
				// adjustedRequestEnvelope
				final int ssWidth = coverage.getWidth();
				final int ssHeight = coverage.getHeight();
				
				// //
	            //
	            // setting new coefficients to define a new affineTransformation 
	            // to be applied to the grid to world transformation 
	            // ----------------------------------------------------------------------------------- 
	            //
	            // With respect to the original envelope, the obtained planarImage needs to be
	            // rescaled and translated. The scaling factors are computed as the ratio between the
	            // cropped source region sizes and the read image sizes. 
	            // The translate settings are represented by the offsets of the source region.
	            //
	            // //
	            final double scaleX = sourceRegion.width / (1.0 * ssWidth);
	            final double scaleY = sourceRegion.height / (1.0 * ssHeight);
	            final double translateX = sourceRegion.x;
	            final double translateY = sourceRegion.y;

	            return super.createImageCoverage(coverage,
	                ConcatenatedTransform.create(ProjectiveTransform.create(
	                        new AffineTransform(scaleX, 0, 0, scaleY, translateX + 0.5, translateY
	                            + 0.5)), raster2Model));
			} else {
				// In case of no adjustedRequestEnvelope (As an instance, when
				// reading the whole image), I can use the transformation. So,
				// no need to specify a raster2model parameter
				return super.createImageCoverage(coverage);
			}
		} catch (NoSuchElementException e) {
			if (LOGGER.isLoggable(Level.SEVERE))
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			throw new DataSourceException(e);
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
	 * <p>
	 * In case the requested envelope does not intersect with this coverage
	 * envelope and empty <code>adjustedRequestEnvelope</code> is returned.
	 * 
	 * @param requestedEnvelope
	 *            is the envelope we are requested to load.
	 * @param adjustedRequestEnvelope
	 *            is the adjusted version of the requested envelope.
	 * @param sourceRegion
	 *            represents the area to load in raster space. This parameter
	 *            cannot be null since it gets filled with whatever the crop
	 *            region is depending on the <code>requestedEnvelope</code>.
	 * @throws DataSourceException
	 *             in case something bad occurs.
	 */
	private void evaluateRequestedEnvelope(GeneralEnvelope requestedEnvelope,
			GeneralEnvelope adjustedRequestEnvelope, Rectangle sourceRegion)
			throws DataSourceException {
		try {
			// /////////////////////////////////////////////////////////////////////
			//
			// Check if we have something to load by intersecting the requested
			// envelope with the bounds of this data set.
			//
			// The comparison is done first in WGS84 in order to avoid problems
			// when trying to reproject envelopes from different CRS
			//
			// /////////////////////////////////////////////////////////////////////
			if (requestedEnvelope != null) {

				// /////////////////////////////////////////////////////////////////////
				//
				// PHASE 1
				//
				// PREPARING THE REQUESTED ENVELOPE FOR LATER INTERSECTION
				//
				// /////////////////////////////////////////////////////////////////////

				// //
				//
				// Get the requested envelope CRS and convert it to 2D if
				// necessary
				//
				// //
				CoordinateReferenceSystem requestedEnvelopeCRS = requestedEnvelope
						.getCoordinateReferenceSystem();
				final MathTransform transformTo2D;
				final GeneralEnvelope requestedEnvelope2D;
				if (requestedEnvelopeCRS.getCoordinateSystem().getDimension() != 2) {
					transformTo2D = CRS.findMathTransform(requestedEnvelopeCRS,
							CRS.getHorizontalCRS(requestedEnvelopeCRS));
					requestedEnvelopeCRS = CRS
							.getHorizontalCRS(requestedEnvelopeCRS);
				} else
					transformTo2D = IdentityTransform.create(2);
				if (!transformTo2D.isIdentity()) {
					requestedEnvelope2D = CRS.transform(transformTo2D,
							requestedEnvelope);
					requestedEnvelope2D
							.setCoordinateReferenceSystem(requestedEnvelopeCRS);
				} else
					requestedEnvelope2D = new GeneralEnvelope(requestedEnvelope);
				assert requestedEnvelope2D.getCoordinateReferenceSystem()
						.getCoordinateSystem().getDimension() == 2;

				// //
				//
				// Now convert the requested envelope to WGS84 if needed for
				// safer
				// comparisons later on with the original crs of this coverage
				//
				// //
				final MathTransform transformToWGS84;
				if (!CRS.equalsIgnoreMetadata(requestedEnvelopeCRS,
						DefaultGeographicCRS.WGS84)) {
					// get a math transform to go to WGS84
					transformToWGS84 = CRS.findMathTransform(
							requestedEnvelopeCRS, DefaultGeographicCRS.WGS84,
							true);

				} else
					transformToWGS84 = IdentityTransform.create(2);
				// do we need to transform the requested envelope?
				final GeneralEnvelope requestedEnvelopeWGS84;
				if (!transformToWGS84.isIdentity()) {
					requestedEnvelopeWGS84 = CRS.transform(transformToWGS84,
							requestedEnvelope2D);
					requestedEnvelopeWGS84
							.setCoordinateReferenceSystem(DefaultGeographicCRS.WGS84);
				} else
					requestedEnvelopeWGS84 = new GeneralEnvelope(
							requestedEnvelope2D);

				// /////////////////////////////////////////////////////////////////////
				//
				// PHASE 2
				//
				// NOW CHECKING THE INTERSECTION IN WGS84
				//
				// //
				//
				// If the two envelopes intersect each other in WGS84 we are
				// reasonably sure that they intersect
				//
				// /////////////////////////////////////////////////////////////////////
				initOriginalEnvelopes2D();
				if (!requestedEnvelopeWGS84.intersects(
						this.WGS84OriginalEnvelope, true)) {
					if (LOGGER.isLoggable(Level.FINE))
						LOGGER
								.warning("The requested envelope does not intersect the envelope of this coverage, we will throw an exception.");
					throw new DataSourceException(
							"The requested envelope does not intersect the envelope of this coverage, we will throw an exception.");
				}

				// /////////////////////////////////////////////////////////////////////
				//
				// PHASE 3
				//
				// NOW DO THE INTERSECTION USING THE NATIVE 2D CRS
				//
				// //
				//
				// If this does not work we go back to reprojectin the wgs84
				// requested envelope
				//				
				// /////////////////////////////////////////////////////////////////////
				try {
					// convert the requested envelope 2d to this coverage native
					// crs
					if (!CRS.equalsIgnoreMetadata(requestedEnvelope2D
							.getCoordinateReferenceSystem(),
							this.spatialReferenceSystem2D))
						adjustedRequestEnvelope = CRS.transform(CRS
								.findMathTransform(requestedEnvelope2D
										.getCoordinateReferenceSystem(),
										this.spatialReferenceSystem2D),
								requestedEnvelope2D);

				} catch (TransformException te) {
					// something bad happened while trying to transform this
					// envelope
					// let's try with wgs84
					adjustedRequestEnvelope
							.intersect(this.WGS84OriginalEnvelope);
					adjustedRequestEnvelope = CRS.transform(CRS
							.findMathTransform(requestedEnvelopeWGS84
									.getCoordinateReferenceSystem(),
									this.spatialReferenceSystem2D),
							requestedEnvelopeWGS84);

				}
				// intersect the requested area with the bounds of this layer in
				// native crs
				if (adjustedRequestEnvelope.isEmpty())
					adjustedRequestEnvelope = requestedEnvelope.clone();
				adjustedRequestEnvelope.intersect(this.originalEnvelope2D);
				adjustedRequestEnvelope
						.setCoordinateReferenceSystem(this.spatialReferenceSystem2D);

				// /////////////////////////////////////////////////////////////////////
				//
				// NOW BUILDING A SUITABLE CROP REGION
				//
				// /////////////////////////////////////////////////////////////////////

				// //
				//
				// compute the crop region
				//
				// //
				final GeneralGridRange finalRange = new GeneralGridRange(CRS
						.transform(this.getOriginalGridToWorld(
								PixelInCell.CELL_CORNER).inverse(),
								adjustedRequestEnvelope),PixelInCell.CELL_CORNER);
				// CROP
				sourceRegion.setRect(finalRange.toRectangle());
				if (!sourceRegion.intersects(this.originalGridRange
						.toRectangle())
						|| sourceRegion.isEmpty())
					throw new DataSourceException(
							"The crop region is empty.");
				sourceRegion.setRect(sourceRegion
						.intersection(this.originalGridRange.toRectangle()));

			} else {
				// we do not have a requested envelope, let's load it all
				adjustedRequestEnvelope.setEnvelope(this.originalEnvelope);
				adjustedRequestEnvelope.setCoordinateReferenceSystem(this.crs);

				// don't use the source region
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
	}

	/**
	 * Tries to lazily compute the WGS84 version for the envelope of this
	 * coverage
	 * 
	 * @throws FactoryException
	 * @throws TransformException
	 */
	private void initOriginalEnvelopes2D() throws FactoryException,
			TransformException {
		synchronized (this) {
			// //
			//
			// Get the original envelope in WGS84
			//
			// //
			if (WGS84OriginalEnvelope == null) {
				if (!CRS.equalsIgnoreMetadata(this.crs,
						DefaultGeographicCRS.WGS84)) {
					// get a math transform to WGS84 and go there.
					final MathTransform toWGS84 = CRS.findMathTransform(
							this.crs, DefaultGeographicCRS.WGS84, true);
					if (!toWGS84.isIdentity()) {
						WGS84OriginalEnvelope = new Envelope2D(CRS.transform(
								toWGS84, this.originalEnvelope));
						WGS84OriginalEnvelope
								.setCoordinateReferenceSystem(DefaultGeographicCRS.WGS84);

					}
				}
				// we don't need to do anything
				if (WGS84OriginalEnvelope == null)
					WGS84OriginalEnvelope = new Envelope2D(
							this.originalEnvelope);
			}

			// //
			//
			// Get the original envelope 2d and its spatial reference system
			//
			// //
			if (this.spatialReferenceSystem2D == null) {
				if (this.crs.getCoordinateSystem().getDimension() != 2) {
					this.spatialReferenceSystem2D = CRS.getHorizontalCRS(this
							.getCrs());
					assert this.spatialReferenceSystem2D.getCoordinateSystem()
							.getDimension() == 2;
					this.originalEnvelope2D = new Envelope2D(
							CRS
									.transform(
											CRS
													.findMathTransform(
															this.crs,
															(CoordinateReferenceSystem) this.spatialReferenceSystem2D),
											originalEnvelope));
				} else {
					this.spatialReferenceSystem2D = this.crs;
					this.originalEnvelope2D = new Envelope2D(
							this.originalEnvelope);

				}
			}
		}
	}

	/**
	 * This method is responsible for building up an envelope according to the
	 * definition of the crs. It assumes that X coordinate on the grid itself
	 * maps to longitude and y coordinate maps to latitude.
	 * 
	 * @param gridMetadata
	 *            The {@link MrSIDIIOImageMetadata} to parse.
	 * 
	 * @throws MismatchedDimensionException
	 * @throws FactoryException
	 */
	private void getOriginalEnvelopeFromMrSIDMetadata(IIOMetadata metadata)
			throws MismatchedDimensionException {

		final GDALCommonIIOImageMetadata gridMetadata = (GDALCommonIIOImageMetadata) metadata;

		// getting metadata
		final Node root = gridMetadata
				.getAsTree(MrSIDIIOImageMetadata.mrsidImageMetadataName);

		Node child = root.getFirstChild();

		// getting GeoReferencing Properties
		child = child.getNextSibling();
		final NamedNodeMap attributes = child.getAttributes();
		if (originalEnvelope == null) {
			final String xResolution = attributes.getNamedItem(
					"IMAGE__X_RESOLUTION").getNodeValue();
			final String yResolution = attributes.getNamedItem(
					"IMAGE__Y_RESOLUTION").getNodeValue();
			final String xyOrigin = attributes.getNamedItem("IMAGE__XY_ORIGIN")
					.getNodeValue();

			if (xResolution != null && yResolution != null && xyOrigin != null
					&& !(xResolution.trim().equalsIgnoreCase(""))
					&& !(yResolution.trim().equalsIgnoreCase(""))
					&& !(xyOrigin.trim().equalsIgnoreCase(""))) {
				double cellsizeX = Double.parseDouble(xResolution);
				double cellsizeY = Double.parseDouble(yResolution);
				final String[] origins = xyOrigin.split(",");
				double xul = Double.parseDouble(origins[0]);
				double yul = Double.parseDouble(origins[1]);

				xul -= (cellsizeX / 2d);
				yul -= (cellsizeY / 2d);
				final double xll = xul;
				final double yur = yul;
				final int width = originalGridRange.getLength(0);
				final int height = originalGridRange.getLength(1);
				final double xur = xul + (cellsizeX * width);
				final double yll = yul - (cellsizeY * height);
				originalEnvelope = new GeneralEnvelope(
						new double[] { xll, yll }, new double[] { xur, yur });
			}
		}
		// Retrieving projection Information
		if (crs == null) {
			Node attribute = attributes.getNamedItem("IMG__WKT");
			if (attribute != null) {
				String wkt = attribute.getNodeValue();
				if (wkt != null && (wkt.trim().length() > 0))
					try {
						crs = CRS.parseWKT(wkt);
					} catch (FactoryException fe) {
						 if (LOGGER.isLoggable(Level.FINE)) {
	                      	    LOGGER.log(Level.FINE, "Unable to get CRS from"
									+ " WKT contained in metadata."
									+ " Looking for a PRJ.");
	                        }
						crs = null;
					}
			}
		}

	}

	/**
	 * Gets the coordinate system that will be associated to the
	 * {@link GridCoverage}. The WGS84 coordinate system is used by default. It
	 * is worth to point out that when reading from a stream which is not
	 * connected to a file, like from an http connection (e.g. from a WCS) we
	 * cannot rely on receiving a prj file too. In this case the exchange of
	 * information about referencing should proceed the exchange of data thus I
	 * rely on this and I ask the user who's invoking the read operation to
	 * provide me a valid crs and envelope through read parameters.
	 * 
	 * @throws FactoryException
	 * @throws IOException
	 * @throws FileNotFoundException
	 * 
	 * TODO: move this method to the parent class
	 */
	private void getCoordinateReferenceSystemFromPrj()
			throws FileNotFoundException, IOException {

		String prjPath = null;
		if (source instanceof File) {
			crs = null;
			prjPath = new StringBuffer(this.parentPath).append(
					File.separatorChar).append(this.coverageName)
					.append(".prj").toString();
			// read the prj info from the file
			PrjFileReader projReader = null;
			try {
				final File prj = new File(prjPath);
				if (prj.exists()) {
					projReader = new PrjFileReader(new FileInputStream(prj)
							.getChannel());
					crs = projReader.getCoordinateReferenceSystem();
				}
			} catch (FileNotFoundException e) {
				// warn about the error but proceed, it is not fatal
				// we have at least the default crs to use
				LOGGER.log(Level.INFO, e.getLocalizedMessage(), e);
			} catch (IOException e) {
				// warn about the error but proceed, it is not fatal
				// we have at least the default crs to use
				LOGGER.log(Level.INFO, e.getLocalizedMessage(), e);
			} catch (FactoryException e) {
				// warn about the error but proceed, it is not fatal
				// we have at least the default crs to use
				LOGGER.log(Level.INFO, e.getLocalizedMessage(), e);
			} finally {
				if (projReader != null)
					try {
						projReader.close();
					} catch (IOException e) {
						// warn about the error but proceed, it is not fatal
						// we have at least the default crs to use
						LOGGER.log(Level.INFO, e.getLocalizedMessage(), e);
					}
			}
		}
	}
}
