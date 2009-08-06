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
package org.osgeo.geometry.primitive;

/**
 * 0-dimensional primitive.
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version. $Revision$, $Date$
 */
public interface Point extends GeometricPrimitive {

    /**
     * Must always return {@link GeometricPrimitive.PrimitiveType#Point}.
     *
     * @return {@link GeometricPrimitive.PrimitiveType#Point}
     */
    public PrimitiveType getPrimitiveType();

    /**
     *
     * @return x ordinate
     */
    public double getX();

    /**
     *
     * @return y ordinate
     */
    public double getY();

    /**
     *
     * @return z ordinate
     */
    public double getZ();

    /**
     *
     * @param dimension
     * @return ordinate of passed dimension. If passed dimension is not supported by a point Double.NAN will be
     *         returned
     */
    public double get( int dimension );

    /**
     *
     * @return a points coordinates as an array
     */
    public double[] getAsArray();
}
