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

import java.awt.geom.PathIterator;
import com.vividsolutions.jts.geom.*;
import java.awt.geom.AffineTransform;

/**
 * A path iterator for the LiteShape class, specialized to iterate
 * over a geometry collection. It can be seen as a composite, since
 * uses in fact other, simpler iterator to work.
 *
 * @version $Id: GeomCollectionIterator.java,v 1.2 2003/02/12 20:15:04 aaime Exp $
 * @author Andrea Aime
 */
class GeomCollectionIterator implements PathIterator {
    private AffineTransform at;
    private Geometry[] geoms;
    private int currentGeom = 0;
    private PathIterator currentIterator;
    private boolean done = false;
    
    private boolean generalize = true;
    private double maxDistance = 1.0;
    private double xScale;
    private double yScale;
    
    /** Creates a new instance of JTSPolygonIterator */
    public GeomCollectionIterator(com.vividsolutions.jts.geom.GeometryCollection gc, AffineTransform at) {
        int numGeometries = gc.getNumGeometries();
        geoms = new Geometry[numGeometries];
        for(int i = 0; i < numGeometries; i++) {
            geoms[i] = gc.getGeometryN(i);
        }
        
        this.at = at;
        xScale = Math.abs(at.getScaleX());
        yScale = Math.abs(at.getScaleY());
        
        currentIterator = getIterator(geoms[0]);
    }
    
    public GeomCollectionIterator(com.vividsolutions.jts.geom.GeometryCollection gc, AffineTransform at, boolean generalize) {
         this(gc, at);
         this.generalize = generalize;
    }

    
    public GeomCollectionIterator(com.vividsolutions.jts.geom.GeometryCollection gc, AffineTransform at, boolean generalize, double maxDistance) {
         this(gc, at, generalize);
         this.maxDistance = maxDistance;
    }
    
    public void setMaxDistance(int distance) {
        maxDistance = distance;
    }
    
    public double getMaxDistance(int distance) {
        return maxDistance;
    }
    
    private PathIterator getIterator(Geometry g) {
        PathIterator pi = null;
        if(g instanceof com.vividsolutions.jts.geom.Polygon) {
            com.vividsolutions.jts.geom.Polygon p = 
               (com.vividsolutions.jts.geom.Polygon) g;
            pi = new PolygonIterator(p, at, generalize, maxDistance);
        } else if(g instanceof com.vividsolutions.jts.geom.GeometryCollection) {
            com.vividsolutions.jts.geom.GeometryCollection gc = 
               (com.vividsolutions.jts.geom.GeometryCollection) g;
            pi = new GeomCollectionIterator(gc, at, generalize, maxDistance);
        } else if(g instanceof com.vividsolutions.jts.geom.LineString) {
            com.vividsolutions.jts.geom.LineString ls = 
               (com.vividsolutions.jts.geom.LineString) g;
            pi = new LineIterator(ls, at, generalize, maxDistance);
        } else if(g instanceof com.vividsolutions.jts.geom.LinearRing) {
            com.vividsolutions.jts.geom.LinearRing lr = 
               (com.vividsolutions.jts.geom.LinearRing) g;
            pi = new LineIterator(lr, at, generalize, maxDistance);
        }
        return pi;
    }
    
    /** Returns the coordinates and type of the current path segment in
     * the iteration.
     * The return value is the path-segment type:
     * SEG_MOVETO, SEG_LINETO, SEG_QUADTO, SEG_CUBICTO, or SEG_CLOSE.
     * A double array of length 6 must be passed in and can be used to
     * store the coordinates of the point(s).
     * Each point is stored as a pair of double x,y coordinates.
     * SEG_MOVETO and SEG_LINETO types returns one point,
     * SEG_QUADTO returns two points,
     * SEG_CUBICTO returns 3 points
     * and SEG_CLOSE does not return any points.
     * @param coords an array that holds the data returned from
     * this method
     * @return the path-segment type of the current path segment.
     * @see #SEG_MOVETO
     * @see #SEG_LINETO
     * @see #SEG_QUADTO
     * @see #SEG_CUBICTO
     * @see #SEG_CLOSE
     *
     */
    public int currentSegment(double[] coords) {
        return currentIterator.currentSegment(coords);
    }
    
    /** Returns the coordinates and type of the current path segment in
     * the iteration.
     * The return value is the path-segment type:
     * SEG_MOVETO, SEG_LINETO, SEG_QUADTO, SEG_CUBICTO, or SEG_CLOSE.
     * A float array of length 6 must be passed in and can be used to
     * store the coordinates of the point(s).
     * Each point is stored as a pair of float x,y coordinates.
     * SEG_MOVETO and SEG_LINETO types returns one point,
     * SEG_QUADTO returns two points,
     * SEG_CUBICTO returns 3 points
     * and SEG_CLOSE does not return any points.
     * @param coords an array that holds the data returned from
     * this method
     * @return the path-segment type of the current path segment.
     * @see #SEG_MOVETO
     * @see #SEG_LINETO
     * @see #SEG_QUADTO
     * @see #SEG_CUBICTO
     * @see #SEG_CLOSE
     *
     */
    public int currentSegment(float[] coords) {
       return currentIterator.currentSegment(coords);
    }
    
    /** Returns the winding rule for determining the interior of the
     * path.
     * @return the winding rule.
     * @see #WIND_EVEN_ODD
     * @see #WIND_NON_ZERO
     *
     */
    public int getWindingRule() {
        return WIND_NON_ZERO;
    }
    
    /** Tests if the iteration is complete.
     * @return <code>true</code> if all the segments have
     * been read; <code>false</code> otherwise.
     *
     */
    public boolean isDone() {
        return done;
    }
    
    /** Moves the iterator to the next segment of the path forwards
     * along the primary direction of traversal as long as there are
     * more points in that direction.
     *
     */
    public void next() {
        if(currentIterator.isDone()) {
            if(currentGeom < (geoms.length - 1)) {
                currentGeom++;
                currentIterator = getIterator(geoms[currentGeom]);
            } else {
                done = true;
            }
        } else {
            currentIterator.next();
        }
    }
    
    
    
}
