/*
 * Geotools - OpenSource mapping toolkit
 * (C) 2003, Centre for Computational Geography
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

// Geometric shapes
import java.awt.Shape;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Dimension2D;
import java.awt.geom.AffineTransform;
import javax.swing.JComponent;

// User interface and Java2D rendering
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;

// Miscellaneous J2SE
import java.util.Locale;
import java.util.EventListener;
import java.util.logging.Level;
import java.util.logging.LogRecord;

// Geotools dependencies
import org.geotools.cs.CoordinateSystem;
import org.geotools.ct.TransformException;
import org.geotools.cs.GeographicCoordinateSystem;
import org.geotools.resources.Utilities;
import org.geotools.resources.CTSUtilities;
import org.geotools.resources.XAffineTransform;
import org.geotools.resources.renderer.Resources;
import org.geotools.resources.renderer.ResourceKeys;


/**
 * Base class for layers to be rendered using the Java2D renderer. Each layer can use
 * its own {@linkplain CoordinateSystem coordinate system} (CS) for its underlying data.
 * Transformations to the {@link RendereringContext#mapCS rendering coordinate system}
 * are performed on the fly at rendering time.
 *
 * @version $Id: RenderedLayer.java,v 1.2 2003/01/23 23:26:22 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public abstract class RenderedLayer {
    /**
     * Minimum amout of milliseconds during rendering before logging a message.
     * A message will be logged only if rendering take longer. This is used for
     * tracking down performance bottleneck.
     */
    private static final int TIME_THRESHOLD = 200;

    /**
     * The renderer that own this layer, or <code>null</code>
     * if this layer has not yet been added to a renderer.
     */
    transient Renderer renderer;

    /**
     * Forme géométrique englobant la région dans laquelle la couche a été dessinée lors du
     * dernier appel de la méthode {@link #paint}.  Les coordonnées de cette région doivent
     * être en exprimées en coordonnées du périphérique ({@link RenderingContext#deviceCS}).
     * La valeur <code>null</code> signifie qu'on peut considérer que cette couche occupe la
     * totalité de la surface dessinable.
     */
    private transient Shape paintedArea;

    /**
     * Système de coordonnées utilisé pour cette couche. Les méthodes {@link #getPreferredArea}
     * et {@link #setPreferredArea} utilisent ce système de coordonnées. Ce champ ne doit jamais
     * être nul.
     */
    private CoordinateSystem coordinateSystem = GeographicCoordinateSystem.WGS84;

    /**
     * Coordonnées géographiques couvertes par cette couche. Ces coordonnées doivent
     * être expriméees selon le système de coordonnées <code>coordinateSystem</code>.
     * Une valeur nulle signifie que cette couche n'a pas de limites bien délimitées.
     *
     * @see #getPreferredArea
     * @see #setPreferredArea
     */
    private Rectangle2D preferredArea;

    /**
     * Dimension préférée des pixels pour un zoom rapproché. Une valeur
     * nulle signifie qu'aucune dimension préférée n'a été spécifiée.
     *
     * @see #getPreferredPixelSize
     * @see #setPreferredPixelSize
     */
    private Dimension2D preferredPixelSize;

    /**
     * Largeur par défaut des lignes à tracer. La valeur <code>null</code>
     * signifie que cette largeur doit être recalculée. Cette largeur sera
     * déterminée à partir de la valeur de {@link #preferredPixelSize}.
     */
    private transient Stroke stroke;

    /**
     * Indique si cette couche est visible. Les couches sont invisibles par défaut. L'appel
     * de {@link Renderer#addLayer} appelera systématiquement <code>setVisible(true)</code>.
     *
     * @see #setVisible
     */
    private boolean visible;

    /**
     * Ordre <var>z</var> à laquelle cette couche doit être dessinée. Les couches avec un
     * <var>z</var> élevé seront dessinées par dessus les couches avec un <var>z</var> bas.
     * Typiquement, cet ordre <var>z</var> devrait être l'altitude en mètres de la couche
     * (par exemple -30 pour l'isobath à 30 mètres de profondeur). La valeur
     * {@link Float#POSITIVE_INFINITY} fait dessiner une couche par dessus tout le reste,
     * tandis que la valeur {@link Float#NEGATIVE_INFINITY} la fait dessiner en dessous.
     * La valeur {@link Float#NaN} n'est pas valide. La valeur par défaut est
     * {@link Float#POSITIVE_INFINITY}.
     *
     * @see #getZOrder
     * @see #setZOrder
     */
    private float zOrder = Float.POSITIVE_INFINITY;

    /**
     * The tools for this layer, or <code>null</code> if none.
     *
     * @see #getTools
     * @see #setTools
     */
    private Tools tools;

    /**
     * Listeners to be notified about any changes in this layer's properties.
     * Examples of properties that may change:
     * <code>"preferredArea"</code>,
     * <code>"preferredPixelSize"</code>,
     * <code>"zOrder"</code>,
     * <code>"visible"</code> and
     * <code>"tools"</code>.
     */
    protected final PropertyChangeSupport listeners;

    /**
     * Construct a new rendered layer. The {@linkplain #getCoordinateSystem coordinate system}
     * default to {@linkplain GeographicCoordinateSystem#WGS84 WGS 1984} and the {@linkplain
     * #getZOrder z-order} default to positive infinity (i.e. this layer is drawn on top of
     * everything else). Subclasses should invokes <code>setXXX</code> methods in order to
     * define properly this layer's properties.
     *
     * @see #setZOrder
     * @see #setCoordinateSystem
     * @see #setPreferredArea
     * @see #setPreferredPixelSize
     */
    public RenderedLayer() {
        listeners = new PropertyChangeSupport(this);
    }

    /**
     * Returns the lock for synchronisation.
     */
    private Object getTreeLock() {
        return (renderer!=null) ? (Object)renderer : (Object)this;
    }

    /**
     * Returns this layer's name. Default implementation returns class name and its z-order.
     *
     * @param  locale The desired locale, or <code>null</code> for a default locale.
     * @return This layer's name.
     *
     * @see Renderer#getLocale
     * @see Component#getLocale
     */
    public String getName(final Locale locale) {
        return Utilities.getShortClassName(this) + '[' + getZOrder() + ']';
    }

    /**
     * Returns to locale for this layer. The renderer will inherit
     * the locale of its {@link Renderer}, if he have one. Otherwise,
     * a default locale will be returned.
     *
     * @see Renderer#getLocale
     * @see Component#getLocale
     */
    public Locale getLocale() {
        final Renderer renderer = this.renderer;
        return (renderer!=null) ? renderer.getLocale() : JComponent.getDefaultLocale();
    }

    /**
     * Returns the two-dimensional coordinate system for the underlying data.  This
     * coordinate system is used by most methods like {@link #getPreferredArea} and
     * {@link #getPreferredPixelSize}.
     *
     * @see #setCoordinateSystem
     * @see #getPreferredArea
     * @see #getPreferredPixelSize
     * @see RenderingContext#mapCS
     */
    public final CoordinateSystem getCoordinateSystem() {
        return coordinateSystem;
    }

    /**
     * Set the coordinate system for the underlying data. This method is usually invoked
     * only once, at construction time.  If the specified coordinate system has more than
     * two dimensions, then it must be a {@link CompoundCoordinateSystem} with a two
     * dimensional {@link CompoundCoordinateSystem#getHeadCS headCS}.
     *
     * @param  cs The coordinate system.
     * @throws TransformException If <code>cs</code> can't be reduced to a two-dimensional
     *         coordinate system., or if data can't be transformed for some other reason.
     */
    protected void setCoordinateSystem(final CoordinateSystem cs) throws TransformException {
        final CoordinateSystem oldCS;
        synchronized (getTreeLock()) {
            oldCS = coordinateSystem;
            coordinateSystem = CTSUtilities.getCoordinateSystem2D(cs);
        }
        listeners.firePropertyChange("coordinateSystem", oldCS, cs);
    }

    /**
     * Retourne les coordonnées géographiques de cette couche. Les coordonnées retournées ne sont
     * pas obligées d'englober toute la couche (quoique ce soit souvent le cas).  Elles indiquent
     * plutôt la partie de la couche que l'on souhaite voir apparaître dans le zoom par défaut.
     * Le rectangle retourné sera exprimé selon le système de coordonnées retourné par
     * {@link #getCoordinateSystem}. Si cette couche n'a pas de limites géographiques bien
     * définies (par exemple si elle n'est qu'une légende ou l'échelle de la carte), alors
     * cette méthode peut retourner <code>null</code>.
     *
     * @see #setPreferredArea
     * @see #getPreferredPixelSize
     * @see #getCoordinateSystem
     */
    public final Rectangle2D getPreferredArea() {
        final Rectangle2D preferredArea = this.preferredArea;
        return (preferredArea!=null) ? (Rectangle2D) preferredArea.clone() : null;
    }

    /**
     * Modifie les coordonnées géographiques de cette couche. L'appel de cette méthode ne modifie
     * par le géoréférencement; elle affecte simplement la région qui sera affichée par défaut
     * dans une fenêtre.
     *
     * @see #getPreferredArea
     * @see #setPreferredPixelSize
     * @see #getCoordinateSystem
     */
    public void setPreferredArea(final Rectangle2D area) {
        final Rectangle2D oldArea;
        synchronized (getTreeLock()) {
            paintedArea = null;
            oldArea = preferredArea;
            preferredArea = (area!=null) ? (Rectangle2D)area.clone() : null;
        }
        listeners.firePropertyChange("preferredArea", oldArea, area);
    }

    /**
     * Returns the preferred pixel size for a close
     * zoom, or <code>null</code> if there is none.
     *
     * @see #setPreferredPixelSize
     * @see #getPreferredArea
     * @see #getCoordinateSystem
     */
    public final Dimension2D getPreferredPixelSize() {
        final Dimension2D preferredPixelSize = this.preferredPixelSize;
        return (preferredPixelSize!=null) ? (Dimension2D) preferredPixelSize.clone() : null;
    }

    /**
     * Set the preferred pixel size for a close zoom. For images, the preferred pixel
     * size is the image's pixel size (in units of {@link #getCoordinateSystem}). For
     * other kind of object, this "pixel" size should be some raisonable resolution
     * for the underlying data. For example a layer drawing an isoline may use the
     * isoline's mean resolution.
     *
     * @param size The preferred pixel size, or <code>null</code> if there is none.
     *
     * @see #getPreferredPixelSize
     * @see #setPreferredArea
     * @see #getCoordinateSystem
     */
    public void setPreferredPixelSize(final Dimension2D size) {
        final Dimension2D oldSize;
        synchronized (getTreeLock()) {
            stroke = null;
            oldSize = preferredPixelSize;
            preferredPixelSize = (size!=null) ? (Dimension2D)size.clone() : null;
        }
        listeners.firePropertyChange("preferredPixelSize", oldSize, size);
    }

    /**
     * Retourne l'ordre <var>z</var> à laquelle cette couche devrait être dessinée.
     * Les couches avec un <var>z</var> élevé seront dessinées par dessus celles
     * qui ont un <var>z</var> plus bas. La valeur retournée par défaut est
     * {@link Float#POSITIVE_INFINITY}.
     *
     * @see #setZOrder
     */
    public final float getZOrder() {
        return zOrder;
    }

    /**
     * Modifie l'altitude <var>z</var> à laquelle sera dessinée cette couche. La
     * valeur spécifiée viendra remplacer la valeur par défaut que retournait
     * normalement {@link #getZOrder}.
     *
     * @throws IllegalArgumentException si <code>zorder</code> est {@link Float#NaN}.
     */
    public void setZOrder(final float zOrder) throws IllegalArgumentException {
        if (Float.isNaN(zOrder)) {
            throw new IllegalArgumentException(String.valueOf(zOrder));
        }
        final float oldZOrder;
        synchronized (getTreeLock()) {
            oldZOrder = this.zOrder;
            if (zOrder == oldZOrder) {
                return;
            }
            this.zOrder = zOrder;
            repaint();
        }
        listeners.firePropertyChange("zOrder", new Float(oldZOrder), new Float(zOrder));
    }

    /**
     * Returns the tools for this layer, or <code>null</code> if none.
     * Tools are used for processing mouse and keyboard events. Tools
     * may be changed at anytime, for example according some user selection.
     *
     * @see Tools#getToolTipText
     * @see Tools#getPopupMenu
     * @see Tools#mouseClicked
     */
    public final Tools getTools() {
        return tools;
    }

    /**
     * Set the tools for this layer.
     *
     * @param tools The new tools, or <code>null</code> for removing any set of tools.
     */
    public void setTools(final Tools tools) {
        final Tools oldTools;
        synchronized (getTreeLock()) {
            oldTools = this.tools;
            this.tools = tools;
        }
        listeners.firePropertyChange("tools", oldTools, tools);
    }

    /**
     * Determines whether this layer should be visible when its container is visible.
     *
     * @return <code>true</code> if the layer is visible, <code>false</code> otherwise.
     */
    public final boolean isVisible() {
        return visible;
    }

    /**
     * Spécifie si cette couche doit être visible ou non. Cette méthode peut être
     * appelée pour cacher momentanément une couche. Elle est aussi appelée de
     * façon systématique lorsque cette couche est ajoutée ou retirée d'un
     * {@link Renderer}:
     *
     * <ul>
     *   <li><code>{@link Renderer#addLayer Renderer.addLayer}(this)</code>
     *       appelera <code>setVisible(true)</code>. Les classes dérivées peuvent
     *       profiter de cette spécification pour s'enregistrer auprès de {@link
     *       org.geotools.gui.swing.MapPane} comme étant intéressées à suivre les
     *       mouvements de la souris par exemple.</li>
     *   <li><code>{@link Renderer#remove Renderer.remove}(this)</code>
     *       appelera <code>setVisible(false)</code>. Les classes dérivées peuvent
     *       profiter de cette spécification pour déclarer à
     *       {@link org.geotools.gui.swing.MapPane} qu'elles ne sont plus
     *       intéressées à suivre les mouvements de la souris par exemple.</li>
     * </ul>
     */
    public void setVisible(final boolean visible) {
        synchronized (getTreeLock()) {
            if (visible == this.visible) {
                return;
            }
            this.visible = visible;
            repaint();
        }
        listeners.firePropertyChange("visible", !visible, visible);
    }

    /**
     * Indique que cette couche a besoin d'être redéssinée. La couche ne sera pas redessinée
     * immediatement, mais seulement un peu plus tard. Cette méthode <code>repaint()</code>
     * peut être appelée à partir de n'importe quel thread (pas nécessairement celui de
     * <i>Swing</i>).
     */
    public void repaint() {
        repaint(paintedArea!=null ? paintedArea.getBounds() : null);
    }

    /**
     * Indique qu'une partie de cette couche a besoin d'être redéssinée.
     * Cette méthode peut être appelée à partir de n'importe quel thread
     * (pas nécessairement celui de <i>Swing</i>).
     *
     * @param bounds Coordonnées (en points) de la partie à redessiner.
     */
    final void repaint(final Rectangle bounds) {
        final Renderer renderer = this.renderer;
        if (renderer == null) {
            return;
        }
        final Component mapPane = renderer.mapPane;
        if (mapPane == null) {
            return;
        }
        if (EventQueue.isDispatchThread()) {
            if (bounds == null) {
                mapPane.repaint();
            } else {
                mapPane.repaint(bounds.x, bounds.y, bounds.width, bounds.height);
            }
        } else {
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    repaint(bounds);
                }
            });
        }
    }

    /**
     * Paint this object. This method is invoked by {@link Renderer} every time this layer needs
     * to be repainted. By default, painting is done in the {@linkplain RendereringContext#mapCS
     * rendering coordinate system} (usually "real world" metres).    This method is responsible
     * for transformations from its own  {@linkplain #getCoordinateSystem underlying CS}  to the
     * {@linkplain RendereringContext#mapCS rendering CS} if needed. The {@link RenderingContext}
     * object provides informations for such transformations:
     *
     * <ul>
     * <li><p><code>context.{@link RenderingContext#getMathTransform getMathTransform}(
     *                      {@link #getCoordinateSystem getCoordinateSystem}(),
     *                      context.{@link RenderingContext#mapCS mapCS} )</code><br>
     * Returns a transform from this layer's CS to the rendering CS.</p></li>
     *
     * <li><p><code>context.{@link RenderingContext#getMathTransform getMathTransform}(
     *                      context.{@link RenderingContext#mapCS mapCS},
     *                      context.{@link RenderingContext#textCS textCS} )</code><br>
     * Returns a transform from the rendering CS to the Java2D CS in "dots" units
     * (usually 1/72 of inch). This transformation is zoom dependent.</p></li>
     *
     * <li><p><code>context.{@link RenderingContext#getMathTransform getMathTransform}(
     *                      context.{@link RenderingContext#textCS textCS},
     *                      context.{@link RenderingContext#deviceCS deviceCS} )</code><br>
     * Returns a transform from the Java2D CS to the device CS. This transformation is
     * device dependent, but not zoom sensitive. When the output device is the screen,
     * then this is the identity transform.</p></li>
     *
     * <p>The {@link RenderingContext} object can takes care of configuring {@link Graphics2D}
     * with the right transform for a limited set of particular CS (namely, only CS leading to
     * an {@linkplain AffineTransform affine transform}). This convenient for switching between
     * {@linkplain RenderingContext#mapCS rendering CS} (the one used for drawing map features)
     * and {@linkplain RenderingContext#textCS Java2D CS} (the one used for rendering texts and
     * labels). Example:</p>
     *
     * <blockquote><pre>
     * &nbsp;Shape paint(RenderingContext context) {
     * &nbsp;    Graphics2D graphics = context.graphics;
     * &nbsp;    // <cite>Paint here map features in geographic coordinates (usually m or °)</cite>
     * &nbsp;    context.addPaintedArea(...); // Optional
     * &nbsp;
     * &nbsp;    context.setCoordinateSystem(context.textCS);
     * &nbsp;    // <cite>Write here text or label. Coordinates are in <u>dots</u>.</cite>
     * &nbsp;    context.addPaintedArea(...); // Optional
     * &nbsp;
     * &nbsp;    context.setCoordinateSystem(context.mapCS);
     * &nbsp;    // <cite>Continue here the rendering of map features in geographic coordinates</cite>
     * &nbsp;    context.addPaintedArea(...); // Optional
     * &nbsp;}
     * </pre></blockquote>
     *
     * During the rendering process, implementations are encouraged to declare a (potentially
     * approximative) bounding shape of their painted area with calls to
     * {@link RenderingContext#addPaintedArea(Shape)}. This is an optional operation: providing
     * those hints help {@link Renderer} to speed up future rendering and events processing.
     *
     * @param  context Information relatives to the rendering context. This object ontains the
     *         {@link Graphics2D} to use and methods for getting {@link MathTransform} objects.
     *         This temporary object will be destroy once the rendering is completed. Consequently,
     *         do not keep a reference to it outside this <code>paint</code> method.
     * @throws TransformException If a coordinate transformation failed during the rendering
     *         process.
     */
    protected abstract void paint(final RenderingContext context) throws TransformException;

    /**
     * Paint this layer and update the {@link #paintedArea} field. If this layer is not visible
     * or if <code>clipBounds</code> doesn't intersect {@link #paintedArea}, then this method do
     * nothing.
     *
     * @param context Information relatives to the rendering context. Will be passed
     *        unchanged to {@link #paint}.
     * @param clipBounds The area to paint, in device coordinates
     *        ({@link RenderingContext#deviceCS}).
     */
    final void update(final RenderingContext context,
                                   final Rectangle clipBounds)
            throws TransformException
    {
        if (visible) synchronized (getTreeLock()) {
            if (paintedArea==null || clipBounds==null || paintedArea.intersects(clipBounds)) {
                long time = System.currentTimeMillis();
                context.paintedArea = null;
                paint(context);
                if (context.textCS == context.deviceCS) {
                    /*
                     * Keeps the bounding shape of the rendered area  only if rendering
                     * was performed on the screen or any other device with an identity
                     * default transform. This is usually not the case during printing.
                     */
                    this.paintedArea = context.paintedArea;
                }
                /*
                 * If this layer took a long time to renderer, log a message.
                 */
                time = System.currentTimeMillis()-time;
                if (time > TIME_THRESHOLD) {
                    final LogRecord record = Resources.getResources(null).getLogRecord(Level.FINEST,
                                             ResourceKeys.PAINTING_$2, getName(null),
                                             new Double(time/1000.0));
                    record.setSourceClassName(Utilities.getShortClassName(this));
                    record.setSourceMethodName("paint");
                    Renderer.LOGGER.log(record);
                }
            }
        }
    }

    /**
     * Tells if this layer <strong>may</strong> contains the specified point. This method
     * performs only a fast check. Subclasses will have to perform a more exhautive check
     * in their {@link #mouseClicked}, {@link #getPopupMenu} and similar methods. The
     * coordinate system is the {@link RenderingContext#textCS} used the last time this
     * layer was rendered.
     *
     * @param  x <var>x</var> coordinate.
     * @param  y <var>y</var> coordinate.
     * @return <code>true</code> if this layer is visible and may contains the specified point.
     */
    final boolean contains(final int x, final int y) {
        if (visible) {
            final Shape paintedArea = this.paintedArea;
            return (paintedArea==null) || paintedArea.contains(x,y);
        }
        return false;
    }

    /**
     * Add a property change listener to the listener list. The listener is registered
     * for all properties. For example, methods {@link #setVisible}, {@link #setZOrder},
     * {@link #setPreferredArea}, {@link #setPreferredPixelSize} and {@link #setTools}
     * will fire <code>"visible"</code>, <code>"zOrder"</code>, <code>"preferredArea"</code>
     * <code>"preferredPixelSize"</code> and <code>"tools"</code> change events.
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
     * Invoked every time the {@link Renderer}'s zoom changed. A zoom change require two
     * updates to {@link #paintedArea}:
     * <ul>
     *   <li>Since <code>paintedArea</code> is in {@link RenderingContext#deviceCS} and since
     *       the transform between the Java2D and the rendering CS is zoom-dependent, a change
     *       of zoom requires a change of <code>paintedArea</code>.</li>
     * <li>Since the zoom change may bring some new area inside the widget bounds, this new
     *     area may need to be rendered and should be added to <code>paintedArea</code>.</li>
     * </ul>
     *
     * Note: The <code>change</code> affine transform must be a change in the <strong>device
     *       coordinate space</strong> ({@link RenderingContext#deviceCS}). But the transform
     *       given by {@link org.geotools.gui.swing.ZoomPane#fireZoomChange} is in the rendering
     *       coordinate space ({@link RenderingContext#mapCS}). Conversion can be performed
     *       as this:
     *
     *       Lets <var>C</var> by the change in rendering space, and <var>Z</var> be the
     *       {@linkplain org.geotools.gui.swing.ZoomPane#zoom zoom}.  Then the change in
     *       device space is <code>ZCZ<sup>-1</sup></code>.
     *
     *       Additionnaly, in order to avoir rounding error, it may be safe to expand slightly
     *       the transformed shape. It may be done with the following operations on the change
     *       matrix, where (x,y) is the widget center:
     *       <blockquote><pre>
     *          translate(x, y);           // final translation
     *          scale(1.00001, 1.00001);   // scale around anchor
     *          translate(-x, -y);         // translate anchor to origin
     *       </pre></blockquote>
     *
     * @param change The zoom <strong>change</strong> in <strong>device</strong> coordinate
     *        system, or <code>null</code> if unknow. If <code>null</code>, then this layer
     *        will be fully redrawn during the next rendering.
     */
    final void zoomChanged(final AffineTransform change) {
        final Renderer renderer = this.renderer;
        if (renderer == null) {
            return;
        }
        final Component mapPane = renderer.mapPane;
        final Shape paintedArea = this.paintedArea;
        if (paintedArea != null) {
            if (change!=null && mapPane!=null) {
                final Area newArea = new Area(mapPane.getBounds());
                newArea.subtract(newArea.createTransformedArea(change));
                final Area area = (paintedArea instanceof Area) ?    (Area)paintedArea
                                                                : new Area(paintedArea);
                area.transform(change);
                area.add(newArea);
                this.paintedArea = area;
            } else {
                this.paintedArea = null;
            }
        }
    }

    /**
     * Efface les données qui avaient été conservées dans une cache interne. L'appel
     * de cette méthode permettra de libérer un peu de mémoire à d'autres fins. Elle
     * sera appelée lorsque qu'on aura déterminé que la couche <code>this</code>  ne
     * sera plus affichée avant un certain temps.  Cette méthode ne doit pas changer
     * le paramétrage de cette couche; son seul impact sera de rendre le prochain
     * traçage un peu plus lent.
     */
    void clearCache() {
        stroke = null;
    }

    /**
     * Libère les ressources occupées par cette couche. Cette méthode est appelée automatiquement
     * lorsqu'il a été déterminé que cette couche sera bientôt détruite.   Elle permet de libérer
     * les ressources plus rapidement que si l'on attend que le ramasse-miettes fasse son travail.
     */
    protected void dispose() {
        synchronized (getTreeLock()) {
            clearCache();
            paintedArea = null;
            final PropertyChangeListener[] list = listeners.getPropertyChangeListeners();
            for (int i=list.length; --i>=0;) {
                listeners.removePropertyChangeListener(list[i]);
            }
        }
    }
}
