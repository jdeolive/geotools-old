/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Management Committee (PMC)
 * (C) 2003, Institut de Recherche pour le Développement
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
package org.geotools.gp;

// J2SE and JAI dependencies
import java.util.Map;
import java.util.logging.Logger;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.image.renderable.ParameterBlock;
import java.awt.image.RenderedImage;
import javax.imageio.ImageIO;

// JAI dependencies
import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;

// Geotools dependencies
import org.geotools.pt.Envelope;
import org.geotools.cs.Ellipsoid;
import org.geotools.cs.Projection;
import org.geotools.cs.CoordinateSystem;
import org.geotools.cs.FittedCoordinateSystem;
import org.geotools.cs.ProjectedCoordinateSystem;
import org.geotools.cs.GeographicCoordinateSystem;
import org.geotools.ct.MathTransformFactory;
import org.geotools.ct.MathTransform2D;
import org.geotools.ct.MathTransform;
import org.geotools.cv.Category;
import org.geotools.cv.SampleDimension;
import org.geotools.gc.GridCoverageTest;
import org.geotools.gc.GridCoverage;
import org.geotools.gc.GridGeometry;
import org.geotools.gc.GridRange;
import org.geotools.gc.Viewer;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Visual test of the "Resample" operation. A remote sensing image is projected from a fitted
 * coordinate system to a geographic one.
 *
 * @version $Id: ResampleTest.java,v 1.8 2003/07/23 10:33:14 desruisseaux Exp $
 * @author Remi Eve
 * @author Martin Desruisseaux
 */
public final class ResampleTest extends GridCoverageTest {
    /**
     * Set to <code>true</code> if the test case should show the projection results
     * in a windows. This flag is set to <code>true</code> if the test is run from
     * the command line through the <code>main(String[])</code> method. Otherwise
     * (for example if it is run from Maven), it is left to <code>false</code>.
     */
    private static boolean SHOW = false;

    /**
     * Small number for comparaisons.
     */
    private static final double EPS = 1E-6;

    /**
     * The source grid coverage.
     */
    private GridCoverage coverage;
    
    /**
     * Constructs a test case with the given name.
     */
    public ResampleTest(final String name) {
        super(name);
    }

    /**
     * Set up common objects used for all tests.
     */
    protected void setUp() throws Exception {
        super.setUp();
        coverage = getExample(0);
    }

    /**
     * Compare two affine transforms.
     */
    public static void assertEquals(final AffineTransform expected, final AffineTransform actual) {
        assertEquals("scaleX",     expected.getScaleX(),     actual.getScaleX(),     EPS);
        assertEquals("scaleY",     expected.getScaleY(),     actual.getScaleY(),     EPS);
        assertEquals("shearX",     expected.getShearX(),     actual.getShearX(),     EPS);
        assertEquals("shearY",     expected.getShearY(),     actual.getShearY(),     EPS);
        assertEquals("translateX", expected.getTranslateX(), actual.getTranslateX(), EPS);
        assertEquals("translateY", expected.getTranslateY(), actual.getTranslateY(), EPS);
    }

    /**
     * Returns the &quot;Sample to geophysics&quot; transform as an affine transform.
     */
    private static AffineTransform getAffineTransform(final GridCoverage coverage) {
        AffineTransform tr;
        tr = (AffineTransform)coverage.getGridGeometry().getGridToCoordinateSystem2D();
        tr = new AffineTransform(tr); // Change the type to the default Java2D implementation.
        return tr;
    }

    /**
     * Projète l'image dans le systeme de coordonnées
     * spécifié et affiche le résultat à l'écran.
     *
     * @param cs Le systeme de coordonées de destination.
     */
    private void projectTo(final CoordinateSystem cs, final GridGeometry geometry) {
        final GridCoverageProcessor processor = GridCoverageProcessor.getDefault();
        final String arg1; final Object value1;
        final String arg2; final Object value2;
        if (cs != null) {
            arg1="CoordinateSystem"; value1=cs;
            if (geometry != null) {
                arg2="GridGeometry"; value2=geometry;
            } else {
                arg2="InterpolationType"; value2="bilinear";
            }
        } else {
            arg1="GridGeometry";      value1=geometry;
            arg2="InterpolationType"; value2="bilinear";
        }
        final GridCoverage projected = processor.doOperation("Resample", coverage.geophysics(true),
                                                              arg1, value1, arg2, value2);
        if (SHOW) {
            Viewer.show(projected.geophysics(false));
        }
        final RenderedImage image = projected.getRenderedImage();
        if (image instanceof RenderedOp) {
            Logger.getLogger("org.geotools.gp")
                .info("Applied \""+((RenderedOp) image).getOperationName()+"\" JAI operation.");
        }
    }

