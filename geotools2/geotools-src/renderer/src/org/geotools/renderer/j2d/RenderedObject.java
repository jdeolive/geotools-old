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

// User interface and Java2D rendering
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.event.EventListenerList;

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
 * @version $Id: RenderedObject.java,v 1.3 2003/01/22 23:06:49 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public abstract class RenderedObject {
    /**
     * Minimum amout of milliseconds during rendering before logging a message.
     * A message will be logged only if rendering take longer. This is used for
     * tracking down performance bottleneck.
     */
    private static final int TIME_THRESHOLD = 200;

    /**
     * The component where to send {@link Component#repaint()} request,
     * or <code>null</code> if none.
     */
    transient Component mapPane;

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
    private Stroke stroke;

    /**
     * Indique si cette couche est visible. Les couches sont invisibles par défaut. L'appel
     * de {@link Renderer#add} appelera systématiquement <code>setVisible(true)</code>.
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
     * Liste des objets intéressés à être informés des
     * changements apportés à cet objet <code>RenderedObject</code>.
     */
    private final EventListenerList listenerList = new EventListenerList();

    /**
     * Construct a new <code>RenderedObject</code> layer. The {@linkplain #getCoordinateSystem
     * coordinate system} default to {@linkplain GeographicCoordinateSystem#WGS84 WGS 1984}
     * and the {@linkplain #getZOrder z-order} default to positive infinity (i.e. this layer
     * is drawn on top of everything else). Subclasses should invokes <code>setXXX</code>
     * methods in order to define properly the properties for this layer.
     *
     * @see #setZOrder
     * @see #setCoordinateSystem
     * @see #setPreferredArea
     * @see #setPreferredPixelSize
     */
    public RenderedObject() {
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
    public synchronized void setCoordinateSystem(final CoordinateSystem cs)
            throws TransformException
    {
        final CoordinateSystem oldCS = coordinateSystem;
        coordinateSystem = CTSUtilities.getCoordinateSystem2D(cs);
        firePropertyChange("coordinateSystem", oldCS, cs);
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
    public synchronized void setPreferredArea(final Rectangle2D area) {
        paintedArea = null;
        firePropertyChange("preferredArea", preferredArea,
                           preferredArea=(area!=null) ? (Rectangle2D) area.clone() : null);
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
    public synchronized void setPreferredPixelSize(final Dimension2D size) {
        stroke = null;
        firePropertyChange("preferredPixelSize", preferredPixelSize,
                           preferredPixelSize=(size!=null) ? (Dimension2D)size.clone() : null);
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
    public synchronized void setZOrder(final float zOrder) throws IllegalArgumentException {
        if (!Float.isNaN(zOrder)) {
            final float oldZOrder = this.zOrder;
            if (zOrder != oldZOrder) {
                this.zOrder = zOrder;
                repaint();
                firePropertyChange("zOrder", new Float(oldZOrder), new Float(zOrder));
            }
        } else {
            throw new IllegalArgumentException(String.valueOf(zOrder));
        }
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
    public synchronized void setTools(final Tools tools) {
        firePropertyChange("tools", this.tools, this.tools=tools);
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
     *   <li><code>{@link Renderer#add Renderer.add}(this)</code>
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
    public synchronized void setVisible(final boolean visible) {
        if (visible != this.visible) {
            this.visible = visible;
            repaint();
            final Boolean before, after;
            if (visible) {
                before = Boolean.FALSE;
                after  = Boolean.TRUE;
            } else {
                before = Boolean.TRUE;
                after  = Boolean.FALSE;
            }
            firePropertyChange("visible", before, after);
        }
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
        final Component mapPane = this.mapPane;
        if (mapPane != null) {
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
    final synchronized void update(final RenderingContext context,
                                   final Rectangle clipBounds)
            throws TransformException
    {
        if (visible) {
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
     * Efface les données qui avaient été conservées dans une cache interne. L'appel
     * de cette méthode permettra de libérer un peu de mémoire à d'autres fins. Elle
     * sera appelée lorsque qu'on aura déterminé que la couche <code>this</code>  ne
     * sera plus affichée avant un certain temps.  Cette méthode ne doit pas changer
     * le paramétrage de cette couche; son seul impact sera de rendre le prochain
     * traçage un peu plus lent.
     */
    void clearCache() {
    }

    /**
     * Ajoute un objet intéressé à être informé chaque fois qu'une propriété de cette
     * couche <code>RenderedObject</code> change. Les méthodes {@link #setVisible}
     * et {@link #setZOrder} en particulier tiendront ces objets au courant des
     * changements qu'ils font.
     */
    public void addPropertyChangeListener(final PropertyChangeListener listener) {
        listenerList.add(PropertyChangeListener.class, listener);
    }

    /**
     * Retire un objet qui n'est plus intéressé à être informé chaque fois
     * que change une propriété de cette couche <code>RenderedObject</code>.
     */
    public void removePropertyChangeListener(final PropertyChangeListener listener) {
        listenerList.remove(PropertyChangeListener.class, listener);
    }

    /**
     * Prévient tous les objets intéressés que l'état de cette couche a changé.
     * La méthode {@link PropertyChangeListener#propertyChange} de tous les
     * listeners sera appelée, sauf si <code>oldValue</code> et
     * <code>newValue</code> sont identiques.
     *
     * @param propertyName nom de la propriété qui change (par exemple "preferredArea"} ou "zOrder"}).
     * @param oldValue Ancienne valeur (avant le changement).
     * @param newValue Nouvelle valeur (après le changement).
     */
    protected void firePropertyChange(final String propertyName,
                                      final Object oldValue, final Object newValue)
    {
        if (oldValue!=newValue && (oldValue==null || !oldValue.equals(newValue))) {
            PropertyChangeEvent event = null;
            final Object[]  listeners = listenerList.getListenerList();
            for (int i=listeners.length; (i-=2)>=0;) {
                if (listeners[i] == PropertyChangeListener.class) {
                    if (event == null) {
                        event=new PropertyChangeEvent(this, propertyName, oldValue, newValue);
                    }
                    try {
                        ((PropertyChangeListener) listeners[i+1]).propertyChange(event);
                    } catch (RuntimeException exception) {
                        Utilities.unexpectedException("fr.ird.map", "RenderedObject",
                                                      "firePropertyChange", exception);
                    }
                }
            }
        }
    }

    /**
     * Libère les ressources occupées par cette couche. Cette méthode est appelée automatiquement
     * lorsqu'il a été déterminé que cette couche sera bientôt détruite.   Elle permet de libérer
     * les ressources plus rapidement que si l'on attend que le ramasse-miettes fasse son travail.
     */
    protected void dispose() {
        paintedArea = null;
        final Object[] listeners = listenerList.getListenerList();
        for (int i=listeners.length; (i-=2)>=0;) {
            listenerList.remove((Class)listeners[i-2], (EventListener)listeners[i-1]);
        }
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
    static boolean contains(final Rectangle2D big, final Rectangle2D small, final boolean edge) {
        return edge ? (small.getMinX()>=big.getMinX() && small.getMaxX()<=big.getMaxX() && small.getMinY()>=big.getMinY() && small.getMaxY()<=big.getMaxY()):
                      (small.getMinX()> big.getMinX() && small.getMaxX()< big.getMaxX() && small.getMinY()> big.getMinY() && small.getMaxY()< big.getMaxY());
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
    static Rectangle2D changeArea(Rectangle2D area,
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
}
