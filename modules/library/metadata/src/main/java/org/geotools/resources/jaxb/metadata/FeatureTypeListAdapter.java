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
import org.geotools.metadata.iso.FeatureTypeListImpl;
import org.opengis.metadata.FeatureTypeList;


/**
 * JAXB adapter in order to map implementing class with the GeoAPI interface. See
 * package documentation for more information about JAXB and interface.
 *
 * @since 2.5
 * @source $URL$
 * @author Cédric Briançon
 */
public final class FeatureTypeListAdapter 
        extends MetadataAdapter<FeatureTypeListAdapter,FeatureTypeList> 
{
    /**
     * Empty constructor for JAXB only.
     */
    private FeatureTypeListAdapter() {
    }

    /**
     * Wraps an FeatureTypeList value with a {@code MD_FeatureTypeList} tags at marshalling-time.
     *
     * @param metadata The metadata value to marshall.
     */
    protected FeatureTypeListAdapter(final FeatureTypeList metadata) {
        super(metadata);
    }

    /**
     * Returns the FeatureTypeList value covered by a {@code MD_FeatureTypeList} tags.
     *
     * @param value The value to marshall.
     * @return The adapter which covers the metadata value.
     */
    protected FeatureTypeListAdapter wrap(final FeatureTypeList value) {
        return new FeatureTypeListAdapter(value);
    }

    /**
     * Returns the {@link FeatureTypeListImpl} generated from the metadata value.
     * This method is systematically called at marshalling-time by JAXB.
     */
    @XmlElement(name = "MD_FeatureTypeList")
    public FeatureTypeListImpl getFeatureTypeList() {
        return (metadata instanceof FeatureTypeListImpl) ?
            (FeatureTypeListImpl) metadata : new FeatureTypeListImpl(metadata);
    }

    /**
     * Sets the value for the {@link FeatureTypeListImpl}. This method is systematically
     * called at unmarshalling-time by JAXB.
     */
    public void setFeatureTypeList(final FeatureTypeListImpl list) {
        this.metadata = list;
    }
}
