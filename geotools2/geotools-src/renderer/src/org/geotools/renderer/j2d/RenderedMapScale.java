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
import java.awt.Font;
import java.awt.Paint;
import java.awt.Color;
import java.awt.Stroke;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.AffineTransform;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.text.FieldPosition;
import java.text.NumberFormat;

// Geotools dependencies
import org.geotools.units.Unit;
import org.geotools.units.UnitException;
import org.geotools.cs.Ellipsoid;
import org.geotools.cs.AxisOrientation;
import org.geotools.cs.CoordinateSystem;
import org.geotools.ct.MathTransform2D;
import org.geotools.ct.TransformException;
import org.geotools.resources.XMath;
import org.geotools.resources.CTSUtilities;
import org.geotools.resources.renderer.Resources;
import org.geotools.resources.renderer.ResourceKeys;


/**
 * A map scale to paint over other layers.
 *
 * @version $Id: RenderedMapScale.java,v 1.1 2003/03/11 12:34:39 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public class RenderedMapScale extends RenderedLegend {
    /**
     * Unité à utiliser pour représenter les distances.
     * La valeur par défaut sera des kilomètres ("km").
     */
    private Unit units = Unit.KILOMETRE;

    /**
     * The format to use for formatting numbers.
     */
    private NumberFormat format;

    /**
     * <code>true</code> if the map scale is horizontal, or <code>false</code> if it is vertical.
     *
     * @task TODO: Only horizontal map scale has been texted up to now. Implementation for
     *             vertical map scale is not finished.
     */
    private static final boolean horizontal = true;

    /**
     * Nombre de sous-divisions à placer dans l'échelle. Ces sous-divisions sont
     * des rectangles plus petits qui apparaissent dans le rectangle principal.
     */
    private int subDivisions = 3;

    /**
     * Longueur désirée de l'échelle, en pixels.  La longueur exacte dépendra du facteur
     * de zoom de la carte; cette classe tentera de donnéer à l'échelle une longueur qui
     * correspond à un chiffre rond.
     */
    private int preferredLength = 256;

    /**
     * Épaisseur de l'échelle en pixels. Cette épaisseur sera
     * constante quelle que soit la longueur de l'échelle.
     */
    private int thickness = 10;

    /**
     * Nombre de pixels de la barre noir à l'intérieur des rectangles blancs.
     */
    private int thicknessSub = 3;

    /**
     * Couleur du texte des graduations et de l'étiquette de l'échelle.
     */
    private Paint textColor = Color.WHITE;

    /**
     * Couleur des rectangles extérieurs de l'échelle.
     */
    private Paint outerRectColor = new Color(215, 235, 255);

    /**
     * Couleur des rectangles intérieurs de l'échelle.
     */
    private Paint innerRectColor = Color.BLACK;

    /**
     * Couleur de l'arrière plan de l'échelle. Cette couleur sert principalement à des fins de
     * déboguages. Ce champ peut être nul pour signifier qu'on ne veut pas d'arrière plan.
     */
    private Paint backgroundColor = null;

    /**
     * Construit une échelle qui sera située par défaut dans le coin
     * inférieur gauche de la carte. La position de l'échelle peut être
     * modifié par la méthode {@link #setPosition}.
     */
    public RenderedMapScale() {
        setPosition(LegendPosition.SOUTH_WEST);
        setMargin(new Insets(8,16,8,16)); // top, left, bottom, right
    }

    /**
     * Retourne les unités qui seront utilisées pour exprimer les distances
     * sur l'échelle. Par défaut, les distances seront exprimées en kilomètres.
     *
     * @return Les units de l'échelle (kilomètres par défaut).
     */
    public Unit getUnits() {
        return units;
    }

    /**
     * Modifie les unités à utiliser pour exprimer les distances sur l'échelle.
     *
     * @param  units Nouvelles unités à utiliser pour exprimer
     *         les distances. Cet argument ne doit pas être nul.
     * @throws UnitException si les unités spécifiées ne sont pas valides.
     */
    public void setUnits(final Unit units) throws UnitException {
        if (units==null || !Unit.METRE.canConvert(units)) {
            throw new UnitException(Resources.getResources(getLocale()).getString(
                                    ResourceKeys.ERROR_BAD_ARGUMENT_$2, "units", units));
        }
        final Unit old;
        synchronized (getTreeLock()) {
            old = this.units;
            this.units = units;
            repaint();
        }
        listeners.firePropertyChange("units", old, units);
    }

    /**
     * Retourne la longueur désirée de l'échelle, en points. Lors des affichages à l'écran,
     * les points correspondent aux pixels. La longueur exacte dépendra du facteur de zoom
     * de la carte;  la classe <code>RenderedMapScale</code> tentera de donner à l'échelle
     * une longueur qui correspond à un chiffre rond en mètres sur le terrain.
     *
     * @return The preferred length for the map scale, in dots.
     */
    public int getPreferredLength() {
        return preferredLength;
    }

    /**
     * Modifie la longueur désirée de l'échelle, en pixels.
     *
     * @param preferredLength The preferred length for the map scale, in dots.
     */
    public void setPreferredLength(final int preferredLength) {
        final int old;
        synchronized (getTreeLock()) {
            old = this.preferredLength;
            this.preferredLength = preferredLength;
            repaint();
        }
        listeners.firePropertyChange("preferredLength", old, preferredLength);
    }

    /**
     * Retourne l'épaisseur de l'échelle en pixels. Cette épaisseur
     * restera constante quelle que soit la longueur de l'échelle.
     *
     * @return The map scale thickness, in dots.
     */
    public int getThickness() {
        return thickness;
    }

    /**
     * Modifie l'épaisseur de l'échelle en pixels. Cette épaisseur
     * restera constante quelle que soit la longueur de l'échelle.
     */
    public void setThickness(final int thickness) {
        final int old;
        synchronized (getTreeLock()) {
            old = this.thickness;
            this.thickness = thickness;
            repaint();
        }
        listeners.firePropertyChange("thickness", old, thickness);
    }

    /**
     * Retourne un titre à placer près de l'échelle, ou <code>null</code> pour
     * ne pas écrire de titre. Par défaut, cette méthode retourne le nom du
     * système de coordonnées.
     */
    public String getTitle() {
        final CoordinateSystem cs = getCoordinateSystem();
        return (cs!=null) ? cs.getName(getLocale()) : null;
    }

    /**
     * Returns the format to use for formatting numbers.
     */
    private NumberFormat getFormat() {
        if (format == null) {
            format = NumberFormat.getNumberInstance(getLocale());
        }
        return format;
    }

    /**
     * Paint an error message. This method is invoked when the rendering coordinate
     * system use unknow units.
     *
     * @param  context Information relatives to the rendering context.
     * @throws TransformException If a coordinate transformation failed
     *         during the rendering process.
     */
    private void paintError(final RenderingContext context) throws TransformException {
    }

    /**
     * Dessine l'échelle de la carte. Cette échelle sera composée d'un certains nombre de
     * rectangles disposés côtes à côtes horizontalement ou verticalement, chaque rectangle
     * représentant une distance telle que 100 mètres ou 1 km, dépendamment de l'échelle de
     * la carte.
     *
     * @param  context Information relatives to the rendering context.
     * @throws TransformException If a coordinate transformation failed
     *         during the rendering process.
     */
    protected void paint(final RenderingContext context) throws TransformException {
        assert Thread.holdsLock(getTreeLock());
        /*
         * Gets an estimation of the map scale in linear units (usually kilometers).
         * First, we get an estimation of the map scale position in screen coordinates.
         * Then, we convert the position in "real world" coordinates and measure its length
         * (using orthodromic distance computation if the rendering units were angular units).
         */
        final Rectangle bounds;
        if (horizontal) {
            bounds = new Rectangle(0, 0, preferredLength, thickness);
        } else {
            bounds = new Rectangle(0, 0, thickness, preferredLength);
        }
        translate(context, bounds, null);
        Point2D P1,P2;
        if (horizontal) {
            final double center = bounds.getCenterY();
            P1 = new Point2D.Double(bounds.getMinX(), center);
            P2 = new Point2D.Double(bounds.getMaxX(), center);
        } else {
            final double center = bounds.getCenterX();
            P1 = new Point2D.Double(center, bounds.getMinY());
            P2 = new Point2D.Double(center, bounds.getMaxY());
        }
        final CoordinateSystem mapCS = context.mapCS;
        final MathTransform2D  toMap = (MathTransform2D)
                                       context.getMathTransform(context.textCS, mapCS);
        P1 = toMap.transform(P1, P1);
        P2 = toMap.transform(P2, P2);
        final Unit mapUnitX = mapCS.getUnits(0);
        final Unit mapUnitY = mapCS.getUnits(1);
        if (mapUnitX==null || mapUnitY==null) {
            paintError(context);
            return;
        }
        double logicalLength;
        final Ellipsoid ellipsoid = CTSUtilities.getHeadGeoEllipsoid(mapCS);
        if (ellipsoid != null) {
            P1.setLocation(Unit.DEGREE.convert(P1.getX(), mapUnitX),
                           Unit.DEGREE.convert(P1.getY(), mapUnitY));
            P2.setLocation(Unit.DEGREE.convert(P2.getX(), mapUnitX),
                           Unit.DEGREE.convert(P2.getY(), mapUnitY));
            logicalLength = ellipsoid.orthodromicDistance(P1, P2);
            logicalLength = units.convert(logicalLength, ellipsoid.getAxisUnit());
        } else {
            P1.setLocation(units.convert(P1.getX(), mapUnitX),
                           units.convert(P1.getY(), mapUnitY));
            P2.setLocation(units.convert(P2.getX(), mapUnitX),
                           units.convert(P2.getY(), mapUnitY));
            logicalLength = P1.distance(P2);
        }
        final double scaleFactor = logicalLength / preferredLength;
        /*
         * "Snap" the length to some number easier to read. For example the number 2371
         * will be snapped to 2500. Then, compute the final map scale length in dots.
         */
        logicalLength /= subDivisions;
        if (true) {
            final double factor = XMath.pow10((int)Math.floor(XMath.log10(logicalLength)));
            logicalLength /= factor;
            if      (logicalLength <= 1.0) logicalLength = 1.0;
            else if (logicalLength <= 2.0) logicalLength = 2.0;
            else if (logicalLength <= 2.5) logicalLength = 2.5;
            else if (logicalLength <= 5.0) logicalLength = 5.0;
            else                           logicalLength =10.0;
            logicalLength *= factor;
        }
        final int visualLength = (int)Math.ceil(logicalLength / scaleFactor);
        /*
         * Détermine les dimensions de l'échelle. Il est nécessaire de calculer
         * à l'avance ces dimensions afin de pouvoir positionner correctement
         * la légende.
         */
        final Graphics2D       graphics = context.getGraphics();
        final AffineTransform currentTr = graphics.getTransform();
        final Stroke          oldStroke = graphics.getStroke();
        final Paint            oldPaint = graphics.getPaint();
        final Font                 font = graphics.getFont();
        if (horizontal) {
            bounds.setBounds(0, 0, visualLength*subDivisions, thickness);
        } else {
            bounds.setBounds(0, 0, thickness, visualLength*subDivisions);
        }
        try {
            context.setCoordinateSystem(context.textCS);
            final GlyphVector[]       ticksText = new GlyphVector[subDivisions+2];
            final Rectangle2D[]     ticksBounds = new Rectangle2D[subDivisions+2];
            final FontRenderContext fontContext = graphics.getFontRenderContext();
            /*
             * Crée à l'avance la numérotation de l'échelle ainsi que
             * les unités à écrire après la dernière numérotation.
             */
            if (true) {
                final StringBuffer buffer = new StringBuffer(16);
                final FieldPosition   pos = new FieldPosition(0);
                for (int i=0; i<=subDivisions; i++) {
                    String text = getFormat().format(logicalLength*i, buffer, pos).toString();
                    GlyphVector glyphs = font.createGlyphVector(fontContext, text);
                    Rectangle2D rect = getVisualBounds(glyphs, visualLength*i, thickness+3,
                                                       LegendPosition.SOUTH);
                    if (i == subDivisions) {
                        buffer.append(' ');
                        buffer.append(units);
                        text = buffer.toString();
                        glyphs = font.createGlyphVector(fontContext, text);
                        rect = getVisualBounds(glyphs, rect.getMinX(), rect.getMaxY(),
                                               LegendPosition.NORTH_EAST);
                    }
                    bounds.add(rect);
                    ticksBounds[i] = rect;
                    ticksText[i] = glyphs;
                    buffer.setLength(0);
                }
            }
            /*
             * Crée l'étiquette. Par défaut, ce sera le
             * nom de la projection cartographique en cours.
             */
            final String title = getTitle();
            if (title != null) {
                final Font        lfont   = font.deriveFont(Font.BOLD | Font.ITALIC);
                final GlyphVector lglyphs = lfont.createGlyphVector(fontContext, title);
                final Rectangle2D lbounds = getVisualBounds(lglyphs, 0.5*(subDivisions*visualLength),
                                                            -3, LegendPosition.NORTH);
                ticksText  [subDivisions+1] = lglyphs;
                ticksBounds[subDivisions+1] = lbounds;
                bounds.add(lbounds);
            }
            /*
             * Applique une translation pour positionner l'échelle à l'endroit voulu.
             * Maintenant que l'espace occupé par la légende a été déterminé, procède
             * ensuite à son traçage.
             */
            translate(context, bounds, graphics);
            if (backgroundColor != null) {
                final AffineTransform tr = graphics.getTransform();
                context.setCoordinateSystem(context.textCS);
                graphics.setPaint(backgroundColor);
                graphics.fill(bounds);
                graphics.setTransform(tr);
            }
            Rectangle2D.Double rect = new Rectangle2D.Double(0, 0, visualLength, thickness);
            graphics.setStroke(DEFAULT_STROKE);
            for (int i=0; i<subDivisions; i++) {
                graphics.setPaint(outerRectColor);
                graphics.fill(rect);
                graphics.setPaint(innerRectColor);
                graphics.draw(rect);
                if ((i&1) != 0) {
                    /*
                     * Dans un rectangle sur deux, on dessinera un
                     * rectangle noir à l'intérieur du rectangle blanc.
                     */
                    int space   = thickness-thicknessSub;
                    rect.height = thicknessSub;
                    rect.y      = space >> 1;
                    if ((space&1)!=0) rect.y++;
                    else rect.height++;
                    graphics.fill(rect);
                    rect.height=thickness;
                    rect.y = 0;
                }
                rect.x += visualLength;
            }
            /*
             * Écrit les graduations, les unités ainsi que la légende de
             * l'échelle. Tous ces textes ont été préparés à l'avance un
             * peu plus haut.
             */
            graphics.setPaint(textColor);
            for (int i=0; i<ticksText.length; i++) {
                if (ticksText[i] != null) {
                    final Rectangle2D pos = ticksBounds[i];
                    graphics.drawGlyphVector(ticksText[i], (float)pos.getMinX(),
                                                           (float)pos.getMaxY());
                }
            }
        } finally {
            graphics.setTransform(currentTr);
            graphics.setStroke(oldStroke);
            graphics.setPaint(oldPaint);
        }
        context.addPaintedArea(bounds, context.textCS);
    }
}
