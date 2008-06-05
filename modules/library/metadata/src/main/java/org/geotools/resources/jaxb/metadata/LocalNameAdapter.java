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

import javax.xml.bind.annotation.adapters.XmlAdapter;
import org.geotools.util.LocalName;


/**
 * JAXB adapter in order to map implementing class with the GeoAPI interface. See
 * package documentation for more information about JAXB and interface.
 *
 * @since 2.5
 * @source $URL$
 * @author Cédric Briançon
 */
public final class LocalNameAdapter extends XmlAdapter<String,LocalName>
{
    /**
     * Does the link between {@linkplain String strings} and the way they will be unmarshalled.
     * JAXB calls automatically this method at unmarshalling-time.
     *
     * @param value The string value.
     * @return The implementing class for this string.
     */
    public LocalName unmarshal(final String value) {
        return (value == null) ? null : new LocalName(value);
    }

    /**
     * Does the link between a {@link LocalName} and the string associated. JAXB calls
     * automatically this method at marshalling-time.
     *
     * @param value The implementing class for this metadata value.
     * @return A {@link String} which represents the metadata value.
     */
    public String marshal(final LocalName value) {
        return (value == null) ? null : value.toInternationalString().toString();
    }
}
