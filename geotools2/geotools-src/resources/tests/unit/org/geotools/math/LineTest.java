/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
 * (C) 2003, Institut de Recherche pour le Développement
 * (C) 1998, Pêches et Océans Canada
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
package org.geotools.math;

// J2SE dependencies
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Test the {@link Line} class.
 *
 * @version $Id: LineTest.java,v 1.3 2003/05/13 10:58:21 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public final class LineTest extends TestCase {
    /**
     * Tolerance factor for comparaisons.
     */
    private static final double EPS = 1E-8;

    /**
     * Run the suit from the command line.
     */
    public static void main(final String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    /**
     * Returns the test suite.
     */
    public static Test suite() {
        return new TestSuite(LineTest.class);
    }

    /**
     * Constructs a test case with the given name.
     */
    public LineTest(final String name) {
        super(name);
    }

    /**
     * Test {@link Line#isoscelesTriangleBase}.
     */
    public void testIsoscelesTriangleBase() {
        final Line test = new Line();
        test.setLine(new Point2D.Double(20,30), new Point2D.Double(80,95));
        assertEquals("slope", 1.083333333333333333333333, test.getSlope(), EPS);
        assertEquals("y0",    8.333333333333333333333333, test.getY0(),    EPS);

        final double distance = 40;
        final Point2D summit = new Point2D.Double(27, -9); // An arbitrary point.
        final Line2D base = test.isoscelesTriangleBase(summit, distance);
        assertEquals("distance P1", distance, base.getP1().distance(summit), EPS);
        assertEquals("distance P2", distance, base.getP2().distance(summit), EPS);

        final double x=10; // Can be any arbitrary point.
        final double y=8;
        assertEquals("nearest colinear point", base.ptLineDist(x,y),
                     test.nearestColinearPoint(new Point2D.Double(x,y)).distance(x,y), EPS);
    }
}
