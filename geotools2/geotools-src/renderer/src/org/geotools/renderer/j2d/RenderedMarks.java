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

// Geometry
import java.awt.Shape;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Font;
import java.awt.font.GlyphVector;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.PathIterator;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;

// Graphics
import java.awt.Color;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.Graphics2D;
import java.awt.BasicStroke;
import javax.swing.Action;

// Geotools dependencies
import org.geotools.cs.CoordinateSystem;
import org.geotools.ct.MathTransform2D;
import org.geotools.ct.TransformException;
import org.geotools.units.Unit;
import org.geotools.resources.XMath;
import org.geotools.resources.XArray;
import org.geotools.resources.Utilities;
import org.geotools.resources.CTSUtilities;
import org.geotools.resources.XAffineTransform;


/**
 * A set of marks and/or labels to be rendered. Marks can have different sizes and orientations
 * (for example a field of wind arrows). This abstract class is not a container for marks.
 * Subclasses must override the {@link #getMarkIterator} method in order to returns informations
 * about marks.
 *
 * @version $Id: RenderedMarks.java,v 1.8 2003/03/15 12:58:15 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public abstract class RenderedMarks extends RenderedLayer {
    /**
     * Default color for marks.
     */
    static final Color DEFAULT_COLOR = new Color(102, 102, 153, 192);

    /**
     * Projection cartographique utilisée la
     * dernière fois pour obtenir les données.
     */
    private transient MathTransform2D lastProjection;

    /**
     * Transformation affine utilisée la dernière fois.
     * Cette information est utilisée pour savoir si on
     * peut réutiliser {@link #transformedShapes}.
     */
    private transient AffineTransform lastTransform;

    /**
     * Formes géométriques transformées utilisées la dernière fois.
     * Ces formes seront réutilisées autant que possible plutôt que
     * d'être constamment recalculées.
     */
    private transient Shape[] transformedShapes;

    /**
     * Boîte englobant toutes les coordonnées des formes apparaissant
     * dans {@link #transformedShapes}. Les coordonnées de cette boîte
     * seront en pixels.
     */
    private transient Rectangle shapeBoundingBox;

    /**
     * Typical amplitude of marks, or 0 or {@link Double#NaN} if it need to be recomputed.
     * This value is computed by {@link #getTypicalAmplitude} and cached here for faster
     * access. The default implementation computes the Root Mean Square (RMS) value of all
     * {@linkplain MarkIterator#amplitude marks amplitude}.
     *
     * Note: this field is read and write by {@link RenderedGridMarks}, which overrides
     * {@link #getTypicalAmplitude}.
     */
    transient double typicalAmplitude;

    /**
     * Construct a new layer of marks.
     */
    public RenderedMarks() {
        super();
    }

    /**
     * Returns the number of marks. <strong>Note: this method is a temporary hack and will
     * be removed in a future version.</strong>
     *
     * @task TODO: Make this method package-privated and rename it "guessCount".
     *             The actual count will be fetched from the MarkIterator.
     */
    protected abstract int getCount();

    /**
     * Returns an iterator for iterating through the marks.
     * This iterator doesn't need to be thread-safe.
     */
    public abstract MarkIterator getMarkIterator();

    /**
     * Returns the units for {@linkplain MarkIterator#amplitude marks amplitude}, or
     * <code>null</code> if unknow. All marks must use the same units. The default
     * implementation returns always <code>null</code>.
     */
    public Unit getAmplitudeUnit() {
        return null;
    }

    /**
     * Returns the typical amplitude of marks. The default implementation computes the <cite>Root
     * Mean Square</cite> (RMS) value of all {@linkplain MarkIterator#amplitude marks amplitude}.
     *
     * This information is used with mark's {@linkplain MarkIterator#amplitude amplitude} and
     * {@linkplain MarkIterator#markShape shape} in order to determine how big they should be
     * rendered. Marks with an {@linkplain MarkIterator#amplitude amplitude} equals to the
     * typical amplitude will be rendered with their {@linkplain MarkIterator#markShape shape}
     * unscaled. Other marks will be rendered with scaled versions of their shapes.
     */
    public double getTypicalAmplitude() {
        synchronized (getTreeLock()) {
            if (!(typicalAmplitude>0)) {
                int n=0;
                double rms=0;
                for (final MarkIterator it=getMarkIterator(); it.next();) {
                    final double v = it.amplitude();
                    if (!Double.isNaN(v)) {
                        rms += v*v;
                        n++;
                    }
                }
                typicalAmplitude = (n>0) ? Math.sqrt(rms/n) : 1;
            }
            return typicalAmplitude;
        }
    }

    /**
     * Dessine la forme géométrique spécifiée. Cette méthode est appellée automatiquement par la
     * méthode {@link #paint(RenderingContext)}. Les classes dérivées peuvent la redéfinir si
     * elles veulent modifier la façon dont les marques sont dessinées. Cette méthode reçoit
     * en argument une forme géométrique <code>shape</code> à dessiner dans <code>graphics</code>.
     * Les rotations, translations et facteurs d'échelles nécessaires pour bien représenter la
     * marque auront déjà été pris en compte. Le graphique <code>graphics</code> a déja reçu la
     * transformation affine appropriée. L'implémentation par défaut ne fait qu'utiliser le
     * pseudo-code suivant:
     *
     * <blockquote><pre>
     * graphics.setColor(<var>defaultColor</var>);
     * graphics.fill(shape);
     * </pre></blockquote>
     *
     * @param graphics Graphique à utiliser pour tracer la marque. L'espace de coordonnées
     *                 de ce graphique sera les pixels ou les points (1/72 de pouce).
     * @param shape    Forme géométrique représentant la marque à tracer.
     * @param iterator The iterator used for computing <code>shape</code>. This method can
     *                 query properties like the {@linkplain MarkIterator#position position},
     *                 the {@linkplain MarkIterator#amplitude amplitude}, etc. However, it
     *                 should <strong>not</strong> moves the iterator (i.e. do not invoke
     *                 any {@link MarkIterator#next} method).
     */
    protected void paint(final Graphics2D graphics, final Shape shape, final MarkIterator iterator)
    {
        graphics.setColor(DEFAULT_COLOR);
        graphics.fill(shape);
    }

    /**
     * Retourne les indices qui correspondent aux coordonnées spécifiées.
     * Ces indices seront utilisées par {@link MarkIterator#visible(Rectangle)}
     * pour vérifier si un point est dans la partie visible. Cette méthode
     * sera redéfinie par {@link RenderedGridMarks}.
     *
     * @param visibleArea Coordonnées logiques de la région visible à l'écran.
     */
    Rectangle getUserClip(final Rectangle2D visibleArea) {
        return null;
    }

    /**
     * Fait en sorte que {@link #transformedShapes} soit non-nul et ait
     * exactement la longueur nécessaire pour contenir toutes les formes
     * géométriques des marques. Si un nouveau tableau a dû être créé,
     * cette méthode retourne <code>true</code>. Si l'ancien tableau n'a
     * pas été modifié parce qu'il convenait déjà, alors cette méthode
     * retourne <code>false</code>.
     */
    private boolean validateShapesArray(final int shapesCount) {
        if (transformedShapes==null || transformedShapes.length!=shapesCount) {
            transformedShapes = new Shape[shapesCount];
            return true;
        }
        return false;
    }

    /**
     * Procède au traçage des marques de cette couche. Les classes dérivées ne
     * devraient pas avoir besoin de redéfinir cette méthode. Pour modifier la
     * façon de dessiner les marques, redéfinissez plutôt une des méthodes
     * énumérées dans la section "voir aussi" ci-dessous.
     *
     * @throws TransformException if a coordinate transformation was required and failed.
     *
     * @see MarkIterator#visible
     * @see MarkIterator#position
     * @see MarkIterator#geographicArea
     * @see MarkIterator#markShape
     * @see MarkIterator#direction
     * @see MarkIterator#amplitude
     * @see #getTypicalAmplitude
     * @see #getAmplitudeUnit
     * @see #paint(Graphics2D, Shape, MarkIterator)
     */
    protected void paint(final RenderingContext context) throws TransformException {
        assert Thread.holdsLock(getTreeLock());
        final Graphics2D        graphics = context.getGraphics();
        final AffineTransform fromWorld  = context.getAffineTransform(context.mapCS, context.textCS);
        final AffineTransform fromPoints = context.getAffineTransform(context.textCS, context.deviceCS);
        final Rectangle   zoomableBounds = context.getPaintingArea(context.textCS).getBounds();
        final int                  count = getCount();
        if (count != 0) {
            final MarkIterator iterator = getMarkIterator();
            /*
             * Vérifie si la transformation affine est la même que la dernière fois. Si ce n'est
             * pas le cas, alors on va recréer une liste de toutes les formes géométriques
             * transformées. Cette liste servira à la fois à tracer les flèches et, plus tard,
             * à déterminer si le curseur de la souris traîne sur l'une d'entre elles. Certains
             * éléments peuvent être nuls s'ils n'apparaissent pas dans la zone de traçage.
             */
            final MathTransform2D projection = (MathTransform2D)
                    context.getMathTransform(getCoordinateSystem(), context.mapCS);
            if (validateShapesArray(count) || !Utilities.equals(projection, lastProjection) ||
                                              !Utilities.equals(fromWorld,  lastTransform))
            {
                shapeBoundingBox = null;
                lastProjection   = projection;
                lastTransform    = fromWorld;
                Rectangle userClip;
                try {
                    Rectangle2D visibleArea;
                    visibleArea = XAffineTransform.inverseTransform(fromWorld, zoomableBounds, null);
                    visibleArea = CTSUtilities.transform((MathTransform2D)projection.inverse(),
                                                          visibleArea, visibleArea);
                    userClip = getUserClip(visibleArea);
                } catch (NoninvertibleTransformException exception) {
                    userClip = null;
                } catch (TransformException exception) {
                    userClip = null;
                }
                /*
                 * On veut utiliser une transformation affine identité (donc en utilisant
                 * une échelle basée sur les pixels plutôt que les coordonnées utilisateur),
                 * mais en utilisant la même rotation que celle qui a cours dans la matrice
                 * <code>fromWorld</code>. On peut y arriver en utilisant l'identité ci-dessous:
                 *
                 *    [ m00  m01 ]     m00² + m01²  == constante sous rotation
                 *    [ m10  m11 ]     m10² + m11²  == constante sous rotation
                 */
                double scale;
                final double[] matrix = new double[6];
                fromWorld.getMatrix(matrix);
                scale = XMath.hypot(matrix[0], matrix[2]);
                matrix[0] /= scale;
                matrix[2] /= scale;
                scale = XMath.hypot(matrix[1], matrix[3]);
                matrix[1] /= scale;
                matrix[3] /= scale;
                /*
                 * Initialise quelques variables qui
                 * serviront dans le reste de ce bloc...
                 */
                final double typicalScale = getTypicalAmplitude();
                final AffineTransform tr  = new AffineTransform();
                double[] array            = new double[32];
                double[] buffer           = new double[32];
                int   [] X                = new int   [16];
                int   [] Y                = new int   [16];
                int      pointIndex       = 0;
                int      shapeIndex       = 0;
                Shape    lastShape        = null;
                boolean  shapeIsPolygon   = false;
                /*
                 * Balaie les données de chaques marques. Pour chacune d'elles,
                 * on définira une transformation affine qui prendra en compte
                 * les translations et rotations de la marque. Cette transformation
                 * servira à transformer les coordonnées de la marque "modèle" en
                 * coordonnées pixels propres à chaque marque.
                 */
                while (iterator.next()) {
                    if (!iterator.visible(userClip)) {
                        transformedShapes[shapeIndex++] = null;
                        continue;
                    }
                    final AffineTransform fromShape;
                    Shape shape = iterator.geographicArea();
                    if (shape != null) {
                        /*
                         * Si l'utilisateur a définit une étendue géographique
                         * pour cette marque,  alors la forme de cette étendue
                         * sera transformée et utilisée telle quelle.
                         */
                        shape = projection.createTransformedShape(shape);
                        fromShape = fromWorld;
                    } else {
                        /*
                         * Si l'utilisateur a définit la forme d'une marque en pixels,
                         * alors cette marque sera translatée à la coordonnées voulue,
                         * puis une rotation sera appliquée en fonction du zoom actuel
                         * et de l'angle spécifié par {@link #getDirection}.
                         */
                        Point2D point;
                        if ((point=iterator.position ())==null ||
                            (shape=iterator.markShape())==null)
                        {
                            transformedShapes[shapeIndex++] = null;
                            continue;
                        }
                        point = projection.transform(point, point);
                        matrix[4] = point.getX();
                        matrix[5] = point.getY();
                        fromWorld.transform(matrix, 4, matrix, 4, 1);
                        tr.setTransform(matrix[0], matrix[1], matrix[2], matrix[3], matrix[4], matrix[5]);
                        scale = iterator.amplitude()/typicalScale;
                        tr.scale(scale,scale);
                        tr.rotate(iterator.direction());
                        fromShape = tr;
                    }
                    /*
                     * A ce stade, on dispose maintenant 1) De la forme géométrique d'une
                     * marque et 2) de la transformation affine à appliquer sur la forme.
                     * Vérifie maintenant si la forme est un polygone (c'est-à-dire si ses
                     * points sont reliés uniquement par des lignes droites). Si c'est le cas,
                     * un traitement spécial sera possible. Dans tous les cas, on conservera
                     * le résultat dans une cache interne afin d'éviter d'avoir à refaire ces
                     * calculs lors du prochain traçage.
                     */
                    if (shape != lastShape) {
                        lastShape      = shape;
                        shapeIsPolygon = false;
                        final PathIterator pit = shape.getPathIterator(null);
                        if (!pit.isDone() && pit.currentSegment(array)==PathIterator.SEG_MOVETO) {
                            pointIndex = 2;
testPolygon:                for (pit.next(); !pit.isDone(); pit.next()) {
                                switch (pit.currentSegment(buffer)) {
                                    case PathIterator.SEG_LINETO: {
                                        if (pointIndex >= array.length) {
                                            array = XArray.resize(array, 2*pointIndex);
                                        }
                                        System.arraycopy(buffer, 0, array, pointIndex, 2);
                                        pointIndex += 2;
                                        continue testPolygon;
                                    }
                                    case PathIterator.SEG_CLOSE: {
                                        pit.next();
                                        shapeIsPolygon = pit.isDone();
                                        break testPolygon;
                                    }
                                    default: {
                                        // The shape is not a polygon.
                                        // Break the 'for' loop now.
                                        break testPolygon;
                                    }
                                }
                            }
                        }
                    }
                    /*
                     * Les coordonnées de la forme géométrique ayant été obtenue,
                     * créé une forme géométrique transformée (c'est-à-dire dont
                     * les coordonnées seront exprimées en pixels au lieu d'être
                     * en mètres).
                     */
                    final Shape transformedShape;
                    if (!shapeIsPolygon) {
                        // La méthode 'createTransformedShape' crée généralement un objet
                        // 'GeneralPath', qui peut convenir mais qui est quand même un peu
                        // lourd. Si possible, on va plutôt utiliser le code du bloc suivant,
                        // qui créera un objet 'Polygon'.
                        transformedShape = fromShape.createTransformedShape(shape);
                    } else {
                        if (pointIndex > buffer.length) {
                            buffer = XArray.resize(buffer, pointIndex);
                        }
                        final int length = pointIndex/2;
                        fromShape.transform(array, 0, buffer, 0, length);
                        if (length > X.length) X=XArray.resize(X, length);
                        if (length > Y.length) Y=XArray.resize(Y, length);
                        for (int j=0; j<length; j++) {
                            final int k = (j*2);
                            X[j] = (int) Math.round(buffer[k+0]);
                            Y[j] = (int) Math.round(buffer[k+1]);
                        }
                        transformedShape = new Polygon(X,Y,length);
                    }
                    /*
                     * Construit un rectangle qui englobera toutes
                     * les marques. Ce rectangle sera utilisé par
                     * {@link MapPanel} pour détecter quand la souris
                     * traîne dans la région...
                     */
                    transformedShapes[shapeIndex++] = (transformedShape.intersects(zoomableBounds))
                                                    ? transformedShape : null;
                    final Rectangle bounds = transformedShape.getBounds();
                    if (shapeBoundingBox == null) {
                        shapeBoundingBox = bounds;
                    } else {
                        shapeBoundingBox.add(bounds);
                    }
                }
            }
            /*
             * Procède maintenant au traçage de
             * toutes les marques de la couche.
             */
            final AffineTransform graphicsTr = graphics.getTransform();
            final Stroke          oldStroke  = graphics.getStroke();
            final Paint           oldPaint   = graphics.getPaint();
            try {
                int shapeIndex=0;
                iterator.seek(-1);
                graphics.setTransform(fromPoints);
                graphics.setStroke(DEFAULT_STROKE);
                final Rectangle clip = graphics.getClipBounds();
                while (iterator.next()) {
                    final Shape shape = transformedShapes[shapeIndex++];
                    if (shape!=null && (clip==null || shape.intersects(clip))) {
                        paint(graphics, shape, iterator);
                    }
                }
            } finally {
                graphics.setTransform(graphicsTr);
                graphics.setStroke(oldStroke);
                graphics.setPaint(oldPaint);
            }
        }
        context.addPaintedArea(shapeBoundingBox, context.textCS);
    }

    /**
     * Indique que cette couche a besoin d'être redéssinée. Cette méthode
     * <code>repaint()</code> peut être appelée à partir de n'importe quel
     * thread (pas nécessairement celui de <cite>Swing</cite>).
     */
    public void repaint() {
        synchronized (getTreeLock()) {
            clearCache();
        }
        super.repaint();
    }

    /**
     * Déclare que la marque spécifiée a besoin d'être redessinée.
     * Cette méthode peut être utilisée pour faire apparaître ou
     * disparaître une marque, après que sa visibilité (telle que
     * retournée par {@link MarkIterator#visible}) ait changée.
     *
     * Si un nombre restreint de marques sont à redessiner, cette
     * méthode sera efficace car elle provoquera le retraçage d'une
     * portion relativement petite de la carte. Si toutes les marques
     * sont à redessiner, il peut être plus efficace d'appeller {@link
     * #repaint()}.
     */
    public void repaint(final int index) {
        synchronized (getTreeLock()) {
            if (transformedShapes != null) {
                final Shape shape = transformedShapes[index];
                if (shape != null) {
                    repaint(shape.getBounds());
                    return;
                }
            }
            repaint();
        }
    }

    /**
     * Efface des informations qui avaient été conservées dans une mémoire cache.
     * Cette méthode est automatiquement appelée lorsqu'il a été déterminé que cette
     * couche ne sera plus affichée avant un certain temps.
     */
    void clearCache() {
        assert Thread.holdsLock(getTreeLock());
        lastTransform     = null;
        lastProjection    = null;
        transformedShapes = null;
        shapeBoundingBox  = null;
        typicalAmplitude  = Double.NaN;
        super.clearCache();
    }




    /////////////////////////////////////////////////////////////////////////////////////////////
    ////////////    EVENTS (note: may be moved out of this class in a future version)    ////////
    /////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Temporary point for mouse events.
     */
    private transient Point2D point;

    /**
     * Retourne le texte à afficher dans une bulle lorsque le curseur
     * de la souris traîne sur la carte. L'implémentation par défaut
     * identifie la marque sur laquelle traîne le curseur et appelle
     * {@link MarkIterator#getToolTipText()}.
     *
     * @param  event Coordonnées du curseur de la souris.
     * @return Le texte à afficher lorsque la souris traîne sur cet élément.
     *         Ce texte peut être nul pour signifier qu'il ne faut pas en écrire.
     */
    final String getToolTipText(final GeoMouseEvent event) {
        synchronized (getTreeLock()) {
            final Shape[] transformedShapes = RenderedMarks.this.transformedShapes;
            if (transformedShapes != null) {
                MarkIterator iterator = null;
                final Point2D point = this.point = event.getPixelCoordinate(this.point);
                for (int i=transformedShapes.length; --i>=0;) {
                    final Shape shape = transformedShapes[i];
                    if (shape != null) {
                        if (shape.contains(point)) {
                            if (iterator == null) {
                                iterator = getMarkIterator();
                            }
                            iterator.seek(i);
                            final String text = iterator.getToolTipText(event);
                            if (text != null) {
                                return text;
                            }
                        }
                    }
                }
            }
        }
        return super.getToolTipText(event);
    }
}
