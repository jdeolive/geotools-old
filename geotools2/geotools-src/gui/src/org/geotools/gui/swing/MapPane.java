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
package org.geotools.gui.swing;

// Standards Java classes
import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;
import java.util.EventObject;

// Geotools classes
import org.geotools.gui.swing.ZoomPane;
import org.geotools.map.*;
import org.geotools.map.events.AreaOfInterestChangedListener;
import org.geotools.map.BoundingBoxImpl;

import org.geotools.gui.swing.event.ZoomChangeEvent;
import org.geotools.gui.swing.event.ZoomChangeListener;
import org.geotools.renderer.*;
import org.geotools.datasource.extents.EnvelopeExtent;
import org.geotools.data.*;
import org.geotools.feature.*;
import org.geotools.styling.*;

//JTS classes
import com.vividsolutions.jts.geom.*;

/**
 * Demonstration application displaying a triangle and a rectangle in a
 * zoomable pane. The class <CODE>org.geotools.swing.ZoomPane</CODE> takes
 * care of most of the internal mechanics. Developers must define only two
 * abstract methods: <CODE>getArea()</CODE>, which returns the logical bounds
 * of the painting area (it doesn't have to be in pixel units) and
 * <CODE>paintComponent(Graphics2D)</CODE> which performs the actual painting.
 *
 * $Id: MapPane.java,v 1.6 2002/12/19 11:33:50 camerons Exp $
 * @version 1.0
 * @author Martin Desruisseaux
 */
public class MapPane extends ZoomPane implements ZoomChangeListener, AreaOfInterestChangedListener{
    
    org.geotools.renderer.Java2DRenderer renderer = new Java2DRenderer();
    org.geotools.map.Map map;
    BoundingBox aoi;
    /**
     * Constructs a demonstration zoom pane. This demo allows scale,
     * translation and rotation. Scrolling the pane will repaint
     * immediately (as opposed to waiting for the user to finish adjusting).
     */
    public MapPane(Map map, BoundingBox aoi) {
        super(UNIFORM_SCALE | TRANSLATE_X | TRANSLATE_Y | ROTATE | RESET | DEFAULT_ZOOM);
        setPaintingWhileAdjusting(true);
        this.map = map;
        this.aoi = aoi;
        //aoi = new DefaultAreaOfInterestModel(ext.getBounds(),null);
        aoi.addAreaOfInterestChangedListener(this);
        //transformation performed by renderer should be combined with
        //transformation calculated by this component.
        renderer.setConcatTransforms(true);
    }
    
    private Envelope fullArea;
    
    public void setFullArea(Envelope e){
        fullArea = e;
    }
    
    /**
     * Returns a bounding box containing the painted area in logical
     * coordinates. Coordinates don't have to be in pixels. An
     * affine transform will be automatically computed in order to
     * fit logical coordinates into the frame.
     */
    public Rectangle2D getArea() {
        if(fullArea == null) {
            return this.getBounds();
        }
        //return this.getBounds();
        
        return new Rectangle2D.Double(fullArea.getMinX(), fullArea.getMinY(), fullArea.getWidth(), fullArea.getHeight());
    }
    
    /**
     * Paints the area. For this demo, we paint the triangle and its bounding
     * rectangle.  Instruction <CODE>graphics.transform(zoom)</CODE> MUST be
     * executed before painting any zoomable components. Painting is done
     * in the same logical coordinates as the coordinates system used by
     * <CODE>getArea()</CODE>. Developers don't have to care about the window's
     * size.
     *
     * If you want to paint a non-zoomable component (e.g. a text of
     * fixed size and position), you can paint it before executing
     * <CODE>graphics.transform(zoom)</CODE>.
     */
    protected void paintComponent(final Graphics2D graphics) {
        // Apply the zoom, which is automatically computed by ZoomPane.
        graphics.transform(zoom);
        
        renderer.setOutput(graphics, this.getBounds());
        map.render(renderer, aoi.getAreaOfInterest());//and finally try and draw it!
        
    }
    
    /**
     * Invoked when a zoom changes. Our implementation just applies the same
     * transform on this <code>ZoomPane</code>. This simple implementation
     * is enough when there is only one "master" zoom pane and one "slave".
     * If we have two "master" (or two "slave") zoom panes, then a more
     * sophisticated implementation is needed to avoid an infinite loop.
     */
    public void zoomChanged(final ZoomChangeEvent event) {
        transform(event.getChange());
    }
    
    /** Process an AreaOfInterestChangedEvent, probably involves a redraw.
     * @param areaOfInterestChangedEvent The new extent.
     */
    public void areaOfInterestChanged(EventObject aoiEvent) {
        Envelope e = aoi.getAreaOfInterest();
        if(fullArea == null){
            fullArea = e;
        }
        System.out.println("New AOI "+e);
        Rectangle2D rect = new Rectangle2D.Double(e.getMinX(),e.getMinX(),e.getWidth(),e.getHeight());
        System.out.println("rect is "+rect+" va is "+getVisibleArea());
        if(!rect.equals(getVisibleArea())){
            super.setVisibleArea(rect);
        }
    }
    
    
    public void setVisibleArea(Rectangle2D rect) throws IllegalArgumentException{
        System.out.println("set va called");
        super.setVisibleArea(rect);
        aoi.setAreaOfInterest(new Envelope(rect.getMinX(),rect.getMaxX(),rect.getMinY(),rect.getMaxY()),null);
    }
    
}
