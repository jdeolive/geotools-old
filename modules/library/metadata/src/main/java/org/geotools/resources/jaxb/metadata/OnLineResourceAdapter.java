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
import org.geotools.metadata.iso.citation.OnLineResourceImpl;
import org.opengis.metadata.citation.OnLineResource;


/**
 * JAXB adapter in order to map implementing class with the GeoAPI interface. See
 * package documentation for more information about JAXB and interface.
 *
 * @since 2.5
 * @source $URL$
 * @author Cédric Briançon
 */
public final class OnLineResourceAdapter 
        extends MetadataAdapter<OnLineResourceAdapter,OnLineResource> 
{
    /**
     * Empty constructor for JAXB only.
     */
    private OnLineResourceAdapter() {
    }

    /**
     * Wraps an OnLineResource value with a {@code CI_OnLineResource} tags at marshalling-time.
     *
     * @param metadata The metadata value to marshall.
     */
    protected OnLineResourceAdapter(final OnLineResource metadata) {
        super(metadata);
    }

    /**
     * Returns the OnLineResource value covered by a {@code CI_OnLineResource} tags.
     *
     * @param value The value to marshall.
     * @return The adapter which covers the metadata value.
     */
    protected OnLineResourceAdapter wrap(final OnLineResource value) {
        return new OnLineResourceAdapter(value);
    }

    /**
     * Returns the {@link OnLineResourceImpl} generated from the metadata value.
     * This method is systematically called at marshalling-time by JAXB.
     */
    @XmlElement(name = "CI_OnlineResource")
    public OnLineResourceImpl getOnLineResource() {
        return (metadata instanceof OnLineResourceImpl) ?
            (OnLineResourceImpl) metadata : new OnLineResourceImpl(metadata);
    }

    /**
     * Sets the value for the {@link OnLineResourceImpl}. This method is systematically
     * called at unmarshalling-time by JAXB.
     */
    public void setOnLineResource(final OnLineResourceImpl resource) {
        this.metadata = resource;
    }
}
