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

// J2SE and JAI dependencies
import java.awt.Shape;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.AffineTransform;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.awt.RenderingHints;
import javax.media.jai.ImageMIPMap;
import javax.media.jai.PlanarImage;
import javax.media.jai.RenderedOp;
import javax.media.jai.JAI;
import java.util.Map;
import java.util.Locale;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.LogRecord;

// Geotools dependencies
import org.geotools.pt.Envelope;
import org.geotools.cs.CoordinateSystem;
import org.geotools.ct.MathTransform2D;
import org.geotools.ct.TransformException;
import org.geotools.cv.SampleDimension;
import org.geotools.cv.CannotEvaluateException;
import org.geotools.cv.PointOutsideCoverageException;
import org.geotools.gc.GridRange;
import org.geotools.gc.GridCoverage;
import org.geotools.gp.GridCoverageProcessor;
import org.geotools.gp.CannotReprojectException;
import org.geotools.resources.Utilities;
import org.geotools.resources.CTSUtilities;
import org.geotools.resources.GCSUtilities;
import org.geotools.resources.ImageUtilities;
import org.geotools.resources.XDimension2D;
import org.geotools.resources.XAffineTransform;
import org.geotools.resources.renderer.Resources;
import org.geotools.resources.renderer.ResourceKeys;


