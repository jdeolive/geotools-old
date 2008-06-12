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
import org.geotools.metadata.iso.extent.VerticalExtentImpl;
import org.opengis.metadata.extent.VerticalExtent;


/**
 * JAXB adapter in order to map implementing class with the GeoAPI interface. See
 * package documentation for more information about JAXB and interface.
 *
 * @since 2.5
 * @source $URL$
 * @author Cédric Briançon
 */
public final class VerticalExtentAdapter extends MetadataAdapter<VerticalExtentAdapter,VerticalExtent> {
    /**
     * Empty constructor for JAXB only.
     */
    private VerticalExtentAdapter() {
    }

    /**
     * Wraps an VerticalExtent value with a {@code EX_VerticalExtent} tags at marshalling-time.
     *
     * @param metadata The metadata value to marshall.
     */
    protected VerticalExtentAdapter(final VerticalExtent metadata) {
        super(metadata);
    }

    /**
     * Returns the VerticalExtent value covered by a {@code EX_VerticalExtent} tags.
     *
     * @param value The value to marshall.
     * @return The adapter which covers the metadata value.
     */
    protected VerticalExtentAdapter wrap(final VerticalExtent value) {
        return new VerticalExtentAdapter(value);
    }

    /**
     * Returns the {@link VerticalExtentImpl} generated from the metadata value.
     * This method is systematically called at marshalling-time by JAXB.
     */
    @XmlElement(name = "EX_VerticalExtent")
    public VerticalExtentImpl getVerticalExtent() {
        return (metadata instanceof VerticalExtentImpl) ?
            (VerticalExtentImpl) metadata : new VerticalExtentImpl(metadata);
    }

    /**
     * Sets the value for the {@link VerticalExtentImpl}. This method is systematically
     * called at unmarshalling-time by JAXB.
     */
    public void setVerticalExtent(final VerticalExtentImpl extent) {
        this.metadata = extent;
    }
}
