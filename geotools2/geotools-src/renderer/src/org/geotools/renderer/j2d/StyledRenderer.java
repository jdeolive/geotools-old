/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
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
 *
 *     CANADA: Observatoire du Saint-Laurent
 *             Institut Maurice-Lamontagne
 *             mailto:osl@osl.gc.ca
 */
package org.geotools.renderer.j2d;

// J2SE dependencies
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.awt.Component;
import java.rmi.RemoteException;

// JTS dependencies
import com.vividsolutions.jts.geom.Envelope;

// Geotools dependencies
import org.geotools.map.Layer;
import org.geotools.map.Context;
import org.geotools.map.LayerList;
import org.geotools.map.BoundingBox;
import org.geotools.styling.Style;
import org.geotools.feature.Feature;
import org.geotools.feature.CollectionEvent;
import org.geotools.feature.CollectionListener;
import org.geotools.feature.FeatureCollection;
import org.geotools.resources.renderer.Resources;
import org.geotools.resources.renderer.ResourceKeys;
import org.geotools.resources.XMath;
import org.geotools.cs.CoordinateSystem;
import org.geotools.ct.TransformException;
import org.geotools.ct.Adapters;


/**
 * A renderer for rendering {@linkplain Style styled} {@linkplain Feature features}.
 * This renderer listen for {@linkplain CollectionEvent feature collection changes}
 * and invokes {@link RenderedLayer#repaint} automatically on changes.
 *
 * @version $Id: StyledRenderer.java,v 1.5 2003/09/01 13:54:02 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public class StyledRenderer extends Renderer {
    /**
     * The factory for rendered layers.
     */
    private final RenderedLayerFactory factory;

    /**
     * The layer to be rendered. Keys are {@link Layer} objects and values are
     * {@link StyledRenderer.LayerEntry}.
     */
    private final Map renderedLayers = new HashMap();

    /**
     * Construct a new renderer for the specified component.
     *
     * @param owner The widget that own this renderer, or <code>null</code> if none.
     */
    public StyledRenderer(final Component owner) {
        this(owner, new RenderedLayerFactory());
    }

    /**
     * Construct a new renderer using the specified factory.
     *
     * @param owner The widget that own this renderer, or <code>null</code> if none.
     */
    public StyledRenderer(final Component owner, final RenderedLayerFactory factory) {
        super(owner);
        this.factory = factory;
    }

    /**
     * Set a new context as the current one. This method performs the following steps:
     * <ul>
     *   <li>Remove all previous layers.</li>
     *   <li>Set the coordinate system to the context CS.</li>
     *   <li>Add all layers found in the context.</li>
     *   <li>Register listeners for feature changes.</li>
     * </ul>
     *
     * @param context The new context, or <code>null</code> for removing any previous context.
     */
    public synchronized void setContext(final Context context) {
        removeAllLayers();
        if (context != null) try {
            final BoundingBox box = context.getBoundingBox();
            if (box != null) {
                final CoordinateSystem cs = Adapters.getDefault().wrap(box.getCoordinateSystem());
                if (cs != null) {
                    setCoordinateSystem(cs);
                    factory.setCoordinateSystem(cs);
                }
            }
            final LayerList list = context.getLayerList();
            if (list != null) {
                final Layer[] layers = list.getLayers();
                if (layers != null) {
                    for (int i=0; i<layers.length; i++) {
                        addLayer(layers[i]);
                    }
                }
            }
        } catch (RemoteException cause) {
            IllegalStateException e=new IllegalStateException(Resources.getResources(getLocale()).
                                        getString(ResourceKeys.ERROR_ILLEGAL_COORDINATE_SYSTEM));
            e.initCause(cause);
            throw e;
        } catch (TransformException cause) {
            // Should not happen because:
            //   1) We removed all previous layer (so there is nothing left to project).
            //   2) New layers should have a coordinate system compatible with current one.
            IllegalStateException e=new IllegalStateException(Resources.getResources(getLocale()).
                                        getString(ResourceKeys.ERROR_ILLEGAL_COORDINATE_SYSTEM));
            e.initCause(cause);
            throw e;
        }
    }

    /**
     * Add a layer to this renderer. A single {@link Layer} may be converted into an
     * arbitrary amount of {@link RenderedLayer}s. Those rendered layers will have
     * {@linkplain RenderedLayer#getZOrder z-order} values as 4.0, 4.1, 4.2, etc.
     * where 4 is the layer number, and .0, .1, .2... is the rendered layer number
     * for this particular layer.
     *
     * @param  layer The layer to add.
     * @throws TransformException if some feature in the layer use an incompatible
     *         coordinate system.
     */
    public synchronized void addLayer(final Layer layer) throws TransformException {
        removeLayer(layer);
        final Style          style = layer.getStyle();
        final FeatureCollection fc = layer.getFeatures();
        final Feature[]   features = (Feature[]) fc.toArray(new Feature[fc.size()]);
        final RenderedLayer[] rend = factory.create(features, style);
        final boolean      visible = layer.isVisible();
        final int       baseZOrder = renderedLayers.size();
        final double   zOrderScale = XMath.pow10((int)Math.ceil(XMath.log10(rend.length)));
        for (int j=0; j<rend.length; j++) {
            final RenderedLayer rendered = rend[j];
            rendered.setVisible(visible);
            rendered.setZOrder((float)(baseZOrder + j/zOrderScale));
            addLayer(rendered);
        }
        final LayerEntry entry = new LayerEntry(layer, rend);
        if (renderedLayers.put(layer, entry) != null) {
            throw new AssertionError(); // Should never happen.
        }
        fc.addListener(entry);
    }

    /**
     * Remove a layer from this renderer. Nothing is done if the specified layer is
     * <code>null</code> or not found in this renderer.
     *
     * @param layer The layer to remove.
     */
    public synchronized void removeLayer(final Layer layer) {
        final LayerEntry entry = (LayerEntry) renderedLayers.remove(layer);
        if (entry != null) {
            layer.getFeatures().removeListener(entry);
            final RenderedLayer[] rendered = entry.rendered;
            for (int i=0; i<rendered.length; i++) {
                removeLayer(rendered[i]);
            }
        }
    }

    /**
     * Remove all layers from this renderer.
     */
    public synchronized void removeAllLayers() {
        for (final Iterator it=renderedLayers.values().iterator(); it.hasNext();) {
            final LayerEntry entry = (LayerEntry) it.next();
            entry.layer.getFeatures().removeListener(entry);
        }
        renderedLayers.clear();
        super.removeAllLayers();
    }

    /**
     * Map a {@link Layer} to a set of {@link RenderedLayer} and to the listeners
     * needed for catching changes in collection and visibility.
     *
     * @version $Id: StyledRenderer.java,v 1.5 2003/09/01 13:54:02 desruisseaux Exp $
     * @author Martin Desruisseaux
     */
    private final class LayerEntry implements CollectionListener {
        /**
         * The layer.
         */
        final Layer layer;

        /**
         * The rendered layers.
         */
        final RenderedLayer[] rendered;

        /**
         * The feature bounds.
         */
        private Envelope bounds;

        /**
         * Construct a new entry.
         *
         * @param rendered The rendered layers.
         */
        public LayerEntry(final Layer layer, final RenderedLayer[] rendered) {
            this.layer    = layer;
            this.rendered = rendered;
            this.bounds   = layer.getFeatures().getBounds();
        }

        /**
         * Tells that all rendered layers need to be repainted. This method can be invoked
         * from any thread; it doesn't need to be the <cite>Swing</cite> thread.
         */
        public void repaint() {
            final Envelope envelope = layer.getFeatures().getBounds();
            final boolean contained = bounds.contains(envelope);
            for (int i=0; i<rendered.length; i++) {
                final RenderedLayer layer = rendered[i];
                if (contained) {
                    layer.repaint();
                } else {
                    layer.repaint(null);
                }
            }
            bounds = envelope;
        }

        /**
         * Invoked automatically when a collection changed.
         *
         * @param event The collection change event.
         */
        public void collectionChanged(final CollectionEvent event) {
            final FeatureCollection fc = event.getCollection();
            switch (event.getEventType()) {
                case CollectionEvent.FEATURES_CHANGED: {
                    repaint();
                    break;
                }
                case CollectionEvent.FEATURES_REMOVED: {
                    // Fall through
                }
                case CollectionEvent.FEATURES_ADDED: {
                    // Remove all features for this layer, and re-add them.
                    try {
                        addLayer(layer);
                    } catch (TransformException exception) {
                        handleException("StyledRenderer", "collectionChanged", exception);
                    }
                    break;
                }
            }
        }
    }
}
