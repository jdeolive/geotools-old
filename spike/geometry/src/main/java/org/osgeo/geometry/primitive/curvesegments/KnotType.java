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

/**
 * Defines allowed values for the knots' type. Uniform knots implies that all knots are of multiplicity 1 and they
 * differ by a positive constant from the preceding knot. Knots are quasi-uniform iff they are of multiplicity (degree +
 * 1) at the ends, of multiplicity 1 elsewhere, and they differ by a positive constant from the preceding knot.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 *
 * @version $Revision:$, $Date:$
 */
public enum KnotType {

    /**
     *
     */
    UNSPECIFIED,

    /**
     * All knots are of multiplicity 1 and they differ by a positive constant from the preceding knot.
     */
    UNIFORM,

    /**
     * Multiplicity of the knots is (degree + 1) at the ends, 1 elsewhere, and knots differ by a positive constant from
     * the preceding knot.
     */
    QUASI_UNIFORM,

    /**
     * ???
     */
    BEZIER

}
