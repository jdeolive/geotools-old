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
import org.geotools.metadata.iso.identification.KeywordsImpl;
import org.opengis.metadata.identification.Keywords;


/**
 * JAXB adapter in order to map implementing class with the GeoAPI interface. See
 * package documentation for more information about JAXB and interface.
 *
 * @since 2.5
 * @source $URL$
 * @author Cédric Briançon
 */
public final class KeywordsAdapter extends MetadataAdapter<KeywordsAdapter,Keywords> {
    /**
     * Empty constructor for JAXB only.
     */
    private KeywordsAdapter() {
    }

    /**
     * Wraps an Keywords value with a {@code MD_Keywords} tags at marshalling-time.
     *
     * @param metadata The metadata value to marshall.
     */
    protected KeywordsAdapter(final Keywords metadata) {
        super(metadata);
    }

    /**
     * Returns the Keywords value covered by a {@code MD_Keywords} tags.
     *
     * @param value The value to marshall.
     * @return The adapter which covers the metadata value.
     */
    protected KeywordsAdapter wrap(final Keywords value) {
        return new KeywordsAdapter(value);
    }

    /**
     * Returns the {@link KeywordsImpl} generated from the metadata value.
     * This method is systematically called at marshalling-time by JAXB.
     */
    @XmlElement(name = "MD_Keywords")
    public KeywordsImpl getKeywords() {
        return (metadata instanceof KeywordsImpl) ?
            (KeywordsImpl) metadata : new KeywordsImpl(metadata);
    }

    /**
     * Sets the value for the {@link KeywordsImpl}. This method is systematically
     * called at unmarshalling-time by JAXB.
     */
    public void setKeywords(final KeywordsImpl keywords) {
        this.metadata = keywords;
    }
}
