/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Management Committee (PMC)
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
 */
package org.geotools.cs;

// J2SE dependencies
import java.io.*;
import java.awt.geom.Point2D;
import java.rmi.RemoteException;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

// OpenGIS dependencies
import org.opengis.cs.CS_CoordinateSystem;

// Geotools dependencies
import org.geotools.units.Unit;
import org.geotools.ct.Adapters;
import org.geotools.resources.Arguments;


/**
 * Test the creation of {@link CoordinateSystem} objects.
 *
 * @version $Id: CreationTest.java,v 1.1 2003/08/04 22:10:09 desruisseaux Exp $
 */
public class CreationTest extends TestCase {
    /**
     * The output stream. Will be overwriten by the {@link #main}
     * if the test is run from the command line.
     */
    private static PrintWriter out = new PrintWriter(new StringWriter());

    /**
     * Returns the test suite.
     */
    public static Test suite() {
        return new TestSuite(CreationTest.class);
    }

    /**
     * Creates a new instance of <code>CreationTest</code>
     */
    public CreationTest(final String name) {
        super(name);
    }

    /**
     * Test the creation of new coordinate systems.
     *
     * @throws FactoryException if a coordinate system can't be created.
     * @throws RemoteException if a coordinate system can't be exported.
     */
    public void testCreation() throws FactoryException, RemoteException {
        out.println();
        out.println("create Coodinate System....1: ");
        final CoordinateSystemFactory factory = CoordinateSystemFactory.getDefault();

        final Ellipsoid airy1830;
        airy1830 = Ellipsoid.createEllipsoid("Airy1830", 6377563.396, 6356256.910, Unit.METRE);
        out.println();
        out.print("create Coodinate System....2: ");
        out.println(airy1830);

        final GeographicCoordinateSystem geogCS;
        HorizontalDatum hdatum = new HorizontalDatum("Airy1830", airy1830);
        geogCS = factory.createGeographicCoordinateSystem("Airy1830", hdatum);
        out.println();
        out.print("create Coodinate System....3");
        out.println(geogCS);

        final Projection p;
        p = new Projection("Great_Britian_National_Grid",
                           "Transverse_Mercator", airy1830,
                           new Point2D.Double(49, -2),
                           new Point2D.Double(400000, -100000));
        out.println();
        out.print("create Coodinate System....4");
        out.println(p);
            
        final CoordinateSystem projectCS;
        projectCS = factory.createProjectedCoordinateSystem("Great_Britian_National_Grid", geogCS, p);
        out.println();
        out.print("create Coodinate System....5");
        out.println(projectCS);

        final CS_CoordinateSystem cs_projectCS;
        cs_projectCS = Adapters.getDefault().export(projectCS);
        out.println();
        out.print("create Coodinate System....6: ");
        out.println(cs_projectCS);
    }

    /**
     * Run the test from the command line.
     *
     * @param args the command line arguments.
     */
    public static void main(final String[] args) {
        final Arguments arguments = new Arguments(args);
        if (arguments.getFlag("-verbose")) {
            out = arguments.out;
        }
        junit.textui.TestRunner.run(suite());
        out.close();
        System.exit(0);
    }
}
