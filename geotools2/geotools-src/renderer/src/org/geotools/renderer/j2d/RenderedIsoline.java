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
import java.awt.geom.Point2D;
import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.AffineTransform;

// Graphics
import java.awt.Paint;
import java.awt.Color;
import java.awt.Stroke;
import java.awt.Graphics2D;
import javax.swing.UIManager;
import javax.media.jai.GraphicsJAI;

// Collections
import java.util.List;
import java.util.Iterator;
import java.util.LinkedList;

// Geotools dependencies
import org.geotools.units.Unit;
import org.geotools.math.Statistics;
import org.geotools.cs.Ellipsoid;
import org.geotools.cs.CoordinateSystem;
import org.geotools.ct.TransformException;
import org.geotools.renderer.geom.InteriorType;
import org.geotools.renderer.geom.GeoShape;
import org.geotools.renderer.geom.Polygon;
import org.geotools.renderer.geom.Isoline;
import org.geotools.resources.XMath;
import org.geotools.resources.XDimension2D;
import org.geotools.resources.XAffineTransform;
import org.geotools.resources.CTSUtilities;


/**
 * A layer for an {@link Isoline} object. Instances of this class are typically
 * used for isobaths. Each isobath (e.g. sea-level, 50 meters, 100 meters...)
 * require a different instance of <code>RenderedIsoline</code>.
 *
 * @version $Id: RenderedIsoline.java,v 1.15 2003/05/12 22:26:06 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public class RenderedIsoline extends RenderedLayer {
    /**
     * The maximum number of clipped isolines to cache. This number can be set to <code>0</code>
     * for disabling clipping acceleration, which may be useful if a bug is suspected to prevent
     * proper rendering.
     */
    private static final int CLIP_CACHE_SIZE = 8;

    /**
     * The threshold ratio for computing a new clip. When the width and height of the underlying
     * {@link #isoline} bounding box divided by the width and height of the clip area is greater
     * than this threshold, a new clip is performed for faster rendering.
     *
     * DEBUGGING TIPS: Set this scale to a value below 1 to <em>see</em> the clipping's
     *                 effect in the window area.
     */
    private static final double CLIP_THRESHOLD = 4;

    /**
     * A factor slightly greater than 1 for computing the minimum size required for accepting
     * a clipped isoline.  A greater isoline's bounding box is necessary since a bounding box
     * too close to the clip's bounding box will make isoline contour apparent.
     */
    private static final double CLIP_EPS = 1.05;

    /**
     * Default color for fills.
     */
    private static final Color FILL_COLOR = new Color(246,212,140);

    /**
     * The "preferred line tickness" relative to the isoline's resolution.
     * A value of 1 means that isoline might be drawn with a line as tick
     * as the isoline's resolution. A value of 0.25 means that isoline might
     * be drawn with a line of tickness equals to 1/4 of the isoline's resolution.
     */
    private static final double TICKNESS = 0.25;

    /**
     * The isoline data. The {@linkplain Isoline#getCoordinateSystem isoline's coordinate system}
     * should matches the {@linkplain #getCoordinateSystem rendering coordinate system}.
     */
    private Isoline isoline;

    /**
     * List of clipped isolines. A clipped isoline may be faster to renderer
     * than the full isoline. Most rencently used isolines are last in this list.
     */
    private final List clipped = (CLIP_CACHE_SIZE!=0) ? new LinkedList() : null;

    /**
     * Color for contour lines. Default to panel's foreground (usually black).
     */
    private Paint contour = UIManager.getColor("Panel.foreground");

    /**
     * Paint for filling holes. Default to panel's background (usually gray).
     */
    private Paint background = UIManager.getColor("Panel.background");

    /**
     * Paint for filling elevations.
     */
    private Paint foreground = FILL_COLOR;

    /**
     * The number of points in {@link #isoline}. For statistics purpose only.
     */
    private int numPoints;

    /**
     * The renderer to use for painting polygons.
     * Will be created only when first needed.
     */
    private transient IsolineRenderer isolineRenderer;

    /**
     * The default {@linkplain #getPreferredArea preferred area} for this layer.
     * Used only if the user didn't set explicitely a preferred area.
     */
    private Rectangle2D preferredArea;

    /**
     * The default {@linkplain #getPreferredPixelSize preferred pixel size} for this layer.
     * Used only if the user didn't set explicitely a preferred pixel size.
     */
    private Dimension2D preferredPixelSize;

    /**
     * Construct a layer for the specified isoline.
     *
     * @param isoline The isoline, or <code>null</code> if none.
     * @see #setIsoline
     */
    public RenderedIsoline(final Isoline isoline) {
        if (isoline!=null) try {
            setCoordinateSystem(isoline.getCoordinateSystem());
            setIsoline(isoline);
        } catch (TransformException exception) {
            /*
             * Should not happen, since isoline use 2D coordinate systems. Rethrow it as an
             * illegal argument exception, which is not too far from the reality: the isoline
             * is not of the usual class.
             */
            final IllegalArgumentException e;
            e = new IllegalArgumentException(exception.getLocalizedMessage());
            e.initCause(exception);
            throw e;
        }
    }

    /**
     * Set a new isoline for this layer.
     *
     * @param  isoline The new isoline, or <code>null</code> if none.
     * @throws TransformException if the isoline can't be projected in the
     *         {@linkplain #getCoordinateSystem rendering coordinate system}.
     */
    public void setIsoline(Isoline isoline) throws TransformException {
        final Isoline oldIsoline;
        synchronized (getTreeLock()) {
            oldIsoline = this.isoline;
            if (isoline != null) {
                isoline = (Isoline)isoline.clone(); // Remind: underlying data are shared, not cloned.
                isoline.setCoordinateSystem(getCoordinateSystem());
                numPoints = isoline.getPointCount();
            } else {
                numPoints = 0;
            }
            this.isoline = isoline;
            clearCache();
            updatePreferences();
        }
        listeners.firePropertyChange("isoline", oldIsoline, isoline);
    }

    /**
     * Set the rendering coordinate system for this layer.
     *
     * @param  cs The coordinate system.
     * @throws TransformException If <code>cs</code> if the isoline
     *         can't be projected to the specified coordinate system.
     */
    protected void setCoordinateSystem(final CoordinateSystem cs) throws TransformException {
        synchronized (getTreeLock()) {
            if (isoline != null) {
                isoline.setCoordinateSystem(cs);
            }
            super.setCoordinateSystem(cs);
            clearCache();
            updatePreferences();
        }
    }
 
    /**
     * Compute the preferred area and the preferred pixel size.
     */
    private void updatePreferences() {
        assert Thread.holdsLock(getTreeLock());
        if (isoline == null) {
            preferredArea = null;
            preferredPixelSize = null;
            return;
        }
        final Rectangle2D  bounds = isoline.getBounds2D();
        final Statistics resStats = isoline.getResolution();
        if (resStats != null) {
            final double dx,dy;
            final double resolution = resStats.mean();
            Ellipsoid ellipsoid = CTSUtilities.getHeadGeoEllipsoid(isoline.getCoordinateSystem());
            if (ellipsoid != null) {
                // Transforms the resolution into a pixel size in the middle of 'bounds'.
                // Note: 'r' is the inverse of **apparent** ellipsoid's radius at latitude 'y'.
                //       For the inverse of "real" radius, we would have to swap sin and cos.
                final double   y = Math.toRadians(bounds.getCenterY());
                final double sin = Math.sin(y);
                final double cos = Math.cos(y);
                final double   r = XMath.hypot(sin/ellipsoid.getSemiMajorAxis(),
                                               cos/ellipsoid.getSemiMinorAxis());
                dy = Math.toDegrees(resolution*r);
                dx = dy*cos;
            } else {
                dx = dy = resolution;
            }
            preferredPixelSize = new XDimension2D.Double(TICKNESS*dx , TICKNESS*dy);
        }
        preferredArea = bounds;
    }

    /**
     * Sets the contouring color or paint.
     * This paint will be used by all polygons.
     */
    public void setContour(final Paint paint) {
        contour = paint;
    }

    /**
     * Returns the contouring color or paint.
     */
    public Paint getContour() {
        return contour;
    }

    /**
     * Sets the filling color or paint. This paint
     * will be used only for closed polygons.
     */
    public void setForeground(final Paint paint) {
        foreground = paint;
    }

    /**
     * Returns the filling color or paint.
     */
    public Paint getForeground() {
        return foreground;
    }

    /**
     * Set the background color or paint. This information is needed in
     * order to allows <code>RenderedIsoline</code> to fill holes correctly.
     */
    public void setBackground(final Paint paint) {
        background = paint;
    }

    /**
     * Returns the background color or paint.
     */
    public Paint getBackground() {
        return background;
    }

    /**
     * Returns the preferred area for this layer. If no preferred area has been explicitely
     * set, then this method returns the isoline's bounding box.
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
     * Returns the preferred pixel size in rendering coordinates. If no preferred pixel size
     * has been explicitely set, then this method returns the isoline's pixel size.
     */
    public Dimension2D getPreferredPixelSize() {
        synchronized (getTreeLock()) {
            final Dimension2D size = super.getPreferredPixelSize();
            if (size != null) {
                return size;
            }
            return (preferredPixelSize!=null) ? (Dimension2D) preferredPixelSize.clone() : null;
        }
    }

    /**
     * Returns the <var>z-order</var> for this layer. Layers with highest <var>z-order</var>
     * will be painted on top of layers with lowest <var>z-order</var>. If no order has been
     * explicitely set, then the default <var>z-order</var> is {@link Isoline#value}.
     */
    public float getZOrder() {
        synchronized (getTreeLock()) {
            if (isoline==null || isZOrderSet()) {
                return super.getZOrder();
            }
            return isoline.value;
        }
    }

    /**
     * The renderer for polygons. An instance of this class is attached to each instance
     * of {@link RenderedIsoline} when its <code>paint(...)</code> method is invoked for
     * the first time.  The <code>paint(...)</code> must initialize the fields before to
     * renderer polygons, and reset them to <code>null</code> once the rendering is completed.
     *
     * @version $Id: RenderedIsoline.java,v 1.15 2003/05/12 22:26:06 desruisseaux Exp $
     * @author Martin Desruisseaux
     */
    private final class IsolineRenderer implements Polygon.Renderer {
        /**
         * The minimum and maximum rendering resolution
         * in units of the isoline's coordinate system.
         */
        protected float minResolution, maxResolution;

        /**
         * The {@link Graphics2D} handler where to draw polygons.
         * Should be <code>null</code> once the rendering is completed.
         */
        protected Graphics2D graphics;

        /**
         * Returns the clip area in units of the isoline's coordinate system.
         * This is usually "real world" metres or degrees of latitude/longitude.
         */
        public Shape getClip() {
            return graphics.getClip();
        }
        
        /**
         * Returns the rendering resolution, in units of the isoline's coordinate system
         * (usually metres or degrees).  A larger resolution speed up rendering, while a
         * smaller resolution draw more precise map.
         *
         * @param  current The current rendering resolution.
         * @return the <code>current</code> rendering resolution if it still good enough,
         *         or a new resolution if a change is needed.
         */
        public float getRenderingResolution(float resolution) {
            if (resolution>=minResolution && resolution<=maxResolution) {
                return resolution;
            }
            return (minResolution + maxResolution)/2;
        }
        
        /**
         * Draw or fill a polygon. This polygon expose some internal state of {@link Isoline}.
         * <strong>Do not modify it, neither keep a reference to it after this method call</strong>
         * in order to avoid unexpected behaviour.
         */
        public void paint(final Polygon polygon) {
            RenderedIsoline.this.paint(graphics, polygon);
        }

        /**
         * Invoked once after a series of polygons has been painted.
         *
         * @param rendered The total number of <em>rendered</em> points.
         * @param recomputed The number of points that has been recomputed
         *        (i.e. decompressed, decimated, projected and transformed).
         * @param resolution The mean resolution of rendered polygons.
         */
        public void paintCompleted(final int rendered, final int recomputed, double resolution) {
            renderer.statistics.addIsoline(numPoints, rendered, recomputed, resolution);
        }
    }
        
    /**
     * Invoked automatically when a polygon is about to be draw. The default implementation
     * draw or fill the polygon according the current {@linkplain #getBackground background},
     * {@linkplain #getForeground foreground} and other settings.
     *
     * @param graphics The graphics in which to draw.
     * @param polygon The polygon to draw. This polygon may exposes some internal state of
     *        {@link Isoline}, for example decimation and clipping. <strong>Do not modify
     *        this polygon, neither keep a reference to it after this method call</strong>
     *        in order to avoid unexpected behaviour.
     */
    protected void paint(final Graphics2D graphics, final Polygon polygon) {
        final InteriorType type = polygon.getInteriorType();
        if (InteriorType.ELEVATION.equals(type)) {
            graphics.setPaint(foreground);
            graphics.fill(polygon);
            if (foreground.equals(contour)) {
                return;
            }
        } else if (InteriorType.DEPRESSION.equals(type)) {
            graphics.setPaint(background);
            graphics.fill(polygon);
            if (background.equals(contour)) {
                return;
            }
        }
        graphics.setPaint(contour);
        graphics.draw(polygon);
    }

    /**
     * Draw the isoline.
     *
     * @param  context The set of transformations needed for transforming geographic
     *         coordinates (<var>longitude</var>,<var>latitude</var>) into pixels coordinates.
     * @throws TransformException If a transformation failed.
     */
    protected void paint(final RenderingContext context) throws TransformException {
        assert Thread.holdsLock(getTreeLock());
        if (isoline == null) {
            return;
        }
        /*
         * If the rendering coordinate system changed since last
         * time, then reproject the isoline and flush the cache.
         */
        CoordinateSystem isolineCS = isoline.getCoordinateSystem();
        if (!context.mapCS.equals(isolineCS, false)) {
            isoline.setCoordinateSystem(context.mapCS);
            isolineCS = isoline.getCoordinateSystem();
            clearCache();
        }
        /*
         * Rendering acceleration: First performs the clip (if enabled),
         * then compute the decimation to use.
         */
        final Rectangle2D  bounds = isoline.getBounds2D();
        final AffineTransform  tr = context.getAffineTransform(context.mapCS, context.textCS);
        final Isoline      toDraw = getIsoline(context.getPaintingArea(isolineCS).getBounds2D());
        if (toDraw != null) {
            final Graphics2D graphics = context.getGraphics();
            final Paint      oldPaint = graphics.getPaint();
            final Stroke    oldStroke = graphics.getStroke();
            final Ellipsoid ellipsoid = CTSUtilities.getHeadGeoEllipsoid(isolineCS);
            double r; // Estimation of the "real world" length (usually in meters) of one pixel.
            if (ellipsoid != null) {
                final Unit xUnit = isolineCS.getUnits(0);
                final Unit yUnit = isolineCS.getUnits(1);
                final double  R2 = 0.70710678118654752440084436210485; // sqrt(0.5)
                final double   x = Unit.DEGREE.convert(bounds.getCenterX(), xUnit);
                final double   y = Unit.DEGREE.convert(bounds.getCenterY(), yUnit);
                final double  dx = Unit.DEGREE.convert(R2/XAffineTransform.getScaleX0(tr), xUnit);
                final double  dy = Unit.DEGREE.convert(R2/XAffineTransform.getScaleY0(tr), yUnit);
                assert !Double.isNaN( x) && !Double.isNaN( y) : bounds;
                assert !Double.isNaN(dx) && !Double.isNaN(dy) : tr;
                r = ellipsoid.orthodromicDistance(x-dx, y-dy, x+dy, y+dy);
            } else {
                // Assume a cartesian coordinate system.
                final double R2 = 1.4142135623730950488016887242097; // sqrt(2)
                r = R2/Math.sqrt((r=tr.getScaleX())*r + (r=tr.getScaleY())*r +
                                 (r=tr.getShearX())*r + (r=tr.getShearY())*r);
                assert !Double.isNaN(r) : tr;
            }
            if (isolineRenderer == null) {
                isolineRenderer = new IsolineRenderer();
            }
            isolineRenderer.minResolution = (float)(renderer.minResolution*r);
            isolineRenderer.maxResolution = (float)(renderer.maxResolution*r);
            isolineRenderer.graphics      = graphics;
            try {
                toDraw.paint(isolineRenderer);
            } finally {
                isolineRenderer.graphics = null;
            }
            graphics.setStroke(oldStroke);
            graphics.setPaint (oldPaint);
        }
        context.addPaintedArea(XAffineTransform.transform(tr, bounds, bounds), context.textCS);
    }

    /**
     * Returns an isoline approximatively clipped to the specified area. The clip is
     * approximative in that the resulting isoline may extends outside the clip area.
     * However, this method garanteed that the clipped isoline will contains at least
     * the interior of the clip area, providing that the "master" isoline cover this
     * area.
     *
     * @param  The clip area, in this {@linkplain #getCoordinateSystem isoline's
     *         coordinate system}. Note: this rectangle will be overwritten with
     *         a bigger one.
     * @return An isoline, or <code>null</code> if no isoline intercepts the clip.
     */
    private Isoline getIsoline(final Rectangle2D clip) {
        if (clipped == null) {
            return isoline;
        }
        final double clipArea = clip.getWidth()*clip.getHeight();
        scale(clip, CLIP_EPS);
        Isoline     bestIsoline = isoline;
        Rectangle2D bestBounds  = bestIsoline.getBounds2D();
        double      bestRatio   = (bestBounds.getWidth()*bestBounds.getHeight()) / clipArea;
        /*
         * Find the isoline that best matches the clipped area.
         */
        for (final Iterator it=clipped.iterator(); it.hasNext();) {
            final Isoline  candidate = (Isoline) it.next();
            final Rectangle2D bounds = candidate.getBounds2D();
            if (Renderer.contains(bounds, clip, true)) {
                final double ratio = (bounds.getWidth()*bounds.getHeight()) / clipArea;
                if (ratio < bestRatio) {
                    bestRatio   = ratio;
                    bestBounds  = bounds;
                    bestIsoline = candidate;
                }
            }
        }
        /*
         * If the isoline covers a widther area than necessary, clip it.
         */
        if (bestRatio >= CLIP_THRESHOLD*CLIP_THRESHOLD) {
            logUpdateCache("RenderedIsoline");
            scale(clip, 0.5*(CLIP_THRESHOLD+1));
            bestIsoline = bestIsoline.clip(clip);
            if (bestIsoline!=null && CLIP_THRESHOLD>1) {
                clipped.add(bestIsoline);
                while (clipped.size() >= CLIP_CACHE_SIZE) {
                    clipped.remove(0);
                }
            }
        }
        return bestIsoline;
    }

    /**
     * Expand or shrunk a rectangle by some factor. A scale of 1 lets the rectangle
     * unchanged. A scale of 2 make the rectangle two times wider and heigher. In
     * any case, the rectangle's center doesn't move.
     */
    private static void scale(final Rectangle2D rect, final double scale) {
        final double trans  = 0.5*(scale-1);
        final double width  = rect.getWidth();
        final double height = rect.getHeight();
        rect.setRect(rect.getX()-trans*width,
                     rect.getY()-trans*height,
                     scale*width, scale*height);
    }

    /**
     * Returns a tool tip text for the specified coordinates.
     * The default implementation delegates to {@link Isoline#getPolygonName}.
     *
     * @param  event The mouve event with geographic coordinétes.
     * @return The tool tip text, or <code>null</code> if there
     *         in no tool tips for this location.
     */
    String getToolTipText(final GeoMouseEvent event) {
        if (isoline != null) {
            final Point2D point = event.getMapCoordinate(null);
            if (point != null) {
                final String toolTips = isoline.getPolygonName(point, getLocale());
                if (toolTips != null) {
                    return toolTips;
                }
            }
        }
        return super.getToolTipText(event);
    }

    /**
     * Discard cached data. Invoking this method should not affect
     * the rendering appearance, but may slow down the next rendering.
     */
    void clearCache() {
        if (clipped != null) {
            clipped.clear();
        }
        isolineRenderer = null;
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
            numPoints  = 0;
            isoline    = null;
            contour    = Color.BLACK;
            foreground = Color.GRAY;
            background = Color.WHITE;
            super.dispose();
        }
    }
}
