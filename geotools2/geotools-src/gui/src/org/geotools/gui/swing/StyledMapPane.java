/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Management Committee (PMC)
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
 */
package org.geotools.gui.swing;

// J2SE dependencies
import java.awt.geom.Rectangle2D;

// JTS dependencies
import com.vividsolutions.jts.geom.Envelope;

// Geotools dependencies
import org.geotools.map.Layer;
import org.geotools.map.Context;
import org.geotools.map.BoundingBox;
import org.geotools.cs.CoordinateSystem;
import org.geotools.renderer.j2d.Renderer;
import org.geotools.renderer.j2d.StyledRenderer;


/**
 * A map pane which support styling.
 *
 * @version $Id: StyledMapPane.java,v 1.1 2003/08/18 17:04:51 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public class StyledMapPane extends MapPane {
    /**
     * The model which stores a list of layers and bounding box.
     */
    private Context context;
    
    /**
     * Construct a default map panel.
     */
    public StyledMapPane() {
        super();
    }

    /**
     * Construct a map panel using the specified coordinate system.
     *
     * @param cs The rendering coordinate system.
     */
    public StyledMapPane(final CoordinateSystem cs) {
        super(cs);
    }

    /**
     * Create the renderer for this map pane. This method
     * is invoked by the constructor at creation time only.
     */
    Renderer createRenderer() {
        return new StyledRenderer(this);
    }

    /**
     * Returns the last context set with {@link #setContext}, or <code>null</code> if none.
     *
     * @return The map context.
     */
    public Context getContext() {
        return context;
    }

    /**
     * Set a new context as the current one. Invoking this method will remove all layers
     * in the {@linkplain #getRenderer renderer} and replace them with new layers from
     * the given context.
     *
     * @param context The new context, or <code>null</code> for removing any previous context.
     */
    public void setContext(final Context context) {
        this.context = context;
        ((StyledRenderer) getRenderer()).setContext(context);
        reset();
    }

    /**
     * Returns a bounding box representative of the geographic area to drawn.
     * This method returns the first of the following area available:
     *
     * <ul>
     *   <li>If a {@linkplain Context context} is set, then the context's
     *       {@linkplain Context#getBounds bounding box} is returned.</li>
     *   <li>Otherwise, the area of interest is computed from the layers currently
     *       registered in the {@linkplain #getRenderer renderer}.</li>
     * </ul>
     *
     * @return The enclosing area to be drawn with the default zoom,
     *         or <code>null</code> if this area can't be computed.
     *
     * @see #setPreferredArea
     * @see Context#getBounds
     * @see Renderer#getPreferredArea
     */
    public Rectangle2D getArea() {
        if (context != null) {
            final BoundingBox box = context.getBbox();
            if (box != null) {
                Envelope envelope = box.getAreaOfInterest();
                if (envelope != null) {
                    return new Rectangle2D.Double(envelope.getMinX(),  envelope.getMinY(),
                                                  envelope.getWidth(), envelope.getHeight());
                }
            }
        }
        return super.getArea();
    }

    /**
     * Returns the {@linkplain StyledRenderer styled renderer} for this map pane.
     *
     * @task TODO: Change the returns type to StyledRenderer when we will be allowed
     *             to compile with J2SE 1.5.
     */
    public Renderer getRenderer() {
        return super.getRenderer();
    }
}
