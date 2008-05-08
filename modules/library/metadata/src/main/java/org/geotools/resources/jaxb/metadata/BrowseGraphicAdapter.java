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
import org.geotools.metadata.iso.identification.BrowseGraphicImpl;
import org.opengis.metadata.identification.BrowseGraphic;


/**
 * JAXB adapter in order to map implementing class with the GeoAPI interface. See
 * package documentation for more information about JAXB and interface.
 *
 * @since 2.5
 * @source $URL$
 * @author Cédric Briançon
 */
public final class BrowseGraphicAdapter
        extends MetadataAdapter<BrowseGraphicAdapter,BrowseGraphic>
{
    /**
     * Empty constructor for JAXB only.
     */
    private BrowseGraphicAdapter() {
    }

    /**
     * Wraps an BrowseGraphic value with a {@code MD_BrowseGraphic} tags at marshalling-time.
     *
     * @param metadata The metadata value to marshall.
     */
    protected BrowseGraphicAdapter(final BrowseGraphic metadata) {
        super(metadata);
    }

    /**
     * Returns the BrowseGraphic value covered by a {@code MD_BrowseGraphic} tags.
     *
     * @param value The value to marshall.
     * @return The adapter which covers the metadata value.
     */
    protected BrowseGraphicAdapter wrap(final BrowseGraphic value) {
        return new BrowseGraphicAdapter(value);
    }

    /**
     * Returns the {@link BrowseGraphicImpl} generated from the metadata value.
     * This method is systematically called at marshalling-time by JAXB.
     */
    @XmlElement(name = "MD_BrowseGraphic")
    public BrowseGraphicImpl getBrowseGraphic() {
        return (metadata instanceof BrowseGraphicImpl) ?
            (BrowseGraphicImpl) metadata : new BrowseGraphicImpl(metadata);
    }

    /**
     * Sets the value for the {@link BrowseGraphicImpl}. This method is systematically
     * called at unmarshalling-time by JAXB.
     */
    public void setBrowseGraphic(final BrowseGraphicImpl browseGraphic) {
        this.metadata = browseGraphic;
    }
}
