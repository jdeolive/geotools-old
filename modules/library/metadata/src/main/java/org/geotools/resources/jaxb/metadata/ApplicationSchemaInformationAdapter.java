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
import org.geotools.metadata.iso.ApplicationSchemaInformationImpl;
import org.opengis.metadata.ApplicationSchemaInformation;


/**
 * JAXB adapter in order to map implementing class with the GeoAPI interface. See
 * package documentation for more information about JAXB and interface.
 *
 * @since 2.5
 * @source $URL$
 * @author Cédric Briançon
 */
public final class ApplicationSchemaInformationAdapter
        extends MetadataAdapter<ApplicationSchemaInformationAdapter,ApplicationSchemaInformation>
{
    /**
     * Empty constructor for JAXB only.
     */
    private ApplicationSchemaInformationAdapter() {
    }

    /**
     * Wraps an ApplicationSchemaInformation value with a
     * {@code MD_ApplicationSchemaInformation} tags at marshalling-time.
     *
     * @param metadata The metadata value to marshall.
     */
    protected ApplicationSchemaInformationAdapter(final ApplicationSchemaInformation metadata) {
        super(metadata);
    }

    /**
     * Returns the ApplicationSchemaInformation value covered by a
     * {@code MD_ApplicationSchemaInformation} tags.
     *
     * @param value The value to marshall.
     * @return The adapter which covers the metadata value.
     */
    protected ApplicationSchemaInformationAdapter wrap(final ApplicationSchemaInformation value) {
        return new ApplicationSchemaInformationAdapter(value);
    }

    /**
     * Returns the {@link ApplicationSchemaInformationImpl} generated from the metadata value.
     * This method is systematically called at marshalling-time by JAXB.
     */
    @XmlElement(name = "MD_ApplicationSchemaInformation")
    public ApplicationSchemaInformationImpl getApplicationSchemaInformation() {
        return (metadata instanceof ApplicationSchemaInformationImpl) ?
            (ApplicationSchemaInformationImpl) metadata : 
            new ApplicationSchemaInformationImpl(metadata);
    }

    /**
     * Sets the value for the {@link ApplicationSchemaInformationImpl}. This method is
     * systematically called at unmarshalling-time by JAXB.
     */
    public void setApplicationSchemaInformation(
            final ApplicationSchemaInformationImpl applicationSchemaInformation) 
    {
        this.metadata = applicationSchemaInformation;
    }
}
