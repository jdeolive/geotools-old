/*$************************************************************************************************
 **
 ** $Id: BoundedSpatialOperator.java 905 2006-11-03 02:16:25Z jive $
 **
 ** $URL: https://geoapi.svn.sourceforge.net/svnroot/geoapi/tags/2.3-M2/geoapi-pending/src/main/java/org/opengis/filter/spatial/BoundedSpatialOperator.java $
 **
 ** Copyright (C) 2005 Open GIS Consortium, Inc.
 ** All Rights Reserved. http://www.opengis.org/legal/
 **
 *************************************************************************************************/
package org.opengis.filter.spatial;

// OpenGIS direct dependencies


/**
 * Marker interface for spatial operators that are a subset of the BBOX relationship.
 * <p>
 * This interface can be used to quickly check when an BBox optimization is applicable.
 * </p>
 * @author Jody Garnett, Refractions Research
 * @since GeoAPI 2.1
 */
public interface BoundedSpatialOperator extends SpatialOperator {

}
