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
import org.geotools.metadata.iso.identification.UsageImpl;
import org.opengis.metadata.identification.Usage;


/**
 * JAXB adapter in order to map implementing class with the GeoAPI interface. See
 * package documentation for more information about JAXB and interface.
 *
 * @since 2.5
 * @source $URL$
 * @author Cédric Briançon
 */
public final class UsageAdapter extends MetadataAdapter<UsageAdapter,Usage> {
    /**
     * Empty constructor for JAXB only.
     */
    private UsageAdapter() {
    }

    /**
     * Wraps an Usage value with a {@code MD_Usage} tags at marshalling-time.
     *
     * @param metadata The metadata value to marshall.
     */
    protected UsageAdapter(final Usage metadata) {
        super(metadata);
    }

    /**
     * Returns the Usage value covered by a {@code MD_Usage} tags.
     *
     * @param value The value to marshall.
     * @return The adapter which covers the metadata value.
     */
    protected UsageAdapter wrap(final Usage value) {
        return new UsageAdapter(value);
    }

    /**
     * Returns the {@link UsageImpl} generated from the metadata value.
     * This method is systematically called at marshalling-time by JAXB.
     */
    @XmlElement(name = "MD_Usage")
    public UsageImpl getUsage() {
        return (metadata instanceof UsageImpl) ?
            (UsageImpl) metadata : new UsageImpl(metadata);
    }

    /**
     * Sets the value for the {@link UsageImpl}. This method is systematically
     * called at unmarshalling-time by JAXB.
     */
    public void setUsage(final UsageImpl usage) {
        this.metadata = usage;
    }
}
