/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2006, Geotools Project Managment Committee (PMC)
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

// J2SE and JAI dependencies
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.RenderedImage;
import java.util.logging.Logger;

import javax.media.jai.PlanarImage;
import javax.media.jai.RenderedOp;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageExamples;
import org.geotools.coverage.grid.GridCoverageTest;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.grid.InvalidGridGeometryException;
import org.geotools.coverage.grid.ViewType;
import org.geotools.coverage.grid.Viewer;
import org.geotools.factory.Hints;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.referencing.crs.DefaultDerivedCRS;
import org.geotools.referencing.operation.transform.ProjectiveTransform;
import org.geotools.util.logging.Logging;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

/**
 * Tests the crop operation.
 * 
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/modules/library/coverage/src/test/java/org/geotools/coverage/grid/CropTest.java $
 * @version $Id$
 * @author Simone Giannecchini,GeoSolutions
 * 
 * @since 2.3
 */
public class CropTest extends GridCoverageTest {
	/**
	 * The logger for trace informations in this test suite.
	 */
	private final static Logger LOGGER = Logging.getLogger("org.geotools.coverage.grid");

	/**
	 * {@code true} if the result should be displayed in windows during test
	 * execution. Default to {@code false}. This flag is set to {@code true}
	 * only if this test suite is executed explicitly though the {@link #main}
	 * method.
	 */
	private static boolean SHOW;

	/**
	 * The grid coverage to test.
	 */
	private GridCoverage2D coverage;

	/**
	 * Creates a test suite for the given name.
	 */
	public CropTest(String name) {
		super(name);
	}

	/**
	 * Set up common objects used for all tests.
	 */
	protected void setUp() throws Exception {
		super.setUp();
		coverage = GridCoverageExamples.getExample(0);
	}

	/**
	 * Returns the test suite.
	 */
	public static Test suite() {
		final TestSuite ts = new TestSuite();
		ts.addTest(new CropTest("testCrop"));
		ts.addTest(new CropTest("testCropRotated"));
		return ts;
	}

	/**
	 * Run the tests from the command line.
	 */
	public static void main(String[] args) {
		SHOW = true;
		Logging.GEOTOOLS.forceMonolineConsoleOutput(AbstractProcessor.OPERATION);
		junit.textui.TestRunner.run(suite());
	}

	/**
	 * Tests the "Crop" operation.
	 */
	public void testCrop() throws InvalidGridGeometryException,
			TransformException {
		final AbstractProcessor processor = AbstractProcessor.getInstance();
		////
		//
		// Get the source coverage and build the cropped envelope
		//
		////
		GridCoverage2D source = coverage.view(ViewType.NATIVE);
		final GeneralEnvelope oldEnvelope = (GeneralEnvelope) source
				.getEnvelope();
		final GeneralEnvelope cropEnvelope = new GeneralEnvelope(new double[] {
				oldEnvelope.getLowerCorner().getOrdinate(0)
						+ oldEnvelope.getLength(0) * 3 / 8,
				oldEnvelope.getLowerCorner().getOrdinate(1)
						+ oldEnvelope.getLength(1) * 3 / 8 }, new double[] {
				oldEnvelope.getLowerCorner().getOrdinate(0)
						+ oldEnvelope.getLength(0) * 5 / 8,
				oldEnvelope.getLowerCorner().getOrdinate(1)
						+ oldEnvelope.getLength(1) * 5 / 8 });
		cropEnvelope.setCoordinateReferenceSystem(oldEnvelope
				.getCoordinateReferenceSystem());
		
		////
		//
		// do the crop without conserving the envelope 
		//
		////
		ParameterValueGroup param = processor
				.getOperation("CoverageCrop").getParameters();
		param.parameter("Source").setValue(source);
		param.parameter("Envelope").setValue(cropEnvelope);
		GridCoverage2D cropped = (GridCoverage2D) processor.doOperation(param);
		if (SHOW) {
			Viewer.show(coverage, coverage.getName().toString());
			Viewer.show(cropped, cropped.getName().toString());
		} else {
			// Force computation
			assertNotNull(cropped.getRenderedImage().getData());
		}
		RenderedImage raster = cropped.getRenderedImage();
		assertEquals(169, raster.getMinX());
		assertEquals(173, raster.getMinY());
		assertEquals(112, raster.getWidth());
		assertEquals(115, raster.getHeight());
		assertEquals(((GridGeometry2D) cropped.getGridGeometry())
				.getGridToCRS2D(), ((GridGeometry2D) cropped.getGridGeometry())
				.getGridToCRS2D());
		assertNotSame(cropped.getEnvelope(), cropEnvelope);
		
		
		////
		//
		// do the crop  conserving the envelope 
		//
		////
		param = processor
				.getOperation("CoverageCrop").getParameters();
		param.parameter("Source").setValue(source);
		param.parameter("ConserveEnvelope").setValue(Boolean.TRUE);
		param.parameter("Envelope").setValue(cropEnvelope);
		cropped = (GridCoverage2D) processor.doOperation(param);
		if (SHOW) {
			Viewer.show(coverage, coverage.getName().toString());
			Viewer.show(cropped, cropped.getName().toString());
		} else {
			// Force computation
			assertNotNull(PlanarImage.wrapRenderedImage(cropped.getRenderedImage()).getTiles());
		}
		raster = cropped.getRenderedImage();
		assertEquals(169, raster.getMinX());
		assertEquals(173, raster.getMinY());
		assertEquals(112, raster.getWidth());
		assertEquals(115, raster.getHeight());
		assertEquals(cropped.getEnvelope(), cropEnvelope);
		
	
	}

