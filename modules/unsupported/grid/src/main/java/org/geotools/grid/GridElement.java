/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.geotools.grid;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Polygon;

/**
 *
 * @author michael
 */
public interface GridElement {

    /**
     * Gets the area of this grid element.
     *
     * @return the area
     */
    public double getArea();

    /**
     * Gets the bounds of this grid element.
     *
     * @return the bounding rectangle
     */
    public Envelope getBounds();

    /**
     * Gets the center coordinates of this grid element.
     *
     * @return the center coordinates
     */
    public Coordinate getCenter();

    /**
     * Gets the vertices of this grid element.
     *
     * @return the vertices
     */
    public Coordinate[] getVertices();

    /**
     * Creates a new {@code Polygon} from the vertices of this
     * grid element.
     * 
     * @return a new {@code Polygon}
     */
    public Polygon toPolygon();
    
}
