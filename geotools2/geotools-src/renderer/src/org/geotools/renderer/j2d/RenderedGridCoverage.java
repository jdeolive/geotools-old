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
import java.util.Locale;
import java.awt.Shape;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.AffineTransform;
import java.awt.image.RenderedImage;
import javax.media.jai.PlanarImage;

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
import org.geotools.resources.CTSUtilities;
import org.geotools.resources.GCSUtilities;
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
 * @version $Id: RenderedGridCoverage.java,v 1.3 2003/02/20 11:18:08 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public class RenderedGridCoverage extends RenderedLayer {
    /**
     * Default value for the {@linkplain #getZOrder z-order}.
     */
    private static final float DEFAULT_Z_ORDER = Float.NEGATIVE_INFINITY;

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
                clearCache();
                updatePreferences();
            }
        }
        listeners.firePropertyChange("gridCoverage", oldCoverage, coverage);
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
     * Returns the grid coverage, or <code>null</code> if none. This is the grid coverage
     * given to the last call of {@link #setGridCoverage}. The rendered grid coverage may
     * not be the same, since a map projection may be applied at rendering time.
     */
    public GridCoverage getGridCoverage() {
        return sourceCoverage;
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
            updatePreferences();
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
            final AffineTransform gridToCoordinate;
            try {
                gridToCoordinate = (AffineTransform) coverage.getGridGeometry()
                                                             .getGridToCoordinateSystem2D();
            } catch (ClassCastException exception) {
                throw new TransformException("Non-affine transformations not yet implemented",
                                             exception);
            }
            final AffineTransform transform = new AffineTransform(gridToCoordinate);
            transform.translate(-0.5, -0.5); // Map to upper-left corner.
            context.getGraphics().drawRenderedImage(coverage.getRenderedImage(), transform);
            context.addPaintedArea(coverage.getEnvelope().getSubEnvelope(0, 2).toRectangle2D(),
                                   coverage.getCoordinateSystem());
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
        }
    }
}
