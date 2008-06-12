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
import org.geotools.metadata.iso.distribution.DistributionImpl;
import org.opengis.metadata.distribution.Distribution;


/**
 * JAXB adapter in order to map implementing class with the GeoAPI interface. See
 * package documentation for more information about JAXB and interface.
 *
 * @since 2.5
 * @source $URL$
 * @author Cédric Briançon
 */
public final class DistributionAdapter extends MetadataAdapter<DistributionAdapter,Distribution> {
    /**
     * Empty constructor for JAXB only.
     */
    private DistributionAdapter() {
    }

    /**
     * Wraps an Distribution value with a {@code MD_Distribution} tags at marshalling-time.
     *
     * @param metadata The metadata value to marshall.
     */
    protected DistributionAdapter(final Distribution metadata) {
        super(metadata);
    }

    /**
     * Returns the Distribution value covered by a {@code MD_Distribution} tags.
     *
     * @param value The value to marshall.
     * @return The adapter which covers the metadata value.
     */
    protected DistributionAdapter wrap(final Distribution value) {
        return new DistributionAdapter(value);
    }

    /**
     * Returns the {@link DistributionImpl} generated from the metadata value.
     * This method is systematically called at marshalling-time by JAXB.
     */
    @XmlElement(name = "MD_Distribution")
    public DistributionImpl getDistribution() {
        return (metadata instanceof DistributionImpl) ?
            (DistributionImpl) metadata : new DistributionImpl(metadata);
    }

    /**
     * Sets the value for the {@link DistributionImpl}. This method is systematically
     * called at unmarshalling-time by JAXB.
     */
    public void setDistribution(final DistributionImpl distribution) {
        this.metadata = distribution;
    }
}
