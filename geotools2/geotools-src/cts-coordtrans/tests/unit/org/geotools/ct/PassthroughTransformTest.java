/*
 * Geotools - OpenSource mapping toolkit
 * Copyright (C) 2003 Geotools Project Management Committee (PMC)
 *           (C) 2003, Institut de Recherche pour le Développement
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
 */
package org.geotools.ct;

// J2SE dependencies
import java.util.*;
import java.awt.geom.*;
import javax.media.jai.*;

// Geotools dependencies
import org.geotools.pt.*;
import org.geotools.cs.*;
import org.geotools.ct.*;
import org.geotools.resources.*;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * Test the following transforms:
 *
 * <ul>
 *   <li>{@link MathTransformFactory#createPassthroughTransform}</li>
 *   <li>{@link MathTransformFactory#createSubTransform}</li>
 *   <li>{@link MathTransformFactory#createFilterTransform}</li>
 * </ul>
 *
 * @version $Id: PassthroughTransformTest.java,v 1.1 2003/05/12 21:27:57 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public class PassthroughTransformTest extends TransformationTest {
    /**
     * Runs the tests with the textual test runner.
     */
    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }

    /**
     * Returns the test suite.
     */
    public static Test suite() {
        return new TestSuite(PassthroughTransformTest.class);
    }

    /**
     * Constructs a test case with the given name.
     */
    public PassthroughTransformTest(final String name) {
        super(name);
    }

    /**
     * Test the pass through transform using an affine transform. The "passthrough" of
     * such transform are optimized in a special way.
     */
    public void testLinear() throws FactoryException, TransformException {
        final MathTransformFactory mtFactory = trFactory.getMathTransformFactory();
        runTest(mtFactory.createAffineTransform(AffineTransform.getScaleInstance(4, 2)));
    }

    /**
     * Test the general passthrough transform.
     */
    public void testPassthrough() throws FactoryException, TransformException {
        final MathTransformFactory mtFactory = trFactory.getMathTransformFactory();
        final ParameterList param = mtFactory.getMathTransformProvider("Exponential")
                                             .getParameterList();
        runTest(mtFactory.createParameterizedTransform("Exponential", param));
    }

    /**
     * Test the pass through transform.
     */
    private void runTest(final MathTransform sub) throws FactoryException, TransformException {
        final MathTransformFactory mtFactory = trFactory.getMathTransformFactory();
        compare(sub, sub, 0);
        try {
            mtFactory.createPassThroughTransform(-1, sub, 0);
            fail("An illegal argument should have been detected");
        } catch (IllegalArgumentException e) {
            // This is the expected exception.
        }
        try {
            mtFactory.createPassThroughTransform(0, sub, -1);
            fail("An illegal argument should have been detected");
        } catch (IllegalArgumentException e) {
            // This is the expected exception.
        }
        assertSame("Failed to recognize that no passthrough transform was needed",
                   sub, mtFactory.createPassThroughTransform(0, sub, 0));

        final int subLower = 2;
        final int subUpper = subLower + sub.getDimSource();
        final MathTransform passthrough = mtFactory.createPassThroughTransform(subLower, sub, 1);
        assertEquals("Wrong number of source dimensions", sub.getDimSource() + subLower + 1, passthrough.getDimSource());
        assertEquals("Wrong number of target dimensions", sub.getDimTarget() + subLower + 1, passthrough.getDimTarget());
        compare(passthrough, sub, 2);

        assertTrue("Expected an identity transform", mtFactory.createSubTransform(passthrough,
                   JAIUtilities.createSequence(0, subLower-1), null).isIdentity());

        assertTrue("Expected an identity transform", mtFactory.createSubTransform(passthrough,
                   JAIUtilities.createSequence(subUpper, passthrough.getDimSource()-1), null).isIdentity());

        final IntegerSequence outputDimensions = new IntegerSequence();
        final IntegerSequence  inputDimensions = JAIUtilities.createSequence(subLower, subUpper-1);
        assertEquals("'createSubTransform' failed", sub, mtFactory.createSubTransform(passthrough,
                     inputDimensions, outputDimensions));
        final int[] expectedDimensions = new int[sub.getDimTarget()];
        for (int i=0; i<expectedDimensions.length; i++) {
            expectedDimensions[i] = subLower + i;
        }
        assertTrue("Unexpected output dimensions",
                   Arrays.equals(JAIUtilities.toArray(outputDimensions), expectedDimensions));
    }

    /**
     * Test the specified transform.
     *
     * @param mt The transform to test.
     * @param submt The sub transform.
     * @param subOffset Index of the first input/output dimension which correspond to
     *        <code>submt</code>.
     */
    private void compare(final MathTransform mt,
                         final MathTransform submt, final int subOffset)
            throws TransformException
    {
        final int  pointCount = 500;
        final int mtDimension = mt.getDimSource();
        final int atDimension = submt.getDimSource();
        final double[] atData = new double[pointCount * atDimension];
        final double[] mtData = new double[pointCount * mtDimension];
        for (int j=0; j<pointCount; j++) {
            for (int i=0; i<mtDimension; i++) {
                mtData[j*mtDimension + i] = 100*random.nextDouble() - 50;
            }
            for (int i=0; i<atDimension; i++) {
                atData[j*atDimension + i] = mtData[j*mtDimension + subOffset + i];
            }
        }
        if (atDimension == mtDimension) {
            assertTrue("Test arrays are not correctly build.", Arrays.equals(atData, mtData));
        }
        final double[] reference = (double[])mtData.clone();
        submt.transform(atData, 0, atData, 0, pointCount);
        mt   .transform(mtData, 0, mtData, 0, pointCount);
        assertTrue("'subOffset' argument too high", subOffset + atDimension <= mtDimension);
        for (int j=0; j<pointCount; j++) {
            for (int i=0; i<mtDimension; i++) {
                final double expected;
                if (i<subOffset || i>=subOffset+atDimension) {
                    expected = reference[j*mtDimension + i];
                } else {
                    expected = atData[j*atDimension + i - subOffset];
                }
                assertEquals("A transformed value is wrong",
                             expected, mtData[j*mtDimension + i], 1E-6);
            }
        }
    }
}
