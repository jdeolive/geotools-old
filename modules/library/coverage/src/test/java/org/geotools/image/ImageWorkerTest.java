/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2006, Geotools Project Managment Committee (PMC)
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
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.geotools.image;

import java.awt.Color;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;

import junit.framework.TestCase;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.test.TestData;


/**
 * Tests the {@link ImageWorker} implementation.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class ImageWorkerTest extends TestCase {
    /**
     * The image to use for testing purpose.
     */
    private static RenderedImage image;

    /**
     * The worker instance to test.
     */
    private ImageWorker worker;

    /**
     * Loads the image (if not already loaded) and creates the worker instance.
     */
    @Override
    protected void setUp() throws IOException {
        if (image == null) {
            final InputStream input = TestData.openStream(GridCoverage2D.class, "QL95209.png");
            image = ImageIO.read(input);
            input.close();
        }
        worker = new ImageWorker(image);
    }

    /**
     * Tests the {@link ImageWorker#makeColorTransparent} methods.
     * Some trivial tests are performed before.
     */
    public void testMakeColorTransparent() {
        assertTrue("Assertions should be enabled.", ImageWorker.class.desiredAssertionStatus());

        assertSame(image, worker.getRenderedImage());
        assertEquals(  1, worker.getNumBands());
        assertEquals( -1, worker.getTransparentPixel());
        assertTrue  (     worker.isBytes());
        assertFalse (     worker.isBinary());
        assertTrue  (     worker.isIndexed());
        assertTrue  (     worker.isColorSpaceRGB());
        assertFalse (     worker.isColorSpaceGRAYScale());
        assertFalse (     worker.isTranslucent());

        assertSame("Expected no operation.", image, worker.rescaleToBytes()           .getRenderedImage());
        assertSame("Expected no operation.", image, worker.forceIndexColorModel(false).getRenderedImage());
        assertSame("Expected no operation.", image, worker.forceIndexColorModel(true ).getRenderedImage());
        assertSame("Expected no operation.", image, worker.forceColorSpaceRGB()       .getRenderedImage());
        assertSame("Expected no operation.", image, worker.retainFirstBand()          .getRenderedImage());
        assertSame("Expected no operation.", image, worker.retainLastBand()           .getRenderedImage());

        // Following will change image, so we need to test after the above assertions.
        assertEquals(  0, worker.getMinimums()[0], 0);
        assertEquals(255, worker.getMaximums()[0], 0);
        assertNotSame(image, worker.getRenderedImage());
        assertSame("Expected same databuffer, i.e. pixels should not be duplicated.",
                   image.getTile(0,0).getDataBuffer(),
                   worker.getRenderedImage().getTile(0,0).getDataBuffer());

        assertSame(worker, worker.makeColorTransparent(Color.WHITE));
        assertEquals(255,  worker.getTransparentPixel());
        assertFalse (      worker.isTranslucent());
        assertSame("Expected same databuffer, i.e. pixels should not be duplicated.",
                   image.getTile(0,0).getDataBuffer(),
                   worker.getRenderedImage().getTile(0,0).getDataBuffer());
    }
}
