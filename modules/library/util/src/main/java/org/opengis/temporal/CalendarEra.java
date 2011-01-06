/*$************************************************************************************************
 **
 ** $Id: CalendarEra.java 1122 2007-11-24 18:49:16Z desruisseaux $
 **
 ** $URL: https://geoapi.svn.sourceforge.net/svnroot/geoapi/tags/2.3-M2/geoapi-pending/src/main/java/org/opengis/temporal/CalendarEra.java $
 **
 ** Copyright (C) 2005 Open GIS Consortium, Inc.
 ** All Rights Reserved. http://www.opengis.org/legal/
 **
 *************************************************************************************************/
package org.opengis.temporal;

import org.opengis.util.InternationalString;
import org.opengis.annotation.UML;

import static org.opengis.annotation.Obligation.*;
import static org.opengis.annotation.Specification.*;


/**
 * Characteristics of each calendar era.
 *
 * @author Stephane Fellah (Image Matters)
 * @author Alexander Petkov
 */
@UML(identifier="TM_CalendarEra", specification=ISO_19108)
public interface CalendarEra {
    /**
     * Uniquely identifies the calendar era within this calendar.
     */
    @UML(identifier="name", obligation=MANDATORY, specification=ISO_19108)
    InternationalString getName();

    /**
     * Provides the name or description of a mythical or historic event which fixes the position
     * of the base scale of the calendar era.
     */
    @UML(identifier="referenceEvent", obligation=OPTIONAL, specification=ISO_19108)
    InternationalString getReferenceEvent();

    /**
     * Provides the date of the reference event expressed as a date in the given calendar.
     */
    @UML(identifier="referenceDate", obligation=OPTIONAL, specification=ISO_19108)
    CalendarDate getReferenceDate();

    /**
     * Provides the {@linkplain JulianDate julian date} that corresponds to the reference date.
     */
    @UML(identifier="julianReference", specification=ISO_19108)
    JulianDate getJulianReference();

    /**
     * Identifies the {@linkplain Period period} for which the calendar era
     * was used as a reference fro dating.
     *
     *
     * @return The period, where the data type for {@linkplain Period#getBegin begin}
     *         and {@link Period#getEnd end} is {@link JulianDate}.
     */
    @UML(identifier="epochOfUse", specification=ISO_19108)
    Period getEpochOfUse();
}
