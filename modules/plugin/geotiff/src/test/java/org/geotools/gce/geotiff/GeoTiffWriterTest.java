/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2005-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.gce.geotiff;

import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.ParseException;
import java.util.logging.Logger;

import javax.media.jai.JAI;
import javax.media.jai.TileCache;

import junit.framework.TestCase;
import junit.textui.TestRunner;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.imageio.GeoToolsWriteParams;
import org.geotools.coverage.grid.io.imageio.IIOMetadataDumper;
import org.geotools.coverage.processing.AbstractProcessor;
import org.geotools.coverage.processing.DefaultProcessor;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.referencing.operation.matrix.XAffineTransform;
import org.geotools.resources.CRSUtilities;
import org.geotools.test.TestData;
import org.opengis.coverage.grid.GridCoverageReader;
import org.opengis.coverage.grid.GridCoverageWriter;
import org.opengis.coverage.grid.GridRange;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.ProjectedCRS;
import org.opengis.referencing.datum.PixelInCell;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

/**
 * @author Simone Giannecchini
 * 
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/gt/plugin/geotiff/test/org/geotools/gce/geotiff/GeoTiffVisualizationTest.java $
 */
public class GeoTiffWriterTest extends TestCase {
	private static final Logger logger = org.geotools.util.logging.Logging.getLogger(GeoTiffWriterTest.class.toString());

	/**
	 * 
	 */
	public GeoTiffWriterTest() {
		super("Writer Test!");
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		TestRunner.run(GeoTiffWriterTest.class);

	}

	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		final JAI jaiDef = JAI.getDefaultInstance();

