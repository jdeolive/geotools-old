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
import org.geotools.metadata.iso.lineage.ProcessStepImpl;
import org.opengis.metadata.lineage.ProcessStep;


/**
 * JAXB adapter in order to map implementing class with the GeoAPI interface. See
 * package documentation for more information about JAXB and interface.
 *
 * @since 2.5
 * @source $URL$
 * @author Cédric Briançon
 */
public final class ProcessStepAdapter extends MetadataAdapter<ProcessStepAdapter,ProcessStep> {
    /**
     * Empty constructor for JAXB only.
     */
    private ProcessStepAdapter() {
    }

    /**
     * Wraps an ProcessStep value with a {@code LI_ProcessStep} tags at marshalling-time.
     *
     * @param metadata The metadata value to marshall.
     */
    protected ProcessStepAdapter(final ProcessStep metadata) {
        super(metadata);
    }

    /**
     * Returns the ProcessStep value covered by a {@code LI_ProcessStep} tags.
     *
     * @param value The value to marshall.
     * @return The adapter which covers the metadata value.
     */
    protected ProcessStepAdapter wrap(final ProcessStep value) {
        return new ProcessStepAdapter(value);
    }

    /**
     * Returns the {@link ProcessStepImpl} generated from the metadata value.
     * This method is systematically called at marshalling-time by JAXB.
     */
    @XmlElement(name = "LI_ProcessStep")
    public ProcessStepImpl getProcessStep() {
        return (metadata instanceof ProcessStepImpl) ?
            (ProcessStepImpl) metadata : new ProcessStepImpl(metadata);
    }

    /**
     * Sets the value for the {@link ProcessStepImpl}. This method is systematically
     * called at unmarshalling-time by JAXB.
     */
    public void setProcessStep(final ProcessStepImpl processStep) {
        this.metadata = processStep;
    }
}
