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
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Component;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JComponent;

// JTS dependencies
import com.vividsolutions.jts.geom.Geometry;

// Geotools dependencies
import org.geotools.gc.GridCoverage;
import org.geotools.renderer.geom.JTSGeometries;
import org.geotools.renderer.j2d.RenderedLayer;
import org.geotools.renderer.j2d.RenderedMapScale;
import org.geotools.renderer.j2d.RenderedGridMarks;
import org.geotools.renderer.j2d.RenderedGeometries;
import org.geotools.renderer.j2d.RenderedGridCoverage;
import org.geotools.resources.Utilities;


/**
 * A set of convenience methods for displaying geographic components in an Swing widget.
 * This factory may be used for an easy visualisation of {@linkplain GridCoverage grid
 * coverages} or {@linkplain Geometry geometry} objects in a Swing widget.
 *
 * @author Martin Desruisseaux
 * @version $Id: FrameFactory.java,v 1.6 2003/11/18 14:26:49 desruisseaux Exp $
 */
public final class FrameFactory {
    /**
     * Do not allow instantiation of this class.
     */
    private FrameFactory() {
    }

    /**
     * Show the given grid coverage as an image.
     *
     * @param coverage The grid coverage to show.
     */
    public static void show(final GridCoverage coverage) {
        // Create the frame
        final JFrame frame = new JFrame(coverage.getName(JComponent.getDefaultLocale()));
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        final Container c = frame.getContentPane();
        c.setLayout(new BorderLayout());

        // Add the map pane and its color bar
        final MapPane mapPane = new MapPane(coverage.getCoordinateSystem());
        mapPane.getRenderer().addLayer(new RenderedGridCoverage(coverage));
        mapPane.getRenderer().addLayer(new RenderedMapScale());
        mapPane.setPaintingWhileAdjusting(false);
        Component mapView = mapPane.createScrollPane();
        try {
            final ColorBar colors = new ColorBar(coverage);
            if (colors.getGraduation() != null) {
                final JPanel panel = new JPanel(new BorderLayout());
                panel.add(mapView, BorderLayout.CENTER);
                panel.add(colors,  BorderLayout.SOUTH);
                mapView = panel;
            }
        } catch (UnsupportedOperationException exception) {
            /*
             * The coverage use an unsupported color model. We can't add the color bar to
             * the widget, but everything else should work fine. Ignore the exception and
             * continue the creation of a widget without color bar.
             */
        }
        c.add(mapView, BorderLayout.CENTER);

        // Add the status bar
        c.add(new StatusBar(mapPane), BorderLayout.SOUTH);

        // Show the frame
        frame.setSize(500, 500);
        frame.show();
    }

    /**
     * Show the given grid coverage as a field of vectors. The <var>x</var> component
     * is taken from the first band and the <var>y</var> component from the second band.
     *
     * @param coverage The grid coverage to show.
     */
    public static void showAsVectors(final GridCoverage coverage) {
        show(coverage.getName(JComponent.getDefaultLocale()), new RenderedGridMarks(coverage));
    }

    /**
     * Show the given geometry.
     *
     * @param geometry The geometry to show.
     */
    public static void show(final Geometry geometry) {
        show(Utilities.getShortClassName(geometry),
             new RenderedGeometries(new JTSGeometries(geometry)));
    }

    /**
     * Show the given layer.
     *
     * @param layer The layer to show.
     */
    private static void show(final String title, final RenderedLayer layer) {
        // Create the frame
        final JFrame frame = new JFrame(title);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        final Container c = frame.getContentPane();
        c.setLayout(new BorderLayout());

        // Add the map pane
        final MapPane mapPane = new MapPane(layer.getCoordinateSystem());
        mapPane.getRenderer().addLayer(layer);
        mapPane.getRenderer().addLayer(new RenderedMapScale());
        mapPane.setPaintingWhileAdjusting(false);
        c.add(mapPane.createScrollPane(), BorderLayout.CENTER);

        // Add the status bar
        c.add(new StatusBar(mapPane), BorderLayout.SOUTH);

        // Show the frame
        frame.setSize(500, 500);
        frame.show();
    }
}
