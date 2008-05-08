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

import javax.xml.bind.annotation.XmlElement;
import org.geotools.metadata.iso.citation.ContactImpl;
import org.opengis.metadata.citation.Contact;


/**
 * JAXB adapter in order to map implementing class with the GeoAPI interface. See
 * package documentation for more information about JAXB and interface.
 *
 * @since 2.5
 * @source $URL$
 * @author Cédric Briançon
 */
public final class ContactAdapter extends MetadataAdapter<ContactAdapter,Contact> {
    /**
     * Empty constructor for JAXB only.
     */
    private ContactAdapter() {
    }

    /**
     * Wraps an Contact value with a {@code CI_Contact} tags at marshalling-time.
     *
     * @param metadata The metadata value to marshall.
     */
    protected ContactAdapter(final Contact metadata) {
        super(metadata);
    }

    /**
     * Returns the Contact value covered by a {@code CI_Contact} tags.
     *
     * @param value The value to marshall.
     * @return The adapter which covers the metadata value.
     */
    protected ContactAdapter wrap(final Contact value) {
        return new ContactAdapter(value);
    }

    /**
     * Returns the {@link ContactImpl} generated from the metadata value.
     * This method is systematically called at marshalling-time by JAXB.
     */
    @XmlElement(name = "CI_Contact")
    public ContactImpl getContact() {
        return (metadata instanceof ContactImpl) ?
            (ContactImpl) metadata : new ContactImpl(metadata);
    }

    /**
     * Sets the value for the {@link ContactImpl}. This method is systematically
     * called at unmarshalling-time by JAXB.
     */
    public void setContact(final ContactImpl contact) {
        this.metadata = contact;
    }
}
