/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Management Committee (PMC)
 * (C) 2000, Institut de Recherche pour le Développement
 * (C) 1999, Pêches et Océans Canada
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
package org.geotools.gui.swing;

// Graphics and geometry
import java.awt.Font;
import java.awt.Shape;
import java.awt.Paint;
import java.awt.Color;
import java.awt.Stroke;
import java.awt.Rectangle;
import java.awt.Graphics2D;
import java.awt.BasicStroke;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;

// Components and events
import java.awt.Container;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentAdapter;

// Collections
import java.util.Set;
import java.util.Map;
import java.util.List;
import java.util.Arrays;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.NoSuchElementException;

// Logging and miscellaneous
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.LogRecord;
import javax.vecmath.MismatchedSizeException;

// Geotools dependencies
import org.geotools.axis.Axis2D;
import org.geotools.axis.Graduation;
import org.geotools.axis.AbstractGraduation;
import org.geotools.renderer.array.GenericArray;
import org.geotools.resources.XMath;


/**
 * A lightweight widget displaying two axis and an arbitrary amount of data series
 * with zoom capability. Axis may have arbitrary orientation (they don't need to be
 * perpendicular). This widget is not a replacement for full featured toolkit like
 * <A HREF="http://jgraph.sourceforge.net/">JGraph</A>; it just provides a mean to
 * quickly display a time serie.
 * <br><br>
 * Axis color and font can bet set with {@link #setForeground} and {@link #setFont}.
 * A scroll pane can be created with {@link #createScrollPane}.
 *
 * <p>&nbsp;</p>
 * <p align="center"><img src="doc-files/Plot2D.png"></p>
 * <p>&nbsp;</p>
 *
 * @version $Id: Plot2D.java,v 1.6 2003/07/23 14:17:11 desruisseaux Exp $
 * @author Martin Desruisseaux
 *
 * @see <A HREF="http://jgraph.sourceforge.net/">JGraph</A>
 */
public class Plot2D extends ZoomPane {
    /**
     * The default stroke for painting axis.
     */
    private static final Stroke DEFAULT_STROKE = new BasicStroke(0);

    /**
     * Default cycle of colors.
     */
    private static final Color[] DEFAULT_COLORS = new Color[] {
        Color.BLUE, Color.RED, Color.ORANGE
    };

    /**
     * The axis for a given series. Instance of this class are used as value in the {@link
     * Plot2D#series} map. The <var>x</var> and <var>y</var> axis in this <code>Entry</code>
     * <strong>must</strong> be listed in {@link Plot2D#xAxis} and {@link Plot2D#yAxis} as
     * well, but those list order don't have to be the same than the {@link Plot2D#series}
     * order.
     */
    private static final class Entry implements Serializable {
        /** The <var>x</var> and <var>y</var> axis for a given series. */
        public final Axis2D xAxis, yAxis;

        /** Construct a new entry with the specified axis. */
        public Entry(final Axis2D xAxis, final Axis2D yAxis) {
            this.xAxis = xAxis;
            this.yAxis = yAxis;
        }
    }

    /**
     * The set of <var>x</var> axis. There is usually only one axis, but more axis are allowed.
     * All <code>Entry.xAxis</code> instance <strong>must</strong> appears in this list as well,
     * but not necessarly in the same order.
     *
     * @see #newAxis
     * @see #addSeries
     */
    private final List xAxis = new ArrayList(3);

    /**
     * The set of <var>y</var> axis. There is usually only one axis, but more axis are allowed.
     * All <code>Entry.yAxis</code> instance <strong>must</strong> appears in this list as well,
     * but not necessarly in the same order.
     *
     * @see #newAxis
     * @see #addSeries
     */
    private final List yAxis = new ArrayList(3);

    /**
     * The set of series to plot. Keys are {@link Series} objects while values are
     * <code>Entry</code> objects with the <var>x</var> and <var>y</var> axis to use
     * for the series.
     *
     * @see #addSeries
     */
    private final Map series = new LinkedHashMap();

