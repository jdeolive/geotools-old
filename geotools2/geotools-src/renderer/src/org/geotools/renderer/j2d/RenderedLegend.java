/*
 * Geotools - OpenSource mapping toolkit
 * (C) 2003, Institut de Recherche pour le Développement
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
import java.awt.Insets;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.font.GlyphVector;
import javax.swing.SwingConstants;

// Geotools dependencies
import org.geotools.ct.TransformException;


/**
 * Classe de base des légendes que l'on placera sur la carte. Cette classe prend en charge le
 * positionnement de la légende. Ce positionnement est exprimé en nombre de pixels par rapport
 * aux bords de la fenêtre qui contient la carte. Cette position peut être modifiée à l'aide des
 * méthodes suivantes:</p>
 *
 * <ul>
 *   <li>{@link #setPosition} pour spécifier par rapport à quels bords de
 *                            la fenêtre la légende sera positionnée.</li>
 *   <li>{@link #setMargin}   pour spécifier de combien de pixels écarter
 *                            la légende du bord de la fenêtre.</li>
 * </ul>
 *
 * @version $Id: RenderedLegend.java,v 1.1 2003/03/11 12:34:39 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public abstract class RenderedLegend extends RenderedLayer {
    /**
     * Position où placer la légende.
     */
    private LegendPosition position = LegendPosition.NORTH_EAST;

    /**
     * Espace à laisser entre le haut de la fenêtre et le haut de la légende.
     */
    private short top = 16;

    /**
     * Espace à laisser entre le bord gauche de la la fenêtre et le bord gauche de la légende.
     */
    private short left = 16;

    /**
     * Espace à laisser entre le bas de la fenêtre et le bas de la légende.
     */
    private short bottom = 16;

    /**
     * Espace à laisser entre le bord droit de la la fenêtre et le bord droit de la légende.
     */
    private short right = 16;

    /**
     * Construit une légende placée par défaut dans le coin supérieur droit
     * ({@link LegendPosition#NORTH_EAST}) avec un espace de 16 pixels par
     * rapport aux bords de la fenêtre.
     */
    public RenderedLegend() {
    }

    /**
     * Retourne la position de la légende.
     */
    public LegendPosition getPosition() {
        return position;
    }

    /**
     * Défini la position où placer la légende. Cette position sera un des
     * bords de la fenêtre qui contient la carte.
     */
    public void setPosition(final LegendPosition position) {
        if (position == null) {
            throw new IllegalArgumentException();
        }
        synchronized (getTreeLock()) {
            this.position = position;
        }
    }

    /**
     * Retourne le nombre de pixels à laisser tout le tour de la légende.
     */
    public Insets getMargin() {
        synchronized (getTreeLock()) {
            return new Insets(top, left, bottom, right);
        }
    }

    /**
     * Défini le nombre de pixels à laisser tout le tour de la légende.
     * Cet espace sert à éviter que la légende soit complètement collée
     * sur le bord de la fenêtre qui contient la carte.
     */
    public void setMargin(final Insets insets) {
        synchronized (getTreeLock()) {
            top    = (short) insets.top;
            left   = (short) insets.left;
            bottom = (short) insets.bottom;
            right  = (short) insets.right;
        }
    }

    /**
     * Modifie la transformation affine en vue de positionner la légende.
     * Cette méthode est conçue pour être appellée par les classes dérivées
     * au moment du traçage de la légende. L'exemple ci-dessous indique quel
     * est le code minimal que devrait contenir la méthode {@link #paint} des
     * classes dérivées:</p>
     *
     * <blockquote><pre>
     * &nbsp;protected Shape paint(RenderingContext context) throws TransformException {
     * &nbsp;    Graphics2D graphics = context.getGraphics();
     * &nbsp;    AffineTransform currentTr = graphics.getTransform();
     * &nbsp;    context.setCoordinateSystem(context.textCS);
     * &nbsp;    Rectangle bounds = new Rectangle(0, 0, <font face="Arial" size=2><i>legend_width</i></font>, <font face="Arial" size=2><i>legend_height</i></font>);
     * &nbsp;    <strong>translate(context, bounds, graphics);</strong>
     * &nbsp;    // <font face="Arial" size=2>draw legend now ...</font>
     * &nbsp;
     * &nbsp;    graphics.setTransform(currentTr);
     * &nbsp;    context.addPaintedArea(bounds, context.textCS);
     * &nbsp;}
     * </pre></blockquote>
     *
     * @param  context Groupe d'informations nécessaires au traçage de la légende. Ce
     *         paramètre aura été automatiquement fournis à la méthode {@link #paint}
     *         et doit être transmis tel quel à cette méthode <code>translate(...)</code>.
     * @param  bounds Les dimensions de la légende, en pixels. En entré, les champs
     *         {@link Rectangle#x} et {@link Rectangle#y} sont généralement (mais pas
     *         obligatoirement) 0. A la sortie, on leur aura additionné la translation
     *         qui a été appliquée sur le graphique et augmenté de 1 pixel les largeur
     *         et hauteur.
     * @param  graphics The graphics on which to apply the translation, or <code>null</code>
     *         for none.
     * @throws TransformException if a transformation was required and failed.
     */
    final void translate(final RenderingContext context,
                         final Rectangle2D      bounds,
                         final Graphics2D       toTranslate)
            throws TransformException
    {
        double dx, dy;
        final double width  = bounds.getWidth();
        final double height = bounds.getHeight();
        final Rectangle  ws = context.getPaintingArea(context.textCS).getBounds();
        switch (position.getHorizontalAlignment()) {
            case SwingConstants.LEFT:   dx=left; break;
            case SwingConstants.RIGHT:  dx=ws.width-(width+right); break;
            case SwingConstants.CENTER: dx=((ws.width-(width+right+left))*0.5)+left; break;
            default: throw new IllegalStateException();
        }
        switch (position.getVerticalAlignment()) {
            case SwingConstants.TOP:    dy=top; break;
            case SwingConstants.BOTTOM: dy=ws.height-(height+bottom); break;
            case SwingConstants.CENTER: dy=((ws.height-(height+bottom+top))*0.5)+top; break;
            default: throw new IllegalStateException();
        }
        if (toTranslate != null) {
            toTranslate.translate(dx-bounds.getX(), dy-bounds.getY());
        }
        bounds.setRect(dx, dy, width+1, height+1);
        // No addition of (x,y) in 'bounds', since (x,y) were
        // taken in account in Graphics2D.translate(...) above.
    }

    /**
     * Retourne un rectangle englobant le texte spécifié. Le rectangle sera positionnée
     * relativement à une coordonnée (<var>x</var>,<var>y</var>) spécifiée en pixels.
     * Cette méthode attend en argument un objet {@link GlyphVector} qui représente le
     * texte à positionner. Cet objet {@link GlyphVector} peut être obtenu à partir d'une
     * chaine de caractères {@link String} à l'aide du code ci-dessous:
     *
     * <pre>
     * {@link java.awt.Font} font=graphics.{@link java.awt.Graphics2D#getFont() getFont()};
     * {@link java.awt.font.FontRenderContext} fontContext=graphics.{@link java.awt.Graphics2D#getFontRenderContext() getFontRenderContext()};
     * glyphs=font.{@link java.awt.Font#createGlyphVector createGlyphVector}(fontContext, string);
     * </pre>
     *
     * Cette méthode retourne un rectangle <code>bounds</code> qui englobe le texte. A l'aide
     * de ce rectangle, on peut écrire le texte <code>glyphs</code> avec le code ci-dessous:
     *
     * <pre>
     * graphics.drawGlyphVector(glyphs, (float) bounds.getMinX(), (float) bounds.getMaxY());
     * </pre>
     *
     * @param glyphs   Texte dont on veut le rectangle.
     * @param x        Coordonnée <var>x</var> par rapport à laquelle positionner le texte.
     * @param y        Coordonnée <var>y</var> par rapport à laquelle positionner le texte.
     * @param position Position du texte par rapport à la coordonnée (<var>x</var>,<var>y</var>).
     *
     * @return Un rectangle englobant le texte.
     * @throws IllegalArgumentException Si l'argument <code>position</code>
     *         n'est pas un des quadrans valides.
     */
    static Rectangle2D getVisualBounds(final GlyphVector glyphs, double x, double y,
                                       final LegendPosition position) {
        final Rectangle2D bounds = glyphs.getVisualBounds();
        final double height = bounds.getHeight();
        final double width  = bounds.getWidth();
        switch (position.getHorizontalAlignment()) {
            case SwingConstants.LEFT:   x -=     width; break;
            case SwingConstants.CENTER: x -= 0.5*width; break;
            case SwingConstants.RIGHT:                  break;
            default: throw new IllegalStateException();
        }
        switch (position.getVerticalAlignment()) {
            case SwingConstants.TOP:    y -=     height; break;
            case SwingConstants.CENTER: y -= 0.5*height; break;
            case SwingConstants.BOTTOM:                  break;
            default: throw new IllegalStateException();
        }
        bounds.setRect(x, y, width, height);
        return bounds;
    }
}
