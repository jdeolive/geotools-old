/*$************************************************************************************************
 **
 ** $Id: Extension.java 1299 2008-08-07 13:06:57Z desruisseaux $
 **
 ** $URL: https://geoapi.svn.sourceforge.net/svnroot/geoapi/tags/2.3-M2/geoapi/src/main/java/org/opengis/annotation/Extension.java $
 **
 ** Copyright (C) 2003-2005 Open GIS Consortium, Inc.
 ** All Rights Reserved. http://www.opengis.org/legal/
 **
 *************************************************************************************************/
package org.opengis.annotation;

import java.lang.annotation.Target;
import java.lang.annotation.Retention;
import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;


/**
 * An annotation for classes or method that are <A HREF="http://geoapi.sourceforge.net">GeoAPI</A>
 * extension. This annotation is mutually exclusive with {@link UML}.
 *
 * @author  Martin Desruisseaux (IRD)
 * @since   GeoAPI 2.0
 */
@Documented
@Retention(SOURCE)
@Target({TYPE, FIELD, METHOD})
public @interface Extension {
}