    /**
     * Immutable version of <code>series</code> to be returned by {@link #getSeries}.
     *
     * @see #getSeries
     */
    private final Set unmodifiableSeries = Collections.unmodifiableSet(series.keySet());

    /**
     * The last axis added to <code>series</code>, or <code>null</code> if none.
     */
    private Entry previousAxis;

    /**
     * Title for next axis to be created for each dimension. Element 0 is the axis name for
     * <var>x</var> axis, element 1 is the axis name for <var>y</var> axis, etc. A null element
     * means that no new axis need to be created for this particular dimension. The length of
     * this array is the maximum number of dimension that this component supports.
     *
     * @see #newAxis
     */
    private final String[] nextAxis = new String[2];

    /**
     * Bounding box of data in all series, or <code>null</code> if it must be recomputed.
     */
    private transient Rectangle2D seriesBounds;

    /**
     * Margin between widget border and the drawing area.
     */
    private int top=30, bottom=60, left=60, right=30;

    /**
     * Horizontal (x) and vertival (y) offset to apply to any supplementary axis.
     */
    private int xOffset=20, yOffset=-20;

    /**
     * The widget's width and height when the graphics was rendered for the last time.
     */
    private int lastWidth, lastHeight;

    /**
     * The plot title.
     */
    private String title;

    /**
     * The title font.
     */
    private Font titleFont = new Font("SansSerif", Font.BOLD, 16);

    /**
     * Listener class for various events.
     */
    private static final class Listeners extends ComponentAdapter {
        /**
         * When resized, force the widget to layout its axis.
         */
        public void componentResized(final ComponentEvent event) {
            final Plot2D c = (Plot2D) event.getSource();
            c.layoutAxis(false);
        }
    }

    /**
     * Construct an initially empty <code>Plot2D</code> with
     * zoom capabilities on horizontal and vertical axis.
     */
    public Plot2D() {
        this(SCALE_X | SCALE_Y | TRANSLATE_X | TRANSLATE_Y | RESET);
    }

    /**
     * Construct an initially empty <code>Plot2D</code> with
     * zoom capabilities on the specified axis.
     *
     * @param zoomX <code>true</code> for allowing zooming on the <var>x</var> axis.
     * @param zoomY <code>true</code> for allowing zooming on the <var>y</var> axis.
     */
    public Plot2D(final boolean zoomX, final boolean zoomY) {
        this((zoomX ? SCALE_X | TRANSLATE_X : 0) |
             (zoomY ? SCALE_Y | TRANSLATE_Y : 0) | RESET);
    }

    /**
     * Construct an initially empty <code>Plot2D</code> with the specified zoom capacities.
     *
     * @param  zoomCapacities Allowed zoom types. It can be a
     *         bitwise combinaison of the following constants:
     *         {@link #SCALE_X SCALE_X}, {@link #SCALE_Y SCALE_Y},
     *         {@link #TRANSLATE_X TRANSLATE_X}, {@link #TRANSLATE_Y TRANSLATE_Y},
     *         {@link #ROTATE ROTATE}, {@link #RESET RESET} and {@link #DEFAULT_ZOOM DEFAULT_ZOOM}.
     * @throws IllegalArgumentException If <code>zoomCapacities</code> is invalid.
     */
    private Plot2D(final int zoomCapacities) {
        super(zoomCapacities);
        final Listeners listeners = new Listeners();
        addComponentListener(listeners);
    }

    /**
     * Advises that the next series to be added with {@link #addSeries addSeries(...)}
     * should uses a new axis for the specified dimension. Dimension 0 is for the <var>x</var>
     * axis while dimension 1 is for the <var>y</var> axis. If this method is never invoked,
     * then <code>addSeries(...)</code> will automatically create untitled axis.
     *
     * @param dimension 0 for adding a new <var>x</var> axis, or
     *                  1 for adding a new <var>y</var> axis.
     * @param title The axis title.
     */
    public void newAxis(final int dimension, String title) {
        if (title == null) {
            title = "";
        }
        if (dimension<0 || dimension>=nextAxis.length) {
            throw new IllegalArgumentException(String.valueOf(dimension));
        }
        nextAxis[dimension] = title;
    }

