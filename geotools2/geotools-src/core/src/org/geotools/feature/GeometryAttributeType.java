/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2003, Geotools Project Managment Committee (PMC)
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
package org.geotools.feature;

import org.geotools.cs.CoordinateSystem;

import com.vividsolutions.jts.geom.GeometryFactory;

/**
 * A CoordianteSystem aware Geometry AttributeType.
 * <p>
 * This class is the bridge between our FeatureType/AttributeType
 * classes and the CoordianteSystem.
 * </p>
 * <p>
 * This also allows access to the GeometryFactory used by this
 * GeometryAttributeType parse( Object ) method.
 * </p>
 * <p>
 * With JTS14 you can use GeometryFactory to to provide your own
 * CoordianteSequence representation. CoordinateSystem is given the
 * responsiblity of providing this class for the GeometryAttributeType as
 * only it knows the CoordianteSequence class and PercisionModel mosted
 * suitable. It also may know an SRID number suitable for the
 * GeometryFactory to use when constructing new Geometry objects.
 * </p>
 * It is recomended that the CoordinateSystem GeometryFactory also
 * supply the CoordinateSystem as the value for Geometry.getUserData().
 * </p>
 * 
 * @author Jody Garnett, Refractions Research, Inc.
 */
public interface GeometryAttributeType extends AttributeType {

    /**
     * Retrieve the CS_CoordinateSystem used by this GeometryAttributeType.
     * <p>
     * The class CoordinateSystem holds a GeometryFactory that is used for
     * creating new content. By extension this includes the SRID,
     * PercisionModel and CoordinateSequenceFactory information.
     * </p>
     * 
     * @return CS_CoordinateSystem for this GeometryAttributeType
     */
    public Object getCoordianteSystem();
    
    /**
     * The Geometryfactory used for creating new content.
     * 
     * @return GeometryFactory used for new Content
     */
    public GeometryFactory getGeometryFactory();    

}
