/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Management Committee (PMC)
 * (C) 2001, Institut de Recherche pour le Développement
 * (C) 1998, Pêches et Océans Canada
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
package org.geotools.gui.swing.event;

//import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.event.MouseEvent;
import org.geotools.cs.CoordinateSystem;
import org.geotools.ct.MathTransform;
import org.geotools.ct.TransformException;
import org.geotools.pt.CoordinatePoint;


/**
 * A MouseEvent which contains methods to obtain coordinates in real world
 * CoordinateSystem as well as Screen Coordinates.
 * All {@link MouseListener}s that have registered for
 * {@link org.geotools.gui.swing.MapPaneImpl} mouseEvents will receive
 * events of this class.
 * Listeners implementations can implements their code as below:
 *
 * <blockquote><pre>
 * &nbsp;public void mouseClicked(MouseEvent e) {
 * &nbsp;    GeoMouseEvent event = (GeoMouseEvent) e;
 * &nbsp;    // Process event here...
 * &nbsp;}
 * </pre></blockquote>
 *
 * @version $Id: GeoMouseEvent.java,v 1.5 2003/05/13 11:01:39 desruisseaux Exp $
 * @author Martin Desruisseaux
 * @author Cameron Shorter
 */
public final class GeoMouseEvent extends MouseEvent {
    /**
     * The transform which will convert screenCoordinates
     * to CoordinateSystem coordinates.
     */
    final MathTransform transform;

    /**
     * The coordinate system for ({@link #x},{@link #y}) or <code>null</code>
     * if the coordinate has not yet been computed. This coordinate system
     * must be two-dimensional.
     */
    private transient CoordinateSystem coordinateSystem;

    /**
     * A mouseClick event which also contains methods to transform from 
     * pixels to the Coordinate System of the Renderer.
     * @param event    The original mouse event.
     * @param transform The transform which will convert screenCoordinates
     * to CoordinateSystem coordinates.
     */
    public GeoMouseEvent(
            final MouseEvent event,
            final MathTransform transform)
    {
        super(
                event.getComponent(),  // the Component that originated the event
              event.getID(),         // the integer that identifies the event
              event.getWhen(),       // a long int that gives the time the
                                     // event occurred
              event.getModifiers(),  // the modifier keys down during event
                                     // (shift, ctrl, alt, meta)
              event.getX(),          // the horizontal x coordinate for the
                                     // mouse location
              event.getY(),          // the vertical y coordinate for the mouse
                                     // location
              event.getClickCount(), // the number of mouse clicks associated
                                     // with event
              event.isPopupTrigger(),// a boolean, true if this event is a
                                     // trigger for a popup-menu
              event.getButton());    // which of the mouse buttons has changed
                                     // state (JDK 1.4 only).
        this.transform = transform;
    }
    
    /**
     * Returns the "real world" mouse's position. The coordinates are expressed
     * in Context's CoordinateSystem.
     *
     * @param  dest A pre-allocated variable to store the mouse's location
     * in CoordinateSystems, can be set to <code>null</code>.
     * @return The mouse's location in CoordinateSystem coordinates. 
     * @throws TransformException when transform is invalid.
     */
    public CoordinatePoint getMapCoordinate(CoordinatePoint dest)
        throws TransformException
    {
         if (dest == null) {
            dest=new CoordinatePoint(getX(), getY());
        } else {
            dest.setLocation(new Point2D.Double(getX(), getY()));
        }
        transform.transform(dest,dest);
        return dest;
    }
}
