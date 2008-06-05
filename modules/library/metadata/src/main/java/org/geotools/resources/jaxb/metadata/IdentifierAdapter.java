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
import org.geotools.metadata.iso.IdentifierImpl;
import org.opengis.metadata.Identifier;


/**
 * JAXB adapter in order to map implementing class with the GeoAPI interface. See
 * package documentation for more information about JAXB and interface.
 *
 * @since 2.5
 * @source $URL$
 * @author Cédric Briançon
 */
public final class IdentifierAdapter extends MetadataAdapter<IdentifierAdapter,Identifier> {
    /**
     * Empty constructor for JAXB only.
     */
    private IdentifierAdapter() {
    }

    /**
     * Wraps an Identifier value with a {@code MD_Identifier} tags at marshalling-time.
     *
     * @param metadata The metadata value to marshall.
     */
    protected IdentifierAdapter(final Identifier metadata) {
        super(metadata);
    }

    /**
     * Returns the Identifier value covered by a {@code MD_Identifier} tags.
     *
     * @param value The value to marshall.
     * @return The adapter which covers the metadata value.
     */
    protected IdentifierAdapter wrap(final Identifier value) {
        return new IdentifierAdapter(value);
    }

    /**
     * Returns the {@link IdentifierImpl} generated from the metadata value.
     * This method is systematically called at marshalling-time by JAXB.
     */
    @XmlElement(name = "MD_Identifier")
    public IdentifierImpl getIdentifier() {
        return (metadata instanceof IdentifierImpl) ?
            (IdentifierImpl) metadata : new IdentifierImpl(metadata);
    }

    /**
     * Sets the value for the {@link IdentifierImpl}. This method is systematically
     * called at unmarshalling-time by JAXB.
     */
    public void setIdentifier(final IdentifierImpl identifier) {
        this.metadata = identifier;
    }
}
