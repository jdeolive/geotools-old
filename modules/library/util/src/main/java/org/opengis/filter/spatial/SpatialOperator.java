/*$************************************************************************************************
 **
 ** $Id: SpatialOperator.java 978 2007-03-27 00:58:39Z jive $
 **
 ** $URL: https://geoapi.svn.sourceforge.net/svnroot/geoapi/tags/2.3-M2/geoapi-pending/src/main/java/org/opengis/filter/spatial/SpatialOperator.java $
 **
 ** Copyright (C) 2005 Open GIS Consortium, Inc.
 ** All Rights Reserved. http://www.opengis.org/legal/
 **
 *************************************************************************************************/
package org.opengis.filter.spatial;

// OpenGIS direct dependencies
import org.opengis.filter.Filter;

// Annotations
import org.opengis.annotation.XmlElement;


/**
 * Abstract base class for operators that perform a spatial comparison on
 * geometric attributes of a feature.
 *
 * @version <A HREF="http://www.opengis.org/docs/02-059.pdf">Implementation specification 1.0</A>
 * @author Chris Dillard (SYS Technologies)
 * @since GeoAPI 2.0
 */
@XmlElement("SpatialOpsType")
public interface SpatialOperator extends Filter {
}
