/*
 * Geotools - OpenSource mapping toolkit
 * (C) 2002, Centre for Computational Geography
 * (C) 2001, Institut de Recherche pour le Développement
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
package org.geotools.renderer.lite;

import java.awt.*;
import java.awt.geom.*;
import com.vividsolutions.jts.geom.*;

/**
 * A thin wrapper that adapts a JTS geometry to the Shape interface
 * so that the geometry can be used by java2d without coordinate
 * cloning
 *
 * @version $Id: LiteShape.java,v 1.2 2003/02/14 15:51:02 ianturton Exp $
 * @author Andrea Aime
 */
public class LiteShape implements java.awt.Shape {
    private static final PrecisionModel floatingPM = new PrecisionModel();
    private Geometry geometry;
    private boolean generalize = true;
    private double maxDistance = 1.0;
    
    /** Creates a new instance of GeometryShape */
    public LiteShape(Geometry g) {
        //System.out.println("Shape created");
        geometry = g;
    }
    
    public LiteShape(Geometry g, boolean generalize) {
        this(g);
        this.generalize = generalize;
    }
    
    public LiteShape(Geometry g, boolean generalize, double maxDistance) {
        this(g, generalize);
        this.maxDistance = maxDistance;
    }
    
    /** Tests if the interior of the <code>Shape</code> entirely contains the
     * specified <code>Rectangle2D</code>.
     * This method might conservatively return <code>false</code> when:
     * <ul>
     * <li>
     * the <code>intersect</code> method returns <code>true</code> and
     * <li>
     * the calculations to determine whether or not the
     * <code>Shape</code> entirely contains the <code>Rectangle2D</code>
     * are prohibitively expensive.
     * </ul>
     * This means that this method might return <code>false</code> even
     * though the <code>Shape</code> contains the
     * <code>Rectangle2D</code>.
     * The <code>Area</code> class can be used to perform more accurate
     * computations of geometric intersection for any <code>Shape</code>
     * object if a more precise answer is required.
     * @param r The specified <code>Rectangle2D</code>
     * @return <code>true</code> if the interior of the <code>Shape</code>
     *          entirely contains the <code>Rectangle2D</code>;
     *          <code>false</code> otherwise or, if the <code>Shape</code>
     *          contains the <code>Rectangle2D</code> and the
     *          <code>intersects</code> method returns <code>true</code>
     *          and the containment calculations would be too expensive to
     *          perform.
     * @see #contains(double, double, double, double)
     *
     */
    public boolean contains(Rectangle2D r) {
        Geometry rect = rectangleToGeometry(r);
        return geometry.contains(rect);
    }
    
    
    /** Tests if a specified {@link Point2D} is inside the boundary
     * of the <code>Shape</code>.
     * @param p a specified <code>Point2D</code>
     * @return <code>true</code> if the specified <code>Point2D</code> is
     *          inside the boundary of the <code>Shape</code>;
     * 		<code>false</code> otherwise.
     *
     */
    public boolean contains(Point2D p) {
        Coordinate coord = new Coordinate(p.getX(), p.getY());
        Geometry point = new com.vividsolutions.jts.geom.Point(coord, floatingPM, 0);
        return geometry.contains(point);
    }
    
    /** Tests if the specified coordinates are inside the boundary of the
     * <code>Shape</code>.
     * @param x,&nbsp;y the specified coordinates
     * @return <code>true</code> if the specified coordinates are inside
     *         the <code>Shape</code> boundary; <code>false</code>
     *         otherwise.
     *
     */
    public boolean contains(double x, double y) {
        Coordinate coord = new Coordinate(x, y);
        Geometry point = new com.vividsolutions.jts.geom.Point(coord, floatingPM, 0);
        return geometry.contains(point);
    }
    
