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
 */
package org.geotools.gui.swing;

// J2Se dependencies
import java.awt.*;
import javax.swing.*;
import java.util.Locale;

// JUnit dependencies
import junit.framework.*;

// Geotools dependencies
import org.geotools.resources.Arguments;
import org.geotools.gc.GridCoverageTest;


/**
 * Tests a set of widgets.
 *
 * @version $Id: MiscellaneousWidgetTest.java,v 1.1 2003/03/28 16:45:58 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public class MiscellaneousWidgetTest extends TestCase {
    /**
     * The location of the next frame to show.
     */
    private int location;

    /**
     * Construct the test case.
     */
    public MiscellaneousWidgetTest(final String name) {
        super(name);
    }

    /**
     * Run the test case from the command line.
     */
    public static void main(final String[] args) {
        final Arguments arguments = new Arguments(args);
        Locale.setDefault(arguments.locale);
        junit.textui.TestRunner.run(suite());
    }

    /**
     * Returns the suite of tests.
     */
    public static Test suite() {
        TestSuite suite = new TestSuite(MiscellaneousWidgetTest.class);
        return suite;
    }

    /**
     * Show a component.
     */
    private void show(final Component component, final String title) {
        final JFrame frame = new JFrame(title);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(component);
        frame.setLocation(location, location);
        frame.pack();
        frame.show();
        location += 15;
    }

    /**
     * Test the {@link ExceptionMonitor}.
     */
    public void testExceptionMonitor() {
        if (false) {
            // Not tested because it block the application (waiting for a user input)
            ExceptionMonitor.show(null, new javax.imageio.IIOException("Can't read the image",
                                        new java.io.FileNotFoundException("File not found")));
        }
    }

    /**
     * Test the {@link CoordinateChooser}.
     */
    public void testCoordinateChooser() {
        CoordinateChooser test = new CoordinateChooser();
        show(test, "CoordinateChooser");
    }

    /**
     * Test the {@link KernelEditor}.
     */
    public void testKernelEditor() {
        KernelEditor test = new KernelEditor();
        test.addDefaultKernels();
        show(test, "KernelEditor");
    }

    /**
     * Test the {@link GradientKernelEditor}.
     */
    public void testGradientKernelEditor() {
        GradientKernelEditor test = new GradientKernelEditor();
        test.addDefaultKernels();
        show(test, "GradientKernelEditor");
    }

    /**
     * Test the {@link ColorBar}.
     */
    public void testColorBar() throws Exception {
        ColorBar test = new ColorBar();
        test.setColors(GridCoverageTest.getExample(0));
        show(test, "ColorBar");
    }
}
