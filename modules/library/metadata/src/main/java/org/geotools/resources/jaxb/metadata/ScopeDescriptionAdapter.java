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

import javax.xml.bind.annotation.XmlElement;
import org.geotools.metadata.iso.maintenance.ScopeDescriptionImpl;
import org.opengis.metadata.maintenance.ScopeDescription;


/**
 * JAXB adapter in order to map implementing class with the GeoAPI interface. See
 * package documentation for more information about JAXB and interface.
 *
 * @since 2.5
 * @source $URL$
 * @author Cédric Briançon
 */
public final class ScopeDescriptionAdapter extends MetadataAdapter<ScopeDescriptionAdapter,ScopeDescription> {
    /**
     * Empty constructor for JAXB only.
     */
    private ScopeDescriptionAdapter() {
    }

    /**
     * Wraps an ScopeDescription value with a {@code MD_ScopeDescription} tags at marshalling-time.
     *
     * @param metadata The metadata value to marshall.
     */
    protected ScopeDescriptionAdapter(final ScopeDescription metadata) {
        super(metadata);
    }

    /**
     * Returns the ScopeDescription value covered by a {@code MD_ScopeDescription} tags.
     *
     * @param value The value to marshall.
     * @return The adapter which covers the metadata value.
     */
    protected ScopeDescriptionAdapter wrap(final ScopeDescription value) {
        return new ScopeDescriptionAdapter(value);
    }

    /**
     * Returns the {@link ScopeDescriptionImpl} generated from the metadata value.
     * This method is systematically called at marshalling-time by JAXB.
     */
    @XmlElement(name = "MD_ScopeDescription")
    public ScopeDescriptionImpl getScopeDescription() {
        return (metadata instanceof ScopeDescriptionImpl) ?
            (ScopeDescriptionImpl) metadata : new ScopeDescriptionImpl(metadata);
    }

    /**
     * Sets the value for the {@link ScopeDescriptionImpl}. This method is systematically
     * called at unmarshalling-time by JAXB.
     */
    public void setScopeDescription(final ScopeDescriptionImpl description) {
        this.metadata = description;
    }
}