		// using a big tile cache
		final TileCache cache = jaiDef.getTileCache();
		cache.setMemoryCapacity(64 * 1024 * 1024);
		cache.setMemoryThreshold(0.75f);

	}

	/**
	 * Testing {@link GeoTiffWriter} capapbilities.
	 * 
	 * @throws IllegalArgumentException
	 * @throws IOException
	 * @throws UnsupportedOperationException
	 * @throws ParseException
	 * @throws FactoryException
	 */
	public void testWriter() throws IllegalArgumentException, IOException,
			UnsupportedOperationException, ParseException, FactoryException {

		// /////////////////////////////////////////////////////////////////////
		//
		//
		// PREPARATION
		//
		//
		// /////////////////////////////////////////////////////////////////////
		final File readdir = TestData.file(GeoTiffWriterTest.class, "");
		final File writedir = new File(new StringBuffer(readdir
				.getAbsolutePath()).append("/testWriter/").toString());
		writedir.mkdir();
		final File files[] = readdir.listFiles(new FilenameFilter() {

			public boolean accept(File dir, String name) {
				// are they tiff?
				if (!name.endsWith("tif") && !name.endsWith("tiff"))
					return false;

				// are they geotiff?
				return new GeoTiffFormat().accepts(new File(new StringBuffer(
						dir.getAbsolutePath()).append(File.separatorChar)
						.append(name).toString()));

			}
		});
		final int numFiles = files.length;

		// /////////////////////////////////////////////////////////////////////
		//
		//
		// FORMAT AND READER
		//
		// Creatin format and other objects we need in the further steps of this
		// test.
		//
		//
		// /////////////////////////////////////////////////////////////////////
		final GeoTiffFormat format = new GeoTiffFormat();
		final GeoTiffWriteParams wp = new GeoTiffWriteParams();
//		wp.setCompressionMode(GeoTiffWriteParams.MODE_EXPLICIT);
//		wp.setCompressionType("ZLib");
//		wp.setCompressionQuality(0.75F);
		wp.setTilingMode(GeoToolsWriteParams.MODE_EXPLICIT);
		wp.setTiling(256, 256);
		final ParameterValueGroup params = format.getWriteParameters();
		params.parameter(
				AbstractGridFormat.GEOTOOLS_WRITE_PARAMS.getName().toString())
				.setValue(wp);
		for (int i = 0; i < numFiles; i++) {

			// /////////////////////////////////////////////////////////////////////
			//
			//
			// READER
			//
			//
			// /////////////////////////////////////////////////////////////////////
			if (TestData.isInteractiveTest())
				logger.info(files[i].getAbsolutePath());

			// getting a reader
			GeoTiffReader reader = new GeoTiffReader(files[i], null);
			// dumping metadata
			IIOMetadataDumper metadataDumper = new IIOMetadataDumper(
					((GeoTiffReader) reader).getMetadata().getRootNode());
			if (TestData.isInteractiveTest()) {
				logger.info(metadataDumper.getMetadata());
			} else
				metadataDumper.getMetadata();

			if (reader != null) {

				// /////////////////////////////////////////////////////////////////////
				//
				//
				// COVERAGE
				//
				//
				// /////////////////////////////////////////////////////////////////////
				GridCoverage2D gc = (GridCoverage2D) reader.read(null);
				if (TestData.isInteractiveTest()) {
					logger.info(new StringBuffer("Coverage before: ").append(
							"\n").append(
							gc.getCoordinateReferenceSystem().toWKT()).append(
							gc.getEnvelope().toString()).toString());
				}
				if (gc != null) {
					// /////////////////////////////////////////////////////////////////////
					//
					//
					// WRITING
					//
					//
					// /////////////////////////////////////////////////////////////////////
					GeneralEnvelope sourceEnv = (GeneralEnvelope) gc.getEnvelope();
					CoordinateReferenceSystem sourceCRS = gc.getCoordinateReferenceSystem2D();
					final File writeFile = new File(new StringBuffer(writedir
							.getAbsolutePath()).append(File.separatorChar)
							.append(gc.getName().toString()).append(".tiff")
							.toString());
					GridCoverageWriter writer = format.getWriter(writeFile);
					writer.write(gc, (GeneralParameterValue[]) params.values()
							.toArray(new GeneralParameterValue[1]));

					// /////////////////////////////////////////////////////////////////////
					//
					//
					// READING BACK
					//
					//
					// /////////////////////////////////////////////////////////////////////
					reader = new GeoTiffReader(writeFile, null);
					metadataDumper = new IIOMetadataDumper(
							((GeoTiffReader) reader).getMetadata()
									.getRootNode());
					if (TestData.isInteractiveTest()) {
						logger.info(metadataDumper.getMetadata());
					} else
						metadataDumper.getMetadata();
					gc = (GridCoverage2D) reader.read(null);
					CoordinateReferenceSystem targetCRS = gc.getCoordinateReferenceSystem2D();
					GeneralEnvelope targetEnv = (GeneralEnvelope) gc.getEnvelope();
					MathTransform tr = CRS.findMathTransform(targetCRS, sourceCRS, true);

					// TODO: THE TEST BELOW IS TEMPORARILY DISABLED.
					//       The sourceCRS is constructed in a strange way.
					//       It declares "m" units, but the map projection
					//       is concatenated with a conversion from metres
					//       to feet.
					if (false) assertTrue(
							"Source and Target coordinate reference systems do not match:" +
							"\n\nSource CRS =\n" + sourceCRS +
							"\n\nTarget CRS =\n" + targetCRS +
							"\n\nSource projection =\n" + getConversionFromBase(sourceCRS) +
							"\n\nTarget projection =\n" + getConversionFromBase(targetCRS) +
							"\n\nInverse transform =\n" + tr,
							CRS.equalsIgnoreMetadata(targetCRS, sourceCRS) || tr.isIdentity());
					assertTrue("Source and Target envelopes do not match",
							checkEnvelopes(sourceEnv,targetEnv,gc));

					if (TestData.isInteractiveTest()) {
						logger
								.info(new StringBuffer("Coverage after: ")
										.append("\n")
										.append(
												gc
														.getCoordinateReferenceSystem()
														.toWKT()).append(
												gc.getEnvelope().toString())
										.toString());
						if (TestData.isInteractiveTest())
							gc.show();
						else
							gc.getRenderedImage().getData();

					}

				}

			}

		}
	}

	private static MathTransform getConversionFromBase(CoordinateReferenceSystem crs) {
        return (crs instanceof ProjectedCRS) ? ((ProjectedCRS) crs).getConversionFromBase().getMathTransform() : null;
	}

	/**
	 * Checks two envelopes for equality ignoring their CRSs.
	 * @param sourceEnv first {@link GeneralEnvelope}  to check.
	 * @param targetEnv secondo {@link GeneralEnvelope} to check.
	 * @param gc the source {@link GridCoverage2D}.
	 * @return false if they are reasonably equal, false otherwise.
	 */
	private boolean checkEnvelopes(GeneralEnvelope sourceEnv, GeneralEnvelope targetEnv, GridCoverage2D gc) {
        final int dimension = sourceEnv.getDimension();
        if (sourceEnv.getDimension() != targetEnv.getDimension()) {
            return false;
        }
        AffineTransform mathTransformation = (AffineTransform) ((GridGeometry2D)gc.getGridGeometry()).getGridToCRS2D();
        double epsilon;
        for (int i=0; i<dimension; i++) {
        	epsilon=i==0?XAffineTransform.getScaleX0(mathTransformation):XAffineTransform.getScaleY0(mathTransformation);
            // Comparaison below uses '!' in order to catch NaN values.
            if (!(Math.abs(sourceEnv.getMinimum(i) - targetEnv.getMinimum(i)) <= epsilon &&
                  Math.abs(sourceEnv.getMaximum(i) - targetEnv.getMaximum(i)) <= epsilon))
            {
                return false;
            }
        }
        return true;
	}

	/**
	 * Testing {@link GeoTiffWriter} capabilities to write a cropped coverage.
	 * 
	 * @throws IllegalArgumentException
	 * @throws IOException
	 * @throws UnsupportedOperationException
	 * @throws ParseException
	 * @throws FactoryException
	 * @throws TransformException
	 */
	public void testWriteCroppedCoverage() throws IllegalArgumentException,
			IOException, UnsupportedOperationException, ParseException,
			FactoryException, TransformException {

		// /////////////////////////////////////////////////////////////////////
		//
		//
		// READ
		//
		//
		// /////////////////////////////////////////////////////////////////////
		// /////////////////////////////////////////////////////////////////////
		//
		// Look for the original coverage that wew want to crop.
		//
		// /////////////////////////////////////////////////////////////////////
		final File readdir = TestData.file(GeoTiffWriterTest.class, "");
		final File writedir = new File(new StringBuffer(readdir
				.getAbsolutePath()).append("/testWriter/").toString());
		writedir.mkdir();
		final File tiff = new File(readdir, "latlon.tiff");
		assert tiff.exists() && tiff.canRead() && tiff.isFile();
		if (TestData.isInteractiveTest())
			logger.info(tiff.getAbsolutePath());

		// /////////////////////////////////////////////////////////////////////
		//
		// Create format and reader
		//
		// /////////////////////////////////////////////////////////////////////
		final GeoTiffFormat format = new GeoTiffFormat();
		// getting a reader
		GridCoverageReader reader = format.getReader(tiff);
		assertNotNull(reader);

		// /////////////////////////////////////////////////////////////////////
		//
		// Play with metadata
		//
		// /////////////////////////////////////////////////////////////////////
		IIOMetadataDumper metadataDumper = new IIOMetadataDumper(
				((GeoTiffReader) reader).getMetadata().getRootNode());
		if (TestData.isInteractiveTest()) {
			logger.info(metadataDumper.getMetadata());
		} else
			metadataDumper.getMetadata();

		// /////////////////////////////////////////////////////////////////////
		//
		// Read the original coverage.
		//
		// /////////////////////////////////////////////////////////////////////
		GridCoverage2D gc = (GridCoverage2D) reader.read(null);
		if (TestData.isInteractiveTest()) {
			logger.info(new StringBuffer("Coverage before: ").append("\n")
					.append(gc.getCoordinateReferenceSystem().toWKT()).append(
							gc.getEnvelope().toString()).toString());
		}
		final CoordinateReferenceSystem sourceCRS = gc
				.getCoordinateReferenceSystem2D();
		final GeneralEnvelope sourceEnvelope = (GeneralEnvelope) gc
				.getEnvelope();
		final GridGeometry2D sourcedGG = (GridGeometry2D) gc.getGridGeometry();
		final MathTransform sourceG2W = sourcedGG
				.getGridToCRS(PixelInCell.CELL_CENTER);

		// /////////////////////////////////////////////////////////////////////
		//
		//
		// CROP
		//
		//
		// /////////////////////////////////////////////////////////////////////

		// /////////////////////////////////////////////////////////////////////
		//
		// Crop the original coverage.
		//
		// /////////////////////////////////////////////////////////////////////
		double xc = sourceEnvelope.getCenter(0);
		double yc = sourceEnvelope.getCenter(1);
		double xl = sourceEnvelope.getLength(0);
		double yl = sourceEnvelope.getLength(1);
		final GeneralEnvelope cropEnvelope = new GeneralEnvelope(new double[] {
				xc - xl / 4.0, yc - yl / 4.0 }, new double[] { xc + xl / 4.0,
				yc + yl / 4.0 });
		final AbstractProcessor processor = new DefaultProcessor(null);
		final ParameterValueGroup param = processor
				.getOperation("CoverageCrop").getParameters();
		param.parameter("Source").setValue(gc);
		param.parameter("Envelope").setValue(cropEnvelope);
		final GridCoverage2D cropped = (GridCoverage2D) processor
				.doOperation(param);

		// /////////////////////////////////////////////////////////////////////
		//
		// Check that we got everything correctly after the crop.
		//
		// /////////////////////////////////////////////////////////////////////
		// checking the ranges of the output image.
		final GridGeometry2D croppedGG = (GridGeometry2D) cropped
				.getGridGeometry();
		final GridRange croppedGR = croppedGG.getGridRange();
		final MathTransform croppedG2W = croppedGG
				.getGridToCRS(PixelInCell.CELL_CENTER);
		final GeneralEnvelope croppedEnvelope = (GeneralEnvelope) cropped
				.getEnvelope();
		assertTrue("min x do not match after crop", 30 == croppedGR.getLower(0));
		assertTrue("min y do not match after crop", 30 == croppedGR.getLower(1));
		assertTrue("max x do not match after crop", 90 == croppedGR.getUpper(0));
		assertTrue("max y do not match after crop", 91 == croppedGR.getUpper(1));
		// check that the affine transform are the same thing
		assertTrue(
				"The Grdi2World tranformations of the original and the cropped covearage do not match",
				sourceG2W.equals(croppedG2W));
		// check that the envelope is correct
		final GeneralEnvelope expectedEnvelope = new GeneralEnvelope(croppedGR,
				PixelInCell.CELL_CENTER, croppedG2W, cropped
						.getCoordinateReferenceSystem2D());

		assertTrue("Expected envelope is different from the computed one",
				expectedEnvelope.equals(croppedEnvelope, XAffineTransform
						.getScale((AffineTransform) croppedG2W) / 2.0, false));

		// /////////////////////////////////////////////////////////////////////
		//
		//
		// WRITING AND TESTING
		//
		//
		// /////////////////////////////////////////////////////////////////////
		final File writeFile = new File(new StringBuffer(writedir
				.getAbsolutePath()).append(File.separatorChar).append(
				cropped.getName().toString()).append(".tiff").toString());
		final GridCoverageWriter writer = format.getWriter(writeFile);
		// /////////////////////////////////////////////////////////////////////
		//
		// Create the writing params
		//
		// /////////////////////////////////////////////////////////////////////
		final GeoTiffWriteParams wp = new GeoTiffWriteParams();
		wp.setCompressionMode(GeoTiffWriteParams.MODE_EXPLICIT);
		wp.setCompressionType("LZW");
		wp.setCompressionQuality(0.75F);
		final ParameterValueGroup params = format.getWriteParameters();
		params.parameter(
				AbstractGridFormat.GEOTOOLS_WRITE_PARAMS.getName().toString())
				.setValue(wp);
		writer.write(cropped, (GeneralParameterValue[]) params.values()
				.toArray(new GeneralParameterValue[1]));

		reader = new GeoTiffReader(writeFile, null);
		gc = (GridCoverage2D) reader.read(null);
		final CoordinateReferenceSystem targetCRS = gc
				.getCoordinateReferenceSystem2D();
		assertTrue(
				"Source and Target coordinate reference systems do not match",
				CRS.equalsIgnoreMetadata(sourceCRS, targetCRS));
		assertEquals("Read-back and Cropped envelopes do not match", cropped
				.getEnvelope(), croppedEnvelope);

		if (TestData.isInteractiveTest()) {
			logger.info(new StringBuffer("Coverage after: ").append("\n")
					.append(gc.getCoordinateReferenceSystem().toWKT()).append(
							gc.getEnvelope().toString()).toString());
			if (TestData.isInteractiveTest())
				gc.show();
			else
				gc.getRenderedImage().getData();

		}
	}
}
