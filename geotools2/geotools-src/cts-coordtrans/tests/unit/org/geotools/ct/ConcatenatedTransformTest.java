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
import java.util.Arrays;

// Geotools dependencies
import org.geotools.cs.*;
import org.geotools.ct.*;
import org.geotools.pt.*;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * Test the {@link ConcatenatedTransform} classes. Actually, there is many
 * {@link ConcatenatedTransform}, each optimized for special cases.   This
 * test try to test a wide range of subclasses.
 *
 * @version $Id: ConcatenatedTransformTest.java,v 1.2 2003/05/13 10:58:50 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public class ConcatenatedTransformTest extends TransformationTest {
    /**
     * Returns the test suite.
     */
     public static Test suite() {
         return new TestSuite(ConcatenatedTransformTest.class);
     }
    
    /**
     * Constructs a test case with the given name.
     */
    public ConcatenatedTransformTest(final String name) {
        super(name);
    }

    /**
     * Test the concatenation of linear transformations. Concatenation of linear
     * transformations should involve only matrix multiplication.  However, this
     * test will also create {@link ConcatenatedTransform} objects in order to
     * compare their results.
     */
    public void testLinear() throws FactoryException, TransformException {
        final MathTransformFactory factory = trFactory.getMathTransformFactory();
        final MathTransform[]   transforms = new MathTransform[2];
        final int numDim = 4;
        final int numPts = 250;
        for (int pass=0; pass<1000; pass++) {
            final int     dimSource = random.nextInt(numDim)+1;
            final int     dimTarget = random.nextInt(numDim)+1;
            final int     dimInterm = random.nextInt(numDim)+1;
            final Matrix    matrix1 = getRandomAffineTransform(dimSource, dimInterm);
            final Matrix    matrix2 = getRandomAffineTransform(dimInterm, dimTarget);
            final MathTransform tr1 = factory.createAffineTransform(matrix1);
            final MathTransform tr2 = factory.createAffineTransform(matrix2);
            final double[] sourcePt = new double[dimSource * numPts];
            final double[] intermPt = new double[dimInterm * numPts];
            final double[] targetPt = new double[dimTarget * numPts];
            final double[]  compare = new double[dimTarget * numPts];
            final double[]    delta = new double[dimTarget];
            for (int i=0; i<numPts; i++) {
                sourcePt[i] = 100*random.nextDouble()-50;
            }
            tr1.transform(sourcePt, 0, intermPt, 0, numPts);
            tr2.transform(intermPt, 0, targetPt, 0, numPts);
            Arrays.fill(delta, 1E-6);
            /*
             * Create two set of concatenated transform: the first one computed from matrix
             * multiplication;   the second one is forced to a ConcatenatedTransform object
             * for testing purpose.
             */
            transforms[0] = factory.createConcatenatedTransform  (tr1, tr2);
            transforms[1] = ConcatenatedTransform.create(factory, tr1, tr2);
            assertTrue (transforms[0] instanceof LinearTransform);
            assertFalse(transforms[1] instanceof LinearTransform);
            for (int i=0; i<transforms.length; i++) {
                final MathTransform transform = transforms[i];
                assertInterfaced(transform);
                assertEquals("dimSource["+i+']', dimSource, transform.getDimSource());
                assertEquals("dimTarget["+i+']', dimTarget, transform.getDimTarget());
                transform.transform(sourcePt, 0, compare, 0, numPts);
                if (i==0) {
                    if (dimTarget>dimInterm || dimInterm>dimSource) {
                        /*
                         * The two methods do not agree when one of the step transforms involve
                         * the "creation" of a coordinate (i.e. have output points with greater
                         * dimension than input points). Those "creative" matrix have more rows
                         * than columns, with last rows filled with only zeros. The "two steps"
                         * method fail in this case (i.e. produce 'NaN' of 'Infinity' numbers),
                         * while the "matrix multiplication" method continue to give a result.
                         * I'm not sure if this result is valid however.
                         */
                        continue;
                    }
                }
                String name = "transform["+i+"]("+dimSource+" -> "+dimInterm+" -> "+dimTarget+')';
                assertPointsEqual(name, targetPt, compare, delta);
            }
        }
    }

    /**
     * Returns a random affine transform.
     *
     * @param dimSource Number of dimension for input points.
     * @param dimTarget Number of dimension for outout points.
     */
    private Matrix getRandomAffineTransform(final int dimSource, final int dimTarget) {
        final Matrix matrix = new Matrix(dimTarget+1, dimSource+1);
        for (int j=0; j<dimTarget; j++) { // Don't touch to the last row!
            for (int i=0; i<=dimSource; i++) {
                matrix.setElement(j,i, 10*random.nextDouble()-5);
            }
            if (j <= dimSource) {
                matrix.setElement(j,j, 40*random.nextDouble()+10);
            }
            matrix.setElement(j,dimSource, 80*random.nextDouble()-40);
        }
        if (dimSource == dimTarget) {
            assertTrue("Affine", matrix.isAffine());
        }
        return matrix;
    }
}
