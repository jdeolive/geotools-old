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
 *
 *     FRANCE: Surveillance de l'Environnement Assistée par Satellite
 *             Institut de Recherche pour le Développement / US-Espace
 *             mailto:seasnet@teledetection.fr
 */
package org.geotools.util;

// J2SE dependencies
import java.io.PrintWriter;
import java.io.IOException;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

// Geotools dependencies
import org.geotools.gc.GridCoverageTest;
import org.geotools.resources.Arguments;


/**
 * Test the {@link RangeSet} implementation.
 *
 * @version $Id: RangeSetTest.java,v 1.2 2003/07/23 13:43:29 desruisseaux Exp $
 * @author Andrea Aime
 */
public class RangeSetTest extends TestCase {
    /**
     * Set to a non-null value for printing some diagnostic message to the standard output.
     */
    private static PrintWriter out;

    /**
     * Run the suit from the command line. Run the test with the
     * "-print" option in order to print test to standard output.
     */
    public static void main(final String[] args) {
        final Arguments arguments = new Arguments(args);
        if (arguments.getFlag("-print")) {
            out = arguments.out;
        }
        arguments.getRemainingArguments(0);
        org.geotools.resources.Geotools.init();
        junit.textui.TestRunner.run(suite());
    }

    /**
     * Returns the test suite.
     */
    public static Test suite() {
        return new TestSuite(RangeSetTest.class);
    }
    
    /**
     * Constructs a test case with the given name.
     */
    public RangeSetTest(final String name) {
        super(name);
    }

    /**
     * Set up common objects used for all tests.
     */
    protected void setUp() throws Exception {
        super.setUp();
    }

    /**
     * Test {@link RangeSet#remove}.
     */
    public void testRangeRemoval() {
        RangeSet rs = new RangeSet(Double.class);
        rs.add(10.0, 22.0);
        rs.remove(8.0, 12.0);
        RangeSet rsResult = new RangeSet(Double.class);
        rsResult.add(12.0, 22.0);
        assertEquals("Lower removal", rs, rsResult);
        
        rs.remove(20.0, 30.0);
        rsResult = new RangeSet(Double.class);
        rsResult.add(12.0, 20.0);
        assertEquals("Upper removal", rs, rsResult);
        
        rs.remove(8.0, 10.0);
        assertEquals("Inferior null removal", rs, rsResult);
        rs.remove(8.0, 12.0);
        assertEquals("Inferior touch removal", rs, rsResult);
        
        rs.remove(22.0, 40.0);
        assertEquals("Upper null removal", rs, rsResult);
        rs.remove(20.0, 40.0);
        assertEquals("Upper touch removal", rs, rsResult);
        
        
        rs.remove(14.0, 16.0);
        rsResult = new RangeSet(Double.class);
        rsResult.add(12.0, 14.0);
        rsResult.add(16.0, 20.0);
        assertEquals("Central removal", rs, rsResult);
        
        rs.remove(15.0, 15.5);
        assertEquals("Central null removal", rs, rsResult);
        
        rs.remove(14.0, 16.0);
        assertEquals("Central touch null removal", rs, rsResult);
        
        rs.remove(15.0, 17.0);
        rsResult = new RangeSet(Double.class);
        rsResult.add(12.0, 14.0);
        rsResult.add(17.0, 20.0);
        assertEquals("Central right removal", rs, rsResult);
        
        rs.remove(13.0, 15.0);
        rsResult = new RangeSet(Double.class);
        rsResult.add(12.0, 13.0);
        rsResult.add(17.0, 20.0);
        assertEquals("Central left removal", rs, rsResult);
        
        rs.remove(12.5, 18.0);
        rsResult = new RangeSet(Double.class);
        rsResult.add(12.0, 12.5);
        rsResult.add(18.0, 20.0);
        assertEquals("Central both removal", rs, rsResult);
        
        rs.remove(18.5, 19.0);
        rsResult = new RangeSet(Double.class);
        rsResult.add(12.0, 12.5);
        rsResult.add(18.0, 18.5);
        rsResult.add(19.0, 20.0);
        assertEquals("Central removal 2", rs, rsResult);
        
        rs.remove(17.0, 19.0);
        rsResult = new RangeSet(Double.class);
        rsResult.add(12.0, 12.5);
        rsResult.add(19.0, 20.0);
        assertEquals("Central wipeout", rs, rsResult);
        
        rs.remove(0.0, 25.0);
        assertEquals("Full wipeout", 0, rs.size());
    }
}
