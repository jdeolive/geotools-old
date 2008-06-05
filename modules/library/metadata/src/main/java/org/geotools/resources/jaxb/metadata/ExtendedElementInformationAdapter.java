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
import org.geotools.metadata.iso.ExtendedElementInformationImpl;
import org.opengis.metadata.ExtendedElementInformation;


/**
 * JAXB adapter in order to map implementing class with the GeoAPI interface. See
 * package documentation for more information about JAXB and interface.
 *
 * @since 2.5
 * @source $URL$
 * @author Cédric Briançon
 */
public final class ExtendedElementInformationAdapter
        extends MetadataAdapter<ExtendedElementInformationAdapter,ExtendedElementInformation>
{
    /**
     * Empty constructor for JAXB only.
     */
    private ExtendedElementInformationAdapter() {
    }

    /**
     * Wraps an ExtendedElementInformation value with a {@code MD_ExtendedElementInformation}
     * tags at marshalling-time.
     *
     * @param metadata The metadata value to marshall.
     */
    protected ExtendedElementInformationAdapter(final ExtendedElementInformation metadata) {
        super(metadata);
    }

    /**
     * Returns the ExtendedElementInformation value covered by a
     * {@code MD_ExtendedElementInformation} tags.
     *
     * @param value The value to marshall.
     * @return The adapter which covers the metadata value.
     */
    protected ExtendedElementInformationAdapter wrap(final ExtendedElementInformation value) {
        return new ExtendedElementInformationAdapter(value);
    }

    /**
     * Returns the {@link ExtendedElementInformationImpl} generated from the metadata value.
     * This method is systematically called at marshalling-time by JAXB.
     */
    @XmlElement(name = "MD_ExtendedElementInformation")
    public ExtendedElementInformationImpl getExtendedElementInformation() {
        return (metadata instanceof ExtendedElementInformationImpl) ?
            (ExtendedElementInformationImpl) metadata : new ExtendedElementInformationImpl(metadata);
    }

    /**
     * Sets the value for the {@link ExtendedElementInformationImpl}. This method is systematically
     * called at unmarshalling-time by JAXB.
     */
    public void setExtendedElementInformation(final ExtendedElementInformationImpl info) {
        this.metadata = info;
    }
}
