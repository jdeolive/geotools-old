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
package org.geotools.pt;

// J2SE dependencies
import java.util.Date;
import java.util.Locale;

// Geotools dependencies
import org.geotools.cs.*;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;


/**
 * Test formatting done by the {@link CoordinateFormat} class.
 *
 * @version $Id: CoordinateFormatTest.java,v 1.2 2003/05/13 10:58:50 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public class CoordinateFormatTest extends TestCase {
    /**
     * Run the suite from the command line.
     */
    public static void main(final String[] args) {
        TestRunner.run(suite());
    }

    /**
     * Returns the test suite.
     */
    public static Test suite() {
        return new TestSuite(CoordinateFormatTest.class);
    }

    /**
     * Constructs a test case with the given name.
     */
    public CoordinateFormatTest(final String name) {
        super(name);
    }

    /**
     * Test formatting.
     */
    public void testFormat() {
        final Date epoch = new Date(1041375600000L); // January 1st, 2003
        final CoordinateSystem cs = new CompoundCoordinateSystem("WGS84 3D + time",
                                        CompoundCoordinateSystem.WGS84,
                                        new TemporalCoordinateSystem("Time", epoch));
        final CoordinateFormat format = new CoordinateFormat(Locale.FRANCE);
        format.setCoordinateSystem(cs);

        assertEquals("23°46,8'E 12°44,4'S 127,9 4 janv. 2003",
                     format.format(new CoordinatePoint(new double[]{23.78, -12.74, 127.9, 3.2})));
    }
}
