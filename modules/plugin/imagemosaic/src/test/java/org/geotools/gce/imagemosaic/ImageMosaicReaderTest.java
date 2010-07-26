/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2006-2008, Open Source Geospatial Foundation (OSGeo)
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

import java.awt.Color;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;

import javax.media.jai.PlanarImage;
import javax.media.jai.widget.ScrollingImagePanel;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import junit.framework.JUnit4TestAdapter;
import junit.textui.TestRunner;

import org.apache.commons.io.filefilter.FileFilterUtils;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.factory.Hints;
import org.geotools.parameter.Parameter;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.test.TestData;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValue;
import org.opengis.referencing.NoSuchAuthorityCodeException;

/**
 * Testing {@link ImageMosaicReader}.
 * 
 * @author Simone Giannecchini, GeoSolutions
 * @author Stefan Alfons Krueger (alfonx), Wikisquare.de 
 * @since 2.3
 * 
 */
@SuppressWarnings("deprecation")
public class ImageMosaicReaderTest extends Assert{

	private static ImageMosaicFormat FORMAT = new ImageMosaicFormat();
	
	public static junit.framework.Test suite() { 
	    return new JUnit4TestAdapter(ImageMosaicReaderTest.class); 
	}

	private URL rgbURL;

	private URL indexURL;

	private URL indexAlphaURL;
	
	private URL index_unique_paletteAlphaURL;

	private URL grayURL;

	private URL rgbAURL;

	private URL overviewURL;

//	private URL rgbJarURL;
//	
//	private URL indexJarURL;
//	
//	private URL indexAlphaJarURL;
//	
//	private URL grayJarURL;
	
	private boolean interactive;

	private URL timeURL;
	
	/**
	 * Tests the {@link ImageMosaicReader} with default parameters for the
	 * various input params.
	 * 
	 * @throws IOException
	 * @throws MismatchedDimensionException
	 * @throws NoSuchAuthorityCodeException
	 */
	@Test
	public void defaultParameterValue() throws IOException,	
			MismatchedDimensionException, NoSuchAuthorityCodeException {
	        final Hints hints = new Hints(Hints.DEFAULT_COORDINATE_REFERENCE_SYSTEM, DefaultGeographicCRS.WGS84);
	    
		final String baseTestName="testDefaultParameterValue-";
		imageMosaicSimpleParamsTest(rgbURL, null, null,baseTestName+rgbURL.getFile(), false, hints);
		imageMosaicSimpleParamsTest(rgbAURL, null,  null,baseTestName+rgbAURL.getFile(), false);
		imageMosaicSimpleParamsTest(overviewURL, null,null,baseTestName+overviewURL.getFile(), false);
		imageMosaicSimpleParamsTest(indexURL, null, null,baseTestName+indexURL.getFile(), false);
		imageMosaicSimpleParamsTest(grayURL, null, null,baseTestName+grayURL.getFile(), false);
		imageMosaicSimpleParamsTest(indexAlphaURL, null, null,baseTestName+indexAlphaURL.getFile(), false);
		imageMosaicSimpleParamsTest(index_unique_paletteAlphaURL, null, null,baseTestName+index_unique_paletteAlphaURL.getFile(), false);
		imageMosaicSimpleParamsTest(timeURL, null, null,baseTestName+timeURL.getFile(), false);
//
//		// And again with URL that points into a JAR
//		imageMosaicSimpleParamsTest(rgbJarURL, null, null,baseTestName+rgbJarURL.getFile(), false);
//		imageMosaicSimpleParamsTest(indexJarURL, null, null,baseTestName+indexJarURL.getFile(), false);
//		imageMosaicSimpleParamsTest(grayJarURL, null, null,baseTestName+grayJarURL.getFile(), false);
//		imageMosaicSimpleParamsTest(indexAlphaJarURL, null, null,baseTestName+indexAlphaJarURL.getFile(), false);
	}
	
	
	       /**
         * Tests the {@link ImageMosaicReader}
         * 
         * @param title
         * 
         * @param threshold
         * 
         * @throws IOException
         * @throws MismatchedDimensionException
         * @throws NoSuchAuthorityCodeException
         */
        private void imageMosaicSimpleParamsTest(
                        final URL testURL, 
                        final Color inputTransparent, 
                        final Color outputTransparent, 
                        final String title,
                        final boolean blend) throws IOException, MismatchedDimensionException,
                        NoSuchAuthorityCodeException {
            imageMosaicSimpleParamsTest(testURL, inputTransparent, outputTransparent, title, blend, (Hints)null);
            
        }
	
