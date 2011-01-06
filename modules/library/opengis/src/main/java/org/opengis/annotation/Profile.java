/*$************************************************************************************************
 **
 ** $Id: Profile.java 1263 2008-07-09 17:25:51Z desruisseaux $
 **
 ** $URL: https://geoapi.svn.sourceforge.net/svnroot/geoapi/tags/2.3-M1/geoapi/src/main/java/org/opengis/annotation/Profile.java $
 **
 ** Copyright (C) 2003-2005 Open GIS Consortium, Inc.
 ** All Rights Reserved. http://www.opengis.org/legal/
 **
 *************************************************************************************************/
package org.opengis.annotation;

import java.lang.annotation.Target;
import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.*;


/**
 * An annotation mapping an interface, methods or fields to a profile.
 *
 * @author  Martin Desruisseaux (IRD)
 * @since   GeoAPI 2.0
 */
@Documented
@Target({TYPE, FIELD, METHOD})
public @interface Profile {
    /**
     * The level for the annoted element. {@link ComplianceLevel#CORE CORE} means
     * that all profiles should provides this element.
     *
     * @return The compliance level.
     */
    ComplianceLevel level();
}
