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
 * the {@link #getMathTransform} method.    If this transformation is close enough to an
 * affine transform, then an instance of {@link AffineTransform} is returned. Otherwise,
 * a transform backed by the localization grid is returned.
 *
 * @version $Id: LocalizationGrid.java,v 1.7 2003/02/28 11:43:16 desruisseaux Exp $
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
    private double[] grid;

    /**
     * A global affine transform for the whole grid. This affine transform
     * will be computed when first requested using a "least squares" fitting.
     */
    private transient AffineTransform global;

    /**
     * A math transform from grid to "real world" data.
     * Will be computed only when first needed.
     */
    private transient MathTransform2D transform;
    
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
        this.grid   = new double[width * height * CP_LENGTH];
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
     * outside this grid's range, use {@link #getMathTransform} instead.
     *
     * @param  source The point in grid coordinates.
     * @return target The corresponding point in "real world" coordinates.
     * @throws IndexOutOfBoundsException If the source point is not in this grid's range.
     */
    public synchronized Point2D getLocalizationPoint(final Point source) {
        final int offset = computeOffset(source.x, source.y);
        return new Point2D.Double(grid[offset + X_OFFSET],
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
        setLocalizationPoint(source.x, source.y, target.getX(), target.getY());
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
    public synchronized void setLocalizationPoint(int    sourceX, int    sourceY,
                                                  double targetX, double targetY)
    {
        final int offset = computeOffset(sourceX, sourceY);
        if (transform != null) {
            transform = null;
            grid = (double[]) grid.clone();
        }
        global = null;
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
    public synchronized void transform(final AffineTransform transform, final Rectangle region) {
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
            if (this.transform != null) {
                this.transform = null;
                grid = (double[]) grid.clone();
            }
            transform.transform(grid, offset, grid, offset, region.width);
        }
        global = null;
    }

    /**
     * Returns <code>true</code> if this localization grid
     * contains at least one <code>NaN</code> value.
     */
    public synchronized boolean isNaN() {
        for (int i=grid.length; --i>=0;) {
            if (Double.isNaN(grid[i])) {
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
    public synchronized boolean isMonotonic(final boolean strict) {
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
    private static int testOrder(final double[] grid, int offset, int num, final int step, int flags)
    {
        // We will check (num-1) combinaisons of coordinates.
        for (--num; --num>=0; offset += step) {
            final double v1 = grid[offset];
            if (Double.isNaN(v1)) continue;
            while (true) {
                final double v2 = grid[offset + step];
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
     * Make sure that the grid doesn't contains identical consecutive ordinates. If many
     * consecutives ordinates are found to be identical in a row or in a column, then
     * the first one is left inchanged and the other ones are linearly interpolated.
     */
    public void removeSingularities() {
        removeSingularities(X_OFFSET, false);
        removeSingularities(X_OFFSET, true );
        removeSingularities(Y_OFFSET, false);
        removeSingularities(Y_OFFSET, true );
    }

    /**
     * Apply a linear interpolation on consecutive identical ordinates.
     *
     * @param index     The offset of the ordinate to test.
     *                  Should be {@link #X_OFFSET} or {@link #Y_OFFSET}.
     * @param vertical  <code>true</code> to scan the grid vertically, or
     *                  <code>false</code> to scan the grid horizontally.
     */
    private void removeSingularities(final int index, final boolean vertical) {
        final int step, val1, val2;

        if (vertical) {
            step = CP_LENGTH*width;
            val1 = width;
            val2 = height;
        } else {
            step = CP_LENGTH;
            val1 = height;
            val2 = width;
        }
        for (int i=0; i<val1; i++) {
            final int offset;
            if (vertical) {
                offset = computeOffset(i,0) + index;
            } else {
                offset = computeOffset(0,i) + index;
            }
            int singularityOffset = -1;
            for (int j=1; j<val2 ; j++) {
                final int previousOffset = offset+step*(j-1);
                final int currentOffset  = previousOffset + step;
                if (grid[previousOffset] == grid [currentOffset]) {
                    if (singularityOffset == -1) {
                        singularityOffset = (previousOffset==offset) ? previousOffset
                                                                     : previousOffset-step;
                    }
                } else if (singularityOffset != -1) {
                    final int num = (currentOffset-singularityOffset)/step + 1;
                    replaceSingularity(grid, singularityOffset,num,step);
                    singularityOffset = -1;
                }
            }
            if (singularityOffset != -1) {
                final int currentOffset = offset+step*(val2-1);
                final int num = (currentOffset-singularityOffset)/step + 1;
                replaceSingularity(grid,singularityOffset,num,step);                
            }            
        }        
    }         
       
    /**
     * Replace consecutive singularity by linear values in sub-array.
     *
     * Example (we consider a grid of five element with singularity) :
     *
     *                  before
     *              *--*--*--*--*--*
     *              |07|08|08|08|11|
     *              *--*--*--*--*--*
     * 
     * Params are : offset = 0, num = 5, step = 1
     *
     *                  after
     *              *--*--*--*--*--*
     *              |07|08|09|10|11|
     *              *--*--*--*--*--*
     *                |           |
     *                |           |
     *              linear values are
     *              computed with these
     *              values
     *
     * @param grid The {link #grid} array.
     * @param offset The first element.
     * @param num    The number of element.
     * @param step   The amount to increment <code>offset</code> in order to reach the next element.
     */
    private static void replaceSingularity(final double[] grid, int offset, int num, final int step)
    {
        final double increment = (grid[offset+(num-1)*step] - grid[offset])/((double)(num-1));
        final double value = grid[offset];
        offset+= step;
        for (int i=0; i<(num-2); i++, offset += step) {
            grid[offset] = value + (increment * (i+1));
        }
    }

    /**
     * Returns an affine transform for the whole grid. This transform is only an approximation
     * for this localization grid.  It is fitted (like "curve fitting") to grid data using the
     * "least squares" method.
     *
     * @return A global affine transform as an approximation for the whole localization grid.
     */
    public synchronized AffineTransform getAffineTransform() {
        if (global == null) {
            final double[] matrix = new double[6];
            fitPlane(X_OFFSET, matrix); assert X_OFFSET==0 : X_OFFSET;
            fitPlane(Y_OFFSET, matrix); assert Y_OFFSET==1 : Y_OFFSET;
            global = new AffineTransform(matrix);
        }
        return (AffineTransform) global.clone();
    }

    /**
     * Fit a plane through the longitude or latitude values. More specifically, find
     * coefficients <var>c</var>, <var>cx</var> and <var>cy</var> for the following
     * equation:
     *
     * <pre>[longitude or latitude] = c + cx*x + cy*y</pre>.
     *
     * where <var>x</var> and <var>cx</var> are grid coordinates.
     * Coefficients are computed using the least-squares method.
     *
     * @param offset {@link X_OFFSET} for fitting longitude values, or {@link X_OFFSET}
     *               for fitting latitude values (assuming tha "real world" coordinates
     *               are longitude and latitude values).
     * @param coeff  An array of length 6 in which to store plane's coefficients.
     *               Coefficients will be store in the following order:
     *
     *                  <code>coeff[0 + offset] = cx;</code>
     *                  <code>coeff[2 + offset] = cy;</code>
     *                  <code>coeff[4 + offset] = c;</code>
     */
    private void fitPlane(final int offset, final double[] coeff) {
        /*
         * Compute the sum of x, y and z values. Compute also the sum of x*x, y*y, x*y, z*x and z*y
         * values. When possible, we will avoid to compute the sum inside the loop and use the
         * following identities instead:
         *
         *           1 + 2 + 3 ... + n    =    n*(n+1)/2              (arithmetic series)
         *        1² + 2² + 3² ... + n²   =    n*(n+0.5)*(n+1)/3
         */
        double x,y,z, xx,yy, xy, zx,zy;
        z = zx = zy = 0; // To be computed in the loop.
        int n=offset;
        for (int yi=0; yi<height; yi++) {
            for (int xi=0; xi<width; xi++) {
                assert computeOffset(xi,yi)+offset == n : n;
                final double zi = grid[n];
                z  += zi;
                zx += zi*xi;
                zy += zi*yi;
                n  += CP_LENGTH;
            }
        }
        n = (n-offset)/CP_LENGTH;
        assert n == width * height : n;
        x  = (n * (double) (width -1))            / 2;
        y  = (n * (double) (height-1))            / 2;
        xx = (n * (width -0.5) * (width -1))      / 3;
        yy = (n * (height-0.5) * (height-1))      / 3;
        xy = (n * (double)((height-1)*(width-1))) / 4;
        /*
         * Solve the following equations for cx and cy:
         *
         *    ( zx - z*x )  =  cx*(xx - x*x) + cy*(xy - x*y)
         *    ( zy - z*y )  =  cx*(xy - x*y) + cy*(yy - y*y)
         */
        zx -= z*x/n;
        zy -= z*y/n;
        xx -= x*x/n;
        xy -= x*y/n;
        yy -= y*y/n;
        final double den= (xy*xy - xx*yy);
        final double cy = (zx*xy - zy*xx) / den;
        final double cx = (zy*xy - zx*yy) / den;
        final double c  = (z - (cx*x + cy*y)) / n;
        coeff[0 + offset] = cx;
        coeff[2 + offset] = cy;
        coeff[4 + offset] = c;
    }

    /**
     * Returns a math transform from grid to "real world" coordinates.
     */
    public synchronized MathTransform2D getMathTransform() {
        if (transform == null) {
            // Note: 'grid' is not cloned. This GridLocalization's grid
            //       will need to be cloned if a "set" method is invoked
            //       after the math transform creation.
            transform = new LocalizationGridTransform2D(width, height, grid, getAffineTransform());
        }
        return transform;
    }
}
