/*$************************************************************************************************
 **
 ** $Id: PropertyIsNotEqualTo.java 1154 2007-12-19 22:29:42Z jive $
 **
 ** $URL: https://geoapi.svn.sourceforge.net/svnroot/geoapi/tags/2.3-M2/geoapi-pending/src/main/java/org/opengis/filter/PropertyIsNotEqualTo.java $
 **
 ** Copyright (C) 2005 Open GIS Consortium, Inc.
 ** All Rights Reserved. http://www.opengis.org/legal/
 **
 *************************************************************************************************/
package org.opengis.filter;

import org.opengis.annotation.XmlElement;


/**
 * Filter operator that compares that its two sub-expressions are not equal to each other.
 *
 * @version <A HREF="http://www.opengis.org/docs/02-059.pdf">Implementation specification 1.0</A>
 * @author Justin Deoliveira (The Open Planning Project)
 * @since GeoAPI 2.0
 */
@XmlElement("PropertyIsNotEqualTo")
public interface PropertyIsNotEqualTo extends BinaryComparisonOperator {
	/** Operator name used to check FilterCapabilities */
	public static String NAME = "NotEqualTo";
}
