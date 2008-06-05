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

import java.net.URI;
import java.net.URISyntaxException;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;


/**
 * JAXB adapter in order to surround an URI value with a 
 * {@code <gco:CharacterString>} tags, respecting the ISO-13139 standard.
 *
 * @since 2.5
 * @source $URL$
 * @author Cédric Briançon
 */
public class URINameAdapter extends XmlAdapter<URINameAdapter, URI> {
    /**
     * The URI value.
     */
    private URI uri;

    /**
     * Empty constructor for JAXB only.
     */
    private URINameAdapter() {
    }

    /**
     * Builds an adapter for {@link URI}.
     *
     * @param URI The URI to marshall.
     */
    protected URINameAdapter(final URI uri) {
        this.uri = uri;
    }

    /**
     * Returns the URI as a string matching with the metadata value. This method is systematically
     * called at marshalling-time by JAXB.
     */
    @XmlElement(name = "CharacterString", namespace = "http://www.isotc211.org/2005/gco")
    public String getName() {
        return uri.toString();
    }

    /**
     * Sets the value for the metadata date. This method is systematically called at
     * unmarshalling-time by JAXB.
     */
    public void setUrl(final String name) {
        try {
            this.uri = new URI(name);
        } catch (URISyntaxException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Does the link between an URI red from an XML stream and the object which will
     * contains this value. JAXB calls automatically this method at unmarshalling-time.
     *
     * @param value The adapter for this metadata value.
     * @return An {@link URI} which represents the metadata value.
     */
    public URI unmarshal(final URINameAdapter value) {
        if (value == null) {
            return null;
        }
        return value.uri;
    }

    /**
     * Does the link between {@link URI} and the way they will be marshalled into
     * an XML file or stream. JAXB calls automatically this method at marshalling-time.
     *
     * @param value The URI value.
     * @return The adapter for this URI.
     */
    public URINameAdapter marshal(final URI value) {
        if (value == null) {
            return null;
        }
        return new URINameAdapter(value);
    }
}
