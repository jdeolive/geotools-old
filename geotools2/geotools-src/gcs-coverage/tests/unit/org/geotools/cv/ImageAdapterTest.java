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

// J2SE dependencies
import java.util.Random;
import java.awt.image.*;

// Geotools dependencies
import org.geotools.cv.*;
import org.geotools.ct.*;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Test the {@link ImageAdapter} implementation. Image adapter depends
 * heavily on {@link CategoryList}, so this one should be tested first.
 *
 * @version $Id: ImageAdapterTest.java,v 1.1 2002/07/23 17:57:25 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public class ImageAdapterTest extends TestCase {
    /**
     * Small value for comparaisons.
     */
    private static final double EPS = 1E-6;

    /**
     * Random number generator for this test.
     */
    private Random random;

    /**
     * A category list for a band.
     */
    private CategoryList band1;

    /**
     * Returns the test suite.
     */
    public static Test suite() {
        return new TestSuite(ImageAdapterTest.class);
    }
    
    /**
     * Constructs a test case with the given name.
     */
    public ImageAdapterTest(final String name) {
        super(name);
    }

    /**
     * Set up common objects used for all tests.
     */
    protected void setUp() throws Exception {
        super.setUp();
        random = new Random();
        band1 = new CategoryList(new Category[] {
            new Category("No data",     null, 0),
            new Category("Land",        null, 1),
            new Category("Clouds",      null, 2),
            new Category("Temperature", null, 3, 100, 0.1, 5),
            new Category("Foo",         null, 100, 120, -1, 3),
            new Category("Tarzan",      null, 120)
        });
    }

    /**
     * The the transformation using a random raster with only one band.
     */
    public void testOneBand() throws TransformException {
        final int SIZE = 64;

        final BufferedImage  source = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_BYTE_INDEXED);
        final DataBufferByte buffer = (DataBufferByte) source.getRaster().getDataBuffer();
        final byte[] array = buffer.getData(0);
        for (int i=0; i<array.length; i++) {
            array[i] = (byte) random.nextInt(121);
        }
        final RenderedImage target = ImageAdapter.getInstance(source, new CategoryList[]{band1});
        assertEquals(DataBuffer.TYPE_BYTE,  source.getSampleModel().getDataType());
        assertEquals(DataBuffer.TYPE_FLOAT, target.getSampleModel().getDataType());

        final double[] sourceData = source.getData().getSamples(0, 0, SIZE, SIZE, 0, (double[])null);
        final double[] targetData = target.getData().getSamples(0, 0, SIZE, SIZE, 0, (double[])null);
        band1.transform(sourceData, 0, sourceData, 0, sourceData.length);
        CategoryListTest.compare(sourceData, targetData, EPS);
    }
}
