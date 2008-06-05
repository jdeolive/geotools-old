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
import org.geotools.metadata.iso.content.RangeDimensionImpl;
import org.opengis.metadata.content.RangeDimension;


/**
 * JAXB adapter in order to map implementing class with the GeoAPI interface. See
 * package documentation for more information about JAXB and interface.
 *
 * @since 2.5
 * @source $URL$
 * @author Cédric Briançon
 */
public final class RangeDimensionAdapter 
        extends MetadataAdapter<RangeDimensionAdapter,RangeDimension> 
{
    /**
     * Empty constructor for JAXB only.
     */
    private RangeDimensionAdapter() {
    }

    /**
     * Wraps an RangeDimension value with a {@code MD_RangeDimension} tags at marshalling-time.
     *
     * @param metadata The metadata value to marshall.
     */
    protected RangeDimensionAdapter(final RangeDimension metadata) {
        super(metadata);
    }

    /**
     * Returns the RangeDimension value covered by a {@code MD_RangeDimension} tags.
     *
     * @param value The value to marshall.
     * @return The adapter which covers the metadata value.
     */
    protected RangeDimensionAdapter wrap(final RangeDimension value) {
        return new RangeDimensionAdapter(value);
    }

    /**
     * Returns the {@link RangeDimensionImpl} generated from the metadata value.
     * This method is systematically called at marshalling-time by JAXB.
     */
    @XmlElementRef
    public RangeDimensionImpl getRangeDimension() {
        return (metadata instanceof RangeDimensionImpl) ?
            (RangeDimensionImpl) metadata : new RangeDimensionImpl(metadata);
    }

    /**
     * Sets the value for the {@link RangeDimensionImpl}. This method is systematically
     * called at unmarshalling-time by JAXB.
     */
    public void setRangeDimension(final RangeDimensionImpl range) {
        this.metadata = range;
    }
}
