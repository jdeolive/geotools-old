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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;


/**
 * JAXB adapter in order to surround an URI with a {@code <gmd:URL>} tags,
 * respecting the ISO-13139 standard.
 *
 * @since 2.5
 * @source $URL$
 * @author Cédric Briançon
 */
public class URIAdapter extends XmlAdapter<URIAdapter, URI> {
    /**
     * The URI value.
     */
    private URI uri;

    /**
     * Empty constructor for JAXB only.
     */
    private URIAdapter() {
    }

    /**
     * Builds an adapter for {@link URI}.
     *
     * @param uri The URI to marshall.
     */
    protected URIAdapter(final URI uri) {
        this.uri = uri;
    }

    /**
     * Returns the URL matching with the metadata value. This method is systematically
     * called at marshalling-time by JAXB.
     */
    @XmlElement(name = "URL")
    public URL getUrl() {
        try {
            return uri.toURL();
        } catch (MalformedURLException ex) {
            return null;
        } catch (IllegalArgumentException ex) {
            // If the value is empty or wrong, the marshalling process will throw this
            // exception, when trying to create an URL. A null value will be returned
            // and the matching tag will not be written.
            return null;
        }
    }

    /**
     * Sets the value for the metadata URL. This method is systematically called at
     * unmarshalling-time by JAXB.
     */
    public void setUrl(final URL url) {
        try {
            this.uri = url.toURI();
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
    public URI unmarshal(final URIAdapter value) {
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
    public URIAdapter marshal(final URI value) {
        if (value == null) {
            return null;
        }
        return new URIAdapter(value);
    }
}
