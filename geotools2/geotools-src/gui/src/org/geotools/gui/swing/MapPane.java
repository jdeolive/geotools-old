/*
 * Geotools - OpenSource mapping toolkit
 * (C) 2002, Center for Computational Geography
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

// J2SE dependencies
import java.awt.EventQueue;
import java.awt.geom.Rectangle2D;
import java.awt.geom.AffineTransform;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

// Geotools dependencies
import org.geotools.cs.CoordinateSystem;
import org.geotools.cs.GeographicCoordinateSystem;
import org.geotools.ct.TransformException;
import org.geotools.renderer.j2d.Renderer;
import org.geotools.renderer.j2d.RenderedLayer;
import org.geotools.gui.swing.event.ZoomChangeEvent;
import org.geotools.gui.swing.event.ZoomChangeListener;


/**
 * A <i>Swing</i> component for displaying geographic features.
 * <br><br>
 * Since this class extends {@link ZoomPane},  the user can use mouse and keyboard
 * to zoom, translate and rotate around the map (Remind: <code>MapPanel</code> has
 * no scrollbar. To display scrollbars, use {@link #createScrollPane}).
 *
 * @version $Id: MapPane.java,v 1.8 2003/01/23 12:14:00 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public abstract class MapPane extends ZoomPane {
    /**
     * The renderer targeting Java2D.
     */
    private final Renderer renderer;

    /**
     * Objet "listener" ayant la charge de réagir aux différents
     * événements qui intéressent cet objet <code>MapPane</code>.
     */
    private final Listeners listeners = new Listeners();

    /**
     * Classe ayant la charge de réagir aux différents événements qui intéressent cet
     * objet <code>MapPane</code>.  Cette classe réagira entre autres aux changements
     * du zoom.
     */
    private final class Listeners implements ZoomChangeListener, PropertyChangeListener {
        /** Invoked when the zoom changed. */
        public void zoomChanged(final ZoomChangeEvent event) {
//            renderer.zoomChanged(event);
        }

        /** Invoked when a {@link Renderer} property is changed. */
        public void propertyChange(final PropertyChangeEvent event) {
            // Make sure we are running in the AWT thread.
            if (!EventQueue.isDispatchThread()) {
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        propertyChange(event);
                    }
                });
                return;
            }
            final String propertyName = event.getPropertyName();
            if (propertyName.equalsIgnoreCase("preferredArea")) {
                fireZoomChanged(new AffineTransform()); // Update scrollbars
                log("MapPane", "setArea", (Rectangle2D) event.getNewValue());
            }
        }
    }

    /**
     * Construct a default map panel.
     */
    public MapPane() {
        super(TRANSLATE_X | TRANSLATE_Y | UNIFORM_SCALE | DEFAULT_ZOOM | ROTATE | RESET);
        renderer = new Renderer(this);
        addZoomChangeListener(listeners);
        setResetPolicy       (true);
    }

    /**
     * Returns the view coordinate system. This is the "real world" coordinate system
     * used for displaying all features. Note that underlying data doesn't need to be
     * in this coordinate system: transformations will performed on the fly as needed
     * at rendering time.
     *
     * @return The two dimensional coordinate system used for display.
     *         Default to {@linkplain GeographicCoordinateSystem#WGS84 WGS 1984}.
     */
    public CoordinateSystem getCoordinateSystem() {
        return renderer.getCoordinateSystem();
    }

    /**
     * Set the view coordinate system. This is the "real world" coordinate system to use for
     * displaying all features. Default is {@linkplain GeographicCoordinateSystem#WGS84  WGS
     * 1984}.   Changing this coordinate system has no effect on any underlying data,  since
     * transformation are performed only at rendering time.
     *
     * @param cs The view coordinate system. If this coordinate system has
     *           more than 2 dimensions, then only the 2 first will be retained.
     * @throws TransformException If <code>cs</code> can't be reduced to a two-dimensional
     *         coordinate system., or if data can't be transformed for some other reason.
     */
    public void setCoordinateSystem(CoordinateSystem cs) throws TransformException {
        renderer.setCoordinateSystem(cs);
    }

    /**
     * Returns a bounding box that completely encloses all feature's preferred area.  This
     * bounding box should be representative of the geographic area to drawn. User wanting
     * to set a different default area should invokes {@link #setPreferredArea}. Coordinates
     * are expressed in this {@linkplain #getCoordinateSystem viewer's coordinate system}.
     *
     * @return The enclosing area computed from available data, or <code>null</code>
     *         if this area can't be computed.
     *
     * @see #getPreferredArea
     * @see #setPreferredArea
     * @see Renderer#getPreferredArea
     */
    public Rectangle2D getArea() {
        return renderer.getPreferredArea();
    }

    /**
     * Add a new layer to this viewer. A <code>MapPane</code> do not draw anything as long as
     * at least one layer hasn't be added. A {@link RenderedLayer} can be anything like an
     * isobath, a remote sensing image, city locations, map scale, etc. The drawing order
     * (relative to other layers) is determined by the {@linkplain RenderedLayer#getZOrder
     * z-order} property.
     *
     * @param  layer Layer to add to this <code>MapPane</code>. This method call
     *         will be ignored if <code>layer</code> has already been added to this
     *         <code>MapPane</code>.
     * @throws IllegalArgumentException If <code>layer</code> has already been added
     *         to an other <code>MapPane</code>.
     *
     * @see #removeLayer
     * @see #removeAllLayers
     * @see #getLayers
     * @see #getLayerCount
     * @see Renderer#addLayer
     */
    public void addLayer(final RenderedLayer layer) throws IllegalArgumentException {
        renderer.addLayer(layer);
        if (renderer.getLayerCount() == 1) {
            reset();
        }
        repaint(); // Must be invoked last
    }

    /**
     * Remove a layer from this viewer. Note that if the layer is going to
     * be added back to the same viewer later, then it is more efficient to invoke
     * <code>{@link RenderedLayer#setVisible RenderedLayer.setVisible}(false)</code>.
     *
     * @param  layer The layer to remove. This method call will be ignored
     *         if <code>layer</code> has already been removed from this
     *         <code>MapPane</code>.
     * @throws IllegalArgumentException If <code>layer</code> is owned by
     *         an other <code>Renderer</code> than <code>this</code>.
     *
     * @see #addLayer
     * @see #removeAllLayers
     * @see #getLayers
     * @see #getLayerCount
     * @see Renderer#removeLayer
     */
    public void removeLayer(final RenderedLayer layer) throws IllegalArgumentException {
        repaint(); // Must be invoked first
        renderer.removeLayer(layer);
    }

    /**
     * Remove all layers from this viewer.
     *
     * @see #addLayer
     * @see #removeLayer
     * @see #getLayers
     * @see #getLayerCount
     * @see Renderer#removeAllLayers
     */
    public void removeAllLayers() {
        repaint(); // Must be invoked first
        renderer.removeAllLayers();
    }

    /**
     * Returns all registered layers. The returned array is sorted in increasing
     * {@linkplain RenderedLayer#getZOrder z-order}: element at index 0 contains
     * the first layer to be drawn.
     *
     * @return The sorted array of layers. May have a 0 length, but will never
     *         be <code>null</code>. Change to this array, will not affect this
     *         <code>MapPane</code>.
     *
     * @see #addLayer
     * @see #removeLayer
     * @see #removeAllLayers
     * @see #getLayerCount
     * @see Renderer#getLayers
     */
    public RenderedLayer[] getLayers() {
        return renderer.getLayers();
    }

    /**
     * Returns the number of layers in this viewer.
     *
     * @see #getLayers
     * @see #addLayer
     * @see #removeLayer
     * @see #removeAllLayers
     * @see Renderer#getLayerCount
     */
    public int getLayerCount() {
        return renderer.getLayerCount();
    }
}
