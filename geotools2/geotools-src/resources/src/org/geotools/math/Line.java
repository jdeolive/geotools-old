/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
 * (C) 2003, Institut de Recherche pour le Développement
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
package org.geotools.math;

// J2SE dependencies
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import javax.vecmath.MismatchedSizeException;
import java.io.Serializable;

// Geotools dependencies
import org.geotools.util.Cloneable;


/**
 * Equation of a line in a two dimensional space (<var>x</var>,<var>y</var>).
 * A line has an equation of the form <var>y</var>=<var>a</var><var>x</var>+<var>b</var>.
 * At the difference of {@link Line2D} (which are bounded by (<var>x1</var>,<var>y1</var>)
 * and (<var>x2</var>,<var>y2</var>) points), <code>Line</code> objects extends toward infinity.
 *
 * The equation parameters for a <code>Line</code> object can bet set at construction
 * time or using one of the <code>setLine(...)</code> methods. The <var>y</var> value
 * can be computed for a given <var>x</var> value using the {@link #y} method. Method
 * {@link #x} compute the converse and should work even if the line is vertical.
 *
 * @version $Id: Line.java,v 1.8 2003/08/28 15:41:18 desruisseaux Exp $
 * @author Martin Desruisseaux
 *
 * @see Point2D
 * @see Line2D
 * @see Plane
 */
public class Line implements Cloneable, Serializable {
    /**
     * Serial number for compatibility with different versions.
     */
    private static final long serialVersionUID = 2185952238314399110L;

    /**
     * Small value for rounding errors.
     */
    private static final double EPS = 1E-12;

    /**
     * The slope for this line.
     */
    private double slope;

    /**
     * The <var>y</var> value at <var>x</var>==0.
     */
    private double y0;

    /**
     * Valeur de <var>x</var> à <var>y</var>==0. Cette
     * valeur est utilisée pour les droites verticales.
     */
    private double x0;

    /**
     * Construct an initially unitialized line. All methods will returns {@link Double#NaN}.
     */
    public Line() {
        slope = y0 = x0 = Double.NaN;
    }

    /**
     * Construct a line with the specified slope and offset.
     * The linear equation will be <var>y</var>=<var>slope</var>*<var>x</var>+<var>y0</var>.
     *
     * @param slope The slope.
     * @param y0 The <var>y</var> value at <var>x</var>==0.
     *
     * @see #setLine(double, double)
     */
    public Line(final double slope, final double y0) {
        this.slope = slope;
        this.y0    = y0;
        this.x0    = -y0/slope;
    }

    /**
     * Set the slope and offset for this line.
     * The linear equation will be <var>y</var>=<var>slope</var>*<var>x</var>+<var>y0</var>.
     *
     * @param slope The slope.
     * @param y0 The <var>y</var> value at <var>x</var>==0.
     *
     * @see #setLine(Point2D, Point2D)
     * @see #setLine(Line2D)
     * @see #setLine(double[], double[])
     */
    public void setLine(final double slope, final double y0) {
        this.slope = slope;
        this.y0    = y0;
        this.x0    = -y0/slope;
    }

    /**
     * Définie une droite passant par le segment de droite spécifié.
     * La droite sera prolongée à l'infini au delà des extrémités du
     * segment.
     *
     * @param line ligne dont on veut l'équation.
     *
     * @see #setLine(Point2D,Point2D)
     */
    public void setLine(final Line2D line) {
        setLine(line.getX1(), line.getY1(), line.getX2(), line.getY2());
    }

    /**
     * Définie une droite passant par les deux points spécifiés.
     * La droite sera prolongée à l'infini au delà des deux points.
     *
     * @param p1 Coordonnées du premier point.
     * @param p2 Coordonnées du deuxième point.
     *
     * @see #setLine(Line2D)
     */
    public void setLine(final Point2D p1, final Point2D p2) {
        setLine(p1.getX(), p1.getY(), p2.getX(), p2.getY());
    }