    /**
     * Add a new serie to the chart. The <var>x</var> and <var>y</var> vectors may be arrays of any
     * of Java primitive types: <code>double[]</code>, <code>float[]</code>, <code>long[]</code>,
     * <code>int[]</code>, <code>short[]</code>, <code>byte[]</code>, <code>char[]</code> (which
     * may be used as a kind of unsigned short) or <code>boolean[]</code> (0 or 1 values). The
     * <var>x</var> and <var>y</var> arrays doesn't need to be of the same type. Arrays are stored
     * by reference only; they are not cloned.
     *
     * @param name The series name.
     * @param x <var>x</var> values.
     * @param y <var>y</var> values.
     * @throws ClassCastException if <var>x</var> and <var>y</var> are not arrays
     *         of a primitive type.
     * @return The series added.
     * @throws MismatchedSizeException if arrays doesn't have the same length.
     */
    public Series addSeries(final String name, final Object x, final Object y)
            throws ClassCastException, MismatchedSizeException
    {
        return addSeries(new DefaultSeries(name, x, y, this.series.size()));
    }

    /**
     * Add a new serie to the chart. The <var>x</var> and <var>y</var> vectors may be arrays of any
     * of Java primitive types.
     *
     * @param name The series name.
     * @param x <var>x</var> values.
     * @param y <var>y</var> values.
     * @param lower Index of first point, inclusive.
     * @param upper Index of last point, exclusive.
     * @return The series added.
     * @throws ClassCastException if <var>x</var> and <var>y</var> are not arrays
     *         of a primitive type.
     */
    public Series addSeries(final String name, final Object x, final Object y,
                                             final int lower, final int upper)
            throws ClassCastException
    {
        return addSeries(new DefaultSeries(name, x, y, lower, upper, this.series.size()));
    }

    /**
     * Add a new serie to the chart.
     *
     * @param series The serie to add.
     * @return The series, for convenience.
     */
    public Series addSeries(final Series series) {
        final Axis2D xAxis;
        final Axis2D yAxis;
        Rectangle2D bounds = null;
        if (previousAxis==null || nextAxis[0]!=null) {
            if (bounds == null) {
                bounds = series.getBounds2D();
            }
            xAxis = new Axis2D();
            final AbstractGraduation grad = (AbstractGraduation) xAxis.getGraduation();
            grad.setMinimum(bounds.getMinX());
            grad.setMaximum(bounds.getMaxX());
            grad.setTitle(nextAxis[0]);
            this.xAxis.add(xAxis);
            nextAxis[0] = null;
        } else {
            xAxis = previousAxis.xAxis;
        }
        if (previousAxis==null || nextAxis[1]!=null) {
            if (bounds == null) {
                bounds = series.getBounds2D();
            }
            yAxis = new Axis2D();
            final AbstractGraduation grad = (AbstractGraduation) yAxis.getGraduation();
            grad.setMinimum(bounds.getMinY());
            grad.setMaximum(bounds.getMaxY());
            grad.setTitle(nextAxis[1]);
            this.yAxis.add(yAxis);
            nextAxis[1] = null;
        } else {
            yAxis = previousAxis.yAxis;
        }
        if (bounds != null) {
            previousAxis = new Entry(xAxis, yAxis);
        }
        this.series.put(series, previousAxis);
        if (title == null) {
            title = series.getName();
        }
        seriesBounds = null;
        if (bounds != null) {
            // New axis added. TODO: We should find a more flexible way.
            reset();
        } else {
            repaint();
        }
        return series;
    }

    /**
     * Returns the set of series to be draw.
     * Series are painted in the order they are returned.
     */
    public Set getSeries() {
        return unmodifiableSeries;
    }

