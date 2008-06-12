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

import javax.xml.bind.annotation.XmlElementRef;
import org.geotools.metadata.iso.constraint.ConstraintsImpl;
import org.opengis.metadata.constraint.Constraints;


/**
 * JAXB adapter in order to map implementing class with the GeoAPI interface. See
 * package documentation for more information about JAXB and interface.
 *
 * @since 2.5
 * @source $URL$
 * @author Cédric Briançon
 */
public final class ConstraintsAdapter extends MetadataAdapter<ConstraintsAdapter,Constraints> {
    /**
     * Empty constructor for JAXB only.
     */
    private ConstraintsAdapter() {
    }

    /**
     * Wraps an Constraints value with a {@code MD_Constraints} tags at marshalling-time.
     *
     * @param metadata The metadata value to marshall.
     */
    protected ConstraintsAdapter(final Constraints metadata) {
        super(metadata);
    }

    /**
     * Returns the Constraints value covered by a {@code MD_Constraints} tags.
     *
     * @param value The value to marshall.
     * @return The adapter which covers the metadata value.
     */
    protected ConstraintsAdapter wrap(final Constraints value) {
        return new ConstraintsAdapter(value);
    }

    /**
     * Returns the {@link ConstraintsImpl} generated from the metadata value.
     * This method is systematically called at marshalling-time by JAXB.
     */
    @XmlElementRef
    public ConstraintsImpl getConstraints() {
        return (metadata instanceof ConstraintsImpl) ?
            (ConstraintsImpl) metadata : new ConstraintsImpl(metadata);
    }

    /**
     * Sets the value for the {@link ConstraintsImpl}. This method is systematically
     * called at unmarshalling-time by JAXB.
     */
    public void setConstraints(final ConstraintsImpl constraints) {
        this.metadata = constraints;
    }
}
