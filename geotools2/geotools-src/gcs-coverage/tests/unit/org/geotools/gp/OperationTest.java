/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Management Committee (PMC)
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
package org.geotools.gp;

// J2SE dependencies
import java.io.*;
import java.util.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.awt.image.renderable.ParameterBlock;
import java.util.logging.Level;

// JAI dependencies
import javax.media.jai.*;

// Geotools dependencies
import org.geotools.pt.*;
import org.geotools.cs.*;
import org.geotools.cv.*;
import org.geotools.gc.*;
import org.geotools.gp.*;
import org.geotools.gp.jai.*;
import org.geotools.resources.Arguments;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Test the {@link OperationJAI} implementation.
 *
 * @version $Id: OperationTest.java,v 1.10 2003/08/03 20:15:04 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public class OperationTest extends GridCoverageTest {
    /**
     * The writer where to print diagnostic messages, or <code>null</code> if none.
     */
    private static PrintWriter out = null;

    /**
     * Small number for comparaison of floating point values.
     */
    private static final double EPS = 1E-6;

    /**
     * Constructs a test case with the given name.
     */
    public OperationTest(final String name) {
        super(name);
    }

    /**
     * Set up common objects used for all tests.
     */
    protected void setUp() throws Exception {
        super.setUp();
    }

    /**
     * Basic test of {@link GridCoverage}.
     */
    public void testGridCoverage() {
        // No test to do. It was already done by {@link GridCoverageTest}.
        // We don't want to reload all tested image...
    }

    /**
     * Test a simple {@link OpenrationJAI}.
     */
    public void testOperationJAI() {
        final OperationJAI operation = new OperationJAI("addConst");
        Writer output = out;
        if (output == null) {
            output = new StringWriter();
        }
        try {
            operation.print(output, null);
        } catch (IOException exception) {
            if (out != null) {
                exception.printStackTrace(out);
            }
            fail();
        }
        assertEquals("numSources",    1, operation.getNumSources());
        assertEquals("numParameters", 2, operation.getNumParameters());
    }

    /**
     * Test the "Recolor" operation.
     */
    public void testRecolor() {
        final Operation   operation = new RecolorOperation();
        final GridCoverage coverage = getRandomCoverage();
        final ParameterList   param = operation.getParameterList().setParameter("Source", coverage);
        final GridCoverage   result = operation.doOperation(param, null);
        assertTrue(!Arrays.equals(getARGB(coverage), getARGB(result)));
        assertTrue(!coverage.geophysics(true) .equals(result.geophysics(true )));
        assertTrue(!coverage.geophysics(false).equals(result.geophysics(false)));
    }

    /**
     * Returns the ARGB code for the specified coverage.
     */
    private static int[] getARGB(final GridCoverage coverage) {
        IndexColorModel colors = (IndexColorModel) coverage.getRenderedImage().getColorModel();
        final int[] ARGB = new int[colors.getMapSize()];
        colors.getRGBs(ARGB);
        return ARGB;
    }

    /**
     * Returns an image of type <code>byte</code> with the specified constant value.
     * The geographic coordinates will always range from -10 to +10.
     *
     * @param  value The constant value.
     * @param  size The image width and height.
     * @return A constant image.
     */
    private static GridCoverage getConstantCoverage(final byte value, final float size) {
        final RenderedImage image = JAI.create("Constant",
              new ParameterBlock().add(size)  // Width
                                  .add(size)  // Height
                                  .add(new Byte[] {new Byte(value)}), null);

        return new GridCoverage(String.valueOf(value), image,
                                GeographicCoordinateSystem.WGS84,
                                new Envelope(new Rectangle2D.Float(-10, -10, 20, 20)));
    }

    /**
     * Make sure that all sample values in the given coverage are equals to the given value.
     */
    private static void assertEquals(final double value, final GridCoverage coverage) {
        final Raster data = coverage.getRenderedImage().getData();
        final int xmin = data.getMinX();
        final int ymin = data.getMinY();
        final int xmax = data.getWidth()  + xmin;
        final int ymax = data.getHeight() + ymin;
        for (int y=ymin; y<ymax; y++) {
            for (int x=xmin; x<xmax; x++) {
                assertEquals("Unexpected sample value in raster.",
                             value, data.getSampleDouble(x,y,0), EPS);
            }
        }
    }

    /**
     * Test the "Combine" operation.
     */
    public void testCombine() {
        final double value0 = 10,  scale0 = 0.50;
        final double value1 = 35,  scale1 = 2.00;
        final double value2 = 52,  scale2 = 0.25,  offset = 4;
        final GridCoverage   src0 = getConstantCoverage((byte) value0, 200f);
        final GridCoverage   src1 = getConstantCoverage((byte) value1, 200f);
        final GridCoverage   src2 = getConstantCoverage((byte) value2, 200f);
        final Operation operation = new PolyadicOperation(CombineDescriptor.OPERATION_NAME);
        final ParameterList param = operation.getParameterList();
        param.setParameter("source0", src0);
        param.setParameter("source1", src1);
        param.setParameter("source2", src2);
        try {
            // We are not allowed to skip a source.
            param.setParameter("source4", src2);
            fail();
        } catch (IllegalArgumentException exception) {
            // This is the expected exception. Continue...
            if (out != null) {
                out.println(exception.getLocalizedMessage());
            }
        }
        /*
         * Set an invalid matrix (missing offset coefficient).
         * The operation should detect that the matrix is invalid.
         */
        param.setParameter("matrix", new double[][] {
            {scale0, scale1, scale2, offset},
            {scale0, 0,      scale2}
        });
        GridCoverage result;
        try {
            result = operation.doOperation(param, null);
            fail();
        } catch (IllegalArgumentException exception) {
            // This is the expected exception. Continue...
            if (out != null) {
                out.println(exception.getLocalizedMessage());
            }
        }
        /*
         * Set a valid matrix and test the operation.
         */
        param.setParameter("matrix", new double[][] {
            {scale0, scale1, scale2, offset}
        });
        result = operation.doOperation(param, null);
        assertEquals(value0*scale0 + value1*scale1 + value2*scale2 + offset, result);
        /*
         * New test with one source image omited.
         */
        param.setParameter("matrix", new double[][] {
            {scale0, 0, scale2, offset}
        });
        result = operation.doOperation(param, null);
        assertEquals(value0*scale0 + 0 + value2*scale2 + offset, result);
        /*
         * Test the dyalic case.
         */
        param.setParameter("source2", null);
        param.setParameter("matrix", new double[][] {
            {scale0, scale1, offset}
        });
        result = operation.doOperation(param, null);
        assertEquals(value0*scale0 + value1*scale1 + offset, result);
        /*
         * Test the combinaison with differents image size. The OperationJAI class
         * should automatically ressample image before to apply the operation.
         */
        final GridCoverage src0x = getConstantCoverage((byte) value0, 100f);
        final GridCoverage src1x = getConstantCoverage((byte) value1, 200f);
        final GridCoverage src2x = getConstantCoverage((byte) value2,  75f);
        param.setParameter("source0", src0x);
        param.setParameter("source1", src1x);
        param.setParameter("source2", src2x);
        param.setParameter("matrix", new double[][] {
            {scale0, scale1, scale2, offset}
        });
        result = operation.doOperation(param, null);
        assertEquals(value0*scale0 + value1*scale1 + value2*scale2 + offset, result);
    }

    /**
     * Returns the test suite.
     */
    public static Test suite() {
        return new TestSuite(OperationTest.class);
    }

    /**
     * Run the suit from the command line.
     */
    public static void main(final String[] args) {
        org.geotools.resources.Geotools.init(Level.INFO);
        final Arguments arguments = new Arguments(args);
        if (arguments.getFlag("-verbose")) {
            out = arguments.out;
        }
        junit.textui.TestRunner.run(suite());
    }
}
