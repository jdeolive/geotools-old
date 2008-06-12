/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2008, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.resources.jaxb.primitive;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;


/**
 * The ISO-19139 standard specifies that primitive types have to be surrounded by a tags
 * which represents the type of the value, using the namespace {@code gco} linked to the
 * following url {@link http://www.isotc211.org/2005/gco}.
 * For the current case, the long value has to be surrounded by {@code <gco:Integer>}
 * tags.
 *
 * However JAXB is able to marshall primitive java types directly "as it", it would not
 * wrap the value in the required tags.
 * This is the role of this class.
 *
 * @since 2.5
 * @source $URL$
 * @author Cédric Briançon
 */
public final class LongAdapter extends XmlAdapter<LongAdapter, Long> {
    /**
     * The long value to handle.
     */
    private Long value;

    /**
     * Empty constructor used only by JAXB.
     */
    private LongAdapter() {
    }

    /**
     * Constructs an adapter for this value.
     */
    protected LongAdapter(final Long value) {
        this.value = value;
    }

    /**
     * Returns the long value.
     */
    @XmlElement(name = "Integer", namespace = "http://www.isotc211.org/2005/gco")
    public Long getLong() {
        return value;
    }

    /**
     * Sets the new long value.
     */
    public void setLong(final Long value) {
        this.value = value;
    }

    /**
     * Allows JAXB to generate a Long object using the value found in the adapter.
     *
     * @param value The value extract from the adapter.
     * @return A Long object.
     */
    public Long unmarshal(final LongAdapter value) {
        if (value == null) {
            return null;
        }
        return value.value;
    }

    /**
     * Allows JAXB to change the result of the marshalling process, according to the
     * ISO-19139 standard and its requirements about primitive types.
     *
     * @param value The integer value we want to surround by a tags representing its type.
     * @return An adaptation of the integer value, that is to say a integer value surrounded
     *         by {@code <gco:Integer>} tags.
     */
    public LongAdapter marshal(final Long value) {
        if (value == null) {
            return null;
        }
        return new LongAdapter(value);
    }
}
