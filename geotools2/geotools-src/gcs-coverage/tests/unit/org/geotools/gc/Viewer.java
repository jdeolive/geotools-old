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
import java.util.*;

// JAI dependencies
import javax.media.jai.PlanarImage;
import javax.media.jai.GraphicsJAI;

// Geotools dependencies
import org.geotools.cv.*;
import org.geotools.gc.*;
import org.geotools.gp.*;
import org.geotools.resources.Arguments;
import org.geotools.resources.Utilities;


/**
 * A very simple viewer for {@link GridCoverage}.   This viewer provides no zoom
 * capability, no user interaction and ignores the coordinate system. It is just
 * for quick test of grid coverage.
 *
 * @version $Id: Viewer.java,v 1.5 2003/04/12 00:04:38 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public class Viewer extends JPanel {
    /**
     * The image to display.
     */
    private final RenderedImage image;

    /**
     * The main sample dimension, or <code>null</code> if none.
     * Used by {@link #printPalette} for printing categories.
     */
    private SampleDimension categories;

    /**
     * The transform from grid to coordinate system.
     * Usually an identity transform for this simple viewer.
     */
    private final AffineTransform gridToCoordinateSystem = new AffineTransform();

    /**
     * The location for the next frame window.
     */
    private static int location;

    /**
     * Construct a viewer for the specified image.
     *
     * @param coverage The image to display.
     */
    public Viewer(RenderedImage image) {
        this.image = PlanarImage.wrapRenderedImage(image);
        gridToCoordinateSystem.translate(-image.getMinX(), -image.getMinY());
        setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
    }

    /**
     * Construct a viewer for the specified grid coverage.
     *
     * @param coverage The coverage to display.
     */
    public Viewer(final GridCoverage coverage) {
        this(coverage.getRenderedImage());
        categories = coverage.getSampleDimensions()[0];
    }

    /**
     * Paint this component.
     */
    public void paintComponent(final Graphics graphics) {
        super.paintComponent(graphics);
        final GraphicsJAI g = GraphicsJAI.createGraphicsJAI((Graphics2D) graphics, this);
        g.drawRenderedImage(image, gridToCoordinateSystem);
    }

    /**
     * A convenience method showing an image. The application
     * will be terminated when the user close the frame.
     *
     * @param  coverage The coverage to display.
     * @return The viewer, for information.
     */
    public static Viewer show(final RenderedImage image) {
        return show(new Viewer(image), null);
    }

    /**
     * A convenience method showing a grid coverage. The application
     * will be terminated when the user close the frame.
     *
     * @param  coverage The coverage to display.
     * @return The viewer, for information.
     */
    public static Viewer show(final GridCoverage coverage) {
        return show(new Viewer(coverage), coverage.getName(JComponent.getDefaultLocale()));
    }

    /**
     * A convenience method showing a grid coverage. The application
     * will be terminated when the user close the frame.
     *
     * @param  viewer The viewer to display.
     * @param  title  The frame title, or <code>null</code> if none.
     * @return The viewer, for convenience.
     */
    private static Viewer show(final Viewer viewer, final String title) {
        final JFrame frame = new JFrame(title);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocation(location, location);
        frame.getContentPane().add(viewer);
        frame.pack();
        frame.show();
        location += 64;
        return viewer;
    }

    /**
     * Print to the color palette to the specified output stream. First, the color model
     * name is displayed. Next, if the color model is an {@link IndexColorModel}, then the
     * RGB codes are written for all samples values. Category names or geophysics values,
     * if any are written after each sample values.
     *
     * @param out The writer where to print the palette.
     */
    public void printPalette(final PrintWriter out) {
        final Locale locale = getLocale();
        final ColorModel model = image.getColorModel();
        out.print(Utilities.getShortClassName(model));
        out.println(':');
        if (model instanceof IndexColorModel) {
            out.println();
            out.println("Sample  Colors              Category or geophysics value");
            out.println("------  ----------------    ----------------------------");
            final IndexColorModel palette = (IndexColorModel) model;
            final int size = palette.getMapSize();
            final byte[] R = new byte[size];
            final byte[] G = new byte[size];
            final byte[] B = new byte[size];
            palette.getReds  (R);
            palette.getGreens(G);
            palette.getBlues (B);
            for (int i=0; i<size; i++) {
                format(out,   i);  out.print(":    RGB[");
                format(out, R[i]); out.print(',');
                format(out, G[i]); out.print(',');
                format(out, R[i]); out.print(']');
                if (categories != null) {
                    final String label = categories.getLabel(i, locale);
                    if (label != null) {
                        out.print("    ");
                        out.print(label);
                    }
                }
                out.println();
            }
        } else {
            out.println(model.getColorSpace());
        }
    }

    /**
     * Format a unsigned byte to the specified output stream.
     * The number will be right-justified in a cell of 3 spaces width.
     *
     * @param The writer where to print the number.
     * @param value The number to format.
     */
    private static void format(final PrintWriter out, final byte value) {
        format(out, ((int)value) & 0xFF);
    }

    /**
     * Format an integer to the specified output stream.
     * The number will be right-justified in a cell of 3 spaces width.
     *
     * @param The writer where to print the number.
     * @param value The number to format.
     */
    private static void format(final PrintWriter out, final int value) {
        final String str = String.valueOf(value);
        out.print(Utilities.spaces(3-str.length()));
        out.print(str);
    }

    /**
     * Load and display one of examples grid coverages.
     */
    public static void main(String[] args) throws IOException {
        final Arguments arguments = new Arguments(args);
        final PrintWriter     out = arguments.out;
        final Locale       locale = arguments.locale;
        final String    operation = arguments.getOptionalString ("-operation");
        final Boolean  geophysics = arguments.getOptionalBoolean("-geophysics");
        final boolean     palette = arguments.getFlag           ("-palette");
        args = arguments.getRemainingArguments(1);
        if (args.length == 0) {
            out.println("Usage: Viewer [options] example");
            out.println();
            out.print("Where \"example\" is the number of the requested example (0 to ");
            out.print(GridCoverageTest.getNumExamples()-1);
            out.println(" inclusive)");
            out.println("and [options] includes:");
            out.println();
            out.println("  -operation=[s]  An operation name to apply (e.g. \"GradientMagniture\").");
            out.println("                  For a list of available operations, run the following:");
            out.println("                  java org.geotools.gp.GridCoverageProcessor");
            out.println("  -geophysics=[b] Set to 'true' or 'false' for requesting a \"geophysics\"");
            out.println("                  version of data or an indexed version, respectively.");
            out.println("  -palette        Dumps RGB codes to standard output.");
            out.flush();
            return;
        }
        GridCoverage coverage = GridCoverageTest.getExample(Integer.parseInt(args[0]));
        if (geophysics != null) {
            coverage = coverage.geophysics(geophysics.booleanValue());
        }
        if (operation != null) {
            final GridCoverageProcessor processor = GridCoverageProcessor.getDefault();
            coverage = processor.doOperation(operation, coverage);
        }
        Viewer viewer = new Viewer(coverage);
        viewer.setLocale(locale);
        viewer = show(viewer, coverage.getName(locale));
        if (palette) {
            viewer.printPalette(out);
        }
        out.flush();
    }
}