    /** Tests if the interior of the <code>Shape</code> entirely contains
     * the specified rectangular area.  All coordinates that lie inside
     * the rectangular area must lie within the <code>Shape</code> for the
     * entire rectanglar area to be considered contained within the
     * <code>Shape</code>.
     * <p>
     * This method might conservatively return <code>false</code> when:
     * <ul>
     * <li>
     * the <code>intersect</code> method returns <code>true</code> and
     * <li>
     * the calculations to determine whether or not the
     * <code>Shape</code> entirely contains the rectangular area are
     * prohibitively expensive.
     * </ul>
     * This means that this method might return <code>false</code> even
     * though the <code>Shape</code> contains the rectangular area.
     * The <code>Area</code> class can be used to perform more accurate
     * computations of geometric intersection for any <code>Shape</code>
     * object if a more precise answer is required.
     * @param x,&nbsp;y the coordinates of the specified rectangular area
     * @param w the width of the specified rectangular area
     * @param h the height of the specified rectangular area
     * @return <code>true</code> if the interior of the <code>Shape</code>
     * 		entirely contains the specified rectangular area;
     * 		<code>false</code> otherwise or, if the <code>Shape</code>
     * 		contains the rectangular area and the
     * 		<code>intersects</code> method returns <code>true</code>
     * 		and the containment calculations would be too expensive to
     * 		perform.
     * @see java.awt.geom.Area
     * @see #intersects
     *
     */
    public boolean contains(double x, double y, double w, double h) {
        Geometry rect = createJtsRectangle(x, y, w, h);
        return geometry.contains(rect);
    }
    
       
    /** Returns an integer {@link Rectangle} that completely encloses the
     * <code>Shape</code>.  Note that there is no guarantee that the
     * returned <code>Rectangle</code> is the smallest bounding box that
     * encloses the <code>Shape</code>, only that the <code>Shape</code>
     * lies entirely within the indicated  <code>Rectangle</code>.  The
     * returned <code>Rectangle</code> might also fail to completely
     * enclose the <code>Shape</code> if the <code>Shape</code> overflows
     * the limited range of the integer data type.  The
     * <code>getBounds2D</code> method generally returns a
     * tighter bounding box due to its greater flexibility in
     * representation.
     * @return an integer <code>Rectangle</code> that completely encloses
     *                 the <code>Shape</code>.
     * @see #getBounds2D
     *
     */
    public Rectangle getBounds() {
        Coordinate[] coords = geometry.getEnvelope().getCoordinates();
        // get out corners. the documentation doens't specify in which
        // order the bounding box coordinates are returned
        double x1, y1, x2, y2;
        x1 = x2 = coords[0].x;
        y1 = y2 = coords[0].y;
        for(int i = 1; i < 3; i++) {
            double x = coords[1].x;
            double y = coords[1].y;
            if(x < x1) x1 = x;
            if(x > x2) x2 = x;
            if(y < y1) y1 = y;
            if(y > y2) y2 = y;
        }
        x1 = Math.ceil(x1); x2 = Math.floor(x2);
        y1 = Math.ceil(y1); y2 = Math.floor(y2);
        return new Rectangle((int) x1, (int) y1, (int) (x2 - x1), (int) (y2 - y1));
    }
    
    /** Returns a high precision and more accurate bounding box of
     * the <code>Shape</code> than the <code>getBounds</code> method.
     * Note that there is no guarantee that the returned
     * {@link Rectangle2D} is the smallest bounding box that encloses
     * the <code>Shape</code>, only that the <code>Shape</code> lies
     * entirely within the indicated <code>Rectangle2D</code>.  The
     * bounding box returned by this method is usually tighter than that
     * returned by the <code>getBounds</code> method and never fails due
     * to overflow problems since the return value can be an instance of
     * the <code>Rectangle2D</code> that uses double precision values to
     * store the dimensions.
     * @return an instance of <code>Rectangle2D</code> that is a
     *                 high-precision bounding box of the <code>Shape</code>.
     * @see #getBounds
     *
     */
    public Rectangle2D getBounds2D() {
        Coordinate[] coords = geometry.getEnvelope().getCoordinates();
        // get out corners. the documentation doens't specify in which
        // order the bounding box coordinates are returned
        double x1, y1, x2, y2;
        x1 = x2 = coords[0].x;
        y1 = y2 = coords[0].y;
        for(int i = 1; i < 3; i++) {
            double x = coords[1].x;
            double y = coords[1].y;
            if(x < x1) x1 = x;
            if(x > x2) x2 = x;
            if(y < y1) y1 = y;
            if(y > y2) y2 = y;
        }
        return new Rectangle2D.Double(x1, y1, x2 - x1, y2 - y1);
    }
    
