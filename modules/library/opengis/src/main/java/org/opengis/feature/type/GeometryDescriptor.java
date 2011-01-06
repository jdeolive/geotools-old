/*$************************************************************************************************
 **
 ** $Id: GeometryDescriptor.java 1256 2008-07-05 07:36:17Z Jive $
 **
 ** $URL: https://geoapi.svn.sourceforge.net/svnroot/geoapi/tags/2.3-M2/geoapi-pending/src/main/java/org/opengis/feature/type/GeometryDescriptor.java $
 **
 ** Copyright (C) 2004-2007 Open GIS Consortium, Inc.
 ** All Rights Reserved. http://www.opengis.org/legal/
 **
 *************************************************************************************************/
package org.opengis.feature.type;

import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * Describes an instance of a geometry attribute.
 * <p>
 * This interface adds no additional methods, the point of it is convenience
 * to type narrow {@link #getType()} to {@link GeometryType}.
 * </p>
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public interface GeometryDescriptor extends AttributeDescriptor {
    /**
     * Override of {@link AttributeDescriptor#getType()} which type narrows
     * to {@link GeometryType}.
     */
    GeometryType getType();

    /**
     * The coordinate reference system in which these geometries are defined.
     * <p>
     * This method may return <code>null</code>, but this should only occur in
     * cases where the actual crs is not known. A common case is when a shapefile
     * does not have an accompanied .prj file.
     * </p>
     */
    CoordinateReferenceSystem getCoordinateReferenceSystem();
}
