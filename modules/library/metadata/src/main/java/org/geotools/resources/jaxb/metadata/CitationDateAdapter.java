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
package org.geotools.resources.jaxb.metadata;

import javax.xml.bind.annotation.XmlElement;
import org.geotools.metadata.iso.citation.CitationDateImpl;
import org.opengis.metadata.citation.CitationDate;


/**
 * JAXB adapter in order to map implementing class with the GeoAPI interface. See
 * package documentation for more information about JAXB and interface.
 *
 * @since 2.5
 * @source $URL$
 * @author Cédric Briançon
 */
public final class CitationDateAdapter extends MetadataAdapter<CitationDateAdapter,CitationDate> {
    /**
     * Empty constructor for JAXB only.
     */
    private CitationDateAdapter() {
    }

    /**
     * Wraps an CitationDate value with a {@code CI_Date} tags at marshalling-time.
     *
     * @param metadata The metadata value to marshall.
     */
    protected CitationDateAdapter(final CitationDate metadata) {
        super(metadata);
    }

    /**
     * Returns the CitationDate value covered by a {@code CI_Date} tags.
     *
     * @param value The value to marshall.
     * @return The adapter which covers the metadata value.
     */
    protected CitationDateAdapter wrap(final CitationDate value) {
        return new CitationDateAdapter(value);
    }

    /**
     * Returns the {@link CitationDateImpl} generated from the metadata value.
     * This method is systematically called at marshalling-time by JAXB.
     */
    @XmlElement(name = "CI_Date")
    public CitationDateImpl getCitationDate() {
        return (metadata instanceof CitationDateImpl) ?
            (CitationDateImpl) metadata : new CitationDateImpl(metadata);
    }

    /**
     * Sets the value for the {@link CitationDateImpl}. This method is systematically
     * called at unmarshalling-time by JAXB.
     */
    public void setCitationDate(final CitationDateImpl citationDate) {
        this.metadata = citationDate;
    }
}
