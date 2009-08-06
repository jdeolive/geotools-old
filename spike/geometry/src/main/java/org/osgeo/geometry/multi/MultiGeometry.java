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
package org.osgeo.geometry.multi;

import java.util.List;

import org.osgeo.geometry.Geometry;
import org.osgeo.geometry.composite.CompositeGeometry;
import org.osgeo.geometry.primitive.Point;

/**
 * Basic aggregation type for {@link Geometry} objects.
 * <p>
 * In contrast to a {@link CompositeGeometry}, a <code>MultiGeometry</code> has no constraints on the topological
 * relations between the contained geometries, i.e. their interiors may intersect.
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 *
 * @version. $Revision$, $Date$
 *
 * @param <T>
 *            the type of the contained geometries
 */
public interface MultiGeometry<T extends Geometry> extends Geometry, List<T> {

    public enum MultiGeometryType {
        MULTI_GEOMETRY,
        MULTI_POINT,
        MULTI_CURVE,
        MULTI_LINE_STRING,
        MULTI_SURFACE,
        MULTI_POLYGON,
        MULTI_SOLID
    }

    /**
     * Must always return {@link Geometry.GeometryType#MULTI_GEOMETRY}.
     *
     * @return {@link Geometry.GeometryType#MULTI_GEOMETRY}.
     */
    public GeometryType getGeometryType();

    /**
     * @return the type of MultiGeometry, see {@link MultiGeometryType}
     */
    public MultiGeometryType getMultiGeometryType();

    /**
     * Returns the centroid of the contained geometries.
     *
     * @return the centroid
     */
    public Point getCentroid();

}
