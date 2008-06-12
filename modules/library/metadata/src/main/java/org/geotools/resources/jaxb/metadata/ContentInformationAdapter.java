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

import javax.xml.bind.annotation.XmlElementRef;
import org.geotools.metadata.iso.content.ContentInformationImpl;
import org.opengis.metadata.content.ContentInformation;


/**
 * JAXB adapter in order to map implementing class with the GeoAPI interface. See
 * package documentation for more information about JAXB and interface.
 *
 * @since 2.5
 * @source $URL$
 * @author Cédric Briançon
 */
public final class ContentInformationAdapter
        extends MetadataAdapter<ContentInformationAdapter,ContentInformation> {
    /**
     * Empty constructor for JAXB only.
     */
    private ContentInformationAdapter() {
    }

    /**
     * Wraps an ContentInformation value with a {@code MD_ContentInformation} tags at marshalling-time.
     *
     * @param metadata The metadata value to marshall.
     */
    protected ContentInformationAdapter(final ContentInformation metadata) {
        super(metadata);
    }

    /**
     * Returns the ContentInformation value covered by a {@code MD_ContentInformation} tags.
     *
     * @param value The value to marshall.
     * @return The adapter which covers the metadata value.
     */
    protected ContentInformationAdapter wrap(final ContentInformation value) {
        return new ContentInformationAdapter(value);
    }

    /**
     * Returns the {@link ContentInformationImpl} generated from the metadata value.
     * This method is systematically called at marshalling-time by JAXB.
     */
    @XmlElementRef
    public ContentInformationImpl getContentInformation() {
        return (metadata instanceof ContentInformationImpl) ?
            (ContentInformationImpl) metadata : new ContentInformationImpl(metadata);
    }

    /**
     * Sets the value for the {@link ContentInformationImpl}. This method is systematically
     * called at unmarshalling-time by JAXB.
     */
    public void setContentInformation(final ContentInformationImpl content) {
        this.metadata = content;
    }
}
