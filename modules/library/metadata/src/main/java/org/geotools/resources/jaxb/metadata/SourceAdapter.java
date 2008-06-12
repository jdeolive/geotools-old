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
import org.geotools.metadata.iso.lineage.SourceImpl;
import org.opengis.metadata.lineage.Source;


/**
 * JAXB adapter in order to map implementing class with the GeoAPI interface. See
 * package documentation for more information about JAXB and interface.
 *
 * @since 2.5
 * @source $URL$
 * @author Cédric Briançon
 */
public final class SourceAdapter extends MetadataAdapter<SourceAdapter,Source> {
    /**
     * Empty constructor for JAXB only.
     */
    private SourceAdapter() {
    }

    /**
     * Wraps an Source value with a {@code LI_Source} tags at marshalling-time.
     *
     * @param metadata The metadata value to marshall.
     */
    protected SourceAdapter(final Source metadata) {
        super(metadata);
    }

    /**
     * Returns the Source value covered by a {@code LI_Source} tags.
     *
     * @param value The value to marshall.
     * @return The adapter which covers the metadata value.
     */
    protected SourceAdapter wrap(final Source value) {
        return new SourceAdapter(value);
    }

    /**
     * Returns the {@link SourceImpl} generated from the metadata value.
     * This method is systematically called at marshalling-time by JAXB.
     */
    @XmlElement(name = "LI_Source")
    public SourceImpl getSource() {
        return (metadata instanceof SourceImpl) ?
            (SourceImpl) metadata : new SourceImpl(metadata);
    }

    /**
     * Sets the value for the {@link SourceImpl}. This method is systematically
     * called at unmarshalling-time by JAXB.
     */
    public void setSource(final SourceImpl source) {
        this.metadata = source;
    }
}
