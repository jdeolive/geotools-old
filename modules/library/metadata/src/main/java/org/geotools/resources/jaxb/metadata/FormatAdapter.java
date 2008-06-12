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
import org.geotools.metadata.iso.distribution.FormatImpl;
import org.opengis.metadata.distribution.Format;


/**
 * JAXB adapter in order to map implementing class with the GeoAPI interface. See
 * package documentation for more information about JAXB and interface.
 *
 * @since 2.5
 * @source $URL$
 * @author Cédric Briançon
 */
public final class FormatAdapter extends MetadataAdapter<FormatAdapter,Format> {
    /**
     * Empty constructor for JAXB only.
     */
    private FormatAdapter() {
    }

    /**
     * Wraps an Format value with a {@code MD_Format} tags at marshalling-time.
     *
     * @param metadata The metadata value to marshall.
     */
    protected FormatAdapter(final Format metadata) {
        super(metadata);
    }

    /**
     * Returns the Format value covered by a {@code MD_Format} tags.
     *
     * @param value The value to marshall.
     * @return The adapter which covers the metadata value.
     */
    protected FormatAdapter wrap(final Format value) {
        return new FormatAdapter(value);
    }

    /**
     * Returns the {@link FormatImpl} generated from the metadata value.
     * This method is systematically called at marshalling-time by JAXB.
     */
    @XmlElement(name = "MD_Format")
    public FormatImpl getFormat() {
        return (metadata instanceof FormatImpl) ?
            (FormatImpl) metadata : new FormatImpl(metadata);
    }

    /**
     * Sets the value for the {@link FormatImpl}. This method is systematically
     * called at unmarshalling-time by JAXB.
     */
    public void setFormat(final FormatImpl format) {
        this.metadata = format;
    }
}