    /**
     * Returns the {<var>x</var>, <var>y</var>} axis for the specified series.
     *
     * @param  series The series for which axis are wanted.
     * @return An array of length 2 containing <var>x</var> and <var>y</var> axis.
     * @throws NoSuchElementException if this widget doesn't contains the specified series.
     */
    public Axis2D[] getAxis(final Series series) throws NoSuchElementException {
        final Entry entry = (Entry) this.series.get(series);
        if (entry != null) {
            assert xAxis.indexOf(entry.xAxis) >= 0 : xAxis;
            assert yAxis.indexOf(entry.yAxis) >= 0 : yAxis;
            return new Axis2D[] {
                entry.xAxis,
                entry.yAxis
            };
        }
        throw new NoSuchElementException(series.getName());
    }

    /**
     * Returns a bounding box that contains the logical coordinates of
     * all data that may be displayed in this <code>Plot2D</code>.
     *
     * @return A bounding box for the logical coordinates of every series
     *         that is going to be drawn on this <code>Plot2D</code>.
     */
    public Rectangle2D getArea() {
        if (seriesBounds == null) {
            final Rectangle2D bounds = new Rectangle2D.Double();
            for (final Iterator it=series.keySet().iterator(); it.hasNext();) {
                final Rectangle2D candidate = ((Series) it.next()).getBounds2D();
                if (bounds.isEmpty()) {
                    bounds.setRect(candidate);
                } else {
                    bounds.add(candidate);
                }
            }
            if (!bounds.isEmpty()) {
                seriesBounds = bounds;
            }
        }
        return seriesBounds;
    }

    /**
     * Returns the bounds used by at least one of the specified axis.
     *
     * @param xAxis The <var>x</var> axis, or <code>null</code>.
     * @param yAxis The <var>y</var> axis, or <code>null</code>.
     */
    private Rectangle2D getBounds(final Axis2D xAxis, final Axis2D yAxis) {
        final Rectangle2D bounds = new Rectangle2D.Double();
        for (final Iterator it=series.entrySet().iterator(); it.hasNext();) {
            final Map.Entry e = (Map.Entry) it.next();
            final Entry entry = (Entry)  e.getValue();
            if (entry.xAxis.equals(xAxis) || entry.yAxis.equals(yAxis)) {
                final Rectangle2D candidate = ((Series) e.getKey()).getBounds2D();
                if (bounds.isEmpty()) {
                    bounds.setRect(candidate);
                } else {
                    bounds.add(candidate);
                }
            }
        }
        return bounds;
    }

    /**
     * Returns the zoomable area in pixel coordinates. This area will not cover the
     * full widget area, since some room will be left for painting axis and titles.
     */
    protected Rectangle getZoomableBounds(Rectangle bounds) {
        bounds = super.getZoomableBounds(bounds);
        bounds.x      += left;
        bounds.y      +=  top;
        bounds.width  -= (left + right);
        bounds.height -= (top + bottom);
        return bounds;
    }

    /**
     * Reinitialize the zoom to its default value. This method is used
     * by {@link ZoomPane} and usually doesn't need to be explicitely
     * invoked.
     */
    public void reset() {
        layoutAxis(true);
        for (final Iterator it=xAxis.iterator(); it.hasNext();) {
            final Axis2D axis = (Axis2D) it.next();
            final Rectangle2D bounds = getBounds(axis, null);
            if (!bounds.isEmpty()) {
                final AbstractGraduation grad = (AbstractGraduation) axis.getGraduation();
                grad.setMinimum(bounds.getMinX());
                grad.setMaximum(bounds.getMaxX());
            }
        }
        for (final Iterator it=yAxis.iterator(); it.hasNext();) {
            final Axis2D axis = (Axis2D) it.next();
            final Rectangle2D bounds = getBounds(null, axis);
            if (!bounds.isEmpty()) {
                final AbstractGraduation grad = (AbstractGraduation) axis.getGraduation();
                grad.setMinimum(bounds.getMinY());
                grad.setMaximum(bounds.getMaxY());
            }
        }
        super.reset();
    }

