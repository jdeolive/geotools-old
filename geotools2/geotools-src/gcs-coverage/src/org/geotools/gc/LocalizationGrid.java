/*
 * Geotools - OpenSource mapping toolkit
 * (C) 2002, Centre for Computational Geography
 * (C) 2002, Institut de Recherche pour le Développement
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
package org.geotools.gc;

// J2SE dependencies
import java.awt.Point;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.AffineTransform;
import java.util.Arrays;

// Geotools dependencies
import org.geotools.ct.MathTransform2D;


/**
 * A factory for {@link MathTransform2D} backed by a <cite>grid of localization</cite>.
 * A grid of localization can be seen as a two-dimensional array of coordinate points.
 * Input coordinates are index in this two-dimensional array. Those input coordinates
 * (or index) should be in the range
 *
 * <code>x</sub>input</sub>&nbsp;=&nbsp;[0..width-1]</code> and
 * <code>y</sub>input</sub>&nbsp;=&nbsp;[0..height-1]</code> inclusive,
 *
 * where <code>width</code> and <code>height</code>  are the number of columns and rows
 * in the grid of localization. Output coordinates are the values stored in the grid of
 * localization at the specified index.
 * <br><br>
 * A grid of localization can be used for an image in which the "real world" coordinates
 * of each pixel is know.   If the real world coordinates is not know for each pixel but
 * only for some pixels at a fixed interval, then a transformation can be constructed by
 * the concatenation of an affine transform with a grid of localization.
 * <br><br>
 * A transformation from grid coordinates to "real world" coordinates can be obtained by
 * the {@link #getMathTransform2D} method.  If this transformation is close enough to an
 * affine transform, then an instance of {@link AffineTransform} is returned. Otherwise,
 * a transform backed by the localization grid is returned.
 *
 * @version $Id: LocalizationGrid.java,v 1.1 2002/08/03 11:06:03 desruisseaux Exp $
 * @author Remi Eve
 * @author Martin Desruisseaux
 */
public class LocalizationGrid {
    /**
     * <var>x</var> (usually longitude) offset relative to an entry.
     * Points are stored in {@link #grid} as <code>(x,y)</code> pairs.
     */
    private static final int X_OFFSET = LocalizationGridTransform2D.X_OFFSET;

    /**
     * <var>y</var> (usually latitude) offset relative to an entry.
     * Points are stored in {@link #grid} as <code>(x,y)</code> pairs.
     */
    private static final int Y_OFFSET = LocalizationGridTransform2D.Y_OFFSET;

    /**
     * Length of an entry in the {@link #grid} array. This lenght
     * is equals to the dimension of output coordinate points.
     */
    private static final int CP_LENGTH = LocalizationGridTransform2D.CP_LENGTH;

    /**
     * Number of grid's columns.
     */
    private final int width;
    
    /**
     * Number of grid's rows.
     */
    private final int height;
               
    /**
     * Grid of coordinate points.
     * Points are stored as <code>(x,y)</code> pairs.
     */
    private final float[] grid;
    
    /**
     * Construct an initially empty localization grid. All "real worlds"
     * coordinates are initially set to <code>(NaN,NaN)</code>.
     *
     * @param width  Number of grid's columns.
     * @param height Number of grid's rows.
     */
    public LocalizationGrid(final int width, final int height) {
        if (width < 2) {
            throw new IllegalArgumentException(String.valueOf(width));
        }
        if (height < 2) {
            throw new IllegalArgumentException(String.valueOf(height));
        }
        this.width  = width;
        this.height = height;
        this.grid   = new float[width * height * CP_LENGTH];
        Arrays.fill(grid, Float.NaN);
    }
    
    /**
     * Calcule l'indice d'un enregistrement dans la grille.
     *
     * @param  row  Coordonnee x du point.
     * @param  col  Coordonnee y du point.
     * @return l'indice de l'enregistrement ou du point dans la matrice.
     */
    private int computeOffset(final int col, final int row) {
        if (col<0 || col>=width) {
            throw new IndexOutOfBoundsException(String.valueOf(col));
        }
        if (row<0 || row>=height) {
            throw new IndexOutOfBoundsException(String.valueOf(row));
        }
        return (col + row * width) * CP_LENGTH;
    }

    /**
     * Returns the grid size. Grid coordinates are always in the range
     * <code>x</sub>input</sub>&nbsp;=&nbsp;[0..width-1]</code> and
     * <code>y</sub>input</sub>&nbsp;=&nbsp;[0..height-1]</code> inclusive.
     */
    public Dimension getSize() {
        return new Dimension(width, height);
    }

