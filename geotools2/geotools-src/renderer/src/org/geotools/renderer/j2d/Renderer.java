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
import java.awt.Frame;
import java.awt.Dialog;
import java.awt.Rectangle;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.IllegalComponentStateException;
import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.AffineTransform;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import javax.swing.JInternalFrame;
import javax.swing.ToolTipManager;
import javax.swing.JComponent;
import javax.swing.Action;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.Locale;
import java.util.Arrays;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.LogRecord;

// Java Advanced Imaging
import javax.media.jai.GraphicsJAI;
import javax.media.jai.PlanarImage; // For Javadoc

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
import org.geotools.resources.XArray;
import org.geotools.resources.Utilities;
import org.geotools.resources.CTSUtilities;
import org.geotools.resources.XDimension2D;
import org.geotools.resources.XRectangle2D;
import org.geotools.resources.GraphicsUtilities;
import org.geotools.resources.renderer.Resources;
import org.geotools.resources.renderer.ResourceKeys;


/**
 * A renderer for drawing map objects into a {@link Graphics2D}. A newly constructed
 * <code>Renderer</code> is initially empty. To make something appears, {@link RenderedLayer}s
 * must be added using one of <code>addLayer(...)</code> methods. The visual content depends of
 * the <code>RenderedLayer</code> subclass. It may be an isoline ({@link RenderedIsoline}),
 * a remote sensing image ({@link RenderedGridCoverage}), a set of arbitrary marks
 * ({@link RenderedMarks}), a map scale ({@link RenderedMapScale}), etc.
 *
 * @version $Id: Renderer.java,v 1.13 2003/02/02 21:47:45 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public class Renderer {
    /**
     * The logger for the Java2D renderer module.
     */
    static final Logger LOGGER = Logger.getLogger("org.geotools.renderer.j2d");

    /**
     * Small value for avoiding rounding error.
     */
    private static final double EPS = 1E-6;



    //////////////////////////////////////////////////////////////////
    ////////                                                  ////////
    ////////         RenderedLayers (a.k.a "Layers")          ////////
    ////////                                                  ////////
    //////////////////////////////////////////////////////////////////
    /**
     * Objet utilisé pour comparer deux objets {@link RenderedLayer}.
     * Ce comparateur permettra de classer les {@link RenderedLayer}
     * par ordre croissant d'ordre <var>z</var>.
     */
    private static final Comparator COMPARATOR = new Comparator() {
        public int compare(final Object layer1, final Object layer2) {
            return Float.compare(((RenderedLayer)layer1).getZOrder(),
                                 ((RenderedLayer)layer2).getZOrder());
        }
    };

    /**
     * An empty list of layers.
     */
    private static final RenderedLayer[] EMPTY = new RenderedLayer[0];

    /**
     * The set of {@link RenderedLayer} to display. Named "layers" here because each
     * {@link RenderedLayer} has its own <var>z</var> value and layer are painted in
     * increasing <var>z</var> order (i.e. layers with a hight <var>z</var> value are
     * painted on top of layers with a low <var>z</var> value).
     */
    private RenderedLayer[] layers;

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
     * The coordinate systems for rendering. This object contains the following:
     * <ul>
     *   <li>The "real world" coordinate system  ({@link RenderingContext#mapCS mapCS}).
     *       Should never be <code>null</code>, since it is the viewer coordinate system
     *       as returned by {@link #getCoordinateSystem}.</li>
     *   <li>The Java2D coordinate system ({@link RenderingContext#textCS textCS}),  which
     *       is rendering-dependent.   This CS must be the one used for the last rendering
     *       ({@link GeoMouseEvent rely on it}), or <code>null</code> if the map has never
     *       been rendered yet.</li>
     *   <li>The device coordinate system ({@link RenderingContext#deviceCS deviceCS}), which
     *       is rendering-dependent.  This CS must be the one used for the last rendering, or
     *       <code>null</code> if the map has never been rendered yet.</li>
     * </ul>
     *
     * @see RenderingContext#mapCS
     * @see RenderingContext#textCS
     * @see RenderingContext#deviceCS
     */
    private RenderingContext context;

    /**
     * The affine transform from {@link RenderingContext#mapCS mapCS} to
     * {@link RenderingContext#textCS textCS} used in the last rendering.
     * This transform is used by {@link GeoMouseEvent#getMapCoordinate}.
     */
    final AffineTransform mapToText = new AffineTransform();

    /**
     * A set of {@link MathTransform}s from various source CS. The target CS must be
     * {@link RenderingContext#mapCS} for all entries. Keys are source CS.  This map
     * is used only in order to avoid the costly call to
     * {@link CoordinateTransformationFactory#createFromCoordinateSystems} as much as
     * possible. If a transformation is not available in this collection, then the usual
     * {@link #factory} will be used.
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
     * any of {@link Hints#FINEST_RESOLUTION}.
     *
     * @see Hints#FINEST_RESOLUTION
     * @see Hints#REQUIRED_RESOLUTION
     * @see Hints#COORDINATE_TRANSFORMATION_FACTORY
     * @see Hints#PREFETCH
     * @see RenderingHints#KEY_RENDERING
     * @see RenderingHints#KEY_COLOR_RENDERING
     * @see RenderingHints#KEY_INTERPOLATION
     */
    protected final RenderingHints hints = new RenderingHints(null);

    /**
     * <code>true</code> if the renderer is allowed to prefetch data before to
     * paint layers.   Prefetching data may speed up rendering on machine with
     * more than one processor.
     */
    private boolean prefetch;

    /**
     * The rendering resolutions, in units of {@link RenderingContext#textCS} coordinate system
     * (usually 1/72 of inch). A larger resolution speed up rendering, while a smaller resolution
     * draw more precise map. The value can be set with {@link #setRenderingHint}. They are read
     * only by {@link RenderedIsoline}.
     */
    float minResolution, maxResolution;

    /**
     * The bounding box of all {@link RenderedLayer} in the {@link RenderingContext#mapCS}
     * coordinate system. This box is computed from {@link RenderedLayer#getPreferredArea}.
     * A <code>null</code> value means that none of them returned a non-null value.
     */
    private Rectangle2D preferredArea;

    /**
     * Statistics about rendering. Used for logging messages only.
     */
    final RenderingStatistics statistics = new RenderingStatistics();



    //////////////////////////////////////////////////////////////////
    ////////                                                  ////////
    ////////         AWT and Swing container / events         ////////
    ////////                                                  ////////
    //////////////////////////////////////////////////////////////////
    /**
     * Default tools to use if there is no {@link RenderedLayer#getTools} for a specific job.
     * Can be <code>null</code> if no default tools has been set.
     */
    private Tools tools;

    /**
     * The component owner, or <code>null</code> if none. This is used for managing
     * repaint request (see {@link RenderedLayer#repaint}) or mouse events.
     */
    final Component mapPane;

    /**
     * Listeners to be notified about any changes in this layer's properties.
     * Examples of properties that may change:
     * <code>"preferredArea"</code>.
     */
    protected final PropertyChangeSupport listeners;

    /**
     * <code>true</code> if {@link #listenerProxy} is currently registered into {@link #mapPane}.
     */
    private boolean listenerRegistered;

    /**
     * Listener for events of interest to this renderer. Events may come
     * from any {@link RenderedLayer} or from the {@link Component}.
     */
    private final ListenerProxy listenerProxy = new ListenerProxy();

    /**
     * Classe ayant la charge de réagir aux différents événements qui intéressent cet
     * objet <code>Renderer</code>. Cette classe réagira entre autres aux changements
     * de l'ordre <var>z</var> ainsi qu'aux changements des coordonnées géographiques
     * d'une couche.
     */
    private final class ListenerProxy extends MouseAdapter implements ComponentListener,
                                                                      PropertyChangeListener
    {
        /** Invoked when the mouse has been clicked on a component. */
        public void mouseClicked(final MouseEvent event) {
            if (event instanceof GeoMouseEvent) {
                Renderer.this.mouseClicked((GeoMouseEvent)event);
            }
        }

        /** Invoked when the component's size changes. */
        public void componentResized(final ComponentEvent event) {
            synchronized (Renderer.this) {
                zoomChanged(null);
            }
        }

        /** Invoked when the component's position changes. */
        public void componentMoved(final ComponentEvent event) {
        }

        /** Invoked when the component has been made visible. */
        public void componentShown(final ComponentEvent event) {
            // It would be nice to invokes 'prefetch(...)' here, but we don't know
            // yet for sure the widget bounds and the zoom.  We are better to wait
            // until 'paint(...)' is invoked.
        }

        /** Invoked when the component has been made invisible. */
        public void componentHidden(final ComponentEvent event) {
            clearCache();
        }

        /** Invoked when a {@link RenderedLayer}'s property is changed. */
        public void propertyChange(final PropertyChangeEvent event) {
            final String propertyName = event.getPropertyName();
            synchronized (Renderer.this) {
                if (propertyName.equalsIgnoreCase("preferredArea")) {
                    changePreferredArea((Rectangle2D)    event.getOldValue(),
                                        (Rectangle2D)    event.getNewValue(),
                                        ((RenderedLayer) event.getSource()).getCoordinateSystem(),
                                        "RenderedLayer", "setPreferredArea");
                    return;
                }
                if (propertyName.equalsIgnoreCase("zOrder")) {
                    layerSorted = false;
                    return;
                }
                if (propertyName.equalsIgnoreCase("tools")) {
                    if ((event.getOldValue()==null) != (event.getNewValue()==null)) {
                        updateListenerRegistration();
                    }
                    return;
                }
                if (propertyName.equalsIgnoreCase("coordinateSystem")) {
                    computePreferredArea("RenderedLayer", "setCoordinateSystem");
                    return;
                }
            }
        }
    }



    //////////////////////////////////////////////////////////////////
    ////////                                                  ////////
    ////////      Constructors and essential properties       ////////
    ////////                                                  ////////
    //////////////////////////////////////////////////////////////////
    /**
     * Construct a new renderer for the specified component.
     *
     * @param owner The widget that own this renderer, or <code>null</code> if none.
     */
    public Renderer(final Component owner) {
        final CoordinateSystem cs = GeographicCoordinateSystem.WGS84;
        context   = new RenderingContext(this, cs, cs, cs);
        listeners = new PropertyChangeSupport(this);
        this.mapPane = owner;
        if (mapPane != null) {
            mapPane.addComponentListener(listenerProxy);
        }
    }

    /**
     * Returns this renderer's name. The default implementation returns the title
     * of the window which contains the map pane.
     *
     * @param  locale The desired locale, or <code>null</code> for a default locale.
     * @return This renderer's name, or <code>null</code> if none.
     *
     * @see #getLocale
     * @see Component#getName
     * @see RenderedLayer#getName
     */
    public synchronized String getName(final Locale locale) {
        for (Component c=mapPane; c!=null; c=c.getParent()) {
            if (c instanceof Frame) {
                return ((Frame) c).getTitle();
            }
            if (c instanceof Dialog) {
                return ((Dialog) c).getTitle();
            }
            if (c instanceof JInternalFrame) {
                return ((JInternalFrame) c).getTitle();
            }
        }
        return null;
    }

    /**
     * Returns the view coordinate system. This is the "real world" coordinate system
     * used for displaying all {@link RenderedLayer}s. Note that underlying data in
     * <code>RenderedLayer</code> doesn't need to be in this coordinate system:
     * transformations will performed on the fly as needed at rendering time.
     *
     * @return The two dimensional coordinate system used for display.
     *         Default to {@linkplain GeographicCoordinateSystem#WGS84 WGS 1984}.
     */
    public CoordinateSystem getCoordinateSystem() {
        return context.mapCS;
    }

    /**
     * Set the view coordinate system. This is the "real world" coordinate
     * system to use for displaying all {@link RenderedLayer}s. Default is
     * {@linkplain GeographicCoordinateSystem#WGS84 WGS 1984}. Changing this
     * coordinate system has no effect on any <code>RenderedLayer</code>'s
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
        final CoordinateSystem oldCS;
        synchronized (this) {
            oldCS = getCoordinateSystem();
            if (!cs.equals(oldCS)) {
                CoordinateSystem textCS = createFittedCoordinateSystem("textCS", cs, mapToText);
                context = new RenderingContext(this, cs, textCS, textCS);
                clearCache();
            }
        }
        listeners.firePropertyChange("coordinateSystem", oldCS, cs);
    }

    /**
     * Returns a bounding box that completely encloses all layer's {@linkplain
     * RenderedLayer#getPreferredArea preferred area}, visible or not. This
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
     *
     * @param newArea The new preferred area (will not be cloned).
     */
    private void setPreferredArea(final Rectangle2D newArea) {
        final Rectangle2D oldArea;
        synchronized (this) {
            oldArea = preferredArea;
            preferredArea = newArea;
        }
        listeners.firePropertyChange("preferredArea", oldArea, newArea);
    }

    /**
     * Recompute inconditionnaly the {@link #preferredArea}. Will be computed
     * from the value returned by {@link RenderedLayer#getPreferredArea} for
     * all layers.
     *
     * @param  sourceClassName  The caller's class name, for logging purpose.
     * @param  sourceMethodName The caller's method name, for logging purpose.
     */
    private void computePreferredArea(final String sourceClassName, final String sourceMethodName) {
        assert Thread.holdsLock(this);
        Rectangle2D         newArea = null;
        CoordinateSystem lastSystem = null;
        MathTransform2D   transform = null;
        for (int i=layerCount; --i>=0;) {
            final RenderedLayer layer = layers[i];
            Rectangle2D bounds = layer.getPreferredArea();
            if (bounds != null) {
                final CoordinateSystem coordinateSystem = layer.getCoordinateSystem();
                try {
                    if (lastSystem==null || !lastSystem.equals(coordinateSystem, false)) {
                        transform  = (MathTransform2D) getMathTransform(
                                                       coordinateSystem, getCoordinateSystem(),
                                                       sourceClassName, sourceMethodName);
                        lastSystem = coordinateSystem;
                    }
                    bounds = CTSUtilities.transform(transform, bounds, null);
                    if (newArea == null) {
                        newArea = bounds;
                    } else {
                        newArea.add(bounds);
                    }
                } catch (TransformException exception) {
                    handleException(sourceClassName, sourceMethodName, exception);
                    // Continue. The preferred area for this layer will be ignored.
                }
            }
        }
        setPreferredArea(newArea);
    }

    /**
     * Remplace un rectangle par un autre dans le calcul de {@link #preferredArea}. Si ça a eu
     * pour effet de changer les coordonnées géographiques couvertes, un événement approprié
     * sera lancé. Cette méthode est plus économique que {@link #computePreferredArea} du fait
     * qu'elle essaie de ne pas tout recalculer. Si on n'a pas pu faire l'économie d'un recalcul
     * toutefois, alors {@link #computePreferredArea} sera appelée.
     *
     * @param  oldSubArea       The old preferred area of the layer that changed.
     * @param  newSubArea       The new preferred area of the layer that changed.
     * @param  coordinateSystem The coordinate system for <code>[old|new]SubArea</code>.
     * @param  sourceClassName  The caller's class name, for logging purpose.
     * @param  sourceMethodName The caller's method name, for logging purpose.
     */
    private void changePreferredArea(Rectangle2D oldSubArea,
                                     Rectangle2D newSubArea,
                                     final CoordinateSystem coordinateSystem,
                                     final String sourceClassName,
                                     final String sourceMethodName)
    {
        assert Thread.holdsLock(this);
        try {
            final MathTransform2D transform = (MathTransform2D) getMathTransform(
                    coordinateSystem, getCoordinateSystem(), sourceClassName, sourceMethodName);
            oldSubArea = CTSUtilities.transform(transform, oldSubArea, null);
            newSubArea = CTSUtilities.transform(transform, newSubArea, null);
        } catch (TransformException exception) {
            handleException(sourceClassName, sourceMethodName, exception);
            computePreferredArea(sourceClassName, sourceMethodName);
            return;
        }
        final Rectangle2D expandedArea = changeArea(preferredArea, oldSubArea, newSubArea);
        if (expandedArea != null) {
            setPreferredArea(expandedArea);
        } else {
            computePreferredArea(sourceClassName, sourceMethodName);
        }
    }

    /**
     * Agrandi (si nécessaire) une région géographique en fonction de l'ajout, la supression ou
     * la modification des coordonnées d'une sous-région. Cette méthode est appelée lorsque les
     * coordonnées de la sous-région <code>oldSubArea</code> ont changées pour devenir
     * <code>newSubArea</code>. Si ce changement s'est traduit par un agrandissement de
     * <code>area</code>, alors le nouveau rectangle agrandi sera retourné. Si le changement
     * n'a aucun impact sur <code>area</code>, alors <code>area</code> sera retourné tel quel.
     * Si le changement PEUT avoir diminué la dimension de <code>area</code>, alors cette méthode
     * retourne <code>null</code> pour indiquer qu'il faut recalculer <code>area</code> à partir
     * de zéro.
     *
     * @param  area       Région géographique qui pourrait être affectée par le changement de
     *                    coordonnées d'une sous-région. En aucun cas ce rectangle <code>area</code>
     *                    ne sera directement modifié. Si une modification est nécessaire, elle sera
     *                    faite sur un clone qui sera retourné. Cet argument peut être
     *                    <code>null</code> si aucune région n'était précédemment définie.
     * @param  oldSubArea Anciennes coordonnées de la sous-région, ou <code>null</code> si la
     *                    sous-région n'existait pas avant l'appel de cette méthode. Ce rectangle
     *                    ne sera jamais modifié ni retourné.
     * @param  newSubArea Nouvelles coordonnées de la sous-région, ou <code>null</code> si la
     *                    sous-région est supprimée. Ce rectangle ne sera jamais modifié ni
     *                    retourné.
     *
     * @return Un rectangle contenant les coordonnées mises-à-jour de <code>area</code>, si cette
     *         mise-à-jour a pu se faire. Si elle n'a pas pu être faite faute d'informations, alors
     *         cette méthode retourne <code>null</code>. Dans ce dernier cas, il faudra recalculer
     *         <code>area</code> à partir de zéro.
     */
    private static Rectangle2D changeArea(Rectangle2D area,
                                          final Rectangle2D oldSubArea,
                                          final Rectangle2D newSubArea)
    {
        if (area == null) {
            /*
             * Si aucune région n'avait été définie auparavant. La sous-région
             * "newSubArea" représente donc la totalité de la nouvelle région
             * "area". On construit un nouveau rectangle plutôt que de faire un
             * clone pour être certain d'avoir un type d'une précision suffisante.
             */
            if (newSubArea != null) {
                area = new Rectangle2D.Double();
                area.setRect(newSubArea);
            }
            return area;
        }
        if (newSubArea == null) {
            /*
             * Une sous-région a été supprimée ("newSubArea" est nulle). Si la sous-région supprimée ne
             * touchait pas au bord de "area",  alors sa suppression ne peut pas avoir diminuée "area":
             * on retournera alors area. Si au contraire "oldSubArea" touchait au bord de "area", alors
             * on ne sait pas si la suppression de "oldSubArea" a diminué "area".  Il faudra recalculer
             * "area" à partir de zéro, ce que l'on indique en retournant NULL.
             */
            if (               oldSubArea==null  ) return area;
            if (contains(area, oldSubArea, false)) return area;
            return null;
        }
        if (oldSubArea != null) {
            /*
             * Une sous-région a changée ("oldSubArea" est devenu "newSubArea"). Si on détecte que ce
             * changement PEUT diminuer la superficie totale de "area", il faudra recalculer "area" à
             * partir de zéro pour en être sur. On retourne donc NULL.  Si au contraire la superficie
             * totale de "area" ne peut pas avoir diminuée, elle peut avoir augmentée. Ce calcul sera
             * fait à la fin de cette méthode, qui poursuit son cours.
             */
            double t;
            if (((t=oldSubArea.getMinX()) <= area.getMinX() && t < newSubArea.getMinX()) ||
                ((t=oldSubArea.getMaxX()) >= area.getMaxX() && t > newSubArea.getMaxX()) ||
                ((t=oldSubArea.getMinY()) <= area.getMinY() && t < newSubArea.getMinY()) ||
                ((t=oldSubArea.getMaxY()) >= area.getMaxY() && t > newSubArea.getMaxY()))
            {
                return null; // Le changement PEUT avoir diminué "area".
            }
        }
        /*
         * Une nouvelle sous-région est ajoutée. Si elle était déjà
         * entièrement comprise dans "area", alors son ajout n'aura
         * aucun impact sur "area" et peut être ignoré.
         */
        if (!contains(area, newSubArea, true)) {
            // Cloner est nécessaire pour que "firePropertyChange"
            // puisse connaître l'ancienne valeur de "area".
            area = (Rectangle2D) area.clone();
            area.add(newSubArea);
        }
        return area;
    }

    /**
     * Indique si la région géographique <code>big</code> contient entièrement la sous-région
     * <code>small</code> spécifiée. Un cas particuluer survient si un ou plusieurs bords de
     * <code>small</code> coïncide avec les bords correspondants de <code>big</code>. L'argument
     * <code>edge</code> indique si on considère qu'il y a inclusion ou pas dans ces circonstances.
     *
     * @param big   Région géographique dont on veut vérifier s'il contient une sous-région.
     * @param small Sous-région géographique dont on veut vérifier l'inclusion dans <code>big</code>.
     * @param edge <code>true</code> pour considérer qu'il y a inclusion si ou ou plusieurs bords
     *        de <code>big</code> et <code>small</code> conïncide, ou <code>false</code> pour exiger
     *        que <code>small</code> ne touche pas aux bords de <code>big</code>.
     */
    private static boolean contains(final Rectangle2D big, final Rectangle2D small, final boolean edge)
    {
        return edge ? (small.getMinX()>=big.getMinX() && small.getMaxX()<=big.getMaxX() && small.getMinY()>=big.getMinY() && small.getMaxY()<=big.getMaxY()):
                      (small.getMinX()> big.getMinX() && small.getMaxX()< big.getMaxX() && small.getMinY()> big.getMinY() && small.getMaxY()< big.getMaxY());
    }

    /**
     * Returns the preferred pixel size in "real world" coordinates. For image layers, this is
     * the size of image's pixels. For other kind of layers, "pixel size" are to be understood
     * as some dimension representative of the layer's resolution.  This method invokes {@link
     * RenderedLayer#getPreferredPixelSize} for each layers and returns the finest resolution,
     * transformed in this {@linkplain #getCoordinateSystem renderer's coordinate system}.
     *
     * @return The preferred pixel size in "real world" coordinates, or <code>null</code>
     *         if no layer provided a transformable preferred pixel size.
     *
     * @task TODO: Transformations should use MathTransform.derivative(...)
     *             instead, but it is not yet implemented for projections.
     */
    public Dimension2D getPreferredPixelSize() {
        double minWidth  = Double.POSITIVE_INFINITY;
        double minHeight = Double.POSITIVE_INFINITY;
        for (int i=layerCount; --i>=0;) {
            final RenderedLayer layer = layers[i];
            final Dimension2D size = layer.getPreferredPixelSize();
            if (size!=null) try {
                double width  = size.getWidth();
                double height = size.getHeight();
                final MathTransform2D transform = (MathTransform2D) getMathTransform(
                                                        layer.getCoordinateSystem(),
                                                        this. getCoordinateSystem(),
                                                        "Renderer", "getPreferredPixelSize");
                if (!transform.isIdentity()) {
                    /*
                     * Create a pixel in the midle of the preferred area and transform it to
                     * this coordinate system.  TODO: we should use MathTransform.derivative
                     * instead, but is is not yet implemented for projections.
                     */
                    Rectangle2D area = layer.getPreferredArea();
                    if (area == null) {
                        area = new Rectangle2D.Double();
                    }
                    area.setRect(area.getCenterX()-0.5*width,
                                 area.getCenterY()-0.5*height,
                                 width, height);
                    area   = CTSUtilities.transform(transform, area, area);
                    width  = area.getWidth();
                    height = area.getHeight();
                }
                if (width  < minWidth ) minWidth =width;
                if (height < minHeight) minHeight=height;
            } catch (TransformException exception) {
                handleException("Renderer", "getPreferredPixelSize", exception);
                // Not a big deal. Continue...
            }
        }
        if (!Double.isInfinite(minWidth) && !Double.isInfinite(minHeight)) {
            return new XDimension2D.Double(minWidth, minHeight);
        } else {
            return null;
        }
    }



    //////////////////////////////////////////////////////////////////
    ////////                                                  ////////
    ////////          add/remove/get rendered layers          ////////
    ////////                                                  ////////
    //////////////////////////////////////////////////////////////////
    /**
     * Add a new layer to this renderer. A <code>Renderer</code> do not draw anything as long
     * as at least one layer hasn't be added.    A {@link RenderedLayer} can be anything like
     * an isobath, a remote sensing image, city locations, map scale, etc.  The drawing order
     * (relative to other layers) is determined by the {@linkplain RenderedLayer#getZOrder
     * z-order} property. A {@link RenderedLayer} object can be added to only one
     * <code>Renderer</code> object.
     *
     * @param  layer Layer to add to this <code>Renderer</code>. This method call
     *         will be ignored if <code>layer</code> has already been added to this
     *         <code>Renderer</code>.
     * @throws IllegalArgumentException If <code>layer</code> has already been added
     *         to an other <code>Renderer</code>.
     *
     * @see #removeLayer
     * @see #removeAllLayers
     * @see #getLayers
     * @see #getLayerCount
     */
    public synchronized void addLayer(final RenderedLayer layer) throws IllegalArgumentException {
        synchronized (layer.getTreeLock()) {
            if (layer.renderer == this) {
                return;
            }
            if (layer.renderer != null) {
                throw new IllegalArgumentException(
                            Resources.format(ResourceKeys.ERROR_RENDERER_NOT_OWNER_$1, layer));
            }
            layer.renderer = this;
            /*
             * Ajoute la nouvelle couche dans le tableau {@link #layers}. Le tableau
             * sera agrandit si nécessaire et on déclarera qu'il a besoin d'être reclassé.
             */
            if (layers == null) {
                layers = new RenderedLayer[16];
            }
            if (layerCount >= layers.length) {
                layers = (RenderedLayer[]) XArray.resize(layers, Math.max(layerCount,8) << 1);
            }
            layers[layerCount++] = layer;
            layerSorted = false;
            layer.setVisible(true);
            changePreferredArea(null, layer.getPreferredArea(), layer.getCoordinateSystem(),
                                "Renderer", "addLayer");
            layer.addPropertyChangeListener(listenerProxy);
        }
        updateListenerRegistration();
        repaint(); // Must be invoked last
        listeners.firePropertyChange("layers", (layerCount==1) ? EMPTY : null, null);
    }

    /**
     * Remove a layer from this renderer. Note that if the layer is going to
     * be added back to the same renderer later, then it is more efficient to invoke
     * <code>{@link RenderedLayer#setVisible RenderedLayer.setVisible}(false)</code>.
     *
     * @param  layer The layer to remove. This method call will be ignored
     *         if <code>layer</code> has already been removed from this
     *         <code>Renderer</code>.
     * @throws IllegalArgumentException If <code>layer</code> is owned by
     *         an other <code>Renderer</code> than <code>this</code>.
     *
     * @see #addLayer
     * @see #removeAllLayers
     * @see #getLayers
     * @see #getLayerCount
     */
    public synchronized void removeLayer(final RenderedLayer layer) throws IllegalArgumentException
    {
        if (layer.renderer == null) {
            return;
        }
        if (layer.renderer != this) {
            throw new IllegalArgumentException(
                        Resources.format(ResourceKeys.ERROR_RENDERER_NOT_OWNER_$1, layer));
        }
        repaint(); // Must be invoked first
        layer.removePropertyChangeListener(listenerProxy);
        final CoordinateSystem layerCS = layer.getCoordinateSystem();
        final Rectangle2D    layerArea = layer.getPreferredArea();
        layer.setVisible(false);
        layer.clearCache();
        layer.renderer = null;
        /*
         * Retire cette couche de la liste {@link #layers}. On recherchera
         * toutes les occurences de cette couche, même si en principe elle ne
         * devrait apparaître qu'une et une seule fois.
         */
        for (int i=layerCount; --i>=0;) {
            final RenderedLayer scan = layers[i];
            if (scan == layer) {
                System.arraycopy(layers, i+1, layers, i, (--layerCount)-i);
                layers[layerCount] = null;
            }
        }
        changePreferredArea(layerArea, null, layerCS, "Renderer", "removeLayer");
        updateListenerRegistration();
        listeners.firePropertyChange("layers", null, (layerCount!=0) ? null : EMPTY);
    }

    /**
     * Remove all layers from this renderer.
     *
     * @see #addLayer
     * @see #removeLayer
     * @see #getLayers
     * @see #getLayerCount
     */
    public synchronized void removeAllLayers() {
        repaint(); // Must be invoked first
        while (--layerCount>=0) {
            final RenderedLayer layer = layers[layerCount];
            layer.removePropertyChangeListener(listenerProxy);
            layer.setVisible(false);
            layer.clearCache();
            layer.renderer = null;
            layers[layerCount] = null;
        }
        layerCount = 0;
        setPreferredArea(null);
        clearCache();
        listeners.firePropertyChange("layers", null, EMPTY);
    }

    /**
     * Returns all registered layers. The returned array is sorted in increasing
     * {@linkplain RenderedLayer#getZOrder z-order}: element at index 0 contains
     * the first layer to be drawn.
     *
     * @return The sorted array of layers. May have a 0 length, but will never
     *         be <code>null</code>. Change to this array, will not affect this
     *         <code>Renderer</code>.
     *
     * @see #addLayer
     * @see #removeLayer
     * @see #removeAllLayers
     * @see #getLayerCount
     */
    public synchronized RenderedLayer[] getLayers() {
        sortLayers();
        if (layers != null) {
            final RenderedLayer[] array = new RenderedLayer[layerCount];
            System.arraycopy(layers, 0, array, 0, layerCount);
            return array;
        } else {
            return EMPTY;
        }
    }

    /**
     * Returns the number of layers in this renderer.
     *
     * @see #getLayers
     * @see #addLayer
     * @see #removeLayer
     * @see #removeAllLayers
     */
    public int getLayerCount() {
        return layerCount;
    }

    /**
     * Procède au classement immédiat des couches, si ce n'était pas déjà fait.
     */
    private void sortLayers() {
        assert Thread.holdsLock(this);
        if (!layerSorted && layers!=null) {
            layers = (RenderedLayer[]) XArray.resize(layers, layerCount);
            Arrays.sort(layers, COMPARATOR);
            layerSorted = true;
        }
    }

    //////////////////////////////////////////////////////////////////
    ////////                                                  ////////
    ////////                    Rendering                     ////////
    ////////                                                  ////////
    //////////////////////////////////////////////////////////////////
    /**
     * Returns a rendering hint.
     *
     * @param  key The hint key (e.g. {@link Hints#FINEST_RESOLUTION}).
     * @return The hint value for the specified key, or <code>null</code> if there is no
     *         hint for the specified key.
     *
     * @see Hints#FINEST_RESOLUTION
     * @see Hints#REQUIRED_RESOLUTION
     * @see Hints#COORDINATE_TRANSFORMATION_FACTORY
     * @see Hints#PREFETCH
     * @see RenderingHints#KEY_RENDERING
     * @see RenderingHints#KEY_COLOR_RENDERING
     * @see RenderingHints#KEY_INTERPOLATION
     */
    public synchronized Object getRenderingHint(final RenderingHints.Key key) {
        return hints.get(key);
    }

    /**
     * Add a rendering hint. Hints provides optional information used by some rendering code.
     *
     * @param key   The hint key (e.g. {@link Hints#FINEST_RESOLUTION}).
     * @param value The hint value. A <code>null</code> value remove the hint.
     *
     * @see Hints#FINEST_RESOLUTION
     * @see Hints#REQUIRED_RESOLUTION
     * @see Hints#COORDINATE_TRANSFORMATION_FACTORY
     * @see Hints#PREFETCH
     * @see RenderingHints#KEY_RENDERING
     * @see RenderingHints#KEY_COLOR_RENDERING
     * @see RenderingHints#KEY_INTERPOLATION
     */
    public synchronized void setRenderingHint(final RenderingHints.Key key, final Object value) {
        if (value != null) {
            hints.put(key, value);
        } else {
            hints.remove(key);
        }
        if (Hints.FINEST_RESOLUTION.equals(key)) {
            if (value != null) {
                minResolution = ((Number) hints.get(key)).floatValue();
                if (minResolution >= 0) {
                    if (minResolution > maxResolution) {
                        maxResolution = minResolution;
                    }
                    return;
                }
            }
            minResolution = 0;
            return;
        }
        if (Hints.REQUIRED_RESOLUTION.equals(key)) {
            if (value != null) {
                maxResolution = ((Number) hints.get(key)).floatValue();
                if (maxResolution >= 0) {
                    if (maxResolution < minResolution) {
                        minResolution = maxResolution;
                    }
                    return;
                }
            }
            maxResolution = minResolution;
            return;
        }
        if (Hints.PREFETCH.equals(key)) {
            prefetch = (value!=null) && ((Boolean) value).booleanValue();
            return;
        }
        if (Hints.COORDINATE_TRANSFORMATION_FACTORY.equals(key)) {
            factory = (value!=null) ? (CoordinateTransformationFactory) value :
                                       CoordinateTransformationFactory.getDefault();
            clearCache();
            return;
        }
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
     * Construct a transform between two coordinate systems. If a {@link
     * Hints#COORDINATE_TRANSFORMATION_FACTORY} has been provided, the
     * specified {@link CoordinateTransformationFactory} will be used.
     *
     * @param  sourceCS The source coordinate system.
     * @param  targetCS The target coordinate system.
     * @param  sourceClassName  The caller's class name, for logging purpose.
     * @param  sourceMethodName The caller's method name, for logging purpose.
     * @return A transformation from <code>sourceCS</code> to <code>targetCS</code>.
     * @throws CannotCreateTransformException if the transformation can't be created.
     *
     * @see #getRenderingHint
     * @see #setRenderingHint
     * @see Hints#COORDINATE_TRANSFORMATION_FACTORY
     */
    final synchronized MathTransform getMathTransform(final CoordinateSystem sourceCS,
                                                      final CoordinateSystem targetCS,
                                                      final String sourceClassName,
                                                      final String sourceMethodName)
            throws CannotCreateTransformException
    {
        if (sourceCS == targetCS) {
            // Fast check for a very common case. We will use the more
            // general (but slower) 'equals(..., false)' version later.
            return MathTransform2D.IDENTITY;
        }
        MathTransform tr;
        /*
         * Check if the math transform is available in the cache. A majority of transformations
         * will be from 'layerCS' to 'mapCS' to 'textCS'.  The cache looks for the 'layerCS' to
         * to 'mapCS' transform.
         */
        final boolean cachedTransform = targetCS.equals(context.mapCS, false);
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
            record.setSourceClassName (sourceClassName);
            record.setSourceMethodName(sourceMethodName);
            LOGGER.log(record);
        }
        tr = factory.createFromCoordinateSystems(sourceCS, targetCS).getMathTransform();
        if (cachedTransform) {
            transforms.put(sourceCS, tr);
        }
        return tr;
    }

    /**
     * Returns a fitted coordinate system for {@link RenderingContext#textCS} and
     * {@link RenderingContext#deviceCS}.
     *
     * @param  name     The coordinate system name (e.g. "text" or "device").
     * @param  base     The base coordinate system (e.g. {@link RenderingContext#mapCS}).
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
     * Paint this <code>Renderer</code> and all visible layers it contains.
     * This method invokes {@link RenderedLayer#paint} for each layer.
     *
     * @param graph  The graphics handler.
     * @param zoom   The zoom (usually provided by {@link org.geotools.gui.swing.ZoomPane#zoom}.
     * @param widget The bounds of drawing area (usually provided by
     *               {@link org.geotools.gui.swing.ZoomPane#getZoomableBounds}).
     */
    public synchronized void paint(final Graphics2D      graph,
                                   final AffineTransform zoom,
                                   final Rectangle       bounds)
    {
        statistics.init();
        sortLayers();
        RenderingContext       context = this.context;
        final RenderedLayer[]   layers = this.layers;
        final GraphicsJAI     graphics = GraphicsJAI.createGraphicsJAI(graph, mapPane);
        final Rectangle     clipBounds = graphics.getClipBounds();
        final AffineTransform toDevice = graphics.getTransform();
        final boolean         toScreen = toDevice.isIdentity();
        final boolean         sameZoom = mapToText.equals(zoom);
        /*
         * If the zoom has changed, send a notification to all layers before
         * to start the rendering.  Layers will update their cache, which is
         * used in order to decide if a layer need to be repainted or not.
         */
        if (!sameZoom) try {
            final AffineTransform change = mapToText.createInverse();
            change.preConcatenate(zoom);
            if (true) {
                // Scale slightly the zoom in order to avoid rounding errors in Area.
                final double centerX = bounds.getCenterX();
                final double centerY = bounds.getCenterY();
                change.translate( centerX,  centerY);
                change.scale(1+EPS, 1+EPS);
                change.translate(-centerX, -centerY);
            }
            zoomChanged(change);
        } catch (java.awt.geom.NoninvertibleTransformException exception) {
            // Should not happen. If it happen anyway, declare that everything must be
            // repainted. It will be slower, but will not prevent the renderer to work.
            Utilities.unexpectedException("org.geotools.renderer.j2d",
                                          "Renderer", "paint", exception);
            zoomChanged(null);
        }
        /*
         * If the zoom or the device changed, then the 'textCS' and 'deviceCS' must
         * be recreated.
         */
        if (!sameZoom || !toScreen) try {
            final CoordinateSystem mapCS, textCS, deviceCS;
            mapCS    = context.mapCS;
            textCS   = createFittedCoordinateSystem("textCS",    mapCS, zoom);
            deviceCS = createFittedCoordinateSystem("deviceCS", textCS, toDevice);
            context  = new RenderingContext(this, mapCS, textCS, deviceCS);
            if (toScreen) {
                mapToText.setTransform(zoom);
                this.context = context;
            }
            if (prefetch) {
                // Prepare data in separated threads.
                prefetch(bounds, context.deviceCS);
            }
        } catch (TransformException exception) {
            // Impossible to process to the rendering. Paint the stack
            // trace right into the component and exit from this method.
            GraphicsUtilities.paintStackTrace(graphics, bounds, exception);
            return;
        }
        /*
         * Dessine les couches en commençant par
         * celles qui ont un <var>z</var> le plus bas.
         */
        graphics.transform(zoom);
        graphics.addRenderingHints(hints);
        context.init(graphics, bounds);
        try {
            for (int i=0; i<layerCount; i++) {
                try {
                    layers[i].update(context, clipBounds);
                } catch (TransformException exception) {
                    handleException("RenderedLayer", "paintComponent", exception);
                } catch (RuntimeException exception) {
                    Utilities.unexpectedException("org.geotools.renderer.j2d",
                                                  "RenderedLayer", "paint", exception);
                }
            }
        } finally {
            context.init(null, null);
        }
        /*
         * If this map took a long time to renderer, log a message.
         */
        statistics.finish(this, Level.FINE);
    }

    /**
     * Declare that the {@link Component} need to be repainted.
     * This method can be invoked from any thread (it doesn't
     * need to be the <cite>Swing</cite> thread).
     */
    final void repaint() {
        final Component mapPane = this.mapPane;
        if (mapPane != null) {
            if (!EventQueue.isDispatchThread()) {
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        repaint();
                    }
                });
                return;
            }
            mapPane.repaint();
        }
    }

    /**
     * Méthode appelée lorsqu'une exception {@link TransformException} non-gérée
     * s'est produite. Cette méthode peut être appelée pendant le traçage de la carte
     * où les mouvements de la souris. Elle ne devrait donc pas utiliser une boîte de
     * dialogue pour reporter l'erreur, et retourner rapidement pour éviter de bloquer
     * la queue des événements de <i>Swing</i>.
     *
     * @param  sourceClassName  The caller's class name, for logging purpose.
     * @param  sourceMethodName The caller's method name, for logging purpose.
     * @param  exception        The transform exception.
     */
    static void handleException(final String className,
                                final String methodName,
                                final TransformException exception)
    {
        Utilities.unexpectedException("org.geotools.renderer.j2d", className, methodName, exception);
    }



    //////////////////////////////////////////////////////////////////
    ////////                                                  ////////
    ////////                     Events                       ////////
    ////////                                                  ////////
    //////////////////////////////////////////////////////////////////
    /**
     * Register {@link #listenerProxy} if at least one layer has a tool, or unregister
     * it if no layer has tools. This method is automatically invoked when the "tools"
     * property change in a {@link RenderedLayer}.
     */
    private void updateListenerRegistration() {
        assert Thread.holdsLock(this);
        if (mapPane != null) {
            boolean hasTools = (tools != null);
            if (!hasTools) {
                for (int i=layerCount; --i>=0;) {
                    if (layers[i].getTools() != null) {
                        hasTools = true;
                        break;
                    }
                }
            }
            if (hasTools != listenerRegistered) {
                /*
                 * Since we are about to change Component registration,
                 * make sure we are running in the AWT thread.
                 */
                if (!EventQueue.isDispatchThread()) {
                    EventQueue.invokeLater(new Runnable() {
                        public void run() {
                            synchronized (Renderer.this) {
                                updateListenerRegistration();
                            }
                        }
                    });
                    return;
                }
                /*
                 * Before to register any listener, unregister unconditionnaly in order
                 * to make sure we don't register the same listener twice.  It is safer
                 * to do this because {@link javax.swing.event.EventListenerList} doesn't
                 * do this check. It is not damageable to unregister twice, but it is
                 * damageable to register twice.
                 */
                mapPane.removeMouseListener(listenerProxy);
                if (hasTools) {
                    mapPane.addMouseListener(listenerProxy);
                }
                /*
                 * Special processing for Swing's tool tip text. If there is no default
                 * tool tip, then we need to register the component manually if we want
                 * 'Tools.getToolTipText()' to work, since Swing doesn't know about it.
                 */
                if (mapPane instanceof JComponent) {
                    final JComponent swing = (JComponent) mapPane;
                    if (hasTools && swing.getToolTipText()==null) {
                        ToolTipManager.sharedInstance().registerComponent(swing);
                    }
                    // Store an information usefull to org.geotools.gui.swing.MapPane
                    swing.putClientProperty("RendererHasTools", Boolean.valueOf(hasTools));
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
     * @see JComponent#getDefaultLocale
     * @see Locale#getDefault
     */
    public Locale getLocale() {
        if (mapPane != null) try {
            return mapPane.getLocale();
        } catch (IllegalComponentStateException exception) {
            // Not yet added to a containment hierarchy. Ignore...
            if (mapPane instanceof JComponent) {
                return JComponent.getDefaultLocale();
            }
        }
        return Locale.getDefault();
    }

    /**
     * Returns the default tools to use when no {@linkplain RenderedLayer#getTools layer's tools}
     * can do the job. If no default tools has been set, then returns <code>null</code>.
     *
     * @see Tools#getToolTipText
     * @see Tools#getPopupMenu
     * @see Tools#mouseClicked
     */
    public Tools getTools() {
        return tools;
    }

    /**
     * Set the default tools to use when no {@linkplain RenderedLayer#getTools layer's tools}
     * can do the job.
     *
     * @param tools The new tools, or <code>null</code> for removing any set of tools.
     */
    public void setTools(final Tools tools) {
        final Tools oldTools;
        synchronized (this) {
            oldTools = this.tools;
            this.tools = tools;
        }
        listeners.firePropertyChange("tools", oldTools, tools);
    }

    /**
     * Returns the string to be used as the tooltip for a given mouse event. This method
     * invokes {@link Tools#getToolTipText} for some registered {@linkplain RenderedLayer
     * layers} in decreasing {@linkplain RenderedLayer#getZOrder z-order} until one is
     * found to returns a non-null string.
     *
     * @param  event The mouse event.
     * @return The tool tip text, or <code>null</code> if there is no tool tip for this location.
     */
    public synchronized String getToolTipText(final GeoMouseEvent event) {
        sortLayers();
        final int x = event.getX();
        final int y = event.getY();
        final RenderedLayer[] layers = this.layers;
        for (int i=layerCount; --i>=0;) {
            final RenderedLayer layer = layers[i];
            final Tools tools = layer.getTools();
            if (tools!=null && layer.contains(x,y)) {
                final String tooltip = tools.getToolTipText(event);
                if (tooltip != null) {
                    return tooltip;
                }
            }
        }
        return (tools!=null) ? tools.getToolTipText(event) : null;
    }

    /**
     * Format a value for the current mouse position. This method invokes
     * {@link Tools#formatValue} for some registered {@linkplain RenderedLayer
     * layers} in decreasing {@linkplain RenderedLayer#getZOrder z-order} until
     * one is found to returns <code>true</code>.
     *
     * @param  event The mouse event.
     * @param  toAppendTo The destination buffer for formatting a value.
     * @return <code>true</code> if this method has formatted a value,
     *         or <code>false</code> otherwise.
     *
     * @see Tools#formatValue
     * @see MouseCoordinateFormat#format(GeoMouseEvent)
     */
    final synchronized boolean formatValue(final GeoMouseEvent event,
                                           final StringBuffer toAppendTo)
    {
        sortLayers();
        final int x = event.getX();
        final int y = event.getY();
        final RenderedLayer[] layers = this.layers;
        for (int i=layerCount; --i>=0;) {
            final RenderedLayer layer = layers[i];
            final Tools tools = layer.getTools();
            if (tools!=null && layer.contains(x,y)) {
                if (tools.formatValue(event, toAppendTo)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns the popup menu to appears for a given mouse event. This method invokes
     * {@link Tools#getPopupMenu} for some registered {@linkplain RenderedLayer layers}
     * in decreasing {@linkplain RenderedLayer#getZOrder z-order} until one is found to
     * returns a non-null menu.
     *
     * @param  event The mouse event.
     * @return Actions for the popup menu, or <code>null</code> if there is none.
     *         If the returned array is non-null but contains null elements,
     *         then the null elements will be understood as menu separator.
     */
    public synchronized Action[] getPopupMenu(final GeoMouseEvent event) {
        sortLayers();
        final int x = event.getX();
        final int y = event.getY();
        final RenderedLayer[] layers = this.layers;
        for (int i=layerCount; --i>=0;) {
            final RenderedLayer layer = layers[i];
            final Tools tools = layer.getTools();
            if (tools!=null && layer.contains(x,y)) {
                final Action[] menu = tools.getPopupMenu(event);
                if (menu != null) {
                    return menu;
                }
            }
        }
        return (tools!=null) ? tools.getPopupMenu(event) : null;
    }

    /**
     * Invoked when user clicks on this <code>Renderer</code>. The default implementation
     * invokes {@link Tools#mouseClicked} for some {@linkplain RenderedLayer layers} in
     * decreasing {@linkplain RenderedLayer#getZOrder z-order} until one of them
     * {@linkplain MouseEvent#consume consume} the event.
     *
     * @param  event The mouse event.
     */
    private synchronized void mouseClicked(final GeoMouseEvent event) {
        sortLayers();
        final int x = event.getX();
        final int y = event.getY();
        final RenderedLayer[] layers = this.layers;
        for (int i=layerCount; --i>=0;) {
            final RenderedLayer layer = layers[i];
            final Tools tools = layer.getTools();
            if (tools!=null && layer.contains(x,y)) {
                tools.mouseClicked(event);
                if (event.isConsumed()) {
                    return;
                }
            }
        }
        if (tools != null) {
            tools.mouseClicked(event);
        }
    }

    /**
     * Méthode appelée automatiquement chaque fois que le zoom a changé.
     * Cette méthode met à jour les coordonnées des formes géométriques
     * déclarées dans les objets {@link RenderedLayer}.
     *
     * @param change The zoom <strong>change</strong> in <strong>device</strong> coordinate
     *        system, or <code>null</code> if unknow. If <code>null</code>, then all layers
     *        will be fully redrawn during the next rendering.
     */
    private void zoomChanged(AffineTransform change) {
        if (change!=null && change.isIdentity()) {
            return;
        }
        assert Thread.holdsLock(this);
        for (int i=layerCount; --i>=0;) {
            layers[i].zoomChanged(change);
        }
    }

    /**
     * Add a property change listener to the listener list. The listener is
     * registered for all properties. For example, adding or removing layers
     * may fire a <code>"preferredArea"</code> change events.
     *
     * @param listener The property change listener to be added
     */
    public void addPropertyChangeListener(final PropertyChangeListener listener) {
        listeners.addPropertyChangeListener(listener);
    }

    /**
     * Remove a property change listener from the listener list.
     * This removes a <code>PropertyChangeListener</code> that
     * was registered for all properties.
     *
     * @param listener The property change listener to be removed
     */
    public void removePropertyChangeListener(final PropertyChangeListener listener) {
        listeners.removePropertyChangeListener(listener);
    }

    /**
     * Hints that the given area might be painted in the near future. Some layers
     * may spawn a thread to compute the data while others may ignore the hint.
     *
     * @param The area (in the <code>cs</code> coordinate system) that may need to
     *        be painted. A <code>null</code> value means that all layers will need
     *        to be fully painted soon.
     * @param cs The coordinate system for <code>area</code>.
     *
     * @see RenderedLayer#prefetch
     * @see PlanarImage#prefetchTiles
     */
    private void prefetch(Rectangle2D area, final CoordinateSystem cs) {
        assert Thread.holdsLock(this);
        Rectangle2D buffer = null;
        for (int i=layerCount; --i>=0;) {
            final RenderedLayer layer = layers[i];
            Rectangle2D layerArea = area;
            try {
                if (area != null) {
                    // Note: the 'getMathTransform(...)' method is faster when the targetCS is
                    //       'context.mapCS'.  This is why we invoke 'MathTransform.inverse()'
                    //       instead of swapping 'sourceCS' and 'targetCS' arguments.
                    final MathTransform2D transform = (MathTransform2D)
                                               getMathTransform(layer.getCoordinateSystem(), cs,
                                                                "Renderer", "prefetch").inverse();
                    if (!transform.isIdentity()) {
                        layerArea = buffer = CTSUtilities.transform(transform, area, buffer);
                    }
                }
                layer.prefetch(layerArea);
            } catch (TransformException exception) {
                // Can't transform the area. This is not a big deal, since
                // 'prefetch' is nothing more than a hint. Continue the loop...
                handleException("Renderer", "prefetch", exception);
            }
        }
    }

    /**
     * Efface les données qui avaient été conservées dans une cache interne. L'appel
     * de cette méthode permettra de libérer un peu de mémoire à d'autres fins. Elle
     * devrait être appelée lorsque l'on sait qu'on n'affichera plus la carte avant
     * un certain temps. Par exemple la méthode {@link java.applet.Applet#stop}
     * devrait appeller <code>clearCache()</code>. Notez que l'appel de cette méthode
     * ne modifie aucunement le paramétrage de la carte. Seulement, son prochain
     * traçage sera plus lent, le temps que <code>Renderer</code> reconstruise les
     * caches internes.
     */
    private synchronized void clearCache() {
        for (int i=layerCount; --i>=0;) {
            layers[i].clearCache();
        }
        transforms.clear();
    }

    /**
     * Préviens que cet afficheur sera bientôt détruit. Cette méthode peut être appelée lorsque
     * cet objet <code>Renderer</code> est sur le point de ne plus être référencé.  Elle permet
     * de libérer des ressources plus rapidement que si l'on attend que le ramasse-miettes fasse
     * son travail. Après l'appel de cette méthode, on ne doit plus utiliser ni cet objet
     * <code>Renderer</code> ni aucune des couches <code>RenderedLayer</code> qu'il contenait.
     */
    public synchronized void dispose() {
        final RenderedLayer[] layers = new RenderedLayer[layerCount];
        System.arraycopy(this.layers, 0, layers, 0, layerCount);
        removeAllLayers();
        for (int i=layerCount; --i>=0;) {
            layers[i].dispose();
        }
        clearCache();
        final PropertyChangeListener[] list = listeners.getPropertyChangeListeners();
        for (int i=list.length; --i>=0;) {
            listeners.removePropertyChangeListener(list[i]);
        }
    }
}
