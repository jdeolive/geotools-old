package org.geotools.gui.swing;

/*
 * Geotools - OpenSource mapping toolkit
 * (C) 2002, Center for Computational Geography
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
 *     UNITED KINDOM: James Macgill
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

// Standards Java classes
import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;

// Geotools classes
import org.geotools.gui.swing.ZoomPane;
import org.geotools.map.*;
import org.geotools.gui.swing.event.ZoomChangeEvent;
import org.geotools.gui.swing.event.ZoomChangeListener;
import org.geotools.renderer.*;
import org.geotools.datasource.extents.EnvelopeExtent;
import org.geotools.datasource.*;
import org.geotools.featuretable.*;
import org.geotools.styling.*;

//JTS classes
import com.vividsolutions.jts.geom.*;

/**
 * Demonstration application displaying a triangle and a rectangle in a zoomable
 * pane. The class <CODE>org.geotools.swing.ZoomPane</CODE> take care of most of
 * the internal mechanics. Developers must define only two abstract methods:
 * <CODE>getArea()</CODE>, which returns the logical bounds of the painting area
 * (it doesn't have to be in pixel unit) and <CODE>paintComponent(Graphics2D)</CODE>
 * which performs the actual painting.
 *
 * @version 1.0
 * @author Martin Desruisseaux
 */
public class MapPane extends ZoomPane implements ZoomChangeListener {
    
    org.geotools.renderer.Java2DRenderer renderer = new Java2DRenderer();
    org.geotools.map.Map map;
    org.geotools.datasource.extents.EnvelopeExtent ext;
    /**
     * Construct a demonstration zoom pane. This demo allow scale,
     * translation and rotation. Scrolling the pane will repaint
     * immediately (as opposite to wait for the user to finish adjusting).
     */
    public MapPane(Map map,EnvelopeExtent ext) {        
        super(UNIFORM_SCALE | TRANSLATE_X | TRANSLATE_Y | ROTATE | RESET | DEFAULT_ZOOM);
        setPaintingWhileAdjusting(true);
        this.map = map;
        this.ext = ext;
        
        //transformation performed by renderer should be combined with
        //transformation calculated by this component.
        renderer.setConcatTransforms(true);
    }
    
    /**
     * Returns a bounding box containing the painted area in logical coordinates.
     * Coordinates don't have to be in pixels. An affine transform will be
     * automatically computed in order to fit logical coordinate into the frame.
     */
    public Rectangle2D getArea() {
        //return this.getBounds();
        Envelope e = ext.getBounds();
        return new Rectangle2D.Double(e.getMinX(),e.getMinY(),e.getWidth(),e.getHeight());
    }
    
    /**
     * Paint the area. For this demo, we paint the triangle and its bounding
     * rectangle.  Instruction <CODE>graphics.transform(zoom)</CODE> MUST be
     * executed before painting any zoomable components. Painting is done
     * in the same logical coordinates than the coordinates system used by
     * <CODE>getArea()</CODE>. Developers don't have to care about the window's
     * size.
     *
     * If you want to paint a non-zoomable component (e.g. a text of
     * fixed size and position), you can paint it before to execute
     * <CODE>graphics.transform(zoom)</CODE>.
     */
    protected void paintComponent(final Graphics2D graphics) {
        // Apply the zoom, which is automatically computed by ZoomPane.
        graphics.transform(zoom);

        renderer.setOutput(graphics,this.getBounds());
        map.render(renderer,ext.getBounds());//and finaly try and draw it!
        
    }
    
    /**
     * Invoked when a zoom changed. Our implementation just apply the same
     * transform on this <code>ZoomPane</code>. This simple implementation
     * is enough when there is only one "master" and one "slave" zoom panes.
     * If we have two "master" (or two "slaves") zoom panes, then a more
     * sophesticated implementation is needed to avoid never-ending loop.
     */
    public void zoomChanged(final ZoomChangeEvent event) {
        transform(event.getChange());
    }
    
}
