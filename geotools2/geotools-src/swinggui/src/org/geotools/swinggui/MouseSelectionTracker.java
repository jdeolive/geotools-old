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
package org.geotools.swinggui;

// Geometry
import java.awt.Shape;
import java.awt.Rectangle;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.geom.RectangularShape;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;

// Graphics
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;

// Events
import java.awt.event.MouseEvent;
import javax.swing.event.MouseInputAdapter;


/**
 * Contrôleur permettant à l'utilisateur de sélectionner une région sur une
 * composante. L'utilisateur doit cliquer sur un point de la composante, puis
 * faire glisser le curseur de la souris en tenant le bouton enfoncé. Pendant
 * le glissement, la forme qui sera dessinée sera généralement un rectangle.
 * D'autres formes pourraient toutefois être utilisées, comme par exemple une
 * ellipse. Pour utiliser cette classe, il faut créer une classe dérivée qui
 * définisse les méthodes suivantes:
 *
 * <ul>
 *   <li>{@link #selectionPerformed} (obligatoire)</li>
 *   <li>{@link #getModel} (facultatif)</li>
 * </ul>
 *
 * Ce contrôleur doit ensuite être enregistré auprés d'une et une
 * seule composante en utilisant la syntaxe suivante:
 *
 * <blockquote><pre>
 * {@link Component} component=...
 * MouseSelectionTracker control=...
 * component.addMouseListener(control);
 * </pre></blockquote>
 *
 * @version 1.0
 * @author Martin Desruisseaux
 */
abstract class MouseSelectionTracker extends MouseInputAdapter
{
    /**
     * Rectangle pointillé représentant la région en train de se faire
     * sélectionner par l'utilisateur. Ce rectangle peut être vide. Ces
     * coordonnées ne prendront une signification qu'entre les moments
     * où l'utilisateur a appuyé sur le bouton de la souris et le moment
     * où il l'a relâché pour délimiter une région. Par convention, la
     * valeur <code>null</code> indique qu'une ligne doit être utilisée
     * au lieu d'une forme rectangulaire. Les coordonnées sont toujours
     * exprimées en pixels.
     */
    private transient RectangularShape mouseSelectedArea;

    /**
     * Couleur à remplacer lors des traçage XOR sur un graphique.
     * Cette couleur est spécifiée à {@link Graphics2D#setColor}.
     */
    private Color backXORColor=Color.white;

    /**
     * Couleur remplacante lors des traçage XOR sur un graphique.
     * Cette couleur est spécifiée à {@link Graphics2D#setXORMode}.
     */
    private Color lineXORColor=Color.black;

    /**
     * Coordonnée <var>x</var> de la souris
     * lorsque le bouton a été enfoncé.
     */
    private transient int ox;

    /**
     * Coordonnée <var>y</var> de la souris
     * lorsque le bouton a été enfoncé.
     */
    private transient int oy;

    /**
     * Coordonnée <var>x</var> de la souris
     * lors du dernier glissement.
     */
    private transient int px;

    /**
     * Coordonnée <var>y</var> de la souris
     * lors du dernier glissement.
     */
    private transient int py;

    /**
     * Indique si une sélection est en cours.
     */
    private transient boolean isDragging;

    /**
     * Construit un objet qui permettra de sélectionner
     * des régions rectangulaires à l'aide de la souris.
     */
    public MouseSelectionTracker() {
    }

    /**
     * Spécifie les couleurs à utiliser pour tracer le contour d'une boîte
     * lorsque l'utilisateur sélectionne une région. Toutes les couleurs
     * <code>a</code> seront remplacées par les couleurs <code>b</code> et
     * vis-versa.
     */
    public void setXORColors(final Color a, final Color b) {
        backXORColor=a;
        lineXORColor=b;
    }

    /**
     * Retourne la forme géométrique à utiliser pour délimiter une région.
     * Cette forme est généralement un rectangle mais pourrait aussi être
     * une ellipse, une flèche ou d'autres formes encore. Les coordonnées
     * de la forme retournée ne seront pas prises en compte. En fait, ces
     * coordonnées seront régulièrement écrasées.  Seule compte la classe
     * de la forme retournée (par exemple {@link java.awt.geom.Ellipse2D}
     * vs {@link java.awt.geom.Rectangle2D}) et ses paramètres non-reliés
     * à sa position (par exemple l'arrondissement des coins d'un rectangle).
     *
     * La forme retournée sera généralement d'une classe dérivée de
     * {@link RectangularShape}, mais peut aussi être de la classe
     * {@link Line2D}. <strong>Tout autre classe risque de lancer une
     * {@link ClassCastException} au moment de l'exécution</strong>.
     *
     * L'implémentation par défaut retourne toujours un objet {@link Rectangle}.
     *
     * @param  event Coordonnée de la souris au moment ou le bouton a été
     *         enfoncé. Cette information peut être utilisée par les classes
     *         dérivées qui voudraient tenir compte de la position de la souris
     *         avant de choisir une forme géométrique.
     * @return Forme de la classe {link RectangularShape} ou {link Line2D}, ou
     *         <code>null</code> pour indiquer qu'on ne veut pas faire de
     *         sélection.
     */
    protected Shape getModel(final MouseEvent event) {
        return new Rectangle();
    }

