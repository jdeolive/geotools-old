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

import javax.xml.bind.annotation.XmlElementRef;
import org.geotools.metadata.iso.identification.IdentificationImpl;
import org.opengis.metadata.identification.Identification;


/**
 * JAXB adapter in order to map implementing class with the GeoAPI interface. See
 * package documentation for more information about JAXB and interface.
 *
 * @since 2.5
 * @source $URL$
 * @author Cédric Briançon
 */
public final class IdentificationAdapter extends MetadataAdapter<IdentificationAdapter,Identification> {
    /**
     * Empty constructor for JAXB only.
     */
    private IdentificationAdapter() {
    }

    /**
     * Wraps an Identification value with a {@code MD_Identification} tags at marshalling-time.
     *
     * @param metadata The metadata value to marshall.
     */
    protected IdentificationAdapter(final Identification metadata) {
        super(metadata);
    }

    /**
     * Returns the Identification value covered by a {@code MD_Identification} tags.
     *
     * @param value The value to marshall.
     * @return The adapter which covers the metadata value.
     */
    protected IdentificationAdapter wrap(final Identification value) {
        return new IdentificationAdapter(value);
    }

    /**
     * Returns the {@link IdentificationImpl} generated from the metadata value.
     * This method is systematically called at marshalling-time by JAXB.
     */
    @XmlElementRef
    public IdentificationImpl getIdentification() {
        return (metadata instanceof IdentificationImpl) ?
            (IdentificationImpl) metadata : new IdentificationImpl(metadata);
    }

    /**
     * Sets the value for the {@link IdentificationImpl}. This method is systematically
     * called at unmarshalling-time by JAXB.
     */
    public void setIdentification(final IdentificationImpl ident) {
        this.metadata = ident;
    }
}
