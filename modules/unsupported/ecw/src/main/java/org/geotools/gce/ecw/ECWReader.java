/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2007, Geotools Project Managment Committee (PMC)
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
 *
 */
package org.geotools.gce.ecw;

import it.geosolutions.imageio.gdalframework.GDALCommonIIOImageMetadata;
import it.geosolutions.imageio.plugins.ecw.ECWImageReaderSpi;
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
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.referencing.operation.builder.GridToEnvelopeMapper;
import org.geotools.referencing.operation.transform.ProjectiveTransform;
import org.geotools.resources.CRSUtilities;
import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.coverage.grid.GridCoverageReader;
import org.opengis.geometry.Envelope;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValue;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.datum.PixelInCell;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.NoninvertibleTransformException;
import org.opengis.referencing.operation.TransformException;

/**
 * This class can read a ECW data source and create a {@link GridCoverage2D}
 * from the data.
 * 
 * @author Daniele Romagnoli, GeoSolutions.
 * @author Simone Giannecchini (simboss), GeoSolutions
 * @since 2.3.x
 */
public final class ECWReader extends AbstractGridCoverage2DReader implements
		GridCoverageReader {
	/** Logger. */
	private final static Logger LOGGER = org.geotools.util.logging.Logging
			.getLogger("org.geotools.gce.ecw");

	/** Caches an {@code ImageReaderSpi} for a {@code ECWImageReader}. */
	private final static ImageReaderSpi readerSPI = new ECWImageReaderSpi();

	/** Absolute path to the parent dir for this coverage. */
	private String parentPath;

	/**
	 * Creates a new instance of a {@code ECWReader}. I assume nothing about
	 * file extension.
	 * 
	 * @param input
	 *            Source object for which we want to build an {@code ECWReader}.
	 * @throws DataSourceException
	 * @throws FactoryException
	 * @throws MismatchedDimensionException
	 */
	public ECWReader(Object input) throws DataSourceException,
			MismatchedDimensionException {
		this(input, null);
	}

	/**
	 * Creates a new instance of a {@code ECWReader}. I assume nothing about
	 * file extension.
	 * 
	 * @param input
	 *            Source object for which we want to build an {@code ECWReader}.
	 * @param hints
	 *            Hints to be used by this reader throughout his life.
	 * @throws DataSourceException
	 * @throws FactoryException
	 * @throws MismatchedDimensionException
	 */
	public ECWReader(Object input, final Hints hints)
			throws DataSourceException, MismatchedDimensionException {

		// /////////////////////////////////////////////////////////////////////
		//
		// Checking input
		//
		// /////////////////////////////////////////////////////////////////////
		coverageName = "ECW";
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
					: "ECW_coverage";
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

		// /////////////////////////////////////////////////////////////////////
		//
		// Setting Envelope and CRS
		//
		// ////////////////////////////////////////////////////////////////////

		// //
		//
		// Using Common metadata from GDAL
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

		if (crs == null) {
			LOGGER.info("crs not found proceeding with EPSG:4326");
			crs = ECWFormat.getDefaultCRS();
		}

		// //
		//
		// If common metadata doesn't have sufficient information to set the
		// envelope, try other ways, such as looking for a WorldFile
		//
		// //
		if (originalEnvelope == null)
			checkForWorldFile();

		originalEnvelope.setCoordinateReferenceSystem(crs);

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

		// TODO: Check if "eww" is the right extension of ecw world files.
		File file2Parse = new File(worldFilePath + ".eww");
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
	 * Checks the input provided to this {@link ECWReader} and sets all the
	 * other objects and flags accordingly.
	 * 
	 * @param input
	 *            provided to this {@link ECWReader}
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
		//
		// URL to FIle
		//
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
		//
		// File
		//
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
		} else
		// //
		//
		// URL
		//
		// //
		if (input instanceof URL) {
			final URL tempURL = ((URL) input);
			inStream = ImageIO.createImageInputStream(tempURL.openConnection()
					.getInputStream());
		} else
		// //
		//
		// InputStream
		//
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
		//
		// ImageInputStream
		//
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
		return new ECWFormat();
	}

	/**
	 * Reads a {@link GridCoverage2D} possibly matching as close as possible the
	 * resolution computed by using the input params provided by using the
	 * parameters for this {@link #read(GeneralParameterValue[])}.
	 * 
	 * <p>
	 * To have an idea about the possible read parameters take a look at
	 * {@link AbstractGridFormat} class and {@link ECWFormat} class.
	 * 
	 * @param params
	 *            an array of {@link GeneralParameterValue} containing the
	 *            parameters to control this read process.
	 * 
	 * @return a {@link GridCoverage2D}.
	 * 
	 * @see AbstractGridFormat
	 * @see ECWFormat
	 * @see org.opengis.coverage.grid.GridCoverageReader#read(org.opengis.parameter.GeneralParameterValue[])
	 */
	public GridCoverage read(GeneralParameterValue[] params)
			throws IllegalArgumentException, IOException {
		GeneralEnvelope readEnvelope = null;
		Rectangle requestedDim = null;
		OverviewPolicy overviewPolicy=null;
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
	 * 
	 * @return a GridCoverage
	 * 
	 * @throws IOException
	 */
	private GridCoverage createCoverage(GeneralEnvelope requestedEnvelope,
			Rectangle requestedDim, int iUseJAI, OverviewPolicy overviewPolicy) throws IOException {

		if (!closeMe) {
			inStream.reset();
			inStream.mark();
		}
		// /////////////////////////////////////////////////////////////////////
		//
		// Doing an image read for reading the coverage.
		//
		// /////////////////////////////////////////////////////////////////////

		// //
		//
		// Setting subsampling factors with some checkings
		// 1) the subsampling factors cannot be zero
		// 2) the subsampling factors cannot be such that the w 7or h are zero
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

		// /////////////////////////////////////////////////////////////////////
		//
		// Check if we have something to load by intersecting the requested
		// envelope with the bounds of the data set.
		//
		// If the requested envelope is not in the same crs of the data set crs
		// we have to perform a conversion towards the latter crs before
		// intersecting anything.
		//
		// /////////////////////////////////////////////////////////////////////
		GeneralEnvelope intersectionEnvelope = null;
		Rectangle sourceRegion = null;
		if (requestedEnvelope != null) {
			if (!CRS.equalsIgnoreMetadata(requestedEnvelope
					.getCoordinateReferenceSystem(), this.crs)) {
				try {
					// //
					//
					// transforming the envelope back to the dataset crs in
					// order to interact with the original envelope
					//
					// //
					final MathTransform transform = CRS.findMathTransform(
							CRSUtilities.getCRS2D(requestedEnvelope
									.getCoordinateReferenceSystem()),
							CRSUtilities.getCRS2D(crs), true);
					if (!transform.isIdentity()) {
						requestedEnvelope = CRS.transform(transform,
								requestedEnvelope);
						requestedEnvelope
								.setCoordinateReferenceSystem(this.crs);

						if (LOGGER.isLoggable(Level.FINE))
							LOGGER.fine(new StringBuffer(
									"Reprojected envelope ").append(
									requestedEnvelope.toString()).append(
									" crs ").append(crs.toWKT()).toString());
					}
				} catch (TransformException e) {
					throw new DataSourceException(
							"Unable to create a coverage for this source", e);
				} catch (FactoryException e) {
					throw new DataSourceException(
							"Unable to create a coverage for this source", e);
				}
			}
			if (!requestedEnvelope.intersects(this.originalEnvelope, true)) {
				if (LOGGER.isLoggable(Level.FINE))
					LOGGER
							.warning("The requested envelope does not intersect the envelope of this coverage, we will return a null coverage.");
				throw new DataSourceException(
						"The requested envelope does not intersect the envelope of this coverage, we will return a null coverage.");
			}
			intersectionEnvelope = new GeneralEnvelope(requestedEnvelope);
			// intersect the requested area with the bounds of this layer
			intersectionEnvelope.intersect(originalEnvelope);
			intersectionEnvelope.setCoordinateReferenceSystem(this.crs);

			// //
			//
			// Crop the source region
			//
			// //
			try {
				final GeneralGridRange finalRange = new GeneralGridRange(CRS
						.transform(this.raster2Model.inverse(),
								intersectionEnvelope));
				// CROP
				sourceRegion = finalRange.toRectangle();
				if (!sourceRegion.intersects(this.originalGridRange
						.toRectangle()))
					sourceRegion = null;
				else
					sourceRegion = sourceRegion
							.intersection(this.originalGridRange.toRectangle());

			} catch (NoninvertibleTransformException e) {
				if (LOGGER.isLoggable(Level.WARNING))
					LOGGER.log(Level.WARNING, e.getLocalizedMessage(), e);
				sourceRegion = null;
			} catch (TransformException e) {
				if (LOGGER.isLoggable(Level.WARNING))
					LOGGER.log(Level.WARNING, e.getLocalizedMessage(), e);
				sourceRegion = null;
			}
		}

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

		final PlanarImage ecwCoverage;
		if (sourceRegion != null)
			readP.setSourceRegion(sourceRegion);

		// //
		//
		// image and metadata
		//
		// //
		boolean useJAI = iUseJAI!=0?(iUseJAI>0):true;
		if (iUseJAI==0&&this.hints != null) {
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
			ecwCoverage = JAI.create("ImageRead", pbjImageRead, hints);
		}
		else
		{
			final ImageReader reader = readerSPI.createReaderInstance();
			reader.setInput(input,true,true);
			ecwCoverage=PlanarImage.wrapRenderedImage(reader.read(imageChoice,readP));
		}
		// /////////////////////////////////////////////////////////////////////
		//
		// Creating the coverage
		//
		// /////////////////////////////////////////////////////////////////////
		try {
			if (intersectionEnvelope != null) {
				// I need to calculate a new transformation (raster2Model)
				// between the cropped image and the required
				// intersectionEnvelope
				final GridToEnvelopeMapper gem = new GridToEnvelopeMapper();
				gem.setEnvelope(intersectionEnvelope);
				final int ssWidth = ecwCoverage.getWidth();
				final int ssHeight = ecwCoverage.getHeight();
				gem.setGridRange(new GeneralGridRange(new Rectangle(0, 0,
						ssWidth, ssHeight)));
				gem.setGridType(PixelInCell.CELL_CENTER);
				return super.createImageCoverage(ecwCoverage, gem
						.createTransform());
			} else {
				// In case of not intersectionEnvelope (As an instance, when
				// reading the whole image), I can use the originalEnvelope. So,
				// no need to specify a raster2model parameter
				return super.createImageCoverage(ecwCoverage);
			}
		} catch (NoSuchElementException e) {
			if (LOGGER.isLoggable(Level.SEVERE))
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			throw new DataSourceException(e);
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