	/**
	 * Tests the {@link ImageMosaicReader}
	 * 
	 * @param title
	 * 
	 * @param threshold
	 * 
	 * @throws IOException
	 * @throws MismatchedDimensionException
	 * @throws NoSuchAuthorityCodeException
	 */
	private void imageMosaicSimpleParamsTest(
			final URL testURL, 
			final Color inputTransparent, 
			final Color outputTransparent, 
			final String title,
			final boolean blend,
			final Hints hints) throws IOException, MismatchedDimensionException,
			NoSuchAuthorityCodeException {

		// Get the resources as needed.
		Assert.assertNotNull(testURL);
		final ImageMosaicReader reader = getReader(testURL, hints);

		// limit yourself to reading just a bit of it
		final ParameterValue<Color> inTransp =  ImageMosaicFormat.INPUT_TRANSPARENT_COLOR.createValue();
		inTransp.setValue(inputTransparent);
		final ParameterValue<Color> outTransp =  ImageMosaicFormat.OUTPUT_TRANSPARENT_COLOR.createValue();
		outTransp.setValue(outputTransparent);
		final ParameterValue<Boolean> blendPV =ImageMosaicFormat.FADING.createValue();
		blendPV.setValue(blend);

		// Test the output coverage
		checkCoverage(reader, new GeneralParameterValue[] { inTransp, blendPV, outTransp }, title);
	}

	private ImageMosaicReader getReader(final URL testURL, final Hints hints) {
		return FORMAT.getReader(testURL, hints);
	}


	/**
	 * Tests the creation of a {@link GridCoverage2D} using the provided
	 * {@link ImageMosaicReader} as well as the provided {@link ParameterValue}.
	 * 
	 * @param reader
	 *            to use for creating a {@link GridCoverage2D}.
	 * @param value
	 *            that control the actions to take for creating a
	 *            {@link GridCoverage2D}.
	 * @param title
	 *            to print out as the head of the frame in case we visualize it.
	 * @return 
	 * @throws IOException
	 */
	private void checkCoverage(final ImageMosaicReader reader,
			GeneralParameterValue[] values, String title) throws IOException {
		// Test the coverage
		final GridCoverage2D coverage = getCoverage(reader, values);
		testCoverage(reader, values, title, coverage);
	}

	@SuppressWarnings("unchecked")
	private void testCoverage(final ImageMosaicReader reader,
			GeneralParameterValue[] values, String title,
			final GridCoverage2D coverage) {
		if (TestData.isInteractiveTest())
			show( coverage.getRenderedImage(), title);
		else
			PlanarImage.wrapRenderedImage( coverage.getRenderedImage()).getTiles();
		
		if(values!=null)	
			for(GeneralParameterValue pv:values){
				if(pv.getDescriptor().getName().equals(AbstractGridFormat.READ_GRIDGEOMETRY2D.getName())){
					
					Parameter<GridGeometry2D> param= (Parameter<GridGeometry2D>) pv;
					// check envelope if it has been requested
					Assert.assertEquals(param.getValue().getEnvelope(), coverage.getEnvelope());
	
				}
			}
		
		if (!interactive){
			// dispose stuff
			coverage.dispose(true);
			reader.dispose();
		}
	}

	private GridCoverage2D getCoverage(final ImageMosaicReader reader,
			GeneralParameterValue[] values) throws IOException {
		final GridCoverage2D coverage = (GridCoverage2D) reader.read(values);
		Assert.assertNotNull(coverage);
		return coverage;
	}

	/**
	 * Shows the provided {@link RenderedImage} ina {@link JFrame} using the
	 * provided <code>title</code> as the frame's title.
	 * 
	 * @param image
	 *            to show.
	 * @param title
	 *            to use.
	 */
	static void show(RenderedImage image, String title) {
		final JFrame jf = new JFrame(title);
		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jf.getContentPane().add(new ScrollingImagePanel(image, 800, 800));
		SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				jf.pack();
				jf.setVisible(true);

			}
		});

	}

