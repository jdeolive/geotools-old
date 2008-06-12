/*
 *    GeoTools - The Open Source Java GIS Tookit
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
package org.geotools.resources.jaxb.metadata;

import javax.xml.bind.annotation.XmlElement;
import org.geotools.metadata.iso.citation.CitationImpl;
import org.opengis.metadata.citation.Citation;


/**
 * JAXB adapter in order to map implementing class with the GeoAPI interface. See
 * package documentation for more information about JAXB and interface.
 *
 * @since 2.5
 * @source $URL$
 * @author Cédric Briançon
 */
public final class CitationAdapter extends MetadataAdapter<CitationAdapter,Citation> {
    /**
     * Empty constructor for JAXB only.
     */
    private CitationAdapter() {
    }

    /**
     * Wraps an Citation value with a {@code CI_Citation} tags at marshalling-time.
     *
     * @param metadata The metadata value to marshall.
     */
    protected CitationAdapter(final Citation metadata) {
        super(metadata);
    }

    /**
     * Returns the Citation value covered by a {@code CI_Citation} tags.
     *
     * @param value The value to marshall.
     * @return The adapter which covers the metadata value.
     */
    protected CitationAdapter wrap(final Citation value) {
        return new CitationAdapter(value);
    }

    /**
     * Returns the {@link CitationImpl} generated from the metadata value.
     * This method is systematically called at marshalling-time by JAXB.
     */
    @XmlElement(name = "CI_Citation")
    public CitationImpl getCitation() {
        return (metadata instanceof CitationImpl) ?
            (CitationImpl) metadata : new CitationImpl(metadata);
    }

    /**
     * Sets the value for the {@link CitationImpl}. This method is systematically
     * called at unmarshalling-time by JAXB.
     */
    public void setCitation(final CitationImpl citation) {
        this.metadata = citation;
    }
}
