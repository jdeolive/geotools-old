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
package org.geotools.ct;

// J2SE dependencies
import java.awt.geom.*;

// Geotools dependencies
import org.geotools.pt.*;
import org.geotools.cs.*;
import org.geotools.ct.*;
import org.geotools.units.*;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * Test linear transformations. Linear transformations includes the
 * following classes:
 *
 * <ul>
 *   <li>{@link LinearTransform1D}</li>
 *   <li>{@link AffineTransform2D}</li>
 *   <li>{@link MatrixTransform}</li>
 * </ul>
 *
 * @version $Id: LinearTransformTest.java,v 1.3 2003/05/13 10:58:50 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public class LinearTransformTest extends TransformationTest {
    /**
     * Returns the test suite.
     */
     public static Test suite() {
         return new TestSuite(LinearTransformTest.class);
     }
    
    /**
     * Constructs a test case with the given name.
     */
     public LinearTransformTest(final String name) {
        super(name);
    }

    /**
     * Tests the {@link MatrixTransform} class.
     *
     * @throws TransformException if a transformation failed.
     */
    public void testMatrixTransform() throws TransformException {
        for (int pass=0; pass<10; pass++) {
            final AffineTransform transform = new AffineTransform();
            transform.rotate(Math.PI*random.nextDouble(), 100*random.nextDouble(), 100*random.nextDouble());
            transform.scale       (2*random.nextDouble(),   2*random.nextDouble());
            transform.shear       (2*random.nextDouble(),   2*random.nextDouble());
            transform.translate (100*random.nextDouble(), 100*random.nextDouble());

            compareTransforms("MatrixTransform", new MathTransform[] {
                new MatrixTransform(new Matrix(transform)),
                new AffineTransform2D(transform)
            });
        }
    }

    /**
     * Test various linear transformations. We test for many differents dimensions.
     * The factory class should have created specialized classes for 1D and 2D cases.
     * This test is used in order to ensure that specialized case produces the same
     * results than general cases.
     */
    public void testLinearTransform() throws FactoryException, TransformException {
        for (int pass=0; pass<5; pass++) {
            /*
             * Construct the reference matrix.
             */
            final int dimension = 10;
            final Matrix matrix = new Matrix(dimension+1, dimension+1);
            for (int i=0; i<dimension; i++) {
                matrix.setElement(i,i,         1000*Math.random()-500);
                matrix.setElement(i,dimension, 1000*Math.random()-500);
            }
            assertTrue(matrix.isAffine());
            /*
             * Construct all math transforms.
             */
            final MathTransformFactory factory = trFactory.getMathTransformFactory();
            final MathTransform[] transforms = new MathTransform[dimension];
            for (int i=1; i<=dimension; i++) {
                final Matrix sub = new Matrix(i+1, i+1);
                matrix.copySubMatrix(0, 0, i, i, 0, 0, sub);         // Scale terms
                matrix.copySubMatrix(0, dimension, i, 1, 0, i, sub); // Translation terms
                final MathTransform transform = transforms[i-1] = factory.createAffineTransform(sub);
                assertTrue  (sub.isAffine());
                assertEquals(sub, ((LinearTransform) transform).getMatrix());
                assertInterfaced(transform);
                assertTrue(i ==  transform.getDimSource());
            }
            /*
             * Check transformations and the inverse transformations.
             */
            assertTrue("MathTransform1D", transforms[0] instanceof MathTransform1D);
            assertTrue("MathTransform2D", transforms[1] instanceof MathTransform2D);
            assertEquals(matrix, ((LinearTransform) transforms[dimension-1]).getMatrix());
            compareTransforms("LinearTransforms", transforms);
            for (int i=0; i<transforms.length; i++) {
                transforms[i] = transforms[i].inverse();
            }
            compareTransforms("LinearTransforms.inverse", transforms);
        }
    }

    /**
     * Make sure that linear transformation preserve NaN values.
     * This is required for {@link org.geotools.cv.Category}.
     */
    public void testNaN() throws FactoryException, TransformException {
        final MathTransformFactory factory = trFactory.getMathTransformFactory();
        final Matrix matrix = new Matrix(2,2);
        matrix.setElement(0,0,0);
        for (int i=0; i<1000; i++) {
            final int rawBits = 0x7FC00000 + random.nextInt(100);
            final float value = Float.intBitsToFloat(rawBits);
            assertTrue("isNaN", Float.isNaN(value));
            matrix.setElement(0,1, value);
            final MathTransform1D tr = (MathTransform1D) factory.createAffineTransform(matrix);
            assertTrue("ConstantTransform1D", tr instanceof ConstantTransform1D);
            final float compare = (float) tr.transform(0);
            assertEquals("rawBits", rawBits, Float.floatToRawIntBits(compare));
        }
    }

    /**
     * Compare the transformation performed by many math transforms. If some transforms
     * don't have the same number of input or output dimension, only the first input or
     * output dimensions will be taken in account.
     *
     * @throws TransformException if a transformation failed.
     */
    private void compareTransforms(final String name, final MathTransform[] transforms)
        throws TransformException
    {
        /*
         * Initialisation...
         */
        final CoordinatePoint[] sources = new CoordinatePoint[transforms.length];
        final CoordinatePoint[] targets = new CoordinatePoint[transforms.length];
        int maxDimSource = 0;
        int maxDimTarget = 0;
        for (int i=0; i<transforms.length; i++) {
            final int dimSource = transforms[i].getDimSource();
            final int dimTarget = transforms[i].getDimTarget();
            if (dimSource > maxDimSource) maxDimSource = dimSource;
            if (dimTarget > maxDimTarget) maxDimTarget = dimTarget;
            sources[i] = new CoordinatePoint(dimSource);
            targets[i] = new CoordinatePoint(dimTarget);
        }
        /*
         * Test with an arbitrary number of randoms points.
         */
        for (int pass=0; pass<1000; pass++) {
            for (int j=0; j<maxDimSource; j++) {
                final double ord = 100*random.nextDouble();
                for (int i=0; i<sources.length; i++) {
                    final CoordinatePoint source = sources[i];
                    if (j < source.ord.length) {
                        source.ord[j] = ord;
                    }
                }
            }
            for (int j=0; j<transforms.length; j++) {
                assertSame(transforms[j].transform(sources[j], targets[j]), targets[j]);
            }
            /*
             * Compare all target points.
             */
            final StringBuffer buffer = new StringBuffer(name);
            buffer.append(": Compare transform[");
            final int lengthJ = buffer.length();
            for (int j=0; j<targets.length; j++) {
                buffer.setLength(lengthJ);
                buffer.append(j);
                buffer.append("] with [");
                final int lengthI = buffer.length();
                final CoordinatePoint targetJ = targets[j];
                for (int i=j+1; i<targets.length; i++) {
                    buffer.setLength(lengthI);
                    buffer.append(i);
                    buffer.append(']');
                    final String label = buffer.toString();
                    final CoordinatePoint targetI = targets[i];
                    assertTrue(targetJ.ord != targetI.ord);
                    for (int k=Math.min(targetJ.ord.length, targetI.ord.length); --k>=0;) {
                        assertEquals(label, targetJ.ord[k], targetI.ord[k], 1E-6);
                    }
                }
            }
        }
    }
}
