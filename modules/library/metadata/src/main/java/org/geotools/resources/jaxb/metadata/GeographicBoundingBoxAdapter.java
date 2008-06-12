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
import org.geotools.metadata.iso.extent.GeographicBoundingBoxImpl;
import org.opengis.metadata.extent.GeographicBoundingBox;


/**
 * JAXB adapter in order to map implementing class with the GeoAPI interface. See
 * package documentation for more information about JAXB and interface.
 *
 * @since 2.5
 * @source $URL$
 * @author Cédric Briançon
 */
public final class GeographicBoundingBoxAdapter extends MetadataAdapter<GeographicBoundingBoxAdapter,GeographicBoundingBox> {
    /**
     * Empty constructor for JAXB only.
     */
    private GeographicBoundingBoxAdapter() {
    }

    /**
     * Wraps an GeographicBoundingBox value with a {@code EX_GeographicBoundingBox} tags at marshalling-time.
     *
     * @param metadata The metadata value to marshall.
     */
    protected GeographicBoundingBoxAdapter(final GeographicBoundingBox metadata) {
        super(metadata);
    }

    /**
     * Returns the GeographicBoundingBox value covered by a {@code EX_GeographicBoundingBox} tags.
     *
     * @param value The value to marshall.
     * @return The adapter which covers the metadata value.
     */
    protected GeographicBoundingBoxAdapter wrap(final GeographicBoundingBox value) {
        return new GeographicBoundingBoxAdapter(value);
    }

    /**
     * Returns the {@link GeographicBoundingBoxImpl} generated from the metadata value.
     * This method is systematically called at marshalling-time by JAXB.
     */
    @XmlElement(name = "EX_GeographicBoundingBox")
    public GeographicBoundingBoxImpl getGeographicBoundingBox() {
        return (metadata instanceof GeographicBoundingBoxImpl) ?
            (GeographicBoundingBoxImpl) metadata : new GeographicBoundingBoxImpl(metadata);
    }

    /**
     * Sets the value for the {@link GeographicBoundingBoxImpl}. This method is systematically
     * called at unmarshalling-time by JAXB.
     */
    public void setGeographicBoundingBox(final GeographicBoundingBoxImpl extent) {
        this.metadata = extent;
    }
}
