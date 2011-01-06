/*$************************************************************************************************
 **
 ** $Id: DateAndTime.java 982 2007-03-27 10:54:51Z desruisseaux $
 **
 ** $URL: https://geoapi.svn.sourceforge.net/svnroot/geoapi/tags/2.3-M2/geoapi-pending/src/main/java/org/opengis/temporal/DateAndTime.java $
 **
 ** Copyright (C) 2005 Open GIS Consortium, Inc.
 ** All Rights Reserved. http://www.opengis.org/legal/
 **
 *************************************************************************************************/
package org.opengis.temporal;

import org.opengis.annotation.UML;

import static org.opengis.annotation.Obligation.*;
import static org.opengis.annotation.Specification.*;


/**
 * Provides a single data type for identifying a temporal position with a resolution
 * of less than a day.
 *
 * @author Stephane Fellah (Image Matters)
 * @author Alexander Petkov
 */
@UML(identifier="TM_DateAndTime", specification=ISO_19108)
public interface DateAndTime extends ClockTime, CalendarDate {
}
