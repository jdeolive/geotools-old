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
import javax.media.jai.PlanarImage; // Pour Javadoc

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
import org.geotools.resources.CTSUtilities;
import org.geotools.resources.XDimension2D;
import org.geotools.resources.XAffineTransform;


/**
 * A layer for rendering a {@linkplain GridCoverage grid coverage}. More than one
 * <code>RenderedGridCoverage</code> can share the same grid coverage, for example
 * in order to display an image in many {@link org.geotools.gui.swing.MapPane} with
 * different zoom.
 *
 * @version $Id: RenderedGridCoverage.java,v 1.2 2003/01/28 16:12:15 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public class RenderedGridCoverage extends RenderedLayer {
    /**
     * The underlying grid coverage.
     */
    private GridCoverage coverage;

    /**
     * The projected grid coverage. This coverage
     * is computed only when first needed.
     */
    private transient GridCoverage projectedCoverage;

    /**
     * Coordonnées géographiques de l'image. Ces coordonnées
     * sont extraites une fois pour toute afin de réduire le
     * nombre d'objets créés lors des tracés de la carte.
     */
    private Rectangle2D geographicArea;

    /**
     * Point dans lequel mémoriser les coordonnées logiques d'un pixel
     * de l'image. Cet objet est utilisé temporairement pour obtenir la
     * valeur du paramètre géophysique d'un pixel.
     */
    private transient Point2D point;

    /**
     * Valeurs sous le curseur de la souris. Ce tableau sera créé
     * une fois pour toute la première fois où il sera nécessaire.
     */
    private transient double[] values;

    /**
     * Liste des bandes. Cette liste ne sera créée
     * que la première fois où elle sera nécessaire.
     */
    private transient SampleDimension[] bands;

    /**
     * Construct an empty grid coverage layer.
     */
    public RenderedGridCoverage() {
    }

    /**
     * Construct a new layer for the specified grid coverage.
     * It is legal to construct many layers for the same grid
     * coverage.
     */
    public RenderedGridCoverage(final GridCoverage coverage) {
        setCoverage(coverage);
        setTools(new Tools());
    }

    /**
     * Set the grid coverage. A <code>null</code> value
     * will remove the current grid coverage.
     */
    public void setCoverage(final GridCoverage coverage) {
        synchronized (getTreeLock()) {
            if (coverage == null) {
                clearCache();
                this.coverage = null;
                return;
            }
            try {
                CoordinateSystem newCS = coverage.getCoordinateSystem();
                newCS = CTSUtilities.getCoordinateSystem2D(newCS);
                if (!getCoordinateSystem().equals(newCS)) {
                    setCoordinateSystem(newCS);
                }
            } catch (TransformException exception) {
                // Should not be very common, since GridCoverage are
                // already supposed to use 2D coordinate system.
                final IllegalArgumentException e;
                e = new IllegalArgumentException(exception.getLocalizedMessage());
                e.initCause(exception);
                throw e;
            }
            clearCache();
            this.coverage = coverage;
            final Envelope envelope = coverage.getEnvelope();
            final GridRange   range = coverage.getGridGeometry().getGridRange();
            this.geographicArea = new Rectangle2D.Double(envelope.getMinimum(0),
                                                         envelope.getMinimum(1),
                                                         envelope.getLength (0),
                                                         envelope.getLength (1));
            setPreferredArea(geographicArea);
            setZOrder(envelope.getDimension()>=3 ? (float)envelope.getCenter(2) : Float.NEGATIVE_INFINITY);
            setPreferredPixelSize(new XDimension2D.Double(envelope.getLength(0)/range.getLength(0),
                                                          envelope.getLength(1)/range.getLength(1)));
        }
    }

    /**
     * Returns the underlying grid coverage, or <code>null</code>
     * if no grid coverage has been set.
     */
    public GridCoverage getCoverage() {
        return coverage;
    }

    /**
     * Returns the grid coverage projected to the specified coordinate system.
     *
     * @param  targetCS The coordinate system for the coverage to be returned.
     * @return The coverage projected in the specified coordinate system.
     * @throws TransformException if the coverage can't be projected.
     */
    private GridCoverage getCoverage(CoordinateSystem targetCS) throws TransformException {
        assert Thread.holdsLock(getTreeLock());
        if (coverage == null) {
            return null;
        }
        if (projectedCoverage == null) {
            projectedCoverage = coverage.geophysics(false);
        }
        CoordinateSystem sourceCS;
        sourceCS = projectedCoverage.getCoordinateSystem();
        sourceCS = CTSUtilities.getCoordinateSystem2D(sourceCS);
        targetCS = CTSUtilities.getCoordinateSystem2D(targetCS);
        if (!sourceCS.equals(targetCS, false)) {
            final GridCoverageProcessor processor = GridCoverageProcessor.getDefault();
            projectedCoverage = processor.doOperation("Resample", coverage.geophysics(false),
                                                      "CoordinateSystem", targetCS);
        }
        return projectedCoverage;
    }

    /**
     * Prévient cette couche qu'elle sera bientôt dessinée sur la carte spécifiée. Cette
     * méthode peut être appelée avant que cette couche soit ajoutée à la carte. Elle peut
     * lancer en arrière-plan quelques threads qui prépareront l'image.
     *
     * @see PlanarImage#prefetchTiles
     */
    protected void prefetch(Rectangle2D area) {
        assert Thread.holdsLock(getTreeLock());
        if (area!=null && !area.isEmpty() && projectedCoverage!=null && renderer!=null) try {
            final MathTransform2D transform = (MathTransform2D) renderer.getMathTransform(
                        getCoordinateSystem(), projectedCoverage.getCoordinateSystem(),
                        "RenderedGridCoverage", "prefetch");
            if (!transform.isIdentity()) {
                area = CTSUtilities.transform(transform, area, null);
            }
            coverage.prefetch(area);
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
        final GridCoverage coverage = getCoverage(context.mapCS);
        if (coverage != null) {
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
            context.addPaintedArea(geographicArea, getCoordinateSystem());
        }
    }

    /**
     * Efface les informations qui avaient été
     * sauvegardées dans la cache interne.
     */
    void clearCache() {
        point             = null;
        values            = null;
        bands             = null;
        projectedCoverage = null;
        super.clearCache();
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
    private boolean formatValue(final GeoMouseEvent event, final StringBuffer toAppendTo) {
        synchronized (getTreeLock()) {
            if (coverage == null) {
                return false;
            }
            try {
                point = event.getCoordinate(getCoordinateSystem(), point);
            } catch (TransformException exception) {
                // Can't transform the point. It may occurs if the mouse cursor is
                // far away from the area of coordinate system validity. Ignore...
                return false;
            }
            try {
                values = coverage.evaluate(point, values);
            } catch (PointOutsideCoverageException exception) {
                // Point is outside grid coverage. This is normal and we should not print any
                // message here. We could test if the point is inside the grid coverage before
                // invoking the 'evaluate' method, but it would slow down the normal case where
                // the point is inside. Catching the exception slow down the case where the point
                // is outside instead. It is one or the other, we had to choose...
                return false;
            } catch (CannotEvaluateException exception) {
                // The point can't be evaluated for some other reason. Append an error flag.
                toAppendTo.append("ERROR");
                return true;
            }
            if (bands == null) {
                bands = coverage.getSampleDimensions();
            }
            boolean modified = false;
            final Locale locale = getLocale();
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
     * A default set of tools for {@link RenderedGridCoverage} layer. An instance of this
     * class is automatically registered at the {@link RenderedGridCoverage} construction
     * stage.
     *
     * @version $Id: RenderedGridCoverage.java,v 1.2 2003/01/28 16:12:15 desruisseaux Exp $
     * @author Martin Desruisseaux
     */
    protected class Tools extends org.geotools.renderer.j2d.Tools {
        /**
         * Default constructor.
         */
        protected Tools() {
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
        protected boolean formatValue(final GeoMouseEvent event, final StringBuffer toAppendTo) {
            return RenderedGridCoverage.this.formatValue(event, toAppendTo);
        }
    }
}