//	/**
//	 * Tests {@link ImageMosaicReader} asking to crop the lower left quarter of
//	 * the input coverage.
//	 * 
//	 * @param title
//	 *            to use when showing image.
//	 * 
//	 * @throws IOException
//	 * @throws MismatchedDimensionException
//	 * @throws FactoryException
//	 */
//	private void imageMosaicCropTest(URL testURL, String title)
//			throws IOException, MismatchedDimensionException, FactoryException {
//
//		// Get the resources as needed.
//		Assert.assertNotNull(testURL);
//		final AbstractGridFormat format = getFormat(testURL);
//		final ImageMosaicReader reader = getReader(testURL, format);
//
//
//		// crop
//		final ParameterValue<GridGeometry2D> gg =  ImageMosaicFormat.READ_GRIDGEOMETRY2D.createValue();
//		final GeneralEnvelope oldEnvelope = reader.getOriginalEnvelope();
//		final GeneralEnvelope cropEnvelope = new GeneralEnvelope(new double[] {
//				oldEnvelope.getLowerCorner().getOrdinate(0)
//						+ oldEnvelope.getSpan(0) / 2,
//				oldEnvelope.getLowerCorner().getOrdinate(1)
//						+ oldEnvelope.getSpan(1) / 2 }, new double[] {
//				oldEnvelope.getUpperCorner().getOrdinate(0),
//				oldEnvelope.getUpperCorner().getOrdinate(1) });
//		cropEnvelope.setCoordinateReferenceSystem(reader.getCrs());
//		gg.setValue(new GridGeometry2D(PixelInCell.CELL_CENTER,reader.getOriginalGridToWorld(PixelInCell.CELL_CENTER),cropEnvelope,null));
//		final ParameterValue<Color> outTransp =  ImageMosaicFormat.OUTPUT_TRANSPARENT_COLOR.createValue();
//		outTransp.setValue(Color.black);
//
//
//		// test the coverage
//		checkCoverage(reader, new GeneralParameterValue[] { gg, outTransp },title);
//        DefaultEngineeringCRS.CARTESIAN_2D
//
//	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		TestRunner.run(ImageMosaicReaderTest.suite());

	}

	@Before
	public void setUp() throws Exception {
		//remove generated file
		cleanUp();
		
		rgbURL = TestData.url(this, "rgb/");
		timeURL = TestData.url(this, "time_geotiff");
//		rgbJarURL = new URL("jar:"+TestData.url(this, "rgb.jar").toExternalForm()+"!/rgb/mosaic.shp");
		
		overviewURL = TestData.url(this, "overview/");
		rgbAURL = TestData.url(this, "rgba/");
		
		indexURL = TestData.url(this, "index/");
//		indexJarURL = new URL("jar:"+TestData.url(this, "index.jar").toExternalForm()+"!/index/modis.shp");
		
		indexAlphaURL = TestData.url(this, "index_alpha/");
//		indexAlphaJarURL = new URL("jar:"+TestData.url(this, "index_alpha.jar").toExternalForm()+"!/index_alpha/modis.shp");
		
		grayURL = TestData.url(this, "gray/");
//		grayJarURL = new URL("jar:"+TestData.url(this, "gray.jar").toExternalForm()+"!/gray/dof.shp");
		
		index_unique_paletteAlphaURL = TestData.url(this,"index_alpha_unique_palette/");
//		index_unique_paletteAlphaJarURL = new URL("jar:"+TestData.url(this, "index_alpha_unique_palette.jar").toExternalForm()+"!/index_alpha_unique_palette/dof.shp");
		
		interactive = TestData.isInteractiveTest();

	}

	/**
	 * Cleaning up the generated files (shape and properties so that we recreate them.
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private void cleanUp() throws FileNotFoundException, IOException {
			if(interactive)
				return;
			File dir=TestData.file(this, "overview/");
			File[] files = dir.listFiles(
					(FilenameFilter)FileFilterUtils.notFileFilter(
							FileFilterUtils.orFileFilter(
									FileFilterUtils.orFileFilter(
											FileFilterUtils.suffixFileFilter("tif"),
											FileFilterUtils.suffixFileFilter("aux")
									),
									FileFilterUtils.nameFileFilter("datastore.properties")
							)
					)
			);
			for(File file:files){
				file.delete();
			}
			
			dir=TestData.file(this, "rgba/");
			files = dir.listFiles((FilenameFilter)FileFilterUtils.notFileFilter(
					FileFilterUtils.orFileFilter(
							FileFilterUtils.notFileFilter(FileFilterUtils.suffixFileFilter("png")),
							FileFilterUtils.notFileFilter(FileFilterUtils.suffixFileFilter("wld"))
					)));
			for(File file:files){
				file.delete();
			}
	}
	
	@After
	public void tearDown() throws FileNotFoundException, IOException{
		cleanUp();
	}


}
