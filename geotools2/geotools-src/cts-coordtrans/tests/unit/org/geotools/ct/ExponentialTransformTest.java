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
package org.geotools.ct;

// J2SE and JAI dependencies
import java.util.Arrays;
import javax.media.jai.ParameterList;

// Geotools dependencies
import org.geotools.pt.*;
import org.geotools.cs.*;
import org.geotools.ct.*;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * Test the {@link ExponentialTransform1D} and {@link LogarithmicTransform1D} classes.
 *
 * @version $Id: ExponentialTransformTest.java,v 1.2 2002/10/10 14:44:52 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public class ExponentialTransformTest extends TransformationTest {
    /**
     * Returns the test suite.
     */
     public static Test suite() {
         return new TestSuite(ExponentialTransformTest.class);
     }
    
    /**
     * Constructs a test case with the given name.
     */
     public ExponentialTransformTest(final String name) {
        super(name);
    }

     /**
      * Test the {@link ExponentialTransform1D} and {@link LogarithmicTransform1D} classes
      * using simple know values.
      */
     public void testSimple() throws FactoryException, TransformException {
         final double[] POWER_2  = {0, 1, 2, 3,  4,  5,  6,   7,   8,   9,   10,   11,   12,   13};
         final double[] VALUE_2  = {1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096, 8192};
         final double[] POWER_10 = {  -5,   -4,   -3,   -2,   -1, 0,   +1,   +2,   +3,   +4,   +5};
         final double[] VALUE_10 = {1E-5, 1E-4, 1E-3, 1E-2, 1E-1, 1, 1E+1, 1E+2, 1E+3, 1E+4, 1E+5};
         runSimpleTest("Exponential",  2, POWER_2,  VALUE_2);
         runSimpleTest("Exponential", 10, POWER_10, VALUE_10);
         runSimpleTest("Logarithmic",  2, VALUE_2,  POWER_2);
         runSimpleTest("Logarithmic", 10, VALUE_10, POWER_10);
     }

     /**
      * Run a test.
      *
      * @param classification The classification name (e.g. "Exponential" or "Logarithmic").
      * @param base           The value for the "base" parameter.
      * @param input          Array of input values.
      * @param expected       Array of expected output values.
      */
     private void runSimpleTest(final String classification,
                                final double   base,
                                final double[] input,
                                final double[] expected)
        throws FactoryException, TransformException
     {
         assertEquals(input.length, expected.length);
         final MathTransformFactory factory = trFactory.getMathTransformFactory();
         final ParameterList     parameters = factory.getMathTransformProvider(classification)
                                                                           .getParameterList();
         parameters.setParameter("base", base);
         final MathTransform1D direct = (MathTransform1D) factory.createParameterizedTransform(
                                                                    classification, parameters);
         final MathTransform1D inverse = (MathTransform1D) direct.inverse();
         final CoordinatePoint   point = new CoordinatePoint(1);
         for (int i=0; i<expected.length; i++) {
             final double x = input[i];
             final double y = direct.transform(x);
             assertEquals("transform[x="+x+']', expected[i], y,          1E-6);
             assertEquals("inverse  [y="+y+']', x, inverse.transform(y), 1E-6);
             point.ord[0] = x;
             assertSame(direct.transform(point, point), point);
             assertEquals(y, point.ord[0], 1E-9);
         }
     }

     /**
      * Test the concatenation of {@link LinearTransform1D}, {@link ExponentialTransform1D}
      * and {@link LogarithmicTransform1D}.
      */
     public void testConcatenation() throws FactoryException, TransformException {
        final int numPts = 200;
        final double[] sourcePt = new double[numPts];
        final double[] targetPt = new double[numPts];
        final double[]  compare = new double[numPts];
        final double[]    delta = new double[numPts];
        final MathTransformFactory factory = trFactory.getMathTransformFactory();
        for (int pass=0; pass<200; pass++) {
            for (int i=0; i<numPts; i++) {
                sourcePt[i] = 20*random.nextDouble()+0.1;
            }
            MathTransform ctr = getRandomTransform();
            ctr.transform(sourcePt, 0, targetPt,  0, numPts);
            for (int i=random.nextInt(2)+1; --i>=0;) {
                final MathTransform1D step = getRandomTransform();
                ctr = (MathTransform1D) factory.createConcatenatedTransform(ctr, step);
                step.transform(targetPt, 0, targetPt, 0, numPts);
            }
            ctr.transform(sourcePt, 0, compare,  0, numPts);
            final double EPS = Math.pow(10, -5+countNonlinear(ctr));
            for (int i=0; i<numPts; i++) {
                delta[i] = Math.max(1E-9, Math.abs(targetPt[i]*EPS));
                if (targetPt[i] >= +1E+300) targetPt[i] = Double.POSITIVE_INFINITY;
                if (targetPt[i] <= -1E+300) targetPt[i] = Double.NEGATIVE_INFINITY;
            }
            assertPointsEqual("transform["+ctr+']', targetPt, compare, delta);
            /*
             * Test the inverse transform. It is difficult to get back the exact original value,
             * since expressions like  'pow(b1, pow(b2,x))'  tend to overflow or underflow very
             * fast. We are very tolerant for this test because of this (exponential expression
             * give exponential error). The 'testSimple' method tested the inverse transform in
             * a more sever way.
             */
            try {
                final MathTransform inv = ctr.inverse();
                Arrays.fill(delta, Math.pow(10, countNonlinear(inv)));
                inv.transform(targetPt, 0, compare,  0, numPts);
                for (int i=0; i<numPts; i++) {
                    if (!isReal(targetPt[i]) || !isReal(compare[i])) {
                        // Ignore all input points that produced NaN or infinity
                        // A succession of 2 "Exponentional" operation produces
                        // infinities pretty fast, so ignore it.
                        sourcePt[i] = Double.NaN;
                    }
                }
                assertPointsEqual("inverse["+inv+']', sourcePt, compare, delta);
            } catch (NoninvertibleTransformException exception) {
                // Some transforms may not be invertible. Ignore...
            }
        }
    }

    /**
     * Gets a random transform.
     */
    private MathTransform1D getRandomTransform() throws FactoryException {
        final String[] candidates = {
            "Logarithmic",
            "Exponential",
            "Affine"
        };
        final MathTransformFactory factory = trFactory.getMathTransformFactory();
        final String classification = candidates[random.nextInt(candidates.length)];
        final ParameterList param = factory.getMathTransformProvider(classification).getParameterList();
        if (classification.equalsIgnoreCase("Affine")) {
            param.setParameter("num_row", 2);
            param.setParameter("num_col", 2);
            param.setParameter("elt_0_0", random.nextDouble()*2+0.1); // scale
            param.setParameter("elt_0_1", random.nextDouble()*1 - 2); // offset
        } else {
            param.setParameter("base", random.nextDouble()*4 + 0.1);
        }
        return (MathTransform1D) factory.createParameterizedTransform(classification, param);
    }

    /**
     * Count the number of non-linear steps in a {@link MathTransform}.
     */
    private static int countNonlinear(final MathTransform transform) {
        if ((transform instanceof ExponentialTransform1D) ||
            (transform instanceof LogarithmicTransform1D))
        {
            return 1;
        }
        if (transform instanceof ConcatenatedTransform) {
            final ConcatenatedTransform ct = (ConcatenatedTransform) transform;
            return countNonlinear(ct.transform1) +
                   countNonlinear(ct.transform2);
        }
        return 0;
    }
}
