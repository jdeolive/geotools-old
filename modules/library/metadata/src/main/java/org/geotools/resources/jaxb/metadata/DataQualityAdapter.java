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
import org.geotools.metadata.iso.quality.DataQualityImpl;
import org.opengis.metadata.quality.DataQuality;


/**
 * JAXB adapter in order to map implementing class with the GeoAPI interface. See
 * package documentation for more information about JAXB and interface.
 *
 * @since 2.5
 * @source $URL$
 * @author Cédric Briançon
 */
public final class DataQualityAdapter extends MetadataAdapter<DataQualityAdapter,DataQuality> {
    /**
     * Empty constructor for JAXB only.
     */
    private DataQualityAdapter() {
    }

    /**
     * Wraps an DataQuality value with a {@code DQ_DataQuality} tags at marshalling-time.
     *
     * @param metadata The metadata value to marshall.
     */
    protected DataQualityAdapter(final DataQuality metadata) {
        super(metadata);
    }

    /**
     * Returns the DataQuality value covered by a {@code DQ_DataQuality} tags.
     *
     * @param value The value to marshall.
     * @return The adapter which covers the metadata value.
     */
    protected DataQualityAdapter wrap(final DataQuality value) {
        return new DataQualityAdapter(value);
    }

    /**
     * Returns the {@link DataQualityImpl} generated from the metadata value.
     * This method is systematically called at marshalling-time by JAXB.
     */
    @XmlElement(name = "DQ_DataQuality")
    public DataQualityImpl getDataQuality() {
        return (metadata instanceof DataQualityImpl) ?
            (DataQualityImpl) metadata : new DataQualityImpl(metadata);
    }

    /**
     * Sets the value for the {@link DataQualityImpl}. This method is systematically
     * called at unmarshalling-time by JAXB.
     */
    public void setDataQuality(final DataQualityImpl dataQuality) {
        this.metadata = dataQuality;
    }
}
