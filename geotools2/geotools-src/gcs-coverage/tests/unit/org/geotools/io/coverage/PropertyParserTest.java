/*
 * Geotools - OpenSource mapping toolkit
 * (C) 2002, Centre for Computational Geography
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
package org.geotools.io.coverage;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Test the {@link PropertyParser} implementation.
 *
 * @version $Id: PropertyParserTest.java,v 1.2 2003/02/16 23:24:24 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public class PropertyParserTest extends TestCase {
    /**
     * Set it to <code>true</code> for printing some diagnostic message to the standard output.
     */
    private static final boolean PRINT = false;

    /**
     * Run the suit from the command line.
     */
    public static void main(final String[] args) {
        org.geotools.resources.Geotools.init();
        junit.textui.TestRunner.run(suite());
    }

    /**
     * Returns the test suite.
     */
    public static Test suite() {
        return new TestSuite(PropertyParserTest.class);
    }
    
    /**
     * Constructs a test case with the given name.
     */
    public PropertyParserTest(final String name) {
        super(name);
    }

    /**
     * Set up common objects used for all tests.
     */
    protected void setUp() throws Exception {
        super.setUp();
    }

    /**
     * Test the addition of alias.
     */
    public void testAlias() throws PropertyException {
        final PropertyParser parser = new PropertyParser();
        /*
         * Test "add" operations.
         */
        parser.add("Alias 1", "Value 1");
        parser.add("Alias 2", "Value 2");
        try {
            parser.add("  alias  1", "Value X");
            fail(); // We should not get there.
        } catch (AmbiguousPropertyException exception) {
            // This is the expected exception.
            if (PRINT) {
                System.out.println(exception);
            }
        }
        parser.add("Alias 1", "Value 1"); // Already defined
        parser.add("Alias 3", "Value 3");
        /*
         * Test "addAlias" operations.
         */
        parser.addAlias(PropertyParser.X_RESOLUTION, "Alias 1");
        parser.addAlias(PropertyParser.Y_RESOLUTION, "Alias 2");
        parser.addAlias(PropertyParser.Y_RESOLUTION, "Alias 2bis");
        parser.addAlias(PropertyParser.X_RESOLUTION, "Alias 1bis");
        parser.addAlias(PropertyParser.X_RESOLUTION, "Alias 1"); // Already defined
        try {
            parser.addAlias(PropertyParser.X_RESOLUTION, "Alias 2");
            fail(); // We should not get there.
        } catch (AmbiguousPropertyException exception) {
            // This is the expected exception.
            if (PRINT) {
                System.out.println(exception);
            }
        }
        parser.add("Alias 2bis", "Value 2");
        try {
            parser.add("Alias 1bis", "Value 2");
            fail(); // We should not get there.
        } catch (AmbiguousPropertyException exception) {
            // This is the expected exception.
            if (PRINT) {
                System.out.println(exception);
            }
        }
        /*
         * Test "get" operations.
         */
        assertEquals("Value 1", parser.get(PropertyParser.X_RESOLUTION));
        assertEquals("Value 2", parser.get(PropertyParser.Y_RESOLUTION));
        try {
            parser.get(PropertyParser.DATUM);
            fail(); // We should not get there.
        } catch (MissingPropertyException exception) {
            // This is the expected exception.
            if (PRINT) {
                System.out.println(exception);
            }
        }
        /*
         * Test "getAsDouble" and "getAsInt" operations.
         */
        parser.add("ULX", "40");
        parser.add("ULY", "12.5");
        parser.addAlias(PropertyParser.X_MINIMUM, "ULX");
        parser.addAlias(PropertyParser.Y_MAXIMUM, "ULY");
        assertEquals(40,   parser.getAsDouble(PropertyParser.X_MINIMUM), 0);
        assertEquals(12.5, parser.getAsDouble(PropertyParser.Y_MAXIMUM), 0);
        assertEquals(40,   parser.getAsInt   (PropertyParser.X_MINIMUM));
        try {
            parser.getAsInt(PropertyParser.Y_MAXIMUM);
            fail(); // We should not get there.
        } catch (PropertyException exception) {
            // This is the expected exception.
            if (PRINT) {
                System.out.println(exception);
            }
        }
        if (PRINT) {
            System.out.println();
            System.out.println(parser);
        }
    }
}