    /**
     * Set axis location. This method is automatically invoked when the axis needs to be layout.
     * This occur for example when new axis are added, or when the component has been resized.
     *
     * @param force If <code>true</code>, then axis orientation and position are reset to
     *        their default value. If <code>false</code>, then this method try to preserve
     *        axis orientation and position relative to widget's border.
     */
    private void layoutAxis(final boolean force) {
        final int width  = getWidth();
        final int height = getHeight();
        final double  tx = width  - lastWidth;
        final double  ty = height - lastHeight;
        int axisCount = 0;
        for (final Iterator it=xAxis.iterator(); it.hasNext();) {
            final Axis2D axis = (Axis2D) it.next();
            if (force) {
                axis.setLabelClockwise(true);
                axis.setLine(left, height-bottom, width-right, height-bottom);
                translatePerpendicularly(axis, xOffset*axisCount, yOffset*axisCount);
            } else {
                resize(axis, tx, ty);
            }
            axisCount++;
        }
        axisCount = 0;
        for (final Iterator it=yAxis.iterator(); it.hasNext();) {
            final Axis2D axis = (Axis2D) it.next();
            if (force) {
                axis.setLabelClockwise(false);
                axis.setLine(left, height-bottom, left, top);
                translatePerpendicularly(axis, xOffset*axisCount, yOffset*axisCount);
            } else {
                resize(axis, tx, ty);
            }
            axisCount++;
        }
        lastWidth  = width;
        lastHeight = height;
    }

    /**
     * Translate an axis in a perpendicular direction to its orientation.
     * The following rules applies:
     *
     * <ul>
     *   <li>If the axis is vertical, then the axis is translated horizontally
     *       by <code>tx</code> only. The <code>ty</code> argument is ignored.</li>
     *   <li>If the axis is horizontal, then the axis is translated vertically
     *       by <code>ty</code> only. The <code>tx</code> argument is ignored.</li>
     *   <li>If the axis is diagonal, then the axis is translated using the
     *       following formula (<var>theta</var> is the axis orientation relative
     *       to the horizontal):
     *       <br>
     *       <blockquote><pre>
     *          dx = tx*sin(theta)
     *          dy = ty*cos(theta)
     *       </pre></blockquote>
     *    </li>
     *  </ul>
     */
    private static void translatePerpendicularly(final Axis2D axis, final double tx, final double ty) {
        final double x1 = axis.getX1();
        final double y1 = axis.getY1();
        final double x2 = axis.getX2();
        final double y2 = axis.getY2();
        double dy = (double) x2 - (double) x1; // Note: dx and dy are really
        double dx = (double) y1 - (double) y2; //       swapped. Not an error.
        double length = Math.sqrt(dx*dx + dy*dy);
        dx *= tx/length;
        dy *= ty/length;
        axis.setLine(x1+dx, y1+dy, x2+dx, y2+dy);
    }

    /**
     * Invoked when this component has been resized. This method adjust axis length will
     * preserving their orientation and position relative to border.
     *
     * @param axis The axis to adjust.
     * @param tx The change in component width.
     * @param ty The change in component height.
     */
    private static void resize(final Axis2D axis, final double tx, final double ty) {
        final Point2D P1 = axis.getP1();
        final Point2D P2 = axis.getP2();
        final Point2D anchor, moveable;
        if (distanceSq(P1) <= distanceSq(P2)) {
            anchor   = P1;
            moveable = P2;
        } else {
            anchor   = P2;
            moveable = P1;
        }
        final double  x = moveable.getX();
        final double  y = moveable.getY();
        final double dx = x-anchor.getX();
        final double dy = y-anchor.getY();
        final double length = XMath.hypot(dx, dy);
        moveable.setLocation(x + tx*dx/length,
                             y + ty*dy/length);
        axis.setLine(P1, P2);
    }

    /**
     * Returns the square of the distance between the specified point and the origin (0,0).
     */
    private static double distanceSq(final Point2D point) {
        final double x = point.getX();
        final double y = point.getY();
        return x*x + y*y;
    }

