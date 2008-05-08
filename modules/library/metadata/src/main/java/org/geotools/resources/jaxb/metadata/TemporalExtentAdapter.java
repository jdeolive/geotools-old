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

import javax.xml.bind.annotation.XmlElementRef;
import org.geotools.metadata.iso.extent.TemporalExtentImpl;
import org.opengis.metadata.extent.TemporalExtent;


/**
 * JAXB adapter in order to map implementing class with the GeoAPI interface. See
 * package documentation for more information about JAXB and interface.
 *
 * @since 2.5
 * @source $URL$
 * @author Cédric Briançon
 */
public final class TemporalExtentAdapter extends MetadataAdapter<TemporalExtentAdapter,TemporalExtent> {
    /**
     * Empty constructor for JAXB only.
     */
    private TemporalExtentAdapter() {
    }

    /**
     * Wraps an TemporalExtent value with a {@code EX_TemporalExtent} tags at marshalling-time.
     *
     * @param metadata The metadata value to marshall.
     */
    protected TemporalExtentAdapter(final TemporalExtent metadata) {
        super(metadata);
    }

    /**
     * Returns the TemporalExtent value covered by a {@code EX_TemporalExtent} tags.
     *
     * @param value The value to marshall.
     * @return The adapter which covers the metadata value.
     */
    protected TemporalExtentAdapter wrap(final TemporalExtent value) {
        return new TemporalExtentAdapter(value);
    }

    /**
     * Returns the {@link TemporalExtentImpl} generated from the metadata value.
     * This method is systematically called at marshalling-time by JAXB.
     */
    @XmlElementRef
    public TemporalExtentImpl getTemporalExtent() {
        return (metadata instanceof TemporalExtentImpl) ?
            (TemporalExtentImpl) metadata : new TemporalExtentImpl(metadata);
    }

    /**
     * Sets the value for the {@link TemporalExtentImpl}. This method is systematically
     * called at unmarshalling-time by JAXB.
     */
    public void setTemporalExtent(final TemporalExtentImpl extent) {
        this.metadata = extent;
    }
}
