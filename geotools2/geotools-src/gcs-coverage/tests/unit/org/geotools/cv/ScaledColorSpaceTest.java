/*
 * Geotools - OpenSource mapping toolkit
 * (C) 2002, Centre for Computational Geography
 * (C) 2002, Institut de Recherche pour le Développement
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
 *
 *
 * Contacts:
 *     UNITED KINGDOM: James Macgill
 *             mailto:j.macgill@geog.leeds.ac.uk
 *
 *     FRANCE: Surveillance de l'Environnement Assistée par Satellite
 *             Institut de Recherche pour le Développement / US-Espace
 *             mailto:seasnet@teledetection.fr
 *
 *     CANADA: Observatoire du Saint-Laurent
 *             Institut Maurice-Lamontagne
 *             mailto:osl@osl.gc.ca
 */
package org.geotools.cv;

// J2SE and JAI dependencies
import java.util.Random;
import java.awt.image.*;
import java.awt.*;

// Geotools dependencies
import org.geotools.gc.Viewer;
import org.geotools.resources.XMath;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Test the {@link ScaledColorSpace} implementation.
 * This is a visual test.
 *
 * @version $Id: ScaledColorSpaceTest.java,v 1.1 2002/08/09 18:41:23 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public class ScaledColorSpaceTest extends TestCase {
    /**
     * Random number generator for this test.
     */
    private Random random;

    /**
     * The minimal and maximal values to renderer.
     */
    private double minimum, maximum;

    /**
     * The scaled color space to test.
     */
    private ScaledColorSpace colors;

    /**
     * The image to use for test.
     */
    private RenderedImage image;

    /**
     * Returns the test suite.
     */
    public static Test suite() {
        return new TestSuite(ScaledColorSpaceTest.class);
    }
    
    /**
     * Constructs a test case with the given name.
     */
    public ScaledColorSpaceTest(final String name) {
        super(name);
    }

    /**
     * Set up common objects used for all tests.
     */
    protected void setUp() throws Exception {
        super.setUp();
        random  = new Random();
        minimum = random.nextDouble()*100;
        maximum = random.nextDouble()*200 + minimum + 10;
        colors  = new ScaledColorSpace(0, 1, minimum, maximum);

        final int transparency = Transparency.OPAQUE;
        final int datatype     = DataBuffer.TYPE_FLOAT;
        final ColorModel model = new ComponentColorModel(colors, false, false, transparency, datatype);
        final WritableRaster data = model.createCompatibleWritableRaster(200,200);
        final BufferedImage image = new BufferedImage(model, data, false, null);
        for (int x=data.getWidth(); --x>=0;) {
            for (int y=data.getHeight(); --y>=0;) {
                double v = XMath.hypot((double)x/data.getWidth() - 0.5,
                                       (double)y/data.getWidth() - 0.5);
                v = v*(maximum-minimum) + minimum;
                data.setSample(x,y,0,v);
            }
        }
        this.image = image;
    }

    /**
     * Test the color space.
     */
    public void testColorSpace() {
        assertEquals(minimum, colors.getMinValue(0), 1E-4);
        assertEquals(maximum, colors.getMaxValue(0), 1E-4);

        final float[] array = new float[1];
        final double step = (maximum-minimum) / 256;
        for (double x=minimum; x<maximum; x+=step) {
            array[0] = (float)x;
            assertEquals(x, colors.fromRGB(colors.toRGB(array))[0], 1E-3);
        }
    }

    /**
     * Run the visual test.
     */
    public static void main(final String[] args) throws Exception {
        final ScaledColorSpaceTest test = new ScaledColorSpaceTest(null);
        test.setUp();
        Viewer.show(test.image);
    }
}
