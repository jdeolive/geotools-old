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
import org.geotools.metadata.iso.maintenance.MaintenanceInformationImpl;
import org.opengis.metadata.maintenance.MaintenanceInformation;


/**
 * JAXB adapter in order to map implementing class with the GeoAPI interface. See
 * package documentation for more information about JAXB and interface.
 *
 * @since 2.5
 * @source $URL$
 * @author Cédric Briançon
 */
public final class MaintenanceInformationAdapter
        extends MetadataAdapter<MaintenanceInformationAdapter,MaintenanceInformation>
{
    /**
     * Empty constructor for JAXB only.
     */
    private MaintenanceInformationAdapter() {
    }

    /**
     * Wraps an MaintenanceInformation value with a {@code MD_MaintenanceInformation}
     * tags at marshalling-time.
     *
     * @param metadata The metadata value to marshall.
     */
    protected MaintenanceInformationAdapter(final MaintenanceInformation metadata) {
        super(metadata);
    }

    /**
     * Returns the MaintenanceInformation value covered by a
     * {@code MD_MaintenanceInformation} tags.
     *
     * @param value The value to marshall.
     * @return The adapter which covers the metadata value.
     */
    protected MaintenanceInformationAdapter wrap(final MaintenanceInformation value) {
        return new MaintenanceInformationAdapter(value);
    }

    /**
     * Returns the {@link MaintenanceInformationImpl} generated from the metadata value.
     * This method is systematically called at marshalling-time by JAXB.
     */
    @XmlElement(name = "MD_MaintenanceInformation")
    public MaintenanceInformationImpl getMaintenanceInformation() {
        return (metadata instanceof MaintenanceInformationImpl) ?
            (MaintenanceInformationImpl) metadata : new MaintenanceInformationImpl(metadata);
    }

    /**
     * Sets the value for the {@link MaintenanceInformationImpl}. This method is systematically
     * called at unmarshalling-time by JAXB.
     */
    public void setMaintenanceInformation(final MaintenanceInformationImpl maintenance) {
        this.metadata = maintenance;
    }
}
