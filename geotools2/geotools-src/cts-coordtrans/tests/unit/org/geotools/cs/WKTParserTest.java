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
package org.geotools.cs;

// J2SE dependencies
import java.io.*;
import java.text.*;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Test the {@link CoordinateSystemEPSGFactory} implementation.
 *
 * @version $Id: WKTParserTest.java,v 1.1 2002/09/03 17:53:00 desruisseaux Exp $
 * @author Yann Cézard
 * @author Remi Eve
 * @author Martin Desruisseaux
 */
public class WKTParserTest extends TestCase {
    /**
     * The factory to test.
     */
    private CoordinateSystemFactory factory;

    /**
     * Returns the test suite.
     */
    public static Test suite() {
        return new TestSuite(WKTParserTest.class);
    }
    
    /**
     * Constructs a test case with the given name.
     */
    public WKTParserTest(final String name) {
        super(name);
    }

    /**
     * Set up common objects used for all tests.
     */
    protected void setUp() throws Exception {
        super.setUp();
        factory = CoordinateSystemFactory.getDefault();
    }

    /**
     * Parse all elements from the specified file.
     */
    public void testParsing() throws IOException, ParseException, FactoryException {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(
                getClass().getClassLoader().getResourceAsStream("test-data/test-cs.txt")));

        String line;
        while ((line=reader.readLine()) != null) {
            line = line.trim();
            if (line.length()==0 || line.startsWith("#")) {
                continue;
            }
            final CoordinateSystem cs;
            try {
                cs = factory.createFromWKT(line);
            } catch (FactoryException exception) {
                Throwable cause = exception.getCause();
                if (cause instanceof ParseException) {
                    throw (ParseException) cause;
                }
                throw exception;
            }
            System.out.println(cs);
        }
        reader.close();
    }

    /**
     * Test form the command line.
     */
    public static void main(String[] args) throws Exception {
        WKTParserTest test = new WKTParserTest(null);
        test.setUp();
        test.testParsing();
    }
}
