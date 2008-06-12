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
import org.geotools.metadata.iso.spatial.GeometricObjectsImpl;
import org.opengis.metadata.spatial.GeometricObjects;


/**
 * JAXB adapter in order to map implementing class with the GeoAPI interface. See
 * package documentation for more information about JAXB and interface.
 *
 * @since 2.5
 * @source $URL$
 * @author Cédric Briançon
 */
public final class GeometricObjectsAdapter
        extends MetadataAdapter<GeometricObjectsAdapter,GeometricObjects>
{
    /**
     * Empty constructor for JAXB only.
     */
    private GeometricObjectsAdapter() {
    }

    /**
     * Wraps an GeometricObjects value with a {@code MD_GeometricObjects} tags at marshalling-time.
     *
     * @param metadata The metadata value to marshall.
     */
    protected GeometricObjectsAdapter(final GeometricObjects metadata) {
        super(metadata);
    }

    /**
     * Returns the GeometricObjects value covered by a {@code MD_GeometricObjects} tags.
     *
     * @param value The value to marshall.
     * @return The adapter which covers the metadata value.
     */
    protected GeometricObjectsAdapter wrap(final GeometricObjects value) {
        return new GeometricObjectsAdapter(value);
    }

    /**
     * Returns the {@link GeometricObjectsImpl} generated from the metadata value.
     * This method is systematically called at marshalling-time by JAXB.
     */
    @XmlElement(name = "MD_GeometricObjects")
    public GeometricObjectsImpl getGeometricObjects() {
        return (metadata instanceof GeometricObjectsImpl) ?
            (GeometricObjectsImpl) metadata : new GeometricObjectsImpl(metadata);
    }

    /**
     * Sets the value for the {@link GeometricObjectsImpl}. This method is systematically
     * called at unmarshalling-time by JAXB.
     */
    public void setGeometricObjects(final GeometricObjectsImpl objects) {
        this.metadata = objects;
    }
}
