/*
 * Geotools - OpenSource mapping toolkit
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
package org.geotools.renderer.j2d;

// J2SE dependencies
import java.awt.Shape;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.IllegalComponentStateException;
import java.awt.geom.Rectangle2D;
import java.awt.geom.AffineTransform;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.ToolTipManager;
import javax.swing.JComponent;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.Locale;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.LogRecord;

// Geotools dependencies
import org.geotools.cs.AxisInfo;
import org.geotools.cs.AxisOrientation;
import org.geotools.cs.CoordinateSystem;
import org.geotools.cs.FittedCoordinateSystem;
import org.geotools.cs.GeographicCoordinateSystem;
import org.geotools.ct.MathTransform;
import org.geotools.ct.MathTransform2D;
import org.geotools.ct.MathTransformFactory;
import org.geotools.ct.CoordinateTransformation;
import org.geotools.ct.CannotCreateTransformException;
import org.geotools.ct.NoninvertibleTransformException;
import org.geotools.ct.CoordinateTransformationFactory;
import org.geotools.ct.TransformException;
import org.geotools.resources.Utilities;
import org.geotools.resources.CTSUtilities;
import org.geotools.resources.renderer.Resources;
import org.geotools.resources.renderer.ResourceKeys;


/**
 * A renderer for drawing map objects into a {@link Graphics2D}. A newly constructed
 * <code>Renderer</code> is initially empty. To make something appears, {@link RendererObject}s
 * must be added using one of <code>add(...)</code> methods. The visual content depends of the
 * <code>RendererObject</code> subclass. It may be an isoline ({@link RenderedIsoline}),
 * a remote sensing image ({@link RenderedGridCoverageLayer}), a set of arbitrary marks
 * ({@link RenderedMarks}), a map scale ({@link RenderedMapScale}), etc.
 *
 * @version $Id: Renderer.java,v 1.3 2003/01/22 23:06:49 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public class Renderer {
    /**
     * The logger for the Java2D renderer module.
     */
    static final Logger LOGGER = Logger.getLogger("org.geotools.renderer.j2d");



    //////////////////////////////////////////////////////////////////
    ////////                                                  ////////
    ////////         RenderedObject (a.k.a "Layers")          ////////
    ////////                                                  ////////
    //////////////////////////////////////////////////////////////////
    /**
     * Objet utilisé pour comparer deux objets {@link RenderedObject}.
     * Ce comparateur permettra de classer les {@link RenderedObject}
     * par ordre croissant d'ordre <var>z</var>.
     */
    private static final Comparator COMPARATOR = new Comparator() {
        public int compare(final Object layer1, final Object layer2) {
            return Float.compare(((RenderedObject)layer1).getZOrder(),
                                 ((RenderedObject)layer2).getZOrder());
        }
    };

    /**
     * The set of {@link RenderedObject} to display. Named "layers" here because each
     * {@link RenderedObject} has its own <var>z</var> value and layer are painted in
     * increasing <var>z</var> order (i.e. layers with a hight <var>z</var> value are
     * painted on top of layers with a low <var>z</var> value).
     */
    private RenderedObject[] layers;

    /**
     * The number of valid elements in {@link #layers}.
     */
    private int layerCount;

    /**
     * Tells if elements in {@link #layers} are sorted in increasing <var>z</var> value.
     * If <code>false</code>, then <code>Arrays.sort(layers, COMPARATOR)</code> need to
     * be invoked.
     */
    private boolean layerSorted;



    //////////////////////////////////////////////////////////////////
    ////////                                                  ////////
    ////////        Coordinate systems and transforms         ////////
    ////////                                                  ////////
    //////////////////////////////////////////////////////////////////
    /**
     * Axis orientations in the Java2D space. Coordinates are in "dots" (about 1/72 of inch),
     * <var>x</var> values increasing right and <var>y</var> values increasing down. East and
     * South directions are relative to the screen (like {@link java.awt.BorderLayout}), not
     * geographic directions.
     */
    private static final AxisInfo[] TEXT_AXIS = new AxisInfo[] {
        new AxisInfo("Column", AxisOrientation.EAST),
        new AxisInfo("Line",   AxisOrientation.SOUTH)
    };

    /**
     * The "real world" coordinate system for rendering. This is the coordinate system for
     * what the user will see on the screen. Data from all {@link RenderedObject}s will be
     * projected in this coordinate system before to be rendered.  Units are usually "real
     * world" metres.
     *
     * This coordinate system is usually set once for ever and do not change anymore, except
     * if the user want to change the projection see on screen.
     *
     * @see #textCS
     * @see RenderingContext#mapCS
     */
    private CoordinateSystem mapCS = GeographicCoordinateSystem.WGS84;

    /**
     * The {@linkplain Graphics2D Java2D coordinate system}. Each "unit" is a dot (about
     * 1/72 of inch). <var>x</var> values increase toward the right of the screen and
     * <var>y</var> values increase toward the bottom of the screen.  This coordinate
     * system is appropriate for rendering text and labels.
     *
     * This coordinate system change every time the zoom change.
     * A <code>null</code> value means that it need to be recomputed.
     *
     * @see #mapCS
     * @see #deviceCS
     * @see RenderingContext#textCS
     */
    private CoordinateSystem textCS;

    /**
     * The device coordinate system. Each "unit" is a pixel of device-dependent size. When
     * rendering on screen, this coordinate system is identical to {@link #textCS}. When
     * rendering on printer or some other devices, it depends of the device's resolution.
     * This coordinate system is rarely used.
     *
     * A <code>null</code> value means that it need to be recomputed.
     *
     * @see #textCS
     * @see RenderingContext#deviceCS
     */
    private CoordinateSystem deviceCS;

    /**
     * A set of {@link MathTransform}s from various source CS. The target CS must be {@link #mapCS}
     * for all entries. Keys are source CS. This map is used only in order to avoid the costly call
     * to {@link CoordinateTransformationFactory#createFromCoordinateSystems} as much as possible.
     * If a transformation is not available in this collection, then the usual {@link #factory}
     * will be used.
     */
    private final Map transforms = new WeakHashMap();

    /**
     * The factory to use for creating {@link CoordinateTransformation} objects.
     * This factory can be set by {@link #setRenderingHint}.
     */
    private CoordinateTransformationFactory factory = CoordinateTransformationFactory.getDefault();



    //////////////////////////////////////////////////////////////////
    ////////                                                  ////////
    ////////        Rendering hints (e.g. resolution)         ////////
    ////////                                                  ////////
    //////////////////////////////////////////////////////////////////
    /**
     * A set of rendering hints. Recognized hints include
     * {@link Hints#COORDINATE_TRANSFORMATION_FACTORY} and
     * any of {@link RenderingHints}.
     *
     * @see Hints#RESOLUTION
     * @see Hints#COORDINATE_TRANSFORMATION_FACTORY
     */
    private final RenderingHints hints = new RenderingHints(null);

    /**
     * The rendering resolution, in units of {@link #mapCS} coordinate system
     * (usually metres or degree). A larger resolution speed up rendering, while
     * a smaller resolution draw more precise map. The value can be set with
     * {@link #setRenderingHint}.
     */
    private float resolution;

    /**
     * The bounding box of all {@link RenderedObject} in the {@link #mapCS} coordinate system.
     * This box is computed from {@link RenderedObject#getPreferredArea}. A <code>null</code>
     * value means that none of them returned a non-null value.
     */
    private Rectangle2D preferredArea;



    //////////////////////////////////////////////////////////////////
    ////////                                                  ////////
    ////////         AWT and Swing container / events         ////////
    ////////                                                  ////////
    //////////////////////////////////////////////////////////////////
    /**
     * The component owner, or <code>null</code> if none. This is used for managing
     * repaint request (see {@link RenderedObject#repaint}) or mouse events.
     */
    private final Component mapPane;

    /**
     * <code>true</code> if {@link #listeners} is currently registered into {@link #mapPane}.
     */
    private boolean listenerRegistered;

    /**
     * Objet "listener" ayant la charge de réagir aux différents
     * événements qui intéressent cet objet <code>Renderer</code>.
     */
    private final Listeners listeners = new Listeners();

    /**
     * Classe ayant la charge de réagir aux différents événements qui intéressent cet
     * objet <code>Renderer</code>. Cette classe réagira entre autres aux changements
     * de l'ordre <var>z</var> ainsi qu'aux changements des coordonnées géographiques
     * d'une couche.
     */
    private final class Listeners extends MouseAdapter implements ComponentListener,
                                                                  PropertyChangeListener
    {
        /** Invoked when the mouse has been clicked on a component. */
        public void mouseClicked(final MouseEvent event) {
//            Renderer.this.mouseClicked(event);
        }

        /** Invoked when the component's size changes. */
        public void componentResized(final ComponentEvent event) {
//            Renderer.this.zoomChanged(null);
        }

        /** Invoked when the component's position changes. */
        public void componentMoved(final ComponentEvent event) {
        }

        /** Invoked when the component has been made visible. */
        public void componentShown(final ComponentEvent event) {
        }

        /** Invoked when the component has been made invisible. */
        public void componentHidden(final ComponentEvent event) {
//            clearCache();
        }

        /** Invoked when a {@link RenderedObject}'s property is changed. */
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
//                changeArea((Rectangle2D)     event.getOldValue(),
//                           (Rectangle2D)     event.getNewValue(),
//                           ((RenderedObject) event.getSource()).getCoordinateSystem(),
//                           "RenderedObject", "setPreferredArea");
                return;
            }
            if (propertyName.equalsIgnoreCase("zOrder")) {
                layerSorted = false;
                return;
            }
            if (propertyName.equalsIgnoreCase("tools")) {
                if ((event.getOldValue()==null) != (event.getNewValue()==null)) {
                    registerListeners();
                }
                return;
            }
        }
    }

    /**
     * Construct a new renderer for the specified component.
     *
     * @param owner The widget that own this renderer, or <code>null</code> if none.
     */
    public Renderer(final Component owner) {
        this.mapPane = owner;
        if (mapPane != null) {
            mapPane.addComponentListener(listeners);
        }
    }

    /**
     * Returns the view coordinate system. This is the "real world" coordinate system
     * used for displaying all {@link RenderedObject}s. Note that underlying data in
     * <code>RenderedObject</code> doesn't need to be in this coordinate system:
     * transformations will performed on the fly as needed at rendering time.
     *
     * @return The two dimensional coordinate system used for display.
     *         Default to {@linkplain GeographicCoordinateSystem#WGS84 WGS 1984}.
     */
    public CoordinateSystem getCoordinateSystem() {
        return mapCS;
    }

    /**
     * Set the view coordinate system. This is the "real world" coordinate
     * system to use for displaying all {@link RenderedObject}s. Default is
     * {@linkplain GeographicCoordinateSystem#WGS84 WGS 1984}. Changing this
     * coordinate system has no effect on any <code>RenderedObject</code>'s
     * underlying data, since transformation are performed only at rendering
     * time.
     *
     * @param cs The view coordinate system. If this coordinate system has
     *           more than 2 dimensions, then only the 2 first will be retained.
     * @throws TransformException If <code>cs</code> can't be reduced to a two-dimensional
     *         coordinate system., or if data can't be transformed for some other reason.
     */
    public void setCoordinateSystem(CoordinateSystem cs) throws TransformException {
        cs = CTSUtilities.getCoordinateSystem2D(cs);
        if (!cs.equals(mapCS)) {
            mapCS    = cs;
            textCS   = null;
            deviceCS = null;
            transforms.clear();
        }
    }

    /**
     * Returns a bounding box that completely encloses all layer's {@linkplain
     * RenderedObject#getPreferredArea preferred area}, visible or not. This
     * bounding box should be representative of the geographic area to drawn.
     * Coordinates are expressed in this {@linkplain #getCoordinateSystem
     * renderer's coordinate system}.
     *
     * @return The enclosing area computed from available data, or <code>null</code>
     *         if this area can't be computed.
     */
    public Rectangle2D getPreferredArea() {
        final Rectangle2D area = this.preferredArea;
        return (area!=null) ? (Rectangle2D) area.clone() : null;
    }

    /**
     * Set the geographic area. This is method is invoked as a result of
     * internal computation. User should not call this method directly.
     */
    private void setArea(final Rectangle2D newArea) {
        final Rectangle2D oldArea = this.preferredArea;
        if (!Utilities.equals(oldArea, newArea)) {
            this.preferredArea = newArea;
//            firePropertyChange("area", oldArea, newArea);
//            fireZoomChanged(new AffineTransform()); // Update scrollbars
// TODO: Need protected access
//          log("fr.ird.map", "MapPanel", "setArea", newArea);
        }
    }

    /**
     * Returns a rendering hints.
     *
     * @param  key The hint key (e.g. {@link Hints#RESOLUTION}).
     * @return The hint value for the specified key.
     *
     * @see Hints#RESOLUTION
     * @see Hints#COORDINATE_TRANSFORMATION_FACTORY
     */
    public Object getRenderingHints(final RenderingHints.Key key) {
        return hints.get(key);
    }

    /**
     * Add a rendering hints. Hints provides optional information used by some rendering code.
     *
     * @param key   The hint key (e.g. {@link Hints#RESOLUTION}).
     * @param value The hint value.
     *
     * @see Hints#RESOLUTION
     * @see Hints#COORDINATE_TRANSFORMATION_FACTORY
     */
    public void setRenderingHint(final RenderingHints.Key key, final Object value) {
        hints.put(key, value);
        if (Hints.RESOLUTION.equals(key)) {
            resolution = ((Number) hints.get(key)).floatValue();
            if (!(resolution >= 0)) {
                resolution = 0;
            }
        } else if (Hints.COORDINATE_TRANSFORMATION_FACTORY.equals(key)) {
            factory = (CoordinateTransformationFactory) hints.get(key);
            transforms.clear();
        }
    }

    /**
     * Returns a fitted coordinate system for {@link #textCS} and {@link #deviceCS}.
     *
     * @param  name     The coordinate system name (e.g. "text" or "device").
     * @param  base     The base coordinate system (e.g. {@link #mapCS}).
     * @param  fromBase The transform from the base CS to the fitted CS.  Note that this is the
     *                  opposite of the usual {@link FittedCoordinateSystem} constructor. We do
     *                  it that way because it is the way we usually gets affine transform from
     *                  {@link Graphics2D}.
     * @return The fitted coordinate system, or <code>base</code> if the transform is the identity
     *         transform.
     * @throws NoninvertibleTransformException if the affine transform is not invertible.
     */
    private CoordinateSystem createFittedCoordinateSystem(final String           name,
                                                          final CoordinateSystem base,
                                                          final AffineTransform fromBase)
            throws NoninvertibleTransformException
    {
        if (fromBase.isIdentity()) {
            return base;
        }
        /*
         * Inverse the MathTransform rather than the AffineTransform because MathTransform
         * keep a reference to its inverse. It avoid the need for re-inversing it again later,
         * which help to avoid rounding errors.
         */
        final MathTransform toBase;
        toBase = factory.getMathTransformFactory().createAffineTransform(fromBase).inverse();
        return new FittedCoordinateSystem(name, base, toBase, TEXT_AXIS);
    }

    /**
     * Construct a transform between two coordinate systems. If a {@link
     * Hints#COORDINATE_TRANSFORMATION_FACTORY} has been provided, the
     * specified {@link CoordinateTransformationFactory} will be used.
     *
     * @param  sourceCS The source coordinate system.
     * @param  targetCS The target coordinate system.
     * @return A transformation from <code>sourceCS</code> to <code>targetCS</code>.
     * @throws CannotCreateTransformException if the transformation can't be created.
     *
     * @see #getRenderingHint
     * @see #setRenderingHint
     * @see Hints#COORDINATE_TRANSFORMATION_FACTORY
     */
    final MathTransform getMathTransform(final CoordinateSystem sourceCS,
                                         final CoordinateSystem targetCS)
            throws CannotCreateTransformException
    {
        MathTransform tr;
        /*
         * Check if the math transform is available in the cache. A majority of transformations
         * will be from 'layerCS' to 'mapCS' to 'textCS'.  The cache looks for the 'layerCS' to
         * to 'mapCS' transform.
         */
        final boolean cachedTransform = targetCS.equals(mapCS, false);
        if (cachedTransform) {
            tr = (MathTransform) transforms.get(sourceCS);
            if (tr != null) {
                return tr;
            }
        }
        /*
         * If one of the CS is a FittedCoordinateSystem, then check if we can use directly
         * its 'toBase' transform without using the costly CoordinateTransformationFactory.
         * This check is worth to be done since it is a very common situation.  A majority
         * of transformations will be from 'mapCS' to 'textCS',  which is the case we test
         * first. The converse (transformations from 'textCS' to 'mapCS') is less frequent
         * and can be catched by the 'transform' cache, which is why we test it last.
         */
        if (targetCS instanceof FittedCoordinateSystem) {
            final FittedCoordinateSystem fittedCS = (FittedCoordinateSystem) targetCS;
            if (sourceCS.equals(fittedCS.getBaseCoordinateSystem(), false)) try {
                return fittedCS.getToBase().inverse();
            } catch (NoninvertibleTransformException exception) {
                throw new CannotCreateTransformException(sourceCS, targetCS, exception);
            }
        }
        if (sourceCS instanceof FittedCoordinateSystem) {
            final FittedCoordinateSystem fittedCS = (FittedCoordinateSystem) sourceCS;
            if (targetCS.equals(fittedCS.getBaseCoordinateSystem(), false)) {
                tr = fittedCS.getToBase();
                if (cachedTransform) {
                    transforms.put(sourceCS, tr);
                }
                return tr;
            }
        }
        if (sourceCS.equals(targetCS, false)) {
            return MathTransform2D.IDENTITY;
        }
        /*
         * Now that we failed to reuse a pre-existing transform, ask to the factory
         * to create a new one. A message is logged in order to trace down the amount
         * of coordinate transformations created.
         */
        if (LOGGER.isLoggable(Level.FINER)) {
            // FINER is the default level for entering, returning, or throwing an exception.
            final LogRecord record = Resources.getResources(null).getLogRecord(Level.FINER,
                                               ResourceKeys.INITIALIZING_TRANSFORMATION_$2,
                                               toString(sourceCS), toString(targetCS));
            record.setSourceClassName ("Renderer");
            record.setSourceMethodName("getMathTransform");
            LOGGER.log(record);
        }
        tr = factory.createFromCoordinateSystems(sourceCS, targetCS).getMathTransform();
        if (cachedTransform) {
            transforms.put(sourceCS, tr);
        }
        return tr;
    }

    /**
     * Returns a string representation of a coordinate system. This method is
     * used for formatting a logging message in {@link #getMathTransform}.
     */
    private static String toString(final CoordinateSystem cs) {
        final StringBuffer buffer = new StringBuffer(Utilities.getShortClassName(cs));
        buffer.append('[');
        final String name = cs.getName(null);
        if (name != null) {
            buffer.append('"');
            buffer.append(name);
            buffer.append('"');
        }
        buffer.append(']');
        return buffer.toString();
    }

    /**
     * Register {@link #listeners} if at least one layer has a tool, or unregister it
     * if no layer has tools.   This method is automatically invoked when the "tools"
     * property change in a {@link RenderedObject}.
     */
    private void registerListeners() {
        if (mapPane != null) {
            boolean hasTools = false;
            for (int i=layerCount; --i>=0;) {
                if (layers[i].getTools() != null) {
                    hasTools = true;
                    break;
                }
            }
            if (hasTools != listenerRegistered) {
                mapPane.removeMouseListener(listeners);
                if (hasTools) {
                    mapPane.addMouseListener(listeners);
                    if (mapPane instanceof JComponent) {
                        ToolTipManager.sharedInstance().registerComponent((JComponent)mapPane);
                    }
                } else {
                    if (mapPane instanceof JComponent) {
                        ToolTipManager.sharedInstance().unregisterComponent((JComponent)mapPane);
                    }
                }
                listenerRegistered = hasTools;
            }
        }
    }

    /**
     * Returns to locale for this renderer. The renderer will inherit
     * the locale of its {@link Component}, if he have one. Otherwise,
     * a default locale will be returned.
     *
     * @see Component#getLocale
     */
    public Locale getLocale() {
        if (mapPane != null) try {
            return mapPane.getLocale();
        } catch (IllegalComponentStateException exception) {
            // Not yet added to a containment hierarchy. Ignore...
        }
        return JComponent.getDefaultLocale();
    }
}
