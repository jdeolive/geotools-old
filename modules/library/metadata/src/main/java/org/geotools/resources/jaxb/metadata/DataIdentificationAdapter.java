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
import org.geotools.metadata.iso.identification.DataIdentificationImpl;
import org.opengis.metadata.identification.DataIdentification;


/**
 * JAXB adapter in order to map implementing class with the GeoAPI interface. See
 * package documentation for more information about JAXB and interface.
 *
 * @since 2.5
 * @source $URL$
 * @author Cédric Briançon
 */
public final class DataIdentificationAdapter extends MetadataAdapter<DataIdentificationAdapter,DataIdentification> {
    /**
     * Empty constructor for JAXB only.
     */
    private DataIdentificationAdapter() {
    }

    /**
     * Wraps an DataIdentification value with a {@code MD_DataIdentification} tags at marshalling-time.
     *
     * @param metadata The metadata value to marshall.
     */
    protected DataIdentificationAdapter(final DataIdentification metadata) {
        super(metadata);
    }

    /**
     * Returns the DataIdentification value covered by a {@code MD_DataIdentification} tags.
     *
     * @param value The value to marshall.
     * @return The adapter which covers the metadata value.
     */
    protected DataIdentificationAdapter wrap(final DataIdentification value) {
        return new DataIdentificationAdapter(value);
    }

    /**
     * Returns the {@link DataIdentificationImpl} generated from the metadata value.
     * This method is systematically called at marshalling-time by JAXB.
     */
    @XmlElement(name = "MD_DataIdentification")
    public DataIdentificationImpl getIdentification() {
        return (metadata instanceof DataIdentificationImpl) ?
            (DataIdentificationImpl) metadata : new DataIdentificationImpl(metadata);
    }

    /**
     * Sets the value for the {@link DataIdentificationImpl}. This method is systematically
     * called at unmarshalling-time by JAXB.
     */
    public void setIdentification(final DataIdentificationImpl ident) {
        this.metadata = ident;
    }
}
