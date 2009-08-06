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

import org.osgeo.commons.uom.Measure;
import org.osgeo.commons.uom.Unit;
import org.osgeo.geometry.primitive.Curve;
import org.osgeo.geometry.primitive.Point;

/**
 * A {@link CurveSegment} that is defined by a base {@link Curve} and an offset.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public interface OffsetCurve extends CurveSegment {

    /**
     * Returns the {@link Curve} from which this curve segment is defined as using an offset.
     * 
     * @return the <code>Curve</code> used as the base geometry
     */
    public Curve getBaseCurve();

    /**
     * Returns the distance from the base curve.
     * 
     * @param requestedUnits
     *            units that the distance shall be expressed as 
     * @return the distance
     */
    public Measure getDistance( Unit requestedUnit );

    /**
     * Returns the direction of the offset.
     * 
     * @return the direction of the offset
     */
    public Point getDirection();
}