    /**
     * Définie une droite passant par les coordonnées spécifiées.
     * La droite sera prolongée à l'infini au delà des deux points.
     *
     * @param x1 Coordonnée <var>x</var> du premier point.
     * @param y1 Coordonnée <var>y</var> du premier point.
     * @param x2 Coordonnée <var>x</var> du deuxième point.
     * @param y2 Coordonnée <var>y</var> du deuxième point.
     *
     * @see #setLine(Point2D,Point2D)
     * @see #setLine(Line2D)
     */
    private void setLine(final double x1, final double y1, final double x2, final double y2) {
        this.slope = (y2-y1)/(x2-x1);
        this.x0    = x2 - y2/slope;
        this.y0    = y2 - slope*x2;
    }

    /**
     * Given a set of data points <code>x[0..ndata-1]</code>, <code>y[0..ndata-1]</code>,
     * fit them to a straight line <var>y</var>=<var>b</var>+<var>m</var><var>x</var> in
     * a least-squares senses. This method assume that the <var>x</var> values are precise
     * and all uncertainty is in <var>y</var>.
     *
     * <p>Reference: <a
     * href="http://shakti.cc.trincoll.edu/~palladin/courses/ENGR431/statistics/node9.html">Linear
     * Regression Curve Fitting</a>.
     *
     * @param x Vector of <var>x</var> values (independant variable).
     * @param y Vector of <var>y</var> values (dependant variable).
     * @return Estimation of the correlation coefficient. The closer
     *         this coefficient is to 1, the better the fit.
     *
     * @throws MismatchedSizeException if <var>x</var> and <var>y</var> don't have the same length.
     */
    public double setLine(final double[] x, final double[] y) throws MismatchedSizeException {
        final int N = x.length;
        if (N != y.length) {
            throw new MismatchedSizeException();
        }
        int    count  = 0;
        double mean_x = 0;
        double mean_y = 0;
        for (int i=0; i<N; i++) {
            final double xi = x[i];
            final double yi = y[i];
            if (!Double.isNaN(xi) && !Double.isNaN(yi)) {
                mean_x += xi;
                mean_y += yi;
                count++;
            }
        }
        mean_x /= count;
        mean_y /= count;
        /*
         * We have to solve two equations with two unknows:
         *
         *   1)    mean(y)  = b + m*mean(x)
         *   2)    mean(xy) = b*mean(x) + m*mean(x²)
         *
         * Those formulas lead to a quadratic equation. However,
         * the formulas become very simples if we set 'mean(x)=0'.
         * We can achieve this result by computing instead of (2):
         *
         *   2b)   mean(dx y) = m*mean(dx²)
         *
         * where dx=x-mean(x). In this case mean(dx)==0.
         */
        double mean_x2 = 0;
        double mean_y2 = 0;
        double mean_xy = 0;
        for (int i=0; i<N; i++) {
            double xi = x[i];
            double yi = y[i];
            if (!Double.isNaN(xi) && !Double.isNaN(yi)) {
                xi      -= mean_x;
                mean_x2 += xi*xi;
                mean_y2 += yi*yi;
                mean_xy += xi*yi;
            }
        }
        mean_x2 /= count;
        mean_y2 /= count;
        mean_xy /= count;
        /*
         * Assuming that 'mean(x)==0', then the correlation
         * coefficient can be approximate by:
         *
         * R = mean(xy) / sqrt( mean(x²) * (mean(y²) - mean(y)²) )
         */
        slope = mean_xy/mean_x2;
        y0 = mean_y-mean_x*slope;
        return mean_xy/Math.sqrt(mean_x2 * (mean_y2-mean_y*mean_y));
    }

    /**
     * Translate the line. The slope stay unchanged.
     *
     * @param dx The horizontal translation.
     * @param dy The vertical translation.
     */
    public void translate(final double dx, final double dy) {
        if (slope==0 || Double.isInfinite(slope)) {
            x0 += dx;
            y0 += dy;
        } else {
            x0 += dx - dy/slope;
            y0 += dy - slope*dx;
        }
    }