	/**
	 * Tests the "Crop" operation when there exists a rotation in the world to
	 * grdi transformation
	 */
	public void testCropRotated() throws InvalidGridGeometryException,
			TransformException {
		// //
		//
		// get the test coverage
		//
		// //
		final GridCoverage2D source = coverage;

		// //
		//
		// get the grid-to-world and apply a transformation in order to get a
		// rotated coverage in the end
		//
		// //
		final AffineTransform atr = getAffineTransform(source);
		atr.preConcatenate(AffineTransform.getRotateInstance(Math.PI / 4.0));
		final MathTransform tr = ProjectiveTransform.create(atr);
		CoordinateReferenceSystem crs = source.getCoordinateReferenceSystem();
		crs = new DefaultDerivedCRS("F2", crs, tr, crs.getCoordinateSystem());
		final GridCoverage2D rotated = projectTo(source, crs, null, null, true);

		// //
		//
		// Preparing the crop. We want to get a rectangle that is locate at the
		// center of this coverage envelope that is large 1/4 f the original
		// width and tall 1/4 of the original height.
		//
		// ///
		final AbstractProcessor processor = AbstractProcessor.getInstance();
		final GeneralEnvelope oldEnvelope = (GeneralEnvelope) rotated
				.getEnvelope();
		final GeneralEnvelope cropEnvelope = new GeneralEnvelope(new double[] {
				oldEnvelope.getLowerCorner().getOrdinate(0)
						+ oldEnvelope.getLength(0) * 3 / 8,
				oldEnvelope.getLowerCorner().getOrdinate(1)
						+ oldEnvelope.getLength(1) * 3 / 8 }, new double[] {
				oldEnvelope.getLowerCorner().getOrdinate(0)
						+ oldEnvelope.getLength(0) * 5 / 8,
				oldEnvelope.getLowerCorner().getOrdinate(1)
						+ oldEnvelope.getLength(1) * 5 / 8 });
		cropEnvelope.setCoordinateReferenceSystem(oldEnvelope
				.getCoordinateReferenceSystem());

		// //
		//
		// do the crop without trying to conserve the envelope
		//
		// //
		ParameterValueGroup param = processor
				.getOperation("CoverageCrop").getParameters();
		param.parameter("Source").setValue(rotated);
		param.parameter("Envelope").setValue(cropEnvelope);

		GridCoverage2D cropped = (GridCoverage2D) processor.doOperation(param);
		if (SHOW) {
			Viewer.show(coverage, coverage.getName().toString());
			Viewer.show(cropped, cropped.getName().toString());
		} else {
			// Force computation
			assertNotNull(PlanarImage.wrapRenderedImage(cropped.getRenderedImage()).getTiles());
			assertNotNull(PlanarImage.wrapRenderedImage(coverage.getRenderedImage()).getTiles());

		}
		RenderedImage raster = cropped.getRenderedImage();
		assertEquals(111, raster.getMinX());
		assertEquals(116, raster.getMinY());
		assertEquals(228, raster.getWidth());
		assertEquals(228, raster.getHeight());
		assertEquals(((GridGeometry2D) rotated.getGridGeometry())
				.getGridToCRS2D(), ((GridGeometry2D) cropped.getGridGeometry())
				.getGridToCRS2D());

		// //
		//
		// get the roi and test it against the crop area
		//
		// //
		Object property = cropped.getProperty("GC_ROI");
		assertNotNull(property);
		assertTrue(property instanceof Polygon);
		Polygon roi = (Polygon) property;
		assertTrue(roi.getBounds().equals(
				new Rectangle(raster.getMinX(), raster.getMinY(), raster
						.getWidth(), raster.getHeight())));
		
		
		// //
		//
		// do the crop trying to conserve the envelope. We will not manage
		// to conserve it since we do not a simple scale and translate transform.
		//
		// //
		param = processor
				.getOperation("CoverageCrop").getParameters();
		param.parameter("Source").setValue(rotated);
		param.parameter("Envelope").setValue(cropEnvelope);
		param.parameter("ConserveEnvelope").setValue(Boolean.TRUE);

		cropped = (GridCoverage2D) processor.doOperation(param);
		if (SHOW) {
			Viewer.show(coverage, coverage.getName().toString());
			Viewer.show(cropped, cropped.getName().toString());
		} else {
			// Force computation
			assertNotNull(PlanarImage.wrapRenderedImage(cropped.getRenderedImage()).getTiles());
			assertNotNull(PlanarImage.wrapRenderedImage(coverage.getRenderedImage()).getTiles());

		}
		raster = cropped.getRenderedImage();
		assertEquals(111, raster.getMinX());
		assertEquals(116, raster.getMinY());
		assertEquals(228, raster.getWidth());
		assertEquals(228, raster.getHeight());
		assertEquals(((GridGeometry2D) rotated.getGridGeometry())
				.getGridToCRS2D(), ((GridGeometry2D) cropped.getGridGeometry())
				.getGridToCRS2D());
		assertNotSame(cropEnvelope,cropped.getEnvelope2D());

		// //
		//
		// get the roi and test it against the crop area
		//
		// //
		property = cropped.getProperty("GC_ROI");
		assertNotNull(property);
		assertTrue(property instanceof Polygon);
		roi = (Polygon) property;
		assertTrue(roi.getBounds().equals(
				new Rectangle(raster.getMinX(), raster.getMinY(), raster
						.getWidth(), raster.getHeight())));

	}




}