    /**
     * Test the "Resample" operation with an identity transform.
     */
    public void testIdentity() {
        projectTo(coverage.getCoordinateSystem(), null);
    }

    /**
     * Test the "Resample" operation with a "Crop" transform.
     */
    public void testCrop() {
        projectTo(null, new GridGeometry(new GridRange(new Rectangle(50,50,200,200)), null));
    }


    /**
     * Test the "Resample" operation with an "Affine" transform.
     */
    public void testAffine() {
        AffineTransform atr = getAffineTransform(coverage);
        atr.preConcatenate(AffineTransform.getTranslateInstance(5, 5));
        MathTransform    tr = MathTransformFactory.getDefault().createAffineTransform(atr);
        CoordinateSystem cs = new FittedCoordinateSystem("F2", coverage.getCoordinateSystem(), tr, null);
        if (true) {
            projectTo(null, new GridGeometry(null, tr));
        } else {
            /*
             * Note: In current Resampler implementation, the affine transform effect tested
             *       here will not be visible with the simple viewer used here.  It would be
             *       visible however with more elaborated viewer like the one provided in the
             *       <code>org.geotools.renderer</code> package.
             */
            projectTo(cs, null);
        }
    }

    /**
     * Test the "Resample" operation with a stereographic coordinate system.
     */
    public void testStereographic() {
        final CoordinateSystem cs = new ProjectedCoordinateSystem("Stereographic",
                GeographicCoordinateSystem.WGS84,
                new Projection("Stereographic","Oblique_Stereographic",Ellipsoid.WGS84,null,null));
        projectTo(cs, null);
    }
    
    /**
     * Tests <var>X</var>,<var>Y</var> translation in the {@link GridGeometry} after
     * a "Resample" operation.
     */
    public void testTranslation() throws NoninvertibleTransformException {
        GridCoverage  grid = coverage;
        final int    transX =  -253;
        final int    transY =  -456;
        final double scaleX =  0.04;
        final double scaleY = -0.04;
        final ParameterBlock block = new ParameterBlock().addSource(grid.getRenderedImage())
                                                         .add((float)transX).add((float)transY);
        RenderedImage img = JAI.create("Translate", block);
        assertEquals("Incorrect X translation", transX, img.getMinX());
        assertEquals("Incorrect Y translation", transY, img.getMinY());
        /*
         * Create a grid coverage from the translated image but with the same envelope.
         * Consequently, the 'gridToCoordinateSystem' should be translated by the same
         * amount, with the opposite sign.
         */
        AffineTransform expected = getAffineTransform(grid);
        grid = new GridCoverage("Translated", img,  grid.getCoordinateSystem(),
                                grid.getEnvelope(), grid.getSampleDimensions(),
                                new GridCoverage[]{grid}, grid.getProperties());
        expected.translate(-transX, -transY);
        assertEquals(expected, getAffineTransform(grid));
        /*
         * Apply the "Resample" operation with a specific 'gridToCoordinateSystem' transform.
         * The envelope is left unchanged. The "Resample" operation should compute automatically
         * new image bounds.
         */
        final AffineTransform at = AffineTransform.getScaleInstance(scaleX, scaleY);
        final MathTransform2D tr = MathTransformFactory.getDefault().createAffineTransform(at);
        final GridGeometry geometry = new GridGeometry(null, tr);
        grid = GridCoverageProcessor.getDefault().doOperation(
                                                  "Resample",         grid,
                                                  "CoordinateSystem", grid.getCoordinateSystem(),
                                                  "GridGeometry",     geometry);
        assertEquals(at, getAffineTransform(grid));
        img = grid.getRenderedImage();
        expected.preConcatenate(at.createInverse());
        Point point = new Point(transX, transY);
        expected.transform(point, point); // Round toward neareast integer
        assertEquals("Incorrect X translation", point.x, img.getMinX());
        assertEquals("Incorrect Y translation", point.y, img.getMinY());
    }

    /**
     * Returns the test suite.
     */
    public static Test suite() {
        return new TestSuite(ResampleTest.class);
    }

    /**
     * Run the suit from the command line.
     */
    public static void main(final String[] args) {
        SHOW = true;
        org.geotools.resources.Geotools.init();
        junit.textui.TestRunner.run(suite());
    }
}
