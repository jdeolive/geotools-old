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
import org.geotools.util.RecordTypeImpl;
import org.opengis.util.RecordType;


/**
 * JAXB adapter in order to map implementing class with the GeoAPI interface. See
 * package documentation for more information about JAXB and interface.
 *
 * @since 2.5
 * @source $URL$
 * @author Cédric Briançon
 */
public final class RecordTypeAdapter extends MetadataAdapter<RecordTypeAdapter,RecordType> {
    /**
     * Empty constructor for JAXB only.
     */
    private RecordTypeAdapter() {
    }

    /**
     * Wraps an RecordType value with a {@code RecordType} tags at marshalling-time.
     *
     * @param metadata The metadata value to marshall.
     */
    protected RecordTypeAdapter(final RecordType metadata) {
        super(metadata);
    }

    /**
     * Returns the RecordType value covered by a {@code RecordType} tags.
     *
     * @param value The value to marshall.
     * @return The adapter which covers the metadata value.
     */
    protected RecordTypeAdapter wrap(final RecordType value) {
        return new RecordTypeAdapter(value);
    }

    /**
     * Returns the {@link RecordTypeImpl} generated from the metadata value.
     * This method is systematically called at marshalling-time by JAXB.
     */
    @XmlElement(name = "RecordType")
    public RecordTypeImpl getRecordType() {
        if (metadata instanceof RecordTypeImpl) {
            return (RecordTypeImpl) metadata;
        } else {
            return new RecordTypeImpl(metadata.getContainer(), metadata.getTypeName(), metadata.getMembers());
        }
    }

    /**
     * Sets the value for the {@link RecordTypeImpl}. This method is systematically
     * called at unmarshalling-time by JAXB.
     */
    public void setRecordType(final RecordTypeImpl recordType) {
        this.metadata = recordType;
    }
}
