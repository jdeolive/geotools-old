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
package org.geotools.resources;

// J2SE dependencies
import java.util.logging.*;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Test the {@link MonolineFormatter} class.
 *
 * @version $Id: MonolineFormatterTest.java,v 1.4 2003/05/13 10:58:21 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public class MonolineFormatterTest extends TestCase {
    /**
     * Returns the test suite.
     */
     public static Test suite() {
         return new TestSuite(MonolineFormatterTest.class);
     }

    /**
     * Constructs a test case with the given name.
     */
    public MonolineFormatterTest(final String name) {
        super(name);
    }

    /**
     * Set up common objects used for all tests. This initialization is performed by
     * JUnit, but is <strong>not</strong> performed when the test is run from the
     * command line. Instead, the initialization on command line is controled by
     * the optional "-init" argument.
     */
    protected void setUp() throws Exception {
        super.setUp();
        MonolineFormatter.init("org.geotools");
    }

    /**
     * Run the test. This is only a visual test.
     */
    public void testInitialization() {
        final String[] namespaces = {
            "org.geotools.core",
            "org.geotools.resources",
            "org.geotools.cts",
            "org.opengis.cts"   // Non-geotools logger should not be affected.
        };
        for (int i=0; i<namespaces.length; i++) {
            System.out.println();
            System.out.print("Testing ");
            final Logger logger = Logger.getLogger(namespaces[i]);
            System.out.println(logger.getName());
            logger.severe ("Don't worry, just a test");
            logger.warning("This is an imaginary warning");
            logger.info   ("This is a pseudo-information message");
            logger.config ("Not really configuring anything...");
            logger.fine   ("This is a detailed (but useless) message\nWe log this one on two lines!");
            logger.finer  ("This is a debug message");
        }
    }

    /**
     * Run the test from the commande line. The {@link GeotoolsHandler} will be registered
     * only if the <code>-init</code> option was explicitely specified on the command line.
     * Otherwise, <code>GeotoolsHandler</code> will be used only if declared in
     * <code>jre/lib/logging.properties</code>.
     */
    public static void main(final String[] args) {
        final Arguments arguments = new Arguments(args);
        if (arguments.getFlag("-init")) {
            MonolineFormatter.init("org.geotools");
        }
        if (arguments.getFlag("-geotools")) {
            Geotools.init();
        }
        arguments.getRemainingArguments(0);
        new MonolineFormatterTest(null).testInitialization();
    }
}