    /**
     * Compute <var>y</var>=<var>f</var>(<var>x</var>).
     * If the line is vertical, then this method returns an infinite value.
     * This method is final for performance reason.
     *
     * @param  x The <var>x</var> value.
     * @return The <var>y</var> value.
     *
     * @see #x(double)
     */
    public final double y(final double x) {
        return slope*x + y0;
    }

    /**
     * Compute <var>x</var>=<var>f</var><sup>-1</sup>(<var>y</var>).
     * If the line is horizontal, then this method returns an infinite value.
     * This method is final for performance reason.
     *
     * @param  y The <var>y</var> value.
     * @return The <var>x</var> value.
     *
     * @see #y(double)
     */
    public final double x(final double y) {
        return y/slope + x0;
    }

    /**
     * Returns the <var>y</var> value for <var>x</var>==0.
     * Coordinate (0, <var>y0</var>) is the intersection point with the <var>y</var> axis.
     */
    public final double getY0() {
        return y0;
    }

    /**
     * Returns the <var>x</var> value for <var>y</var>==0.
     * Coordinate (<var>x0</var>,0) is the intersection point with the <var>x</var> axis.
     */
    public final double getX0() {
        return x0;
    }

    /**
     * Returns the slope.
     */
    public final double getSlope() {
        return slope;
    }

    /**
     * Returns the intersection point between this line and the specified one.
     * If both lines are parallel, then this method returns <code>null</code>.
     *
     * @param  line The line to intersect.
     * @return The intersection point, or <code>null</code>.
     */
    public Point2D intersectionPoint(final Line line) {
        double x, y;
        if (Double.isInfinite(slope)) {
            if (Double.isInfinite(line.slope)) {
                return null;
            }
            x = x0;
            y = x*line.slope + line.y0;
        } else {
            if (!Double.isInfinite(line.slope)) {
                x = (y0-line.y0) / (line.slope-slope);
                if (Double.isInfinite(x)) {
                    return null;
                }
            } else {
                x = line.x0;
            }
            y = x*slope + y0;
        }
        return new Point2D.Double(x,y);
    }

    /**
     * Returns the intersection point between this line and the specified bounded line.
     * If both lines are parallel or if the specified <code>line</code> doesn't reach
     * this line (since {@link Line2D} do not extends toward infinities), then this
     * method returns <code>null</code>.
     *
     * @param  line The bounded line to intersect.
     * @return The intersection point, or <code>null</code>.
     */
    public Point2D intersectionPoint(final Line2D line) {
        final double x1 = line.getX1();
        final double y1 = line.getY1();
        final double x2 = line.getX2();
        final double y2 = line.getY2();
        double x,y;
        double m = (y2-y1)/(x2-x1);
        if (Double.isInfinite(slope)) {
            if (Double.isInfinite(m)) {
                return null;
            }
            x = x0;
            y = x*m + (y2-m*x2);
        } else {
            if (!Double.isInfinite(m)) {
                x = (y0-(y2-m*x2)) / (m-slope);
                if (Double.isInfinite(x)) {
                    return null;
                }
            } else {
                x = 0.5*(x1+x2);
            }
            y = x*slope + y0;
        }
        double eps;
        /*
         * Vérifie si l'intersection se trouve
         * dans la plage permise pour <var>x</var>.
         */
        eps = EPS*Math.abs(x);
        if (x1 <= x2) {
            if (x<x1-eps || x>x2+eps) {
                return null;
            }
        } else {
            if (x>x1+eps || x<x2-eps) {
                return null;
            }
        }
        /*
         * Vérifie si l'intersection se trouve
         * dans la plage permise pour <var>y</var>.
         */
        eps = EPS*Math.abs(y);
        if (y1 <= y2) {
            if (y<y1-eps || y>y2+eps) {
                return null;
            }
        } else {
            if (y>y1-eps || y<y2+eps) {
                return null;
            }
        }
        return new Point2D.Double(x,y);
    }

