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
package org.geotools.cv;

// J2SE dependencies
import java.util.Arrays;
import java.rmi.RemoteException;

// OpenGIS dependencies
import org.opengis.cv.*;

// Geotools dependencies
import org.geotools.cv.*;
import org.geotools.units.Unit;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Test the {@link Adapters} implementation.
 *
 * @version $Id: AdaptersTest.java,v 1.3 2003/05/13 10:59:53 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public class AdaptersTest extends TestCase {
    /**
     * The adapters to test.
     */
    private Adapters adapters;

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
        return new TestSuite(AdaptersTest.class);
    }
    
    /**
     * Constructs a test case with the given name.
     */
    public AdaptersTest(final String name) {
        super(name);
    }

    /**
     * Set up common objects used for all tests.
     */
    protected void setUp() throws Exception {
        super.setUp();
        adapters = new Adapters(org.geotools.ct.Adapters.getDefault());
    }

    /**
     * Make sure that qualitative category produce the expected result.
     *
     * @throws RemoteException Should not happen.
     */
    public void testSampleDimension() throws RemoteException {
        final Category[] categories = new Category[] {
            new Category("No-data",     null, 0),
            new Category("Clouds",      null, 1),
            new Category("Lands",       null, 2),
            new Category("Height",      null, 3, 254, 10, -50),
            new Category("Clouds-2",    null, 254),
            new Category("Lands-2",     null, 255)
        };
        final SampleDimension dimension = new SampleDimension(categories, Unit.METRE);
        final CV_SampleDimension export = adapters.export(dimension);
        //
        // Test the usual 'wrap' method. We should gets exactly the
        // same object, since we are running this test locally.
        //
        assertSame("Adapters.wrap(CV_SampleDimension) didn't returned the same object.",
                    dimension, adapters.wrap(export));
        //
        // Test the 'wrap' method in the general case: internal
        // fields are reconstructed from the public methods.
        //
        final SampleDimension wrap = adapters.doWrap(export);
        assertTrue("Categories name are not preserved",
                Arrays.equals(dimension.getCategoryNames(null), wrap.getCategoryNames(null)));
        assertTrue("Pad values are not preserved",
                Arrays.equals(dimension.getNoDataValue(), wrap.getNoDataValue()));
        assertEquals("Scale factor is not the same", dimension.getScale(), wrap.getScale(), 0);
        assertEquals("Offset is not the same", dimension.getOffset(), wrap.getOffset(), 0);
    }
}
