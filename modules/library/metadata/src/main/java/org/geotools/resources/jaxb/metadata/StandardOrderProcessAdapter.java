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
import org.geotools.metadata.iso.distribution.StandardOrderProcessImpl;
import org.opengis.metadata.distribution.StandardOrderProcess;


/**
 * JAXB adapter in order to map implementing class with the GeoAPI interface. See
 * package documentation for more information about JAXB and interface.
 *
 * @since 2.5
 * @source $URL$
 * @author Cédric Briançon
 */
public final class StandardOrderProcessAdapter
        extends MetadataAdapter<StandardOrderProcessAdapter,StandardOrderProcess>
{
    /**
     * Empty constructor for JAXB only.
     */
    private StandardOrderProcessAdapter() {
    }

    /**
     * Wraps an StandardOrderProcess value with a {@code MD_StandardOrderProcess} tags at marshalling-time.
     *
     * @param metadata The metadata value to marshall.
     */
    protected StandardOrderProcessAdapter(final StandardOrderProcess metadata) {
        super(metadata);
    }

    /**
     * Returns the StandardOrderProcess value covered by a {@code MD_StandardOrderProcess} tags.
     *
     * @param value The value to marshall.
     * @return The adapter which covers the metadata value.
     */
    protected StandardOrderProcessAdapter wrap(final StandardOrderProcess value) {
        return new StandardOrderProcessAdapter(value);
    }

    /**
     * Returns the {@link StandardOrderProcessImpl} generated from the metadata value.
     * This method is systematically called at marshalling-time by JAXB.
     */
    @XmlElement(name = "MD_StandardOrderProcess")
    public StandardOrderProcessImpl getStandardOrder() {
        return (metadata instanceof StandardOrderProcessImpl) ?
            (StandardOrderProcessImpl) metadata : new StandardOrderProcessImpl(metadata);
    }

    /**
     * Sets the value for the {@link StandardOrderProcessImpl}. This method is systematically
     * called at unmarshalling-time by JAXB.
     */
    public void setStandardOrderProcess(final StandardOrderProcessImpl order) {
        this.metadata = order;
    }
}
