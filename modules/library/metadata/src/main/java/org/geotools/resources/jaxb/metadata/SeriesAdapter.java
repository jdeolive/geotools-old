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
import org.geotools.metadata.iso.citation.SeriesImpl;
import org.opengis.metadata.citation.Series;


/**
 * JAXB adapter in order to map implementing class with the GeoAPI interface. See
 * package documentation for more information about JAXB and interface.
 *
 * @since 2.5
 * @source $URL$
 * @author Cédric Briançon
 */
public final class SeriesAdapter extends MetadataAdapter<SeriesAdapter,Series> {
    /**
     * Empty constructor for JAXB only.
     */
    private SeriesAdapter() {
    }

    /**
     * Wraps an Series value with a {@code CI_Series} tags at marshalling-time.
     *
     * @param metadata The metadata value to marshall.
     */
    protected SeriesAdapter(final Series metadata) {
        super(metadata);
    }

    /**
     * Returns the Series value covered by a {@code CI_Series} tags.
     *
     * @param value The value to marshall.
     * @return The adapter which covers the metadata value.
     */
    protected SeriesAdapter wrap(final Series value) {
        return new SeriesAdapter(value);
    }

    /**
     * Returns the {@link SeriesImpl} generated from the metadata value.
     * This method is systematically called at marshalling-time by JAXB.
     */
    @XmlElement(name = "CI_Series")
    public SeriesImpl getSeries() {
        return (metadata instanceof SeriesImpl) ?
            (SeriesImpl) metadata : new SeriesImpl(metadata);
    }

    /**
     * Sets the value for the {@link SeriesImpl}. This method is systematically
     * called at unmarshalling-time by JAXB.
     */
    public void setSeries(final SeriesImpl series) {
        this.metadata = series;
    }
}
