/*$************************************************************************************************
 **
 ** $Id: SpatialReferenceSystemUsingGeographicIdentifier.java 1265 2008-07-09 18:24:37Z desruisseaux $
 **
 ** $URL: https://geoapi.svn.sourceforge.net/svnroot/geoapi/tags/2.3-M1/geoapi/src/main/java/org/opengis/referencing/SpatialReferenceSystemUsingGeographicIdentifier.java $
 **
 ** Copyright (C) 2003-2005 Open GIS Consortium, Inc.
 ** All Rights Reserved. http://www.opengis.org/legal/
 **
 *************************************************************************************************/
package org.opengis.referencing;

import org.opengis.annotation.UML;
import static org.opengis.annotation.Specification.*;


/**
 * Spatial reference system using geographic identifier, a reference to a feature with a known
 * spatial location. Spatial reference systems using geographic identifiers are not based on
 * coordinates.
 *
 * @version <A HREF="http://portal.opengeospatial.org/files/?artifact_id=6716">Abstract specification 2.0</A>
 * @author  Martin Desruisseaux (IRD)
 * @since   GeoAPI 1.0
 */
@UML(identifier="RS_SpatialReferenceSystemUsingGeographicIdentifier", specification=ISO_19111)
public interface SpatialReferenceSystemUsingGeographicIdentifier extends ReferenceSystem {
}