    /**
     * Apply a zoom. This method is used by {@link ZoomPane} and usually
     * doesn't need to be explicitely invoked.
     */
    public void transform(final AffineTransform transform) {
        super.transform(transform);
        Point2D.Double P1 = new Point2D.Double();
        Point2D.Double P2 = new Point2D.Double();
        try {
            /*
             * Process horizontal axis first, then process the vertical axis.
             */
            boolean processVerticalAxis = false;
            do {
                for (final Iterator it=(processVerticalAxis ? yAxis : xAxis).iterator(); it.hasNext();) {
                    final Axis2D axis = (Axis2D) it.next();
                    P1.setLocation(axis.getX1(), axis.getY1());
                    P2.setLocation(axis.getX2(), axis.getY2());
                    zoom.inverseTransform(P1, P1);
                    zoom.inverseTransform(P2, P2);
                    final AbstractGraduation grad = (AbstractGraduation) axis.getGraduation();
                    if (!processVerticalAxis) {
                        if (P1.x > P2.x) {
                            final Point2D.Double tmp = P1;
                            P1 = P2;
                            P2 = tmp;
                        }
                        grad.setMinimum(P1.x);
                        grad.setMaximum(P2.x);
                    } else {
                        if (P1.y > P2.y) {
                            final Point2D.Double tmp = P1;
                            P1 = P2;
                            P2 = tmp;
                        }
                        grad.setMinimum(P1.y);
                        grad.setMaximum(P2.y);
                    }
                }
            } while ((processVerticalAxis = !processVerticalAxis) == true);
        } catch (NoninvertibleTransformException exception) {
            final LogRecord record = new LogRecord(Level.WARNING, "Illegal zoom"); // TODO: localize
            record.setSourceClassName("Plot2D");
            record.setSourceMethodName("transform");
            record.setThrown(exception);
            Logger.getLogger("org.geotools.gui.swing").log(record);
        }
        repaint();
    }

    /**
     * Paints the axis and all series.
     */
    protected void paintComponent(final Graphics2D graphics) {
        final Rectangle       bounds       = getZoomableBounds(null);
        final AffineTransform oldTransform = graphics.getTransform();
        final Stroke          oldStroke    = graphics.getStroke();
        final Paint           oldPaint     = graphics.getPaint();
        final Shape           oldClip      = graphics.getClip();
        final Font            oldFont      = graphics.getFont();
        /*
         * Paint series first.
         */
        int axisCount = 0;
        graphics.clip(bounds);
        graphics.setStroke(DEFAULT_STROKE);
        final AffineTransform zoomTr = graphics.getTransform();
        for (final Iterator it=series.entrySet().iterator(); it.hasNext();) {
            final Map.Entry   e = (Map.Entry) it.next();
            final Series series = (Series) e.getKey();
            final Entry  entry  = (Entry)  e.getValue();
            final AffineTransform transform = Axis2D.createAffineTransform(entry.xAxis, entry.yAxis);
            final Shape path = series.toShape(null);
            graphics.setPaint(series.getColor());
            graphics.transform(transform);
            graphics.draw(path);
            graphics.setTransform(zoomTr);
            axisCount++;
        }
        /*
         * Paint axis on top of series.
         */
        graphics.setTransform(oldTransform);
        graphics.setStroke(DEFAULT_STROKE);
        graphics.setPaint(getForeground());
        graphics.setFont(getFont());
        graphics.setClip(oldClip);
        for (final Iterator it=xAxis.iterator(); it.hasNext();) {
            ((Axis2D) it.next()).paint(graphics);
        }
        for (final Iterator it=yAxis.iterator(); it.hasNext();) {
            ((Axis2D) it.next()).paint(graphics);
        }
        /*
         * Paint the title.
         */
        if (title != null) {
            final FontRenderContext    fc = graphics.getFontRenderContext();
            final GlyphVector      glyphs = titleFont.createGlyphVector(fc, title);
            final Rectangle2D titleBounds = glyphs.getVisualBounds();
            graphics.drawGlyphVector(glyphs, (float)((getWidth()-titleBounds.getWidth())/2), 20);
        }
        graphics.transform(zoom); // Reset the zoom for the magnifier.
        graphics.setStroke(oldStroke);
        graphics.setPaint(oldPaint);
        graphics.setFont(oldFont);
    }

