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
import java.awt.Paint;
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
import org.geotools.cs.CompoundCoordinateSystem;
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
 * @version $Id: RenderedGridMarks.java,v 1.11 2003/03/20 22:49:34 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public class RenderedGridMarks extends RenderedMarks {
    /**
     * Extends the zoomable area by this amount of pixels when computing the "grid" clip area.
     * This is needed since a mark located outside the clip area may have a part showing in the
     * visible area if the mark is big enough.
     *
     * @task REVISIT: This number should be computed rather than hard-coded.
     */
    private static final int VISIBLE_AREA_EXTENSION = 10;

    /**
     * Default value for the {@linkplain #getZOrder z-order}.
     */
    private static final float DEFAULT_Z_ORDER = Float.POSITIVE_INFINITY;

    /**
     * The default shape for displaying data with only 1 band.
     * This shape is a circle centered at (0,0) with a radius of 5 dots.
     */
    private static final Shape DEFAULT_SHAPE_1D = MarkIterator.DEFAULT_SHAPE;

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
     * Couleur des flèches.
     */
    private Paint markPaint = MarkIterator.DEFAULT_COLOR;

    /**
     * The shape to use for marks, or <code>null</code> for displaying labels only.
     */
    private Shape markShape = DEFAULT_SHAPE_1D;

    /**
     * The default {@linkplain #getZOrder z-order} for this layer.
     * Used only if the user didn't set explicitely a z-order.
     */
    private float zOrder = DEFAULT_Z_ORDER;

    /**
     * The default {@linkplain #getPreferredArea preferred area} for this layer.
     * Used only if the user didn't set explicitely a preferred area.
     */
    private Rectangle2D preferredArea;

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
     * Construct a new layer for the specified grid coverage. If the supplied grid coverage has
     * only one band, then marks will be displayed as circles with area proportional to sample
     * values. Otherwise, marks will be displayed as arrows with <var>x</var> and <var>y</var>
     * components fetched from sample dimensions (bands) 0 and 1 respectively.
     *
     * @param coverage The grid coverage, or <code>null</code> if none.
     */
    public RenderedGridMarks(final GridCoverage coverage) {
        if (coverage != null) try {
            numBands = coverage.getRenderedImage().getSampleModel().getNumBands();
            if (numBands >= 2) {
                numBands  = 2;
                bandY     = 1;
                markShape = DEFAULT_SHAPE_2D;
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
     * Returns the name of this layer.
     *
     * @param  locale The desired locale, or <code>null</code> for a default locale.
     * @return This layer's name.
     */
    public String getName(final Locale locale) {
        synchronized (getTreeLock()) {
            if (coverage == null) {
                return super.getName(locale);
            }
            return coverage.getName(locale) + " (" + super.getName(locale) + ')';
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
            if (coverage != null) {
                updatePreferences();
            }
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
            if (coverage == null) {
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
                gridToCoordinateSystem  = getGridToCoordinateSystem(coverage, getCoordinateSystem(),
                                                                    "setGridCoverage");
                this.coverage = coverage;
                this.image    = PlanarImage.wrapRenderedImage(coverage.getRenderedImage());
                this.mainSD   = sampleX;
                updatePreferences();
            }
            clearCache();
            repaint();
        }
        listeners.firePropertyChange("gridCoverage", oldCoverage, coverage);
    }

    /**
     * Compute the preferred area and the z-order.
     */
    private void updatePreferences() throws TransformException {
        assert Thread.holdsLock(getTreeLock());
        final Envelope envelope = coverage.getEnvelope();
        zOrder = envelope.getDimension()>=3 ? (float)envelope.getCenter(2) : DEFAULT_Z_ORDER;
        preferredArea= envelope.getSubEnvelope(0,2).toRectangle2D();
        preferredArea= CTSUtilities.transform(gridToCoordinateSystem, preferredArea, preferredArea);
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
                    if (markShape == DEFAULT_SHAPE_1D) {
                        markShape  = DEFAULT_SHAPE_2D;
                    }
                    break;
                }
                case 1: {
                    bandX = bandY = bands[0];
                    if (markShape == DEFAULT_SHAPE_2D) {
                        markShape  = DEFAULT_SHAPE_1D;
                    }
                    break;
                }
                case 0: {
                    bandX = bandY = 0;
                    setVisible(false);
                    if (markShape == DEFAULT_SHAPE_2D) {
                        markShape  = DEFAULT_SHAPE_1D;
                    }
                    break;
                }
            }
            numBands = bands.length;
            clearCache();
            repaint();
        }
        listeners.firePropertyChange("bands", oldBands, bands);
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
     * iterated by the {@link MarkIterator}. Note that points are not actually decimated,
     * but rather averaged. For example a "decimation" factor of 2 will average two neighbor
     * points and replace them with new one in the middle of the original points.
     *
     * @param decimateX Decimation among <var>x</var>, or 1 for none.
     * @param decimateY Decimation among <var>y</var>, or 1 for none.
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
                clearCache();
                repaint();
            }
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
                clearCache();
                repaint();
            }
        }
    }

    /**
     * Returns the shape to use for painting marks. If this layer paint labels rather than
     * marks, then this method returns <code>null</code>.
     *
     * @see #setMarkShape
     * @see Iterator#markShape
     */
    public Shape getMarkShape() {
        return markShape;
    }

    /**
     * Set the shape to use for painting marks. This shape must be centred at the origin (0,0)
     * and its coordinates must be expressed in dots (1/72 of inch). For example in order to
     * paint wind arrows, this shape should be oriented toward positives <var>x</var> (i.e.
     * toward 0 arithmetic radians), has a base centred at (0,0) and have a raisonable size
     * (for example 16&times;4 pixels). The method {@link RenderedMarks#paint(RenderingContext)}
     * will automatically takes care of rotation, translation and scale in order to adjust this
     * model to each mark properties.
     * <br><br>
     * A value of <code>null</code> is legal. In this case, this layer will renderer amplitudes
     * as labels rather than marks.
     *
     * @see #getMarkShape
     * @see Iterator#markShape
     */
    public void setMarkShape(final Shape shape) {
        final Shape oldShape;
        synchronized (getTreeLock()) {
            oldShape = markShape;
            markShape = shape;
        }
        listeners.firePropertyChange("markShape", oldShape, shape);
    }

    /**
     * Returns the default fill paint for marks.
     *
     * @see #setMarkPaint
     * @see Iterator#markPaint
     */
    public Paint getMarkPaint() {
        return markPaint;
    }

    /**
     * Set the default fill paint for marks.
     *
     * @see #getMarkPaint
     * @see Iterator#markPaint
     */
    public void setMarkPaint(final Paint paint) {
        if (paint == null) {
            throw new IllegalArgumentException(
                            Resources.format(ResourceKeys.ERROR_BAD_ARGUMENT_$2, "paint", paint));
        }
        final Paint oldPaint;
        synchronized (getTreeLock()) {
            oldPaint = markPaint;
            markPaint = paint;
        }
        listeners.firePropertyChange("markPaint", oldPaint, paint);
    }

    /**
     * Returns the preferred area for this layer. If no preferred area has been explicitely
     * set, then this method returns the grid coverage's bounding box.
     */
    public Rectangle2D getPreferredArea() {
        synchronized (getTreeLock()) {
            final Rectangle2D area = super.getPreferredArea();
            if (area != null) {
                return area;
            }
            return (preferredArea!=null) ? (Rectangle2D) preferredArea.clone() : null;
        }
    }

    /**
     * Returns the <var>z-order</var> for this layer. If the grid coverage
     * has at least 3 dimension, then the default <var>z-order</var> is
     *
     * <code>gridCoverage.getEnvelope().getCenter(2)</code>.
     *
     * Otherwise, the default value is {@link Float#POSITIVE_INFINITY} in order to paint
     * the marks over everything else. The default value can be overriden with a call to
     * {@link #setZOrder}.
     *
     * @see #setZOrder
     */
    public float getZOrder() {
        synchronized (getTreeLock()) {
            if (isZOrderSet()) {
                return super.getZOrder();
            }
            return zOrder;
        }
    }

    /**
     * Retourne le nombre de points de cette grille. Le nombre de point retourné
     * tiendra compte de la décimation spécifiée avec {@link #setDecimation}.
     */
    final int getCount() {
        assert Thread.holdsLock(getTreeLock());
        if (image==null || numBands==0) {
            return 0;
        }
        return (image.getWidth()/decimateX) * (image.getHeight()/decimateY);
    }

    /**
     * Returns an iterator for iterating through the marks. The default implementation
     * returns an instance of {@link RenderedGridMarks.Iterator}.
     */
    public MarkIterator getMarkIterator() {
        return new Iterator();
    }

    /**
     * Returns the units for {@linkplain MarkIterator#amplitude marks amplitude}.
     * The default implementation infers the units from the underlying grid coverage.
     */
    public Unit getAmplitudeUnit() {
        return mainSD.getUnits();
    }

    /**
     * Returns the typical amplitude of marks. The default implementation computes the <cite>Root
     * Mean Square</cite> (RMS) value of sample values in the underlying grid coverage, no matter
     * what the {@linkplain #setDecimation decimation factor} is (if any).
     */
    public double getTypicalAmplitude() {
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
     * Returns the grid indices for the specified zoomable bounds.
     * Those indices will be used by {@link Iterator#visible(Rectangle)}.
     *
     * @param  zoomableBounds The zoomable bounds. Do not modify!
     * @param  csToMap  The transform from {@link #getCoordinateSystem()} to the rendering CS.
     * @param  mapToTxt The transform from the rendering CS to the Java2D CS.
     * @return The grid clip, or <code>null</code> if it can't be computed.
     */
    final Rectangle getGridClip(final Rectangle zoomableBounds,
                                final MathTransform2D  csToMap,
                                final AffineTransform mapToTxt)
    {
        assert Thread.holdsLock(getTreeLock());
        Rectangle2D visibleArea = new Rectangle2D.Double(
                zoomableBounds.x      - VISIBLE_AREA_EXTENSION,
                zoomableBounds.y      - VISIBLE_AREA_EXTENSION,
                zoomableBounds.width  + VISIBLE_AREA_EXTENSION*2,
                zoomableBounds.height + VISIBLE_AREA_EXTENSION*2);
        try {
            visibleArea = XAffineTransform.inverseTransform(mapToTxt, visibleArea, visibleArea);
            final MathTransform2D mapToCS = (MathTransform2D)csToMap.inverse();
            if (!mapToCS.isIdentity()) {
                visibleArea = CTSUtilities.transform(mapToCS, visibleArea, visibleArea);
            }
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
        } catch (NoninvertibleTransformException exception) {
            return null;
        } catch (TransformException exception) {
            return null;
        }
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
                invalidate();
            }
        }
        super.paint(context);
    }

    /**
     * Clear all informations relative to the grid coverage.
     */
    private void clearCoverage() {
        coverage      = null;
        image         = null;
        mainSD        = null;
        preferredArea = null;
        zOrder        = DEFAULT_Z_ORDER;
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




    /**
     * Iterates through all marks in a {@link RenderedGridMarks}.
     *
     * @version $Id: RenderedGridMarks.java,v 1.11 2003/03/20 22:49:34 desruisseaux Exp $
     * @author Martin Desruisseaux
     */
    protected class Iterator extends MarkIterator {
        /**
         * The upper limit (exclusive) for {@link #index}.
         */
        private final int count;

        /**
         * The index of current mark. The <var>i</var>,<var>j</var> index in the underlying
         * grid coverage can be deduced with:
         * <blockquote><pre>
         * i = index % width;
         * j = index / width;
         * </pre></blockquote>
         */
        private int index = -1;

        /**
         * The <var>i</var>,<var>j</var> indices for the current mark.
         * Will be computed only when first required.
         */
        private double i, j;

        /**
         * The <var>x</var>,<var>y</var> component for the current mark.
         * Will be computed only when first required.
         */
        private double x, y;

        /**
         * <code>true</code> if {@link #i}, {@link #j}, {@link #x} and {@link #y} are valids.
         */
        private boolean valid;

        /**
         * Construct a mark iterator.
         */
        public Iterator() {
            count = getCount();
        }

        public int getIteratorPosition() {
            return index;
        }
        
        /**
         * Moves the iterator to the specified index.
         */
        public void setIteratorPosition(final int n) {
            index = n;
            valid = false;
        }

        /**
         * Moves the iterator to the next mark.
         */
        public boolean next() {
            index++;
            valid = false;
            return index < count;
        }

        /**
         * Indique si la marque à l'index spécifié est visible dans le clip spécifié.
         * Le rectangle <code>clip</code> doit avoir été obtenu par {@link #getGridClip}.
         */
        final boolean visible(final Rectangle clip) {
            if (!visible()) {
                return false;
            }
            if (clip == null) {
                return true;
            }
            assert Thread.holdsLock(getTreeLock());
            final int decWidth = image.getWidth()/decimateX;
            return clip.contains(index%decWidth, index/decWidth);
        }

        /**
         * Calcule les composantes <var>x</var> et <var>y</var> du vecteur à l'index spécifié.
         */
        private void compute() {
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
            this.x = vectX/count;
            this.y = vectY/count;
            this.i = (double)sumI / count;
            this.j = (double)sumJ / count;
            assert Double.isNaN(i) == Double.isNaN(j);
            assert Double.isNaN(x) == Double.isNaN(y);
            valid = true;
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
        public Point2D position() throws TransformException {
            assert Thread.holdsLock(getTreeLock());
            final Point2D point;
            if (!decimate) {
                final int width = image.getWidth();
                point = new Point2D.Double(index%width, index/width);
            } else {
                if (!valid) {
                    compute();
                }
                point = new Point2D.Double(i, j);
            }
            return gridToCoordinateSystem.transform(point, point);
        }

        /**
         * Retourne l'amplitude à la position d'une marque. Si une décimation a été spécifiée avec
         * la méthode {@link #setDecimation}, alors cette méthode calcule la moyenne vectorielle
         * (la moyenne des composantes <var>x</var> et <var>y</var>) aux positions des marques à
         * décimer, et retourne l'amplitude du vecteur moyen.
         */
        public double amplitude() {
            assert Thread.holdsLock(getTreeLock());
            if (!valid) {
                compute();
            }
            switch (numBands) {
                case 0:  return 0;
                case 1:  return Math.sqrt(Math.abs(x));
                case 2:  return XMath.hypot(x, y);
                default: throw new AssertionError(numBands);
            }
        }

        /**
         * Retourne la direction de la valeur d'une marque. Si une décimation a été spécifiée avec
         * la méthode {@link #setDecimation}, alors cette méthode calcule la moyenne vectorielle
         * (la moyenne des composantes <var>x</var> et <var>y</var>) aux positions des marques à
         * décimer, et retourne la direction du vecteur moyen.
         */
        public double direction() {
            assert Thread.holdsLock(getTreeLock());
            if (!valid) {
                compute();
            }
            switch (numBands) {
                case 0:  // Fall through
                case 1:  return 0;
                case 2:  return Math.atan2(y, x);
                default: throw new AssertionError(numBands);
            }
        }

        /**
         * Retourne la forme géométrique servant de modèle au traçage des marques.
         * Lorsque deux bandes sont utilisées, la forme par défaut sera une flèche
         * dont l'origine est à (0,0) et qui pointe dans la direction des <var>x</var>
         * positifs (soit à un angle de 0 radians arithmétiques).
         *
         * @see RenderedGridMarks#getMarkShape
         * @see RenderedGridMarks#setMarkShape
         */
        public Shape markShape() {
            return markShape;
        }

        /**
         * Returns the paint for current mark.
         *
         * @see RenderedGridMarks#getMarkPaint
         * @see RenderedGridMarks#setMarkPaint
         */
        public Paint markPaint() {
            return markPaint;
        }

        /**
         * Returns the label for the current mark, or <code>null</code> if none.
         */
        public String label() {
            if (markShape == null) {
                double amplitude = amplitude();
                if (numBands == 1) {
                    // 'getAmplitude' took the square root of amplitude.
                    // Overrides with the plain amplitude.
                    amplitude = x;
                }
                return mainSD.getLabel(amplitude, getLocale());
            }
            return super.label();
        }

        /**
         * Returns a tooltip text for the current mark. The default implementation returns
         * the arrow's amplitude and direction. <strong>Note:</strong> This method is not a
         * commited part of the API. It may moves elsewhere in a future version.
         *
         * @param  event The mouse event.
         * @return The tool tip text for the current mark, or <code>null</code> if none.
         */
        protected String getToolTipText(final GeoMouseEvent event) {
            assert Thread.holdsLock(getTreeLock());
            double amplitude = amplitude();
            if (numBands == 1) {
                // 'getAmplitude' took the square root of amplitude.
                // Overrides with the plain amplitude.
                amplitude = x;
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
                double angle = direction();
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
}