    /**
     * Returns the "real world" coordinates for the specified grid coordinates.
     * Grid coordinates must be integers inside this grid's range.  For general
     * transformations involving non-integer grid coordinates and/or coordinates
     * outside this grid's range, use {@link #getMathTransform2D} instead.
     *
     * @param  source The point in grid coordinates.
     * @return target The corresponding point in "real world" coordinates.
     * @throws IndexOutOfBoundsException If the source point is not in this grid's range.
     */
    public Point2D getLocalizationPoint(final Point source) {
        final int offset = computeOffset(source.x, source.y);
        return new Point2D.Float(grid[offset + X_OFFSET],
                                 grid[offset + Y_OFFSET]);
    }

    /**
     * Set a point in this localization grid.
     *
     * @param  source The point in grid coordinates.
     * @param  target The corresponding point in "real world" coordinates.
     * @throws IndexOutOfBoundsException If the source point is not in this grid's range.
     */
    public void setLocalizationPoint(final Point source, final Point2D target) {
        setLocalizationPoint(source.x, source.y, (float)target.getX(), (float)target.getY());
    }

    /**
     * Set a point in this localization grid.
     *
     * @param sourceX  <var>x</var> coordinates in grid coordinates,
     *                 in the range <code>[0..width-1]</code> inclusive.
     * @param sourceY  <var>y</var> coordinates in grid coordinates.
     *                 in the range <code>[0..height-1]</code> inclusive.
     * @param targetX  <var>x</var> coordinates in "real world" coordinates.
     * @param targetY  <var>y</var> coordinates in "real world" coordinates.
     * @throws IndexOutOfBoundsException If the source coordinates is not in this grid's range.
     */
    public void setLocalizationPoint(int sourceX, int sourceY, float targetX, float targetY) {
        final int offset = computeOffset(sourceX, sourceY);
        grid[offset + X_OFFSET] = targetX;
        grid[offset + Y_OFFSET] = targetY;
    }
    
    /**
     * Apply a transformation to every "real world" coordinate points in a sub-region
     * of this grid.
     *
     * @param transform The transform to apply.
     * @param region The bounding rectangle (in grid coordinate) for region where to
     *        apply the transform, or <code>null</code> to transform the whole grid.
     */
    public void transform(final AffineTransform transform, final Rectangle region) {
        assert X_OFFSET  == 0 : X_OFFSET;
        assert Y_OFFSET  == 1 : Y_OFFSET;
        assert CP_LENGTH == 2 : CP_LENGTH;
        if (region == null) {
            transform.transform(grid, 0, grid, 0, width*height);
            return;
        }
        computeOffset(region.x, region.y); // Range check.
        int j = region.x + region.width;
        if (j > width) {
            throw new IndexOutOfBoundsException(String.valueOf(j));
        }
        j = region.y + region.height; // Range check performed in the loop.
        while (--j >= region.y) {
            final int offset = computeOffset(region.x, j);
            transform.transform(grid, offset, grid, offset, region.width);
        }
    }

