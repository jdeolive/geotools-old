/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
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

import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.io.File;

import javax.media.jai.ImageLayout;
import javax.media.jai.JAI;

import org.geotools.coverage.grid.GeneralGridRange;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.OverviewPolicy;
import org.geotools.factory.Hints;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.referencing.operation.matrix.XAffineTransform;
import org.geotools.test.TestData;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValue;

/**
 * @author Daniele Romagnoli, GeoSolutions
 * @author Simone Giannecchini (simboss), GeoSolutions
 * 
 * Testing {@link ECWReader}
 */
public final class ECWTest extends AbstractECWTestCase {

	/**
	 * file name of a valid ECW sample data to be used for tests. All ECW sample
	 * data found are too large to be placed in the test-data folder by means of
	 * the SVN.
	 * 
	 * We suggest to download a valid ECW sample file from this site:
	 * ftp://www.sjrwmd.com/2004_DOQQs/rgb_ecw/
	 * 
	 * Any zipped file contains a .ECW file as well as a HTML file containing a
	 * "Spatial Reference Information" paragraph where to find useful
	 * information about Projection, in order to build a valid .prj file.
	 */
	private final static String fileName = "sampledata.ecw";

	/**
	 * Creates a new instance of {@code ECWTest}
	 * 
	 * @param name
	 */
	public ECWTest(String name) {
		super(name);
	}

	public static final void main(String[] args) throws Exception {
		junit.textui.TestRunner.run(ECWTest.class);
	}

	public void test() throws Exception {
		if (!testingEnabled())
			return;

		// Preparing an useful layout in case the image is striped.
		final ImageLayout l = new ImageLayout();
		l.setTileGridXOffset(0).setTileGridYOffset(0).setTileHeight(512)
				.setTileWidth(512);

		Hints hints = new Hints();
		hints.add(new RenderingHints(JAI.KEY_IMAGE_LAYOUT, l));

		// get a reader
		final File file = TestData.file(this, fileName);

		// get a reader
		final AbstractGridCoverage2DReader reader = new ECWReader(file, hints);
		// /////////////////////////////////////////////////////////////////////
		//
		// read once
		//
		// /////////////////////////////////////////////////////////////////////
		GridCoverage2D gc = (GridCoverage2D) reader.read(null);
		assertNotNull(gc);
		if (TestData.isInteractiveTest())
			gc.show();
		else
			gc.getRenderedImage().getData();

		// /////////////////////////////////////////////////////////////////////
		//
		// read again with subsampling and crop
		//
		// /////////////////////////////////////////////////////////////////////
		final double cropFactor = 2.0;
		final int oldW = gc.getRenderedImage().getWidth();
		final int oldH = gc.getRenderedImage().getHeight();
		final Rectangle range = reader.getOriginalGridRange().toRectangle();
		final GeneralEnvelope oldEnvelope = reader.getOriginalEnvelope();
		final GeneralEnvelope cropEnvelope = new GeneralEnvelope(new double[] {
				oldEnvelope.getLowerCorner().getOrdinate(0)
						+ oldEnvelope.getLength(0) / cropFactor,
				oldEnvelope.getLowerCorner().getOrdinate(1)
						+ oldEnvelope.getLength(1) / cropFactor },
				new double[] { oldEnvelope.getUpperCorner().getOrdinate(0),
						oldEnvelope.getUpperCorner().getOrdinate(1) });
		cropEnvelope.setCoordinateReferenceSystem(reader.getCrs());

		final ParameterValue gg = (ParameterValue) ((AbstractGridFormat) reader
				.getFormat()).READ_GRIDGEOMETRY2D.createValue();
		gg.setValue(new GridGeometry2D(new GeneralGridRange(new Rectangle(0, 0,
				(int) (range.width / 2.0 / cropFactor),
				(int) (range.height / 2.0 / cropFactor))), cropEnvelope));
		gc = (GridCoverage2D) reader.read(new GeneralParameterValue[] { gg });
		assertNotNull(gc);
		// NOTE: in some cases might be too restrictive
		assertTrue(cropEnvelope.equals(gc.getEnvelope(), XAffineTransform
				.getScale(((AffineTransform) ((GridGeometry2D) gc
						.getGridGeometry()).getGridToCRS2D())) / 2, true));
		// this should be fine since we give 1 pixel tolerance
		assertEquals(oldW / 2.0 / (cropFactor), gc.getRenderedImage()
				.getWidth(), 1);
		assertEquals(oldH / 2.0 / (cropFactor), gc.getRenderedImage()
				.getHeight(), 1);
		if (TestData.isInteractiveTest()) {
			gc.show();
		} else
			gc.getRenderedImage().getData();

		// /////////////////////////////////////////////////////////////////////
		//
		// read ignoring overviews with subsampling and crop
		//
		// /////////////////////////////////////////////////////////////////////
		final ParameterValue policy = (ParameterValue) ((AbstractGridFormat) reader
				.getFormat()).OVERVIEW_POLICY.createValue();
		policy.setValue(OverviewPolicy.IGNORE);
		gc = (GridCoverage2D) reader.read(new GeneralParameterValue[] { gg,
				policy });
		assertNotNull(gc);
		// NOTE: in some cases might be too restrictive
		assertTrue(cropEnvelope.equals(gc.getEnvelope(), XAffineTransform
				.getScale(((AffineTransform) ((GridGeometry2D) gc
						.getGridGeometry()).getGridToCRS2D())) / 2, true));
		// this should be fine since we give 1 pixel tolerance
		assertEquals(oldW / cropFactor, gc.getRenderedImage().getWidth(), 1);
		assertEquals(oldH / cropFactor, gc.getRenderedImage().getHeight(), 1);

		if (TestData.isInteractiveTest()) {
			gc.show();
		} else
			gc.getRenderedImage().getData();
		if (TestData.isInteractiveTest()) {
			// printing CRS information
			LOGGER.info(gc.getCoordinateReferenceSystem().toWKT());
			LOGGER.info(gc.getEnvelope().toString());
		}
	}
}
