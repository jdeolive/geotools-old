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
import java.util.Arrays;
import java.awt.image.*;

// JAI dependencies
import javax.media.jai.*;

// Geotools dependencies
import org.geotools.cv.*;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Test the {@link SampleDimension} implementation. Since <code>SampleDimension</code>
 * rely on {@link CategoryList} for many of its work, many <code>SampleDimension</code>
 * tests are actually <code>CategoryList</code> tests.
 *
 * @version $Id: SampleDimensionTest.java,v 1.5 2002/07/26 22:18:33 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public class SampleDimensionTest extends TestCase {
    /**
     * The categories making the sample dimension to test.
     */
    private static final String[] CATEGORIES = {
        "No data",
        "Clouds",
        "Lands"
    };

    /**
     * The "no data" values making the sample dimension to test.
     * There is one for each category in {@link #CATEGORIES}.
     */
    private static final int[] NO_DATA = {0, 1, 255};

    /**
     * The scale factor for the sample dimension to test.
     */
    private static final double scale  = 0.1;

    /**
     * The offset value for the sample dimension to test.
     */
    private static final double offset = 5.0;


    /**
     * The sample dimension to test.
     */
    private SampleDimension test;

    /**
     * Random number generator for this test.
     */
    private Random random;

    /**
     * Returns the test suite.
     */
    public static Test suite() {
        return new TestSuite(SampleDimensionTest.class);
    }
    
    /**
     * Constructs a test case with the given name.
     */
    public SampleDimensionTest(final String name) {
        super(name);
    }

    /**
     * Set up common objects used for all tests.
     */
    protected void setUp() throws Exception {
        super.setUp();
        random = new Random();

        assertEquals("setUp", CATEGORIES.length, NO_DATA.length);
        final Category[] categories = new Category[CATEGORIES.length+1];
        for (int i=0; i<CATEGORIES.length; i++) {
            categories[i] = new Category(CATEGORIES[i], null, NO_DATA[i]);
        }
        categories[CATEGORIES.length] = new Category("SST", null, 10, 200, scale, offset);
        test = new SampleDimension(categories, null);
    }

    /**
     * Test the consistency of the sample dimension.
     */
    public void testSampleDimension() {
        final double[] nodataValues = test.getNoDataValue();
        assertEquals("nodataValues.length", CATEGORIES.length, nodataValues.length);
        for (int i=0; i<CATEGORIES.length; i++) {
            assertEquals("nodataValues["+i+']', NO_DATA[i], nodataValues[i], 0);
        }
        assertTrue  ("identity", !test.getSampleToGeophysics().isIdentity());
        assertEquals("scale",     scale,  test.getScale(),        0);
        assertEquals("offset",    offset, test.getOffset(),       0);
        assertEquals("minimum",   0,      test.getMinimumValue(), 0);
        assertEquals("maximum",   255,    test.getMaximumValue(), 0);

        final SampleDimension invt = test.geophysics(true);
        assertTrue(test != invt);
        assertTrue  ("identity",  invt.getSampleToGeophysics().isIdentity());
        assertEquals("scale",     1,    invt.getScale(),        0);
        assertEquals("offset",    0,    invt.getOffset(),       0);
        assertEquals("minimum",   6,    invt.getMinimumValue(), 0);
        assertEquals("maximum",   24.9, invt.getMaximumValue(), 1E-7);
    }

    /**
     * Test the creation of an {@link ImageAdapter} using the image
     * operation registry. This allow to apply the operation in the
     * same way than other JAI operations, without any need for a
     * direct access to package-private method.
     */
    public void testImageAdapterCreation() {
        final OperationRegistry registry = JAI.getDefaultInstance().getOperationRegistry();
        assertNotNull(registry.getDescriptor("rendered", "GC_SampleTranscoding"));

        final BufferedImage       dummy = new BufferedImage(10, 10, BufferedImage.TYPE_BYTE_GRAY);
        final ParameterBlockJAI   param = new ParameterBlockJAI("GC_SampleTranscoding");
        final ParameterListDescriptor d = param.getParameterListDescriptor();
        assertTrue(d.getParamClasses()[0].equals( SampleDimension[].class ));

        try {
            JAI.create("GC_SampleTranscoding", param);
            fail();
        } catch (IllegalArgumentException expected) {
            // This is the expected exception: source required
        }

        param.addSource(dummy);
        try {
            JAI.create("GC_SampleTranscoding", param);
            fail();
        } catch (IllegalArgumentException expected) {
            // This is the expected exception: source required
        }

        param.setParameter("sampleDimensions", new SampleDimension[] {test});
        final RenderedOp op = JAI.create("GC_SampleTranscoding", param);
        assertTrue(op.getRendering() instanceof ImageAdapter);
    }
}
