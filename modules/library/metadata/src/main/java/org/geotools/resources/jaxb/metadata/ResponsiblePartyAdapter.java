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
import org.geotools.metadata.iso.citation.ResponsiblePartyImpl;
import org.opengis.metadata.citation.ResponsibleParty;


/**
 * JAXB adapter in order to map implementing class with the GeoAPI interface. See
 * package documentation for more information about JAXB and interface.
 *
 * @since 2.5
 * @source $URL$
 * @author Cédric Briançon
 */
public final class ResponsiblePartyAdapter extends MetadataAdapter<ResponsiblePartyAdapter,ResponsibleParty> {
    /**
     * Empty constructor for JAXB only.
     */
    private ResponsiblePartyAdapter() {
    }

    /**
     * Wraps an ResponsibleParty value with a {@code CI_ResponsibleParty} tags at marshalling-time.
     *
     * @param metadata The metadata value to marshall.
     */
    protected ResponsiblePartyAdapter(final ResponsibleParty metadata) {
        super(metadata);
    }

    /**
     * Returns the ResponsibleParty value covered by a {@code CI_ResponsibleParty} tags.
     *
     * @param value The value to marshall.
     * @return The adapter which covers the metadata value.
     */
    protected ResponsiblePartyAdapter wrap(final ResponsibleParty value) {
        return new ResponsiblePartyAdapter(value);
    }

    /**
     * Returns the {@link ResponsiblePartyImpl} generated from the metadata value.
     * This method is systematically called at marshalling-time by JAXB.
     */
    @XmlElement(name = "CI_ResponsibleParty")
    public ResponsiblePartyImpl getResponsibleParty() {
        return (metadata instanceof ResponsiblePartyImpl) ?
            (ResponsiblePartyImpl) metadata : new ResponsiblePartyImpl(metadata);
    }

    /**
     * Sets the value for the {@link ResponsiblePartyImpl}. This method is systematically
     * called at unmarshalling-time by JAXB.
     */
    public void setResponsibleParty(final ResponsiblePartyImpl responsible) {
        this.metadata = responsible;
    }
}
