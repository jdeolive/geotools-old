/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
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
import java.awt.TexturePaint;
import javax.swing.UIManager;
import javax.media.jai.GraphicsJAI;

// Collections
import java.util.List;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Collection;

// Geotools dependencies
import org.geotools.units.Unit;
import org.geotools.math.Statistics;
import org.geotools.cs.Ellipsoid;
import org.geotools.cs.CoordinateSystem;
import org.geotools.ct.TransformException;
import org.geotools.units.UnitException;
import org.geotools.renderer.geom.Clipper;
import org.geotools.renderer.geom.Polyline;
import org.geotools.renderer.geom.Geometry;
import org.geotools.renderer.geom.GeometryCollection;
import org.geotools.renderer.style.Style2D;
import org.geotools.renderer.style.Style;
import org.geotools.resources.XMath;
import org.geotools.resources.XDimension2D;
import org.geotools.resources.XAffineTransform;
import org.geotools.resources.CTSUtilities;


/**
 * A layer for a {@link GeometryCollection} object. Instances of this class are typically
 * used for isobaths. Each isobath (e.g. sea-level, 50 meters, 100 meters...) may be rendererd
 * with an instance of <code>RenderedGeometries</code>.
 *
 * @version $Id: RenderedGeometries.java,v 1.6 2003/06/10 11:30:27 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public class RenderedGeometries extends RenderedLayer {
    /**
     * The maximum number of clipped geometries to cache. This number can be set to <code>0</code>
     * for disabling clipping acceleration, which may be useful if a bug is suspected to prevent
     * proper rendering.
     */
    private static final int CLIP_CACHE_SIZE = 8;

    /**
     * The threshold ratio for computing a new clip. When the width and height of the underlying
     * {@link #geometry} bounding box divided by the width and height of the clip area is greater
     * than this threshold, a new clip is performed for faster rendering.
     *
     * DEBUGGING TIPS: Set this scale to a value below 1 to <em>see</em> the clipping's
     *                 effect in the window area.
     */
    private static final double CLIP_THRESHOLD = 4;

    /**
     * A factor slightly greater than 1 for computing the minimum size required for accepting
     * a clipped geometry. A greater geometry's bounding box is necessary since a bounding box
     * too close to the clip's bounding box will make geometry contour apparent.
     */
    private static final double CLIP_EPS = 1.05;

    /**
     * Default color for fills.
     */
    private static final Color FILL_COLOR = new Color(246,212,140);

    /**
     * The "preferred line tickness" relative to the geometry's resolution.
     * A value of 1 means that geometry might be drawn with a line as tick
     * as the geometry's resolution. A value of 0.25 means that geometry might
     * be drawn with a line of tickness equals to 1/4 of the geometry's resolution.
     */
    private static final double TICKNESS = 0.25;

    /**
     * The geometry data.
     * The {@linkplain GeometryCollection#getCoordinateSystem geometry's coordinate system}
     * should matches the {@linkplain #getCoordinateSystem rendering coordinate system}.
     */
    private GeometryCollection geometry;

    /**
     * List of clipped geometries. A clipped geometry may be faster to renderer
     * than the full geometry. Most rencently used geometries are last in this list.
     */
    private final List clipped = (CLIP_CACHE_SIZE!=0) ? new LinkedList() : null;

    /**
     * Color for contour lines. Default to panel's foreground (usually black).
     */
    private Paint contour = UIManager.getColor("Panel.foreground");

    /**
     * Paint for filling polygons.
     */
    private Paint foreground = FILL_COLOR;

    /**
     * The number of points in {@link #geometry}. For statistics purpose only.
     */
    private int numPoints;

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
     * The minimum and maximum resolution used the last time the map was rendered.
     * The actual map resolution should be between those two values. Those values
     * are in geometry units (usually meters)  and are updated every time the map
     * is rendered. They are related to {@link Renderer#minResolution} and {@link
     * Renderer#maxResolution}, except that the later are in device units (usually
     * pixels).
     *
     * @task REVISIT: consider moving them into {@link Renderer}, since they are going
     *                to be the same for every {@link RenderedLayer}.
     */
    private transient float minResolution, maxResolution;

    /**
     * <code>true</code> if {@linkplain #paint(Graphics2D,Shape,Style2D) polygon rendering} uses
     * the &quot;real world&quot; coordinate system, or <code>false</code> if it uses the device
     * coordinate system. If <code>true</code>, then {@linkplain Stroke stroke} and {@linkplain
     * TexturePaint texture} attributes are in &quot;real world&quot; units (usually meters);
     * otherwise, they are in pixels.
     */
    private boolean renderUsingMapCS = true;

    /**
     * A shape wrapping a {@link Geometry} with a given {@link AffineTransform}. Used only
     * for rendering geometries when {@link #renderUsingMapCS} is <code>false</code>, null
     * otherwise. Its value is updated in the {@link #paint(RenderingContext)} method only
     * in order to protect it from <code>renderUsingMapCS</code> change while painting.
     */
    private transient TransformedShape transformedShape;

    /**
     * Construct a layer for the specified geometry.
     *
     * @param geometry The geometry, or <code>null</code> if none.
     * @see #setGeometry
     */
    public RenderedGeometries(final GeometryCollection geometry) {
        if (geometry!=null) try {
            setCoordinateSystem(geometry.getCoordinateSystem());
            setGeometry(geometry);
        } catch (TransformException exception) {
            /*
             * Should not happen, since geometry use 2D coordinate systems. Rethrow it as an
             * illegal argument exception, which is not too far from the reality: the geometry
             * is not of the usual class.
             */
            final IllegalArgumentException e;
            e = new IllegalArgumentException(exception.getLocalizedMessage());
            e.initCause(exception);
            throw e;
        }
    }

    /**
     * Set a new geometry for this layer.
     *
     * @param  geometry The new geometry, or <code>null</code> if none.
     * @throws TransformException if the geometry can't be projected in the
     *         {@linkplain #getCoordinateSystem rendering coordinate system}.
     */
    public void setGeometry(GeometryCollection geometry) throws TransformException {
        final GeometryCollection oldGeometry;
        synchronized (getTreeLock()) {
            oldGeometry = this.geometry;
            if (geometry != null) {
                // Remind: underlying data are shared, not cloned.
                geometry = (GeometryCollection)geometry.clone();
                geometry.setCoordinateSystem(getCoordinateSystem());
                numPoints = geometry.getPointCount();
            } else {
                numPoints = 0;
            }
            this.geometry = geometry;
            clearCache();
            updatePreferences(false);
        }
        listeners.firePropertyChange("geometry", oldGeometry, geometry);
    }

    /**
     * Set the rendering coordinate system for this layer.
     *
     * @param  cs The coordinate system.
     * @throws TransformException if the geometry can't be projected
     *         to the specified coordinate system.
     */
    protected void setCoordinateSystem(final CoordinateSystem cs) throws TransformException {
        synchronized (getTreeLock()) {
            if (geometry != null) {
                geometry.setCoordinateSystem(cs);
            }
            super.setCoordinateSystem(cs);
            clearCache();
            updatePreferences(false);
        }
    }
 
    /**
     * Compute the preferred area and the preferred pixel size.
     *
     * @param updateResolution <code>true</code> for updating the {@link #preferredPixelSize}
     *        property as well.
     */
    private void updatePreferences(final boolean updateResolution) {
        assert Thread.holdsLock(getTreeLock());
        preferredArea = null;
        preferredPixelSize = null;
        if (geometry == null) {
            return;
        }
        final Rectangle2D bounds = geometry.getBounds2D();
        if (updateResolution) {
            final Statistics resStats = geometry.getResolution();
            if (resStats != null) {
                final double dx,dy;
                final double resolution = resStats.mean();
                Ellipsoid ellipsoid = CTSUtilities.getHeadGeoEllipsoid(geometry.getCoordinateSystem());
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
        }
        preferredArea = bounds;
    }

    /**
     * Sets the contouring color or paint. This is the default paint
     * to use when no styling information is provided for a polyline.
     */
    public void setContour(final Paint paint) {
        contour = paint;
    }

    /**
     * Returns the default contouring color.
     */
    public Paint getContour() {
        return contour;
    }

    /**
     * Sets the filling color or paint. This is the default paint
     * to use when no styling information is provided for a polyline.
     */
    public void setForeground(final Paint paint) {
        foreground = paint;
    }

    /**
     * Returns the default filling color or paint.
     */
    public Paint getForeground() {
        return foreground;
    }

    /**
     * Returns the preferred area for this layer. If no preferred area has been
     * explicitely set, then this method returns the geometry's bounding box.
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
     * has been explicitely set, then this method returns the geometry's pixel size.
     */
    public Dimension2D getPreferredPixelSize() {
        synchronized (getTreeLock()) {
            final Dimension2D size = super.getPreferredPixelSize();
            if (size != null) {
                return size;
            }
            if (preferredPixelSize == null) {
                updatePreferences(true);
            }
            return (preferredPixelSize!=null) ? (Dimension2D) preferredPixelSize.clone() : null;
        }
    }

    /**
     * Returns the <var>z-order</var> for this layer. Layers with highest <var>z-order</var>
     * will be painted on top of layers with lowest <var>z-order</var>. If no order has been
     * explicitely set, then the default <var>z-order</var> is
     * {@link GeometryCollection#getValue}.
     */
    public float getZOrder() {
        synchronized (getTreeLock()) {
            if (geometry==null || isZOrderSet()) {
                return super.getZOrder();
            }
            final float z = geometry.getValue();
            return Float.isNaN(z) ? 0 : z;
        }
    }

    /**
     * Returns <code>true</code> if {@linkplain #paint(Graphics2D,Shape,Style2D) polygon rendering}
     * uses the &quot;real world&quot; coordinate system, or <code>false</code> if it uses the
     * output device coordinate system. If <code>true</code>, then {@linkplain Stroke stroke} and
     * {@linkplain TexturePaint texture} attributes are in &quot;real world&quot; units (usually
     * meters); otherwise, they are in device units (usually pixels).
     */
    protected boolean getRenderUsingMapCS() {
        return renderUsingMapCS;
    }

    /**
     * Specifies if {@linkplain #paint(Graphics2D,Shape,Style2D) polygon rendering} uses the
     * &quot;real world&quot; coordinate system. If <code>true</code>, then {@linkplain Stroke
     * stroke} and {@linkplain TexturePaint texture} attributes are in &quot;real world&quot;
     * units (usually meters); otherwise, they are in device units (usually pixels). When using
     * &quot;real world&quot; units, the visual line width will changes with zoom. When using
     * output device units, the visual line width are constant under any zoom.
     */
    protected void setRenderUsingMapCS(final boolean renderUsingMapCS) {
        final boolean oldValue;
        synchronized (getTreeLock()) {
            oldValue = this.renderUsingMapCS;
            this.renderUsingMapCS = renderUsingMapCS;
        }
        listeners.firePropertyChange("renderUsingMapCS", oldValue, renderUsingMapCS);
    }

    /**
     * Gets the style from a geometry object, or <code>null</code> if none. If the geometry style
     * is not an instance of of {@link Style2D} (for example if it came from a renderer targeting
     * an other output device), set it to <code>null</code> in order to lets the garbage collector
     * do its work. It will not hurt the foreigner rendering device, since the constructor cloned
     * the geometries.
     */
    private static Style2D getStyle(final Geometry geometry, final Style2D fallback) {
        final Style style = geometry.getStyle();
        if (style != null) {
            if (style instanceof Style2D) {
                return (Style2D) style;
            }
            geometry.setStyle(null);
        }
        return fallback;
    }

    /**
     * Invoked automatically when a polyline is about to be draw. The default implementation
     * draw or fill the polyline according the current {@linkplain #getForeground foreground}
     * color.
     *
     * @param graphics The graphics in which to draw.
     * @param polyline The polyline to draw.
     * @param style    The style to apply, or <code>null</code> if none.
     */
    protected void paint(final Graphics2D graphics, final Shape polyline, final Style2D style) {
        // HACK: In a future version, the shape may not be a polyline instance.
        //       We should never cast to Polyline.
        if ((polyline instanceof Polyline) && ((Polyline)polyline).isClosed()) {
            graphics.setPaint(foreground);
            graphics.fill(polyline);
            if (foreground.equals(contour)) {
                return;
            }
        }
        graphics.setPaint(contour);
        graphics.draw(polyline);
    }

    /*
     * Recursively draw all geometries in the given collection. If one geometry is itself a
     * collection, then this method will be invoked again in order to draw the geometries in
     * the "child" collection which is in the "parent" collection.
     */
    private void paint(final Graphics2D       graphics,
                       final Shape                clip,
                       final GeometryCollection toDraw,
                       final Style2D      defaultStyle)
    {
        double meanResolution = 0;
        int rendered=0, recomputed=0;
        final boolean loggable = renderer.statistics.isLoggable();
        final Collection polylines = toDraw.getGeometries();
        for (final Iterator it=polylines.iterator(); it.hasNext();) {
            final Geometry geometry = (Geometry)it.next();
            if (clip.intersects(geometry.getBounds2D())) {
                final Style2D style = getStyle(geometry, defaultStyle);
                if (geometry instanceof GeometryCollection) {
                    paint(graphics, clip, (GeometryCollection)geometry, style);
                } else {
                    /*
                     * Now draw the polyline. If polyline's rendering resolution is not in
                     * current bounds, then a new rendering resolution will be set,  which
                     * will probably flush the cache.
                     */
                    float resolution = geometry.getRenderingResolution();
                    if (!(resolution>=minResolution && resolution<=maxResolution)) {
                        resolution = (minResolution + maxResolution)/2;
                        geometry.setRenderingResolution(resolution);
                    }
                    Shape shape = geometry;
                    if (transformedShape != null) {
                        transformedShape.shape = shape;
                        shape = transformedShape;
                    }
                    paint(graphics, shape, style);
                    if (loggable && geometry instanceof Polyline) {
                        final int numPts = ((Polyline)geometry).getCachedPointCount();
                        rendered += Math.abs(numPts);
                        if (numPts < 0) {
                            recomputed -= numPts;
                        }
                        meanResolution += resolution * Math.abs(numPts);
                    }
                }
            }
        }
        if (rendered != 0) {
            meanResolution /= rendered;
            renderer.statistics.addGeometry(0, rendered, recomputed, meanResolution);
        }
    }

    /**
     * Draw the geometry.
     *
     * @param  context The set of transformations needed for transforming geographic
     *         coordinates (<var>longitude</var>,<var>latitude</var>) into pixels coordinates.
     * @throws TransformException If a transformation failed.
     */
    protected void paint(final RenderingContext context) throws TransformException {
        assert Thread.holdsLock(getTreeLock());
        if (geometry == null) {
            return;
        }
        /*
         * If the rendering coordinate system changed since last
         * time, then reproject the geometry and flush the cache.
         */
        CoordinateSystem geometryCS = geometry.getCoordinateSystem();
        if (!context.mapCS.equals(geometryCS, false)) {
            geometry.setCoordinateSystem(context.mapCS);
            geometryCS = geometry.getCoordinateSystem();
            clearCache();
        }
        /*
         * Rendering acceleration: First performs the clip (if enabled), then compute the
         * decimation to use.  The decimation is computed assuming a cartesian coordinate
         * system even if the output CS is actually a geographic one. Distance in degrees
         * is not really meaningful on a mathematical point of view, but we use it because
         * it is was the user see.
         */
        final Rectangle2D        bounds = geometry.getBounds2D();
        final AffineTransform        tr = context.getAffineTransform(context.mapCS, context.textCS);
        final GeometryCollection toDraw = getGeometry(
                                     context.getPaintingArea(geometryCS).getBounds2D(), geometryCS);
        if (toDraw != null) {
            final Graphics2D   graphics = context.getGraphics();
            final Paint        oldPaint = graphics.getPaint();
            final Stroke      oldStroke = graphics.getStroke();
            final Shape            clip = graphics.getClip();
            if (clip.intersects(toDraw.getBounds2D())) {
                final double R2 = 1.4142135623730950488016887242097; // sqrt(2)
                double r = R2/Math.sqrt((r=tr.getScaleX())*r + (r=tr.getScaleY())*r +
                                        (r=tr.getShearX())*r + (r=tr.getShearY())*r);
                assert !Double.isNaN(r) : tr;
                minResolution = (float)(renderer.minResolution*r);
                maxResolution = (float)(renderer.maxResolution*r);
                /*
                 * If the rendering coordinate system is geographic, then the resolution computed
                 * above do not use linear units. Computes an approximative correction factor.
                 * This factor has no impact on the rendering appareance. It is used for
                 * statistics purpose only, usually for logging.
                 */
                if (renderer.statistics.isLoggable()) {
                    Unit unit;
                    final Unit xUnit = geometryCS.getUnits(0);
                    final Unit yUnit = geometryCS.getUnits(1);
                    final Ellipsoid ellipsoid = CTSUtilities.getHeadGeoEllipsoid(geometryCS);
                    if (ellipsoid != null) {
                        final double R2I = 0.70710678118654752440084436210485; // sqrt(0.5)
                        final double   x = Unit.DEGREE.convert(bounds.getCenterX(), xUnit);
                        final double   y = Unit.DEGREE.convert(bounds.getCenterY(), yUnit);
                        final double  dx = Unit.DEGREE.convert(R2I/XAffineTransform.getScaleX0(tr), xUnit);
                        final double  dy = Unit.DEGREE.convert(R2I/XAffineTransform.getScaleY0(tr), yUnit);
                        assert !Double.isNaN( x) && !Double.isNaN( y) : bounds;
                        assert !Double.isNaN(dx) && !Double.isNaN(dy) : tr;
                        r = ellipsoid.orthodromicDistance(x-dx, y-dy, x+dy, y+dy) / (r*1000);
                        unit = Unit.KILOMETRE;
                    } else try {
                        unit = Unit.KILOMETRE;
                        r = XMath.hypot(unit.convert(1, xUnit), unit.convert(1, yUnit));
                    } catch (UnitException exception) {
                        r = 1; // Not a linear unit. Not a big deal, since it is just for logging.
                        unit = xUnit;
                    }
                    renderer.statistics.setResolutionScale(r, unit);
                }
                /*
                 * Now recursively draw all polylines. If rendering must be done in output device
                 * units (usually pixels),  then the transformation from map to device units must
                 * be done in an intermediate shape. Since the intermediate shape just concatenates
                 * affine transforms and pass it to Geometry.getPathIterator(...), the cached
                 * geometry data are exactly the same. This intermediate step should not have any
                 * noticeable impact on performance or memory usage.
                 */
                if (renderUsingMapCS) {
                    transformedShape = null;
                } else {
                    if (transformedShape == null) {
                        transformedShape = new TransformedShape();
                    }
                    transformedShape.setTransform(graphics.getTransform());
                    context.setCoordinateSystem(context.textCS);
                    graphics.setStroke(DEFAULT_STROKE);
                }
                try {
                    paint(graphics, clip, toDraw, getStyle(toDraw, null));
                } finally {
                    if (transformedShape != null) {
                        transformedShape.shape = null;
                        context.setCoordinateSystem(context.mapCS);
                    }
                    graphics.setStroke(oldStroke);
                    graphics.setPaint (oldPaint);
                }
            }
        }
        context.addPaintedArea(XAffineTransform.transform(tr, bounds, null), context.textCS);
        renderer.statistics.addGeometry(numPoints, 0, 0, 0);
    }

    /**
     * Returns an geometry approximatively clipped to the specified area. The clip is
     * approximative in that the resulting geometry may extends outside the clip area.
     * However, this method garanteed that the clipped geometry will contains at least
     * the interior of the clip area, providing that the "master" geometry cover this
     * area.
     *
     * @param  The clip area, in this {@linkplain #getCoordinateSystem geometry's
     *         coordinate system}. Note: this rectangle will be overwritten with
     *         a bigger one.
     * @return An geometry, or <code>null</code> if no geometry intercepts the clip.
     * @throws TransformException if a transform was required and failed.
     */
    private GeometryCollection getGeometry(final Rectangle2D clip, final CoordinateSystem clipCS)
            throws TransformException
    {
        if (clipped == null) {
            return geometry;
        }
        final double clipArea = clip.getWidth()*clip.getHeight();
        scale(clip, CLIP_EPS);
        GeometryCollection bestGeometry = geometry;
        Rectangle2D bestBounds  = bestGeometry.getBounds2D();
        double      bestRatio   = (bestBounds.getWidth()*bestBounds.getHeight()) / clipArea;
        /*
         * Find the geometry that best matches the clipped area.
         */
        for (final Iterator it=clipped.iterator(); it.hasNext();) {
            final GeometryCollection candidate = (GeometryCollection) it.next();
            final Rectangle2D bounds = candidate.getBounds2D();
            if (Renderer.contains(bounds, clip, true)) {
                final double ratio = (bounds.getWidth()*bounds.getHeight()) / clipArea;
                if (ratio < bestRatio) {
                    bestRatio    = ratio;
                    bestBounds   = bounds;
                    bestGeometry = candidate;
                }
            }
        }
        /*
         * If the geometry covers a widther area than necessary, clip it.
         */
        if (bestRatio >= CLIP_THRESHOLD*CLIP_THRESHOLD) {
            logUpdateCache("RenderedGeometries");
            scale(clip, 0.5*(CLIP_THRESHOLD+1));
            final Geometry candidate = bestGeometry.clip(new Clipper(clip, clipCS));
            if (candidate==null || candidate instanceof GeometryCollection) {
                bestGeometry = (GeometryCollection) candidate;
            } else {
                // TODO: We should modify RenderedGeometries in order to work
                //       directly with Geometry rather than GeometryCollection.
                bestGeometry = new GeometryCollection();
                bestGeometry.add(candidate);
            }
            if (bestGeometry!=null && CLIP_THRESHOLD>1) {
                clipped.add(bestGeometry);
                while (clipped.size() >= CLIP_CACHE_SIZE) {
                    clipped.remove(0);
                }
            }
        }
        return bestGeometry;
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
     * The default implementation delegates to {@link GeometryCollection#getPolygonName}.
     *
     * @param  event The mouve event with geographic coordinétes.
     * @return The tool tip text, or <code>null</code> if there
     *         in no tool tips for this location.
     */
    String getToolTipText(final GeoMouseEvent event) {
        if (geometry != null) {
            final Point2D point = event.getMapCoordinate(null);
            if (point != null) {
                final String toolTips = geometry.getPolygonName(point, getLocale());
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
        transformedShape = null;
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
            geometry   = null;
            contour    = Color.BLACK;
            foreground = Color.GRAY;
            super.dispose();
        }
    }
}
