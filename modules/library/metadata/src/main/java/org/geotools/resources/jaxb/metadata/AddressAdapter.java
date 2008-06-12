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
import org.geotools.metadata.iso.citation.AddressImpl;
import org.opengis.metadata.citation.Address;


/**
 * JAXB adapter in order to map implementing class with the GeoAPI interface. See
 * package documentation for more information about JAXB and interface.
 *
 * @since 2.5
 * @source $URL$
 * @author Cédric Briançon
 */
public final class AddressAdapter extends MetadataAdapter<AddressAdapter,Address> {
    /**
     * Empty constructor for JAXB only.
     */
    private AddressAdapter() {
    }

    /**
     * Wraps an address value with a {@code CI_Address} tags at marshalling-time.
     *
     * @param metadata The metadata value to marshall.
     */
    protected AddressAdapter(final Address metadata) {
        super(metadata);
    }

    /**
     * Returns the address value covered by a {@code CI_Address} tags.
     *
     * @param value The value to marshall.
     * @return The adapter which covers the metadata value.
     */
    protected AddressAdapter wrap(final Address value) {
        return new AddressAdapter(value);
    }

    /**
     * Returns the {@link AddressImpl} generated from the metadata value.
     * This method is systematically called at marshalling-time by JAXB.
     */
    @XmlElement(name = "CI_Address")
    public AddressImpl getAddress() {
        return (metadata instanceof AddressImpl) ?
            (AddressImpl) metadata : new AddressImpl(metadata);
    }

    /**
     * Sets the value for the {@link AddressImpl}. This method is systematically
     * called at unmarshalling-time by JAXB.
     */
    public void setAddress(final AddressImpl address) {
        this.metadata = address;
    }
}
