/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2008, Geotools Project Managment Committee (PMC)
 *    (C) 2008, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.resources.jaxb.metadata;

import java.util.Date;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;


/**
 * JAXB adapter in order to surround the date value with a {@code <gco:DateTime>} tags,
 * respecting the ISO-13139 standard.
 *
 * @since 2.5
 * @source $URL$
 * @author Cédric Briançon
 */
public class DateTimeAdapter extends XmlAdapter<DateTimeAdapter, Date> {
    /**
     * The date value.
     */
    private Date date;

    /**
     * Empty constructor for JAXB only.
     */
    private DateTimeAdapter() {
    }

    /**
     * Builds an adapter for {@link Date}.
     *
     * @param date The date to marshall.
     */
    protected DateTimeAdapter(final Date date) {
        this.date = date;
    }

    /**
     * Returns the date matching with the metadata value. This method is systematically
     * called at marshalling-time by JAXB.
     */
    @XmlElement(name = "DateTime", namespace = "http://www.isotc211.org/2005/gco")
    public Date getDate() {
        return date;
    }

    /**
     * Sets the value for the metadata date. This method is systematically called at
     * unmarshalling-time by JAXB.
     */
    public void setDate(final Date date) {
        this.date = date;
    }

    /**
     * Does the link between a date red from an XML stream and the object which will
     * contains this value. JAXB calls automatically this method at unmarshalling-time.
     *
     * @param value The adapter for this metadata value.
     * @return A {@linkplain Date date} which represents the metadata value.
     */
    public Date unmarshal(final DateTimeAdapter value) {
        if (value == null) {
            return null;
        }
        return value.date;
    }

    /**
     * Does the link between {@linkplain Date date} and the way they will be marshalled into
     * an XML file or stream. JAXB calls automatically this method at marshalling-time.
     *
     * @param value The date value.
     * @return The adapter for this date.
     */
    public DateTimeAdapter marshal(final Date value) {
        if (value == null) {
            return null;
        }
        return new DateTimeAdapter(value);
    }
}
