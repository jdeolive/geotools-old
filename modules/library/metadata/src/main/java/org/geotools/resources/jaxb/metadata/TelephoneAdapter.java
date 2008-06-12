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
import org.geotools.metadata.iso.citation.TelephoneImpl;
import org.opengis.metadata.citation.Telephone;


/**
 * JAXB adapter in order to map implementing class with the GeoAPI interface. See
 * package documentation for more information about JAXB and interface.
 *
 * @since 2.5
 * @source $URL$
 * @author Cédric Briançon
 */
public final class TelephoneAdapter extends MetadataAdapter<TelephoneAdapter,Telephone> {
    /**
     * Empty constructor for JAXB only.
     */
    private TelephoneAdapter() {
    }

    /**
     * Wraps an Telephone value with a {@code CI_Telephone} tags at marshalling-time.
     *
     * @param metadata The metadata value to marshall.
     */
    protected TelephoneAdapter(final Telephone metadata) {
        super(metadata);
    }

    /**
     * Returns the Telephone value covered by a {@code CI_Telephone} tags.
     *
     * @param value The value to marshall.
     * @return The adapter which covers the metadata value.
     */
    protected TelephoneAdapter wrap(final Telephone value) {
        return new TelephoneAdapter(value);
    }

    /**
     * Returns the {@link TelephoneImpl} generated from the metadata value.
     * This method is systematically called at marshalling-time by JAXB.
     */
    @XmlElement(name = "CI_Telephone")
    public TelephoneImpl getTelephone() {
        return (metadata instanceof TelephoneImpl) ?
            (TelephoneImpl) metadata : new TelephoneImpl(metadata);
    }

    /**
     * Sets the value for the {@link TelephoneImpl}. This method is systematically
     * called at unmarshalling-time by JAXB.
     */
    public void setTelephone(final TelephoneImpl telephone) {
        this.metadata = telephone;
    }
}
