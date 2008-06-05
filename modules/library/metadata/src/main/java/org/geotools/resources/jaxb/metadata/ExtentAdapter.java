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
import org.geotools.metadata.iso.extent.ExtentImpl;
import org.opengis.metadata.extent.Extent;


/**
 * JAXB adapter in order to map implementing class with the GeoAPI interface. See
 * package documentation for more information about JAXB and interface.
 *
 * @since 2.5
 * @source $URL$
 * @author Cédric Briançon
 */
public final class ExtentAdapter extends MetadataAdapter<ExtentAdapter,Extent> {
    /**
     * Empty constructor for JAXB only.
     */
    private ExtentAdapter() {
    }

    /**
     * Wraps an Extent value with a {@code EX_Extent} tags at marshalling-time.
     *
     * @param metadata The metadata value to marshall.
     */
    protected ExtentAdapter(final Extent metadata) {
        super(metadata);
    }

    /**
     * Returns the Extent value covered by a {@code EX_Extent} tags.
     *
     * @param value The value to marshall.
     * @return The adapter which covers the metadata value.
     */
    protected ExtentAdapter wrap(final Extent value) {
        return new ExtentAdapter(value);
    }

    /**
     * Returns the {@link ExtentImpl} generated from the metadata value.
     * This method is systematically called at marshalling-time by JAXB.
     */
    @XmlElement(name = "EX_Extent")
    public ExtentImpl getExtent() {
        return (metadata instanceof ExtentImpl) ?
            (ExtentImpl) metadata : new ExtentImpl(metadata);
    }

    /**
     * Sets the value for the {@link ExtentImpl}. This method is systematically
     * called at unmarshalling-time by JAXB.
     */
    public void setExtent(final ExtentImpl extent) {
        this.metadata = extent;
    }
}
