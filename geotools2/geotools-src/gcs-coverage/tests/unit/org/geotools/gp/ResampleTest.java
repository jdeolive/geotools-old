/*
 * Geotools - OpenSource mapping toolkit
 * (C) 2003, Institut de Recherche pour le Développement
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
package org.geotools.gp;

// J2SE and JAI dependencies
import java.util.Map;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.image.RenderedImage;
import javax.imageio.ImageIO;

// Geotools dependencies
import org.geotools.pt.Envelope;
import org.geotools.cs.Ellipsoid;
import org.geotools.cs.Projection;
import org.geotools.cs.CoordinateSystem;
import org.geotools.cs.ProjectedCoordinateSystem;
import org.geotools.cs.GeographicCoordinateSystem;
import org.geotools.cv.Category;
import org.geotools.cv.SampleDimension;
import org.geotools.gc.GridCoverageTest;
import org.geotools.gc.GridCoverage;
import org.geotools.gc.Viewer;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Visual test of the "Resample" operation. A remote sensing image is projected from a fitted
 * coordinate system to a geographic one.
 *
 * @version $Id: ResampleTest.java,v 1.1 2003/02/13 23:01:04 desruisseaux Exp $
 * @author Remi Eve
 * @author Martin Desruisseaux
 */
public final class ResampleTest extends GridCoverageTest {
    /**
     * The source grid coverage.
     */
    private GridCoverage coverage;
    
    /**
     * Constructs a test case with the given name.
     */
    public ResampleTest(final String name) {
        super(name);
    }

    /**
     * Set up common objects used for all tests.
     */
    protected void setUp() throws Exception {
        super.setUp();
        coverage = getExample(0);
    }

    /**
     * Projete l'image dans le systeme de coordonnees
     * spécifié et affiche le résultat à l'écran.
     *
     * @param cs Le systeme de coordonees de destination.
     */
    private void projectTo(final CoordinateSystem cs) {
        final GridCoverageProcessor processor = GridCoverageProcessor.getDefault();
        final GridCoverage projected = processor.doOperation("Resample", coverage,
                                                             "CoordinateSystem", cs);
        Viewer.show(projected);
    }

    /**
     * Test the "Resample" operation with an identity transform.
     */
    public void testIdentity() {
        projectTo(coverage.getCoordinateSystem());
    }

    /**
     * Test the "Resample" operation with a stereographic coordinate system.
     */
    public void testStereographic() {
        final CoordinateSystem cs = new ProjectedCoordinateSystem("Stereographic",
                GeographicCoordinateSystem.WGS84,
                new Projection("Stereographic", "Oblique_Stereographic", Ellipsoid.WGS84, null, null));
        projectTo(cs);
    }

    /**
     * Returns the test suite.
     */
    public static Test suite() {
        return new TestSuite(ResampleTest.class);
    }

    /**
     * Run the suit from the command line.
     */
    public static void main(final String[] args) {
        org.geotools.resources.Geotools.init();
        junit.textui.TestRunner.run(suite());
    }
}
