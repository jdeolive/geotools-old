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
import javax.vecmath.MismatchedSizeException;

// Components and events
import java.awt.Container;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentAdapter;

// Collections
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.NoSuchElementException;

// Logging
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.LogRecord;

// Geotools dependencies
import org.geotools.axis.Axis2D;
import org.geotools.axis.Graduation;
import org.geotools.axis.AbstractGraduation;
import org.geotools.renderer.array.GenericArray;


/**
 * A lightweight widget displaying two axis and an arbitrary amount of data series
 * with zoom capability. Axis may have arbitrary orientation (they don't need to be
 * perpendicular). This widget is not a replacement for full featured toolkit like
 * <A HREF="http://jgraph.sourceforge.net/">JGraph</A>; it just provides a mean to
 * quickly display a time serie.
 *
 * @version $Id: Plot2D.java,v 1.1 2003/05/23 18:00:26 desruisseaux Exp $
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
     * The set of <var>x</var> axis. There is usually only one axis,
     * but more axis are allowed. If this list length is smaller than
     * <code>series.size()</code>, then the last axis is reused for
     * all remaining series.
     *
     * @see #newAxis
     * @see #addSeries(Series)
     */
    private final List xAxis = new ArrayList(3);

    /**
     * The set of <var>y</var> axis. There is usually only one axis,
     * but more axis are allowed. If this list length is smaller than
     * <code>series.size()</code>, then the last axis is reused for
     * all remaining series.
     *
     * @see #newAxis
     * @see #addSeries(Series)
     */
    private final List yAxis = new ArrayList(3);

    /**
     * The set of series to plot.
     *
     * @see #addSeries(Series)
     */
    private final List series = new ArrayList();

    /**
     * Immutable version of <code>series</code> to be returned by {@link #getSeries}.
     *
     * @see #getSeries
     */
    private final List unmodifiableSeries = Collections.unmodifiableList(series);

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
     * Margin between widget border and the drawing area.
     */
    private int top=30, bottom=60, left=60, right=30;

    /**
     * Horizontal (x) and vertival (y) offset to apply to any supplementary axis.
     */
    private int xOffset=20, yOffset=-20;

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
    private final class Listeners extends ComponentAdapter {
        /**
         * When resized, force the widget to layout its axis.
         */
        public void componentResized(final ComponentEvent event) {
            final Container c = (Container) event.getSource();
            c.invalidate();
            c.validate(); // Doesn't seems be automatically invoked as a result of 'invalidate'.
                          // Is it a bug?  This 'validate'/'invalidate' stuff is pretty hard to
                          // get working right!!!!!
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
     * Advises that the next series to be added with {@link #addSeries(Series) addSeries(...)}
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
     * @throws MismatchedSizeException if arrays doesn't have the same length.
     */
    public void addSeries(final String name, final Object x, final Object y)
            throws ClassCastException, MismatchedSizeException
    {
        addSeries(new DefaultSeries(name, x, y));
    }

    /**
     * Add a new serie to the chart.
     *
     * @param series The serie to add.
     */
    public void addSeries(final Series series) {
        Rectangle2D bounds = null;
        /*
         * Si aucun axe n'a été définie, construit
         * et ajoute de nouveau axes maintenant.
         */
        if (xAxis.isEmpty() || nextAxis[0]!=null) {
            if (bounds == null) {
                bounds = series.getBounds2D();
            }
            final Axis2D axis = new Axis2D();
            final AbstractGraduation grad = (AbstractGraduation) axis.getGraduation();
            grad.setMinimum(bounds.getMinX());
            grad.setMaximum(bounds.getMaxX());
            grad.setTitle(nextAxis[0]);
            xAxis.add(axis);
            invalidate();
        }
        if (yAxis.isEmpty() || nextAxis[1]!=null) {
            if (bounds == null) {
                bounds = series.getBounds2D();
            }
            final Axis2D axis = new Axis2D();
            final AbstractGraduation grad = (AbstractGraduation) axis.getGraduation();
            grad.setMinimum(bounds.getMinY());
            grad.setMaximum(bounds.getMaxY());
            grad.setTitle(nextAxis[1]);
            yAxis.add(axis);
            invalidate();
        }
        this.series.add(series);
        if (title == null) {
            title = series.getName();
        }
        validate();
    }

    /**
     * Returns the list of all series. Series are painted in the order
     * they are returned. The returned list is immutable.
     */
    public Collection getSeries() {
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
        final int index = this.series.indexOf(series);
        if (index >= 0) {
            if (!xAxis.isEmpty() && !yAxis.isEmpty()) {
                return new Axis2D[] {
                    (Axis2D) xAxis.get(Math.min(index, xAxis.size()-1)),
                    (Axis2D) yAxis.get(Math.min(index, yAxis.size()-1))
                };
            }
            // Should not occurs. As a safety, an empty array still a reasonable output.
            return new Axis2D[0];
        }
        throw new NoSuchElementException(series.getName());
    }

    /**
     * Remove all series and axis previously displayed.
     */
    public void clear() {
        series.clear();
        xAxis .clear();
        yAxis .clear();
    }

    /**
     * Returns a bounding box that contains the logical coordinates of
     * all data that may be displayed in this <code>ZoomPane</code>.
     *
     * @return A bounding box for the logical coordinates of every content
     *         that is going to be drawn on this <code>ZoomPane</code>. If
     *         this bounding box is unknow, then this method can returns
     *         <code>null</code> (but this is not recommanded).
     */
    public Rectangle2D getArea() {
        double xmin = Double.POSITIVE_INFINITY;
        double xmax = Double.NEGATIVE_INFINITY;
        double ymin = Double.POSITIVE_INFINITY;
        double ymax = Double.NEGATIVE_INFINITY;
        for (final Iterator it=xAxis.iterator(); it.hasNext();) {
            double value;
            final Graduation grad = ((Axis2D) it.next()).getGraduation();
            if ((value=grad.getMinimum()) < xmin) xmin=value;
            if ((value=grad.getMaximum()) > xmax) xmax=value;
        }
        for (final Iterator it=yAxis.iterator(); it.hasNext();) {
            double value;
            final Graduation grad = ((Axis2D) it.next()).getGraduation();
            if ((value=grad.getMinimum()) < ymin) ymin=value;
            if ((value=grad.getMaximum()) > ymax) ymax=value;
        }
        if (xmin<=xmax && ymin<=ymax) {
            return new Rectangle2D.Double(xmin, ymin, xmax-xmin, ymax-ymin);
        }
        return null;
    }

    /**
     * Returns the zoomable area in pixel coordinates.
     */
    protected Rectangle getZoomableBounds(Rectangle bounds) {
        bounds = super.getZoomableBounds(bounds);
        bounds.x += left;
        bounds.y += top;
        bounds.width -= (left + right);
        bounds.height -= (top + bottom);
        return bounds;
    }

    /**
     * Reinitialize the zoom to its default value. This method is used
     * by {@link ZoomPane} and usually doesn't need to be explicitely
     * invoked.
     */
    public void reset() {
        final Rectangle2D bounds = new Rectangle2D.Double();
        for (final Iterator it=series.iterator(); it.hasNext();) {
            final Rectangle2D candidate = ((Series) it.next()).getBounds2D();
            if (bounds.isEmpty()) {
                bounds.setRect(candidate);
            } else {
                bounds.add(candidate);
            }
        }
        if (!bounds.isEmpty()) {
            double min, max;
            min = bounds.getMinX();
            max = bounds.getMaxX();
            for (final Iterator it=xAxis.iterator(); it.hasNext();) {
                final AbstractGraduation grad = (AbstractGraduation) ((Axis2D) it.next()).getGraduation();
                grad.setMinimum(min);
                grad.setMaximum(max);
            }
            min = bounds.getMinY();
            max = bounds.getMaxY();
            for (final Iterator it=yAxis.iterator(); it.hasNext();) {
                final AbstractGraduation grad = (AbstractGraduation) ((Axis2D) it.next()).getGraduation();
                grad.setMinimum(min);
                grad.setMaximum(max);
            }
        }
        super.reset();
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
             * Process horizontal axis first, then
             * process the vertical axis.
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
     * Validate this panel. This method is automatically invoked
     * when the axis needs to be layout. This occur for example
     * when new axis are added, or when the component has been
     * resized.
     *
     * @task TODO: we should modify this method in order to preserve axis orientation,
     *             since the orientation may have been set by user.
     */
    public void validate() {
        super.validate();
        final int width  = getWidth();
        final int height = getHeight();
        int axisCount = 0;
        for (final Iterator it=xAxis.iterator(); it.hasNext();) {
            final Axis2D axis = (Axis2D) it.next();
            axis.setLabelClockwise(true);
            axis.setLine(left, height-bottom, width-right, height-bottom);
            translatePerpendicularly(axis, xOffset*axisCount, yOffset*axisCount);
            axisCount++;
        }
        axisCount = 0;
        for (final Iterator it=yAxis.iterator(); it.hasNext();) {
            final Axis2D axis = (Axis2D) it.next();
            axis.setLabelClockwise(false);
            axis.setLine(left, height-bottom, left, top);
            translatePerpendicularly(axis, xOffset*axisCount, yOffset*axisCount);
            axisCount++;
        }
        reset(); // TODO: temporary patch.
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
     *          dx = x*sin(theta)
     *          dy = y*cos(theta)
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
     * Paints the axis and all series.
     */
    protected void paintComponent(final Graphics2D graphics) {
        final Rectangle       bounds       = getZoomableBounds(null);
        final AffineTransform oldTransform = graphics.getTransform();
        final Stroke          oldStroke    = graphics.getStroke();
        final Paint           oldPaint     = graphics.getPaint();
        /*
         * Paint series first.
         */
        int axisCount = 0;
        graphics.clip(bounds);
        graphics.setColor(Color.BLUE);
        graphics.setStroke(DEFAULT_STROKE);
        final int upperXAxis = xAxis.size()-1;
        final int upperYAxis = yAxis.size()-1;
        final AffineTransform zoomTr = graphics.getTransform();
        for (final Iterator it=series.iterator(); it.hasNext();) {
            final Axis2D xAxis = (Axis2D) this.xAxis.get(Math.min(axisCount, upperXAxis));
            final Axis2D yAxis = (Axis2D) this.yAxis.get(Math.min(axisCount, upperYAxis));
            final AffineTransform transform = Axis2D.createAffineTransform(xAxis, yAxis);
            final Series series = (Series) it.next();
            final Shape path = series.toShape(null);
            graphics.transform(transform);
            graphics.draw(path);
            graphics.setTransform(zoomTr);
            axisCount++;
        }
        /*
         * Paint axis on top of series.
         */
        graphics.setClip(super.getZoomableBounds(bounds));
        graphics.setTransform(oldTransform);
        graphics.setStroke(DEFAULT_STROKE);
        graphics.setPaint(Color.BLACK);
        for (final Iterator it=xAxis.iterator(); it.hasNext();) {
            ((Axis2D) it.next()).paint(graphics);
        }
        for (final Iterator it=yAxis.iterator(); it.hasNext();) {
            ((Axis2D) it.next()).paint(graphics);
        }
        /*
         * Paint axis the title.
         */
        if (title != null) {
            graphics.setFont(titleFont);
            graphics.drawString(title, getWidth()/2, 20);
        }
        graphics.transform(zoom); // Reset the zoom for the magnifier.
    }

    /**
     * A series to be displayed in a {@link Plot2D} widget. A <code>Series</code> contains the
     * data to draw as a {@link Shape}. It also contains the {@link Paint} and {@link Stroke}
     * attributes.
     *
     * @version $Id: Plot2D.java,v 1.1 2003/05/23 18:00:26 desruisseaux Exp $
     * @author Martin Desruisseaux
     */
    public static interface Series {
        /**
         * Returns the name of this series.
         */
        public abstract String getName();

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
     */
    private static final class DefaultSeries extends GenericArray implements Series {
        /** The series name. */
        private final String name;

        /** Construct a series with the given name and  (<var>x</var>,<var>y</var>) vectors. */
        public DefaultSeries(final String name, final Object x, final Object y) {
            super(x,y);
            this.name = name;
        }

        /** Returns the series name. */
        public String getName() {
            return name;
        }

        /** Returns the number of points in this series. */
        public int getNumPoints() {
            return count();
        }
    }
}
