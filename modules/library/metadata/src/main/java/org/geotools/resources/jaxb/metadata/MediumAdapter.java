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
import org.geotools.metadata.iso.distribution.MediumImpl;
import org.opengis.metadata.distribution.Medium;


/**
 * JAXB adapter in order to map implementing class with the GeoAPI interface. See
 * package documentation for more information about JAXB and interface.
 *
 * @since 2.5
 * @source $URL$
 * @author Cédric Briançon
 */
public final class MediumAdapter extends MetadataAdapter<MediumAdapter,Medium> {
    /**
     * Empty constructor for JAXB only.
     */
    private MediumAdapter() {
    }

    /**
     * Wraps an Medium value with a {@code MD_Medium} tags at marshalling-time.
     *
     * @param metadata The metadata value to marshall.
     */
    protected MediumAdapter(final Medium metadata) {
        super(metadata);
    }

    /**
     * Returns the Medium value covered by a {@code MD_Medium} tags.
     *
     * @param value The value to marshall.
     * @return The adapter which covers the metadata value.
     */
    protected MediumAdapter wrap(final Medium value) {
        return new MediumAdapter(value);
    }

    /**
     * Returns the {@link MediumImpl} generated from the metadata value.
     * This method is systematically called at marshalling-time by JAXB.
     */
    @XmlElement(name = "MD_Medium")
    public MediumImpl getMedium() {
        return (metadata instanceof MediumImpl) ?
            (MediumImpl) metadata : new MediumImpl(metadata);
    }

    /**
     * Sets the value for the {@link MediumImpl}. This method is systematically
     * called at unmarshalling-time by JAXB.
     */
    public void setMedium(final MediumImpl medium) {
        this.metadata = medium;
    }
}