/**
 * A layer for rendering a {@linkplain GridCoverage grid coverage}. More than one
 * <code>RenderedGridCoverage</code> can share the same grid coverage, for example
 * in order to display an image in many {@link org.geotools.gui.swing.MapPane} with
 * different zoom.
 *
 * @version $Id: RenderedGridCoverage.java,v 1.5 2003/02/22 22:36:03 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public class RenderedGridCoverage extends RenderedLayer {
    /**
     * Tells if we should try an optimisation using pyramidal images.
     */
    private static final boolean USE_PYRAMID = true;

    /**
     * Decimation factor for image. A value of 0.5 means that each
     * level in the image pyramid will contains an image with half
     * the resolution of previous level. This value is used only if
     * {@link #USE_PYRAMID} is <code>true</code>.
     */
    private static final float DOWN_SAMPLER = 0.25f;

    /**
     * Natural logarithm of {@link #DOWN_SAMPLER}. Used
     * only if {@link #USE_PYRAMID} is <code>true</code>.
     */
    private static final double LOG_DOWN_SAMPLER = Math.log(DOWN_SAMPLER);

    /**
     * Minimum size (in pixel) for use of pyramidal image. Images smaller
     * than this size will not use pyramidal images, since it would not
     * give many visible benefict. Used only if {@link #USE_PYRAMID} is
     * <code>true</code>.
     */
    private static final int MIN_SIZE = 512;

    /**
     * Default value for the {@linkplain #getZOrder z-order}.
     */
    private static final float DEFAULT_Z_ORDER = Float.NEGATIVE_INFINITY;

    /**
     * {@link ImageMIPMap} previously created for {@link RenderedImage}.
     * Key are weak references to {@link RenderedImage}, and values are
     * {@link ImageMIPMap}. This map will be created only when first needed.
     *
     * @see #getImageMIPMap
     */
    private static Map sharedImages;

    /**
     * The coverage given to {@link #setGridCoverage}. This coverage is
     * either the same than {@link #coverage}, or one of its sources.
     *
     * @see #getGridCoverage
     * @see #setGridCoverage
     */
    private GridCoverage sourceCoverage;

    /**
     * The grid coverage projected in this {@linkplain #getCoordinateSystem rendering
     * coordinate system}. It may or may not be the same than {@link #sourceCoverage}.
     */
    private GridCoverage coverage;

    /**
     * The {@link #coverage} area in the coverage's coordinate system.
     * Computed once for ever and reused for every {@link #paint} invocations.
     */
    private Rectangle2D coverageArea;

    /**
     * A list of multi-resolution images. Image at level 0 is identical to
     * {@link GridCoverage#getRenderedImage()}.  Other levels contains the
     * image at lower resolution for faster rendering.
     */
    private ImageMIPMap images;
    
    /**
     * Maximum amount of level to use for multi-resolution images.
     */
    private int maxLevel;

    /**
     * Point dans lequel mémoriser les coordonnées logiques d'un pixel
     * de l'image. Cet objet est utilisé temporairement pour obtenir la
     * valeur du paramètre géophysique d'un pixel.
     *
     * @see #formatValue
     */
    private transient Point2D point;

    /**
     * Valeurs sous le curseur de la souris. Ce tableau sera créé
     * une fois pour toute la première fois où il sera nécessaire.
     *
     * @see #formatValue
     */
    private transient double[] values;

    /**
     * Liste des bandes. Cette liste ne sera créée
     * que la première fois où elle sera nécessaire.
     *
     * @see #formatValue
     */
    private transient SampleDimension[] bands;

    /**
     * Construct a new layer for the specified grid coverage.
     * It is legal to construct many layers for the same grid
     * coverage.
     *
     * @param coverage The grid coverage, or <code>null</code> if none.
     */
    public RenderedGridCoverage(final GridCoverage coverage) {
        if (coverage == null) {
            setZOrder(DEFAULT_Z_ORDER);
        } else try {
            setCoordinateSystem(coverage.getCoordinateSystem());
            setGridCoverage(coverage);
        } catch (TransformException exception) {
            // Should not happen in most cases, since the GridCoverage's
            // coordinate system usually has a two-dimensional head CS.
            final IllegalArgumentException e;
            e = new IllegalArgumentException(exception.getLocalizedMessage());
            e.initCause(exception);
            throw e;
        }
    }

    /**
     * Set the grid coverage to renderer. This grid coverage will be automatically projected
     * to the {@linkplain #getCoordinateSystem rendering coordinate system}, if needed.
     *
     * @param  newCoverage The new grid coverage, or <code>null</code> if none.
     * @throws TransformException if the specified coverage can't be projected to
     *         the current {@linkplain #getCoordinateSystem  rendering coordinate
     *         system}.
     */
    public void setGridCoverage(final GridCoverage newCoverage) throws TransformException {
        final GridCoverage oldCoverage;
        synchronized (getTreeLock()) {
            oldCoverage    = coverage;
            coverage       = project(newCoverage, getCoordinateSystem());
            sourceCoverage = newCoverage; // Must be set after 'coverage'.
            if (coverage != oldCoverage) {
                initialize();
            }
        }
        listeners.firePropertyChange("gridCoverage", oldCoverage, coverage);
        repaint();
    }

    /**
     * Project the specified coverage.
     *
     * @param  coverage The grid coverage to project, or <code>null</code> if none.
     * @param  targetCS The target coordinate system for the coverage.
     * @throws TransformException if the specified coverage can't be projected to
     *         the specified coordinate system.
     */
    private static GridCoverage project(GridCoverage coverage, CoordinateSystem targetCS)
            throws TransformException
    {
        if (coverage != null) {
            CoordinateSystem sourceCS;
            coverage = coverage.geophysics(false);
            sourceCS = coverage.getCoordinateSystem();
            sourceCS = CTSUtilities.getCoordinateSystem2D(sourceCS);
            targetCS = CTSUtilities.getCoordinateSystem2D(targetCS);
            if (!sourceCS.equals(targetCS, false)) {
                final GridCoverageProcessor processor = GridCoverageProcessor.getDefault();
                try {
                    coverage = processor.doOperation("Resample",         coverage,
                                                     "CoordinateSystem", targetCS);
                } catch (CannotReprojectException exception) {
                    throw new TransformException(exception.getLocalizedMessage(), exception);
                }
            }
        }
        return coverage;
    }

    /**
     * Initialize this object after a change of <strong>rendering</strong> grid coverage. This
     * change may occurs as a result of {@link #setGridCoverage} or {@link #setCoordinateSystem}.
     * This method constructs the pyramidal image for the current grid coverage. Together with
     * tiling, pyramidal images attempt to speed up rendering of large images. This method try
     * to constructs a shared instance, in order to allow many views of the same image without
     * duplicating the decimated images.
     *
     * @task REVISIT: <code>ImageMIPMap</code> caching may not be wanted if the user provided
     *                custom rendering hints, since the cached images may use different ones.
     *
     * @task TODO: <code>ImagePyramid</code> is an <code>ImageMIPMap</code> subclass that may be
     *             more efficient, but seems more difficult to use. We should investigage it.
     */
    private void initialize() {
        assert Thread.holdsLock(getTreeLock());
        clearCache();
        updatePreferences();
        if (coverage == null) {
            coverageArea = null;
            return;
        }
        coverageArea = coverage.getEnvelope().getSubEnvelope(0, 2).toRectangle2D();
        /*
         * The rest of this method compute the pyramidal image.
         * In input,  this method use the following fields: {@link #coverage}, {@link #renderer}.
         * In output, this method ovewrite the following fields: {@link #maxLevel}, {@link #images}.
         */
        if (!USE_PYRAMID) {
            images = null;
            maxLevel = 0;
        }
        /*
         * Compute the number of levels-1 (i.e. compute the maximal level allowed).
         * If the result is 0, then there is no point to construct a pyramidal image.
         */
        RenderedImage rootImage = coverage.getRenderedImage();
        while (rootImage instanceof RenderedOp) {
            rootImage = ((RenderedOp) rootImage).getRendering();
            // Required because an operation in the chain other than the down sampler
            // will confuse ImageMIPMap.   TODO: a drawback of that is that change to
            // the RenderedOp will not be forwarded to the rendered image.
        }
        maxLevel = Math.max(0, (int)(Math.log((double)MIN_SIZE/
                   Math.max(rootImage.getWidth(), rootImage.getHeight()))/LOG_DOWN_SAMPLER));
        if (maxLevel == 0) {
            images = null;
            return;
        }
        synchronized (RenderedGridCoverage.class) {
            /*
             * Verify if a pyramidal image exists already. This pyramidal image
             * can be shared among many 'RenderedGridCoverage' instances.
             */
            assert rootImage.hashCode() == System.identityHashCode(rootImage);
            if (sharedImages != null) {
                images = (ImageMIPMap) sharedImages.get(rootImage);
                if (images != null) {
                    return;
                }
            }
            /*
             * Gets the JAI instance to use and construct the chain of "Scale" operations.
             * Reminder: JAI will differ the execution of "Scale" operation until they are
             * requested, and only requested tiles will be computed.
             */
            JAI jai = null;
            RenderingHints hints = null;
            if (renderer != null) {
                hints = renderer.hints;
                if (hints != null) {
                    jai = (JAI) hints.get(Hints.JAI_INSTANCE);
                }
            }
            if (jai == null) {
                jai = JAI.getDefaultInstance();
            }
            RenderedImage image = rootImage;
            final ParameterBlock parameters = new ParameterBlock();
            parameters.add(DOWN_SAMPLER); // xScale
            parameters.add(DOWN_SAMPLER); // yScale
            for (int i=0; i<maxLevel; i++) {
                RenderingHints tileHints = ImageUtilities.getRenderingHints(image);
                if (tileHints == null) {
                    tileHints = hints;
                } else if (hints != null) {
                    tileHints.add(hints);
                }
                parameters.removeSources();
                parameters.addSource(image);
                image = jai.createNS("Scale", parameters, tileHints);
                assert Math.max(image.getWidth(), image.getHeight()) >= MIN_SIZE;
            }
            /*
             * Construct the pyramidal image and cache the result for future
             * use in possibily different 'RenderedGridCoverage' instance.
             */
            images = new ImageMIPMap((RenderedOp)image);
            if (sharedImages == null) {
                sharedImages = new WeakHashMap();
            }
            sharedImages.put(rootImage, images);
        }
    }

    /**
     * Returns the grid coverage, or <code>null</code> if none. This is the grid coverage
     * given to the last call of {@link #setGridCoverage}. The rendered grid coverage may
     * not be the same, since a map projection may be applied at rendering time.
     */
    public GridCoverage getGridCoverage() {
        return sourceCoverage;
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
            return Utilities.getShortClassName(this) +
                   "[\"" + coverage.getName(locale) + "\", " + getZOrder() + ']';
        }
    }

    /**
     * Set the rendering coordinate system for this layer.
     *
     * @param  cs The coordinate system.
     * @throws TransformException If <code>cs</code> if the grid coverage
     *         can't be resampled to the specified coordinate system.
     */
    protected void setCoordinateSystem(final CoordinateSystem cs) throws TransformException {
        final GridCoverage newCoverage = project(sourceCoverage, cs);
        synchronized (getTreeLock()) {
            super.setCoordinateSystem(cs);
            // Change the coverage only after the projection succed.
            coverage = newCoverage;
            initialize();
        }
    }

    /**
     * Update the {@linkplain #getPreferredArea preferred area},
     * {@linkplain #getPreferredPixelSize preferred pixel size}
     * and {@linkplain #getZOrder z-order} properties.
     */
    private void updatePreferences() {
        assert Thread.holdsLock(getTreeLock());
        if (coverage == null) {
            setPreferredArea(null);
            setPreferredPixelSize(null);
            setZOrder(DEFAULT_Z_ORDER);
            return;
        }
        final Envelope envelope = coverage.getEnvelope();
        final GridRange   range = coverage.getGridGeometry().getGridRange();
        setPreferredArea(envelope.getSubEnvelope(0, 2).toRectangle2D());
        setZOrder(envelope.getDimension()>=3 ? (float)envelope.getCenter(2) : DEFAULT_Z_ORDER);
        setPreferredPixelSize(new XDimension2D.Double(envelope.getLength(0)/range.getLength(0),
                                                      envelope.getLength(1)/range.getLength(1)));
    }

    /**
     * Prévient cette couche qu'elle sera bientôt dessinée sur la carte spécifiée. Cette
     * méthode peut être appelée avant que cette couche soit ajoutée à la carte. Elle peut
     * lancer en arrière-plan quelques threads qui prépareront l'image.
     *
     * @see PlanarImage#prefetchTiles
     */
    protected void prefetch(final RenderingContext context) {
        assert Thread.holdsLock(getTreeLock());
        if (coverage!=null) try {
            Rectangle2D area=context.getPaintingArea(coverage.getCoordinateSystem()).getBounds2D();
            if (area!=null && !area.isEmpty()) {
                coverage.prefetch(area);
            }
        } catch (TransformException exception) {
            Renderer.handleException("RenderedGridCoverage", "prefetch", exception);
            // Not a big deal, since this method is just a hint. Ignore...
        }
    }

    /**
     * Dessine l'image.
     *
     * @param  context  Suite des transformations nécessaires à la conversion de coordonnées
     *         géographiques (<var>longitude</var>,<var>latitude</var>) en coordonnées pixels.
     * @throws TransformException si une projection cartographique était nécessaire et qu'elle a
     *         échoué.
     */
    protected void paint(final RenderingContext context) throws TransformException {
        assert Thread.holdsLock(getTreeLock());
        if (coverage != null) {
            assert coverage.getCoordinateSystem().equals(context.mapCS, false);
            final AffineTransform gridToCoordinate; // This transform is immutable.
            try {
                gridToCoordinate = (AffineTransform) coverage.getGridGeometry()
                                                             .getGridToCoordinateSystem2D();
            } catch (ClassCastException exception) {
                throw new TransformException(Resources.getResources(getLocale()).getString(
                                             ResourceKeys.ERROR_NON_AFFINE_TRANSFORM), exception);
            }
            final AffineTransform transform;
            final RenderedImage   image; // The image to display (will be computed below).
            if (images != null) {
                /*
                 * Calcule quel "niveau" d'image serait le plus approprié.
                 * Ce calcul est fait en fonction de la résolution requise.
                 */
                transform = context.getGraphics().getTransform();
                transform.concatenate(gridToCoordinate);
                final int level = Math.max(0,
                                  Math.min(maxLevel,
                                  (int)(Math.log(Math.max(XAffineTransform.getScaleX0(transform),
                                  XAffineTransform.getScaleY0(transform)))/LOG_DOWN_SAMPLER)));
                /*
                 * Si on utilise une résolution inférieure (pour un
                 * affichage plus rapide), alors il faut utiliser un
                 * géoréférencement ajusté en conséquence.
                 */
                transform.setTransform(gridToCoordinate);
                if (level != 0) {
                    final double scale = Math.pow(DOWN_SAMPLER, -level);
                    transform.scale(scale, scale);
                }
                if (level != images.getCurrentLevel()) {
                    final Logger logger = Logger.getLogger("org.geotools.renderer.j2d");
                    if (logger.isLoggable(Level.FINE)) {
                        final Locale locale = getLocale();
                        final LogRecord record = Resources.getResources(locale).getLogRecord(
                                           Level.FINE, ResourceKeys.RESSAMPLING_RENDERED_IMAGE_$3,
                                           coverage.getName(locale),
                                           new Integer(level), new Integer(maxLevel));
                        record.setSourceClassName(Utilities.getShortClassName(this));
                        record.setSourceMethodName("paint");
                        logger.log(record);
                    }
                }
                image = images.getImage(level);
            } else {
                transform = new AffineTransform(gridToCoordinate);
                image = coverage.getRenderedImage();                
            }
            transform.translate(-0.5, -0.5); // Map to upper-left corner.
            context.getGraphics().drawRenderedImage(image, transform);
            context.addPaintedArea(coverageArea, coverage.getCoordinateSystem());
        }
    }

    /**
     * Format a value for the current mouse position. This method append in
     * <code>toAppendTo</code> the value in each bands for the pixel at the
     * mouse position. For example if the current image show Sea Surface
     * Temperature (SST), then this method will format the temperature in
     * geophysical units (e.g. "12°C").
     *
     * @param  event The mouse event.
     * @param  toAppendTo The destination buffer for formatting a value.
     * @return <code>true</code> if this method has formatted a value, or <code>false</code>
     *         otherwise.
     */
    boolean formatValue(final GeoMouseEvent event, final StringBuffer toAppendTo) {
        synchronized (getTreeLock()) {
            if (sourceCoverage == null) {
                return false;
            }
            try {
                point = event.getCoordinate(sourceCoverage.getCoordinateSystem(), point);
            } catch (TransformException exception) {
                // Can't transform the point. It may occurs if the mouse cursor is
                // far away from the area of coordinate system validity. Ignore...
                return false;
            }
            final Locale locale = getLocale();
            try {
                values = sourceCoverage.evaluate(point, values);
            } catch (PointOutsideCoverageException exception) {
                // Point is outside grid coverage. This is normal and we should not print any
                // message here. We could test if the point is inside the grid coverage before
                // invoking the 'evaluate' method, but it would slow down the normal case where
                // the point is inside. Catching the exception slow down the case where the point
                // is outside instead. It is one or the other, we had to choose...
                return false;
            } catch (CannotEvaluateException exception) {
                // The point can't be evaluated for some other reason. Append an error flag.
                toAppendTo.append(Resources.getResources(locale).getString(ResourceKeys.ERROR));
                return true;
            }
            if (bands == null) {
                bands = sourceCoverage.getSampleDimensions();
            }
            boolean modified = false;
            for (int i=0; i<values.length; i++) {
                final String text = bands[i].getLabel(values[i], locale);
                if (text != null) {
                    if (modified) {
                        toAppendTo.append(", ");
                    }
                    toAppendTo.append(text);
                    modified = true;
                }
            }
            return modified;
        }
    }

    /**
     * Efface les informations qui avaient été
     * sauvegardées dans la cache interne.
     */
    void clearCache() {
        point  = null;
        values = null;
        bands  = null;
        super.clearCache();
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
            super.dispose();
            /*
             * We will not dispose the planar image if there is any
             * chance that it is referenced outside of this class.
             */
            if (coverage != null) {
                final RenderedImage image = coverage.getRenderedImage();
                if (!GCSUtilities.uses(sourceCoverage.geophysics(false), image)) {
                    if (image instanceof PlanarImage) {
                        ((PlanarImage) image).dispose();
                    }
                }
            }
            coverage = sourceCoverage = null;
            coverageArea = null;
            images       = null;
            maxLevel     = 0;
        }
    }
}