    /** Returns an iterator object that iterates along the
     * <code>Shape</code> boundary and provides access to the geometry of the
     * <code>Shape</code> outline.  If an optional {@link AffineTransform}
     * is specified, the coordinates returned in the iteration are
     * transformed accordingly.
     * <p>
     * Each call to this method returns a fresh <code>PathIterator</code>
     * object that traverses the geometry of the <code>Shape</code> object
     * independently from any other <code>PathIterator</code> objects in use
     * at the same time.
     * <p>
     * It is recommended, but not guaranteed, that objects
     * implementing the <code>Shape</code> interface isolate iterations
     * that are in process from any changes that might occur to the original
     * object's geometry during such iterations.
     * <p>
     * Before using a particular implementation of the <code>Shape</code>
     * interface in more than one thread simultaneously, refer to its
     * documentation to verify that it guarantees that iterations are isolated
     * from modifications.
     * @param at an optional <code>AffineTransform</code> to be applied to the
     * 		coordinates as they are returned in the iteration, or
     * 		<code>null</code> if untransformed coordinates are desired
     * @return a new <code>PathIterator</code> object, which independently
     * 		traverses the geometry of the <code>Shape</code>.
     *
     */
    public PathIterator getPathIterator(AffineTransform at) {
        PathIterator pi = null;
        
        //System.out.println("Path iterator");
        
        // return iterator according to the kind of geometry we include
        if(this.geometry instanceof com.vividsolutions.jts.geom.Polygon)
            pi = new PolygonIterator((com.vividsolutions.jts.geom.Polygon) geometry, 
                                        at, generalize, maxDistance);
        else if(this.geometry instanceof com.vividsolutions.jts.geom.LinearRing)
            pi = new LineIterator((com.vividsolutions.jts.geom.LinearRing) geometry, 
                                        at, generalize, maxDistance);
        else if(this.geometry instanceof com.vividsolutions.jts.geom.LineString)
            pi = new LineIterator((com.vividsolutions.jts.geom.LineString) geometry,
                                        at, generalize, maxDistance);
        else if(this.geometry instanceof com.vividsolutions.jts.geom.GeometryCollection)
            pi = new GeomCollectionIterator(
                     (com.vividsolutions.jts.geom.GeometryCollection) geometry, 
                                        at, generalize, maxDistance);

        return pi;
    }
    
    /** Returns an iterator object that iterates along the <code>Shape</code>
     * boundary and provides access to a flattened view of the
     * <code>Shape</code> outline geometry.
     * <p>
     * Only SEG_MOVETO, SEG_LINETO, and SEG_CLOSE point types are
     * returned by the iterator.
     * <p>
     * If an optional <code>AffineTransform</code> is specified,
     * the coordinates returned in the iteration are transformed
     * accordingly.
     * <p>
     * The amount of subdivision of the curved segments is controlled
     * by the <code>flatness</code> parameter, which specifies the
     * maximum distance that any point on the unflattened transformed
     * curve can deviate from the returned flattened path segments.
     * Note that a limit on the accuracy of the flattened path might be
     * silently imposed, causing very small flattening parameters to be
     * treated as larger values.  This limit, if there is one, is
     * defined by the particular implementation that is used.
     * <p>
     * Each call to this method returns a fresh <code>PathIterator</code>
     * object that traverses the <code>Shape</code> object geometry
     * independently from any other <code>PathIterator</code> objects in use at
     * the same time.
     * <p>
     * It is recommended, but not guaranteed, that objects
     * implementing the <code>Shape</code> interface isolate iterations
     * that are in process from any changes that might occur to the original
     * object's geometry during such iterations.
     * <p>
     * Before using a particular implementation of this interface in more
     * than one thread simultaneously, refer to its documentation to
     * verify that it guarantees that iterations are isolated from
     * modifications.
     * @param at an optional <code>AffineTransform</code> to be applied to the
     * 		coordinates as they are returned in the iteration, or
     * 		<code>null</code> if untransformed coordinates are desired
     * @param flatness the maximum distance that the line segments used to
     *          approximate the curved segments are allowed to deviate
     *          from any point on the original curve
     * @return a new <code>PathIterator</code> that independently traverses
     * 		the <code>Shape</code> geometry.
     *
     */
    public PathIterator getPathIterator(AffineTransform at, double flatness) {
        return getPathIterator(at);
    }
    