    /**
     * Returns the nearest point on this line from the specified point.
     *
     * @param  point An arbitrary point.
     * @return The point on this line which is the nearest of the specified <code>point</code>.
     */
    public Point2D nearestColinearPoint(final Point2D point) {
        if (!Double.isInfinite(slope)) {
            final double x = ((point.getY()-y0)*slope + point.getX()) / (slope*slope+1);
            return new Point2D.Double(x, x*slope+y0);
        } else {
            return new Point2D.Double(x0, point.getY());
        }
    }

    /**
     * Compute the base of a isosceles triangle having the specified summit and side length.
     * The base will be colinear with this line. In other words, this method compute two
     * points (<var>x1</var>,<var>y1</var>) and (<var>x2</var>,<var>y2</var>) located in
     * such a way that:
     * <ul>
     *   <li>Both points are on this line.</li>
     *   <li>The distance between any of the two points and the specified <code>summit</code>
     *       is exactly <code>sideLength</code>.</li>
     * </ul>
     *
     * @param  summit The summit of the isosceles triangle.
     * @param  sideLength The length for the two sides of the isosceles triangle.
     * @return The base of the isoscele triangle, colinear with this line, or <code>null</code>
     *         if the base can't be computed. If non-null, then the triangle is the figure formed
     *         by joining (<var>x1</var>,<var>y1</var>), (<var>x2</var>,<var>y2</var>) and
     *         <code>summit</code>. 
     */
    public Line2D isoscelesTriangleBase(final Point2D summit, double sideLength) {
        sideLength *= sideLength;
        if (slope == 0) {
            final double  x =    summit.getX();
            final double dy = y0-summit.getY();
            final double dx = Math.sqrt(sideLength - dy*dy);
            if (Double.isNaN(dx)) {
                return null;
            }
            return new Line2D.Double(x+dx, y0, x-dx, y0);
        }
        if (Double.isInfinite(slope)) {
            final double  y =    summit.getY();
            final double dx = x0-summit.getX();
            final double dy = Math.sqrt(sideLength - dx*dx);
            if (Double.isNaN(dy)) {
                return null;
            }
            return new Line2D.Double(x0, y+dy, x0, y-dy);
        }
        final double x  = summit.getX();
        final double y  = summit.getY();
        final double dy = y0 - y + slope*x;
        final double B  = -slope*dy;
        final double A  = slope*slope + 1;
        final double C  = Math.sqrt(B*B + A*(sideLength - dy*dy));
        if (Double.isNaN(C)) {
            return null;
        }
        final double x1 = (B+C)/A + x;
        final double x2 = (B-C)/A + x;
        return new Line2D.Double(x1, slope*x1+y0, x2, slope*x2+y0);
    }

    /**
     * Returns a string representation of this line. This method returns
     * the linear equation in the form <code>"y=m*x+b"</code>.
     *
     * @return A string representation of this line.
     */
    public String toString() {
        if (!Double.isInfinite(slope)) {
            StringBuffer buffer = new StringBuffer("y= ");
            if (slope != 0) {
                buffer.append(slope);
                buffer.append("*x");
                if (y0 != 0) {
                    buffer.append(" + ");
                } else {
                    return buffer.toString();
                }
            }
            buffer.append(y0);
            return buffer.toString();
        } else {
            return "x= "+x0;
        }
    }

    /**
     * Compare this object with the specified one for equality.
     */
    public boolean equals(final Object object) {
        if (object!=null && getClass().equals(object.getClass())) {
            final Line that = (Line) object;
            return Double.doubleToLongBits(this.slope) == Double.doubleToLongBits(that.slope) &&
                   Double.doubleToLongBits(this.y0   ) == Double.doubleToLongBits(that.y0   ) &&
                   Double.doubleToLongBits(this.x0   ) == Double.doubleToLongBits(that.x0   );
        } else {
            return false;
        }
    }

    /**
     * Returns a hash code value for this line.
     */
    public int hashCode() {
        final long code = Double.doubleToLongBits(slope) + 37*Double.doubleToLongBits(y0);
        return (int) code ^ (int) (code >>> 32);
    }

    /**
     * Returns a clone of this line.
     */
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException exception) {
            throw new AssertionError(exception);
        }
    }
}
