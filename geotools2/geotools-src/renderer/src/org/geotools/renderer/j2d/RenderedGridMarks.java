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
import java.awt.Color;
import java.awt.Shape;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.image.RenderedImage;
import java.awt.image.Raster;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.util.Locale;

// Java Advanced Imaging dependencies
import javax.media.jai.PlanarImage;
import javax.media.jai.iterator.RectIter;
import javax.media.jai.iterator.RectIterFactory;

// Geotools dependencies
import org.geotools.units.Unit;
import org.geotools.pt.Envelope;
import org.geotools.pt.AngleFormat;
import org.geotools.cs.CoordinateSystem;
import org.geotools.ct.MathTransform;
import org.geotools.ct.MathTransform2D;
import org.geotools.ct.TransformException;
import org.geotools.ct.MathTransformFactory;
import org.geotools.cv.SampleDimension;
import org.geotools.gc.GridCoverage;
import org.geotools.renderer.geom.Arrow2D;
import org.geotools.resources.XMath;
import org.geotools.resources.Utilities;
import org.geotools.resources.CTSUtilities;
import org.geotools.resources.XAffineTransform;
import org.geotools.resources.renderer.Resources;
import org.geotools.resources.renderer.ResourceKeys;


/**
 * Renderer {@linkplain GridCoverage grid coverage} data as marks.
 * The default appearance depends on the number of bands:
 * <ul>
 *   <li>For one band, data are displayed as {@linkplain Ellipse2D circles}.
 *       Circle area are proportional to the sample value.</li>
 *   <li>For two bands, data are displayed as {@linkplain Arrow2D arrows}.
 *       Arrows sizes and direction depends of the sample values.</li>
 * </ul>
 *
 * @version $Id: RenderedGridMarks.java,v 1.4 2003/02/23 21:27:38 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public class RenderedGridMarks extends RenderedMarks {
    /**
     * Default value for the {@linkplain #getZOrder z-order}.
     */
    private static final float DEFAULT_Z_ORDER = 0;

    /**
     * The default shape for displaying data with only 1 band.
     * This shape is a circle centered at (0,0) with a radius of 5 dots.
     */
    private static final Shape DEFAULT_SHAPE_1D = RenderedMarks.DEFAULT_SHAPE;

    /**
     * Forme géométrique représentant une flèche.  Le début de cette flèche
     * est l'origine à (0,0) et sa longueur est de 10 points. La flèche est
     * pointée dans la direction des <var>x</var> positifs (soit à un angle
     * de 0 radians arithmétiques).
     */
    private static final Shape DEFAULT_SHAPE_2D = new Arrow2D(0, -2.5, 10, 5);

    /**
     * The grid coverage.
     *
     * @see #image
     * @see #mainSD
     */
    private GridCoverage coverage;

    /**
     * Image contenant les composantes <var>x</var> et <var>x</var> des vecteurs.
     */
    private PlanarImage image;

    /**
     * The number of visible bands. Should be 0, 1 or 2.
     *
     * @see #getBands
     * @see #setBands
     * @see #bandX
     * @see #bandY
     */
    private int numBands;

    /**
     * Index des bandes <var>X</var> et <var>Y</var> dans l'image.
     *
     * @see #numBands
     * @see #getBands
     * @see #setBands
     */
    private int bandX, bandY;

    /**
     * Nombre de points à décimer selon l'axe des <var>x</var> et des <var>y</var>.
     * Ce nombre doit être supérieur à 0. La valeur <code>1</code> signifie qu'aucune
     * décimation ne sera faite.
     */
    private int decimateX=1, decimateY=1;

    /**
     * Espace minimal (en points) à laisser entre les points de la grille selon les axes
     * <var>x</var> et <var>y</var>. La valeur 0 désactive la décimation selon cet axe.
     */
    private int spaceX=0, spaceY=0;

    /**
     * Indique si la décimation est active. Ce champ prend la valeur <code>true</code>
     * si <code>decimateX</code> ou <code>decimateY</code> sont supérieurs à 1.
     */
    private boolean decimate = false;

    /**
     * Indique si la décimation automatique est active. Ce champ prend la
     * valeur <code>true</code> lorsque {@link #setAutoDecimation} est
     * appellée et que <code>spaceX</code> ou <code>spaceY</code> sont
     * supérieurs à 0.
     */
    private boolean autoDecimate = false;

    /**
     * The transform from grid (<var>i</var>,<var>j</var>) to <strong>rendering</strong>
     * coordinate system (<var>x</var>,<var>y</var>).
     */
    private MathTransform2D gridToCoordinateSystem = MathTransform2D.IDENTITY;

    /**
     * Index du dernier élément dont on a obtenu les composantes X et Y du vecteur.
     */
    private transient int lastIndex = -1;

    /**
     * Indices <var>x</var> et <var>x</var> calculés
     * lors du dernier appel de {@link #computeUV}.
     */
    private transient double lastI, lastJ;

    /**
     * Composantes <var>x</var> et <var>x</var> calculées
     * lors du dernier appel de {@link #computeUV}.
     */
    private transient double lastX, lastY;

    /**
     * The typical amplitude value, or 0 if it need to be computed.
     */
    private transient double typicalAmplitude;

    /**
     * Couleur des flèches.
     */
    private Color color = DEFAULT_COLOR;

    /**
     * Procède à la lecture binaire de cet objet, puis initialise des champs internes.
     */
    private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        lastIndex = -1;
    }

    /**
     * Construct a new layer for the specified grid coverage. If the supplied grid coverage has
     * only one band, then marks will be displayed as circles with area proportional to sample
     * values. Otherwise, marks will be displayed as arrows with <var>x</var> and <var>y</var>
     * components fetched from sample dimensions (bands) 0 and 1 respectively.
     *
     * @param coverage The grid coverage, or <code>null</code> if none.
     */
    public RenderedGridMarks(final GridCoverage coverage) {
        if (coverage == null) {
            setZOrder(DEFAULT_Z_ORDER);
        } else try {
            numBands = coverage.getRenderedImage().getSampleModel().getNumBands();
            if (numBands >= 2) {
                numBands = 2;
                bandY    = 1;
            }
            setCoordinateSystem(coverage.getCoordinateSystem());
            setGridCoverage(coverage);
        } catch (TransformException exception) {
            // Should not happen for most GridCoverage instances.
            // However, it may occurs in some special cases.
            final IllegalArgumentException e;
            e = new IllegalArgumentException(exception.getLocalizedMessage());
            e.initCause(exception);
            throw e;
        }
    }

    /**
     * Set the rendering coordinate system for this layer.
     *
     * @param  cs The coordinate system. If the specified coordinate system has more than
     *            two dimensions, then it must be a {@link CompoundCoordinateSystem} with
     *            a two dimensional {@link CompoundCoordinateSystem#getHeadCS headCS}.
     * @throws TransformException If <code>cs</code> can't be reduced to a two-dimensional
     *         coordinate system, or if this method do not accept the new coordinate system
     *         for some other reason. In case of failure, this method should keep the old CS
     *         and leave this layer in a consistent state.
     */
    protected void setCoordinateSystem(CoordinateSystem cs) throws TransformException {
        synchronized (getTreeLock()) {
            final MathTransform2D candidate;
            if (coverage!=null) {
                candidate = getGridToCoordinateSystem(coverage, cs, "setCoordinateSystem");
            } else {
                candidate = MathTransform2D.IDENTITY;
            }
            super.setCoordinateSystem(cs);
            gridToCoordinateSystem = candidate;
        }
    }
 
    /**
     * Compute the transformation for grid to <strong>rendering</strong>'s coordinate system,
     * which may not be the same than the coverage's coordinate system. This transformation is
     * a two steps process:
     *
     * <blockquote><pre>
     * grid   -->  coverage CS  -->  rendering CS
     * </pre></blockquote>
     *
     * The first step is usually an affine transform. The transform is translated
     * by <code>-min(x,y)</code> in order to locate the first indices to (0,0).
     * The second step can be an arbitrary two-dimensional transform.
     *
     * This method do not change any <code>RenderedGridMarks</code> internal state.
     *
     * @param  coverage The coverage for which to compute a transformation.
     * @param  mapCS The rendering coordinate system.
     * @param  sourceMethodName The source method name, for logging purpose.
     * @return The <code>gridToCoordinateSystem</code> transform.
     * @throws TransformException if the transform can't be created.
     */
    private MathTransform2D getGridToCoordinateSystem(final GridCoverage coverage,
                                                      final CoordinateSystem mapCS,
                                                      final String sourceMethodName)
            throws TransformException
    {
        final RenderedImage image = coverage.getRenderedImage();
        final MathTransform step1, step2, step3;
        final MathTransformFactory factory;
        if (renderer != null) {
            factory = renderer.getMathTransformFactory();
            step3   = (MathTransform2D) renderer.getMathTransform(
                        CTSUtilities.getCoordinateSystem2D(coverage.getCoordinateSystem()),
                        mapCS, "RenderedGridMarks", sourceMethodName);
        } else {
            factory = MathTransformFactory.getDefault();
            step3   = MathTransform2D.IDENTITY;
        }
        step2 = coverage.getGridGeometry().getGridToCoordinateSystem2D();
        step1 = factory.createAffineTransform(
                AffineTransform.getTranslateInstance(-image.getMinX(), -image.getMinY()));
        return (MathTransform2D) factory.createConcatenatedTransform(
                                 factory.createConcatenatedTransform(step1, step2), step3);
    }

    /**
     * Set the grid coverage for this layer.
     *
     * @param  coverage The grid coverage, or <code>null</code> if none.
     * @throws TransformException is a transformation was required and failed.
     */
    public void setGridCoverage(GridCoverage coverage) throws TransformException {
        final GridCoverage oldCoverage;
        synchronized (getTreeLock()) {
            oldCoverage = this.coverage;
            final Rectangle2D preferredArea;
            float zOrder = DEFAULT_Z_ORDER;
            if (coverage == null) {
                preferredArea = null;
                clearCoverage();
            } else {
                coverage = coverage.geophysics(true);
                final SampleDimension[] samples = coverage.getSampleDimensions();
                if (Math.max(bandX, bandY) >= samples.length) {
                    // TODO: localize.
                    throw new IllegalArgumentException("Too few bands in the grid coverage.");
                }
                final SampleDimension sampleX = samples[bandX];
                final SampleDimension sampleY = samples[bandY];
                final Unit              unitX = sampleX.getUnits();
                final Unit              unitY = sampleY.getUnits();
                if (!Utilities.equals(unitX, unitY)) {
                    // TODO: localize.
                    throw new IllegalArgumentException("Mismatched units");
                }
                /*
                 * Change this object's state only after all checks passed.
                 */
                gridToCoordinateSystem = getGridToCoordinateSystem(coverage, getCoordinateSystem(),
                                                                   "setGridCoverage");
                this.coverage = coverage;
                this.image    = PlanarImage.wrapRenderedImage(coverage.getRenderedImage());
                this.mainSD   = sampleX;
                final Envelope envelope = coverage.getEnvelope();
                preferredArea = envelope.getSubEnvelope(0,2).toRectangle2D();
                if (envelope.getDimension()>=3) {
                    zOrder = (float)envelope.getCenter(2);
                }
            }
            setZOrder(zOrder);
            setPreferredArea(preferredArea);
        }
        listeners.firePropertyChange("gridCoverage", oldCoverage, coverage);
        repaint();
    }

    /**
     * Returns the current grid coverage.
     */
    public GridCoverage getGridCoverage() {
        return coverage;
    }

    /**
     * Set the bands to use for querying mark values.
     *
     * @param  bands The band. This array length should 0, 1 or 2. A length of 0 is equivalents
     *         to a call to <code>{@link #setVisible setVisible}(false)</code>.
     * @throws IllegalArgumentException if the array length is illegal, or if a band is greater than
     *         the number of bands in the {@linkplain #getGridCoverage underlying grid coverage}.
     */
    public void setBands(final int[] bands) throws IllegalArgumentException {
        final int[] oldBands;
        synchronized (getTreeLock()) {
            final int max = (coverage!=null) ? image.getNumBands() : Integer.MAX_VALUE;
            for (int i=0; i<bands.length; i++) {
                final int band = bands[i];
                if (band<0 || band>=max) {
                    throw new IllegalArgumentException("No such band: "+band);
                    // TODO: localize
                }
            }
            oldBands = getBands();
            switch (bands.length) {
                default: {
                    // TODO: localize
                    throw new IllegalArgumentException("Can't renderer more than 2 bands.");
                }
                case 2: {
                    bandX = bands[0];
                    bandY = bands[1];
                    break;
                }
                case 1: {
                    bandX = bandY = bands[0];
                    break;
                }
                case 0: {
                    bandX = bandY = 0;
                    setVisible(false);
                    break;
                }
            }
            numBands = bands.length;
        }
        listeners.firePropertyChange("bands", oldBands, bands);
        repaint();
    }

    /**
     * Returns the bands to use for querying mark values.
     */
    public int[] getBands() {
        synchronized (getTreeLock()) {
            switch (numBands) {
                default: throw new AssertionError(numBands); // Should not happen.
                case  2: return new int[] {bandX, bandY};
                case  1: return new int[] {bandX};
                case  0: return new int[] {};
            }
        }
    }

    /**
     * Set a decimation factor. A value greater than 1 will reduces the number of points
     * returned by {@link #getCount}, {@link #getPosition} and similar methods. Note that
     * points are not actually decimated, but rather averaged. For example a "decimation"
     * factor of 2 will average two neighbor points and replace them with new one in the
     * middle of the original points.
     *
     * @param decimateX Décimation selon <var>x</var>, ou 1 pour ne pas en faire.
     * @param decimateY Décimation selon <var>y</var>, ou 1 pour ne pas en faire.
     */
    public void setDecimation(final int decimateX, final int decimateY) {
        if (decimateX <=0) {
            throw new IllegalArgumentException(Resources.format(
                            ResourceKeys.ERROR_NOT_GREATER_THAN_ZERO_$1, new Integer(decimateX)));
        }
        if (decimateY <=0) {
            throw new IllegalArgumentException(Resources.format(
                            ResourceKeys.ERROR_NOT_GREATER_THAN_ZERO_$1, new Integer(decimateY)));
        }
        if (decimateX!=this.decimateX || decimateY!=this.decimateY) {
            synchronized (getTreeLock()) {
                autoDecimate   = false;
                this.decimateX = decimateX;
                this.decimateY = decimateY;
                decimate = (decimateX!=1 || decimateY!=1);
            }
            repaint();
        }
    }

    /**
     * Décime automatiquement les points de la grille de façon à conserver un espace
     * d'au moins <code>spaceX</code> et <code>spaceY</code> entre chaque point.
     *
     * @param spaceX Espace minimal (en points) selon <var>x</var> à laisser entre les
     *        points de la grille. La valeur 0 désactive la décimation selon cet axe.
     * @param spaceY Espace minimal (en points) selon <var>y</var> à laisser entre les
     *        points de la grille. La valeur 0 désactive la décimation selon cet axe.
     */
    public void setAutoDecimation(final int spaceX, final int spaceY) {
        if (spaceX < 0) {
            throw new IllegalArgumentException(Resources.format(
                                ResourceKeys.ERROR_BAD_ARGUMENT_$2, "spaceX", new Integer(spaceX)));
        }
        if (spaceY < 0) {
            throw new IllegalArgumentException(Resources.format(
                                ResourceKeys.ERROR_BAD_ARGUMENT_$2, "spaceY", new Integer(spaceY)));
        }
        if (spaceX!=this.spaceX || spaceY!=this.spaceY) {
            synchronized (getTreeLock()) {
                this.spaceX  = spaceX;
                this.spaceY  = spaceY;
                autoDecimate = (spaceX!=0 || spaceY!=0);
            }
            repaint();
        }
    }

    /**
     * Set the default fill color.
     */
    public void setColor(final Color color) {
        this.color = color;
    }

    /**
     * Returns the default fill color.
     */
    public Color getColor() {
        return color;
    }

    /**
     * Retourne le nombre de points de cette grille. Le nombre de point retourné
     * tiendra compte de la décimation spécifiée avec {@link #setDecimation}.
     */
    protected int getCount() {
        synchronized (getTreeLock()) {
            if (image==null || numBands==0) {
                return 0;
            }
            return (image.getWidth()/decimateX) * (image.getHeight()/decimateY);
        }
    }

    /**
     * Retourne les coordonnées (<var>x</var>,<var>y</var>) d'un point de la grille.
     * Les coordonnées <var>x</var> et <var>y</var> seront exprimées selon le système
     * de coordonnées du {@linkplain #getGridCoverage grid coverage}.
     *
     * Si une décimation a été spécifiée avec la méthode {@link #setDecimation},
     * alors la position retournée sera située au milieu des points à moyenner.
     *
     * @throws TransformException if a transform was required and failed.
     */
    protected Point2D getPosition(final int index) throws TransformException {
        assert Thread.holdsLock(getTreeLock());
        final Point2D point;
        if (!decimate) {
            final int width = image.getWidth();
            point = new Point2D.Double(index%width, index/width);
        } else {
            if (lastIndex != index) {
                computeUV(index);
            }
            point = new Point2D.Double(lastI, lastJ);
        }
        return gridToCoordinateSystem.transform(point, point);
    }

    /**
     * Retourne les unités de l'amplitude des vecteurs, ou <code>null</code>
     * si ces unités ne sont pas connues. Dans les cas des flèches de courant
     * par exemple, ça sera typiquement des "cm/s".
     */
    protected Unit getAmplitudeUnit() {
        return mainSD.getUnits();
    }

    /**
     * Returns the RMS value of all mark amplitudes.
     */
    protected double getTypicalAmplitude() {
        synchronized (getTreeLock()) {
            if (!(typicalAmplitude > 0)) {
                double sum = 0;
                int  count = 0;
                final RectIter iter = RectIterFactory.create(image, null);
                if (!iter.finishedLines()) do {
                    iter.startPixels();
                    if (!iter.finishedPixels()) do {
                        final double x = iter.getSampleDouble(bandX);
                        final double y = iter.getSampleDouble(bandY);
                        final double s = x*x + y*y;
                        if (!Double.isNaN(s)) {
                            sum += s;
                            count++;
                        }
                    }
                    while (!iter.nextPixelDone());
                } while (!iter.nextLineDone());
                if (numBands == 1) {
                    typicalAmplitude = Math.pow(sum/(2*count), 0.25);
                } else {
                    typicalAmplitude = Math.sqrt(sum/count);
                }
                if (!(typicalAmplitude > 0)) {
                    typicalAmplitude = 1;
                }
            }
            return typicalAmplitude;
        }
    }

    /**
     * Retourne l'amplitude à la position d'une marque. Si une décimation a été spécifiée avec
     * la méthode {@link #setDecimation}, alors cette méthode calcule la moyenne vectorielle
     * (la moyenne des composantes <var>x</var> et <var>y</var>) aux positions des marques à
     * décimer, et retourne l'amplitude du vecteur moyen.
     */
    protected double getAmplitude(final int index) {
        assert Thread.holdsLock(getTreeLock());
        if (lastIndex != index) {
            computeUV(index);
        }
        switch (numBands) {
            case 0:  return 0;
            case 1:  return Math.sqrt(Math.abs(lastX));
            case 2:  return XMath.hypot(lastX, lastY);
            default: throw new AssertionError(numBands);
        }
    }

    /**
     * Retourne la direction de la valeur d'une marque. Si une décimation a été spécifiée avec
     * la méthode {@link #setDecimation}, alors cette méthode calcule la moyenne vectorielle
     * (la moyenne des composantes <var>x</var> et <var>y</var>) aux positions des marques à
     * décimer, et retourne la direction du vecteur moyen.
     */
    protected double getDirection(final int index) {
        assert Thread.holdsLock(getTreeLock());
        if (lastIndex != index) {
            computeUV(index);
        }
        switch (numBands) {
            case 0:  // Fall through
            case 1:  return 0;
            case 2:  return Math.atan2(lastY, lastX);
            default: throw new AssertionError(numBands);
        }
    }

    /**
     * Calcule les composantes <var>x</var> et <var>y</var> du vecteur à l'index spécifié.
     */
    private void computeUV(final int index) {
        assert Thread.holdsLock(getTreeLock());
        int    count = 0;
        int    sumI  = 0;
        int    sumJ  = 0;
        double vectX = 0;
        double vectY = 0;
        final int decWidth = image.getWidth()/decimateX;
        final int imin = (index % decWidth)*decimateX + image.getMinX();
        final int jmin = (index / decWidth)*decimateY + image.getMinY();
        for (int i=imin+decimateX; --i>=imin;) {
            for (int j=jmin+decimateY; --j>=jmin;) {
                final Raster tile = image.getTile(image.XToTileX(i), image.YToTileY(j));
                final double x = tile.getSampleDouble(i, j, bandX);
                final double y = tile.getSampleDouble(i, j, bandY);
                if (!Double.isNaN(x) && !Double.isNaN(y)) {
                    vectX += x;
                    vectY += y;
                    sumI  += i;
                    sumJ  += j;
                    count++;
                }
            }
        }
        this.lastIndex = index;
        this.lastX     = vectX/count;
        this.lastY     = vectY/count;
        this.lastI     = (double)sumI / count;
        this.lastJ     = (double)sumJ / count;
        assert Double.isNaN(lastI) == Double.isNaN(lastJ);
        assert Double.isNaN(vectX) == Double.isNaN(vectY);
    }

    /**
     * Retourne la forme géométrique servant de modèle au traçage des marques.
     * Lorsque deux bandes sont utilisées, la forme par défaut sera une flèche
     * dont l'origine est à (0,0) et qui pointe dans la direction des <var>x</var>
     * positifs (soit à un angle de 0 radians arithmétiques).
     */
    protected Shape getMarkShape(final int i) {
        return (numBands>=2) ? DEFAULT_SHAPE_2D : DEFAULT_SHAPE_1D;
    }

    /**
     * Procède au traçage d'une marque.
     *
     * @param graphics Graphique à utiliser pour tracer la flèche. L'espace de coordonnées
     *                 de ce graphique sera les pixels en les points (1/72 de pouce).
     * @param shape    Forme géométrique représentant la flèche à tracer.
     * @param index    Index de la flèche à tracer.
     */
    protected void paint(final Graphics2D graphics, final Shape shape, final int index) {
        graphics.setColor(color);
        graphics.fill(shape);
    }

    /**
     * Retourne les indices qui correspondent aux coordonnées spécifiées.
     * Ces indices seront utilisées par {@link #isVisible(int,Rectangle)}
     * pour vérifier si un point est dans la partie visible.
     *
     * @param visibleArea Coordonnées logiques de la région visible à l'écran.
     */
    final Rectangle getUserClip(final Rectangle2D visibleArea) {
        assert Thread.holdsLock(getTreeLock());
        if (visibleArea != null) try {
            // Note: on profite du fait que {@link Rectangle#setRect}
            //       arrondie correctement vers les limites supérieures.
            final Rectangle bounds = (Rectangle) CTSUtilities.transform(
                                                 (MathTransform2D) gridToCoordinateSystem.inverse(),
                                                 visibleArea, new Rectangle());

            bounds.x      = (bounds.x      -1) / decimateX;
            bounds.y      = (bounds.y      -1) / decimateY;
            bounds.width  = (bounds.width  +2) / decimateX +1;
            bounds.height = (bounds.height +2) / decimateY +1;
            return bounds;
        } catch (TransformException exception) {
            // Retourne un clip englobant toutes les coordonnées.
        }
        return new Rectangle(0, 0, image.getWidth(), image.getHeight());
    }

    /**
     * Indique si la station à l'index spécifié est visible
     * dans le clip spécifié. Le rectangle <code>clip</code>
     * doit avoir été obtenu par {@link #getUserClip}.
     */
    final boolean isVisible(final int index, final Rectangle clip) {
        if (clip == null) {
            return true;
        }
        assert Thread.holdsLock(getTreeLock());
        final int decWidth = image.getWidth()/decimateX;
        return clip.contains(index%decWidth, index/decWidth);
    }

    /**
     * Procède au traçage des marques de cette grille.
     *
     * @throws TransformException si une projection
     *         cartographique était nécessaire et a échouée.
     */
    protected void paint(final RenderingContext context) throws TransformException {
        if (autoDecimate) {
            assert Thread.holdsLock(getTreeLock());
            Point2D delta = new Point2D.Double(1,1);
            delta = CTSUtilities.deltaTransform(gridToCoordinateSystem,
                    new Point2D.Double(0.5*image.getWidth(), 0.5*image.getHeight()), delta, delta);
            delta = context.getAffineTransform(getCoordinateSystem(), context.textCS)
                    .deltaTransform(delta, delta);
            final int decimateX = Math.max(1, (int)Math.ceil(spaceX/delta.getX()));
            final int decimateY = Math.max(1, (int)Math.ceil(spaceY/delta.getY()));
            if (decimateX!=this.decimateX || decimateY!=this.decimateY) {
                this.decimateX = decimateX;
                this.decimateY = decimateY;
                decimate = (decimateX!=1 || decimateY!=1);
                clearCache();
            }
        }
        super.paint(context);
    }

    /**
     * Efface des informations qui avaient été conservées dans une mémoire cache.
     */
    void clearCache() {
        assert Thread.holdsLock(getTreeLock());
        typicalAmplitude = 0;
        super.clearCache();
    }

    /**
     * Clear all informations relative to the grid coverage.
     */
    private void clearCoverage() {
        coverage = null;
        image    = null;
        mainSD   = null;
        gridToCoordinateSystem = MathTransform2D.IDENTITY;
    }

    /**
     * Provides a hint that a layer will no longer be accessed from a reference in user
     * space. The results are equivalent to those that occur when the program loses its
     * last reference to this layer, the garbage collector discovers this, and finalize
     * is called. This can be used as a hint in situations where waiting for garbage
     * collection would be overly conservative.
     */
    public void dispose() {
        synchronized (getTreeLock()) {
            clearCoverage();
            super.dispose();
        }
    }




    /////////////////////////////////////////////////////////////////////////////////////////////
    ////////////    EVENTS (note: may be moved out of this class in a future version)    ////////
    /////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * A band to use as a formatter for geophysics values.
     */
    private SampleDimension mainSD;

    /**
     * Buffer temporaire pour l'écriture des "tooltip".
     */
    private transient StringBuffer buffer;

    /**
     * Objet à utiliser pour l'écriture des angles.
     */
    private transient AngleFormat angleFormat;

    /**
     * Retourne l'amplitude de la flèche.
     */
    String getToolTipText(final int index) {
        assert Thread.holdsLock(getTreeLock());
        double amplitude = getAmplitude(index);
        if (numBands == 1) {
            // 'getAmplitude' took the square root of amplitude.
            // Overrides with the plain amplitude.
            amplitude = lastX;
        }
        if (buffer == null) {
            buffer = new StringBuffer();
        }
        buffer.setLength(0);
        final Locale locale = getLocale();
        final String label  = mainSD.getLabel(amplitude, locale);
        if (label != null) {
            buffer.append(label);
        } else {
            if (angleFormat == null) {
                angleFormat = new AngleFormat("D.dd°", locale);
            }
            // Will be formatted as a number (because of 'Double' type).
            buffer = angleFormat.format(new Double(amplitude), buffer, null);
        }
        if (numBands >= 2) {
            double angle = getDirection(index);
            angle = 90-Math.toDegrees(angle);
            angle -= 360*Math.floor(angle/360);
            if (angleFormat == null) {
                angleFormat = new AngleFormat("D.dd°", locale);
            }
            buffer.append("  ");
            buffer = angleFormat.format(angle, buffer, null);
        }
        return buffer.toString();
    }
}
