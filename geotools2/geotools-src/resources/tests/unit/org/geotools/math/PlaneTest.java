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

// J2SE and Vecmath dependencies
import java.util.Random;
import javax.vecmath.Point3d;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Test the {@link Plane} class.
 *
 * @version $Id: PlaneTest.java,v 1.2 2003/05/13 10:58:21 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public final class PlaneTest extends TestCase {
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
        return new TestSuite(PlaneTest.class);
    }

    /**
     * Constructs a test case with the given name.
     */
    public PlaneTest(final String name) {
        super(name);
    }

    /**
     * Test {@link Plane#setPlane} methods.
     */
    public void testFit() {
        final Random  rd = new Random(457821698762354L);
        final Plane plan = new Plane();
        final Point3d P1 = new Point3d(100*rd.nextDouble()+25, 100*rd.nextDouble()+25, Math.rint(100*rd.nextDouble()+40));
        final Point3d P2 = new Point3d(100*rd.nextDouble()+25, 100*rd.nextDouble()+25, Math.rint(100*rd.nextDouble()+40));
        final Point3d P3 = new Point3d(100*rd.nextDouble()+25, 100*rd.nextDouble()+25, Math.rint(100*rd.nextDouble()+40));
        plan.setPlane(P1, P2, P3);
        assertEquals("P1", P1.z, plan.z(P1.x,P1.y), EPS);
        assertEquals("P2", P2.z, plan.z(P2.x,P2.y), EPS);
        assertEquals("P3", P3.z, plan.z(P3.x,P3.y), EPS);

        final double[] x = new double[4000];
        final double[] y = new double[4000];
        final double[] z = new double[4000];
        for (int i=0; i<z.length; i++) {
            x[i] = 40 + 100*rd.nextDouble();
            y[i] = 40 + 100*rd.nextDouble();
            z[i] = plan.z(x[i], y[i]) + 10*rd.nextDouble()-5;
        }
        final Plane copy = (Plane) plan.clone();
        final double eps = 1E-2; // We do expect some difference, but not much more than that.
        assertEquals("c",  copy.c,  plan.c,  eps);
        assertEquals("cx", copy.cx, plan.cx, eps);
        assertEquals("cy", copy.cy, plan.cy, eps);
    }
}
