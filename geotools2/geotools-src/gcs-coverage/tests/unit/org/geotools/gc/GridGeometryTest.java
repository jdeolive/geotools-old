/*
 * Geotools - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
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
package org.geotools.gc;

// J2SE dependencies
import java.util.Arrays;
import java.awt.geom.*;

// Geotools dependencies
import org.geotools.gc.*;
import org.geotools.cs.*;
import org.geotools.ct.*;
import org.geotools.pt.*;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Test the {@link GridGeometry} implementation.
 *
 * @version $Id: GridGeometryTest.java,v 1.1 2003/05/12 21:29:31 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public class GridGeometryTest extends TestCase {
    /**
     * Run the suite from the command line.
     */
    public static void main(final String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    /**
     * Returns the test suite.
     */
    public static Test suite() {
        return new TestSuite(GridGeometryTest.class);
    }
    
    /**
     * Constructs a test case with the given name.
     */
    public GridGeometryTest(final String name) {
        super(name);
    }

    /**
     * Test the construction with an identity transform.
     */
    public void testIdentity() {
        final MathTransformFactory factory = MathTransformFactory.getDefault();
        final int[] lower = new int[] {0, 0, 0};
        final int[] upper = new int[] {100, 200, 10};
        final MathTransform identity = factory.createIdentityTransform(3);
        final GridGeometry gg = new GridGeometry(new GridRange(lower,upper), identity);
        assertTrue(identity.isIdentity());
        assertTrue(gg.getGridToCoordinateSystem().isIdentity());
        assertTrue(gg.getGridToCoordinateSystem2D().isIdentity());
        assertEquals(3, gg.getGridToCoordinateSystem().getDimSource());
        assertEquals(2, gg.getGridToCoordinateSystem2D().getDimSource());
        assertTrue(gg.getGridToCoordinateSystem2D() instanceof AffineTransform);
    }

    /**
     * Test the construction from an envelope.
     */
    public void testEnvelope() {
        final int[]    lower   = new int[]    {   0,   0,  0};
        final int[]    upper   = new int[]    {  90,  45,  5};
        final double[] minimum = new double[] {-180, -90,  0};
        final double[] maximum = new double[] {+180, +90, 10};
        final GridGeometry  gg = new GridGeometry(new GridRange(lower,upper),
                                                  new Envelope(minimum, maximum), null);
        final AffineTransform tr = (AffineTransform) gg.getGridToCoordinateSystem2D();
        assertEquals(AffineTransform.TYPE_UNIFORM_SCALE |
                     AffineTransform.TYPE_TRANSLATION, tr.getType());

        assertEquals(4, tr.getScaleX(), 0);
        assertEquals(4, tr.getScaleY(), 0);
        assertEquals(-178, tr.getTranslateX(), 0);
        assertEquals( -88, tr.getTranslateY(), 0);
    }
}
