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
package org.geotools.gc;

// J2SE dependencies
import java.io.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import javax.swing.*;

// JAI dependencies
import javax.media.jai.PlanarImage;
import javax.media.jai.GraphicsJAI;

// Geotools dependencies
import org.geotools.gc.*;
import org.geotools.gp.*;
import org.geotools.resources.Arguments;


/**
 * A very simple viewer for {@link GridCoverage}.   This viewer provides no zoom
 * capability, no user interaction and ignores the coordinate system. It is just
 * for quick test of grid coverage.
 *
 * @version $Id: Viewer.java,v 1.1 2002/08/09 18:43:35 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public class Viewer extends JPanel {
    /**
     * The image to display.
     */
    private final RenderedImage image;

    /**
     * The transform from grid to coordinate system.
     * Always maps to an identity transform for this
     * simple viewer.
     */
    private final AffineTransform gridToCoordinateSystem = new AffineTransform();

    /**
     * Construct a viewer for the specified image.
     *
     * @param coverage The image to display.
     */
    public Viewer(final RenderedImage image) {
        this.image = PlanarImage.wrapRenderedImage(image);
        setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
        final Raster data = image.getTile(0,0);
        for (int x=0; x<300; x++) {
            for (int y=0; y<300; y++) {
                System.out.println(data.getSampleFloat(x,y,0));
            }
        }
    }

    /**
     * Construct a viewer for the specified grid coverage.
     *
     * @param coverage The coverage to display.
     */
    public Viewer(final GridCoverage coverage) {
        this(coverage.geophysics(true).getRenderedImage());
    }

    /**
     * Paint this component.
     */
    public void paintComponent(final Graphics graphics) {
//        final GraphicsJAI g = GraphicsJAI.createGraphicsJAI((Graphics2D) graphics, this);
        final Graphics2D g = (Graphics2D) graphics;
System.out.println(image);
        g.drawRenderedImage(image, gridToCoordinateSystem);
    }

    /**
     * A convenience method showing an image. The application
     * will be terminated when the user close the frame.
     *
     * @param  coverage The coverage to display.
     * @return The displayed frame, for information.
     */
    public static JFrame show(final RenderedImage image) {
        final JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(new Viewer(image));
        frame.pack();
        frame.show();
        return frame;
    }

    /**
     * A convenience method showing a grid coverage. The application
     * will be terminated when the user close the frame.
     *
     * @param  coverage The coverage to display.
     * @return The displayed frame, for information.
     */
    public static JFrame show(final GridCoverage coverage) {
        final JFrame frame = new JFrame(coverage.getName(JComponent.getDefaultLocale()));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(new Viewer(coverage));
        frame.pack();
        frame.show();
        return frame;
    }

    /**
     * Load and display one of examples grid coverages.
     */
    public static void main(String[] args) throws IOException {
        args = new String[]{"0", "-operation=GradientMagnitude"};
        args = new String[]{"0"};
        final Arguments arguments = new Arguments(args);
        final PrintWriter     out = arguments.out;
        final Integer     example = arguments.getOptionalInteger("-example");
        final String    operation = arguments.getOptionalString("-operation");
        args = arguments.getRemainingArguments(1);
        if (example==null && args.length==0) {
            out.println("Usage: Viewer [options]");
            out.println();
            out.println("Where options includes:");
            out.print  ("  -example=[n]    The number of the requested example (0 to ");
            out.print(GridCoverageTest.getNumExamples()-1);
            out.println(" inclusive)");
            out.println("  -operation=[s]  An operation name to apply (e.g. \"GradientMagniture\")");
            out.println("                  For a list of available operations, run the following:");
            out.println("                  java org.geotools.gp.GridCoverageProcessor");
            out.flush();
            return;
        }

        javax.imageio.ImageReader reader = new fr.ird.io.image.USRelaxed_ASC().createReaderInstance();
        reader.setInput(new File("D:/Données/Réunion/Bathymétrie/Baudry.asc"));
        RenderedImage test = reader.read(0);
        System.out.println(test);
        show(test);

        final int number = (example!=null) ? example.intValue() : Integer.parseInt(args[0]);
        GridCoverage coverage = GridCoverageTest.getExample(number);
        if (operation != null) {
            final GridCoverageProcessor processor = GridCoverageProcessor.getDefault();
            coverage = processor.doOperation(operation, coverage);
        }
        show(coverage);
        out.flush();
    }
}