    /** Tests if the interior of the <code>Shape</code> intersects the
     * interior of a specified <code>Rectangle2D</code>.
     * This method might conservatively return <code>true</code> when:
     * <ul>
     * <li>
     * there is a high probability that the <code>Rectangle2D</code> and the
     * <code>Shape</code> intersect, but
     * <li>
     * the calculations to accurately determine this intersection
     * are prohibitively expensive.
     * </ul>
     * This means that this method might return <code>true</code> even
     * though the <code>Rectangle2D</code> does not intersect the
     * <code>Shape</code>.
     * @param r the specified <code>Rectangle2D</code>
     * @return <code>true</code> if the interior of the <code>Shape</code> and
     * 		the interior of the specified <code>Rectangle2D</code>
     * 		intersect, or are both highly likely to intersect and intersection
     * 		calculations would be too expensive to perform; <code>false</code>
     * 		otherwise.
     * @see #intersects(double, double, double, double)
     *
     */
    public boolean intersects(Rectangle2D r) {
        Geometry rect = rectangleToGeometry(r);
        return geometry.intersects(rect);
    }
    
    /** Tests if the interior of the <code>Shape</code> intersects the
     * interior of a specified rectangular area.
     * The rectangular area is considered to intersect the <code>Shape</code>
     * if any point is contained in both the interior of the
     * <code>Shape</code> and the specified rectangular area.
     * <p>
     * This method might conservatively return <code>true</code> when:
     * <ul>
     * <li>
     * there is a high probability that the rectangular area and the
     * <code>Shape</code> intersect, but
     * <li>
     * the calculations to accurately determine this intersection
     * are prohibitively expensive.
     * </ul>
     * This means that this method might return <code>true</code> even
     * though the rectangular area does not intersect the <code>Shape</code>.
     * The {@link java.awt.geom.Area Area} class can be used to perform
     * more accurate computations of geometric intersection for any
     * <code>Shape</code> object if a more precise answer is required.
     * @param x,&nbsp;y the coordinates of the specified rectangular area
     * @param w the width of the specified rectangular area
     * @param h the height of the specified rectangular area
     * @return <code>true</code> if the interior of the <code>Shape</code> and
     * 		the interior of the rectangular area intersect, or are
     * 		both highly likely to intersect and intersection calculations
     * 		would be too expensive to perform; <code>false</code> otherwise.
     * @see java.awt.geom.Area
     *
     */
    public boolean intersects(double x, double y, double w, double h) {
        Geometry rect = createJtsRectangle(x, y, w, h);
        return geometry.intersects(rect);
    }
    
    /** Converts the Rectangle2D passed as parameter in a jts Geometry object
      */
    private Geometry rectangleToGeometry(Rectangle2D r) {
        Coordinate[] coords = { new Coordinate(r.getMinX(), r.getMinY()),
                                new Coordinate(r.getMinX(), r.getMaxY()),
                                new Coordinate(r.getMaxX(), r.getMaxY()),
                                new Coordinate(r.getMaxX(), r.getMinY()) };
        LinearRing lr = new com.vividsolutions.jts.geom.LinearRing(coords, floatingPM, 0);
        return new com.vividsolutions.jts.geom.Polygon(lr, floatingPM, 0);
    }
    
     /** Creates a jts Geometry object representing a rectangle with the given
      *  parameters
      */
    private Geometry createJtsRectangle(double x, double y, double w, double h) {
        Coordinate[] coords = { new Coordinate(x, y),
                                new Coordinate(x, y+h),
                                new Coordinate(x + w, y+h),
                                new Coordinate(x + w, y) };
        LinearRing lr = new com.vividsolutions.jts.geom.LinearRing(coords, floatingPM, 0);
        return new com.vividsolutions.jts.geom.Polygon(lr, floatingPM, 0);
    }
}