    /**
     * Remove all series. If the <code>removeAxis</code> is <code>true</code>,
     * then all axis are removed as well. Otherwise, the last pair of axis is keep.
     *
     * @param removeAxis <code>true</code> for removing axis as well,
     *        or <code>false</code> for keeping the last used axis.
     */
    public void clear(final boolean removeAxis) {
        series.clear();
        seriesBounds = null;
        Arrays.fill(nextAxis, null);
        xAxis.clear();
        yAxis.clear();
        if (removeAxis) {
            previousAxis = null;
        } else {
            if (previousAxis != null) {
                if (previousAxis.xAxis != null) {
                    xAxis.add(previousAxis.xAxis);
                }
                if (previousAxis.yAxis != null) {
                    yAxis.add(previousAxis.yAxis);
                }
            }
        }
        repaint();
    }

    /**
     * A series to be displayed in a {@link Plot2D} widget. A <code>Series</code> contains the
     * data to draw as a {@link Shape}. It also contains the {@link Paint} and {@link Stroke}
     * attributes.
     *
     * @version $Id: Plot2D.java,v 1.6 2003/07/23 14:17:11 desruisseaux Exp $
     * @author Martin Desruisseaux
     */
    public static interface Series {
        /**
         * Returns the name of this series.
         */
        public abstract String getName();

        /**
         * Returns the color for this series.
         */
        public abstract Paint getColor();

        /**
         * Returns the number of points in this series.
         */
        public abstract int getNumPoints();

        /**
         * Returns the point at the specified index.
         *
         * @param  i The index from 0 inclusive to {@link #getNumPoints} exclusive.
         * @return The point at the given index.
         * @throws IndexOutOfBoundsException if <code>index</code> is out of bounds.
         */
        public abstract Point2D getValue(int i) throws IndexOutOfBoundsException;

        /**
         * Returns the bounding box of all <var>x</var> and <var>y</var> ordinates.
         */
        public abstract Rectangle2D getBounds2D();

        /**
         * Returns the series data as a path.
         *
         * @param transform An optional transform to apply on values, or <code>null</code> if none.
         * @return The (<var>x</var>,<var>y</var>) coordinates as a Java2D {@linkplain Shape shape}.
         */
        public abstract Shape toShape(AffineTransform transform);
    }

    /**
     * Default implementation of {@link Plot2D.Series}.
     *
     * @version $Id: Plot2D.java,v 1.6 2003/07/23 14:17:11 desruisseaux Exp $
     * @author Martin Desruisseaux
     */
    private static final class DefaultSeries extends GenericArray implements Series {
        /** The series name. */
        private final String name;

        /** The color. */
        private Paint color = Color.BLUE;

        /** Construct a series with the given name and  (<var>x</var>,<var>y</var>) vectors. */
        public DefaultSeries(final String name, final Object x, final Object y, final int index) {
            super(x,y);
            this.name  = name;
            this.color = DEFAULT_COLORS[index % DEFAULT_COLORS.length];
        }

        /** Construct a series with the given name and  (<var>x</var>,<var>y</var>) vectors. */
        public DefaultSeries(final String name, final Object x, final Object y,
                             final int lower, final int upper, final int index) {
            super(x,y, lower, upper);
            this.name  = name;
            this.color = DEFAULT_COLORS[index % DEFAULT_COLORS.length];
        }

        /** Returns the series name. */
        public String getName() {
            return name;
        }

        /** Returns the color for this series. */
        public Paint getColor() {
            return color;
        }

        /** Returns the number of points in this series. */
        public int getNumPoints() {
            return count();
        }
    }
}