    /**
     * Méthode appelée automatiquement après que l'utilisateur
     * ait sélectionnée une région à l'aide de la souris. Toutes
     * les coordonnées passées en paramètres sont exprimées en
     * pixels.
     *
     * @param ox Coordonnée <var>x</var> de la souris lorsque
     *        l'utilisateur a enfoncé le bouton de la souris.
     * @param oy Coordonnée <var>y</var> de la souris lorsque
     *        l'utilisateur a enfoncé le bouton de la souris.
     * @param px Coordonnée <var>x</var> de la souris lorsque
     *        l'utilisateur a relaché le bouton de la souris.
     * @param py Coordonnée <var>y</var> de la souris lorsque
     *        l'utilisateur a relaché le bouton de la souris.
     */
    protected abstract void selectionPerformed(int ox, int oy, int px, int py);

    /**
     * Retourne la forme géométrique entourant la dernière région sélectionée
     * par l'utilisateur. Une transformation affine facultative peut être
     * spécifiée pour convertir en coordonnées logiques la région sélectionnée
     * par l'utilisateur. La classe de la forme retournée dépend du model que
     * retourne {@link #getModel}:
     *
     * <ul>
     *   <li>Si le modèle est nul (ce qui signifie que cet objet
     *       <code>MouseSelectionTracker</code> ne fait que dessiner une ligne
     *       entre deux points), alors l'objet retourné sera de la classe
     *       {@link Line2D}.</li>
     *   <li>Si le modèle est non-nul, alors l'objet retourné peut être de la
     *       même classe (le plus souvent {@link java.awt.geom.Rectangle2D}).
     *       Il peut toutefois y avoir des situations ou l'objet retourné sera
     *       d'une autre classe, par exemple si la transformation affine
     *       <code>transform</code> effectue une rotation.</li>
     * </ul>
     *
     * @param  transform Transformation affine qui sert à convertir les
     *         coordonnées logiques en coordonnées pixels. Il s'agit
     *         généralement de la transformation affine qui est utilisée dans
     *         une méthode <code>paint(...)</code> pour dessiner des formes
     *         exprimées en coordonnées logiques.
     * @return Une forme géométrique  entourant la dernière région sélectionée
     *         par l'utilisateur, ou <code>null</code> si aucune sélection n'a
     *         encore été faite.
     * @throws NoninvertibleTransformException Si la transformation affine
     *         <code>transform</code> ne peut pas être inversée.
     */
    public Shape getSelectedArea(final AffineTransform transform) throws NoninvertibleTransformException {
        if (ox==px && oy==py) return null;
        RectangularShape shape=mouseSelectedArea;
        if (transform!=null && !transform.isIdentity()) {
            if (shape==null) {
                final Point2D.Float po=new Point2D.Float(ox,oy);
                final Point2D.Float pp=new Point2D.Float(px,py);
                transform.inverseTransform(po,po);
                transform.inverseTransform(pp,pp);
                return new Line2D.Float(po,pp);
            } else {
                if (canReshape(shape, transform)) {
                    final Point2D.Double point=new Point2D.Double();
                    double xmin=Double.POSITIVE_INFINITY;
                    double ymin=Double.POSITIVE_INFINITY;
                    double xmax=Double.NEGATIVE_INFINITY;
                    double ymax=Double.NEGATIVE_INFINITY;
                    for (int i=0; i<4; i++) {
                        point.x = (i&1)==0 ? shape.getMinX() : shape.getMaxX();
                        point.y = (i&2)==0 ? shape.getMinY() : shape.getMaxY();
                        transform.inverseTransform(point, point);
                        if (point.x<xmin) xmin=point.x;
                        if (point.x>xmax) xmax=point.x;
                        if (point.y<ymin) ymin=point.y;
                        if (point.y>ymax) ymax=point.y;
                    }
                    if (shape instanceof Rectangle) {
                        return new Rectangle2D.Float((float) xmin,
                                                     (float) ymin,
                                                     (float) (xmax-xmin),
                                                     (float) (ymax-ymin));
                    } else {
                        shape = (RectangularShape) shape.clone();
                        shape.setFrame(xmin, ymin, xmax-xmin, ymax-ymin);
                        return shape;
                    }
                }
                else {
                    return transform.createInverse().createTransformedShape(shape);
                }
            }
        }
        else {
            return (shape!=null) ? (Shape) shape.clone() : new Line2D.Float(ox,oy,px,py);
        }
    }

