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
package org.geotools.renderer.j2d;

// J2SE dependencies
import java.awt.*;
import java.awt.geom.*;

// JUnit dependencies
import junit.framework.*;

// Geotools dependencies
import org.geotools.cs.*;
import org.geotools.ct.*;
import org.geotools.units.Unit;
import org.geotools.renderer.geom.GeometryCollection;


/**
 * Test the {@link Renderer} class.
 *
 * @version $Id: RendererTest.java,v 1.1 2003/08/11 20:04:16 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public class RendererTest extends TestCase {
    /**
     * A cartesian coordinate system.
     */
    private static final LocalCoordinateSystem CARTESIAN = new LocalCoordinateSystem("Cartesian",
                         new LocalDatum("Cartesian",
                         (DatumType.Local)DatumType.getEnum(DatumType.Local.MINIMUM)),
                         Unit.METRE, new AxisInfo[] {AxisInfo.X, AxisInfo.Y});

    /**
     * Run the suit from the command line.
     */
    public static void main(final String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    /**
     * Returns the test suite.
     */
    public static Test suite() {
        return new TestSuite(RendererTest.class);
    }

    /**
     * Default constructor.
     */
    public RendererTest(final String name) {
        super(name); 
    }

    /**
     * Test the rendering using offscreen buffer.
     */
    public void testOffscreenBuffer() throws TransformException {
        final Pane pane = new Pane();
        pane.renderer.setCoordinateSystem(CARTESIAN);
        pane.renderer.setOffscreenBuffered(50, 150, ImageType.VOLATILE);
        for (int i=0; i<300; i+=100) {
            final GeometryCollection geom = new GeometryCollection(CARTESIAN);
            geom.add(new RoundRectangle2D.Float(100+i, 100+i/2, 200, 200, 20, 20));
            final RenderedLayer layer = new RenderedGeometries(geom);
            pane.renderer.addLayer(layer);
            layer.setZOrder(i); // Try changing z-order after addition.
        }
        pane.display();
    }

    /**
     * A canvas for displaying {@link Renderer} content.
     */
    private static final class Pane extends Canvas {
        /**
         * The renderer.
         */
        protected final Renderer renderer;

        /**
         * Default constructor.
         */
        public Pane() {
            renderer = new Renderer(this);
        }

        /**
         * Paint the canvas.
         */
        public void paint(final Graphics graphics) {
            super.paint(graphics);
            renderer.paint((Graphics2D) graphics, getBounds(), new AffineTransform(), false);
        }

        /**
         * Show this canvas in a frame.
         */
        public void display() {
            final Frame frame = new Frame("Renderer test");
            frame.add(this);
            frame.setSize(600, 600);
            frame.show();
            try {
                Thread.sleep(2000);
            } catch (InterruptedException exception) {
                // Go back to work.
            }
            frame.dispose();
        }
    }
}
