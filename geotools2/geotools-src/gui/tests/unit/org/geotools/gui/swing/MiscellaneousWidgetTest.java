/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Management Committee (PMC)
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

// J2SE dependencies
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import javax.swing.*;
import java.util.Random;
import java.util.Locale;
import java.util.List;
import java.util.ArrayList;

// JAI dependencies
import javax.media.jai.operator.*;

// JUnit dependencies
import junit.framework.*;

// Geotools dependencies
import org.geotools.resources.Arguments;
import org.geotools.resources.ColorUtilities;


/**
 * Tests a set of widgets.
 *
 * @version $Id: MiscellaneousWidgetTest.java,v 1.8 2003/08/07 16:57:46 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public class MiscellaneousWidgetTest extends TestCase {
    /**
     * Set to <code>true</code> if window should be keep once the test is completed.
     */
    private static boolean keep;

    /**
     * The location of the next frame to show.
     */
    private static volatile int location = 60;

    /**
     * The list of widget created up to date.
     */
    private final List widgets = new ArrayList();

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
        keep = arguments.getFlag("-keep");
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
     * Dispose all widgets. This method is called after tests are executed.
     */
    protected void tearDown() throws Exception {
        for (int i=widgets.size(); --i>=0;) {
            final Window window = (Window)widgets.get(i);
            window.dispose();
        }
        super.tearDown();
    }

    /**
     * Show a component.
     */
    private void show(final Component component, final String title) {
        try {
            final JFrame frame = new JFrame(title);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.getContentPane().add(component);
            frame.setLocation(location, location);
            frame.pack();
            frame.show();
            if (!keep) {
                widgets.add(frame);
            }
            location += 30;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException exception) {
                // Ignore
            }
        } catch (HeadlessException exception) {
            // The test is running on a machine without display. Ignore.
        }
    }

    /**
     * Test the {@link About} dialog.
     */
    public void testAbout() {
        show(new About(), "About");
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
    public void testColorBar() {
        ColorBar test = new ColorBar();
        final int[] ARGB = new int[256];
        ColorUtilities.expand(new Color[] {Color.RED, Color.ORANGE, Color.YELLOW, Color.CYAN},
                              ARGB, 0, ARGB.length);
        test.setColors(ColorUtilities.getIndexColorModel(ARGB));
        show(test, "ColorBar");
    }

    /**
     * Test the {@link ProgressWindow}.
     */
    public void testProgress() throws InterruptedException {
        if (!keep) {
            return;
        }
        final ProgressWindow progress = new ProgressWindow(null);
        progress.setDescription("Some progress");
        progress.started();
        progress.progress(75);
        progress.warningOccurred("File foo.txt", "(47)", "Some warning");
        progress.complete();
    }

    /**
     * Test the {@link Plot2D}.
     */
    public void testPlot2D() {
        final Random random = new Random();
        Plot2D test = new Plot2D(true, false);
        test.newAxis(0, "Some x values");
        test.newAxis(1, "Some y values");
        for (int j=0; j<2; j++) {
            final float[] x = new float[800];
            final float[] y = new float[800];
            for (int i=0; i<x.length; i++) {
                x[i] = i/10f;
                y[i] = (float)random.nextGaussian();
                if (i!=0) {
                    y[i] += y[i-1];
                }
            }
            test.addSeries("Random values", x, y);
        }
        test.setPaintingWhileAdjusting(true);
        show(test.createScrollPane(), "Plot2D");
    }

    /**
     * Test the {@link ZoomPane}.
     */
    public void testZoomPane() {
        final Rectangle rect = new Rectangle(100,200,100,93);
        final Polygon   poly = new Polygon(new int[] {125,175,150}, new int[] {225,225,268}, 3);
        final ZoomPane  pane = new ZoomPane(ZoomPane.UNIFORM_SCALE |
                                            ZoomPane.TRANSLATE_X   |
                                            ZoomPane.TRANSLATE_Y   |
                                            ZoomPane.ROTATE        |
                                            ZoomPane.RESET         |
                                            ZoomPane.DEFAULT_ZOOM)
        {
            public Rectangle2D getArea() {
                return rect;
            }

            protected void paintComponent(final Graphics2D graphics) {
                graphics.transform(zoom);
                graphics.setColor(Color.RED);
                graphics.fill(poly);
                graphics.setColor(Color.BLUE);
                graphics.draw(poly);
                graphics.draw(rect);
            }
        };
        pane.setPaintingWhileAdjusting(true);
        show(pane, "ZoomPane");
    }

    /**
     * Test the {@link OperationTreeBrowser}.
     */
    public void testOperationTree() {
        RenderedImage image;
        final Float size = new Float(200);
        final Byte value = new Byte((byte)10);
        image = ConstantDescriptor.create(size,size, new Byte[]{value}, null);
        image = MultiplyConstDescriptor.create(image, new double[] {2}, null);
        image = GradientMagnitudeDescriptor.create(image, null, null, null);
        image = AddConstDescriptor.create(image, new double[] {35}, null);
        show(new OperationTreeBrowser(image), "OperationTreeBrowser");
    }
}
