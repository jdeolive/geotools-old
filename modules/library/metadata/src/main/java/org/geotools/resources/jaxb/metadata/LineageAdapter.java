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
import org.geotools.metadata.iso.lineage.LineageImpl;
import org.opengis.metadata.lineage.Lineage;


/**
 * JAXB adapter in order to map implementing class with the GeoAPI interface. See
 * package documentation for more information about JAXB and interface.
 *
 * @since 2.5
 * @source $URL$
 * @author Cédric Briançon
 */
public final class LineageAdapter extends MetadataAdapter<LineageAdapter,Lineage> {
    /**
     * Empty constructor for JAXB only.
     */
    private LineageAdapter() {
    }

    /**
     * Wraps an Lineage value with a {@code LI_Lineage} tags at marshalling-time.
     *
     * @param metadata The metadata value to marshall.
     */
    protected LineageAdapter(final Lineage metadata) {
        super(metadata);
    }

    /**
     * Returns the Lineage value covered by a {@code LI_Lineage} tags.
     *
     * @param value The value to marshall.
     * @return The adapter which covers the metadata value.
     */
    protected LineageAdapter wrap(final Lineage value) {
        return new LineageAdapter(value);
    }

    /**
     * Returns the {@link LineageImpl} generated from the metadata value.
     * This method is systematically called at marshalling-time by JAXB.
     */
    @XmlElement(name = "LI_Lineage")
    public LineageImpl getLineage() {
        return (metadata instanceof LineageImpl) ?
            (LineageImpl) metadata : new LineageImpl(metadata);
    }

    /**
     * Sets the value for the {@link LineageImpl}. This method is systematically
     * called at unmarshalling-time by JAXB.
     */
    public void setLineage(final LineageImpl lineage) {
        this.metadata = lineage;
    }
}
