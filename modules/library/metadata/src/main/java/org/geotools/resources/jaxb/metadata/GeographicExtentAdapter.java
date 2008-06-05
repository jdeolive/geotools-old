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

import javax.xml.bind.annotation.XmlElementRef;
import org.geotools.metadata.iso.extent.GeographicExtentImpl;
import org.opengis.metadata.extent.GeographicExtent;


/**
 * JAXB adapter in order to map implementing class with the GeoAPI interface. See
 * package documentation for more information about JAXB and interface.
 *
 * @since 2.5
 * @source $URL$
 * @author Cédric Briançon
 */
public final class GeographicExtentAdapter extends MetadataAdapter<GeographicExtentAdapter,GeographicExtent> {
    /**
     * Empty constructor for JAXB only.
     */
    private GeographicExtentAdapter() {
    }

    /**
     * Wraps an GeographicExtent value with a {@code EX_GeographicExtent} tags at marshalling-time.
     *
     * @param metadata The metadata value to marshall.
     */
    protected GeographicExtentAdapter(final GeographicExtent metadata) {
        super(metadata);
    }

    /**
     * Returns the GeographicExtent value covered by a {@code EX_GeographicExtent} tags.
     *
     * @param value The value to marshall.
     * @return The adapter which covers the metadata value.
     */
    protected GeographicExtentAdapter wrap(final GeographicExtent value) {
        return new GeographicExtentAdapter(value);
    }

    /**
     * Returns the {@link GeographicExtentImpl} generated from the metadata value.
     * This method is systematically called at marshalling-time by JAXB.
     */
    @XmlElementRef
    public GeographicExtentImpl getGeographicExtent() {
        return (metadata instanceof GeographicExtentImpl) ?
            (GeographicExtentImpl) metadata : new GeographicExtentImpl(metadata);
    }

    /**
     * Sets the value for the {@link GeographicExtentImpl}. This method is systematically
     * called at unmarshalling-time by JAXB.
     */
    public void setGeographicExtent(final GeographicExtentImpl extent) {
        this.metadata = extent;
    }
}
