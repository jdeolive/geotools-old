/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 */
package org.geotools.graph;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import org.geotools.feature.Feature;


/**
 * DOCUMENT ME!
 *
 * @author Justin Deoliveira
 * @version $Revision: 1.1 $
 */
public class PointNode extends Node {
    /** DOCUMENT ME! */
    private double m_x;

    /** DOCUMENT ME! */
    private double m_y;

    /**
     * Creates a new PointNode object.
     *
     * @param feature DOCUMENT ME!
     * @param edgeList DOCUMENT ME!
     * @param coord DOCUMENT ME!
     */
    public PointNode(Feature feature, EdgeList edgeList, Coordinate coord) {
        super(feature, edgeList);
        m_x = coord.x;
        m_y = coord.y;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public double x() {
        return (m_x);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public double y() {
        return (m_y);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Coordinate asCoordinate() {
        return (new Coordinate(x(), y()));
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Geometry buildGeometry() {
        return (new GeometryFactory().createPoint(new Coordinate(m_x, m_y)));
    }
}
