/*$************************************************************************************************
 **
 ** $Id: TemporalGeometricPrimitive.java 1208 2008-05-14 15:46:36Z desruisseaux $
 **
 ** $URL: https://geoapi.svn.sourceforge.net/svnroot/geoapi/tags/2.3-M2/geoapi-pending/src/main/java/org/opengis/temporal/TemporalGeometricPrimitive.java $
 **
 ** Copyright (C) 2005 Open GIS Consortium, Inc.
 ** All Rights Reserved. http://www.opengis.org/legal/
 **
 *************************************************************************************************/
package org.opengis.temporal;

import org.opengis.annotation.UML;

import static org.opengis.annotation.Specification.*;


/**
 * An abstract class with two subclasses for representing
 * a temporal instant and a temporal period.
 *
 * @author Stephane Fellah (Image Matters)
 * @author Alexander Petkov
 */
@UML(identifier="TM_GeometricPrimitive", specification=ISO_19108)
public interface TemporalGeometricPrimitive extends TemporalPrimitive, Separation {
}
