/*
 *    GeoTools - The Open Source Java GIS Tookit
 *    http://geotools.org
 * 
 *    (C) 2008, Open Source Geospatial Foundation (OSGeo)
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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;


/**
 * JAXB adapter in order to surround the string value with a {@code <gco:CharacterString>}
 * tags, respecting the ISO-13139 standard.
 *
 * @source $URL$
 * @author Cédric Briançon
 */
public class StringAdapter extends XmlAdapter<StringAdapter, String> {
    /**
     * The text value.
     */
    private String text;

    /**
     * Empty constructor for JAXB only.
     */
    private StringAdapter() {
    }

    /**
     * Builds an adapter for {@link String}.
     *
     * @param text The string to marshall.
     */
    protected StringAdapter(final String text) {
        this.text = text;
    }

    /**
     * Returns the string matching with the metadata value. This method is systematically
     * called at marshalling-time by JAXB.
     */
    @XmlElement(name = "CharacterString", namespace = "http://www.isotc211.org/2005/gco")
    public String getCharacterString() {
        return text;
    }

    /**
     * Sets the value for the metadata string. This method is systematically called at
     * unmarshalling-time by JAXB.
     */
    public void setCharacterString(final String text) {
        this.text = text;
    }

    /**
     * Does the link between a string red from an XML stream and the object which will
     * contains this value. JAXB calls automatically this method at unmarshalling-time.
     *
     * @param value The adapter for this metadata value.
     * @return A {@link String} which represents the metadata value.
     */
    public String unmarshal(final StringAdapter value) {
        if (value == null) {
            return null;
        }
        return value.text;
    }

    /**
     * Does the link between {@linkplain String strings} and the way they will be marshalled
     * into an XML file or stream. JAXB calls automatically this method at marshalling-time.
     *
     * @param value The string value.
     * @return The adapter for this string.
     */
    public StringAdapter marshal(final String value) {
        if (value == null) {
            return null;
        }
        return new StringAdapter(value);
    }
}
