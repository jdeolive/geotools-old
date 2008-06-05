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
import org.geotools.metadata.iso.identification.RepresentativeFractionImpl;
import org.opengis.metadata.identification.RepresentativeFraction;


/**
 * JAXB adapter in order to map implementing class with the GeoAPI interface. See
 * package documentation for more information about JAXB and interface.
 *
 * @since 2.5
 * @source $URL$
 * @author Cédric Briançon
 */
public final class RepresentativeFractionAdapter
        extends MetadataAdapter<RepresentativeFractionAdapter,RepresentativeFraction>
{
    /**
     * Empty constructor for JAXB only.
     */
    private RepresentativeFractionAdapter() {
    }

    /**
     * Wraps an RepresentativeFraction value with a {@code MD_RepresentativeFraction}
     * tags at marshalling-time.
     *
     * @param metadata The metadata value to marshall.
     */
    protected RepresentativeFractionAdapter(final RepresentativeFraction metadata) {
        super(metadata);
    }

    /**
     * Returns the RepresentativeFraction value covered by a
     * {@code MD_RepresentativeFraction} tags.
     *
     * @param value The value to marshall.
     * @return The adapter which covers the metadata value.
     */
    protected RepresentativeFractionAdapter wrap(final RepresentativeFraction value) {
        return new RepresentativeFractionAdapter(value);
    }

    /**
     * Returns the {@link RepresentativeFractionImpl} generated from the metadata value.
     * This method is systematically called at marshalling-time by JAXB.
     */
    @XmlElement(name = "MD_RepresentativeFraction")
    public RepresentativeFractionImpl getRepresentativeFraction() {
        return (metadata instanceof RepresentativeFractionImpl) ?
            (RepresentativeFractionImpl) metadata : 
            new RepresentativeFractionImpl(metadata.getDenominator());
    }

    /**
     * Sets the value for the {@link RepresentativeFractionImpl}. This method is systematically
     * called at unmarshalling-time by JAXB.
     */
    public void setRepresentativeFraction(final RepresentativeFractionImpl fraction) {
        this.metadata = fraction;
    }
}
