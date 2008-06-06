/*
 *    GeoTools - The Open Source Java GIS Tookit
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
 */
package org.geotools.coverage.image;

import java.awt.HeadlessException;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.ImageIO;
import javax.media.jai.widget.ScrollingImagePanel;
import javax.swing.JFrame;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.geotools.image.ImageWorker;
import org.geotools.test.TestData;;
/**
 * Testing {@link ImageWorker} capabilities.
 * 
 * @author Simone Giannecchini
 * @since 2.3.x
 * 
 */
public class ImageWorkerTest extends TestCase {

	private BufferedImage worldImage;
	
    /**
     * Returns the test suite.
     */
    public static Test suite() {
        return new TestSuite(ImageWorkerTest.class);
    }
	/**
	 * @param arg0
	 */
	public ImageWorkerTest(String arg0) {
		super(arg0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		File fileImage = TestData.file(this, "world.PNG");
		assertNotNull("Unable to get the test file world.PNG, test data returned null",fileImage);
		assertTrue("Unable to get the test file world.PNG",fileImage.exists());
		this.worldImage = ImageIO.read(fileImage);
		assertNotNull("Returned image was null",worldImage);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		this.worldImage.flush();
		this.worldImage = null;
		super.tearDown();
	}

	public static void main(String[] args) {
		TestRunner.run(ImageWorkerTest.class);
	}

	/**
	 * Testing capability to write GIF image.
	 * 
	 * @throws IOException
	 * 
	 */
	public void testGIFImageWrite() throws IOException {
		// get the image of the world with transparency
		ImageWorker worker = new ImageWorker(worldImage);

	

		// visualize it
		if (TestData.isInteractiveTest()) {
			visualize(worker.getRenderedImage(), "Input GIF");

		} else
			worker.getPlanarImage().getAsBufferedImage();
		// assertions
//		assertTrue("wrong color model", worker.getRenderedImage()
//				.getColorModel() instanceof IndexColorModel);
//		assertTrue("wrong transparency model", worker.getRenderedImage()
//				.getColorModel().getTransparency() == Transparency.BITMASK);
//		assertTrue("wrong transparency index",
//				((IndexColorModel) worker.getRenderedImage().getColorModel())
//						.getTransparentPixel() == 255);

		// write it out as GIF on a file
		final File outFile = TestData.temp(this, "temp.gif");
		// go to index color model with floyd stenberg alg.
		worker.forceIndexColorModelForGIF(true);
		worker.writeGIF(outFile, "LZW", 0.75f);

		// read it back
		final ImageWorker worker2 = new ImageWorker(ImageIO.read(outFile));

		// re visualize it
		if (TestData.isInteractiveTest()) {
			visualize(worker2.getRenderedImage(), "GIF to file");
		} else
			worker.getPlanarImage().getAsBufferedImage();
//		// assertions
//		assertTrue("wrong color model", worker2.getRenderedImage()
//				.getColorModel() instanceof IndexColorModel);
//		assertTrue("wrong transparency model", worker2.getRenderedImage()
//				.getColorModel().getTransparency() == Transparency.BITMASK);
//		assertTrue("wrong transparency index",
//				((IndexColorModel) worker2.getRenderedImage().getColorModel())
//						.getTransparentPixel() == 255);

		// write on an output streams
		ImageIO.setUseCache(true);
		final OutputStream os = new FileOutputStream(outFile);
		worker = new ImageWorker(worldImage);
		worker.forceIndexColorModelForGIF(true);
		worker.writeGIF(os, "LZW", 0.75f);

		// read it back
		worker2.setImage(ImageIO.read(outFile));

		// re visualize it
		if (TestData.isInteractiveTest()) {
			visualize(worker2.getRenderedImage(), "GIF to output stream");
		} else
			worker.getPlanarImage().getAsBufferedImage();
//		// assertions
//		assertTrue("wrong color model", worker2.getRenderedImage()
//				.getColorModel() instanceof IndexColorModel);
//		assertTrue("wrong transparency model", worker2.getRenderedImage()
//				.getColorModel().getTransparency() == Transparency.BITMASK);
//		assertTrue("wrong transparency index",
//				((IndexColorModel) worker2.getRenderedImage().getColorModel())
//						.getTransparentPixel() == 255);

	}

	/**
	 * @param image
	 * @param string
	 * @throws HeadlessException
	 */
	private void visualize(final RenderedImage image, String string)
			throws HeadlessException {
		final JFrame f = new JFrame(string);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.getContentPane().add(new ScrollingImagePanel(image, 400, 400));
		f.pack();
		f.setVisible(true);
	}

	/**
	 * Testing JPEG capabilities
	 * 
	 * @throws IOException
	 */
	public void testJPEGWrite() throws IOException {
		// get the image of the world with transparency
		final ImageWorker worker = new ImageWorker(worldImage);

		// visualize it
		if (TestData.isInteractiveTest()) {
			visualize(worker.getRenderedImage(), "Input JPEG");

		}

		// /////////////////////////////////////////////////////////////////////
		// nativeJPEG  with compression JPEG-LS
		// TODO: Disabled for now, because Continuum fails in this case.
		// /////////////////////////////////////////////////////////////////////
		final File outFile = TestData.temp(this, "temp.jpeg");
//		worker.writeJPEG(outFile, "JPEG-LS", 0.75f, true);
//
//		// read it back
//		final ImageWorker worker2 = new ImageWorker(ImageIO.read(outFile));
//
//		// re visualize it
//		if (TestData.isInteractiveTest()) {
//			visualize(worker2.getRenderedImage(), "Native JPEG LS");
//		} else
//			worker.getPlanarImage().getAsBufferedImage();

		// /////////////////////////////////////////////////////////////////////
		// native JPEG compression
		// /////////////////////////////////////////////////////////////////////
		worker.setImage(worldImage);
		worker.writeJPEG(outFile, "JPEG", 0.75f, true);

		// read it back
		final ImageWorker worker2 = new ImageWorker(ImageIO.read(outFile));

		// re visualize it
		if (TestData.isInteractiveTest()) {
			visualize(worker2.getRenderedImage(), "native JPEG");
		} else
			worker.getPlanarImage().getAsBufferedImage();

		// /////////////////////////////////////////////////////////////////////
		// pure java JPEG compression
		// /////////////////////////////////////////////////////////////////////
		worker.setImage(worldImage);
		worker.writeJPEG(outFile, "JPEG", 0.75f, false);

		// read it back
		worker2.setImage(ImageIO.read(outFile));

		// re visualize it
		if (TestData.isInteractiveTest()) {
			visualize(worker2.getRenderedImage(), "Pure Java JPEG");
		} else
			worker.getPlanarImage().getAsBufferedImage();

	}
	/**
	 * Testing PNG capabilities
	 * 
	 * @throws IOException
	 */
	public void testPNGWrite() throws IOException {
		// get the image of the world with transparency
		final ImageWorker worker = new ImageWorker(worldImage);

		// visualize it
		if (TestData.isInteractiveTest()) {
			visualize(worker.getRenderedImage(), "Input file");

		}

		// /////////////////////////////////////////////////////////////////////
		// native png filtered compression 24 bits
		// /////////////////////////////////////////////////////////////////////
		final File outFile = TestData.temp(this, "temp.png");
		worker.writePNG(outFile, "FILTERED", 0.75f, true,false);

		// read it back
		final ImageWorker worker2 = new ImageWorker(ImageIO.read(outFile));

		// re visualize it
		if (TestData.isInteractiveTest()) {
			visualize(worker2.getRenderedImage(), "Native PNG24");
		} else
			worker.getPlanarImage().getAsBufferedImage();

		// /////////////////////////////////////////////////////////////////////
		// native png filtered compression 8 bits
		// /////////////////////////////////////////////////////////////////////
		worker.setImage(worldImage);
		worker.writePNG(outFile, "FILTERED", 0.75f, true,true);

		// read it back
		worker2.setImage(ImageIO.read(outFile));

		// re visualize it
		if (TestData.isInteractiveTest()) {
			visualize(worker2.getRenderedImage(), "native PNG8");
		} else
			worker.getPlanarImage().getAsBufferedImage();

		// /////////////////////////////////////////////////////////////////////
		// pure java png 24
		// /////////////////////////////////////////////////////////////////////
		worker.setImage(worldImage);
		worker.writePNG(outFile, "FILTERED", 0.75f, false,false);

		// read it back
		worker2.setImage(ImageIO.read(outFile));

		// re visualize it
		if (TestData.isInteractiveTest()) {
			visualize(worker2.getRenderedImage(), "Pure  PNG24");
		} else
			worker.getPlanarImage().getAsBufferedImage();

		
		// /////////////////////////////////////////////////////////////////////
		// pure java png 8
		// /////////////////////////////////////////////////////////////////////
		worker.setImage(worldImage);
		worker.writePNG(outFile, "FILTERED", 0.75f, false,true);

		// read it back
		worker2.setImage(ImageIO.read(outFile));

		// re visualize it
		if (TestData.isInteractiveTest()) {
			visualize(worker2.getRenderedImage(), "Pure  PNG8");
		} else
			worker.getPlanarImage().getAsBufferedImage();
	}

	public void testRGB2Palette(){
		//creating a worker
		final ImageWorker worker= new ImageWorker(worldImage);
		
		// visualize it
		if (TestData.isInteractiveTest()) {
			visualize(worker.getRenderedImage(), "Input file");

		}
		worker.forceIndexColorModelForGIF(true);
		
		//converto to index color bitmask
		// assertions
//		assertTrue("wrong color model", worker.getRenderedImage()
//				.getColorModel() instanceof IndexColorModel);
//		assertTrue("wrong transparency model", worker.getRenderedImage()
//				.getColorModel().getTransparency() == Transparency.BITMASK);
//		assertTrue("wrong transparency index",
//				((IndexColorModel) worker.getRenderedImage().getColorModel())
//						.getTransparentPixel() == 255);
		// re visualize it
		if (TestData.isInteractiveTest()) {
			visualize(worker.getRenderedImage(), "Paletted bitmask");
		} else
			worker.getPlanarImage().getAsBufferedImage();
		
		
		//go back to rgb
		worker.forceComponentColorModel();
		
//		// assertions
//		assertTrue("wrong color model", worker.getRenderedImage()
//				.getColorModel() instanceof ComponentColorModel);
//		assertTrue("wrong bands number", worker.getRenderedImage()
//				.getColorModel().getNumComponents()==4);
//		assertTrue("wrong transparency model", worker.getRenderedImage()
//				.getColorModel().getTransparency() == Transparency.TRANSLUCENT);

		// re visualize it
		if (TestData.isInteractiveTest()) {
			visualize(worker.getRenderedImage(), "RGB translucent");
		} else
			worker.getPlanarImage().getAsBufferedImage();
		
	}
}
