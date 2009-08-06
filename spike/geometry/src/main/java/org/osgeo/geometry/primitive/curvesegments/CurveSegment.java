/*
 *    OSGeom -- Geometry Collab
 *
 *    (C) 2009, Open Source Geospatial Foundation (OSGeo)
 *    (C) 2001-2009 Department of Geography, University of Bonn
 *    (C) 2001-2009 lat/lon GmbH
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
 */
package org.osgeo.geometry.primitive.curvesegments;

import org.osgeo.geometry.primitive.Curve;
import org.osgeo.geometry.primitive.Point;

/**
 * A <code>CurveSegment</code> is a portion of a {@link Curve} which uses a single interpolation method.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version. $Revision$, $Date$
 */
public interface CurveSegment {

    /**
     * Convenience enum type for discriminating the different curve segment variants.
     */
    public enum CurveSegmentType {
        ARC, ARC_BY_BULGE, ARC_BY_CENTER_POINT, ARC_STRING, ARC_STRING_BY_BULGE, BEZIER, BSPLINE, CIRCLE, CIRCLE_BY_CENTER_POINT, CLOTHOID, CUBIC_SPLINE, GEODESIC, GEODESIC_STRING, LINE_STRING_SEGMENT, OFFSET_CURVE
    }

    /**
     * Returns the coordinate dimension, i.e. the dimension of the space that the curve is embedded in.
     * 
     * @return the coordinate dimension
     */
    public int getCoordinateDimension();

    /**
     * Returns the type of curve segment.
     *
     * @return the type of curve segment
     */
    public CurveSegmentType getSegmentType();

    /**
     * Returns the start point of the segment.
     *
     * @return the start point of the segment
     */
    public Point getStartPoint();

    /**
     * Returns the end point of the segment.
     *
     * @return the end point of the segment
     */
    public Point getEndPoint();
}
