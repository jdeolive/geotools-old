/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
 * (C) 2001, Institut de Recherche pour le Développement
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
package org.geotools.units;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Test the Unit class.
 *
 * @version $Id: UnitTest.java,v 1.4 2003/05/13 10:58:21 desruisseaux Exp $
 * @author Philip Crotwell
 */
public class UnitTest extends TestCase {

    /**
     * Returns the test suite.
     */
     public static Test suite() {
         return new TestSuite(UnitTest.class);
     }

    /**
     * Constructs a test case with the given name.
     */
    public UnitTest(final String name) {
        super(name);
    }

    /**
     * Set up common objects used for all tests.
     */
    protected void setUp() throws Exception {
        super.setUp();
    }

    /**
     * Test the Unit class loading.
     */
    public void testUnitClassLoad() {
        //Class c = org.geotools.units.Unit.class;
        try {
        Class c = Class.forName("org.geotools.units.Unit");
        } catch (ClassNotFoundException e) {
        fail(e.toString());
        }
    }

    /**
     * Test the KILOGRAM Unit.
     */
    public void testKILOGRAM() {
        assertNotNull(Unit.KILOGRAM);
    }

    /**
     * Test the METRE Unit.
     */
    public void testMETRE() {
        assertNotNull(Unit.METRE);
    }

    /**
     * Test the DAY Unit.
     */
    public void testDAY() {
        assertNotNull(Unit.DAY);
    }

    /**
     * Test the DEGREE Unit.
     */
    public void testDegree() {
        assertNotNull(Unit.DEGREE);
    }

    public void testLocalizedNames() {
        Unit.KILOGRAM.getLocalizedName();
        Unit.METRE.getLocalizedName();
        Unit.DAY.getLocalizedName();
    }
}