    /**
     * Indique si on peut transformer la forme <code>shape</code> en appellant
     * simplement sa méthode <code>shape.setFrame(...)</code> plutôt que
     * d'utiliser l'artillerie lourde qu'est la méthode
     * <code>transform.createTransformedShape(shape)</code>.
     */
    private static boolean canReshape(final RectangularShape shape,
                                      final AffineTransform transform) {
        final int type=transform.getType();
        if ((type & AffineTransform.TYPE_GENERAL_TRANSFORM) != 0) return false;
        if ((type & AffineTransform.TYPE_MASK_ROTATION)     != 0) return false;
        if ((type & AffineTransform.TYPE_FLIP)              != 0) {
            if (shape instanceof Rectangle2D)      return true;
            if (shape instanceof Ellipse2D)        return true;
            if (shape instanceof RoundRectangle2D) return true;
            return false;
        }
        return true;
    }

    /**
     * Retourne un objet {@link Graphics2D} à utiliser pour dessiner dans
     * la composante spécifiée. Il ne faudra pas oublier d'appeller {@link
     * Graphics2D#dispose} lorsque le graphique ne sera plus nécessaire.
     */
    private Graphics2D getGraphics(final Component c) {
        final Graphics2D graphics=(Graphics2D) c.getGraphics();
        graphics.setXORMode(lineXORColor);
        graphics.setColor  (backXORColor);
        return graphics;
    }

    /**
     * Informe ce controleur que le bouton de la souris vient d'être enfoncé.
     * L'implémentation par défaut retient la coordonnée de la souris (qui
     * deviendra un des coins du futur rectangle à dessiner) et prépare
     * <code>this</code> à observer les déplacements de la souris.
     *
     * @throws ClassCastException si {@link #getModel} ne retourne pas une
     *         forme de la classe {link RectangularShape} ou {link Line2D}.
     */
    public void mousePressed(final MouseEvent event) throws ClassCastException {
        if (!event.isConsumed() && (event.getModifiers() & MouseEvent.BUTTON1_MASK)!=0) {
            final Component source=event.getComponent();
            if (source!=null) {
                Shape model=getModel(event);
                if (model!=null) {
                    isDragging=true;
                    ox=px=event.getX();
                    oy=py=event.getY();
                    if (model instanceof Line2D) model=null;
                    mouseSelectedArea=(RectangularShape) model;
                    if (mouseSelectedArea!=null) {
                        mouseSelectedArea.setFrame(ox, oy, 0, 0);
                    }
                    source.addMouseMotionListener(this);
                }
                source.requestFocus();
                event.consume();
            }
        }
    }

    /**
     * Informe ce controleur que la souris vient de glisser avec le bouton
     * enfoncé. L'implémentation par défaut observe ce glissement pour déplacer
     * un coin du rectangle servant à sélectionner une région. L'autre coin
     * reste fixé à l'endroit où était la souris au moment ou le bouton a été
     * enfoncé.
     */
    public void mouseDragged(final MouseEvent event) {
        if (isDragging) {
            final Graphics2D graphics=getGraphics(event.getComponent());
            if (mouseSelectedArea==null) {
                graphics.drawLine(ox, oy, px, py);
                px=event.getX();
                py=event.getY();
                graphics.drawLine(ox, oy, px, py);
            } else {
                graphics.draw(mouseSelectedArea);
                int xmin=this.ox;
                int ymin=this.oy;
                int xmax=px=event.getX();
                int ymax=py=event.getY();
                if (xmin>xmax) {
                    final int xtmp=xmin;
                    xmin=xmax;xmax=xtmp;
                }
                if (ymin>ymax) {
                    final int ytmp=ymin;
                    ymin=ymax;ymax=ytmp;
                }
                mouseSelectedArea.setFrame(xmin, ymin, xmax-xmin, ymax-ymin);
                graphics.draw(mouseSelectedArea);
            }
            graphics.dispose();
            event.consume();
        }
    }

    /**
     * Informe ce controleur que le bouton de la souris vient d'être relâché.
     * L'implémentation par défaut appelle {@link #selectionPerformed} avec
     * en paramètres les limites de la région sélectionnée.
     */
    public void mouseReleased(final MouseEvent event) {
        if (isDragging && (event.getModifiers() & MouseEvent.BUTTON1_MASK)!=0) {
            isDragging=false;
            final Component component=event.getComponent();
            component.removeMouseMotionListener(this);

            final Graphics2D graphics=getGraphics(event.getComponent());
            if (mouseSelectedArea==null) {
                graphics.drawLine(ox, oy, px, py);
            } else {
                graphics.draw(mouseSelectedArea);
            }
            graphics.dispose();
            px = event.getX();
            py = event.getY();
            selectionPerformed(ox, oy, px, py);
            event.consume();
        }
    }

    /**
     * Informe ce controleur que la souris vient d'être déplacé sans que
     * ce soit dans le contexte où l'utilisateur sélectionne une région.
     * L'implémentation par défaut signale à la composante source que
     * <code>this</code> n'est plus interessé à être informé des
     * déplacements de la souris.
     */
    public void mouseMoved(final MouseEvent event) {
        // Normalement pas nécessaire, mais il semble que ce
        // "listener" reste parfois en place alors qu'il n'aurait pas dû.
        event.getComponent().removeMouseMotionListener(this);
    }
}
