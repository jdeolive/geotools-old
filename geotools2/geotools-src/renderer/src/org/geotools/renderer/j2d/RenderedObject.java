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
import java.awt.Rectangle;
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
import javax.swing.JPopupMenu;

// Miscellaneous J2SE
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.LogRecord;

// Geotools dependencies
import org.geotools.gui.swing.ZoomPane; // For JavaDoc
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
 * @version $Id: RenderedObject.java,v 1.2 2003/01/20 23:21:10 desruisseaux Exp $
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
    transient Component mapPanel;

    /**
     * Forme géométrique englobant la région dans laquelle la couche a été dessinée lors du
     * dernier appel de la méthode {@link #paint}.  Les coordonnées de cette région doivent
     * être en exprimées en coordonnées pixels de l'écran ({@link RenderingContext#deviceCS}).
     * La valeur <code>null</code> signifie qu'on peut considérer que cette couche occupe la
     * totalité de la surface dessinable.
     */
    private transient Shape shape;

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
     * Liste des objets intéressés à être informés des
     * changements apportés à cet objet <code>RenderedObject</code>.
     */
    private final EventListenerList listenerList = new EventListenerList();

    /**
     * Construct a new <code>RenderedObject</code> layer. The {@linkplain #getCoordinateSystem
     * coordinate system} default to {@linkplain GeographicCoordinateSystem#WGS84 WGS 1984}
     * and the {@linkplain #getZOrder z-order} default to positive infinity (i.e. this layer
     * is drawn on top of everything else). Subclasses should invokes <code>setXXX</code>
     * methods in order to define properly this object's properties.
     *
     * @see #setZOrder
     * @see #setCoordinateSystem
     * @see #setPreferredArea
     * @see #setPreferredPixelSize
     */
    public RenderedObject() {
    }

    /**
     * Returns this layer's name. Default implementation returns class name
     * and its z-order.
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
     * Returns the two-dimensional coordinate system in use. This coordinate system is
     * used by {@link #getPreferredArea} and {@link #getPreferredPixelSize} among others.
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
     * Set the coordinate system to use. This method is usually invoked only once,
     * at construction time.  If the specified coordinate system has more than two
     * dimensions, then it must be a {@link CompoundCoordinateSystem} in which the
     * <code>headCS</code> has only two dimensions.
     *
     * @param  cs The coordinate system.
     * @throws IllegalArgumentException If <code>cs</code> can't be reduced
     *         to a two-dimensional coordinate system.
     */
    public synchronized void setCoordinateSystem(final CoordinateSystem cs) {
        final CoordinateSystem oldCS = coordinateSystem;
        coordinateSystem = CTSUtilities.getCoordinateSystem2D(cs);
        firePropertyChange("coordinateSystem", oldCS, cs);
    }

    /**
     * Retourne les coordonnées géographiques de cette couche. Les coordonnées retournées ne sont
     * pas obligées d'englober toute la couche (quoique ce soit souvent le cas).  Elles indiquent
     * plutôt la partie de la couche que l'on souhaite voir apparaître. Le rectangle retourné sera
     * exprimé selon le système de coordonnées retourné par {@link #getCoordinateSystem}. Si cette
     * couche n'a pas de limites géographiques bien définies (par exemple si elle n'est qu'une
     * légende ou l'échelle de la carte), alors cette méthode peut retourner <code>null</code>.
     */
    public final Rectangle2D getPreferredArea() {
        final Rectangle2D preferredArea = this.preferredArea;
        return (preferredArea!=null) ? (Rectangle2D) preferredArea.clone() : null;
    }

    /**
     * Modifie les coordonnées géographiques de cette couche. L'appel de cette méthode ne modifie
     * par le géoréférencement; elle affecte simplement la région qui sera affichée par défaut dans
     * une fenêtre.
     */
    public synchronized void setPreferredArea(final Rectangle2D area) {
        shape = null;
        firePropertyChange("preferredArea", preferredArea,
                           preferredArea=(area!=null) ? (Rectangle2D) area.clone() : null);
    }

    /**
     * Returns the preferred pixel size for a close
     * zoom, or <code>null</code> if there is none.
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
     */
    public synchronized void setPreferredPixelSize(final Dimension2D size) {
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
     * Determines whether this layer should be visible when its parent is visible.
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
     *       appelera <code>setVisible(true)</code>. Les classes dérivées peuvent profiter
     *       de cette spécification pour s'enregistrer auprès de {@link MapPanel} comme étant
     *       intéressées à suivre les mouvements de la souris par exemple.</li>
     *   <li><code>{@link Renderer#remove Renderer.remove}(this)</code>
     *       appelera <code>setVisible(false)</code>. Les classes dérivées peuvent profiter
     *       de cette spécification pour déclarer à {@link MapPanel} qu'elles ne sont plus
     *       intéressées à suivre les mouvements de la souris par exemple.</li>
     * </ul>
     */
    public synchronized void setVisible(final boolean visible) {
        if (visible != this.visible) {
            this.visible = visible;
            repaint();
            if (visible) firePropertyChange("visible", Boolean.FALSE, Boolean.TRUE);
            else         firePropertyChange("visible", Boolean.TRUE, Boolean.FALSE);
        }
    }

    /**
     * Indique que cette couche a besoin d'être redéssinée. La couche ne sera pas redessinée
     * immediatement, mais seulement un peu plus tard. Cette méthode <code>repaint()</code>
     * peut être appelée à partir de n'importe quel thread (pas nécessairement celui de
     * <i>Swing</i>).
     */
    public void repaint() {
        repaint(shape!=null ? shape.getBounds() : null);
    }

    /**
     * Indique qu'une partie de cette couche a besoin d'être redéssinée.
     * Cette méthode peut être appelée à partir de n'importe quel thread
     * (pas nécessairement celui de <i>Swing</i>).
     *
     * @param bounds Coordonnées (en points) de la partie à redessiner.
     */
    protected void repaint(final Rectangle bounds) {
        final Component mapPanel = this.mapPanel;
        if (mapPanel != null) {
            if (EventQueue.isDispatchThread()) {
                if (bounds == null) {
                    mapPanel.repaint();
                } else {
                    mapPanel.repaint(bounds.x, bounds.y, bounds.width, bounds.height);
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
     * to be repainted. The painting must be done in the {@linkplain RendereringContext#mapCS
     * rendering coordinate system} (usually "real world" metres). This method is responsible
     * for transformations from its own {@linkplain #getCoordinateSystem underlying CS} to the
     * {@linkplain RendereringContext#mapCS rendering CS}. The {@link RenderingContext} object
     * provides informations for such transformations:
     *
     * <p><code>context.{@link RenderingContext#getMathTransform getMathTransform}(
     *                  {@link #getCoordinateSystem getCoordinateSystem}(),
     *                  context.{@link RenderingContext#mapCS mapCS} )</code><br>
     * Returns a transform from this layer's CS to the rendering CS.</p>
     *
     * <p><code>context.{@link RenderingContext#getMathTransform getMathTransform}(
     *                  context.{@link RenderingContext#mapCS mapCS},
     *                  context.{@link RenderingContext#textCS textCS} )</code><br>
     * Returns a transform from the rendering CS to "points" (usually 1/72 of inch).
     * This transformation is zoom dependent.</p>
     *
     * <p><code>context.{@link RenderingContext#getMathTransform getMathTransform}(
     *                  context.{@link RenderingContext#textCS textCS},
     *                  context.{@link RenderingContext#deviceCS deviceCS} )</code><br>
     * Returns a transform from the "points" CS to device CS.
     * This transformation is device dependent.</p>
     *
     * <p>Cet objet contient un objet {@link java.awt.Graphics2D} qui aura déjà été
     * configuré en fonction de l'afficheur. En général, tous les traçages fait sur cet objet le
     * seront en <em>mètres sur le terrain</em>, ou en degrés de longitude et de latitude. C'est
     * approprié pour le traçage d'une carte, mais pas pour l'écriture de textes. Pour alterner
     * entre le traçage de cartes et l'écriture de texte, on peut procéder comme suit:</p>
     *
     * <blockquote><pre>
     * &nbsp;Shape paint(RenderingContext context) {
     * &nbsp;    Graphics2D graphics = context.graphics;
     * &nbsp;    // <i>Paint here map features in geographic coordinates (m or °)</i>
     * &nbsp;
     * &nbsp;    context.setCoordinateSystem(context.textCS);
     * &nbsp;    // <i>Write here text or label. Coordinates are in <u>points</u>.</i>
     * &nbsp;
     * &nbsp;    context.setCoordinateSystem(context.mapCS);
     * &nbsp;    // <i>Continue here the rendering of map features in geographic coordinates</i>
     * &nbsp;}
     * </pre></blockquote>
     *
     * @param  context  Suite des transformations nécessaires à la conversion de coordonnées
     *         géographiques (<var>longitude</var>,<var>latitude</var>) en coordonnées pixels.
     *
     * @throws TransformException Si un problème est survenu lors d'une projection cartographique.
     */
    protected abstract void paint(final Graphics2D graphics, final RenderingContext context)
            throws TransformException;

    /**
     * Méthode appelée automatiquement pour construire une chaîne de caractères représentant la
     * valeur pointée par la souris. En général (mais pas obligatoirement), lorsque cette méthode
     * est appelée, le buffer <code>toAppendTo</code> contiendra déjà une chaîne de caractères
     * représentant les coordonnées pointées par la souris. Cette méthode est appelée pour donner
     * une chance aux <code>RenderedObject</code> d'ajouter d'autres informations pertinentes. Par
     * exemple des couches qui représentent une image satellitaire de température peuvent ajouter
     * à <code>toAppendTo</code> un texte du genre "12°C" (sans espaces au début). L'implémentation
     * par défaut de cette méthode retourne toujours <code>false</code> sans rien faire.
     *
     * @param  event Coordonnées du curseur de la souris.
     * @param  toAppendTo Le buffer dans lequel ajouter des informations.
     * @return <code>true</code> si cette méthode a ajouté des informations dans <code>toAppendTo</code>.
     *         Dans ce cas, les couches en-dessous de <code>this</code> ne seront pas interrogées.
     */
    protected boolean getLabel(final GeoMouseEvent event, final StringBuffer toAppendTo) {
        return false;
    }

    /**
     * Retourne le texte à afficher dans une bulle lorsque la souris traîne sur cette couche.
     * L'implémentation par défaut retourne toujours <code>null</code>, ce qui signifie que
     * cette couche n'a aucun texte à afficher (les autres couches seront alors interrogées).
     * Les classes dérivées peuvent redéfinir cette méthode pour retourner un texte après avoir
     * vérifié que les coordonnées de <code>event</code> correspondent bien à un point de cette
     * couche.
     *
     * @param  event Coordonnées du curseur de la souris.
     * @return Le texte à afficher lorsque la souris traîne sur cet élément.
     *         Ce texte peut être nul pour signifier qu'il ne faut pas en écrire.
     */
    protected String getToolTipText(final GeoMouseEvent event) {
        return null;
    }

    /**
     * Méthode appellée automatiquement chaque fois qu'il a été déterminé qu'un menu contextuel
     * devrait être affiché. Sur Windows et Solaris, cette méthode est appelée lorsque l'utilisateur
     * a appuyé sur le bouton droit de la souris. Si cette couche désire faire apparaître un menu,
     * elle devrait retourner le menu en question. Si non, elle devrait retourner <code>null</code>.
     * L'implémentation par défaut retourne toujours <code>null</code>.
     *
     * @param  event Coordonnées du curseur de la souris.
     * @return Menu contextuel à faire apparaître, ou <code>null</code>
     *         si cette couche ne propose pas de menu contextuel.
     */
    protected JPopupMenu getPopupMenu(final GeoMouseEvent event) {
        return null;
    }

    /**
     * Méthode appellée chaque fois que le bouton de la souris a été cliqué sur une couche qui
     * pourrait être <code>this</code>. L'implémentation par défaut ne fait rien. Les classes
     * dérivées qui souhaite entrepredre une action doivent d'abord vérifier si les coordonnées
     * de <code>event</code> correspondent bien à un point de cette couche. Si oui, alors elles
     * doivent aussi appeler {@link GeoMouseEvent#consume} après leur action, pour que le clic
     * de la souris ne soit pas transmis aux autres couches en-dessous de celle-ci.
     */
    protected void mouseClicked(final GeoMouseEvent event) {
    }

    /**
     * Efface les données qui avaient été conservées dans une cache interne. L'appel
     * de cette méthode permettra de libérer un peu de mémoire à d'autres fins. Elle
     * sera appelée lorsque qu'on aura déterminé que la couche <code>this</code>  ne
     * sera plus affichée avant un certain temps.  Cette méthode ne doit pas changer
     * le paramétrage de cette couche;  son seul impact sera que le prochain traçage
     * sera un peu plus lent.
     */
    protected void clearCache() {
    }

    /**
     * Libère les ressources occupées par cette couche. Cette méthode est appelée automatiquement
     * lorsqu'il a été déterminé que cette couche sera bientôt détruite.   Elle permet de libérer
     * les ressources plus rapidement que si l'on attend que le ramasse-miettes fasse son travail.
     */
    protected void dispose() {
        shape = null;
    }

    /**
     * Ajoute un objet intéressé à être informé chaque fois qu'une propriété de cette
     * couche <code>RenderedObject</code> change. Les méthodes {@link #setVisible}
     * et {@link #setZOrder} en particulier tiendront ces objets au courant des
     * changements qu'ils font.
     */
    public final void addPropertyChangeListener(final PropertyChangeListener listener) {
        listenerList.add(PropertyChangeListener.class, listener);
    }

    /**
     * Retire un objet qui n'est plus intéressé à être informé chaque fois
     * que change une propriété de cette couche <code>RenderedObject</code>.
     */
    public final void removePropertyChangeListener(final PropertyChangeListener listener) {
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




    /////////////////////////////////////////////////////////////
    ////////                                             ////////
    ////////        METHODES RESERVEES A Renderer        ////////
    ////////                                             ////////
    /////////////////////////////////////////////////////////////

    /**
     * Méthode appelée automatiquement chaque fois que le zoom a changé.
     * Cette méthode met à jour les coordonnées des formes géométriques
     * déclarées dans les objets {@link RenderedObject}.
     * <br><br>
     * Note: La transformation affine donnée en argument à cette méthode doit
     * représenter une transformation <strong>dans l'espace des pixels</strong>.
     * Or, la transformation affine spécifiée par {@link ZoomPane#fireZoomChange}
     * représente une transformation dans l'espace des coordonnées logiques. Soit
     * <var>C</var> la transformation spécifiée par {@link ZoomPane}, et <var>Z</var>
     * le zoom {@link ZoomPane#zoom}. Alors la transformation donnée à cette méthode
     * doit être <code>ZCZ<sup>-1</sup></code>.
     *
     * @param change Transformation à utiliser pour transformer les coordonnées,
     *        ou <code>null</code> si elle n'est pas connue. Dans ce dernier cas,
     *        la couche sera inconditionnellement redessinée lors du prochain traçage.
     */
    final void zoomChanged(final AffineTransform change) {
        final Shape shape = this.shape;
        if (shape != null) {
            if (change != null) {
                final Rectangle2D bounds = shape.getBounds2D();
                this.shape = XAffineTransform.transform(change, bounds, bounds);
            } else {
                this.shape = null;
            }
        }
    }

    /**
     * Procède au traçage de cette couche, à la condition qu'elle soit
     * visible à l'intérieur du rectangle <code>clipBounds</code> spécifié.
     *
     * @param graphics   Graphique dans lequel faire le traçage.
     * @param context    Suite des transformations permettant de passer des
     *                   coordonnées de cette couche en coordonnées pixels.
     * @param clipBounds Coordonnées en points de la portion de l'écran à redessiner.
     */
    final synchronized void paint(final Graphics2D graphics,
                                  final RenderingContext context,
                                  final Rectangle clipBounds)
            throws TransformException
    {
        if (visible) {
            if (shape==null || clipBounds==null || shape.intersects(clipBounds)) {
                long time = System.currentTimeMillis();
                paint(graphics, context);
                if (context.normalDrawing()) {
                    this.shape = context.getRenderedArea();
                }
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
     * Indique si cette couche contient le point spécifié.
     *
     * @param  x Coordonnées <var>x</var> du point.
     * @param  y Coordonnées <var>y</var> du point.
     * @return <code>true</code> si cette composante est
     *         visible et contient le point spécifié.
     */
    final boolean contains(final int x, final int y) {
        if (visible) {
            final Shape shape = this.shape;
            return (shape!=null) && shape.contains(x,y);
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