    /**
     * Returns <code>true</code> if this localization grid
     * contains at least one <code>NaN</code> value.
     */
    public boolean isNaN() {
        for (int i=grid.length; --i>=0;) {
            if (Float.isNaN(grid[i])) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns <code>true</code> if all coordinates in this grid are increasing or decreasing.
     * More specifically, returns <code>true</code> if the following conditions are meets:
     * <ul>
     *   <li>Coordinates in a row must be increasing or decreasing. If <code>strict</code> is
     *       <code>true</code>, then coordinates must be strictly increasing or decreasing (i.e.
     *       equals value are not accepted). <code>NaN</code> values are always ignored.</li>
     *   <li>Coordinates in all rows must be increasing, or coordinates in all rows must be
     *       decreasing.</li>
     *   <li>Idem for columns (Coordinates in a columns must be increasing or decreasing,
     *       etc.).</li>
     * </ul>
     *
     * <var>x</var> and <var>y</var> coordinates are tested independently.
     *
     * @param  strict <code>true</code> to require strictly increasing or decreasing order,
     *         or <code>false</code> to accept values that are equals.
     * @return <code>true</code> if coordinates are increasing or decreasing in the same
     *         direction for all rows and columns.
     */
    public boolean isMonotonic(final boolean strict) {
        int orderX = INCREASING|DECREASING;
        int orderY = INCREASING|DECREASING;
        if (!strict) {
            orderX |= EQUALS;
            orderY |= EQUALS;
        }
        for (int i=0; i<width; i++) {
            final int offset = computeOffset(i,0);
            final int s = CP_LENGTH * width;
            if ((orderX = testOrder(grid, offset+X_OFFSET, height, s, orderX)) == 0) return false;
            if ((orderY = testOrder(grid, offset+Y_OFFSET, height, s, orderY)) == 0) return false;
        }
        orderX = INCREASING|DECREASING;
        orderY = INCREASING|DECREASING;
        if (!strict) {
            orderX |= EQUALS;
            orderY |= EQUALS;
        }
        for (int j=0; j<height; j++) {
            final int offset = computeOffset(0,j);
            final int s = CP_LENGTH;
            if ((orderX = testOrder(grid, offset+X_OFFSET, width, s, orderX)) == 0) return false;
            if ((orderY = testOrder(grid, offset+Y_OFFSET, width, s, orderY)) == 0) return false;
        }
        return true;
    }

    /** Constant for {@link #testOrder}. */ private static final int INCREASING = 1;
    /** Constant for {@link #testOrder}. */ private static final int DECREASING = 2;
    /** Constant for {@link #testOrder}. */ private static final int EQUALS     = 4;

    /**
     * Check the ordering of elements in a sub-array. {@link Float#NaN} values are ignored.
     *
     * @param grid   The {link #grid} array.
     * @param offset The first element to test.
     * @param num    The number of elements to test.
     * @param step   The amount to increment <code>offset</code> in order to reach the next element.
     * @param flags  A combinaison of {@link #INCREASING}, {@link #DECREASING} and {@link #EQUALS}
     *               that specify which ordering are accepted.
     * @return       0 if the array is unordered. Otherwise, returns <code>flags</code> with maybe
     *               one of {@link #INCREASING} or {@link #DECREASING} flags cleared.
     */
    private static int testOrder(final float[] grid, int offset, int num, final int step, int flags)
    {
        // We will check (num-1) combinaisons of coordinates.
        for (--num; --num>=0; offset += step) {
            final float v1 = grid[offset];
            if (Float.isNaN(v1)) continue;
            while (true) {
                final float v2 = grid[offset + step];
                final int required, clear;
                if (v1 == v2) {
                    required =  EQUALS;      // "equals" must be accepted.
                    clear    = ~0;           // Do not clear anything.
                } else if (v2 > v1) {
                    required =  INCREASING;  // "increasing" must be accepted.
                    clear    = ~DECREASING;  // do not accepts "decreasing" anymore.
                } else if (v2 < v1) {
                    required =  DECREASING;  // "decreasing" must be accepted.
                    clear    = ~INCREASING;  // do not accepts "increasing" anymore.
                } else {
                    // 'v2' is NaN. Search for the next element.
                    if (--num < 0) {
                        return flags;
                    }
                    offset += step;
                    continue; // Mimic the "goto" statement.
                }
                if ((flags & required) == 0) {
                    return 0;
                }
                flags &= clear;
                break;
            }
        }
        return flags;
    }

    /**
     * <pre>c + cx*x + cy*y</pre>.
     */
    private void fitPlane(final int offset) {
        double sum_z  = 0;
        double sum_xx = 0; // TODO: We must have a mathematical identity for this one?
        double sum_yy = 0; // TODO: We must have a mathematical identity for this one?
        double sum_xy = 0; // TODO: We must have a mathematical identity for this one?
        double sum_zx = 0;
        double sum_zy = 0;

        int n=offset;
        for (int y=0; y<height; y++) {
            for (int x=0; x<width; x++) {
                assert computeOffset(x,y)+offset == n : n;
                final float z = grid[n];
                sum_z  += z;
                sum_xx += x*x;
                sum_yy += y*y;
                sum_xy += x*y;
                sum_zx += z*x;
                sum_zy += z*y;
                n += CP_LENGTH;
            }
        }
        n = (n-offset)/CP_LENGTH;
        assert n == width * height : n;
        final double sum_x = (n * (width -1)) / 2;
        final double sum_y = (n * (height-1)) / 2;
        /*
         *    ( sum_zx - sum_z*sum_x )  =  cx*(sum_xx - sum_x*sum_x) + cy*(sum_xy - sum_x*sum_y)
         *    ( sum_zy - sum_z*sum_y )  =  cx*(sum_xy - sum_x*sum_y) + cy*(sum_yy - sum_y*sum_y)
         */
        final double ZX = sum_zx - sum_z*sum_x/n;
        final double ZY = sum_zy - sum_z*sum_y/n;
        final double XX = sum_xx - sum_x*sum_x/n;
        final double XY = sum_xy - sum_x*sum_y/n;
        final double YY = sum_yy - sum_y*sum_y/n;
        final double den= (XY*XY - XX*YY);

        final double cy = (ZX*XY - ZY*XX) / den;
        final double cx = (ZY*XY - ZX*YY) / den;
        final double c  = (sum_z - (cx*sum_x + cy*sum_y)) / n;
    }
}
