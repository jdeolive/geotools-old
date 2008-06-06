/*
 *    GeoTools - The Open Source Java GIS Tookit
 *    http://geotools.org
 * 
 *    (C) 2005-2008, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.coverage.processing;

import java.awt.geom.AffineTransform;
import java.awt.image.DataBuffer;
import java.awt.image.WritableRaster;

import javax.media.jai.RasterFactory;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.geotools.coverage.CoverageFactoryFinder;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.coverage.processing.operation.Extrema;
import org.geotools.coverage.processing.operation.Histogram;
import org.geotools.geometry.Envelope2D;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.referencing.operation.matrix.XAffineTransform;
import org.opengis.geometry.Envelope;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;

/**
 * Testing Extrema and {@link Histogram} operations.
 * 
 * @author Simone Giannecchini, GeoSolutions.
 * 
 */
public class StatisticsOperationsTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
		createFloatRaster();
		createByteRaster();
	}

	protected GridCoverage2D sampleFloatCoverage;

	protected GridCoverage2D sampleByteCoverage;

	/**
	 * @param name
	 */
	public StatisticsOperationsTest(String name) {
		super(name);
	}

	/**
	 * Run the suite from the command line.
	 */
	public static void main(final String[] args) {
		junit.textui.TestRunner.run(suite());
	}

	/**
	 * Returns the test suite.
	 */
	public static Test suite() {
		final TestSuite ts = new TestSuite();
		ts.addTest(new StatisticsOperationsTest("testHistogram"));
		ts.addTest(new StatisticsOperationsTest("testExtrema"));
		return ts;
	}

	/**
	 * Tests the creation of a floating point {@link WritableRaster}.
	 */
	private void createFloatRaster() {
		/*
		 * Set the pixel values. Because we use only one tile with one band, the
		 * code below is pretty similar to the code we would have if we were
		 * just setting the values in a matrix.
		 */
		final int width = 500;
		final int height = 500;
		final WritableRaster raster = RasterFactory.createBandedRaster(
				DataBuffer.TYPE_FLOAT, width, height, 1, null);
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				raster.setSample(x, y, 0, x + y);
			}
		}
		/*
		 * Set some metadata (the CRS, the geographic envelope, etc.) and
		 * display the image. The display may be slow, since the translation
		 * from floating-point values to some color (or grayscale) is performed
		 * on the fly everytime the image is rendered.
		 */
		final CoordinateReferenceSystem crs = DefaultGeographicCRS.WGS84;
		final Envelope envelope = new Envelope2D(crs, 0, 0, 30, 30);
		final GridCoverageFactory factory = CoverageFactoryFinder
				.getGridCoverageFactory(null);
		sampleFloatCoverage = factory.create("My grayscale float coverage",
				raster, envelope);

	}

	/**
	 * Tests the creation of a floating point {@link WritableRaster}.
	 */
	private void createByteRaster() {
		/*
		 * Set the pixel values. Because we use only one tile with one band, the
		 * code below is pretty similar to the code we would have if we were
		 * just setting the values in a matrix.
		 */
		final int width = 500;
		final int height = 500;
		final WritableRaster raster = RasterFactory.createBandedRaster(
				DataBuffer.TYPE_BYTE, width, height, 1, null);
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				// we exploit the clmaping capabilities of the sample model
				raster.setSample(x, y, 0, x + y);
			}
		}
		/*
		 * Set some metadata (the CRS, the geographic envelope, etc.) and
		 * display the image. The display may be slow, since the translation
		 * from floating-point values to some color (or grayscale) is performed
		 * on the fly everytime the image is rendered.
		 */
		final CoordinateReferenceSystem crs = DefaultGeographicCRS.WGS84;
		final Envelope envelope = new Envelope2D(crs, 0, 0, 30, 30);
		final GridCoverageFactory factory = CoverageFactoryFinder
				.getGridCoverageFactory(null);
		sampleByteCoverage = factory.create("My grayscale byte coverage",
				raster, envelope);

	}

	public void testExtrema() {

		// /////////////////////////////////////////////////////////////////////
		//
		// Create the operation for the Extrema with a ROI
		//
		// /////////////////////////////////////////////////////////////////////
		Operation2D op = (Operation2D) new DefaultProcessor(null)
				.getOperation("Extrema");
		ParameterValueGroup params = op.getParameters();
		params.parameter("Source").setValue(sampleFloatCoverage);
		params.parameter("xPeriod").setValue(
				1 * XAffineTransform
						.getScaleX0((AffineTransform) sampleFloatCoverage
								.getGridGeometry().getGridToCRS()));
		params.parameter("yPeriod").setValue(
				1 * XAffineTransform
						.getScaleY0((AffineTransform) sampleFloatCoverage
								.getGridGeometry().getGridToCRS()));

		final PrecisionModel pm = new PrecisionModel();
		final GeometryFactory gf = new GeometryFactory(pm, 0);
		final Envelope2D rect = sampleFloatCoverage.getEnvelope2D();
		final Coordinate[] coord = new Coordinate[] {
				new Coordinate(rect.getMinX(), rect.getMinY()),
				new Coordinate(rect.getMinX()
						+ (rect.getMaxX() - rect.getMinX()) / 2.0, rect
						.getMinY()),
				new Coordinate(rect.getMinX()
						+ (rect.getMaxX() - rect.getMinX()) / 2.0, rect
						.getMinY()
						+ (rect.getMaxY() - rect.getMinY()) / 2.0),
				new Coordinate(rect.getMinX(), rect.getMinY()
						+ (rect.getMaxY() - rect.getMinY()) / 2.0),
				new Coordinate(rect.getMinX(), rect.getMinY()) };
		final LinearRing ring = gf.createLinearRing(coord);
		final Polygon p = new Polygon(ring, null, gf);
		params.parameter("roi").setValue(p);

		GridCoverage2D coverage = (GridCoverage2D) op.doOperation(params, null);
		assertEquals(((double[]) coverage.getProperty("minimum"))[0], 250.0, 0);
		assertEquals(((double[]) coverage.getProperty("maximum"))[0], 748.0, 0);

		// /////////////////////////////////////////////////////////////////////
		//
		// Create the operation for the Extrema with a ROI and subsampling by 2
		//
		// /////////////////////////////////////////////////////////////////////
		op = new Extrema();
		params = op.getParameters();
		params.parameter("Source").setValue(sampleFloatCoverage);
		params.parameter("xPeriod").setValue(
				2 * XAffineTransform
						.getScaleX0((AffineTransform) sampleFloatCoverage
								.getGridGeometry().getGridToCRS()));
		params.parameter("yPeriod").setValue(
				2 * XAffineTransform
						.getScaleY0((AffineTransform) sampleFloatCoverage
								.getGridGeometry().getGridToCRS()));
		params.parameter("roi").setValue(p);
		coverage = (GridCoverage2D) op.doOperation(params, null);
		assertEquals(((double[]) coverage.getProperty("minimum"))[0], 250.0, 0);
		assertEquals(((double[]) coverage.getProperty("maximum"))[0], 746.0, 0);

	}

	public void testHistogram() {

		// /////////////////////////////////////////////////////////////////////
		//
		// Create the operation for the Extrema with a ROI
		//
		// /////////////////////////////////////////////////////////////////////
		Operation2D op = (Operation2D) new DefaultProcessor(null)
				.getOperation("Histogram");
		ParameterValueGroup params = op.getParameters();
		params.parameter("Source").setValue(sampleByteCoverage);
		params.parameter("xPeriod").setValue(
				1 * XAffineTransform
						.getScaleX0((AffineTransform) sampleByteCoverage
								.getGridGeometry().getGridToCRS()));
		params.parameter("yPeriod").setValue(
				1 * XAffineTransform
						.getScaleY0((AffineTransform) sampleByteCoverage
								.getGridGeometry().getGridToCRS()));

		final PrecisionModel pm = new PrecisionModel();
		final GeometryFactory gf = new GeometryFactory(pm, 0);
		final Envelope2D rect = sampleByteCoverage.getEnvelope2D();
		final Coordinate[] coord = new Coordinate[] {
				new Coordinate(rect.getMinX(), rect.getMaxY()),
				new Coordinate(rect.getMinX()
						+ (rect.getMaxX() - rect.getMinX()) / 16.0, rect
						.getMaxY()),
				new Coordinate(rect.getMinX()
						+ (rect.getMaxX() - rect.getMinX()) / 16.0, rect
						.getMaxY()
						- (rect.getMaxY() - rect.getMinY()) / 16.0),
				new Coordinate(rect.getMinX(), rect.getMaxY()
						- (rect.getMaxY() - rect.getMinY()) / 16.0),
				new Coordinate(rect.getMinX(), rect.getMaxY()) };
		final LinearRing ring = gf.createLinearRing(coord);
		final Polygon p = new Polygon(ring, null, gf);
		params.parameter("roi").setValue(p);

		GridCoverage2D coverage = (GridCoverage2D) op.doOperation(params, null);
		javax.media.jai.Histogram histogram = (javax.media.jai.Histogram) coverage
				.getProperty(Histogram.GT_SYNTHETIC_PROPERTY_HISTOGRAM);
		assertEquals(0, histogram.getBinSize(0, 255));
		assertEquals(1, histogram.getBinSize(0, 60));
		// /////////////////////////////////////////////////////////////////////
		//
		// Create the operation for the Extrema with a ROI and subsampling by 7
		//
		// /////////////////////////////////////////////////////////////////////
		op = new Histogram();
		params = op.getParameters();
		params.parameter("Source").setValue(sampleByteCoverage);
		params.parameter("xPeriod").setValue(
				7 * XAffineTransform
						.getScaleX0((AffineTransform) sampleByteCoverage
								.getGridGeometry().getGridToCRS()));
		params.parameter("yPeriod").setValue(
				7 * XAffineTransform
						.getScaleY0((AffineTransform) sampleByteCoverage
								.getGridGeometry().getGridToCRS()));
		params.parameter("roi").setValue(p);
		coverage = (GridCoverage2D) op.doOperation(params, null);
		histogram = (javax.media.jai.Histogram) coverage
				.getProperty(Histogram.GT_SYNTHETIC_PROPERTY_HISTOGRAM);
		assertEquals(0, histogram.getBinSize(0, 255));
		assertEquals(0, histogram.getBinSize(0, 60));
		assertEquals(1, histogram.getBinSize(0, 56));

	}

}
